/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-18 AT&T Intellectual Property. All rights reserved.
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
 *  * ============LICENSE_END=========================================================
 */

package org.onap.appc.ccadaptor;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class ConfigComponentAdaptor implements SvcLogicAdaptor {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ConfigComponentAdaptor.class);

    private static final String ACTION_PARAM = "action";
    private static final String ACTION_PREPARE = "prepare";
    private static final String ACTION_ACTIVATE = "activate";

    private static final String KEY_PUT = "put";
    private static final String KEY_GET = "get";
    private static final String KEY_CLI = "cli";
    private static final String KEY_ESCAPE_SQL = "escapeSql";
    private static final String KEY_XML_DOWNLOAD = "xml-download";
    private static final String KEY_XML_GET_RUNNING_CONF = "xml-getrunningconfig";
    private static final String KEY_DOWNLOAD_CLI_CONFIG = "DownloadCliConfig";
    private static final String KEY_GET_CLI_RUNNING_CONFIG = "GetCliRunningConfig";

    private static final String OPERATION_CREATE = "create";
    private static final String OPERATION_CHANGE = "change";
    private static final String OPERATION_SCALE = "scale";

    static final String USERNAME_PARAM = "User_name";
    static final String PASSWORD_PARAM = "Password";
    static final String HOST_IP_PARAM = "Host_ip_address";
    static final String PORT_NUMBER_PARAM = "Port_number";
    static final String GET_CONFIG_TEMPLATE_PARAM = "Get_config_template";

    private static final String CLI_OUTPUT_PARAM = "cliOutput";
    private static final String REQUEST_ID_PARAM = "request-id";
    private static final String CALLBACK_URL_PARAM = "callback-url";
    private static final String EQUIPMENT_NAME_PARAM = "equipment-name";

    private static final String XML_BUILDING_ERR_STR = "Error building the XML request: ";
    private static final String TEMPLATE_ERR_STR = "Template error: Matching \"}\" not found";
    private static final String SSH_JCRAFT_WRAPPER_ERR_STR = "Exception occurred while using sshJcraftWrapper";
    private static final String PROMPT_STR = "]]>]]>";
    private static final String RESPONSE_STR = "response=\n{}\n";

    private static final String SERVICE_INSTANCE_ID_ATTR = "service-data.service-information.service-instance-id";
    private static final String SVC_REQUEST_ID_ATTR = "service-data.appc-request-header.svc-request-id";

    private static final String RPC_REPLY_END_TAG = "</rpc-reply>";
    private static final String BASE_REQUEST = "BASE";

    private String configUrl;
    private String configUser;
    private String configPassword;
    private String auditUrl;
    private String auditUser;
    private String auditPassword;
    private String configCallbackUrl;
    private String auditCallbackUrl;

    public ConfigComponentAdaptor(Properties props) {
        if (props != null) {
            configUrl = props.getProperty("configComponent.url", "");
            configUser = props.getProperty("configComponent.user", "");
            configPassword = props.getProperty("configComponent.passwd", "");
            auditUrl = props.getProperty("auditComponent.url", "");
            auditUser = props.getProperty("auditComponent.user", "");
            auditPassword = props.getProperty("auditComponent.passwd", "");
            configCallbackUrl = props.getProperty("service-configuration-notification-url", "");
            auditCallbackUrl = props.getProperty("audit-configuration-notification-url", "");
        }
    }

    @Override
    public ConfigStatus configure(String key, Map<String, String> parameters, SvcLogicContext ctx) {
        HttpResponse r = new HttpResponse();
        r.code = 200;
        log.debug("ConfigComponentAdaptor.configure - key = " + key);
        log.debug("key = {}", key);
        log.debug("Parameters:");
        for (Entry<String, String> paramEntrySet : parameters.entrySet()) {
            log.debug("    {} = {}", paramEntrySet.getKey(), paramEntrySet.getValue());
        }

        String parmval = parameters.get("config-component-configUrl");
        if (!nullOrEmpty(parmval)) {
            log.debug("Overwriting URL with {}", parmval);
            configUrl = parmval;
        }

        parmval = parameters.get("config-component-configPassword");
        if (!nullOrEmpty(parmval)) {
            log.debug("Overwriting configPassword with {}", parmval);
            configPassword = parmval;
        }

        parmval = parameters.get("config-component-configUser");
        if (!nullOrEmpty(parmval)) {
            log.debug("Overwriting configUser id with {}", parmval);
            configUser = parmval;
        }

        String action = parameters.get(ACTION_PARAM);

        String chg = ctx.getAttribute(
            "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name");
        if (chg != null && areEqual(action, ACTION_PREPARE)) {
            return prepare(ctx, "CHANGE", OPERATION_CHANGE);
        }
        if (chg != null && areEqual(action, ACTION_ACTIVATE)) {
            return activate(ctx, true);
        }

        String scale = ctx.getAttribute(
            "service-data.vnf-config-parameters-list.vnf-config-parameters[0].scale-configuration[0].network-type");
        if (scale != null && areEqual(action, ACTION_PREPARE)) {
            return prepare(ctx, "CHANGE", OPERATION_SCALE);
        }
        if (scale != null && areEqual(action, ACTION_ACTIVATE)) {
            return activate(ctx, true);
        }

        if (areEqual(action, ACTION_PREPARE)) {
            return prepare(ctx, BASE_REQUEST, OPERATION_CREATE);
        }
        if (areEqual(action, ACTION_ACTIVATE)) {
            return activate(ctx, false);
        }

        if ("backup".equalsIgnoreCase(action)) {
            return prepare(ctx, "BACKUP", "backup");
        }
        if ("restorebackup".equalsIgnoreCase(action)) {
            return prepare(ctx, "RESTOREBACKUP", "restorebackup");
        }
        if ("deletebackup".equalsIgnoreCase(action)) {
            return prepare(ctx, "DELETEBACKUP", "deletebackup");
        }
        if ("audit".equalsIgnoreCase(action)) {
            return audit(ctx, "FULL");
        }
        if ("getrunningconfig".equalsIgnoreCase(action)) {
            return audit(ctx, "RUNNING");
        }

        if ((key.equals(KEY_PUT)) || (key.equals(KEY_GET))) {
            String loginId = parameters.get("loginId");
            String host = parameters.get("host");
            String password = parameters.get("password");
            password = EncryptionTool.getInstance().decrypt(password);
            String fullPathFileName = parameters.get("fullPathFileName");

            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            log.debug("SCP: SshJcraftWrapper has been instantiated");

            try {
                if (key.equals(KEY_PUT)) {
                    String data = parameters.get("data");
                    log.debug("Command is for put: Length of data is: {}", data.length());
                    InputStream is = new ByteArrayInputStream(data.getBytes());
                    log.debug("SCP: Doing a put: fullPathFileName={}", fullPathFileName);
                    sshJcraftWrapper.put(is, fullPathFileName, host, loginId, password);
                    trySleepFor(1000L * 180);
                } else {  // Must be a get
                    log.debug("SCP: Doing a get: fullPathFileName={}", fullPathFileName);
                    String response = sshJcraftWrapper.get(fullPathFileName, host, loginId, password);
                    log.debug("Got the response and putting into the ctx object");
                    ctx.setAttribute("fileContents", response);
                    log.debug("SCP: Closing the SFTP connection");
                }
                return setResponseStatus(ctx, r);
            } catch (IOException e) {
                log.error(SSH_JCRAFT_WRAPPER_ERR_STR, e);
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                return setResponseStatus(ctx, r);
            }
        }
        if (key.equals(KEY_CLI)) {
            String loginId = parameters.get("loginId");
            String host = parameters.get("host");
            String password = parameters.get("password");
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get("portNumber");
            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            try {
                log.debug("CLI: Attempting to login: host={} loginId={} password={} portNumber={}", host, loginId,
                    password, portNumber);
                sshJcraftWrapper.connect(host, loginId, password); //what about portNum?

                log.debug("Sending 'sdc'");
                sshJcraftWrapper.send("sdc", ":");
                log.debug("Sending 1");
                sshJcraftWrapper.send("1", ":");
                log.debug("Sending 1, the second time");
                sshJcraftWrapper.send("1", "#");
                log.debug("Sending paging-options disable");
                sshJcraftWrapper.send("paging-options disable", "#");
                log.debug("Sending show config");
                String response = sshJcraftWrapper.send("show config", "#");

                log.debug("response is now:'{}'", response);
                log.debug("Populating the ctx object with the response");
                ctx.setAttribute(CLI_OUTPUT_PARAM, response);
                sshJcraftWrapper.closeConnection();
                r.code = 200;
                return setResponseStatus(ctx, r);
            } catch (IOException e) {
                log.error(SSH_JCRAFT_WRAPPER_ERR_STR, e);
                sshJcraftWrapper.closeConnection();
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                return setResponseStatus(ctx, r);
            }
        }
        if (key.equals(KEY_ESCAPE_SQL)) {
            String data = parameters.get("artifactContents");
            log.debug("ConfigComponentAdaptor.configure - escapeSql");
            data = escapeMySql(data);
            ctx.setAttribute("escapedData", data);
            return setResponseStatus(ctx, r);
        }
        if (key.equals(KEY_GET_CLI_RUNNING_CONFIG)) {
            log.debug("key was: " + KEY_GET_CLI_RUNNING_CONFIG);
            String username = parameters.get(USERNAME_PARAM);
            String hostIpAddress = parameters.get(HOST_IP_PARAM);
            String password = parameters.get(PASSWORD_PARAM);
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get(PORT_NUMBER_PARAM);
            String getConfigTemplate = parameters.get(GET_CONFIG_TEMPLATE_PARAM);
            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            log.debug("GetCliRunningConfig: sshJcraftWrapper was instantiated");
            try {
                log.debug("GetCliRunningConfig: Attempting to login: Host_ip_address=" + hostIpAddress + " User_name="
                    + username + " Password=" + password + " Port_number=" + portNumber);

                boolean showConfigFlag = false;
                sshJcraftWrapper
                    .connect(hostIpAddress, username, password, "", 30000, Integer.parseInt(portNumber));
                log.debug("GetCliRunningConfig: On the VNF device");
                StringTokenizer st = new StringTokenizer(getConfigTemplate, "\n");
                String command = null;

                StringBuilder cliResponse = new StringBuilder();

                // shouldn't this be used somewhere?
                StringBuilder response = new StringBuilder();

                try {
                    while (st.hasMoreTokens()) {
                        String line = st.nextToken();
                        log.debug("line={}", line);
                        if (line.contains("Request:")) {
                            log.debug("Found a Request line: line={}", line);
                            command = getStringBetweenQuotes(line);
                            log.debug("Sending command={}", command);
                            sshJcraftWrapper.send(command);
                            log.debug("command has been sent");
                            if (line.contains("show config")) {
                                showConfigFlag = true;
                                log.debug("GetCliRunningConfig: GetCliRunningConfig: setting 'showConfigFlag' to true");
                            }
                        }
                        if (line.contains("Response: Ends_With")) {
                            log.debug("Found a Response line: line={}", line);
                            String delemeter = getStringBetweenQuotes(line);
                            log.debug("The delemeter={}", delemeter);
                            String tmpResponse = sshJcraftWrapper.receiveUntil(delemeter, 120 * 1000, command);
                            response.append(tmpResponse);
                            if (showConfigFlag) {
                                showConfigFlag = false;
                                StringTokenizer st2 = new StringTokenizer(tmpResponse, "\n");
                                while (st2.hasMoreTokens()) {
                                    String line2 = st2.nextToken();
                                    if (!line2.contains("#")) {
                                        cliResponse.append(line2).append('\n');
                                    }
                                }
                            }
                        }
                    }
                } catch (NoSuchElementException e) {
                    log.error(e.getMessage(), e);
                }
                log.debug("CliResponse=\n{}", cliResponse.toString());
                ctx.setAttribute(CLI_OUTPUT_PARAM, cliResponse.toString());
                sshJcraftWrapper.closeConnection();
                r.code = 200;
                return setResponseStatus(ctx, r);
            } catch (IOException e) {
                log.error(SSH_JCRAFT_WRAPPER_ERR_STR, e);
                sshJcraftWrapper.closeConnection();
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                return setResponseStatus(ctx, r);
            }
        }
        if (key.equals(KEY_XML_DOWNLOAD)) {
            log.debug("key was: " + KEY_XML_DOWNLOAD);
            String userName = parameters.get(USERNAME_PARAM);
            String hostIpAddress = parameters.get(HOST_IP_PARAM);
            String password = parameters.get(PASSWORD_PARAM);
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get(PORT_NUMBER_PARAM);
            String contents = parameters.get("Contents");
            String netconfHelloCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n  <capabilities>\n   <capability>urn:ietf:params:netconf:base:1.0</capability>\n  <capability>urn:com:ericsson:ebase:1.1.0</capability> </capabilities>\n </hello>";
            String terminateConnectionCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n  <rpc message-id=\"terminateConnection\" xmlns:netconf=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n <close-session/> \n </rpc>\n ]]>]]>";
            String commitCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <rpc> <commit/> </rpc>\n ]]>]]>";

            log.debug("xml-download: User_name={} Host_ip_address={} Password={} Port_number={}", userName,
                hostIpAddress, password, portNumber);
            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            try {
                // what about prompt "]]>]]>"?
                sshJcraftWrapper
                    .connect(hostIpAddress, userName, password, 30000, Integer.parseInt(portNumber), "netconf");

                netconfHelloCmd += PROMPT_STR;
                log.debug("Sending the hello command");
                sshJcraftWrapper.send(netconfHelloCmd);
                String response;
                log.debug("Sending xmlCmd cmd");
                String messageId = "1";
                messageId = "\"" + messageId + "\"";
                String loadConfigurationString =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id="
                        + messageId
                        + "> <edit-config> <target> <candidate /> </target> <default-operation>merge</default-operation> <config xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + contents + "</config> </edit-config> </rpc>";
                loadConfigurationString = loadConfigurationString + PROMPT_STR;
                sshJcraftWrapper.send(loadConfigurationString);
                log.debug("After sending loadConfigurationString");
                response = sshJcraftWrapper.receiveUntil(RPC_REPLY_END_TAG, 600000, "");
                if (response.contains("rpc-error")) {
                    log.debug("Error from device: Response from device had 'rpc-error'");
                    log.debug(RESPONSE_STR, response);
                    r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                    r.message = response;
                } else {
                    log.debug(":LoadConfiguration was a success, sending commit cmd");
                    sshJcraftWrapper.send(commitCmd);
                    log.debug(":After sending commitCmd");
                    response = sshJcraftWrapper.receiveUntil(RPC_REPLY_END_TAG, 180000, "");
                    handleRpcError(r, response);
                }
                sshJcraftWrapper.send(terminateConnectionCmd);
                sshJcraftWrapper.closeConnection();
                return setResponseStatus(ctx, r);
            } catch (Exception e) {
                log.error("Caught an Exception", e);
                sshJcraftWrapper.closeConnection();
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                log.debug("Returning error message");
                return setResponseStatus(ctx, r);
            }
        }
        if (key.equals(KEY_XML_GET_RUNNING_CONF)) {
            log.debug("key was: : xml-getrunningconfig");
            String xmlGetRunningConfigCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">  <get-config> <source> <running /> </source> </get-config> </rpc>\n";
            String hostIpAddress = parameters.get(HOST_IP_PARAM);
            String username = parameters.get(USERNAME_PARAM);
            String password = parameters.get(PASSWORD_PARAM);
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get(PORT_NUMBER_PARAM);
            String netconfHelloCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n  <capabilities>\n   <capability>urn:ietf:params:netconf:base:1.0</capability>\n <capability>urn:com:ericsson:ebase:1.1.0</capability> </capabilities>\n </hello>";
            String terminateConnectionCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n  <rpc message-id=\"terminateConnection\" xmlns:netconf=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n <close-session/> \n </rpc>\n ]]>]]>";
            log.debug("xml-getrunningconfig: User_name={} Host_ip_address={} Password={} Port_number={}", username,
                hostIpAddress, password, portNumber);
            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            try {

                sshJcraftWrapper
                    .connect(hostIpAddress, username, password, 30000, Integer.parseInt(portNumber),
                        "netconf"); //What about prompt "]]>]]>" here?
                netconfHelloCmd += PROMPT_STR;
                log.debug(":Sending the hello command");
                sshJcraftWrapper.send(netconfHelloCmd);
                String response;
                log.debug("Sending get running config command");
                sshJcraftWrapper.send(xmlGetRunningConfigCmd + "]]>]]>\n");
                response = sshJcraftWrapper.receiveUntil(RPC_REPLY_END_TAG, 180000, "");
                log.debug("Response from getRunningconfigCmd={}", response);
                response = trimResponse(response);
                ctx.setAttribute("xmlRunningConfigOutput", response);
                sshJcraftWrapper.send(terminateConnectionCmd);
                sshJcraftWrapper.closeConnection();
                r.code = 200;
                return setResponseStatus(ctx, r);
            } catch (Exception e) {
                log.error("Caught an Exception", e);
                sshJcraftWrapper.closeConnection();
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                log.debug("Returning error message");
                return setResponseStatus(ctx, r);
            }
        }
        if (key.equals(KEY_DOWNLOAD_CLI_CONFIG)) {
            log.debug("key was: DownloadCliConfig: ");
            String username = parameters.get(USERNAME_PARAM);
            String hostIpAddress = parameters.get(HOST_IP_PARAM);
            String password = parameters.get(PASSWORD_PARAM);
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get(PORT_NUMBER_PARAM);
            String downloadConfigTemplate = parameters.get("Download_config_template");
            String configContents = parameters.get("Config_contents");
            log.debug("Contents of the 'Config_contents' are: {}", configContents);
            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            log.debug("DownloadCliConfig: sshJcraftWrapper was instantiated");
            int timeout = 4 * 60 * 1000;
            try {
                log.debug("DownloadCliConfig: Attempting to login: Host_ip_address=" + hostIpAddress + " User_name="
                    + username + " Password=" + password + " Port_number=" + portNumber);

                StringBuilder cliResponse = new StringBuilder();

                // shouldn't this be used somewhere?
                StringBuilder response = new StringBuilder();

                sshJcraftWrapper
                    .connect(hostIpAddress, username, password, "", 30000, Integer.parseInt(portNumber));
                log.debug("DownloadCliConfig: On the VNF device");
                StringTokenizer st = new StringTokenizer(downloadConfigTemplate, "\n");
                String command = null;
                String executeConfigContentsDelimiter;
                try {
                    while (st.hasMoreTokens()) {
                        String line = st.nextToken();
                        log.debug("line={}", line);
                        if (line.contains("Request:")) {
                            log.debug("Found a Request line: line={}", line);
                            command = getStringBetweenQuotes(line);
                            log.debug("Sending command={}", command);
                            sshJcraftWrapper.send(command);
                            log.debug("command has been sent");
                        } else if ((line.contains("Response: Ends_With")) && (
                            !line.contains("Execute_config_contents Response: Ends_With"))) {
                            log.debug("Found a Response line: line={}", line);
                            String delimiter = getStringBetweenQuotes(line);
                            log.debug("The delimiter={}", delimiter);
                            String tmpResponse = sshJcraftWrapper.receiveUntil(delimiter, timeout, command);
                            response.append(tmpResponse);
                            cliResponse.append(tmpResponse);
                        } else if (line.contains("Execute_config_contents Response: Ends_With")) {
                            log.debug("Found a 'Execute_config_contents Response:' line={}", line);
                            executeConfigContentsDelimiter = getStringBetweenQuotes(line);
                            log.debug("executeConfigContentsDelemeter={}", executeConfigContentsDelimiter);
                            StringTokenizer st2 = new StringTokenizer(configContents, "\n");
                            while (st2.hasMoreTokens()) {
                                String cmd = st2.nextToken();
                                log.debug("Config_contents: cmd={}", cmd);
                                sshJcraftWrapper.send(cmd);
                                String tmpResponse = sshJcraftWrapper
                                    .receiveUntil(executeConfigContentsDelimiter, timeout, command);
                                cliResponse.append(tmpResponse);
                            }
                        }
                    }
                } catch (NoSuchElementException e) {
                    log.error(e.getMessage(), e);
                }
                sshJcraftWrapper.closeConnection();
                log.debug(":Escaping all the single and double quotes in the response");

                String escapedCliResponse = cliResponse
                    .toString()
                    .replaceAll("\"", "\\\\\"")
                    .replaceAll("\'", "\\\\'");

                log.debug("CliResponse=\n{}" + escapedCliResponse);
                ctx.setAttribute(CLI_OUTPUT_PARAM, escapedCliResponse);
                r.code = 200;
                return setResponseStatus(ctx, r);
            } catch (IOException e) {
                log.error(e.getMessage() + e);
                sshJcraftWrapper.closeConnection();
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                log.debug("DownloadCliConfig: Returning error message");
                return setResponseStatus(ctx, r);
            }
        }
        log.debug("Unsupported action - {}", action);
        return ConfigStatus.FAILURE;
    }

    private boolean areEqual(String action, String actionPrepare) {
        return action != null && action.equalsIgnoreCase(actionPrepare);
    }

    private boolean nullOrEmpty(String parmval) {
        return (parmval == null || parmval.length() <= 0);
    }

    private void handleRpcError(HttpResponse r, String response) {
        if (response.contains("rpc-error")) {
            log.debug("Error from device: Response from device had 'rpc-error'");
            log.debug(RESPONSE_STR, response);
            r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
            r.message = response;
        } else {
            log.debug(":Looks like a success");
            log.debug(RESPONSE_STR, response);
            r.code = 200;
        }
    }

    private void trySleepFor(long length) {
        try {
            log.debug("Sleeping for 180 seconds....");
            Thread.sleep(length);
            log.debug("Woke up....");
        } catch (InterruptedException ee) {
            log.error("Sleep interrupted", ee);
            Thread.currentThread().interrupt();
        }
    }

    private ConfigStatus prepare(SvcLogicContext ctx, String requestType, String operation) {
        String templateName = requestType.equals(BASE_REQUEST) ? "/config-base.xml" : "/config-data.xml";
        String ndTemplate = expandRepeats(ctx, readFile(templateName), 1);
        String nd = buildNetworkData2(ctx, ndTemplate, operation);

        String reqTemplate = readFile("/config-request.xml");
        Map<String, String> param = new HashMap<>();
        param.put(REQUEST_ID_PARAM, ctx.getAttribute(SVC_REQUEST_ID_ATTR));
        param.put("request-type", requestType);
        param.put(CALLBACK_URL_PARAM, configCallbackUrl);
        if (operation.equals(OPERATION_CREATE) || operation.equals(OPERATION_CHANGE)
            || operation.equals(OPERATION_SCALE)) {
            param.put(ACTION_PARAM, "GenerateOnly");
        }
        param.put(EQUIPMENT_NAME_PARAM, ctx.getAttribute(SERVICE_INSTANCE_ID_ATTR));
        param.put("equipment-ip-address", ctx.getAttribute("service-data.vnf-config-information.vnf-host-ip-address"));
        param.put("vendor", ctx.getAttribute("service-data.vnf-config-information.vendor"));
        param.put("network-data", nd);

        String req;
        try {
            req = buildXmlRequest(param, reqTemplate);
        } catch (Exception e) {
            log.error(XML_BUILDING_ERR_STR, e);

            HttpResponse r = new HttpResponse();
            r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
            r.message = e.getMessage();
            return setResponseStatus(ctx, r);
        }

        HttpResponse r = sendXmlRequest(req, configUrl, configUser, configPassword);
        return setResponseStatus(ctx, r);
    }

    private ConfigStatus activate(SvcLogicContext ctx, boolean change) {
        String reqTemplate = readFile("/config-request.xml");
        Map<String, String> param = new HashMap<>();
        param.put(REQUEST_ID_PARAM, ctx.getAttribute(SVC_REQUEST_ID_ATTR));
        param.put(CALLBACK_URL_PARAM, configCallbackUrl);
        param.put(ACTION_PARAM, change ? "DownloadChange" : "DownloadBase");
        param.put(EQUIPMENT_NAME_PARAM, ctx.getAttribute(SERVICE_INSTANCE_ID_ATTR));

        String req;
        try {
            req = buildXmlRequest(param, reqTemplate);
        } catch (Exception e) {
            log.error(XML_BUILDING_ERR_STR, e);

            HttpResponse r = new HttpResponse();
            r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
            r.message = e.getMessage();
            return setResponseStatus(ctx, r);
        }

        HttpResponse r = sendXmlRequest(req, configUrl, configUser, configPassword);
        return setResponseStatus(ctx, r);
    }

    private ConfigStatus audit(SvcLogicContext ctx, String auditLevel) {
        String reqTemplate = readFile("/audit-request.xml");
        Map<String, String> param = new HashMap<>();
        param.put(REQUEST_ID_PARAM, ctx.getAttribute(SVC_REQUEST_ID_ATTR));
        param.put(CALLBACK_URL_PARAM, auditCallbackUrl);
        param.put(EQUIPMENT_NAME_PARAM, ctx.getAttribute(SERVICE_INSTANCE_ID_ATTR));
        param.put("audit-level", auditLevel);
        String req;

        try {
            req = buildXmlRequest(param, reqTemplate);
        } catch (Exception e) {
            log.error(XML_BUILDING_ERR_STR, e);

            HttpResponse r = new HttpResponse();
            r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
            r.message = e.getMessage();
            return setResponseStatus(ctx, r);
        }

        HttpResponse r = sendXmlRequest(req, auditUrl, auditUser, auditPassword);
        return setResponseStatus(ctx, r);
    }

    @Override
    public ConfigStatus activate(String key, SvcLogicContext ctx) {
        return ConfigStatus.SUCCESS;
    }

    @Override
    public ConfigStatus deactivate(String key, SvcLogicContext ctx) {
        return ConfigStatus.SUCCESS;
    }

    private String escapeMySql(String input) {
        if (input == null) {
            return null;
        }

        return input
            .replace("\\", "\\\\")
            .replace("\'", "\\'");
    }

    private String readFile(String fileName) {
        InputStream is = getClass().getResourceAsStream(fileName);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader in = new BufferedReader(isr);
        StringBuilder builder = new StringBuilder();
        try {
            String s = in.readLine();
            while (s != null) {
                builder.append(s).append('\n');
                s = in.readLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading " + fileName, e);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                log.warn("Could not close BufferedReader", e);
            }
            try {
                isr.close();
            } catch (Exception e) {
                log.warn("Could not close InputStreamReader", e);
            }
            try {
                is.close();
            } catch (Exception e) {
                log.warn("Could not close InputStream", e);
            }
        }
        return builder.toString();
    }

    private String buildXmlRequest(Map<String, String> param, String template) {
        StringBuilder ss = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            int i1 = template.indexOf("${", i);
            if (i1 < 0) {
                ss.append(template.substring(i));
                break;
            }

            int i2 = template.indexOf('}', i1 + 2);
            if (i2 < 0) {
                throw new TemplateException(TEMPLATE_ERR_STR);
            }

            String var1 = template.substring(i1 + 2, i2);
            String value1 = param.get(var1);
            if (emptyOrNull(value1)) {
                // delete the whole element (line)
                int i3 = template.lastIndexOf('\n', i1);
                if (i3 < 0) {
                    i3 = 0;
                }
                int i4 = template.indexOf('\n', i1);
                if (i4 < 0) {
                    i4 = template.length();
                }

                if (i < i3) {
                    ss.append(template.substring(i, i3));
                }
                i = i4;
            } else {
                ss.append(template.substring(i, i1)).append(value1);
                i = i2 + 1;
            }
        }

        return ss.toString();
    }

    private String buildNetworkData2(SvcLogicContext ctx, String template, String operation) {
        log.info("Building XML started");
        long t1 = System.currentTimeMillis();

        Map<String, String> mm = new HashMap<>();
        for (String s : ctx.getAttributeKeySet()) {
            mm.put(s, ctx.getAttribute(s));
        }
        mm.put("operation", operation);

        StringBuilder ss = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            int i1 = template.indexOf("${", i);
            if (i1 < 0) {
                ss.append(template.substring(i));
                break;
            }

            int i2 = template.indexOf('}', i1 + 2);
            if (i2 < 0) {
                throw new TemplateException(TEMPLATE_ERR_STR);
            }

            String var1 = template.substring(i1 + 2, i2);
            String value1 = XmlUtil.getXml(mm, var1);
            if (emptyOrNull(value1)) {
                int i3 = template.lastIndexOf('\n', i1);
                if (i3 < 0) {
                    i3 = 0;
                }
                int i4 = template.indexOf('\n', i1);
                if (i4 < 0) {
                    i4 = template.length();
                }

                if (i < i3) {
                    ss.append(template.substring(i, i3));
                }
                i = i4;
            } else {
                ss.append(template.substring(i, i1)).append(value1);
                i = i2 + 1;
            }
        }

        long t2 = System.currentTimeMillis();
        log.info("Building XML completed. Time: " + (t2 - t1));

        return ss.toString();
    }

    private boolean emptyOrNull(String value1) {
        return value1 == null || value1.trim().length() == 0;
    }

    private String expandRepeats(SvcLogicContext ctx, String template, int level) {
        StringBuilder newTemplate = new StringBuilder();
        int k = 0;
        while (k < template.length()) {
            int i1 = template.indexOf("${repeat:", k);
            if (i1 < 0) {
                newTemplate.append(template.substring(k));
                break;
            }

            int i2 = template.indexOf(':', i1 + 9);
            if (i2 < 0) {
                throw new TemplateException(
                    "Template error: Context variable name followed by \":\" is required after repeat");
            }

            // Find the closing "}", store in i3
            int i3 = findLastBracketIndex(template, i2);

            String var1 = template.substring(i1 + 9, i2);
            String value1 = ctx.getAttribute(var1);
            log.info("     " + var1 + ": " + value1);
            int n = tryParseValue(value1);

            newTemplate.append(template.substring(k, i1));

            String rpt = template.substring(i2 + 1, i3);

            for (int ii = 0; ii < n; ii++) {
                String ss = rpt.replaceAll("\\[\\$\\{" + level + "\\}\\]", "[" + ii + "]");
                newTemplate.append(ss);
            }
            k = i3 + 1;
        }

        if (k == 0) {
            return newTemplate.toString();
        }
        return expandRepeats(ctx, newTemplate.toString(), level + 1);
    }

    private int findLastBracketIndex(String template, int i2) {
        int i3 = -1;
        int i = i2;
        int nn = 1;
        while (nn > 0 && i < template.length()) {
            i3 = template.indexOf('}', i);
            if (i3 < 0) {
                throw new TemplateException(TEMPLATE_ERR_STR);
            }
            int i32 = template.indexOf('{', i);
            if (i32 >= 0 && i32 < i3) {
                nn++;
                i = i32 + 1;
            } else {
                nn--;
                i = i3 + 1;
            }
        }
        return i3;
    }

    private int tryParseValue(String value1) {
        int n;
        try {
            n = Integer.parseInt(value1);
        } catch (Exception e) {
            log.error("Failed to parse value. Using default (0).", e);
            n = 0;
        }
        return n;
    }

    private HttpResponse sendXmlRequest(String xmlRequest, String url, String user, String password) {
        try {
            Client client = Client.create();
            client.setConnectTimeout(5000);
            WebResource webResource = client.resource(url);

            log.info("SENDING...............");
            log.info(xmlRequest);

            String authString = user + ":" + password;
            byte[] authEncBytes = Base64.encode(authString);
            String authStringEnc = new String(authEncBytes);
            authString = "Basic " + authStringEnc;

            ClientResponse response =
                webResource.header("Authorization", authString).accept("UTF-8").type("application/xml").post(
                    ClientResponse.class, xmlRequest);

            int code = response.getStatus();
            String message = null;

            log.info("RESPONSE...............");
            log.info("HTTP response code: " + code);
            log.info("HTTP response message: " + message);
            log.info("");

            HttpResponse r = new HttpResponse();
            r.code = code;
            r.message = message;
            return r;

        } catch (Exception e) {
            log.error("Error sending the request: ", e);

            HttpResponse r = new HttpResponse();
            r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
            r.message = e.getMessage();
            return r;
        }
    }

    private static class HttpResponse {

        public int code;
        public String message;
    }

    private ConfigStatus setResponseStatus(SvcLogicContext ctx, HttpResponse r) {
        ctx.setAttribute("error-code", String.valueOf(r.code));
        ctx.setAttribute("error-message", r.message);

        return r.code > 299 ? ConfigStatus.FAILURE : ConfigStatus.SUCCESS;
    }

    private String getStringBetweenQuotes(String string) {
        log.debug("string=" + string);
        String retString;
        int start = string.indexOf('\"');
        int end = string.lastIndexOf('\"');
        retString = string.substring(start + 1, end);
        log.debug("retString=" + retString);
        return retString;
    }

    public static String _readFile(String fileName) {
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            while ((line = in.readLine()) != null) {
                builder.append(line).append('\n');
            }
            in.close();
        } catch (IOException e) {
            log.error("Caught an IOException in method readFile()", e);
        }
        return builder.toString();
    }

    private String trimResponse(String response) {
        StringTokenizer line = new StringTokenizer(response, "\n");
        StringBuilder builder = new StringBuilder();
        boolean captureText = false;
        while (line.hasMoreTokens()) {
            String token = line.nextToken();
            if (token.contains("<configuration xmlns=")) {
                captureText = true;
            }
            if (captureText) {
                builder.append(token).append('\n');
            }
            if (token.contains("</configuration>")) {
                captureText = false;
            }
        }
        return builder.toString();
    }

    public static void main(String[] args) throws Exception {
        Properties props = null;
        log.info("*************************Hello*****************************");
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String getConfigTemplate = _readFile("/home/userID/data/Get_config_template");
        String key = "GetCliRunningConfig";
        Map<String, String> parameters = new HashMap<>();
        parameters.put(HOST_IP_PARAM, "000.00.000.00");
        parameters.put(USERNAME_PARAM, "root");
        parameters.put(PASSWORD_PARAM, "!bootstrap");
        parameters.put(PORT_NUMBER_PARAM, "22");
        parameters.put(GET_CONFIG_TEMPLATE_PARAM, getConfigTemplate);
        SvcLogicContext ctx = null;
        log.info("*************************TRACE 1*****************************");
        cca.configure(key, parameters, ctx);
    }
}
