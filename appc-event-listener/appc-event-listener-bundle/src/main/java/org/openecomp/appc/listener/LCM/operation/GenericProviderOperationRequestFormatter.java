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

package org.openecomp.appc.listener.LCM.operation;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.listener.LCM.model.InputBody;
import org.openecomp.appc.listener.LCM.model.ResponseStatus;
import org.openecomp.appc.listener.util.Mapper;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.net.URL;



public class GenericProviderOperationRequestFormatter implements ProviderOperationRequestFormatter {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(GenericProviderOperationRequestFormatter.class);

    //@formatter:off
    @SuppressWarnings("nls")
    private final static String TEMPLATE = "{\"input\": %s}";
    //@formatter:on

    @Override
    public String buildPath(URL url, String rpcName) {
        return url.getPath() + ":"+rpcName;
    }

//    private String convertActionToUrl(String action) {
//        String res = "";
//        switch (action) {
//            case "SoftwareUpload":
//                res = "software-upload";
//                break;
//            case "LiveUpgrade":
//                res = "live-upgrade";
//                break;
//            case "HealthCheck":
//                res = "health-check";
//                break;
//            case "CheckLock":
//                res = "check-lock";
//                break;
//            default :
//                res = action;
//        }
//        return res.toLowerCase();
//    }

    @Override
    public String buildRequest(InputBody msg) {
        JSONObject jsonObject = Mapper.toJsonObject(msg);
        return String.format(TEMPLATE, jsonObject.toString());
    }

/*    @Override
    public OperationStatus getOperationStatus(JSONObject responseBody) throws APPCException {
        try {
            JSONObject status = responseBody.getJSONObject("output").getJSONObject("status");
            return new OperationStatus(String.valueOf(status.getInt("code")), status.getString("message"));
        } catch (Exception e) {
            LOG.error("Unknown error processing failed response from provider. Json not in expected format", e);
            throw new APPCException("APPC has an unknown RPC error");
        }
    }*/

    @Override
    public ResponseStatus getResponseStatus(JsonNode responseBody)throws APPCException{
        try {
            JsonNode status = responseBody.get("output").get("status");
            return new ResponseStatus(status.get("code").asInt(), status.get("message").asText());
        } catch (Exception e) {
            LOG.error("Unknown error processing failed response from provider. Json not in expected format", e);
            throw new APPCException("APPC has an unknown RPC error");
        }
    }

    @Override
    public String getLocked(JSONObject responseBody) throws APPCException {
        try {
            JSONObject outputObject=responseBody.getJSONObject("output");
            if(outputObject.has("locked")){
                return outputObject.getString("locked");
            }else{
                return null;
            }
        } catch (Exception e) {
            LOG.error("Unknown error processing failed response from provider. Json not in expected format", e);
            throw new APPCException("APPC has an unknown RPC error");
        }
    }
}
