/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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
 */

package org.openecomp.appc.dg.mock.instance;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;

public class MockSvcLogicJavaPlugin implements SvcLogicJavaPlugin{
	private final static Logger logger = LoggerFactory.getLogger(MockSvcLogicJavaPlugin.class);
	public static String INPUT_PARAM_RESPONSE_PREFIX = "responsePrefix";
	public static String OUTPUT_STATUS_SUCCESS = "success";
	public static String OUTPUT_STATUS_FAILURE = "failure";
	public static String OUTPUT_PARAM_STATUS = "status";
	

	public void mountDevice(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException{
		logger.info("Executed MountDevice Plugin");
	}

	public void downloadDeviceConfiguration(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException{
		ctx.setAttribute("downloadStatus", "success");
		logger.info("Executed Download Device Configuration Plugin");
		throw new SvcLogicException("failed in Download..");
	}


	
	
	public void getCommonConfigInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException, FileNotFoundException, IOException {
		logger.info("Mock getCommonConfigInfo Called....");
		
	
		/***
		ctx.setAttribute("device-authentication.USER-NAME", "root");
		
		String responsePrefix = inParams.get(INPUT_PARAM_RESPONSE_PREFIX);
		responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
		
		
		ctx.setAttribute(responsePrefix +OUTPUT_PARAM_STATUS,
				OUTPUT_STATUS_SUCCESS);
				
		**/
		
	}

	
	public void getConfigFileReference(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
		logger.info("Mock getConfigFileReference Called....");
	}
	
	
	
	public void getTemplate(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		logger.info("Mock getTemplate Called....");
	}
	
	
	
	public void saveConfigFiles(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		logger.info("Mock saveConfigFiles called...");
		
	}
	
	
	
	public void updateUploadConfig(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		logger.info("Mock updateUploadConfig called...");
	}
	
	
	
	
	public void savePrepareRelationship(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
		logger.info("Mock savePrepareRelationship called...");
	}
	
	
	
	
	
	public void saveConfigBlock(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
		logger.info("Mock saveConfigBlock called...");
		
		if (StringUtils.isBlank(ctx.getAttribute("configuration-params"))) {
			logger.info("No params...");
			ctx.setAttribute("file-category", "device_configuration");
			ctx.setAttribute("deviceconfig-file-content", "deviceConfig");
		
		} else {
			logger.info("Config params exist...");
			ctx.setAttribute("file-category", "device_configuration");
			ctx.setAttribute("file-category1", "configuration_block");
			ctx.setAttribute("file-category2", "config_data");
			
		}
	
	}
	
	public void saveTemplateConfig(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
		
		logger.info("Mock saveTemplateConfig called...");
		
		if (StringUtils.isBlank(ctx.getAttribute("configuration-params"))) {
			logger.info("No params...");
			ctx.setAttribute("file-category", "device_configuration");
			ctx.setAttribute("deviceconfig-file-content", "deviceConfig");
		
		} else {
			logger.info("Config params exist...");
			ctx.setAttribute("file-category", "device_configuration");
			ctx.setAttribute("file-category1", "config_data");
			
		}
		
		
	}
}
