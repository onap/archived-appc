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
package org.onap.appc.flow.controller.dbervices;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.utils.EscapeUtils;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class FlowControlDBService {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(FlowControlDBService.class);
    private SvcLogicResource serviceLogic;
    private static FlowControlDBService dgGeneralDBService = null;

    public static FlowControlDBService initialise() {
        if (dgGeneralDBService == null) {
            dgGeneralDBService = new FlowControlDBService();
        }
        return dgGeneralDBService;
    }

    private FlowControlDBService() {
        if (serviceLogic == null) {
            serviceLogic = new SqlResource();
        }
    }

    public void getFlowReferenceData(SvcLogicContext ctx, Map<String, String> inParams, SvcLogicContext localContext)
            throws SvcLogicException {

        String fn = "DBService.getflowModelInfo";
        String whereClause = " where ACTION = $" + FlowControllerConstants.REQUEST_ACTION;

        if (StringUtils.isNotBlank(ctx.getAttribute(FlowControllerConstants.VNF_TYPE)))
            whereClause = whereClause.concat(" and VNF_TYPE = $" + FlowControllerConstants.VNF_TYPE);
        if (StringUtils.isNotBlank(ctx.getAttribute(FlowControllerConstants.ACTION_LEVEL)))
            whereClause = whereClause.concat(" and ACTION_LEVEL = $" + FlowControllerConstants.ACTION_LEVEL);

        QueryStatus status = null;
        if (serviceLogic != null& localContext != null) {
            String key = "select SEQUENCE_TYPE, CATEGORY, GENERATION_NODE, EXECUTION_NODE from "
                    + FlowControllerConstants.DB_MULTISTEP_FLOW_REFERENCE + whereClause;
            log.debug(fn + "Query String : " + key);
            status = serviceLogic.query("SQL", false, null, key, null, null, localContext);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error - while getting FlowReferenceData ");
        }
    }

    public String getEndPointByAction(String action) {
        return null;
    }

    public String getDesignTimeFlowModel(SvcLogicContext localContext) throws SvcLogicException {
        String fn = "DBService.getDesignTimeFlowModel ";
        QueryStatus status = null;
        if (serviceLogic != null& localContext != null) {
            String queryString = "select max(internal_version) as maxInternalVersion, artifact_name as artifactName from "
                    + FlowControllerConstants.DB_SDC_ARTIFACTS + " where artifact_name in (select artifact_name from "
                    + FlowControllerConstants.DB_SDC_REFERENCE + " where vnf_type= $" + FlowControllerConstants.VNF_TYPE
                    + " and  vnfc_type = $" + FlowControllerConstants.VNFC_TYPE + " and  action = $"
                    + FlowControllerConstants.REQUEST_ACTION + " and file_category =  $"
                    + FlowControllerConstants.CATEGORY + " )";

            log.debug(fn + "Query String : " + queryString);
            status = serviceLogic.query("SQL", false, null, queryString, null, null, localContext);

            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error - while getting FlowReferenceData ");

            String queryString1 = "select artifact_content from " + FlowControllerConstants.DB_SDC_ARTIFACTS
                    + " where artifact_name = $artifactName  and internal_version = $maxInternalVersion ";

            log.debug(fn + "Query String : " + queryString1);
            status = serviceLogic.query("SQL", false, null, queryString1, null, null, localContext);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error - while getting FlowReferenceData ");
        }
        return localContext != null ? localContext.getAttribute("artifact-content") : null;
    }

    public QueryStatus loadSequenceIntoDB(SvcLogicContext localContext) throws SvcLogicException {

        QueryStatus status = null;

        if (localContext != null) {
            String fn = "DBService.saveArtifacts";

            localContext.setAttribute(FlowControllerConstants.ARTIFACT_CONTENT_ESCAPED,
                    EscapeUtils.escapeSql(localContext.getAttribute(FlowControllerConstants.ARTIFACT_CONTENT)));
            log.debug("ESCAPED sequence for DB : "
                    + localContext.getAttribute(FlowControllerConstants.ARTIFACT_CONTENT_ESCAPED));

            for (Object key : localContext.getAttributeKeySet()) {
                String parmName = (String) key;
                String parmValue = localContext.getAttribute(parmName);
                log.debug(" loadSequenceIntoDB " + parmName + "=" + parmValue);
            }

            String queryString = "INSERT INTO " + FlowControllerConstants.DB_REQUEST_ARTIFACTS + " set request_id =  $"
                    + FlowControllerConstants.REQUEST_ID + " , action =  $" + FlowControllerConstants.REQUEST_ACTION
                    + " , action_level =  $" + FlowControllerConstants.ACTION_LEVEL + " , vnf_type = $"
                    + FlowControllerConstants.VNF_TYPE + " , category = $" + FlowControllerConstants.CATEGORY
                    + " , artifact_content = $" + FlowControllerConstants.ARTIFACT_CONTENT_ESCAPED
                    + " , updated_date = sysdate() ";

            log.debug(fn + "Query String : " + queryString);
            status = serviceLogic.save("SQL", false, false, queryString, null, null, localContext);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error While processing storing Artifact: "
                        + localContext.getAttribute(FlowControllerConstants.ARTIFACT_NAME));
        }
        return status;

    }

    public void populateModuleAndRPC(Transaction transaction, String vnf_type) throws Exception {
        String fn = "FlowControlDBService.populateModuleAndRPC ";
        QueryStatus status = null;
        SvcLogicContext context = new SvcLogicContext();
        String protocolType = null;

        protocolType = getProtocolType(transaction, vnf_type, fn, context, protocolType);

        String key = "select execution_type, execution_module, execution_rpc from "
                + FlowControllerConstants.DB_PROCESS_FLOW_REFERENCE + " where action = '" + transaction.getAction()
                + "'" + " and action_level = '" + transaction.getActionLevel() + "'" + " and protocol = '"
                + protocolType + "'";

        log.debug(fn + "Query String : " + key);
        status = serviceLogic.query("SQL", false, null, key, null, null, context);
        if (status.toString().equals("FAILURE"))
            throw new SvcLogicException("Error - while getting FlowReferenceData ");

        transaction.setExecutionModule(context.getAttribute(FlowControllerConstants.EXECUTTION_MODULE));
        transaction.setExecutionRPC(context.getAttribute(FlowControllerConstants.EXECUTION_RPC));
        transaction.setExecutionType(context.getAttribute(FlowControllerConstants.EXECUTION_TYPE));

    }

    private String getProtocolType(Transaction transaction, String vnf_type, String fn, SvcLogicContext context,
            String protocolType) throws Exception {
        QueryStatus status;
        String protocolQuery;
        int protocolCount;
        protocolQuery = "select count(protocol) from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
                + " where action = '" + transaction.getAction() + "'" + " and action_level = '"
                + transaction.getActionLevel() + "'";

        log.debug(fn + "Query String : " + protocolQuery);
        status = serviceLogic.query("SQL", false, null, protocolQuery, null, null, context);
        if (status.toString().equals("FAILURE"))
            throw new SvcLogicException("Error - while getting FlowReferenceData ");

        log.debug(" Protocol Count " + context.getAttribute("count(protocol)"));
        protocolCount = Integer.parseInt(context.getAttribute("count(protocol)"));

        if (protocolCount == 1) {
            protocolQuery = "select protocol from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
                    + " where action = '" + transaction.getAction() + "'" + " and action_level = '"
                    + transaction.getActionLevel() + "'";

            log.debug(fn + "Query String : " + protocolQuery);
            status = serviceLogic.query("SQL", false, null, protocolQuery, null, null, context);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error - while getting FlowReferenceData ");
            protocolType = context.getAttribute("protocol");
        }
            else  {
                protocolQuery = "select count(protocol) from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
                        + " where action = '" + transaction.getAction() + "'" + " and action_level = '"
                        + transaction.getActionLevel() + "'" + " and vnf_type = '" + vnf_type + "'";

                log.debug(fn + "Query String : " + protocolQuery);
                status = serviceLogic.query("SQL", false, null, protocolQuery, null, null, context);
                if (status.toString().equals("FAILURE"))
                    throw new SvcLogicException("Error - while getting FlowReferenceData ");

                log.debug(" Protocol Count " + context.getAttribute("count(protocol)"));
                protocolCount = Integer.parseInt(context.getAttribute("count(protocol)"));
                if(protocolCount > 1){
                    throw new Exception("Got more than 2 values..");
                }else if(protocolCount == 1){
                    protocolQuery = "select protocol from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
                            + " where action = '" + transaction.getAction() + "'" + " and action_level = '"
                            + transaction.getActionLevel() + "'"+ " and vnf_type = '" + vnf_type + "'";
                    log.debug(fn + "Query String : " + protocolQuery);
                    status = serviceLogic.query("SQL", false, null, protocolQuery, null, null, context);
                    if (status.toString().equals("FAILURE"))
                        throw new SvcLogicException("Error - while getting FlowReferenceData ");
                    protocolType = context.getAttribute("protocol");
                }
            }
        return protocolType;
    }

    public String getDependencyInfo(SvcLogicContext localContext) throws SvcLogicException {
        String fn = "DBService.getDependencyInfo ";
        QueryStatus status = null;
        if (serviceLogic != null& localContext != null) {
            String queryString = "select max(internal_version) as maxInternalVersion, artifact_name as artifactName from "
                    + FlowControllerConstants.DB_SDC_ARTIFACTS + " where artifact_name in (select artifact_name from "
                    + FlowControllerConstants.DB_SDC_REFERENCE + " where vnf_type= $" + FlowControllerConstants.VNF_TYPE
                    + " and file_category = '" + FlowControllerConstants.DEPENDENCYMODEL + "' )";

            log.debug(fn + "Query String : " + queryString);
            status = serviceLogic.query("SQL", false, null, queryString, null, null, localContext);

            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error - while getting dependencydata ");

            String queryString1 = "select artifact_content from " + FlowControllerConstants.DB_SDC_ARTIFACTS
                    + " where artifact_name = $artifactName  and internal_version = $maxInternalVersion ";

            log.debug(fn + "Query String : " + queryString1);
            status = serviceLogic.query("SQL", false, null, queryString1, null, null, localContext);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error - while getting dependencyData ");
        }

        return localContext != null ? localContext.getAttribute("artifact-content") : null;

    }

    public String getCapabilitiesData(SvcLogicContext localContext) throws SvcLogicException {
        String fn = "DBService.getCapabilitiesData ";
        QueryStatus status = null;
        if (serviceLogic != null& localContext != null) {
            String queryString = "select max(internal_version) as maxInternalVersion, artifact_name as artifactName from "
                    + FlowControllerConstants.DB_SDC_ARTIFACTS + " where artifact_name in (select artifact_name from "
                    + FlowControllerConstants.DB_SDC_REFERENCE + " where vnf_type= $" + FlowControllerConstants.VNF_TYPE
                    + " and file_category = '" + FlowControllerConstants.CAPABILITY + "' )";

            log.info(fn + "Query String : " + queryString);
            status = serviceLogic.query("SQL", false, null, queryString, null, null, localContext);

            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error - while getting capabilitiesData ");

            String queryString1 = "select artifact_content from " + FlowControllerConstants.DB_SDC_ARTIFACTS
                    + " where artifact_name = $artifactName  and internal_version = $maxInternalVersion ";

            log.debug(fn + "Query String : " + queryString1);
            status = serviceLogic.query("SQL", false, null, queryString1, null, null, localContext);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error - while getting capabilitiesData ");
        }
        return localContext != null ? localContext.getAttribute("artifact-content") : null;
    }
}
