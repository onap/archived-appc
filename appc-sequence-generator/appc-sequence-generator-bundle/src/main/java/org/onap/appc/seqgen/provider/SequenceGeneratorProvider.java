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

package org.onap.appc.seqgen.provider;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.util.concurrent.Futures;
import org.apache.commons.lang.StringUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.GenerateSequenceInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.GenerateSequenceOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.GenerateSequenceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.SequenceGeneratorService;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.dependency.info.dependency.info.Vnfcs;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.inventory.info.inventory.info.vnf.info.Vm;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.response.StatusBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.response.Transactions;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.response.TransactionsBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.response.transactions.ActionIdentifier;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.response.transactions.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.sequencegenerator.rev170706.response.transactions.responses.ResponseActionBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.onap.appc.dg.objects.InventoryModel;
import org.onap.appc.dg.objects.Node;
import org.onap.appc.dg.objects.VnfcDependencyModel;
import org.onap.appc.domainmodel.Vnf;
import org.onap.appc.domainmodel.Vnfc;
import org.onap.appc.domainmodel.Vserver;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.seqgen.SequenceGenerator;
import org.onap.appc.seqgen.impl.SequenceGeneratorFactory;
import org.onap.appc.seqgen.objects.Constants;
import org.onap.appc.seqgen.objects.PreCheckOption;
import org.onap.appc.seqgen.objects.RequestInfo;
import org.onap.appc.seqgen.objects.RequestInfoBuilder;
import org.onap.appc.seqgen.objects.Response;
import org.onap.appc.seqgen.objects.SequenceGeneratorInput;
import org.onap.appc.seqgen.objects.SequenceGeneratorInputBuilder;
import org.onap.appc.seqgen.objects.Transaction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class SequenceGeneratorProvider implements AutoCloseable,SequenceGeneratorService{
    protected DataBroker dataBroker;
    protected RpcProviderRegistry rpcRegistry;
    protected NotificationProviderService notificationService;
    protected BindingAwareBroker.RpcRegistration<SequenceGeneratorService> rpcRegistration;
    private final EELFLogger log = EELFManager.getInstance().getLogger(SequenceGeneratorProvider.class);
    private final ExecutorService executor;
    private final static String APP_NAME = "SequenceGeneratorProvider";

    public SequenceGeneratorProvider(DataBroker dataBroker2, NotificationProviderService notificationProviderService
            , RpcProviderRegistry rpcRegistry2) {
        log.info("Creating provider for " + APP_NAME);
        executor = Executors.newFixedThreadPool(1);
        this.dataBroker = dataBroker2;
        this.notificationService = notificationProviderService;

        this.rpcRegistry = rpcRegistry2;

        if (this.rpcRegistry != null) {
            rpcRegistration = rpcRegistry.addRpcImplementation(SequenceGeneratorService.class, this);
        }
        log.info("Initialization complete for " + APP_NAME);
    }

    @Override
    public void close() throws Exception {
        log.info("Closing provider for " + APP_NAME);
        if(this.executor != null){
            executor.shutdown();
        }
        if(this.rpcRegistration != null){
            rpcRegistration.close();
        }
        log.info("Successfully closed provider for " + APP_NAME);
    }

    @Override
    public Future<RpcResult<GenerateSequenceOutput>> generateSequence(GenerateSequenceInput input) {
        RpcResult<GenerateSequenceOutput> rpcResult=null;
        log.debug("Received input = " + input );
        try {
            if(input.getRequestInfo()==null){
                throw new APPCException("Request info is missing in the input");
            }
            SequenceGenerator seqGenerator = SequenceGeneratorFactory.getInstance()
                    .createSequenceGenerator(VNFOperation.findByString(input.getRequestInfo().getAction().name()));
            SequenceGeneratorInput seqGenInput = buildSeqGenInput(input);
            List<Transaction> transactions = seqGenerator.generateSequence(seqGenInput);
            rpcResult = buildSuccessResponse(transactions);
        } catch (Exception e) {
            log.error("Error Generating Sequence",e);
            rpcResult = buildFailureResponse(e.getMessage());
        }
        return Futures.immediateFuture(rpcResult);
    }

    private RpcResult<GenerateSequenceOutput> buildSuccessResponse(List<Transaction> transactions) {
        log.info("Building response from the list of transactions");
        List<Transactions> transactionList = new LinkedList<>();
        for(Transaction transaction:transactions){
            ActionIdentifier actionIdentifier = buildActionIdentifierForResponse(transaction);
            List<PrecheckOptions> precheckOptions = buildPrecheckOptionsForResponse(transaction);
            List<Responses> responseList = getResponses(transaction);
            Transactions transactionObj
                    = new TransactionsBuilder()
                    .setActionIdentifier(actionIdentifier)
                    .setAction(transaction.getAction())
                    .setActionLevel(transaction.getActionLevel())
                    .setPrecheckOperator(transaction.getPreCheckOperator())
                    .setPayload(transaction.getPayload())
                    .setTransactionId(transaction.getTransactionId())
                    .setPrecheckOptions(precheckOptions)
                    .setResponses(responseList)
                    .build();
            transactionList.add(transactionObj);
        }

        GenerateSequenceOutputBuilder builder = new GenerateSequenceOutputBuilder()
                .setTransactions(transactionList);

        return RpcResultBuilder
                .<GenerateSequenceOutput> status(true)
                .withResult(builder.build()).build();
    }

    private ActionIdentifier buildActionIdentifierForResponse(Transaction transaction) {
        log.info("Adding action identifiers to response.");
        ActionIdentifier actionIdentifier = null;
        if(transaction.getActionIdentifier() != null){
            actionIdentifier = new ActionIdentifierBuilder()
                    .setVnfId(transaction.getActionIdentifier().getVnfId())
                    .setVnfcName(transaction.getActionIdentifier().getVnfcName())
                    .setVserverId(transaction.getActionIdentifier().getvServerId())
                    .build();
        }
        return actionIdentifier;
    }

    private List<PrecheckOptions> buildPrecheckOptionsForResponse(Transaction transaction) {
        log.info("Adding Precheck options to response");
        List<PrecheckOptions> precheckOptions = new LinkedList<>();
        if(transaction.getPrecheckOptions()!=null){
            for(PreCheckOption option:transaction.getPrecheckOptions()){
                PrecheckOptions precheckOption = new PrecheckOptionsBuilder()
                        .setParamName(option.getParamName())
                        .setParamValue(option.getParamValue())
                        .setPreTransactionId(option.getPreTransactionId())
                        .setRule(option.getRule())
                        .build();
                precheckOptions.add(precheckOption);
            }
        }
        return precheckOptions;
    }
    private List<Responses> getResponses(Transaction transaction) {
        List<Responses> responseList = new LinkedList<>();
        for(Response resp : transaction.getResponses()){
            Map<String,String> responseActions = resp.getResponseAction();
            ResponseActionBuilder responseActionBuilder = new ResponseActionBuilder();
            if(responseActions.get(Constants.ResponseAction.WAIT.getAction())!=null){
                responseActionBuilder = responseActionBuilder.setWait(Integer.parseInt(responseActions.get(Constants.ResponseAction.WAIT.getAction())));
            }
            if(responseActions.get(Constants.ResponseAction.RETRY.getAction())!=null){
                responseActionBuilder = responseActionBuilder.setRetry(Integer.parseInt(responseActions.get(Constants.ResponseAction.RETRY.getAction())));
            }
            if(responseActions.get(Constants.ResponseAction.CONTINUE.getAction().toLowerCase())!=null){
                responseActionBuilder = responseActionBuilder
                        .setContinue(Boolean.parseBoolean(responseActions.get(Constants.ResponseAction.CONTINUE.getAction().toLowerCase())));
            }
            if(responseActions.get(Constants.ResponseAction.IGNORE.getAction()) !=null){
                responseActionBuilder = responseActionBuilder.setIgnore(Boolean.parseBoolean(responseActions.get(Constants.ResponseAction.IGNORE.getAction())));
            }
            if(responseActions.get(Constants.ResponseAction.STOP.getAction()) !=null){
                responseActionBuilder = responseActionBuilder.setStop(Boolean.parseBoolean(responseActions.get(Constants.ResponseAction.STOP.getAction())));
            }
            if(responseActions.get(Constants.ResponseAction.JUMP.getAction()) !=null){
                responseActionBuilder = responseActionBuilder.setJump(Integer.parseInt(responseActions.get(Constants.ResponseAction.JUMP.getAction())));
            }
            Responses response = new ResponsesBuilder()
                    .setResponseMessage(resp.getResponseMessage())
                    .setResponseAction(responseActionBuilder.build())
                    .build();
            responseList.add(response);
        }
        return responseList;
    }

    private SequenceGeneratorInput buildSeqGenInput(GenerateSequenceInput input) throws APPCException {

        log.info("Building SequenceGeneratorInput from Yang object GenerateSequenceInput.");
        validateMandatory(input);

        RequestInfo requestInfo = buildRequestInfoForSeqGenInput(input);
        InventoryModel inventoryModel = readInventoryModel(input);

        VnfcDependencyModel dependencyModel = readDependencyModel(input);
        if(dependencyModel!=null){
            validateInventoryModelWithDependencyModel(dependencyModel,inventoryModel);
        }

        SequenceGeneratorInputBuilder builder = new SequenceGeneratorInputBuilder()
                .requestInfo(requestInfo)
                .inventoryModel(inventoryModel)
                .dependendcyModel(dependencyModel);

        builder = buildCapabilitiesForSeqGenInput(input, builder);

        builder = buildTunableParamsForSeqGenInput(input, builder);

        return builder.build();
    }

    private SequenceGeneratorInputBuilder buildTunableParamsForSeqGenInput(GenerateSequenceInput input, SequenceGeneratorInputBuilder builder) {
        log.info("Initializing Tunable Parameters based on YANG object.");
        if(input.getTunableParameters() != null){
            builder = builder.tunableParameter(Constants.RETRY_COUNT,String.valueOf(input.getTunableParameters().getRetryCount()))
                    .tunableParameter(Constants.WAIT_TIME,String.valueOf(input.getTunableParameters().getWaitTime()));
            if(input.getTunableParameters().getStrategy() !=null){
                builder  = builder.tunableParameter(Constants.STRATEGY,input.getTunableParameters().getStrategy().name());
            }
        }
        return builder;
    }

    private SequenceGeneratorInputBuilder buildCapabilitiesForSeqGenInput(GenerateSequenceInput input, SequenceGeneratorInputBuilder builder) {
        log.info("Initializing capabilities based on YANG object.");
        if(input.getCapabilities() !=null){
            if(input.getCapabilities().getVnf()!=null){
                builder = builder.capability("vnf",input.getCapabilities().getVnf());
            }
            if(input.getCapabilities().getVnfc()!=null){
                builder = builder.capability("vnfc",input.getCapabilities().getVnfc());
            }
            if(input.getCapabilities().getVm()!=null){
                builder = builder.capability("vm",input.getCapabilities().getVm());
            }
            if(input.getCapabilities().getVfModule()!=null){
                builder = builder.capability("vf-module",input.getCapabilities().getVfModule());
            }
        }

        return builder;
    }

    private void validateInventoryModelWithDependencyModel(VnfcDependencyModel dependencyModel, InventoryModel inventoryModel) throws APPCException {
        Set<String> dependencyModelVnfcSet = new HashSet<>();
        Set<String> dependencyModelMandatoryVnfcSet = new HashSet<>();
        Set<String> inventoryModelVnfcsSet = new HashSet<>();

        for (Node<Vnfc> node : dependencyModel.getDependencies()) {
            dependencyModelVnfcSet.add(node.getChild().getVnfcType().toLowerCase());
            if (node.getChild().isMandatory()) {
                dependencyModelMandatoryVnfcSet.add(node.getChild().getVnfcType().toLowerCase());
            }
        }

        for (Vnfc vnfc : inventoryModel.getVnf().getVnfcs()) {
            inventoryModelVnfcsSet.add(vnfc.getVnfcType().toLowerCase());
        }

        // if dependency model and inventory model contains same set of VNFCs, validation succeed and hence return
        if (dependencyModelVnfcSet.equals(inventoryModelVnfcsSet)) {
            return;
        }

        if (inventoryModelVnfcsSet.size() >= dependencyModelVnfcSet.size()) {
            Set<String> difference = new HashSet<>(inventoryModelVnfcsSet);
            difference.removeAll(dependencyModelVnfcSet);
            log.error("Dependency model is missing following vnfc type(s): " + difference);
            throw new APPCException("Dependency model is missing following vnfc type(s): " + difference);
        } else {
            Set<String> difference = new HashSet<>(dependencyModelMandatoryVnfcSet);
            difference.removeAll(inventoryModelVnfcsSet);
            if (difference.size() > 0) {
                log.error("Inventory model is missing following mandatory vnfc type(s): " + difference);
                throw new APPCException("VMs missing for the mandatory VNFC : " + difference);
            }
        }
    }

    private RequestInfo buildRequestInfoForSeqGenInput(GenerateSequenceInput input) {
        log.info("Building RequestInfo from Yang object");
        RequestInfoBuilder requestInfobuilder = buildRequestInformation(input);

        if(input.getRequestInfo().getActionIdentifier() !=null){
            requestInfobuilder = buildActionIdentifiers(input, requestInfobuilder);
        }

        return requestInfobuilder.build();
    }

    private RequestInfoBuilder buildActionIdentifiers(GenerateSequenceInput input, RequestInfoBuilder requestInfobuilder) {
        log.info("Initializing actionIdentifier for RequestInfo");
        requestInfobuilder = requestInfobuilder
                .actionIdentifier()
                .vnfId(input.getRequestInfo().getActionIdentifier().getVnfId())
                .vnfcName(input.getRequestInfo().getActionIdentifier().getVnfcName())
                .vServerId(input.getRequestInfo().getActionIdentifier().getVserverId());
        return requestInfobuilder;
    }

    private RequestInfoBuilder buildRequestInformation(GenerateSequenceInput input) {
        log.info("Initializing action, actionLevel and payload for RequestInfo");
        return new RequestInfoBuilder()
                .action(input.getRequestInfo().getAction().name())
                .actionLevel(input.getRequestInfo().getActionLevel().getName().toLowerCase())
                .payload(input.getRequestInfo().getPayload());
    }

    private void validateMandatory(GenerateSequenceInput input) throws APPCException {
        if(input.getRequestInfo() ==null){
            throw new APPCException("Request Info is not present in the request");
        }
        if(input.getRequestInfo().getAction() ==null){
            throw new APPCException("Action is not present in the request");
        }
        if(input.getInventoryInfo() ==null){
            throw new APPCException("inventoryInfo is not provided in the input");
        }
        if (input.getInventoryInfo().getVnfInfo()== null) {
            log.error("vnfInfo is null in the input");
            throw new APPCException("vnfInfo is missing in the input");
        }
        if(input.getInventoryInfo().getVnfInfo().getVm().isEmpty()){
            log.error("Null vm information in input.");
            throw new APPCException("VnfInfo is missing in the input");
        }
        log.info("Mandatory information present in the request.");
    }

    private VnfcDependencyModel readDependencyModel(GenerateSequenceInput input) throws APPCException{
        log.info("Initializing DependencyModel from YANG model.");
        if(input.getDependencyInfo() == null || input.getDependencyInfo().getVnfcs() ==null || input.getDependencyInfo().getVnfcs().isEmpty()){
            log.info("No dependency model information is present for the request.");
            return null;
        }
        List<Vnfcs> vnfcs = input.getDependencyInfo().getVnfcs();
        Set<Node<org.onap.appc.domainmodel.Vnfc>> dependencies = new HashSet<>();
        Set<String> parentVnfcs=new HashSet<>();
        Set<String> allVnfcTypes=new HashSet<>();
        for(Vnfcs vnfcObj:vnfcs){
            org.onap.appc.domainmodel.Vnfc vnfc = new org.onap.appc.domainmodel.Vnfc();
            vnfc.setVnfcType(vnfcObj.getVnfcType());
            allVnfcTypes.add(vnfcObj.getVnfcType());
            vnfc.setResilienceType(vnfcObj.getResilience());
            Node<Vnfc> currentNode = buildVnfcNodeForDependenyInfo(dependencies, vnfcObj, vnfc);
            for(String parentVnfcType:vnfcObj.getParents()){
                parentVnfcs.add(parentVnfcType);
                Node<Vnfc> parentNode = readNode(parentVnfcType,dependencies);
                if(parentNode == null){
                    Vnfc parentVnfc = new Vnfc();
                    parentVnfc.setVnfcType(parentVnfcType);
                    parentNode = new Node<>(parentVnfc);
                    currentNode.addParent(parentVnfc);
                    dependencies.add(parentNode);
                }
                else{
                    currentNode.addParent(parentNode.getChild());
                }
            }
        }
        for(String parent:parentVnfcs){
            if(!allVnfcTypes.contains(parent)){
                throw new APPCException("Dependency model missing vnfc type "+parent);
            }
        }
        return new VnfcDependencyModel(dependencies);
    }

    private Node<Vnfc> buildVnfcNodeForDependenyInfo(Set<Node<Vnfc>> dependencies, Vnfcs vnfcObj, Vnfc vnfc) {
        Node<Vnfc> currentNode = readNode(vnfcObj.getVnfcType(),dependencies);
        if(currentNode == null){
            currentNode = new Node<>(vnfc);
            dependencies.add(currentNode);
        }
        else{
            currentNode.getChild().setResilienceType(vnfcObj.getResilience());
            currentNode.getChild().setMandatory(vnfcObj.isMandatory());
        }
        return currentNode;
    }

    private Node<org.onap.appc.domainmodel.Vnfc> readNode(String vnfcType, Set<Node<org.onap.appc.domainmodel.Vnfc>> dependencies) {
        for(Node<org.onap.appc.domainmodel.Vnfc> node : dependencies){
            if(node.getChild().getVnfcType().equalsIgnoreCase(vnfcType)){
                return node;
            }
        }
        return null;
    }

    private InventoryModel readInventoryModel(GenerateSequenceInput input) throws APPCException {

        log.info("Initializing InventoryModel from Yang input model");
        Vnf vnf = createVnfForInventoryModel(input);
        Map<org.onap.appc.domainmodel.Vnfc,List<Vserver>> map = new HashMap<>();
        buildVserverDetailsForInventoryModel(input, vnf, map);
        for(Map.Entry<org.onap.appc.domainmodel.Vnfc,List<Vserver>> entry:map.entrySet()){
            org.onap.appc.domainmodel.Vnfc vnfc = entry.getKey();
            List<Vserver> vmList = entry.getValue();
            vnfc.addVservers(vmList);
        }
        return new InventoryModel(vnf);
    }

    private void buildVserverDetailsForInventoryModel(GenerateSequenceInput input, Vnf vnf, Map<Vnfc, List<Vserver>> map) throws APPCException {
        if(input.getInventoryInfo().getVnfInfo().getVm().size()<1){
            throw  new APPCException("vnfInfo is missing  in the input");
        }
        for(Vm vm:input.getInventoryInfo().getVnfInfo().getVm()){
            if(StringUtils.isBlank(vm.getVserverId())){
                throw new APPCException("vserver-id not found ");
            }
            Vserver vserver=new Vserver();
            vserver.setId(vm.getVserverId());
            if(!StringUtils.isBlank(vm.getVnfc().getVnfcName()) &&
                !StringUtils.isBlank(vm.getVnfc().getVnfcType())){
                Vnfc vfc = new Vnfc();
                vfc.setVnfcName(vm.getVnfc().getVnfcName());
                vfc.setVnfcType(vm.getVnfc().getVnfcType());
                vserver.setVnfc(vfc);
                List<Vserver> vms = map.get(vfc);
                if(vms ==null){
                    vms = new LinkedList<>();
                    map.put(vfc,vms);
                }
                vms.add(vserver);
            }
            vnf.addVserver(vserver);
         }
    }

    private Vnf createVnfForInventoryModel(GenerateSequenceInput input) {
        log.info("Setting VnfId and VnfType values for Vnf Inventory Model ");
        Vnf vnf=new Vnf();
        vnf.setVnfId(input.getInventoryInfo().getVnfInfo().getVnfId());
        vnf.setVnfType(input.getInventoryInfo().getVnfInfo().getVnfType());
        vnf.setIdentityUrl(input.getInventoryInfo().getVnfInfo().getIdentityUrl());
        return vnf;
    }

    private RpcResult<GenerateSequenceOutput> buildFailureResponse(String errorMessage){
        GenerateSequenceOutputBuilder sequenceGeneratorOutputBuilder=new GenerateSequenceOutputBuilder();
        StatusBuilder statusBuilder =new StatusBuilder();
        statusBuilder.setCode(401);
        statusBuilder.setMessage(errorMessage);
        sequenceGeneratorOutputBuilder.setStatus(statusBuilder.build());
        return RpcResultBuilder
                .<GenerateSequenceOutput> status(true)
                .withResult(sequenceGeneratorOutputBuilder.build())
                .build();
    }
}
