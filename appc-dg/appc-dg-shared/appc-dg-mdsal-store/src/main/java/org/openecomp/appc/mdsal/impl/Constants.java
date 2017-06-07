/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.mdsal.impl;
/**
 * This class contains the definitions of all constant values used in the appc-dg-mdsal-store
 * These properties are used for creating osgi bundle zip file. It also defines contents for Blueprint.xml file of bundle
*/
public class Constants {

    private Constants(){}
    /**
     * Manifest attribute for OSGI Bundle Name
     */
    public static final String MANIFEST_ATTR_BUNDLE_NAME= "Bundle-Name";

    /**
     * Manifest attribute for OSGI Bundle Symbolic Name
     */
    public static final String MANIFEST_ATTR_BUNDLE_SYMBOLIC_NAME= "Bundle-SymbolicName";

    /**
     * Manifest attribute for OSGI Bundle Description
     */
    public static final String MANIFEST_ATTR_BUNDLE_DESCRIPTION= "Bundle-Description";

    /**
     * Manifest attribute for OSGI Bundle Manifest version
     */
    public static final String MANIFEST_ATTR_BUNDLE_MANIFEST_VERSION= "Bundle-ManifestVersion";

    /**
     * Manifest attribute for OSGI Bundle Version
     */
    public static final String MANIFEST_ATTR_BUNDLE_VERSION= "Bundle-Version";

    /**
     * Manifest attribute for OSGI Bundle Blueprint
     */
    public static final String MANIFEST_ATTR_BUNDLE_BLUEPRINT= "Bundle-Blueprint";

    /**
     * Manifest value for Mainfest Version
     */
    public static final String MANIFEST_VALUE_VERSION= "1.0";

    /**
     * Manifest value for OSGI Bundle Vesion
     */
    public static final String MANIFEST_VALUE_BUNDLE_MAN_VERSION= "2";

    /**
     * Manifest value for OSGI Bundle Blueprint location
     */
    public static final String MANIFEST_VALUE_BUNDLE_BLUEPRINT= "OSGI-INF/blueprint/blueprint.xml";

    /**
     * Base URL for config actions exposed by RESTCONF API
     */

    public static final String CONFIG_URL = "https://localhost:8443/restconf/config";

    /**
     * Content for blueprint.xml used while creation of OSGI bundle.
     */
    public static final String BLUEPRINT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!--\n" +
            "    Starter Blueprint Camel Definition appc-aai-adapter-blueprint\n" +
            "-->\n" +
            "<blueprint xmlns=\"http://www.osgi.org/xmlns/blueprint/v1.0.0\"\n" +
            "                       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                       xsi:schemaLocation=\"http://www.osgi.org/xmlns/blueprint/v1.0.0 http://www.osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd\">\n" +
            "\n" +
            "</blueprint>";

    /**
     * HTTP Header attribute for Content type - JSON
     */
    public static final String OPERATION_APPLICATION_JSON= " application/json";

    /**
     * HTTP protocol used for config operations
     */
    public static final String OPERATION_HTTPS= "https";

    /**
     *  Constant for backslash to be used while formatting URL
     */
    public static final String URL_BACKSLASH ="/";
}
