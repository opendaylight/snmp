/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.snmp.plugin.internal;

import com.google.common.util.concurrent.SettableFuture;
import java.io.IOException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpSetInput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

public class AsyncSetHandler implements ResponseListener {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncSetHandler.class);
    private SettableFuture<RpcResult<Void>> rpcResultSettableFuture;
    private final Snmp snmp4j;
    private final SnmpSetInput snmpSetInput;
    private final Target target;
    private final OID oid;
    private PDU pdu;


    public AsyncSetHandler(SnmpSetInput input, Snmp snmp) {
        snmp4j = snmp;
        snmpSetInput = input;

        String community = input.getCommunity();
        if (community == null) {
            community = SNMPImpl.DEFAULT_COMMUNITY;
        }

        target = SNMPImpl.getTargetForIp(input.getIpAddress(), community);
        oid  = new OID(input.getOid());
        pdu = new PDU();
        pdu.add(new VariableBinding(oid, new OctetString(input.getValue())));
        pdu.setType(PDU.SET);
    }

    private void sendSnmpSet() {
        try {
            snmp4j.set(pdu, target, null, this);
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.failed();
            rpcResultBuilder.withError(RpcError.ErrorType.APPLICATION, e.getMessage());
            rpcResultSettableFuture.set(rpcResultBuilder.build());
        }
    }

    public SettableFuture<RpcResult<Void>> getRpcResponse() {
        rpcResultSettableFuture = SettableFuture.create();
        sendSnmpSet();
        return rpcResultSettableFuture;
    }

    private static PDU getNextPDUType(PDU pdu, String value, OID oid) throws SetTypeException {
        Variable variable = pdu.getVariable(oid);
        VariableBinding variableBinding = null;

        if (variable instanceof OctetString) {

            try {
                Integer intValue = Integer.parseInt(value);
                variableBinding = new VariableBinding(oid, new Integer32(intValue));
            } catch (NumberFormatException e) {
                throw new SetTypeException("Unknown set type " + value, e);
            }
        } else if (variable instanceof Integer32) {

            // It's already an Int32, set the next type to an unsignedInt32
            Long intValue = Long.parseLong(value);
            variableBinding = new VariableBinding(oid, new UnsignedInteger32(intValue));

        } else if (variable instanceof UnsignedInteger32) {
            Long intValue = Long.parseLong(value);
            variableBinding = new VariableBinding(oid, new Counter64(intValue));

        } else if (variable instanceof Counter64) {
            // We're out of options to set it to
            throw new SetTypeException("Unknown Set type");
        }

        pdu.clear();
        pdu.add(variableBinding);

        return pdu;
    }

    @Override
    public void onResponse(ResponseEvent responseEvent) {
        // JavaDocs state not doing the following will cause a leak
        ((Snmp)responseEvent.getSource()).cancel(responseEvent.getRequest(), this);

        RpcResultBuilder<Void> rpcResultBuilder;
        PDU responseEventPDU = responseEvent.getResponse();
        if (responseEventPDU != null) {
            int errorStatus = responseEventPDU.getErrorStatus();
            if (errorStatus != PDU.noError) {
                // SET wasn't successful!

                if (errorStatus == PDU.wrongType) {
                    // Try again with a different type

                    try {

                        pdu = getNextPDUType(pdu, snmpSetInput.getValue(), oid);
                        sendSnmpSet();
                        return;

                    } catch (SetTypeException e) {

                        // We're out of data types to try.
                        // Return an error and set the future.

                        rpcResultBuilder = RpcResultBuilder.failed();
                        rpcResultBuilder.withError(RpcError.ErrorType.APPLICATION,
                                "SnmpSET failed. Unknown object set type");
                    }

                } else {

                    int errorIndex = responseEventPDU.getErrorIndex();
                    String errorString = responseEventPDU.getErrorStatusText();

                    rpcResultBuilder = RpcResultBuilder.failed();
                    rpcResultBuilder.withError(RpcError.ErrorType.APPLICATION,
                            String.format("SnmpSET failed with error status: %s, error index: %s. StatusText: %s",
                                    errorStatus, errorIndex, errorString));
                }
            } else {
                rpcResultBuilder = RpcResultBuilder.success();
            }

        } else {
            // Response Event PDU was null
            rpcResultBuilder = RpcResultBuilder.failed();
            rpcResultBuilder.withError(RpcError.ErrorType.APPLICATION,
                    "SNMP set timed out.");
        }

        rpcResultSettableFuture.set(rpcResultBuilder.build());
    }
}
