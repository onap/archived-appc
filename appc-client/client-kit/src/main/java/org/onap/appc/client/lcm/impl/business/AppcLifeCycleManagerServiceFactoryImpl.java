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

package org.onap.appc.client.lcm.impl.business;

import java.util.HashMap;

import org.onap.appc.client.lcm.api.AppcLifeCycleManagerServiceFactory;
import org.onap.appc.client.lcm.api.ApplicationContext;
import org.onap.appc.client.lcm.api.LifeCycleManagerStateful;
import org.onap.appc.client.lcm.exceptions.AppcClientException;

import java.lang.reflect.Proxy;
import java.util.Properties;

public class AppcLifeCycleManagerServiceFactoryImpl implements AppcLifeCycleManagerServiceFactory {

    class AppcLifeCycleManagerServiceFactoryImplData {

        private LifeCycleManagerStateful _lifeCycleManagerStateful;
        private LCMRequestProcessor _lcmRequestProcessor;

        AppcLifeCycleManagerServiceFactoryImplData(LifeCycleManagerStateful lifeCycleManagerStateful,
                                                   LCMRequestProcessor lcmRequestProcessor) {

            _lifeCycleManagerStateful = lifeCycleManagerStateful;
            _lcmRequestProcessor = lcmRequestProcessor;
        }

        LifeCycleManagerStateful getLifeCycleManagerStateful() {
            return _lifeCycleManagerStateful;
        }

        LCMRequestProcessor getLCMRequestProcessor() {
            return _lcmRequestProcessor;
        }
    }

    private HashMap<String, AppcLifeCycleManagerServiceFactoryImplData> lcmMap = new HashMap<String, AppcLifeCycleManagerServiceFactoryImplData>();

    @Override
    public synchronized LifeCycleManagerStateful createLifeCycleManagerStateful(ApplicationContext context,
                                                                                Properties properties) throws AppcClientException {
        String cType = properties.getProperty("controllerType");
        if (cType == null || cType.length() == 0)
        {
            cType = "APPC";
            properties.put("controllerType", cType);
        }

        AppcLifeCycleManagerServiceFactoryImplData lcmData = lcmMap.get(cType);
        LifeCycleManagerStateful lifeCycleManagerStateful = null;
        LCMRequestProcessor lcmRequestProcessor = null;

        if (lcmData != null) {
            lifeCycleManagerStateful = lcmData.getLifeCycleManagerStateful();
            lcmRequestProcessor = lcmData.getLCMRequestProcessor();
        }

        if (lifeCycleManagerStateful == null) {
            lcmRequestProcessor = new LCMRequestProcessor(context, properties);
            lifeCycleManagerStateful = (LifeCycleManagerStateful) Proxy.newProxyInstance(
                    LifeCycleManagerStateful.class.getClassLoader(), new Class<?>[] { LifeCycleManagerStateful.class },
                    new RPCInvocator(lcmRequestProcessor));
            lcmMap.put(cType,
                    new AppcLifeCycleManagerServiceFactoryImplData(lifeCycleManagerStateful, lcmRequestProcessor));
        } else {
            throw new IllegalStateException("already instansiated LifeCycleManagerStateful instance");
        }
        return lifeCycleManagerStateful;
    }

    public LifeCycleManagerStateful createLifeCycleManagerStateful(ApplicationContext context,
                                                                   Properties properties, String controllerType) throws AppcClientException {
        if (controllerType != null && controllerType.length() != 0)
            properties.put("controllerType", controllerType.toUpperCase());
        return createLifeCycleManagerStateful(context, properties);
    }

    @Override
    public void shutdownLifeCycleManager(boolean isForceShutdown) {

        shutdownLifeCycleManager(isForceShutdown, "APPC");
    }

    @Override
    public void shutdownLifeCycleManager(boolean isForceShutdown, String controllerType) {
        if (controllerType == null || controllerType.length() == 0)
            controllerType = "APPC";
        else
            controllerType = controllerType.toUpperCase();

        AppcLifeCycleManagerServiceFactoryImplData lcmData = lcmMap.get(controllerType);
        LCMRequestProcessor lcmRequestProcessor = null;

        if (lcmData != null) {
            lcmRequestProcessor = lcmData.getLCMRequestProcessor();
        }

        if (lcmRequestProcessor != null) {
            lcmRequestProcessor.shutdown(isForceShutdown);
        } else {
            throw new IllegalStateException(
                    "The life cycle manager library wasn't instantiated properly, therefore the shutdown event will not be handled");
        }
    }

}
