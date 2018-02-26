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

package org.onap.appc.aai.interfaceImpl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.onap.appc.aai.data.AaiVmInfo;
import org.onap.appc.aai.data.AaiVnfInfo;
import org.onap.appc.aai.data.AaiVnfcInfo;
import org.onap.appc.aai.utils.AaiClientConstant;
import org.onap.appc.instar.interfaces.RuleHandlerInterface;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.sdnc.config.params.data.Parameter;
import org.onap.sdnc.config.params.data.ResponseKey;

public class AaiInterfaceRulesHandler implements RuleHandlerInterface {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(AaiInterfaceRulesHandler.class);
    private Parameter parameters;
    private SvcLogicContext context;
    private AaiVnfInfo vnfInfoData;

    public AaiInterfaceRulesHandler(Parameter params, SvcLogicContext ctx) {
        this.parameters = params;
        this.context = ctx;
        this.setVnfInfoData(generateAaiVnfInfoData());
    }

    @Override
    public void processRule() throws IllegalStateException {

        String fn = "AaiInterfaceIpAddressHandler.processRule";
        log.info(fn + "Processing rule :" + parameters.getRuleType());
        List<ResponseKey> responseKeyList = parameters.getResponseKeys();
        ResponseKey respKeys = new ResponseKey();

        if (responseKeyList == null || responseKeyList.isEmpty()) {
            throw new IllegalStateException("NO response Keys set  for : " + parameters.getRuleType());
        }

        for (ResponseKey filterKeys : responseKeyList) {

            if (null == filterKeys) {
                continue;
            }

            if (StringUtils.isNotBlank(filterKeys.getUniqueKeyName())) {
                respKeys.setUniqueKeyName(filterKeys.getUniqueKeyName());
            }
            if (StringUtils.isNotBlank(filterKeys.getUniqueKeyValue())) {
                respKeys.setUniqueKeyValue(filterKeys.getUniqueKeyValue());
            }
            if (StringUtils.isNotBlank(filterKeys.getFieldKeyName())) {
                respKeys.setFieldKeyName(filterKeys.getFieldKeyName());
            }
            if (StringUtils.isNotBlank(filterKeys.getFilterByField())) {
                respKeys.setFilterByField(filterKeys.getFilterByField());
            }
            if (StringUtils.isNotBlank(filterKeys.getFilterByValue())) {
                respKeys.setFilterByValue(filterKeys.getFilterByValue());
            }
        }

        processKeys(respKeys, parameters.getName());
    }

    public void processKeys(ResponseKey filterKey, String aaiKey) {

        String fn = "AaiInterfaceRulesHandler.processKeys()::";
        log.info(fn + "processing for " + aaiKey);
        String values = new String();
        JSONObject aaiKeyValues = null;
        log.info("Aai Data in Context : " + context.getAttribute(AaiClientConstant.AAI_KEY_VALUES));
        if (context.getAttribute(AaiClientConstant.AAI_KEY_VALUES) != null) {
            aaiKeyValues = new JSONObject(context.getAttribute(AaiClientConstant.AAI_KEY_VALUES));
            log.info("Aai data already exsits :  " + aaiKeyValues.toString());
        } else {
            aaiKeyValues = new JSONObject();
        }

        if (StringUtils.equalsIgnoreCase(filterKey.getUniqueKeyValue(), "vnf")) {
            values = getVnfDetailsFromContext(filterKey.getFieldKeyName());

        }
        if (StringUtils.equalsIgnoreCase(filterKey.getUniqueKeyValue(), "vnfc")) {
            values = getVnfcDetailsFromContext(filterKey.getFieldKeyName(), filterKey.getFilterByField(),
                filterKey.getFilterByValue());
        }
        if (StringUtils.equalsIgnoreCase(filterKey.getUniqueKeyValue(), "vserver")) {
            values = getVServerDetailsFromContext(filterKey.getFieldKeyName(), filterKey.getFilterByField(),
                filterKey.getFilterByValue());
        }
        aaiKeyValues.put(aaiKey, values);
        context.setAttribute(AaiClientConstant.AAI_KEY_VALUES, aaiKeyValues.toString());
        return;
    }

    private String getVServerDetailsFromContext(String fieldKeyName, String filterByField, String filterByValue) {
        String fn = "AaiInterfaceRulesHander::getVServerDetailsFromContext():";
        String values = "";
        log.info(fn + "FieldKeyName:" + fieldKeyName + " FilterByName:" + filterByField + " FilterByValue:"
            + filterByValue);

        if (!StringUtils.equalsIgnoreCase(fieldKeyName, "vserver-name")) {
            log.info(fn + "Returning values:" + values);
            return values;
        }

        if (StringUtils.isNotEmpty(filterByField)
            && StringUtils.isNotEmpty(filterByValue)) {
            int vmIndex = -1;
            for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {
                vmIndex++;

                if (!StringUtils.equalsIgnoreCase(filterByField, "vm-number")) {
                    continue;
                }

                int vmNumber = Integer.parseInt(filterByValue);
                if (vmNumber != vmIndex) {
                    continue;
                }

                if (StringUtils.isBlank(values)) {
                    values = vm.getVserverName();
                } else {
                    values = values + "," + vm.getVserverName();
                }
            }
        } else {
            for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {
                if (StringUtils.isBlank(values)) {
                    values = vm.getVserverName();
                } else {
                    values = values + "," + vm.getVserverName();
                }
            }
        }

        log.info(fn + "Returning values:" + values);
        return values;
    }

    //split from getVnfcDetailsFromContext
    private String add2ValuesIpaddressV4OamVipNotEmpty(String values, String filterByField, String filterByValue) {
        for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {
            for (AaiVnfcInfo vnfcInfo : vm.getVnfcInfo()) {
                if (!StringUtils.equalsIgnoreCase(filterByField, "vnfc-function-code")
                    || !StringUtils.equalsIgnoreCase(filterByValue, vnfcInfo.getVnfcFunctionCode())) {
                    continue;
                }

                if (StringUtils.isBlank(values)) {
                    values = vnfcInfo.getVnfcOamIpAddress();
                } else {
                    values = values + "," + vnfcInfo.getVnfcOamIpAddress();
                }
            }

        }
        return values;
    }

    //split from getVnfcDetailsFromContext
    private String add2ValuesIpaddressV4OamVipEmpty(String values, String filterByField, String filterByValue) {
        for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {
            for (AaiVnfcInfo vnfcInfo : vm.getVnfcInfo()) {
                if (StringUtils.isBlank(values)) {
                    values = vnfcInfo.getVnfcOamIpAddress();
                } else {
                    values = values + "," + vnfcInfo.getVnfcOamIpAddress();
                }
            }
        }
        return values;
    }

    //split from getVnfcDetailsFromContext
    private String add2ValuesVnfcNameNotEmpty(String values, String filterByField, String filterByValue) {
        for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {
            for (AaiVnfcInfo vnfcInfo : vm.getVnfcInfo()) {
                if (!StringUtils.equalsIgnoreCase(filterByField, "vnfc-function-code")
                    || !StringUtils.equalsIgnoreCase(filterByValue, vnfcInfo.getVnfcFunctionCode())) {
                    continue;
                }

                if (StringUtils.isBlank(values)) {
                    values = vnfcInfo.getVnfcName();
                } else {
                    values = values + "," + vnfcInfo.getVnfcName();
                }
            }

        }
        return values;
    }

    //split from getVnfcDetailsFromContext
    private String add2ValuesVnfcNameEmpty(String values, String filterByField, String filterByValue) {
        for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {
            for (AaiVnfcInfo vnfcInfo : vm.getVnfcInfo()) {
                if (StringUtils.isBlank(values)) {
                    values = vnfcInfo.getVnfcName();
                } else {
                    values = values + "," + vnfcInfo.getVnfcName();
                }
            }
        }
        return values;
    }

    private String getVnfcDetailsFromContext(String fieldKeyName, String filterByField, String filterByValue) {
        String fn = "AaiInterfaceRulesHander::getVnfcDetailsFromContext()";
        String values = "";
        log.info(fn + "FieldKeyName:" + fieldKeyName + " FilterByField:" + filterByField + " FilterByValue:"
            + filterByValue);
        if (StringUtils.equalsIgnoreCase(fieldKeyName, "ipaddress-v4-oam-vip")) {
            if (StringUtils.isNotEmpty(filterByField) && StringUtils.isNotEmpty(filterByValue)) {
                values = add2ValuesIpaddressV4OamVipNotEmpty(values, filterByField, filterByValue);
            } else {
                values = add2ValuesIpaddressV4OamVipEmpty(values, filterByField, filterByValue);
            }
        }
        if (StringUtils.equalsIgnoreCase(fieldKeyName, "vnfc-name")) {
            if (StringUtils.isNotBlank(filterByField) && StringUtils.isNotBlank(filterByValue)) {
                values = add2ValuesVnfcNameNotEmpty(values, filterByField, filterByValue);
            } else {
                values = add2ValuesVnfcNameEmpty(values, filterByField, filterByValue);
            }
        }
        log.info(fn + "Returning values:" + values);
        return values;
    }

    private String getVnfDetailsFromContext(String fieldKeyName) {

        log.info("getVnfDetailsFromContext::" + fieldKeyName);
        String values = "";
        if (StringUtils.equalsIgnoreCase(fieldKeyName, "vnf-name")) {
            String vnfName = context.getAttribute("tmp.vnfInfo.vnf.vnf-name");
            values = vnfName;
        }
        if (StringUtils.equalsIgnoreCase(fieldKeyName, "ipv4-oam-ipaddress")) {
            String ipv4OamAddress = context.getAttribute("tmp.vnfInfo.vnf.ipv4-oam-address");
            values = ipv4OamAddress;
        }
        return values;
    }

    public AaiVnfInfo getVnfInfoData() {
        return vnfInfoData;
    }

    public void setVnfInfoData(AaiVnfInfo vnfInfoData) {
        this.vnfInfoData = vnfInfoData;
    }

    public AaiVnfInfo generateAaiVnfInfoData() {

        log.info("AaiInterfaceRulesHandlerImpl:generateAaiVnfInfoData(): Printing variables in context");
        for (Object key : context.getAttributeKeySet()) {
            String parmName = (String) key;
            String parmValue = context.getAttribute(parmName);
            log.debug("generateAaiVnfInfoData():: " + parmName + "=" + parmValue);

        }

        String vmcount = context.getAttribute("tmp.vnfInfo.vm-count");
        int vmCount = 0;
        if (!StringUtils.isBlank(vmcount)) {
            vmCount = Integer.parseInt(vmcount);
        }
        log.info("generateAaiVnfInfoData::" + "vmCount:" + vmCount);
        AaiVnfInfo vnfInfo = new AaiVnfInfo();
        vnfInfo.setVnfName("vnf-name");
        ArrayList<AaiVmInfo> vmList = new ArrayList<AaiVmInfo>();

        for (int i = 0; i < vmCount; i++) {
            AaiVmInfo vm = new AaiVmInfo();
            String vnfcCountStr = context.getAttribute("tmp.vnfInfo.vm[" + i + "].vnfc-count");
            int vnfcCount = Integer.parseInt(vnfcCountStr);
            ArrayList<AaiVnfcInfo> vnfcInfoList = new ArrayList<AaiVnfcInfo>();
            for (int j = 0; j < vnfcCount; j++) {
                AaiVnfcInfo vnfcInfo = new AaiVnfcInfo();
                vnfcInfo.setVnfcName(context.getAttribute("tmp.vnfInfo.vm[" + i + "].vnfc-name"));
                vnfcInfo.setVnfcFunctionCode(context.getAttribute("tmp.vnfInfo.vm[" + i + "].vnfc-function-code"));
                vnfcInfo.setVnfcOamIpAddress(
                    context.getAttribute("tmp.vnfInfo.vm[" + i + "].vnfc-ipaddress-v4-oam-vip"));
                vnfcInfoList.add(vnfcInfo);
            }
            vm.setVnfcInfo(vnfcInfoList);
            vm.setVserverId(context.getAttribute("tmp.vnfInfo.vm[" + i + "].vserver-id"));
            vm.setVserverName(context.getAttribute("tmp.vnfInfo.vm[" + i + "].vserver-name"));
            vmList.add(vm);
        }
        vnfInfo.setVmInfo(vmList);
        return vnfInfo;
    }
}