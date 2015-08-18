/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.snmp.plugin.internal;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.SettableFuture;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.GetInterfacesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.GetInterfacesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.GetInterfacesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpSetInput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPImpl implements SnmpService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SNMPImpl.class);
    static final String DEFAULT_COMMUNITY = "public";
    private Snmp snmp;
    static final Integer SNMP_LISTEN_PORT = 161;
    static final int RETRIES = 1;
    static final int TIMEOUT = 500;
    static final int MAXREPETITIONS = 10000;

    private final RpcProviderRegistry rpcProviderRegistery;
    private final BindingAwareBroker.RpcRegistration<SnmpService> rpcRegistration;

    public SNMPImpl(RpcProviderRegistry rpcProviderRegistery) {
        this(rpcProviderRegistery, initSnmp());
    }

    SNMPImpl(RpcProviderRegistry rpcProviderRegistery, Snmp snmp) {
        this.rpcProviderRegistery = Preconditions.checkNotNull(rpcProviderRegistery);
        this.snmp = Preconditions.checkNotNull(snmp);
        this.rpcRegistration = this.rpcProviderRegistery.addRpcImplementation(SnmpService.class, this);
    }

    private static Snmp initSnmp() {
        Snmp snmp = null;
        try {
            snmp = new Snmp( new DefaultUdpTransportMapping());
            snmp.listen();
        } catch (IOException e) {
            LOG.warn("Failed to create Snmp instance", e);
        }
        return snmp;
    }

    static Target getTargetForIp(Ipv4Address address, String community) {
        Address addr = null;
        try {
            addr = new UdpAddress(Inet4Address.getByName(address.getValue()), SNMP_LISTEN_PORT);
        } catch (UnknownHostException e) {
            LOG.warn("Failed to create UDP Address", e);
            return null;
        }

        CommunityTarget communityTarget = new CommunityTarget();
        communityTarget.setCommunity(new OctetString(community));
        communityTarget.setAddress(addr);
        communityTarget.setRetries(RETRIES);
        communityTarget.setTimeout(TIMEOUT);
        communityTarget.setVersion(SnmpConstants.version2c);
        return communityTarget;
    }

    @Override
    public Future<RpcResult<SnmpGetOutput>> snmpGet(SnmpGetInput input) {
        LOG.info("Sending " + input.getGetType() + " SNMP request for host: " + input.getIpAddress() + " for OID: " + input.getOid() + " Community: " + input.getCommunity());
        AsyncGetHandler getHandler = new AsyncGetHandler(input, snmp);
        return getHandler.getRpcResponse();
    }

    @Override
    public Future<RpcResult<Void>> snmpSet(SnmpSetInput input) {
        AsyncSetHandler setHandler = new AsyncSetHandler(input, snmp);
        return setHandler.getRpcResponse();
    }

    @Override
    public Future<RpcResult<GetInterfacesOutput>> getInterfaces(final GetInterfacesInput getInterfacesInput) {

        final SettableFuture<RpcResult<GetInterfacesOutput>> settableFuture = SettableFuture.create();

        Runnable nonBlockingPopulateRunnable = new Runnable() {
            @Override
            public void run() {
                MibTable<IfEntryBuilder> ifEntryBuilderMibTable = new MibTable<>(snmp, getInterfacesInput.getIpAddress(), getInterfacesInput.getCommunity(), IfEntryBuilder.class);

                GetInterfacesOutputBuilder getInterfacesOutputBuilder = new GetInterfacesOutputBuilder();

                Map<Integer, IfEntryBuilder> ifEntryBuilders = ifEntryBuilderMibTable.populate();

                List<IfEntry> ifEntries = new ArrayList<>(ifEntryBuilders.size());
                for (Integer index : ifEntryBuilders.keySet()) {
                    IfEntryBuilder ifEntryBuilder = ifEntryBuilders.get(index);
                    ifEntries.add(ifEntryBuilder.build());
                }

                getInterfacesOutputBuilder.setIfEntry(ifEntries)
                        .setIfNumber(ifEntries.size());

                RpcResultBuilder<GetInterfacesOutput> rpcResultBuilder = RpcResultBuilder.success(getInterfacesOutputBuilder.build());

                settableFuture.set(rpcResultBuilder.build());
            }
        };

        Thread nonBlockingPopulate = new Thread(nonBlockingPopulateRunnable);
        nonBlockingPopulate.start();

        return settableFuture;
    }

    @Override
    public void close() throws IOException {
        if(rpcRegistration != null) {
            rpcRegistration.close();
        }
        if (snmp != null) {
            snmp.close();
            snmp = null;
        }
    }

}
