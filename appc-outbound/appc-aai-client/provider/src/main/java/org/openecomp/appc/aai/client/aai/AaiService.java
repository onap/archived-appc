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

package org.openecomp.appc.aai.client.aai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

import org.openecomp.appc.aai.client.AppcAaiClientConstant;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class AaiService {

	private static final EELFLogger log = EELFManager.getInstance().getLogger(AaiService.class);
	 private AAIClient aaiClient;
	  
	 
	 public AaiService(AAIClient aaiClient) {
		 this.aaiClient = aaiClient;
	 }
	 
	 public AaiService() {
	        BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
	        ServiceReference sref = bctx.getServiceReference(AAIService.class);
	        aaiClient = (AAIClient) bctx.getService(sref);
	 }

	 public void getGenericVnfInfo(Map<String, String> params, SvcLogicContext ctx) throws Exception {
		 
		 	
			 	String vnfId = params.get("vnfId");
				if(StringUtils.isBlank(vnfId)){
					throw new Exception("VnfId is missing");
				}
			 	
			 	String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
			 
			 	
			 	prefix = StringUtils.isNotBlank(prefix) ? (prefix+".") : "";
					
		      		        
		        //String resourceKey = "generic-vnf.vnf-id = '" + vnfId + "' AND relationship-key = 'vserver.vserver-id'";
		       
		        String resourceKey = "generic-vnf.vnf-id = '" + vnfId + "'";
		        
		        String resourceType = "generic-vnf";
		        String queryPrefix = "vnfInfo";
		        SvcLogicContext vnfCtx = readResource(resourceKey,queryPrefix,resourceType);
	        
	        
	    
	                      	
	        	ctx.setAttribute(prefix + "vnf.vnf-name", vnfCtx.getAttribute("vnfInfo.vnf-name"));
	        	ctx.setAttribute(prefix + "vnf.vnf-type", vnfCtx.getAttribute("vnfInfo.vnf-type"));
	        	ctx.setAttribute(prefix + "vnf.prov-status", vnfCtx.getAttribute("vnfInfo.prov-status"));
	        	ctx.setAttribute(prefix + "vnf.orchestration-status", vnfCtx.getAttribute("vnfInfo.orchestration-status"));
	        	
	        	        	  
	            
	        	int vmCount = 0;
	        	
	        	
	        	String relLen = vnfCtx.getAttribute("vnfInfo.relationship-list.relationship_length");
	        	int relationshipLength = 0;
	        	if ( relLen != null )
	        		 relationshipLength = Integer.parseInt(relLen);
	        	
	        	log.info("RELLEN " + relationshipLength);
	        	for ( int i=0; i < relationshipLength; i++ ) {
	        	
			        	String vserverId = getRelationshipValue(i, vnfCtx, "vserver", "vserver.vserver-id", "vnfInfo");
			        	String tenantId = getRelationshipValue(i, vnfCtx, "vserver", "tenant.tenant-id", "vnfInfo");
			        	String cloudOwner = getRelationshipValue(i, vnfCtx, "vserver", "cloud-region.cloud-owner", "vnfInfo");
			        	String cloudRegionId = getRelationshipValue(i, vnfCtx, "vserver", "cloud-region.cloud-region-id", "vnfInfo");
			        	
			        	if ( vserverId != null ) { 
			        		
			        		log.info("VSERVER KEYS " + vserverId + " " + tenantId + " " + cloudOwner + " " + cloudRegionId);
				        	String vnfPrefix = prefix + "vm[" + vmCount + "].";
			        		
		        			ctx.setAttribute(vnfPrefix + "vserver-id", vserverId);
		        			ctx.setAttribute(vnfPrefix + "tenant-id", tenantId);
		        			ctx.setAttribute(vnfPrefix + "cloud-owner", cloudOwner);
		        			ctx.setAttribute(vnfPrefix + "cloud-region-id", cloudRegionId);
		        			
		        			vmCount++;
			        	}
	        	}
				
	        
	        			
	        	ctx.setAttribute(prefix + "vm-count", String.valueOf(vmCount));	
	        		
	        	log.info("VMCOUNT FROM VNF INFO " + ctx.getAttribute(prefix + "vm-count"));
	        		   
	     
	            
	          
	 }

	

	public void getVMInfo(Map<String, String> params,SvcLogicContext ctx ) 	throws Exception {
			log.info("Received getVmInfo call with params : " + params);
			
			String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
			
			
			prefix = StringUtils.isNotBlank(prefix) ? (prefix+".") : "";
			
			int vnfcCount = 0;
			ctx.setAttribute(prefix + "vm.vnfc-count", String.valueOf(vnfcCount)); // Incase no vnfcs are found
			
			String vserverId =  params.get("vserverId");
			if(StringUtils.isBlank(vserverId)){
				throw new Exception("VServerId is missing");
			}
			
			String tenantId = params.get("tenantId");
			if(StringUtils.isBlank(tenantId)){
				throw new Exception("TenantId is missing");
			}
			
			String cloudOwner = params.get("cloudOwner");
			if(StringUtils.isBlank(cloudOwner)){
				throw new Exception("Cloud Owner is missing");
			}
			
			String cloudRegionId = params.get("cloudRegionId");
			if(StringUtils.isBlank(cloudRegionId)){
				throw new Exception("Cloud region Id is missing");
			}
			
			    
			
	        String resourceKey =  "vserver.vserver-id = '" +vserverId + "' AND tenant.tenant-id = '" + tenantId + 
	        		"' AND cloud-region.cloud-owner = '" +cloudOwner  + 
	        		"' AND cloud-region.cloud-region-id = '" +cloudRegionId + "'";
	        
	  
	        String queryPrefix = "vmInfo";
	       
	        String resourceType = "vserver";
	        SvcLogicContext vmCtx = readResource(resourceKey,queryPrefix,resourceType);
	        
	       
	                    
	     
	       
        	ctx.setAttribute(prefix+ "vm.prov-status", vmCtx.getAttribute("vmInfo.prov-status"));
        	
        	ctx.setAttribute(prefix+ "vm.vserver-name", vmCtx.getAttribute("vmInfo.vserver-name"));
        	
        
        	
        	String relLen = vmCtx.getAttribute("vmInfo.relationship-list.relationship_length");
        	
        	
           	int relationshipLength = 0;
        	if ( relLen != null )
        		 relationshipLength = Integer.parseInt(relLen);
        	
        	log.info("RELLEN" + relationshipLength);
        	for ( int i=0; i < relationshipLength; i++ ) {
        		
        		String vfModuleId = getRelationshipValue(i, vmCtx, "vf-module", "vf-module.vf-module-id", "vmInfo");
        		
        		if ( vfModuleId != null )
        			ctx.setAttribute(prefix + "vm.vf-module-id", vfModuleId);
        		
        		
        		String vnfcName = getRelationshipValue(i, vmCtx, "vnfc", "vnfc.vnfc-name", "vmInfo");
        		
        		if ( vnfcName != null ) {
        			
        			  ctx.setAttribute(prefix + "vm.vnfc[" + vnfcCount +  "].vnfc-name", vnfcName);
        			  vnfcCount++;
        		}
        		
        		
    			 
        		
        	} //relationshipLength
        	ctx.setAttribute(prefix + "vm.vnfc-count", String.valueOf(vnfcCount));
			 
			log.info("VSERVERNAME " + ctx.getAttribute(prefix+ "vm.vserver-name") + " HAS NUM VNFCS = " + ctx.getAttribute(prefix+ "vm.vnfc-count"));
        			
	}

	
	
	private String  getRelationshipValue(int i, SvcLogicContext ctx, String relatedTo, String relationshipKey, String prefix) throws Exception {
		
	    		
    	if ( relatedTo.equals(ctx.getAttribute(prefix + ".relationship-list.relationship[" + i + "].related-to")) ) {
    			
    			
    		log.info("RELATEDTO " + relatedTo);
    		int relationshipDataLength = 0;
    		String relDataLen = ctx.getAttribute(prefix + ".relationship-list.relationship[" + i + "].relationship-data_length");
    		
    		if ( relDataLen != null ) 
    			relationshipDataLength = Integer.parseInt(relDataLen);
    			
    					
    				
    		for ( int j =0 ; j < relationshipDataLength ; j++) {
    				 				
    			String key = ctx.getAttribute(prefix + ".relationship-list.relationship[" + i + "].relationship-data[" + j + "].relationship-key");
    				
    			String value = ctx.getAttribute(prefix + ".relationship-list.relationship[" + i + "].relationship-data[" + j + "].relationship-value");
    				
    			log.info("GENERIC KEY " + key);
    			log.info("GENERIC VALUE " + value);
    			
    			if (relationshipKey.equals(key)) {
                     return value;
                         
                }
                                            
    		} // relationshipDataLength
    			
    			
    	} // if related-To
     
		
		return null;	 
			
			
	} 
		
	
	public void getVnfcInfo(Map<String, String> params,SvcLogicContext ctx ) 	throws Exception {
		log.info("Received getVnfc call with params : " + params);
		
		String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
		prefix = StringUtils.isNotBlank(prefix) ? (prefix+".") : "";
		
		String vnfcName =  params.get("vnfcName");
		if(StringUtils.isBlank(vnfcName)){
			throw new Exception("Vnfc Name is missing");
		}
		
		String resourceKey = "vnfc.vnfc-name = '" + vnfcName + "'";
		
	    String queryPrefix = "vnfcInfo";
	    String resourceType = "vnfc";
	    SvcLogicContext vnfcCtx = readResource(resourceKey,queryPrefix,resourceType);
	        
	    	// Changes for US 315820 for 1710 vnfc-type renamed to nfc-function,vnfc-function-code renamed to nfc-naming-code
	    
	     /*ctx.setAttribute(prefix+ "vnfc.vnfc-type", vnfcCtx.getAttribute("vnfcInfo.vnfc-type"));
	     ctx.setAttribute(prefix+ "vnfc.vnfc-function-code", vnfcCtx.getAttribute("vnfcInfo.vnfc-function-code"));
	     ctx.setAttribute(prefix+ "vnfc.group-notation", vnfcCtx.getAttribute("vnfcInfo.group-notation"));*/
	    
	     ctx.setAttribute(prefix+ "vnfc.vnfc-type", vnfcCtx.getAttribute("vnfcInfo.nfc-function"));
	     ctx.setAttribute(prefix+ "vnfc.vnfc-function-code", vnfcCtx.getAttribute("vnfcInfo.nfc-naming-code"));
	     ctx.setAttribute(prefix+ "vnfc.group-notation", vnfcCtx.getAttribute("vnfcInfo.group-notation"));
	        	
	  
	}
	
	public void insertVnfcs(Map<String, String> params,SvcLogicContext ctx, int vnfcRefLen, int vmCount) 	throws Exception {
			log.info("Received insertVnfcs call with params : " + params);
		
			String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
		 	
		 	prefix = StringUtils.isNotBlank(prefix) ? (prefix+".") : "";
		 	
		 		
		 	 	
			int vnfcRefIndx =-1;
			for ( int i = 0; i < vmCount ; i++ ) {
				String aaiRefKey = prefix + "vm[" + i + "].";
				
				log.info("VNFCNAME IN INSERTVNFCS "  + ctx.getAttribute(aaiRefKey + "vnfc-name"));
				String numVnfcsStr = ctx.getAttribute(aaiRefKey + "vnfc-count");
				
		 	
				//if ( numVnfcsStr != null || Integer.parseInt(numVnfcsStr) >= 1 ) 
				
				
				if ( ctx.getAttribute(aaiRefKey + "vnfc-name") != null ) 
					continue;
				else
					vnfcRefIndx++;
				
						
				// Get Vnfc_reference data
				String vnfcRefKey = "vnfcReference[" + vnfcRefIndx + "].";
				
				log.info("VNFCREFKEY " + vnfcRefKey);
				log.info("AAIREFKEY " + aaiRefKey);
				
				String vmInstance = ctx.getAttribute(vnfcRefKey+ "VM-INSTANCE");
				String vnfcInstance = ctx.getAttribute(vnfcRefKey+ "VNFC-INSTANCE");
				
				String groupNotationType = ctx.getAttribute(vnfcRefKey+ "GROUP-NOTATION-TYPE");
				String  groupNotationValue = ctx.getAttribute(vnfcRefKey+ "GROUP-NOTATION-VALUE");
				
				String  vnfcType = ctx.getAttribute(vnfcRefKey+ "VNFC-TYPE");
				
				String  vnfcFuncCode = ctx.getAttribute(vnfcRefKey+ "VNFC-FUNCTION-CODE");
				
				String  populateIpAddressV4OamVip = ctx.getAttribute(vnfcRefKey+ "IPADDRESS-V4-OAM-VIP");
				
				
				// Get vnfc Data to be added
				String vserverName = ctx.getAttribute(aaiRefKey + "vserver-name");
				String vnfcName = vserverName + vnfcFuncCode + "001";
			
				String groupNotation = getGroupNotation(groupNotationType, groupNotationValue, vnfcName, vserverName,prefix, ctx, vnfcType);
				
				
				String ipAddressV4OamVip = null;
				if ( "Y".equals(populateIpAddressV4OamVip))
					ipAddressV4OamVip = ctx.getAttribute("vnf-host-ip-address");  // from input
				
				
				Map<String, String> vnfcParams = populateVnfcParams(ctx, aaiRefKey, ipAddressV4OamVip, groupNotation, vnfcType, vnfcFuncCode);
				
				
				addVnfc( vnfcName,  vnfcParams, prefix);
				
				// Add VNFC Info to context for current added VNFC
				ctx.setAttribute(aaiRefKey + "vnfc-name", vnfcName);
				ctx.setAttribute(aaiRefKey + "vnfc-type", vnfcType);
				ctx.setAttribute(aaiRefKey + "vnfc-function-code", vnfcFuncCode);
				ctx.setAttribute(aaiRefKey + "group-notation", groupNotation);
				
			}
		 	
	
	}
	
	

	public Map<String, String> populateVnfcParams(SvcLogicContext ctx,  String aaiRefKey, 
			String ipAddressV4OamVip, String groupNotation, String vnfcType, String vnfcFuncCode)  throws Exception {
	
		
		Map<String, String> vnfcParams = new HashMap<String, String>();
		 
		// Changes for US 315820 for 1710 vnfc-type renamed to nfc-function,vnfc-function-code renamed to nfc-naming-code
		
		/*
		vnfcParams.put("vnfc-function-code", vnfcFuncCode);
		vnfcParams.put("vnfc-type", vnfcType);
		*/
		vnfcParams.put("nfc-naming-code", vnfcFuncCode);
		vnfcParams.put("nfc-function", vnfcType);
		
		//
		
		vnfcParams.put("ipaddress-v4-oam-vip", ipAddressV4OamVip);
		
		vnfcParams.put("prov-status", "NVTPROV");
		vnfcParams.put("orchestration-status", "CONFIGURED");
		vnfcParams.put("in-maint", "false");
		vnfcParams.put("is-closed-loop", "false");
		vnfcParams.put("group-notation",groupNotation);
		
		
		vnfcParams.put("relationship-list.relationship[0].related-to","vserver");
		vnfcParams.put("relationship-list.relationship[0].relationship-data[0].relationship-key","vserver.vserver-id");
		vnfcParams.put("relationship-list.relationship[0].relationship-data[0].relationship-value",ctx.getAttribute(aaiRefKey + "vserver-id"));
		
		
		vnfcParams.put("relationship-list.relationship[0].relationship-data[1].relationship-key","tenant.tenant-id");
		vnfcParams.put("relationship-list.relationship[0].relationship-data[1].relationship-value",ctx.getAttribute(aaiRefKey + "tenant-id"));
		
		
		vnfcParams.put("relationship-list.relationship[0].relationship-data[2].relationship-key","cloud-region.cloud-owner");
		vnfcParams.put("relationship-list.relationship[0].relationship-data[2].relationship-value",ctx.getAttribute(aaiRefKey + "cloud-owner"));
		
		
		vnfcParams.put("relationship-list.relationship[0].relationship-data[3].relationship-key","cloud-region.cloud-region-id");
		vnfcParams.put("relationship-list.relationship[0].relationship-data[3].relationship-value",ctx.getAttribute(aaiRefKey + "cloud-region-id"));
		
		
		 
		vnfcParams.put("relationship-list.relationship[1].related-to","generic-vnf");
		vnfcParams.put("relationship-list.relationship[1].relationship-data[0].relationship-key","generic-vnf.vnf-id");
		vnfcParams.put("relationship-list.relationship[1].relationship-data[0].relationship-value",ctx.getAttribute("vnf-id"));
		
		
		vnfcParams.put("relationship-list.relationship[2].related-to","vf-module");
		vnfcParams.put("relationship-list.relationship[2].relationship-data[0].relationship-key","generic-vnf.vnf-id");
		vnfcParams.put("relationship-list.relationship[2].relationship-data[0].relationship-value",ctx.getAttribute("vnf-id"));
		
		
		vnfcParams.put("relationship-list.relationship[2].relationship-data[1].relationship-key","vf-module.vf-module-id");
		vnfcParams.put("relationship-list.relationship[2].relationship-data[1].relationship-value",ctx.getAttribute(aaiRefKey + "vf-module-id"));
	
	
		return vnfcParams;
	}

	public void addVnfc(String vnfcName, Map<String, String> params, String prefix) throws Exception  {
		
		log.info("Received addVnfc call with vnfcName : " +vnfcName);
		log.info("Received addVnfc call with params : " + params);
		String resourceKey  =  "vnfc.vnfc-name = '" + vnfcName + "'";
		
		log.info("Received addVnfc call with resourceKey : " + resourceKey);
		
	
		 SvcLogicContext vnfcCtx = new SvcLogicContext();
		 SvcLogicResource.QueryStatus response = aaiClient.save("vnfc", true, false, resourceKey, params, prefix, vnfcCtx) ;
		 
		 if (SvcLogicResource.QueryStatus.SUCCESS.equals(response)) {
			 log.info("Added VNFC SUCCESSFULLY " + vnfcName);
			 
		 }
		 else if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
			 throw new Exception("VNFC Add failed for for vnfc_name " + vnfcName);
			 
		 }
				 
		
	}

	public String getGroupNotation(String groupNotationType, String groupNotationValue, String vnfcName,
			String vserverName, String prefix, SvcLogicContext ctx, String vnfcRefVnfcType) throws Exception  {
		
		String grpNotation = null;
		
		if ( "fixed-value".equals(groupNotationType)) {
			grpNotation = groupNotationValue;
			
		}
		else if ( "first-vnfc-name".equals(groupNotationType) ) {
			
			/*If the group-notation-type value = ?first-vnfc-name?, 
			 * then populate the group-notation value with the concatenation of 
			 * [vnfc name associated with the first vnfc for the vnfc-type (e.g., *******)] 
			 * and [the value in group-notation-value (e.g., pair)].   
			 *  There may be several vnfc-types associated with the VM?s.
			 */
			 /* Vnfc-type should be from refrence data */
			
			/* vDBE has 2 VNFCs with same VNFC type . The pair name should be same for both . */
			/* When first VNFC is added details should be added to context so FirstVnfcName doesnt return null second time. */
			 String tmpVnfcName = getFirstVnfcNameForVnfcType(ctx, prefix, vnfcRefVnfcType);
			 
			 log.info("RETURNED FIRSTVNFCNAME"  + tmpVnfcName);
			 log.info("CURRENTVNFCNAME"  + vnfcName);
			 if ( tmpVnfcName == null ) {
				 log.info("CURRENTVNFCNAME"  + vnfcName);
				 // No Vnfcs currently exist. Use Current vnfcName
				 grpNotation = vnfcName  + groupNotationValue;
			 }
			 else
				 grpNotation = tmpVnfcName + groupNotationValue;
			
			 
		}
		else if ( "relative-value".equals(groupNotationType) ) {
			
			/*If the group-notation-type = ?relative-value?, then find the group-notation value 
			 * from the prior vnfc (where prior means the vnfc with where the last three digits of the 
			 * vm-name is one lower than the current one; note that this vnfc may have been previously configured.)
					1.	If the group-notation-value = next, then add 1 to the group-notation value from the prior vnfc and use this value
					2.	If the group-notation-value = same, then use the group-notation-value from the prior vnfc record*/

			// next and same cant be defined for first VM.  if next will not generate grpNotation if Prior is not a number
			String tmpVserverName = null;
			if ( vserverName != null ) {
				
				String vmNamePrefix =  vserverName.substring(0,vserverName.length()-3);
				
				String lastThreeChars = vserverName.substring(vserverName.length() - 3); 
				
				if ( NumberUtils.isDigits(lastThreeChars)) {
					int vmNum = Integer.parseInt(lastThreeChars) - 1;
					String formatted = String.format("%03d", vmNum);
				
					log.info("FORMATTED " + formatted);
				
					tmpVserverName = vmNamePrefix + formatted;
				
				
					String priorGroupNotation = getGroupNotationForVServer(ctx, prefix, tmpVserverName);
				
					if ( "same".equals(groupNotationValue))
						grpNotation = priorGroupNotation;
					else if ( "next".equals(groupNotationValue)) {
						if ( priorGroupNotation != null && NumberUtils.isDigits(priorGroupNotation)) {
							int nextGrpNotation = Integer.parseInt(priorGroupNotation) + 1;
							grpNotation = String.valueOf(nextGrpNotation);
						}
					}
				}
				
			}
			
			
			
		}
		
		
		log.info("RETURNED GROUPNOTATION " + grpNotation);
		return grpNotation;
	}

	public String getGroupNotationForVServer(SvcLogicContext ctx, String prefix, String vserverName) throws Exception {
		
		
		String vmCountStr = ctx.getAttribute(prefix+"vnf.vm-count");
		
		if ( vmCountStr == null )
			return null;
		
		int vmCount = Integer.valueOf(vmCountStr);
		for ( int i = 0; i < vmCount ; i++ ) {
		
			String tmpVserver = ctx.getAttribute(prefix+ "vm[" + i + "].vserver-name");
			
			if (vserverName.equals(tmpVserver))
				return ctx.getAttribute(prefix+ "vm[" + i + "].group-notation");
		
		} // vmCount
		
		return null;
		
	}

	
	

	public String getFirstVnfcNameForVnfcType(SvcLogicContext ctx, String prefix, String vnfcRefVnfcType) throws Exception {
		
		
		
		/*if(StringUtils.isBlank(vnfcRefVnfcType)){
			throw new Exception("Vnfc Reference : VNFC Type is missing");
		}*/
		
		
		String vmCountStr = ctx.getAttribute(prefix+"vnf.vm-count");
		
		if ( vmCountStr == null )
			return null;
		
		int vmCount = Integer.valueOf(vmCountStr);
		for ( int i = 0; i < vmCount ; i++ ) {
		
			String tmpvnfcType = ctx.getAttribute(prefix+ "vm[" + i + "].vnfc-type");
			
			if (vnfcRefVnfcType.equals(tmpvnfcType))
				return ctx.getAttribute(prefix+ "vm[" + i + "].vnfc-name");
		
		} // vmCount
		
		
		
		return null;
		
	}

	public void updateVServerStatus(Map<String, String> params,SvcLogicContext ctx, int vmCount) 	throws Exception {
		log.info("Received updateVServerStatus call with params : " + params);
	
		String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
	 	
	 	prefix = StringUtils.isNotBlank(prefix) ? (prefix+".") : "";
	 	
	 		
		Map<String, String> vServerParams = new HashMap<String, String>();
		 
		
		// TODO - Should this just update prov-status or both? What about generic-vnf status? Will that be updated by Dispatcher?
		
		vServerParams.put("prov-status", "NVTPROV");
		//vServerParams.put("orchestration-status", "CONFIGURED");
		
		
		for ( int i = 0; i < vmCount ; i++ ) {
			String aaiRefKey = prefix + "vm[" + i + "].";
			
			log.info("VNFCNAME IN UpdateVServer "  + ctx.getAttribute(aaiRefKey + "vnfc-name"));
			
			if ( ctx.getAttribute(aaiRefKey + "vnfc-name") != null ) 
				continue;
			
			
						
			String resourceKey  = "vserver.vserver-id = '" +  ctx.getAttribute(aaiRefKey + "vserver-id") + "'" +
					" AND tenant.tenant-id = '"  + ctx.getAttribute(aaiRefKey + "tenant-id") + "'" + 
					" AND cloud-region.cloud-owner = '" + ctx.getAttribute(aaiRefKey + "cloud-owner") + "'" + 
					" AND cloud-region.cloud-region-id = '" +  ctx.getAttribute(aaiRefKey + "cloud-region-id") + "'";
			
			
			updateResource( "vserver", resourceKey,   vServerParams);
			
		}
	 	

	}
	
	
	
	public void updateVnfStatus(Map<String, String> params,SvcLogicContext ctx) 	throws Exception {
		log.info("Received updateVnfStatus call with params : " + params);
	
		String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
	 	
	 	prefix = StringUtils.isNotBlank(prefix) ? (prefix+".") : "";
	 	
	 		
		Map<String, String> vnfParams = new HashMap<String, String>();
		 
		
		// TODO - Should this just update prov-status or both? What about generic-vnf status? Will that be updated by Dispatcher?
		
		vnfParams.put("prov-status", "NVTPROV");
		//vnfParams.put("orchestration-status", "CONFIGURED");
		
								
		String resourceKey  = "generic-vnf.vnf-id = '" + ctx.getAttribute("vnf-id") + "'";
					
		updateResource( "generic-vnf" , resourceKey,  vnfParams);
			
		
	 	

	}
	
	public void updateResource( String resource, String resourceKey,  Map<String, String> params)  throws Exception {
		
		log.info("Received updateResource call with Key : " +resourceKey);
	
	
		SvcLogicContext ctx = new SvcLogicContext();
	
		
		SvcLogicResource.QueryStatus response =  aaiClient.update(resource, resourceKey , params, "tmp.update", ctx);
		
		
		 
		if (SvcLogicResource.QueryStatus.SUCCESS.equals(response)) {
			log.info("Updated " + resource + " SUCCESSFULLY for " + resourceKey);
			 
		}
		else if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
			throw new Exception(resource + " Update failed for " + resourceKey);
			 
		}
	}

	public SvcLogicContext readResource(String query, String prefix, String resourceType) throws Exception {
        SvcLogicContext resourceContext = new SvcLogicContext();
       
        SvcLogicResource.QueryStatus response = aaiClient.query(resourceType,false,null,query,prefix,null,resourceContext);
        log.info("AAIResponse: " + response.toString());
        if(!SvcLogicResource.QueryStatus.SUCCESS.equals(response)){
                throw new Exception("Error Retrieving " + resourceType + " from A&AI");
        }
       
        return resourceContext;
        
        
        
    }
	
	//Added  1710 & Backward Compatibility

		public void checkAndUpdateVnfc(Map<String, String> params,SvcLogicContext ctx, int vnfcRefLen, int vmCount) 	throws Exception {
			log.info("Received checkAndUpdateVnfcStatus call with params : " + params);

			String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

			prefix = StringUtils.isNotBlank(prefix) ? (prefix+".") : "";	

			for ( int i = 0; i < vmCount ; i++ ) {
				String aaiRefKey = prefix + "vm[" + i + "].";
				
				log.info("VNFCNAME IN INSERTVNFCS "+ aaiRefKey+"vnfc-name:" + ctx.getAttribute(aaiRefKey + "vnfc-name"));
				
				String numVnfcsStr = ctx.getAttribute(aaiRefKey + "vnfc-count");
				String vnfcNameAai = ctx.getAttribute(aaiRefKey + "vnfc-name");

				if (StringUtils.isNotBlank(vnfcNameAai)) {
					// Get Vnfc_reference data
					for(int vnfcRefIndx=0;vnfcRefIndx < vnfcRefLen;vnfcRefIndx++ ) {					

						String vnfcRefKey = "vnfcReference[" + vnfcRefIndx + "].";

						log.info("VNFCREFKEY " + vnfcRefKey);
						log.info("AAIREFKEY " + aaiRefKey);

						String  vnfcFuncCode = ctx.getAttribute(vnfcRefKey+ "VNFC-FUNCTION-CODE");
						String vserverName = ctx.getAttribute(aaiRefKey + "vserver-name");
						String vnfcNameReference = vserverName + vnfcFuncCode + "001";

						if(vnfcNameAai.equals(vnfcNameReference)) {

							updateVnfcStatus( vnfcNameAai,  params, prefix);
						}
						

					}
				}






			}


		}

		public void updateVnfcStatus(String vnfcName, Map<String, String> params, String prefix) throws Exception  {

			log.info("Received updateVnfcStatus call with vnfcName : " +vnfcName);
			log.info("Received updateVnfcStatus call with params : " + params);

			String resourceKey  =  "vnfc.vnfc-name = '" + vnfcName + "'";		
			log.info("Received updateVnfcStatus call with resourceKey : " + resourceKey);


			Map<String, String> vnfcParams = new HashMap<String, String>(); 		
			vnfcParams.put("prov-status", "NVTPROV");
			vnfcParams.put("orchestration-status", "CONFIGURED");	

			log.info("In updateVnfcStatus call with vnfcParams : " + vnfcParams);	

			updateResource( "vnfc" , resourceKey,  vnfcParams);	

			log.info("End of updateVnfcStatus");


		}



		//Added  for 1710	
		
	
}
