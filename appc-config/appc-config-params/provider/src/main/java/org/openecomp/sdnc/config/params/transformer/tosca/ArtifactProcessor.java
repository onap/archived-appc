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

package org.openecomp.sdnc.config.params.transformer.tosca;

import org.openecomp.sdnc.config.params.data.PropertyDefinition;
import org.openecomp.sdnc.config.params.transformer.tosca.exceptions.ArtifactProcessorException;

import java.io.OutputStream;

public interface ArtifactProcessor
{
    /**
     * Generates Tosca artifact from PropertyDefinition object.
     *
     * @param artifact
     *                PropertyDefinition object which is to be converted to Tosca.
     * @param stream
     *                Stream to which the generated Tosca is to be written.
     * @throws ArtifactProcessorException
     *                If the Tosca Generation failed
     */
    void generateArtifact(PropertyDefinition artifact, OutputStream stream) throws ArtifactProcessorException;

    /**
     * Generates Tosca artifact from PropertyDefinition string.
     *
     * @param artifact
     *               PropertyDefinition string which is to be converted to Tosca.
     * @param stream
     *               Stream to which the generated Tosca is to be written.
     * @throws ArtifactProcessorException
     *               If the Tosca Generation failed
     */
    void generateArtifact(String artifact, OutputStream stream) throws ArtifactProcessorException;

    /**
     * Generates the PropertyDefinition object from a Tosca artifact.
     *
     * @param toscaArtifact
     *                Tosca artifact which is to be converted.
     * @return PropertyDefinition object generated from Tosca
     * @throws ArtifactProcessorException
     *                If the PropertyDefinition Generation failed
     */
    PropertyDefinition readArtifact(String toscaArtifact) throws ArtifactProcessorException;
}
