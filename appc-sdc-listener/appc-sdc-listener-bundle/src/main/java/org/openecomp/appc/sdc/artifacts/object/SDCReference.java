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

package org.openecomp.appc.sdc.artifacts.object;

public class SDCReference {

    private String vnfType;
    private String vnfcType;
    private String fileCategory;
    private String action;
    private String artifactType;
    private String artifactName;

    public String getVnfType() {
        return vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public String getVnfcType() {
        return vnfcType;
    }

    public void setVnfcType(String vnfcType) {
        this.vnfcType = vnfcType;
    }

    public String getFileCategory() {
        return fileCategory;
    }

    public void setFileCategory(String fileCategory) {
        this.fileCategory = fileCategory;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }
    @Override
    public String toString() {
        return    "vnfType = " + vnfType+
                ", vnfType = " + vnfType+
                ", fileCategory = " + fileCategory+
                ", action = " + action+
                ", artifactType = " + artifactType+
                ", artifactName = " + artifactName+
                ", vnfType = " + vnfType;
    }

}
