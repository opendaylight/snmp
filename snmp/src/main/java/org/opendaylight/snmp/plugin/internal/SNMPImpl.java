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

import com.google.common.util.concurrent.SettableFuture;
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
import org.snmp4j.event.ResponseListener;
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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;

public class SNMPImpl implements SnmpService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SNMPImpl.class);
    private static final String DEFAULT_COMMUNITY = "public";
    private Snmp snmp;
    private static final Integer snmpListenPort = 161;
    TransportMapping transport;
    
    private final RpcProviderRegistry rpcProviderRegistery;
    private final BindingAwareBroker.RpcRegistration<SnmpService> rpcRegistration;

    /*
     * Constructor
     */
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
    
    /*
     * Constructor for unit test
     */
    public SNMPImpl(RpcProviderRegistry rpcProviderRegistery, Snmp snmp) {
    	LOG.debug("SNMPImpl unit test constructor");
    	this.rpcProviderRegistery = Preconditions.checkNotNull(rpcProviderRegistery);
    	this.snmp = snmp;
        // register this class as teh RPC service
        this.rpcRegistration = this.rpcProviderRegistery.addRpcImplementation(SnmpService.class, this);
    }
    
    /* 
     * Used in unit testing
     */
    protected Snmp getSnmp() {
    	return snmp;
    }
    /*
     * Used in unit testing
     */
    protected BindingAwareBroker.RpcRegistration<SnmpService> getRpcRegistration() {
    	return rpcRegistration;
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

    List<VariableBinding> sendQuery(SnmpGetInput input) {

        PDU pdu = new PDU();
        OID oid = new OID(input.getOid());
        pdu.add(new VariableBinding(oid));
        pdu.setMaxRepetitions(10000);
        pdu.setNonRepeaters(0);
        ArrayList<VariableBinding> variableBindings = new ArrayList<>();

        String community = input.getCommunity();
        if (community == null) community = DEFAULT_COMMUNITY;

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

                if (!stop && (lastBinding != null)) {
                    pdu.setRequestID(new Integer32(0));
                    pdu.set(0, lastBinding);
                }

            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }

        return variableBindings;
    }

    public <T> Collection<T> populateMibTable(Ipv4Address address, Class<T> builderClass) {
        return populateMibTable(address, builderClass, DEFAULT_COMMUNITY);
    }

    public <T> Collection<T> populateMibTable(Ipv4Address address, Class<T> builderClass, String community) {

        ConcurrentHashMap<Integer, T> indexToBuilderObject = new ConcurrentHashMap<>();

        SnmpGetInputBuilder getInputBuilder = new SnmpGetInputBuilder()
                .setIpAddress(address)
                .setCommunity(community)
                .setGetType(SnmpGetType.GETBULK);

        Method[] methods = builderClass.getMethods();
        ArrayList<Thread> threads = new ArrayList<>(methods.length);

        // Iterate through all of the fields on the builder class, getting the objects, and adding them into the map
        for (Method method : methods) {
            PopulateMibThread populateMibThread = new PopulateMibThread<>(method, indexToBuilderObject, getInputBuilder, builderClass, this);
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

        return indexToBuilderObject.values();
    }

    @Override
    public Future<RpcResult<SnmpGetOutput>> snmpGet(SnmpGetInput input) {
        LOG.info("Sending " + input.getGetType() + " SNMP request for host: " + input.getIpAddress() + " for OID: " + input.getOid() + " Community: " + input.getCommunity());

        List<VariableBinding> variableBindings = sendQuery(input);
        ArrayList<Results> resultsArrayList = new ArrayList<>(variableBindings.size());

        for (VariableBinding variableBinding : variableBindings) {
            ResultsBuilder resultsBuilder = new ResultsBuilder();

            String oid = variableBinding.getOid().toString();
            String val = variableBinding.getVariable().toString();

            resultsBuilder.setOid(oid).setValue(val);
            resultsArrayList.add(resultsBuilder.build());
        }

        SnmpGetOutputBuilder getOutputBuilder = new SnmpGetOutputBuilder();
        getOutputBuilder.setResults(resultsArrayList);

        RpcResultBuilder<SnmpGetOutput> rpcResultBuilder = RpcResultBuilder.success();
        rpcResultBuilder.withResult(getOutputBuilder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> snmpSet(SnmpSetInput input) {
        final SettableFuture<RpcResult<Void>> future = SettableFuture.create();

        PDU pdu = new PDU();
        OID oid = new OID(input.getOid());
        Variable variable = new OctetString(input.getValue());
        pdu.add(new VariableBinding(oid, variable));
        pdu.setType(PDU.SET);

        String community = input.getCommunity();
        if (community == null) {
            community = DEFAULT_COMMUNITY;
        }

        Target target = getTargetForIp(input.getIpAddress(), community);

        try {
            snmp.set(pdu, target, null, new ResponseListener() {
                @Override
                public void onResponse(ResponseEvent responseEvent) {
                    // JavaDocs state not doing the following will cause a leak
                	String sourceName = (responseEvent.getSource()).getClass().getName();
                    if (responseEvent.getSource() != null && !sourceName.equals("java.lang.Object")) {
                	    ((Snmp)responseEvent.getSource()).cancel(responseEvent.getRequest(), this);
                	}

                    RpcResultBuilder<Void> rpcResultBuilder;
                    PDU responseEventPDU = responseEvent.getResponse();
                    if (responseEventPDU != null) {
                        int errorStatus = responseEventPDU.getErrorStatus();
                        if (errorStatus != PDU.noError) {
                            // SET wasn't successful!

                            int errorIndex = responseEventPDU.getErrorIndex();
                            String errorString = responseEventPDU.getErrorStatusText();

                            rpcResultBuilder = RpcResultBuilder.failed();
                            rpcResultBuilder.withError(RpcError.ErrorType.APPLICATION,
                                    String.format("SnmpSET failed with error status: %s, error index: %s. StatusText: %s",
                                            errorStatus, errorIndex, errorString));
                        }
                        else {
                            rpcResultBuilder = RpcResultBuilder.success();
                        }

                    } else {
                        // Response Event PDU was null
                        rpcResultBuilder = RpcResultBuilder.failed();
                        rpcResultBuilder.withError(RpcError.ErrorType.APPLICATION,
                                "SNMP set timed out.");
                    }

                    future.set(rpcResultBuilder.build());
                }
            });
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.failed();
            rpcResultBuilder.withError(RpcError.ErrorType.APPLICATION, e.getMessage());
            future.set(rpcResultBuilder.build());
        }

        return future;
    }

    @Override
    public Future<RpcResult<GetInterfacesOutput>> getInterfaces(GetInterfacesInput getInterfacesInput) {

        GetInterfacesOutputBuilder getInterfacesOutputBuilder = new GetInterfacesOutputBuilder();
        Collection<IfEntryBuilder> ifEntryBuilders = populateMibTable(getInterfacesInput.getIpAddress(), IfEntryBuilder.class, getInterfacesInput.getCommunity());

        ArrayList<IfEntry> ifEntries = new ArrayList<>(ifEntryBuilders.size());
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
            snmp = null;
		}
	}

}
