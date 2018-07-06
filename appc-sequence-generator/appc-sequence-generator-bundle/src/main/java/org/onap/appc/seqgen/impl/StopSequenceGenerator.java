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

import org.apache.commons.lang3.StringUtils;
import org.onap.appc.dg.flowbuilder.FlowBuilder;
import org.onap.appc.dg.flowbuilder.impl.FlowBuilderFactory;
import org.onap.appc.dg.flowbuilder.exception.InvalidDependencyModelException;
import org.onap.appc.dg.objects.FlowStrategies;
import org.onap.appc.dg.objects.InventoryModel;
import org.onap.appc.dg.objects.VnfcDependencyModel;
import org.onap.appc.dg.objects.VnfcFlowModel;
import org.onap.appc.domainmodel.Vnfc;
import org.onap.appc.domainmodel.Vserver;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.seqgen.SequenceGenerator;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.seqgen.objects.ActionIdentifier;
import org.onap.appc.seqgen.objects.Constants;
import org.onap.appc.seqgen.objects.Response;
import org.onap.appc.seqgen.objects.SequenceGeneratorInput;
import org.onap.appc.seqgen.objects.Transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.onap.appc.seqgen.objects.Constants.Action;
import static org.onap.appc.seqgen.objects.Constants.ActionLevel;
import static org.onap.appc.seqgen.objects.Constants.ResponseAction;
import static org.onap.appc.seqgen.objects.Constants.ResponseMessage;
import static org.onap.appc.seqgen.objects.Constants.Capabilties;

public class StopSequenceGenerator implements SequenceGenerator {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(StopSequenceGenerator.class);

    @Override
    public List<Transaction> generateSequence(SequenceGeneratorInput input) throws Exception {
        if (input.getRequestInfo().getActionLevel().equals(ActionLevel.VNF.getAction()) && input.getDependencyModel() != null ) {
            if(isVnfcPresent(input)) {
                FlowStrategies flowStrategy = readFlowStrategy(input);
                VnfcFlowModel flowModel = null;
                try {
                        flowModel = buildFlowModel(input.getInventoryModel(), input.getDependencyModel(), flowStrategy);
                    } catch (InvalidDependencyModelException invalidDependencyModelException) {
                        logger.error("Error Generating Sequence", invalidDependencyModelException);
                        throw  new APPCException(invalidDependencyModelException.getMessage(), invalidDependencyModelException);
                }
                logger.debug("Flow Model " + flowModel);
                return generateSequenceWithDependencyModel(flowModel, input);
            }
                else throw  new APPCException("Vnfc details missing in the input");
            } else {
                logger.info("Generating sequence without dependency model");
                return generateSequenceWithOutDependency(input);
            }
    }

    private List<Transaction> generateSequenceWithOutDependency(SequenceGeneratorInput input)throws Exception{
        String payload = null;
        PayloadGenerator payloadGenerator = new PayloadGenerator();
        List<Transaction> transactionList = new LinkedList<>();
        Integer transactionId = 1;
        List<Vserver> vservers = input.getInventoryModel().getVnf().getVservers();
        List<Integer> transactionIds = new LinkedList<>();
        for (Vserver vm : vservers) {
            Transaction transaction = new Transaction();
            transaction.setTransactionId(transactionId);
            transactionIds.add(transactionId++);
            transaction.setAction(Action.STOP.getActionType());
            transaction.setActionLevel(ActionLevel.VM.getAction());
            ActionIdentifier actionIdentifier = new ActionIdentifier();
            actionIdentifier.setvServerId(vm.getId());
            transaction.setActionIdentifier(actionIdentifier);
            String vmId = vm.getId();
            String url = vm.getUrl();
            payload = payloadGenerator.getPayload(input, vmId, url);
            transaction.setPayload(payload);
            if(vservers.size()>1){
                Response failureResponse = new Response();
                failureResponse.setResponseMessage(ResponseMessage.FAILURE.getResponse());
                Map<String,String> failureAction = new HashMap<>();
                failureAction.put(ResponseAction.IGNORE.getAction(),Boolean.TRUE.toString());
                failureResponse.setResponseAction(failureAction);
                transaction.addResponse(failureResponse);
            }
            transactionList.add(transaction);
       }
       return transactionList;
    }

    private List<Transaction> generateSequenceWithDependencyModel(VnfcFlowModel flowModel,SequenceGeneratorInput input){
        List<Transaction> transactionList = new LinkedList<>();
        Integer transactionId = 1;
        List<Integer> transactionIds = new LinkedList<>();
        Iterator<List<Vnfc>> itr = flowModel.getModelIterator();
        while (itr.hasNext()){
            List<Vnfc> vnfcs = itr.next();
            for(Vnfc vnfc:vnfcs){
                boolean stopApplicationSupported = readApplicationStopCapability(input);
                if(stopApplicationSupported && !vnfc.getVserverList().isEmpty()){
                    Transaction stopAppTransaction = new Transaction();
                    stopAppTransaction.setTransactionId(transactionId++);
                    stopAppTransaction.setAction(Action.STOP_APPLICATION.getActionType());
                    stopAppTransaction.setActionLevel(ActionLevel.VNFC.getAction());
                    ActionIdentifier stopActionIdentifier = new ActionIdentifier();
                    stopActionIdentifier .setVnfcName(vnfc.getVnfcName());
                    stopAppTransaction.setActionIdentifier(stopActionIdentifier );
                    stopAppTransaction.setPayload(input.getRequestInfo().getPayload());
                    Response failureResponse = new Response();
                    failureResponse.setResponseMessage(ResponseMessage.FAILURE.getResponse());
                    Map<String,String> failureAction = new HashMap<>();
                    failureAction.put(ResponseAction.IGNORE.getAction(),Boolean.TRUE.toString());
                    failureResponse.setResponseAction(failureAction);
                    stopAppTransaction.addResponse(failureResponse);
                    transactionList.add(stopAppTransaction);
                }
                List<Vserver> vms = vnfc.getVserverList();
                for(Vserver vm:vms){
                    Transaction transaction = new Transaction();
                    transaction.setTransactionId(transactionId);
                    transactionIds.add(transactionId++);
                    transaction.setAction(Action.STOP.getActionType());
                    transaction.setActionLevel(ActionLevel.VM.getAction());
                    ActionIdentifier actionIdentifier = new ActionIdentifier();
                    actionIdentifier.setvServerId(vm.getId());
                    transaction.setActionIdentifier(actionIdentifier);
                    transaction.setPayload(input.getRequestInfo().getPayload());
                    Response failureResponse = new Response();
                    failureResponse.setResponseMessage(ResponseMessage.FAILURE.getResponse());
                    Map<String,String> failureAction = new HashMap<>();
                    failureAction.put(ResponseAction.IGNORE.getAction(),Boolean.TRUE.toString());
                    failureResponse.setResponseAction(failureAction);
                    transaction.addResponse(failureResponse);
                    transactionList.add(transaction);
                }
            }
        }
        return transactionList;
    }

    private VnfcFlowModel buildFlowModel(InventoryModel inventoryModel, VnfcDependencyModel dependencyModel, FlowStrategies flowStrategy) throws APPCException, InvalidDependencyModelException {
        FlowBuilder flowBuilder = FlowBuilderFactory.getInstance().getFlowBuilder(flowStrategy);
        if (flowBuilder == null) {
            throw new APPCException("Flow Strategy not supported " + flowStrategy);
        }
        return flowBuilder.buildFlowModel(dependencyModel, inventoryModel);
    }

    private FlowStrategies readFlowStrategy(SequenceGeneratorInput sequenceGeneratorInput)  {
        Map<String, String> tunableParams = sequenceGeneratorInput.getTunableParams();
        FlowStrategies strategy = null;
        String strategyStr = null;
        if (tunableParams != null) {
            strategyStr = tunableParams.get(Constants.STRATEGY);
            strategy = FlowStrategies.findByString(strategyStr);
        }
        if (strategy == null)
             strategy= FlowStrategies.REVERSE;
        return strategy;
    }

    private boolean isVnfcPresent(SequenceGeneratorInput input){
        boolean vnfcPresent=true;
        List<Vserver> vservers = input.getInventoryModel().getVnf().getVservers();
        for (Vserver vm : vservers) {
            if(!(vm.getVnfc()!=null&& vm.getVnfc().getVnfcType()!=null&& vm.getVnfc().getVnfcName()!=null)){
                vnfcPresent=false;break;
            }
        }
        return vnfcPresent;
    }

    private boolean readApplicationStopCapability(SequenceGeneratorInput input) {
        Map<String,List<String>> capability = input.getCapability();
        if(capability!= null){
            List<String> vnfcCapabilities = capability.get(Constants.CapabilityLevel.VNFC.getLevel());
            if(vnfcCapabilities!=null)
                return vnfcCapabilities.stream().anyMatch(p -> Capabilties.STOP_APPLICATION.getCapability().equalsIgnoreCase(p));
        }
        return false;
    }


}
