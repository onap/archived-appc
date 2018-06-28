/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */


  
package org.onap.appc.pool;

/**
 * This exception is thrown whenever the pool is not specified correctly
 * 
 */
public class PoolSpecificationException extends PoolException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * PoolSpecificationException constructor
     *
     * @param msg
     *            The error message
     */
    public PoolSpecificationException(String msg) {
        super(msg);
    }

}
