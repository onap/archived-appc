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

package org.openecomp.appc.adapter.message;

import java.util.List;
import java.util.concurrent.Callable;

public class CallableConsumer implements Callable<List<String>> {

    private Consumer consumer;

    private int timeout = 15000;
    private int limit = 1000;

    public CallableConsumer(Consumer c) {
        this.consumer = c;
    }

    public CallableConsumer(Consumer c, int waitMs, int fetchSize) {
        this.consumer = c;
        this.timeout = waitMs;
        this.limit = fetchSize;
    }

    @Override
    public List<String> call() {
        return consumer.fetch(timeout, limit);
    }

    /**
     * The maximum amount of time to keep a connection alive. Currently is set to waitMs + 10s
     *
     * @return An integer representing the maximum amount of time to keep this thread alive
     */
    public int getMaxLife() {
        return 10000 + timeout;
    }

}
