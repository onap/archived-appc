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



package org.openecomp.appc.exceptions;

/**
 * This exception indicates that the named provider could not be found or was unidentifiable.
 */
public class UnknownProviderException extends APPCException {

    /**
    *
    */
   private static final long serialVersionUID = 1L;

   /**
    * Constructs a new exception with null as its detail message. The cause is not initialized, and may subsequently be
    * initialized by a call to initCause.
    */
   public UnknownProviderException() {
   }

   /**
    * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently
    * be initialized by a call to initCause.
    * 
    * @param message
    *            the detail message. The detail message is saved for later retrieval by the getMessage() method.
    */
   public UnknownProviderException(String message) {
       super(message);
   }

   /**
    * Constructs a new exception with the specified cause and a detail message of (cause==null ? null :
    * cause.toString()) (which typically contains the class and detail message of cause). This constructor is useful
    * for exceptions that are little more than wrappers for other throwables (for example,
    * java.security.PrivilegedActionException).
    * 
    * @param cause
    *            the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted,
    *            and indicates that the cause is nonexistent or unknown.)
    */
   public UnknownProviderException(Throwable cause) {
       super(cause);
   }

   /**
    * 
    Constructs a new exception with the specified detail message and cause.
    * <p>
    * Note that the detail message associated with cause is not automatically incorporated in this exception's detail
    * message.
    * </p>
    * 
    * @param message
    *            the detail message (which is saved for later retrieval by the getMessage() method).
    * @param cause
    *            the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted,
    *            and indicates that the cause is nonexistent or unknown.)
    */
   public UnknownProviderException(String message, Throwable cause) {
       super(message, cause);
   }

   /**
    * 
    Constructs a new exception with the specified detail message, cause, suppression enabled or disabled, and
    * writable stack trace enabled or disabled.
    * 
    * @param message
    *            the detail message.
    * @param cause
    *            the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
    * @param enableSuppression
    *            whether or not suppression is enabled or disabled
    * @param writableStackTrace
    *            whether or not the stack trace should be writable
    */
   public UnknownProviderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
       super(message, cause, enableSuppression, writableStackTrace);
   }

}
