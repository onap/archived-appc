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

package org.onap.appc.design.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.introspector.BeanAccess;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.XMLConstants;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.onap.appc.design.services.util.DesignServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ValidatorService {

    private static final Logger log = LoggerFactory.getLogger(ValidatorService.class);

    public String execute(String action, String payload, String dataType) throws ValidatorException {

        log.info("Received validation for action= " + action + "Data :" + payload + " dataType = " + dataType);
        String validateResponse  = null;

        try{
            switch (dataType) {
                case DesignServiceConstants.DATA_TYPE_XML:
                    validateResponse = validateXML(payload);
                    break;
                case DesignServiceConstants.DATA_TYPE_JSON:
                    validateResponse = validateJSON(payload);
                    break;
                case DesignServiceConstants.DATA_TYPE_VELOCITY:
                    validateResponse = validateVelocity(payload);
                    break;
                case DesignServiceConstants.DATA_TYPE_YAML:
                    validateResponse = validateYAML(payload);
                    break;
                default:
                    break;
            }
        }
        catch (ParserConfigurationException | SAXException | IOException e){
            log.info("An error occurred while executing validator", e);
            throw new ValidatorException("An error occurred while executing validator", e);
        }
        return validateResponse;
    }

    private String validateYAML(String payload) throws IOException {
        try {
            InputStream is = new ByteArrayInputStream(payload.getBytes());

            Reader in = new InputStreamReader(is);
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            yaml.load(in);
            return DesignServiceConstants.SUCCESS;
        } catch (Exception e) {
            log.error("Not a Valid YAML Format", e);
            throw e;
        }

    }

    private String validateVelocity(String payload) {

        try {
            VelocityEngine engine = new VelocityEngine();
            engine.setProperty(Velocity.RESOURCE_LOADER, "string");
            engine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
            engine.addProperty("string.resource.loader.repository.static", "false");
            engine.init();
            StringResourceRepository repo = (StringResourceRepository) engine
                .getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
            repo.putStringResource("TestTemplate", payload);

            return DesignServiceConstants.SUCCESS;
        } catch (ResourceNotFoundException | ParseErrorException | MethodInvocationException e) {
            log.error("Not a Valid Velocity Template", e);
            throw e;
        }
    }

    private String validateJSON(String payload) throws IOException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(payload);
            return DesignServiceConstants.SUCCESS;
        } catch (JsonProcessingException e) {
            log.error("Not a Valid JSON file", e);
            throw e;
        }

    }

    private String validateXML(String payload) throws IOException, SAXException, ParserConfigurationException {

        try {
            DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
            dBF.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dBF.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dBF.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = dBF.newDocumentBuilder();
            builder.parse(new InputSource(new ByteArrayInputStream(payload.getBytes("utf-8"))));
            return DesignServiceConstants.SUCCESS;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.info("Error While parsing Payload", e);
            throw e;
        }
    }
}

