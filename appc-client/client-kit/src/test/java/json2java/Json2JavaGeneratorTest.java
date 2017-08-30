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

package json2java;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.generator.JsonHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class Json2JavaGeneratorTest {

    private static JsonNode jsonNode = null;
    private static final String MODEL_PACKAGE = "org.openecomp.appc.client.lcm.model";
    private static final String GENERATED_LCM_API_CLASS = "org.openecomp.appc.client.lcm.api.LifeCycleManagerStateful";

    //@Before
    public void readIOfiles() throws IOException{
        if(jsonNode == null){
            jsonRead();
        }

    }

    //@Test
    public void testGeneratedJavaModel() {
        Iterator<Map.Entry<String, JsonNode>> definitions = jsonNode.get("definitions").fields();
        for (; definitions.hasNext(); ) {
            Map.Entry<String, JsonNode> definitionEntry = definitions.next();
            String definitionEntryKey = definitionEntry.getKey();
            String className = MODEL_PACKAGE + "." + definitionEntryKey;
            Class<?> generatedClass = null;
            String errMsg = "the " + className + " was supposed to be generated, but not found";
            try {
                generatedClass = Class.forName(className);
                Assert.assertNotNull(errMsg, generatedClass);
            } catch (ClassNotFoundException e) {
                Assert.fail(errMsg);
            }

            JsonNode properties = definitionEntry.getValue().get("properties");
            if (generatedClass != null && properties != null && properties.fields() != null) {
                Field[] declaredFields = generatedClass.getDeclaredFields();
                Set<String> generatedFieldNames = new HashSet();
                for(Field field : declaredFields){
                    generatedFieldNames.add(field.getName().toLowerCase());
                }
                Iterator<Map.Entry<String, JsonNode>> propertiesFields = properties.fields();
                int totalExpectedFields = 0;
                for (; propertiesFields.hasNext(); ) {
                    totalExpectedFields++;
                    Map.Entry<String, JsonNode> propertyEntry = propertiesFields.next();
                    String propertyEntryKey = propertyEntry.getKey();
                    String fieldNameFromJson = propertyEntryKey.replaceAll(Pattern.quote("-"),"").toLowerCase();
                    errMsg = "the field " + propertyEntryKey + " for " + className + " was supposed to be generated, but not found";
                    boolean contains = generatedFieldNames.contains(fieldNameFromJson);
                    Assert.assertTrue(errMsg, contains);
                }
                Assert.assertEquals("number of fields in "+className+" are not as expected!",totalExpectedFields,generatedFieldNames.size());
            }
        }
    }

    //@Test
    public void testGeneratedJavaAPI() {
        Map<String/*rpcOperation*/,Set<String> /*bodyInputParams*/> jsonBodyInputParams = new HashMap();
        Map<String/*rpcOperation*/,Set<String> /*bodyOutputParams*/> jsonBodyOutputParams= new HashMap();
        JsonHelper.populateRpcInputOutputSchemaRefFromJson(jsonNode,jsonBodyInputParams, jsonBodyOutputParams);
        Assert.assertFalse(jsonBodyInputParams.isEmpty());
        Assert.assertFalse(jsonBodyOutputParams.isEmpty());

        //verify LifecycleManagementStatefulService was generated
        Class<?> generatedClass = null;
        String errMsg = "the " + GENERATED_LCM_API_CLASS + " was supposed to be generated, but not found";
        try {
            generatedClass = Class.forName(GENERATED_LCM_API_CLASS);
            Assert.assertNotNull(errMsg, generatedClass);
        } catch (ClassNotFoundException e) {
            Assert.fail(errMsg);
        }

        //verify LifecycleManagementStatefulService was generated with methods
        Method[] declaredMethods = generatedClass.getDeclaredMethods();
        Assert.assertNotNull("no method was generated for "+GENERATED_LCM_API_CLASS,declaredMethods);

        //verify correctness of input parameters and return type of each method
        Set<String> generatedNonVoidMethods = new HashSet();
        Set<String> generatedVoidMethods = new HashSet();
        for(Method method : declaredMethods){
            String returnType = method.getReturnType().getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            String methodName = method.getName().toLowerCase();
            if(returnType.equals("void")){
                generatedVoidMethods.add(methodName);
            }else {
                generatedNonVoidMethods.add(methodName);
                //verify correctness of return type
                String returnTypeSuffix = JsonHelper.getStringSuffix(returnType,".");
                Set<String> jsonOutputParams = jsonBodyOutputParams.get(methodName);
                Assert.assertNotNull(methodName+ " was not expected to return anything!",jsonOutputParams);
                boolean contains = jsonOutputParams.contains(returnTypeSuffix);
                Assert.assertTrue(methodName+ " was not expected to be with "+returnTypeSuffix+" return type!", contains);
            }
            //verify correctness of method input parameters
            for (Class<?> parameterType :parameterTypes){
                String parameterTypeSuffix = JsonHelper.getStringSuffix(parameterType.getName(), ".");
                if(returnType.equals("void") && parameterTypeSuffix.equals("ResponseHandler")){
                    continue;
                }
                Set<String> jsonInputParams = jsonBodyInputParams.get(methodName);
                Assert.assertNotNull(methodName+ " was not expected to be with any input parameter!",jsonInputParams);
                boolean contains = jsonInputParams.contains(parameterTypeSuffix);
                Assert.assertTrue(methodName+ " was not expected to be with "+parameterTypeSuffix+" parameter!", contains);
            }

        }

        //verify total number of generated methods is same as expected
        Assert.assertEquals("Total number of generated methods are not as expected!!",jsonBodyInputParams.size()*2,declaredMethods.length);
        //verify all expected methods(void and non void) were generated
        for(Map.Entry<String, Set<String>> rpcInputParams : jsonBodyInputParams.entrySet()){
            errMsg = "none void method " + rpcInputParams.getKey() + "(case insensitive) for " + GENERATED_LCM_API_CLASS + " was supposed to be generated, but not found";
            boolean contains = generatedNonVoidMethods.contains(rpcInputParams.getKey());
            Assert.assertTrue(errMsg, contains);

            errMsg = "void method " + rpcInputParams.getKey() + "(case insensitive) for " + GENERATED_LCM_API_CLASS + " was supposed to be generated, but not found";
            contains = generatedVoidMethods.contains(rpcInputParams.getKey());
            Assert.assertTrue(errMsg, contains);
        }

    }


    private static void jsonRead ()throws IOException {
        String jsonFilePath = System.getProperty("outputFile");
        File file = new File(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        jsonNode = mapper.readTree(file);
    }

}
