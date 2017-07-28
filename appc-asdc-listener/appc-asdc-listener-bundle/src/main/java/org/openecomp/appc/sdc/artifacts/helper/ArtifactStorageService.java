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

import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;
import org.openecomp.appc.sdc.artifacts.object.SDCArtifact;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.openecomp.appc.licmgr.Constants.ASDC_ARTIFACTS;
import static org.openecomp.appc.licmgr.Constants.ASDC_ARTIFACTS_FIELDS.*;

/**
 * Provides methods for storing sdc artifacts into app-c database
 */
public class ArtifactStorageService {

    private DbLibService dbLibService;

    private static final String COMMA = " , ";
    private static final String QUERY_PLACEHOLDER = " = ? ";

    private static final String SCHEMA =  "sdnctl";

    private static final String SELECT_QUERY = "SELECT * FROM " + ASDC_ARTIFACTS +
                                                " WHERE " + RESOURCE_NAME + QUERY_PLACEHOLDER +
                                                " AND " + RESOURCE_VERSION + QUERY_PLACEHOLDER +
                                                " AND " + ARTIFACT_TYPE + QUERY_PLACEHOLDER;

    private static final String INSERT_QUERY = "INSERT INTO " + ASDC_ARTIFACTS +
            " ( " + SERVICE_UUID + COMMA +
                    DISTRIBUTION_ID + COMMA +
                    SERVICE_NAME + COMMA +
                    SERVICE_DESCRIPTION + COMMA +
                    RESOURCE_UUID + COMMA +
                    RESOURCE_INSTANCE_NAME + COMMA +
                    RESOURCE_NAME + COMMA +
                    RESOURCE_VERSION + COMMA +
                    RESOURCE_TYPE + COMMA +
                    ARTIFACT_UUID + COMMA +
                    ARTIFACT_TYPE + COMMA +
                    ARTIFACT_VERSION + COMMA +
                    ARTIFACT_DESCRIPTION + COMMA +
                    CREATION_DATE + COMMA +
                    ARTIFACT_NAME  +COMMA +
                    ARTIFACT_CONTENT + " ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


    private final EELFLogger logger = EELFManager.getInstance().getLogger(ArtifactStorageService.class);

    /**
     * Stores Artifact received from SDC into APP-C database
     * @param artifact
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

    private void initializeDBLibService() {
        if(dbLibService == null){
            BundleContext context = FrameworkUtil.getBundle(DbLibService.class).getBundleContext();
            ServiceReference serviceReference = context.getServiceReference(DbLibService.class.getName());
            dbLibService  = (DbLibService)context.getService(serviceReference);
        }
    }

    private ArrayList<String> prepareArguments(SDCArtifact artifact) {
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(artifact.getServiceUUID());
        arguments.add(artifact.getDistributionId());
        arguments.add(artifact.getServiceName());
        arguments.add(artifact.getServiceDescription());
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

    /**
     * reads the SDC artifact from APP-C database
     * @param resourceName
     * @param resourceVersion
     * @param artifactType
     * @return
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
                artifact.setArtifactUUID(rowSet.getString(ARTIFACT_UUID.toString()));
                artifact.setArtifactName(rowSet.getString(ARTIFACT_NAME.toString()));
                artifact.setArtifactType(rowSet.getString(ARTIFACT_TYPE.toString()));
                artifact.setArtifactVersion(rowSet.getString(ARTIFACT_VERSION.toString()));
                artifact.setArtifactDescription(rowSet.getString(ARTIFACT_DESCRIPTION.toString()));
                artifact.setArtifactContent(rowSet.getString(ARTIFACT_CONTENT.toString()));

                artifact.setResourceUUID(rowSet.getString(RESOURCE_UUID.toString()));
                artifact.setResourceName(rowSet.getString(RESOURCE_NAME.toString()));
                artifact.setResourceType(rowSet.getString(RESOURCE_TYPE.toString()));
                artifact.setResourceVersion(rowSet.getString(RESOURCE_VERSION.toString()));
                artifact.setResourceInstanceName(rowSet.getString(RESOURCE_INSTANCE_NAME.toString()));

                artifact.setServiceUUID(rowSet.getString(SERVICE_UUID.toString()));
                artifact.setServiceName(rowSet.getString(SERVICE_NAME.toString()));
                artifact.setServiceDescription(rowSet.getString(SERVICE_DESCRIPTION.toString()));

                artifact.setCreationDate(rowSet.getString(CREATION_DATE.toString()));
                artifact.setDistributionId(rowSet.getString(DISTRIBUTION_ID.toString()));
            }

        } catch (SQLException e) {
            logger.error("Error query artifact for " + RESOURCE_NAME + " = " + resourceName +
                    RESOURCE_VERSION + " = " + resourceVersion +
                    ARTIFACT_TYPE + " = " + artifactType, e);
            throw new APPCException(e);
        }
        return artifact;
    }
}
