/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.appc.data.services.db;


public class MockDGGeneralDBService extends DGGeneralDBService {

    private static MockDbLibServiceQueries dbLibServiceQueries = new MockDbLibServiceQueries();

    public MockDGGeneralDBService() {

        super(dbLibServiceQueries);
        dbLibServiceQueries = new MockDbLibServiceQueries();
    }

    public MockDGGeneralDBService(MockDbLibServiceQueries dbLibServiceQueries) {
        super(dbLibServiceQueries);
        this.dbLibServiceQueries = dbLibServiceQueries;
    }

    public static MockDGGeneralDBService initialise() {
        MockDGGeneralDBService mockDGGeneralDBService = new MockDGGeneralDBService(dbLibServiceQueries);
        return mockDGGeneralDBService;
    }
}
