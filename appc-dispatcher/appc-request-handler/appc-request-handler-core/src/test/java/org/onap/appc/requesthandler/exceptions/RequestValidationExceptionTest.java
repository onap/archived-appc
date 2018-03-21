package org.onap.appc.requesthandler.exceptions;

import static org.junit.Assert.*;
import org.junit.Test;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;

public class RequestValidationExceptionTest {
    @Test
    public final void testRequestValidationExceptionWithParams() {
        String message = "tesingMessage";
        String command = "tesingCommand";
        Params p = new Params().addParam("actionName", command);
        RequestValidationException rve = new RequestValidationException(message, LCMCommandStatus.MISSING_MANDATORY_PARAMETER, p);
        assertNotNull(rve.getParams());
        assertNotNull(rve.getMessage());
    }
}
