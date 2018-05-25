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

package org.onap.appc.simulator.client.impl;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;
import org.onap.appc.client.lcm.api.AppcClientServiceFactoryProvider;
import org.onap.appc.client.lcm.api.AppcLifeCycleManagerServiceFactory;
import org.onap.appc.client.lcm.api.ApplicationContext;
import org.onap.appc.client.lcm.api.LifeCycleManagerStateful;
import org.onap.appc.client.lcm.api.ResponseHandler;
import org.onap.appc.client.lcm.exceptions.AppcClientException;
import org.onap.appc.simulator.client.RequestHandler;

public class JsonRequestHandler implements RequestHandler {


    private final EELFLogger logger = EELFManager.getInstance().getLogger(JsonRequestHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String INPUT_PARAM = "input";

    private String inputClassName = null;
    private String actionName = null;
    private String methodName = null;
    private String packageName = null;
    private LifeCycleManagerStateful service = null;
    private Properties properties;
    private HashMap<String, String> exceptRpcMap = null;

    private AppcLifeCycleManagerServiceFactory appcLifeCycleManagerServiceFactory = null;

    public JsonRequestHandler() {/*default constructor*/}

    public JsonRequestHandler(Properties prop) throws AppcClientException {
        properties = prop;
        packageName = properties.getProperty("ctx.model.package") + ".";
        try {
            service = createService();
        } catch (AppcClientException e) {
            logger.error("An error occurred while instantiating JsonRequestHandler", e);
        }
        exceptRpcMap = prepareExceptionsMap();
    }

    private HashMap<String, String> prepareExceptionsMap() {
        exceptRpcMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
            new FileReader(properties.getProperty(
                "client.rpc.exceptions.map.file")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length >= 2) {
                    String key = parts[0];
                    String value = parts[1];
                    exceptRpcMap.put(key, value);
                } else {
                    logger.info("ignoring line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Map file not found", e);
            return exceptRpcMap;
        } catch (IOException e) {
            logger.error("An error occurred while preparing exceptions map", e);
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
            response.setFile(source.getPath());
            switch (isSyncMode(inputNode)) {
                case SYNCH:
                    processSync(input, response);
                    break;
                case ASYNCH:
                    processAsync(input, response);
                    break;
                default:
                    throw new InvalidRequestException("Unrecognized request mode");
            }
        } catch (Exception e) {
            logger.error("An error occurred when proceeding file", e);
        }

        logger.debug("Action <" + actionName + "> from input file <" + source.getPath() + "> processed");
    }

    private void processAsync(Object input, JsonResponseHandler response)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        logger.debug("Received input request will be processed in asynchronously mode");
        Method rpc = LifeCycleManagerStateful.class
            .getDeclaredMethod(methodName, input.getClass(), ResponseHandler.class);
        rpc.invoke(service, input, response);
    }

    private void processSync(Object input, JsonResponseHandler response)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        logger.debug("Received input request will be processed in synchronously mode");
        Method rpc = LifeCycleManagerStateful.class.getDeclaredMethod(methodName, input.getClass());
        response.onResponse(rpc.invoke(service, input));
    }

    private modeT isSyncMode(JsonNode inputNode) {
        // The following solution is for testing purposes only
        // the sync/async decision logic may change upon request
        try {
            int mode = Integer
                .parseInt(
                    inputNode.findValue(INPUT_PARAM).findValue("common-header").findValue("sub-request-id").asText());
            if ((mode % 2) == 0) {
                return modeT.SYNCH;
            }
        } catch (Exception e) {
            logger.error("Failed to parse sub-request-id", e);
            //use ASYNC as default, if value is not integer.
        }
        return modeT.ASYNCH;
    }

    private LifeCycleManagerStateful createService() throws AppcClientException {
        appcLifeCycleManagerServiceFactory = AppcClientServiceFactoryProvider
            .getFactory(AppcLifeCycleManagerServiceFactory.class);
        return appcLifeCycleManagerServiceFactory.createLifeCycleManagerStateful(new ApplicationContext(), properties);
    }

    @Override
    public void shutdown(boolean isForceShutdown) {
        appcLifeCycleManagerServiceFactory.shutdownLifeCycleManager(isForceShutdown);
    }

    public Object prepareObject(JsonNode input) {
        try {
            Class cls = Class.forName(inputClassName);
            tryAlignPayload(input);
            return OBJECT_MAPPER.treeToValue(input.get(INPUT_PARAM), cls);
        } catch (Exception ex) {
            logger.error("Failed to prepare object", ex);
        }
        return null;
    }

    private void tryAlignPayload(JsonNode input) {
        try {
            // since payload is not mandatory field and not all actions contains payload
            // so we have to check that during input parsing
            alignPayload(input);
        } catch (NoSuchFieldException e) {
            logger.debug("In " + actionName + " no payload defined", e);
        }
    }

    private void prepareNames(JsonNode input) throws NoSuchFieldException {
        JsonNode inputNode = input.findValue(INPUT_PARAM);
        actionName = inputNode.findValue("action").asText();
        if (actionName.isEmpty()) {
            throw new NoSuchFieldException("Input doesn't contains field <action>");
        }
        inputClassName = packageName + actionName + "Input";
        methodName = prepareMethodName(prepareRpcFromAction(actionName));
    }

    private void alignPayload(JsonNode input) throws NoSuchFieldException {
        JsonNode inputNode = input.findValue(INPUT_PARAM);
        JsonNode payload = inputNode.findValue("payload");
        if (payload == null || payload.asText().isEmpty() || payload.toString().isEmpty()) {
            throw new NoSuchFieldException("Input doesn't contains field <payload>");
        }

        String payloadData = payload.asText();
        if (payloadData.isEmpty()) {
            payloadData = payload.toString();
        }
        ((ObjectNode) inputNode).put("payload", payloadData);
    }

    private String prepareRpcFromAction(String action) {
        String exRpc = checkExceptionalRpcList(action);
        if (exRpc != null && !exRpc.isEmpty()) {
            return exRpc; // we found exceptional rpc, so no need to format it
        }

        StringBuilder rpc = new StringBuilder();
        boolean makeItLowerCase = true;
        for (int i = 0; i < action.length(); i++) {
            if (makeItLowerCase) // first character will make lower case
            {
                rpc.append(toLowerCase(action.charAt(i)));
                makeItLowerCase = false;
            } else if ((i + 1 < action.length()) && Character.isUpperCase(action.charAt(i + 1))) {
                rpc.append(action.charAt(i)).append('-');
                makeItLowerCase = true;
            } else {
                rpc.append(action.charAt(i));
                makeItLowerCase = false;
            }
        }
        return rpc.toString();
    }

    private String checkExceptionalRpcList(String action) {
        if (exceptRpcMap.isEmpty()) {
            return null;
        }
        return exceptRpcMap.get(action);
    }

    private String prepareMethodName(String inputRpcName) {
        boolean makeItUpperCase = false;
        StringBuilder method = new StringBuilder();

        for (int i = 0; i < inputRpcName.length(); i++)  //to check the characters of string..
        {
            if (Character.isLowerCase(inputRpcName.charAt(i))
                && makeItUpperCase) // skip first character if it lower case
            {
                method.append(toUpperCase(inputRpcName.charAt(i)));
                makeItUpperCase = false;
            } else if (inputRpcName.charAt(i) == '-') {
                makeItUpperCase = true;
            } else {
                method.append(inputRpcName.charAt(i));
                makeItUpperCase = false;
            }
        }
        return method.toString();
    }

    private enum modeT {
        SYNCH,
        ASYNCH
    }
}
