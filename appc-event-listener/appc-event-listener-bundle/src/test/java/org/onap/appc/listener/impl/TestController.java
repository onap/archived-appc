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

package org.onap.appc.listener.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.listener.Controller;
import org.onap.appc.listener.Listener;
import org.onap.appc.listener.ListenerProperties;
import org.onap.appc.listener.demo.impl.ListenerImpl;
import org.powermock.reflect.Whitebox;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class TestController {

    private ListenerProperties listenerProperties;
    private Set<ListenerProperties> properties = Mockito.spy(new HashSet<>());
    private EELFLogger log = Mockito.spy(EELFManager.getInstance().getLogger(ControllerImpl.class));

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testExceptionConstructor() {
        listenerProperties = Mockito.mock(ListenerProperties.class);
        properties.add(listenerProperties);
        new ControllerImpl(properties);
        Mockito.verify(properties).remove(Mockito.any());
    }

    @Test
    public void testStartException() throws NoSuchMethodException, SecurityException {
        Properties props = new Properties();
        props.put("TEST", "TEST");
        listenerProperties = Mockito.spy(new ListenerProperties("TEST", props));
        listenerProperties.setListenerClass(Listener.class);
        properties.add(listenerProperties);
        ControllerImpl controllerImpl = new ControllerImpl(properties);
        controllerImpl.start();
        Mockito.verify(listenerProperties, Mockito.times(2)).getListenerClass();
    }

    @Test
    public void testStopException() throws NoSuchMethodException, SecurityException, InterruptedException {
        Properties props = new Properties();
        props.put("TEST", "TEST");
        listenerProperties = Mockito.spy(new ListenerProperties("TEST", props));
        listenerProperties.setListenerClass(Listener.class);
        properties.add(listenerProperties);
        ControllerImpl controllerImpl = new ControllerImpl(properties);
        //controllerImpl.start();
        Map<String, Listener> map = Whitebox.getInternalState(controllerImpl, "listeners");
        map.put("TEST", new ListenerImpl(listenerProperties));
        ThreadPoolExecutor executor = Mockito.mock(ThreadPoolExecutor.class);
        Mockito.when(executor.awaitTermination(300, TimeUnit.SECONDS)).thenReturn(false);
        Whitebox.setInternalState(controllerImpl, "executor", executor);
        controllerImpl.stop(false);
        Mockito.verify(executor).shutdown();
    }
}
