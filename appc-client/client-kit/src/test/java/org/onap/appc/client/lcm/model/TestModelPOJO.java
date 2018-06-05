/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.client.lcm.model;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestModelPOJO {
    static List<Class> pojoClassName;
    static List<String> fields;
    static{
        pojoClassName = new ArrayList<>();
        pojoClassName.add(AuditInput.class);
        pojoClassName.add(AuditOutput.class);
        pojoClassName.add(CheckLockInput.class);
        pojoClassName.add(CheckLockOutput.class);
        pojoClassName.add(ConfigBackupDeleteInput.class);
        pojoClassName.add(ConfigBackupDeleteOutput.class);
        pojoClassName.add(ConfigBackupInput.class);
        pojoClassName.add(ConfigBackupOutput.class);
        pojoClassName.add(ConfigExportInput.class);
        pojoClassName.add(ConfigExportOutput.class);
        pojoClassName.add(ConfigModifyInput.class);
        pojoClassName.add(ConfigModifyOutput.class);
        pojoClassName.add(ConfigScaleOutInput.class);
        pojoClassName.add(ConfigScaleOutOutput.class);
        pojoClassName.add(ConfigRestoreInput.class);
        pojoClassName.add(ConfigRestoreOutput.class);
        pojoClassName.add(ConfigureInput.class);
        pojoClassName.add(ConfigureOutput.class);
        pojoClassName.add(EvacuateInput.class);
        pojoClassName.add(EvacuateOutput.class);
        pojoClassName.add(HealthCheckInput.class);
        pojoClassName.add(HealthCheckOutput.class);
        pojoClassName.add(LiveUpgradeInput.class);
        pojoClassName.add(LiveUpgradeOutput.class);
        pojoClassName.add(LockInput.class);
        pojoClassName.add(LockOutput.class);
        pojoClassName.add(MigrateInput.class);
        pojoClassName.add(MigrateOutput.class);
        pojoClassName.add(RebuildInput.class);
        pojoClassName.add(RebuildOutput.class);
        pojoClassName.add(HealthCheckInput.class);
        pojoClassName.add(RollbackInput.class);
        pojoClassName.add(RollbackOutput.class);
        pojoClassName.add(RestartInput.class);
        pojoClassName.add(RestartOutput.class);
        pojoClassName.add(SnapshotInput.class);
        pojoClassName.add(SnapshotOutput.class);
        pojoClassName.add(SoftwareUploadInput.class);
        pojoClassName.add(SoftwareUploadOutput.class);
        pojoClassName.add(StartApplicationInput.class);
        pojoClassName.add(StartApplicationOutput.class);
        pojoClassName.add(StartInput.class);
        pojoClassName.add(StartOutput.class);
        pojoClassName.add(StopApplicationInput.class);
        pojoClassName.add(StopApplicationOutput.class);
        pojoClassName.add(StopInput.class);
        pojoClassName.add(SyncInput.class);
        pojoClassName.add(SyncOutput.class);
        pojoClassName.add(TerminateInput.class);
        pojoClassName.add(TerminateOutput.class);
        pojoClassName.add(TestInput.class);
        pojoClassName.add(TestOutput.class);
        pojoClassName.add(UnlockInput.class);
        pojoClassName.add(UnlockOutput.class);

        fields = new ArrayList<>();
        fields.add("CommonHeader");
        fields.add("Action");
        fields.add("Payload");
        fields.add("Status");
    }
    @Test
    public void testModel() throws Exception{
        for(String field: fields){
            for(Class c: pojoClassName){
                Object instance = c.newInstance();
                Field[] fields =  c.getDeclaredFields();
                for(Field f: fields){
                    if(f.getType() == getClassForString(field)){
                        Method m = c.getDeclaredMethod("set"+field,getClassForString(field));
                        Object argument = createArgument(field);
                        m.invoke(instance,argument);

                        Method getter = c.getDeclaredMethod("get"+field);
                        Object getValue = getter.invoke(instance);
                        Assert.assertEquals("POJO test failed for class:"+c.getCanonicalName()+" for method:"+m.getName(),argument, getValue);
                    }
                }
            }
        }
    }

    private Object createArgument(String field){
        if(field.equals("CommonHeader")){
            return createHeader();
        }
        else if(field.equals("Action")){
            return createAction();
        }else if(field.equals("Payload")){
            return createPayload();
        }else if(field.equals("Status")){
            return createStatus();
        }

        throw new IllegalArgumentException();
    }

    private Action createAction(){
        return Action.Restart;
    }
    private Payload createPayload(){
        return new Payload();
    }
    private CommonHeader createHeader(){
        CommonHeader header = new CommonHeader();
        header.setApiVer("apiver");
        header.setOriginatorId("originator");
        return header;
    }
    private Status createStatus() {
        return new Status();
    }

    private Class getClassForString(String field){

        if(field.equals("CommonHeader")){
            return CommonHeader.class;
        }else if(field.equals("Action")){
            return Action.class;
        }else if(field.equals("ActionIdentifiers")){
            return ActionIdentifiers.class;
        }else if(field.equals("Payload")){
            return Payload.class;
        }else if(field.equals("Status")){
            return Status.class;
        }
        throw new IllegalArgumentException();
    }
}
