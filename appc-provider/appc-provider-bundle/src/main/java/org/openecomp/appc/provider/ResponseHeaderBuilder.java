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

package org.openecomp.appc.provider;

import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.TIMESTAMP;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.common.response.header.CommonResponseHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.common.response.header.CommonResponseHeaderBuilder;
import org.openecomp.appc.util.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * Builds the responses from the APP-C services according to the YANG domainmodel
 *
 * @since Nov 16, 2015
 * @version $Id$
 */
public class ResponseHeaderBuilder {

    /**
     * The date/time formatter to format timestamps.
     */
    @SuppressWarnings("nls")
    public static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
    public static final DateFormat ZULU_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");

    /**
     * Private default constructor prevents instantiation
     */
    private ResponseHeaderBuilder() {
    }

    /**
     * This method builds the common response header and returns it to the caller for integration into the response
     *
     * @param success
     *            True or false indicating the outcome of the operation. True indicates that the operation was
     *            successful, false indicates it failed.
     * @param requestId
     *            The original request id for the service
     * @param reason
     *            The reason for the failure if the success flag is false. If success is true, the reason is not used.
     * @param duration
     *            The duration of the request processing
     * @return The common response header to be returned to the caller.
     */
    @SuppressWarnings("nls")
    public static CommonResponseHeader buildHeader(Boolean success, String requestId, String reason, long duration) {
        CommonResponseHeaderBuilder builder = new CommonResponseHeaderBuilder();

        TIMESTAMP timestamp = new TIMESTAMP(FORMATTER.format(Time.utcDate()));
        builder.setServiceRequestId(requestId);
        builder.setCompleted(timestamp);
        builder.setDuration(duration);
        builder.setSuccess(success);
        
        if (success.equals(Boolean.TRUE)) {
            builder.setReason("Success");
        } else {
            builder.setReason(reason);
        }

        return builder.build();
    }


}
