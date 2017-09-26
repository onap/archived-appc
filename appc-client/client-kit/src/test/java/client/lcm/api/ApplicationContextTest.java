package client.lcm.api;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.client.lcm.api.ApplicationContext;


public class ApplicationContextTest {
    @Test
    public void getMechIDTest(){
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.setMechID("MechId");
        Assert.assertNotNull(applicationContext.getMechID());
        Assert.assertEquals(applicationContext.getMechID(),"MechId");
    }

    @Test
    public void getApplicationIDTest(){
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.setApplicationID("applicationID");
        Assert.assertNotNull(applicationContext.getApplicationID());
        Assert.assertEquals(applicationContext.getApplicationID(),"applicationID");
    }
}
