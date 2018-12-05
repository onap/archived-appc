/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Ericsson
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeaderBuilder;


public class AppcProviderLcmTest {

    private DataBroker dataBroker;
    private NotificationPublishService notificationProviderService;
    private RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<AppcProviderLcmService> rpcRegistration;
    private AppcProviderLcm underTest;

    @Before
    public void setupMocksForTests() {
        dataBroker = mock(DataBroker.class);
        notificationProviderService = mock(NotificationPublishService.class);
        rpcProviderRegistry = mock(RpcProviderRegistry.class);
        rpcRegistration = mock(ParameterizedRpcRegistration.class);
        Mockito.doReturn(rpcRegistration).when(rpcProviderRegistry).addRpcImplementation(
                eq(AppcProviderLcm.class), Mockito.any(AppcProviderLcm.class));
        underTest =
                new AppcProviderLcm(dataBroker, notificationProviderService, rpcProviderRegistry);
    }

    @Test
    public void rebuildTest() {
        RebuildInput rebuildInput = mock(RebuildInput.class);
        Mockito.doReturn(Action.Rebuild).when(rebuildInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(rebuildInput).getCommonHeader();
        assertTrue(underTest.rebuild(rebuildInput).isDone());
    }

    @Test
    public void restartTest() {
        RestartInput restartInput = mock(RestartInput.class);
        Mockito.doReturn(Action.Restart).when(restartInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(restartInput).getCommonHeader();
        assertTrue(underTest.restart(restartInput).isDone());
    }

    @Test
    public void startApplicationTest() {
        StartApplicationInput startApplicationInput = mock(StartApplicationInput.class);
        Mockito.doReturn(Action.StartApplication).when(startApplicationInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(startApplicationInput)
                .getCommonHeader();
        assertTrue(underTest.startApplication(startApplicationInput).isDone());
    }

    @Test
    public void migrateTest() {
        MigrateInput migrateInput = mock(MigrateInput.class);
        Mockito.doReturn(Action.Migrate).when(migrateInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(migrateInput).getCommonHeader();
        assertTrue(underTest.migrate(migrateInput).isDone());
    }

    @Test
    public void evacuateTest() {
        EvacuateInput evacuateInput = mock(EvacuateInput.class);
        Mockito.doReturn(Action.Evacuate).when(evacuateInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(evacuateInput).getCommonHeader();
        assertTrue(underTest.evacuate(evacuateInput).isDone());
    }

    @Test
    public void snapshotTest() {
        SnapshotInput snapshotInput = mock(SnapshotInput.class);
        Mockito.doReturn(Action.Snapshot).when(snapshotInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(snapshotInput).getCommonHeader();
        assertTrue(underTest.snapshot(snapshotInput).isDone());
    }

    @Test
    public void rollbackTest() {
        RollbackInput rollbackInput = mock(RollbackInput.class);
        Mockito.doReturn(Action.Rollback).when(rollbackInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(rollbackInput).getCommonHeader();
        assertTrue(underTest.rollback(rollbackInput).isDone());
    }

    @Test
    public void syncTest() {
        SyncInput syncInput = mock(SyncInput.class);
        Mockito.doReturn(Action.Sync).when(syncInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(syncInput).getCommonHeader();
        assertTrue(underTest.sync(syncInput).isDone());
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
    public void terminateTest() {
        TerminateInput terminateInput = mock(TerminateInput.class);
        Mockito.doReturn(Action.Terminate).when(terminateInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(terminateInput).getCommonHeader();
        assertTrue(underTest.terminate(terminateInput).isDone());
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
    public void configModifyTest() {
        ConfigModifyInput configModifyInput = mock(ConfigModifyInput.class);
        Mockito.doReturn(Action.ConfigModify).when(configModifyInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configModifyInput)
                .getCommonHeader();
        assertTrue(underTest.configModify(configModifyInput).isDone());
    }

    @Test
    public void actionStatusTest() {
        ActionStatusInput actionStatusInput = mock(ActionStatusInput.class);
        Mockito.doReturn(Action.ActionStatus).when(actionStatusInput).getAction();
        assertTrue(underTest.actionStatus(actionStatusInput).isDone());
    }

    @Test
    public void configRestoreTest() {
        ConfigRestoreInput configRestoreInput = mock(ConfigRestoreInput.class);
        Mockito.doReturn(Action.ConfigRestore).when(configRestoreInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configRestoreInput)
                .getCommonHeader();
        assertTrue(underTest.configRestore(configRestoreInput).isDone());
    }

    @Test
    public void configureTest() {
        ConfigureInput configureInput = mock(ConfigureInput.class);
        Mockito.doReturn(Action.Configure).when(configureInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configureInput).getCommonHeader();
        assertTrue(underTest.configure(configureInput).isDone());
    }

    @Test
    public void testTest() {
        TestInput testInput = mock(TestInput.class);
        Mockito.doReturn(Action.Test).when(testInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(testInput).getCommonHeader();
        assertTrue(underTest.test(testInput).isDone());
    }

    @Test
    public void stopTest() {
        StopInput stopInput = mock(StopInput.class);
        Mockito.doReturn(Action.Stop).when(stopInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(stopInput).getCommonHeader();
        assertTrue(underTest.stop(stopInput).isDone());
    }

    @Test
    public void startTest() {
        StartInput startInput = mock(StartInput.class);
        Mockito.doReturn(Action.Start).when(startInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(startInput).getCommonHeader();
        assertTrue(underTest.start(startInput).isDone());
    }

    @Test
    public void auditTest() {
        AuditInput auditInput = mock(AuditInput.class);
        Mockito.doReturn(Action.Audit).when(auditInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(auditInput).getCommonHeader();
        assertTrue(underTest.audit(auditInput).isDone());
    }

    @Test
    public void softwareUploadTest() {
        SoftwareUploadInput softwareUploadInput = mock(SoftwareUploadInput.class);
        Mockito.doReturn(Action.SoftwareUpload).when(softwareUploadInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(softwareUploadInput)
                .getCommonHeader();
        assertTrue(underTest.softwareUpload(softwareUploadInput).isDone());
    }

    @Test
    public void healthCheckTest() {
        HealthCheckInput healthCheckInput = mock(HealthCheckInput.class);
        Mockito.doReturn(Action.HealthCheck).when(healthCheckInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(healthCheckInput).getCommonHeader();
        assertTrue(underTest.healthCheck(healthCheckInput).isDone());
    }

    @Test
    public void liveUpgradeTest() {
        LiveUpgradeInput liveUpgradeInput = mock(LiveUpgradeInput.class);
        Mockito.doReturn(Action.LiveUpgrade).when(liveUpgradeInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(liveUpgradeInput).getCommonHeader();
        assertTrue(underTest.liveUpgrade(liveUpgradeInput).isDone());
    }

    @Test
    public void lockTest() {
        LockInput lockInput = mock(LockInput.class);
        Mockito.doReturn(Action.Lock).when(lockInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(lockInput).getCommonHeader();
        assertTrue(underTest.lock(lockInput).isDone());
    }

    @Test
    public void unlockTest() {
        UnlockInput unlockInput = mock(UnlockInput.class);
        Mockito.doReturn(Action.Unlock).when(unlockInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(unlockInput).getCommonHeader();
        assertTrue(underTest.unlock(unlockInput).isDone());
    }

    @Test
    public void checkLockTest() {
        CheckLockInput checkLockInput = mock(CheckLockInput.class);
        Mockito.doReturn(Action.CheckLock).when(checkLockInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(checkLockInput).getCommonHeader();
        assertTrue(underTest.checkLock(checkLockInput).isDone());
    }

    @Test
    public void configBackupTest() {
        ConfigBackupInput configBackupInput = mock(ConfigBackupInput.class);
        Mockito.doReturn(Action.ConfigBackup).when(configBackupInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configBackupInput)
                .getCommonHeader();
        assertTrue(underTest.configBackup(configBackupInput).isDone());
    }

    @Test
    public void configBackupDeleteTest() {
        ConfigBackupDeleteInput configBackupDeleteInput = mock(ConfigBackupDeleteInput.class);
        Mockito.doReturn(Action.ConfigBackupDelete).when(configBackupDeleteInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configBackupDeleteInput)
                .getCommonHeader();
        assertTrue(underTest.configBackupDelete(configBackupDeleteInput).isDone());
    }

    @Test
    public void configExportTest() {
        ConfigExportInput configExportInput = mock(ConfigExportInput.class);
        Mockito.doReturn(Action.ConfigExport).when(configExportInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(configExportInput)
                .getCommonHeader();
        assertTrue(underTest.configExport(configExportInput).isDone());
    }

    @Test
    public void stopApplicationTest() {
        StopApplicationInput stopApplicationInput = mock(StopApplicationInput.class);
        Mockito.doReturn(Action.StopApplication).when(stopApplicationInput).getAction();
        Mockito.doReturn(getCommonHeaderBuilder().build()).when(stopApplicationInput)
                .getCommonHeader();
        assertTrue(underTest.stopApplication(stopApplicationInput).isDone());
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

interface ParameterizedRpcRegistration extends RpcRegistration<AppcProviderLcmService> {
}
