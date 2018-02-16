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

package org.onap.appc.adapter.chef.chefclient.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.onap.appc.adapter.chef.chefclient.api.ChefApiClient;
import org.onap.appc.adapter.chef.chefclient.api.ChefResponse;
import org.onap.appc.adapter.chef.chefclient.impl.ChefRequestBuilder.OngoingRequestBuilder;

public class ChefApiClientImpl implements ChefApiClient {

    private final HttpClient httpClient;
    private final ChefApiHeaderFactory chefApiHeaderFactory;
    private String endpoint;
    private String userId;
    private String pemPath;
    private String organizations;

    public ChefApiClientImpl(HttpClient httpClient, ChefApiHeaderFactory chefApiHeaderFactory,
        String endpoint, String organizations, String userId, String pemPath) {
        this.httpClient = httpClient;
        this.chefApiHeaderFactory = chefApiHeaderFactory;
        this.endpoint = endpoint;
        this.organizations = organizations;
        this.userId = userId;
        this.pemPath = pemPath;
    }

    @Override
    public ChefResponse get(String path) {
        OngoingRequestBuilder requestBuilder = ChefRequestBuilder.newRequestTo(endpoint)
            .httpGet()
            .withPath(path)
            .withHeaders(chefApiHeaderFactory.create("GET", path, "", userId, organizations, pemPath));
        return execute(requestBuilder);
    }

    @Override
    public ChefResponse delete(String path) {
        OngoingRequestBuilder requestBuilder = ChefRequestBuilder.newRequestTo(endpoint)
            .httpDelete()
            .withPath(path)
            .withHeaders(chefApiHeaderFactory.create("DELETE", path, "", userId, organizations, pemPath));
        return execute(requestBuilder);
    }

    @Override
    public ChefResponse post(String path, String body) {
        OngoingRequestBuilder requestBuilder = ChefRequestBuilder.newRequestTo(endpoint)
            .httpPost(body)
            .withPath(path)
            .withHeaders(chefApiHeaderFactory.create("POST", path, body, userId, organizations, pemPath));
        return execute(requestBuilder);
    }

    @Override
    public ChefResponse put(String path, String body) {
        OngoingRequestBuilder requestBuilder = ChefRequestBuilder.newRequestTo(endpoint)
            .httpPut(body)
            .withPath(path)
            .withHeaders(chefApiHeaderFactory.create("PUT", path, body, userId, organizations, pemPath));
        return execute(requestBuilder);
    }

    private ChefResponse execute(OngoingRequestBuilder chefRequest) {
        try {
            HttpResponse response = httpClient.execute(chefRequest.build());
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity httpEntity = response.getEntity();
            String responseBody = EntityUtils.toString(httpEntity);
            return ChefResponse.create(statusCode, responseBody);
        } catch (IOException ex) {
            return ChefResponse.create(HttpStatus.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (URISyntaxException ex) {
            return ChefResponse.create(HttpStatus.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
