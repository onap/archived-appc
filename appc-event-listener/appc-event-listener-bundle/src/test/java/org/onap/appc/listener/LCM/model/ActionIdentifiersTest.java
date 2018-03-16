package org.onap.appc.listener.LCM.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ActionIdentifiersTest {

    private ActionIdentifiers actionIdentifiers;


    @Before
    public void setup(){
        actionIdentifiers = new ActionIdentifiers();
    }

    @Test
    public void should_set_properties(){

        actionIdentifiers.setServiceInstanceId("test-instance-id");
        actionIdentifiers.setVnfID("test-vnf-id");
        actionIdentifiers.setVnfcName("test-name");
        actionIdentifiers.setVserverId("test-vserver-id");


        assertEquals("test-instance-id", actionIdentifiers.getServiceInstanceId());
        assertEquals("test-vnf-id", actionIdentifiers.getVnfID());
        assertEquals("test-name", actionIdentifiers.getVnfcName());
        assertEquals("test-vserver-id", actionIdentifiers.getVserverId());
    }

    @Test
    public void should_initialize_parameters_from_constructor(){

        actionIdentifiers.setServiceInstanceId("test-instance-id");
        actionIdentifiers.setVnfID("test-vnf-id");
        actionIdentifiers.setVnfcName("test-name");
        actionIdentifiers.setVserverId("test-vserver-id");

        ActionIdentifiers testObject = new ActionIdentifiers(actionIdentifiers);

        assertEquals("test-instance-id", testObject.getServiceInstanceId());
        assertEquals("test-vnf-id",  testObject.getVnfID());
        assertEquals("test-name",  testObject.getVnfcName());
        assertEquals("test-vserver-id",  testObject.getVserverId());
    }
}
