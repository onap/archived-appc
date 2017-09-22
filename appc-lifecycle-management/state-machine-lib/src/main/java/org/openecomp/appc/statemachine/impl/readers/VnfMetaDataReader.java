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

package org.openecomp.appc.statemachine.impl.readers;

import org.openecomp.appc.statemachine.StateMetaDataReader;
import org.openecomp.appc.lifecyclemanager.objects.VNFOperationOutcome;
import org.openecomp.appc.statemachine.objects.Event;
import org.openecomp.appc.statemachine.objects.State;
import org.openecomp.appc.statemachine.objects.StateMachineMetadata;

/**
 * Reader for VNF MetaData
 */
public class VnfMetaDataReader implements StateMetaDataReader {

    /**
     * VNF Operations
     */
    public enum VNFOperation {

        Configure, Test, HealthCheck, Start, Terminate, Restart, Rebuild, Stop, ConfigModify,
        ConfigScaleOut,ConfigRestore,Backup, Snapshot,
        SoftwareUpload, LiveUpgrade, Rollback, Sync, Audit, Test_lic, Migrate, Evacuate,
        ConfigBackup, ConfigBackupDelete, ConfigExport,
        Lock(true), Unlock(true), CheckLock(true), StartApplication,StopApplication;

        private boolean builtIn;

        VNFOperation(boolean builtIn) {
            this.builtIn = builtIn;
        }

        VNFOperation() {
            this(false);
        }
    }

    /**
     * VNF States
     */
    enum VNFStates {
        Not_Instantiated, Instantiated, Configuring, Configured, Testing, Tested,
        Rebuilding, Restarting, Starting,
        Error, Running, Unknown, Terminating, Stopping, Stopped,
        Backing_Up, Snapshotting, Software_Uploading, Upgrading,
        Rollbacking, Licensing, Migrating, Evacuating,
        NOT_ORCHESTRATED("NOT ORCHESTRATED"), Created;

        String stateName;

        VNFStates(String name) {
            this.stateName = name;
        }

        VNFStates() {
            this.stateName = name();
        }

        @Override
        public String toString() {
            return this.stateName;
        }
    }

    @Override
    public StateMachineMetadata readMetadata() {
        State notInstantiated = new State(VNFStates.Not_Instantiated.toString());
        State instantiated = new State(VNFStates.Instantiated.toString());
        State configuring = new State(VNFStates.Configuring.toString());
        State configured = new State(VNFStates.Configured.toString());
        State testing = new State(VNFStates.Testing.toString());
        State tested = new State(VNFStates.Tested.toString());
        State rebuilding = new State(VNFStates.Rebuilding.toString());
        State restarting = new State(VNFStates.Restarting.toString());
        State starting = new State(VNFStates.Starting.toString());
        State error = new State(VNFStates.Error.toString());
        State running = new State(VNFStates.Running.toString());
        State unknown = new State(VNFStates.Unknown.toString());
        State terminating = new State(VNFStates.Terminating.toString());
        State stopping = new State(VNFStates.Stopping.toString());
        State stopped = new State(VNFStates.Stopped.toString());
        State notOrchestrated = new State(VNFStates.NOT_ORCHESTRATED.toString());

        State backingUp = new State(VNFStates.Backing_Up.toString());
        State snapshotting = new State(VNFStates.Snapshotting.toString());
        State softwareUploading = new State(VNFStates.Software_Uploading.toString());
        State upgrading = new State(VNFStates.Upgrading.toString());
        State rollbacking = new State(VNFStates.Rollbacking.toString());

        State migrating = new State(VNFStates.Migrating.toString());
        State evacuating = new State(VNFStates.Evacuating.toString());
        State created= new State(VNFStates.Created.toString());

        Event startApplication = new Event(VNFOperation.StartApplication.toString());
        Event configure = new Event(VNFOperation.Configure.toString());
        Event healthcheck = new Event(VNFOperation.HealthCheck.toString());
        Event test = new Event(VNFOperation.Test.toString());
        Event start = new Event(VNFOperation.Start.toString());
        Event terminate = new Event(VNFOperation.Terminate.toString());
        Event restart = new Event(VNFOperation.Restart.toString());
        Event rebuild = new Event(VNFOperation.Rebuild.toString());
        Event stop = new Event(VNFOperation.Stop.toString());
        Event configModify = new Event(VNFOperation.ConfigModify.toString());
        Event configScaleout = new Event(VNFOperation.ConfigScaleOut.toString());
        Event configRestore = new Event(VNFOperation.ConfigRestore.toString());
        Event backup = new Event(VNFOperation.Backup.toString());
        Event snapshot = new Event(VNFOperation.Snapshot.toString());
        Event softwareUpload = new Event(VNFOperation.SoftwareUpload.toString());
        Event liveUpgrade = new Event(VNFOperation.LiveUpgrade.toString());
        Event rollback = new Event(VNFOperation.Rollback.toString());
        Event sync = new Event(VNFOperation.Sync.toString());
        Event audit = new Event(VNFOperation.Audit.toString());
        Event migrate = new Event(VNFOperation.Migrate.toString());
        Event evacuate = new Event(VNFOperation.Evacuate.toString());
        Event configBackup = new Event(VNFOperation.ConfigBackup.toString());
        Event configBackupDelete = new Event(VNFOperation.ConfigBackupDelete.toString());
        Event configExport = new Event(VNFOperation.ConfigExport.toString());
        Event stopApplication= new Event(VNFOperation.StopApplication.toString());

        Event lock = new Event(VNFOperation.Lock.toString());
        Event unlock = new Event(VNFOperation.Unlock.toString());
        Event checklock = new Event(VNFOperation.CheckLock.toString());

        Event success = new Event(VNFOperationOutcome.SUCCESS.toString());
        Event failure = new Event(VNFOperationOutcome.FAILURE.toString());


        StateMachineMetadata.StateMachineMetadataBuilder builder =
                new StateMachineMetadata.StateMachineMetadataBuilder();

        builder = builder.addState(notInstantiated);
        builder = builder.addState(instantiated);
        builder = builder.addState(configuring);
        builder = builder.addState(configured);
        builder = builder.addState(testing);
        builder = builder.addState(tested);
        builder = builder.addState(rebuilding);
        builder = builder.addState(restarting);
        builder = builder.addState(starting);
        builder = builder.addState(error);
        builder = builder.addState(running);
        builder = builder.addState(unknown);
        builder = builder.addState(terminating);
        builder = builder.addState(stopping);
        builder = builder.addState(stopped);

        builder = builder.addState(backingUp);
        builder = builder.addState(snapshotting);
        builder = builder.addState(softwareUploading);
        builder = builder.addState(upgrading);
        builder = builder.addState(rollbacking);
        builder = builder.addState(migrating);
        builder = builder.addState(evacuating);
        builder = builder.addState(notOrchestrated);
        builder = builder.addState(created);
        builder = builder.addEvent(startApplication);
        builder = builder.addEvent(configure);
        builder = builder.addEvent(test);
        builder = builder.addEvent(start);
        builder = builder.addEvent(terminate);
        builder = builder.addEvent(restart);
        builder = builder.addEvent(rebuild);
        builder = builder.addEvent(success);
        builder = builder.addEvent(failure);
        builder = builder.addEvent(stop);
        builder = builder.addEvent(configModify);
        builder = builder.addEvent(configScaleout);
        builder = builder.addEvent(configRestore);
        builder = builder.addEvent(healthcheck);
        builder = builder.addEvent(backup);
        builder = builder.addEvent(snapshot);
        builder = builder.addEvent(softwareUpload);
        builder = builder.addEvent(liveUpgrade);
        builder = builder.addEvent(rollback);
        builder = builder.addEvent(sync);
        builder = builder.addEvent(audit);
        builder = builder.addEvent(migrate);
        builder = builder.addEvent(evacuate);
        builder = builder.addEvent(lock);
        builder = builder.addEvent(unlock);
        builder = builder.addEvent(checklock);
        builder = builder.addEvent(configBackup);
        builder = builder.addEvent(configBackupDelete);
        builder = builder.addEvent(configExport);
        builder = builder.addEvent(stopApplication);

        builder = builder.addTransition(notOrchestrated,configure,configuring);
        builder = builder.addTransition(notOrchestrated,test,testing);
        builder = builder.addTransition(notOrchestrated,start,starting);
        builder = builder.addTransition(notOrchestrated,terminate,terminating);
        builder = builder.addTransition(notOrchestrated,restart,restarting);
        builder = builder.addTransition(notOrchestrated,rebuild,rebuilding);
        builder = builder.addTransition(notOrchestrated,stop,stopping);
        builder = builder.addTransition(notOrchestrated,configModify,configuring);
        builder = builder.addTransition(notOrchestrated,configScaleout,configuring);
        builder = builder.addTransition(notOrchestrated,configRestore,configuring);
        builder = builder.addTransition(notOrchestrated,healthcheck,testing);
        builder = builder.addTransition(notOrchestrated,backup,backingUp);
        builder = builder.addTransition(notOrchestrated,snapshot,snapshotting);
        builder = builder.addTransition(notOrchestrated,softwareUpload,softwareUploading);
        builder = builder.addTransition(notOrchestrated,liveUpgrade,upgrading);
        builder = builder.addTransition(notOrchestrated,rollback,rollbacking);
        builder = builder.addTransition(notOrchestrated,migrate,migrating);
        builder = builder.addTransition(notOrchestrated,evacuate,evacuating);
        builder = builder.addTransition(notOrchestrated,lock,notOrchestrated);
        builder = builder.addTransition(notOrchestrated,unlock,notOrchestrated);
        builder = builder.addTransition(notOrchestrated,checklock,notOrchestrated);
        builder = builder.addTransition(notOrchestrated,startApplication,starting);
        builder = builder.addTransition(notOrchestrated,stopApplication,stopping);
        builder = builder.addTransition(notOrchestrated,configBackup,notOrchestrated);

        builder = builder.addTransition(created,configure,configuring);
        builder = builder.addTransition(created,test,testing);
        builder = builder.addTransition(created,start,starting);
        builder = builder.addTransition(created,terminate,terminating);
        builder = builder.addTransition(created,restart,restarting);
        builder = builder.addTransition(created,rebuild,rebuilding);
        builder = builder.addTransition(created,stop,stopping);
        builder = builder.addTransition(created,configModify,configuring);
        builder = builder.addTransition(created,configScaleout,configuring);
        builder = builder.addTransition(created,configRestore,configuring);
        builder = builder.addTransition(created,healthcheck,testing);
        builder = builder.addTransition(created,backup,backingUp);
        builder = builder.addTransition(created,snapshot,snapshotting);
        builder = builder.addTransition(created,softwareUpload,softwareUploading);
        builder = builder.addTransition(created,liveUpgrade,upgrading);
        builder = builder.addTransition(created,rollback,rollbacking);
        builder = builder.addTransition(created,migrate,migrating);
        builder = builder.addTransition(created,evacuate,evacuating);
        builder = builder.addTransition(created,lock,created);
        builder = builder.addTransition(created,unlock,created);
        builder = builder.addTransition(created,checklock,created);
        builder = builder.addTransition(created,startApplication,starting);
        builder = builder.addTransition(created,stopApplication,stopping);
        builder = builder.addTransition(created,configBackup,created);

        builder = builder.addTransition(instantiated,configure,configuring);
        builder = builder.addTransition(instantiated,test,testing);
        builder = builder.addTransition(instantiated,start,starting);
        builder = builder.addTransition(instantiated,terminate,terminating);
        builder = builder.addTransition(instantiated,restart,restarting);
        builder = builder.addTransition(instantiated,rebuild,rebuilding);
        builder = builder.addTransition(instantiated,stop,stopping);
        builder = builder.addTransition(instantiated,configModify,configuring);
        builder = builder.addTransition(instantiated,configScaleout,configuring);
        builder = builder.addTransition(instantiated,configRestore,configuring);
        builder = builder.addTransition(instantiated,healthcheck,testing);
        builder = builder.addTransition(instantiated,backup,backingUp);
        builder = builder.addTransition(instantiated,snapshot,snapshotting);
        builder = builder.addTransition(instantiated,softwareUpload,softwareUploading);
        builder = builder.addTransition(instantiated,liveUpgrade,upgrading);
        builder = builder.addTransition(instantiated,rollback,rollbacking);
        builder = builder.addTransition(instantiated,migrate,migrating);
        builder = builder.addTransition(instantiated,evacuate,evacuating);
        builder = builder.addTransition(instantiated,lock,instantiated);
        builder = builder.addTransition(instantiated,unlock,instantiated);
        builder = builder.addTransition(instantiated,checklock,instantiated);

        builder = builder.addTransition(configured,configure,configuring);
        builder = builder.addTransition(configured,test,testing);
        builder = builder.addTransition(configured,start,starting);
        builder = builder.addTransition(configured,terminate,terminating);
        builder = builder.addTransition(configured,restart,restarting);
        builder = builder.addTransition(configured,rebuild,rebuilding);
        builder = builder.addTransition(configured,stop,stopping);
        builder = builder.addTransition(configured,configModify,configuring);
        builder = builder.addTransition(configured,configScaleout,configuring);
        builder = builder.addTransition(configured,configRestore,configuring);
        builder = builder.addTransition(configured,healthcheck,testing);
        builder = builder.addTransition(configured,backup,backingUp);
        builder = builder.addTransition(configured,snapshot,snapshotting);
        builder = builder.addTransition(configured,softwareUpload,softwareUploading);
        builder = builder.addTransition(configured,liveUpgrade,upgrading);
        builder = builder.addTransition(configured,rollback,rollbacking);
        builder = builder.addTransition(configured,sync,configured);
        builder = builder.addTransition(configured,audit,configured);
        builder = builder.addTransition(configured,migrate,migrating);
        builder = builder.addTransition(configured,evacuate,evacuating);
        builder = builder.addTransition(configured,lock,configured);
        builder = builder.addTransition(configured,unlock,configured);
        builder = builder.addTransition(configured,checklock,configured);
        builder = builder.addTransition(configured,configBackup,configured);
        builder = builder.addTransition(configured,configBackupDelete,configured);
        builder = builder.addTransition(configured,configExport,configured);
        builder = builder.addTransition(configured,stopApplication,stopping);

        builder = builder.addTransition(tested,configure,configuring);
        builder = builder.addTransition(tested,test,testing);
        builder = builder.addTransition(tested,start,starting);
        builder = builder.addTransition(tested,terminate,terminating);
        builder = builder.addTransition(tested,restart,restarting);
        builder = builder.addTransition(tested,rebuild,rebuilding);
        builder = builder.addTransition(tested,stop,stopping);
        builder = builder.addTransition(tested,configModify,configuring);
        builder = builder.addTransition(tested,configScaleout,configuring);
        builder = builder.addTransition(tested,configRestore,configuring);
        builder = builder.addTransition(tested,healthcheck,testing);
        builder = builder.addTransition(tested,backup,backingUp);
        builder = builder.addTransition(tested,snapshot,snapshotting);
        builder = builder.addTransition(tested,softwareUpload,softwareUploading);
        builder = builder.addTransition(tested,liveUpgrade,upgrading);
        builder = builder.addTransition(tested,rollback,rollbacking);
        builder = builder.addTransition(tested,sync,tested);
        builder = builder.addTransition(tested,audit,tested);
        builder = builder.addTransition(tested,migrate,migrating);
        builder = builder.addTransition(tested,evacuate,evacuating);
        builder = builder.addTransition(tested,lock,tested);
        builder = builder.addTransition(tested,unlock,tested);
        builder = builder.addTransition(tested,checklock,tested);
        builder = builder.addTransition(tested,configBackup,tested);
        builder = builder.addTransition(tested,configBackupDelete,tested);
        builder = builder.addTransition(tested,configExport,tested);
        builder = builder.addTransition(tested,stopApplication,stopping);

        builder = builder.addTransition(running,configure,configuring);
        builder = builder.addTransition(running,test,testing);
        builder = builder.addTransition(running,start,starting);
        builder = builder.addTransition(running,terminate,terminating);
        builder = builder.addTransition(running,restart,restarting);
        builder = builder.addTransition(running,rebuild,rebuilding);
        builder = builder.addTransition(running,stop,stopping);
        builder = builder.addTransition(running,configModify,configuring);
        builder = builder.addTransition(running,configScaleout,configuring);
        builder = builder.addTransition(running,configRestore,configuring);
        builder = builder.addTransition(running,healthcheck,testing);
        builder = builder.addTransition(running,backup,backingUp);
        builder = builder.addTransition(running,snapshot,snapshotting);
        builder = builder.addTransition(running,softwareUpload,softwareUploading);
        builder = builder.addTransition(running,liveUpgrade,upgrading);
        builder = builder.addTransition(running,rollback,rollbacking);
        builder = builder.addTransition(running,sync,running);
        builder = builder.addTransition(running,audit,running);
        builder = builder.addTransition(running,migrate,migrating);
        builder = builder.addTransition(running,evacuate,evacuating);
        builder = builder.addTransition(running,lock,running);
        builder = builder.addTransition(running,unlock,running);
        builder = builder.addTransition(running,checklock,running);
        builder = builder.addTransition(running,configBackup,running);
        builder = builder.addTransition(running,configBackupDelete,running);
        builder = builder.addTransition(running,configExport,running);
        builder = builder.addTransition(running,stopApplication,stopping);

        builder = builder.addTransition(error,configure,configuring);
        builder = builder.addTransition(error,test,testing);
        builder = builder.addTransition(error,start,starting);
        builder = builder.addTransition(error,terminate,terminating);
        builder = builder.addTransition(error,restart,restarting);
        builder = builder.addTransition(error,rebuild,rebuilding);
        builder = builder.addTransition(error,stop,stopping);
        builder = builder.addTransition(error,configModify,configuring);
        builder = builder.addTransition(error,configScaleout,configuring);
        builder = builder.addTransition(error,configRestore,configuring);
        builder = builder.addTransition(error,healthcheck,testing);
        builder = builder.addTransition(error,backup,backingUp);
        builder = builder.addTransition(error,snapshot,snapshotting);
        builder = builder.addTransition(error,softwareUpload,softwareUploading);
        builder = builder.addTransition(error,liveUpgrade,upgrading);
        builder = builder.addTransition(error,rollback,rollbacking);
        builder = builder.addTransition(error,sync,error);
        builder = builder.addTransition(error,audit,error);
        builder = builder.addTransition(error,migrate,migrating);
        builder = builder.addTransition(error,evacuate,evacuating);
        builder = builder.addTransition(error,lock,error);
        builder = builder.addTransition(error,unlock,error);
        builder = builder.addTransition(error,checklock,error);
        builder = builder.addTransition(error,configBackup,error);
        builder = builder.addTransition(error,configBackupDelete,error);
        builder = builder.addTransition(error,configExport,error);
        builder = builder.addTransition(error,stopApplication,stopping);

        builder = builder.addTransition(unknown,configure,configuring);
        builder = builder.addTransition(unknown,test,testing);
        builder = builder.addTransition(unknown,start,starting);
        builder = builder.addTransition(unknown,terminate,terminating);
        builder = builder.addTransition(unknown,restart,restarting);
        builder = builder.addTransition(unknown,rebuild,rebuilding);
        builder = builder.addTransition(unknown,stop,stopping);
        builder = builder.addTransition(unknown,configModify,configuring);
        builder = builder.addTransition(unknown,configScaleout,configuring);
        builder = builder.addTransition(unknown,configRestore,configuring);
        builder = builder.addTransition(unknown,healthcheck,testing);
        builder = builder.addTransition(unknown,backup,backingUp);
        builder = builder.addTransition(unknown,snapshot,snapshotting);
        builder = builder.addTransition(unknown,softwareUpload,softwareUploading);
        builder = builder.addTransition(unknown,liveUpgrade,upgrading);
        builder = builder.addTransition(unknown,rollback,rollbacking);
        builder = builder.addTransition(unknown,sync,unknown);
        builder = builder.addTransition(unknown,audit,unknown);
        builder = builder.addTransition(unknown,migrate,migrating);
        builder = builder.addTransition(unknown,evacuate,evacuating);
        builder = builder.addTransition(unknown,lock,unknown);
        builder = builder.addTransition(unknown,unlock,unknown);
        builder = builder.addTransition(unknown,checklock,unknown);
        builder = builder.addTransition(unknown,configBackup,unknown);
        builder = builder.addTransition(unknown,configBackupDelete,unknown);
        builder = builder.addTransition(unknown,configExport,unknown);
        builder = builder.addTransition(unknown,stopApplication,stopping);

        builder = builder.addTransition(stopped,configure,configuring);
        builder = builder.addTransition(stopped,test,testing);
        builder = builder.addTransition(stopped,start,starting);
        builder = builder.addTransition(stopped,terminate,terminating);
        builder = builder.addTransition(stopped,restart,restarting);
        builder = builder.addTransition(stopped,rebuild,rebuilding);
        builder = builder.addTransition(stopped,configModify,configuring);
        builder = builder.addTransition(stopped,configScaleout,configuring);
        builder = builder.addTransition(stopped,configRestore,configuring);
        builder = builder.addTransition(stopped,healthcheck,testing);
        builder = builder.addTransition(stopped,backup,backingUp);
        builder = builder.addTransition(stopped,snapshot,snapshotting);
        builder = builder.addTransition(stopped,softwareUpload,softwareUploading);
        builder = builder.addTransition(stopped,liveUpgrade,upgrading);
        builder = builder.addTransition(stopped,rollback,rollbacking);
        builder = builder.addTransition(stopped,migrate,migrating);
        builder = builder.addTransition(stopped,evacuate,evacuating);
        builder = builder.addTransition(stopped,lock,stopped);
        builder = builder.addTransition(stopped,unlock,stopped);
        builder = builder.addTransition(stopped,checklock,stopped);

        builder = builder.addTransition(configuring,success,configured);
        builder = builder.addTransition(configuring,failure,error);

        builder = builder.addTransition(testing,success,tested);
        builder = builder.addTransition(testing,failure,error);

        builder = builder.addTransition(restarting,success,running);
        builder = builder.addTransition(restarting,failure,error);

        builder = builder.addTransition(starting,success,running);
        builder = builder.addTransition(starting,failure,error);

        builder = builder.addTransition(terminating,success,notInstantiated);
        builder = builder.addTransition(terminating,failure,error);

        builder = builder.addTransition(rebuilding,success,running);
        builder = builder.addTransition(rebuilding,failure,error);

        builder = builder.addTransition(stopping,success,stopped);
        builder = builder.addTransition(stopping,failure,error);

        builder = builder.addTransition(backingUp,success,running);
        builder = builder.addTransition(backingUp,failure,error);

        builder = builder.addTransition(snapshotting,success,running);
        builder = builder.addTransition(snapshotting,failure,error);

        builder = builder.addTransition(softwareUploading,success,running);
        builder = builder.addTransition(softwareUploading,failure,error);

        builder = builder.addTransition(upgrading,success,running);
        builder = builder.addTransition(upgrading,failure,error);

        builder = builder.addTransition(rollbacking,success,running);
        builder = builder.addTransition(rollbacking,failure,error);

        builder = builder.addTransition(migrating,success,running);
        builder = builder.addTransition(migrating,failure,error);

        builder = builder.addTransition(evacuating,success,running);
        builder = builder.addTransition(evacuating,failure,error);


        builder = builder.addTransition(configured,startApplication,starting);
        builder = builder.addTransition(tested,startApplication,starting);
        builder = builder.addTransition(error,startApplication,starting);
        builder = builder.addTransition(unknown,startApplication,starting);
        builder = builder.addTransition(running,startApplication,starting);

        return builder.build();
    }
}
