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

package org.onap.appc.adapter.factory;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.adapter.message.Consumer;
import org.onap.appc.adapter.message.Producer;

public class DmaapMessageAdapterFactoryImplTest {

    private DmaapMessageAdapterFactoryImpl factory;
    private Collection<String> pools;

    @Before
    public void setup() {
        factory = new DmaapMessageAdapterFactoryImpl();
        pools = new ArrayList<>();
        pools.add("http://server:1");
    }

    @Test
    public void testCreateProducer() {
        Set<String> writeTopics = new HashSet<>();
        assertTrue(factory.createProducer(pools, writeTopics, null, null) instanceof Producer);
        assertTrue(factory.createProducer(pools, "TEST" , null, null) instanceof Producer);
    }

    @Test
    public void testCreateConsumer() {
        assertTrue(factory.createConsumer(pools, "TEST", null, null, null, null, null) instanceof Consumer);
    }

}
