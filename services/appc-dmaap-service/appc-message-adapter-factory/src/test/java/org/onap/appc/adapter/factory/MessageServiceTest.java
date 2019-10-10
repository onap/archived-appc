/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
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
package org.onap.appc.adapter.factory;

import org.junit.Assert;
import org.junit.Test;

public class MessageServiceTest {

    private MessageService messageService = MessageService.DMaaP;

    @Test
    public void testName() {
        Assert.assertEquals("DMaaP", messageService.name());
    }

    @Test
    public void testGetValue() {
        Assert.assertEquals("dmaap", messageService.getValue());
    }

    @Test
    public void testParse() {
        Assert.assertEquals(messageService, MessageService.parse("DMAAP"));
    }

    @Test
    public void testParse_Null() {
        Assert.assertEquals(MessageService.DMaaP, MessageService.parse(null));
    }

}
