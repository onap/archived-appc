package org.onap.appc.design.services.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ArtifactHandlerInternalExceptionTest {

    @Test
    public void testException(){
        ArtifactHandlerInternalException exception = new ArtifactHandlerInternalException("ArtifactHandlerInternalException");
        assertThat(exception.getMessage(), is("ArtifactHandlerInternalException"));
    }

}