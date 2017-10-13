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

package org.openecomp.appc.adapter.iaas.impl;

import java.util.Properties;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ServiceCatalogFactory {
	
	private static EELFLogger logger= EELFManager.getInstance().getLogger(org.openecomp.appc.adapter.iaas.impl.ServiceCatalogFactory.class);

    /**
     * This method accepts a fully qualified identity service URL and uses that to determine which version of the
     * serviceCatalog to load.
     *
     * @param url The parsed URL of the identity service
     * @param projectIdentifier The project or tenant to be used to connect to the service
     * @param principal The principal or user to be used to connect to the service
     * @param ceredential The credential or password to be used to connect to the service
     * @param properties Properties object for proxy information
     * @return The serviceCatalog for identity service version specified in the url, null if not supported.
     */
    public static ServiceCatalog getServiceCatalog(String url, String projectIdentifier, String principal,
            String credential, String domain, Properties properties) {
    	IdentityURL idUrl = IdentityURL.parseURL(url);
    	if(idUrl == null){
    		logger.error("Url " + url + " could not be parsed.");
    		return null;
    	}
        String version = idUrl.getVersion();
        if(version == null){
        	logger.error("Invalid Identity URL check configuration");
        	return null;
        }
        String prefix = version.split("\\.")[0];
        if (prefix != null) {
            switch (prefix) {
                case "v2":
                    return new ServiceCatalogV2(url, projectIdentifier, principal, credential, properties);
                case "v3":
                    return new ServiceCatalogV3(url, projectIdentifier, principal, credential, domain, properties);
            }
        }
        return null;
    }
}
