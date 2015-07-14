/*
 * Copyright (c) 2014-2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.snmp.plugin.internal;

import com.google.common.util.concurrent.SettableFuture;

import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.snmp.get.output.Results;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.snmp.get.output.ResultsBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.opendaylight.snmp.plugin.internal.SNMPImpl.DEFAULT_COMMUNITY;
import static org.opendaylight.snmp.plugin.internal.SNMPImpl.MAXREPETITIONS;


public class AsyncGetHandler implements ResponseListener {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncGetHandler.class);

    private SnmpGetInput snmpGetInput;
    private SettableFuture<RpcResult<SnmpGetOutput>> rpcSettableFuture;
    private SettableFuture<List<VariableBinding>> listSettableFuture;
    private final List<VariableBinding> variableBindings = new ArrayList<>();
    private final List<Results> resultsArrayList = new ArrayList<>();
    private Target target;
    private PDU pdu;
    private OID oid;
    private Snmp snmp;

    public AsyncGetHandler(SnmpGetInput getInput, Snmp snmp) {
        snmpGetInput = getInput;
        this.snmp = snmp;
        pdu = new PDU();
        oid = new OID(snmpGetInput.getOid());
        pdu.add(new VariableBinding(oid));
        pdu.setMaxRepetitions(MAXREPETITIONS);
        pdu.setNonRepeaters(0);

        String community = getInput.getCommunity();
        if (community == null) {
            community = DEFAULT_COMMUNITY;
        }

        target = SNMPImpl.getTargetForIp(getInput.getIpAddress(), community);
        if (snmpGetInput.getGetType().equals(SnmpGetType.GET)) {
            pdu.setType(PDU.GET);
        } else if (snmpGetInput.getGetType().equals(SnmpGetType.GETNEXT)) {
            pdu.setType(PDU.GETNEXT);
        } else {
            pdu.setType(PDU.GETBULK);
        }
    }

    @Override
    public void onResponse(ResponseEvent responseEvent) {
        try {
            // acknowledge receipt of the event
            Object source = responseEvent.getSource();
            ((Snmp)source).cancel(responseEvent.getRequest(), this);

            boolean stop = false;
            PDU response = responseEvent.getResponse();
            VariableBinding lastBinding = null;
            if (response != null) {
                for (VariableBinding binding : response.getVariableBindings()) {
                    lastBinding = binding;
                    if (binding.getOid() == null ||
                            binding.getOid().size() < oid.size() ||
                            oid.leftMostCompare(oid.size(), binding.getOid()) != 0 ||
                            binding.getOid().compareTo(oid) < 0) {

                        stop = true;
                        break;
                    } else {
                        storeResult(binding);
                    }
                }
                if (!snmpGetInput.getGetType().equals(SnmpGetType.GETWALK)) {
                    stop = true;
                }
                if (response.getErrorStatus() != PDU.noError) {
                    LOG.error("Error: " + response.getErrorStatusText());
                    stop = true;
                }
            } else {
                throw new TimeoutException("Stopped due to timeout; results will be incomplete. Request: " + responseEvent.getRequest());
            }

            if (!stop && (lastBinding != null)) {
                pdu.setRequestID(new Integer32(0));
                pdu.set(0, lastBinding);
                sendRequest();
            } else {
                setResult(null);
            }
        } catch (Throwable e) {
            setResult(e);
        }
    }

    private void storeResult(VariableBinding variableBinding) {
        variableBindings.add(variableBinding);

        ResultsBuilder resultsBuilder = new ResultsBuilder();

        String oidString = variableBinding.getOid().toString();
        String val = variableBinding.getVariable().toString();

        resultsBuilder.setOid(oidString).setValue(val);
        resultsArrayList.add(resultsBuilder.build());
    }

    private void setResult(Throwable e) {
        boolean success = (e == null);
        LOG.info("Setting result, success=" + success);

        SnmpGetOutputBuilder getOutputBuilder = new SnmpGetOutputBuilder().setResults(resultsArrayList);

        RpcResultBuilder<SnmpGetOutput> rpcResultBuilder = RpcResultBuilder.status(success);
        rpcResultBuilder.withResult(getOutputBuilder.build());
        if (e != null) {
            rpcResultBuilder.withError(getErrorType(e), e.getClass().getSimpleName() + " - see error logs for details");
            listSettableFuture.setException(e);
        }
        rpcSettableFuture.set(rpcResultBuilder.build());
        listSettableFuture.set(variableBindings);
    }

    private ErrorType getErrorType(Throwable e) {
        if (e instanceof TimeoutException || e instanceof IOException ) {
            return ErrorType.TRANSPORT;
        }
        return ErrorType.APPLICATION;
    }

    private void sendRequest() throws IOException {
        snmp.send(pdu, target, null, this);
    }

    public SettableFuture<RpcResult<SnmpGetOutput>> getRpcResponse() {
        rpcSettableFuture = SettableFuture.create();
        listSettableFuture = SettableFuture.create();

        try {
            sendRequest();
        } catch (IOException e) {
            LOG.warn("Exception when sending GET request", e);
            RpcResultBuilder<SnmpGetOutput> errorOutput = RpcResultBuilder.failed();
            errorOutput.withError(RpcError.ErrorType.APPLICATION, "IOException when sending GET request");
            rpcSettableFuture.set(errorOutput.build());
        }

        return rpcSettableFuture;
    }

    public SettableFuture<List<VariableBinding>> getListResponse() {
        getRpcResponse();
        return listSettableFuture;
    }
}
