/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

/*
 * NOTE: This file is auto-generated and should not be changed manually.
 */
package org.onap.appc.client.lcm.api;

import org.onap.appc.client.lcm.model.ActionStatusOutput;
import org.onap.appc.client.lcm.model.ActionStatusInput;
import org.onap.appc.client.lcm.model.AttachVolumeOutput;
import org.onap.appc.client.lcm.model.AttachVolumeInput;
import org.onap.appc.client.lcm.model.AuditOutput;
import org.onap.appc.client.lcm.model.AuditInput;
import org.onap.appc.client.lcm.model.CheckLockOutput;
import org.onap.appc.client.lcm.model.CheckLockInput;
import org.onap.appc.client.lcm.model.ConfigBackupOutput;
import org.onap.appc.client.lcm.model.ConfigBackupInput;
import org.onap.appc.client.lcm.model.ConfigBackupDeleteOutput;
import org.onap.appc.client.lcm.model.ConfigBackupDeleteInput;
import org.onap.appc.client.lcm.model.ConfigExportOutput;
import org.onap.appc.client.lcm.model.ConfigExportInput;
import org.onap.appc.client.lcm.model.ConfigModifyOutput;
import org.onap.appc.client.lcm.model.ConfigModifyInput;
import org.onap.appc.client.lcm.model.ConfigRestoreOutput;
import org.onap.appc.client.lcm.model.ConfigRestoreInput;
import org.onap.appc.client.lcm.model.ConfigScaleoutOutput;
import org.onap.appc.client.lcm.model.ConfigScaleoutInput;
import org.onap.appc.client.lcm.model.ConfigureOutput;
import org.onap.appc.client.lcm.model.ConfigureInput;
import org.onap.appc.client.lcm.model.DetachVolumeOutput;
import org.onap.appc.client.lcm.model.DetachVolumeInput;
import org.onap.appc.client.lcm.model.EvacuateOutput;
import org.onap.appc.client.lcm.model.EvacuateInput;
import org.onap.appc.client.lcm.model.HealthCheckOutput;
import org.onap.appc.client.lcm.model.HealthCheckInput;
import org.onap.appc.client.lcm.model.LiveUpgradeOutput;
import org.onap.appc.client.lcm.model.LiveUpgradeInput;
import org.onap.appc.client.lcm.model.LockOutput;
import org.onap.appc.client.lcm.model.LockInput;
import org.onap.appc.client.lcm.model.MigrateOutput;
import org.onap.appc.client.lcm.model.MigrateInput;
import org.onap.appc.client.lcm.model.QueryOutput;
import org.onap.appc.client.lcm.model.QueryInput;
import org.onap.appc.client.lcm.model.QuiesceTrafficOutput;
import org.onap.appc.client.lcm.model.QuiesceTrafficInput;
import org.onap.appc.client.lcm.model.RebootOutput;
import org.onap.appc.client.lcm.model.RebootInput;
import org.onap.appc.client.lcm.model.RebuildOutput;
import org.onap.appc.client.lcm.model.RebuildInput;
import org.onap.appc.client.lcm.model.RestartOutput;
import org.onap.appc.client.lcm.model.RestartInput;
import org.onap.appc.client.lcm.model.ResumeTrafficOutput;
import org.onap.appc.client.lcm.model.ResumeTrafficInput;
import org.onap.appc.client.lcm.model.RollbackOutput;
import org.onap.appc.client.lcm.model.RollbackInput;
import org.onap.appc.client.lcm.model.SnapshotOutput;
import org.onap.appc.client.lcm.model.SnapshotInput;
import org.onap.appc.client.lcm.model.SoftwareUploadOutput;
import org.onap.appc.client.lcm.model.SoftwareUploadInput;
import org.onap.appc.client.lcm.model.StartOutput;
import org.onap.appc.client.lcm.model.StartInput;
import org.onap.appc.client.lcm.model.StartApplicationOutput;
import org.onap.appc.client.lcm.model.StartApplicationInput;
import org.onap.appc.client.lcm.model.StopOutput;
import org.onap.appc.client.lcm.model.StopInput;
import org.onap.appc.client.lcm.model.StopApplicationOutput;
import org.onap.appc.client.lcm.model.StopApplicationInput;
import org.onap.appc.client.lcm.model.SyncOutput;
import org.onap.appc.client.lcm.model.SyncInput;
import org.onap.appc.client.lcm.model.TerminateOutput;
import org.onap.appc.client.lcm.model.TerminateInput;
import org.onap.appc.client.lcm.model.TestOutput;
import org.onap.appc.client.lcm.model.TestInput;
import org.onap.appc.client.lcm.model.UnlockOutput;
import org.onap.appc.client.lcm.model.UnlockInput;
import org.onap.appc.client.lcm.model.UpgradeBackoutOutput;
import org.onap.appc.client.lcm.model.UpgradeBackoutInput;
import org.onap.appc.client.lcm.model.UpgradeBackupOutput;
import org.onap.appc.client.lcm.model.UpgradeBackupInput;
import org.onap.appc.client.lcm.model.UpgradePostCheckOutput;
import org.onap.appc.client.lcm.model.UpgradePostCheckInput;
import org.onap.appc.client.lcm.model.UpgradePreCheckOutput;
import org.onap.appc.client.lcm.model.UpgradePreCheckInput;
import org.onap.appc.client.lcm.model.UpgradeSoftwareOutput;
import org.onap.appc.client.lcm.model.UpgradeSoftwareInput;
import org.onap.appc.client.lcm.exceptions.AppcClientException;
import org.onap.appc.RPC;

/**
* Defines the services and request/response requirements for the ECOMP APP-C component.
*/
@javax.annotation.Generated(
    value = {"client-kit/open-api-to-java.ftl"},
    date = "2017-11-27T12:27:49.205-06:00",
    comments = "Auto-generated from Open API specification")
public interface LifeCycleManagerStateful {

    /**
     * An operation to get the current state of the previously submitted LCM request
     *
     * @param actionStatusInput - RPC input object
     */
    @RPC(name="action-status", outputType=ActionStatusOutput.class)
    ActionStatusOutput actionStatus(ActionStatusInput actionStatusInput) throws AppcClientException;

    /**
     * An operation to get the current state of the previously submitted LCM request
     *
     * @param actionStatusInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="action-status", outputType=ActionStatusOutput.class)
    void actionStatus(ActionStatusInput actionStatusInput, ResponseHandler<ActionStatusOutput> listener) throws AppcClientException;

    /**
     * An operation to attach a cinder volume to a VM
     *
     * @param attachVolumeInput - RPC input object
     */
    @RPC(name="attach-volume", outputType=AttachVolumeOutput.class)
    AttachVolumeOutput attachVolume(AttachVolumeInput attachVolumeInput) throws AppcClientException;

    /**
     * An operation to attach a cinder volume to a VM
     *
     * @param attachVolumeInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="attach-volume", outputType=AttachVolumeOutput.class)
    void attachVolume(AttachVolumeInput attachVolumeInput, ResponseHandler<AttachVolumeOutput> listener) throws AppcClientException;

    /**
     * An operation to audit the configurations of a virtual network function (or VM)
     *
     * @param auditInput - RPC input object
     */
    @RPC(name="audit", outputType=AuditOutput.class)
    AuditOutput audit(AuditInput auditInput) throws AppcClientException;

    /**
     * An operation to audit the configurations of a virtual network function (or VM)
     *
     * @param auditInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="audit", outputType=AuditOutput.class)
    void audit(AuditInput auditInput, ResponseHandler<AuditOutput> listener) throws AppcClientException;

    /**
     * An operation to check VNF lock status
     *
     * @param checkLockInput - RPC input object
     */
    @RPC(name="check-lock", outputType=CheckLockOutput.class)
    CheckLockOutput checkLock(CheckLockInput checkLockInput) throws AppcClientException;

    /**
     * An operation to check VNF lock status
     *
     * @param checkLockInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="check-lock", outputType=CheckLockOutput.class)
    void checkLock(CheckLockInput checkLockInput, ResponseHandler<CheckLockOutput> listener) throws AppcClientException;

    /**
     * An operation to Backup configurations of a virtual network function (or VM)
     *
     * @param configBackupInput - RPC input object
     */
    @RPC(name="config-backup", outputType=ConfigBackupOutput.class)
    ConfigBackupOutput configBackup(ConfigBackupInput configBackupInput) throws AppcClientException;

    /**
     * An operation to Backup configurations of a virtual network function (or VM)
     *
     * @param configBackupInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="config-backup", outputType=ConfigBackupOutput.class)
    void configBackup(ConfigBackupInput configBackupInput, ResponseHandler<ConfigBackupOutput> listener) throws AppcClientException;

    /**
     * An operation to Delete backup configurations of a virtual network function (or VM)
     *
     * @param configBackupDeleteInput - RPC input object
     */
    @RPC(name="config-backup-delete", outputType=ConfigBackupDeleteOutput.class)
    ConfigBackupDeleteOutput configBackupDelete(ConfigBackupDeleteInput configBackupDeleteInput) throws AppcClientException;

    /**
     * An operation to Delete backup configurations of a virtual network function (or VM)
     *
     * @param configBackupDeleteInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="config-backup-delete", outputType=ConfigBackupDeleteOutput.class)
    void configBackupDelete(ConfigBackupDeleteInput configBackupDeleteInput, ResponseHandler<ConfigBackupDeleteOutput> listener) throws AppcClientException;

    /**
     * An operation to Export configurations of a virtual network function (or VM)
     *
     * @param configExportInput - RPC input object
     */
    @RPC(name="config-export", outputType=ConfigExportOutput.class)
    ConfigExportOutput configExport(ConfigExportInput configExportInput) throws AppcClientException;

    /**
     * An operation to Export configurations of a virtual network function (or VM)
     *
     * @param configExportInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="config-export", outputType=ConfigExportOutput.class)
    void configExport(ConfigExportInput configExportInput, ResponseHandler<ConfigExportOutput> listener) throws AppcClientException;

    /**
     * Use the ModifyConfig command when a full configuration cycle is either not required or is considered too costly. The ModifyConfig LCM action affects only a subset of the total configuration data of a VNF. The set of configuration parameters to be affected is a subset of the total configuration data of the target VNF type. The payload Stop Application must contain the configuration parameters to be modified and their values. A successful modify returns a success response. A failed modify returns a failure response and the specific failure messages in the response payload Stop Application
     *
     * @param configModifyInput - RPC input object
     */
    @RPC(name="config-modify", outputType=ConfigModifyOutput.class)
    ConfigModifyOutput configModify(ConfigModifyInput configModifyInput) throws AppcClientException;

    /**
     * Use the ModifyConfig command when a full configuration cycle is either not required or is considered too costly. The ModifyConfig LCM action affects only a subset of the total configuration data of a VNF. The set of configuration parameters to be affected is a subset of the total configuration data of the target VNF type. The payload Stop Application must contain the configuration parameters to be modified and their values. A successful modify returns a success response. A failed modify returns a failure response and the specific failure messages in the response payload Stop Application
     *
     * @param configModifyInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="config-modify", outputType=ConfigModifyOutput.class)
    void configModify(ConfigModifyInput configModifyInput, ResponseHandler<ConfigModifyOutput> listener) throws AppcClientException;

    /**
     * An operation to restore the configurations of a virtual network function (or VM)
     *
     * @param configRestoreInput - RPC input object
     */
    @RPC(name="config-restore", outputType=ConfigRestoreOutput.class)
    ConfigRestoreOutput configRestore(ConfigRestoreInput configRestoreInput) throws AppcClientException;

    /**
     * An operation to restore the configurations of a virtual network function (or VM)
     *
     * @param configRestoreInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="config-restore", outputType=ConfigRestoreOutput.class)
    void configRestore(ConfigRestoreInput configRestoreInput, ResponseHandler<ConfigRestoreOutput> listener) throws AppcClientException;

    /**
     * An operation to scaleout the configurations of a virtual network function (or VM)
     *
     * @param configScaleoutInput - RPC input object
     */
    @RPC(name="config-scaleout", outputType=ConfigScaleoutOutput.class)
    ConfigScaleoutOutput configScaleout(ConfigScaleoutInput configScaleoutInput) throws AppcClientException;

    /**
     * An operation to scaleout the configurations of a virtual network function (or VM)
     *
     * @param configScaleoutInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="config-scaleout", outputType=ConfigScaleoutOutput.class)
    void configScaleout(ConfigScaleoutInput configScaleoutInput, ResponseHandler<ConfigScaleoutOutput> listener) throws AppcClientException;

    /**
     * An operation to configure the configurations of a virtual network function (or VM)
     *
     * @param configureInput - RPC input object
     */
    @RPC(name="configure", outputType=ConfigureOutput.class)
    ConfigureOutput configure(ConfigureInput configureInput) throws AppcClientException;

    /**
     * An operation to configure the configurations of a virtual network function (or VM)
     *
     * @param configureInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="configure", outputType=ConfigureOutput.class)
    void configure(ConfigureInput configureInput, ResponseHandler<ConfigureOutput> listener) throws AppcClientException;

    /**
     * An operation to detach a cinder volume from a VM
     *
     * @param detachVolumeInput - RPC input object
     */
    @RPC(name="detach-volume", outputType=DetachVolumeOutput.class)
    DetachVolumeOutput detachVolume(DetachVolumeInput detachVolumeInput) throws AppcClientException;

    /**
     * An operation to detach a cinder volume from a VM
     *
     * @param detachVolumeInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="detach-volume", outputType=DetachVolumeOutput.class)
    void detachVolume(DetachVolumeInput detachVolumeInput, ResponseHandler<DetachVolumeOutput> listener) throws AppcClientException;

    /**
     * An operation to evacuate a virtual network function (or VM)
     *
     * @param evacuateInput - RPC input object
     */
    @RPC(name="evacuate", outputType=EvacuateOutput.class)
    EvacuateOutput evacuate(EvacuateInput evacuateInput) throws AppcClientException;

    /**
     * An operation to evacuate a virtual network function (or VM)
     *
     * @param evacuateInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="evacuate", outputType=EvacuateOutput.class)
    void evacuate(EvacuateInput evacuateInput, ResponseHandler<EvacuateOutput> listener) throws AppcClientException;

    /**
     * An operation to perform health check of vSCP prior its upgrading
     *
     * @param healthCheckInput - RPC input object
     */
    @RPC(name="health-check", outputType=HealthCheckOutput.class)
    HealthCheckOutput healthCheck(HealthCheckInput healthCheckInput) throws AppcClientException;

    /**
     * An operation to perform health check of vSCP prior its upgrading
     *
     * @param healthCheckInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="health-check", outputType=HealthCheckOutput.class)
    void healthCheck(HealthCheckInput healthCheckInput, ResponseHandler<HealthCheckOutput> listener) throws AppcClientException;

    /**
     * An operation to perform upgrade of vSCP
     *
     * @param liveUpgradeInput - RPC input object
     */
    @RPC(name="live-upgrade", outputType=LiveUpgradeOutput.class)
    LiveUpgradeOutput liveUpgrade(LiveUpgradeInput liveUpgradeInput) throws AppcClientException;

    /**
     * An operation to perform upgrade of vSCP
     *
     * @param liveUpgradeInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="live-upgrade", outputType=LiveUpgradeOutput.class)
    void liveUpgrade(LiveUpgradeInput liveUpgradeInput, ResponseHandler<LiveUpgradeOutput> listener) throws AppcClientException;

    /**
     * An operation to perform VNF lock operation
     *
     * @param lockInput - RPC input object
     */
    @RPC(name="lock", outputType=LockOutput.class)
    LockOutput lock(LockInput lockInput) throws AppcClientException;

    /**
     * An operation to perform VNF lock operation
     *
     * @param lockInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="lock", outputType=LockOutput.class)
    void lock(LockInput lockInput, ResponseHandler<LockOutput> listener) throws AppcClientException;

    /**
     * An operation to migrate a virtual network function (or VM)
     *
     * @param migrateInput - RPC input object
     */
    @RPC(name="migrate", outputType=MigrateOutput.class)
    MigrateOutput migrate(MigrateInput migrateInput) throws AppcClientException;

    /**
     * An operation to migrate a virtual network function (or VM)
     *
     * @param migrateInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="migrate", outputType=MigrateOutput.class)
    void migrate(MigrateInput migrateInput, ResponseHandler<MigrateOutput> listener) throws AppcClientException;

    /**
     * An operation to query the status of a targe VNF. Returns information on each VM, including state (active or standby) and status (healthy or unhealthy)
     *
     * @param queryInput - RPC input object
     */
    @RPC(name="query", outputType=QueryOutput.class)
    QueryOutput query(QueryInput queryInput) throws AppcClientException;

    /**
     * An operation to query the status of a targe VNF. Returns information on each VM, including state (active or standby) and status (healthy or unhealthy)
     *
     * @param queryInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="query", outputType=QueryOutput.class)
    void query(QueryInput queryInput, ResponseHandler<QueryOutput> listener) throws AppcClientException;

    /**
     * An operation to stop traffic gracefully on the VF. It stops traffic gracefully without stopping the application
     *
     * @param quiesceTrafficInput - RPC input object
     */
    @RPC(name="quiesce-traffic", outputType=QuiesceTrafficOutput.class)
    QuiesceTrafficOutput quiesceTraffic(QuiesceTrafficInput quiesceTrafficInput) throws AppcClientException;

    /**
     * An operation to stop traffic gracefully on the VF. It stops traffic gracefully without stopping the application
     *
     * @param quiesceTrafficInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="quiesce-traffic", outputType=QuiesceTrafficOutput.class)
    void quiesceTraffic(QuiesceTrafficInput quiesceTrafficInput, ResponseHandler<QuiesceTrafficOutput> listener) throws AppcClientException;

    /**
     * An operation to reboot a specified virtual machine (VM)
     *
     * @param rebootInput - RPC input object
     */
    @RPC(name="reboot", outputType=RebootOutput.class)
    RebootOutput reboot(RebootInput rebootInput) throws AppcClientException;

    /**
     * An operation to reboot a specified virtual machine (VM)
     *
     * @param rebootInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="reboot", outputType=RebootOutput.class)
    void reboot(RebootInput rebootInput, ResponseHandler<RebootOutput> listener) throws AppcClientException;

    /**
     * An operation to rebuild a virtual network function (or VM)
     *
     * @param rebuildInput - RPC input object
     */
    @RPC(name="rebuild", outputType=RebuildOutput.class)
    RebuildOutput rebuild(RebuildInput rebuildInput) throws AppcClientException;

    /**
     * An operation to rebuild a virtual network function (or VM)
     *
     * @param rebuildInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="rebuild", outputType=RebuildOutput.class)
    void rebuild(RebuildInput rebuildInput, ResponseHandler<RebuildOutput> listener) throws AppcClientException;

    /**
     * An operation to restart a virtual network function (or VM)
     *
     * @param restartInput - RPC input object
     */
    @RPC(name="restart", outputType=RestartOutput.class)
    RestartOutput restart(RestartInput restartInput) throws AppcClientException;

    /**
     * An operation to restart a virtual network function (or VM)
     *
     * @param restartInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="restart", outputType=RestartOutput.class)
    void restart(RestartInput restartInput, ResponseHandler<RestartOutput> listener) throws AppcClientException;

    /**
     * An operation to resume traffic gracefully on the VF. It resumes traffic gracefully without stopping the application
     *
     * @param resumeTrafficInput - RPC input object
     */
    @RPC(name="resume-traffic", outputType=ResumeTrafficOutput.class)
    ResumeTrafficOutput resumeTraffic(ResumeTrafficInput resumeTrafficInput) throws AppcClientException;

    /**
     * An operation to resume traffic gracefully on the VF. It resumes traffic gracefully without stopping the application
     *
     * @param resumeTrafficInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="resume-traffic", outputType=ResumeTrafficOutput.class)
    void resumeTraffic(ResumeTrafficInput resumeTrafficInput, ResponseHandler<ResumeTrafficOutput> listener) throws AppcClientException;

    /**
     * An operation to rollback to particular snapshot of a virtual network function (or VM)
     *
     * @param rollbackInput - RPC input object
     */
    @RPC(name="rollback", outputType=RollbackOutput.class)
    RollbackOutput rollback(RollbackInput rollbackInput) throws AppcClientException;

    /**
     * An operation to rollback to particular snapshot of a virtual network function (or VM)
     *
     * @param rollbackInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="rollback", outputType=RollbackOutput.class)
    void rollback(RollbackInput rollbackInput, ResponseHandler<RollbackOutput> listener) throws AppcClientException;

    /**
     * An operation to create a snapshot of a virtual network function (or VM)
     *
     * @param snapshotInput - RPC input object
     */
    @RPC(name="snapshot", outputType=SnapshotOutput.class)
    SnapshotOutput snapshot(SnapshotInput snapshotInput) throws AppcClientException;

    /**
     * An operation to create a snapshot of a virtual network function (or VM)
     *
     * @param snapshotInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="snapshot", outputType=SnapshotOutput.class)
    void snapshot(SnapshotInput snapshotInput, ResponseHandler<SnapshotOutput> listener) throws AppcClientException;

    /**
     * An operation to upload a new version of vSCP image to vSCP for updating it
     *
     * @param softwareUploadInput - RPC input object
     */
    @RPC(name="software-upload", outputType=SoftwareUploadOutput.class)
    SoftwareUploadOutput softwareUpload(SoftwareUploadInput softwareUploadInput) throws AppcClientException;

    /**
     * An operation to upload a new version of vSCP image to vSCP for updating it
     *
     * @param softwareUploadInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="software-upload", outputType=SoftwareUploadOutput.class)
    void softwareUpload(SoftwareUploadInput softwareUploadInput, ResponseHandler<SoftwareUploadOutput> listener) throws AppcClientException;

    /**
     * An operation to start a virtual network function (or VM)
     *
     * @param startInput - RPC input object
     */
    @RPC(name="start", outputType=StartOutput.class)
    StartOutput start(StartInput startInput) throws AppcClientException;

    /**
     * An operation to start a virtual network function (or VM)
     *
     * @param startInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="start", outputType=StartOutput.class)
    void start(StartInput startInput, ResponseHandler<StartOutput> listener) throws AppcClientException;

    /**
     * An operation to perform VNF Start Application operation
     *
     * @param startApplicationInput - RPC input object
     */
    @RPC(name="start-application", outputType=StartApplicationOutput.class)
    StartApplicationOutput startApplication(StartApplicationInput startApplicationInput) throws AppcClientException;

    /**
     * An operation to perform VNF Start Application operation
     *
     * @param startApplicationInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="start-application", outputType=StartApplicationOutput.class)
    void startApplication(StartApplicationInput startApplicationInput, ResponseHandler<StartApplicationOutput> listener) throws AppcClientException;

    /**
     * An operation to stop the configurations of a virtual network function (or VM)
     *
     * @param stopInput - RPC input object
     */
    @RPC(name="stop", outputType=StopOutput.class)
    StopOutput stop(StopInput stopInput) throws AppcClientException;

    /**
     * An operation to stop the configurations of a virtual network function (or VM)
     *
     * @param stopInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="stop", outputType=StopOutput.class)
    void stop(StopInput stopInput, ResponseHandler<StopOutput> listener) throws AppcClientException;

    /**
     * An operation to Stop Application traffic to a virtual network function
     *
     * @param stopApplicationInput - RPC input object
     */
    @RPC(name="stop-application", outputType=StopApplicationOutput.class)
    StopApplicationOutput stopApplication(StopApplicationInput stopApplicationInput) throws AppcClientException;

    /**
     * An operation to Stop Application traffic to a virtual network function
     *
     * @param stopApplicationInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="stop-application", outputType=StopApplicationOutput.class)
    void stopApplication(StopApplicationInput stopApplicationInput, ResponseHandler<StopApplicationOutput> listener) throws AppcClientException;

    /**
     * An operation to sync the configurations of a virtual network function (or VM)
     *
     * @param syncInput - RPC input object
     */
    @RPC(name="sync", outputType=SyncOutput.class)
    SyncOutput sync(SyncInput syncInput) throws AppcClientException;

    /**
     * An operation to sync the configurations of a virtual network function (or VM)
     *
     * @param syncInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="sync", outputType=SyncOutput.class)
    void sync(SyncInput syncInput, ResponseHandler<SyncOutput> listener) throws AppcClientException;

    /**
     * An operation to terminate the configurations of a virtual network function (or VM)
     *
     * @param terminateInput - RPC input object
     */
    @RPC(name="terminate", outputType=TerminateOutput.class)
    TerminateOutput terminate(TerminateInput terminateInput) throws AppcClientException;

    /**
     * An operation to terminate the configurations of a virtual network function (or VM)
     *
     * @param terminateInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="terminate", outputType=TerminateOutput.class)
    void terminate(TerminateInput terminateInput, ResponseHandler<TerminateOutput> listener) throws AppcClientException;

    /**
     * An operation to test the configurations of a virtual network function (or VM)
     *
     * @param testInput - RPC input object
     */
    @RPC(name="test", outputType=TestOutput.class)
    TestOutput test(TestInput testInput) throws AppcClientException;

    /**
     * An operation to test the configurations of a virtual network function (or VM)
     *
     * @param testInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="test", outputType=TestOutput.class)
    void test(TestInput testInput, ResponseHandler<TestOutput> listener) throws AppcClientException;

    /**
     * An operation to perform VNF unlock operation
     *
     * @param unlockInput - RPC input object
     */
    @RPC(name="unlock", outputType=UnlockOutput.class)
    UnlockOutput unlock(UnlockInput unlockInput) throws AppcClientException;

    /**
     * An operation to perform VNF unlock operation
     *
     * @param unlockInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="unlock", outputType=UnlockOutput.class)
    void unlock(UnlockInput unlockInput, ResponseHandler<UnlockOutput> listener) throws AppcClientException;

    /**
     * An operation does a backout after an UpgradeSoftware is completed (either successfully or unsuccessfully).
     *
     * @param upgradeBackoutInput - RPC input object
     */
    @RPC(name="upgrade-backout", outputType=UpgradeBackoutOutput.class)
    UpgradeBackoutOutput upgradeBackout(UpgradeBackoutInput upgradeBackoutInput) throws AppcClientException;

    /**
     * An operation does a backout after an UpgradeSoftware is completed (either successfully or unsuccessfully).
     *
     * @param upgradeBackoutInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="upgrade-backout", outputType=UpgradeBackoutOutput.class)
    void upgradeBackout(UpgradeBackoutInput upgradeBackoutInput, ResponseHandler<UpgradeBackoutOutput> listener) throws AppcClientException;

    /**
     * An operation to do full backup of the VNF data prior to an upgrade.
     *
     * @param upgradeBackupInput - RPC input object
     */
    @RPC(name="upgrade-backup", outputType=UpgradeBackupOutput.class)
    UpgradeBackupOutput upgradeBackup(UpgradeBackupInput upgradeBackupInput) throws AppcClientException;

    /**
     * An operation to do full backup of the VNF data prior to an upgrade.
     *
     * @param upgradeBackupInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="upgrade-backup", outputType=UpgradeBackupOutput.class)
    void upgradeBackup(UpgradeBackupInput upgradeBackupInput, ResponseHandler<UpgradeBackupOutput> listener) throws AppcClientException;

    /**
     * An operation to check the VNF upgrade has been successful completed and all processes are running properly.
     *
     * @param upgradePostCheckInput - RPC input object
     */
    @RPC(name="upgrade-post-check", outputType=UpgradePostCheckOutput.class)
    UpgradePostCheckOutput upgradePostCheck(UpgradePostCheckInput upgradePostCheckInput) throws AppcClientException;

    /**
     * An operation to check the VNF upgrade has been successful completed and all processes are running properly.
     *
     * @param upgradePostCheckInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="upgrade-post-check", outputType=UpgradePostCheckOutput.class)
    void upgradePostCheck(UpgradePostCheckInput upgradePostCheckInput, ResponseHandler<UpgradePostCheckOutput> listener) throws AppcClientException;

    /**
     * An operation to check that the VNF has the correct software version needed for a software upgrade.
     *
     * @param upgradePreCheckInput - RPC input object
     */
    @RPC(name="upgrade-pre-check", outputType=UpgradePreCheckOutput.class)
    UpgradePreCheckOutput upgradePreCheck(UpgradePreCheckInput upgradePreCheckInput) throws AppcClientException;

    /**
     * An operation to check that the VNF has the correct software version needed for a software upgrade.
     *
     * @param upgradePreCheckInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="upgrade-pre-check", outputType=UpgradePreCheckOutput.class)
    void upgradePreCheck(UpgradePreCheckInput upgradePreCheckInput, ResponseHandler<UpgradePreCheckOutput> listener) throws AppcClientException;

    /**
     * An operation to upgrade the target VNF to a new version and expected that the VNF is in a quiesced status .
     *
     * @param upgradeSoftwareInput - RPC input object
     */
    @RPC(name="upgrade-software", outputType=UpgradeSoftwareOutput.class)
    UpgradeSoftwareOutput upgradeSoftware(UpgradeSoftwareInput upgradeSoftwareInput) throws AppcClientException;

    /**
     * An operation to upgrade the target VNF to a new version and expected that the VNF is in a quiesced status .
     *
     * @param upgradeSoftwareInput - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="upgrade-software", outputType=UpgradeSoftwareOutput.class)
    void upgradeSoftware(UpgradeSoftwareInput upgradeSoftwareInput, ResponseHandler<UpgradeSoftwareOutput> listener) throws AppcClientException;

}
