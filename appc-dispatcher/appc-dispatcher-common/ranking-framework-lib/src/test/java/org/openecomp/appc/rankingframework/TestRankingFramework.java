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

package org.openecomp.appc.rankingframework;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.rankingframework.AbstractRankedAttributesResolverFactory;
import org.openecomp.appc.rankingframework.ConfigurationEntry;
import org.openecomp.appc.rankingframework.ConfigurationSet;
import org.openecomp.appc.rankingframework.RankedAttributesContext;
import org.openecomp.appc.rankingframework.RankedAttributesResolver;

public class  TestRankingFramework {

    private static final String COUNTRY = "COUNTRY";
    private static final String STATE = "STATE";
    private static final String CITY = "CITY";

    private static final List<String> NAMES = Arrays.asList(COUNTRY, STATE, CITY);

    private static final String PACIFIC = "Pacific";
    private static final String ATLANTIC = "Atlantic";
    private static final String ARCTIC = "Arctic";
    private static final String NA = "N/A";

    private static ConfigurationEntry<String> entry(String [] attributes, String result) {
        if (attributes == null || attributes.length != NAMES.size()) {
            throw new IllegalArgumentException();
        }

        Map<String, String> map = new HashMap<>(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            map.put(NAMES.get(i), attributes[i]);
        }

        return new ConfigurationEntryImpl(map, result);
    }

    private static ConfigurationSet<String> config(ConfigurationEntry<String> ... entries) {
        return new ConfigurationSetImpl(Arrays.asList(entries), NAMES);
    }

    private static RankedAttributesContext context(String ... attributes) {
        if (attributes == null || attributes.length != NAMES.size()) {
            throw new IllegalArgumentException();
        }

        Map<String, String> map = new HashMap<>(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            map.put(NAMES.get(i), attributes[i]);
        }

        return new Context(map);
    }

    private static class ConfigurationSetImpl implements ConfigurationSet<String> {

        private final Collection<ConfigurationEntry<String>> entries;
        private final Collection<String> names;

        ConfigurationSetImpl(Collection<ConfigurationEntry<String>> entries, Collection<String> names) {
            this.entries = entries;
            this.names = names;
        }

        @Override
        public Iterable<ConfigurationEntry<String>> getEntries() {
            return entries;
        }

        @Override
        public Collection<String> getRankedAttributeNames() {
            return names;
        }
    }

    private static class ConfigurationEntryImpl implements ConfigurationEntry<String> {

        private final Map<String, String> attributes;
        private final String result;

        ConfigurationEntryImpl(Map<String, String> attributes, String result) {
            this.attributes = attributes;
            this.result = result;
        }

        @Override
        public Object getAttributeValue(String name) {
            return attributes.get(name);
        }

        @Override
        public String getResult() {
            return result;
        }
    }

    private static class Context implements RankedAttributesContext {

        private final Map<String, String> map;

        Context(Map<String, String> map) {
            this.map = map;
        }

        @Override
        public Object getAttributeValue(String name) {
            return map.get(name);
        }
    }

    private static ConfigurationSet<String> testData() {
        @SuppressWarnings("unchecked")
        ConfigurationSet<String> config = config(
                entry(new String [] {"US", "CA", "SFO"}, PACIFIC),
                entry(new String [] {"US", "CA", "LA"}, PACIFIC),
                entry(new String [] {"US", "FL", "MIAMI"}, ATLANTIC),
                entry(new String [] {"US", "AK", "Barrow"}, ARCTIC),
                entry(new String [] {"US", "AK", "*"}, PACIFIC),
                entry(new String [] {"US", "*", "Houston"}, ATLANTIC),
                entry(new String [] {"US", "*", "*"}, NA)
                );

        return config;
    }

    private static RankedAttributesResolver<String> resolver(ConfigurationSet<String> config) {
        return AbstractRankedAttributesResolverFactory.getInstance().create(config);
    }

    @Test
    public void testExactMatch() {

        ConfigurationSet<String> config = testData();

        RankedAttributesResolver<String> resolver = resolver(config) ;

        RankedAttributesContext context = context("US", "CA", "SFO");

        String result = resolver.resolve(context);

        Assert.assertEquals(PACIFIC, result);
    }

    @Test
    public void testDefaultMatchPartial() {

        ConfigurationSet<String> config = testData();

        RankedAttributesResolver<String> resolver = resolver(config) ;

        RankedAttributesContext context = context("US", "AK", "Anchorage");

        String result = resolver.resolve(context);

        Assert.assertEquals(PACIFIC, result);
    }

    @Test
    public void testDefaultMatchFull() {

        ConfigurationSet<String> config = testData();

        RankedAttributesResolver<String> resolver = resolver(config) ;

        RankedAttributesContext context = context("US", "IL", "Chicago");

        String result = resolver.resolve(context);

        Assert.assertEquals(NA, result);
    }

    @Test
    public void testDefaultMatchInTheMiddle() {

        ConfigurationSet<String> config = testData();

        RankedAttributesResolver<String> resolver = resolver(config) ;

        RankedAttributesContext context = context("US", "TX", "Houston");

        String result = resolver.resolve(context);

        Assert.assertEquals(ATLANTIC, result);
    }

    @Test
    public void testBacktrace() {

        ConfigurationSet<String> config = testData();

        RankedAttributesResolver<String> resolver = resolver(config) ;

        RankedAttributesContext context = context("US", "CA", "SJC");

        String result = resolver.resolve(context);

        Assert.assertEquals(NA, result);
    }
}
