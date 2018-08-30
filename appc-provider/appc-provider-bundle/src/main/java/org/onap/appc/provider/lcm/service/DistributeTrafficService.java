package org.onap.appc.provider.lcm.service;


import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.util.JsonUtil;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DistributeTrafficInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DistributeTrafficOutputBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * Provide LCM command service for Distribute Traffic between VNFs.
 */
public class DistributeTrafficService extends AbstractBaseService {


    private static final String CONFIG_FILE_NAME_PARAMETER = "ConfigFileName";

    /**
     * Constructor
     */
    public DistributeTrafficService() {
        super(Action.DistributeTraffic);
        logger.debug("DistributeTrafficService starts");
    }

    /**
     * Process a DistributeTraffic request
     * @param input of DistributeTrafficInput from the REST API input
     * @return DistributeTrafficOutputBuilder which has the process results
     */
    public DistributeTrafficOutputBuilder process(DistributeTrafficInput input) {

        validate(input);
        if (status == null) {
            proceedAction(input);
        }

        DistributeTrafficOutputBuilder outputBuilder = new DistributeTrafficOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        return outputBuilder;
    }

    /**
     * Validate input.
     * Set Status if any error detected.
     *
     * @param input of DistributeTrafficInput from the REST API input
     */
    void validate(DistributeTrafficInput input) {
        status = validateVnfId(input.getCommonHeader(), input.getAction(), input.getActionIdentifiers());
        if (status != null) {
            return;
        }

        // validate payload
        String keyName = "payload";
        if (input.getPayload() == null) {
            status = buildStatusForParamName(LCMCommandStatus.MISSING_MANDATORY_PARAMETER, keyName);
            return;
        }
        String payloadString = input.getPayload().getValue();
        status = validateMustHaveParamValue(payloadString == null ? payloadString : payloadString.trim(), "payload");
        if (status != null) {
            return;
        }

//        try {
//            Map<String, String> payloadMap = JsonUtil.convertJsonStringToFlatMap(payloadString);
//            // ConfigFileName validation
//            final String configFileName = payloadMap.get(CONFIG_FILE_NAME_PARAMETER);
//            if (configFileName == null) {
//                return;
//            }
//
//        } catch(IOException e) {
//            logger.error(String.format("DistributeTrafficService (%s) got IOException when converting payload", rpcName), e);
//            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, e.getMessage());
//        }
    }

    void proceedAction(DistributeTrafficInput input) {
        RequestHandlerInput requestHandlerInput = getRequestHandlerInput(
                input.getCommonHeader(), input.getActionIdentifiers(), input.getPayload(), this.getClass().getName());
        if (requestHandlerInput != null) {
            executeAction(requestHandlerInput);
        }
    }

}
