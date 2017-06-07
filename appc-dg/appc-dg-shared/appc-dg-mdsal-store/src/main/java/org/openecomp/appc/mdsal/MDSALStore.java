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

package org.openecomp.appc.mdsal;

import org.openecomp.appc.mdsal.exception.MDSALStoreException;
import org.openecomp.appc.mdsal.objects.BundleInfo;

import java.util.Date;

/**
 * Provides APIs for interacting with MD-SAL store
 */
public interface MDSALStore {

    /**
     * Checks the presence of any yang module in the MD-SAL store,
     * <i>Due to limitation of SchemaContext interface of ODL that it does not
     * contain the information about dynamically loaded yang modules, it
     * checks the presence of OSGI bundle</i>
     * @param moduleName Name of the Module
     * @param revision revision of the Module
     * @return returns true- module is present, false - module is absent
     */
    boolean isModulePresent(String moduleName, Date revision);

    /**
     * This method will be used to store yang module to MD-SAL store
     * @param yang - yang module that need to be stored. In String format
     * @param bundleInfo - Bundle Information that contains name , description,  version , location. These parameters used to create bundle which will push yang to MD-SAL store.
     * @throws MDSALStoreException
     */
    void storeYangModule(String yang, BundleInfo bundleInfo) throws MDSALStoreException;

    /**
     * This method is used to store configuration JSON to MD-SAL store. It invokes store configuration Operation with required parameters
     * @param moduleName - Yang module name where JSON need to be posted
     * @param requestId - Request ID which is used as unique key for configuration JSON
     * @param configJSON - String value of configuration JSON that needs to be stored in MD-SAl store
     * @throws MDSALStoreException
     */
    void storeJson(String moduleName , String requestId , String configJSON ) throws MDSALStoreException;

}
