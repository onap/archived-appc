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

package org.openecomp.appc.dg.common.impl;

import org.openecomp.appc.dao.util.DBUtils;
import org.openecomp.appc.rankingframework.AbstractRankedAttributesResolverFactory;
import org.openecomp.appc.rankingframework.ConfigurationEntry;
import org.openecomp.appc.rankingframework.ConfigurationSet;
import org.openecomp.appc.rankingframework.RankedAttributesResolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

abstract class AbstractResolverDataReader {

    private  class ConfigurationSetAdaptor implements ConfigurationSet<FlowKey> {

        private final ResultSet resultSet;

        private class ResultSetIterator implements Iterator<ConfigurationEntry<FlowKey>>, ConfigurationEntry<FlowKey> {
            @Override
            public boolean hasNext() {
                try {
                    return resultSet.next();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public ConfigurationEntry<FlowKey> next() {
                return this;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object getAttributeValue(String name) {
                try {
                    return resultSet.getObject(name);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public FlowKey getResult() {
                try {
                    return new FlowKey(resultSet.getString("dg_name"), resultSet.getString("dg_version"), resultSet.getString("dg_module"));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        ConfigurationSetAdaptor(ResultSet resultSet) {
            this.resultSet = resultSet;
        }

        @Override
        public Iterable<ConfigurationEntry<FlowKey>> getEntries() {
            return new Iterable<ConfigurationEntry<FlowKey>>() {

                @Override
                public Iterator<ConfigurationEntry<FlowKey>> iterator() {
                    return new ResultSetIterator();
                }
            };
        }

        @Override
        public Collection<String> getRankedAttributeNames() {
            return getAttributeNames();
        }
    }

    protected abstract Collection<String> getAttributeNames();
    protected abstract String getQueryStmt();



    RankedAttributesResolver<FlowKey> read() {
        try {
            try (Connection conn = DBUtils.getConnection("sdnctl")) {
                try (PreparedStatement stmt = conn.prepareStatement(getQueryStmt())) {
                    try (ResultSet res = stmt.executeQuery()) {
                        if (res.next()) {
                            res.beforeFirst();
                            ConfigurationSet<FlowKey> resolverConfig = new ConfigurationSetAdaptor(res);
                            return AbstractRankedAttributesResolverFactory.getInstance().create(resolverConfig);
                        } else {
                            throw new IllegalStateException();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
