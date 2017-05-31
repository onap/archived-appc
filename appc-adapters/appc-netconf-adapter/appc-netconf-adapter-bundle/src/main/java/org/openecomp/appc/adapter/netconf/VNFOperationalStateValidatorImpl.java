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

package org.openecomp.appc.adapter.netconf;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class VNFOperationalStateValidatorImpl implements OperationalStateValidator {
    private static final String OPERATIONAL_STATE_ELEMENT_NAME = "operationalState";
    @Override
    public VnfType getVnfType() {
        return VnfType.VNF;
    }

    @Override
    public String getConfigurationFileName() {
        String configFileName = OperationalStateValidatorFactory.configuration.getProperty(this.getClass().getCanonicalName() + CONFIG_FILE_PROPERTY_SUFFIX);
        configFileName = configFileName == null?  "VnfGetOperationalStates" : configFileName;
        return configFileName;
    }

    @Override
    public void validateResponse(String response) throws APPCException {
        if(StringUtils.isEmpty(response)) {
            throw new APPCException("empty response");
        }

        boolean isValid = false;
        String errorMsg = "unexpected response";
        try {
            List<Map.Entry> operationalStateList = getOperationalStateList(response);
            if(operationalStateList != null && !operationalStateList.isEmpty()) {
                for (Map.Entry stateEntry : operationalStateList) {
                    if(!((String)stateEntry.getValue()).equalsIgnoreCase("ENABLED")){
                        errorMsg = "at least one "+OPERATIONAL_STATE_ELEMENT_NAME+" is not in valid satae. "+operationalStateList.toString();
                        isValid = false;
                        break;
                    }else{
                        isValid =true;
                    }
                }
            }else {
                errorMsg = "response without any "+OPERATIONAL_STATE_ELEMENT_NAME+" element";
            }
        } catch (Exception e ) {
            isValid = false;
            errorMsg = e.toString();
        }
        if(!isValid) throw new APPCException(errorMsg);
    }

    private static List<Map.Entry> getOperationalStateList(String xmlText) throws IOException, ParserConfigurationException, SAXException {
        List<Map.Entry> entryList = null;
        if(StringUtils.isNotEmpty(xmlText)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(new ByteArrayInputStream(xmlText.getBytes("UTF-8")));
            if(document != null) {
                Element rootElement = document.getDocumentElement();
                NodeList nodeList = rootElement.getElementsByTagName(OPERATIONAL_STATE_ELEMENT_NAME);
                if (nodeList != null && nodeList.getLength() > 0) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        String text = node.getTextContent();
                        String id = getElementID(node);
                        entryList = (entryList == null) ? new ArrayList<Map.Entry>() : entryList;
                        Map.Entry entry = new AbstractMap.SimpleEntry<String, String>(id, text);
                        entryList.add(entry);
                    }
                }
            }
        }
        return entryList;
    }

    private static String getElementID(Node node) {
        String id = null;
        Node parentNode = node.getParentNode();
        if (parentNode != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                NodeList nodeList = ((Element) parentNode).getElementsByTagName("id");
                if (nodeList != null && nodeList.getLength() > 0) {
                    Node idNode = nodeList.item(0);
                    id = idNode != null ? idNode.getTextContent() : null;
                }
            }else {
                id = parentNode.getNodeValue()+"|"+parentNode.getTextContent();
            }
        }

        id = StringUtils.isEmpty(id) ? null : StringUtils.normalizeSpace(id);
        id = StringUtils.isBlank(id) ? null : id;
        id = id != null ? id : "unknown-id";
        return id;
    }

}
