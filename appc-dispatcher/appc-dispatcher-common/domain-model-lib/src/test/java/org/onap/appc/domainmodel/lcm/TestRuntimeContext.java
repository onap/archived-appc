/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Modifications Copyright (C) 2018 IBM.
* ================================================================================
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

public class TestRuntimeContext {
    private RuntimeContext runtimeContext;

    @Before
    public void setUp() {
        runtimeContext =new  RuntimeContext();
    }

    @Test
    public void testGetRpcName() {
        runtimeContext.setRpcName("ABC");
        Assert.assertNotNull(runtimeContext.getRpcName());
        Assert.assertEquals(runtimeContext.getRpcName(), "ABC");
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(runtimeContext.toString(), "");
        assertNotEquals(runtimeContext.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(runtimeContext.toString().contains("RuntimeContext{requestContext"));
    }
    
    @Test
    public void testGetRequestContext() {
        RequestContext requestContext= new RequestContext();
        runtimeContext.setRequestContext(requestContext);
        assertEquals(requestContext, runtimeContext.getRequestContext());
    }
    
    @Test
    public void testGetResponseContext() {
        ResponseContext responseContext= new ResponseContext();
        runtimeContext.setResponseContext(responseContext);
        assertEquals(responseContext, runtimeContext.getResponseContext());
    }
    
    @Test
    public void testGetTimeStart() {
        Instant instant= Instant.now();
        runtimeContext.setTimeStart(instant);
        assertEquals(instant, runtimeContext.getTimeStart());
    }
    
    @Test
    public void testGetVnfContext() {
        VNFContext vnfContext= new VNFContext();
        runtimeContext.setVnfContext(vnfContext);
        assertEquals(vnfContext, runtimeContext.getVnfContext());
    }
    
    @Test
    public void testGetTransactionRecord() {
        TransactionRecord transactionRecord= new TransactionRecord();
        runtimeContext.setTransactionRecord(transactionRecord);
        assertEquals(transactionRecord, runtimeContext.getTransactionRecord());
    }
}
