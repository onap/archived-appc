/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Modifications Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.appc.domainmodel.lcm;

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTransactionRecord {

    private TransactionRecord transactionRecord;

    @Before
    public void setUp() {
        transactionRecord = new TransactionRecord();
    }

    @Test
    public void testGetTransactionId() {
        transactionRecord.setTransactionId("1234");
        Assert.assertNotNull(transactionRecord.getTransactionId());
        Assert.assertEquals(transactionRecord.getTransactionId(), "1234");
    }

    @Test
    public void testGetRequestId() {
        transactionRecord.setRequestId("1298ABC");
        Assert.assertNotNull(transactionRecord.getRequestId());
        Assert.assertEquals(transactionRecord.getRequestId(), "1298ABC");
    }

    @Test
    public void testGetSubRequestId() {
        transactionRecord.setSubRequestId("1298");
        Assert.assertNotNull(transactionRecord.getSubRequestId());
        Assert.assertEquals(transactionRecord.getSubRequestId(), "1298");
    }

    @Test
    public void testGetOriginatorId() {
        transactionRecord.setOriginatorId("1111");
        Assert.assertNotNull(transactionRecord.getOriginatorId());
        Assert.assertEquals(transactionRecord.getOriginatorId(), "1111");
    }

    @Test
    public void testGetTargetId() {
        transactionRecord.setTargetId("2222");
        Assert.assertNotNull(transactionRecord.getTargetId());
        Assert.assertEquals(transactionRecord.getTargetId(), "2222");
    }

    @Test
    public void testGetTargetType() {
        transactionRecord.setTargetType("A");
        Assert.assertNotNull(transactionRecord.getTargetType());
        Assert.assertEquals(transactionRecord.getTargetType(), "A");
    }

    @Test
    public void testGetResultCode() {
        transactionRecord.setResultCode(200);
        Assert.assertNotNull(transactionRecord.getResultCode());
        Assert.assertEquals(transactionRecord.getResultCode(), 200);
    }

    @Test
    public void testGetDescription() {
        transactionRecord.setDescription("SUCCESS");
        Assert.assertNotNull(transactionRecord.getDescription());
        Assert.assertEquals(transactionRecord.getDescription(), "SUCCESS");
    }

    @Test
    public void testGetServiceInstanceId() {
        transactionRecord.setServiceInstanceId("A1");
        Assert.assertNotNull(transactionRecord.getServiceInstanceId());
        Assert.assertEquals(transactionRecord.getServiceInstanceId(), "A1");
    }

    @Test
    public void testGetVnfcName() {
        transactionRecord.setVnfcName("Vnf1");
        Assert.assertNotNull(transactionRecord.getVnfcName());
        Assert.assertEquals(transactionRecord.getVnfcName(), "Vnf1");
    }

    @Test
    public void testGetVserverId() {
        transactionRecord.setVserverId("V1");
        Assert.assertNotNull(transactionRecord.getVserverId());
        Assert.assertEquals(transactionRecord.getVserverId(), "V1");
    }

    @Test
    public void testGetVfModuleId() {
        transactionRecord.setVfModuleId("M1");
        Assert.assertNotNull(transactionRecord.getVfModuleId());
        Assert.assertEquals(transactionRecord.getVfModuleId(), "M1");
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(transactionRecord.toString(), "");
        assertNotEquals(transactionRecord.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(transactionRecord.toString().contains("TransactionRecord{transactionId"));
    }
    
    @Test
    public void testGetOriginTimeStamp() {
        Instant instant= Instant.now();
        transactionRecord.setOriginTimestamp(instant);
        assertEquals(instant, transactionRecord.getOriginTimestamp());
    }
    
    @Test
    public void testGetStartTime() {
        Instant instant= Instant.now();
        transactionRecord.setStartTime(instant);
        assertEquals(instant, transactionRecord.getStartTime());
    }
    
    @Test
    public void testGetEndTime() {
        Instant instant= Instant.now();
        transactionRecord.setEndTime(instant);
        assertEquals(instant, transactionRecord.getEndTime());
    }
    
    @Test
    public void testGetOperation() {
        VNFOperation vnfOperation= VNFOperation.ActionStatus;
        transactionRecord.setOperation(vnfOperation);
        assertEquals(vnfOperation, transactionRecord.getOperation());
    }
    
    @Test
    public void testGetMode() {
        Flags.Mode mode= Flags.Mode.EXCLUSIVE;
        transactionRecord.setMode(mode);
        assertEquals("EXCLUSIVE", transactionRecord.getMode());
    }
    
    @Test
    public void testGetRequestState() {
        RequestStatus requestStatus= RequestStatus.ACCEPTED;
        transactionRecord.setRequestState(requestStatus);
        assertEquals("ACCEPTED", transactionRecord.getRequestState());
    }
    
}
