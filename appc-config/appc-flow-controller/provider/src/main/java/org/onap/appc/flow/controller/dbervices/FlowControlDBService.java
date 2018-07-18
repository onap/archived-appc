/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
package org.onap.appc.flow.controller.dbervices;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.utils.EscapeUtils;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

public class FlowControlDBService {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(FlowControlDBService.class);
    private static final String QUERY_STR = "Query String : ";
    private static final String FAILURE_PARAM = "FAILURE";
    private static final String GET_FLOW_REF_DATA_ERROR = "Error - while getting FlowReferenceData ";
    private static final String SELECT_AS_QUERY_STR = "select max(internal_version) as maxInternalVersion, artifact_name as artifactName from ";
    private static final String WHERE_ART_NAME_QUERY_STR = " where artifact_name in (select artifact_name from ";
    private static final String WHERE_VNF_TYPE_QUERY_STR = " where vnf_type= $";
    private static final String SELECT_ART_CONTENT_QUERY_STR = "select artifact_content from ";
    private static final String WHERE_ARTIFACT_NAME_QUERY_STR = " where artifact_name = $artifactName  and internal_version = $maxInternalVersion ";
    private static final String ARTIFACT_CONTENT_PARAM = "artifact-content";
    private static final String COUNT_PROTOCOL_PARAM = "count(protocol)";
    private static final String WHERE_ACTION_QUERY_STR = " where action = '";
    private static final String AND_ACTION_LEVEL_QUERY_STR = " and action_level = '";

    private SvcLogicResource serviceLogic;
    private static FlowControlDBService dgGeneralDBService = null;

    private FlowControlDBService() {
        if (serviceLogic == null) {
            serviceLogic = new SqlResource();
        }
    }
    
    protected FlowControlDBService(SqlResource svcLogic) {
        if (serviceLogic == null) {
            serviceLogic = svcLogic;
        }
    }

    public static FlowControlDBService initialise() {
        if (dgGeneralDBService == null) {
            dgGeneralDBService = new FlowControlDBService();
        }
        return dgGeneralDBService;
    }

    public void getFlowReferenceData(SvcLogicContext ctx, Map<String, String> inParams, SvcLogicContext localContext)
        throws SvcLogicException {

        String fn = "DBService.getflowModelInfo";
        String whereClause = " where ACTION = $" + FlowControllerConstants.REQUEST_ACTION;

        if (StringUtils.isNotBlank(ctx.getAttribute(FlowControllerConstants.VNF_TYPE))) {
            whereClause = whereClause.concat(" and VNF_TYPE = $" + FlowControllerConstants.VNF_TYPE);
        }
        if (StringUtils.isNotBlank(ctx.getAttribute(FlowControllerConstants.ACTION_LEVEL))) {
            whereClause = whereClause.concat(" and ACTION_LEVEL = $" + FlowControllerConstants.ACTION_LEVEL);
        }

        QueryStatus status;
        if (serviceLogic != null && localContext != null) {
            String key = "select SEQUENCE_TYPE, CATEGORY, GENERATION_NODE, EXECUTION_NODE from "
                + FlowControllerConstants.DB_MULTISTEP_FLOW_REFERENCE + whereClause;
            log.debug(fn + QUERY_STR + key);
            status = serviceLogic.query("SQL", false, null, key, null, null, localContext);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
            }
        }
    }

    public String getEndPointByAction(String action) {
        return null;
    }

    public String getDesignTimeFlowModel(SvcLogicContext localContext) throws SvcLogicException {
        String fn = "DBService.getDesignTimeFlowModel ";
        QueryStatus status;
        if (serviceLogic != null && localContext != null) {
            String queryString =
                SELECT_AS_QUERY_STR
                    + FlowControllerConstants.DB_SDC_ARTIFACTS + WHERE_ART_NAME_QUERY_STR
                    + FlowControllerConstants.DB_SDC_REFERENCE + WHERE_VNF_TYPE_QUERY_STR
                    + FlowControllerConstants.VNF_TYPE
                    + " and  vnfc_type = $" + FlowControllerConstants.VNFC_TYPE + " and  action = $"
                    + FlowControllerConstants.REQUEST_ACTION + " and file_category =  $"
                    + FlowControllerConstants.CATEGORY + " )";

            log.debug(fn + QUERY_STR + queryString);
            status = serviceLogic.query("SQL", false, null, queryString, null, null, localContext);

            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
            }

            String queryString1 = SELECT_ART_CONTENT_QUERY_STR + FlowControllerConstants.DB_SDC_ARTIFACTS
                + WHERE_ARTIFACT_NAME_QUERY_STR;

            log.debug(fn + QUERY_STR + queryString1);
            status = serviceLogic.query("SQL", false, null, queryString1, null, null, localContext);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
            }
        }
        return localContext != null ? localContext.getAttribute(ARTIFACT_CONTENT_PARAM) : null;
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

            log.debug(fn + QUERY_STR + queryString);
            status = serviceLogic.save("SQL", false, false, queryString, null, null, localContext);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error While processing storing Artifact: "
                    + localContext.getAttribute(FlowControllerConstants.ARTIFACT_NAME));
            }
        }
        return status;

    }

    public void populateModuleAndRPC(Transaction transaction, String vnfType) throws SvcLogicException {
        String fn = "FlowControlDBService.populateModuleAndRPC ";
        QueryStatus status;
        SvcLogicContext context = new SvcLogicContext();
        String protocolType = getProtocolType(transaction, vnfType, fn, context);

        String key = "select execution_type, execution_module, execution_rpc from "
            + FlowControllerConstants.DB_PROCESS_FLOW_REFERENCE + WHERE_ACTION_QUERY_STR + transaction.getAction()
            + "'" + AND_ACTION_LEVEL_QUERY_STR + transaction.getActionLevel() + "'" + " and protocol = '"
            + protocolType + "'";

        log.debug(fn + QUERY_STR + key);
        status = serviceLogic.query("SQL", false, null, key, null, null, context);
        if (status.toString().equals(FAILURE_PARAM)) {
            throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
        }

        transaction.setExecutionModule(context.getAttribute(FlowControllerConstants.EXECUTTION_MODULE));
        transaction.setExecutionRPC(context.getAttribute(FlowControllerConstants.EXECUTION_RPC));
        transaction.setExecutionType(context.getAttribute(FlowControllerConstants.EXECUTION_TYPE));

    }

    private String getProtocolType(Transaction transaction, String vnfType, String fn, SvcLogicContext context)
        throws SvcLogicException {
        QueryStatus status;
        String protocolQuery;
        int protocolCount;
        protocolQuery = "select count(protocol) from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
            + WHERE_ACTION_QUERY_STR + transaction.getAction() + "'" + AND_ACTION_LEVEL_QUERY_STR
            + transaction.getActionLevel() + "'";

        log.debug(fn + QUERY_STR + protocolQuery);
        status = serviceLogic.query("SQL", false, null, protocolQuery, null, null, context);
        if (status.toString().equals(FAILURE_PARAM)) {
            throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
        }

        log.debug(" Protocol Count " + context.getAttribute(COUNT_PROTOCOL_PARAM));
        protocolCount = Integer.parseInt(context.getAttribute(COUNT_PROTOCOL_PARAM));

        if (protocolCount == 1) {
            protocolQuery = "select protocol from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
                + WHERE_ACTION_QUERY_STR + transaction.getAction() + "'" + AND_ACTION_LEVEL_QUERY_STR
                + transaction.getActionLevel() + "'";

            log.debug(fn + QUERY_STR + protocolQuery);
            status = serviceLogic.query("SQL", false, null, protocolQuery, null, null, context);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
            }
            return context.getAttribute("protocol");
        } else {
            if (hasSingleProtocol(transaction, vnfType, fn, context)) {
                return context.getAttribute("protocol");
            }
        }
        return null;
    }

    private boolean hasSingleProtocol(Transaction transaction, String vnfType, String fn, SvcLogicContext context)
        throws SvcLogicException {
        String protocolQuery;
        QueryStatus status;
        int protocolCount;
        protocolQuery = "select count(protocol) from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
            + WHERE_ACTION_QUERY_STR + transaction.getAction() + "'" + AND_ACTION_LEVEL_QUERY_STR
            + transaction.getActionLevel() + "'" + " and vnf_type = '" + vnfType + "'";

        log.debug(fn + QUERY_STR + protocolQuery);
        status = serviceLogic.query("SQL", false, null, protocolQuery, null, null, context);
        if (status.toString().equals(FAILURE_PARAM)) {
            throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
        }

        log.debug(" Protocol Count " + context.getAttribute(COUNT_PROTOCOL_PARAM));
        protocolCount = Integer.parseInt(context.getAttribute(COUNT_PROTOCOL_PARAM));
        if (protocolCount > 1) {
            throw new SvcLogicException("Got more than 2 values..");
        } else if (protocolCount == 1) {
            protocolQuery = "select protocol from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
                + WHERE_ACTION_QUERY_STR + transaction.getAction() + "'" + AND_ACTION_LEVEL_QUERY_STR
                + transaction.getActionLevel() + "'" + " and vnf_type = '" + vnfType + "'";
            log.debug(fn + QUERY_STR + protocolQuery);
            status = serviceLogic.query("SQL", false, null, protocolQuery, null, null, context);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
            }
            return true;
        }
        return false;
    }

    public String getDependencyInfo(SvcLogicContext localContext) throws SvcLogicException {
        String fn = "DBService.getDependencyInfo ";
        QueryStatus status;
        if (serviceLogic != null && localContext != null) {
            String queryString =
                SELECT_AS_QUERY_STR
                    + FlowControllerConstants.DB_SDC_ARTIFACTS + WHERE_ART_NAME_QUERY_STR
                    + FlowControllerConstants.DB_SDC_REFERENCE + WHERE_VNF_TYPE_QUERY_STR
                    + FlowControllerConstants.VNF_TYPE
                    + " and file_category = '" + FlowControllerConstants.DEPENDENCYMODEL + "' )";

            log.debug(fn + QUERY_STR + queryString);
            status = serviceLogic.query("SQL", false, null, queryString, null, null, localContext);

            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error - while getting dependencydata ");
            }

            String queryString1 = SELECT_ART_CONTENT_QUERY_STR + FlowControllerConstants.DB_SDC_ARTIFACTS
                + WHERE_ARTIFACT_NAME_QUERY_STR;

            log.debug(fn + QUERY_STR + queryString1);
            status = serviceLogic.query("SQL", false, null, queryString1, null, null, localContext);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error - while getting dependencyData ");
            }
        }

        return localContext != null ? localContext.getAttribute(ARTIFACT_CONTENT_PARAM) : null;

    }

    public String getCapabilitiesData(SvcLogicContext localContext) throws SvcLogicException {
        String fn = "DBService.getCapabilitiesData ";
        QueryStatus status;
        if (serviceLogic != null && localContext != null) {
            String queryString =
                SELECT_AS_QUERY_STR
                    + FlowControllerConstants.DB_SDC_ARTIFACTS + WHERE_ART_NAME_QUERY_STR
                    + FlowControllerConstants.DB_SDC_REFERENCE + WHERE_VNF_TYPE_QUERY_STR
                    + FlowControllerConstants.VNF_TYPE
                    + " and file_category = '" + FlowControllerConstants.CAPABILITY + "' )";

            log.info(fn + QUERY_STR + queryString);
            status = serviceLogic.query("SQL", false, null, queryString, null, null, localContext);

            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error - while getting capabilitiesData ");
            }

            String queryString1 = SELECT_ART_CONTENT_QUERY_STR + FlowControllerConstants.DB_SDC_ARTIFACTS
                + WHERE_ARTIFACT_NAME_QUERY_STR;

            log.debug(fn + QUERY_STR + queryString1);
            status = serviceLogic.query("SQL", false, null, queryString1, null, null, localContext);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error - while getting capabilitiesData ");
            }
        }
        return localContext != null ? localContext.getAttribute(ARTIFACT_CONTENT_PARAM) : null;
    }
}
