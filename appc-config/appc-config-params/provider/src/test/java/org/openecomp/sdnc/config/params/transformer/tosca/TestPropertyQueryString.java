/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 *  ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.config.params.transformer.tosca;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdnc.config.params.data.RequestKey;
import org.openecomp.sdnc.config.params.data.ResponseKey;


import java.util.ArrayList;
import java.util.List;


public class TestPropertyQueryString
{
   // @Test
    public void testBuildResponseKeys()
    {
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        String properties= arp.buildResponseKeyExpression(createResponseKeys());
        Assert.assertEquals("<response-keys = address-fqdn:000000000000000000000:ipaddress-v4 , key2:value2:field2>",properties);
    }

    //@Test
    public void testBuildRequestKeys()
    {
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        String properties= arp.buildRequestKeyExpression(createRequestKeys());
        Assert.assertEquals("<request-keys = class-type:interface-ip-address , address_fqdn:m001dbj001p1n004v006 , address_type:v4>",properties);
    }

    //@Test
    public void testEncoding()
    {
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();

        String expected1 = "&lt;class-type&gt;";
        String encoded1 = arp.encode("<class-type>");
        Assert.assertEquals(expected1,encoded1);

        String expected2 = "&lt;&lt;&lt;metallica&lt;&gt;iron_maiden&gt;&gt;&gt;";
        String encoded2 = arp.encode("<<<metallica<>iron_maiden>>>");
        Assert.assertEquals(expected2,encoded2);

        String expected3 = "band-list&colon;metallica&comma;ironmaiden";
        String encoded3 = arp.encode("band-list:metallica,ironmaiden");
        Assert.assertEquals(expected3,encoded3);

        String expected4 = "motorhead&equals;lemmy";
        String encoded4 = arp.encode("motorhead=lemmy");
        Assert.assertEquals(expected4,encoded4);

        String expected5 = "DreamTheater";
        String encoded5 = arp.encode("  DreamTheater  ");
        Assert.assertEquals(expected5,encoded5);
    }

    //@Test
    public void testBuildRuleType()
    {
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        String input = "IPV4";
        String expected = "<rule-type = IPV4>";
        Assert.assertEquals(expected,arp.buildRuleType(input));
    }

   // @Test
    public void testRuleTypeSetNull()
    {
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        String expected = "<rule-type = >";
        Assert.assertEquals(expected,arp.buildRuleType(null));
    }

    //@Test
    public void testBuildRequestKeysWithKeyNull()
    {
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        List<RequestKey> requestKeyList = new ArrayList<RequestKey>();
        requestKeyList.add(null);
        String properties= arp.buildRequestKeyExpression(requestKeyList);
        Assert.assertEquals("<request-keys = >",properties);
    }

    //@Test
    public void testBuildResponseKeysWithKeyNull()
    {
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        List<ResponseKey> responseKeyList = new ArrayList<ResponseKey>();
        responseKeyList.add(null);
        String properties= arp.buildResponseKeyExpression(responseKeyList);
        Assert.assertEquals("<response-keys = >",properties);
    }

    //@Test
    public void testBuildSourceSystem()
    {
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        Assert.assertEquals("<source-system = INSTAR>",arp.buildSourceSystem("INSTAR"));
    }
    //@Test
    private List<RequestKey> createRequestKeys()
    {
        //Create RequestKey object 1
        RequestKey requestKey1 = new RequestKey();
        requestKey1.setKeyName("class-type");
        requestKey1.setKeyValue("interface-ip-address");

        //Create RequestKey object 2
        RequestKey requestKey2 = new RequestKey();
        requestKey2.setKeyName("address_fqdn");
        requestKey2.setKeyValue("00000000000000");

        //Create RequestKey object 3
        RequestKey requestKey3 = new RequestKey();
        requestKey3.setKeyName("address_type");
        requestKey3.setKeyValue("v4");

        //Add the RequestKey Objects to the List
        List<RequestKey> requestKeyList = new ArrayList<RequestKey>();
        requestKeyList.add(requestKey1);
        requestKeyList.add(requestKey2);
        requestKeyList.add(requestKey3);
        return  requestKeyList;
    }
	//@Test
    private List<ResponseKey> createResponseKeys()
    {
        //Create RequestKey object 1
        ResponseKey responseKey1 = new ResponseKey();

        responseKey1.setUniqueKeyName("address-fqdn");
        responseKey1.setUniqueKeyValue("0000000000000");
        responseKey1.setFieldKeyName("ipaddress-v4");

        ResponseKey responseKey2 = new ResponseKey();
        responseKey2.setUniqueKeyName("key2");
        responseKey2.setUniqueKeyValue("value2");
        responseKey2.setFieldKeyName("field2");


        //Add the RequestKey Objects to the List
        List<ResponseKey> responseKeyList = new ArrayList<ResponseKey>();
        responseKeyList.add(responseKey1);
        responseKeyList.add(responseKey2);

        return  responseKeyList;
    }
}
