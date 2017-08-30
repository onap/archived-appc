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
package org.openecomp.appc.client.lcm.api;

import org.openecomp.appc.client.lcm.model.AuditOutput;
import org.openecomp.appc.client.lcm.model.AuditInput;
import org.openecomp.appc.client.lcm.model.CheckLockOutput;
import org.openecomp.appc.client.lcm.model.CheckLockInput;
import org.openecomp.appc.client.lcm.model.ConfigBackupOutput;
import org.openecomp.appc.client.lcm.model.ConfigBackupInput;
import org.openecomp.appc.client.lcm.model.ConfigBackupDeleteOutput;
import org.openecomp.appc.client.lcm.model.ConfigBackupDeleteInput;
import org.openecomp.appc.client.lcm.model.ConfigExportOutput;
import org.openecomp.appc.client.lcm.model.ConfigExportInput;
import org.openecomp.appc.client.lcm.model.ConfigModifyOutput;
import org.openecomp.appc.client.lcm.model.ConfigModifyInput;
import org.openecomp.appc.client.lcm.model.ConfigRestoreOutput;
import org.openecomp.appc.client.lcm.model.ConfigRestoreInput;
import org.openecomp.appc.client.lcm.model.ConfigScaleoutOutput;
import org.openecomp.appc.client.lcm.model.ConfigScaleoutInput;
import org.openecomp.appc.client.lcm.model.ConfigureOutput;
import org.openecomp.appc.client.lcm.model.ConfigureInput;
import org.openecomp.appc.client.lcm.model.EvacuateOutput;
import org.openecomp.appc.client.lcm.model.EvacuateInput;
import org.openecomp.appc.client.lcm.model.HealthCheckOutput;
import org.openecomp.appc.client.lcm.model.HealthCheckInput;
import org.openecomp.appc.client.lcm.model.LiveUpgradeOutput;
import org.openecomp.appc.client.lcm.model.LiveUpgradeInput;
import org.openecomp.appc.client.lcm.model.LockOutput;
import org.openecomp.appc.client.lcm.model.LockInput;
import org.openecomp.appc.client.lcm.model.MigrateOutput;
import org.openecomp.appc.client.lcm.model.MigrateInput;
import org.openecomp.appc.client.lcm.model.RebuildOutput;
import org.openecomp.appc.client.lcm.model.RebuildInput;
import org.openecomp.appc.client.lcm.model.RestartOutput;
import org.openecomp.appc.client.lcm.model.RestartInput;
import org.openecomp.appc.client.lcm.model.RollbackOutput;
import org.openecomp.appc.client.lcm.model.RollbackInput;
import org.openecomp.appc.client.lcm.model.SnapshotOutput;
import org.openecomp.appc.client.lcm.model.SnapshotInput;
import org.openecomp.appc.client.lcm.model.SoftwareUploadOutput;
import org.openecomp.appc.client.lcm.model.SoftwareUploadInput;
import org.openecomp.appc.client.lcm.model.StartOutput;
import org.openecomp.appc.client.lcm.model.StartInput;
import org.openecomp.appc.client.lcm.model.StopOutput;
import org.openecomp.appc.client.lcm.model.StopInput;
import org.openecomp.appc.client.lcm.model.SyncOutput;
import org.openecomp.appc.client.lcm.model.SyncInput;
import org.openecomp.appc.client.lcm.model.TerminateOutput;
import org.openecomp.appc.client.lcm.model.TerminateInput;
import org.openecomp.appc.client.lcm.model.TestOutput;
import org.openecomp.appc.client.lcm.model.TestInput;
import org.openecomp.appc.client.lcm.model.UnlockOutput;
import org.openecomp.appc.client.lcm.model.UnlockInput;
import org.openecomp.appc.RPC;
import org.openecomp.appc.client.lcm.exceptions.AppcClientException;

/**
 * Defines the services and request/response requirements for the ECOMP APP-C
 * component.
 */
@javax.annotation.Generated(value = {
        "templates/client-kit/open-api-to-java.ftl" }, date = "2017-05-04T20:09:01.723+05:30", comments = "Auto-generated from Open API specification")
public interface LifeCycleManagerStateful {

    /**
     * The Audit command compares the configuration of the VNF associated with the
     * current request against the configuration that is stored in APPC's
     * configuration store. A successful Audit means that the current VNF
     * configuration matches the APPC stored configuration. A failed Audit indicates
     * that the request configuration is different from the stored configuration.
     * This command can be applied to any VNF type. The only restriction is that a
     * particular VNF should be able to support the interface for Reading
     * Configuration using existing adapters and use the following protocols: CLI,
     * RestConf and XML. The Audit action does not require any payload parameters
     *
     * @param auditInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "audit", outputType = AuditOutput.class)
    AuditOutput audit(AuditInput auditInput) throws AppcClientException;

    /**
     * The Audit command compares the configuration of the VNF associated with the
     * current request against the configuration that is stored in APPC's
     * configuration store. A successful Audit means that the current VNF
     * configuration matches the APPC stored configuration. A failed Audit indicates
     * that the request configuration is different from the stored configuration.
     * This command can be applied to any VNF type. The only restriction is that a
     * particular VNF should be able to support the interface for Reading
     * Configuration using existing adapters and use the following protocols: CLI,
     * RestConf and XML. The Audit action does not require any payload parameters
     *
     * @param auditInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "audit", outputType = AuditOutput.class)
    void audit(AuditInput auditInput, ResponseHandler<AuditOutput> listener) throws AppcClientException;

    /**
     * The CheckLock command returns true if the specified VNF is locked, false if
     * not. A CheckLock command is deemed successful if the processing completes
     * without error, whether the VNF is locked or not. The command returns only a
     * single response with a final status. The APPC also locks the target VNF
     * during any command processing, so a VNF can have a locked status even if no
     * Lock command has been explicitly called. The CheckLock command returns a
     * specific response structure that extends the default LCM response. The
     * CheckLock action does not require any payload parameters
     *
     * @param checkLockInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "check-lock", outputType = CheckLockOutput.class)
    CheckLockOutput checkLock(CheckLockInput checkLockInput) throws AppcClientException;

    /**
     * The CheckLock command returns true if the specified VNF is locked, false if
     * not. A CheckLock command is deemed successful if the processing completes
     * without error, whether the VNF is locked or not. The command returns only a
     * single response with a final status. The APPC also locks the target VNF
     * during any command processing, so a VNF can have a locked status even if no
     * Lock command has been explicitly called. The CheckLock command returns a
     * specific response structure that extends the default LCM response. The
     * CheckLock action does not require any payload parameters
     *
     * @param checkLockInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "check-lock", outputType = CheckLockOutput.class)
    void checkLock(CheckLockInput checkLockInput, ResponseHandler<CheckLockOutput> listener) throws AppcClientException;

    /**
     * An operation to Backup configurations of a virtual network function (or VM)
     *
     * @param configBackupInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-backup", outputType = ConfigBackupOutput.class)
    ConfigBackupOutput configBackup(ConfigBackupInput configBackupInput) throws AppcClientException;

    /**
     * An operation to Backup configurations of a virtual network function (or VM)
     *
     * @param configBackupInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-backup", outputType = ConfigBackupOutput.class)
    void configBackup(ConfigBackupInput configBackupInput, ResponseHandler<ConfigBackupOutput> listener)
            throws AppcClientException;

    /**
     * An operation to Delete backup configurations of a virtual network function
     * (or VM)
     *
     * @param configBackupDeleteInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-backup-delete", outputType = ConfigBackupDeleteOutput.class)
    ConfigBackupDeleteOutput configBackupDelete(ConfigBackupDeleteInput configBackupDeleteInput)
            throws AppcClientException;

    /**
     * An operation to Delete backup configurations of a virtual network function
     * (or VM)
     *
     * @param configBackupDeleteInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-backup-delete", outputType = ConfigBackupDeleteOutput.class)
    void configBackupDelete(ConfigBackupDeleteInput configBackupDeleteInput,
            ResponseHandler<ConfigBackupDeleteOutput> listener) throws AppcClientException;

    /**
     * An operation to Export configurations of a virtual network function (or VM)
     *
     * @param configExportInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-export", outputType = ConfigExportOutput.class)
    ConfigExportOutput configExport(ConfigExportInput configExportInput) throws AppcClientException;

    /**
     * An operation to Export configurations of a virtual network function (or VM)
     *
     * @param configExportInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-export", outputType = ConfigExportOutput.class)
    void configExport(ConfigExportInput configExportInput, ResponseHandler<ConfigExportOutput> listener)
            throws AppcClientException;

    /**
     * Use the ModifyConfig command when a full configuration cycle is either not
     * required or is considered too costly. The ModifyConfig LCM action affects
     * only a subset of the total configuration data of a VNF. The set of
     * configuration parameters to be affected is a subset of the total
     * configuration data of the target VNF type. The payload block must contain the
     * configuration parameters to be modified and their values. A successful modify
     * returns a success response. A failed modify returns a failure response and
     * the specific failure messages in the response payload block
     *
     * @param configModifyInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-modify", outputType = ConfigModifyOutput.class)
    ConfigModifyOutput configModify(ConfigModifyInput configModifyInput) throws AppcClientException;

    /**
     * Use the ModifyConfig command when a full configuration cycle is either not
     * required or is considered too costly. The ModifyConfig LCM action affects
     * only a subset of the total configuration data of a VNF. The set of
     * configuration parameters to be affected is a subset of the total
     * configuration data of the target VNF type. The payload block must contain the
     * configuration parameters to be modified and their values. A successful modify
     * returns a success response. A failed modify returns a failure response and
     * the specific failure messages in the response payload block
     *
     * @param configModifyInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-modify", outputType = ConfigModifyOutput.class)
    void configModify(ConfigModifyInput configModifyInput, ResponseHandler<ConfigModifyOutput> listener)
            throws AppcClientException;

    /**
     * An operation to restore the configurations of a virtual network function (or
     * VM)
     *
     * @param configRestoreInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-restore", outputType = ConfigRestoreOutput.class)
    ConfigRestoreOutput configRestore(ConfigRestoreInput configRestoreInput) throws AppcClientException;

    /**
     * An operation to restore the configurations of a virtual network function (or
     * VM)
     *
     * @param configRestoreInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-restore", outputType = ConfigRestoreOutput.class)
    void configRestore(ConfigRestoreInput configRestoreInput, ResponseHandler<ConfigRestoreOutput> listener)
            throws AppcClientException;

    /**
     * An operation to scaleout the configurations of a virtual network function (or
     * VM)
     *
     * @param configScaleoutInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-scaleout", outputType = ConfigScaleoutOutput.class)
    ConfigScaleoutOutput configScaleout(ConfigScaleoutInput configScaleoutInput) throws AppcClientException;

    /**
     * An operation to scaleout the configurations of a virtual network function (or
     * VM)
     *
     * @param configScaleoutInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "config-scaleout", outputType = ConfigScaleoutOutput.class)
    void configScaleout(ConfigScaleoutInput configScaleoutInput, ResponseHandler<ConfigScaleoutOutput> listener)
            throws AppcClientException;

    /**
     * An operation to configure the configurations of a virtual network function
     * (or VM)
     *
     * @param configureInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "configure", outputType = ConfigureOutput.class)
    ConfigureOutput configure(ConfigureInput configureInput) throws AppcClientException;

    /**
     * An operation to configure the configurations of a virtual network function
     * (or VM)
     *
     * @param configureInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "configure", outputType = ConfigureOutput.class)
    void configure(ConfigureInput configureInput, ResponseHandler<ConfigureOutput> listener) throws AppcClientException;

    /**
     * An operation to evacuate a virtual network function (or VM)
     *
     * @param evacuateInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "evacuate", outputType = EvacuateOutput.class)
    EvacuateOutput evacuate(EvacuateInput evacuateInput) throws AppcClientException;

    /**
     * An operation to evacuate a virtual network function (or VM)
     *
     * @param evacuateInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "evacuate", outputType = EvacuateOutput.class)
    void evacuate(EvacuateInput evacuateInput, ResponseHandler<EvacuateOutput> listener) throws AppcClientException;

    /**
     * This command runs a VNF health check and returns the result. A health check
     * is VNF-specific. For a complex VNF, APPC initiates further subordinate health
     * checks
     *
     * @param healthCheckInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "health-check", outputType = HealthCheckOutput.class)
    HealthCheckOutput healthCheck(HealthCheckInput healthCheckInput) throws AppcClientException;

    /**
     * This command runs a VNF health check and returns the result. A health check
     * is VNF-specific. For a complex VNF, APPC initiates further subordinate health
     * checks
     *
     * @param healthCheckInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "health-check", outputType = HealthCheckOutput.class)
    void healthCheck(HealthCheckInput healthCheckInput, ResponseHandler<HealthCheckOutput> listener)
            throws AppcClientException;

    /**
     * The LiveUpgrade LCM action upgrades the target VNF to a new version without
     * interrupting VNF operation. A successful upgrade returns a success status. A
     * failed upgrade returns a failure code and the failure messages in the
     * response payload block. The payload includes the IP of the location that
     * hosts the new software version installer file and the new software version.
     * Connections or operations that are active at the time of the LiveUpgrade
     * action request will not be interrupted by the action and, therefore, the
     * action may take a significant amount of time to run. A LiveUpgrade is defined
     * as non-disruptive; it is the responsibility of the VNF to handle disruptions
     * if they occur
     *
     * @param liveUpgradeInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "live-upgrade", outputType = LiveUpgradeOutput.class)
    LiveUpgradeOutput liveUpgrade(LiveUpgradeInput liveUpgradeInput) throws AppcClientException;

    /**
     * The LiveUpgrade LCM action upgrades the target VNF to a new version without
     * interrupting VNF operation. A successful upgrade returns a success status. A
     * failed upgrade returns a failure code and the failure messages in the
     * response payload block. The payload includes the IP of the location that
     * hosts the new software version installer file and the new software version.
     * Connections or operations that are active at the time of the LiveUpgrade
     * action request will not be interrupted by the action and, therefore, the
     * action may take a significant amount of time to run. A LiveUpgrade is defined
     * as non-disruptive; it is the responsibility of the VNF to handle disruptions
     * if they occur
     *
     * @param liveUpgradeInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "live-upgrade", outputType = LiveUpgradeOutput.class)
    void liveUpgrade(LiveUpgradeInput liveUpgradeInput, ResponseHandler<LiveUpgradeOutput> listener)
            throws AppcClientException;

    /**
     * Use the Lock command to ensure exclusive access during a series of critical
     * LCM commands. The Lock action will return a successful result if the VNF is
     * not already locked or if it was locked with the same request-id, otherwise
     * the action returns a response with a reject status code. When a VNF is
     * locked, any subsequent sequential commands with same request-id will be
     * accepted. Commands associated with other request-ids will be rejected. The
     * Lock command returns only one final response with the status of the request
     * processing. The APPC also locks the target VNF during any command processing.
     * If a lock action is then requested on that VNF, it will be rejected because
     * the VNF was already locked, even though no actual lock command was explicitly
     * invoked
     *
     * @param lockInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "lock", outputType = LockOutput.class)
    LockOutput lock(LockInput lockInput) throws AppcClientException;

    /**
     * Use the Lock command to ensure exclusive access during a series of critical
     * LCM commands. The Lock action will return a successful result if the VNF is
     * not already locked or if it was locked with the same request-id, otherwise
     * the action returns a response with a reject status code. When a VNF is
     * locked, any subsequent sequential commands with same request-id will be
     * accepted. Commands associated with other request-ids will be rejected. The
     * Lock command returns only one final response with the status of the request
     * processing. The APPC also locks the target VNF during any command processing.
     * If a lock action is then requested on that VNF, it will be rejected because
     * the VNF was already locked, even though no actual lock command was explicitly
     * invoked
     *
     * @param lockInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "lock", outputType = LockOutput.class)
    void lock(LockInput lockInput, ResponseHandler<LockOutput> listener) throws AppcClientException;

    /**
     * Migrates a running target VFC from its current AIC host to another. A
     * destination AIC node will be selected by relying on AIC internal rules to
     * migrate. A successful Migrate action returns a success response and the new
     * AIC node identity in the response payload block. A failed Migrate action
     * returns a failure and the failure messages in the response payload block
     *
     * @param migrateInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "migrate", outputType = MigrateOutput.class)
    MigrateOutput migrate(MigrateInput migrateInput) throws AppcClientException;

    /**
     * Migrates a running target VFC from its current AIC host to another. A
     * destination AIC node will be selected by relying on AIC internal rules to
     * migrate. A successful Migrate action returns a success response and the new
     * AIC node identity in the response payload block. A failed Migrate action
     * returns a failure and the failure messages in the response payload block
     *
     * @param migrateInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "migrate", outputType = MigrateOutput.class)
    void migrate(MigrateInput migrateInput, ResponseHandler<MigrateOutput> listener) throws AppcClientException;

    /**
     * Recreates a target VFC instance to a known, stable state. A successful
     * rebuild returns a success response and the rebuild details in the response
     * payload block. A failed rebuild returns a failure and the failure messages in
     * the response payload block
     *
     * @param rebuildInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "rebuild", outputType = RebuildOutput.class)
    RebuildOutput rebuild(RebuildInput rebuildInput) throws AppcClientException;

    /**
     * Recreates a target VFC instance to a known, stable state. A successful
     * rebuild returns a success response and the rebuild details in the response
     * payload block. A failed rebuild returns a failure and the failure messages in
     * the response payload block
     *
     * @param rebuildInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "rebuild", outputType = RebuildOutput.class)
    void rebuild(RebuildInput rebuildInput, ResponseHandler<RebuildOutput> listener) throws AppcClientException;

    /**
     * An operation to restart a virtual network function (or VM)
     *
     * @param restartInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "restart", outputType = RestartOutput.class)
    RestartOutput restart(RestartInput restartInput) throws AppcClientException;

    /**
     * An operation to restart a virtual network function (or VM)
     *
     * @param restartInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "restart", outputType = RestartOutput.class)
    void restart(RestartInput restartInput, ResponseHandler<RestartOutput> listener) throws AppcClientException;

    /**
     * Sets a VNF to the previous version of the configuration without explicitly
     * invoking the configuration set name. This command is used when the
     * configuration was successful, but the health-check was not. A successful
     * rollback returns a success status when the restart process has completed. A
     * failed or a partially failed (for a complex VNF) rollback returns a failure
     * and the failure messages in the response payload block. This command can be
     * applied to any VNF type. The only restriction is that the particular VNF
     * should be built based on the generic heap stack
     *
     * @param rollbackInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "rollback", outputType = RollbackOutput.class)
    RollbackOutput rollback(RollbackInput rollbackInput) throws AppcClientException;

    /**
     * Sets a VNF to the previous version of the configuration without explicitly
     * invoking the configuration set name. This command is used when the
     * configuration was successful, but the health-check was not. A successful
     * rollback returns a success status when the restart process has completed. A
     * failed or a partially failed (for a complex VNF) rollback returns a failure
     * and the failure messages in the response payload block. This command can be
     * applied to any VNF type. The only restriction is that the particular VNF
     * should be built based on the generic heap stack
     *
     * @param rollbackInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "rollback", outputType = RollbackOutput.class)
    void rollback(RollbackInput rollbackInput, ResponseHandler<RollbackOutput> listener) throws AppcClientException;

    /**
     * Creates a snapshot of a VNF, or VM. The Snapshot command returns a customized
     * response containing a reference to the newly created snapshot instance if the
     * action is successful. This command can be applied to any VNF type. The only
     * restriction is that the particular VNF should be built based on the generic
     * heap stack
     *
     * @param snapshotInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "snapshot", outputType = SnapshotOutput.class)
    SnapshotOutput snapshot(SnapshotInput snapshotInput) throws AppcClientException;

    /**
     * Creates a snapshot of a VNF, or VM. The Snapshot command returns a customized
     * response containing a reference to the newly created snapshot instance if the
     * action is successful. This command can be applied to any VNF type. The only
     * restriction is that the particular VNF should be built based on the generic
     * heap stack
     *
     * @param snapshotInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "snapshot", outputType = SnapshotOutput.class)
    void snapshot(SnapshotInput snapshotInput, ResponseHandler<SnapshotOutput> listener) throws AppcClientException;

    /**
     * This LCM command uploads the file that contains a new software version to the
     * target VNF
     *
     * @param softwareUploadInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "software-upload", outputType = SoftwareUploadOutput.class)
    SoftwareUploadOutput softwareUpload(SoftwareUploadInput softwareUploadInput) throws AppcClientException;

    /**
     * This LCM command uploads the file that contains a new software version to the
     * target VNF
     *
     * @param softwareUploadInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "software-upload", outputType = SoftwareUploadOutput.class)
    void softwareUpload(SoftwareUploadInput softwareUploadInput, ResponseHandler<SoftwareUploadOutput> listener)
            throws AppcClientException;

    /**
     * An operation to start a virtual network function (or VM)
     *
     * @param startInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "start", outputType = StartOutput.class)
    StartOutput start(StartInput startInput) throws AppcClientException;

    /**
     * An operation to start a virtual network function (or VM)
     *
     * @param startInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "start", outputType = StartOutput.class)
    void start(StartInput startInput, ResponseHandler<StartOutput> listener) throws AppcClientException;

    /**
     * Stop a target VNF or VNFC. A successful stop returns a success response. For
     * a multi-component stop to be considered successful, all component stop
     * actions must succeed. A failed stop returns a failure and the failure
     * messages in the response payload block
     *
     * @param stopInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "stop", outputType = StopOutput.class)
    StopOutput stop(StopInput stopInput) throws AppcClientException;

    /**
     * Stop a target VNF or VNFC. A successful stop returns a success response. For
     * a multi-component stop to be considered successful, all component stop
     * actions must succeed. A failed stop returns a failure and the failure
     * messages in the response payload block
     *
     * @param stopInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "stop", outputType = StopOutput.class)
    void stop(StopInput stopInput, ResponseHandler<StopOutput> listener) throws AppcClientException;

    /**
     * The Sync action updates the current configuration in the APPC store with the
     * running configuration from the device. A successful Sync returns a success
     * status. A failed Sync returns a failure response status and failure messages
     * in the response payload block. This command can be applied to any VNF type.
     * The only restriction is that a particular VNF should be able to support the
     * interface for Reading Configuration using existing adapters and use the
     * following protocols: CLI, RestConf and XML
     *
     * @param syncInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "sync", outputType = SyncOutput.class)
    SyncOutput sync(SyncInput syncInput) throws AppcClientException;

    /**
     * The Sync action updates the current configuration in the APPC store with the
     * running configuration from the device. A successful Sync returns a success
     * status. A failed Sync returns a failure response status and failure messages
     * in the response payload block. This command can be applied to any VNF type.
     * The only restriction is that a particular VNF should be able to support the
     * interface for Reading Configuration using existing adapters and use the
     * following protocols: CLI, RestConf and XML
     *
     * @param syncInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "sync", outputType = SyncOutput.class)
    void sync(SyncInput syncInput, ResponseHandler<SyncOutput> listener) throws AppcClientException;

    /**
     * Terminate a target VNF and release its resources (possibly gracefully).
     * Specific scripts can be run before termination by placing them under the
     * Terminate life cycle event. All configuration files related to the target VNF
     * are deleted. The resources of a terminated VNF that are not managed by APPC,
     * such as those handled by SDNC or other components, are not handled and remain
     * the responsibility of their respective managing functions. A successful
     * Terminate action returns a success response. For a multi-component terminate
     * to be considered successful, all component Terminate actions must also
     * succeed. A failed Terminate returns a failure status and the failure messages
     * in the response payload block
     *
     * @param terminateInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "terminate", outputType = TerminateOutput.class)
    TerminateOutput terminate(TerminateInput terminateInput) throws AppcClientException;

    /**
     * Terminate a target VNF and release its resources (possibly gracefully).
     * Specific scripts can be run before termination by placing them under the
     * Terminate life cycle event. All configuration files related to the target VNF
     * are deleted. The resources of a terminated VNF that are not managed by APPC,
     * such as those handled by SDNC or other components, are not handled and remain
     * the responsibility of their respective managing functions. A successful
     * Terminate action returns a success response. For a multi-component terminate
     * to be considered successful, all component Terminate actions must also
     * succeed. A failed Terminate returns a failure status and the failure messages
     * in the response payload block
     *
     * @param terminateInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "terminate", outputType = TerminateOutput.class)
    void terminate(TerminateInput terminateInput, ResponseHandler<TerminateOutput> listener) throws AppcClientException;

    /**
     * The Test LCM action checks a target VNF or VNFC for correct operation. The
     * functionality of the Test LCM action involves should involve more than a
     * HealthCheck , it should provide a means for launching a test transaction and
     * determining if the transaction completed successfully or not. A transaction
     * launcher microservice will have to be supplied by the VNF and called by APPC.
     * A successful test returns a success and the results of the test in the
     * payload block. A failed test returns a failure and specific failure messages
     * in the payload block
     *
     * @param testInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "test", outputType = TestOutput.class)
    TestOutput test(TestInput testInput) throws AppcClientException;

    /**
     * The Test LCM action checks a target VNF or VNFC for correct operation. The
     * functionality of the Test LCM action involves should involve more than a
     * HealthCheck , it should provide a means for launching a test transaction and
     * determining if the transaction completed successfully or not. A transaction
     * launcher microservice will have to be supplied by the VNF and called by APPC.
     * A successful test returns a success and the results of the test in the
     * payload block. A failed test returns a failure and specific failure messages
     * in the payload block
     *
     * @param testInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "test", outputType = TestOutput.class)
    void test(TestInput testInput, ResponseHandler<TestOutput> listener) throws AppcClientException;

    /**
     * Run the Unlock command to release the lock on a VNF and allow other clients
     * to perform LCM commands on that VNF. The Unlock command will result in
     * success if the VNF successfully unlocked or if it was already unlocked,
     * otherwise commands will be rejected. The Unlock command will only return
     * success if the VNF was locked with same request-id (on page 6). The Unlock
     * command returns only one final response with the status of the request
     * processing. The APPC also locks the target VNF during any command processing.
     * If an Unlock action is then requested on that VNF with a different
     * request-id, it will be rejected because the VNF is already locked for another
     * process, even though no actual lock command was explicitly invoked
     *
     * @param unlockInput
     *            - RPC input object
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "unlock", outputType = UnlockOutput.class)
    UnlockOutput unlock(UnlockInput unlockInput) throws AppcClientException;

    /**
     * Run the Unlock command to release the lock on a VNF and allow other clients
     * to perform LCM commands on that VNF. The Unlock command will result in
     * success if the VNF successfully unlocked or if it was already unlocked,
     * otherwise commands will be rejected. The Unlock command will only return
     * success if the VNF was locked with same request-id (on page 6). The Unlock
     * command returns only one final response with the status of the request
     * processing. The APPC also locks the target VNF during any command processing.
     * If an Unlock action is then requested on that VNF with a different
     * request-id, it will be rejected because the VNF is already locked for another
     * process, even though no actual lock command was explicitly invoked
     *
     * @param unlockInput
     *            - RPC input object
     * @param listener
     *            - callback implementation
     * @throws AppcClientException
     *             - throw AppcClientException
     */
    @RPC(name = "unlock", outputType = UnlockOutput.class)
    void unlock(UnlockInput unlockInput, ResponseHandler<UnlockOutput> listener) throws AppcClientException;

}
