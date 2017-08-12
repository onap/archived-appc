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

package org.openecomp.appc.oam.util;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.MaintenanceModeInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.RestartInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.StartInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.StopInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.common.header.common.header.Flags;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.status.Status;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.exceptions.InvalidInputException;
import org.openecomp.appc.exceptions.InvalidStateException;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.lifecyclemanager.objects.LifecycleException;
import org.openecomp.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.openecomp.appc.oam.AppcOam;
import org.openecomp.appc.oam.messageadapter.MessageAdapter;
import org.openecomp.appc.oam.messageadapter.OAMContext;
import org.openecomp.appc.statemachine.impl.readers.AppcOamMetaDataReader;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Utility class provides general operational helps.
 */
@SuppressWarnings("unchecked")
public class OperationHelper {
    final String MISSING_COMMON_HEADER_MESSAGE = "Missing common header";
    final String MISSING_FIELD_MESSAGE = "Common header must have both originatorId and requestId";
    final String NOT_SUPPORT_FLAG = "Flags is not supported by this operation";
    final String NO_SERVICE_REF_FORMAT = "Using the BundleContext failed to get service reference for %s";

    private final EELFLogger logger = EELFManager.getInstance().getLogger(OperationHelper.class);
    private LifecycleManager lifecycleMgr;
    private MessageAdapter messageAdapter;

    public OperationHelper() {
        // do nothing
    }

    /**
     * This method is used to validate OAM REST API input due to the following ODL bugs results no validation : </tt>
     * <p>  - <a href="https://bugs.opendaylight.org/show_bug.cgi?id=8088">
     *       Bug 8088 - Mandatory attributes in RPC input are not honoured</a>
     * <p>  - <a href="https://bugs.opendaylight.org/show_bug.cgi?id=5830">
     *       Bug 5830 - Mandatory leaf enforcement is not correct with presence container</a>
     *
     * @param inputObject object from the OAM REST API input object
     * @throws InvalidInputException is thrown when the commonHeader is invalid
     */
    public void isInputValid(final Object inputObject) throws InvalidInputException {
        CommonHeader commonHeader = getCommonHeader(inputObject);
        if (commonHeader == null) {
            throw new InvalidInputException(MISSING_COMMON_HEADER_MESSAGE);
        }

        if (commonHeader.getOriginatorId() == null
                || commonHeader.getRequestId() == null) {
            throw new InvalidInputException(MISSING_FIELD_MESSAGE);
        }

        // check Flags
        if (inputObject instanceof MaintenanceModeInput
                && commonHeader.getFlags() != null) {
            throw new InvalidInputException(NOT_SUPPORT_FLAG);
        }
    }

    /**
     * Get commonHead of the inputObject (expecting the inputObject of OAM REST API)
     * @param inputObject the OAM REST API input object
     * @return CommonHeader of the inputObject. If the inputObject is not a OAM REST API input, null is returned.
     */
    public CommonHeader getCommonHeader(final Object inputObject) {
        if (inputObject instanceof StartInput) {
            return ((StartInput)inputObject).getCommonHeader();
        }
        if (inputObject instanceof StopInput) {
            return ((StopInput)inputObject).getCommonHeader();
        }
        if (inputObject instanceof MaintenanceModeInput) {
            return ((MaintenanceModeInput)inputObject).getCommonHeader();
        }
        if (inputObject instanceof RestartInput) {
            return ((RestartInput)inputObject).getCommonHeader();
        }
        return null;
    }

    public Integer getParamRequestTimeout(final Object inputObject) {
        if (inputObject instanceof MaintenanceModeInput) {
            // maintanence mode, we do not support request timeout
            return 0;
        }

        CommonHeader commonHeader = getCommonHeader(inputObject);
        if (commonHeader == null) {
            return 0;
        }

        Flags inputFlags = commonHeader.getFlags();
        if (inputFlags == null) {
            return null;
        }
        return inputFlags.getRequestTimeout();
    }
    /**
     * Get service instance using bundle context.
     *
     * @param _class of the expected service instance
     * @param <T> of the expected service instance
     * @return service instance of the expected
     * @throws APPCException when cannot find service reference or service isntance
     */
    public <T> T getService(Class<T> _class) throws APPCException {
        BundleContext bctx = FrameworkUtil.getBundle(_class).getBundleContext();
        if (bctx != null) {
            ServiceReference sref = bctx.getServiceReference(_class.getName());
            if (sref != null) {
                if (logger.isTraceEnabled()) {
                    logger.debug("Using the BundleContext got the service reference for " + _class.getName());
                }
                return (T) bctx.getService(sref);
            }
        }

        throw new APPCException(String.format(NO_SERVICE_REF_FORMAT, _class.getName()));
    }

    /**
     * Get next valid state from life cycle manager.
     *
     * @param operation of the AppcOperation for the state changes
     * @param currentState of AppcOamStates
     * @return next AppcOamStates based on the currentState and operation
     * @throws APPCException If life cycle manager instance cannot be retrieved
     * @throws InvalidStateException when the operation is not supported on the currentState
     */
    public AppcOamStates getNextState(AppcOamMetaDataReader.AppcOperation operation, AppcOamStates currentState)
            throws APPCException, InvalidStateException {
        if (lifecycleMgr == null) {
            lifecycleMgr = getService(LifecycleManager.class);
        }

        try {
            String nextState = lifecycleMgr.getNextState("APPC", currentState.name(), operation.toString());
            if (nextState != null) {
                return AppcOamStates.valueOf(nextState);
            }
        } catch (LifecycleException |NoTransitionDefinedException ex) {
            logger.error("Invalid next state based on the current state and attempted Operation " + ex.getMessage());
        }

        throw new InvalidStateException(String.format(AppcOam.INVALID_STATE_MESSAGE_FORMAT, operation, "APPC", currentState));
    }

    /**
     * Post notification through MessageAdapter.
     *
     * @param rpc of REST API RPC
     * @param commonHeader of REST API request common header
     * @param status of the to be post message
     */
    public void sendNotificationMessage(AppcOam.RPC rpc, CommonHeader commonHeader, Status status) {
        if (messageAdapter == null) {
            messageAdapter = new MessageAdapter();
            messageAdapter.init();

        }

        OAMContext oamContext = new OAMContext();
        oamContext.setRpcName(rpc);
        oamContext.setCommonHeader(commonHeader);
        oamContext.setStatus(status);
        messageAdapter.post(oamContext);
    }
}
