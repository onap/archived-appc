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

package org.openecomp.appc.seqgen.provider;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.util.concurrent.Futures;
import org.apache.commons.lang.StringUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.GenerateSequenceInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.GenerateSequenceOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.GenerateSequenceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.SequenceGeneratorService;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.dependency.info.dependency.info.Vnfcs;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.inventory.info.inventory.info.vnf.info.Vm;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.response.StatusBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.response.Transactions;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.response.TransactionsBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.response.transactions.ActionIdentifier;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.response.transactions.*;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.sequencegenerator.rev170706.response.transactions.responses.ResponseActionBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.openecomp.appc.dg.objects.InventoryModel;
import org.openecomp.appc.dg.objects.Node;
import org.openecomp.appc.dg.objects.VnfcDependencyModel;
import org.openecomp.appc.domainmodel.Vnf;
import org.openecomp.appc.domainmodel.Vserver;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.seqgen.SequenceGenerator;
import org.openecomp.appc.seqgen.impl.SequenceGeneratorFactory;
import org.openecomp.appc.seqgen.objects.*;

import java.util.*;
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
            SequenceGenerator seqGenerator = SequenceGeneratorFactory.getInstance()
                    .createSequenceGenerator(VNFOperation.findByString(input.getRequestInfo().getAction().name()));
            SequenceGeneratorInput seqGenInput = buildSeqGenInput(input);
            List<Transaction> transactions = seqGenerator.generateSequence(seqGenInput);
            rpcResult = buildSuccessResponse(transactions);
        } catch (APPCException e) {
            log.error("Error Generating Sequence",e);
            rpcResult = buildFailureResponse(e.getMessage());
        }
        return Futures.immediateFuture(rpcResult);
    }

    private RpcResult<GenerateSequenceOutput> buildSuccessResponse(List<Transaction> transactions) {

        List<Transactions> transactionList = new LinkedList<>();
        for(Transaction transaction:transactions){
            ActionIdentifier actionIdentifier = null;
            if(transaction.getActionIdentifier() != null){
                actionIdentifier = new ActionIdentifierBuilder()
                        .setVnfId(transaction.getActionIdentifier().getVnfId())
                        .setVnfcName(transaction.getActionIdentifier().getVnfcName())
                        .setVserverId(transaction.getActionIdentifier().getvServerId())
                        .build();
            }

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
            Responses response = new ResponsesBuilder()
                    .setResponseMessage(resp.getResponseMessage())
                    .setResponseAction(responseActionBuilder.build())
                    .build();
            responseList.add(response);
        }
        return responseList;
    }

    private SequenceGeneratorInput buildSeqGenInput(GenerateSequenceInput input) throws APPCException {

        validateMandatory(input);

        RequestInfoBuilder requestInfobuilder = new RequestInfoBuilder()
                .action(input.getRequestInfo().getAction().name())
                .actionLevel(input.getRequestInfo().getActionLevel().getName().toLowerCase())
                .payload(input.getRequestInfo().getPayload());

        if(input.getRequestInfo().getActionIdentifier() !=null){
            requestInfobuilder = requestInfobuilder
                    .actionIdentifier()
                    .vnfId(input.getRequestInfo().getActionIdentifier().getVnfId())
                    .vnfcName(input.getRequestInfo().getActionIdentifier().getVnfcName())
                    .vServerId(input.getRequestInfo().getActionIdentifier().getVserverId());
        }

        RequestInfo requestInfo = requestInfobuilder.build();

        InventoryModel inventoryModel = readInventoryModel(input);

        VnfcDependencyModel dependencyModel = readDependencyModel(input);

        SequenceGeneratorInputBuilder builder = new SequenceGeneratorInputBuilder()
                .requestInfo(requestInfo)
                .inventoryModel(inventoryModel)
                .dependendcyModel(dependencyModel);

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

        if(input.getTunableParameters() != null){
            builder = builder.tunableParameter(Constants.RETRY_COUNT,String.valueOf(input.getTunableParameters().getRetryCount()))
                    .tunableParameter(Constants.WAIT_TIME,String.valueOf(input.getTunableParameters().getWaitTime()));
            if(input.getTunableParameters().getStrategy() !=null){
                builder  = builder.tunableParameter(Constants.STRATEGY,input.getTunableParameters().getStrategy().name());
            }
        }
        return builder.build();
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
    }

    private VnfcDependencyModel readDependencyModel(GenerateSequenceInput input) {
        if(input.getDependencyInfo() == null || input.getDependencyInfo().getVnfcs() ==null || input.getDependencyInfo().getVnfcs().isEmpty()){
            return null;
        }
        List<Vnfcs> vnfcs = input.getDependencyInfo().getVnfcs();
        Set<Node<org.openecomp.appc.domainmodel.Vnfc>> dependencies = new HashSet<>();
        for(Vnfcs vnfcObj:vnfcs){
            org.openecomp.appc.domainmodel.Vnfc vnfc;
            Node<org.openecomp.appc.domainmodel.Vnfc> currentNode = readNode(vnfcObj.getVnfcType(),dependencies);
            if(currentNode == null){
                vnfc = new org.openecomp.appc.domainmodel.Vnfc(vnfcObj.getVnfcType(),vnfcObj.getResilience());
                currentNode = new Node<>(vnfc);
                dependencies.add(currentNode);
            }
            else{
                currentNode.getChild().setResilienceType(vnfcObj.getResilience());
                currentNode.getChild().setMandatory(vnfcObj.isMandatory());
            }
            for(String parentVnfcType:vnfcObj.getParents()){
                Node<org.openecomp.appc.domainmodel.Vnfc> parentNode = readNode(parentVnfcType,dependencies);
                if(parentNode == null){
                    org.openecomp.appc.domainmodel.Vnfc parentVnfc = new org.openecomp.appc.domainmodel.Vnfc(parentVnfcType,null);
                    parentNode = new Node<>(parentVnfc);
                    currentNode.addParent(parentVnfc);
                    dependencies.add(parentNode);
                }
                else{
                    currentNode.addParent(parentNode.getChild());
                }
            }
        }
        return new VnfcDependencyModel(dependencies);
    }

    private Node<org.openecomp.appc.domainmodel.Vnfc> readNode(String vnfcType, Set<Node<org.openecomp.appc.domainmodel.Vnfc>> dependencies) {
        for(Node<org.openecomp.appc.domainmodel.Vnfc> node : dependencies){
            if(node.getChild().getVnfcType().equalsIgnoreCase(vnfcType)){
                return node;
            }
        }
        return null;
    }

    private InventoryModel readInventoryModel(GenerateSequenceInput input) throws APPCException {
        if (input.getInventoryInfo().getVnfInfo()== null) {
            throw new APPCException("vnfInfo is not provided in the input");
        }

        Vnf vnf = new Vnf(input.getInventoryInfo().getVnfInfo().getVnfId(),
                input.getInventoryInfo().getVnfInfo().getVnfType(),null);

        Map<org.openecomp.appc.domainmodel.Vnfc,List<Vserver>> map = new HashMap<>();
        for(Vm vm:input.getInventoryInfo().getVnfInfo().getVm()){
            if(StringUtils.isBlank(vm.getVserverId())){
                throw new APPCException("vserver-id not found ");
            }
            if(StringUtils.isBlank(vm.getVnfc().getVnfcType())){
                throw new APPCException("vnfc-type not found for vserver " + vm.getVserverId());
            }
            if(StringUtils.isBlank(vm.getVnfc().getVnfcName())){
                throw new APPCException("vnfc-name not found for vserver " + vm.getVserverId());
            }

            org.openecomp.appc.domainmodel.Vnfc vnfc = new org.openecomp.appc.domainmodel.Vnfc(vm.getVnfc().getVnfcType(),null,vm.getVnfc().getVnfcName());
            List<Vserver> vms = map.get(vnfc);
            if(vms ==null){
                vms = new LinkedList<>();
                map.put(vnfc,vms);
            }
            vms.add(new Vserver(null,null,vm.getVserverId(),null,null));
        }
        for(Map.Entry<org.openecomp.appc.domainmodel.Vnfc,List<Vserver>> entry:map.entrySet()){
            org.openecomp.appc.domainmodel.Vnfc vnfc = entry.getKey();
            List<Vserver> vmList = entry.getValue();
            vnfc.addVms(vmList);
            vnf.addVnfc(vnfc);
        }
        return new InventoryModel(vnf);
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
