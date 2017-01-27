/*
 * Copyright © 2016 Cisco Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.snmp.plugin.internal;


import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SnmpProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpProvider.class);


    private final RpcProviderRegistry rpcProviderRegistry;

    public SnmpProvider(RpcProviderRegistry rpcProviderRegistry) {
        this.rpcProviderRegistry = rpcProviderRegistry;
    }


    public void init() {

        LOG.info("Snmp blueprint session initialized");

        new SNMPImpl(rpcProviderRegistry);
    }

    public void close() {

    }
}
