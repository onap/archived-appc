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

package org.openecomp.appc.dg.common.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openecomp.appc.dg.common.VnfExecutionFlow;
import org.openecomp.appc.dg.common.impl.Constants;
import org.openecomp.appc.dg.common.impl.VnfExecutionFlowImpl;
import org.openecomp.appc.dg.dependencymanager.DependencyManager;
import org.openecomp.appc.dg.dependencymanager.exception.DependencyModelNotFound;
import org.openecomp.appc.dg.dependencymanager.impl.DependencyModelFactory;
import org.openecomp.appc.dg.flowbuilder.exception.InvalidDependencyModel;
import org.openecomp.appc.dg.objects.DependencyTypes;
import org.openecomp.appc.dg.objects.Node;
import org.openecomp.appc.dg.objects.VnfcDependencyModel;
import org.openecomp.appc.domainmodel.Vnfc;
import org.openecomp.appc.metadata.objects.DependencyModelIdentifier;
import org.osgi.framework.FrameworkUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TestVnfExecutionFlowImpl.class, FrameworkUtil.class,DependencyManager.class,DependencyModelFactory.class})
public class TestVnfExecutionFlowImpl {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestVnfExecutionFlowImpl.class);

    @Before
    public void setUp() {
        logger.setLevel(EELFLogger.Level.DEBUG);
    }

    @Test
    public void testPositiveFlow() throws DependencyModelNotFound {
        Map<String, String> params = prepareParams();
        SvcLogicContext context = prepareContext();
        VnfcDependencyModel dependencyModel = readDependencyModel();

        PowerMockito.mockStatic(DependencyModelFactory.class);
        DependencyManager dependencyManager = PowerMockito.mock(DependencyManager.class);

        PowerMockito.when(DependencyModelFactory.createDependencyManager()).thenReturn(dependencyManager);
        PowerMockito.when(dependencyManager.getVnfcDependencyModel((
                DependencyModelIdentifier) Matchers.any(),(DependencyTypes) Matchers.any()))
                .thenReturn(dependencyModel);

        VnfExecutionFlow vnfExecutionFlow = new VnfExecutionFlowImpl();
        vnfExecutionFlow.getVnfExecutionFlowData(params,context);
    }

    @Test
    public void testComplexFlow() throws DependencyModelNotFound {
        Map<String, String> params = prepareParams();
        SvcLogicContext context = prepareContextForComplexDependency();
        VnfcDependencyModel dependencyModel = readComplexDependencyModel();

        PowerMockito.mockStatic(DependencyModelFactory.class);
        DependencyManager dependencyManager = PowerMockito.mock(DependencyManager.class);

        PowerMockito.when(DependencyModelFactory.createDependencyManager()).thenReturn(dependencyManager);
        PowerMockito.when(dependencyManager.getVnfcDependencyModel((
                DependencyModelIdentifier) Matchers.any(),(DependencyTypes) Matchers.any()))
                .thenReturn(dependencyModel);

        VnfExecutionFlow vnfExecutionFlow = new VnfExecutionFlowImpl();
        vnfExecutionFlow.getVnfExecutionFlowData(params,context);
    }

    @Test(expected = InvalidDependencyModel.class)
    public void testCycleFlow() throws DependencyModelNotFound {
        Map<String, String> params = prepareParams();
        SvcLogicContext context = prepareContextForComplexDependency();
        VnfcDependencyModel dependencyModel = readCyclicDependencyModel();
        PowerMockito.mockStatic(DependencyModelFactory.class);
        DependencyManager dependencyManager = PowerMockito.mock(DependencyManager.class);

        PowerMockito.when(DependencyModelFactory.createDependencyManager()).thenReturn(dependencyManager);
        PowerMockito.when(dependencyManager.getVnfcDependencyModel((
                DependencyModelIdentifier) Matchers.any(),(DependencyTypes) Matchers.any()))
                .thenReturn(dependencyModel);

        VnfExecutionFlow vnfExecutionFlow = new VnfExecutionFlowImpl();
        vnfExecutionFlow.getVnfExecutionFlowData(params,context);
    }

    private VnfcDependencyModel readCyclicDependencyModel() {

        Vnfc a = new Vnfc("A","Active-Passive",null);
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
        cNode.addParent(b);

        bNode.addParent(d);
        dNode.addParent(c);

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

    private SvcLogicContext prepareContextForComplexDependency() {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("input.action-identifiers.vnf-id","1");
        context.setAttribute("vnf.type","vSCP");
        context.setAttribute("vnf.vnfcCount","7");

        context.setAttribute("vnf.vnfc[0].name","A");
        context.setAttribute("vnf.vnfc[0].type","A");
        context.setAttribute("vnf.vnfc[0].vm_count","2");
        context.setAttribute("vnf.vnfc[0].vm[0].url","A1");
        context.setAttribute("vnf.vnfc[0].vm[1].url","A2");

        context.setAttribute("vnf.vnfc[1].name","B");
        context.setAttribute("vnf.vnfc[1].type","B");
        context.setAttribute("vnf.vnfc[1].vm_count","5");
        context.setAttribute("vnf.vnfc[1].vm[0].url","B1");
        context.setAttribute("vnf.vnfc[1].vm[1].url","B2");
        context.setAttribute("vnf.vnfc[1].vm[2].url","B3");
        context.setAttribute("vnf.vnfc[1].vm[3].url","B4");
        context.setAttribute("vnf.vnfc[1].vm[4].url","B5");

        context.setAttribute("vnf.vnfc[2].name","C");
        context.setAttribute("vnf.vnfc[2].type","C");
        context.setAttribute("vnf.vnfc[2].vm_count","4");
        context.setAttribute("vnf.vnfc[2].vm[0].url","C1");
        context.setAttribute("vnf.vnfc[2].vm[1].url","C2");
        context.setAttribute("vnf.vnfc[2].vm[2].url","C3");
        context.setAttribute("vnf.vnfc[2].vm[3].url","C4");

        context.setAttribute("vnf.vnfc[3].name","D");
        context.setAttribute("vnf.vnfc[3].type","D");
        context.setAttribute("vnf.vnfc[3].vm_count","3");
        context.setAttribute("vnf.vnfc[3].vm[0].url","D1");
        context.setAttribute("vnf.vnfc[3].vm[1].url","D2");
        context.setAttribute("vnf.vnfc[3].vm[2].url","D3");

        context.setAttribute("vnf.vnfc[4].name","E");
        context.setAttribute("vnf.vnfc[4].type","E");
        context.setAttribute("vnf.vnfc[4].vm_count","2");
        context.setAttribute("vnf.vnfc[4].vm[0].url","E1");
        context.setAttribute("vnf.vnfc[4].vm[1].url","E2");

        context.setAttribute("vnf.vnfc[5].name","F");
        context.setAttribute("vnf.vnfc[5].type","F");
        context.setAttribute("vnf.vnfc[5].vm_count","1");
        context.setAttribute("vnf.vnfc[5].vm[0].url","F1");

        context.setAttribute("vnf.vnfc[6].name","G");
        context.setAttribute("vnf.vnfc[6].type","G");
        context.setAttribute("vnf.vnfc[6].vm_count","1");
        context.setAttribute("vnf.vnfc[6].vm[0].url","G1");


        return context;
    }

    private VnfcDependencyModel readComplexDependencyModel() {
        Vnfc a = new Vnfc("A","Active-Passive",null);
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

        dNode.addParent(b);
        eNode.addParent(b);
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

    private VnfcDependencyModel readDependencyModel() {

        Vnfc smp = new Vnfc("SMP","Active-Passive",null);
        Vnfc be = new Vnfc("BE","Active-Active",null);
        Vnfc fe = new Vnfc("FE","Active-Active",null);


        Node smpNode = new Node(smp);
        Node beNode = new Node(be);
        Node feNode = new Node(fe);

        beNode.addParent(smp);
        feNode.addParent(be);
//        smpNode.addParent(fe);

        Set<Node<Vnfc>> dependencies = new HashSet<>();
        dependencies.add(smpNode);
        dependencies.add(feNode);
        dependencies.add(beNode);

        return new VnfcDependencyModel(dependencies);
    }

    private Map<String, String> prepareParams() {
        Map<String,String> params = new HashMap<>();
        params.put(Constants.DEPENDENCY_TYPE,"RESOURCE");
        params.put(Constants.FLOW_STRATEGY,"FORWARD");

        params.put(Constants.VNF_TYPE,"vSCP");
        params.put(Constants.VNF_VERION,"1.00");
        return params;
    }

    private SvcLogicContext prepareContext() {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("input.action-identifiers.vnf-id","1");
        context.setAttribute("vnf.type","vSCP");
        context.setAttribute("vnf.vnfcCount","3");

        context.setAttribute("vnf.vnfc[0].name","SMPname");
        context.setAttribute("vnf.vnfc[0].type","SMP");
        context.setAttribute("vnf.vnfc[0].vm_count","2");
        context.setAttribute("vnf.vnfc[0].vm[0].url","SMP_URL1");
        context.setAttribute("vnf.vnfc[0].vm[1].url","SMP_URL2");

        context.setAttribute("vnf.vnfc[1].name","BEname");
        context.setAttribute("vnf.vnfc[1].type","BE");
        context.setAttribute("vnf.vnfc[1].vm_count","5");
        context.setAttribute("vnf.vnfc[1].vm[0].url","BE_URL1");
        context.setAttribute("vnf.vnfc[1].vm[1].url","BE_URL2");
        context.setAttribute("vnf.vnfc[1].vm[2].url","BE_URL3");
        context.setAttribute("vnf.vnfc[1].vm[3].url","BE_URL4");
        context.setAttribute("vnf.vnfc[1].vm[4].url","BE_URL5");

        context.setAttribute("vnf.vnfc[2].name","FEname");
        context.setAttribute("vnf.vnfc[2].type","FE");
        context.setAttribute("vnf.vnfc[2].vm_count","2");
        context.setAttribute("vnf.vnfc[2].vm[0].url","FE_URL1");
        context.setAttribute("vnf.vnfc[2].vm[1].url","FE_URL2");

        return context;
    }

    @Test(expected = RuntimeException.class)
    public void testMissingVnfcTypeInDependencyModel() throws DependencyModelNotFound {
        Map<String, String> params = prepareParams();
        SvcLogicContext context = prepareContext();
        context.setAttribute("vnf.vnfc[3].name","XEname");
        context.setAttribute("vnf.vnfc[3].type","XE");
        context.setAttribute("vnf.vnfc[3].vm_count","2");
        context.setAttribute("vnf.vnfc[3].vm[0].url","XE_URL1");
        context.setAttribute("vnf.vnfc[3].vm[1].url","XE_URL2");
        context.setAttribute("vnf.vnfcCount","4");

        VnfcDependencyModel dependencyModel = readDependencyModel();

        PowerMockito.mockStatic(DependencyModelFactory.class);
        DependencyManager dependencyManager = PowerMockito.mock(DependencyManager.class);

        PowerMockito.when(DependencyModelFactory.createDependencyManager()).thenReturn(dependencyManager);
        PowerMockito.when(dependencyManager.getVnfcDependencyModel((
                DependencyModelIdentifier) Matchers.any(),(DependencyTypes) Matchers.any()))
                .thenReturn(dependencyModel);

        VnfExecutionFlow vnfExecutionFlow = new VnfExecutionFlowImpl();
        vnfExecutionFlow.getVnfExecutionFlowData(params,context);
    }

    @Test(expected = RuntimeException.class)
    public void testMissingMandatoryVnfcTypeInInventoryModel() throws DependencyModelNotFound {
        Map<String, String> params = prepareParams();
        SvcLogicContext context = prepareContext();
        VnfcDependencyModel dependencyModel = readDependencyModel();

        Vnfc xe = new Vnfc("XE","Active-Active",null, true);
        Node xeNode = new Node(xe);
        dependencyModel.getDependencies().add(xeNode);

        PowerMockito.mockStatic(DependencyModelFactory.class);
        DependencyManager dependencyManager = PowerMockito.mock(DependencyManager.class);

        PowerMockito.when(DependencyModelFactory.createDependencyManager()).thenReturn(dependencyManager);
        PowerMockito.when(dependencyManager.getVnfcDependencyModel((
                DependencyModelIdentifier) Matchers.any(),(DependencyTypes) Matchers.any()))
                .thenReturn(dependencyModel);

        VnfExecutionFlow vnfExecutionFlow = new VnfExecutionFlowImpl();
        vnfExecutionFlow.getVnfExecutionFlowData(params,context);
    }

    @Test
    public void testMissingOptionalVnfcTypeInInventoryModel() throws DependencyModelNotFound {
        Map<String, String> params = prepareParams();
        SvcLogicContext context = prepareContext();
        VnfcDependencyModel dependencyModel = readDependencyModel();

        Vnfc xe = new Vnfc("XE","Active-Active",null, false);
        Node xeNode = new Node(xe);
        dependencyModel.getDependencies().add(xeNode);

        PowerMockito.mockStatic(DependencyModelFactory.class);
        DependencyManager dependencyManager = PowerMockito.mock(DependencyManager.class);

        PowerMockito.when(DependencyModelFactory.createDependencyManager()).thenReturn(dependencyManager);
        PowerMockito.when(dependencyManager.getVnfcDependencyModel((
                DependencyModelIdentifier) Matchers.any(),(DependencyTypes) Matchers.any()))
                .thenReturn(dependencyModel);

        VnfExecutionFlow vnfExecutionFlow = new VnfExecutionFlowImpl();
        vnfExecutionFlow.getVnfExecutionFlowData(params,context);
    }

    @Test
    public void testMissingOptionalVnfcTypeInInventoryModelWithDependentChild() throws DependencyModelNotFound {
        Map<String, String> params = prepareParams();
        SvcLogicContext context = prepareContext();
        context.setAttribute("vnf.vnfc[3].name","YEname");
        context.setAttribute("vnf.vnfc[3].type","YE");
        context.setAttribute("vnf.vnfc[3].vm_count","2");
        context.setAttribute("vnf.vnfc[3].vm[0].url","YE_URL1");
        context.setAttribute("vnf.vnfc[3].vm[1].url","YE_URL2");
        context.setAttribute("vnf.vnfcCount","4");

        VnfcDependencyModel dependencyModel = readDependencyModel();

        Vnfc xe = new Vnfc("XE","Active-Active",null, false);
        Vnfc ye = new Vnfc("YE","Active-Active",null, true);
        Node xeNode = new Node(xe);
        Node yeNode = new Node(ye);
        yeNode.addParent(xe);

        dependencyModel.getDependencies().add(yeNode);
        dependencyModel.getDependencies().add(xeNode);

        PowerMockito.mockStatic(DependencyModelFactory.class);
        DependencyManager dependencyManager = PowerMockito.mock(DependencyManager.class);

        PowerMockito.when(DependencyModelFactory.createDependencyManager()).thenReturn(dependencyManager);
        PowerMockito.when(dependencyManager.getVnfcDependencyModel((
                DependencyModelIdentifier) Matchers.any(),(DependencyTypes) Matchers.any()))
                .thenReturn(dependencyModel);

        VnfExecutionFlow vnfExecutionFlow = new VnfExecutionFlowImpl();
        vnfExecutionFlow.getVnfExecutionFlowData(params,context);
    }
}
