/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.design.validator;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.design.data.ArtifactInfo;
import org.onap.appc.design.data.DesignInfo;
import org.onap.appc.design.data.DesignRequest;
import org.onap.appc.design.data.DesignResponse;
import org.onap.appc.design.data.StatusInfo;
import org.onap.appc.design.data.UserPermissionInfo;

public class TestDesigndata {

    DesignResponse dr = new DesignResponse();

    @Test
    public void testSetUserID() {
        String userId = "0000";
        dr.setUserId(userId);
        Assert.assertEquals("Unexpected getUserId value", userId, dr.getUserId());
    }

    @Test
    public void testSetDesignInfoList() {
        DesignInfo di = new DesignInfo();
        List<DesignInfo> li = new ArrayList<DesignInfo>();
        String action = "TestAction";
        di.setAction(action);
        Assert.assertEquals("Unexpected getAction value", action, di.getAction());
        String artifact_name = "TestName";
        di.setArtifact_name(artifact_name);
        Assert.assertEquals("Unexpected getArtifact_name value", artifact_name, di.getArtifact_name());
        String artifact_type = "TestType";
        di.setArtifact_type(artifact_type);
        Assert.assertEquals("Unexpected getArtifact_type value", artifact_type, di.getArtifact_type());
        String inCart = "TestCart";
        di.setInCart(inCart);
        Assert.assertEquals("Unexpected getInCart value", inCart, di.getInCart());
        String protocol = "TestProtocol";
        di.setProtocol(protocol);
        Assert.assertEquals("Unexpected getProtocol value", protocol, di.getProtocol());
        String vnf_type = "TestVNF";
        di.setVnf_type(vnf_type);
        Assert.assertEquals("Unexpected getVnf_type value", vnf_type, di.getVnf_type());
        String vnfc_type = "TestVNFC";
        di.setVnfc_type(vnfc_type);
        Assert.assertEquals("Unexpected getVnfc_type value", vnfc_type, di.getVnfc_type());
        li.add(di);
        dr.setDesignInfoList(li);
        Assert.assertEquals("Unexpected getDesignInfoList value", li, dr.getDesignInfoList());
    }

    @Test
    public void testSetArtifactInfo() {
        ArtifactInfo ai = new ArtifactInfo();
        List<ArtifactInfo> li = new ArrayList<ArtifactInfo>();
        String artifact_content = "TestContent";
        ai.setArtifact_content(artifact_content);
        Assert.assertEquals("Unexpected getArtifact_content value", artifact_content, ai.getArtifact_content());
        li.add(ai);
        dr.setArtifactInfo(li);
        Assert.assertEquals("Unexpected getArtifactInfo value", li, dr.getArtifactInfo());
    }

    @Test
    public void testStatusInfo() {
        StatusInfo si = new StatusInfo();
        List<StatusInfo> li = new ArrayList<StatusInfo>();
        String action = "TestAction";
        si.setAction(action);
        Assert.assertEquals("Unexpected getAction value", action, si.getAction());
        String action_status = "TestActionStatus";
        si.setAction_status(action_status);
        Assert.assertEquals("Unexpected getAction_status value", action_status, si.getAction_status());
        String artifact_status = "TestArtifactStatus";
        si.setArtifact_status(artifact_status);
        Assert.assertEquals("Unexpected getArtifact_status value", artifact_status, si.getArtifact_status());
        String vnf_type = "TestVNF";
        si.setVnf_type(vnf_type);
        Assert.assertEquals("Unexpected getVnf_type value", vnf_type, si.getVnf_type());
        String vnfc_type = "TestVNFC";
        si.setVnfc_type(vnfc_type);
        Assert.assertEquals("Unexpected getVnfc_type value", vnfc_type, si.getVnfc_type());
        li.add(si);
        dr.setStatusInfoList(li);
        Assert.assertEquals("Unexpected getStatusInfoList value", li, dr.getStatusInfoList());
    }

    @Test
    public void testDesignRequest() {
        DesignRequest dreq = new DesignRequest();
        String action = "TestAction";
        dreq.setAction(action);
        Assert.assertEquals("Unexpected getAction value", action, dreq.getAction());
        String artifact_contents = "TestContent";
        dreq.setArtifact_contents(artifact_contents);
        Assert.assertEquals("Unexpected getArtifact_contents value", artifact_contents, dreq.getArtifact_contents());
        String artifact_name = "TestName";
        dreq.setArtifact_name(artifact_name);
        Assert.assertEquals("Unexpected getArtifact_name value", artifact_name, dreq.getArtifact_name());
        String protocol = "TestProtocol";
        dreq.setProtocol(protocol);
        Assert.assertEquals("Unexpected getProtocol value", protocol, dreq.getProtocol());
        String userId = "0000";
        dreq.setUserId(userId);
        Assert.assertEquals("Unexpected getUserId value", userId, dreq.getUserId());
        String vnf_type = "testvnf";
        dreq.setVnf_type(vnf_type);
        Assert.assertEquals("Unexpected getVnf_type value", vnf_type, dreq.getVnf_type());
        String vnfc_type = "testvnfc";
        dreq.setVnfc_type(vnfc_type);
        Assert.assertEquals("Unexpected getVnfc_type value", vnfc_type, dreq.getVnfc_type());
        String expecting =
                "DesignRequest [userId=" + userId + ", vnf_type=" + vnf_type + ", vnfc_type=" + vnfc_type + ", protocol="
                + protocol + ", action=" + action + ", artifact_name=" + artifact_name + ", artifact_contents="
                + artifact_contents + "]";
        Assert.assertEquals("Unexpected toString value", expecting, dreq.toString());
    }

    @Test
    public void testUserPermissionInfo() {
        UserPermissionInfo upi = new UserPermissionInfo();
        List<UserPermissionInfo> li = new ArrayList<UserPermissionInfo>();
        String userId = "uu1234";
        upi.setUserID(userId);
        Assert.assertEquals("Unexpected getUserId value", userId, upi.getUserID());
        String permission = "owner";
        upi.setPermission(permission);
        Assert.assertEquals("Unexpected getPermission value", permission, upi.getPermission());
        li.add(upi);
        dr.setUsers(li);
        Assert.assertEquals("Unexpected getUsers value", li, dr.getUsers());
    }

}
