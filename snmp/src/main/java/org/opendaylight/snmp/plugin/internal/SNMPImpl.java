/*
 * Copyright (c) 2014-2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.snmp.plugin.internal;

import com.google.common.util.concurrent.Futures;
import com.google.common.base.Preconditions;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.GetInterfacesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.GetInterfacesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.GetInterfacesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpSetInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.snmp.get.output.Results;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.snmp.get.output.ResultsBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;

public class SNMPImpl implements SnmpService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SNMPImpl.class);
    private Snmp snmp;
    private static final Integer snmpListenPort = 161;
    TransportMapping transport;
    
    private final RpcProviderRegistry rpcProviderRegistery;
    private final BindingAwareBroker.RpcRegistration<SnmpService> rpcRegistration;

    public SNMPImpl(RpcProviderRegistry rpcProviderRegistery) {
    	LOG.debug("SNMPImpl constructor");
    	this.rpcProviderRegistery = Preconditions.checkNotNull(rpcProviderRegistery);
    	
        try {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            // Do not forget this line!
            snmp.listen();
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        }
        
        // register this class as teh RPC service
        this.rpcRegistration = this.rpcProviderRegistery.addRpcImplementation(SnmpService.class, this);
    }

    private Target getTargetForIp(Ipv4Address address, String community) {
        Address addr = null;
        try {
            addr = new UdpAddress(Inet4Address.getByName(address.getValue()), snmpListenPort);
        } catch (UnknownHostException e) {
            LOG.warn(e.getMessage());
        }

        CommunityTarget communityTarget = new CommunityTarget();
        communityTarget.setCommunity(new OctetString(community));
        communityTarget.setAddress(addr);
        communityTarget.setRetries(1);
        communityTarget.setTimeout(500);
        communityTarget.setVersion(SnmpConstants.version2c);
        return communityTarget;
    }

    ArrayList<VariableBinding> sendQuery(SnmpGetInput input) {

        PDU pdu = new PDU();
        OID oid = new OID(input.getOid());
        pdu.add(new VariableBinding(oid));
        pdu.setMaxRepetitions(10000);
        pdu.setNonRepeaters(0);
        ArrayList<VariableBinding> variableBindings = new ArrayList<>();

        String community = input.getCommunity();
        if (community == null) community = "public";

        Target target = getTargetForIp(input.getIpAddress(), community);


        if (input.getGetType().equals(SnmpGetType.GET)) {
            pdu.setType(PDU.GET);
        } else if (input.getGetType().equals(SnmpGetType.GETNEXT)) {
            pdu.setType(PDU.GETNEXT);
        } else if (input.getGetType().equals(SnmpGetType.GETBULK)) {
            pdu.setType(PDU.GETBULK);
        }

        try {
            boolean stop = false;
            while (!stop) {
                //  LOG.info("Sending Query");
                ResponseEvent responseEvent = snmp.send(pdu, target);
                PDU response = responseEvent.getResponse();
                VariableBinding binding = null;
                if (response != null) {
                    Vector<? extends VariableBinding> vector = response.getVariableBindings();

                    for (int i=0; i<vector.size(); i++) {
                        binding = vector.get(i);
                        if (binding.getOid() == null ||
                                binding.getOid().size() < oid.size() ||
                                oid.leftMostCompare(oid.size(), binding.getOid()) != 0 ||
                                binding.getOid().compareTo(oid) < 0) {

                            stop = true;
                            break;
                        } else {
                            variableBindings.add(binding);
                        }
                    }
                    if (!input.getGetType().equals(SnmpGetType.GETBULK)) {
                        stop = true;
                    }
                    if (response.getErrorStatus() != PDU.noError) {
                        LOG.info("Error: " + response.getErrorStatusText());
                        stop = true;
                    }
                } else {
                    stop = true;
                }

                if (!stop) {
                    pdu.setRequestID(new Integer32(0));
                    pdu.set(0, binding);
                }

            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }

        return variableBindings;
    }

    public <T> ArrayList<T> populateMibTable(Ipv4Address address, Class<T> builderClass) {
        return populateMibTable(address, builderClass, "public");
    }

    public <T> ArrayList<T> populateMibTable(Ipv4Address address, Class<T> builderClass, String community) {

        ConcurrentHashMap<Integer, T> indexToBuilderObject = new ConcurrentHashMap<>();

        SnmpGetInputBuilder getInputBuilder = new SnmpGetInputBuilder()
                .setIpAddress(address)
                .setCommunity(community)
                .setGetType(SnmpGetType.GETBULK);

        ArrayList<Thread> threads = new ArrayList<>();

        // Iterate through all of the fields on the builder class, getting the objects, and adding them into the map
        for (Method method : builderClass.getMethods()) {
            PopulateMibThread populateMibThread = new PopulateMibThread(method, indexToBuilderObject, getInputBuilder, builderClass, this);
            threads.add(populateMibThread);
            populateMibThread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LOG.warn(e.getMessage());
            }
        }

        return new ArrayList<>(indexToBuilderObject.values());
    }

    @Override
    public Future<RpcResult<SnmpGetOutput>> snmpGet(SnmpGetInput input) {
        LOG.info("Sending " + input.getGetType() + " SNMP request for host: " + input.getIpAddress() + " for OID: " + input.getOid() + " Community: " + input.getCommunity());

        ArrayList<Results> resultsArrayList = new ArrayList<>();

        SnmpGetOutputBuilder getOutputBuilder = new SnmpGetOutputBuilder();

        ArrayList<VariableBinding> variableBindings = sendQuery(input);
        for (VariableBinding variableBinding : variableBindings) {
            ResultsBuilder resultsBuilder = new ResultsBuilder();

            String oid = variableBinding.getOid().toString();
            String val = variableBinding.getVariable().toString();

            resultsBuilder.setOid(oid).setValue(val);
            resultsArrayList.add(resultsBuilder.build());
        }

        getOutputBuilder.setResults(resultsArrayList);
        RpcResultBuilder<SnmpGetOutput> rpcResultBuilder = RpcResultBuilder.success();
        rpcResultBuilder.withResult(getOutputBuilder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> snmpSet(SnmpSetInput input) {
        RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.success();
        PDU pdu = new PDU();
        OID oid = new OID(input.getOid());
        Variable variable = new OctetString(input.getValue());
        pdu.add(new VariableBinding(oid, variable));
        pdu.setType(PDU.SET);


        String community = input.getCommunity();
        if (community == null) {
            community = "public";
        }

        Target target = getTargetForIp(input.getIpAddress(), community);

        try {
            ResponseEvent responseEvent = snmp.set(pdu, target);
            if (responseEvent != null) {
                PDU responseEventPDU = responseEvent.getResponse();
                if (responseEventPDU != null) {
                    int errorStatus = responseEventPDU.getErrorStatus();
                    if (errorStatus != PDU.noError) {
                        // SET wasn't successfull!

                        int errorIndex = responseEventPDU.getErrorIndex();
                        String errorString = responseEventPDU.getErrorStatusText();

                        rpcResultBuilder = RpcResultBuilder.failed();
                        rpcResultBuilder.withError(RpcError.ErrorType.APPLICATION,
                                String.format("SnmpSET failed with error status: %s, error index: %s. StatusText: %s",
                                        errorStatus, errorIndex, errorString));
                    }
                } else {
                    // Response Event PDU was null
                    rpcResultBuilder = RpcResultBuilder.failed();
                    rpcResultBuilder.withError(RpcError.ErrorType.APPLICATION,
                            "Response PDU is null.");
                }
            } else {
                // Response Event was null
                rpcResultBuilder = RpcResultBuilder.failed();
                rpcResultBuilder.withError(RpcError.ErrorType.APPLICATION,
                        "Response timed out.");
            }
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            rpcResultBuilder = RpcResultBuilder.failed();
            rpcResultBuilder.withError(RpcError.ErrorType.APPLICATION, e.getMessage());
        }

        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<GetInterfacesOutput>> getInterfaces(GetInterfacesInput getInterfacesInput) {

        GetInterfacesOutputBuilder getInterfacesOutputBuilder = new GetInterfacesOutputBuilder();
        ArrayList<IfEntryBuilder> ifEntryBuilders = populateMibTable(getInterfacesInput.getIpAddress(), IfEntryBuilder.class, getInterfacesInput.getCommunity());

        ArrayList<IfEntry> ifEntries = new ArrayList<>();
        for (IfEntryBuilder ifEntryBuilder : ifEntryBuilders) {
            ifEntries.add(ifEntryBuilder.build());
        }

        getInterfacesOutputBuilder.setIfEntry(ifEntries)
                .setIfNumber(ifEntries.size());

        RpcResultBuilder<GetInterfacesOutput> rpcResultBuilder = RpcResultBuilder.success(getInterfacesOutputBuilder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

	@Override
	public void close() throws Exception {
		if(rpcRegistration != null) {
			rpcRegistration.close();
		}
		if (snmp != null) {
			snmp.close();
		}
	}

}
