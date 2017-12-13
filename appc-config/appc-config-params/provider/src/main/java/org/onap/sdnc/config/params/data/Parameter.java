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

package org.openecomp.sdnc.config.params.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Parameter {
	private String name;
	private String description;
	private String type;
	private boolean required;
	private String source;

	@JsonProperty("rule-type")
	private String ruleType;

	@JsonProperty("default")
	private String defaultValue;

	@JsonProperty("request-keys")
	private List<RequestKey> requestKeys;

	@JsonProperty("response-keys")
	private List<ResponseKey> responseKeys;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getRuleType() {
		return ruleType;
	}

	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public List<RequestKey> getRequestKeys() {
		return requestKeys;
	}

	public void setRequestKeys(List<RequestKey> requestKeys) {
		this.requestKeys = requestKeys;
	}

	public List<ResponseKey> getResponseKeys() {
		return responseKeys;
	}

	public void setResponseKeys(List<ResponseKey> responseKeys) {
		this.responseKeys = responseKeys;
	}





}
