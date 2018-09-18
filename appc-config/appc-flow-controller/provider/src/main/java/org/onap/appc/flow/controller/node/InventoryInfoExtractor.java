/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
package org.onap.appc.flow.controller.node;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.flow.controller.interfaceData.InventoryInfo;
import org.onap.appc.flow.controller.interfaceData.Vm;
import org.onap.appc.flow.controller.interfaceData.VnfInfo;
import org.onap.appc.flow.controller.interfaceData.Vnfcslist;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

/**
 * Helper class for FlowControlNode
 */
class InventoryInfoExtractor {

  private static final EELFLogger log = EELFManager.getInstance().getLogger(InventoryInfoExtractor.class);

  InventoryInfo getInventoryInfo(SvcLogicContext ctx, String vnfId)  {
    String fn = "InventoryInfoExtractor.getInventoryInfo";

    VnfInfo vnfInfo = new VnfInfo();
    vnfInfo.setVnfId(vnfId);
    vnfInfo.setVnfName(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-name"));
    vnfInfo.setVnfType(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type"));
    vnfInfo.setIdentityUrl(getIdentityUrl(ctx,vnfInfo,vnfId));

    String vmcount = ctx.getAttribute("tmp.vnfInfo.vm-count");
    log.info(fn + "vmcount:" + vmcount);

    int vmCount = (StringUtils.isNotBlank(vmcount)) ? Integer.parseInt(vmcount) : 0;

    for (int i = 0; i < vmCount; i++) {
      processVm(ctx, vnfInfo, i);
    }

    InventoryInfo inventoryInfo = new InventoryInfo();
    inventoryInfo.setVnfInfo(vnfInfo);
    log.info(fn + "Inventory Output:" + inventoryInfo.toString());

    return inventoryInfo;
  }

  private void processVm(SvcLogicContext ctx, VnfInfo vnfInfo, int index) {
    Vm vm = new Vm();
    vm.setVserverId(ctx.getAttribute("tmp.vnfInfo.vm[" + index + "].vserver-id"));
    vm.setVmId(ctx.getAttribute("tmp.vnfInfo.vm[" + index + "].vserver-selflink"));
    int vnfcCount = Integer.parseInt(ctx.getAttribute("tmp.vnfInfo.vm[" + index + "].vnfc-count"));
    if (vnfcCount > 0) {
      Vnfcslist vnfc = new Vnfcslist();
      vnfc.setVnfcName(ctx.getAttribute("tmp.vnfInfo.vm[" + index + "].vnfc-name"));
      vnfc.setVnfcType(ctx.getAttribute("tmp.vnfInfo.vm[" + index + "].vnfc-type"));
      vm.setVnfc(vnfc);
    }
    vnfInfo.getVm().add(vm);
  }


  public String getIdentityUrl(SvcLogicContext ctx, VnfInfo vnfInfo, String vnfId) {
      String identityUrl = "";
      for (String key : ctx.getAttributeKeySet()) {
            log.debug("InventoryData " + key + "=" + ctx.getAttribute(key));
      }
      String urlFromPayload= ctx.getAttribute("identity-url");
      log.info("Url from payload:" + urlFromPayload);
      String urlFromAAI=ctx.getAttribute("tmp.vnfInfo.identity-url");

      if(StringUtils.isNotBlank(urlFromPayload)){
          identityUrl=urlFromPayload;
      }else if(StringUtils.isNotBlank(urlFromAAI)){
          identityUrl=urlFromAAI;
      }
    return identityUrl;


  }

}
