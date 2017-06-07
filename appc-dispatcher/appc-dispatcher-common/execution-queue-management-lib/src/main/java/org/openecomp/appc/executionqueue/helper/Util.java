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

package org.openecomp.appc.executionqueue.helper;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;

public class Util {

    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    public static int DEFAULT_QUEUE_SIZE = 10;
    public static int DEFAULT_THREADPOOL_SIZE = 10;

    public static int getExecutionQueSize(){
        String sizeStr = configuration.getProperty("appc.dispatcher.executionqueue.backlog.size", String.valueOf(DEFAULT_QUEUE_SIZE));
        int size = DEFAULT_QUEUE_SIZE;
        try{
            size = Integer.parseInt(sizeStr);
        }
        catch (NumberFormatException e){

        }
        return size;
    }

    public static int getThreadPoolSize(){
        String sizeStr = configuration.getProperty("appc.dispatcher.executionqueue.threadpool.size", String.valueOf(DEFAULT_THREADPOOL_SIZE));
        int size = DEFAULT_THREADPOOL_SIZE;
        try{
            size = Integer.parseInt(sizeStr);
        }
        catch (NumberFormatException e){

        }
        return size;
    }

    public static ThreadFactory getThreadFactory(final boolean isDaemon){
        return new ThreadFactory() {
            final ThreadFactory factory = Executors.defaultThreadFactory();
            public Thread newThread(Runnable r) {
                Thread t = factory.newThread(r);
                t.setDaemon(isDaemon);
                return t;
            }
        };
    }
}
