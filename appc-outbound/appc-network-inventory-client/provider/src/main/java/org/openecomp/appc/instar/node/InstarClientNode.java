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

package org.openecomp.appc.instar.node;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.instar.interfaceImpl.InstarRestClientImpl;
import org.openecomp.appc.instar.interfaceImpl.InterfaceIpAddressImpl;
import org.openecomp.appc.instar.interfaces.RestClientInterface;
import org.openecomp.appc.instar.interfaces.RuleHandlerInterface;
import org.openecomp.appc.instar.utils.InstarClientConstant;
import org.openecomp.sdnc.config.params.data.Parameter;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

public class InstarClientNode implements SvcLogicJavaPlugin
{
	private static final EELFLogger log = EELFManager.getInstance().getLogger(InstarClientNode.class);

	public void getInstarInfo(Map<String, String> inParams, SvcLogicContext ctx)
	throws SvcLogicException{
		log.info("Received getInstarInfo call with params : " + inParams);
		String responsePrefix = (String)inParams.get(InstarClientConstant.INPUT_PARAM_RESPONSE_PRIFIX);
		try
		{
			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? responsePrefix + "." : "";			
			String [] instarKeys = getInstarKeys(inParams.get(InstarClientConstant.INSTAR_KEYS));	
			for (String instarKey : instarKeys){
				log.info("Processing Key : " + instarKey);
				log.info("Searching key for  : " + "INSTAR." + instarKey);
				ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
				RuleHandlerInterface handler = null;
				log.info("Received Context : " + ctx.getAttribute("INSTAR." + instarKey));
				Parameter params = mapper.readValue(ctx.getAttribute(InstarClientConstant.SOURCE_SYSTEM_INSTAR + "." +  instarKey), Parameter.class);
			
				log.info("Processing rule Type : "  + params.getRuleType());	
				switch(params.getRuleType()){					
					case InstarClientConstant.INTERFACE_IP_ADDRESS:
						handler = new InterfaceIpAddressImpl(params, ctx);			
						break;
					default:
						throw new Exception("No Rule Defined to process :" + params.getRuleType());			
				}
				handler.processRule();	
				
			}
			log.info("responsePrefix =" + responsePrefix);
			ctx.setAttribute(responsePrefix + InstarClientConstant.INSTAR_KEY_VALUES, ctx.getAttribute(InstarClientConstant.INSTAR_KEY_VALUES));
			ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_STATUS, InstarClientConstant.OUTPUT_STATUS_SUCCESS);
			ctx.setAttribute(InstarClientConstant.INSTAR_KEY_VALUES, null);
		}
		catch (Exception e)
		{
			ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_STATUS, InstarClientConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed processing Instar request" + e.getMessage());
			e.printStackTrace();
			throw new SvcLogicException(e.getMessage());
		}
	}
	 private static String[] getInstarKeys(String keyString) {
         String fn = "InstarClientNode.getInstarKeys";
         System.out.println("Received instar Key String as :" + keyString);

         keyString = keyString.replace("[","");
         keyString = keyString.replace("]", "");
        keyString = keyString.replace("\"", "");
         if(keyString.contains(","))
         {
                 String[] keys  = keyString.split(",");
                 return keys;
         }
         else{
                 String[] keys = {keyString};
                 return keys;
         }
	}
	public void getInstarData(Map<String, String> inParams, SvcLogicContext ctx)
			throws SvcLogicException{
				log.info("Received getInstarData call with params : " + inParams);
				String responsePrefix = (String)inParams.get(InstarClientConstant.INPUT_PARAM_RESPONSE_PRIFIX);
				try
				{
					HashMap<String, String> input  = new HashMap<String, String>();
					input.putAll(inParams);
					RestClientInterface rcINterface = new InstarRestClientImpl(input);
					String response = rcINterface.sendRequest(inParams.get("operationName"));
					
					responsePrefix = StringUtils.isNotBlank(responsePrefix) ? responsePrefix + "." : "";	
					ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_STATUS, InstarClientConstant.OUTPUT_STATUS_SUCCESS);
					ctx.setAttribute(responsePrefix + InstarClientConstant.INSTAR_KEY_VALUES, response);
												
				}
				catch (Exception e)
				{
					ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_STATUS, InstarClientConstant.OUTPUT_STATUS_FAILURE);
					ctx.setAttribute(responsePrefix + InstarClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
					log.error("Failed processing Instar request" + e.getMessage());
					e.printStackTrace();
					throw new SvcLogicException(e.getMessage());
				}
			}

}
