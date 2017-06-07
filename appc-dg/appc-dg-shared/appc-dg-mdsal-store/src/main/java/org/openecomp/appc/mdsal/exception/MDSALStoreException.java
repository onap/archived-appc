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

package org.openecomp.appc.mdsal.exception;

/**
 * This is custom exception type defined for MD-SAL store. All exceptions thrown by mdsal store module need to be wrapped in this class.
*/
 public class MDSALStoreException extends Exception {

    private static final long serialVersionUID = 1L;

    public MDSALStoreException(){
    }

    /**
     * Create MDSALStoreException using only message.
     * @param message -- message to the caller.
     */
    public MDSALStoreException (String message){
        super(message);
    }

    /**
     * Create MDSALStoreException using orignal cause
     * @param cause - cause that is being wrapped / suppressed.
     */
    public MDSALStoreException (Throwable cause){
        super(cause);
    }

    /**
     *
     * @param message - message to the caller.
     * @param cause - cause that is being wrapped / suppressed .
     */
    public MDSALStoreException(String message , Throwable cause){
        super(message , cause);
    }

    /**
     *
     * @param message - message to the caller.
     * @param cause - cause that is being wrapped / suppressed .
     * @param enableSuppression - Indicates if suppression is enabled.
     * @param writableStackTrace - Indicates if writable stacktrace is supported
     */
    public MDSALStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
