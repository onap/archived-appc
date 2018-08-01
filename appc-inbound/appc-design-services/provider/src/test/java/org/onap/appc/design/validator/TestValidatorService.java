/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modification Copyright 2018 IBM.
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

package org.onap.appc.design.validator;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap; 
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.design.services.util.DesignServiceConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import junit.framework.Assert;

public class TestValidatorService {

//Onap Migration    

    private final Logger logger = LoggerFactory.getLogger(TestValidatorService.class);
    @Test
    public void testValidXMLValidation(){
        String response = null;
        String xmlString = "<xml><configure>" + "<operation>create</operation>" + "<base>" + "<request-information>"
                + "<source>SDN-GP</source>" + "</request-information>" + "</base>" + "</configure>" + "</xml>";

        ValidatorService vs = new ValidatorService();
        try {
            response = vs.execute("", xmlString, "XML");
        } catch (Exception e) {
        }
        Assert.assertEquals("success", response);
        
    }
    @Test
    public void testInvalidXMLValidation() {
        String response = null;
        String xmlString = "<xml><configure>" + "<operation>create</operation>" + "<base>" + "<request-information>"
                + "<source>SDN-GP</source>" + "</request-information>" + "</configure>" + "</xml>";

        ValidatorService vs = new ValidatorService();
        try {
            response = vs.execute("", xmlString, "XML");
        } catch (Exception e) {
        }
        Assert.assertEquals(null, response);
    }
    @Test
    public void testYAMLValidation() {
        String response = null;
        String YAMLString = "en:";
        ValidatorService vs = new ValidatorService();
        try {
            response = vs.execute("", YAMLString, "YAML");
        } catch (Exception e) {
        }
        Assert.assertEquals("success", response);
    }
    @Test
    public void testInvalidYAMLValidation()  {
        String response = null;
        String YAMLString = "Test \n A:";
        ValidatorService vs = new ValidatorService();
        try {
            response = vs.execute("", YAMLString, "YAML");
        } catch (Exception e) {
        }
            
        Assert.assertEquals(null, response);
    }

    @Test
    public void testJSONValidation(){
        String response = null;
        String YAMLString = "{\"Test\": \"Test1\" }";

        ValidatorService vs = new ValidatorService();
        try {
            response = vs.execute("", YAMLString, "JSON");
        } catch (Exception e) {
        }
        Assert.assertEquals("success", response);
    }
    @Test
    public void testInvalidJSONValidation(){
        String response = null;
        String YAMLString = "{\"Test\" \"Test1\" }";
        ValidatorService vs = new ValidatorService();
        try {
            response = vs.execute("", YAMLString, "JSON");
        } catch (Exception e) {
        }
        Assert.assertEquals(null, response);
    }
    
    @Test
    public void testInvalidvalidateVelocity(){
        String response = null;
        String validateVelocity = "{\"Test\" \"Test1\" }";
        ValidatorService vs = new ValidatorService();
        try {
            response = vs.execute("", validateVelocity, "Velocity");
        } catch (Exception e) {
        }
        Assert.assertEquals(null, response);
        
    }
    
    @Test
    public void testValidValidateVelocity() throws Exception{
        String response = null;
        String validateVelocity = "{\"Test\" \"Test1\" }";
        ValidatorService vs = new ValidatorService();
        response = vs.execute("", validateVelocity, "VELOCITY");
        Assert.assertEquals("success", response);
        
    }
}
