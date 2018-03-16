package org.onap.appc.listener.LCM.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ResponseStatusTest {

    private ResponseStatus responseStatus;

    @Before
    public void setup() {
        responseStatus = new ResponseStatus();
    }

    @Test
    public void should_set_properties() {

        responseStatus.setCode(200);
        responseStatus.setValue("OK");

        assertEquals(Integer.valueOf(200), responseStatus.getCode());
        assertEquals("OK", responseStatus.getValue());
    }

    @Test
    public void should_initialize_parameters_from_constructor() {
        responseStatus = new ResponseStatus(200, "OK");

        assertEquals(Integer.valueOf(200), responseStatus.getCode());
        assertEquals("OK", responseStatus.getValue());
    }
}
