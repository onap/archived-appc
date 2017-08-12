/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 *  ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.config.params.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseKey{
	@JsonProperty("unique-key-name")
	private String uniqueKeyName;
	@JsonProperty("unique-key-value")
	private String uniqueKeyValue;
	@JsonProperty("field-key-name")
	private String fieldKeyName;
	
	public String getUniqueKeyName() {
		return uniqueKeyName;
	}
	public void setUniqueKeyName(String uniqueKeyName) {
		this.uniqueKeyName = uniqueKeyName;
	}
	public String getUniqueKeyValue() {
		return uniqueKeyValue;
	}
	public void setUniqueKeyValue(String uniqueKeyValue) {
		this.uniqueKeyValue = uniqueKeyValue;
	}
	public String getFieldKeyName() {
		return fieldKeyName;
	}
	public void setFieldKeyName(String fieldKeyName) {
		this.fieldKeyName = fieldKeyName;
	}
	
	
}
