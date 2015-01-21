/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.snmp.plugin.internal;

import com.google.common.util.concurrent.Futures;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.GetInterfacesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.GetInterfacesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.GetInterfacesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SendSnmpQueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SendSnmpQueryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SendSnmpQueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SendSnmpQueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpQueryType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.send.snmp.query.output.Results;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.send.snmp.query.output.ResultsBuilder;
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

public class SNMPImpl implements SnmpService{
    private static final Logger LOG = LoggerFactory.getLogger(SNMPImpl.class);
    private Snmp snmp;
    TransportMapping transport;

    public SNMPImpl() {
    	LOG.info("SNMPImpl started");
        try {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            // Do not forget this line!
            snmp.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Target getTargetForIp(Ipv4Address address, String community) {
        Address addr = null;
        try {
            addr = new UdpAddress(Inet4Address.getByName(address.getValue()), 161);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        CommunityTarget communityTarget = new CommunityTarget();
        communityTarget.setCommunity(new OctetString(community));
        communityTarget.setAddress(addr);
        communityTarget.setRetries(1);
        communityTarget.setTimeout(500);
        communityTarget.setVersion(SnmpConstants.version2c);
        return communityTarget;
    }

    ArrayList<VariableBinding> sendQuery(SendSnmpQueryInput input) {

        PDU pdu = new PDU();
        OID oid = new OID(input.getOid());
        pdu.add(new VariableBinding(oid));
        pdu.setMaxRepetitions(10000);
        pdu.setNonRepeaters(0);
        ArrayList<VariableBinding> variableBindings = new ArrayList<>();

        String community = input.getCommunity();
        if (community == null) community = "public";

        Target target = getTargetForIp(input.getIpAddress(), community);


        if (input.getQueryType().equals(SnmpQueryType.GET)) {
            pdu.setType(PDU.GET);
        } else if (input.getQueryType().equals(SnmpQueryType.GETNEXT)) {
            pdu.setType(PDU.GETNEXT);
        } else if (input.getQueryType().equals(SnmpQueryType.GETBULK)) {
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
                    if (!input.getQueryType().equals(SnmpQueryType.GETBULK)) {
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

        SendSnmpQueryInputBuilder sendSnmpQueryInputBuilder = new SendSnmpQueryInputBuilder()
                .setIpAddress(address)
                .setCommunity(community)
                .setQueryType(SnmpQueryType.GETBULK);

        ArrayList<Thread> threads = new ArrayList<>();

        // Iterate through all of the fields on the builder class, getting the objects, and adding them into the map
        for (Method method : builderClass.getMethods()) {
            PopulateMibThread populateMibThread = new PopulateMibThread(method, indexToBuilderObject, sendSnmpQueryInputBuilder, builderClass, this);
            threads.add(populateMibThread);
            populateMibThread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return new ArrayList<>(indexToBuilderObject.values());
    }

    @Override
    public Future<RpcResult<SendSnmpQueryOutput>> sendSnmpQuery(SendSnmpQueryInput input) {
        LOG.info("Sending " + input.getQueryType() + " SNMP request for host: " + input.getIpAddress() + " for OID: " + input.getOid() + " Community: " + input.getCommunity());

        ArrayList<Results> resultsArrayList = new ArrayList<>();

        SendSnmpQueryOutputBuilder sendSnmpQueryOutputBuilder = new SendSnmpQueryOutputBuilder();

        ArrayList<VariableBinding> variableBindings = sendQuery(input);
        for (VariableBinding variableBinding : variableBindings) {
            ResultsBuilder resultsBuilder = new ResultsBuilder();

            String oid = variableBinding.getOid().toString();
            String val = variableBinding.getVariable().toString();

            resultsBuilder.setOid(oid).setValue(val);
            resultsArrayList.add(resultsBuilder.build());
        }

        sendSnmpQueryOutputBuilder.setResults(resultsArrayList);
        RpcResultBuilder<SendSnmpQueryOutput> rpcResultBuilder = RpcResultBuilder.success();
        rpcResultBuilder.withResult(sendSnmpQueryOutputBuilder.build());
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

}
