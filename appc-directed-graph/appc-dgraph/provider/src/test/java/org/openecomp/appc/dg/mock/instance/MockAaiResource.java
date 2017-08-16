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


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openecomp.sdnc.sli.SvcLogicAdaptor;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.SvcLogicResource.QueryStatus;
import static org.junit.Assert.*;

public class MockAaiResource implements SvcLogicResource {

	private final static Logger logger = LoggerFactory.getLogger(MockAaiResource.class);
	
	@Override
	public QueryStatus isAvailable(String resource, String key, String prefix, SvcLogicContext ctx) throws SvcLogicException {
		
		return QueryStatus.SUCCESS;
		
	}
	
	@Override
	public QueryStatus exists(String resource, String key, String prefix, SvcLogicContext ctx)  throws SvcLogicException {
		
		return QueryStatus.SUCCESS;
	}
	
	
	
	public QueryStatus query(String resource, boolean localOnly, String select, String key,  String prefix, String orderBy, SvcLogicContext ctx) throws SvcLogicException {
		
		if (ctx.getAttribute("j").equals("0") && "tmp.aai-data-vm".equals(prefix)) {
			logger.info("Mock VM Get query1 called " + ctx.getAttribute("j") );
			
			assertEquals(resource, "vserver");
			assertEquals(key, "'vserver-name = $request-parameters.vm[$j].vm-name'");
			
			ctx.setAttribute("tmp.aai-data-vm.vserver-id", "vserverid1");
			
			ctx.setAttribute("tmp.aai-data-vm.tenant-id", "tenantid1");
			ctx.setAttribute("tmp.aai-data-vm.cloud-owner", "att-aic");
			ctx.setAttribute("tmp.aai-data-vm.cloud-region-id", "cloudregionid1");
		}
		else if (ctx.getAttribute("j").equals("1") && "tmp.aai-data-vm".equals(prefix)) {
			logger.info("Mock VM Get query1 called " + ctx.getAttribute("j") );
			
			assertEquals(resource, "vserver");
			assertEquals(key, "'vserver-name = $request-parameters.vm[$j].vm-name'");
			
			
			ctx.setAttribute("tmp.aai-data-vm.vserver-id", "vserverid2");
			
			ctx.setAttribute("tmp.aai-data-vm.tenant-id", "tenantid2");
			ctx.setAttribute("tmp.aai-data-vm.cloud-owner", "att-aic");
			ctx.setAttribute("tmp.aai-data-vm.cloud-region-id", "cloudregionid2");
		}
		else if ("tmp.aai-data-vnfc".equals(prefix)) {
			logger.info("Mock VNFC Get query1 called "  );
			
			assertEquals(resource, "vnfc");
			assertEquals(key, "'vnfc-name = $request-parameters.vm[$j].vnfc[$k].vnfc-name'");
			
			return QueryStatus.NOT_FOUND;
		}
		return QueryStatus.SUCCESS;
	}
	
	
	public QueryStatus query(String resource, boolean localOnly, String select, String key, String prefix, SvcLogicContext ctx)  throws SvcLogicException {
		
		logger.info("Mock query2 called " + ctx.getAttribute("j"));
		
				
		return QueryStatus.SUCCESS;
	}
	
	@Override
	public QueryStatus reserve(String resource, String select, String key, String prefix, SvcLogicContext ctx) throws SvcLogicException {
		
		return QueryStatus.SUCCESS;
	}
	
	@Override
	public QueryStatus save(String resource, boolean force, boolean localOnly, String key, Map<String, String> parms, String prefix, SvcLogicContext ctx) throws SvcLogicException {
		
		logger.info("Mock Save called");
		if (ctx.getAttribute("j").equals("0") && 
				ctx.getAttribute("k").equals("0") &&
				"tmp.vnfc-sys-controller".equals(prefix)) {
			
			assertEquals(resource, "vnfc");
			
			assertEquals(key, "'vnfc-name = $request-parameters.vm[$j].vnfc[$k].vnfc-name'");
			
			
			assertEquals(parms.get("prov-status"), "NVTPROV");
			assertEquals(parms.get("orchestration-status"), "CONFIGURED");
			assertEquals(parms.get("in-maint"), "false");
			assertEquals(parms.get("is-closed-loop"), "false");
			
			
			assertEquals(parms.get("vnfc-function-code"), "funccode");
			assertEquals(parms.get("vnfc-type"), "vnfctype");
			assertEquals(parms.get("ipaddress-v4-oam-vip"), "135.1.1.1");
			assertEquals(parms.get("group-notation"), "groupnotation");
			
			assertEquals(parms.get("relationship-list.relationship[0].related-to"), "vserver");
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[0].relationship-key"), "vserver.vserver-id");
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[0].relationship-value"), "vserverid1");
			
			
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[1].relationship-key"), "tenant.tenant-id");
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[1].relationship-value"), "tenantid1");
			
			
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[2].relationship-key"), "cloud-region.cloud-owner");
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[2].relationship-value"), "att-aic");
			
			
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[3].relationship-key"), "cloud-region.cloud-region-id");
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[3].relationship-value"), "cloudregionid1");
			
			
			assertEquals(parms.get("relationship-list.relationship[1].related-to"), "generic-vnf");
			assertEquals(parms.get("relationship-list.relationship[1].relationship-data[0].relationship-key"), "generic-vnf.vnf-id");
			assertEquals(parms.get("relationship-list.relationship[1].relationship-data[0].relationship-value"), "ibcx0001v");
			
			
			assertEquals(parms.get("relationship-list.relationship[2].related-to"), "vf-module");
			assertEquals(parms.get("relationship-list.relationship[2].relationship-data[0].relationship-key"), "generic-vnf.vnf-id");
			assertEquals(parms.get("relationship-list.relationship[2].relationship-data[0].relationship-value"), "ibcx0001v");
			
			assertEquals(parms.get("relationship-list.relationship[2].relationship-data[1].relationship-key"), "vf-module.vf-module-id");
			assertEquals(parms.get("relationship-list.relationship[2].relationship-data[1].relationship-value"), "1");
			
		}
		else if (ctx.getAttribute("j").equals("1") && 
				ctx.getAttribute("k").equals("0") &&
				"tmp.vnfc-sys-controller".equals(prefix)) {
			
			assertEquals(resource, "vnfc");
			
			assertEquals(key, "'vnfc-name = $request-parameters.vm[$j].vnfc[$k].vnfc-name'");
			
			
			assertEquals(parms.get("prov-status"), "NVTPROV");
			assertEquals(parms.get("orchestration-status"), "CONFIGURED");
			assertEquals(parms.get("in-maint"), "false");
			assertEquals(parms.get("is-closed-loop"), "false");
			
			
			assertEquals(parms.get("vnfc-function-code"), "funccode1");
			assertEquals(parms.get("vnfc-type"), "vnfctype1");
			assertEquals(parms.get("ipaddress-v4-oam-vip"), "135.2.2.2");
			assertEquals(parms.get("group-notation"), "groupnotation1");
			
			assertEquals(parms.get("relationship-list.relationship[0].related-to"), "vserver");
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[0].relationship-key"), "vserver.vserver-id");
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[0].relationship-value"), "vserverid2");
			
			
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[1].relationship-key"), "tenant.tenant-id");
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[1].relationship-value"), "tenantid2");
			
			
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[2].relationship-key"), "cloud-region.cloud-owner");
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[2].relationship-value"), "att-aic");
			
			
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[3].relationship-key"), "cloud-region.cloud-region-id");
			assertEquals(parms.get("relationship-list.relationship[0].relationship-data[3].relationship-value"), "cloudregionid2");
			
			
			assertEquals(parms.get("relationship-list.relationship[1].related-to"), "generic-vnf");
			assertEquals(parms.get("relationship-list.relationship[1].relationship-data[0].relationship-key"), "generic-vnf.vnf-id");
			assertEquals(parms.get("relationship-list.relationship[1].relationship-data[0].relationship-value"), "ibcx0001v");
			
			
			assertEquals(parms.get("relationship-list.relationship[2].related-to"), "vf-module");
			assertEquals(parms.get("relationship-list.relationship[2].relationship-data[0].relationship-key"), "generic-vnf.vnf-id");
			assertEquals(parms.get("relationship-list.relationship[2].relationship-data[0].relationship-value"), "ibcx0001v");
			
			assertEquals(parms.get("relationship-list.relationship[2].relationship-data[1].relationship-key"), "vf-module.vf-module-id");
			assertEquals(parms.get("relationship-list.relationship[2].relationship-data[1].relationship-value"), "1");
		}
		return QueryStatus.SUCCESS;
	}
	
	@Override
	public QueryStatus release(String resource, String key, SvcLogicContext ctx)  throws SvcLogicException {
		
		return QueryStatus.SUCCESS;
	}
	
	@Override
	public QueryStatus delete(String resource, String key, SvcLogicContext ctx) throws SvcLogicException {
		
		return QueryStatus.SUCCESS;
	}
	
	
	
	@Override
	public QueryStatus notify(String resource, 	String action,	String key, SvcLogicContext ctx) throws SvcLogicException {
	


		return QueryStatus.SUCCESS;
	}

	
	public QueryStatus update(String resource, String key,
			Map<String, String> parms, String prefix, SvcLogicContext ctx)
			throws SvcLogicException {
		
		return QueryStatus.SUCCESS;
	}

}





