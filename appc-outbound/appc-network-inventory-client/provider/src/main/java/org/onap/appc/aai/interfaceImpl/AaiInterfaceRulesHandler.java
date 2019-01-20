/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
import org.onap.appc.system.interfaces.RuleHandlerInterface;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.sdnc.config.params.data.Parameter;
import org.onap.sdnc.config.params.data.ResponseKey;

public class AaiInterfaceRulesHandler implements RuleHandlerInterface {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(AaiInterfaceRulesHandler.class);
    private static final String STR_RETURNING_VALUES = "Returning values: ";
    private static final String STR_VNF_INFO_VM = "tmp.vnfInfo.vm[";

    private Parameter parameters;
    private SvcLogicContext context;
    private AaiVnfInfo vnfInfoData;

    public AaiInterfaceRulesHandler(Parameter params, SvcLogicContext ctx) {
        this.parameters = params;
        this.context = ctx;
        this.setVnfInfoData(generateAaiVnfInfoData());
    }

    @Override
    public void processRule() {

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
            if (StringUtils.isNotBlank(filterKeys.getFieldKeyName())) {
                respKeys.setFieldKeyName(filterKeys.getFieldKeyName());
            }
            trySetUniqueKey(respKeys, filterKeys);
            trySetFilters(respKeys, filterKeys);
        }
        processKeys(respKeys, parameters.getName());
    }

    private void trySetFilters(ResponseKey respKeys, ResponseKey filterKeys) {
        if (StringUtils.isNotBlank(filterKeys.getFilterByField())) {
            respKeys.setFilterByField(filterKeys.getFilterByField());
        }
        if (StringUtils.isNotBlank(filterKeys.getFilterByValue())) {
            respKeys.setFilterByValue(filterKeys.getFilterByValue());
        }
    }

    private void trySetUniqueKey(ResponseKey respKeys, ResponseKey filterKeys) {
        if (StringUtils.isNotBlank(filterKeys.getUniqueKeyName())) {
            respKeys.setUniqueKeyName(filterKeys.getUniqueKeyName());
        }
        if (StringUtils.isNotBlank(filterKeys.getUniqueKeyValue())) {
            respKeys.setUniqueKeyValue(filterKeys.getUniqueKeyValue());
        }
    }

    private void processKeys(ResponseKey filterKey, String aaiKey) {

        String fn = "AaiInterfaceRulesHandler.processKeys()::";
        log.info(fn + "processing for " + aaiKey);
        String values = "";
        JSONObject aaiKeyValues;
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
    }

    private String getVServerDetailsFromContext(String fieldKeyName, String filterByField, String filterByValue) {
        String fn = "AaiInterfaceRulesHander::getVServerDetailsFromContext():";
        StringBuilder values = new StringBuilder("");
        log.info(fn + "FieldKeyName:" + fieldKeyName + " FilterByName:" + filterByField + " FilterByValue:"
            + filterByValue);

        if (!StringUtils.equalsIgnoreCase(fieldKeyName, "vserver-name")) {
            log.info(fn + STR_RETURNING_VALUES + values);
            return values.toString();
        }

        if (validateFilters(filterByField, filterByValue)) {
            int vmIndex = -1;
            for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {
                vmIndex++;

                int vmNumber = Integer.parseInt(filterByValue);
                if (!StringUtils.equalsIgnoreCase(filterByField, "vm-number") || vmNumber != vmIndex) {
                    continue;
                }
                if (StringUtils.isBlank(values.toString())) {
                    values = new StringBuilder(vm.getVserverName());
                } else {
                    values.append(",").append(vm.getVserverName());
                }
            }
        } else {
            for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {
                if (StringUtils.isBlank(values.toString())) {
                    values = new StringBuilder(vm.getVserverName());
                } else {
                    values.append(",").append(vm.getVserverName());
                }
            }
        }
        log.info(fn + STR_RETURNING_VALUES + values);
        return values.toString();
    }

    private boolean validateFilters(String filterByField, String filterByValue) {
        return StringUtils.isNotEmpty(filterByField) && StringUtils.isNotEmpty(filterByValue);
    }

    //split from getVnfcDetailsFromContext
    private String add2ValuesIpaddressV4OamVipNotEmpty(String values, String filterByField, String filterByValue) {
        StringBuilder builder = new StringBuilder(values);
        for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {

            for (AaiVnfcInfo vnfcInfo : vm.getVnfcInfo()) {
                if (!StringUtils.equalsIgnoreCase(filterByField, "vnfc-function-code")
                    || !StringUtils.equalsIgnoreCase(filterByValue, vnfcInfo.getVnfcFunctionCode())) {
                    continue;
                }

                if (StringUtils.isBlank(builder.toString())) {
                    builder = new StringBuilder(vnfcInfo.getVnfcOamIpAddress());
                } else {
                    builder.append(",").append(vnfcInfo.getVnfcOamIpAddress());
                }
            }
        }
        return builder.toString();
    }

    //split from getVnfcDetailsFromContext
    private String add2ValuesIpaddressV4OamVipEmpty(String values, String filterByField, String filterByValue) {
        StringBuilder builder = new StringBuilder(values);
        for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {
            for (AaiVnfcInfo vnfcInfo : vm.getVnfcInfo()) {
                if (StringUtils.isBlank(builder.toString())) {
                    builder = new StringBuilder(vnfcInfo.getVnfcOamIpAddress());
                } else {
                    builder.append(",").append(vnfcInfo.getVnfcOamIpAddress());
                }
            }
        }
        return builder.toString();
    }

    //split from getVnfcDetailsFromContext
    private String add2ValuesVnfcNameNotEmpty(String values, String filterByField, String filterByValue) {
        StringBuilder builder = new StringBuilder(values);
        for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {
            for (AaiVnfcInfo vnfcInfo : vm.getVnfcInfo()) {
                if (!StringUtils.equalsIgnoreCase(filterByField, "vnfc-function-code")
                    || !StringUtils.equalsIgnoreCase(filterByValue, vnfcInfo.getVnfcFunctionCode())) {
                    continue;
                }
                if (StringUtils.isBlank(builder.toString())) {
                    builder = new StringBuilder(vnfcInfo.getVnfcName());
                } else {
                    builder.append(",").append(vnfcInfo.getVnfcName());
                }
            }
        }
        return builder.toString();
    }

    //split from getVnfcDetailsFromContext
    private String add2ValuesVnfcNameEmpty(String values, String filterByField, String filterByValue) {
        for (AaiVmInfo vm : vnfInfoData.getVmInfo()) {
            StringBuilder builder = new StringBuilder(values);
            for (AaiVnfcInfo vnfcInfo : vm.getVnfcInfo()) {
                if (StringUtils.isBlank(builder.toString())) {
                    builder = new StringBuilder(vnfcInfo.getVnfcName());
                } else {
                    builder.append(",").append(vnfcInfo.getVnfcName());
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
            if (validateFilters(filterByField, filterByValue)) {
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
        log.info(fn + STR_RETURNING_VALUES + values);
        return values;
    }

    private String getVnfDetailsFromContext(String fieldKeyName) {

        log.info("getVnfDetailsFromContext::" + fieldKeyName);
        String values = "";
        if (StringUtils.equalsIgnoreCase(fieldKeyName, "vnf-name")) {
            values = context.getAttribute("tmp.vnfInfo.vnf.vnf-name");
        }
        if (StringUtils.equalsIgnoreCase(fieldKeyName, "ipv4-oam-ipaddress")) {
            values = context.getAttribute("tmp.vnfInfo.vnf.ipv4-oam-address");
        }
        return values;
    }

    public AaiVnfInfo getVnfInfoData() {
        return vnfInfoData;
    }

    private void setVnfInfoData(AaiVnfInfo vnfInfoData) {
        this.vnfInfoData = vnfInfoData;
    }

    private AaiVnfInfo generateAaiVnfInfoData() {

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
        ArrayList<AaiVmInfo> vmList = new ArrayList<>();

        for (int i = 0; i < vmCount; i++) {
            AaiVmInfo vm = new AaiVmInfo();
            String vnfcCountStr = context.getAttribute(STR_VNF_INFO_VM + i + "].vnfc-count");
            int vnfcCount = Integer.parseInt(vnfcCountStr);
            ArrayList<AaiVnfcInfo> vnfcInfoList = new ArrayList<>();
            for (int j = 0; j < vnfcCount; j++) {
                AaiVnfcInfo vnfcInfo = new AaiVnfcInfo();
                vnfcInfo.setVnfcName(context.getAttribute(STR_VNF_INFO_VM + i + "].vnfc-name"));
                vnfcInfo.setVnfcFunctionCode(context.getAttribute(STR_VNF_INFO_VM + i + "].vnfc-function-code"));
                vnfcInfo.setVnfcOamIpAddress(
                    context.getAttribute(STR_VNF_INFO_VM + i + "].vnfc-ipaddress-v4-oam-vip"));
                vnfcInfoList.add(vnfcInfo);
            }
            vm.setVnfcInfo(vnfcInfoList);
            vm.setVserverId(context.getAttribute(STR_VNF_INFO_VM + i + "].vserver-id"));
            vm.setVserverName(context.getAttribute(STR_VNF_INFO_VM + i + "].vserver-name"));
            vmList.add(vm);
        }
        vnfInfo.setVmInfo(vmList);
        return vnfInfo;
    }
}