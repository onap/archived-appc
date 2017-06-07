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

package org.openecomp.appc.lifecyclemanager.helper;


import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.lifecyclemanager.objects.VNFOperationOutcome;
import org.openecomp.appc.statemachine.objects.Event;
import org.openecomp.appc.statemachine.objects.State;
import org.openecomp.appc.statemachine.objects.StateMachineMetadata;


public class MetadataReader {

    private enum VNFStates {
        Not_Instantiated, Instantiated, Configuring, Configured, Testing, Tested, Rebuilding, Restarting, Starting, Error, Running, Unknown, Terminating, Stopping, Stopped,
        Backing_Up, Snapshotting, Software_Uploading, Upgrading, Rollbacking, Licensing, Migrating, Evacuating , NOT_ORCHESTRATED("NOT ORCHESTRATED");

        String stateName;

        VNFStates(String name){
            this.stateName = name;
        }

        VNFStates(){
            this.stateName = name();
        }

        public String toString(){
            return this.stateName;
        }
    }

    @SuppressWarnings("unused")
    public StateMachineMetadata readMetadata(String vnfType){
        State NOT_INSTANTIATED = new State(VNFStates.Not_Instantiated.toString());
        State INSTANTIATED = new State(VNFStates.Instantiated.toString());
        State CONFIGURING = new State(VNFStates.Configuring.toString());
        State CONFIGURED = new State(VNFStates.Configured.toString());
        State TESTING = new State(VNFStates.Testing.toString());
        State TESTED = new State(VNFStates.Tested.toString());
        State REBUILDING = new State(VNFStates.Rebuilding.toString());
        State RESTARTING = new State(VNFStates.Restarting.toString());
        State STARTING = new State(VNFStates.Starting.toString());
        State ERROR = new State(VNFStates.Error.toString());
        State RUNNING = new State(VNFStates.Running.toString());
        State UNKNOWN = new State(VNFStates.Unknown.toString());
        State TERMINATING = new State(VNFStates.Terminating.toString());
        State STOPPING = new State(VNFStates.Stopping.toString());
        State STOPPED = new State(VNFStates.Stopped.toString());
        State NOT_ORCHESTRATED = new State(VNFStates.NOT_ORCHESTRATED.toString());

        State BACKING_UP = new State(VNFStates.Backing_Up.toString());
        State SNAPSHOTTING = new State(VNFStates.Snapshotting.toString());
        State SOFTWARE_UPLOADING = new State(VNFStates.Software_Uploading.toString());
        State UPGRADING = new State(VNFStates.Upgrading.toString());
        State ROLLBACKING = new State(VNFStates.Rollbacking.toString());

        State MIGRATING = new State(VNFStates.Migrating.toString());
        State EVACUATING = new State(VNFStates.Evacuating.toString());

        Event CONFIGURE = new Event(VNFOperation.Configure.toString());
        Event HEALTHCHECK = new Event(VNFOperation.HealthCheck.toString());
        Event TEST = new Event(VNFOperation.Test.toString());
        Event START = new Event(VNFOperation.Start.toString());
        Event TERMINATE = new Event(VNFOperation.Terminate.toString());
        Event RESTART = new Event(VNFOperation.Restart.toString());
        Event REBUILD = new Event(VNFOperation.Rebuild.toString());
        Event STOP = new Event(VNFOperation.Stop.toString());
        Event CONFIG_MODIFY = new Event(VNFOperation.ConfigModify.toString());
        Event CONFIG_SCALEOUT = new Event(VNFOperation.ConfigScaleOut.toString());
        Event CONFIG_RESTORE = new Event(VNFOperation.ConfigRestore.toString());
        Event BACKUP = new Event(VNFOperation.Backup.toString());
        Event SNAPSHOT = new Event(VNFOperation.Snapshot.toString());
        Event SOFTWARE_UPLOAD = new Event(VNFOperation.SoftwareUpload.toString());
        Event LIVE_UPGRADE = new Event(VNFOperation.LiveUpgrade.toString());
        Event ROLLBACK = new Event(VNFOperation.Rollback.toString());
        Event SYNC = new Event(VNFOperation.Sync.toString());
        Event AUDIT = new Event(VNFOperation.Audit.toString());
        Event MIGRATE = new Event(VNFOperation.Migrate.toString());
        Event EVACUATE = new Event(VNFOperation.Evacuate.toString());
        Event CONFIG_BACKUP = new Event(VNFOperation.ConfigBackup.toString());
        Event CONFIG_BACKUP_DELETE = new Event(VNFOperation.ConfigBackupDelete.toString());
        Event CONFIG_EXPORT = new Event(VNFOperation.ConfigExport.toString());

        Event LOCK = new Event(VNFOperation.Lock.toString());
        Event UNLOCK = new Event(VNFOperation.Unlock.toString());
        Event CHECKLOCK = new Event(VNFOperation.CheckLock.toString());

        Event SUCCESS = new Event(VNFOperationOutcome.SUCCESS.toString());
        Event FAILURE = new Event(VNFOperationOutcome.FAILURE.toString());


        StateMachineMetadata.StateMachineMetadataBuilder builder = new StateMachineMetadata.StateMachineMetadataBuilder();

        builder = builder.addState(NOT_INSTANTIATED);
        builder = builder.addState(INSTANTIATED);
        builder = builder.addState(CONFIGURING);
        builder = builder.addState(CONFIGURED);
        builder = builder.addState(TESTING);
        builder = builder.addState(TESTED);
        builder = builder.addState(REBUILDING);
        builder = builder.addState(RESTARTING);
        builder = builder.addState(STARTING);
        builder = builder.addState(ERROR);
        builder = builder.addState(RUNNING);
        builder = builder.addState(UNKNOWN);
        builder = builder.addState(TERMINATING);
        builder = builder.addState(STOPPING);
        builder = builder.addState(STOPPED);
        builder = builder.addState(BACKING_UP);
        builder = builder.addState(SNAPSHOTTING);
        builder = builder.addState(SOFTWARE_UPLOADING);
        builder = builder.addState(UPGRADING);
        builder = builder.addState(ROLLBACKING);
        builder = builder.addState(MIGRATING);
        builder = builder.addState(EVACUATING);
        builder = builder.addState(NOT_ORCHESTRATED);

        builder = builder.addEvent(CONFIGURE);
        builder = builder.addEvent(TEST);
        builder = builder.addEvent(START);
        builder = builder.addEvent(TERMINATE);
        builder = builder.addEvent(RESTART);
        builder = builder.addEvent(REBUILD);
        builder = builder.addEvent(SUCCESS);
        builder = builder.addEvent(FAILURE);
        builder = builder.addEvent(STOP);
        builder = builder.addEvent(CONFIG_MODIFY);
        builder = builder.addEvent(CONFIG_SCALEOUT);
        builder = builder.addEvent(CONFIG_RESTORE);
        builder = builder.addEvent(HEALTHCHECK);
        builder = builder.addEvent(BACKUP);
        builder = builder.addEvent(SNAPSHOT);
        builder = builder.addEvent(SOFTWARE_UPLOAD);
        builder = builder.addEvent(LIVE_UPGRADE);
        builder = builder.addEvent(ROLLBACK);
        builder = builder.addEvent(SYNC);
        builder = builder.addEvent(AUDIT);
        builder = builder.addEvent(MIGRATE);
        builder = builder.addEvent(EVACUATE);
        builder = builder.addEvent(LOCK);
        builder = builder.addEvent(UNLOCK);
        builder = builder.addEvent(CHECKLOCK);
        builder = builder.addEvent(CONFIG_BACKUP);
        builder = builder.addEvent(CONFIG_BACKUP_DELETE);
        builder = builder.addEvent(CONFIG_EXPORT);

        builder = builder.addTransition(NOT_ORCHESTRATED,CONFIGURE,CONFIGURING);

        builder = builder.addTransition(INSTANTIATED,CONFIGURE,CONFIGURING);
        builder = builder.addTransition(INSTANTIATED,TEST,TESTING);
        builder = builder.addTransition(INSTANTIATED,START,STARTING);
        builder = builder.addTransition(INSTANTIATED,TERMINATE,TERMINATING);
        builder = builder.addTransition(INSTANTIATED,RESTART,RESTARTING);
        builder = builder.addTransition(INSTANTIATED,REBUILD,REBUILDING);
        builder = builder.addTransition(INSTANTIATED,STOP,STOPPING);
        builder = builder.addTransition(INSTANTIATED,CONFIG_MODIFY,CONFIGURING);
        builder = builder.addTransition(INSTANTIATED,CONFIG_SCALEOUT,CONFIGURING);
        builder = builder.addTransition(INSTANTIATED,CONFIG_RESTORE,CONFIGURING);
        builder = builder.addTransition(INSTANTIATED,HEALTHCHECK,TESTING);
        builder = builder.addTransition(INSTANTIATED,BACKUP,BACKING_UP);
        builder = builder.addTransition(INSTANTIATED,SNAPSHOT,SNAPSHOTTING);
        builder = builder.addTransition(INSTANTIATED,SOFTWARE_UPLOAD,SOFTWARE_UPLOADING);
        builder = builder.addTransition(INSTANTIATED,LIVE_UPGRADE,UPGRADING);
        builder = builder.addTransition(INSTANTIATED,ROLLBACK,ROLLBACKING);
        builder = builder.addTransition(INSTANTIATED,MIGRATE,MIGRATING);
        builder = builder.addTransition(INSTANTIATED,EVACUATE,EVACUATING);
        builder = builder.addTransition(INSTANTIATED,LOCK,INSTANTIATED);
        builder = builder.addTransition(INSTANTIATED,UNLOCK,INSTANTIATED);
        builder = builder.addTransition(INSTANTIATED,CHECKLOCK,INSTANTIATED);

        builder = builder.addTransition(CONFIGURED,CONFIGURE,CONFIGURING);
        builder = builder.addTransition(CONFIGURED,TEST,TESTING);
        builder = builder.addTransition(CONFIGURED,START,STARTING);
        builder = builder.addTransition(CONFIGURED,TERMINATE,TERMINATING);
        builder = builder.addTransition(CONFIGURED,RESTART,RESTARTING);
        builder = builder.addTransition(CONFIGURED,REBUILD,REBUILDING);
        builder = builder.addTransition(CONFIGURED,STOP,STOPPING);
        builder = builder.addTransition(CONFIGURED,CONFIG_MODIFY,CONFIGURING);
        builder = builder.addTransition(CONFIGURED,CONFIG_SCALEOUT,CONFIGURING);
        builder = builder.addTransition(CONFIGURED,CONFIG_RESTORE,CONFIGURING);
        builder = builder.addTransition(CONFIGURED,HEALTHCHECK,TESTING);
        builder = builder.addTransition(CONFIGURED,BACKUP,BACKING_UP);
        builder = builder.addTransition(CONFIGURED,SNAPSHOT,SNAPSHOTTING);
        builder = builder.addTransition(CONFIGURED,SOFTWARE_UPLOAD,SOFTWARE_UPLOADING);
        builder = builder.addTransition(CONFIGURED,LIVE_UPGRADE,UPGRADING);
        builder = builder.addTransition(CONFIGURED,ROLLBACK,ROLLBACKING);
        builder = builder.addTransition(CONFIGURED,SYNC,CONFIGURED);
        builder = builder.addTransition(CONFIGURED,AUDIT,CONFIGURED);
        builder = builder.addTransition(CONFIGURED,MIGRATE,MIGRATING);
        builder = builder.addTransition(CONFIGURED,EVACUATE,EVACUATING);
        builder = builder.addTransition(CONFIGURED,LOCK,CONFIGURED);
        builder = builder.addTransition(CONFIGURED,UNLOCK,CONFIGURED);
        builder = builder.addTransition(CONFIGURED,CHECKLOCK,CONFIGURED);
        builder = builder.addTransition(CONFIGURED,CONFIG_BACKUP,CONFIGURED);
        builder = builder.addTransition(CONFIGURED,CONFIG_BACKUP_DELETE,CONFIGURED);
        builder = builder.addTransition(CONFIGURED,CONFIG_EXPORT,CONFIGURED);

        builder = builder.addTransition(TESTED,CONFIGURE,CONFIGURING);
        builder = builder.addTransition(TESTED,TEST,TESTING);
        builder = builder.addTransition(TESTED,START,STARTING);
        builder = builder.addTransition(TESTED,TERMINATE,TERMINATING);
        builder = builder.addTransition(TESTED,RESTART,RESTARTING);
        builder = builder.addTransition(TESTED,REBUILD,REBUILDING);
        builder = builder.addTransition(TESTED,STOP,STOPPING);
        builder = builder.addTransition(TESTED,CONFIG_MODIFY,CONFIGURING);
        builder = builder.addTransition(TESTED,CONFIG_SCALEOUT,CONFIGURING);
        builder = builder.addTransition(TESTED,CONFIG_RESTORE,CONFIGURING);
        builder = builder.addTransition(TESTED,HEALTHCHECK,TESTING);
        builder = builder.addTransition(TESTED,BACKUP,BACKING_UP);
        builder = builder.addTransition(TESTED,SNAPSHOT,SNAPSHOTTING);
        builder = builder.addTransition(TESTED,SOFTWARE_UPLOAD,SOFTWARE_UPLOADING);
        builder = builder.addTransition(TESTED,LIVE_UPGRADE,UPGRADING);
        builder = builder.addTransition(TESTED,ROLLBACK,ROLLBACKING);
        builder = builder.addTransition(TESTED,SYNC,TESTED);
        builder = builder.addTransition(TESTED,AUDIT,TESTED);
        builder = builder.addTransition(TESTED,MIGRATE,MIGRATING);
        builder = builder.addTransition(TESTED,EVACUATE,EVACUATING);
        builder = builder.addTransition(TESTED,LOCK,TESTED);
        builder = builder.addTransition(TESTED,UNLOCK,TESTED);
        builder = builder.addTransition(TESTED,CHECKLOCK,TESTED);
        builder = builder.addTransition(TESTED,CONFIG_BACKUP,TESTED);
        builder = builder.addTransition(TESTED,CONFIG_BACKUP_DELETE,TESTED);
        builder = builder.addTransition(TESTED,CONFIG_EXPORT,TESTED);

        builder = builder.addTransition(RUNNING,CONFIGURE,CONFIGURING);
        builder = builder.addTransition(RUNNING,TEST,TESTING);
        builder = builder.addTransition(RUNNING,START,STARTING);
        builder = builder.addTransition(RUNNING,TERMINATE,TERMINATING);
        builder = builder.addTransition(RUNNING,RESTART,RESTARTING);
        builder = builder.addTransition(RUNNING,REBUILD,REBUILDING);
        builder = builder.addTransition(RUNNING,STOP,STOPPING);
        builder = builder.addTransition(RUNNING,CONFIG_MODIFY,CONFIGURING);
        builder = builder.addTransition(RUNNING,CONFIG_SCALEOUT,CONFIGURING);
        builder = builder.addTransition(RUNNING,CONFIG_RESTORE,CONFIGURING);
        builder = builder.addTransition(RUNNING,HEALTHCHECK,TESTING);
        builder = builder.addTransition(RUNNING,BACKUP,BACKING_UP);
        builder = builder.addTransition(RUNNING,SNAPSHOT,SNAPSHOTTING);
        builder = builder.addTransition(RUNNING,SOFTWARE_UPLOAD,SOFTWARE_UPLOADING);
        builder = builder.addTransition(RUNNING,LIVE_UPGRADE,UPGRADING);
        builder = builder.addTransition(RUNNING,ROLLBACK,ROLLBACKING);
        builder = builder.addTransition(RUNNING,SYNC,RUNNING);
        builder = builder.addTransition(RUNNING,AUDIT,RUNNING);
        builder = builder.addTransition(RUNNING,MIGRATE,MIGRATING);
        builder = builder.addTransition(RUNNING,EVACUATE,EVACUATING);
        builder = builder.addTransition(RUNNING,LOCK,RUNNING);
        builder = builder.addTransition(RUNNING,UNLOCK,RUNNING);
        builder = builder.addTransition(RUNNING,CHECKLOCK,RUNNING);
        builder = builder.addTransition(RUNNING,CONFIG_BACKUP,RUNNING);
        builder = builder.addTransition(RUNNING,CONFIG_BACKUP_DELETE,RUNNING);
        builder = builder.addTransition(RUNNING,CONFIG_EXPORT,RUNNING);

        builder = builder.addTransition(ERROR,CONFIGURE,CONFIGURING);
        builder = builder.addTransition(ERROR,TEST,TESTING);
        builder = builder.addTransition(ERROR,START,STARTING);
        builder = builder.addTransition(ERROR,TERMINATE,TERMINATING);
        builder = builder.addTransition(ERROR,RESTART,RESTARTING);
        builder = builder.addTransition(ERROR,REBUILD,REBUILDING);
        builder = builder.addTransition(ERROR,STOP,STOPPING);
        builder = builder.addTransition(ERROR,CONFIG_MODIFY,CONFIGURING);
        builder = builder.addTransition(ERROR,CONFIG_SCALEOUT,CONFIGURING);
        builder = builder.addTransition(ERROR,CONFIG_RESTORE,CONFIGURING);
        builder = builder.addTransition(ERROR,HEALTHCHECK,TESTING);
        builder = builder.addTransition(ERROR,BACKUP,BACKING_UP);
        builder = builder.addTransition(ERROR,SNAPSHOT,SNAPSHOTTING);
        builder = builder.addTransition(ERROR,SOFTWARE_UPLOAD,SOFTWARE_UPLOADING);
        builder = builder.addTransition(ERROR,LIVE_UPGRADE,UPGRADING);
        builder = builder.addTransition(ERROR,ROLLBACK,ROLLBACKING);
        builder = builder.addTransition(ERROR,SYNC,ERROR);
        builder = builder.addTransition(ERROR,AUDIT,ERROR);
        builder = builder.addTransition(ERROR,MIGRATE,MIGRATING);
        builder = builder.addTransition(ERROR,EVACUATE,EVACUATING);
        builder = builder.addTransition(ERROR,LOCK,ERROR);
        builder = builder.addTransition(ERROR,UNLOCK,ERROR);
        builder = builder.addTransition(ERROR,CHECKLOCK,ERROR);
        builder = builder.addTransition(ERROR,CONFIG_BACKUP,ERROR);
        builder = builder.addTransition(ERROR,CONFIG_BACKUP_DELETE,ERROR);
        builder = builder.addTransition(ERROR,CONFIG_EXPORT,ERROR);

        builder = builder.addTransition(UNKNOWN,CONFIGURE,CONFIGURING);
        builder = builder.addTransition(UNKNOWN,TEST,TESTING);
        builder = builder.addTransition(UNKNOWN,START,STARTING);
        builder = builder.addTransition(UNKNOWN,TERMINATE,TERMINATING);
        builder = builder.addTransition(UNKNOWN,RESTART,RESTARTING);
        builder = builder.addTransition(UNKNOWN,REBUILD,REBUILDING);
        builder = builder.addTransition(UNKNOWN,STOP,STOPPING);
        builder = builder.addTransition(UNKNOWN,CONFIG_MODIFY,CONFIGURING);
        builder = builder.addTransition(UNKNOWN,CONFIG_SCALEOUT,CONFIGURING);
        builder = builder.addTransition(UNKNOWN,CONFIG_RESTORE,CONFIGURING);
        builder = builder.addTransition(UNKNOWN,HEALTHCHECK,TESTING);
        builder = builder.addTransition(UNKNOWN,BACKUP,BACKING_UP);
        builder = builder.addTransition(UNKNOWN,SNAPSHOT,SNAPSHOTTING);
        builder = builder.addTransition(UNKNOWN,SOFTWARE_UPLOAD,SOFTWARE_UPLOADING);
        builder = builder.addTransition(UNKNOWN,LIVE_UPGRADE,UPGRADING);
        builder = builder.addTransition(UNKNOWN,ROLLBACK,ROLLBACKING);
        builder = builder.addTransition(UNKNOWN,SYNC,UNKNOWN);
        builder = builder.addTransition(UNKNOWN,AUDIT,UNKNOWN);
        builder = builder.addTransition(UNKNOWN,MIGRATE,MIGRATING);
        builder = builder.addTransition(UNKNOWN,EVACUATE,EVACUATING);
        builder = builder.addTransition(UNKNOWN,LOCK,UNKNOWN);
        builder = builder.addTransition(UNKNOWN,UNLOCK,UNKNOWN);
        builder = builder.addTransition(UNKNOWN,CHECKLOCK,UNKNOWN);
        builder = builder.addTransition(UNKNOWN,CONFIG_BACKUP,UNKNOWN);
        builder = builder.addTransition(UNKNOWN,CONFIG_BACKUP_DELETE,UNKNOWN);
        builder = builder.addTransition(UNKNOWN,CONFIG_EXPORT,UNKNOWN);

        builder = builder.addTransition(STOPPED,CONFIGURE,CONFIGURING);
        builder = builder.addTransition(STOPPED,TEST,TESTING);
        builder = builder.addTransition(STOPPED,START,STARTING);
        builder = builder.addTransition(STOPPED,TERMINATE,TERMINATING);
        builder = builder.addTransition(STOPPED,RESTART,RESTARTING);
        builder = builder.addTransition(STOPPED,REBUILD,REBUILDING);
        builder = builder.addTransition(STOPPED,CONFIG_MODIFY,CONFIGURING);
        builder = builder.addTransition(STOPPED,CONFIG_SCALEOUT,CONFIGURING);
        builder = builder.addTransition(STOPPED,CONFIG_RESTORE,CONFIGURING);
        builder = builder.addTransition(STOPPED,HEALTHCHECK,TESTING);
        builder = builder.addTransition(STOPPED,BACKUP,BACKING_UP);
        builder = builder.addTransition(STOPPED,SNAPSHOT,SNAPSHOTTING);
        builder = builder.addTransition(STOPPED,SOFTWARE_UPLOAD,SOFTWARE_UPLOADING);
        builder = builder.addTransition(STOPPED,LIVE_UPGRADE,UPGRADING);
        builder = builder.addTransition(STOPPED,ROLLBACK,ROLLBACKING);
        builder = builder.addTransition(STOPPED,MIGRATE,MIGRATING);
        builder = builder.addTransition(STOPPED,EVACUATE,EVACUATING);
        builder = builder.addTransition(STOPPED,LOCK,STOPPED);
        builder = builder.addTransition(STOPPED,UNLOCK,STOPPED);
        builder = builder.addTransition(STOPPED,CHECKLOCK,STOPPED);

        builder = builder.addTransition(CONFIGURING,SUCCESS,CONFIGURED);
        builder = builder.addTransition(CONFIGURING,FAILURE,ERROR);

        builder = builder.addTransition(TESTING,SUCCESS,TESTED);
        builder = builder.addTransition(TESTING,FAILURE,ERROR);

        builder = builder.addTransition(RESTARTING,SUCCESS,RUNNING);
        builder = builder.addTransition(RESTARTING,FAILURE,ERROR);

        builder = builder.addTransition(STARTING,SUCCESS,RUNNING);
        builder = builder.addTransition(STARTING,FAILURE,ERROR);

        builder = builder.addTransition(TERMINATING,SUCCESS,NOT_INSTANTIATED);
        builder = builder.addTransition(TERMINATING,FAILURE,ERROR);

        builder = builder.addTransition(REBUILDING,SUCCESS,RUNNING);
        builder = builder.addTransition(REBUILDING,FAILURE,ERROR);

        builder = builder.addTransition(STOPPING,SUCCESS,STOPPED);
        builder = builder.addTransition(STOPPING,FAILURE,ERROR);

        builder = builder.addTransition(BACKING_UP,SUCCESS,RUNNING);
        builder = builder.addTransition(BACKING_UP,FAILURE,ERROR);

        builder = builder.addTransition(SNAPSHOTTING,SUCCESS,RUNNING);
        builder = builder.addTransition(SNAPSHOTTING,FAILURE,ERROR);

        builder = builder.addTransition(SOFTWARE_UPLOADING,SUCCESS,RUNNING);
        builder = builder.addTransition(SOFTWARE_UPLOADING,FAILURE,ERROR);

        builder = builder.addTransition(UPGRADING,SUCCESS,RUNNING);
        builder = builder.addTransition(UPGRADING,FAILURE,ERROR);

        builder = builder.addTransition(ROLLBACKING,SUCCESS,RUNNING);
        builder = builder.addTransition(ROLLBACKING,FAILURE,ERROR);

        builder = builder.addTransition(MIGRATING,SUCCESS,RUNNING);
        builder = builder.addTransition(MIGRATING,FAILURE,ERROR);

        builder = builder.addTransition(EVACUATING,SUCCESS,RUNNING);
        builder = builder.addTransition(EVACUATING,FAILURE,ERROR);

        return builder.build();

    }

}
