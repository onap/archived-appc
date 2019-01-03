/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.dg.flowbuilder;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.dg.flowbuilder.FlowBuilder;
import org.onap.appc.dg.flowbuilder.exception.InvalidDependencyModelException;
import org.onap.appc.dg.flowbuilder.impl.FlowBuilderFactory;
import org.onap.appc.dg.objects.*;
import org.onap.appc.domainmodel.Vnf;
import org.onap.appc.domainmodel.Vnfc;
import org.onap.appc.domainmodel.Vserver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class TestFlowBuilder {
    @Test
    public void testForwardFlowBuilder() throws InvalidDependencyModelException {
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.FORWARD);
        VnfcDependencyModel dependencyModel = readDependencyModel();
        InventoryModel inventoryModel = readInventoryModel();
        VnfcFlowModel flowModel = builder.buildFlowModel(dependencyModel,inventoryModel);
        Iterator<List<Vnfc>> itr = flowModel.getModelIterator();

        List<Vnfc> list = itr.next();
        Assert.assertTrue(list.contains(createVnfc("SMP","Active-Passive","SMP_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(createVnfc("BE","Active-Active","BE_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(createVnfc("FE","Active-Active","FE_Name")));
    }

    private Vnfc createVnfc(String vnfcType,String resilienceType,String vnfcName) {
        Vnfc vnfc = new Vnfc();
        vnfc.setVnfcType(vnfcType);
        vnfc.setVnfcName(vnfcName);
        vnfc.setResilienceType(resilienceType);
        return vnfc;
    }

    @Test
    public void testReverseFlowBuilder() throws InvalidDependencyModelException {
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.REVERSE);
        VnfcDependencyModel dependencyModel = readDependencyModel();
        InventoryModel inventoryModel = readInventoryModel();
        VnfcFlowModel flowModel = builder.buildFlowModel(dependencyModel,inventoryModel);
        Iterator<List<Vnfc>> itr = flowModel.getModelIterator();

        List<Vnfc> list = itr.next();
        Assert.assertTrue(list.contains(createVnfc("FE","Active-Active","FE_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(createVnfc("BE","Active-Active","BE_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(createVnfc("SMP","Active-Passive","SMP_Name")));

        Assert.assertThat(flowModel.toString(), CoreMatchers.containsString("Flow Model : Vnfc : vnfcType = FE"));
    }

    @Test
    public void testComplexFlowBuilderForward() throws InvalidDependencyModelException {
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.FORWARD);
        VnfcDependencyModel dependencyModel = readComplexDependencyModel();
        InventoryModel inventoryModel = readComplexInventoryModel();
        VnfcFlowModel flowModel = builder.buildFlowModel(dependencyModel,inventoryModel);
        Iterator<List<Vnfc>> itr = flowModel.getModelIterator();
        try{
            List<Vnfc> list = itr.next();
            Assert.assertTrue(list.contains(createVnfc("A","Active-Active","A_Name")));
            Assert.assertTrue(list.contains(createVnfc("E","Active-Active","E_Name")));

            list = itr.next();
            Assert.assertTrue(list.contains(createVnfc("B","Active-Active","B_Name")));
            Assert.assertTrue(list.contains(createVnfc("C","Active-Active","C_Name")));

            list = itr.next();
            Assert.assertTrue(list.contains(createVnfc("D","Active-Active","D_Name")));
            Assert.assertTrue(list.contains(createVnfc("F","Active-Active","F_Name")));

            list = itr.next();
            Assert.assertTrue(list.contains(createVnfc("G","Active-Active","G_Name")));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testComplexFlowBuilderReverse() throws InvalidDependencyModelException {
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.REVERSE);
        VnfcDependencyModel dependencyModel = readComplexDependencyModel();
        InventoryModel inventoryModel = readComplexInventoryModel();
        VnfcFlowModel flowModel = builder.buildFlowModel(dependencyModel,inventoryModel);
        Iterator<List<Vnfc>> itr = flowModel.getModelIterator();

        List<Vnfc> list = itr.next();
        Assert.assertTrue(list.contains(createVnfc("D","Active-Active","D_Name")));

        Assert.assertTrue(list.contains(createVnfc("G","Active-Active","G_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(createVnfc("B","Active-Active","B_Name")));
        Assert.assertTrue(list.contains(createVnfc("F","Active-Active","F_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(createVnfc("C","Active-Active","C_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(createVnfc("E","Active-Active","E_Name")));
        Assert.assertTrue(list.contains(createVnfc("A","Active-Active","A_Name")));

    }

    @Test(expected = InvalidDependencyModelException.class)
    public void testCyclicBuilder() throws InvalidDependencyModelException {
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.findByString("FORWARD"));
        VnfcDependencyModel dependencyModel = readCyclicDependencyModel();
        InventoryModel inventoryModel = readInventoryModel();
        builder.buildFlowModel(dependencyModel,inventoryModel);
    }

    @Test(expected = InvalidDependencyModelException.class)
    public void testCyclicBuilderWithRootNode() throws InvalidDependencyModelException {
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.FORWARD);
        VnfcDependencyModel dependencyModel = readCyclicDependencyModelWithRootNode();
        InventoryModel inventoryModel = readInventoryModel();
        builder.buildFlowModel(dependencyModel,inventoryModel);
    }

    private VnfcDependencyModel readCyclicDependencyModelWithRootNode() {
        Vnfc a = createVnfc("A","Active-Passive",null);
        Vnfc b = createVnfc("B","Active-Active",null);
        Vnfc c = createVnfc("C","Active-Active",null);


        Node aNode = new Node(a);
        Node bNode = new Node(b);
        Node cNode = new Node(c);

        Assert.assertTrue(aNode.equals(aNode));
        Assert.assertFalse(aNode.equals(bNode));

        bNode.addParent(c);
        cNode.addParent(b);


        Set<Node<Vnfc>> dependencies = new HashSet<>();
        dependencies.add(aNode);
        dependencies.add(bNode);
        dependencies.add(cNode);

        return new VnfcDependencyModel(dependencies);
    }

    private InventoryModel readComplexInventoryModel() {
        Vnf vnf = createVnf("vnf_1","vABCD","1");

        Vnfc vnfcA = createVnfc("A","Active-Active","A_Name");
        Vnfc vnfcB = createVnfc("B","Active-Active","B_Name");
        Vnfc vnfcC = createVnfc("C","Active-Active","C_Name");
        Vnfc vnfcD = createVnfc("D","Active-Active","D_Name");
        Vnfc vnfcE = createVnfc("E","Active-Active","E_Name");
        Vnfc vnfcF = createVnfc("F","Active-Active","F_Name");
        Vnfc vnfcG = createVnfc("G","Active-Active","G_Name");

        vnf.addVserver(createVserver("VM_URL_A1",vnfcA));
        vnf.addVserver(createVserver("VM_URL_B1",vnfcB));
        vnf.addVserver(createVserver("VM_URL_C1",vnfcC));
        vnf.addVserver(createVserver("VM_URL_D1",vnfcD));
        vnf.addVserver(createVserver("VM_URL_E1",vnfcE));
        vnf.addVserver(createVserver("VM_URL_F1",vnfcF));
        vnf.addVserver(createVserver("VM_URL_G1",vnfcG));

        return new InventoryModel(vnf);
    }

    private Vnf createVnf(String vnfId,String vnfType,String vnfVersion) {
        Vnf vnf = new Vnf();
        vnf.setVnfId(vnfId);
        vnf.setVnfType(vnfType);
        vnf.setVnfVersion(vnfVersion);
        return vnf;
    }

    private VnfcDependencyModel readComplexDependencyModel() {
        Vnfc a = createVnfc("A","Active-Active",null);
        Vnfc b = createVnfc("B","Active-Active",null);
        Vnfc c = createVnfc("C","Active-Active",null);
        Vnfc d = createVnfc("D","Active-Active",null);
        Vnfc e = createVnfc("E","Active-Active",null);
        Vnfc f = createVnfc("F","Active-Active",null);
        Vnfc g = createVnfc("G","Active-Active",null);


        Node aNode = new Node(a);
        Node bNode = new Node(b);
        Node cNode = new Node(c);
        Node dNode = new Node(d);
        Node eNode = new Node(e);
        Node fNode = new Node(f);
        Node gNode = new Node(g);

        bNode.addParent(a);
        cNode.addParent(a);

        bNode.addParent(e);
        cNode.addParent(e);

        dNode.addParent(b);
        gNode.addParent(b);

        fNode.addParent(c);

        gNode.addParent(f);

        Set<Node<Vnfc>> dependencies = new HashSet<>();
        dependencies.add(aNode);
        dependencies.add(bNode);
        dependencies.add(cNode);
        dependencies.add(dNode);
        dependencies.add(eNode);
        dependencies.add(fNode);
        dependencies.add(gNode);

        return new VnfcDependencyModel(dependencies);
    }

    private VnfcDependencyModel readCyclicDependencyModel() {

        Vnfc a = createVnfc("A","Active-Passive",null);
        Vnfc b = createVnfc("B","Active-Active",null);
        Vnfc c = createVnfc("C","Active-Active",null);
        Vnfc d = createVnfc("D","Active-Active",null);


        Node aNode = new Node(a);
        Node bNode = new Node(b);
        Node cNode = new Node(c);
        Node dNode = new Node(d);

        bNode.addParent(a);

        bNode.addParent(d);
        dNode.addParent(c);
        cNode.addParent(b);


        Set<Node<Vnfc>> dependencies = new HashSet<>();
        dependencies.add(aNode);
        dependencies.add(bNode);
        dependencies.add(cNode);
        dependencies.add(dNode);

        return new VnfcDependencyModel(dependencies);

    }

    private InventoryModel readInventoryModel() {
        Vnf vnf = createVnf("vnf_1","vSCP","1");

        Vnfc smp = createVnfc("SMP",null,"SMP_Name");
        Vserver smpVm1 = createVserver("SMP_URL1",smp);
        Vserver smpVm2 = createVserver("SMP_URL2",smp);

        vnf.addVserver(smpVm1);
        vnf.addVserver(smpVm2);

        Vnfc be = createVnfc("BE",null,"BE_Name");

        Vserver beVm1 = createVserver("BE_URL1",be);
        Vserver beVm2 = createVserver("BE_URL2",be);
        Vserver beVm3 = createVserver("BE_URL3",be);
        Vserver beVm4 = createVserver("BE_URL4",be);
        Vserver beVm5 = createVserver("BE_URL5",be);

        vnf.addVserver(beVm1);
        vnf.addVserver(beVm2);
        vnf.addVserver(beVm3);
        vnf.addVserver(beVm4);
        vnf.addVserver(beVm5);

        Vnfc fe = createVnfc("FE",null,"FE_Name");

        Vserver feVm1 = createVserver("FE_URL1",fe);
        Vserver feVm2 = createVserver("FE_URL2",fe);

        vnf.addVserver(feVm1);
        vnf.addVserver(feVm2);

        return new InventoryModel(vnf);
    }

    private Vserver createVserver(String url,Vnfc vnfc) {
        Vserver vserver = new Vserver();
        vserver.setUrl(url);
        vserver.setVnfc(vnfc);
        vnfc.addVserver(vserver);
        return vserver;
    }

    private VnfcDependencyModel readDependencyModel() {
        Vnfc smp = createVnfc("SMP","Active-Passive",null);
        Vnfc be = createVnfc("BE","Active-Active",null);
        Vnfc fe = createVnfc("FE","Active-Active",null);


        Node smpNode = new Node(smp);
        Node beNode = new Node(be);
        Node feNode = new Node(fe);

        beNode.addParent(smp);
        feNode.addParent(be);

        Set<Node<Vnfc>> dependencies = new HashSet<>();
        dependencies.add(smpNode);
        dependencies.add(feNode);
        dependencies.add(beNode);

        return new VnfcDependencyModel(dependencies);
    }
}
