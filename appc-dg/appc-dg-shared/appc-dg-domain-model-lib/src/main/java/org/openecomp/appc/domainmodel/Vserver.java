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

package org.openecomp.appc.domainmodel;


public class Vserver {

    private String url;

    private String tenantId;
    private String id;
    private String relatedLink;
    private String name;

    public Vserver(String url){
        this(url,null,null,null,null);
    }

    public Vserver(String url,
                   String tenantId,
                   String id,
                   String relatedLink,
                   String name){
        this.url = url;
        this.tenantId =tenantId;
        this.id = id;
        this.relatedLink =relatedLink;
        this.name = name;

    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Vserver : url = " +url + ", tenantId = " +tenantId +", id = " +id + " ,relatedLink = " +relatedLink +" , name = "+name;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getId() {
        return id;
    }

    public String getRelatedLink() {
        return relatedLink;
    }

    public String getName() {
        return name;
    }
}
