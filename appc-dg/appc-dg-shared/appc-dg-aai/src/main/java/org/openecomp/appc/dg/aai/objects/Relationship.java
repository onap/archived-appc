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

package org.openecomp.appc.dg.aai.objects;

import java.util.HashMap;
import java.util.Map;


public class Relationship {

    private String relatedTo;

    private String relatedLink;

    private Map<String,String> relationShipDataMap;

    private Map<String,String> relatedProperties;

    public Relationship(){
        relationShipDataMap = new HashMap<>();
        relatedProperties = new HashMap<>();
    }

    public String getRelatedTo() {
        return relatedTo;
    }

    public String getRelatedLink() {
        return relatedLink;
    }

    public Map<String, String> getRelationShipDataMap() {
        return relationShipDataMap;
    }

    public Map<String, String> getRelatedProperties() {
        return relatedProperties;
    }

    public void setRelatedTo(String relatedTo) {
        this.relatedTo = relatedTo;
    }

    public void setRelatedLink(String relatedLink) {
        this.relatedLink = relatedLink;
    }
}
