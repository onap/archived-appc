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

package org.openecomp.appc.adapter.netconf.odlconnector;

import org.apache.http.HttpStatus;
import org.openecomp.appc.adapter.netconf.NetconfClient;
import org.openecomp.appc.adapter.netconf.NetconfClientRestconf;
import org.openecomp.appc.adapter.netconf.NetconfConnectionDetails;
import org.openecomp.appc.adapter.netconf.util.Constants;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.util.httpClient;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.Properties;

public class NetconfClientRestconfImpl implements NetconfClient, NetconfClientRestconf {

    private EELFLogger logger = EELFManager.getInstance().getLogger(NetconfClientRestconfImpl.class);

    private NetconfConnectionDetails connectionDetails;

    //constructor
    public NetconfClientRestconfImpl(){
    }

    //restconf client impl

    @SuppressWarnings("deprecation")
    @Override
    public void configure(String configuration, String deviceMountPointName, String moduleName, String nodeName) throws APPCException {

        logger.info("Configuring device "+deviceMountPointName+" with configuration "+configuration);

        int httpCode = httpClient.putMethod(Constants.PROTOCOL,Constants.CONTROLLER_IP,Constants.CONTROLLER_PORT,getModuleConfigurePath(deviceMountPointName, moduleName, nodeName),configuration,"application/json");

        if (httpCode != HttpStatus.SC_OK) {
            logger.error("Configuration request failed. throwing Exception !");
            throw new APPCException("Error configuring node :"+nodeName + ", of Module :" + moduleName + ", in device :" + deviceMountPointName);
        }
    }

    @Override
    public void connect(String deviceMountPointName, String payload) throws APPCException{

        logger.info("Connecting device "+deviceMountPointName);

        int httpCode = httpClient.postMethod(Constants.PROTOCOL,Constants.CONTROLLER_IP,Constants.CONTROLLER_PORT,getConnectPath(),payload,"application/json");

        if(httpCode != HttpStatus.SC_NO_CONTENT){
            logger.error("Connect request failed with code "+httpCode+". throwing Exception !");
            throw new APPCException("Error connecting device :" + deviceMountPointName);
        }
    }

    @Override
    public boolean checkConnection(String deviceMountPointName) throws APPCException {
        logger.info("Checking device "+deviceMountPointName+" connectivity");

        String result = httpClient.getMethod(Constants.PROTOCOL,Constants.CONTROLLER_IP,Constants.CONTROLLER_PORT,getCheckConnectivityPath(deviceMountPointName),"application/json");

        return result != null;
    }

    @Override
    public void disconnect(String deviceMountPointName) throws APPCException {
        logger.info("Disconnecting "+deviceMountPointName);

        int httpCode = httpClient.deleteMethod(Constants.PROTOCOL,Constants.CONTROLLER_IP,Constants.CONTROLLER_PORT,getDisconnectPath(deviceMountPointName),"application/json");

        if(httpCode != HttpStatus.SC_OK){
            logger.error("Disconnection of device "+deviceMountPointName+" failed!");
            throw new APPCException("Disconnection of device "+deviceMountPointName+" failed!");
        }
    }

    @Override
    public String getConfiguration(String deviceMountPointName, String moduleName, String nodeName) throws APPCException{
        logger.info("Getting configuration of device "+deviceMountPointName);

        String result = httpClient.getMethod(Constants.PROTOCOL,Constants.CONTROLLER_IP,Constants.CONTROLLER_PORT,getModuleConfigurePath(deviceMountPointName, moduleName, nodeName),"application/json");

        if (result == null) {
            logger.error("Configuration request failed. throwing Exception !");
            throw new APPCException("Error getting configuration of node :"+nodeName + ", of Module :" + moduleName + ", in device :" + deviceMountPointName);
        }

        return result;
    }

    //netconf client impl

    @Override
    public void connect(NetconfConnectionDetails connectionDetails) throws APPCException {
        if(connectionDetails == null){
            throw new APPCException("Invalid connection details - null value");
        }
        this.connectionDetails = connectionDetails;
        this.connect(connectionDetails.getHost(),getPayload());
    }

    @Override
    public String exchangeMessage(String message) throws APPCException {
        // TODO implement
        return null;
    }

    @Override
    public void configure(String configuration) throws APPCException {
        if(connectionDetails == null){
            throw new APPCException("Invalid connection details - null value");
        }

        Properties props = connectionDetails.getAdditionalProperties();
        if(props == null || !props.containsKey("module.name") || !props.containsKey("node.name")){
            throw new APPCException("Invalid properties!");
        }

        String moduleName = props.getProperty("module.name");
        String nodeName = props.getProperty("node.name");
        String deviceMountPointName = connectionDetails.getHost();

        int httpCode = httpClient.putMethod(Constants.PROTOCOL,Constants.CONTROLLER_IP,Constants.CONTROLLER_PORT,getModuleConfigurePath(deviceMountPointName, moduleName, nodeName),configuration,"application/xml");

        if (httpCode != HttpStatus.SC_OK) {
            logger.error("Configuration request failed. throwing Exception !");
            throw new APPCException("Error configuring node :"+nodeName + ", of Module :" + moduleName + ", in device :" + deviceMountPointName);
        }
    }

    @Override
    public String getConfiguration() throws APPCException {
        if(connectionDetails == null){
            throw new APPCException("Invalid connection details - null value");
        }

        Properties props = connectionDetails.getAdditionalProperties();
        if(props == null || !props.containsKey("module.name") || !props.containsKey("node.name")){
            throw new APPCException("Invalid properties!");
        }

        return this.getConfiguration(connectionDetails.getHost(),props.getProperty("module.name"),props.getProperty("node.name"));
    }

    @Override
    public void disconnect() throws APPCException {
        if(connectionDetails == null){
            throw new APPCException("Invalid connection details - null value");
        }
        this.disconnect(connectionDetails.getHost());
    }

    //private methods
    private String getModuleConfigurePath(String deviceMountPointName, String moduleName, String nodeName){


        String deviceSpecificPath = deviceMountPointName + "/yang-ext:mount/" + moduleName + ":" + nodeName;

        return Constants.CONFIGURE_PATH + deviceSpecificPath;
    }

    private String getConnectPath(){

        return Constants.CONNECT_PATH;
    }

    private String getCheckConnectivityPath(String deviceMountPointName) {
        return Constants.CHECK_CONNECTION_PATH + deviceMountPointName;
    }

    private String getDisconnectPath(String deviceMountPointName) {
        return Constants.DISCONNECT_PATH + deviceMountPointName;
    }

    private String getPayload() {
        return "{\n" +
                "    \"config:module\":\n" +
                "        {\n" +
                "        \"type\":\"odl-sal-netconf-connector-cfg:sal-netconf-connector\",\n" +
                "        \"netconf-northbound-ssh\\odl-sal-netconf-connector-cfg:name\":"+connectionDetails.getHost()+",\n" +
                "        \"odl-sal-netconf-connector-cfg:address\":"+connectionDetails.getHost()+",\n" +
                "        \"odl-sal-netconf-connector-cfg:port\":"+connectionDetails.getPort()+",\n" +
                "        \"odl-sal-netconf-connector-cfg:username\":"+connectionDetails.getUsername()+",\n" +
                "        \"odl-sal-netconf-connector-cfg:password\":"+connectionDetails.getPassword()+",\n" +
                "        \"tcp-only\":\"false\",\n" +
                "        \"odl-sal-netconf-connector-cfg:event-executor\":\n" +
                "            {\n" +
                "            \"type\":\"netty:netty-event-executor\",\n" +
                "            \"name\":\"global-event-executor\"\n" +
                "            },\n" +
                "        \"odl-sal-netconf-connector-cfg:binding-registry\":\n" +
                "            {\n" +
                "            \"type\":\"opendaylight-md-sal-binding:binding-broker-osgi-registry\",\n" +
                "            \"name\":\"binding-osgi-broker\"\n" +
                "            },\n" +
                "        \"odl-sal-netconf-connector-cfg:dom-registry\":\n" +
                "            {\n" +
                "            \"type\":\"opendaylight-md-sal-dom:dom-broker-osgi-registry\",\n" +
                "            \"name\":\"dom-broker\"\n" +
                "            },\n" +
                "        \"odl-sal-netconf-connector-cfg:client-dispatcher\":\n" +
                "            {\n" +
                "            \"type\":\"odl-netconf-cfg:netconf-client-dispatcher\",\n" +
                "            \"name\":\"global-netconf-dispatcher\"\n" +
                "            },\n" +
                "        \"odl-sal-netconf-connector-cfg:processing-executor\":\n" +
                "            {\n" +
                "            \"type\":\"threadpool:threadpool\",\n" +
                "            \"name\":\"global-netconf-processing-executor\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }
}
