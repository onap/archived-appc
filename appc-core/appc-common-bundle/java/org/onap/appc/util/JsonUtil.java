/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {

    static ObjectMapper MAPPER = null;
    static {
        MAPPER = new ObjectMapper();
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // allow translation even
                                                                           // if extra attrs exist
                                                                           // in the json
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // Uncomment below when Jackson is upgraded to version 2.7 or above
        // MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
    }

    /**
     * @param valueAsString a valid json Map represented as String
     * @return a flat map that each entry key derived from hierarchy path in the json object and
     *         flatted to a dotted separated string. e.g.
     *         "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"}}"; will be
     *         represented as {A=A-value, B.C=B.C-value, B.D=B.D-value} when it required that the
     *         input will not be flatted the json string should be formatted as below example: e.g.
     *         "{\"A\":\"A-value\",\"B\":\"{\\\"C\\\":\\\"C-value\\\",\\\"D\\\":\\\"D-value\\\"}\"}"
     *         will be represented as {A=A-value, B={"C":"C-value","D":"D-value"}}
     * @throws IOException when the object is not valid json Map
     */
    public static Map<String, String> convertJsonStringToFlatMap(String valueAsString)
            throws IOException {
        Map readValueMap = MAPPER.readValue(valueAsString, Map.class);
        return org.onap.appc.util.ObjectMapper.map(readValueMap);
    }

    /**
     * 0 is the getStackTrace method 1 is the current method 2 is the parent method, 3 is the
     * grandparent method or the parent class in this case.
     */
    private static final int PARENT_CLASS_INDEX = 3;


    /**
     * @see #readInputJson(String, Class, Class)
     */
    public static <T> T readInputJson(String location, Class<T> returnClass) throws IOException {
        return readInputJson(location, returnClass, getCallingClass(PARENT_CLASS_INDEX));
    }

    /**
     * @param location The location or name of the file we are trying to read e.g. JsonBody.json
     * @param returnClass The class *this* Json is suppose to represent.
     * @param locationClass The starting point for json lookup. the value specified by location is
     *        relative to this class.
     * @return The object being returned
     * @throws IOException Can't find the specified json file at Location.
     */
    public static <T> T readInputJson(String location, Class<T> returnClass, Class<?> locationClass)
            throws IOException {
        try (InputStream is = locationClass.getResourceAsStream(location)) {
            validateInput(is, location);
            return MAPPER.readValue(is, returnClass);
        }
    }

    /**
     * Note that this method is sensitive to the depth of the call stack. For example if a public
     * method calls a private method, that calls this method likely the desired classIndex value is
     * 4 rather than 3. However, it's convenient to reduce the input required by callers of this
     * class.
     *
     * @param classIndex How far up the stack trace to find the class we want.
     * @return The class that called one of the public methods of this class.
     */
    private static Class<?> getCallingClass(int classIndex) {
        String className = Thread.currentThread().getStackTrace()[classIndex].getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            // Theoretically impossible.
            throw new IllegalStateException(
                    "Could not do class lookup for class in our stack trace?!?");
        }
    }

    private static void validateInput(InputStream is, String location)
            throws FileNotFoundException {
        if (is == null) {
            throw new FileNotFoundException(String.format("Could not find file at '%s'", location));
        }
    }

}
