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

package org.openecomp.appc.flow.controller.executorImpl;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.openecomp.appc.flow.controller.data.Parameters;
import org.openecomp.appc.flow.controller.data.Transaction;
import org.openecomp.appc.flow.controller.interfaces.FlowExecutorInterface;
import org.openecomp.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


public class GraphExecutor implements FlowExecutorInterface {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(GraphExecutor.class);

    private SvcLogicService svcLogic = null;

    public GraphExecutor() {
        BundleContext bctx = FrameworkUtil.getBundle(SvcLogicService.class)
                .getBundleContext();
        
        ServiceReference sref = bctx.getServiceReference(SvcLogicService.NAME);
        if (sref != null) {
            svcLogic = (SvcLogicService) bctx.getService(sref);
            

        } else {
            log.warn("Cannot find service reference for "
                    + SvcLogicService.NAME);
        }
        log.debug("Graph Executor Initialized successfully");
    }

    public boolean hasGraph(String module, String rpc, String version,
            String mode) throws SvcLogicException {
        return (svcLogic.hasGraph(module, rpc, version, mode));
    }

    public Properties executeGraph(String module, String rpc, String version,
            String mode,  Properties parms) throws SvcLogicException {
        log.debug("Parameters passed to SLI");

//        for (Object key : parms.keySet()) {
//            String parmName = (String) key;
//            String parmValue = parms.getProperty(parmName);
//
//            log.debug(parmName + " = " + parmValue);
//        }

    Properties respProps = svcLogic.execute(module, rpc, version, mode,parms);
    if (log.isDebugEnabled()) {
        log.debug("Parameters returned by SLI");
        for (Object key : respProps.keySet()) {
            String parmName = (String) key;
            String parmValue = respProps.getProperty(parmName);

            log.debug(parmName + " = " + parmValue);
        }
    }    
    if ("failure"
            .equalsIgnoreCase(respProps.getProperty("SvcLogic.status"))) {
        return (respProps);
    }
    return (respProps);
}

@Override
public HashMap<String, String> execute(Transaction transaction, SvcLogicContext ctx) throws Exception {

    String fn = "GraphExecutor.execute ";
    log.debug(fn + "About to execute graph : " + transaction.getExecutionRPC())    ;
    
    Properties parms = new Properties();
    for (Object key : ctx.getAttributeKeySet()) {
            String parmName = (String) key;
            String parmValue = ctx.getAttribute(parmName);
            parms.put(parmName, parmValue);
            log.info(fn + "Setting Key= "  + parmName + "and Value = " +  parmValue);
            
    }
    Properties returnParams = executeGraph(transaction.getExecutionModule(),transaction.getExecutionRPC(), null, "sync",  parms);
    
    //log.debug("Return Params executing DG :"  + returnParams.toString());

    log.debug("Returned Params from DG Module: " + transaction.getExecutionModule() + "and DG NAME: "  + transaction.getExecutionRPC()
     + returnParams.toString());

    Enumeration e = returnParams.propertyNames();

    while (e.hasMoreElements()) {
         String key = (String) e.nextElement();
         log.info("NEW KEY =  " + key + " -- " + returnParams.getProperty(key));

            ctx.setAttribute(key, returnParams.getProperty(key));
    }
    

    //Get the correct code from the SVC Logic and set it in transaction  
//    transaction.setStatusCode(returnParams.getProperty("SvcLogic.code"));
    
    if (FlowControllerConstants.FAILURE
            .equalsIgnoreCase(returnParams.getProperty("SvcLogic.status")))    {
        transaction.setStatus(FlowControllerConstants.FAILURE);
        ctx.setAttribute(ctx.getAttribute(FlowControllerConstants.RESPONSE_PREFIX) + FlowControllerConstants.OUTPUT_PARAM_STATUS, FlowControllerConstants.OUTPUT_STATUS_FAILURE);
        ctx.setAttribute(ctx.getAttribute(FlowControllerConstants.RESPONSE_PREFIX) + FlowControllerConstants.OUTPUT_STATUS_MESSAGE, returnParams.getProperty("error-message"));
        transaction.setStatusCode("401");
        transaction.setState((ctx.getAttribute(transaction.getExecutionModule() + "." + transaction.getExecutionRPC() + "." + FlowControllerConstants.OUTPUT_STATUS_MESSAGE)) !=null ? 
                ctx.getAttribute(transaction.getExecutionModule() + "." + transaction.getExecutionRPC() + "." + FlowControllerConstants.OUTPUT_STATUS_MESSAGE): null);
        //Get error code from above instead setting here ...its for testing purpose
        
        
    }
    else if(FlowControllerConstants.SUCCESS
            .equalsIgnoreCase(returnParams.getProperty("SvcLogic.status")))    {        
        transaction.setStatus(FlowControllerConstants.SUCCESS);
        transaction.setStatusCode("400");
        ctx.setAttribute(ctx.getAttribute(FlowControllerConstants.RESPONSE_PREFIX) + FlowControllerConstants.OUTPUT_PARAM_STATUS, FlowControllerConstants.OUTPUT_STATUS_SUCCESS);
        transaction.setState((ctx.getAttribute(transaction.getExecutionModule() + "." + transaction.getExecutionRPC() + "." + FlowControllerConstants.OUTPUT_STATUS_MESSAGE)) !=null ? 
                ctx.getAttribute(transaction.getExecutionModule() + "." + transaction.getExecutionRPC() + "." + FlowControllerConstants.OUTPUT_STATUS_MESSAGE): null);
        //Get error code from above instead setting here ...its for testing purpose
    }
    else {        
        transaction.setStatus(FlowControllerConstants.OTHERS);
        ctx.setAttribute(ctx.getAttribute(FlowControllerConstants.RESPONSE_PREFIX) + FlowControllerConstants.OUTPUT_PARAM_STATUS, FlowControllerConstants.OUTPUT_STATUS_FAILURE);
        transaction.setStatusCode("401");
        ctx.setAttribute(ctx.getAttribute(FlowControllerConstants.RESPONSE_PREFIX) + FlowControllerConstants.OUTPUT_STATUS_MESSAGE, returnParams.getProperty("error-message"));
    }
    
    return null;
    //Change null to required value if required in upper level
}
}
