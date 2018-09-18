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

package org.onap.appc.encryptiontool.wrapper;

import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;

public class EncryptionToolDGWrapper implements SvcLogicJavaPlugin {
    private static final EELFLogger log = EELFManager.getInstance().getLogger(EncryptionToolDGWrapper.class);
    private SvcLogicResource serviceLogic;
    private static EncryptionToolDGWrapper dgGeneralDBService = null;

    public static EncryptionToolDGWrapper initialise() {
        if (dgGeneralDBService == null) {
            dgGeneralDBService = new EncryptionToolDGWrapper();
        }
        return dgGeneralDBService;
    }

    public EncryptionToolDGWrapper() {
        if (serviceLogic == null) {
            serviceLogic = new SqlResource();
        }
    }

    protected EncryptionToolDGWrapper(SqlResource svcLogic) {
        if (serviceLogic == null) {
            serviceLogic = svcLogic;
        }
    }

    public void runEncryption(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        String userName = inParams.get("userName");
        String password = inParams.get("password");
        String vnfType = inParams.get("vnf_type");
        try {
            if (StringUtils.isBlank(userName) || StringUtils.isBlank(password) || StringUtils.isBlank(vnfType)) {
                throw new SvcLogicException("username or Password is missing");
            }
            String[] input = new String[] { vnfType, userName, password };
            WrapperEncryptionTool.main(input);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
    }

    public void getProperty(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        String fn = "getproperty.deviceauthentication";
        String responsePrefix = inParams.get("prefix");
        String vnf_Type = ctx.getAttribute("vnf-type");
        String action = ctx.getAttribute("input.action");
        String protocol = ctx.getAttribute("APPC.protocol.PROTOCOL");
        String user = "";
        String password = "";
        String port = "0";
        String url = "";
        QueryStatus status = null;

        responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
        try {
            if (serviceLogic != null && ctx != null) {
                String key = "SELECT USER_NAME ,PASSWORD,PORT_NUMBER,URL FROM  DEVICE_AUTHENTICATION  WHERE VNF_TYPE = '"
                        + vnf_Type + "' AND PROTOCOL = '" + protocol + "' AND ACTION = '" + action + "'";
                log.info("Getting authentication details :" + key);
                status = serviceLogic.query("SQL", false, null, key, null, null, ctx);
                if (status == QueryStatus.FAILURE) {
                    log.info(fn + ":: Error retrieving credentials");
                    throw new SvcLogicException("Error retrieving credentials");
                }
                if (status == QueryStatus.NOT_FOUND) {
                    log.info(fn + ":: NOT_FOUND! No data found in device_authentication table for " + vnf_Type + " "
                            + protocol + "" + action + "");
                    throw new SvcLogicException(fn + ":: NOT_FOUND! No data found in device_authentication table for "
                            + vnf_Type + " " + protocol + "" + action + "");
                }

                user = ctx.getAttribute("USER-NAME");
                password = ctx.getAttribute("PASSWORD");
                port = ctx.getAttribute("PORT-NUMBER");
                url = ctx.getAttribute("URL");
                log.info("data retrieved " + "user" + user + "pwd" + password + "port" + port + "url" + url);

                if (StringUtils.isNotBlank(user))
                    ctx.setAttribute(responsePrefix + "user", user);
                if (StringUtils.isNotBlank(password))
                    ctx.setAttribute(responsePrefix + "password", password);
                if (StringUtils.isNotBlank(url))
                    ctx.setAttribute(responsePrefix + "url", url);
                if (StringUtils.isNotBlank(port))
                    ctx.setAttribute(responsePrefix + "port", port);
                log.debug("user" + ctx.getAttribute(responsePrefix + "user"));
                log.debug("password" + ctx.getAttribute(responsePrefix + "password"));
                log.debug("url" + ctx.getAttribute(responsePrefix + "url"));
                log.debug("port" + ctx.getAttribute(responsePrefix + "port"));

            }
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + "status", "failure");
            ctx.setAttribute(responsePrefix + "error-message", e.getMessage());
            log.info("Caught exception", e);
            throw new SvcLogicException(e.getMessage());

        }
    }
}
