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

package org.openecomp.appc.data.services.db;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.core.adaptors.resource.sql.SqlResource;

import org.openecomp.appc.data.services.AppcDataServiceConstant;
import org.openecomp.appc.data.services.utils.EscapeUtils;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class GeneralDataService {

	private static final EELFLogger log = EELFManager.getInstance().getLogger(GeneralDataService.class);
	
	public void saveTransactionLog(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException 
	{
		SvcLogicContext logger = new SvcLogicContext();
		String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
		String messageType = inParams.get(AppcDataServiceConstant.INPUT_PARAM_MESSAGE_TYPE);
		String message = inParams.get(AppcDataServiceConstant.INPUT_PARAM_MESSAGE);
		try 
		{
			
			String escapedMessage = EscapeUtils.escapeSql(message);
			logger.setAttribute("request-id", ctx.getAttribute("request-id"));
			logger.setAttribute("log-message-type", messageType);
			logger.setAttribute("log-message", escapedMessage);

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			DGGeneralDBService db = DGGeneralDBService.initialise();
			QueryStatus status = db.saveConfigTransactionLog( logger, responsePrefix);

			logger.setAttribute("log-message", null);
			logger.setAttribute("log-message-type", null);
			logger.setAttribute("request-id", null);

			if (status == QueryStatus.FAILURE)
				throw new Exception("Unable to insert into config_transaction_log");


		} 
		catch (Exception e) 
		{
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
			AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			throw new SvcLogicException(e.getMessage());
		}
	}
	
}
