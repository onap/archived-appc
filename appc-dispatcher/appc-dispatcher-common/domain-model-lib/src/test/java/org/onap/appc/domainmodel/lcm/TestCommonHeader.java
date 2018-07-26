/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Modifications Copyright 2018 IBM.
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

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.onap.appc.domainmodel.lcm.Flags.Mode;

public class TestCommonHeader {
    private CommonHeader commonHeader;

    @Before
    public void setUp() {
        commonHeader =new  CommonHeader();
    }

    @Test
    public void testgetRpcName() {
        commonHeader.setApiVer("2.0");
        Assert.assertNotNull(commonHeader.getApiVer());
        Assert.assertEquals(commonHeader.getApiVer(), "2.0");
    }

    @Test
    public void testGetOriginatorId() {
        commonHeader.setOriginatorId("AAAA");
        Assert.assertNotNull(commonHeader.getOriginatorId());
        Assert.assertEquals(commonHeader.getOriginatorId(), "AAAA");
    }

    @Test
    public void testGetRequestId() {
        commonHeader.setRequestId("1111ABCD");
        Assert.assertNotNull(commonHeader.getRequestId());
        Assert.assertEquals(commonHeader.getRequestId(), "1111ABCD");
    }

    @Test
    public void testGetSubRequestId() {
        commonHeader.setSubRequestId("1111");
        Assert.assertNotNull(commonHeader.getSubRequestId());
        Assert.assertEquals(commonHeader.getSubRequestId(), "1111");
    }

    @Test
    public void testGetFlags() {
        Flags flags=new Flags();
        flags.setTtl(60);
        Mode mode=Mode.EXCLUSIVE;
        flags.setMode(mode);
        flags.setForce(false);
        commonHeader.setFlags(flags);
        Assert.assertNotNull(commonHeader.getFlags());
        Assert.assertEquals(60,commonHeader.getFlags().getTtl());
        Assert.assertEquals(Mode.EXCLUSIVE,commonHeader.getFlags().getMode());
        Assert.assertEquals(false,commonHeader.getFlags().isForce());
        
    }
    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(commonHeader.toString(), "");
        assertNotEquals(commonHeader.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(commonHeader.toString().contains("CommonHeader{flags"));
    }
    
    @Test
    public void testTimeStamp() {
       commonHeader.setTimestamp(new Date("02/09/2004"));
       String timeStamp= commonHeader.getTimeStamp().toString();
       String expected="Mon Feb 09 00:00:00 UTC 2004";
       assertEquals(expected, timeStamp);
    }

}
