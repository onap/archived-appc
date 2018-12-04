/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 Ericsson
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

package org.onap.appc.instar.node;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.aai.interfaceImpl.AaiInterfaceRulesHandler;
import org.onap.appc.aai.utils.AaiClientConstant;
import org.onap.appc.instar.interfaceImpl.InstarRestClientImpl;
import org.onap.appc.instar.interfaceImpl.InterfaceIpAddressImpl;
import org.onap.appc.instar.interfaces.RestClientInterface;
import org.onap.appc.instar.interfaces.RuleHandlerInterface;
import org.onap.appc.instar.utils.InstarClientConstant;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.sdnc.config.params.data.Parameter;

public class InstarClientNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(InstarClientNode.class);


    public void getInstarInfo(Map<String, String> inParams, SvcLogicContext ctx)
        throws SvcLogicException {
        log.info("Received getInstarInfo call with params : " + inParams);
        String responsePrefix = inParams.get(InstarClientConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? responsePrefix + "." : "";
            String[] instarKeys = getKeys(inParams.get(InstarClientConstant.INSTAR_KEYS));
            for (String instarKey : instarKeys) {
                log.info("Processing Key : " + instarKey);
                log.info("Searching key for  : " + "INSTAR." + instarKey);
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                log.info("Received Context : " + ctx.getAttribute("INSTAR." + instarKey));
                Parameter params = mapper
                    .readValue(ctx.getAttribute(InstarClientConstant.SOURCE_SYSTEM_INSTAR + "." + instarKey),
                        Parameter.class);
                RuleHandlerInterface handler;
                log.info("Processing rule Type : " + params.getRuleType());
                if (params.getRuleType().equals(InstarClientConstant.INTERFACE_IP_ADDRESS)) {
                    handler = createHandler(params, ctx);
                } else {
                    throw new SvcLogicException("No Rule Defined to process :" + params.getRuleType());
                }
                handler.processRule();
            }
            log.info("responsePrefix =" + responsePrefix);
            log.info("instar key values =" + ctx.getAttribute(InstarClientConstant.INSTAR_KEY_VALUES));
            ctx.setAttribute(responsePrefix + InstarClientConstant.INSTAR_KEY_VALUES,
                ctx.getAttribute(InstarClientConstant.INSTAR_KEY_VALUES));
            ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_STATUS,
                InstarClientConstant.OUTPUT_STATUS_SUCCESS);
            log.info(ctx.getAttribute("TEST." + InstarClientConstant.OUTPUT_PARAM_STATUS));
            ctx.setAttribute(InstarClientConstant.INSTAR_KEY_VALUES, null);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_STATUS,
                InstarClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed processing Instar request", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    private static String[] getKeys(String keyString) {
        log.error("Received Key String as :" + keyString);
        String key = keyString
            .replace("[", "")
            .replace("]", "")
            .replace("\"", "");
        if (key.contains(",")) {
            return key.split(",");
        } else {
            return new String[]{key};
        }
    }

    public void getInstarData(Map<String, String> inParams, SvcLogicContext ctx)
        throws SvcLogicException {
        log.info("Received getInstarData call with params : " + inParams);
        String responsePrefix = inParams.get(InstarClientConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            HashMap<String, String> input = new HashMap<>();
            input.putAll(inParams);
            RestClientInterface rcINterface = createRestClientInterface(input);
            String response = rcINterface.sendRequest(inParams.get("operationName"));
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? responsePrefix + "." : "";
            ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_STATUS,
                InstarClientConstant.OUTPUT_STATUS_SUCCESS);
            ctx.setAttribute(responsePrefix + InstarClientConstant.INSTAR_KEY_VALUES, response);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_STATUS,
                InstarClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed processing Instar request", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    public void getAaiInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.info("Received getAaiInfo call with params : " + inParams);
        String responsePrefix = inParams.get(AaiClientConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? responsePrefix + "." : "";
            String[] aaiKeys = getKeys(inParams.get(AaiClientConstant.AAI_KEYS));
            for (String aaiKey : aaiKeys) {
                log.info("Processing Key : " + aaiKey);
                log.info("Searching key for  : " + "AAI." + aaiKey);
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                log.info("Received Context : " + ctx.getAttribute("AAI." + aaiKey));
                Parameter params = mapper.readValue(
                    ctx.getAttribute(AaiClientConstant.SOURCE_SYSTEM_AAI + "." + aaiKey), Parameter.class);
                log.info("Processing rule Type : " + params.getRuleType());
                RuleHandlerInterface handler = new AaiInterfaceRulesHandler(params, ctx);
                handler.processRule();
            }
            log.info("responsePrefix =" + responsePrefix);
            ctx.setAttribute(responsePrefix + AaiClientConstant.AAI_KEY_VALUES,
                ctx.getAttribute(AaiClientConstant.AAI_KEY_VALUES));
            ctx.setAttribute(responsePrefix + AaiClientConstant.OUTPUT_PARAM_STATUS,
                AaiClientConstant.OUTPUT_STATUS_SUCCESS);
            ctx.setAttribute(AaiClientConstant.AAI_KEY_VALUES, null);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AaiClientConstant.OUTPUT_PARAM_STATUS,
                InstarClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed processing AAI data", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    protected RuleHandlerInterface createHandler(Parameter params, SvcLogicContext ctx) {
        return new InterfaceIpAddressImpl(params, ctx);
    }

    protected RestClientInterface createRestClientInterface(Map<String, String> input) {
        return new InstarRestClientImpl(input);
    }
}
