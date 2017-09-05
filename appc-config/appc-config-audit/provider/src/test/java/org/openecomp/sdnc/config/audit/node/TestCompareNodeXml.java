/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.config.audit.node;

import java.io.IOException;
import java.util.HashMap;
import org.junit.Test;
import org.openecomp.sdnc.config.audit.node.CompareNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class TestCompareNodeXml {
    private static final Logger log = LoggerFactory.getLogger(TestCompareNodeXml.class);

    @Test
    public void TestCompareExtactXML() throws SvcLogicException {
        log.debug("TestCompareNode.TestCompareExtactXML()");
        SvcLogicContext ctx = new SvcLogicContext();
        HashMap<String, String> testMap = new HashMap<String, String>();
        CompareNode cmp = new CompareNode();
        String s = "<configuration  xmlns=" + "\"http://xml.juniper.net/xnm/1.1/xnm\"" + " junos:commit-seconds="
                + "\"1473957536\" " + "junos:commit-localtime=" + "\"2016-09-15 16:38:56 UTC\" " + "junos:commit-user="
                + "\"root\"" + "><name>Test</name></configuration>";

        String t = "<configuration  xmlns=" + "\"http://xml.juniper.net/xnm/1.1/xnm\"" + " junos:commit-seconds="
                + "\"1473957536\" " + "junos:commit-localtime=" + "\"2016-09-15 16:38:56 UTC\" " + "junos:commit-user="
                + "\"root\"" + "><name>Test</name></configuration>";
        testMap.put("compareDataType", "RESTCONF-XML");
        testMap.put("requestIdentifier", "123");
        testMap.put("sourceData", s);
        testMap.put("targetData", t);
        cmp.compare(testMap, ctx);
        assert (ctx.getAttribute("123." + "STATUS").equals("SUCCESS"));
    }

    @Test
    public void TestCompareforAttributeOrder() throws IOException, SvcLogicException {
        log.debug("TestCompareNode.TestCompareforAttributeOrder()");
        SvcLogicContext ctx = new SvcLogicContext();
        HashMap<String, String> testMap = new HashMap<String, String>();
        CompareNode cmp = new CompareNode();
        testMap.put("compareDataType", "XML");
        testMap.put("sourceData",
                "<SipIfTermination><id>2</id><udpPortInUse>true</udpPortInUse><udpPort>5060</udpPort><tcpPortInUse>true</tcpPortInUse><tcpPort>5060</tcpPort></SipIfTermination>");
        testMap.put("targetData",
                "<SipIfTermination><udpPortInUse>true</udpPortInUse><udpPort>5060</udpPort><tcpPortInUse>true</tcpPortInUse><tcpPort>5060</tcpPort><id>2</id></SipIfTermination>");
        cmp.compare(testMap, ctx);
        assert (ctx.getAttribute("STATUS").equals("SUCCESS"));
    }

    @Test
    public void TestCompareForComments() throws SvcLogicException {
        log.debug("TestCompareNode.TestCompareForComments()");
        SvcLogicContext ctx = new SvcLogicContext();
        HashMap<String, String> testMap = new HashMap<String, String>();
        CompareNode cmp = new CompareNode();
        testMap.put("compareDataType", "XML");
        testMap.put("sourceData", "<SipIfTermination><id>2</id><!--this is a commnect --></SipIfTermination>");
        testMap.put("targetData", "<SipIfTermination><id>2</id></SipIfTermination>");
        cmp.compare(testMap, ctx);
        assert (ctx.getAttribute("STATUS").equals("SUCCESS"));
    }

}
