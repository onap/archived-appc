/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.dg.util.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.openecomp.appc.dg.util.InputParameterValidation;
import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;



public class InputParameterValidationImpl implements InputParameterValidation
{
    private static final char NL = '\n';
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(InputParameterValidationImpl.class);

    public InputParameterValidationImpl() {
    }


    @SuppressWarnings("nls")
    @Override
    public void validateAttribute(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Map<String, String> contextParams=getValueFromContext(ctx);
        boolean isSuccess = true;
        try {
            for (String k : params.keySet()) {
                logger.info("validating attribute  " + k);
                if (!contextParams.containsKey(k)) {
                    logger.info("missing attribute  " + k);
                    isSuccess =false;
                }
                if(contextParams.get(k)==null){
                    logger.info("mandatory attribute " + k+ "is null");
                    isSuccess =false;
                }
            }
        }catch (NullPointerException np) {
            isSuccess =false;
        }
        ctx.setAttribute("validateAttribute", String.valueOf(isSuccess));
    }

    @SuppressWarnings("nls")
    @Override
    public void validateAttributeLength(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Map<String, String> contextParams=getValueFromContext(ctx);
        boolean isSuccess =true;
        try {
            int maxLength = Integer.parseInt(params.get("maximum_length_param"));
            params.remove("maximum_length_param");

            for (String k : params.keySet()) {
                logger.info("validating attribute  " + k);
                if(contextParams.get(k).length() > maxLength){
                    logger.info("attribute " + k+ "'s length is exceeding Maximum limit of " + maxLength +" character");
                    isSuccess=false;
                }
            }
        }catch (NullPointerException np) {
            isSuccess=false;
        }
        ctx.setAttribute("validateAttributeLength", String.valueOf(isSuccess));
    }

    @SuppressWarnings("nls")
    @Override
    public void validateAttributeCharacter(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Map<String, String> contextParams=getValueFromContext(ctx);
        boolean isSuccess =true;
        try {
            String specialCharacter = params.get("special_characters");
            String pattern = ".*[" + Pattern.quote(specialCharacter) + "].*";
            params.remove("special_characters");

            for (String k : params.keySet()) {
                logger.info("validating attribute  " + k);
                if(contextParams.get(k).matches(pattern)){
                    logger.info("attribute " + k + " contains any of these " + specialCharacter + " special character ");
                    isSuccess =false;
                }

            }
        }catch (NullPointerException np) {
            isSuccess =false;
        }
        ctx.setAttribute("validateAttributeCharacter", String.valueOf(isSuccess));
    }


    private Map<String, String> getValueFromContext(SvcLogicContext context) {
        Set<String> keys = context.getAttributeKeySet();
        Map<String, String> params = new HashMap<String, String>();
        StringBuilder builder = new StringBuilder();
        if (keys != null && !keys.isEmpty()) {
            builder.append(NL);
            for (String key : keys) {
                String value = context.getAttribute(key);
                params.put(key,value);

            }
        }
        return params;

    }


}
