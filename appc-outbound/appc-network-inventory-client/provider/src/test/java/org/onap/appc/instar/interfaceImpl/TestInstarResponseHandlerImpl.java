/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.instar.interfaceImpl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.appc.instar.utils.InstarClientConstant;
import org.onap.sdnc.config.params.data.ResponseKey;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import static org.junit.Assert.assertNotNull;

public class TestInstarResponseHandlerImpl {

    @Test
    public void testProcessResponseForIpv4() {
        ResponseKey resKey = new ResponseKey();
        resKey.setUniqueKeyValue("x");
        SvcLogicContext svc = new SvcLogicContext();
        svc.setAttribute("vnf-name", "fqdn");
        resKey.setFieldKeyName("ipaddress-v4");
        String instarKey = "key";
        String instarRes ="{\"vnfConfigurationParameterDetails\":"
                + "[{\"fqdn\":\"fqdnx\",\"v4IPAddress\":\"value2\"}]}";
        InstarResponseHandlerImpl impl = new InstarResponseHandlerImpl(resKey,svc);
        impl.processResponse(instarRes, instarKey);
        assertNotNull(resKey);
    }

    @Test
    public void testProcessResponseForIpv6() {
        ResponseKey resKey = new ResponseKey();
        resKey.setUniqueKeyValue("x");
        SvcLogicContext svc = new SvcLogicContext();
        svc.setAttribute("vnf-name", "fqdn");
        resKey.setFieldKeyName("ipaddress-v6");
        String instarKey = "key";
        String instarRes ="{\"vnfConfigurationParameterDetails\":"
                + "[{\"fqdn\":\"fqdnx\",\"v6IPAddress\":\"value2\"}]}";
        InstarResponseHandlerImpl impl = new InstarResponseHandlerImpl(resKey,svc);
        impl.processResponse(instarRes, instarKey);
        assertNotNull(resKey);
    }

    @Test
    public void testProcessResponseInstarKeyValues() {
        ResponseKey resKey = new ResponseKey();
        resKey.setUniqueKeyValue("x");
        String json ="{\"vnfConfigurationParameterDetails\":"
                + "[{\"fqdn\":\"fqdnx\",\"v4IPAddress\":\"value2\"}]}";
        SvcLogicContext svc = new SvcLogicContext();
        svc.setAttribute("vnf-name", "fqdn");
        svc.setAttribute("INSTAR-KEY-VALUES", json);
        resKey.setFieldKeyName("ipaddress-v6");
        String instarKey = "key";
        String instarRes ="{\"vnfConfigurationParameterDetails\":"
                + "[{\"fqdn\":\"fqdnx\",\"v6IPAddress\":\"value2\"}]}";
        InstarResponseHandlerImpl impl = new InstarResponseHandlerImpl(resKey,svc);
        impl.processResponse(instarRes, instarKey);
        assertNotNull(impl);
    }

    @Test
    public void testV4SubNet() {
        ResponseKey resKey = new ResponseKey();
        resKey.setUniqueKeyValue("x");
        resKey.setFieldKeyName(InstarClientConstant.INSTAR_V4_SUBNET);
        String json ="{\"v4IpAddress\":\"ipAddressV4\"}]}";
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-name", "fqdn");
        ctx.setAttribute("INSTAR-KEY-VALUES", json);
        String instarKey = "V4SubnetParameter";
        String instarRes ="{\"vnfConfigurationParameterDetails\":"
                + "[{\"fqdn\":\"fqdnx\",\"v4Subnet\":\"subnetv4\"}]}";
        InstarResponseHandlerImpl impl = new InstarResponseHandlerImpl(resKey,ctx);
        impl.processResponse(instarRes, instarKey);
        String values = ctx.getAttribute("INSTAR-KEY-VALUES");
        assertTrue(values.contains("subnetv4"));
    }

    @Test
    public void testV6SubNet() {
        ResponseKey resKey = new ResponseKey();
        resKey.setUniqueKeyValue("x");
        resKey.setFieldKeyName(InstarClientConstant.INSTAR_V6_SUBNET);
        String json ="{\"v6IpAddress\":\"ipAddressV6\"}]}";
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-name", "fqdn");
        ctx.setAttribute("INSTAR-KEY-VALUES", json);
        String instarKey = "V6SubnetParameter";
        String instarRes ="{\"vnfConfigurationParameterDetails\":"
                + "[{\"fqdn\":\"fqdnx\",\"v6Subnet\":\"subnetv6\"}]}";
        InstarResponseHandlerImpl impl = new InstarResponseHandlerImpl(resKey,ctx);
        impl.processResponse(instarRes, instarKey);
        String values = ctx.getAttribute("INSTAR-KEY-VALUES");
        assertTrue(values.contains("subnetv6"));
    }

    @Test
    public void testV6DefaultGateway() {
        ResponseKey resKey = new ResponseKey();
        resKey.setUniqueKeyValue("x");
        resKey.setFieldKeyName(InstarClientConstant.INSTAR_V6_DEFAULT_GATEWAY);
        String json ="{\"v6IpAddress\":\"ipAddressV6\"}]}";
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-name", "fqdn");
        ctx.setAttribute("INSTAR-KEY-VALUES", json);
        String instarKey = "V6DefaultGatewayParameter";
        String instarRes ="{\"vnfConfigurationParameterDetails\":"
                + "[{\"fqdn\":\"fqdnx\",\"v6DefaultGateway\":\"defaultGatewayV6\"}]}";
        InstarResponseHandlerImpl impl = new InstarResponseHandlerImpl(resKey,ctx);
        impl.processResponse(instarRes, instarKey);
        String values = ctx.getAttribute("INSTAR-KEY-VALUES");
        assertTrue(values.contains("defaultGatewayV6"));
    }

    @Test
    public void testV4DefaultGateway() {
        ResponseKey resKey = new ResponseKey();
        resKey.setUniqueKeyValue("x");
        resKey.setFieldKeyName(InstarClientConstant.INSTAR_V4_DEFAULT_GATEWAY);
        String json ="{\"v4IpAddress\":\"ipAddressV4\"}]}";
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-name", "fqdn");
        ctx.setAttribute("INSTAR-KEY-VALUES", json);
        String instarKey = "V4DefaultGatewayParameter";
        String instarRes ="{\"vnfConfigurationParameterDetails\":"
                + "[{\"fqdn\":\"fqdnx\",\"v4DefaultGateway\":\"defaultGatewayV4\"}]}";
        InstarResponseHandlerImpl impl = new InstarResponseHandlerImpl(resKey,ctx);
        impl.processResponse(instarRes, instarKey);
        String values = ctx.getAttribute("INSTAR-KEY-VALUES");
        assertTrue(values.contains("defaultGatewayV4"));
    }
}
