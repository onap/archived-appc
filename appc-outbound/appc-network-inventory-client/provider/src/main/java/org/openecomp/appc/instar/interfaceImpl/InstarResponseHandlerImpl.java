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

package org.openecomp.appc.instar.interfaceImpl;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openecomp.appc.instar.interfaces.ResponseHandlerInterface;
import org.openecomp.appc.instar.utils.InstarClientConstant;
import org.openecomp.sdnc.config.params.data.ResponseKey;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class InstarResponseHandlerImpl implements ResponseHandlerInterface {

	private static final EELFLogger log = EELFManager.getInstance().getLogger(InstarResponseHandlerImpl.class);

	ResponseKey resKey = null;
	SvcLogicContext ctxt = null;
	
	public InstarResponseHandlerImpl(ResponseKey filterKeys, SvcLogicContext context) {
		this.resKey = filterKeys;
		this.ctxt = context;
		
	}

	@Override
	public Object processResponse(String instarResponse, String instarKey) {	
		String fn = " InstarResponseHandlerImpl.processResponse ";
		log.info(fn + " Instar Response :" + instarResponse);
		
		JSONObject instarKeyValues;
		
		log.info("Instar Data in Context : "+ ctxt.getAttribute(InstarClientConstant.INSTAR_KEY_VALUES));
		if(ctxt.getAttribute(InstarClientConstant.INSTAR_KEY_VALUES) != null){
			instarKeyValues = new JSONObject(ctxt.getAttribute(InstarClientConstant.INSTAR_KEY_VALUES));
			log.info("Instar data already exsits :  " + instarKeyValues.toString());
		}
		else
			instarKeyValues = new JSONObject();
		JSONArray instarResponses = new JSONObject(instarResponse).getJSONArray(InstarClientConstant.INSTAR_RESPONSE_BLOCK_NAME);
		for (int i = 0; i < instarResponses.length(); i++){
			JSONObject res = instarResponses.getJSONObject(i);
			log.info(fn + "Instar Block :" + i + " Values :" +  res.toString());
			log.info(fn + "Appc Filter Key :"  +  ctxt.getAttribute(InstarClientConstant.VNF_NAME) + resKey.getUniqueKeyValue());
			
			if(res.getString(InstarClientConstant.FDQN) != null &&
					res.getString(InstarClientConstant.FDQN).equalsIgnoreCase(ctxt.getAttribute(InstarClientConstant.VNF_NAME) + resKey.getUniqueKeyValue())){					
					if(resKey.getFieldKeyName().equals(InstarClientConstant.V4_ADDRESS))
						instarKeyValues.put(instarKey, res.getString(InstarClientConstant.INSTAR_V4_ADDRESS));
					else if(resKey.getFieldKeyName().equals(InstarClientConstant.V6_ADDRESS))
						instarKeyValues.put(instarKey, res.getString(InstarClientConstant.INSTAR_V6_ADDRESS));				
					break;
			}
		}
		log.info(fn + "Instar KeyValues  :" + instarKeyValues);
		ctxt.setAttribute(InstarClientConstant.INSTAR_KEY_VALUES, instarKeyValues.toString());
		
	
		return instarKeyValues;
	}

}
