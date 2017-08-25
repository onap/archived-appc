/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.instar.interfaceImpl;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.openecomp.sdnc.config.params.data.Parameter;
import org.openecomp.sdnc.config.params.data.ResponseKey;
import org.openecomp.sdnc.sli.SvcLogicContext;

public class TestInterfaceIpAddressImpl {

    @Test(expected=Exception.class)
    public void testProcessRuleException1() throws Exception{
        Parameter param = new Parameter();
        param.setRuleType("test");
        param.setName("test");
        param.setSource("INSTAR");
        SvcLogicContext svc = new SvcLogicContext();
        svc.setAttribute("vnf-name", "test");
        InterfaceIpAddressImpl impl = new InterfaceIpAddressImpl(param,svc);
        impl.processRule();
    }
    
    @Test(expected=Exception.class)
    public void testProcessRuleException2() throws Exception{
        List<ResponseKey> list = new ArrayList<>();
        list.add(new ResponseKey());
        list.add(new ResponseKey());
        Parameter param = new Parameter();
        param.setResponseKeys(list);
        param.setRuleType("test");
        param.setName("test");
        param.setSource("INSTAR");
        SvcLogicContext svc = new SvcLogicContext();
        svc.setAttribute("vnf-name", "test");
        InterfaceIpAddressImpl impl = new InterfaceIpAddressImpl(param,svc);
        impl.processRule();
        param.setSource("INSTAR1");
        impl.processRule();
    }
}
