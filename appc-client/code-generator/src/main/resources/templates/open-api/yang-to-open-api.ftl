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

<#global _ = "    ">
<#global __ = _ + _>
<#global ___ = __ + _>
<#global ____ = ___ + _>
<#global _____ = ____ + _>
<#global ______ = _____ + _>
<#global _______ = ______ + _>
<#global ________ = _______ + _>
<#global _________ = ________ + _>
<#global __________ = _________ + _>
<#--
=================================================================
    Function definitions
=================================================================
-->
<#function toCamelNotation text>
    <#local temp = text?upper_case>
    <#-- Preserve abbreviations (e.g. UUID) - only replace hyphens with underscores, if any -->
    <#if text == temp>
        <#return temp?replace("-", "_ ")>
    <#else>
        <#return text?replace("-", " ")?capitalize?replace(" ", "")>
    </#if>
</#function>
<#function toRpcInput rpcName>
    <#return toCamelNotation(rpcName) + "Input">
</#function>
<#function toRpcOutput rpcName>
    <#return toCamelNotation(rpcName) + "Output">
</#function>
<#function toJsonType type>
    <#if type == "string" || type == "enumeration">
        <#return "string">
    <#elseif type == "uint16" || type == "uint32">
        <#return "integer">
    <#elseif type == "boolean">
         <#return "boolean">
    <#else>
        <#stop "UNSUPPORTED TYPE - ${type}">
    </#if>
</#function>
<#function encode text>
    <#local temp = text?replace('(\r|\n)+', '', 'r')?replace('( )+', ' ', 'rm')>
    <#return temp>
</#function>
<#--
  Similar to Java's Class.isAssignableFrom(Class)
-->
<#function isAssignableFrom class typeName>
    <#list class.interfaces as interfaceClass>
        <#if interfaceClass.name == typeName || isAssignableFrom(interfaceClass, typeName)>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function isContainerNode node>
    <#return isAssignableFrom(node.class, "org.opendaylight.yangtools.yang.model.api.DataNodeContainer")>
</#function>
<#function isLeafNode node>
    <#return isAssignableFrom(node.class, "org.opendaylight.yangtools.yang.model.api.LeafSchemaNode")>
</#function>
<#function isEnumType type>
    <#return isAssignableFrom(type.class, "org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition")>
</#function>
<#function isStringType type>
    <#return isAssignableFrom(type.class, "org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition")>
</#function>
<#function isExtendedType type>
    <#if type.baseType??>
        <#return true>
    </#if>
    <#return false>
</#function>
<#function isGlobalTypeDef obj>
    <#list module.typeDefinitions as typeDef>
        <#if typeDef.QName.localName == obj.type.QName.localName>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function getMandatoryProperties obj>
    <#local result = {}>
    <#list obj.childNodes as property>
        <#if property.constraints.mandatory>
            <#local result = result + {property.QName.localName : ""}>
        </#if>
    </#list>
    <#return result>
</#function>
<#function gatherModuleTypes module>
    <#local result = {}>
    <#list module.rpcs as rpc>
        <#local result = gatherNestedTypes(rpc.input, result)>
        <#local result = gatherNestedTypes(rpc.output, result)>
    </#list>
    <#return result>
</#function>
<#function gatherNestedTypes container result>
    <#list container.childNodes as property>
        <#if isContainerNode(property)>
            <#local name = property.QName.localName>
            <#if !result[name]??>
                <#local result = result + {name : property}>
                <#local result = gatherNestedTypes(property, result)>
            </#if>
        </#if>
    </#list>
    <#return result>
</#function>
<#--
=================================================================
    Macro definitions
=================================================================
-->
<#-- 
    Macro to generate description property, if any.
-->
<#macro description obj indent = "">
<#if obj.description??>
${indent},
${indent}"description" : "${encode(obj.description)}"<#else>${indent}</#if>
</#macro>
<#--
    Macro to generate enum of valid values
-->
<#macro enum yangType indent = "">
<#if isEnumType(yangType)>
${indent},
${indent}"enum" : [
${indent}${_}<#list yangType.values as enum>"${enum.name}"<#if enum?has_next>, </#if></#list>
${indent}]
</#if>
</#macro>
<#--
    Macro to print out schema constraints (min/max/pattern/etc.) if any. Used for custom types defined either globaly via 'typedef'
    statement or locally (in-line withing a leaf node definition).
 -->
<#macro constraints yangType indent = "">
<#if yangType.patternConstraints?size != 0>
${indent},
${indent}"pattern" : "${yangType.patternConstraints?first.regularExpression?replace('\\\\', '\\\\\\\\', 'r')}"
</#if>
<#if yangType.lengthConstraints?size != 0>
${indent},
${indent}"minLength" : ${yangType.lengthConstraints?first.min},
${indent}"maxLength"  : ${yangType.lengthConstraints?first.max}
</#if>
<@enum yangType = yangType.baseType indent = indent />
</#macro>
<#--
    Macro to generate a property within an outer object
 -->
<#macro property name prop indent = "">
${indent}"${name}": {
<#if isContainerNode(prop)>
${indent}${_}"$ref" : "#/definitions/${toCamelNotation(prop.QName.localName)}"
<#elseif isExtendedType(prop.type) && isGlobalTypeDef(prop)>
${indent}${_}"$ref" : "#/definitions/${toCamelNotation(prop.type.QName.localName)}"
<#elseif isExtendedType(prop.type)>
${indent}${_}"type" : "${toJsonType(prop.type.QName.localName)}"
<@description obj = prop indent = indent + _ />
<@constraints yangType = prop.type indent = indent + _ />
<#else><#--  leaf node -->
${indent}${_}"type" : "${toJsonType(prop.type.QName.localName)}"
<@description obj = prop indent = indent + _ />
<@enum yangType = prop.type indent = indent + _ />
</#if>
${indent}}</#macro>
<#--
    Macro generates declaration of a container node modeled in YANG schema
-->
<#macro container name node indent = "">
${indent}"${name}" : {
${indent}${_}"type" : "object"
<@description obj = node indent = indent + _ />
${indent}${_},
${indent}${_}"properties" : {
<#list node.childNodes as child>
<@property name = child.QName.localName prop = child indent = indent + __ /><#if child?has_next>,</#if>
</#list>
${indent}${_}}<#--  end of properties -->
<#local mandatoryProperties = getMandatoryProperties(node)>
<#if mandatoryProperties?size != 0>
,
${indent}${_}"required" : [<#list mandatoryProperties?keys as p>"${p}"<#if p?has_next> ,</#if></#list>]
</#if>
${indent}}<#--  end of container -->
</#macro>
<#--
    Macro generates declaration of JSON object for a custom type defined in YANG schema via 'typedef' statement.
-->
<#macro typedef name yangType indent = "">
${indent}"${name}" : {
<#local jsonType = toJsonType(yangType.baseType.QName.localName)>
${indent}${_}"type" : "${jsonType}"
<@description obj = yangType indent = indent + _ />
<@constraints yangType = yangType indent = indent + _ />
${indent}}<#--  end of container -->
</#macro>
<#--
=================================================================
    Content body
=================================================================
-->
<#assign moduleName = module.name>
{
${_}"swagger": "2.0",
${_}"info": {
${__}"version": "${module.QNameModule.formattedRevision}"
<@description obj = module indent = __ />,
${__}"contact": {
${_____}"name" : "${module.contact}"
${__}},
${__}"title": "${moduleName}"
${_}},
${_}"basePath": "/restconf",
${_}"tags": [{"name": "${moduleName}"}],
${_}"schemes": [ "https", "http" ],
${_}"paths": {
<#list module.rpcs as rpc>
<#assign rpcName = rpc.QName.localName>
${__}"/operations/${moduleName}:${rpcName}": {
${___}"post": {
${____}"tags": ["${moduleName}"],
${____}"summary": ""
<@description obj = rpc indent = ____ />,
${____}"operationId": "${rpcName}",
${____}"consumes": ["application/json"],
${____}"produces": ["application/json"],
${____}"parameters": [{
${_____}"in": "body",
${_____}"name": "input",
${_____}"required": true,
${_____}"schema": {
${______}"type" : "object",
${______}"properties" : {
${_______}"input" : {
${________}"$ref": "#/definitions/${toRpcInput(rpcName)}"
${_______}}
${______}}
${_____}}
${____}}],
${____}"responses": {
${_____}"200": {
${______}"description": "Successful operation",
${______}"schema": {
${_______}"type" : "object",
${_______}"properties" : {
${________}"output" : {
${_________}"$ref": "#/definitions/${toRpcOutput(rpcName)}"
${________}}
${_______}}
${______}}
${_____}},
${_____}"401": {"description" : "Unauthorized"},
${_____}"500": {"description" : "Internal server error"}
${____}}
${___}}
${__}}<#if rpc?has_next>,</#if>
</#list>
${_}},
${_}"definitions": {
<#--
    Definition per custom type defined via 'typedef'
-->
<#list module.typeDefinitions as typeDef>
<@typedef name = toCamelNotation(typeDef.QName.localName) yangType = typeDef indent = __ />
<#if typeDef?has_next>${__},</#if>
</#list>
<#--
    Definition per container explicitly defined in YANG schema
-->
<#assign definitions = gatherModuleTypes(module)>
<#if module.typeDefinitions?size != 0 && definitions?size != 0>${__},</#if>
<#list definitions?keys as definition>
<@container name = toCamelNotation(definition) node = definitions[definition] indent = __ /><#if definition?has_next>${__},</#if>
</#list>
<#if definitions?size != 0 && module.rpcs?size != 0>${__},</#if>
<#--
    Definition per PRC input/output parameter block implicitly defined in YANG schema
-->
<#list module.rpcs as rpc>
<#assign rpcName = rpc.QName.localName>
<@container name = toRpcInput(rpcName) node = rpc.input indent = __ />${__},
<@container name = toRpcOutput(rpcName) node = rpc.output indent = __ /><#if rpc?has_next>${__},</#if>
</#list>
${_}}<#-- end of "definitions" -->
} <#--  end of file  -->
