<#--
 ============LICENSE_START=======================================================
 ONAP : APPC
 ================================================================================
 Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 ================================================================================
 Copyright (C) 2017 Amdocs
 =============================================================================
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 ECOMP is a trademark and service mark of AT&T Intellectual Property.
 ============LICENSE_END=========================================================
-->

<#global template = .main_template_name>
<#function toJavaName text>
    <#assign names = text?split("-")>
    <#assign returnValue = "">
    <#list names as name>
        <#assign returnValue = returnValue + name?cap_first>
    </#list>
    <#return returnValue?uncap_first>
</#function>
<#function toJavaType type>
    <#switch type>
        <#case "string">
            <#return "String">
        <#case "integer">
            <#return "int">
        <#default>
            <#return type>
    </#switch>
</#function>
<#macro generated>
@javax.annotation.Generated(
    value = {"${template}"},
    date = "${.now?string.iso}",
    comments = "Auto-generated from Open API specification")
</#macro>
