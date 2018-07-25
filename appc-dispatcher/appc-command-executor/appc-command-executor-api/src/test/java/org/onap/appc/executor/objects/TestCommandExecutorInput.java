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
*/
package org.onap.appc.executor.objects;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.onap.appc.domainmodel.lcm.RuntimeContext;

public class TestCommandExecutorInput {

    private CommandExecutorInput commandExecutorInput;
    @Before
    public void setUp() {
        commandExecutorInput=new CommandExecutorInput();
        
    }
    
    @Test
    public void testGetTtl() {
        commandExecutorInput.setTtl(180);
        assertNotNull(commandExecutorInput.getTtl());
        assertEquals(commandExecutorInput.getTtl(), 180);
    }
    @Test
    public void testTostring() {
        assertTrue(commandExecutorInput.toString().contains("CommandExecutorInput"));
    }
    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(commandExecutorInput.toString(), "");
        assertNotEquals(commandExecutorInput.toString(), null);

    }
    
    @Test
    public void testGetRuntimeContext() {
        RuntimeContext runtime= new RuntimeContext();
        commandExecutorInput.setRuntimeContext(runtime);
        assertEquals(runtime, commandExecutorInput.getRuntimeContext());

    }
    
}
