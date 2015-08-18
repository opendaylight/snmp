package org.opendaylight.snmp.plugin.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.InterfaceIndex;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.smiv2._if.mib.rev000614.interfaces.group.IfEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.GetInterfacesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.GetInterfacesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpGetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.SnmpSetInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.snmp.rev140922.snmp.get.output.Results;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
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

public class SNMPImplTest {
    private static final String SYS_OID_REQUEST = "1.3.6.1.2.1.1.2.0";
    private static final String SYS_OID_RESPONSE = "1.3.6.1.4.1.11.2.3.7.11.119";
    private static final String LOCATION_OID = "1.3.6.1.2.1.1.6.0";
    private static final String GET_IP_ADDRESS = "10.10.10.10";
    private static final String SET_IP_ADDRESS = "20.20.20.20";
    private static final Integer snmpListenPort = 161;
    private static final String COMMUNITY = "CommunityName";
    private static final String VALUE = "test";
    private static final int RETRIES = 1;
    private static final int TIMEOUT = 500;
    private static final int MAXREPETITIONS = 10000;

    private static Snmp mockSnmp = null;
    private static RpcProviderRegistry mockRpcReg = null;

    @Before
    public void setUp() throws IOException {
        mockRpcReg = mock(RpcProviderRegistry.class);
        when(mockRpcReg.addRpcImplementation(eq(SnmpService.class), any(SnmpService.class))).thenReturn(null);

        // GET response
        mockSnmp = mock(Snmp.class);
    }

    @SuppressWarnings("resource")
    @Test
    public void testGet() throws IOException, InterruptedException, ExecutionException {
        final ResponseEvent event = createResponseEvent();

        // Use mockito when command so that when snmp.send is called it
        // will return event. Use argThat to provide custom matchers.  In the
        // matchers we check that the parameters passed to snmp.send are as
        // expected
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ResponseListener callback = (ResponseListener) invocation.getArguments()[3];
                callback.onResponse(event);
                return null;
            }
        }).when(mockSnmp).send(argThat(new ArgumentMatcher<PDU>(){
            @Override
            public boolean matches(Object argument) {
                if (argument instanceof PDU) {
                    PDU pdu = (PDU)argument;
                    assertEquals("Checking PDU Get type", pdu.getType(), PDU.GET);
                    assertEquals("Checking PDU OID value", pdu.getVariableBindings().get(0).getOid().toString(), SYS_OID_REQUEST);
                    assertEquals("Checking max repititions", pdu.getMaxRepetitions(), MAXREPETITIONS);
                    return true;
                }
                return false;
        }}), argThat(new ArgumentMatcher<Target>(){
                @Override
                public boolean matches(Object argument) {
                    if (argument instanceof Target) {
                        Target target = (Target)argument;
                        assertEquals(target.getSecurityName().toString(), COMMUNITY);
                        assertEquals(target.getAddress().toString(), (GET_IP_ADDRESS + "/" + snmpListenPort.toString()));
                        assertEquals(target.getTimeout(), TIMEOUT);
                        assertEquals(target.getRetries(), RETRIES);
                        assertEquals(target.getVersion(), SnmpConstants.version2c);
                        return true;
                    }
                    return false;
                }}),  any(), (ResponseListener) any());

        SNMPImpl snmpImpl = new SNMPImpl(mockRpcReg, mockSnmp);

        String value = "Failed";
        String oid = "bad oid";

        Ipv4Address ip = new Ipv4Address(GET_IP_ADDRESS);
        SnmpGetInputBuilder input = new SnmpGetInputBuilder();
        input.setCommunity(COMMUNITY);
        input.setIpAddress(ip);
        input.setOid(SYS_OID_REQUEST);
        input.setGetType(SnmpGetType.GET);

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
        assertEquals(value, SYS_OID_RESPONSE);
        assertEquals(oid, SYS_OID_REQUEST);
    }

    @SuppressWarnings("resource")
    @Test
    public void testSet() throws IOException, InterruptedException, ExecutionException {
        final ResponseEvent event = createResponseEvent();

        // SET response - because SET is async, use mockito doAnswer to
        // call the ResponseListener callback
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ResponseListener callback = (ResponseListener) invocation.getArguments()[3];
                callback.onResponse(event);
                return null;
            }
        }).when(mockSnmp).set(argThat(new ArgumentMatcher<PDU>() {
            @Override
            public boolean matches(Object argument) {
                if (argument instanceof PDU) {
                    PDU pdu = (PDU)argument;
                    assertEquals("Checking SET PDU type", pdu.getType(), PDU.SET);
                    assertEquals("Checking Value of SET", pdu.getVariableBindings().get(0).toValueString(), VALUE);
                    assertEquals("Checking OID of SET", pdu.getVariableBindings().get(0).getOid().toString(), LOCATION_OID);
                    return true;
                }
                return false;
            }}), argThat(new ArgumentMatcher<Target>(){
                @Override
                public boolean matches(Object argument) {
                    if (argument instanceof Target) {
                        Target target = (Target)argument;
                        assertEquals("Checking community of SET", target.getSecurityName().toString(), COMMUNITY);
                        assertEquals("Checking target IP", target.getAddress().toString(), (SET_IP_ADDRESS + "/" + snmpListenPort.toString()));
                        assertEquals("Checking timeout", target.getTimeout(), TIMEOUT);
                        assertEquals("Checking retries", target.getRetries(), RETRIES);
                        assertEquals("Checking version", target.getVersion(), SnmpConstants.version2c);
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
        Future<RpcResult<Void>> resultFuture = snmpImpl.snmpSet(input.build());
        result = resultFuture.get();
        assertTrue(result.isSuccessful());
    }

    /*
     * create a response from a GET request, it can also be
     * use as the responseEvent for SET
     */
    public static ResponseEvent createResponseEvent() throws UnknownHostException {
        // create request PDU
        PDU requestPdu = new PDU();
        OID requestOid = new OID(SYS_OID_REQUEST);
        requestPdu.add(new VariableBinding(requestOid));
        requestPdu.setMaxRepetitions(MAXREPETITIONS);
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
        addr = new UdpAddress(Inet4Address.getByName(GET_IP_ADDRESS), snmpListenPort);

        // create the ResponseEvent
        ResponseEvent responseEvent = new ResponseEvent(mockSnmp,     // source
                                          addr,                       // peer address
                                          requestPdu,                 // request PDU
                                          responsePdu,                // response PDU
                                          null,                       // User object
                                          null);                      // error
        return responseEvent;
    }

    @Test
    public void testGetInterfaces() throws Exception {
        final String baseIFOIB = "1.3.6.1.2.1.2.2.1.";
        final OID ifIndexOID = new OID(baseIFOIB + "1");
        final OID ifAdminStatusOID = new OID(baseIFOIB + "7");
        final OID ifDescrOID = new OID(baseIFOIB + "2");
        final OID ifInErrorsOID = new OID(baseIFOIB + "14");


        // Generate the test list of interfaces
        final List<IfEntry> testInterfaceEntries = new ArrayList<>();

        for (int i=0; i<10; i++) {
            IfEntryBuilder ifEntryBuilder = new IfEntryBuilder()
                    .setIfIndex(new InterfaceIndex(i + 1))
                    .setIfAdminStatus(IfEntry.IfAdminStatus.forValue(i % 3))
                    .setIfDescr(String.format("Interface %s", i))
                    .setIfInErrors(new Counter32(99l-i));
            testInterfaceEntries.add(ifEntryBuilder.build());
        }

        // Set up the response for the mock snmp4j
        // This is responsible for calling the onResponse() callback for SNMP messages
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                PDU requestPDU = (PDU) invocation.getArguments()[0];
                ResponseListener callback = (ResponseListener) invocation.getArguments()[3];

                // Create the response PDU based on the request PDU
                PDU responsePDU = new PDU();
                responsePDU.setType(PDU.GET);

                // Get the OID of the request
                assertEquals("Checking size of PDU response variable bindings", 1, requestPDU.getVariableBindings().size());
                OID requestOID = requestPDU.getVariableBindings().get(0).getOid();

                for (int i=0; i<testInterfaceEntries.size(); i++) {
                    IfEntry testIfEntry = testInterfaceEntries.get(i);
                    int[] prefix = requestOID.getValue();
                    Variable val = null;

                    if (requestOID.equals(ifIndexOID)) {
                        // Add all of the ifIndexes to the response
                        val = new Integer32(i + 1);

                    } else if (requestOID.equals(ifAdminStatusOID)) {
                        val = new Integer32(i % 3);

                    } else if (requestOID.equals(ifDescrOID)) {
                        val = new OctetString(testIfEntry.getIfDescr());

                    } else if (requestOID.equals(ifInErrorsOID)) {
                        val = new org.snmp4j.smi.Counter32(testIfEntry.getIfInErrors().getValue());

                    } else {
                        // Don't add any variable bindings to the response
                        break;
                    }

                    if (val != null) {
                        OID objOID = new OID(prefix, i);
                        responsePDU.add(new VariableBinding(objOID, val));
                    }
                }

                ResponseEvent responseEvent = new ResponseEvent(mockSnmp,
                        new UdpAddress(Inet4Address.getByName(GET_IP_ADDRESS), snmpListenPort),
                        requestPDU,
                        responsePDU,
                        null,
                        null);

                callback.onResponse(responseEvent);
                return null;
            }
        }).when(mockSnmp).send(any(PDU.class), any(Target.class), any(), (ResponseListener) any());

        try (SNMPImpl snmpImpl = new SNMPImpl(mockRpcReg, mockSnmp)) {
            Ipv4Address ip = new Ipv4Address(GET_IP_ADDRESS);
            GetInterfacesInputBuilder input = new GetInterfacesInputBuilder();
            input.setCommunity(COMMUNITY);
            input.setIpAddress(ip);

            RpcResult<GetInterfacesOutput> result = null;
            Future<RpcResult<GetInterfacesOutput>> resultFuture = snmpImpl.getInterfaces(input.build());
            result = resultFuture.get();
            assertTrue(result.isSuccessful());
        }
    }

    /*
     * create a response for a GETBULK request
     * @startWith sub-identifier to start with
     * @startWith sub-identifier to end with; if <code>endWith &lt; startWith</code> return no results
     */
    public static ResponseEvent createBulkResponseEvent(int startWith, int endWith) throws UnknownHostException {
        // create request PDU
        PDU requestPdu = new PDU();
        OID requestOid = new OID(SYS_OID_REQUEST + "." + startWith);
        requestPdu.add(new VariableBinding(requestOid));
        requestPdu.setMaxRepetitions(MAXREPETITIONS);
        requestPdu.setNonRepeaters(0);
        requestPdu.setType(PDU.GETBULK);

        // create response PDU
        PDU responsePdu = new PDU();
        for (int i = startWith; i <= endWith; i++) {
            OID responseOid = new OID(SYS_OID_REQUEST + "." + i);
            Variable variable = new OctetString(SYS_OID_RESPONSE);
            responsePdu.add(new VariableBinding(responseOid, variable));
        }
        responsePdu.setMaxRepetitions(10000);
        responsePdu.setNonRepeaters(0);
        responsePdu.setType(PDU.GETBULK);

        // create ip address
        Address addr = null;
        addr = new UdpAddress(Inet4Address.getByName(GET_IP_ADDRESS), snmpListenPort);

        // create the ResponseEvent
        ResponseEvent responseEvent = new ResponseEvent(mockSnmp,     // source
                                          addr,                       // peer address
                                          requestPdu,                 // request PDU
                                          responsePdu,                // response PDU
                                          null,                       // User object
                                          null);                      // error
        return responseEvent;
    }

    @Test
    public void testWalk() throws IOException, InterruptedException, ExecutionException {
        int stopWithBinding = 95; // number of bindings to simulate - the SNMPImpl mock does not know this number
        RpcResult<SnmpGetOutput> result = bulkTest(10, stopWithBinding, Integer.MAX_VALUE); // no timeout simulation
        assertTrue("Checking results success", result.isSuccessful());
        List<Results> snmpResults = result.getResult().getResults();
        assertEquals("Checking results size", stopWithBinding, snmpResults.size());
    }

    /**
     * Walk some OIDS and simulate a timeout partway through.  This also tests exception handling in {@link AsyncGetHandler},
     * since timeouts lead to thrown exceptions there.
     */
    @Test
    public void testWalkTimeout() throws IOException, InterruptedException, ExecutionException {
        int timeoutAfterBinding = 100;
        RpcResult<SnmpGetOutput> result = bulkTest(10, 200, timeoutAfterBinding);
        assertFalse("Checking results success", result.isSuccessful());
        List<Results> snmpResults = result.getResult().getResults();
        assertEquals("Checking results size", timeoutAfterBinding, snmpResults.size()); // partial result set, up to the timeout
    }

    private RpcResult<SnmpGetOutput> bulkTest(final int bindingsPerCall, final int stopWithBinding, final int timeoutAfterBinding) throws IOException, InterruptedException, ExecutionException {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws UnknownHostException, InterruptedException {
                PDU pdu = (PDU)invocation.getArguments()[0];
                OID oid = pdu.getVariableBindings().get(0).getOid();
                assertTrue("Checking PDU OID value", oid.toString().startsWith(SYS_OID_REQUEST));
                int last = oid.removeLast();
                int startWith = last + 1;
                oid.append(startWith);
                ResponseEvent responseEvent;
                if (last < timeoutAfterBinding) {
                    int endWith = Math.min(last + bindingsPerCall, stopWithBinding);
                    responseEvent = createBulkResponseEvent(startWith, endWith);
                } else {
                    // null responsePdu means timeout - see AsyncGetHandler
                    responseEvent = new ResponseEvent(mockSnmp, null, null, null, null, null);
                }
                ResponseListener callback = (ResponseListener) invocation.getArguments()[3];
                callback.onResponse(responseEvent);
                return null;
            }
        }).when(mockSnmp).send(argThat(new ArgumentMatcher<PDU>(){
            @Override
            public boolean matches(Object argument) {
                if (argument instanceof PDU) {
                    PDU pdu = (PDU)argument;
                    assertEquals("Checking PDU Get type", pdu.getType(), PDU.GETBULK);
                    assertTrue("Checking PDU OID value", pdu.getVariableBindings().get(0).getOid().toString().startsWith(SYS_OID_REQUEST));
                    assertEquals("Checking max repetitions", pdu.getMaxRepetitions(), MAXREPETITIONS);
                    return true;
                }
                return false;
            }}), argThat(new ArgumentMatcher<Target>(){
                @Override
                public boolean matches(Object argument) {
                    if (argument instanceof Target) {
                        Target target = (Target)argument;
                        assertEquals(target.getSecurityName().toString(), COMMUNITY);
                        assertEquals(target.getAddress().toString(), (GET_IP_ADDRESS + "/" + snmpListenPort.toString()));
                        assertEquals(target.getTimeout(), TIMEOUT);
                        assertEquals(target.getRetries(), RETRIES);
                        assertEquals(target.getVersion(), SnmpConstants.version2c);
                        return true;
                    }
                    return false;
                }}),  any(), (ResponseListener) any());

        try (SNMPImpl snmpImpl = new SNMPImpl(mockRpcReg, mockSnmp)) {
            Ipv4Address ip = new Ipv4Address(GET_IP_ADDRESS);
            SnmpGetInputBuilder input = new SnmpGetInputBuilder();
            input.setCommunity(COMMUNITY);
            input.setIpAddress(ip);
            input.setOid(SYS_OID_REQUEST);
            input.setGetType(SnmpGetType.GETWALK);

            Future<RpcResult<SnmpGetOutput>> resultFuture = snmpImpl.snmpGet(input.build());

            RpcResult<SnmpGetOutput> result = resultFuture.get();
            SnmpGetOutput output = result.getResult();
            List<Results> snmpResults = output.getResults();
            for (Results r: snmpResults) {
                String oid = r.getOid();
                assertEquals("Checking results value, oid " + oid, SYS_OID_RESPONSE, r.getValue());
                assertTrue("Checking results oid " + oid, oid.startsWith(SYS_OID_REQUEST));
            }
            return result;
        }
    }
}
