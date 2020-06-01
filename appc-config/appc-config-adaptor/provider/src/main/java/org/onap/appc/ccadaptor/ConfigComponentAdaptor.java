/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * =============================================================================
 * Modifications Copyright (C) 2018-2019 IBM.
 * =============================================================================
 * Modifications Copyright (C) 2018-2019 Ericsson
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

package org.onap.appc.ccadaptor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.glassfish.jersey.oauth1.signature.Base64;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class ConfigComponentAdaptor implements SvcLogicAdaptor {

    private static EELFLogger log = EELFManager.getInstance().getLogger(ConfigComponentAdaptor.class);
    DebugLog debugLog = new DebugLog();
    private String configUrl = null;
    private String configUser = null;
    private String configPassword = null;
    private String auditUrl = null;
    private String auditUser = null;
    private String auditPassword = null;

    public String getConfigUrl() {
        return configUrl;
    }

    public String getConfigUser() {
        return configUser;
    }

    public String getConfigPassword() {
        return configPassword;
    }

    public String getAuditUrl() {
        return auditUrl;
    }

    public String getAuditUser() {
        return auditUser;
    }

    public String getAuditPassword() {
        return auditPassword;
    }

    public String getConfigCallbackUrl() {
        return configCallbackUrl;
    }

    public String getAuditCallbackUrl() {
        return auditCallbackUrl;
    }

    private String configCallbackUrl = null;
    private String auditCallbackUrl = null;
    private int DEFAULT_TIMEOUT_GETRUNNING_CLI = 120 * 1000 ;

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
        } else {
            configUrl = "";
            configUser = "";
            configPassword = "";
            auditUrl = "";
            auditUser = "";
            auditPassword = "";
            configCallbackUrl = "";
            auditCallbackUrl = "";
        }
    }

    @Override
    public ConfigStatus configure(String key, Map<String, String> parameters, SvcLogicContext ctx) {
        String fnName = "ConfigComponentAdaptor.configure";
        HttpResponse r = new HttpResponse();
        r.code = 200;
        log.debug("ConfigComponentAdaptor.configure - key = " + key);
        DebugLog.printRTAriDebug(fnName, "key = " + key);
        log.debug("Parameters:");
        DebugLog.printRTAriDebug(fnName, "Parameters:");
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            log.debug("        " + entry.getKey() + " = " + entry.getValue());
            DebugLog.printRTAriDebug(fnName, "        " + entry.getKey() + " = " + entry.getValue());
        }

        String parmval = parameters.get("config-component-configUrl");
        if ((parmval != null) && (parmval.length() > 0)) {
            log.debug("Overwriting URL with " + parmval);
            configUrl = parmval;
        }

        parmval = parameters.get("config-component-configPassword");
        if ((parmval != null) && (parmval.length() > 0)) {
            //log.debug("Overwriting configPassword with " + parmval);
            configPassword = parmval;
        }

        parmval = parameters.get("config-component-configUser");
        if ((parmval != null) && (parmval.length() > 0)) {
            log.debug("Overwriting configUser id with " + parmval);
            configUser = parmval;
        }

        String action = parameters.get("action");

        String chg = ctx.getAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name");
        if (chg != null && "prepare".equalsIgnoreCase(action)) {
            return prepare(ctx, "CHANGE", "change");
        }
        if (chg != null && "activate".equalsIgnoreCase(action)) {
            return activate(ctx, true);
        }

        String scale = ctx.getAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].scale-configuration[0].network-type");
        if (scale != null && "prepare".equalsIgnoreCase(action)) {
            return prepare(ctx, "CHANGE", "scale");
        }
        if (scale != null && "activate".equalsIgnoreCase(action)) {
            return activate(ctx, true);
        }

        if ("prepare".equalsIgnoreCase(action)) {
            return prepare(ctx, "BASE", "create");
        }
        if ("activate".equalsIgnoreCase(action)) {
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
            String data = null;

            SshJcraftWrapper sshJcraftWrapper = getSshJcraftWrapper();
            log.debug("SCP: SshJcraftWrapper has been instantiated");
            DebugLog.printRTAriDebug(fnName, "SCP: SshJcraftWrapper has been instantiated");
            try {
                if (key.equals("put")) {
                    data = parameters.get("data");
                    if (data != null) {
                        DebugLog.printRTAriDebug(fnName, "Command is for put: Length of data is: " + data.length());
                        InputStream is = new ByteArrayInputStream(data.getBytes());
                        log.debug("SCP: Doing a put: fullPathFileName=" + fullPathFileName);
                        DebugLog.printRTAriDebug(fnName, "SCP: Doing a put: fullPathFileName=" + fullPathFileName);
                        sshJcraftWrapper.put(is, fullPathFileName, host, loginId, password);
                        try {
                            DebugLog.printRTAriDebug(fnName, "Sleeping for 180 seconds....");
                            Thread.sleep(1000l * 180);
                            DebugLog.printRTAriDebug(fnName, "Woke up....");
                        } catch (java.lang.InterruptedException ee) {
                            boolean ignore = true;
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                        log.debug(fnName + " Command is for put: data is null");
                    }
                } else     // Must be a get
                {
                    log.debug("SCP: Doing a get: fullPathFileName=" + fullPathFileName);
                    DebugLog.printRTAriDebug(fnName, "SCP: Doing a get: fullPathFileName=" + fullPathFileName);
                    String response = sshJcraftWrapper.get(fullPathFileName, host, loginId, password);
                    DebugLog.printRTAriDebug(fnName, "Got the response and putting into the ctx object");
                    ctx.setAttribute("fileContents", response);
                    log.debug("SCP: Closing the SFTP connection");
                }
                return (setResponseStatus(ctx, r));
            } catch (IOException e) {
                DebugLog.printAriDebug(fnName, "Caught a IOException e=" + e);
                log.debug(fnName + " : Caught a IOException e=" + e);
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                return (setResponseStatus(ctx, r));
            }
        }
        if (key.equals("cli")) {
            String loginId = parameters.get("loginId");
            String host = parameters.get("host");
            String password = parameters.get("password");
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get("portNumber");
            SshJcraftWrapper sshJcraftWrapper = getSshJcraftWrapper();
            try {
                log.debug("CLI: Attempting to login: host=" + host + " loginId=" + loginId +
                        " portNumber=" + portNumber);
                DebugLog.printRTAriDebug(fnName, "CLI: Attempting to login: host=" + host + " loginId=" + loginId +
                        " portNumber=" + portNumber);
                sshJcraftWrapper.connect(host, loginId, password, Integer.parseInt(portNumber));

                DebugLog.printAriDebug(fnName, "Sending 'sdc'");
                String response = sshJcraftWrapper.send("sdc", ":");
                DebugLog.printAriDebug(fnName, "Sending 1");
                response = sshJcraftWrapper.send("1", ":");
                DebugLog.printAriDebug(fnName, "Sending 1, the second time");
                response = sshJcraftWrapper.send("1", "#");
                DebugLog.printAriDebug(fnName, "Sending paging-options disable");
                response = sshJcraftWrapper.send("paging-options disable", "#");
                DebugLog.printAriDebug(fnName, "Sending show config");
                response = sshJcraftWrapper.send("show config", "#");

                DebugLog.printAriDebug(fnName, "response is now:'" + response + "'");
                DebugLog.printAriDebug(fnName, "Populating the ctx object with the response");
                ctx.setAttribute("cliOutput", response);
                sshJcraftWrapper.closeConnection();
                r.code = 200;
                sshJcraftWrapper = null;
                return (setResponseStatus(ctx, r));
            } catch (IOException e) {
                DebugLog.printAriDebug(fnName, "Caught a IOException e=" + e);
                log.debug(fnName + " : Caught a IOException e=" + e);
                sshJcraftWrapper.closeConnection();
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                sshJcraftWrapper = null;
                DebugLog.printAriDebug(fnName, "Returning error message");
                return (setResponseStatus(ctx, r));
            }
        }
        if (key.equals("escapeSql")) {
            String data = parameters.get("artifactContents");
            log.debug("ConfigComponentAdaptor.configure - escapeSql");
            data = escapeMySql(data);
            ctx.setAttribute("escapedData", data);
            return (setResponseStatus(ctx, r));
        }
        if (key.equals("GetCliRunningConfig")) {
            DebugLog.printRTAriDebug(fnName, "key was: GetCliRunningConfig: ");
            log.debug("key was: GetCliRunningConfig: ");
            String userName = parameters.get("User_name");
            String hostIpAddress = parameters.get("Host_ip_address");
            String password = parameters.get("Password");
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get("Port_number");
            String getConfigTemplate = parameters.get("Get_config_template");
            SshJcraftWrapper sshJcraftWrapper = getSshJcraftWrapper();
            log.debug("GetCliRunningConfig: sshJcraftWrapper was instantiated");
            DebugLog.printRTAriDebug(fnName, "GetCliRunningConfig: sshJcraftWrapper was instantiated");
            try {
                DebugLog.printAriDebug(fnName, "GetCliRunningConfig: User_name=" + userName +
                        " Host_ip_address=" + hostIpAddress + " Port_number="
                        + portNumber);
                log.debug("GetCliRunningConfig: Attempting to login: Host_ip_address=" + hostIpAddress +
                        " User_name=" + userName + " Port_number=" + portNumber);
                String response = "";
                String CliResponse = "";
                boolean showConfigFlag = false;
                sshJcraftWrapper.connect(hostIpAddress,
                        userName,
                        password,
                        "",
                        30000,
                        Integer.parseInt(portNumber));
                DebugLog.printAriDebug(fnName, "GetCliRunningConfig: On the VNF device");
                StringTokenizer st = new StringTokenizer(getConfigTemplate, "\n");
                String command = null;
                try {
                    while (st.hasMoreTokens()) {
                        String line = st.nextToken();
                        DebugLog.printAriDebug(fnName, "line=" + line);
                        if (line.indexOf("Request:") != -1) {
                            DebugLog.printAriDebug(fnName, "Found a Request line: line=" + line);
                            command = getStringBetweenQuotes(line);
                            DebugLog.printAriDebug(fnName, "Sending command=" + command);
                            sshJcraftWrapper.send(command);
                            DebugLog.printAriDebug(fnName, "command has been sent");
                            if (line.indexOf("show config") != -1) {
                                showConfigFlag = true;
                                DebugLog.printAriDebug(fnName, "GetCliRunningConfig: setting 'showConfigFlag' to true");
                                log.debug("GetCliRunningConfig: GetCliRunningConfig: setting 'showConfigFlag' to true");
                            }
                        }
                        if (line.indexOf("Response: Ends_With") != -1) {
                            DebugLog.printAriDebug(fnName, "Found a Response line: line=" + line);
                            String delemeter = getStringBetweenQuotes(line);
                            DebugLog.printAriDebug(fnName, "The delemeter=" + delemeter);
                            //DEFAULT_TIMEOUT_GETRUNNING_CLI : changed the default time out to 2 mins in 1806
                            String tmpResponse = sshJcraftWrapper.receiveUntil(delemeter,
                                    DEFAULT_TIMEOUT_GETRUNNING_CLI, command);
                            response += tmpResponse;
                            if (showConfigFlag) {
                                showConfigFlag = false;
                                StringTokenizer st2 = new StringTokenizer(tmpResponse, "\n");
                                //    Strip off the last line which is the command prompt from the VNF device.
                                while (st2.hasMoreTokens()) {
                                    String line2 = st2.nextToken();
                                    if (line2.indexOf('#') == -1) {
                                        CliResponse += line2 + "\n";
                                    }
                                }
                            }
                        }
                    }
                } catch (NoSuchElementException e) {
                    DebugLog.printAriDebug(fnName, "Caught a NoSuchElementException: e=" + e);
                }
                DebugLog.printAriDebug(fnName, "CliResponse=\n" + CliResponse);
                ctx.setAttribute("cliOutput", CliResponse);
                sshJcraftWrapper.closeConnection();
                r.code = 200;
                sshJcraftWrapper = null;
                return (setResponseStatus(ctx, r));
            } catch (IOException e) {
                DebugLog.printAriDebug(fnName, "GetCliRunningConfig: Caught a IOException e=" + e);
                log.debug(fnName + " : GetCliRunningConfig: Caught a IOException e=" + e);
                sshJcraftWrapper.closeConnection();
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                sshJcraftWrapper = null;
                DebugLog.printAriDebug(fnName, "GetCliRunningConfig: Returning error message");
                return (setResponseStatus(ctx, r));
            }
        }
        if (key.equals("xml-download")) {
            log(fnName, "key was:    xml-download");
            String userName = parameters.get("User_name");
            String hostIpAddress = parameters.get("Host_ip_address");
            String password = parameters.get("Password");
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get("Port_number");
            String Contents = parameters.get("Contents");
            String netconfHelloCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n    <capabilities>\n   <capability>urn:ietf:params:netconf:base:1.0</capability>\n  <capability>urn:com:ericsson:ebase:1.1.0</capability> </capabilities>\n </hello>";
            String terminateConnectionCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n    <rpc message-id=\"terminateConnection\" xmlns:netconf=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n <close-session/> \n </rpc>\n ]]>]]>";
            String commitCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"> <commit/> </rpc>\n ]]>]]>";

            log(fnName,
                    "xml-download: User_name=" + userName + " Host_ip_address=" + hostIpAddress
                    + " Port_number=" + portNumber);
            SshJcraftWrapper sshJcraftWrapper = getSshJcraftWrapper();
            try {
                sshJcraftWrapper.connect(hostIpAddress,
                        userName,
                        password,
                        "]]>]]>",
                        30000,
                        Integer.parseInt(portNumber),
                        "netconf");
                String NetconfHelloCmd = netconfHelloCmd;
                NetconfHelloCmd = NetconfHelloCmd + "]]>]]>";
                log(fnName, "Sending the hello command");
                sshJcraftWrapper.send(NetconfHelloCmd);
                String response = sshJcraftWrapper.receiveUntil("]]>]]>", 10000, "");
                log(fnName, "Sending xmlCmd cmd");
                String xmlCmd = Contents;
                String messageId = "1";
                messageId = "\"" + messageId + "\"";
                String loadConfigurationString =
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id="
                                + messageId
                                + "> <edit-config> <target> <candidate /> </target> <default-operation>merge</default-operation> <config xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                                + xmlCmd + "</config> </edit-config> </rpc>";
                loadConfigurationString = loadConfigurationString + "]]>]]>";
                sshJcraftWrapper.send(loadConfigurationString);
                DebugLog.printAriDebug(fnName, ":After sending loadConfigurationString");
                response = sshJcraftWrapper.receiveUntil("</rpc-reply>", 600000, "");
                if (response.indexOf("rpc-error") != -1) {
                    DebugLog.printAriDebug(fnName, "Error from device: Response from device had 'rpc-error'");
                    DebugLog.printAriDebug(fnName, "response=\n" + response + "\n");
                    r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                    r.message = response;
                } else {
                    DebugLog.printAriDebug(fnName, ":LoadConfiguration was a success, sending commit cmd");
                    sshJcraftWrapper.send(commitCmd);
                    DebugLog.printAriDebug(fnName, ":After sending commitCmd");
                    response = sshJcraftWrapper.receiveUntil("</rpc-reply>", 180000, "");
                    if (response.indexOf("rpc-error") != -1) {
                        DebugLog.printAriDebug(fnName, "Error from device: Response from device had 'rpc-error'");
                        DebugLog.printAriDebug(fnName, "response=\n" + response + "\n");
                        r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                        r.message = response;
                    } else {
                        DebugLog.printAriDebug(fnName, ":Looks like a success");
                        DebugLog.printAriDebug(fnName, "response=\n" + response + "\n");
                        r.code = 200;
                    }
                }
                sshJcraftWrapper.send(terminateConnectionCmd);
                sshJcraftWrapper.closeConnection();
                sshJcraftWrapper = null;
                return (setResponseStatus(ctx, r));
            } catch (Exception e) {
                log(fnName, "Caught an Exception, e=" + e);
                log(fnName, "StackTrace=" + DebugLog.getStackTraceString(e));
                sshJcraftWrapper.closeConnection();
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                sshJcraftWrapper = null;
                log(fnName, "Returning error message");
                return (setResponseStatus(ctx, r));
            }
        }
        if (key.equals("xml-getrunningconfig")) {
            log(fnName, "key was: : xml-getrunningconfig");
            String xmlGetRunningConfigCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">    <get-config> <source> <running /> </source> </get-config> </rpc>\n";
            String hostIpAddress = parameters.get("Host_ip_address");
            String userName = parameters.get("User_name");
            String password = parameters.get("Password");
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get("Port_number");
            String Protocol = parameters.get("Protocol");
            String netconfHelloCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n    <capabilities>\n   <capability>urn:ietf:params:netconf:base:1.0</capability>\n <capability>urn:com:ericsson:ebase:1.1.0</capability> </capabilities>\n </hello>";
            String terminateConnectionCmd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n    <rpc message-id=\"terminateConnection\" xmlns:netconf=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n <close-session/> \n </rpc>\n ]]>]]>";
            log(fnName,
                    "xml-getrunningconfig: User_name=" + userName + " Host_ip_address=" + hostIpAddress
                    + " Port_number=" + portNumber);
            SshJcraftWrapper sshJcraftWrapper = getSshJcraftWrapper();
            try {
                String NetconfHelloCmd = netconfHelloCmd;
                sshJcraftWrapper.connect(hostIpAddress,
                        userName,
                        password,
                        "]]>]]>",
                        30000,
                        Integer.parseInt(portNumber),
                        "netconf");
                NetconfHelloCmd = NetconfHelloCmd + "]]>]]>";
                log(fnName, ":Sending the hello command");
                sshJcraftWrapper.send(NetconfHelloCmd);
                String response = sshJcraftWrapper.receiveUntil("]]>]]>", 10000, "");
                log(fnName, "Sending get running config command");
                sshJcraftWrapper.send(xmlGetRunningConfigCmd + "]]>]]>\n");
                response = sshJcraftWrapper.receiveUntil("</rpc-reply>", 180000, "");
                DebugLog.printAriDebug(fnName, "Response from getRunningconfigCmd=" + response);
                response = trimResponse(response);
                ctx.setAttribute("xmlRunningConfigOutput", response);
                sshJcraftWrapper.send(terminateConnectionCmd);
                sshJcraftWrapper.closeConnection();
                r.code = 200;
                sshJcraftWrapper = null;
                return (setResponseStatus(ctx, r));
            } catch (Exception e) {
                log(fnName, "Caught an Exception, e=" + e);
                log(fnName, "StackTrace=" + DebugLog.getStackTraceString(e));
                sshJcraftWrapper.closeConnection();
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                sshJcraftWrapper = null;
                log(fnName, "Returning error message");
                return (setResponseStatus(ctx, r));
            }
        }
        if (key.equals("DownloadCliConfig")) {
            DebugLog.printRTAriDebug(fnName, "key was: DownloadCliConfig: ");
            log.debug("key was: DownloadCliConfig: ");
            String userName = parameters.get("User_name");
            String hostIpAddress = parameters.get("Host_ip_address");
            String password = parameters.get("Password");
            password = EncryptionTool.getInstance().decrypt(password);
            String portNumber = parameters.get("Port_number");
            String downloadConfigTemplate = parameters.get("Download_config_template");
            String Config_contents = parameters.get("Config_contents");
            DebugLog.printAriDebug(fnName, "Contents of the 'Config_contents' are: " + Config_contents);
            SshJcraftWrapper sshJcraftWrapper = getSshJcraftWrapper();
            log.debug("DownloadCliConfig: sshJcraftWrapper was instantiated");
            DebugLog.printRTAriDebug(fnName, "DownloadCliConfig: sshJcraftWrapper was instantiated");
            int timeout = 4 * 60 * 1000;
            try {
                DebugLog.printAriDebug(fnName, "DownloadCliConfig: User_name=" + userName +
                        " Host_ip_address=" + hostIpAddress + " Port_number="
                        + portNumber);
                log.debug("DownloadCliConfig: Attempting to login: Host_ip_address=" + hostIpAddress +
                        " User_name=" + userName + " Port_number=" + portNumber);
                String CliResponse = "";
                sshJcraftWrapper.connect(hostIpAddress,
                        userName,
                        password,
                        "",
                        30000,
                        Integer.parseInt(portNumber));
                DebugLog.printAriDebug(fnName, "DownloadCliConfig: On the VNF device");
                StringTokenizer st = new StringTokenizer(downloadConfigTemplate, "\n");
                String command = null;
                String executeConfigContentsDelemeter = null;
                try {
                    while (st.hasMoreTokens()) {
                        String line = st.nextToken();
                        DebugLog.printAriDebug(fnName, "line=" + line);
                        if (line.indexOf("Request:") != -1) {
                            DebugLog.printAriDebug(fnName, "Found a Request line: line=" + line);
                            command = getStringBetweenQuotes(line);
                            DebugLog.printAriDebug(fnName, "Sending command=" + command);
                            sshJcraftWrapper.send(command);
                            DebugLog.printAriDebug(fnName, "command has been sent");
                        } else if ((line.indexOf("Response: Ends_With") != -1) && (
                                line.indexOf("Execute_config_contents Response: Ends_With") == -1)) {
                            DebugLog.printAriDebug(fnName, "Found a Response line: line=" + line);
                            String delemeter = getStringBetweenQuotes(line);
                            DebugLog.printAriDebug(fnName, "The delemeter=" + delemeter);
                            String tmpResponse = sshJcraftWrapper.receiveUntil(delemeter, timeout, command);
                            CliResponse += tmpResponse;
                        } else if (line.indexOf("Execute_config_contents Response: Ends_With") != -1) {
                            DebugLog.printAriDebug(fnName, "Found a 'Execute_config_contents Response:' line=" + line);
                            executeConfigContentsDelemeter = getStringBetweenQuotes(line);
                            DebugLog.printAriDebug(fnName,
                                    "executeConfigContentsDelemeter=" + executeConfigContentsDelemeter);
                            StringTokenizer st2 = new StringTokenizer(Config_contents, "\n");
                            while (st2.hasMoreTokens()) {
                                String cmd = st2.nextToken();
                                DebugLog.printAriDebug(fnName, "Config_contents: cmd=" + cmd);
                                sshJcraftWrapper.send(cmd);
                                String tmpResponse = sshJcraftWrapper.receiveUntil(executeConfigContentsDelemeter,
                                        timeout,
                                        command);
                                CliResponse += tmpResponse;
                            }
                        }
                    }
                } catch (NoSuchElementException e) {
                    DebugLog.printAriDebug(fnName, "Caught a NoSuchElementException: e=" + e);
                }
                sshJcraftWrapper.closeConnection();
                sshJcraftWrapper = null;
                DebugLog.printAriDebug(fnName, ":Escaping all the single and double quotes in the response");
                CliResponse = CliResponse.replaceAll("\"", "\\\\\"");
                CliResponse = CliResponse.replaceAll("\'", "\\\\'");
                DebugLog.printAriDebug(fnName, "CliResponse=\n" + CliResponse);
                ctx.setAttribute("cliOutput", CliResponse);
                r.code = 200;
                return (setResponseStatus(ctx, r));
            } catch (IOException e) {
                DebugLog.printAriDebug(fnName, "DownloadCliConfig: Caught a IOException e=" + e);
                log.debug(fnName + " : DownloadCliConfig: Caught a IOException e=" + e);
                sshJcraftWrapper.closeConnection();
                r.code = HttpURLConnection.HTTP_INTERNAL_ERROR;
                r.message = e.getMessage();
                sshJcraftWrapper = null;
                DebugLog.printAriDebug(fnName, "DownloadCliConfig: Returning error message");
                return (setResponseStatus(ctx, r));
            }
        }

        DebugLog.printRTAriDebug(fnName, "Unsupported action - " + action);
        log.error("Unsupported action - " + action);
        return ConfigStatus.FAILURE;
    }

    private void log(String fileName, String messg) {
        DebugLog.printRTAriDebug(fileName, messg);
        log.debug(fileName + ": " + messg);
    }

    private ConfigStatus prepare(SvcLogicContext ctx, String requestType, String operation) {
        String templateName = requestType.equals("BASE") ? "/config-base.xml" : "/config-data.xml";
        String ndTemplate = readFile(templateName);
        String nd = buildNetworkData2(ctx, ndTemplate, operation);

        String reqTemplate = readFile("/config-request.xml");
        Map<String, String> param = new HashMap<String, String>();
        param.put("request-id", ctx.getAttribute("service-data.appc-request-header.svc-request-id"));
        param.put("request-type", requestType);
        param.put("callback-url", configCallbackUrl);
        if (operation.equals("create") || operation.equals("change") || operation.equals("scale")) {
            param.put("action", "GenerateOnly");
        }
        param.put("equipment-name", ctx.getAttribute("service-data.service-information.service-instance-id"));
        param.put("equipment-ip-address", ctx.getAttribute("service-data.vnf-config-information.vnf-host-ip-address"));
        param.put("vendor", ctx.getAttribute("service-data.vnf-config-information.vendor"));
        param.put("network-data", nd);

        String req = null;
        try {
            req = buildXmlRequest(param, reqTemplate);
        } catch (Exception e) {
            log.error("Error building the XML request: ", e);

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
        Map<String, String> param = new HashMap<String, String>();
        param.put("request-id", ctx.getAttribute("service-data.appc-request-header.svc-request-id"));
        param.put("callback-url", configCallbackUrl);
        param.put("action", change ? "DownloadChange" : "DownloadBase");
        param.put("equipment-name", ctx.getAttribute("service-data.service-information.service-instance-id"));

        String req = null;
        try {
            req = buildXmlRequest(param, reqTemplate);
        } catch (Exception e) {
            log.error("Error building the XML request: ", e);

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
        Map<String, String> param = new HashMap<String, String>();
        param.put("request-id", ctx.getAttribute("service-data.appc-request-header.svc-request-id"));
        param.put("callback-url", auditCallbackUrl);
        param.put("equipment-name", ctx.getAttribute("service-data.service-information.service-instance-id"));
        param.put("audit-level", auditLevel);

        String req = null;
        try {
            req = buildXmlRequest(param, reqTemplate);
        } catch (Exception e) {
            log.error("Error building the XML request: ", e);

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

        input = input.replace("\\", "\\\\");
        input = input.replace("\'", "\\'");

        return input;
    }

    protected String readFile(String fileName) {
        InputStream is = getClass().getResourceAsStream(fileName);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader in = new BufferedReader(isr);
        StringBuilder ss = new StringBuilder();
        try {
            String s = in.readLine();
            while (s != null) {
                ss.append(s).append('\n');
                s = in.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error reading " + fileName + ": " + e.getMessage());
            throw new RuntimeException("Error reading " + fileName + ": " + e.getMessage(), e);
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
        return ss.toString();
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

        template = expandRepeats(ctx, template, 1);

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
                throw new RuntimeException("Template error: Matching } not found");
            }

            String var1 = template.substring(i1 + 2, i2);
            String value1 = XmlUtil.getXml(mm, var1);
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
            log.info("         " + var1 + ": " + value1);
            int n = 0;
            try {
                n = Integer.parseInt(value1);
            } catch (Exception e) {
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
            Client client = getClient();
            WebTarget webResource = client.target(url);

            log.info("SENDING...............");
            if (log.isTraceEnabled()) {
                log.trace(xmlRequest);
            }
            else {
                if(xmlRequest.length() > 255 ) {
                    log.info(xmlRequest.substring(0, 255));
                    log.info("\n...\n" + xmlRequest.length() +
                            " characters in request, turn on TRACE logging to log entire request");
                }
                else {
                    log.info(xmlRequest);
                }
            }
            String authString = user + ":" + password;
            String authStringEnc = Base64.encode(authString.getBytes());
            authString = "Basic " + authStringEnc;

            Response response = getClientResponse(webResource, authString, xmlRequest);

            int code = response.getStatus();
            String message = response.getStatusInfo().getReasonPhrase();
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
        String fnName = "ConfigComponentAdaptor.getStringBetweenQuotes";
        DebugLog.printAriDebug(fnName, "string=" + string);
        String retString = null;
        int start = string.indexOf('\"');
        int end = string.lastIndexOf('\"');
        retString = string.substring(start + 1, end);
        DebugLog.printAriDebug(fnName, "retString=" + retString);
        return (retString);
    }

    public static String _readFile(String fileName) {
        StringBuffer strBuff = new StringBuffer();
        String line;
        try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
            while ((line = in.readLine()) != null) {
                strBuff.append(line + "\n");
            }
        } catch (IOException e) {
            System.out.println("Caught an IOException in method readFile(): e=" + e.toString());
        }
        return (strBuff.toString());
    }

  /**
   * A supporting method to extract the data and configuration element from the NETCONF-XML response
   * from the node and create well formatted xml.
   *
   * @param requestData The unformatted NETCONF-XML
   * @return responseBuilder the extracted data
   * @throws APPCException Exception during parsing xml data
   */
  private String extractFromNetconfXml(String requestData) throws APPCException {
    log.debug("extractFromNetconfXml -- Start");
    StringBuilder responseBuilder = new StringBuilder();
    StringWriter stringBuffer = new StringWriter();
    try {
      requestData = requestData.replaceAll("]]>]]>", "");
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(new InputSource(new StringReader(requestData)));
      document.getDocumentElement().normalize();
      XPath xPath = XPathFactory.newInstance().newXPath();
      Node dataNode = (Node) xPath.evaluate("//data", document, XPathConstants.NODE);
      Transformer xform = TransformerFactory.newInstance().newTransformer();
      xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      xform.setOutputProperty(OutputKeys.INDENT, "yes");
      xform.transform(new DOMSource(dataNode), new StreamResult(stringBuffer));
      responseBuilder.append(stringBuffer.toString());
      if (log.isTraceEnabled()) {
        log.trace("ConfigComponentAdaptor:extractFromNetconfXml after Extract: "
            + responseBuilder.toString());
      }
    } catch (SAXException | IOException | ParserConfigurationException | TransformerException
        | XPathExpressionException exception) {
      throw new APPCException("Error Occured during parsing Netconf-XML", exception);

    }
    log.debug("extractFromNetconfXml -- End");
    return responseBuilder.toString();
  }

  private String trimResponse(String response) throws APPCException {
        response = extractFromNetconfXml(response);
        log.debug("runningConfig before trimResponse : " + response);
        StringTokenizer line = new StringTokenizer(response, "\n");
        StringBuffer sb = new StringBuffer();
        String runningConfig = "" ;
        boolean captureText = false;
        while (line.hasMoreTokens()) {
            String token = line.nextToken();
            if (token.indexOf("<configuration xmlns=") != -1) {
                captureText = true;
            }else if(token.indexOf("<data") != -1) {
                log.debug("token-line:with in Data: "+token);
                captureText = true;
                continue;
            }

            if(token.indexOf("</data>") != -1) {
                log.debug("token-line:with in </data>"+token);
                captureText = false;
            }
            if (captureText) {
                sb.append(token + "\n");
            }
            if (token.indexOf("</configuration>") != -1) {
                captureText = false;
            }
        }
        runningConfig = sb.toString();

        if (log.isTraceEnabled()) {
            log.trace("ConfigComponentAdaptor:RunningConfig after trimResponse : " + runningConfig);
        }
        else {
            if (runningConfig.length() > 255) {
                log.info("ConfigComponentAdaptor:RunningConfig after trimResponse : " +
                        runningConfig.substring(0, 255));
                log.info("\n...\n" + runningConfig.length() +
                        " characters in config, turn on TRACE logging to log entire config");
            }
            else {
                log.info("ConfigComponentAdaptor:RunningConfig after trimResponse : " +
                        runningConfig);
            }
        }
        log.info("ConfigComponentAdaptor:RunningConfig after trimResponse : " + runningConfig);
        return runningConfig;
    }

    public static void main(String args[]) throws Exception {
        Properties props = null;
        System.out.println("*************************Hello*****************************");
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String getConfigTemplate = _readFile("/home/userID/data/Get_config_template");
        String downloadConfigTemplate = _readFile("/home/userID/data/Download_config_template_2");
        String key = "GetCliRunningConfig";
        Map<String, String> parameters = new HashMap();
        parameters.put("Host_ip_address", "000.00.000.00");
        parameters.put("User_name", "root");
        parameters.put("Password", "!bootstrap");
        parameters.put("Port_number", "22");
        parameters.put("Get_config_template", getConfigTemplate);

        SvcLogicContext ctx = null;
        System.out.println("*************************TRACE 1*****************************");
        cca.configure(key, parameters, ctx);
    }

    protected SshJcraftWrapper getSshJcraftWrapper() {
        return new SshJcraftWrapper();
    }

    protected Client getClient() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 5000);
        return ClientBuilder.newClient(clientConfig);
    }

    protected Response getClientResponse(WebTarget webResource, String authString, String xmlRequest) {
        return webResource.request("UTF-8").header("Authorization", authString).header("Content-Type", "application/xml").post(
                Entity.xml(xmlRequest),Response.class);
    }
}
