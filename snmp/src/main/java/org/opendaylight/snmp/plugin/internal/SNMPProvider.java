/*
 * Copyright (c) 2014-2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.snmp.plugin.internal;

import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SNMPProvider extends AbstractBindingAwareProvider {
    static final Logger LOG  = LoggerFactory.getLogger(SNMPProvider.class);

    @Override
    public void onSessionInitiated(BindingAwareBroker.ProviderContext providerContext) {
        SNMPImpl impl = new SNMPImpl();
        providerContext.addRpcImplementation(SnmpService.class, impl);
        LOG.info("SNMP Provider Service Initialized");
    }
}
