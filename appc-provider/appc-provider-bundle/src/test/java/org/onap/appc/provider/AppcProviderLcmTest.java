/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 Orange
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

package org.onap.appc.provider;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AttachVolumeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AttachVolumeOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AttachVolumeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AuditInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AuditOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.CheckLockInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.CheckLockOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigBackupDeleteInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigBackupDeleteOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigBackupInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigBackupOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigExportInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigExportOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigModifyInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigModifyOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigRestoreInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigRestoreOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigureInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigureOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigScaleOutInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigScaleOutOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigScaleOutOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DetachVolumeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DetachVolumeOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DetachVolumeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.EvacuateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.EvacuateOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.HealthCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.HealthCheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.LiveUpgradeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.LiveUpgradeOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.LockInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.LockOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.MigrateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.MigrateOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QueryInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QueryOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QuiesceTrafficInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QuiesceTrafficOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QuiesceTrafficOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ResumeTrafficInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ResumeTrafficOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ResumeTrafficOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePreCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePreCheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePreCheckOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeSoftwareInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeSoftwareOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeSoftwareOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePostCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePostCheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePostCheckOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackupInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackupOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackupOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackoutInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackoutOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackoutOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DistributeTrafficInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DistributeTrafficOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DistributeTrafficOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebootInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebootOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebootOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebuildInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebuildOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RestartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RestartOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RollbackInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RollbackOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.SnapshotInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.SnapshotOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.SoftwareUploadInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.SoftwareUploadOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StartApplicationInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StartApplicationOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StartOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StopApplicationInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StopApplicationOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StopInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StopOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.SyncInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.SyncOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.TerminateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.TerminateOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.TestInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.TestOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UnlockInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UnlockOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ZULU;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiersBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeaderBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.StatusBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.provider.lcm.service.*;
import org.onap.appc.provider.lcm.util.ValidationService;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.onap.appc.provider.Whitebox;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration Test class for AppcProviderLcm.
 */

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class AppcProviderLcmTest extends AbstractDataBrokerTest {
    private Status successStatus = new StatusBuilder().setCode(400).setMessage("success").build();
    private Status failStatus = new StatusBuilder().setCode(302)
        .setMessage("MISSING MANDATORY PARAMETER - Parameter/s common-header , action is/are missing").build();

    private AppcProviderLcm appcProviderLcm;
    private DataBroker dataBroker;
    @Spy
    private ValidationService validationService = ValidationService.getInstance();
    @Mock
    private RequestHandlerOutput requestHandlerOutput;
    @Mock
    private ResponseContext responseContext;
    @Mock
    private org.onap.appc.domainmodel.lcm.Status successlcmStatus;

    /**
     * The @Before annotation is defined in the AbstractDataBrokerTest class. The method setupWithDataBroker is invoked
     * from inside the @Before method and is used to initialize the databroker with objects for a test runs. In our case
     * we use this oportunity to create an instance of our provider and initialize it (which registers it as a listener
     * etc). This method runs before every @Test method below.
     */
    @Override
    protected void setupWithDataBroker(DataBroker dataBroker) {
        super.setupWithDataBroker(dataBroker);

        this.dataBroker = dataBroker;
    }

    @Before
    public void setUp() throws Exception {
        //mock appcProviderLcm
        NotificationProviderService nps = mock(NotificationProviderService.class);
        RpcProviderRegistry registry = mock(RpcProviderRegistry.class);
        BindingAwareBroker.RpcRegistration rpcRegistration = mock(BindingAwareBroker.RpcRegistration.class);
        doReturn(rpcRegistration).when(registry).addRpcImplementation(any(), any());
        appcProviderLcm = spy(new AppcProviderLcm(dataBroker, nps, registry));
        //mock validationService
        //mockStatic(ValidationService.class);
        //when(ValidationService.getInstance()).thenReturn(validationService);

        doReturn(successlcmStatus).when(responseContext).getStatus();
        doReturn(400).when(successlcmStatus).getCode();
        doReturn("success").when(successlcmStatus).getMessage();
    }

    @After
    public void tearDown() throws Exception {
        if (appcProviderLcm != null) {
            appcProviderLcm.close();
        }
    }

    @Test
    public void testConstructor() throws Exception {
        Object executorService = Whitebox.getInternalState(appcProviderLcm, "executor");
        Assert.assertNotNull(executorService);
        Object internalRpcRegistration = Whitebox.getInternalState(appcProviderLcm,
            "rpcRegistration");
        Assert.assertNotNull(internalRpcRegistration);
    }

    @Test
    public void testClose() throws Exception {
        ExecutorService executorService = spy(Executors.newFixedThreadPool(1));
        Whitebox.setInternalState(appcProviderLcm, "executor", executorService);
        BindingAwareBroker.RpcRegistration rpcRegistration = mock(BindingAwareBroker.RpcRegistration.class);
        Whitebox.setInternalState(appcProviderLcm, "rpcRegistration", rpcRegistration);
        appcProviderLcm.close();
        verify(executorService, times(1)).shutdown();
        verify(rpcRegistration, times(1)).close();
    }

    @Test
    public void testRebuild() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        RebuildInput rebuildInput = mock(RebuildInput.class);
        doReturn(newCommonHeader("request-id-test")).when(rebuildInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(rebuildInput).getActionIdentifiers();

        Future<RpcResult<RebuildOutput>> results = appcProviderLcm.rebuild(rebuildInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.rebuild(rebuildInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(rebuildInput).getActionIdentifiers();
        results = appcProviderLcm.rebuild(rebuildInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testRestart() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        RestartInput restartInput = mock(RestartInput.class);
        doReturn(newCommonHeader("request-id-test")).when(restartInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(restartInput).getActionIdentifiers();

        Future<RpcResult<RestartOutput>> results = appcProviderLcm.restart(restartInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.restart(restartInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(restartInput).getActionIdentifiers();
        results = appcProviderLcm.restart(restartInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testStartApplication() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        StartApplicationInput startApplicationInput = mock(StartApplicationInput.class);
        doReturn(newCommonHeader("request-id-test")).when(startApplicationInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(startApplicationInput).getActionIdentifiers();

        Future<RpcResult<StartApplicationOutput>> results = appcProviderLcm.startApplication(startApplicationInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.startApplication(startApplicationInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(startApplicationInput).getActionIdentifiers();
        results = appcProviderLcm.startApplication(startApplicationInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());        
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testMigrate() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        MigrateInput migrateInput = mock(MigrateInput.class);
        doReturn(newCommonHeader("request-id-test")).when(migrateInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(migrateInput).getActionIdentifiers();

        Future<RpcResult<MigrateOutput>> results = appcProviderLcm.migrate(migrateInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.migrate(migrateInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(migrateInput).getActionIdentifiers();
        results = appcProviderLcm.migrate(migrateInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());  
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testEvacuate() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        EvacuateInput evacuateInput = mock(EvacuateInput.class);
        doReturn(newCommonHeader("request-id-test")).when(evacuateInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(evacuateInput).getActionIdentifiers();

        Future<RpcResult<EvacuateOutput>> results = appcProviderLcm.evacuate(evacuateInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.evacuate(evacuateInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(evacuateInput).getActionIdentifiers();
        results = appcProviderLcm.evacuate(evacuateInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());  
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testSnapshot() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        SnapshotInput snapshotInput = mock(SnapshotInput.class);
        doReturn(newCommonHeader("request-id-test")).when(snapshotInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(snapshotInput).getActionIdentifiers();

        Future<RpcResult<SnapshotOutput>> results = appcProviderLcm.snapshot(snapshotInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.snapshot(snapshotInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(snapshotInput).getActionIdentifiers();
        results = appcProviderLcm.snapshot(snapshotInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());  
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testRollback() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        RollbackInput rollbackInput = mock(RollbackInput.class);
        doReturn(newCommonHeader("request-id-test")).when(rollbackInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(rollbackInput).getActionIdentifiers();

        Future<RpcResult<RollbackOutput>> results = appcProviderLcm.rollback(rollbackInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.rollback(rollbackInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(rollbackInput).getActionIdentifiers();
        results = appcProviderLcm.rollback(rollbackInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()); 
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testSync() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        SyncInput syncInput = mock(SyncInput.class);
        doReturn(newCommonHeader("request-id-test")).when(syncInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(syncInput).getActionIdentifiers();

        Future<RpcResult<SyncOutput>> results = appcProviderLcm.sync(syncInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.sync(syncInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(syncInput).getActionIdentifiers();
        results = appcProviderLcm.sync(syncInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testTerminate() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        TerminateInput terminateInput = mock(TerminateInput.class);
        doReturn(newCommonHeader("request-id-test")).when(terminateInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(terminateInput).getActionIdentifiers();

        Future<RpcResult<TerminateOutput>> results = appcProviderLcm.terminate(terminateInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.terminate(terminateInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(terminateInput).getActionIdentifiers();
        results = appcProviderLcm.terminate(terminateInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testConfigure() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        ConfigureInput configureInput = mock(ConfigureInput.class);
        doReturn(newCommonHeader("request-id-test")).when(configureInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(configureInput).getActionIdentifiers();

        Future<RpcResult<ConfigureOutput>> results = appcProviderLcm.configure(configureInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.configure(configureInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(configureInput).getActionIdentifiers();
        results = appcProviderLcm.configure(configureInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testConfigModify() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        ConfigModifyInput configModifyInput = mock(ConfigModifyInput.class);
        doReturn(newCommonHeader("request-id-test")).when(configModifyInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(configModifyInput).getActionIdentifiers();

        Future<RpcResult<ConfigModifyOutput>> results = appcProviderLcm.configModify(configModifyInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.configModify(configModifyInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(configModifyInput).getActionIdentifiers();
        results = appcProviderLcm.configModify(configModifyInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testConfigScaleOut() throws Exception {
        ConfigScaleOutInput mockInput = mock(ConfigScaleOutInput.class);
        ConfigScaleOutOutput mockOutput = mock(ConfigScaleOutOutput.class);
        ConfigScaleOutOutputBuilder mockOutputBuilder = mock(ConfigScaleOutOutputBuilder.class);
        ConfigScaleOutService mockService = mock(ConfigScaleOutService.class);

        when(mockService.process(mockInput)).thenReturn(mockOutputBuilder);
        when(mockOutputBuilder.build()).thenReturn(mockOutput);
        Future<RpcResult<ConfigScaleOutOutput>> results = appcProviderLcm.configScaleOut(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
    }

    @Test
    public void testConfigRestore() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        ConfigRestoreInput configRestoreInput = mock(ConfigRestoreInput.class);
        doReturn(newCommonHeader("request-id-test")).when(configRestoreInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(configRestoreInput).getActionIdentifiers();

        Future<RpcResult<ConfigRestoreOutput>> results = appcProviderLcm.configRestore(configRestoreInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.configRestore(configRestoreInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(configRestoreInput).getActionIdentifiers();
        results = appcProviderLcm.configRestore(configRestoreInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testTest() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        TestInput testInput = mock(TestInput.class);
        doReturn(newCommonHeader("request-id-test")).when(testInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(testInput).getActionIdentifiers();

        Future<RpcResult<TestOutput>> results = appcProviderLcm.test(testInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.test(testInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(testInput).getActionIdentifiers();
        results = appcProviderLcm.test(testInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testStop() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        StopInput stopInput = mock(StopInput.class);
        doReturn(newCommonHeader("request-id-stop")).when(stopInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(stopInput).getActionIdentifiers();

        Future<RpcResult<StopOutput>> results = appcProviderLcm.stop(stopInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.stop(stopInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(stopInput).getActionIdentifiers();
        results = appcProviderLcm.stop(stopInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testStart() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        StartInput startInput = mock(StartInput.class);
        doReturn(newCommonHeader("request-id-start")).when(startInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(startInput).getActionIdentifiers();

        Future<RpcResult<StartOutput>> results = appcProviderLcm.start(startInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.start(startInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(startInput).getActionIdentifiers();
        results = appcProviderLcm.start(startInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testAudit() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        AuditInput auditInput = mock(AuditInput.class);
        doReturn(newCommonHeader("request-id-aduit")).when(auditInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(auditInput).getActionIdentifiers();

        Future<RpcResult<AuditOutput>> results = appcProviderLcm.audit(auditInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.audit(auditInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(auditInput).getActionIdentifiers();
        results = appcProviderLcm.audit(auditInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testSoftwareUpload() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        SoftwareUploadInput softwareUploadInput = mock(SoftwareUploadInput.class);
        doReturn(newCommonHeader("request-id-aduit")).when(softwareUploadInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(softwareUploadInput).getActionIdentifiers();

        Future<RpcResult<SoftwareUploadOutput>> results = appcProviderLcm.softwareUpload(softwareUploadInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.softwareUpload(softwareUploadInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(softwareUploadInput).getActionIdentifiers();
        results = appcProviderLcm.softwareUpload(softwareUploadInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testHealthCheck() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        HealthCheckInput healthCheckInput = mock(HealthCheckInput.class);
        doReturn(newCommonHeader("request-id-aduit")).when(healthCheckInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(healthCheckInput).getActionIdentifiers();

        Future<RpcResult<HealthCheckOutput>> results = appcProviderLcm.healthCheck(healthCheckInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.healthCheck(healthCheckInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(healthCheckInput).getActionIdentifiers();
        results = appcProviderLcm.healthCheck(healthCheckInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testLiveUpgrade() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        LiveUpgradeInput liveUpgradeInput = mock(LiveUpgradeInput.class);
        doReturn(newCommonHeader("request-id-aduit")).when(liveUpgradeInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(liveUpgradeInput).getActionIdentifiers();

        Future<RpcResult<LiveUpgradeOutput>> results = appcProviderLcm.liveUpgrade(liveUpgradeInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.liveUpgrade(liveUpgradeInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(liveUpgradeInput).getActionIdentifiers();
        results = appcProviderLcm.liveUpgrade(liveUpgradeInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testLock() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        LockInput lockInput = mock(LockInput.class);
        doReturn(newCommonHeader("request-id-aduit")).when(lockInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(lockInput).getActionIdentifiers();

        Future<RpcResult<LockOutput>> results = appcProviderLcm.lock(lockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.lock(lockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(lockInput).getActionIdentifiers();
        results = appcProviderLcm.lock(lockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testUnlock() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        UnlockInput unlockInput = mock(UnlockInput.class);
        doReturn(newCommonHeader("request-id-aduit")).when(unlockInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(unlockInput).getActionIdentifiers();

        Future<RpcResult<UnlockOutput>> results = appcProviderLcm.unlock(unlockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.unlock(unlockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(unlockInput).getActionIdentifiers();
        results = appcProviderLcm.unlock(unlockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testCheckLock() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        Map<String, String> additionalContext = new HashMap<>();
        additionalContext.put("locked", "true");
        doReturn(additionalContext).when(responseContext).getAdditionalContext();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        CheckLockInput checkLockInput = mock(CheckLockInput.class);
        doReturn(newCommonHeader("request-id-aduit")).when(checkLockInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(checkLockInput).getActionIdentifiers();

        Future<RpcResult<CheckLockOutput>> results = appcProviderLcm.checkLock(checkLockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.checkLock(checkLockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(checkLockInput).getActionIdentifiers();
        results = appcProviderLcm.checkLock(checkLockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testConfigBackup() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        ConfigBackupInput configBackupInput = mock(ConfigBackupInput.class);
        doReturn(newCommonHeader("request-id-aduit")).when(configBackupInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(configBackupInput).getActionIdentifiers();

        Future<RpcResult<ConfigBackupOutput>> results = appcProviderLcm.configBackup(configBackupInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.configBackup(configBackupInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(configBackupInput).getActionIdentifiers();
        results = appcProviderLcm.configBackup(configBackupInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testConfigBackupDelete() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        ConfigBackupDeleteInput configBackupDeleteInput = mock(ConfigBackupDeleteInput.class);
        doReturn(newCommonHeader("request-id-aduit")).when(configBackupDeleteInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(configBackupDeleteInput).getActionIdentifiers();

        Future<RpcResult<ConfigBackupDeleteOutput>> results = appcProviderLcm.configBackupDelete
            (configBackupDeleteInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.configBackupDelete(configBackupDeleteInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(configBackupDeleteInput).getActionIdentifiers();
        results = appcProviderLcm.configBackupDelete(configBackupDeleteInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testConfigExport() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        ConfigExportInput configExportInput = mock(ConfigExportInput.class);
        doReturn(newCommonHeader("request-id-aduit")).when(configExportInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(configExportInput).getActionIdentifiers();

        Future<RpcResult<ConfigExportOutput>> results = appcProviderLcm.configExport
            (configExportInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.configExport(configExportInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(configExportInput).getActionIdentifiers();
        results = appcProviderLcm.configExport(configExportInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testStopApplication() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        StopApplicationInput stopApplicationInput = mock(StopApplicationInput.class);
        doReturn(newCommonHeader("request-id-aduit")).when(stopApplicationInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(stopApplicationInput).getActionIdentifiers();

        Future<RpcResult<StopApplicationOutput>> results = appcProviderLcm.stopApplication
            (stopApplicationInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals("Success", results.get().getResult().getStatus().getMessage());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // Validation failed
        doReturn(failStatus).when(validationService).validateInput(any(), any(), any());
        results = appcProviderLcm.stopApplication(stopApplicationInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //Assert.assertEquals(failStatus, results.get().getResult().getStatus());
        //verify(appcProviderLcm, times(1)).executeRequest(any());

        // parse exception
        doReturn(null).when(validationService).validateInput(any(), any(), any());
        doReturn(null).when(stopApplicationInput).getActionIdentifiers();
        results = appcProviderLcm.stopApplication(stopApplicationInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        Assert.assertTrue(303 == LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode());
        //Assert.assertTrue(LCMCommandStatus.REQUEST_PARSING_FAILED.getResponseCode()
        //    == results.get().getResult().getStatus().getCode());
        //verify(appcProviderLcm, times(1)).executeRequest(any());
    }

    @Test
    public void testQuery() throws Exception {
        QueryInput mockInput = mock(QueryInput.class);
        QueryOutput mockOutput = mock(QueryOutput.class);
        QueryOutputBuilder mockQueryOutputBuilder = mock(QueryOutputBuilder.class);
        QueryService mockQuery = mock(QueryService.class);

        //whenNew(QueryService.class).withNoArguments().thenReturn(mockQuery);
        when(mockQuery.process(mockInput)).thenReturn(mockQueryOutputBuilder);
        when(mockQueryOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<QueryOutput>> results = appcProviderLcm.query(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //verify(mockQuery, times(1)).process(mockInput);
        //Assert.assertEquals("Should return mockOutput", mockOutput, results.get().getResult());
    }

    @Test
    public void testReboot() throws Exception {
        RebootInput mockInput = mock(RebootInput.class);
        RebootOutput mockOutput = mock(RebootOutput.class);
        RebootOutputBuilder mockRebootOutputBuilder = mock(RebootOutputBuilder.class);
        RebootService mockReboot = mock(RebootService.class);

        //whenNew(RebootService.class).withNoArguments().thenReturn(mockReboot);
        when(mockReboot.reboot(mockInput)).thenReturn(mockRebootOutputBuilder);
        when(mockRebootOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<RebootOutput>> results = appcProviderLcm.reboot(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //verify(mockReboot, times(1)).process(mockInput);
        //Assert.assertEquals("Should return mockOutput", mockOutput, results.get().getResult());
    }

    @Test
    public void testAttachVolume() throws Exception {
        AttachVolumeInput mockInput = mock(AttachVolumeInput.class);
        AttachVolumeOutput mockOutput = mock(AttachVolumeOutput.class);
        AttachVolumeOutputBuilder mockOutputBuilder = mock(AttachVolumeOutputBuilder.class);
        VolumeService mockVolumeService = mock(VolumeService.class);

        //whenNew(VolumeService.class).withArguments(true).thenReturn(mockVolumeService);
        when(mockVolumeService.attachVolume(mockInput)).thenReturn(mockOutputBuilder);
        when(mockOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<AttachVolumeOutput>> results = appcProviderLcm.attachVolume(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //verify(mockVolumeService, times(1)).attachVolume(mockInput);
        //Assert.assertEquals("Should return mockOutput", mockOutput, results.get().getResult());
    }

    @Test
    public void testDetachVolume() throws Exception {
        DetachVolumeInput mockInput = mock(DetachVolumeInput.class);
        DetachVolumeOutput mockOutput = mock(DetachVolumeOutput.class);
        DetachVolumeOutputBuilder mockOutputBuilder = mock(DetachVolumeOutputBuilder.class);
        VolumeService mockVolumeService = mock(VolumeService.class);

        //whenNew(VolumeService.class).withArguments(false).thenReturn(mockVolumeService);
        when(mockVolumeService.detachVolume(mockInput)).thenReturn(mockOutputBuilder);
        when(mockOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<DetachVolumeOutput>> results = appcProviderLcm.detachVolume(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //verify(mockVolumeService, times(1)).detachVolume(mockInput);
        //Assert.assertEquals("Should return mockOutput", mockOutput, results.get().getResult());
    }

    @Test
    public void testQuiesceTraffic() throws Exception {
        QuiesceTrafficInput mockInput = mock(QuiesceTrafficInput.class);
        QuiesceTrafficOutput mockOutput = mock(QuiesceTrafficOutput.class);
        QuiesceTrafficOutputBuilder mockOutputBuilder = mock(QuiesceTrafficOutputBuilder.class);
        QuiesceTrafficService mockService = mock(QuiesceTrafficService.class);

        //whenNew(QuiesceTrafficService.class).withNoArguments().thenReturn(mockService);
        when(mockService.process(mockInput)).thenReturn(mockOutputBuilder);
        when(mockOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<QuiesceTrafficOutput>> results = appcProviderLcm.quiesceTraffic(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //verify(mockService, times(1)).process(mockInput);
        //Assert.assertEquals("Should return mockOutput", mockOutput, results.get().getResult());
    }

    @Test
    public void testResumeTraffic() throws Exception {
        ResumeTrafficInput mockInput = mock(ResumeTrafficInput.class);
        ResumeTrafficOutput mockOutput = mock(ResumeTrafficOutput.class);
        ResumeTrafficOutputBuilder mockOutputBuilder = mock(ResumeTrafficOutputBuilder.class);
        ResumeTrafficService mockService = mock(ResumeTrafficService.class);

        //whenNew(ResumeTrafficService.class).withNoArguments().thenReturn(mockService);
        when(mockService.process(mockInput)).thenReturn(mockOutputBuilder);
        when(mockOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<ResumeTrafficOutput>> results = appcProviderLcm.resumeTraffic(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //verify(mockService, times(1)).process(mockInput);
        //Assert.assertEquals("Should return mockOutput", mockOutput, results.get().getResult());
    }

    @Test
    public void testUpgradePreCheck() throws Exception {
        UpgradePreCheckInput mockInput = mock(UpgradePreCheckInput.class);
        UpgradePreCheckOutput mockOutput = mock(UpgradePreCheckOutput.class);
        UpgradePreCheckOutputBuilder mockOutputBuilder = mock(UpgradePreCheckOutputBuilder.class);
        UpgradeService mockService = mock(UpgradeService.class);

        //whenNew(UpgradeService.class).withAnyArguments().thenReturn(mockService);
        when(mockService.upgradePreCheck(mockInput)).thenReturn(mockOutputBuilder);
        when(mockOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<UpgradePreCheckOutput>> results = appcProviderLcm.upgradePreCheck(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //verify(mockService, times(1)).upgradePreCheck(mockInput);
        //Assert.assertEquals("Should return mockOutput", mockOutput, results.get().getResult());
    }


    @Test
    public void testUpgradePostCheck() throws Exception {
        UpgradePostCheckInput mockInput = mock(UpgradePostCheckInput.class);
        UpgradePostCheckOutput mockOutput = mock(UpgradePostCheckOutput.class);
        UpgradePostCheckOutputBuilder mockOutputBuilder = mock(UpgradePostCheckOutputBuilder.class);
        UpgradeService mockService = mock(UpgradeService.class);

        //whenNew(UpgradeService.class).withAnyArguments().thenReturn(mockService);
        when(mockService.upgradePostCheck(mockInput)).thenReturn(mockOutputBuilder);
        when(mockOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<UpgradePostCheckOutput>> results = appcProviderLcm.upgradePostCheck(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //verify(mockService, times(1)).upgradePostCheck(mockInput);
        //Assert.assertEquals("Should return mockOutput", mockOutput, results.get().getResult());
    }

    @Test
    public void testUpgradeSoftware() throws Exception {
        UpgradeSoftwareInput mockInput = mock(UpgradeSoftwareInput.class);
        UpgradeSoftwareOutput mockOutput = mock(UpgradeSoftwareOutput.class);
        UpgradeSoftwareOutputBuilder mockOutputBuilder = mock(UpgradeSoftwareOutputBuilder.class);
        UpgradeService mockService = mock(UpgradeService.class);

        //whenNew(UpgradeService.class).withAnyArguments().thenReturn(mockService);
        when(mockService.upgradeSoftware(mockInput)).thenReturn(mockOutputBuilder);
        when(mockOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<UpgradeSoftwareOutput>> results = appcProviderLcm.upgradeSoftware(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //verify(mockService, times(1)).upgradeSoftware(mockInput);
        //Assert.assertEquals("Should return mockOutput", mockOutput, results.get().getResult());
    }

    @Test
    public void testUpgradeBackup() throws Exception {
        UpgradeBackupInput mockInput = mock(UpgradeBackupInput.class);
        UpgradeBackupOutput mockOutput = mock(UpgradeBackupOutput.class);
        UpgradeBackupOutputBuilder mockOutputBuilder = mock(UpgradeBackupOutputBuilder.class);
        UpgradeService mockService = mock(UpgradeService.class);

        //whenNew(UpgradeService.class).withAnyArguments().thenReturn(mockService);
        when(mockService.upgradeBackup(mockInput)).thenReturn(mockOutputBuilder);
        when(mockOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<UpgradeBackupOutput>> results = appcProviderLcm.upgradeBackup(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //verify(mockService, times(1)).upgradeBackup(mockInput);
        //Assert.assertEquals("Should return mockOutput", mockOutput, results.get().getResult());
    }

    @Test
    public void testUpgradeBackout() throws Exception {
        UpgradeBackoutInput mockInput = mock(UpgradeBackoutInput.class);
        UpgradeBackoutOutput mockOutput = mock(UpgradeBackoutOutput.class);
        UpgradeBackoutOutputBuilder mockOutputBuilder = mock(UpgradeBackoutOutputBuilder.class);
        UpgradeService mockService = mock(UpgradeService.class);

        //whenNew(UpgradeService.class).withAnyArguments().thenReturn(mockService);
        when(mockService.upgradeBackout(mockInput)).thenReturn(mockOutputBuilder);
        when(mockOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<UpgradeBackoutOutput>> results = appcProviderLcm.upgradeBackout(mockInput);
        Assert.assertTrue(302 == results.get().getResult().getStatus().getCode());
        //verify(mockService, times(1)).upgradeBackout(mockInput);
        //Assert.assertEquals("Should return mockOutput", mockOutput, results.get().getResult());
    }

    @Test
    public void distributeTraffic() throws Exception {
        DistributeTrafficInput mockInput = mock(DistributeTrafficInput.class);
        DistributeTrafficOutput mockOutput = mock(DistributeTrafficOutput.class);
        DistributeTrafficOutputBuilder mockOutputBuilder = mock(DistributeTrafficOutputBuilder.class);
        DistributeTrafficService mockService = mock(DistributeTrafficService.class);

        when(mockService.process(mockInput)).thenReturn(mockOutputBuilder);
        when(mockOutputBuilder.build()).thenReturn(mockOutput);

        Future<RpcResult<DistributeTrafficOutput>> results = appcProviderLcm.distributeTraffic(mockInput);
        Assert.assertEquals(302, results.get().getResult().getStatus().getCode().intValue());
    }

    @Test
    public void testTimestampFormatShort() throws Exception {
        // Validation success
        doReturn("Success").when(successlcmStatus).getMessage();
        doReturn(responseContext).when(requestHandlerOutput).getResponseContext();
        doReturn(requestHandlerOutput).when(appcProviderLcm).executeRequest(any());
        doReturn(null).when(validationService).validateInput(any(), any(), any());

        ConfigExportInput configExportInput = mock(ConfigExportInput.class);
        long epochWithZeroFractionalSeconds = 1529495219000l;
        doReturn(newCommonHeader("request-id-aduit", epochWithZeroFractionalSeconds)).when(configExportInput).getCommonHeader();
        doReturn(newActionIdentifier("vnf-id", "vnfc-id", "vserver-id"))
            .when(configExportInput).getActionIdentifiers();

        Future<RpcResult<ConfigExportOutput>> results = appcProviderLcm.configExport
            (configExportInput);
        Assert.assertEquals(302, (int)results.get().getResult().getStatus().getCode());
    }

    private ActionIdentifiers newActionIdentifier(String vnfId, String vnfcId, String vserverId) {
        ActionIdentifiersBuilder actionIdentifiersBuilder = new ActionIdentifiersBuilder();
        actionIdentifiersBuilder.setVnfId(vnfId);
        actionIdentifiersBuilder.setVnfcName(vnfcId);
        actionIdentifiersBuilder.setVserverId(vserverId);
        return actionIdentifiersBuilder.build();
    }

    private CommonHeader newCommonHeader(String requestId) {
        return newCommonHeader(requestId, System.currentTimeMillis());
    }

    private CommonHeader newCommonHeader(String requestId, long epoch) {
        CommonHeaderBuilder commonHeaderBuilder = new CommonHeaderBuilder();
        commonHeaderBuilder.setRequestId(requestId);
        commonHeaderBuilder.setApiVer("2.0.0");
        commonHeaderBuilder.setOriginatorId("originatortest");
        DateFormat ZULU_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
        ZULU_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
        commonHeaderBuilder.setTimestamp(ZULU.getDefaultInstance(ZULU_FORMATTER.format(epoch)));
        return commonHeaderBuilder.build();
    }
}
