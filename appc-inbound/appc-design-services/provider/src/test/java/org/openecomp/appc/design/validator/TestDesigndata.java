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

package org.openecomp.appc.design.validator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev170627.DbserviceInput;
import org.openecomp.appc.design.data.ArtifactInfo;
import org.openecomp.appc.design.data.DesignInfo;
import org.openecomp.appc.design.data.DesignRequest;
import org.openecomp.appc.design.data.DesignResponse;
import org.openecomp.appc.design.data.StatusInfo;
import org.openecomp.appc.design.dbervices.DbResponseProcessor;
import org.openecomp.appc.design.dbervices.DbService;
import org.openecomp.appc.design.services.impl.DesignServicesImpl;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;

public class TestDesigndata {
	
	
		
	DesignResponse dr = new DesignResponse();
	
	
	
	@Test
	public void testSetUserID(){
		
		 dr.setUserId("00000");
		 
		 String str = dr.getUserId();
		 System.out.println(str);
	}
	
	@Test
	public void testSetDesignInfoList(){
		DesignInfo di = new DesignInfo();
		List<DesignInfo> li = new ArrayList<DesignInfo>();
		di.setAction("TestAction");
		di.setArtifact_name("TestName");
		di.setArtifact_type("TestType");
		di.setInCart("TestCart");
		di.setProtocol("TestProtocol");
		di.setVnf_type("TestVNF");
		di.setVnfc_type("TestVNFC");
		li.add(di);
		dr.setDesignInfoList(li);
		System.out.println(dr.getDesignInfoList());
	}
	
	@Test
	public void testSetArtifactInfo(){
		ArtifactInfo ai = new ArtifactInfo();
		List<ArtifactInfo> li = new ArrayList<ArtifactInfo>();
		ai.setArtifact_content("TestContent");
		li.add(ai);
		dr.setArtifactInfo(li);
		System.out.println(dr.getArtifactInfo());
		
	}
	@Test
	public void testStatusInfo(){
		StatusInfo si = new StatusInfo();
		List<StatusInfo> li = new ArrayList<StatusInfo>();
		si.setAction("TestAction");
		si.setAction_status("TestActionStatus");
		si.setArtifact_status("TestArtifactStatus");
		si.setVnf_type("TestVNF");
		si.setVnfc_type("TestVNFC");
		li.add(si);
		dr.setStatusInfoList(li);
		System.out.println(dr.getStatusInfoList());
	}
	@Test
	public void testDesignRequest(){
		DesignRequest dreq = new DesignRequest();
		dreq.setAction("TestAction");
		dreq.setArtifact_contents("TestContent");
		dreq.setArtifact_name("TestName");
		dreq.setProtocol("TestProtocol");
		dreq.setUserId("0000");
		dreq.setVnf_type("testvnf");
		dreq.setVnfc_type("testvnfc");
		System.out.println(dreq.getAction() + dreq.getArtifact_contents() + dreq.getArtifact_name() + dreq.getProtocol() + dreq.getUserId() + dreq.getVnf_type()+ dreq.getVnfc_type());
	System.out.println(dreq.toString());
	}
	

}
