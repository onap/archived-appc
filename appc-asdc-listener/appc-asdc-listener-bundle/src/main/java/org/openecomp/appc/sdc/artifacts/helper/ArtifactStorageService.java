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

package org.openecomp.appc.sdc.artifacts.helper;

import org.apache.commons.lang.StringUtils;
import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.appc.sdc.artifacts.object.SDCReference;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;
import org.openecomp.appc.sdc.artifacts.object.SDCArtifact;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.openecomp.appc.sdc.artifacts.helper.Constants.COMMA;
import static org.openecomp.appc.sdc.artifacts.helper.Constants.AND;

/**
 * Provides methods for storing sdc artifacts into app-c database
 */
public class ArtifactStorageService {

    private DbLibService dbLibService;

    private static final String SCHEMA =  "sdnctl";

    private static final String SELECT_QUERY = Constants.SELECT_FROM + Constants.ASDC_ARTIFACTS +
            Constants.WHERE + Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_NAME + Constants.QUERY_PLACEHOLDER +
            AND + Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_VERSION + Constants.QUERY_PLACEHOLDER +
            AND + Constants.ARTIFACT_TYPE + Constants.QUERY_PLACEHOLDER;

    private static final String SELECT_QUERY_SDC_REFERENCE = Constants.SELECT_FROM + Constants.ASDC_REFERENCE +
            Constants.WHERE + Constants.ASDC_REFERENCE_FIELDS.VNF_TYPE + Constants.QUERY_PLACEHOLDER +
            AND + Constants.ASDC_REFERENCE_FIELDS.FILE_CATEGORY + Constants.QUERY_PLACEHOLDER ;

    private static final String INSERT_QUERY = Constants.INSERT + Constants.ASDC_ARTIFACTS +
            " ( " + Constants.ASDC_ARTIFACTS_FIELDS.SERVICE_UUID + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.DISTRIBUTION_ID + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.SERVICE_NAME + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.SERVICE_DESCRIPTION + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_UUID + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_INSTANCE_NAME + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_NAME + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_VERSION + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_TYPE + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_UUID + COMMA +
            Constants.ARTIFACT_TYPE + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_VERSION + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_DESCRIPTION + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.CREATION_DATE + COMMA +
            Constants.ARTIFACT_NAME  +COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_CONTENT + " ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String INSERT_QUERY_WITH_INT_VER = Constants.INSERT + Constants.ASDC_ARTIFACTS +
            " ( " + Constants.ASDC_ARTIFACTS_FIELDS.SERVICE_UUID + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.DISTRIBUTION_ID + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.SERVICE_NAME + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.SERVICE_DESCRIPTION + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_UUID + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_INSTANCE_NAME + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_NAME + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_VERSION + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_TYPE + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_UUID + COMMA +
            Constants.ARTIFACT_TYPE + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_VERSION + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_DESCRIPTION + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.CREATION_DATE + COMMA +
            Constants.ARTIFACT_NAME + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_CONTENT + COMMA +
            Constants.ASDC_ARTIFACTS_FIELDS.INTERNAL_VERSION + " ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String ASDC_REF_INSERT_QUERY = Constants.INSERT + Constants.ASDC_REFERENCE +
            "( "+ Constants.ASDC_REFERENCE_FIELDS.VNF_TYPE + COMMA +
            Constants.ASDC_REFERENCE_FIELDS.VNFC_TYPE+ COMMA +
            Constants.ASDC_REFERENCE_FIELDS.FILE_CATEGORY +COMMA +
            Constants.ASDC_REFERENCE_FIELDS.ACTION +COMMA +
            Constants.ARTIFACT_TYPE + COMMA +
            Constants.ARTIFACT_NAME  + " ) values (?,?,?,?,?,?)";

    private static final String SELECT_MAX_INT_VERSION = "SELECT coalesce(max(" + Constants.ASDC_ARTIFACTS_FIELDS.INTERNAL_VERSION + ")+1,1) as " + Constants.ASDC_ARTIFACTS_FIELDS.INTERNAL_VERSION  +
            " FROM " + Constants.ASDC_ARTIFACTS + Constants.WHERE + Constants.ARTIFACT_NAME  + Constants.QUERY_PLACEHOLDER;


    private final EELFLogger logger = EELFManager.getInstance().getLogger(ArtifactStorageService.class);

    /**
     * Stores Artifact received from SDC into APP-C database
     * @param artifact - SDC Artifact object
     * @throws APPCException
     */
    public void storeASDCArtifact(SDCArtifact artifact) throws APPCException {
        if(logger.isDebugEnabled()){
            logger.debug("Entering storeASDCArtifact with : " + artifact.toString());
        }
        try {
            initializeDBLibService();
            ArrayList<String> arguments = prepareArguments(artifact);
            dbLibService.writeData(INSERT_QUERY,arguments,SCHEMA);
        } catch (SQLException e) {
            logger.error("Error storing artifact in database : " +artifact.toString(),e);
            throw new APPCException(e.getMessage(),e);
        }
        if(logger.isDebugEnabled()){
            logger.debug("Exiting storeASDCArtifact");
        }
    }

    /**
     * Stores Artifact received from SDC and its Reference into APP-C database if it does not exist
     * @param artifact - SDC Artifact object
     * @param reference - SDC reference object
     * @throws APPCException
     */
    public void storeASDCArtifactWithReference(SDCArtifact artifact , SDCReference reference) throws APPCException {
        if(logger.isDebugEnabled()){
            logger.debug("Entering storeASDCArtifactWithReference with : " + artifact.toString());
        }
        try {
            initializeDBLibService();
            SDCArtifact existingArtifact = retrieveSDCArtifact(artifact.getResourceName(), artifact.getResourceVersion(),artifact.getArtifactType());
            if (existingArtifact ==null) { // new resource
                logger.debug(String.format("Artifact not found for vnfType = %s, version = %s and artifactType = %s. Inserting data." ,
                        artifact.getResourceName(),artifact.getResourceVersion() ,artifact.getArtifactType()));
                ArrayList<String> arguments = prepareArguments(artifact);
                Integer version = getNextInternalVersion(artifact.getArtifactName());
                arguments.add(version.toString());
                dbLibService.writeData(INSERT_QUERY_WITH_INT_VER,arguments,SCHEMA);
            } else { // duplicate
                logger.debug(String.format("Artifact of type '%s' already deployed for resource_type='%s' and resource_version='%s'",
                        artifact.getArtifactType() , artifact.getResourceName() , artifact.getResourceVersion()));
            }

            SDCReference existingReference = retrieveSDCReference(reference.getVnfType(),reference.getFileCategory());
            if(existingReference == null){
                logger.debug("Inserting SDC Reference data: " +reference.toString());
                ArrayList<String> arguments = prepareReferenceArguments(reference);
                dbLibService.writeData(ASDC_REF_INSERT_QUERY,arguments,SCHEMA);
            }else{
                logger.debug("Artifact reference already exists for: " +reference.toString());
            }
        } catch (SQLException e) {
            logger.error("Error storing artifact to database: " + artifact.toString(),e);
            throw new APPCException(e.getMessage(),e);
        }
        if(logger.isDebugEnabled()){
            logger.debug("Exiting storeASDCArtifactWithReference");
        }
    }

    private Integer getNextInternalVersion(String artifactName) throws APPCException {
        if (logger.isDebugEnabled()) {
            logger.debug("Entering getNextInternalVersion with artifactName:" + artifactName);
        }
        Integer version = 1;
        try {
            initializeDBLibService();
            ArrayList<String> arguments = new ArrayList<>();
            arguments.add(artifactName);
            CachedRowSet rowSet = dbLibService.getData(SELECT_MAX_INT_VERSION, arguments, SCHEMA);
            if (rowSet.first()) {
                version = rowSet.getInt(Constants.ASDC_ARTIFACTS_FIELDS.INTERNAL_VERSION .toString());
            }
        }catch (SQLException e) {
            logger.error("Error getting internal version for artifact name " + artifactName , e);
            throw new APPCException(e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting getNextInternalVersion with retrieved version:" + version.toString());
        }
        return version;
    }

    private void initializeDBLibService() {
        if(dbLibService == null){
            BundleContext context = FrameworkUtil.getBundle(DbLibService.class).getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(DbLibService.class.getName());
            dbLibService  = (DbLibService)context.getService(serviceReference);
        }
    }

    private ArrayList<String> prepareReferenceArguments(SDCReference reference) {
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(reference.getVnfType());
        arguments.add(reference.getVnfcType());
        arguments.add(reference.getFileCategory());
        arguments.add(reference.getAction());
        arguments.add(reference.getArtifactType());
        arguments.add(reference.getArtifactName());
        return arguments;
    }

    private ArrayList<String> prepareArguments(SDCArtifact artifact) {
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(artifact.getServiceUUID());
        arguments.add(artifact.getDistributionId());
        arguments.add(artifact.getServiceName());
        arguments.add(truncateServiceDescription(artifact.getServiceDescription()));
        arguments.add(artifact.getResourceUUID());
        arguments.add(artifact.getResourceInstanceName());
        arguments.add(artifact.getResourceName());
        arguments.add(artifact.getResourceVersion());
        arguments.add(artifact.getResourceType());
        arguments.add(artifact.getArtifactUUID());
        arguments.add(artifact.getArtifactType());
        arguments.add(artifact.getArtifactVersion());
        arguments.add(artifact.getArtifactDescription());
        arguments.add(artifact.getCreationDate());
        arguments.add(artifact.getArtifactName());
        arguments.add(artifact.getArtifactContent());
        return arguments;
    }

    private String truncateServiceDescription(String serviceDescription){
        if (!StringUtils.isBlank(serviceDescription) && serviceDescription.length()>255){
            logger.info("Truncating the SERVICE_DESCRIPTION to 255 characters");
            serviceDescription=serviceDescription.substring(0,255);
        }
        return serviceDescription;
    }

    /**
     * Reads the SDC artifact from APP-C database
     * @param resourceName  - resource Name from ASDC Artifact
     * @param resourceVersion - resource version from ASDC Artifact
     * @param artifactType artifact type from ASDC Artifact
     * @return - ASDC_ARTIFACT record if data exists
     * @throws APPCException
     */
    public SDCArtifact retrieveSDCArtifact(String resourceName, String resourceVersion, String artifactType) throws APPCException {
        SDCArtifact artifact = null;
        try {
            initializeDBLibService();
            ArrayList<String> arguments = new ArrayList<>();
            arguments.add(resourceName);
            arguments.add(resourceVersion);
            arguments.add(artifactType);
            CachedRowSet rowSet = dbLibService.getData(SELECT_QUERY, arguments, SCHEMA);
            if (rowSet.first()) {
                artifact = new SDCArtifact();
                artifact.setArtifactUUID(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_UUID.toString()));
                artifact.setArtifactName(rowSet.getString(Constants.ARTIFACT_NAME));
                artifact.setArtifactType(rowSet.getString(Constants.ARTIFACT_TYPE));
                artifact.setArtifactVersion(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_VERSION.toString()));
                artifact.setArtifactDescription(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_DESCRIPTION.toString()));
                artifact.setArtifactContent(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_CONTENT.toString()));

                artifact.setResourceUUID(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_UUID.toString()));
                artifact.setResourceName(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_NAME.toString()));
                artifact.setResourceType(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_TYPE.toString()));
                artifact.setResourceVersion(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_VERSION.toString()));
                artifact.setResourceInstanceName(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_INSTANCE_NAME.toString()));

                artifact.setServiceUUID(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.SERVICE_UUID.toString()));
                artifact.setServiceName(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.SERVICE_NAME.toString()));
                artifact.setServiceDescription(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.SERVICE_DESCRIPTION.toString()));

                artifact.setCreationDate(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.CREATION_DATE.toString()));
                artifact.setDistributionId(rowSet.getString(Constants.ASDC_ARTIFACTS_FIELDS.DISTRIBUTION_ID.toString()));
            }

        } catch (SQLException e) {
            logger.error("Error query artifact for " + Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_NAME + " = " + resourceName +
                    Constants.ASDC_ARTIFACTS_FIELDS.RESOURCE_VERSION + " = " + resourceVersion +
                    Constants.ARTIFACT_TYPE + " = " + artifactType, e);
            throw new APPCException(e);
        }
        return artifact;
    }

    /**
     * Reads the SDC reference from APP-C database
     * @param vnfType  - vnf Type from ASDC reference
     * @param fileCategory - file category from ASDC reference
     * @return - ASDC_ARTIFACT record if data exists
     * @throws APPCException
     */
    public SDCReference retrieveSDCReference(String vnfType, String fileCategory) throws APPCException {
        SDCReference reference = null;
        try {
            initializeDBLibService();
            ArrayList<String> arguments = new ArrayList<>();
            arguments.add(vnfType);
            arguments.add(fileCategory);
            CachedRowSet rowSet = dbLibService.getData(SELECT_QUERY_SDC_REFERENCE, arguments, SCHEMA);
            if (rowSet.first()) {
                reference = new SDCReference();
                reference.setVnfType(rowSet.getString(Constants.ASDC_REFERENCE_FIELDS.VNF_TYPE.toString()));
                reference.setVnfcType(rowSet.getString(Constants.ASDC_REFERENCE_FIELDS.VNFC_TYPE.toString()));
                reference.setFileCategory(rowSet.getString(Constants.ASDC_REFERENCE_FIELDS.FILE_CATEGORY.toString()));
                reference.setAction(rowSet.getString(Constants.ASDC_REFERENCE_FIELDS.ACTION.toString()));
                reference.setArtifactType(rowSet.getString(Constants.ARTIFACT_TYPE));
                reference.setArtifactName(rowSet.getString(Constants.ARTIFACT_NAME));
            }
        } catch (SQLException e) {
            logger.error("Error querying ASDC_REFERENCE for " + Constants.ASDC_REFERENCE_FIELDS.VNF_TYPE + " = " + vnfType +
                    Constants.ASDC_REFERENCE_FIELDS.FILE_CATEGORY + " = " + fileCategory , e);
            throw new APPCException(e);
        }
        return reference;
    }
}
