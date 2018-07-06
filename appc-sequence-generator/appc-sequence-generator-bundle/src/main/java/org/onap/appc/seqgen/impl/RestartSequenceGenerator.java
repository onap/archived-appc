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

package org.onap.appc.seqgen.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.domainmodel.Vserver;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.seqgen.SequenceGenerator;
import org.onap.appc.seqgen.objects.ActionIdentifier;
import org.onap.appc.seqgen.objects.Constants;
import org.onap.appc.seqgen.objects.Response;
import org.onap.appc.seqgen.objects.SequenceGeneratorInput;
import org.onap.appc.seqgen.objects.Transaction;;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RestartSequenceGenerator implements SequenceGenerator{
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RestartSequenceGenerator.class);

    @Override
    public List<Transaction> generateSequence(SequenceGeneratorInput input) throws Exception {
            logger.info("Generating sequence without dependency model");
            return generateSequenceWithOutDependency(input);
    }

    private List<Transaction> generateSequenceWithOutDependency(SequenceGeneratorInput input) throws Exception{
        String payload = null;
        List<Transaction> transactionList = new LinkedList<>();
        Integer transactionId = 1;
        List<Vserver> vservers = input.getInventoryModel().getVnf().getVservers();
        List<Integer> transactionIds = new LinkedList<>();
        PayloadGenerator payloadGenerator = new PayloadGenerator();
            for (Vserver vm : vservers) {
                Transaction transactionStop = new Transaction();
                transactionStop.setTransactionId(transactionId);
                transactionIds.add(transactionId++);
                transactionStop.setAction(Constants.Action.STOP.getActionType());
                transactionStop.setActionLevel(Constants.ActionLevel.VM.getAction());
                ActionIdentifier actionIdentifier = new ActionIdentifier();
                actionIdentifier.setvServerId(vm.getId());
                transactionStop.setActionIdentifier(actionIdentifier);
                String vmId = vm.getId();
		String url = vm.getUrl();
		payload = payloadGenerator.getPayload(input, vmId, url);
		transactionStop.setPayload(payload);
                if (vservers.size()>1) {
                    Response failureResponse = new Response();
                    failureResponse.setResponseMessage(Constants.ResponseMessage.FAILURE.getResponse());
                    Map<String,String> failureAction = new HashMap<>();
                    if(!checkLastVM(vservers,vm.getId()))
                    {
                        failureAction.put(Constants.ResponseAction.JUMP.getAction(), String.valueOf(transactionId+1));
                        failureResponse.setResponseAction(failureAction);
                        transactionStop.addResponse(failureResponse);
                    }
                }
                transactionList.add(transactionStop);
                Transaction transactionStart = new Transaction();
                transactionStart.setTransactionId(transactionId);
                transactionIds.add(transactionId++);
                transactionStart.setAction(Constants.Action.START.getActionType());
                transactionStart.setActionLevel(Constants.ActionLevel.VM.getAction());
                ActionIdentifier actionIdentifierStart = new ActionIdentifier();
                actionIdentifierStart.setvServerId(vm.getId());
                transactionStart.setActionIdentifier(actionIdentifierStart);
                payload = payloadGenerator.getPayload(input, vmId, url);
	        transactionStart.setPayload(payload);
                if (vservers.size()>1) {
                    Response failureResponse = new Response();
                    failureResponse.setResponseMessage(Constants.ResponseMessage.FAILURE.getResponse());
                    Map<String,String> failureAction = new HashMap<>();
                    if(!checkLastVM(vservers,vm.getId()))
                    {
                        failureAction.put(Constants.ResponseAction.JUMP.getAction(),transactionId.toString());
                        failureResponse.setResponseAction(failureAction);
                        transactionStart.addResponse(failureResponse);
                    }
                }
                transactionList.add(transactionStart);
            }
        return transactionList;
    }

    private boolean checkLastVM(List<Vserver> vservers, String  vmId){
        Vserver vm= vservers.get(vservers.size()-1);
        return vm.getId().equals(vmId);
    }
}
