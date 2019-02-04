package org.onap.appc.seqgen.provider;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.GenerateSequenceInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.GenerateSequenceInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.SequenceGeneratorService;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.dependency.info.DependencyInfo;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.dependency.info.DependencyInfoBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.dependency.info.dependency.info.Vnfcs;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.dependency.info.dependency.info.VnfcsBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.inventory.info.InventoryInfo;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.inventory.info.InventoryInfoBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.inventory.info.inventory.info.VnfInfo;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.inventory.info.inventory.info.VnfInfoBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.inventory.info.inventory.info.vnf.info.Vm;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.inventory.info.inventory.info.vnf.info.VmBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.inventory.info.inventory.info.vnf.info.vm.Vnfc;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.inventory.info.inventory.info.vnf.info.vm.VnfcBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.request.info.RequestInfo;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.request.info.RequestInfo.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.request.info.RequestInfo.ActionLevel;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.request.info.RequestInfoBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;


public class SequenceGeneratorProviderTest {

    private DataBroker dataBroker = Mockito.mock(DataBroker.class);
    private RpcProviderRegistry rpcRegistry = Mockito.mock(RpcProviderRegistry.class);
    private NotificationProviderService notificationService = Mockito.mock(NotificationProviderService.class);

    @Test
    public void testClose() throws Exception {
        BindingAwareBroker.RpcRegistration<SequenceGeneratorService> registration = 
                (BindingAwareBroker.RpcRegistration<SequenceGeneratorService>) Mockito.mock(BindingAwareBroker.RpcRegistration.class);
        Mockito.doReturn(registration).when(rpcRegistry).addRpcImplementation(Mockito.any(), Mockito.any());
        SequenceGeneratorProvider provider = new SequenceGeneratorProvider(dataBroker, notificationService, rpcRegistry);
        provider.close();
        Mockito.verify(provider.rpcRegistration).close();
    }

    @Test
    public void testGenerateSequence() throws Exception {
        BindingAwareBroker.RpcRegistration<SequenceGeneratorService> registration = 
                (BindingAwareBroker.RpcRegistration<SequenceGeneratorService>) Mockito.mock(BindingAwareBroker.RpcRegistration.class);
        Mockito.doReturn(registration).when(rpcRegistry).addRpcImplementation(Mockito.any(), Mockito.any());
        SequenceGeneratorProvider provider = new SequenceGeneratorProvider(dataBroker, notificationService, rpcRegistry);
        GenerateSequenceInputBuilder builder = new GenerateSequenceInputBuilder();
        RequestInfoBuilder riBuilder = new RequestInfoBuilder();
        riBuilder.setAction(Action.Start);
        riBuilder.setActionLevel(ActionLevel.Vm);
        riBuilder.setPayload("PAYLOAD");
        RequestInfo info = riBuilder.build();
        builder.setRequestInfo(info);
        InventoryInfoBuilder iiBuilder = new InventoryInfoBuilder();
        VnfInfoBuilder viBuilder = new VnfInfoBuilder();
        viBuilder.setVnfId("VNF_ID");
        List<Vm> vmList = new ArrayList<Vm>();
        VmBuilder vmBuilder = new VmBuilder();
        vmBuilder.setVmId("VM_ID");
        vmBuilder.setVserverId("VSERVER_ID");
        VnfcBuilder vnfcBuilder = new VnfcBuilder();
        vnfcBuilder.setVnfcName("VNFC_NAME");
        vnfcBuilder.setVnfcType("VNFC_TYPE");
        Vnfc vnfc = vnfcBuilder.build();
        vmBuilder.setVnfc(vnfc);
        Vm vm = vmBuilder.build();
        vmList.add(vm);
        viBuilder.setVm(vmList);
        VnfInfo vnfInfo = viBuilder.build();
        iiBuilder.setVnfInfo(vnfInfo);
        InventoryInfo iInfo = iiBuilder.build();
        builder.setInventoryInfo(iInfo);
        DependencyInfoBuilder dependencyBuilder = new DependencyInfoBuilder();
        List<Vnfcs> vnfcsList = new ArrayList<>();
        VnfcsBuilder vnfcsBuilder = new VnfcsBuilder();
        vnfcsBuilder.setVnfcType("VNFC_TYPE");
        List<String> parentList = new ArrayList<>();
        parentList.add("VNFC_TYPE");
        vnfcsBuilder.setParents(parentList);
        Vnfcs vnfcs = vnfcsBuilder.build();
        vnfcsList.add(vnfcs);
        dependencyBuilder.setVnfcs(vnfcsList);
        DependencyInfo dependencyInfo = dependencyBuilder.build();
        builder.setDependencyInfo(dependencyInfo);
        GenerateSequenceInput input = builder.build();
        assertTrue(provider.generateSequence(input).get() instanceof RpcResult);
    }

}
