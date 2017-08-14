/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.design.services.impl;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.DbserviceInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.DbserviceOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.DbserviceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.DesignServicesService;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.ValidatorInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.ValidatorOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.ValidatorOutputBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.XinterfaceserviceInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.XinterfaceserviceOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.XinterfaceserviceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.data.DataBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.status.StatusBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.openecomp.appc.design.dbervices.DbResponseProcessor;
import org.openecomp.appc.design.dbervices.DesignDBService;
import org.openecomp.appc.design.services.util.DesignServiceConstants;
import org.openecomp.appc.design.validator.ValidatorResponseProcessor;
import org.openecomp.appc.design.validator.ValidatorService;
import org.openecomp.appc.design.xinterface.XInterfaceService;
import org.openecomp.appc.design.xinterface.XResponseProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;

public class DesignServicesImpl implements DesignServicesService {

    private static final Logger log = LoggerFactory.getLogger(DesignServicesImpl.class);

    @Override
    public Future<RpcResult<DbserviceOutput>> dbservice(DbserviceInput input) {

        log.info("Received Request: " + input.getDesignRequest().getRequestId() + " Action : " + 
                input.getDesignRequest().getAction() + " with Payload :" + input.getDesignRequest().getPayload());
        
        
        DbserviceOutputBuilder outputBuilder = new DbserviceOutputBuilder();
        DataBuilder databuilder = new DataBuilder();
        StatusBuilder statusBuilder = new StatusBuilder();    

        try{
            DesignDBService dbservices = DesignDBService.initialise();
            DbResponseProcessor responseProcessor = new DbResponseProcessor();    
            String response = responseProcessor.parseResponse(dbservices.execute(input.getDesignRequest().getAction(), input.getDesignRequest().getPayload(), input.getDesignRequest().getRequestId()), input.getDesignRequest().getAction());
            log.info("Response in for Design Service : " + response);
            databuilder.setBlock(response);
            databuilder.setRequestId(input.getDesignRequest().getRequestId());
            statusBuilder.setCode("400");
            statusBuilder.setMessage("success");            
        }
        catch(Exception e){    
            log.error("Error" + e.getMessage());
            e.printStackTrace();
            statusBuilder.setCode("401");
            statusBuilder.setMessage(e.getMessage());            
        }

        outputBuilder.setData(databuilder.build());
        outputBuilder.setStatus(statusBuilder.build());

        RpcResult<DbserviceOutput> result  = RpcResultBuilder.<DbserviceOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<XinterfaceserviceOutput>> xinterfaceservice(XinterfaceserviceInput input) {
        log.info("Received Request: " + input.getDesignRequest().getRequestId() + " Action : " + 
                input.getDesignRequest().getAction() + " with Payload :" + input.getDesignRequest().getPayload());
        XinterfaceserviceOutputBuilder outputBuilder = new XinterfaceserviceOutputBuilder();
        DataBuilder databuilder = new DataBuilder();
        StatusBuilder statusBuilder = new StatusBuilder();            
        try {

            XInterfaceService xInterfaceService = new XInterfaceService();    
            XResponseProcessor responseProcessor = new XResponseProcessor();        
            String response = responseProcessor.parseResponse(xInterfaceService.execute(input.getDesignRequest().getAction(), input.getDesignRequest().getPayload()), input.getDesignRequest().getAction());
            databuilder.setBlock(response);
            databuilder.setRequestId(input.getDesignRequest().getRequestId());
            statusBuilder.setCode("400");
            statusBuilder.setMessage("success");            
        } catch (Exception e) {
            e.printStackTrace();
            statusBuilder.setCode("401");
            statusBuilder.setMessage(e.getMessage());        
        }        
        outputBuilder.setData(databuilder.build());
        outputBuilder.setStatus(statusBuilder.build());

        RpcResult<XinterfaceserviceOutput> result  = RpcResultBuilder.<XinterfaceserviceOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<ValidatorOutput>> validator(ValidatorInput input) {
        log.info("Received Request: " + input.getDesignRequest().getRequestId() + " Action : " + 
                input.getDesignRequest().getAction() + " with Payload :" + input.getDesignRequest().getPayload() +  " and Data Type = " + input.getDesignRequest().getDataType());        
        ValidatorOutputBuilder outputBuilder = new ValidatorOutputBuilder();
        StatusBuilder statusBuilder = new StatusBuilder();        
        
        
        try {
            if(input.getDesignRequest().getDataType() == null || input.getDesignRequest().getDataType().isEmpty())                     
                     throw new Exception ("Data Type required for validate Serivce");
            if(input.getDesignRequest().getAction()== null || input.getDesignRequest().getAction().isEmpty())                     
                 throw new Exception ("Action required for validate Serivce");
            
            
            if(! input.getDesignRequest().getDataType().equals(DesignServiceConstants.DATA_TYPE_JSON) && 
                    ! input.getDesignRequest().getDataType().equals(DesignServiceConstants.DATA_TYPE_YAML) &&
                    ! input.getDesignRequest().getDataType().equals(DesignServiceConstants.DATA_TYPE_XML) &&
                    ! input.getDesignRequest().getDataType().equals(DesignServiceConstants.DATA_TYPE_VELOCITY))
                throw new Exception ("Request Data format " + input.getDesignRequest().getDataType() 
                        + " is not supported by validate Service : Supported data types are : XML, YAML, VELOCITY, JSON ");
                        
            ValidatorService validatorService = new ValidatorService();    
            ValidatorResponseProcessor responseProcessor = new ValidatorResponseProcessor();        
            String response = validatorService.execute(input.getDesignRequest().getAction(), input.getDesignRequest().getPayload(), input.getDesignRequest().getDataType());
            statusBuilder.setCode("400");
            statusBuilder.setMessage(response);            
        } catch (Exception e) {
            e.printStackTrace();
            statusBuilder.setCode("401");
            statusBuilder.setMessage(e.getMessage());        
        }        

        outputBuilder.setStatus(statusBuilder.build());

        RpcResult<ValidatorOutput> result  = RpcResultBuilder.<ValidatorOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }
}
