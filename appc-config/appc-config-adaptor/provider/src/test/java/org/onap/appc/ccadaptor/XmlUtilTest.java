
/*============LICENSE_START=======================================================
 * ONAP : APPC
 *================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *============LICENSE_END=========================================================
*/

package org.onap.appc.ccadaptor;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class XmlUtilTest {

   @Test
    public void testXml() {
        Map<String, String> varmap = new HashMap<String, String>();
        varmap.put("network.data", "test");
        String xmlData = XmlUtil.getXml(varmap, "network");
        Assert.assertEquals("<data>test</data>\n", xmlData);
    }
 
    @Test
    public void testXml2 () {
        Map<String, String> varmap = new HashMap<String, String>();
        varmap.put("network.data", "testData");
        varmap.put("network.dt[0]", "test0");
        varmap.put("network.dt[1]", "test1");
        varmap.put("network.dt_length", "2");
        String xmlData = XmlUtil.getXml(varmap, "network");
        Assert.assertTrue(xmlData.contains("<data>testData</data>") );
        Assert.assertTrue(xmlData.contains("test0</dt>") );
        Assert.assertTrue(xmlData.contains("test1</dt>") );
        Assert.assertTrue(xmlData.contains("<dt_length>2") );
    }
}