/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.dg.flowbuilder;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.dg.flowbuilder.FlowBuilder;
import org.openecomp.appc.dg.flowbuilder.exception.InvalidDependencyModel;
import org.openecomp.appc.dg.flowbuilder.impl.FlowBuilderFactory;
import org.openecomp.appc.dg.objects.*;
import org.openecomp.appc.domainmodel.Vnf;
import org.openecomp.appc.domainmodel.Vnfc;
import org.openecomp.appc.domainmodel.Vserver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class TestFlowBuilder {
    
    @Test
    public void testForwardFlowBuilder(){
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.FORWARD);
        VnfcDependencyModel dependencyModel = readDependencyModel();
        InventoryModel inventoryModel = readInventoryModel();
        VnfcFlowModel flowModel = builder.buildFlowModel(dependencyModel,inventoryModel);
        Iterator<List<Vnfc>> itr = flowModel.getModelIterator();

        List<Vnfc> list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("SMP","Active-Passive","SMP_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("BE","Active-Active","BE_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("FE","Active-Active","FE_Name")));
    }

    @Test
    public void testReverseFlowBuilder(){
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.REVERSE);
        VnfcDependencyModel dependencyModel = readDependencyModel();
        InventoryModel inventoryModel = readInventoryModel();
        VnfcFlowModel flowModel = builder.buildFlowModel(dependencyModel,inventoryModel);
        Iterator<List<Vnfc>> itr = flowModel.getModelIterator();

        List<Vnfc> list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("FE","Active-Active","FE_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("BE","Active-Active","BE_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("SMP","Active-Passive","SMP_Name")));
    }

    @Test
    public void testComplexFlowBuilderForward(){
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.FORWARD);
        VnfcDependencyModel dependencyModel = readComplexDependencyModel();
        InventoryModel inventoryModel = readComplexInventoryModel();
        VnfcFlowModel flowModel = builder.buildFlowModel(dependencyModel,inventoryModel);
        Iterator<List<Vnfc>> itr = flowModel.getModelIterator();

        List<Vnfc> list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("A","Active-Active","A_Name")));
        Assert.assertTrue(list.contains(new Vnfc("E","Active-Active","E_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("B","Active-Active","B_Name")));
        Assert.assertTrue(list.contains(new Vnfc("C","Active-Active","C_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("D","Active-Active","D_Name")));
        Assert.assertTrue(list.contains(new Vnfc("F","Active-Active","F_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("G","Active-Active","G_Name")));

    }

    @Test
    public void testComplexFlowBuilderReverse(){
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.REVERSE);
        VnfcDependencyModel dependencyModel = readComplexDependencyModel();
        InventoryModel inventoryModel = readComplexInventoryModel();
        VnfcFlowModel flowModel = builder.buildFlowModel(dependencyModel,inventoryModel);
        Iterator<List<Vnfc>> itr = flowModel.getModelIterator();

        List<Vnfc> list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("D","Active-Active","D_Name")));

        Assert.assertTrue(list.contains(new Vnfc("G","Active-Active","G_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("B","Active-Active","B_Name")));
        Assert.assertTrue(list.contains(new Vnfc("F","Active-Active","F_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("C","Active-Active","C_Name")));

        list = itr.next();
        Assert.assertTrue(list.contains(new Vnfc("E","Active-Active","E_Name")));
        Assert.assertTrue(list.contains(new Vnfc("A","Active-Active","A_Name")));

    }

    @Test(expected = InvalidDependencyModel.class)
    public void testCyclicBuilder(){
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.FORWARD);
        VnfcDependencyModel dependencyModel = readCyclicDependencyModel();
        InventoryModel inventoryModel = readInventoryModel();
        builder.buildFlowModel(dependencyModel,inventoryModel);
    }

    @Test(expected = InvalidDependencyModel.class)
    public void testCyclicBuilderWithRootNode(){
        FlowBuilder builder = FlowBuilderFactory.getInstance().getFlowBuilder(FlowStrategies.FORWARD);
        VnfcDependencyModel dependencyModel = readCyclicDependencyModelWithRootNode();
        InventoryModel inventoryModel = readInventoryModel();
        builder.buildFlowModel(dependencyModel,inventoryModel);
    }

    private VnfcDependencyModel readCyclicDependencyModelWithRootNode() {
        Vnfc a = new Vnfc("A","Active-Passive",null);
        Vnfc b = new Vnfc("B","Active-Active",null);
        Vnfc c = new Vnfc("C","Active-Active",null);


        Node aNode = new Node(a);
        Node bNode = new Node(b);
        Node cNode = new Node(c);

        bNode.addParent(c);
        cNode.addParent(b);


        Set<Node<Vnfc>> dependencies = new HashSet<>();
        dependencies.add(aNode);
        dependencies.add(bNode);
        dependencies.add(cNode);

        return new VnfcDependencyModel(dependencies);
    }

    private InventoryModel readComplexInventoryModel() {
        Vnf vnf = new Vnf("vnf_1","vABCD","1");

        Vnfc vnfcA = new Vnfc("A","Active-Active","A_Name");
        Vnfc vnfcB = new Vnfc("B","Active-Active","B_Name");
        Vnfc vnfcC = new Vnfc("C","Active-Active","C_Name");
        Vnfc vnfcD = new Vnfc("D","Active-Active","D_Name");
        Vnfc vnfcE = new Vnfc("E","Active-Active","E_Name");
        Vnfc vnfcF = new Vnfc("F","Active-Active","F_Name");
        Vnfc vnfcG = new Vnfc("G","Active-Active","G_Name");

        vnfcA.addVm(new Vserver("VM_URL_A1"));
        vnfcB.addVm(new Vserver("VM_URL_B1"));
        vnfcC.addVm(new Vserver("VM_URL_C1"));
        vnfcD.addVm(new Vserver("VM_URL_D1"));
        vnfcE.addVm(new Vserver("VM_URL_E1"));
        vnfcF.addVm(new Vserver("VM_URL_F1"));
        vnfcG.addVm(new Vserver("VM_URL_G1"));

        vnf.addVnfc(vnfcA);
        vnf.addVnfc(vnfcB);
        vnf.addVnfc(vnfcC);
        vnf.addVnfc(vnfcD);
        vnf.addVnfc(vnfcE);
        vnf.addVnfc(vnfcF);
        vnf.addVnfc(vnfcG);

        return new InventoryModel(vnf);
    }

    private VnfcDependencyModel readComplexDependencyModel() {
        Vnfc a = new Vnfc("A","Active-Active",null);
        Vnfc b = new Vnfc("B","Active-Active",null);
        Vnfc c = new Vnfc("C","Active-Active",null);
        Vnfc d = new Vnfc("D","Active-Active",null);
        Vnfc e = new Vnfc("E","Active-Active",null);
        Vnfc f = new Vnfc("F","Active-Active",null);
        Vnfc g = new Vnfc("G","Active-Active",null);


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

        Vnfc a = new Vnfc("A","Active-Passive",null);
        Vnfc b = new Vnfc("B","Active-Active",null);
        Vnfc c = new Vnfc("C","Active-Active",null);
        Vnfc d = new Vnfc("D","Active-Active",null);


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
        Vnf vnf = new Vnf("vnf_1","vSCP","1");

        Vnfc smp = new Vnfc("SMP",null,"SMP_Name");
        Vserver smpVm1 = new Vserver("SMP_URL1");
        Vserver smpVm2 = new Vserver("SMP_URL2");

        smp.addVm(smpVm1);
        smp.addVm(smpVm2);

        Vnfc be = new Vnfc("BE",null,"BE_Name");

        Vserver beVm1 = new Vserver("BE_URL1");
        Vserver beVm2 = new Vserver("BE_URL2");
        Vserver beVm3 = new Vserver("BE_URL3");
        Vserver beVm4 = new Vserver("BE_URL4");
        Vserver beVm5 = new Vserver("BE_URL5");

        be.addVm(beVm1);
        be.addVm(beVm2);
        be.addVm(beVm3);
        be.addVm(beVm4);
        be.addVm(beVm5);

        Vnfc fe = new Vnfc("FE",null,"FE_Name");

        Vserver feVm1 = new Vserver("FE_URL1");
        Vserver feVm2 = new Vserver("FE_URL2");

        fe.addVm(feVm1);
        fe.addVm(feVm2);

        vnf.addVnfc(smp);
        vnf.addVnfc(be);
        vnf.addVnfc(fe);

        return new InventoryModel(vnf);
    }

    private VnfcDependencyModel readDependencyModel() {
        Vnfc smp = new Vnfc("SMP","Active-Passive",null);
        Vnfc be = new Vnfc("BE","Active-Active",null);
        Vnfc fe = new Vnfc("FE","Active-Active",null);


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
