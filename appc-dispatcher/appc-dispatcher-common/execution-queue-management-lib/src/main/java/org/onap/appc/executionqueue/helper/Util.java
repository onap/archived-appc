/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.executionqueue.helper;

import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Util {

    private int defaultQueueSize = 10;
    private int defaultThreadpoolSize = 10;
    private String queueSizeKey = "appc.dispatcher.executionqueue.backlog.size";
    private String threadpoolSizeKey = "appc.dispatcher.executionqueue.threadpool.size";
    private EELFLogger logger = EELFManager.getInstance().getLogger(Util.class);

    private Configuration configuration;

    /**
     * Initialization.
     * <p>Used by blueprint.
     */
    public void init() {
        configuration = ConfigurationFactory.getConfiguration();
    }

    public int getExecutionQueueSize() {
        String sizeStr = configuration.getProperty(queueSizeKey, String.valueOf(defaultQueueSize));

        int size = defaultQueueSize;
        try {
            size = Integer.parseInt(sizeStr);
        } catch (NumberFormatException e) {
            logger.error("Error parsing dispatcher execution queue backlog size", e);
        }

        return size;
    }

    public int getThreadPoolSize() {
        String sizeStr = configuration.getProperty(threadpoolSizeKey, String.valueOf(defaultThreadpoolSize));

        int size = defaultThreadpoolSize;
        try {
            size = Integer.parseInt(sizeStr);
        } catch (NumberFormatException e) {
            logger.error("Error parsing dispatcher execution queue threadpool size", e);
        }

        return size;
    }

    public ThreadFactory getThreadFactory(final boolean isDaemon, final String threadNamePrefix) {
        return new ThreadFactory() {
            private final String THREAD_NAME_PATTERN = "%s-%d";
            private final ThreadFactory factory = Executors.defaultThreadFactory();
            private final AtomicInteger counter = new AtomicInteger();

            public Thread newThread(Runnable r) {
                Thread t = factory.newThread(r);
                t.setDaemon(isDaemon);
                if (threadNamePrefix != null && !threadNamePrefix.isEmpty()) {
                    final String threadName = String.format(THREAD_NAME_PATTERN, threadNamePrefix, counter.incrementAndGet());
                    t.setName(threadName);
                }
                return t;
            }
        };
    }
}
