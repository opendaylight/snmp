/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.snmp.plugin.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.opendaylight.snmp.OID;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ieee.types.rev080522.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Gauge32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.PhysAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Timestamp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Timeticks;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.InterfaceIndex;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.InterfaceIndexOrZero;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2.inet.address.mib.rev050204.InetAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.Snmp;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

public class MibTable<T> {
    private static final Logger LOG = LoggerFactory.getLogger(MibTable.class);

    private final SnmpGetInputBuilder snmpGetInputBuilder;
    private final Class<T> builderClass;
    private Map<Integer, T> indexToBuilderObject;
    private final Snmp snmp;

    public MibTable(Snmp snmp, Ipv4Address ipv4Address, PortNumber port, String community, Class<T> builderClass) {
        this.snmp = snmp;
        this.builderClass = builderClass;
        snmpGetInputBuilder = new SnmpGetInputBuilder()
                .setCommunity(community)
                .setIpAddress(ipv4Address)
                .setPort(port)
                .setGetType(SnmpGetType.GETWALK);
    }

    public Map<Integer, T> populate() {
        Collection<GetDataObject> getDataObjects = new ArrayList<>();
        indexToBuilderObject = new HashMap<>();

        // Send out each of the requests
        for (Method method : builderClass.getMethods()) {
            if (method.getName().startsWith("set")) {
                GetDataObject dataObject = new GetDataObject();
                dataObject.setSetMethod(method);

                dataObject = sendRequest(dataObject);
                if (dataObject.getListFuture() != null) {
                    getDataObjects.add(dataObject);
                }
            }
        }

        for (GetDataObject getDataObject : getDataObjects) {
            parse(getDataObject);
        }

        return indexToBuilderObject;
    }

    private GetDataObject sendRequest(GetDataObject dataObject) {
        Method method = dataObject.getSetMethod();

        if (method != null && method.getName().startsWith("set")) {
            OID oid = method.getAnnotation(OID.class);
            if (oid != null) {
                String oidString = oid.value();
                snmpGetInputBuilder.setOid(oidString);
                dataObject.setBaseOID(oidString);

                AsyncGetHandler getHandler = new AsyncGetHandler(snmpGetInputBuilder.build(), snmp);
                dataObject.setListFuture(getHandler.getListResponse());
            }
        }
        return dataObject;
    }

    private void parse(GetDataObject dataObject) {

        Future<List<VariableBinding>> future = dataObject.getListFuture();
        List<VariableBinding> variableBindings;
        try {
            variableBindings = future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("VariableBinding Future", e);
            return;
        }

        Method method = dataObject.getSetMethod();
        org.snmp4j.smi.OID baseOID = new org.snmp4j.smi.OID(dataObject.getBaseOID());

        // Send the request for the oid
        Variable variable = null;
        Class<?> objectType = null;
        for (int i = 0; i < variableBindings.size(); i++) {
            VariableBinding variableBinding = variableBindings.get(i);

            try {
                org.snmp4j.smi.OID snmpOID = variableBinding.getOid();

                if (!snmpOID.startsWith(baseOID)) {
                    continue;
                }

                variable = variableBinding.getVariable();

                // Map the type of object we got based on the set method

                objectType = method.getParameterTypes()[0];
                Object setObject = null;

                if (objectType.equals(Counter32.class)) {
                    setObject = new Counter32(variable.toLong());

                } else if (objectType.equals(Counter64.class)) {
                    BigInteger counter64 = new BigInteger(variable.toString());
                    setObject = new Counter64(counter64);

                } else if (objectType.equals(MacAddress.class)) {
                    // Get the byte array
                    OctetString octetString = (OctetString) variable;

                    String macAddress = octetString.toHexString(':');
                    setObject = new MacAddress(macAddress);

                } else if (objectType.equals(Ipv4Address.class)) {
                    IpAddress ipAddress = (IpAddress) variable;
                    setObject = new Ipv4Address(ipAddress.toString());

                } else if (objectType.equals(Long.class)) {
                    setObject = variable.toLong();

                } else if (objectType.equals(Integer.class)) {
                    setObject = variable.toInt();

                } else if (objectType.equals(InterfaceIndexOrZero.class)) {
                    setObject = new InterfaceIndexOrZero(Integer.valueOf(variable.toString()));

                } else if (objectType.equals(Timestamp.class)) {
                    setObject = new Timestamp(variable.toLong());

                } else if (objectType.equals(InetAddress.class)) {
                    org.snmp4j.smi.IpAddress ipAddress = (org.snmp4j.smi.IpAddress) variable;
                    setObject = ipAddress.getInetAddress();

                } else if (Enum.class.isAssignableFrom(objectType)) {
                    setObject = objectType.getEnumConstants()[variable.toInt()];

                } else if (objectType.equals(PhysAddress.class)) {
                    setObject = new PhysAddress(variable.toString());

                } else if (objectType.equals(String.class)) {
                    setObject = variable.toString();

                } else if (objectType.equals(Gauge32.class)) {
                    setObject = new Gauge32(variable.toLong());

                } else if (objectType.equals(InterfaceIndex.class)) {
                    setObject = new InterfaceIndex(variable.toInt());

                } else if (objectType.equals(Timeticks.class)) {
                    TimeTicks timeTicks = (TimeTicks) variable;
                    setObject = new TimeTicks(timeTicks.toMilliseconds());
                }

                // Get the index of from the OID
                Integer index = getIndexFromOID(snmpOID.toString());

                T builderObject;

                if (setObject == null) {
                    continue;
                }

                if (!indexToBuilderObject.containsKey(index)) {
                    builderObject = builderClass.newInstance();
                    indexToBuilderObject.put(index, builderObject);
                }

                builderObject = indexToBuilderObject.get(index);

                try {
                    method.invoke(builderObject, setObject);
                } catch (InvocationTargetException | IllegalArgumentException e) {
                    LOG.debug(String.format("Error invoking %s with %s", method.getName(), setObject), e);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                LOG.warn("Populate Exception", e);
            }
        }
    }

    private static Integer getIndexFromOID(String oid) {
        String[] splitString = oid.split("\\.");
        String last = splitString[splitString.length - 1];
        return Integer.valueOf(last);
    }

    private class GetDataObject {
        private Future<List<VariableBinding>> listFuture;
        private Method setMethod;
        private String baseOID;

        public Future<List<VariableBinding>> getListFuture() {
            return listFuture;
        }

        public void setListFuture(Future<List<VariableBinding>> listFuture) {
            this.listFuture = listFuture;
        }

        public Method getSetMethod() {
            return setMethod;
        }

        public void setSetMethod(Method setMethod) {
            this.setMethod = setMethod;
        }

        public String getBaseOID() {
            return baseOID;
        }

        public void setBaseOID(String baseOID) {
            this.baseOID = baseOID;
        }
    }

}
