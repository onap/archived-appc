/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM. All rights reserved.
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.seqgen.objects;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet; 
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onap.appc.dg.objects.InventoryModel;
import org.onap.appc.dg.objects.VnfcDependencyModel;
import org.onap.appc.domainmodel.Vnf;

public class SequenceGeneratorInputBuilderTest {
    
    private SequenceGeneratorInputBuilder sequenceGeneratorInputBuilder;
    
    @Before
    public void setUp()
    {
        sequenceGeneratorInputBuilder= new SequenceGeneratorInputBuilder();
    }
    
    @Test
    public void testRequestInfo()
    {
        RequestInfo requestInfo=  new RequestInfo();
        assertTrue(sequenceGeneratorInputBuilder.requestInfo(requestInfo) instanceof SequenceGeneratorInputBuilder);
        
    }
    
    @Test
    public void testCapability()
    {
        String level= "testLevel";
        List<String> capabilities= new ArrayList<>();
        assertTrue(sequenceGeneratorInputBuilder.capability(level, capabilities) instanceof SequenceGeneratorInputBuilder);
        
    }
    
    @Test
    public void testTunableParameter()
    {
        assertTrue(sequenceGeneratorInputBuilder.tunableParameter("key", "value") instanceof SequenceGeneratorInputBuilder);
        
    }
    
    @Test
    public void testInventoryModel()
    {
        InventoryModel model= new InventoryModel(new Vnf());
        assertTrue(sequenceGeneratorInputBuilder.inventoryModel(model) instanceof SequenceGeneratorInputBuilder);
        
    }
    
    @Test
    public void testDependencyModel()
    {
        VnfcDependencyModel model= new VnfcDependencyModel(new HashSet<>());
        assertTrue(sequenceGeneratorInputBuilder.dependendcyModel(model) instanceof SequenceGeneratorInputBuilder);
        
    }
    
    @Test
    public void testBuild()
    {
        
        assertTrue(sequenceGeneratorInputBuilder.build() instanceof SequenceGeneratorInput);
        
    }

}
