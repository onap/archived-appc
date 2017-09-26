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

package org.openecomp.appc.data.services.node;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

import org.openecomp.appc.data.services.AppcDataServiceConstant;
import org.openecomp.appc.data.services.db.DGGeneralDBService;
import org.openecomp.appc.data.services.utils.EscapeUtils;

import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

public class ConfigResourceNode implements SvcLogicJavaPlugin {

	private static final EELFLogger log = EELFManager.getInstance().getLogger(ConfigResourceNode.class);

	public void getConfigFileReference(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received getConfigFiles call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

		try {

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			DGGeneralDBService db = DGGeneralDBService.initialise();
			QueryStatus status = db.getConfigFileReferenceByFileTypeNVnfType(ctx, "configfilereference-deviceconfig",
					"device_configuration");

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to Read ConfigFileReference:device-configuration");

			status = db.getConfigFileReferenceByFileTypeNVnfType(ctx, "configfilereference-success",
					"configuration_success");

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to Read ConfigFileReference:configuration_success");

			status = db.getConfigFileReferenceByFileTypeNVnfType(ctx, "configfilereference-failure",
					"configuration_error");

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to Read ConfigFileReference:configuration_error");

			status = db.getConfigFileReferenceByFileTypeNVnfType(ctx, "configfilereference-log", "log");

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to Read ConfigFileReference:configuration_log");


			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("GetConfigFileReference Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in GetConfigFileReference " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}

	public void getCommonConfigInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received getDeviceInfo call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

		try {

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			DGGeneralDBService db = DGGeneralDBService.initialise();
			QueryStatus status = db.getDeviceAuthenticationByVnfType(ctx, "device-authentication");

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to Read device_authentication");

			status = db.getDeviceProtocolByVnfType(ctx, "tmp.deviceinterfaceprotocol");

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to Read device_interface_protocol");

			status = db.getConfigureActionDGByVnfTypeNAction(ctx, "tmp.configureactiondg");
			if (status == QueryStatus.FAILURE)
				throw new Exception("Unable to Read configure_action_dg");

			if (status == QueryStatus.NOT_FOUND) {
				status = db.getConfigureActionDGByVnfType(ctx, "tmp.configureactiondg");

				if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
					throw new Exception("Unable to Read configure_action_dg");
			}





			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("getCommonConfigInfo Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in getCommonConfigInfo " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}


	// fileCategory Can be  config_template, parameter_definitions, parameter_yang
	public void getTemplate(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received getTemplate call with params : " + inParams);


		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
		String fileCategory = inParams.get(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY);
		String templateName = ctx.getAttribute("template-name");
		QueryStatus status = null;
		String responsePrefix1 = "";

		try {

			responsePrefix1 = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			log.info("RESPONSEPREFIX : " + responsePrefix);
			log.info("RESPONSEPREFIX1 : " + responsePrefix1);

			DGGeneralDBService db = DGGeneralDBService.initialise();

			if (StringUtils.isBlank(templateName)) {

				//if ( !StringUtils.isBlank(ctx.getAttribute("vnfc-type"))) {


				status = db.getTemplate(ctx, responsePrefix, fileCategory);
				if (status == QueryStatus.FAILURE)
					throw new Exception("Unable to Read " + fileCategory );
				//}

				if (status == QueryStatus.NOT_FOUND) {


					status = db.getTemplateByVnfTypeNAction(ctx, responsePrefix, fileCategory);

					if (status == QueryStatus.FAILURE)
						throw new Exception("Unable to Read " + fileCategory );

					if (status == QueryStatus.NOT_FOUND) {

						//status = db.getTemplateByVnfType(ctx, responsePrefix, fileCategory);

						//if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
							throw new Exception("Unable to Read " + fileCategory );
					}
				}
			} else {

				status = db.getTemplateByTemplateName(ctx, responsePrefix, templateName);

				if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
					throw new Exception("Unable to Read " + fileCategory + " template");
			}


			ctx.setAttribute(responsePrefix1 + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("GetTemplate Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix1 + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix1 + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in getTemplate " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}

	public void saveConfigFiles(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received saveConfigFiles call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

		try {

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			DGGeneralDBService db = DGGeneralDBService.initialise();
			QueryStatus status = db.saveConfigFiles(ctx, "tmp.configFiles");

			if (status == QueryStatus.FAILURE)
				throw new Exception("Unable to Save " + ctx.getAttribute("file-category") + " in configfiles");

			status = db.getMaxConfigFileId(ctx, "tmp.configfilesmax", ctx.getAttribute("file-category"));

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to get " + ctx.getAttribute("file-category") + " from configfiles");

			status = db.savePrepareRelationship(ctx, "tmp.preparerel",
					ctx.getAttribute("tmp.configfilesmax.configfileid"), "N");
			if (status == QueryStatus.FAILURE)
				throw new Exception("Unable to save prepare_relationship");

			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("saveConfigFiles Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in saveConfigFiles " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}

	public void updateUploadConfig(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received updateUploadConfig call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

		try {

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			DGGeneralDBService db = DGGeneralDBService.initialise();

			ctx.setAttribute("tmp.escaped.devicerunningconfig",
					EscapeUtils.escapeSql(ctx.getAttribute("device-running-config")));

			QueryStatus status = db.saveUploadConfig(ctx, "tmp.uploadConfig");

			if (status == QueryStatus.FAILURE)
				throw new Exception("Unable to Save configuration in upload_config");

			/*status = db.getMaxUploadConfigFileId(ctx, "tmp.uploadconfigmax");

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to get record from upload_config");

			status = db.updateUploadConfig(ctx, "tmp.uploadConfig",
					Integer.parseInt(ctx.getAttribute("tmp.uploadconfigmax.uploadconfigid")));
			if (status == QueryStatus.FAILURE)
				throw new Exception("Unable to upload upload_config");*/

			status = db.getUploadConfigInfo(ctx, "tmp.uploadConfigInfo");

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to get record from upload_config");

			status = db.updateUploadConfig(ctx, "tmp.uploadConfig",
					Integer.parseInt(ctx.getAttribute("tmp.uploadConfigInfo.UPLOAD-CONFIG-ID")));
			if (status == QueryStatus.FAILURE)
				throw new Exception("Unable to upload upload_config");

			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("updateUploadConfig Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in updateUploadConfig  " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}

	public void savePrepareRelationship(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received savePrepareRelationship call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
		String sdcArtifactInd = inParams.get(AppcDataServiceConstant.INPUT_PARAM_SDC_ARTIFACT_IND);
		String fileId = inParams.get(AppcDataServiceConstant.INPUT_PARAM_FILE_ID);
		try {

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			DGGeneralDBService db = DGGeneralDBService.initialise();

			QueryStatus status = db.savePrepareRelationship(ctx, "tmp.preparerel", fileId, sdcArtifactInd);
			if (status == QueryStatus.FAILURE)
				throw new Exception("Unable to save prepare_relationship");

			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("savePrepareRelationship Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in saveConfigFiles " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}

	public void saveConfigBlock(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received saveConfigBlock call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

		try {

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			ctx.setAttribute("tmp.convertconfig.escapeData",
					EscapeUtils.escapeSql(ctx.getAttribute("configuration")));
			DGGeneralDBService db = DGGeneralDBService.initialise();

			if (StringUtils.isBlank(ctx.getAttribute("configuration-params"))) {
				saveDeviceConfiguration(inParams, ctx, "Request", ctx.getAttribute("tmp.convertconfig.escapeData"),
						ctx.getAttribute("configuration"));
			} else {

				saveConfigurationBlock(inParams, ctx);

				ctx.setAttribute("tmp.convertconfig.escapeData",
						EscapeUtils.escapeSql(ctx.getAttribute("tmp.merge.mergedData")));
				saveDeviceConfiguration(inParams, ctx, "Configurator", ctx.getAttribute("tmp.convertconfig.escapeData"),
						ctx.getAttribute("tmp.merge.mergedData"));

				saveConfigurationData(inParams, ctx);
			}

			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("saveConfigBlock Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in saveConfigBlock " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}

	public void saveTemplateConfig(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received saveTemplateConfig call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

		try {

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			DGGeneralDBService db = DGGeneralDBService.initialise();

			if (StringUtils.isBlank(ctx.getAttribute("configuration-params"))) {

				ctx.setAttribute("tmp.convertconfig.escapeData",
						EscapeUtils.escapeSql(ctx.getAttribute("config-template.file-content")));
				saveDeviceConfiguration(inParams, ctx, "Template", ctx.getAttribute("tmp.convertconfig.escapeData"),
						ctx.getAttribute("config-template.file-content"));

			} else {
				saveConfigurationData(inParams, ctx);

				ctx.setAttribute("tmp.convertconfig.escapeData",
						EscapeUtils.escapeSql(ctx.getAttribute("tmp.merge.mergedData")));
				saveDeviceConfiguration(inParams, ctx, "Configurator", ctx.getAttribute("tmp.convertconfig.escapeData"),
						ctx.getAttribute("tmp.merge.mergedData"));

			}

			QueryStatus status = db.savePrepareRelationship(ctx, "tmp.preparerel",
					ctx.getAttribute("config-template.config-file-id"), "Y");
			if (status == QueryStatus.FAILURE)
				throw new Exception("Unable to save prepare_relationship");

			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("saveTemplateConfig Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in saveTemplateConfig " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}




	public void saveStyleSheetConfig(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {


		log.info("Received saveStyleSheet call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

		try {

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			ctx.setAttribute("tmp.convertconfig.escapeData",
					EscapeUtils.escapeSql(ctx.getAttribute("tmp.merge.mergedData")));
			saveDeviceConfiguration(inParams, ctx, "StyleSheet", ctx.getAttribute("tmp.convertconfig.escapeData"),
					ctx.getAttribute("tmp.merge.mergedData"));


			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS, AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("saveStyleSheet Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS, AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE,e.getMessage());
			log.error("Failed in saveStyleSheet " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}


	public void getSmmChainKeyFiles(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {


		log.info("Received saveStyleSheet call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
		String siteLocation = ctx.getAttribute("site-location");

		QueryStatus status = null;

		try{


			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			DGGeneralDBService db = DGGeneralDBService.initialise();



			status = db.getTemplateByArtifactType(ctx,  "smm", "smm", siteLocation);

			if ( status == QueryStatus.FAILURE )
				throw new Exception("Unable to Read smm file");


			status = db.getTemplateByArtifactType(ctx,  "intermediate-ca-chain", "intermediate_ca_chain", siteLocation);

			if ( status == QueryStatus.FAILURE )
				throw new Exception("Unable to Read intermediate_ca_chain file");




			status = db.getTemplateByArtifactType(ctx,  "server-certificate-and-key", "server_certificate_and_key", siteLocation);

			if ( status == QueryStatus.FAILURE )
				throw new Exception("Unable to Read server_certificate_and_key file");


			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS, AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("saveStyleSheet Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS, AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE,e.getMessage());
			log.error("Failed in saveStyleSheet " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}


	public void saveDeviceConfiguration(Map<String, String> inParams, SvcLogicContext ctx, String dataSource,
			String fileContent, String deviceConfig) throws SvcLogicException {
		ctx.setAttribute("data-source", dataSource);
		ctx.setAttribute("file-content", fileContent);
		ctx.setAttribute("file-category", "device_configuration");
		ctx.setAttribute("deviceconfig-file-content", deviceConfig);

		saveConfigFiles(inParams, ctx);
	}

	public void saveConfigurationBlock(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
		ctx.setAttribute("data-source", "Request");
		ctx.setAttribute("file-content", ctx.getAttribute("tmp.convertconfig.escapeData"));
		ctx.setAttribute("file-category", "configuration_block");
		saveConfigFiles(inParams, ctx);
	}

	public void saveConfigurationData(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
		ctx.setAttribute("data-source", ctx.getAttribute("originator-id"));
		ctx.setAttribute("file-content", ctx.getAttribute("configuration-params"));
		ctx.setAttribute("file-category", "config_data");
		saveConfigFiles(inParams, ctx);
	}


	public void getConfigFilesByVnfVmNCategory(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received getConfigFilesByVnfVmNCategory call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
		String fileCategory = inParams.get(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY);
		String vnfId = inParams.get(AppcDataServiceConstant.INPUT_PARAM_VNF_ID);
		String vmName = inParams.get(AppcDataServiceConstant.INPUT_PARAM_VM_NAME);
		try {


			DGGeneralDBService db = DGGeneralDBService.initialise();

			QueryStatus status = db.getConfigFilesByVnfVmNCategory(ctx, responsePrefix, fileCategory, vnfId, vmName);

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to get " + ctx.getAttribute("fileCategory") + " from configfiles");


			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("getConfigFilesByVnfVmNCategory Successful " + ctx.getAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS));
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in getConfigFilesByVnfVmNCategory " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}


	public void getDownloadConfigTemplateByVnf(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received getDownloadConfigTemplateByVnfNProtocol call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
		try {
			DGGeneralDBService db = DGGeneralDBService.initialise();

			QueryStatus status = db.getDownloadConfigTemplateByVnf(ctx, responsePrefix);

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to get download config template.");


			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,	AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("getDownloadConfigTemplateByVnf Successful " + ctx.getAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS));
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in getDownloadConfigTemplateByVnf " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}


	public void saveConfigTransactionLog(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {



		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

		String messageType = inParams.get(AppcDataServiceConstant.INPUT_PARAM_MESSAGE_TYPE);
		String message = inParams.get(AppcDataServiceConstant.INPUT_PARAM_MESSAGE);

		try {

			SvcLogicContext logctx = new SvcLogicContext();

			String escapedMessage = EscapeUtils.escapeSql(message);


			logctx.setAttribute("request-id", ctx.getAttribute("request-id"));
			logctx.setAttribute("log-message-type", messageType);
			logctx.setAttribute("log-message", escapedMessage);


			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			DGGeneralDBService db = DGGeneralDBService.initialise();
			QueryStatus status = db.saveConfigTransactionLog( logctx, responsePrefix);

			logctx.setAttribute("log-message", null);

			if (status == QueryStatus.FAILURE)
				throw new Exception("Unable to insert into config_transaction_log");


		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());


			throw new SvcLogicException(e.getMessage());
		}
	}



	public void getVnfcReference(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received getVnfcReference call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);


		QueryStatus status = null;

		try {


			DGGeneralDBService db = DGGeneralDBService.initialise();

			if ( !StringUtils.isBlank(ctx.getAttribute("vnfc-type"))) {
				status = db.getVnfcReferenceByVnfcTypeNAction(ctx, responsePrefix);

				if ( status == QueryStatus.FAILURE)
					throw new Exception("Unable to Read vnfc-reference");
			}
			//else if (status == QueryStatus.NOT_FOUND ) {
			status = db.getVnfcReferenceByVnfTypeNAction(ctx, responsePrefix);

			if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE)
				throw new Exception("Unable to Read vnfc reference");

			//}

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("getVnfcReference Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in getVnfcReference " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}

	public void getCapability(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
		log.info("Received getCapability call with params : " + inParams);
		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
		responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
		String caplevel = inParams.get("caplevel");
		String findCapability = inParams.get("checkCapability");

		try {			
			DGGeneralDBService db = DGGeneralDBService.initialise();			
			String cap = db.getCapability(ctx, inParams.get("vnf-type"));
			ObjectMapper mapper = new ObjectMapper();
			JsonNode caps = mapper.readTree(cap);
			log.info("From DB =   " + caps);
			JsonNode capabilities = caps.get("capabilities");
			log.info("capabilities =   " + capabilities);
			if(caplevel !=null && !caplevel.isEmpty()){
				JsonNode subCapabilities = capabilities.get(caplevel);
				log.info("subCapabilities =  " +  caplevel + " : " + subCapabilities);
				if(findCapability !=null && !findCapability.isEmpty()){
					if(subCapabilities != null && subCapabilities.toString().contains(findCapability))
						ctx.setAttribute(responsePrefix + "capabilities." + caplevel + "." +  findCapability,
								"Supported");
					else
						ctx.setAttribute(responsePrefix + "capabilities." + caplevel + "." +  findCapability,
								"Not-Supported");
				}
				else
				{
					ctx.setAttribute(responsePrefix + "capabilities." + caplevel,
							subCapabilities.toString());
				}
			
			}
			else
				ctx.setAttribute(responsePrefix + "capabilities",
						capabilities.toString());
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("getCapability Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in getCapability " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}


	/*public void getUploadConfigInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received getUploadConfigInfo call with params : " + inParams);

		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

		String uploadConfigId = inParams.get(AppcDataServiceConstant.INPUT_PARAM_UPLOAD_CONFIG_ID);
		QueryStatus status = null;

		int id = 0;
		try {


			DGGeneralDBService db = DGGeneralDBService.initialise();

			if ( uploadConfigId != null )
				id = Integer.parseInt(uploadConfigId);

			status = db.getUploadConfigInfo(ctx, responsePrefix,id);

			if ( status == QueryStatus.FAILURE || status == QueryStatus.NOT_FOUND)
				throw new Exception("Unable to Read upload-config");


			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
			log.info("getUploadConfigInfo Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
					AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in getUploadConfigInfo " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}
	 */

}
