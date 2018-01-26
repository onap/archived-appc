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
import java.net.HttpURLConnection;
import java.nio.file.Paths;
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
    private static final DebugLog debugLog = new DebugLog(Paths.get(DebugLog.LOG_FILE));
    private static final String ACTION_PARAM_NAME = "action";
    private static final String PREPARE_PARAM_NAME = "prepare";
    private static final String ACTIVATE_PARAM_NAME = "activate";
    private static final String USER_NAME_PARAM_NAME = "User_name";
    private static final String HOST_IP_ADDRESS_PARAM_NAME = "Host_ip_address";
    private static final String PASSWORD_PARAM_NAME = "Password";
    private static final String PORT_NUMBER_PARAM_NAME = "Port_number";
    private static final String GET_CONFIG_TEMPLATE_PARAM_NAME = "Get_config_template";

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
        String fnName = "ConfigComponentAdaptor.configure";
        log.debug("ConfigComponentAdaptor.configure - key = " + key);
        debugLog.printRTAriDebug(fnName, "key = " + key);
        log.debug("Parameters:");
        debugLog.printRTAriDebug(fnName, "Parameters:");
        for (Entry<String, String> paramEntrySet : parameters.entrySet()) {
            log.debug("    " + paramEntrySet.getKey() + " = " + paramEntrySet.getValue());
            debugLog.printRTAriDebug(fnName, "    " + paramEntrySet.getKey() + " = " + paramEntrySet.getValue());
        }

        String parmval = parameters.get("config-component-configUrl");
        if ((parmval != null) && (parmval.length() > 0)) {
            log.debug("Overwriting URL with " + parmval);
            configUrl = parmval;
        }

        parmval = parameters.get("config-component-configPassword");
        if ((parmval != null) && (parmval.length() > 0)) {
            log.debug("Overwriting configPassword with " + parmval);
            configPassword = parmval;
        }

        parmval = parameters.get("config-component-configUser");
        if ((parmval != null) && (parmval.length() > 0)) {
            log.debug("Overwriting configUser id with " + parmval);
            configUser = parmval;
        }

        String action = parameters.get(ACTION_PARAM_NAME);

        String chg = ctx.getAttribute(
            "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name");
        if (chg != null && PREPARE_PARAM_NAME.equalsIgnoreCase(action)) {
            return prepare(ctx, "CHANGE", "change");
        }
        if (chg != null && ACTIVATE_PARAM_NAME.equalsIgnoreCase(action)) {
            return activate(ctx, true);
        }

        String scale = ctx.getAttribute(
            "service-data.vnf-config-parameters-list.vnf-config-parameters[0].scale-configuration[0].network-type");
        if (scale != null && PREPARE_PARAM_NAME.equalsIgnoreCase(action)) {
            return prepare(ctx, "CHANGE", "scale");
        }
        if (scale != null && ACTIVATE_PARAM_NAME.equalsIgnoreCase(action)) {
            return activate(ctx, true);
        }

        if (PREPARE_PARAM_NAME.equalsIgnoreCase(action)) {
            return prepare(ctx, "BASE", "create");
        }
        if (ACTIVATE_PARAM_NAME.equalsIgnoreCase(action)) {
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

        if (("put".equals(key)) || ("get".equals(key))) {
            String loginId = parameters.get("loginId");
            String host = parameters.get("host");
            String password = parameters.get("password");
            password = EncryptionTool.getInstance().decrypt(password);
            String fullPathFileName = parameters.get("fullPathFileName");
            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            log.debug("SCP: SshJcraftWrapper has been instantiated");
            debugLog.printRTAriDebug(fnName, "SCP: SshJcraftWrapper has been instantiated");

            try {
                if ("put".equals(key)) {
                    String data = parameters.get("data");
                    debugLog.printRTAriDebug(fnName, "Command is for put: Length of data is: " + data.length());
                    InputStream is = new ByteArrayInputStream(data.getBytes());
                    log.debug("SCP: Doing a put: fullPathFileName=" + fullPathFileName);
                    debugLog.printRTAriDebug(fnName, "SCP: Doing a put: fullPathFileName=" + fullPathFileName);
                    sshJcraftWrapper.put(is, fullPathFileName, host, loginId, password);
                    try {
                        debugLog.printRTAriDebug(fnName, "Sleeping for 180 seconds....");
                        Thread.sleep(1000L * 180);
                        debugLog.printRTAriDebug(fnName, "Woke up....");
                    } catch (java.lang.InterruptedException ee) {
                        log.error("Sleep interrupted", ee);
                        Thread.currentThread().interrupt();
                    }
                } else {  // Must be a get
                    log.debug("SCP: Doing a get: fullPathFileName=" + fullPathFileName);
                    debugLog.printRTAriDebug(fnName, "SCP: Doing a get: fullPathFileName=" + fullPathFileName);
                    String response = sshJcraftWrapper.get(fullPathFileName, host, loginId, password);
                    debugLog.printRTAriDebug(fnName, "Got the response and putting into the ctx object");
                    ctx.setAttribute("fileContents", response);
                    log.debug("SCP: Closing the SFTP connection");
                }
                return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_OK, ""));
            } catch (IOException e) {
                debugLog.printRTAriDebug(fnName, "Caught a IOException e=" + e);
                log.debug(fnName + " : Caught a IOException e=" + e);
                return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage()));
            }
        }
        if ("cli".equals(key)) {
            String loginId = parameters.get("loginId");
            String host = parameters.get("host");
            String password = parameters.get("password");
            password = EncryptionTool.getInstance().decrypt(password);
            String cliCommand = parameters.get("cli");
            String portNumber = parameters.get("portNumber");
            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            try {
                log.debug("CLI: Attempting to login: host=" + host + " loginId=" + loginId + " password=" + password +
                    " portNumber=" + portNumber);
                debugLog.printRTAriDebug(fnName, "CLI: Attempting to login: host=" + host + " loginId=" + loginId +
                    " password=" + password + " portNumber=" + portNumber);
                sshJcraftWrapper.connect(host, loginId, password, Integer.parseInt(portNumber));

                debugLog.printRTAriDebug(fnName, "Sending 'sdc'");
                sshJcraftWrapper.send("sdc", ":");
                debugLog.printRTAriDebug(fnName, "Sending 1");
                sshJcraftWrapper.send("1", ":");
                debugLog.printRTAriDebug(fnName, "Sending 1, the second time");
                sshJcraftWrapper.send("1", "#");
                debugLog.printRTAriDebug(fnName, "Sending paging-options disable");
                sshJcraftWrapper.send("paging-options disable", "#");
                debugLog.printRTAriDebug(fnName, "Sending show config");
                String response = sshJcraftWrapper.send("show config", "#");

                debugLog.printRTAriDebug(fnName, "response is now:'" + response + "'");
                debugLog.printRTAriDebug(fnName, "Populating the ctx object with the response");
                ctx.setAttribute("cliOutput", response);
                sshJcraftWrapper.closeConnection();
                return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_OK, ""));
            } catch (IOException e) {
                debugLog.printRTAriDebug(fnName, "Caught a IOException e=" + e);
                log.debug(fnName + " : Caught a IOException e=" + e);
                sshJcraftWrapper.closeConnection();
                debugLog.printRTAriDebug(fnName, e.getMessage());
                return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage()));
            }
        }
        if ("escapeSql".equals(key)) {
            String data = parameters.get("artifactContents");
            log.debug("ConfigComponentAdaptor.configure - escapeSql");
            data = escapeMySql(data);
            ctx.setAttribute("escapedData", data);
            return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_OK, ""));
        }
        if ("GetCliRunningConfig".equals(key)) {
            debugLog.printRTAriDebug(fnName, "key was: GetCliRunningConfig: ");
            log.debug("key was: GetCliRunningConfig: ");
            String userName = parameters.get(USER_NAME_PARAM_NAME);
            String hostIpAddress = parameters.get(HOST_IP_ADDRESS_PARAM_NAME);
            String password = parameters.get(PASSWORD_PARAM_NAME);
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get(PORT_NUMBER_PARAM_NAME);
            String getConfigTemplate = parameters.get(GET_CONFIG_TEMPLATE_PARAM_NAME);
            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            log.debug("GetCliRunningConfig: sshJcraftWrapper was instantiated");
            debugLog.printRTAriDebug(fnName, "GetCliRunningConfig: sshJcraftWrapper was instantiated");
            try {
                debugLog.printRTAriDebug(fnName,
                    "GetCliRunningConfig: User_name=" + userName + " Host_ip_address=" + hostIpAddress + " Password="
                        + password + " Port_number=" + portNumber);
                log.debug("GetCliRunningConfig: Attempting to login: Host_ip_address=" + hostIpAddress + " User_name="
                    + userName + " Password=" + password + " Port_number=" + portNumber);
                String cliResponse = "";
                boolean showConfigFlag = false;
                sshJcraftWrapper
                    .connect(hostIpAddress, userName, password, "", 30000, Integer.parseInt(portNumber));
                debugLog.printRTAriDebug(fnName, "GetCliRunningConfig: On the VNF device");
                StringTokenizer st = new StringTokenizer(getConfigTemplate, "\n");
                String command = null;
                try {
                    while (st.hasMoreTokens()) {
                        String line = st.nextToken();
                        debugLog.printRTAriDebug(fnName, "line=" + line);
                        if (line.contains("Request:")) {
                            debugLog.printRTAriDebug(fnName, "Found a Request line: line=" + line);
                            command = getStringBetweenQuotes(line);
                            debugLog.printRTAriDebug(fnName, "Sending command=" + command);
                            sshJcraftWrapper.send(command);
                            debugLog.printRTAriDebug(fnName, "command has been sent");
                            if (line.contains("show config")) {
                                showConfigFlag = true;
                                debugLog
                                    .printRTAriDebug(fnName, "GetCliRunningConfig: setting 'showConfigFlag' to true");
                                log.debug("GetCliRunningConfig: GetCliRunningConfig: setting 'showConfigFlag' to true");
                            }
                        }
                        if (line.contains("Response: Ends_With")) {
                            debugLog.printRTAriDebug(fnName, "Found a Response line: line=" + line);
                            String delemeter = getStringBetweenQuotes(line);
                            debugLog.printRTAriDebug(fnName, "The delemeter=" + delemeter);
                            String tmpResponse = sshJcraftWrapper.receiveUntil(delemeter, 120 * 1000, command);
                            if (showConfigFlag) {
                                showConfigFlag = false;
                                StringTokenizer st2 = new StringTokenizer(tmpResponse, "\n");
                                while (st2.hasMoreTokens()) {
                                    String line2 = st2.nextToken();
                                    if (!line2.contains("#")) {
                                        cliResponse += line2 + "\n";
                                    }
                                }
                            }
                        }
                    }
                } catch (NoSuchElementException e) {
                    debugLog.printRTAriDebug(fnName, "Caught a NoSuchElementException: e=" + e);
                }
                debugLog.printRTAriDebug(fnName, "CliResponse=\n" + cliResponse);
                ctx.setAttribute("cliOutput", cliResponse);
                sshJcraftWrapper.closeConnection();
                return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_OK, ""));
            } catch (IOException e) {
                debugLog.printRTAriDebug(fnName, "GetCliRunningConfig: Caught a IOException e=" + e);
                log.debug(fnName + " : GetCliRunningConfig: Caught a IOException e=" + e);
                sshJcraftWrapper.closeConnection();
                debugLog.printRTAriDebug(fnName, "GetCliRunningConfig: Returning error message");
                return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage()));
            }
        }
        if ("xml-download".equals(key)) {
            log(fnName, "key was:  xml-download");
            String userName = parameters.get(USER_NAME_PARAM_NAME);
            String hostIpAddress = parameters.get(HOST_IP_ADDRESS_PARAM_NAME);
            String password = parameters.get(PASSWORD_PARAM_NAME);
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get(PORT_NUMBER_PARAM_NAME);
            String contents = parameters.get("Contents");
            String netconfHelloCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n  <capabilities>\n   <capability>urn:ietf:params:netconf:base:1.0</capability>\n  <capability>urn:com:ericsson:ebase:1.1.0</capability> </capabilities>\n </hello>";
            String terminateConnectionCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n  <rpc message-id=\"terminateConnection\" xmlns:netconf=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n <close-session/> \n </rpc>\n ]]>]]>";
            String commitCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <rpc> <commit/> </rpc>\n ]]>]]>";

            log(fnName,
                "xml-download: User_name=" + userName + " Host_ip_address=" + hostIpAddress + " Password=" + password
                    + " Port_number=" + portNumber);
            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            try {
                sshJcraftWrapper
                    .connect(hostIpAddress, userName, password, "]]>]]>", 30000, Integer.parseInt(portNumber),
                        "netconf");
                log(fnName, "Sending the hello command");
                sshJcraftWrapper.send(netconfHelloCmd + "]]>]]>");
                sshJcraftWrapper.receiveUntil("]]>]]>", 10000, "");
                log(fnName, "Sending xmlCmd cmd");
                String messageId = "1";
                messageId = "\"" + messageId + "\"";
                String loadConfigurationString =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id="
                        + messageId
                        + "> <edit-config> <target> <candidate /> </target> <default-operation>merge</default-operation> <config xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + contents + "</config> </edit-config> </rpc>";
                loadConfigurationString = loadConfigurationString + "]]>]]>";
                sshJcraftWrapper.send(loadConfigurationString);
                debugLog.printRTAriDebug(fnName, ":After sending loadConfigurationString");
                String response = sshJcraftWrapper.receiveUntil("</rpc-reply>", 600000, "");
                HttpResponse httpResponse;
                if (response.contains("rpc-error")) {
                    debugLog.printRTAriDebug(fnName, "Error from device: Response from device had 'rpc-error'");
                    debugLog.printRTAriDebug(fnName, "response=\n" + response + "\n");
                    httpResponse = new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, response);
                } else {
                    debugLog.printRTAriDebug(fnName, ":LoadConfiguration was a success, sending commit cmd");
                    sshJcraftWrapper.send(commitCmd);
                    debugLog.printRTAriDebug(fnName, ":After sending commitCmd");
                    response = sshJcraftWrapper.receiveUntil("</rpc-reply>", 180000, "");
                    if (response.contains("rpc-error")) {
                        debugLog.printRTAriDebug(fnName, "Error from device: Response from device had 'rpc-error'");
                        debugLog.printRTAriDebug(fnName, "response=\n" + response + "\n");
                        httpResponse = new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, response);
                    } else {
                        debugLog.printRTAriDebug(fnName, ":Looks like a success");
                        debugLog.printRTAriDebug(fnName, "response=\n" + response + "\n");
                        httpResponse = new HttpResponse(HttpURLConnection.HTTP_OK, "");
                    }
                }
                sshJcraftWrapper.send(terminateConnectionCmd);
                sshJcraftWrapper.closeConnection();
                return setResponseStatus(ctx, httpResponse);
            } catch (Exception e) {
                log(fnName, "Caught an Exception, e=" + e);
                debugLog.outputStackTrace(e);
                sshJcraftWrapper.closeConnection();
                log(fnName, "Returning error message");
                return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage()));
            }
        }
        if ("xml-getrunningconfig".equals(key)) {
            log(fnName, "key was: : xml-getrunningconfig");
            String xmlGetRunningConfigCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">  <get-config> <source> <running /> </source> </get-config> </rpc>\n";
            String hostIpAddress = parameters.get(HOST_IP_ADDRESS_PARAM_NAME);
            String userName = parameters.get(USER_NAME_PARAM_NAME);
            String password = parameters.get(PASSWORD_PARAM_NAME);
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get(PORT_NUMBER_PARAM_NAME);
            String protocol = parameters.get("Protocol");
            String netconfHelloCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n  <capabilities>\n   <capability>urn:ietf:params:netconf:base:1.0</capability>\n <capability>urn:com:ericsson:ebase:1.1.0</capability> </capabilities>\n </hello>";
            String terminateConnectionCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n  <rpc message-id=\"terminateConnection\" xmlns:netconf=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n <close-session/> \n </rpc>\n ]]>]]>";
            log(fnName,
                "xml-getrunningconfig: User_name=" + userName + " Host_ip_address=" + hostIpAddress + " Password="
                    + password + " Port_number=" + portNumber);
            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            try {
                sshJcraftWrapper
                    .connect(hostIpAddress, userName, password, "]]>]]>", 30000, Integer.parseInt(portNumber),
                        "netconf");
                log(fnName, ":Sending the hello command");
                sshJcraftWrapper.send(netconfHelloCmd + "]]>]]>");
                sshJcraftWrapper.receiveUntil("]]>]]>", 10000, "");
                log(fnName, "Sending get running config command");
                sshJcraftWrapper.send(xmlGetRunningConfigCmd + "]]>]]>\n");
                String response = sshJcraftWrapper.receiveUntil("</rpc-reply>", 180000, "");
                debugLog.printRTAriDebug(fnName, "Response from getRunningconfigCmd=" + response);
                response = trimResponse(response);
                ctx.setAttribute("xmlRunningConfigOutput", response);
                sshJcraftWrapper.send(terminateConnectionCmd);
                sshJcraftWrapper.closeConnection();
                return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_OK, ""));
            } catch (Exception e) {
                log(fnName, "Caught an Exception, e=" + e);
                debugLog.outputStackTrace(e);
                sshJcraftWrapper.closeConnection();
                log(fnName, "Returning error message");
                return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage()));
            }
        }
        if ("DownloadCliConfig".equals(key)) {
            debugLog.printRTAriDebug(fnName, "key was: DownloadCliConfig: ");
            log.debug("key was: DownloadCliConfig: ");
            String userName = parameters.get(USER_NAME_PARAM_NAME);
            String hostIpAddress = parameters.get(HOST_IP_ADDRESS_PARAM_NAME);
            String password = parameters.get(PASSWORD_PARAM_NAME);
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get(PORT_NUMBER_PARAM_NAME);
            String downloadConfigTemplate = parameters.get("Download_config_template");
            String configContents = parameters.get("Config_contents");
            debugLog.printRTAriDebug(fnName, "Contents of the 'Config_contents' are: " + configContents);
            SshJcraftWrapper sshJcraftWrapper = new SshJcraftWrapper();
            log.debug("DownloadCliConfig: sshJcraftWrapper was instantiated");
            debugLog.printRTAriDebug(fnName, "DownloadCliConfig: sshJcraftWrapper was instantiated");
            int timeout = 4 * 60 * 1000;
            try {
                debugLog.printRTAriDebug(fnName,
                    "DownloadCliConfig: User_name=" + userName + " Host_ip_address=" + hostIpAddress + " Password="
                        + password + " Port_number=" + portNumber);
                log.debug("DownloadCliConfig: Attempting to login: Host_ip_address=" + hostIpAddress + " User_name="
                    + userName + " Password=" + password + " Port_number=" + portNumber);
                StringBuilder cliResponseStringBuilder = new StringBuilder();
                sshJcraftWrapper
                    .connect(hostIpAddress, userName, password, "", 30000, Integer.parseInt(portNumber));
                debugLog.printRTAriDebug(fnName, "DownloadCliConfig: On the VNF device");
                StringTokenizer st = new StringTokenizer(downloadConfigTemplate, "\n");
                String command = null;
                String executeConfigContentsDelemeter;
                try {
                    while (st.hasMoreTokens()) {
                        String line = st.nextToken();
                        debugLog.printRTAriDebug(fnName, "line=" + line);
                        if (line.contains("Request:")) {
                            debugLog.printRTAriDebug(fnName, "Found a Request line: line=" + line);
                            command = getStringBetweenQuotes(line);
                            debugLog.printRTAriDebug(fnName, "Sending command=" + command);
                            sshJcraftWrapper.send(command);
                            debugLog.printRTAriDebug(fnName, "command has been sent");
                        } else if ((line.contains("Response: Ends_With")) && (
                            !line.contains("Execute_config_contents Response: Ends_With"))) {
                            debugLog.printRTAriDebug(fnName, "Found a Response line: line=" + line);
                            String delemeter = getStringBetweenQuotes(line);
                            debugLog.printRTAriDebug(fnName, "The delemeter=" + delemeter);
                            String tmpResponse = sshJcraftWrapper.receiveUntil(delemeter, timeout, command);
                            cliResponseStringBuilder.append(tmpResponse);
                        } else if (line.contains("Execute_config_contents Response: Ends_With")) {
                            debugLog
                                .printRTAriDebug(fnName, "Found a 'Execute_config_contents Response:' line=" + line);
                            executeConfigContentsDelemeter = getStringBetweenQuotes(line);
                            debugLog.printRTAriDebug(fnName,
                                "executeConfigContentsDelemeter=" + executeConfigContentsDelemeter);
                            StringTokenizer st2 = new StringTokenizer(configContents, "\n");
                            while (st2.hasMoreTokens()) {
                                String cmd = st2.nextToken();
                                debugLog.printRTAriDebug(fnName, "Config_contents: cmd=" + cmd);
                                sshJcraftWrapper.send(cmd);
                                String tmpResponse = sshJcraftWrapper
                                    .receiveUntil(executeConfigContentsDelemeter, timeout, command);
                                cliResponseStringBuilder.append(tmpResponse);
                            }
                        }
                    }
                } catch (NoSuchElementException e) {
                    debugLog.printRTAriDebug(fnName, "Caught a NoSuchElementException: e=" + e);
                }
                sshJcraftWrapper.closeConnection();
                debugLog.printRTAriDebug(fnName, ":Escaping all the single and double quotes in the response");
                String cliResponse = cliResponseStringBuilder.toString()
                    .replaceAll("\"", "\\\\\"")
                    .replaceAll("\'", "\\\\'");
                debugLog.printRTAriDebug(fnName, "CliResponse=\n" + cliResponseStringBuilder);
                ctx.setAttribute("cliOutput", cliResponse);
                return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_OK, ""));
            } catch (IOException e) {
                debugLog.printRTAriDebug(fnName, "DownloadCliConfig: Caught a IOException e=" + e);
                log.debug(fnName + " : DownloadCliConfig: Caught a IOException e=" + e);
                sshJcraftWrapper.closeConnection();
                debugLog.printRTAriDebug(fnName, "DownloadCliConfig: Returning error message");
                return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage()));
            }
        }

        debugLog.printRTAriDebug(fnName, "Unsupported action - " + action);
        log.error("Unsupported action - " + action);
        return ConfigStatus.FAILURE;
    }

    private void log(String fileName, String messg) {
        debugLog.printRTAriDebug(fileName, messg);
        log.debug(fileName + ": " + messg);
    }

    private ConfigStatus prepare(SvcLogicContext ctx, String requestType, String operation) {
        String templateName = "BASE".equals(requestType) ? "/config-base.xml" : "/config-data.xml";
        String ndTemplate = readTemplateFile(templateName);
        String nd = buildNetworkData2(ctx, ndTemplate, operation);

        String reqTemplate = readTemplateFile("/config-request.xml");
        Map<String, String> param = new HashMap<>();
        param.put("request-id", ctx.getAttribute("service-data.appc-request-header.svc-request-id"));
        param.put("request-type", requestType);
        param.put("callback-url", configCallbackUrl);
        if ("create".equals(operation) || "change".equals(operation) || "scale".equals(operation)) {
            param.put(ACTION_PARAM_NAME, "GenerateOnly");
        }
        param.put("equipment-name", ctx.getAttribute("service-data.service-information.service-instance-id"));
        param.put("equipment-ip-address", ctx.getAttribute("service-data.vnf-config-information.vnf-host-ip-address"));
        param.put("vendor", ctx.getAttribute("service-data.vnf-config-information.vendor"));
        param.put("network-data", nd);

        String req;
        try {
            req = buildXmlRequest(param, reqTemplate);
        } catch (Exception e) {
            log.error("Error building the XML request: ", e);
            return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage()));
        }

        HttpResponse r = sendXmlRequest(req, configUrl, configUser, configPassword);
        return setResponseStatus(ctx, r);
    }

    private ConfigStatus activate(SvcLogicContext ctx, boolean change) {
        String reqTemplate = readTemplateFile("/config-request.xml");
        Map<String, String> param = new HashMap<>();
        param.put("request-id", ctx.getAttribute("service-data.appc-request-header.svc-request-id"));
        param.put("callback-url", configCallbackUrl);
        param.put(ACTION_PARAM_NAME, change ? "DownloadChange" : "DownloadBase");
        param.put("equipment-name", ctx.getAttribute("service-data.service-information.service-instance-id"));

        String req;
        try {
            req = buildXmlRequest(param, reqTemplate);
        } catch (Exception e) {
            log.error("Error building the XML request: ", e);
            return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage()));
        }

        HttpResponse r = sendXmlRequest(req, configUrl, configUser, configPassword);
        return setResponseStatus(ctx, r);
    }

    private ConfigStatus audit(SvcLogicContext ctx, String auditLevel) {
        String reqTemplate = readTemplateFile("/audit-request.xml");
        Map<String, String> param = new HashMap<>();
        param.put("request-id", ctx.getAttribute("service-data.appc-request-header.svc-request-id"));
        param.put("callback-url", auditCallbackUrl);
        param.put("equipment-name", ctx.getAttribute("service-data.service-information.service-instance-id"));
        param.put("audit-level", auditLevel);

        String req;
        try {
            req = buildXmlRequest(param, reqTemplate);
        } catch (Exception e) {
            log.error("Error building the XML request: ", e);
            return setResponseStatus(ctx, new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage()));
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

        return input.replace("\\", "\\\\").replace("\'", "\\'");
    }

    private String readTemplateFile(String fileName) {
        StringBuilder sb = new StringBuilder();

        try (InputStream is = getClass().getResourceAsStream(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line)
                    .append('\n');
            }
        }
        catch (IOException e) {
            log.error("Error reading template file: " + fileName, e);
//            throw new
        }
        return sb.toString();
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
                throw new RuntimeException("Template error: Matching } not found");
            }

            String var1 = template.substring(i1 + 2, i2);
            String value1 = param.get(var1);
            if (value1 == null || value1.trim().length() == 0) {
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

        String newTemplate = expandRepeats(ctx, template, 1);

        Map<String, String> mm = new HashMap<>();
        for (String s : ctx.getAttributeKeySet()) {
            mm.put(s, ctx.getAttribute(s));
        }
        mm.put("operation", operation);

        StringBuilder ss = new StringBuilder();
        int i = 0;
        while (i < newTemplate.length()) {
            int i1 = newTemplate.indexOf("${", i);
            if (i1 < 0) {
                ss.append(newTemplate.substring(i));
                break;
            }

            int i2 = newTemplate.indexOf('}', i1 + 2);
            if (i2 < 0) {
                throw new RuntimeException("Template error: Matching } not found");
            }

            String var1 = newTemplate.substring(i1 + 2, i2);
            String value1 = XmlUtil.getXml(mm, var1);
            if (value1 == null || value1.trim().length() == 0) {
                int i3 = newTemplate.lastIndexOf('\n', i1);
                if (i3 < 0) {
                    i3 = 0;
                }
                int i4 = newTemplate.indexOf('\n', i1);
                if (i4 < 0) {
                    i4 = newTemplate.length();
                }

                if (i < i3) {
                    ss.append(newTemplate.substring(i, i3));
                }
                i = i4;
            } else {
                ss.append(newTemplate.substring(i, i1)).append(value1);
                i = i2 + 1;
            }
        }

        long t2 = System.currentTimeMillis();
        log.info("Building XML completed. Time: " + (t2 - t1));

        return ss.toString();
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
                throw new RuntimeException(
                    "Template error: Context variable name followed by : is required after repeat");
            }

            // Find the closing }, store in i3
            int nn = 1;
            int i3 = -1;
            int i = i2;
            while (nn > 0 && i < template.length()) {
                i3 = template.indexOf('}', i);
                if (i3 < 0) {
                    throw new RuntimeException("Template error: Matching } not found");
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

            String var1 = template.substring(i1 + 9, i2);
            String value1 = ctx.getAttribute(var1);
            log.info("     " + var1 + ": " + value1);
            int n;
            try {
                n = Integer.parseInt(value1);
            } catch (NumberFormatException e) {
                n = 0;
            }

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

            return new HttpResponse(code, message);

        } catch (Exception e) {
            log.error("Error sending the request: ", e);
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage());
        }
    }

    private ConfigStatus setResponseStatus(SvcLogicContext ctx, HttpResponse r) {
        ctx.setAttribute("error-code", String.valueOf(r.getCode()));
        ctx.setAttribute("error-message", r.getMessage());

        return r.getCode() > 299 ? ConfigStatus.FAILURE : ConfigStatus.SUCCESS;
    }

    private String getStringBetweenQuotes(String string) {
        String fnName = "ConfigComponentAdaptor.getStringBetweenQuotes";
        debugLog.printRTAriDebug(fnName, "string=" + string);
        String retString;
        int start = string.indexOf('\"');
        int end = string.lastIndexOf('\"');
        retString = string.substring(start + 1, end);
        debugLog.printRTAriDebug(fnName, "retString=" + retString);
        return retString;
    }

    private String trimResponse(String response) {
        StringTokenizer line = new StringTokenizer(response, "\n");
        StringBuilder sb = new StringBuilder();
        boolean captureText = false;
        while (line.hasMoreTokens()) {
            String token = line.nextToken();
            if (token.contains("<configuration xmlns=")) {
                captureText = true;
            }
            if (captureText) {
                sb.append(token);
                sb.append("\n");
            }
            if (token.contains("</configuration>")) {
                captureText = false;
            }
        }
        return sb.toString();
    }

    private static class HttpResponse {

        private final int code;

        private final String message;

        public HttpResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

    }

    public static void main(String... args) {
        Properties props = null;
        System.out.println("*************************Hello*****************************");
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String getConfigTemplate = readAllLinesFromFile("/home/userID/data/Get_config_template");
        String key = "GetCliRunningConfig";
        Map<String, String> parameters = new HashMap();
        parameters.put(HOST_IP_ADDRESS_PARAM_NAME, "000.00.000.00");
        parameters.put(USER_NAME_PARAM_NAME, "root");
        parameters.put(PASSWORD_PARAM_NAME, "!bootstrap");
        parameters.put(PORT_NUMBER_PARAM_NAME, "22");
        parameters.put(GET_CONFIG_TEMPLATE_PARAM_NAME, getConfigTemplate);
        SvcLogicContext ctx = null;
        System.out.println("*************************TRACE 1*****************************");
        cca.configure(key, parameters, ctx);
    }

    private static String readAllLinesFromFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            in.close();
        } catch (IOException e) {
            log.error("Caught an IOException in method readFile()", e);
        }
        return sb.toString();
    }
}

