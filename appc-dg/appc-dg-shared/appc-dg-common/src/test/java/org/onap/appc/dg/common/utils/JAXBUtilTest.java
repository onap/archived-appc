/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 AT&T
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
package org.onap.appc.dg.common.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;



import javax.xml.bind.JAXBException;

public class JAXBUtilTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testToObjectFail() {
      String xmlStr = "<?xml version=\\\"1.0\\\"?><vnfId>I1</vnfId><vnfType>T1</vnfType>";
      JSONUtilVnfTest jOut = null;
        try {
            jOut = JAXBUtil.toObject(xmlStr, JSONUtilVnfTest.class);
        } catch (JAXBException jaxbe) {
            assertEquals(jOut, null);
        }
    }

}
