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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.openecomp.appc.dg.dependencymanager.helper.DependencyModelParser;
import org.openecomp.appc.dg.objects.Node;
import org.openecomp.appc.dg.objects.VnfcDependencyModel;
import org.openecomp.appc.domainmodel.Vnfc;
import org.openecomp.appc.exceptions.APPCException;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides method for genrating Dependency JSON from Tosca model
 */
public class DependencyModelGenerator {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(DependencyModelGenerator.class);

    /**
     *
     * @param tosca - tosca string from SDC
     * @param vnfType - Vnf Type  from tosca
     * @return - Dependency JSON in String format
     * @throws APPCException is thrown if error occurs
     */
    public String getDependencyModel(String tosca, String vnfType) throws APPCException {
        logger.debug(String.format("Generating dependency model for vnfType : %s , TOSCA: %s ",  vnfType ,tosca));
        String dependencyJson;
        DependencyModelParser dependencyModelParser = new DependencyModelParser();
        VnfcDependencyModel vnfcDependencyModel = dependencyModelParser.generateDependencyModel(tosca, vnfType);

        if (vnfcDependencyModel != null && !vnfcDependencyModel.getDependencies().isEmpty()) {
            logger.debug(String.format("Dependency Model generated : %s ", vnfcDependencyModel.toString()));
            List<org.openecomp.appc.sdc.artifacts.object.Vnfc> vnfcs = new ArrayList<>();

            for (Node<Vnfc> node : vnfcDependencyModel.getDependencies()) {
                org.openecomp.appc.sdc.artifacts.object.Vnfc vnfc = new org.openecomp.appc.sdc.artifacts.object.Vnfc();
                vnfc.setVnfcType(node.getChild().getVnfcType());
                vnfc.setMandatory(node.getChild().isMandatory());
                vnfc.setResilienceType(node.getChild().getResilienceType());
                if (node.getParents() != null && !node.getParents().isEmpty()) {
                    List<String> parents = new ArrayList<>();
                    for (Vnfc parentNode : node.getParents()) {
                        parents.add(parentNode.getVnfcType());
                    }
                    vnfc.setParents(parents);
                }
                vnfcs.add(vnfc);
            }
            ObjectMapper objectMapper = new ObjectMapper();

            ObjectWriter writer = objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).configure
                    (MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true).writer().withRootName("vnfcs");
            try {
                dependencyJson = writer.writeValueAsString(vnfcs);
            } catch (JsonProcessingException e) {
                logger.error("Error converting dependency model to JSON");
                throw new APPCException("Error converting dependency model to JSON",e);
            }
        } else {
            logger.error("Error generating dependency model from tosca. Empty dependency model");
            throw new APPCException("Error generating dependency model from tosca. Empty dependency model");
        }
        return  dependencyJson;
    }
}
