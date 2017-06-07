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

package org.openecomp.appc.metadata.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;

import javax.sql.rowset.CachedRowSet;

import org.openecomp.appc.cache.MetadataCache;
import org.openecomp.appc.cache.impl.MetadataCacheFactory;
import org.openecomp.appc.metadata.MetadataService;
import org.openecomp.appc.metadata.objects.DependencyModelIdentifier;

import java.sql.SQLException;
import java.util.ArrayList;


public class MetadataServiceImpl implements MetadataService {

    private DbLibService dbLibService;

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(MetadataServiceImpl.class);

    private MetadataCache<DependencyModelIdentifier,String> cache;

    public MetadataServiceImpl(){
        initialize();
    }

    private void initialize(){
        cache = MetadataCacheFactory.getInstance().getMetadataCache();
        // TODO initialze dbLibService
    }

    public void setDbLibService(DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    @Override
    public String getVnfModel(DependencyModelIdentifier modelIdentifier) {
        logger.debug("Reading Vnf Model data from cache for vnfType : "+ modelIdentifier.getVnfType() +" and catalog version : " +modelIdentifier.getCatalogVersion());
        String vnfModel = cache.getObject(modelIdentifier);
        if(vnfModel ==null || vnfModel.length() ==0){
            logger.debug("Vnf Model not available in cache. Reading from database.");
            vnfModel = readVnfModel(modelIdentifier);
            if(vnfModel !=null  && vnfModel.length()>0){
                logger.debug("Adding retrieved Vnf Model to cache.");
                addVnfModel(modelIdentifier,vnfModel);
            }
        }
        return vnfModel;
    }

    private void addVnfModel(DependencyModelIdentifier modelIdentifier, String vnfModel) {
        cache.putObject(modelIdentifier,vnfModel);
    }

    private String readVnfModel(DependencyModelIdentifier modelIdentifier) {

        logger.debug("Reading Vnf Model data from database for RESOURCE_NAME : "+ modelIdentifier.getVnfType() +" and RESOURCE_VERSION : " +modelIdentifier.getCatalogVersion());
        StringBuilder query = new StringBuilder();
        String vnfModel =null;
        query.append("SELECT ARTIFACT_CONTENT FROM sdnctl.ASDC_ARTIFACTS WHERE RESOURCE_NAME = ? ") ;
        ArrayList<String> argList = new ArrayList<>();
        argList.add(modelIdentifier.getVnfType());

        if (modelIdentifier.getCatalogVersion()==null){
            query.append(" ORDER BY  SUBSTRING_INDEX(RESOURCE_VERSION, '.', 1)*1  DESC , " +
                    "SUBSTRING_INDEX(SUBSTRING_INDEX(RESOURCE_VERSION, '.', 2),'.', -1) *1 DESC , " +
                    "SUBSTRING_INDEX(RESOURCE_VERSION, '.', -1)*1 DESC ;");
        }else{
            query.append("AND RESOURCE_VERSION = ? ;");
            argList.add(modelIdentifier.getCatalogVersion());
        }
        try {
            final CachedRowSet data = dbLibService.getData(query.toString(), argList, "sdnctl");
            if (data.first()) {
                vnfModel = data.getString("ARTIFACT_CONTENT");
                if (vnfModel == null || vnfModel.isEmpty()) {
                    logger.error("Invalid dependency model for vnf type : "+ modelIdentifier.getVnfType() +" and catalog version : " +modelIdentifier.getCatalogVersion());
                    throw new RuntimeException("Invalid or Empty VNF Model");
                }
                logger.debug("Retrieved Vnf Model : " + vnfModel);
            }else {
                logger.warn("VNF Model not found in datastore for RESOURCE_NAME : "+ modelIdentifier.getVnfType() +" AND RESOURCE_VERSION : " +modelIdentifier.getCatalogVersion());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred");
        }
        return  vnfModel;
    }
}
