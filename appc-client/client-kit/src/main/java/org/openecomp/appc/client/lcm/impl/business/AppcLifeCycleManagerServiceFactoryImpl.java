/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.client.lcm.impl.business;

import org.openecomp.appc.client.lcm.api.AppcLifeCycleManagerServiceFactory;
import org.openecomp.appc.client.lcm.api.ApplicationContext;
import org.openecomp.appc.client.lcm.api.LifeCycleManagerStateful;
import org.openecomp.appc.client.lcm.exceptions.AppcClientException;

import java.lang.reflect.Proxy;
import java.util.Properties;

public class AppcLifeCycleManagerServiceFactoryImpl implements AppcLifeCycleManagerServiceFactory {

    private LifeCycleManagerStateful lifeCycleManagerStateful;
    private LCMRequestProcessor lcmRequestProcessor;

    @Override
    public synchronized LifeCycleManagerStateful createLifeCycleManagerStateful(ApplicationContext context, Properties properties) throws AppcClientException{
        if (lifeCycleManagerStateful == null) {
            lcmRequestProcessor = new LCMRequestProcessor(context, properties);
            lifeCycleManagerStateful = (LifeCycleManagerStateful) Proxy.newProxyInstance(LifeCycleManagerStateful.class.getClassLoader(), new Class<?>[]{LifeCycleManagerStateful.class}, new RPCInvocator(lcmRequestProcessor));
        }
        else {
            throw new IllegalStateException("already instansiated LifeCycleManagerStateful instance");
        }
        return lifeCycleManagerStateful;
    }

    @Override
    public void shutdownLifeCycleManager(boolean isForceShutdown) {
        if(lcmRequestProcessor != null){
            lcmRequestProcessor.shutdown(isForceShutdown);
        }
        else{
            throw new IllegalStateException("The life cycle manager library wasn't instantiated properly, therefore the shutdown event will not be handled");
        }
    }
}
