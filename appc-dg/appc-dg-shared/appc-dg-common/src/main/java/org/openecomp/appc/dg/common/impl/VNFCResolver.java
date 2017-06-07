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

package org.openecomp.appc.dg.common.impl;

import org.openecomp.appc.rankingframework.RankedAttributesContext;


public class VNFCResolver extends AbstractResolver {
    VNFCResolver(int interval) {
        super(interval);
    }

    @Override
    protected FlowKey resolve(String... args) {
        if(args.length !=4){
            throw new IllegalStateException(args.toString());
        }
        return resolve(args[0],args[1],args[2],args[3]);
    }

    protected FlowKey resolve(final String action, final String vnfType, final String vnfcType, final String apiVersion) {
        RankedAttributesContext context = new RankedAttributesContext() {
            @Override
            public Object getAttributeValue(String name) {
                switch (name) {
                    case "action":
                        return action;
                    case "api_version":
                        return apiVersion;
                    case "vnf_type":
                        return vnfType;
                    case "vnfc_type":
                        return vnfcType;
                    default:
                        throw new IllegalStateException(name);
                }
            }
        };

        FlowKey wfKey = resolver("VNFC").resolve(context);

        return wfKey;
    }
}

