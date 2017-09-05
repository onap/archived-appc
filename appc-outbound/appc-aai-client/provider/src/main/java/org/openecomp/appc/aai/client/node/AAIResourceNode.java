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

package org.openecomp.appc.aai.client.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.openecomp.appc.aai.client.AppcAaiClientConstant;
import org.openecomp.appc.aai.client.aai.AaiService;


import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

//import com.fasterxml.jackson.databind.ObjectMapper;


public class AAIResourceNode implements SvcLogicJavaPlugin {

	private static final EELFLogger log = EELFManager.getInstance().getLogger(AAIResourceNode.class);

	
	public AaiService getAaiService() {
		return new AaiService();
	}
	/* Gets VNF Info and All VServers associated with Vnf */
	public void getVnfInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received getVnfInfo call with params : " + inParams);

		String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
		
		try {

			
			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			AaiService aai = getAaiService();
			
			
					
			aai.getGenericVnfInfo(inParams,ctx);
			
						
						
			
			ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
					AppcAaiClientConstant.OUTPUT_STATUS_SUCCESS);
			log.info("getVnfInfo Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
					AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in getVnfInfo " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}
	
	
	
	public void getAllVServersVnfcsInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
		
		log.info("Received getAllVServersVnfcsInfo call with params : " + inParams);

		String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
		
		try {
			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			AaiService aai = getAaiService();
			
			
			
			ArrayList<Map<String, String>> vservers = new ArrayList<Map<String, String>>();
			
			int vmWithNoVnfcsCount = 0;
			String  vmCountStr = ctx.getAttribute(responsePrefix + "vm-count");
			
			if ( vmCountStr == null )
				throw new Exception("Unable to get VServers for the VNF");
			
			int vmCount = Integer.parseInt(vmCountStr);
			for ( int i = 0; i < vmCount; i++ ) {
				
				SvcLogicContext vmServerCtx = new SvcLogicContext();
				
				Map<String, String> paramsVm = new HashMap<String, String>();
	            paramsVm.put("vserverId", ctx.getAttribute(responsePrefix + "vm[" + i + "].vserver-id"));
	            paramsVm.put("tenantId", ctx.getAttribute(responsePrefix +"vm[" + i + "].tenant-id"));
	            paramsVm.put("cloudOwner", ctx.getAttribute(responsePrefix +"vm[" + i + "].cloud-owner"));
	            paramsVm.put("cloudRegionId", ctx.getAttribute(responsePrefix +"vm[" + i + "].cloud-region-id"));
	        	paramsVm.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX, inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX));
	            
	            
	            
				aai.getVMInfo(paramsVm, vmServerCtx);
				
				HashMap<String, String> vserverMap = new HashMap<String, String>();
				vserverMap.put("vserver-id", ctx.getAttribute(responsePrefix + "vm[" + i + "].vserver-id"));
				vserverMap.put("tenant-id", ctx.getAttribute(responsePrefix +"vm[" + i + "].tenant-id"));
				vserverMap.put("cloud-owner", ctx.getAttribute(responsePrefix +"vm[" + i + "].cloud-owner"));
				vserverMap.put("cloud-region-id", ctx.getAttribute(responsePrefix +"vm[" + i + "].cloud-region-id"));
				
				// Parameters returned by getVMInfo
				vserverMap.put("vserver-name", vmServerCtx.getAttribute(responsePrefix + "vm.vserver-name"));
				vserverMap.put("vf-module-id", vmServerCtx.getAttribute(responsePrefix + "vm.vf-module-id"));
				
				
				// as Per 17.07 requirements we are supporting only one VNFC per VM.
			      
				String vnfcName = vmServerCtx.getAttribute(responsePrefix + "vm.vnfc[0].vnfc-name");
				vserverMap.put("vnfc-name", vnfcName);
				
				
				String vnfcCount = vmServerCtx.getAttribute(responsePrefix + "vm.vnfc-count");
				if ( vnfcCount == null )
					vnfcCount = "0";
				
				vserverMap.put("vnfc-count", vnfcCount);
				
				if ( vnfcName != null  ) {
					Map<String, String> paramsVnfc = new HashMap<String, String>();
                    paramsVnfc.put("vnfcName", vnfcName);
                   
        			paramsVnfc.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX, inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX));
        			
        			SvcLogicContext vnfcCtx = new SvcLogicContext();
	                    
        			aai.getVnfcInfo(paramsVnfc, vnfcCtx);
        			
        			vserverMap.put("vnfc-type", vnfcCtx.getAttribute(responsePrefix + "vnfc.vnfc-type"));
        			vserverMap.put("vnfc-function-code", vnfcCtx.getAttribute(responsePrefix + "vnfc.vnfc-function-code"));
        			vserverMap.put("group-notation", vnfcCtx.getAttribute(responsePrefix + "vnfc.group-notation"));
        			
	    				
				}
				else
					vmWithNoVnfcsCount++;
				
				
				
				vservers.add(vserverMap);
				
			} // vmCount
			
			
			
			
    		Collections.sort(vservers, new Comparator<Map<String, String>>() {
			    @Override
			    public int compare(Map<String, String> o1, Map<String, String> o2) {
			        return o1.get("vserver-name").compareTo(o2.get("vserver-name"));
			    }
			});
    		
    		log.info("SORTED VSERVERS " + vservers.toString());
    		
    		populateContext(vservers, ctx, responsePrefix);
    		
    		log.info("VMCOUNT IN GETALLVSERVERS " + vmCount);
    		log.info("VMSWITHNOVNFCSCOUNT IN GETALLVSERVERS " + vmWithNoVnfcsCount);
    		ctx.setAttribute(responsePrefix+"vnf.vm-count", String.valueOf(vmCount));
    		ctx.setAttribute(responsePrefix+"vnf.vm-with-no-vnfcs-count", String.valueOf(vmWithNoVnfcsCount));
    		
			
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
					AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in getAllVServersVnfcsInfo " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}
	
	
	
	
	public void populateContext(ArrayList<Map<String, String>> vservers, SvcLogicContext ctx, String prefix) {
		
		
		log.info("Populating Final Context");
		int ctr = 0;
		
		for (Map<String, String> entry : vservers) {
		    for (String key : entry.keySet()) {
		        String value = entry.get(key);
		        
		       	ctx.setAttribute(prefix+ "vm[" + ctr + "]."+ key, value);
		    	log.info("Populating Context Key = " + prefix+ "vm[" + ctr + "]."+ key + " Value = " + value);
		   	
		    }
		    
		   
		    ctr++;
		}
		
		String firstVServerName = null;
		for  ( int i =0; i < ctr; i++ ) {
			String vnfcName = ctx.getAttribute(prefix + "vm[" + i + "].vnfc-name");
		    log.info("VNFCNAME " + i + vnfcName);
		    if (  vnfcName == null && firstVServerName == null ) {
	       		firstVServerName = ctx.getAttribute(prefix + "vm[" + i + "].vserver-name");
	       		ctx.setAttribute("vm-name" , firstVServerName);
	       		log.info("Populating Context Key = " +  "vm-name" + " Value = " + firstVServerName);
		    }
		}
	}



	public void addVnfcs(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received addVnfcs call with params : " + inParams);

		String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
		
		int vnfcRefLen =0 ;
		int vmCount = 0;
		int vmWithNoVnfcCount = 0;
		
		try {

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			AaiService aai = getAaiService();
			
			
			
			String vnfcRefLenStr = ctx.getAttribute("vnfcReference_length");
			
			if ( vnfcRefLenStr == null) {
				log.info("Vnfc Reference data is missing");
				throw new Exception("Vnfc Reference data is missing");
				
			}
			else		
				vnfcRefLen = Integer.parseInt(vnfcRefLenStr);
			
			String vmWithNoVnfcCountStr = ctx.getAttribute(responsePrefix+"vnf.vm-with-no-vnfcs-count");
			
      //Commented  for backward compatibility
			
			/*if ( vmWithNoVnfcCountStr == null) {
			throw new Exception("VNFCs to be added data from A&AI is missing");
			//log.info("VNFCs to be added data from A&AI is missing");
		     }
			else
				vmWithNoVnfcCount = Integer.parseInt(vmWithNoVnfcCountStr);

			if ( vmWithNoVnfcCount!= vnfcRefLen ) 
				throw new Exception("Unable to Add Vnfcs to A&AI. Reference data mismatch.");

			String vmCountStr = ctx.getAttribute(responsePrefix+"vnf.vm-count");

			if ( vmCountStr == null)
				throw new Exception("VM data from A&AI is missing");
			else
				vmCount = Integer.parseInt(vmCountStr);


			log.info("VMCOUNT " + vmCount);
			log.info("VNFCREFLEN " + vnfcRefLen);
			aai.insertVnfcs(inParams,ctx, vnfcRefLen, vmCount);
						
		 */
		
		
		// Modified for 1710
		
			if ( vmWithNoVnfcCountStr == null) {
				log.info("Parameter VM without VNFCs(vmWithNoVnfcCountStr) from A&AI is Null");
			}
			else
				vmWithNoVnfcCount = Integer.parseInt(vmWithNoVnfcCountStr);
			
			log.info("No of VM without VNFCs(vmWithNoVnfcCount) from A&AI is " +vmWithNoVnfcCount);

			String vmCountStr = ctx.getAttribute(responsePrefix+"vnf.vm-count");

			if ( vmCountStr == null)
				throw new Exception("VM data from A&AI is missing");
			else
				vmCount = Integer.parseInt(vmCountStr);

			log.info("VMCOUNT " + vmCount);
			log.info("VNFCREFLEN " + vnfcRefLen);

			if ( vmWithNoVnfcCount!= vnfcRefLen ) {
				//throw new Exception("Unable to Add Vnfcs to A&AI. Reference data mismatch.");
				log.info("vmWithNoVnfcCount and vnfcRefLen data from table are not same ");
				aai.checkAndUpdateVnfc(inParams,ctx, vnfcRefLen, vmCount);
			}	

			else {

				aai.insertVnfcs(inParams,ctx, vnfcRefLen, vmCount);
			}

				   //// Modified 1710
			
			ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
					AppcAaiClientConstant.OUTPUT_STATUS_SUCCESS);
			
			log.info("addVnfcs Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
					AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in addVnfcs " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}
	
	
	public void updateVnfAndVServerStatus(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

		log.info("Received updateVnfAndVServerStatus call with params : " + inParams);

		String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
		
		
		int vmCount = 0;
		
		
		try {

			responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
			AaiService aai = getAaiService();
			
						
			
			String vmCountStr = ctx.getAttribute(responsePrefix+"vnf.vm-count");
			
			if ( vmCountStr == null)
				throw new Exception("VM data from A&AI is missing");
			else
				vmCount = Integer.parseInt(vmCountStr);
			
			
			log.info("VMCOUNT " + vmCount);
			
			
			aai.updateVnfStatus(inParams, ctx);
			aai.updateVServerStatus(inParams,ctx, vmCount);
			
			ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
					AppcAaiClientConstant.OUTPUT_STATUS_SUCCESS);
			
			log.info("updateVnfAndVServerStatus Successful ");
		} catch (Exception e) {
			ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
					AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
			ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
			log.error("Failed in updateVnfAndVServerStatus " + e.getMessage());

			throw new SvcLogicException(e.getMessage());
		}
	}
	
	/*public void getDummyValues(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
		
		log.info("Received getDummyValues call with params : " + inParams);

		String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
		
		try {
			
				responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
				String instarKeys = inParams.get("instarKeys");
				ObjectMapper mapper = new ObjectMapper();
				if ( instarKeys != null ) {
					
					List<String> keyList = mapper.readValue(instarKeys, ArrayList.class);
					
					Map<String, String> instarParams  =new HashMap<String, String>();
					if(keyList != null){
						//System.out.println(keyList.toString());
						
						
						for(int i=0;i<keyList.size();i++)
						{
							log.info(" -->"+keyList.get(i));
						    
						    //ctx.setAttribute(keyList.get(i), "test" + i);
						    
						    instarParams.put( keyList.get(i), "test" + i);
						}
						
					}
					log.info("INSTARPARAMMAP " + instarParams);
					String jsonString = mapper.writeValueAsString(instarParams);
					log.info(jsonString);
					ctx.setAttribute(responsePrefix + "configuration-parameters", jsonString);
					
				}

				log.info("getDummyValues Successful ");
			} catch (Exception e) {
				ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
						AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
				ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
				log.error("Failed in getDummyValues " + e.getMessage());
		
				throw new SvcLogicException(e.getMessage());
			}
	
	}
	
	*/
	/*public void getRequestKeys(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
		
		log.info("Received getRequestKeys call with params : " + inParams);

		String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
		
		try {
			
				responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
				String instarKeys = inParams.get("instarKeys");
				
				ObjectMapper mapper = new ObjectMapper();
				if ( instarKeys != null ) {
					
					List<String> keyList = mapper.readValue(instarKeys, ArrayList.class);
					
					//Map<String, String> instarParams  =new HashMap<String, String>();
					if(keyList != null){
											
						
						for(int i=0;i<keyList.size();i++)
						{
							log.info("INSTARKEY -->"+keyList.get(i));
							
						   				   
							String instarParameter = ctx.getAttribute("INSTAR." + keyList.get(i));
							log.info("INSTARPARAMETER " + instarParameter);
							Parameter param = parseParameterContent(instarParameter);
							
							log.info("PARAMETER KEY SIZE " + param.getRequestKeys().size());
							log.info("RULE TYPE " + param.getClassType());
							
							for ( int j =0 ; j < param.getRequestKeys().size() ; j++ ) {
								
								log.info(" PARAM KEY NAME " + param.getRequestKeys().get(j).getKeyName());
								log.info(" PARAM KEY VALUE " + param.getRequestKeys().get(j).getKeyValue());
							}
						    
						   // instarParams.put( keyList.get(i), "test" + i);
						}
						
					}
					//log.info("INSTARPARAMMAP " + instarParams);
					//String jsonString = mapper.writeValueAsString(instarParams);
					//log.info(jsonString);
					//ctx.setAttribute(responsePrefix + "configuration-parameters", jsonString);
					
				}

				log.info("getRequestKeys Successful ");
			} catch (Exception e) {
				ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
						AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
				ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
				log.error("Failed in getRequestKeys " + e.getMessage());
		
				throw new SvcLogicException(e.getMessage());
			}
	
	}


	public Parameter parseParameterContent(String parameter) throws JsonParseException, JsonMappingException, IOException{
		Parameter parameterDefinition = null;
		if(StringUtils.isNotBlank(parameter)){
			ObjectMapper mapper = new ObjectMapper();
			parameterDefinition = mapper.readValue(parameter, Parameter.class);
		}
		return parameterDefinition;
	}*/
}
