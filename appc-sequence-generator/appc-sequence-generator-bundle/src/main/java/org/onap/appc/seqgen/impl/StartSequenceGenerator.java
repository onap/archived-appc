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
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.dg.flowbuilder.FlowBuilder;
import org.onap.appc.dg.flowbuilder.exception.InvalidDependencyModelException;
import org.onap.appc.dg.flowbuilder.impl.FlowBuilderFactory;
import org.onap.appc.dg.objects.FlowStrategies;
import org.onap.appc.dg.objects.InventoryModel;
import org.onap.appc.dg.objects.VnfcDependencyModel;
import org.onap.appc.dg.objects.VnfcFlowModel;
import org.onap.appc.domainmodel.Vnfc;
import org.onap.appc.domainmodel.Vserver;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.seqgen.SequenceGenerator;
import org.onap.appc.seqgen.objects.ActionIdentifier;
import org.onap.appc.seqgen.objects.Constants;
import org.onap.appc.seqgen.objects.PreCheckOption;
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
import static org.onap.appc.seqgen.objects.Constants.PreCheckOperator;
import static org.onap.appc.seqgen.objects.Constants.Capabilties;
import static org.onap.appc.seqgen.objects.Constants.CapabilityLevel;
import static org.onap.appc.seqgen.objects.Constants.RETRY_COUNT_VALUE;
import static org.onap.appc.seqgen.objects.Constants.WAIT_TIME;
import static org.onap.appc.seqgen.objects.Constants.RETRY_COUNT;
import static org.onap.appc.seqgen.objects.Constants.WAIT_TIME_VALUE;;

public class StartSequenceGenerator implements SequenceGenerator {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(StartSequenceGenerator.class);

    private List<Transaction> generateSequenceWithOutDependency(SequenceGeneratorInput input) throws Exception {

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
            transaction.setAction(Action.START.getActionType());
            transaction.setActionLevel(ActionLevel.VM.getAction());
            ActionIdentifier actionIdentifier = new ActionIdentifier();
            actionIdentifier.setvServerId(vm.getId());
            transaction.setActionIdentifier(actionIdentifier);
            String vmId = vm.getUrl();
        String url = vm.getUrl();
        payload = payloadGenerator.getPayload(input, vmId, url);
        transaction.setPayload(payload);
            if(vservers.size()>1){
                Response ignoreResponse = new Response();
                ignoreResponse.setResponseMessage(ResponseMessage.FAILURE.getResponse());
                Map<String, String> ignoreAction = new HashMap<>();
                ignoreAction.put(ResponseAction.IGNORE.getAction(), Boolean.TRUE.toString());
                ignoreResponse.setResponseAction(ignoreAction);
                transaction.addResponse(ignoreResponse);
            }
            transactionList.add(transaction);
        }
        return transactionList;
    }

    private boolean checkSingleTransaction(List<Vnfc> invVnfcList) {
        int vServerCount=0;
        for(Vnfc vnfc : invVnfcList) {
            List<Vserver> vms = vnfc.getVserverList();
            vServerCount=vServerCount+vms.size();
        }
        return vServerCount <= 1;
    }

    private void updateResponse(Transaction transaction) {
        Response ignoreResponse = new Response();
        ignoreResponse.setResponseMessage(ResponseMessage.FAILURE.getResponse());
        Map<String, String> ignoreAction = new HashMap<>();
        ignoreAction.put(ResponseAction.IGNORE.getAction(), Boolean.TRUE.toString());
        ignoreResponse.setResponseAction(ignoreAction);
        transaction.addResponse(ignoreResponse);
    }

    private List<Transaction> generateSequenceWithDependencyModel(VnfcFlowModel flowModel, SequenceGeneratorInput input) throws APPCException {
        Integer waitTime = readWaitTime(input);
        Integer retryCount = readRetryCount(input);
        List<Transaction> transactionList = new LinkedList<>();
        Integer transactionId = 1;
        Iterator<List<Vnfc>> itr = flowModel.getModelIterator();
        while (itr.hasNext()) {
            List<Vnfc> vnfcs = itr.next();
            for (Vnfc vnfc : vnfcs) {
                List<Vserver> vms = vnfc.getVserverList();
                List<Integer> transactionIds = new LinkedList<>();
                if(!vms.isEmpty()) {
                    for (Vserver vm : vms) {
                        Transaction transaction = new Transaction();
                        transaction.setTransactionId(transactionId);
                        transactionIds.add(transactionId++);
                        transaction.setAction(Action.START.getActionType());
                        transaction.setActionLevel(ActionLevel.VM.getAction());
                        ActionIdentifier actionIdentifier = new ActionIdentifier();
                        actionIdentifier.setvServerId(vm.getId());
                        transaction.setActionIdentifier(actionIdentifier);
                        transaction.setPayload(input.getRequestInfo().getPayload());
                        Response ignoreResponse = new Response();
                        ignoreResponse.setResponseMessage(ResponseMessage.FAILURE.getResponse());
                        Map<String, String> ignoreAction = new HashMap<>();
                        ignoreAction.put(ResponseAction.IGNORE.getAction(), Boolean.TRUE.toString());
                        ignoreResponse.setResponseAction(ignoreAction);
                        transaction.addResponse(ignoreResponse);
                        transactionList.add(transaction);
                    }
                    boolean startApplicationSupported = readApplicationStartCapability(input);
                    if (startApplicationSupported) {
                        Transaction startAppTransaction = new Transaction();
                        startAppTransaction.setTransactionId(transactionId++);
                        startAppTransaction.setAction(Action.START_APPLICATION.getActionType());
                        startAppTransaction.setActionLevel(ActionLevel.VNFC.getAction());
                        ActionIdentifier startActionIdentifier = new ActionIdentifier();
                        startActionIdentifier.setVnfcName(vnfc.getVnfcName());
                        startAppTransaction.setActionIdentifier(startActionIdentifier);
                        startAppTransaction.setPayload(input.getRequestInfo().getPayload());

                        List<PreCheckOption> preCheckOptions = buildPreCheckOptions(transactionIds);
                        startAppTransaction.setPreCheckOperator(PreCheckOperator.ANY.getOperator());
                        startAppTransaction.setPrecheckOptions(preCheckOptions);
                        transactionList.add(startAppTransaction);
                    }
                    boolean healthCheckSupported = readHealthCheckCapabilites(input.getCapability());
                    if (healthCheckSupported) {
                        Transaction healthCheckTransaction = new Transaction();
                        healthCheckTransaction.setTransactionId(transactionId++);
                        healthCheckTransaction.setAction(Action.HEALTH_CHECK.getActionType());
                        healthCheckTransaction.setActionLevel(ActionLevel.VNFC.getAction());
                        ActionIdentifier healthCheckActionIdentifier = new ActionIdentifier();
                        healthCheckActionIdentifier.setVnfcName(vnfc.getVnfcName());
                        healthCheckTransaction.setActionIdentifier(healthCheckActionIdentifier);
                        healthCheckTransaction.setPayload(input.getRequestInfo().getPayload());

                        Response retryResponse = new Response();
                        retryResponse.setResponseMessage(ResponseMessage.UNHEALTHY.getResponse());
                        Map<String, String> retryAction = new HashMap<>();
                        retryAction.put(ResponseAction.RETRY.getAction(), retryCount.toString());
                        retryAction.put(ResponseAction.WAIT.getAction(), waitTime.toString());
                        retryResponse.setResponseAction(retryAction);
                        healthCheckTransaction.addResponse(retryResponse);

                        Response healthyResponse = new Response();
                        healthyResponse.setResponseMessage(ResponseMessage.HEALTHY.getResponse());
                        Map<String, String> healthyAction = new HashMap<>();
                        healthyAction.put(ResponseAction.CONTINUE.getAction().toLowerCase(), Boolean.TRUE.toString());
                        healthyResponse.setResponseAction(healthyAction);
                        healthCheckTransaction.addResponse(healthyResponse);

                        Response failureResponse = new Response();
                        failureResponse.setResponseMessage(ResponseMessage.FAILURE.getResponse());
                        Map<String, String> failureResonseAction = new HashMap<>();
                        failureResonseAction.put(ResponseAction.STOP.getAction(), Boolean.TRUE.toString());
                        failureResponse.setResponseAction(failureResonseAction);
                        healthCheckTransaction.addResponse(failureResponse);
                        transactionList.add(healthCheckTransaction);
                    }
                }
            }
        }
        return transactionList;
    }

    private List<PreCheckOption> buildPreCheckOptions(List<Integer> transactionIds) {
        List<PreCheckOption> preCheckOptions = new LinkedList<>();
        for (Integer vmTransactionId : transactionIds) {
            PreCheckOption option = new PreCheckOption();
            option.setPreTransactionId(vmTransactionId);
            option.setParamName("status");
            option.setParamValue("success");
            preCheckOptions.add(option);
        }
        return preCheckOptions;
    }

    @Override
    public List<Transaction> generateSequence(SequenceGeneratorInput input) throws Exception {
            if (input.getRequestInfo().getActionLevel().equals(ActionLevel.VNF.getAction()) && input.getDependencyModel() != null) {
                if(isVnfcPresent(input)) {
                    FlowStrategies flowStrategy = readFlowStrategy(input);
                    VnfcFlowModel flowModel = null;
                    try {
                        flowModel = buildFlowModel(input.getInventoryModel()
                                , input.getDependencyModel(), flowStrategy);
                    } catch (InvalidDependencyModelException invalidDependencyModelException) {
                        logger.error("Error Generating Sequence", invalidDependencyModelException);
                        throw  new APPCException(invalidDependencyModelException.getMessage(), invalidDependencyModelException);
                    }
                    logger.debug("Flow Model " + flowModel);
                    return generateSequenceWithDependencyModel(flowModel, input);
                }
                 else throw new APPCException("Vnfc details is missing in the input");
            } else {
                logger.info("Generating sequence without dependency model");
                return generateSequenceWithOutDependency(input);
            }
    }

    private VnfcFlowModel buildFlowModel(InventoryModel inventoryModel, VnfcDependencyModel dependencyModel, FlowStrategies flowStrategy) throws APPCException, InvalidDependencyModelException {
        FlowBuilder flowBuilder = FlowBuilderFactory.getInstance().getFlowBuilder(flowStrategy);
        if (flowBuilder == null) {
            throw new APPCException("Flow Strategy not supported " + flowStrategy);
        }
        return flowBuilder.buildFlowModel(dependencyModel, inventoryModel);
    }

    private FlowStrategies readFlowStrategy(SequenceGeneratorInput sequenceGeneratorInput) {
        Map<String, String> tunableParams = sequenceGeneratorInput.getTunableParams();
        FlowStrategies strategy=null;
        String strategyStr = null;
        if (tunableParams != null) {
            strategyStr = tunableParams.get(Constants.STRATEGY);
            strategy = FlowStrategies.findByString(strategyStr);
        }
        if (strategy == null)
            strategy= FlowStrategies.FORWARD;
        return strategy;
    }

    private boolean readHealthCheckCapabilites(Map<String, List<String>> capabilities) {
        if (capabilities != null) {
            List<String> vnfcCapabilities = capabilities.get(CapabilityLevel.VNFC.getLevel());
            if (vnfcCapabilities != null)
                return vnfcCapabilities.stream()
                        .anyMatch(p -> Capabilties.HEALTH_CHECK.getCapability().equalsIgnoreCase(p));
        }
        return false;
    }

    private boolean readApplicationStartCapability(SequenceGeneratorInput input) {
        Map<String, List<String>> capability = input.getCapability();
        if (capability != null) {
            List<String> vnfcCapabilities = capability.get(CapabilityLevel.VNFC.getLevel());
            if (vnfcCapabilities != null)
                return vnfcCapabilities.stream().anyMatch(p -> Capabilties.START_APPLICATION.getCapability().equalsIgnoreCase(p));
        }
        return false;
    }

    private Integer readRetryCount(SequenceGeneratorInput input) throws APPCException {
        String paramValStr = input.getTunableParams().get(RETRY_COUNT);
        if (StringUtils.isEmpty(paramValStr)) {
            return RETRY_COUNT_VALUE;
        }
        try {
            return Integer.parseInt(paramValStr);
        } catch (NumberFormatException e) {
            String message = "Invalid Number for Retry Count " + paramValStr;
            logger.error(message, e);
            throw new APPCException(message);
        }
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

    private Integer readWaitTime(SequenceGeneratorInput input) throws APPCException {
        String paramValStr = input.getTunableParams().get(WAIT_TIME);
        if (StringUtils.isEmpty(paramValStr)) {
            return WAIT_TIME_VALUE;
        }
        try {
            return Integer.parseInt(paramValStr);
        } catch (NumberFormatException e) {
            String message = "Invalid Number for Wait Time " + paramValStr;
            logger.error(message, e);
            throw new APPCException(message);
        }
    }
}
