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

package org.openecomp.appc.design.validator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Validator;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.openecomp.appc.design.services.util.DesignServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.introspector.BeanAccess;

public class ValidatorService {

    private static final Logger log = LoggerFactory.getLogger(ValidatorService.class);
    public String execute(String action, String payload, String dataType) throws Exception {
        
        String validateResponse  = null;
        log.info("Received validation for action= " + action + "Data :" + payload + " dataType = " + dataType);
        if(dataType.equals(DesignServiceConstants.DATA_TYPE_XML))
            validateResponse = validateXML(payload);
        else if(dataType.equals(DesignServiceConstants.DATA_TYPE_JSON))
            validateResponse = validateJOSN(payload);
        else if(dataType.equals(DesignServiceConstants.DATA_TYPE_VELOCITY))
            validateResponse = validateVelocity(payload);
        else if(dataType.equals(DesignServiceConstants.DATA_TYPE_YAML))
            validateResponse = validateYAML(payload);
        
        return validateResponse;

    }

    private String validateYAML(String payload) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());        
        try{
            InputStream is = new ByteArrayInputStream(payload.getBytes());

            Reader in = new InputStreamReader(is);
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            yaml.load(in);
            return DesignServiceConstants.SUCCESS;
        }
        catch(Exception e){
            log.error("Not a Valid YAML Format ");
            throw e;
        }

    }

    private String validateVelocity(String payload) {

        try{
            VelocityEngine engine = new VelocityEngine();
            engine.setProperty(Velocity.RESOURCE_LOADER, "string");
            engine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
            engine.addProperty("string.resource.loader.repository.static", "false");
            engine.init();                
            StringResourceRepository repo = (StringResourceRepository) engine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
            repo.putStringResource("TestTemplate", payload);
            //Template t = ve.getTemplate(payload);
            Template t = engine.getTemplate("TestTemplate");
            
            return DesignServiceConstants.SUCCESS;
        }
        catch(ResourceNotFoundException e ){
            log.error("Not a Valid Velocity Template ");
            throw e;
        }
        catch(ParseErrorException pe){
            log.error("Not a Valid Velocity Template ");
            throw pe;
        }
        catch(MethodInvocationException mi){
            log.error("Not a Valid Velocity Template ");
            throw mi;
        }
    }
    
    private String validateJOSN(String payload) throws Exception {

        try{ 
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(payload);
            return DesignServiceConstants.SUCCESS;
        } catch(JsonProcessingException e){
            log.error("Not a Valid JOSN file ");
            throw e;
        }

    }

    private String validateXML(String payload) throws IOException, SAXException, ParserConfigurationException {

        try{
            
            DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dBF.newDocumentBuilder();
            InputSource is = new InputSource(payload);
            builder.parse(new InputSource(new ByteArrayInputStream(payload.getBytes("utf-8"))));
            return DesignServiceConstants.SUCCESS;

        } catch(ParserConfigurationException e){
            log.info("Error While parsing Payload : " + e.getMessage());
            throw e;
        }
        catch(SAXException se){
            log.info("Error While parsing Payload : " + se.getMessage());
            throw se;
        }
        catch(IOException io){
            log.info("Error While parsing Payload : " + io.getMessage());
            throw io;
        }
    }
}

