/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
 */

package org.openecomp.appc.executor.impl.objects;

import java.util.Date;

import org.openecomp.appc.executor.objects.CommandExecutorInput;

@SuppressWarnings("unused")
public class CommandRequest {


    private CommandExecutorInput commandExecutorInput;
    private Date commandInTimeStamp;

    public CommandRequest(CommandExecutorInput commandExecutorInput) {
        this.commandExecutorInput = commandExecutorInput;
    }


    public CommandExecutorInput getCommandExecutorInput() {
        return commandExecutorInput;
    }

    public void setCommandExecutorInput(CommandExecutorInput commandExecutorInput) {
        this.commandExecutorInput = commandExecutorInput;
    }

    public Date getCommandInTimeStamp() {
        return commandInTimeStamp;
    }

    public void setCommandInTimeStamp(Date commandInTimeStamp) {
        this.commandInTimeStamp = commandInTimeStamp;
    }

    //    @Override
    //    public boolean isTTLExpired() {
    //        Calendar tempTimeStamp = addTTLToRequestTime();
    //        long currentTime = System.currentTimeMillis();
    //        long tempTimeStampWithTTL = tempTimeStamp.getTimeInMillis() ;
    //        return  currentTime > tempTimeStampWithTTL;
    //    }
    //
    //    @Override
    //    public int getRemainingTTL(TimeUnit timeunit) {
    //        long tempTimeStampWithTTL = addTTLToRequestTime().getTimeInMillis() ;
    //        long currentTime = System.currentTimeMillis();
    //        long remainingTTL = tempTimeStampWithTTL - currentTime;
    //        return (int)(tempTimeStampWithTTL - currentTime);
    //    }
    //    private Calendar addTTLToRequestTime()
    //    {
    //        Date timeInRequest =  this.getCommandInTimeStamp();
    //        int ttlValue = this.getCommandContext().getTtl();
    //        Calendar tempTimeStamp = Calendar.getInstance();
    //        tempTimeStamp.setTime(timeInRequest);
    //        tempTimeStamp.add(Calendar.SECOND, ttlValue);
    //        return tempTimeStamp;
    //    }


    @Override
    public String toString() {
        return "CommandRequest{" +
                "commandExecutorInput=" + commandExecutorInput +
                ", commandInTimeStamp=" + commandInTimeStamp +
                '}';
    }
}
