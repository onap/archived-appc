/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 *  ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.config.params.transformer.tosca;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang.StringUtils;

import org.openecomp.sdc.tosca.datatypes.model.*;
import org.openecomp.sdc.tosca.services.YamlUtil;
import org.openecomp.sdnc.config.params.data.Parameter;
import org.openecomp.sdnc.config.params.data.PropertyDefinition;
import org.openecomp.sdnc.config.params.data.RequestKey;
import org.openecomp.sdnc.config.params.data.ResponseKey;
import org.openecomp.sdnc.config.params.transformer.tosca.exceptions.ArtifactProcessorException;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;

public class ArtifactProcessorImpl implements ArtifactProcessor
{
    private static final String DERIVEDFROM = "org.openecomp.genericvnf";
    private static final EELFLogger Log = EELFManager.getInstance().getLogger(ArtifactProcessorImpl.class);
    private static final String EQUALSENCODING = "&equals;";
    private static final String COLONENCODING = "&colon;";
    private static final String COMMAENCODING = "&comma;";
    private static final String GREATERTHANENCODING = "&gt;";
    private static final String LESSTHANENCODING = "&lt;";

    @Override
    public void generateArtifact(PropertyDefinition artifact, OutputStream stream) throws ArtifactProcessorException
    {
        MDC.clear();
        MDC.put(MDC_SERVICE_NAME,"ArtifactGenerator");
        Log.info("Entered into generateArtifact");
        if(!StringUtils.isBlank(artifact.getKind())) {
            logArtifact(artifact);
            ServiceTemplate serviceTemplate = new ServiceTemplate();

            addNodeType(artifact, serviceTemplate);

            TopologyTemplate topologyTemplate = new TopologyTemplate();
            serviceTemplate.setTopology_template(topologyTemplate);
            addNodeTemplate(artifact, serviceTemplate);

            String tosca = new YamlUtil().objectToYaml(serviceTemplate);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            try {
                writer.write(tosca);
                writer.flush();
            } catch (IOException e) {
                Log.error("Error writing to outputstream", e);
                throw new ArtifactProcessorException(e);
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.error("Error while closing outputstream writer", e);
                }
                MDC.clear();
            }
        }
        else
        {
            Log.error("Kind in PropertyDefinition is blank or null");
            throw new ArtifactProcessorException("Kind in PropertyDefinition is blank or null");
        }
    }

    @Override
    public void generateArtifact(String artifact, OutputStream stream) throws ArtifactProcessorException
    {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            PropertyDefinition pd = mapper.readValue(artifact, PropertyDefinition.class);
            generateArtifact(pd, stream);
        }
        catch (IOException e)
        {
            Log.error("Error parsing property definition content = "+ artifact,e);
            throw new ArtifactProcessorException(e);
        }
    }

    @Override
    public PropertyDefinition readArtifact(String toscaArtifact) throws ArtifactProcessorException{
        Log.info("Entered into readArtifact.");
        Log.info("Received ToscaArtifact:\n" + toscaArtifact);

        PropertyDefinition propertyDefinitionObj = new PropertyDefinition();
        ServiceTemplate serviceTemplate = new YamlUtil().yamlToObject(toscaArtifact, ServiceTemplate.class);

        //mapping parameters
        Map<String, NodeType> nodeTypeMap = serviceTemplate.getNode_types();
        Map<String, NodeTemplate> nodeTemplateMap = serviceTemplate.getTopology_template().getNode_templates();

        String nodeTemplateName = nodeTemplateMap.keySet().toArray(new String[0])[0];
        NodeTemplate nodeTemplate = nodeTemplateMap.get(nodeTemplateName);
        Map<String, Object> nodeTemplateProperties = nodeTemplate.getProperties();

        String kind = nodeTypeMap.keySet().toArray(new String[0])[0];
        NodeType nodeType = nodeTypeMap.get(kind);
        String version = nodeType.getVersion();
        Log.info("ReadArtifact for "+ kind + " with version "+version);
        propertyDefinitionObj.setKind(kind);
        propertyDefinitionObj.setVersion(version);

        List<Parameter> parameterList = new LinkedList<>();

        Map<String, org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition> propertyDefinitionFromTOSCA = nodeType.getProperties();
        if(null != propertyDefinitionFromTOSCA){
            for (String propertyName : propertyDefinitionFromTOSCA.keySet()) {
                org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition propertyDefinition = propertyDefinitionFromTOSCA.get(propertyName);

                Parameter parameter = new Parameter();
                parameter.setName(propertyName);

                if (propertyDefinition.get_default() != null) {
                    parameter.setDefaultValue(propertyDefinition.get_default().toString());
                }
                parameter.setDescription(propertyDefinition.getDescription());
                if (null != propertyDefinition.getRequired()) {
                    parameter.setRequired(propertyDefinition.getRequired());
                } else {
                    parameter.setRequired(false);
                }

                if (StringUtils.isNotEmpty(propertyDefinition.getType())) {
                    parameter.setType(propertyDefinition.getType());
                }

                String propertValueExpr = (String) nodeTemplateProperties.get(propertyName);
                String[] stringTokens = parsePropertyValueExpression(propertValueExpr);
                String ruleType = stringTokens[0].substring(stringTokens[0].indexOf('=')+1,stringTokens[0].length()).replaceAll(">","").trim();
                String responseExpression = stringTokens[1].substring(stringTokens[1].indexOf('=')+1,stringTokens[1].length());
                String source = stringTokens[2].substring(stringTokens[2].indexOf('=')+1,stringTokens[2].length()).replaceAll(">","").trim();
                String requestExpression = stringTokens[3].substring(stringTokens[3].indexOf('=')+1,stringTokens[3].length());

                List<RequestKey> requestKeys = readRequestKeys(requestExpression);
                List<ResponseKey> responseKeys = readResponseKeys(responseExpression);

                parameter.setRuleType(ruleType);
                parameter.setSource(source);
                parameter.setRequestKeys(requestKeys);
                parameter.setResponseKeys(responseKeys);

                parameterList.add(parameter);

            }
        }
        propertyDefinitionObj.setParameters(parameterList);
        Log.info("Exiting from readArtifact. ");
        return propertyDefinitionObj;
    }

    private List<ResponseKey> readResponseKeys(String responseExpression) throws ArtifactProcessorException {
        Log.info("Entered into readResponseKeys.");
        List<ResponseKey> responseKeyList = null;
        String expression;
        expression = responseExpression.replaceAll("<", "").replaceAll(">", "").trim();
        if (StringUtils.isNotEmpty(expression)) {
            responseKeyList = new ArrayList<>();

            String[] responseKeys = expression.split(",");
            for (String responseKeyStr : responseKeys) {
                ResponseKey responseKey = new ResponseKey();
                try {
                    responseKey.setUniqueKeyName(responseKeyStr.split(":")[0].replaceAll(LESSTHANENCODING, "<").replaceAll(GREATERTHANENCODING, ">").replaceAll(COLONENCODING, ":").replaceAll(COMMAENCODING, ",").replaceAll(EQUALSENCODING,"=").trim());
                    responseKey.setUniqueKeyValue(responseKeyStr.split(":")[1].replaceAll(LESSTHANENCODING, "<").replaceAll(GREATERTHANENCODING, ">").replaceAll(COLONENCODING, ":").replaceAll(COMMAENCODING, ",").replaceAll(EQUALSENCODING,"=").trim());
                    responseKey.setFieldKeyName(responseKeyStr.split(":")[2].replaceAll(LESSTHANENCODING, "<").replaceAll(GREATERTHANENCODING, ">").replaceAll(COLONENCODING, ":").replaceAll(COMMAENCODING, ",").replaceAll(EQUALSENCODING,"=").trim());
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.error("Invalid response attribute found :" + responseKeyStr + "due to "+e);
                    throw new ArtifactProcessorException("Invalid response attribute found :" + responseKeyStr);
                }
                responseKeyList.add(responseKey);
            }
        }
        Log.info("Exiting from readResponseKeys.");
        return responseKeyList;
    }

    private List<RequestKey> readRequestKeys(String requestExpression) {
        Log.info("Entered into readRequestKeys.");
        List<RequestKey> requestKeyList = null;
        String expression;
        expression = requestExpression.replaceAll("<","").replaceAll(">","").trim();
        if(StringUtils.isNotEmpty(expression)){
            requestKeyList = new ArrayList<>();
            String[] requestKeys = expression.split(",");
            for(String responseKeyStr :requestKeys){
                RequestKey requestKey = new RequestKey();
                requestKey.setKeyName(responseKeyStr.split(":")[0].replaceAll(LESSTHANENCODING, "<").replaceAll(GREATERTHANENCODING, ">").replaceAll(COLONENCODING,":").replaceAll(COMMAENCODING,",").replaceAll(EQUALSENCODING,"=").trim());
                requestKey.setKeyValue(responseKeyStr.split(":")[1].replaceAll(LESSTHANENCODING, "<").replaceAll(GREATERTHANENCODING, ">").replaceAll(COLONENCODING,":").replaceAll(COMMAENCODING,",").replaceAll(EQUALSENCODING,"=").trim());
                requestKeyList.add(requestKey);
            }
        }
        Log.info("Exiting from readRequestKeys.");
        return requestKeyList;
    }

    private String[] parsePropertyValueExpression(String propertValueExpr) throws ArtifactProcessorException{
        Log.info("Entered into parsePropertyValueExpression.");
        String nodeRegex = "<(.*?)>";
        Pattern pattern = Pattern.compile(nodeRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(propertValueExpr);
        List<String> stringTokens = new ArrayList<>();
        while(matcher.find()){
            stringTokens.add(matcher.group(0));
        }
        String[] propertiesArr = new String[stringTokens.size()];
        propertiesArr = stringTokens.toArray(propertiesArr);
        if(propertiesArr.length!=4){
            throw new ArtifactProcessorException("Invalid input found " + propertValueExpr);
        }
        Log.info("Exiting from parsePropertyValueExpression.");
        return propertiesArr;
    }

    private void addNodeType(PropertyDefinition artifact, ServiceTemplate toscaTemplate) throws ArtifactProcessorException {
        //Add basic fields for the node
        NodeType toscaNodeType = new NodeType();
        toscaNodeType.setDerived_from(DERIVEDFROM);
        toscaNodeType.setVersion(artifact.getVersion());
        toscaNodeType.setDescription("");
        if(artifact.getParameters()!=null) {
            Map<String, org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition> toscaPropertyMap = new HashMap<>();
            toscaNodeType.setProperties(toscaPropertyMap);

            //Add properties from parameters of PD
            for (Parameter pdParameter : artifact.getParameters()) {
                addProperty(toscaNodeType, pdParameter);
            }
        }

        // This is where it adds node in node Map and adds the map in tosca template
        Map<String,NodeType> toscaNodeMap = new HashMap<>();
        toscaNodeMap.put(artifact.getKind(),toscaNodeType);
        toscaTemplate.setNode_types(toscaNodeMap);
    }

    private void addProperty(NodeType toscaNodeType, Parameter pdParameter) throws ArtifactProcessorException {
        if(!StringUtils.isBlank(pdParameter.getName())&& !pdParameter.getName().matches(".*\\s+.*")) {
            Log.info("Adding parameter " + pdParameter.getName() + " in node type");
            org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition toscaProperty = new org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition();

            toscaProperty.setType(StringUtils.isBlank(pdParameter.getType()) ? "string" : pdParameter.getType());
            toscaProperty.set_default(pdParameter.getDefaultValue());

            toscaProperty.setDescription(pdParameter.getDescription());
            toscaProperty.setRequired(pdParameter.isRequired());

            toscaNodeType.getProperties().put(pdParameter.getName(), toscaProperty);
        }
        else
        {
            String message ="Parameter name is empty,null or contains whitespace";
            Log.error(message);
            throw new ArtifactProcessorException(message);
        }
    }

    private void addNodeTemplate(PropertyDefinition artifact, ServiceTemplate toscaTemplate)
    {
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setType(artifact.getKind());
        Map<String,Object> templateProperties = new HashMap<>();
        //Add properties from parameters of PD
        if(artifact.getParameters()!=null) {
            for (Parameter pdParameter : artifact.getParameters()) {
                addTemplateProperty(templateProperties, pdParameter);
            }
            nodeTemplate.setProperties(templateProperties);
        }
        Map<String,NodeTemplate> nodeTemplateMap = new HashMap<>();
        nodeTemplateMap.put(artifact.getKind()+"_Template",nodeTemplate);
        toscaTemplate.getTopology_template().setNode_templates(nodeTemplateMap);
    }

    private void addTemplateProperty(Map<String,Object> templateProperties, Parameter pdParameter)
    {
        Log.info("Adding parameter "+ pdParameter.getName() + " in node templates");
        String responseKeys = buildResponseKeyExpression(pdParameter.getResponseKeys());
        String requestKeys = buildRequestKeyExpression(pdParameter.getRequestKeys());
        String ruleType = buildRuleType(pdParameter.getRuleType());
        String source = buildSourceSystem(pdParameter.getSource());
        String properties = ruleType + " " + responseKeys + " " + source + " " + requestKeys;
        templateProperties.put(pdParameter.getName(),properties);
    }

    protected String buildResponseKeyExpression(List<ResponseKey> responseKeys)
    {
        StringBuilder propertyBuilder = new StringBuilder();
        propertyBuilder.append("<response-keys = ");
        if(responseKeys!=null) {
            Iterator<ResponseKey> itr = responseKeys.iterator();
            while (itr.hasNext()) {
                ResponseKey res = itr.next();
                if(res!=null)
                    propertyBuilder.append(encode(res.getUniqueKeyName()) + ":" + encode(res.getUniqueKeyValue()) + ":" + encode(res.getFieldKeyName()));
                if (itr.hasNext())
                    propertyBuilder.append(" , ");
            }
        }
        propertyBuilder.append(">");
        return propertyBuilder.toString();
    }

    protected String buildRequestKeyExpression(List<RequestKey> requestKeys)
    {
        StringBuilder propertyBuilder = new StringBuilder();
        propertyBuilder.append("<request-keys = ");
        if(requestKeys!=null) {
            Iterator<RequestKey> itr = requestKeys.iterator();
            while (itr.hasNext()) {
                RequestKey res = itr.next();
                if(res!=null)
                    propertyBuilder.append(encode(res.getKeyName()) + ":" + encode(res.getKeyValue()));
                if (itr.hasNext())
                    propertyBuilder.append(" , ");
            }
        }
        propertyBuilder.append(">");
        return propertyBuilder.toString();
    }

    protected String buildRuleType(String classType)
    {
        StringBuilder propertyBuilder = new StringBuilder();
        String encodedClassType = StringUtils.isBlank(encode(classType))?"":encode(classType);
        propertyBuilder.append("<");
        propertyBuilder.append("rule-type = "+encodedClassType);
        propertyBuilder.append(">");
        return propertyBuilder.toString();
    }

    protected String buildSourceSystem(String source)
    {
        StringBuilder sourceBuilder = new StringBuilder();
        sourceBuilder.append("<source-system = ");
        sourceBuilder.append(StringUtils.isBlank(encode(source))?"":encode(source));
        sourceBuilder.append(">");
        return sourceBuilder.toString();
    }

    protected String encode(String string)
    {
        String encodedString = null;
        if(string!=null) {
            encodedString = string.trim().replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll(":","&colon;").replaceAll(",","&comma;").replaceAll("=","&equals;");
        }
        return encodedString;
    }

    private void logArtifact(PropertyDefinition artifact)
    {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String stringArtifact=null;
        try
        {
            stringArtifact = mapper.writeValueAsString(artifact);
            Log.info("Received PropertyDefinition:\n" + stringArtifact);
        }
        catch (JsonProcessingException e)
        {
            Log.error("Exception while logging artifact:",e);
        }

    }
}
