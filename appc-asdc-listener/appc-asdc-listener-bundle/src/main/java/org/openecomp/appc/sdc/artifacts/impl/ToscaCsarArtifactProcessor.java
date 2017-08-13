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

package org.openecomp.appc.sdc.artifacts.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang.StringUtils;
import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.licmgr.Constants;
import org.openecomp.appc.sdc.artifacts.helper.DependencyModelGenerator;
import org.openecomp.appc.sdc.artifacts.object.Resource;
import org.openecomp.appc.sdc.artifacts.object.SDCArtifact;
import org.openecomp.appc.sdc.artifacts.object.SDCReference;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;

import javax.json.Json;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ToscaCsarArtifactProcessor extends AbstractArtifactProcessor{

    private final EELFLogger logger = EELFManager.getInstance().getLogger(ToscaCsarArtifactProcessor.class);

    private DependencyModelGenerator dependencyModelGenerator;

    public ToscaCsarArtifactProcessor(IDistributionClient client, EventSender eventSender, INotificationData notification, IResourceInstance resource,
                                      IArtifactInfo artifact, URI storeUri){
        super(client,eventSender,notification,resource,artifact,storeUri);
        dependencyModelGenerator = new DependencyModelGenerator();
    }

    @Override
    public void processArtifact(IDistributionClientDownloadResult download) throws APPCException {
        logger.debug("processing artifact " + super.artifact.getArtifactType());
        byte[] byteArray = download.getArtifactPayload();
        String serviceFileName = "";
        String serviceTemplateContent = "";
        List<Resource> resources = null;
        Map<String,String> csarFiles = new HashMap<>();
        try (ZipInputStream inputStream = new ZipInputStream(new ByteArrayInputStream(byteArray))) {
            ZipEntry entry = inputStream.getNextEntry();
            logger.debug("First Entry = " +entry);
            while(entry!= null){
                String filename = entry.getName();
                logger.debug("Next Entry = "+ filename);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String str = null;
                StringBuilder sb = new StringBuilder();
                while((str = bufferedReader.readLine()) != null){
                    sb.append(new String(str)).append(System.getProperty("line.separator"));
                }
                csarFiles.put(filename,sb.toString());
                entry = inputStream.getNextEntry();
            }

        } catch (IOException e) {
            logger.error("Error Reading TOSCA.meta from CSAR",e);
            throw new APPCException(e);
        }
        serviceFileName = readServiceFileName(csarFiles.get("TOSCA-Metadata/TOSCA.meta"));
        logger.debug("Service File Name = " + serviceFileName);
        serviceTemplateContent = csarFiles.get(serviceFileName);

        try {
            resources = readResources (serviceTemplateContent);
        } catch (Exception e) {
            logger.error("Error reading resources from " + ", serviceFileName = " + serviceFileName
                    + ", TOSCA Metadata = " + csarFiles.get("TOSCA-Metadata/TOSCA.meta"),e);
            throw new APPCException(e);
        }

        for(Resource resource:resources){
            String resourceTemplate = csarFiles.get("Definitions/resource-" + resource.getFileNameTag() + "-template.yml");
            SDCArtifact artifact = this.getArtifactObject(resource,resourceTemplate);
            processArtifact(artifact);
        }
    }

    private String readServiceFileName(String toscaMetadata) {
        toscaMetadata = toscaMetadata.substring(toscaMetadata.indexOf("Entry-Definitions"), toscaMetadata.indexOf(System.getProperty("line.separator"),toscaMetadata.indexOf("Entry-Definitions")));
        toscaMetadata =toscaMetadata.split(":")[1].trim();
        return toscaMetadata;
    }

    protected SDCArtifact getArtifactObject(Resource resource, String data){

        SDCArtifact sdcArtifact = new SDCArtifact();

        sdcArtifact.setArtifactUUID(this.artifact.getArtifactUUID());
        sdcArtifact.setArtifactName(this.artifact.getArtifactName());
        sdcArtifact.setArtifactType(this.artifact.getArtifactType());
        sdcArtifact.setArtifactVersion(this.artifact.getArtifactVersion());
        sdcArtifact.setArtifactDescription(this.artifact.getArtifactDescription());
        sdcArtifact.setArtifactContent(data);
        sdcArtifact.setCreationDate(super.getCurrentDateTime());

        sdcArtifact.setDistributionId(this.notification.getDistributionID());
        sdcArtifact.setServiceUUID(this.notification.getServiceUUID());
        sdcArtifact.setServiceName(this.notification.getServiceName());
        sdcArtifact.setServiceDescription(this.notification.getServiceDescription());

        sdcArtifact.setResourceName(resource.getName());
        sdcArtifact.setResourceType(resource.getType());
        sdcArtifact.setResourceVersion(resource.getVersion());
        sdcArtifact.setResourceUUID(resource.getUuid());
        sdcArtifact.setResourceInstanceName(resource.getInstanceName());

        return sdcArtifact;
    }

    private List<Resource> readResources(String serviceTemplateContent) throws IOException {
        List<Resource> resources = new LinkedList<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode root = mapper.readTree(serviceTemplateContent);
        JsonNode topologyTemplate =  root.get("topology_template");
        JsonNode nodeTemplates =  topologyTemplate.get("node_templates");
        Iterator<Map.Entry<String, JsonNode>> itr = nodeTemplates.fields();
        while(itr.hasNext()){
            Map.Entry<String, JsonNode> entry = itr.next();
            String instanceName = entry.getKey();
            JsonNode nodeTemplate = entry.getValue();

            String fileNameTag =  nodeTemplate.get("type").asText();
            logger.debug("Resource type in Service Template = " + fileNameTag);
            fileNameTag = fileNameTag.substring(fileNameTag.lastIndexOf(".")+1,fileNameTag.length());
            String version = nodeTemplate.get("metadata").get("version").asText();
            String uuid = nodeTemplate.get("metadata").get("UUID").asText();
            String name = nodeTemplate.get("metadata").get("name").asText();
            String type = nodeTemplate.get("metadata").get("type").asText();

            if(!"VF".equalsIgnoreCase(type)){
                continue;
            }

            Resource resource = new Resource();
            resource.setFileNameTag(fileNameTag);
            resource.setVersion(version);
            resource.setUuid(uuid);
            resource.setInstanceName(instanceName);
            resource.setName(name);
            resource.setType(type);

            resources.add(resource);
        }
        return resources;
    }


    @Override
    protected void processArtifact(SDCArtifact artifact) throws APPCException {
        String vnfType = artifact.getResourceName();
        String version = artifact.getResourceVersion();
        String packageArtifactID = artifact.getArtifactUUID();

        if (StringUtils.isEmpty(vnfType) ||
                StringUtils.isEmpty(version) ||
                StringUtils.isEmpty(packageArtifactID)) {
            String errStr = String.format("Missing information in SDC request. Details: resource_type='%s', resource_version='%s', artifactID='%s'", vnfType, version, packageArtifactID);
            logger.error(errStr);
            throw new APPCException(errStr);
        }
        try {
            SDCReference reference = new SDCReference();
            reference.setVnfType(vnfType);
            reference.setFileCategory("tosca_model");
            reference.setArtifactName(artifact.getArtifactName());
            logger.debug("Storing TOSCA to ASDC Artifact");
            artifactStorageService.storeASDCArtifactWithReference(artifact,reference);

            SDCArtifact dependencyArtifact = getDependencyArtifact(artifact);
            SDCReference dependencyReference = new SDCReference();
            dependencyReference.setVnfType(vnfType);
            dependencyReference.setFileCategory("tosca_dependency_model");
            dependencyReference.setArtifactName(dependencyArtifact.getArtifactName());
            logger.debug("Storing Dependency to ASDC Artifact");
            artifactStorageService.storeASDCArtifactWithReference(dependencyArtifact,dependencyReference);
        } catch (Exception e) {
            logger.error("Error processing artifact : " + artifact.toString() );
            throw new APPCException(e.getMessage(),e);
        }
    }

    private SDCArtifact getDependencyArtifact(SDCArtifact toscaArtifact) throws APPCException {
        SDCArtifact artifact = new SDCArtifact();
        artifact.setArtifactName("dependency_"+toscaArtifact.getArtifactName());
        String dependencyModel = dependencyModelGenerator.getDependencyModel(toscaArtifact.getArtifactContent(),toscaArtifact.getResourceName());
        artifact.setArtifactContent(dependencyModel);
        artifact.setArtifactType("DEPENDENCY_MODEL");

        artifact.setArtifactUUID(toscaArtifact.getArtifactUUID());
        artifact.setArtifactVersion(toscaArtifact.getArtifactVersion());
        artifact.setArtifactDescription(toscaArtifact.getArtifactDescription());
        artifact.setCreationDate(super.getCurrentDateTime());
        artifact.setDistributionId(toscaArtifact.getDistributionId());
        artifact.setServiceUUID(toscaArtifact.getServiceUUID());
        artifact.setServiceName(toscaArtifact.getServiceName());
        artifact.setServiceDescription(toscaArtifact.getServiceDescription());
        artifact.setResourceName(toscaArtifact.getResourceName());
        artifact.setResourceType(toscaArtifact.getResourceType());
        artifact.setResourceVersion(toscaArtifact.getResourceVersion());
        artifact.setResourceUUID(toscaArtifact.getResourceUUID());
        artifact.setResourceInstanceName(toscaArtifact.getResourceInstanceName());
        return artifact;
    }


}
