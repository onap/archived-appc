/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
 * ================================================================================
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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.07.31 at 10:30:39 AM EDT 
//


package org.openecomp.appc.flow.controller.interfaceData;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "vserverId",
    "vnfc"
})
@XmlRootElement(name = "vm")
public class Vm {

    @XmlElement(name = "vserver-id", required = true)
    @JsonProperty("vserver-id")
    protected String vserverId;
    @XmlElement(required = true)
    @JsonProperty("vnfc")
    protected Vnfcslist vnfc;

    /**
     * Gets the value of the vserverId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVserverId() {
        return vserverId;
    }

    /**
     * Sets the value of the vserverId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVserverId(String value) {
        this.vserverId = value;
    }

    /**
     * Gets the value of the vnfc property.
     * 
     * @return
     *     possible object is
     *     {@link Vnfcslist }
     *     
     */
    public Vnfcslist getVnfc() {
        return vnfc;
    }

    /**
     * Sets the value of the vnfc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Vnfcslist }
     *     
     */
    public void setVnfc(Vnfcslist value) {
        this.vnfc = value;
    }

    @Override
    public String toString() {
        return "Vm [vserverId=" + vserverId + ", vnfc=" + vnfc + "]";
    }

}
