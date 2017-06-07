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

package org.openecomp.appc.requesthandler.helper;

import org.apache.commons.lang.ObjectUtils;
import org.openecomp.appc.executor.objects.UniqueRequestIdentifier;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * This class serves as Request Registry, which holds the
 * request unique parameters (originatorId,requestId,subRequestId)
 * in memory.
 */
public class RequestRegistry {

    static Set<UniqueRequestIdentifier> set = Collections.newSetFromMap(new ConcurrentHashMap<UniqueRequestIdentifier, Boolean>());
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RequestRegistry.class);
    public RequestRegistry(){

    }

    /**
     * This method accepts unique request parameters and adds it to Request Registry
     * if Registry already contains same parameters it returns false,
     * else returns true.
     * @param requestIdentifier
     * @return
     */
    public boolean registerRequest(UniqueRequestIdentifier requestIdentifier){

        if (logger.isTraceEnabled()) {
            logger.trace("Entering to registerRequest with UniqueRequestIdentifier = "+ ObjectUtils.toString(requestIdentifier));
        }
        boolean output = set.add(requestIdentifier);
        logger.debug(" Output = " + output);
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from registerRequest with (output = "+ ObjectUtils.toString(output)+")");
        }
        return output;
    }

    /**
     * This method accepts unique request parameters and removes request
     * from the Request Registry
     * @param requestIdentifier
     */
    public void removeRequest(UniqueRequestIdentifier requestIdentifier){
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to removeRequest with UniqueRequestIdentifier = "+ ObjectUtils.toString(requestIdentifier));
        }
        set.remove(requestIdentifier);
    }

    /**
     * This method returns the count of currently registered requests
     * in the request registry
     * * @return currently registered requests count
     */
    public int getRegisteredRequestCount() {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to getRegisteredRequestCount");
        }
        return set.size();
    }
}
