package org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922;

import org.opendaylight.snmp.plugin.internal.SNMPImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SNMPImplModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.AbstractSNMPImplModule {
	private static final Logger LOG = LoggerFactory.getLogger(SNMPImplModule.class);
	
	public SNMPImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public SNMPImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SNMPImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
       return new SNMPImpl(getRpcRegistryDependency());
    }

}
