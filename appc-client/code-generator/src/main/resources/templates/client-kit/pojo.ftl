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

/**
 * NOTE: This file is auto-generated and should not be changed manually.
 */
package ${meta.model\.package};

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

<#if pojo.description??>
/**
 * ${pojo.description}
 */
</#if>
<@generated/>
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ${objectName} {

<#assign properties = pojo.properties>
<#list properties?keys as member>
    <#if properties[member]?is_hash>
        <#assign varName = toJavaName(member)>
        <#if properties[member].type??>
            <#assign property = properties[member]>
        <#else>
            <#assign ref = properties[member].$ref?keep_after_last("/")>
            <#assign property = model.definitions[ref]>
        </#if>
        <#if properties[member].enum??>
            <#assign varType = property.type?cap_first>
    public enum ${varName?cap_first} {
            <#list property.enum as enum>
        ${enum}<#if enum?has_next>,<#else>;</#if>
            </#list>
    }

    @JsonProperty("${member}")
    private ${varName?cap_first} ${varName};
        <#else>
            <#if properties[member].$ref??>
                <#assign varType = toJavaType(ref)>
            <#else>
                <#assign varType = toJavaType(property.type)>
            </#if>
    @JsonProperty("${member}")
    private ${varType} ${varName};
        </#if>
    </#if>

</#list>
<#list properties?keys as member>
    <#if properties[member]?is_hash>
        <#if properties[member].type??>
            <#assign property = properties[member]>
        <#elseif properties[member].$ref??>
            <#assign ref = properties[member].$ref?keep_after_last("/")>
            <#assign property = model.definitions[ref]>
        </#if>
        <#if properties[member].$ref??>
            <#assign varType = ref>
        <#else>
            <#assign varType = toJavaType(property.type)>
        </#if>
        <#if property.enum??>
            <#assign varType = toJavaName(member)?cap_first>
        </#if>
        <#assign varName = toJavaName(member)>
        <#if property.description??>
    /**
     * ${property.description}
     */
        </#if>
    public ${varType} get${varName?cap_first}() {
        return ${varName};
    }

        <#if property.description??>
    /**
     * ${property.description}
     */
        </#if>
    public void set${toJavaName(member)?cap_first}(${varType} ${varName}) {
        this.${varName} = ${varName};
    }

    </#if>
</#list>
}
