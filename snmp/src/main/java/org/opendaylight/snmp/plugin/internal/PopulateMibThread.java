/*
 * Copyright (c) 2014-2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.snmp.plugin.internal;

import org.opendaylight.snmp.OID;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ieee.types.rev080522.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Gauge32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.PhysAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Timestamp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Timeticks;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.InterfaceIndex;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.InterfaceIndexOrZero;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2.inet.address.mib.rev050204.InetAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SendSnmpQueryInputBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class PopulateMibThread<T> extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(PopulateMibThread.class);
    private Method method;
    private ConcurrentHashMap<Integer, T> indexToBuilderObject;
    private SendSnmpQueryInputBuilder sendSnmpQueryInputBuilder;
    private Class builderClass;
    private SNMPImpl snmpImpl;

    public PopulateMibThread(Method method,
                             ConcurrentHashMap<Integer, T> indexToBuilderObject,
                             SendSnmpQueryInputBuilder sendSnmpQueryInputBuilder,
                             Class<T> builderClass,
                             SNMPImpl snmp) {
        this.method = method;
        this.indexToBuilderObject = indexToBuilderObject;
        this.sendSnmpQueryInputBuilder = sendSnmpQueryInputBuilder;
        this.builderClass = builderClass;
        this.snmpImpl = snmp;
    }


    private static Integer getIndexFromOID(String oid) {
        String[] splitString = oid.split("\\.");
        String last = splitString[splitString.length - 1];
        return new Integer(last);
    }

    public void run() {
        if (!method.getName().startsWith("set")) return;

        OID oid = method.getAnnotation(OID.class);
        if (oid != null) {
            String oidString = oid.value();
            sendSnmpQueryInputBuilder.setOid(oidString);

            org.snmp4j.smi.OID baseOID = new org.snmp4j.smi.OID(oid.value());

            // Send the request for the oid
            Variable variable = null;
            Class objectType = null;
            try {

                ArrayList<VariableBinding> variableBindings = snmpImpl.sendQuery(sendSnmpQueryInputBuilder.build());
                for (VariableBinding variableBinding : variableBindings) {
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
                        setObject = new Long(variable.toLong());

                    } else if (objectType.equals(Integer.class)) {
                        setObject = new Integer(variable.toInt());

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
                        setObject = new String(variable.toString());

                    } else if (objectType.equals(Gauge32.class)) {
                        setObject = new Gauge32(variable.toLong());

                    } else if (objectType.equals(InterfaceIndex.class)) {
                        setObject = new InterfaceIndex(variable.toInt());

                    } else if (objectType.equals(Timeticks.class)) {
                        TimeTicks timeTicks = (TimeTicks)variable;
                        setObject = new TimeTicks(timeTicks.toMilliseconds());
                    }

                    // Get the index of from the OID
                    Integer index = getIndexFromOID(snmpOID.toString());

                    T builderObject;

                    if (!indexToBuilderObject.contains(index)) {
                        builderObject = (T) builderClass.newInstance();
                        indexToBuilderObject.putIfAbsent(index, builderObject);
                    }

                    builderObject = indexToBuilderObject.get(index);
                    method.invoke(builderObject, setObject);
                }
            } catch (NullPointerException n) {
                n.printStackTrace();
            } catch (Exception e) {
                LOG.info(String.format("Error converting class: %s to %s on method: %s", variable.getClass().getName(), objectType.getName(), method.getName()));
                LOG.info(e.getMessage());
            }
        }
    }
}

