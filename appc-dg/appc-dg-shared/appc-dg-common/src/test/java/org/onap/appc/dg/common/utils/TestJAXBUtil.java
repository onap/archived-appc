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

import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;



import javax.xml.bind.JAXBException;

public class TestJAXBUtil {

    @Before
    public void setUp() {
    }

    @Test
    public void tesstToObject() {
      String xmlStr = "<vnfId>I1</vnfId><vnfType>T1</vnfType>";
      TestJSONUtilVnf jOut = null;
        try {
            jOut = JAXBUtil.toObject(xmlStr, TestJSONUtilVnf.class);
            assertNotEquals(jOut, null);
        } catch (JAXBException uioe) {
        }
    }

}
