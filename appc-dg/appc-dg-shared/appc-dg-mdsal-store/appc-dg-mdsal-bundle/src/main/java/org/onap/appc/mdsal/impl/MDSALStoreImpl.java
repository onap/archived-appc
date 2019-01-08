/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.mdsal.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.opendaylight.yang.gen.v1.org.onap.appc.mdsal.store.rev170925.StoreYangInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.mdsal.store.rev170925.StoreYangInputBuilder;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.mdsal.MDSALStore;
import org.onap.appc.mdsal.exception.MDSALStoreException;
import org.onap.appc.mdsal.objects.BundleInfo;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.mdsal.operation.ConfigOperationRequestFormatter;
import org.onap.appc.rest.client.RestClientInvoker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Implementation of MDSALStore
 */
public class MDSALStoreImpl implements MDSALStore {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(MDSALStoreImpl.class);
    private RestClientInvoker client;
    private ConfigOperationRequestFormatter requestFormatter = new ConfigOperationRequestFormatter();
    private ObjectMapper mapper = new ObjectMapper();
    private Map<String, RestClientInvoker> remoteClientMap = new HashMap<>();

    MDSALStoreImpl() {
        String configUrl = null;
        String user = null;
        String password = null;
        Configuration configuration = ConfigurationFactory.getConfiguration();
        Properties properties = configuration.getProperties();
        if (properties != null) {
            configUrl = properties.getProperty(Constants.CONFIG_URL_PROPERTY, Constants.CONFIG_URL_DEFAULT);
            user = properties.getProperty(Constants.CONFIG_USER_PROPERTY);
            password = properties.getProperty(Constants.CONFIG_PASS_PROPERTY);
        }
        if (configUrl != null) {
            try {
                client = getRestClientInvoker(new URL(configUrl));
                client.setAuthentication(user, password);
            } catch (MalformedURLException e) {
                logger.error("Error initializing RestConf client: " + e.getMessage(), e);
            }
        }
    }


    @Override
    public boolean isModulePresent(String moduleName, Date revision) {

        if (logger.isDebugEnabled()) {
            logger.debug("isModulePresent invoked with moduleName = " + moduleName + " , revision = " + revision);
        }

        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        /*
         * SchemaContext interface of ODL provides APIs for querying details of yang modules
         * loaded into MD-SAL store, but its limitation is, it only returns information about
         * static yang modules loaded on server start up, it does not return information about
         * the yang modules loaded dynamically. Due to this limitation, we are checking the
         * presence of OSGI bundle instead of yang module. (Note: Assuming OSGI bundle is named
         * with the yang module name).
         */

        Bundle bundle = bundleContext.getBundle(moduleName);
        if (logger.isDebugEnabled()) {
            logger.debug("isModulePresent returned = " + (bundle != null));
        }
        return bundle != null;
    }

    @Override
    public void storeYangModule(String yang, BundleInfo bundleInfo) throws MDSALStoreException {

        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        byte[] byteArray = createBundleJar(yang, Constants.BLUEPRINT, bundleInfo);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray)) {
            Bundle bundle = bundleContext.installBundle(bundleInfo.getLocation(), inputStream);
            bundle.start();
        } catch (Exception e) {
            logger.error(String.format("Error storing yang module: %s. Error message: %s.", yang, e.getMessage()));
            throw new MDSALStoreException("Error storing yang module: " + yang + " " + e.getMessage(), e);
        }
    }

    @Override
    public void storeYangModuleOnLeader(String yang, String moduleName) throws MDSALStoreException {
        try {
            String leader = getLeaderNode();
            if (Constants.SELF.equals(leader)){
                logger.debug("Current node is a leader.");
            }else{
                logger.debug("Attempting to load yang module on Leader: " + leader );
                String inputJson = createInputJson(yang, moduleName);
                RestClientInvoker remoteClient = getRemoteClient(leader);
                HttpResponse response = remoteClient.doPost(Constants.YANG_LOADER_PATH, inputJson);
                int httpCode = response.getStatusLine().getStatusCode();
                String respBody = IOUtils.toString(response.getEntity().getContent());
                if (httpCode < 200 || httpCode >= 300) {
                    logger.debug("Error while loading yang module on leader. Response code: " + httpCode);
                    processRestconfResponse(respBody);
                } else {
                    logger.debug("Yang module successfully loaded on leader. Response code: " + httpCode);
                }
            }
        } catch (APPCException e) {
            logger.error("Error loading Yang on Leader. Error message: " + e.getMessage());
            throw new MDSALStoreException("Error loading Yang on Leader. Error message: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Error reading response from remote client. Error message: " + e.getMessage());
            throw new MDSALStoreException("Error reading response from remote client. Error message: " + e.getMessage(), e);
        }
    }

    private String createInputJson(String yang, String moduleName)  throws MDSALStoreException {
        StoreYangInputBuilder builder = new StoreYangInputBuilder();
        builder.setYang(yang).setModuleName(moduleName);
        StoreYangInput input = builder.build();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.addMixInAnnotations(StoreYangInput.class, MixIn.class);
            String inputJson = objectMapper.writer().withRootName("input").writeValueAsString(input);
            logger.debug("Input JSON :" + inputJson);
            return inputJson;
        } catch (JsonProcessingException e) {
            logger.error(String.format("Error creating JSON input using yang: %s. Error message: %s",yang ,e.getMessage()));
            throw new MDSALStoreException(String.format("Error creating JSON input using yang: %s. Error message: %s",yang ,e.getMessage()), e);
        }
    }

    private RestClientInvoker getRemoteClient(String leader) throws MDSALStoreException {
        if (remoteClientMap.containsKey(leader)) {
            return remoteClientMap.get(leader);
        } else {
            Configuration configuration = ConfigurationFactory.getConfiguration();
            Properties properties = configuration.getProperties();
            if (properties != null) {
                try {
                    URL configUrl = new URL(properties.getProperty(Constants.CONFIG_URL_PROPERTY, Constants.CONFIG_URL_DEFAULT));
                    String user = properties.getProperty(Constants.CONFIG_USER_PROPERTY);
                    String password = properties.getProperty(Constants.CONFIG_PASS_PROPERTY);
                    RestClientInvoker remoteClient = getRestClientInvoker(new URL(configUrl.getProtocol(), leader, configUrl.getPort(), ""));
                    remoteClient.setAuthentication(user, password);
                    remoteClientMap.put(leader, remoteClient);
                    return remoteClient;
                } catch (MalformedURLException e) {
                    logger.error("Error initializing remote RestConf client: " + e.getMessage(), e);
                    throw new MDSALStoreException("Error initializing Remote RestConf client: " + e.getMessage(), e);
                }
            } else {
                logger.error("Error initializing Remote RestConf client. Could not read appc properties");
                throw new MDSALStoreException("Error initializing Remote RestConf client. Could not read appc properties");
            }
        }
    }

    abstract class MixIn {
        @JsonIgnore
        abstract Class<? extends DataContainer> getImplementedInterface(); // to be removed during serialization

        @JsonValue
        abstract java.lang.String getValue();

        @JsonProperty("module-name")
        abstract java.lang.String getModuleName();
    }

    @Override
    public void storeJson(String module, String requestId, String configJson) throws MDSALStoreException {
        if (configJson == null) {
            throw new MDSALStoreException("Configuration JSON is empty or null");
        }
        logger.debug("Configuration JSON: " + configJson + "\n" + "module" + module);
        try {
            String path = requestFormatter.buildPath(module, org.onap.appc.Constants.YANG_BASE_CONTAINER,
                    org.onap.appc.Constants.YANG_VNF_CONFIG_LIST, requestId, org.onap.appc.Constants.YANG_VNF_CONFIG);
            logger.debug("Configuration Path : " + path);
            HttpResponse response = client.doPut(path, configJson);
            int httpCode = response.getStatusLine().getStatusCode();
            String respBody = IOUtils.toString(response.getEntity().getContent());
            if (httpCode < 200 || httpCode >= 300) {
                logger.debug("Error while storing configuration JSON to MD-SAL store. Response code: " + httpCode);
                processRestconfResponse(respBody);
            } else {
                logger.debug("Configuration JSON stored to MD-SAL store successfully. Response code: " + httpCode);
            }
        } catch (IOException | APPCException e) {
            logger.error("Error while storing configuration json. Error Message" + e.getMessage(), e);
            throw new MDSALStoreException(e);
        }
    }

    private void processRestconfResponse(String response) throws MDSALStoreException {
        try {
            JsonNode responseJson = mapper.readTree(response);
            ArrayList<String> errorMessage = new ArrayList<>();
            if (responseJson != null && responseJson.get("errors") != null) {
                JsonNode errors = responseJson.get("errors").get("error");
                for (Iterator<JsonNode> i = errors.elements(); i.hasNext(); ) {
                    JsonNode error = i.next();
                    errorMessage.add(error.get("error-message").textValue());
                }
            }
            logger.error("Failed to load config JSON to MD SAL store. " + errorMessage.toString());
            throw new MDSALStoreException("Failed to load config JSON to MD SAL store. Error Message: " + errorMessage.toString());
        } catch (IOException e) {
            logger.error("Failed to process error response from RestConf: " + e.getMessage());
            throw new MDSALStoreException("Failed to process RestConf response. Error Message: " + e.toString(), e);
        }
    }

    private byte[] createBundleJar(String yang, String blueprint, BundleInfo bundleInfo) throws MDSALStoreException {

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, Constants.MANIFEST_VALUE_VERSION);
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_NAME), bundleInfo.getName());
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_SYMBOLIC_NAME), bundleInfo.getName());
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_DESCRIPTION), bundleInfo.getDescription());
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_MANIFEST_VERSION), Constants.MANIFEST_VALUE_BUNDLE_MAN_VERSION);
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_VERSION), String.valueOf(bundleInfo.getVersion()));
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_BLUEPRINT), Constants.MANIFEST_VALUE_BUNDLE_BLUEPRINT);

        byte[] retunValue;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest)) {
            jarOutputStream.putNextEntry(new JarEntry("META-INF/yang/"));
            jarOutputStream.putNextEntry(new JarEntry("META-INF/yang/" + bundleInfo.getName() + ".yang"));
            jarOutputStream.write(yang.getBytes());
            jarOutputStream.closeEntry();

            jarOutputStream.putNextEntry(new JarEntry("OSGI-INF/blueprint/"));
            jarOutputStream.putNextEntry(new JarEntry(Constants.MANIFEST_VALUE_BUNDLE_BLUEPRINT));
            jarOutputStream.write(blueprint.getBytes());
            jarOutputStream.closeEntry();
            jarOutputStream.close();
            retunValue = outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error creating bundle jar: " + bundleInfo.getName() + ". Error message: " + e.getMessage());
            throw new MDSALStoreException("Error creating bundle jar: " + bundleInfo.getName() + " " + e.getMessage(), e);
        }
        return retunValue;
    }

    private String getLeaderNode() throws MDSALStoreException {
        try {
            String shardName = String.format(Constants.SHARD_NAME_FORMAT, getNodeName());
            HttpResponse response = client.doGet(String.format(Constants.GET_NODE_STATUS_PATH_FORMAT, shardName));
            int httpCode = response.getStatusLine().getStatusCode();
            String respBody = IOUtils.toString(response.getEntity().getContent());
            logger.debug(String.format("Get node status returned Code: %s. Response: %s ", httpCode, respBody));
            if (httpCode == 200 && mapper.readTree(respBody).get(Constants.JSON_RESPONSE_VALUE) !=null ) {
                JsonNode responseValue = mapper.readTree(respBody).get(Constants.JSON_RESPONSE_VALUE);
                    String leaderShard = responseValue.get("Leader").asText();
                    if (shardName.equals(leaderShard)) {
                        logger.debug("Current node is leader.");
                        return Constants.SELF;
                    } else {
                        String[] peers = responseValue.get("PeerAddresses").asText().split(",");
                        for (String peer : peers) {
                            if (peer.trim().startsWith(leaderShard)) {
                                String leader = peer.substring(peer.indexOf('@') + 1, peer.indexOf(':', peer.indexOf('@')));
                                logger.debug(String.format("Node %s is a leader", leader));
                                return leader;
                            }
                        }
                        logger.error("No Leader found for a cluster");
                        throw new MDSALStoreException("No Leader found for a cluster");
                    }
            } else {
                logger.error("Error while retrieving leader node.");
                throw new MDSALStoreException("Error while retrieving leader node.");
            }
        } catch (IOException | APPCException e) {
            logger.error(String.format("Error while retrieving leader Node. Error message : %s ", e.getMessage()), e);
            throw new MDSALStoreException(e);
        }
    }

    private String getNodeName() throws MDSALStoreException {
        try {
            HttpResponse response = client.doGet(Constants.GET_SHARD_LIST_PATH);
            int httpCode = response.getStatusLine().getStatusCode();
            String respBody = IOUtils.toString(response.getEntity().getContent());
            logger.debug(String.format("Get shard list returned Code: %s. Response: %s ", httpCode, respBody));
            if (httpCode == 200) {
                JsonNode responseValue = mapper.readTree(respBody).get(Constants.JSON_RESPONSE_VALUE);
                if (responseValue != null && responseValue.get(Constants.JSON_RESPONSE_MEMBER_NAME) != null) {
                    String name = responseValue.get(Constants.JSON_RESPONSE_MEMBER_NAME).asText();
                    logger.debug("Node name : " + name);
                    return name;
                }else{
                    logger.error(String.format("Error while retrieving node name from response. Response body: %s.", respBody));
                    throw new MDSALStoreException(String.format("Error while retrieving node name from response. Response body: %s.", respBody));
                }
            } else {
                logger.error(String.format("Error while retrieving node name. Error code: %s. Error response: %s.", httpCode, respBody));
                throw new MDSALStoreException(String.format("Error while retrieving node name. Error code: %s. Error response: %s.", httpCode, respBody));
            }
        } catch (IOException | APPCException e) {
            logger.error("Error while getting node name " + e.getMessage(), e);
            throw new MDSALStoreException(e);
        }
    }

    protected RestClientInvoker getRestClientInvoker(URL configUrl) throws MalformedURLException {
        return new RestClientInvoker(configUrl);
    }
}
