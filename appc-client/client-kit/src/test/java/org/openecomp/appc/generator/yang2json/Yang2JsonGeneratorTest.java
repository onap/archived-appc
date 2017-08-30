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

package org.openecomp.appc.generator.yang2json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.openecomp.appc.generator.JsonHelper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Yang2JsonGeneratorTest {

    private static SchemaContext yangSchema = null;
    private static JsonNode jsonNode = null;

    public void readIOfiles() throws IOException{
        if(yangSchema == null){
            yangParse();
        }
        if(jsonNode == null){
            jsonRead();
        }

    }

    public void testJsonRpcNum(){

        int rpcYangNum = yangSchema.getModules().iterator().next().getRpcs().size();
        int rpcJsonNum = jsonNode.get("paths").size();
        Assert.assertTrue(rpcYangNum == rpcJsonNum);
    }

    public void testJsonRpcsAndInOutParams(){

        Map<String/*rpcOperation*/,Set<String> /*bodyInputParams*/> jsonBodyInputParams = new HashMap();
        Map<String/*rpcOperation*/,Set<String> /*bodyOutputParams*/> jsonBodyOutputParams= new HashMap();
        JsonHelper.populateRpcInputOutputParamsFromJson(jsonNode,jsonBodyInputParams, jsonBodyOutputParams);

        Set<RpcDefinition> rpcDefinitions = yangSchema.getModules().iterator().next().getRpcs();
        for(RpcDefinition rpcDef : rpcDefinitions){
            String rpcOperation = rpcDef.getQName().getLocalName();
            //verify all yang rpc operations & all input yang params/groupings exist in yang2json
            Assert.assertTrue(jsonBodyInputParams.containsKey(rpcOperation));
            Collection<DataSchemaNode> inputChildNodes = rpcDef.getInput().getChildNodes();
            if(inputChildNodes != null || !inputChildNodes.isEmpty()) {
                Assert.assertTrue(rpcOperation+": yang and yang2json input params size is not equal!",jsonBodyInputParams.get(rpcOperation).size() == inputChildNodes.size());
                Set<String> inputYangParams = getYangParams(inputChildNodes);
                Assert.assertTrue(rpcOperation+": yang and yang2json input params are not same!",jsonBodyInputParams.get(rpcOperation).containsAll(inputYangParams));
            }

            //verify all yang rpc operations & all output yang params/groupings exist in yang2json
            Assert.assertTrue(jsonBodyOutputParams.containsKey(rpcOperation));
            Collection<DataSchemaNode> outputChildNodes = rpcDef.getOutput().getChildNodes();
            if(outputChildNodes != null || !outputChildNodes.isEmpty()) {
                Assert.assertTrue(rpcOperation+": yang and yang2json output params size is not equal!",jsonBodyOutputParams.get(rpcOperation).size()== outputChildNodes.size());
                Set<String> outputYangParams = getYangParams(outputChildNodes);
                Assert.assertTrue(rpcOperation+": yang and yang2json output params are not same!",jsonBodyOutputParams.get(rpcOperation).containsAll(outputYangParams));
            }
        }
    }


    private Set<String> getYangParams(Collection<DataSchemaNode> dataSchemaNodes) {
        Set<String> yangParams = new HashSet();
        for(DataSchemaNode child : dataSchemaNodes){
            yangParams.add(child.getQName().getLocalName());
        }
        return yangParams;
    }



    private static void yangParse(){

        try {
            //sourceFileName
            String yangFileName = System.getProperty("inputFile");
            YangTextSchemaContextResolver yangContextResolver = null;
            yangContextResolver = YangTextSchemaContextResolver.create("yang-context-resolver");
            URL url = new File(yangFileName).toURI().toURL();
            yangContextResolver.registerSource(url);
            Optional<SchemaContext> yangSchemaContext = yangContextResolver.getSchemaContext();
            yangSchema = yangSchemaContext.get();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (YangSyntaxErrorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SchemaSourceException e) {
            e.printStackTrace();
        }


    }


    private static void jsonRead ()throws IOException {
        String jsonFilePath = System.getProperty("outputFile");
        File file = new File(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        jsonNode = mapper.readTree(file);
    }
}
