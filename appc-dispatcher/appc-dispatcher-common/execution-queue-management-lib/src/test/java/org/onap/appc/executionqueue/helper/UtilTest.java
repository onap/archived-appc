/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.executionqueue.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.ThreadFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.att.eelf.configuration.EELFLogger;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConfigurationFactory.class)
public class UtilTest {

    private Configuration configuration;

    @Before
    public void setup() {
        PowerMockito.mockStatic(ConfigurationFactory.class);
         configuration = Mockito.mock(Configuration.class);
        PowerMockito.when(ConfigurationFactory.getConfiguration()).thenReturn(configuration);
    }

    @Test
    public void testInit() {
        Util util = new Util();
        util.init();
        assertEquals(configuration, Whitebox.getInternalState(util, "configuration"));
    }

    @Test
    public void testGetExecutionQueueSize() {
        Mockito.when(configuration.getProperty("appc.dispatcher.executionqueue.backlog.size", String.valueOf(10))).thenReturn("1");
        Util util = new Util();
        Whitebox.setInternalState(util, "configuration", configuration);
        assertEquals(1, util.getExecutionQueueSize());
    }

    @Test
    public void testGetExecutionQueueSizeExceptionFlow() {
        Mockito.when(configuration.getProperty("appc.dispatcher.executionqueue.backlog.size", String.valueOf(10))).thenReturn("Not A Number");
        Util util = new Util();
        Whitebox.setInternalState(util, "configuration", configuration);
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        Whitebox.setInternalState(util, "logger", mockLogger);
        util.getExecutionQueueSize();
        Mockito.verify(mockLogger).error("Error parsing dispatcher execution queue backlog size");
    }

    @Test
    public void testGetThreadPoolSize() {
        Mockito.when(configuration.getProperty("appc.dispatcher.executionqueue.threadpool.size", String.valueOf(10))).thenReturn("1");
        Util util = new Util();
        Whitebox.setInternalState(util, "configuration", configuration);
        assertEquals(1, util.getThreadPoolSize());
    }

    @Test
    public void testGetThreadPoolSizeExceptionFlow() {
        Mockito.when(configuration.getProperty("appc.dispatcher.executionqueue.threadpool.size", String.valueOf(10))).thenReturn("Not A Number");
        Util util = new Util();
        Whitebox.setInternalState(util, "configuration", configuration);
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        Whitebox.setInternalState(util, "logger", mockLogger);
        util.getThreadPoolSize();
        Mockito.verify(mockLogger).error("Error parsing dispatcher execution queue threadpool size");
    }

    @Test
    public void testGetThreadFactory() {
        Util util = new Util();
        Whitebox.setInternalState(util, "configuration", configuration);
        assertTrue(util.getThreadFactory(true, "prefix") instanceof ThreadFactory);
        assertTrue(util.getThreadFactory(true, "prefix").newThread(new Thread()) instanceof Thread);
    }
}
