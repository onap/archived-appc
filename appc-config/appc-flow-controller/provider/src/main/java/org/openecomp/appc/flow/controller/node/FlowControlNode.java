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

package org.openecomp.appc.flow.controller.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.flow.controller.ResponseHandlerImpl.DefaultResponseHandler;
import org.openecomp.appc.flow.controller.data.PrecheckOption;
import org.openecomp.appc.flow.controller.data.ResponseAction;
import org.openecomp.appc.flow.controller.data.Transaction;
import org.openecomp.appc.flow.controller.data.Transactions;
import org.openecomp.appc.flow.controller.dbervices.FlowControlDBService;
import org.openecomp.appc.flow.controller.executorImpl.GraphExecutor;
import org.openecomp.appc.flow.controller.executorImpl.NodeExecutor;
import org.openecomp.appc.flow.controller.executorImpl.RestExecutor;
import org.openecomp.appc.flow.controller.interfaceData.ActionIdentifier;
import org.openecomp.appc.flow.controller.interfaceData.Capabilities;
import org.openecomp.appc.flow.controller.interfaceData.DependencyInfo;
import org.openecomp.appc.flow.controller.interfaceData.Input;
import org.openecomp.appc.flow.controller.interfaceData.InventoryInfo;
import org.openecomp.appc.flow.controller.interfaceData.RequestInfo;
import org.openecomp.appc.flow.controller.interfaceData.Vm;
import org.openecomp.appc.flow.controller.interfaceData.VnfInfo;
import org.openecomp.appc.flow.controller.interfaceData.Vnfcs;
import org.openecomp.appc.flow.controller.interfaceData.Vnfcslist;
import org.openecomp.appc.flow.controller.interfaces.FlowExecutorInterface;
import org.openecomp.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class FlowControlNode implements SvcLogicJavaPlugin{


    private static final  EELFLogger log = EELFManager.getInstance().getLogger(FlowControlNode.class);
    private static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";

    public void processFlow(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.debug("Received processParamKeys call with params : " + inParams);
        String responsePrefix = inParams.get(FlowControllerConstants.INPUT_PARAM_RESPONSE_PRIFIX);        
        try
        {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            FlowControlDBService dbservice = FlowControlDBService.initialise();
            SvcLogicContext  localContext = new SvcLogicContext();
            localContext.setAttribute(FlowControllerConstants.REQUEST_ID, ctx.getAttribute(FlowControllerConstants.REQUEST_ID));
            localContext.setAttribute(FlowControllerConstants.VNF_TYPE, ctx.getAttribute(FlowControllerConstants.VNF_TYPE));
            localContext.setAttribute(FlowControllerConstants.REQUEST_ACTION, ctx.getAttribute(FlowControllerConstants.REQUEST_ACTION));
            localContext.setAttribute(FlowControllerConstants.ACTION_LEVEL, ctx.getAttribute(FlowControllerConstants.ACTION_LEVEL));    
            localContext.setAttribute(FlowControllerConstants.RESPONSE_PREFIX, responsePrefix);
            ctx.setAttribute(FlowControllerConstants.RESPONSE_PREFIX, responsePrefix);
            dbservice.getFlowReferenceData(ctx, inParams, localContext);

            for (Object key : localContext.getAttributeKeySet()) {
                String parmName = (String) key;
                String parmValue = ctx.getAttribute(parmName);
                log.debug("processFlow " + parmName +  "="  + parmValue);

            }
            processFlowSequence(inParams, ctx, localContext);        
            if(!ctx.getAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_STATUS).equals(FlowControllerConstants.OUTPUT_STATUS_SUCCESS))
                throw new SvcLogicException(ctx.getAttribute(responsePrefix +  FlowControllerConstants.OUTPUT_STATUS_MESSAGE));
            
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_STATUS, FlowControllerConstants.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            e.printStackTrace();
            throw new SvcLogicException(e.getMessage());
        }
    }

    private void processFlowSequence( Map<String, String> inParams, SvcLogicContext ctx, SvcLogicContext localContext) throws Exception 
    {
        String fn = "FlowExecutorNode.processflowSequence";
        log.debug(fn + "Received model for flow : " + localContext.toString());    
        FlowControlDBService dbservice = FlowControlDBService.initialise();
        String flowSequnce  =null;
        for (Object key : localContext.getAttributeKeySet()) {
            String parmName = (String) key;
            String parmValue = ctx.getAttribute(parmName);
            log.debug(parmName +  "="  + parmValue);

        }
        if(localContext != null && localContext.getAttribute(FlowControllerConstants.SEQUENCE_TYPE) !=null){

            if(localContext.getAttribute(FlowControllerConstants.GENERATION_NODE) != null){
                GraphExecutor transactionExecutor = new GraphExecutor();
                Boolean generatorExists = transactionExecutor.hasGraph("APPC_COMMOM", localContext.getAttribute(FlowControllerConstants.GENERATION_NODE), null, "sync");
                if(generatorExists){                    
                    flowSequnce = transactionExecutor.executeGraph("APPC_COMMOM", localContext.getAttribute(FlowControllerConstants.GENERATION_NODE),
                            null, "sync", null).getProperty(FlowControllerConstants.FLOW_SEQUENCE);
                }
                else
                    throw new Exception("Can not find Custom defined Flow Generator for " + localContext.getAttribute(FlowControllerConstants.GENERATION_NODE));                
            }            
            else if(((String) localContext.getAttribute(FlowControllerConstants.SEQUENCE_TYPE)).equalsIgnoreCase(FlowControllerConstants.DESINGTIME)){
                localContext.setAttribute(FlowControllerConstants.VNFC_TYPE, ctx.getAttribute(FlowControllerConstants.VNFC_TYPE));
                flowSequnce = dbservice.getDesignTimeFlowModel(localContext);
                if(flowSequnce == null)
                    throw new Exception("Flow Sequence is not found User Desinged VNF " + ctx.getAttribute(FlowControllerConstants.VNF_TYPE));
            }
            else if(((String) localContext.getAttribute(FlowControllerConstants.SEQUENCE_TYPE)).equalsIgnoreCase(FlowControllerConstants.RUNTIME)){
                
                Transaction transaction = new Transaction();
                String input = collectInputParams(ctx,transaction);
                log.info("collectInputParamsData" + input );
                
                RestExecutor restExe = new RestExecutor();    
                HashMap<String,String>flowSeq= restExe.execute(transaction, localContext);
                flowSequnce=flowSeq.get("restResponse");
                
                if(flowSequnce == null)
                    throw new Exception("Failed to get the Flow Sequece runtime for VNF type" + ctx.getAttribute(FlowControllerConstants.VNF_TYPE));

            }
            else if(((String) localContext.getAttribute(FlowControllerConstants.SEQUENCE_TYPE)).equalsIgnoreCase(FlowControllerConstants.EXTERNAL)){
                //String input = collectInputParams(localContext);
                //    flowSequnce = ""; //get it from the External interface calling the Rest End point - TBD
                if(flowSequnce == null)
                    throw new Exception("Flow Sequence not found for " + ctx.getAttribute(FlowControllerConstants.VNF_TYPE));                
            }
            else
            {
                //No other type of model supported...in Future can get flowModel from other generators which will be included here
                throw new Exception("No information found for sequence Owner Design-Time Vs Run-Time" );
            }
        }
        else{
            FlowGenerator flowGenerator = new FlowGenerator();
            Transactions trans = flowGenerator.createSingleStepModel(inParams,ctx);
            ObjectMapper mapper = new ObjectMapper();    
            flowSequnce = mapper.writeValueAsString(trans);
            log.debug("Single step Flow Sequence : " +  flowSequnce);

        }
        log.debug("Received Flow Sequence : " +  flowSequnce);

        HashMap<Integer, Transaction> transactionMap = createTransactionMap(flowSequnce, localContext);
        exeuteAllTransaction(transactionMap, ctx); 
        log.info("Executed all the transacstion successfully");

    }

    private void exeuteAllTransaction(HashMap<Integer, Transaction> transactionMap, SvcLogicContext ctx) throws Exception {

        String fn = "FlowExecutorNode.exeuteAllTransaction ";
        int retry = 0;
        FlowExecutorInterface flowExecutor = null;
        for (int key = 1; key <= transactionMap.size() ; key++ ) 
        {            
            log.debug(fn + "Starting transactions ID " + key + " :)=" + retry);
            Transaction transaction = transactionMap.get(key);
            if(!preProcessor(transactionMap, transaction)){
                log.info("Skipping Transaction ID " +  transaction.getTransactionId());
                continue;
            }            
            if(transaction.getExecutionType() != null){
                switch (transaction.getExecutionType()){
                case FlowControllerConstants.GRAPH :
                    flowExecutor = new GraphExecutor();
                    break;
                case FlowControllerConstants.NODE :
                    flowExecutor =  new NodeExecutor();
                    break;
                case FlowControllerConstants.REST :
                    flowExecutor = new RestExecutor();
                    break;
                default :
                    throw new Exception("No Executor found for transaction ID" + transaction.getTransactionId());
                }
                flowExecutor.execute(transaction, ctx);
                ResponseAction responseAction= handleResponse(transaction);

                if(responseAction.getWait() != null && Integer.parseInt(responseAction.getWait()) > 0){
                    log.debug(fn + "Going to Sleep .... " + responseAction.getWait());
                    Thread.sleep(Integer.parseInt(responseAction.getWait())*1000);
                }

                if(responseAction.isIntermediateMessage()){
                    log.debug(fn + "Sending Intermediate Message back  .... ");
                    sendIntermediateMessage();
                }
                if(responseAction.getRetry() != null && Integer.parseInt(responseAction.getRetry()) > retry ){                
                    log.debug(fn + "Ooppss!!! We will retry again ....... ");
                        key--; 
                        retry++;
                    log.debug(fn + "key =" +  key +  "retry =" + retry);

                }
                if(responseAction.isIgnore()){
                    log.debug(fn + "Ignoring this Error and moving ahead  ....... ");
                    continue;
                }
                if(responseAction.isStop()){
                    log.debug(fn + "Need to Stop  ....... ");
                    break;
                }
                if(responseAction.getJump() != null && Integer.parseInt(responseAction.getJump()) > 0 ){
                    key = Integer.parseInt(responseAction.getJump());
                    key --;        
                }
                log.debug(fn + "key =" +  key +  "retry =" + retry);

            }
            else{
                throw new Exception("Don't know how to execute transaction ID " + transaction.getTransactionId());
            }
        }

    }
    private void sendIntermediateMessage() {
        // TODO Auto-generated method stub

    }

    private ResponseAction handleResponse(Transaction transaction) {        
        log.info("Handling Response for transaction Id " + transaction.getTransactionId());
        DefaultResponseHandler defaultHandler = new DefaultResponseHandler();         
        return defaultHandler.handlerResponse(transaction);
    }

    private boolean preProcessor(HashMap<Integer, Transaction> transactionMap, Transaction transaction) throws IOException {

        log.debug("Starting Preprocessing Logic ");
        boolean runthisStep = false;
        try{
            if(transaction.getPrecheck() != null && transaction.getPrecheck().getPrecheckOptions() != null 
                    && !transaction.getPrecheck().getPrecheckOptions().isEmpty()){
                List<PrecheckOption> precheckOptions  = transaction.getPrecheck().getPrecheckOptions();
                for(PrecheckOption precheck : precheckOptions){
                    Transaction trans = transactionMap.get(precheck.getpTransactionID());
                    ObjectMapper mapper = new ObjectMapper();
                    log.info("Mapper= " + mapper.writeValueAsString(trans));
                    HashMap<Object, Object> trmap = mapper.readValue(mapper.writeValueAsString(trans), HashMap.class);
                    if(trmap.get(precheck.getParamName()) != null && 
                            ((String) trmap.get(precheck.getParamName())).equalsIgnoreCase(precheck.getParamValue()))
                        runthisStep = true;
                    else
                        runthisStep = false;

                    if(transaction.getPrecheck().getPrecheckOperator() != null && 
                            transaction.getPrecheck().getPrecheckOperator().equalsIgnoreCase("any") && runthisStep)
                        break;                        
                }
            }

            else{
                log.debug("No Pre check defined for transaction ID " + transaction.getTransactionId());
                runthisStep = true;

            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
        log.debug("Returing process current Transaction = " + runthisStep);

        return runthisStep ;
    }

    private HashMap<Integer, Transaction> createTransactionMap(String flowSequnce, SvcLogicContext localContext) throws SvcLogicException, JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();    
        Transactions transactions = mapper.readValue(flowSequnce,Transactions.class);
        HashMap<Integer, Transaction> transMap = new HashMap<Integer, Transaction>();    
        for(Transaction transaction : transactions.getTransactions()){        
            compileFlowDependencies(transaction, localContext);
            //loadTransactionIntoStatus(transactions, ctx); //parse the Transactions Object and create records in process_flow_status table 
            transMap.put(transaction.getTransactionId(), transaction);            
        }        
        return transMap;
    }

    private void compileFlowDependencies(Transaction transaction, SvcLogicContext localContext) throws SvcLogicException, JsonParseException, JsonMappingException, IOException {

        String fn = "FlowExecutorNode.compileFlowDependencies";
        FlowControlDBService dbservice = FlowControlDBService.initialise();
        dbservice.populateModuleAndRPC(transaction, localContext.getAttribute(FlowControllerConstants.VNF_TYPE));
        ObjectMapper mapper = new ObjectMapper();
        log.debug("Indivisual Transaction Details :" + transaction.toString());
        if((localContext.getAttribute(FlowControllerConstants.SEQUENCE_TYPE) == null) ||
                ( localContext.getAttribute(FlowControllerConstants.SEQUENCE_TYPE) != null && 
                ! localContext.getAttribute(FlowControllerConstants.SEQUENCE_TYPE).equalsIgnoreCase(FlowControllerConstants.DESINGTIME))){
            localContext.setAttribute("artifact-content", mapper.writeValueAsString(transaction));
            dbservice.loadSequenceIntoDB(localContext);
        }
        //get a field in transction class as transactionhandle interface and register the Handler here for each trnactions
    }
    
    private String collectInputParams(SvcLogicContext ctx, Transaction transaction) throws Exception {

        String fn = "FlowExecuteNode.collectInputParams";
        Properties prop = loadProperties();
        log.info("Loaded Properties " + prop.toString());

        String vnfId = ctx.getAttribute(FlowControllerConstants.VNF_ID);
        log.debug(fn + "vnfId :" + vnfId);

        if (StringUtils.isBlank(vnfId)) {
            throw new Exception("VnfId is missing");
        }

        ActionIdentifier actionIdentifier = new ActionIdentifier();
        actionIdentifier.setVnfId(vnfId);
        actionIdentifier.setVserverId(ctx.getAttribute(FlowControllerConstants.VSERVER_ID));
        actionIdentifier.setVnfcName(ctx.getAttribute(FlowControllerConstants.VNFC_NAME));

        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAction(ctx.getAttribute(FlowControllerConstants.ACTION));
        requestInfo.setActionLevel(ctx.getAttribute(FlowControllerConstants.ACTION_LEVEL));
        requestInfo.setPayload(ctx.getAttribute(FlowControllerConstants.PAYLOAD));
        requestInfo.setActionIdentifier(actionIdentifier);
        
        InventoryInfo inventoryInfo = getInventoryInfo(ctx,vnfId);
        DependencyInfo dependencyInfo = getDependencyInfo(ctx);
        Capabilities capabilites = getCapabilitesData(ctx);
        
        Input input = new Input();
        input.setRequestInfo(requestInfo);
        input.setInventoryInfo(inventoryInfo);
        input.setDependencyInfo(dependencyInfo);
        input.setCapabilities(capabilites);
        //input.setTunableParameters(null);
        

        log.info(fn + "Input parameters:" + input.toString());

        String inputData = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(Include.NON_NULL);
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE,true);
            inputData = mapper.writeValueAsString(input);
            log.info("InputDataJson:"+inputData);

        } catch (Exception e) {
            e.printStackTrace();
        }

        String resourceUri = prop.getProperty(FlowControllerConstants.SEQ_GENERATOR_URL);
        log.info(fn + "resourceUri= " + resourceUri);

        transaction.setPayload(inputData);
        transaction.setExecutionRPC("POST");
        transaction.setuId(prop.getProperty(FlowControllerConstants.SEQ_GENERATOR_UID));
        transaction.setPswd(prop.getProperty(FlowControllerConstants.SEQ_GENERATOR_PWD));
        transaction.setExecutionEndPoint(resourceUri);

        return inputData;

    }
    
    private DependencyInfo getDependencyInfo(SvcLogicContext ctx) throws Exception {
        
        String fn = "FlowExecutorNode.getDependencyInfo";
        DependencyInfo dependencyInfo = new DependencyInfo();
        FlowControlDBService dbservice = FlowControlDBService.initialise();
        String dependencyData = dbservice.getDependencyInfo(ctx);
        log.info(fn + "dependencyDataInput:" + dependencyData);

        if (dependencyData != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            JsonNode dependencyInfoData = mapper.readTree(dependencyData).get("dependencyInfo");
            JsonNode vnfcData = mapper.readTree(dependencyInfoData.toString()).get("vnfcs");
            List<Vnfcs> vnfclist = Arrays.asList(mapper.readValue(vnfcData.toString(), Vnfcs[].class));
            dependencyInfo.getVnfcs().addAll(vnfclist);
            
            log.info("Dependency Output:"+ dependencyInfo.toString());
        }

        return dependencyInfo;

    }
    
    private Capabilities getCapabilitesData(SvcLogicContext ctx)throws Exception {

        String fn = "FlowExecutorNode.getCapabilitesData";
        Capabilities capabilities = new Capabilities();
        FlowControlDBService dbservice = FlowControlDBService.initialise();
        String capabilitiesData = dbservice.getCapabilitiesData(ctx);
        log.info(fn + "capabilitiesDataInput:" + capabilitiesData);

        if (capabilitiesData != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            JsonNode capabilitiesNode = mapper.readValue(capabilitiesData,JsonNode.class);
            log.info("capabilitiesNode:" + capabilitiesNode.toString());

            JsonNode vnfs = capabilitiesNode.findValue(FlowControllerConstants.VNF);
            List<String> vnfsList = new ArrayList<String>();
            if (vnfs != null) {
                for (int i = 0; i < vnfs.size(); i++) {

                    String vnf = vnfs.get(i).asText();
                    vnfsList.add(vnf);
                }
            }

            JsonNode vfModules = capabilitiesNode.findValue(FlowControllerConstants.VF_MODULE);
            List<String> vfModulesList = new ArrayList<String>();
            if (vfModules != null) {
                for (int i = 0; i < vfModules.size(); i++) {

                    String vfModule = vfModules.get(i).asText();
                    vfModulesList.add(vfModule);
                }
            }

            JsonNode vnfcs = capabilitiesNode.findValue(FlowControllerConstants.VNFC);
            List<String> vnfcsList = new ArrayList<String>();
            if (vnfcs != null) {
                for (int i = 0; i < vnfcs.size(); i++) {

                    String vnfc1 = vnfcs.get(i).asText();
                    vnfcsList.add(vnfc1);
                }
            }

            JsonNode vms = capabilitiesNode.findValue(FlowControllerConstants.VM);

            List<String> vmList = new ArrayList<String>();
            if (vms != null) {
                for (int i = 0; i < vms.size(); i++) {

                    String vm1 = vms.get(i).asText();
                    vmList.add(vm1);
                }
            }

            capabilities.getVnfc().addAll(vnfcsList);
            capabilities.getVnf().addAll(vnfsList);
            capabilities.getVfModule().addAll(vfModulesList);
            capabilities.getVm().addAll(vmList);
            
            log.info("Capabilities Output:"+ capabilities.toString());

        }

        return capabilities;

    }
    
    private InventoryInfo getInventoryInfo(SvcLogicContext ctx, String vnfId) throws Exception{
        
        String fn = "FlowExecutorNode.getInventoryInfo";
        String vmcount = ctx.getAttribute("tmp.vnfInfo.vm-count");
        int vmCount = Integer.parseInt(vmcount);
        log.info(fn +"vmcount:"+ vmCount);

        VnfInfo vnfInfo = new VnfInfo();
        vnfInfo.setVnfId(vnfId);
        vnfInfo.setVnfName(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-name"));
        vnfInfo.setVnfType(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type"));

        Vm vm = new Vm();
        Vnfcslist vnfc = new Vnfcslist();

        if (vmCount > 0) {

            for (int i = 0; i < vmCount; i++) {

                vm.setVserverId(ctx.getAttribute("tmp.vnfInfo.vm[" + i + "].vserverId"));
                String vnfccount = ctx.getAttribute("tmp.vnfInfo.vm[" + i + "].vnfc-count");
                int vnfcCount = Integer.parseInt(vnfccount);

                if (vnfcCount > 0) {
                    vnfc.setVnfcName(ctx.getAttribute("tmp.vnfInfo.vm[" + i    + "].vnfc-name"));
                    vnfc.setVnfcType(ctx.getAttribute("tmp.vnfInfo.vm[" + i    + "].vnfc-type"));
                    vm.setVnfc(vnfc);
                }
                vnfInfo.getVm().add(vm);
            }
        }

        InventoryInfo inventoryInfo = new InventoryInfo();
        inventoryInfo.setVnfInfo(vnfInfo);
        
        return inventoryInfo;
        
    }
        
    private String getFlowSequence() throws IOException {

        String sequenceModel = IOUtils.toString(FlowControlNode.class.getClassLoader().getResourceAsStream("sequence.json"), Charset.defaultCharset());

        return null;
    }

    
private static Properties loadProperties() throws Exception {
    Properties props = new Properties();
    String propDir = System.getenv(SDNC_CONFIG_DIR_VAR);
    if (propDir == null)
        throw new Exception("Cannot find Property file -" + SDNC_CONFIG_DIR_VAR);
    String propFile = propDir + FlowControllerConstants.APPC_FLOW_CONTROLLER;
    InputStream propStream = new FileInputStream(propFile);        
    try
    {
        props.load(propStream);
    }
    catch (Exception e)
    {
        throw new Exception("Could not load properties file " + propFile, e);
    }
    finally
    {
        try
        {
            propStream.close();
        }
        catch (Exception e)
        {
            log.warn("Could not close FileInputStream", e);
        }
    }
    return props;
}

}
