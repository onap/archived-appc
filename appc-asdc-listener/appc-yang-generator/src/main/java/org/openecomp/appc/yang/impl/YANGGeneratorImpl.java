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

package org.openecomp.appc.yang.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.openecomp.appc.yang.YANGGenerator;
import org.openecomp.appc.yang.exception.YANGGenerationException;
import org.openecomp.appc.yang.objects.Leaf;
import org.openecomp.appc.yang.type.YangTypes;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.yamlutil.ToscaExtensionYamlUtil;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("CheckStyle")
public class YANGGeneratorImpl implements YANGGenerator {

	private static final EELFLogger Log = EELFManager.getInstance().getLogger(YANGGeneratorImpl.class);
	private static final String MODULE_TYPE = "moduleType";
	private static final String LEAVES = "leaves";


	/* (non-Javadoc)
	 * @see org.openecomp.appc.yang.YANGGenerator#generateYANG(java.lang.String, java.lang.String, java.io.OutputStream)
	 */
	@Override
	public void generateYANG(String uniqueID, String tosca, OutputStream stream)
			throws  YANGGenerationException {
		Log.info("Entered into generateYANG.");
		Log.debug("Received Tosca:\n" + tosca +"\n Received uniqueID:  "+uniqueID);

		validateInput(uniqueID, tosca, stream);
		Map<String,Object> parsedToscaMap = parseTosca(tosca);
		String moduleType =parsedToscaMap.get(MODULE_TYPE).toString();
		List<Leaf> leaves = (List<Leaf>) parsedToscaMap.get(LEAVES);
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();
		Template template;

		try {
			template = ve.getTemplate("templates/YangTemplate.vm");
		} catch ( ResourceNotFoundException | ParseErrorException ex) {
			Log.error("Error while retrieving YANG Template", ex);
			throw new YANGGenerationException("Error while retrieving YANG Template",ex);
		}

		VelocityContext vc = new VelocityContext();

		vc.put("moduleName", uniqueID);
		vc.put(MODULE_TYPE, moduleType);
		vc.put(LEAVES, leaves);

		StringWriter sw = new StringWriter();
		template.merge(vc,sw);
		Log.debug("generated YANG \n "+sw.toString());
		try {
			String yang = sw.toString();
			validateYang(yang);
			stream.write(yang.getBytes());
			stream.flush();
		} catch (IOException e) {
			Log.error("Error while writing to outputstream", e);
			throw new YANGGenerationException("Error writing to outputstream",e);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				Log.error("Error while closing outputstream", e);
			}
		}
		Log.info("exiting generateYANG method.");
	}

	private void validateYang(String yang) throws YANGGenerationException {

		try(InputStream inputYangStream = new ByteArrayInputStream(yang.getBytes())){
			YangStatementSourceImpl statementSource = new YangStatementSourceImpl(inputYangStream);
			if(statementSource.getYangAST()==null){
				throw new YANGGenerationException("Syntax Error in Generated YANG = " + yang);
			}
		}
		catch(IOException e){
			Log.error("Error validating yang file "+ yang,e);
			throw new YANGGenerationException("Invalid YANG generated",e);
		}
	}

	private Map<String,Object> parseTosca(String tosca) throws YANGGenerationException {
		Log.info("Entered into parseTosca.");
		ServiceTemplate serviceTemplate = new ToscaExtensionYamlUtil().yamlToObject(tosca, ServiceTemplate.class);
		Map<String, NodeType> nodeTypeMap = serviceTemplate.getNode_types();
		String kind = nodeTypeMap.keySet().toArray(new String[0])[0];
		NodeType nodeType = nodeTypeMap.get(kind);
		Map<String,Object> returnMap= new HashMap<>();
		Map<String, PropertyDefinition> propertyDefinitionFromTOSCA = nodeType.getProperties();
		returnMap.put(MODULE_TYPE, kind);
		List<Leaf> leaves =  new LinkedList<>();

		for(Map.Entry<String, PropertyDefinition> entry: propertyDefinitionFromTOSCA.entrySet()){
			Leaf leaf = new Leaf();
			leaf.setName(entry.getKey());
			PropertyDefinition pd = entry.getValue();
			Map<String,String> typeMap=YangTypes.getYangTypeMap();
			if (typeMap.containsKey(pd.getType())) {
				String paramType = typeMap.get(pd.getType());
				leaf.setType(paramType);
				leaf.setDescription(!StringUtils.isEmpty(pd.getDescription()) ? pd.getDescription() : "");
				leaf.setMandatory((pd.getRequired() != null) ? Boolean.toString(pd.getRequired()) : Boolean.toString(false));
				leaf.setDefaultValue((pd.get_default() != null) ? pd.get_default().toString(): "");
				leaves.add(leaf);
			} else {
				YANGGenerationException yangGenerationException = new YANGGenerationException(pd.getType() + " Type is not supported ", null);
				Log.error(pd.getType() + " Type is not supported ", yangGenerationException);
				throw yangGenerationException;
			}
		}
		returnMap.put(LEAVES, leaves);
		Log.info("exiting parseTosca method with return MAP "+returnMap);
		return returnMap;
	}

	private void validateInput(String uniqueID, String tosca, OutputStream stream) throws YANGGenerationException {
		Log.info("Entered into validateInput.");
		if(StringUtils.isEmpty(uniqueID)) {
			throw new YANGGenerationException("uniqueID is mandatory, cannot be null or empty.",null);
		}
		if(StringUtils.isEmpty(tosca)) {
			throw new YANGGenerationException("tosca is mandatory, cannot be null or empty.",null);
		}
		if(stream == null){
			throw new YANGGenerationException("stream is mandatory, cannot be null.",null);
		}
		Log.info("exiting validateInput method.");
	}

}
