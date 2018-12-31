/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Ericsson
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

package org.onap.appc.dg.common.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.onap.appc.dao.util.DBUtils;
import org.onap.appc.rankingframework.AbstractRankedAttributesResolverFactory;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DBUtils.class, AbstractRankedAttributesResolverFactory.class})
public class VNFCResolverTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException, SQLException {
        PowerMockito.mockStatic(DBUtils.class);
        Connection mockConnection = Mockito.mock(Connection.class);
        PreparedStatement mockStatement = Mockito.mock(PreparedStatement.class);
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);
        PowerMockito.when(DBUtils.getConnection(Mockito.anyString())).thenReturn(mockConnection);
        PowerMockito.when(mockConnection.prepareStatement(Mockito.anyString())).thenReturn(mockStatement);
        PowerMockito.when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        PowerMockito.when(mockResultSet.next()).thenReturn(true);
    }

    @Test
    public void test() {
        VNFCResolver resolver = (VNFCResolver) ResolverFactory.createResolver("VNFC");
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Duplicated configuration entry:");
        FlowKey flowKey = resolver.resolve("Start", "null", "null", "null");
    }

}
