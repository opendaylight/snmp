package org.opendaylight.snmp.plugin.internal;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpSetInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.snmp.get.output.Results;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.snmp4j.Snmp;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.Target;


public class SNMPImplTest {

    private static final String SYS_OID_REQUEST = "1.3.6.1.2.1.1.2.0";
    private static final String SYS_OID_RESPONSE = "1.3.6.1.4.1.11.2.3.7.11.119";
    private static final String LOCATION_OID = "1.3.6.1.2.1.1.6.0";
    private static final String GET_IP_ADDRESS = "10.10.10.10";
    private static final String SET_IP_ADDRESS = "20.20.20.20";
    private static final Integer snmpListenPort = 161;
    private static final String COMMUNITY = "ComunityName";
    private static final String VALUE = "test";
    private static final int RETRIES = 1;
    private static final int TIMEOUT = 500;
    private static final int MAXREPETITIONS = 10000;

    private static Snmp mockSnmp = null;
    private static RpcProviderRegistry mockRpcReg = null;

    @BeforeClass
    public static void setUpTest() throws IOException {
        mockRpcReg = mock(RpcProviderRegistry.class);
        when(mockRpcReg.addRpcImplementation(eq(SnmpService.class), any(SnmpService.class))).thenReturn(null);

        // GET response
        mockSnmp = mock(Snmp.class);
    }

    @SuppressWarnings("resource")
    @Test
    public void testConstructor() {
        SNMPImpl snmpImpl = new SNMPImpl(mockRpcReg);
        assertNotNull(snmpImpl.getSnmp());
        assertNull(snmpImpl.getRpcRegistration());
    }

    @SuppressWarnings("resource")
    @Test
    public void testGet() throws IOException {
        final ResponseEvent event = createResponseEvent();

        // Use mockito when command so that when snmp.send is called it
        // will return event. Use argThat to provide custom matchers.  In the
        // matchers we check that the parameters passed to snmp.send are as
        // expected
        when(mockSnmp.send(argThat(new ArgumentMatcher<PDU>(){
            @Override
            public boolean matches(Object argument) {
                if (argument instanceof PDU) {
                    assertEquals(((PDU)argument).getType(), PDU.GET);
                    assertEquals(((PDU)argument).getVariableBindings().get(0).getOid().toString(), SYS_OID_REQUEST);
                    assertEquals(((PDU)argument).getMaxRepetitions(), MAXREPETITIONS);
                    return true;
                }
                return false;
            }}), argThat(new ArgumentMatcher<Target>(){
                @Override
                public boolean matches(Object argument) {
                    if (argument instanceof Target) {
                        assertEquals(((Target)argument).getSecurityName().toString(), COMMUNITY);
                        assertEquals(((Target)argument).getAddress().toString(), (GET_IP_ADDRESS + "/" + snmpListenPort.toString())); 
                        assertEquals(((Target)argument).getTimeout(), TIMEOUT);
                        assertEquals(((Target)argument).getRetries(), RETRIES);
                        assertEquals(((Target)argument).getVersion(), SnmpConstants.version2c);
                        return true;
                    }
                    return false;
                }}))).thenReturn(event);

        SNMPImpl snmpImpl = new SNMPImpl(mockRpcReg, mockSnmp);

        String value = "Failed";
        String oid = "bad oid";

        Ipv4Address ip = new Ipv4Address(GET_IP_ADDRESS);
        SnmpGetInputBuilder input = new SnmpGetInputBuilder(); 
        input.setCommunity(COMMUNITY);
        input.setIpAddress(ip);
        input.setOid(SYS_OID_REQUEST);
        input.setGetType(SnmpGetType.GET);

        try {
            Future<RpcResult<SnmpGetOutput>> resultFuture = snmpImpl.snmpGet(input.build());

            RpcResult<SnmpGetOutput> result = resultFuture.get();
            if (result.isSuccessful()) {
                SnmpGetOutput output = result.getResult();
                List<Results> snmpResults = output.getResults();
                if (snmpResults.size() == 1) {
                    for (Results r: snmpResults) {
                        value = r.getValue();
                        oid = r.getOid();
                        break;
                    }
                }
            }
        } catch( InterruptedException | ExecutionException e ) {
           e.printStackTrace();
        }
        assertEquals(value, SYS_OID_RESPONSE);
        assertEquals(oid, SYS_OID_REQUEST);
    }

    @SuppressWarnings("resource")
    @Test
    public void testSet() throws IOException {
        final ResponseEvent event = createResponseEvent();

        // SET response - because SET is async, use mockito doAnswer to 
        // call the ResponseListener callback
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ResponseListener callback = (ResponseListener) invocation.getArguments()[3];
                callback.onResponse(event);
                return null;
            }
        }).when(mockSnmp).set(argThat(new ArgumentMatcher<PDU>(){
            @Override
            public boolean matches(Object argument) {
                if (argument instanceof PDU) {
                    assertEquals(((PDU)argument).getType(), PDU.SET);
                    assertEquals(((PDU)argument).getVariableBindings().get(0).toValueString(), VALUE);
                    assertEquals(((PDU)argument).getVariableBindings().get(0).getOid().toString(), LOCATION_OID);
                    return true;
                }
                return false;
            }}), argThat(new ArgumentMatcher<Target>(){
                @Override
                public boolean matches(Object argument) {
                    if (argument instanceof Target) {
                        assertEquals(((Target)argument).getSecurityName().toString(), COMMUNITY);
                        assertEquals(((Target)argument).getAddress().toString(), (SET_IP_ADDRESS + "/" + snmpListenPort.toString())); 
                        assertEquals(((Target)argument).getTimeout(), TIMEOUT);
                        assertEquals(((Target)argument).getRetries(), RETRIES);
                        assertEquals(((Target)argument).getVersion(), SnmpConstants.version2c);
                        return true;
                    }
                    return false;
                }}), any(), (ResponseListener)any());

        SNMPImpl snmpImpl = new SNMPImpl(mockRpcReg, mockSnmp);

        Ipv4Address ip = new Ipv4Address(SET_IP_ADDRESS);
        SnmpSetInputBuilder input = new SnmpSetInputBuilder();
        input.setCommunity(COMMUNITY);
        input.setIpAddress(ip);
        input.setOid(LOCATION_OID);
        input.setValue(VALUE);

        RpcResult<Void> result = null;
        try {
            Future<RpcResult<Void>> resultFuture = snmpImpl.snmpSet(input.build());
            result = resultFuture.get();
        } catch( InterruptedException | ExecutionException e ) {
            e.printStackTrace();
        }
        assertTrue(result.isSuccessful());
    }

    /*
     * create a response from a GET request, it can also be
     * use as the responseEvent for SET
     */
    public static ResponseEvent createResponseEvent() {
        // create request PDU
        PDU requestPdu = new PDU();
        OID requestOid = new OID(SYS_OID_REQUEST);
        requestPdu.add(new VariableBinding(requestOid));
        requestPdu.setMaxRepetitions(10000);
        requestPdu.setNonRepeaters(0);
        requestPdu.setType(PDU.GET);

        // create response PUD
        PDU responsePdu = new PDU();
        OID responseOid = new OID(SYS_OID_REQUEST);
        Variable variable = new OctetString(SYS_OID_RESPONSE);
        responsePdu.add(new VariableBinding(responseOid, variable));
        responsePdu.setMaxRepetitions(10000);
        responsePdu.setNonRepeaters(0);
        responsePdu.setType(PDU.GET);

        // create ip address
        Address addr = null;
        try {
            addr = new UdpAddress(Inet4Address.getByName(GET_IP_ADDRESS), snmpListenPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // create the ResponseEvent
        ResponseEvent responseEvent = new ResponseEvent(mockSnmp,     // source
                                          addr,                       // peer address
                                          requestPdu,                 // request PDU
                                          responsePdu,                // response PDU
                                          new Object(),               // User object
                                          null);                      // error
        return responseEvent;
    }

}
