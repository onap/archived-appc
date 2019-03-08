/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018-2019 Ericsson
 * ================================================================================
 * Modifications Copyright (C) 2019 Orange
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

package org.onap.appc.provider;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.Status;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ActionStatusInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AppcProviderLcmService;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AttachVolumeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AuditInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.CheckLockInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigBackupDeleteInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigBackupInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigExportInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigModifyInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigRestoreInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigScaleOutInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ConfigureInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DetachVolumeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DistributeTrafficInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DistributeTrafficCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.EvacuateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.HealthCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.LiveUpgradeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.LockInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.MigrateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QueryInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QuiesceTrafficInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebootInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebuildInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RestartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ResumeTrafficInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RollbackInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.SnapshotInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.SoftwareUploadInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StartApplicationInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StopApplicationInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StopInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.SyncInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.TerminateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.TestInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UnlockInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackoutInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackupInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePostCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePreCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeSoftwareInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ZULU;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiersBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeaderBuilder;


public class AppcProviderLcmTest {

    private interface ParameterizedRpcRegistration extends RpcRegistration<AppcProviderLcmService> {}
    private DataBroker dataBroker;
    private NotificationPublishService notificationProviderService;
    private RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<AppcProviderLcmService> rpcRegistration;
    private AppcProviderLcm underTest;
    private RequestHandlerOutput output;

    @Before
    public void setupMocksForTests() {
        dataBroker = mock(DataBroker.class);
        notificationProviderService = mock(NotificationPublishService.class);
        rpcProviderRegistry = mock(RpcProviderRegistry.class);
        rpcRegistration = mock(ParameterizedRpcRegistration.class);
        Mockito.doReturn(rpcRegistration).when(rpcProviderRegistry).addRpcImplementation(
                Mockito.any(Class.class), Mockito.any(AppcProviderLcm.class));
        underTest =
                new AppcProviderLcm(dataBroker, notificationProviderService, rpcProviderRegistry);
        output = Mockito.mock(RequestHandlerOutput.class);
        ResponseContext responseContext = Mockito.mock(ResponseContext.class);
        Status status = Mockito.mock(Status.class);
        Mockito.doReturn(200).when(status).getCode();
        Mockito.doReturn(status).when(responseContext).getStatus();
        Mockito.doReturn(responseContext).when(output).getResponseContext();
    }

    @Test
    public void rebuildTestParseException() {
        RebuildInput rebuildInput = mock(RebuildInput.class);
        Mockito.doReturn(Action.Rebuild).when(rebuildInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(rebuildInput).getCommonHeader();
        assertTrue(underTest.rebuild(rebuildInput).isDone());
    }

    @Test
    public void rebuildTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        RebuildInput rebuildInput = mock(RebuildInput.class);
        Mockito.doReturn(Action.Rebuild).when(rebuildInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(rebuildInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(rebuildInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.rebuild(rebuildInput).isDone());
    }

    @Test
    public void restartTestParseException() {
        RestartInput restartInput = mock(RestartInput.class);
        Mockito.doReturn(Action.Restart).when(restartInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(restartInput).getCommonHeader();
        assertTrue(underTest.restart(restartInput).isDone());
    }

    @Test
    public void restartTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        RestartInput restartInput = mock(RestartInput.class);
        Mockito.doReturn(Action.Restart).when(restartInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(restartInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(restartInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.restart(restartInput).isDone());
    }

    @Test
    public void startApplicationTestParseException() {
        StartApplicationInput startApplicationInput = mock(StartApplicationInput.class);
        Mockito.doReturn(Action.StartApplication).when(startApplicationInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(startApplicationInput)
                .getCommonHeader();
        assertTrue(underTest.startApplication(startApplicationInput).isDone());
    }

    @Test
    public void startApplicationTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        StartApplicationInput startApplicationInput = mock(StartApplicationInput.class);
        Mockito.doReturn(Action.StartApplication).when(startApplicationInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(startApplicationInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(startApplicationInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.startApplication(startApplicationInput).isDone());
    }

    @Test
    public void migrateTestParseException() {
        MigrateInput migrateInput = mock(MigrateInput.class);
        Mockito.doReturn(Action.Migrate).when(migrateInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(migrateInput).getCommonHeader();
        assertTrue(underTest.migrate(migrateInput).isDone());
    }

    @Test
    public void migrateTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        MigrateInput migrateInput = mock(MigrateInput.class);
        Mockito.doReturn(Action.Migrate).when(migrateInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(migrateInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(migrateInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.migrate(migrateInput).isDone());
    }

    @Test
    public void evacuateTestParseException() {
        EvacuateInput evacuateInput = mock(EvacuateInput.class);
        Mockito.doReturn(Action.Evacuate).when(evacuateInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(evacuateInput).getCommonHeader();
        assertTrue(underTest.evacuate(evacuateInput).isDone());
    }

    @Test
    public void evacuateTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        EvacuateInput evacuateInput = mock(EvacuateInput.class);
        Mockito.doReturn(Action.Evacuate).when(evacuateInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(evacuateInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(evacuateInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.evacuate(evacuateInput).isDone());
    }

    @Test
    public void snapshotTestParseException() {
        SnapshotInput snapshotInput = mock(SnapshotInput.class);
        Mockito.doReturn(Action.Snapshot).when(snapshotInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(snapshotInput).getCommonHeader();
        assertTrue(underTest.snapshot(snapshotInput).isDone());
    }

    @Test
    public void snapshotTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        SnapshotInput snapshotInput = mock(SnapshotInput.class);
        Mockito.doReturn(Action.Snapshot).when(snapshotInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(snapshotInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(snapshotInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.snapshot(snapshotInput).isDone());
    }

    @Test
    public void rollbackTestParseException() {
        RollbackInput rollbackInput = mock(RollbackInput.class);
        Mockito.doReturn(Action.Rollback).when(rollbackInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(rollbackInput).getCommonHeader();
        assertTrue(underTest.rollback(rollbackInput).isDone());
    }

    @Test
    public void rollbackTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        RollbackInput rollbackInput = mock(RollbackInput.class);
        Mockito.doReturn(Action.Rollback).when(rollbackInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(rollbackInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(rollbackInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.rollback(rollbackInput).isDone());
    }

    @Test
    public void syncTestParseException() {
        SyncInput syncInput = mock(SyncInput.class);
        Mockito.doReturn(Action.Sync).when(syncInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(syncInput).getCommonHeader();
        assertTrue(underTest.sync(syncInput).isDone());
    }

    @Test
    public void syncTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        SyncInput syncInput = mock(SyncInput.class);
        Mockito.doReturn(Action.Sync).when(syncInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(syncInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(syncInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.sync(syncInput).isDone());
    }

    @Test
    public void queryTest() {
        QueryInput queryInput = mock(QueryInput.class);
        Mockito.doReturn(Action.Query).when(queryInput).getAction();
        assertTrue(underTest.query(queryInput).isDone());
    }

    @Test
    public void attachVolumeTest() {
        AttachVolumeInput attachVolumeInput = mock(AttachVolumeInput.class);
        Mockito.doReturn(Action.AttachVolume).when(attachVolumeInput).getAction();
        assertTrue(underTest.attachVolume(attachVolumeInput).isDone());
    }

    @Test
    public void rebootTest() {
        RebootInput rebootInput = mock(RebootInput.class);
        Mockito.doReturn(Action.Reboot).when(rebootInput).getAction();
        assertTrue(underTest.reboot(rebootInput).isDone());
    }

    @Test
    public void detachVolumeTest() {
        DetachVolumeInput detachVolumeInput = mock(DetachVolumeInput.class);
        Mockito.doReturn(Action.DetachVolume).when(detachVolumeInput).getAction();
        assertTrue(underTest.detachVolume(detachVolumeInput).isDone());
    }

    @Test
    public void quiesceTrafficTest() {
        QuiesceTrafficInput quiesceTrafficInput = mock(QuiesceTrafficInput.class);
        Mockito.doReturn(Action.QuiesceTraffic).when(quiesceTrafficInput).getAction();
        assertTrue(underTest.quiesceTraffic(quiesceTrafficInput).isDone());
    }

    @Test
    public void resumeTrafficTest() {
        ResumeTrafficInput resumeTrafficInput = mock(ResumeTrafficInput.class);
        Mockito.doReturn(Action.ResumeTraffic).when(resumeTrafficInput).getAction();
        assertTrue(underTest.resumeTraffic(resumeTrafficInput).isDone());
    }

    @Test
    public void distributeTrafficTest() {
        DistributeTrafficInput distributeTrafficInput = mock(DistributeTrafficInput.class);
        Mockito.doReturn(Action.DistributeTraffic).when(distributeTrafficInput).getAction();
        assertTrue(underTest.distributeTraffic(distributeTrafficInput).isDone());
    }

    @Test
    public void distributeTrafficCheckTest() {
        DistributeTrafficCheckInput distributeTrafficCheckInput = mock(DistributeTrafficCheckInput.class);
        Mockito.doReturn(Action.DistributeTrafficCheck).when(distributeTrafficCheckInput).getAction();
        assertTrue(underTest.distributeTrafficCheck(distributeTrafficCheckInput).isDone());
    }

    @Test
    public void upgradePreCheckInputTest() {
        UpgradePreCheckInput upgradePreCheckInput = mock(UpgradePreCheckInput.class);
        Mockito.doReturn(Action.UpgradePreCheck).when(upgradePreCheckInput).getAction();
        assertTrue(underTest.upgradePreCheck(upgradePreCheckInput).isDone());
    }

    @Test
    public void upgradeSoftwareTest() {
        UpgradeSoftwareInput upgradeSoftwareInput = mock(UpgradeSoftwareInput.class);
        Mockito.doReturn(Action.UpgradeSoftware).when(upgradeSoftwareInput).getAction();
        assertTrue(underTest.upgradeSoftware(upgradeSoftwareInput).isDone());
    }

    @Test
    public void upgradePostCheckTest() {
        UpgradePostCheckInput upgradePostCheckInput = mock(UpgradePostCheckInput.class);
        Mockito.doReturn(Action.UpgradePostCheck).when(upgradePostCheckInput).getAction();
        assertTrue(underTest.upgradePostCheck(upgradePostCheckInput).isDone());
    }

    @Test
    public void upgradeBackupTest() {
        UpgradeBackupInput upgradeBackupInput = mock(UpgradeBackupInput.class);
        Mockito.doReturn(Action.UpgradeBackup).when(upgradeBackupInput).getAction();
        assertTrue(underTest.upgradeBackup(upgradeBackupInput).isDone());
    }

    @Test
    public void upgradeBackoutTest() {
        UpgradeBackoutInput upgradeBackoutInput = mock(UpgradeBackoutInput.class);
        Mockito.doReturn(Action.UpgradeBackout).when(upgradeBackoutInput).getAction();
        assertTrue(underTest.upgradeBackout(upgradeBackoutInput).isDone());
    }

    @Test
    public void terminateTestParseException() {
        TerminateInput terminateInput = mock(TerminateInput.class);
        Mockito.doReturn(Action.Terminate).when(terminateInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(terminateInput).getCommonHeader();
        assertTrue(underTest.terminate(terminateInput).isDone());
    }

    @Test
    public void terminateTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        TerminateInput terminateInput = mock(TerminateInput.class);
        Mockito.doReturn(Action.Terminate).when(terminateInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(terminateInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(terminateInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.terminate(terminateInput).isDone());
    }

    @Test
    public void configScaleOutTest() {
        ConfigScaleOutInput configScaleOutInput = mock(ConfigScaleOutInput.class);
        Mockito.doReturn(Action.ConfigScaleOut).when(configScaleOutInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configScaleOutInput)
                .getCommonHeader();
        assertTrue(underTest.configScaleOut(configScaleOutInput).isDone());
    }

    @Test
    public void configModifyTestParseException() {
        ConfigModifyInput configModifyInput = mock(ConfigModifyInput.class);
        Mockito.doReturn(Action.ConfigModify).when(configModifyInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configModifyInput)
                .getCommonHeader();
        assertTrue(underTest.configModify(configModifyInput).isDone());
    }

    @Test
    public void configModifyTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        ConfigModifyInput configModifyInput = mock(ConfigModifyInput.class);
        Mockito.doReturn(Action.ConfigModify).when(configModifyInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configModifyInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(configModifyInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.configModify(configModifyInput).isDone());
    }

    @Test
    public void actionStatusTest() {
        ActionStatusInput actionStatusInput = mock(ActionStatusInput.class);
        Mockito.doReturn(Action.ActionStatus).when(actionStatusInput).getAction();
        assertTrue(underTest.actionStatus(actionStatusInput).isDone());
    }

    @Test
    public void configRestoreTestParseException() {
        ConfigRestoreInput configRestoreInput = mock(ConfigRestoreInput.class);
        Mockito.doReturn(Action.ConfigRestore).when(configRestoreInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configRestoreInput)
                .getCommonHeader();
        assertTrue(underTest.configRestore(configRestoreInput).isDone());
    }

    @Test
    public void configRestoreTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        ConfigRestoreInput configRestoreInput = mock(ConfigRestoreInput.class);
        Mockito.doReturn(Action.ConfigRestore).when(configRestoreInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configRestoreInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(configRestoreInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.configRestore(configRestoreInput).isDone());
    }

    @Test
    public void configureTestParseException() {
        ConfigureInput configureInput = mock(ConfigureInput.class);
        Mockito.doReturn(Action.Configure).when(configureInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configureInput).getCommonHeader();
        assertTrue(underTest.configure(configureInput).isDone());
    }

    @Test
    public void configureTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        ConfigureInput configureInput = mock(ConfigureInput.class);
        Mockito.doReturn(Action.Configure).when(configureInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configureInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(configureInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.configure(configureInput).isDone());
    }

    @Test
    public void testTestParseException() {
        TestInput testInput = mock(TestInput.class);
        Mockito.doReturn(Action.Test).when(testInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(testInput).getCommonHeader();
        assertTrue(underTest.test(testInput).isDone());
    }

    @Test
    public void testTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        TestInput testInput = mock(TestInput.class);
        Mockito.doReturn(Action.Test).when(testInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(testInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(testInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.test(testInput).isDone());
    }

    @Test
    public void stopTestParseException() {
        StopInput stopInput = mock(StopInput.class);
        Mockito.doReturn(Action.Stop).when(stopInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(stopInput).getCommonHeader();
        assertTrue(underTest.stop(stopInput).isDone());
    }

    @Test
    public void stopTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        StopInput stopInput = mock(StopInput.class);
        Mockito.doReturn(Action.Stop).when(stopInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(stopInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(stopInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.stop(stopInput).isDone());
    }

    @Test
    public void startTestParseException() {
        StartInput startInput = mock(StartInput.class);
        Mockito.doReturn(Action.Start).when(startInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(startInput).getCommonHeader();
        assertTrue(underTest.start(startInput).isDone());
    }

    @Test
    public void startTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        StartInput startInput = mock(StartInput.class);
        Mockito.doReturn(Action.Rebuild).when(startInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(startInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(startInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.start(startInput).isDone());
    }

    @Test
    public void auditTestParseExcpetion() {
        AuditInput auditInput = mock(AuditInput.class);
        Mockito.doReturn(Action.Audit).when(auditInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(auditInput).getCommonHeader();
        assertTrue(underTest.audit(auditInput).isDone());
    }

    @Test
    public void auditTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        AuditInput auditInput = mock(AuditInput.class);
        Mockito.doReturn(Action.Audit).when(auditInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(auditInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(auditInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.audit(auditInput).isDone());
    }

    @Test
    public void softwareUploadTestParseException() {
        SoftwareUploadInput softwareUploadInput = mock(SoftwareUploadInput.class);
        Mockito.doReturn(Action.SoftwareUpload).when(softwareUploadInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(softwareUploadInput)
                .getCommonHeader();
        assertTrue(underTest.softwareUpload(softwareUploadInput).isDone());
    }

    @Test
    public void softwareUploadTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        SoftwareUploadInput softwareUploadInput = mock(SoftwareUploadInput.class);
        Mockito.doReturn(Action.SoftwareUpload).when(softwareUploadInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(softwareUploadInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(softwareUploadInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.softwareUpload(softwareUploadInput).isDone());
    }

    @Test
    public void healthCheckTestParseException() {
        HealthCheckInput healthCheckInput = mock(HealthCheckInput.class);
        Mockito.doReturn(Action.HealthCheck).when(healthCheckInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(healthCheckInput).getCommonHeader();
        assertTrue(underTest.healthCheck(healthCheckInput).isDone());
    }

    @Test
    public void healthCheckTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        HealthCheckInput healthCheckInput = mock(HealthCheckInput.class);
        Mockito.doReturn(Action.HealthCheck).when(healthCheckInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(healthCheckInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(healthCheckInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.healthCheck(healthCheckInput).isDone());
    }

    @Test
    public void liveUpgradeTestParseException() {
        LiveUpgradeInput liveUpgradeInput = mock(LiveUpgradeInput.class);
        Mockito.doReturn(Action.LiveUpgrade).when(liveUpgradeInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(liveUpgradeInput).getCommonHeader();
        assertTrue(underTest.liveUpgrade(liveUpgradeInput).isDone());
    }

    @Test
    public void liveUpgradeTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        LiveUpgradeInput liveUpgradeInput = mock(LiveUpgradeInput.class);
        Mockito.doReturn(Action.LiveUpgrade).when(liveUpgradeInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(liveUpgradeInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(liveUpgradeInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.liveUpgrade(liveUpgradeInput).isDone());
    }

    @Test
    public void lockTestParseException() {
        LockInput lockInput = mock(LockInput.class);
        Mockito.doReturn(Action.Lock).when(lockInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(lockInput).getCommonHeader();
        assertTrue(underTest.lock(lockInput).isDone());
    }

    @Test
    public void lockTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        LockInput lockInput = mock(LockInput.class);
        Mockito.doReturn(Action.LiveUpgrade).when(lockInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(lockInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(lockInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.lock(lockInput).isDone());
    }

    @Test
    public void unlockTestParseException() {
        UnlockInput unlockInput = mock(UnlockInput.class);
        Mockito.doReturn(Action.Unlock).when(unlockInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(unlockInput).getCommonHeader();
        assertTrue(underTest.unlock(unlockInput).isDone());
    }

    @Test
    public void unLockTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        UnlockInput unlockInput = mock(UnlockInput.class);
        Mockito.doReturn(Action.Unlock).when(unlockInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(unlockInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(unlockInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.unlock(unlockInput).isDone());
    }

    @Test
    public void checkLockTestParseException() {
        CheckLockInput checkLockInput = mock(CheckLockInput.class);
        Mockito.doReturn(Action.CheckLock).when(checkLockInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(checkLockInput).getCommonHeader();
        assertTrue(underTest.checkLock(checkLockInput).isDone());
    }

    @Test
    public void checkLockTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        CheckLockInput checkLockInput = mock(CheckLockInput.class);
        Mockito.doReturn(Action.CheckLock).when(checkLockInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(checkLockInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(checkLockInput).getActionIdentifiers();
        RequestHandlerOutput output = Mockito.mock(RequestHandlerOutput.class);
        ResponseContext responseContext = Mockito.mock(ResponseContext.class);
        Status status = Mockito.mock(Status.class);
        Map<String, String> additionalContext = new HashMap<>();
        additionalContext.put("locked", "true");
        Mockito.doReturn(additionalContext).when(responseContext).getAdditionalContext();
        Mockito.doReturn(400).when(status).getCode();
        Mockito.doReturn(status).when(responseContext).getStatus();
        Mockito.doReturn(responseContext).when(output).getResponseContext();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.checkLock(checkLockInput).isDone());
    }

    @Test
    public void configBackupTestParseException() {
        ConfigBackupInput configBackupInput = mock(ConfigBackupInput.class);
        Mockito.doReturn(Action.ConfigBackup).when(configBackupInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configBackupInput)
                .getCommonHeader();
        assertTrue(underTest.configBackup(configBackupInput).isDone());
    }

    @Test
    public void configBackupTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        ConfigBackupInput configBackupInput = mock(ConfigBackupInput.class);
        Mockito.doReturn(Action.ConfigBackup).when(configBackupInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configBackupInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(configBackupInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.configBackup(configBackupInput).isDone());
    }

    @Test
    public void configBackupDeleteTestParseException() {
        ConfigBackupDeleteInput configBackupDeleteInput = mock(ConfigBackupDeleteInput.class);
        Mockito.doReturn(Action.ConfigBackupDelete).when(configBackupDeleteInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configBackupDeleteInput)
                .getCommonHeader();
        assertTrue(underTest.configBackupDelete(configBackupDeleteInput).isDone());
    }

    @Test
    public void configBackupDeleteTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        ConfigBackupDeleteInput configBackupDeleteInput = mock(ConfigBackupDeleteInput.class);
        Mockito.doReturn(Action.ConfigBackupDelete).when(configBackupDeleteInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configBackupDeleteInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(configBackupDeleteInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.configBackupDelete(configBackupDeleteInput).isDone());
    }

    @Test
    public void configExportTestParseException() {
        ConfigExportInput configExportInput = mock(ConfigExportInput.class);
        Mockito.doReturn(Action.ConfigExport).when(configExportInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configExportInput)
                .getCommonHeader();
        assertTrue(underTest.configExport(configExportInput).isDone());
    }

    @Test
    public void configExportTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        ConfigExportInput configExportInput = mock(ConfigExportInput.class);
        Mockito.doReturn(Action.ConfigExport).when(configExportInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configExportInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(configExportInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.configExport(configExportInput).isDone());
    }

    @Test
    public void stopApplicationTestParseException() {
        StopApplicationInput stopApplicationInput = mock(StopApplicationInput.class);
        Mockito.doReturn(Action.StopApplication).when(stopApplicationInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(stopApplicationInput)
                .getCommonHeader();
        assertTrue(underTest.stopApplication(stopApplicationInput).isDone());
    }

    @Test
    public void stopApplicationTest() {
        AppcProviderLcm underTestSpy = Mockito.spy(underTest);
        StopApplicationInput stopApplicationInput = mock(StopApplicationInput.class);
        Mockito.doReturn(Action.StopApplication).when(stopApplicationInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(stopApplicationInput).getCommonHeader();
        Mockito.doReturn(new ActionIdentifiersBuilder().build()).when(stopApplicationInput).getActionIdentifiers();
        Mockito.doReturn(output).when(underTestSpy).executeRequest(Mockito.any(RequestHandlerInput.class));
        assertTrue(underTestSpy.stopApplication(stopApplicationInput).isDone());
    }

    @Test
    public void closeTest() throws Exception {
        underTest.close();
        Mockito.verify(rpcRegistration).close();
    }

    private CommonHeaderBuilder getCommonHeaderBuilder() {
        CommonHeaderBuilder headerBuilder = new CommonHeaderBuilder();
        headerBuilder.setApiVer("API-VERSION");
        headerBuilder.setTimestamp(ZULU.getDefaultInstance("1970-01-01T00:00:00.000Z"));
        headerBuilder.setOriginatorId("ORIGINATOR-ID");
        headerBuilder.setRequestId("REQUEST-ID");
        return headerBuilder;
    }
}
