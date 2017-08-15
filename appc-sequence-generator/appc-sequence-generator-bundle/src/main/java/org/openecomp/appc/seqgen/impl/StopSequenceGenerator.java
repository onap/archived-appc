/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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

package org.openecomp.appc.seqgen.impl;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.dg.flowbuilder.FlowBuilder;
import org.openecomp.appc.dg.flowbuilder.impl.FlowBuilderFactory;
import org.openecomp.appc.dg.objects.FlowStrategies;
import org.openecomp.appc.dg.objects.InventoryModel;
import org.openecomp.appc.dg.objects.VnfcDependencyModel;
import org.openecomp.appc.dg.objects.VnfcFlowModel;
import org.openecomp.appc.domainmodel.Vnfc;
import org.openecomp.appc.domainmodel.Vserver;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.seqgen.SequenceGenerator;
import org.openecomp.appc.seqgen.objects.*;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.*;

import static org.openecomp.appc.seqgen.objects.Constants.*;

public class StopSequenceGenerator implements SequenceGenerator {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(StartSequenceGenerator.class);

    @Override
    public List<Transaction> generateSequence(SequenceGeneratorInput input) throws APPCException {
        if(input.getRequestInfo().getActionLevel().equals(ActionLevel.VM.getAction())||input.getRequestInfo().getActionLevel().equals(ActionLevel.VNFC.getAction())||
                input.getRequestInfo().getActionLevel().equals(ActionLevel.VNF.getAction())||input.getRequestInfo().getActionLevel().equals(ActionLevel.VF_MODULE.getAction())) {
            if (input.getRequestInfo().getActionLevel().equals(ActionLevel.VNF.getAction()) && input.getDependencyModel() != null) {
                FlowStrategies flowStrategy = readStopFlowStrategy(input);
                VnfcFlowModel flowModel = buildFlowModel(input.getInventoryModel()
                        , input.getDependencyModel(), flowStrategy);
                logger.debug("Flow Model " + flowModel);
                return generateSequenceWithDependencyModel(flowModel, input);
            } else {
                logger.info("Generating sequence without dependency model");
                return generateSequenceWithOutDependency(input);
            }
        }throw new  APPCException("Invalid action level "+input.getRequestInfo().getActionLevel());

    }
    private List<Transaction> generateSequenceWithOutDependency(SequenceGeneratorInput input){
        List<Transaction> transactionList = new LinkedList<>();
        Integer transactionId = 1;
        List<Integer> transactionIds = new LinkedList<>();
        List<Vnfc> invVnfcList = input.getInventoryModel().getVnf().getVnfcs();
        boolean singleTransaction=checkSingleTransaction(invVnfcList);
        for (Vnfc vnfc : invVnfcList) {
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
                    if(!singleTransaction){
                        updateStopResponse(transaction);
                    }
                    transactionList.add(transaction);
                }
            }
        return transactionList;
    }

    private void updateStopResponse(Transaction transaction) {
        Response failureResponse = new Response();
        failureResponse.setResponseMessage(ResponseMessage.FAILURE.getResponse());
        Map<String,String> failureAction = new HashMap<>();
        failureAction.put(ResponseAction.IGNORE.getAction(),Boolean.TRUE.toString());
        failureResponse.setResponseAction(failureAction);
        transaction.addResponse(failureResponse);
    }
    private boolean checkSingleTransaction(List<Vnfc> invVnfcList) {
        int vServerCount=0;
        for(Vnfc vnfc : invVnfcList) {
            List<Vserver> vms = vnfc.getVserverList();
            vServerCount=vServerCount+vms.size();
        }
        return vServerCount <= 1;
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
                if(stopApplicationSupported){
                    Transaction stopAppTransaction = new Transaction();
                    stopAppTransaction.setTransactionId(transactionId++);
                    stopAppTransaction.setAction(Action.STOP_APPLICATION.getActionType());
                    stopAppTransaction.setActionLevel(ActionLevel.VNFC.getAction());
                    ActionIdentifier stopActionIdentifier = new ActionIdentifier();
                    stopActionIdentifier .setVnfcName(vnfc.getVnfcName());
                    stopAppTransaction.setActionIdentifier(stopActionIdentifier );
                    stopAppTransaction.setPayload(input.getRequestInfo().getPayload());
                    updateStopResponse(stopAppTransaction);
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

                    updateStopResponse(transaction);
                    transactionList.add(transaction);
                }
            }
        }
        return transactionList;
    }

    private VnfcFlowModel buildFlowModel(InventoryModel inventoryModel, VnfcDependencyModel dependencyModel, FlowStrategies flowStrategy) throws APPCException {
        FlowBuilder flowBuilder = FlowBuilderFactory.getInstance().getFlowBuilder(flowStrategy);
        if (flowBuilder == null) {
            throw new APPCException("Flow Strategy not supported " + flowStrategy);
        }
        return flowBuilder.buildFlowModel(dependencyModel, inventoryModel);
    }

    private FlowStrategies readStopFlowStrategy(SequenceGeneratorInput sequenceGeneratorInput) throws APPCException {
        Map<String, String> tunableParams = sequenceGeneratorInput.getTunableParams();
        FlowStrategies strategy;
        String strategyStr = null;
        if (tunableParams != null) {
            strategyStr = tunableParams.get(Constants.STRATEGY);
            if (StringUtils.isBlank(strategyStr)) {
                return FlowStrategies.REVERSE;
            }
            strategy = FlowStrategies.findByString(strategyStr);
            if (strategy != null) {
                return strategy;
            }
        }
        throw new APPCException("Invalid Strategy " + strategyStr);
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
