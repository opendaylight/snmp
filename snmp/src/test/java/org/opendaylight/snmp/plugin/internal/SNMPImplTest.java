package org.opendaylight.snmp.plugin.internal;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
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
	private static final String IP_ADDRESS = "10.10.10.10";
	private static final Integer snmpListenPort = 161;
	private static final String COMMUNITY = "public";
	private static final String VALUE = "test";
	
	private static Snmp mockSnmp = null;
	private static RpcProviderRegistry mockRpcReg = null;
	
	@BeforeClass
	public static void setUpTest() throws IOException {
		mockRpcReg = mock(RpcProviderRegistry.class);
		when(mockRpcReg.addRpcImplementation(eq(SnmpService.class), any(SnmpService.class))).thenReturn(null);
		
		// GET response
		mockSnmp = mock(Snmp.class);
		final ResponseEvent event = createResponseEvent();
		when(mockSnmp.send(any(PDU.class), any(Target.class))).thenReturn(event);

		// SET response - because SET is async, use mockito doAnswer to 
		// call the ResponseListener callback
		doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) throws Throwable {
				ResponseListener callback = (ResponseListener) invocation.getArguments()[3];
				callback.onResponse(event);
				return null;
			}
		}).when(mockSnmp).set(any(PDU.class), any(Target.class), any(), (ResponseListener)any());
	}
		
	@SuppressWarnings("resource")
	@Test
	public void testGet() {
		SNMPImpl snmpImpl = new SNMPImpl(mockRpcReg, mockSnmp);
		
		String value = "Failed";
		
		Ipv4Address ip = new Ipv4Address(IP_ADDRESS);
		SnmpGetInputBuilder input = new SnmpGetInputBuilder(); 
		input.setCommunity("public");
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
						break;
					}
				}
			}
		} catch( InterruptedException | ExecutionException e ) {
           e.printStackTrace(); 
        }	
		assertEquals(value, SYS_OID_RESPONSE);
	}
	
	@SuppressWarnings("resource")
	@Test
	public void testSet() {
		SNMPImpl snmpImpl = new SNMPImpl(mockRpcReg, mockSnmp);
		
		Ipv4Address ip = new Ipv4Address(IP_ADDRESS);
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
            addr = new UdpAddress(Inet4Address.getByName(IP_ADDRESS), snmpListenPort);
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