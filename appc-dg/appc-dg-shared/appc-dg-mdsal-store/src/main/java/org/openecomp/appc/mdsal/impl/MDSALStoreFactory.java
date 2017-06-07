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

package org.openecomp.appc.mdsal.impl;

import org.openecomp.appc.mdsal.MDSALStore;

/*
 * Factory class to create/get instance of MDSALStore
 */
public class MDSALStoreFactory {
    private static class ReferenceHolder{
        private static MDSALStore store = new MDSALStoreImpl();
        private ReferenceHolder(){}
    }
    private MDSALStoreFactory(){

    }

    /**
     * Method for creating MDSALStore instance, It creates an instance of
     * MDSALStoreImpl once and returns the same instance everytime it is invoked.
     * @return
     */
    public static MDSALStore createMDSALStore (){
        return ReferenceHolder.store;
    }
}

