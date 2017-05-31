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

package org.openecomp.appc.executionqueue.impl.object;

import java.time.Instant;
import java.util.Objects;


public class QueueMessage<M extends Runnable> {
    private final M message;
    private final Instant expirationTime;
    public QueueMessage(M message, Instant expirationTime){
        this.message = message;
        this.expirationTime = Objects.requireNonNull(expirationTime);
    }

    public M getMessage() {
        return message;
    }

    public boolean isExpired() {
        return expirationTime.isBefore(Instant.now());
    }
}
