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

package org.openecomp.appc.simulator.client.impl;

import org.openecomp.appc.client.lcm.api.*;
import org.openecomp.appc.client.lcm.exceptions.*;
import org.openecomp.appc.simulator.client.RequestHandler;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;

public class JsonRequestHandler implements RequestHandler {

    private enum modeT {
        SYNCH,
        ASYNCH
    }

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private String rpcName = null;
    private String inputClassName = null;
    private String actionName = null;
    private String methodName = null;
    String packageName = null;
    private LifeCycleManagerStateful service = null;
    private Properties properties;
    HashMap<String, String> exceptRpcMap = null;
    private final EELFLogger LOG = EELFManager.getInstance().getLogger(JsonRequestHandler.class);
    private AppcLifeCycleManagerServiceFactory appcLifeCycleManagerServiceFactory = null;


    public JsonRequestHandler(Properties prop) throws AppcClientException {
        properties = prop;
        packageName = properties.getProperty("ctx.model.package") + ".";
        try {
            service = createService();
        } catch (AppcClientException e) {
            e.printStackTrace();
        }
        exceptRpcMap = prepareExceptionsMap();
    }

    private HashMap<String,String> prepareExceptionsMap() {
        exceptRpcMap = new HashMap<String, String>();

        String line;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(properties.getProperty("client.rpc.exceptions.map.file")));
        } catch (FileNotFoundException e) {
            return exceptRpcMap;
        }

        try {
            while ((line = reader.readLine()) != null)
            {
                String[] parts = line.split(":", 2);
                if (parts.length >= 2)
                {
                    String key = parts[0];
                    String value = parts[1];
                    exceptRpcMap.put(key, value);
                } else {
                    System.out.println("ignoring line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exceptRpcMap;
    }

    @Override
    public void proceedFile(File source, File log) throws IOException {
        final JsonNode inputNode = OBJECT_MAPPER.readTree(source);

        try {
            // proceed with inputNode and get some xxxInput object, depends on action
            prepareNames(inputNode);

            Object input = prepareObject(inputNode);

            JsonResponseHandler response = new JsonResponseHandler();
            response.setFile(source.getPath().toString());
            switch (isSyncMode(inputNode)) {
                case SYNCH: {
                    LOG.debug("Received input request will be processed in synchronously mode");
                    Method rpc = LifeCycleManagerStateful.class.getDeclaredMethod(methodName, input.getClass());
                    response.onResponse(rpc.invoke(service, input));
                    break;
                }
                case ASYNCH: {
                    LOG.debug("Received input request will be processed in asynchronously mode");
                    Method rpc = LifeCycleManagerStateful.class.getDeclaredMethod(methodName, input.getClass(), ResponseHandler.class);
                    rpc.invoke(service, input, response);
                    break;
                }
                default: {
                    throw new RuntimeException("Unrecognized request mode");
                }
            }
        }
        catch(Exception ex){
            //ex.printStackTrace();
        }

        LOG.debug("Action <" + actionName + "> from input file <" + source.getPath().toString() + "> processed");
    }

    private modeT isSyncMode(JsonNode inputNode) {
        // The following solution is for testing purposes only
        // the sync/async decision logic may change upon request
        try {
            int mode = Integer.parseInt(inputNode.findValue("input").findValue("common-header").findValue("sub-request-id").asText());
            if ((mode % 2) == 0) {
                return modeT.SYNCH;
            }
        }catch (Throwable ex) {
            //use ASYNC as default, if value is not integer.
        }
        return modeT.ASYNCH;
    }

    private LifeCycleManagerStateful createService() throws AppcClientException {
        appcLifeCycleManagerServiceFactory = AppcClientServiceFactoryProvider.getFactory(AppcLifeCycleManagerServiceFactory.class);
        return appcLifeCycleManagerServiceFactory.createLifeCycleManagerStateful(new ApplicationContext(), properties);
    }

    public void shutdown(boolean isForceShutdown){
        appcLifeCycleManagerServiceFactory.shutdownLifeCycleManager(isForceShutdown);
    }

    public Object prepareObject(JsonNode input) {
        try {
            Class cls = Class.forName(inputClassName);
            try {
                // since payload is not mandatory field and not all actions contains payload
                // so we have to check that during input parsing
                alignPayload(input);
            } catch (NoSuchFieldException e) {
                LOG.debug("In " + actionName + " no payload defined");
            }

            return OBJECT_MAPPER.treeToValue(input.get("input"), cls);
        }
        catch(Exception ex){
            //ex.printStackTrace();
        }
        return null;
    }

    private void prepareNames(JsonNode input) throws NoSuchFieldException {
        JsonNode inputNode = input.findValue("input");
        actionName = inputNode.findValue("action").asText();
        if (actionName.isEmpty()) {
            throw new NoSuchFieldException("Input doesn't contains field <action>");
        }

        rpcName = prepareRpcFromAction(actionName);
        inputClassName = packageName + actionName + "Input";
        methodName = prepareMethodName(rpcName);
    }

    private void alignPayload(JsonNode input) throws NoSuchFieldException {
        JsonNode inputNode = input.findValue("input");
        JsonNode payload = inputNode.findValue("payload");
        if (payload == null || payload.asText().isEmpty() || payload.toString().isEmpty())
            throw new NoSuchFieldException("Input doesn't contains field <payload>");

        String payloadData = payload.asText();
        if (payloadData.isEmpty())
            payloadData = payload.toString();
        ((ObjectNode)inputNode).put("payload", payloadData);
    }

    private String prepareRpcFromAction(String action) {
        String rpc = checkExceptionalRpcList(action);
        if (rpc!= null && !rpc.isEmpty()) {
            return rpc; // we found exceptional rpc, so no need to format it
        }

        rpc = "";
        boolean makeItLowerCase = true;
        for(int i = 0; i < action.length(); i++)
        {
            if(makeItLowerCase) // first character will make lower case
            {
                rpc+=Character.toLowerCase(action.charAt(i));
                makeItLowerCase = false;
            }
            else  if((i+1 < action.length()) && Character.isUpperCase(action.charAt(i+1)))
            {
                rpc+=action.charAt(i) + "-";
                makeItLowerCase = true;
            }
            else
            {
                rpc+=action.charAt(i);
                makeItLowerCase = false;
            }
        }
        return rpc;
    }

    private String checkExceptionalRpcList(String action) {
        if (exceptRpcMap.isEmpty()) {
            return null;
        }
        return exceptRpcMap.get(action);
    }

    private String prepareMethodName(String inputRpcName) {
        boolean makeItUpperCase = false;
        String method = "";

        for(int i = 0; i < inputRpcName.length(); i++)  //to check the characters of string..
        {
            if(Character.isLowerCase(inputRpcName.charAt(i)) && makeItUpperCase) // skip first character if it lower case
            {
                method+=Character.toUpperCase(inputRpcName.charAt(i));
                makeItUpperCase = false;
            }
            else  if(inputRpcName.charAt(i) == '-')
            {
                makeItUpperCase = true;
            }
            else
            {
                method+=inputRpcName.charAt(i);
                makeItUpperCase = false;
            }
        }
        return method;
    }

}
