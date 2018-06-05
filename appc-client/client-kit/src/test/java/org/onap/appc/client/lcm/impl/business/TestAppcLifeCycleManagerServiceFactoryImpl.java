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


import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.appc.client.lcm.api.ApplicationContext;
import org.onap.appc.client.lcm.api.LifeCycleManagerStateful;
import org.onap.appc.client.lcm.exceptions.AppcClientException;
import org.onap.appc.client.lcm.impl.business.LCMRequestProcessor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestAppcLifeCycleManagerServiceFactoryImpl {

    AppcLifeCycleManagerServiceFactoryImpl appcLifeCycleManagerServiceFactory=new AppcLifeCycleManagerServiceFactoryImpl();

    @Ignore
    public void testCreateLifeCycleManagerStateful() throws AppcClientException{
        LifeCycleManagerStateful lifeCycleManagerStateful;
        ApplicationContext applicationContext=new ApplicationContext();
        applicationContext.setApplicationID("AppID");
        applicationContext.setMechID("mechId");
        String folder="src/test/resources/data";
        Properties properties =getProperties(folder);
        lifeCycleManagerStateful=appcLifeCycleManagerServiceFactory.createLifeCycleManagerStateful(applicationContext,properties);

        Assert.assertNotNull(lifeCycleManagerStateful);

    }

    public static Properties getProperties(String folder) {
        Properties prop = new Properties();

        InputStream conf = null;
        try {
            conf = new FileInputStream(folder + "client-simulator.properties");
        } catch (FileNotFoundException e) {

        }
        if (conf != null) {
            try {
                prop.load(conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("client-simulator.properties"));
            } catch (Exception e) {
                throw new RuntimeException("### ERROR ### - Could not load properties to test");
            }
        }
        return prop;
    }
}
