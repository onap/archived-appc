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

/*
 * NOTE: This file is auto-generated and should not be changed manually.
 */
package ${meta.api\.package};

<#list service?keys as action>
    <#assign outputType = service[action].post.responses["200"].schema.properties.output.$ref?keep_after_last("/")>
    <#assign inputType = service[action].post.parameters[0].schema.properties.input.$ref?keep_after_last("/")>
import ${meta.model\.package}.${outputType};
import ${meta.model\.package}.${inputType};
</#list>
import ${meta.exceptions\.package}.AppcClientException;
import ${meta.utils\.package}.RPC;

<#if model.info.description??>
/**
* ${model.info.description}
*/
</#if>
<@generated/>
public interface ${meta.interface\.classname} {

<#list service?keys as action>
    <#assign returnType = service[action].post.responses["200"].schema.properties.output.$ref?keep_after_last("/")>
    <#assign rpcName = service[action].post.operationId>
    <#assign methodName = toJavaName(rpcName)>
    <#assign methodInputType = service[action].post.parameters[0].schema.properties.input.$ref?keep_after_last("/")>
    <#assign methodInputName = methodInputType?uncap_first>
    <#assign description = service[action].post.description>
    /**
     * ${description}
     *
     * @param ${methodInputName} - RPC input object
     */
    @RPC(name="${rpcName}", outputType=${returnType}.class)
    ${returnType} ${methodName}(${methodInputType} ${methodInputName}) throws AppcClientException;

    /**
     * ${description}
     *
     * @param ${methodInputName} - RPC input object
     * @return listener - callback implementation
     */
    @RPC(name="${rpcName}", outputType=${returnType}.class)
    void ${methodName}(${methodInputType} ${methodInputName}, ResponseHandler<${returnType}> listener) throws AppcClientException;

</#list>
}
