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

package org.onap.appc.design.services.impl;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;
import org.onap.appc.design.dbervices.DbResponseProcessor;
import org.onap.appc.design.dbervices.DesignDBService;
import org.onap.appc.design.services.util.DesignServiceConstants;
import org.onap.appc.design.validator.ValidatorService;
import org.onap.appc.design.xinterface.XInterfaceService;
import org.onap.appc.design.xinterface.XResponseProcessor;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.DbserviceInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.DbserviceOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.DbserviceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.DesignServicesService;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.ValidatorInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.ValidatorOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.ValidatorOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.XinterfaceserviceInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.XinterfaceserviceOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.XinterfaceserviceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.data.DataBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.status.StatusBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesignServicesImpl implements DesignServicesService {

    private static final Logger log = LoggerFactory.getLogger(DesignServicesImpl.class);
    private static final String RECEIVED_REQUEST_STR = "Received Request: ";
    private static final String WITH_PAYLOAD_STR = " with Payload :";
    private static final String ACTION_STR = " Action : ";

    @Override
    public Future<RpcResult<DbserviceOutput>> dbservice(DbserviceInput input) {

        log.info(RECEIVED_REQUEST_STR + input.getDesignRequest().getRequestId() + ACTION_STR +
            input.getDesignRequest().getAction() + WITH_PAYLOAD_STR + input.getDesignRequest().getPayload());

        DbserviceOutputBuilder outputBuilder = new DbserviceOutputBuilder();
        DataBuilder databuilder = new DataBuilder();
        StatusBuilder statusBuilder = new StatusBuilder();

        try {
            DesignDBService dbservices = DesignDBService.initialise();
            DbResponseProcessor responseProcessor = new DbResponseProcessor();
            String response = responseProcessor.parseResponse(dbservices
                .execute(input.getDesignRequest().getAction(), input.getDesignRequest().getPayload(),
                    input.getDesignRequest().getRequestId()), input.getDesignRequest().getAction());
            log.info("Response in for Design Service : " + response);
            databuilder.setBlock(response);
            databuilder.setRequestId(input.getDesignRequest().getRequestId());
            statusBuilder.setCode("400");
            statusBuilder.setMessage("success");
        } catch (Exception e) {
            log.error("Error", e);
            statusBuilder.setCode("401");
            statusBuilder.setMessage(e.getMessage());
        }

        outputBuilder.setData(databuilder.build());
        outputBuilder.setStatus(statusBuilder.build());

        RpcResult<DbserviceOutput> result = RpcResultBuilder.<DbserviceOutput>status(true)
            .withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<XinterfaceserviceOutput>> xinterfaceservice(XinterfaceserviceInput input) {
        log.info(RECEIVED_REQUEST_STR + input.getDesignRequest().getRequestId() + ACTION_STR +
            input.getDesignRequest().getAction() + WITH_PAYLOAD_STR + input.getDesignRequest().getPayload());
        XinterfaceserviceOutputBuilder outputBuilder = new XinterfaceserviceOutputBuilder();
        DataBuilder databuilder = new DataBuilder();
        StatusBuilder statusBuilder = new StatusBuilder();
        try {

            XInterfaceService xInterfaceService = new XInterfaceService();
            XResponseProcessor responseProcessor = new XResponseProcessor();
            String response = responseProcessor.parseResponse(
                xInterfaceService.execute(input.getDesignRequest().getAction(), input.getDesignRequest().getPayload()),
                input.getDesignRequest().getAction());
            databuilder.setBlock(response);
            databuilder.setRequestId(input.getDesignRequest().getRequestId());
            statusBuilder.setCode("400");
            statusBuilder.setMessage("success");
        } catch (Exception e) {
            log.error("An error occurred in xInterfaceService", e);
            statusBuilder.setCode("401");
            statusBuilder.setMessage(e.getMessage());
        }
        outputBuilder.setData(databuilder.build());
        outputBuilder.setStatus(statusBuilder.build());

        RpcResult<XinterfaceserviceOutput> result = RpcResultBuilder.<XinterfaceserviceOutput>status(true)
            .withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<ValidatorOutput>> validator(ValidatorInput input) {
        log.info(RECEIVED_REQUEST_STR + input.getDesignRequest().getRequestId() + ACTION_STR +
            input.getDesignRequest().getAction() + WITH_PAYLOAD_STR + input.getDesignRequest().getPayload()
            + " and Data Type = " + input.getDesignRequest().getDataType());
        ValidatorOutputBuilder outputBuilder = new ValidatorOutputBuilder();
        StatusBuilder statusBuilder = new StatusBuilder();

        build(input, statusBuilder);

        outputBuilder.setStatus(statusBuilder.build());

        RpcResult<ValidatorOutput> result = RpcResultBuilder.<ValidatorOutput>status(true)
            .withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    private void build(ValidatorInput input, StatusBuilder statusBuilder) {
        try {
            if (input.getDesignRequest().getDataType() == null || input.getDesignRequest().getDataType().isEmpty()) {
                throw new RequestValidationException("Data Type required for validate Serivce");
            }
            if (input.getDesignRequest().getAction() == null || input.getDesignRequest().getAction().isEmpty()) {
                throw new RequestValidationException("Action required for validate Serivce");
            }

            if (validateInput(input)) {
                throw new RequestValidationException("Request Data format " + input.getDesignRequest().getDataType()
                    + " is not supported by validate Service : Supported data types are : XML, YAML, VELOCITY, JSON ");
            }

            ValidatorService validatorService = new ValidatorService();
            String response = validatorService
                .execute(input.getDesignRequest().getAction(), input.getDesignRequest().getPayload(),
                    input.getDesignRequest().getDataType());
            statusBuilder.setCode("400");
            statusBuilder.setMessage(response);
        } catch (Exception e) {
            log.error("An error occurred in validator", e);
            statusBuilder.setCode("401");
            statusBuilder.setMessage(e.getMessage());
        }
    }

    private boolean validateInput(ValidatorInput input) {
        return !input.getDesignRequest().getDataType().equals(DesignServiceConstants.DATA_TYPE_JSON) &&
            !input.getDesignRequest().getDataType().equals(DesignServiceConstants.DATA_TYPE_YAML) &&
            !input.getDesignRequest().getDataType().equals(DesignServiceConstants.DATA_TYPE_XML) &&
            !input.getDesignRequest().getDataType().equals(DesignServiceConstants.DATA_TYPE_VELOCITY);
    }
}
