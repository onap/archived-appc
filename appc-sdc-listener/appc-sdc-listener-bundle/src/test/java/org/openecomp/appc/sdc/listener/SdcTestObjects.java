package org.openecomp.appc.sdc.listener;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.sdc.artifacts.object.Vnfc;

public class SdcTestObjects {

    @Test
    public void testVnfcInstance(){
        Vnfc vnfc=new Vnfc();
        vnfc.setVnfcType("Firewall");
        vnfc.setMandatory(true);
        vnfc.setResilienceType("Active");
        Assert.assertEquals("Firewall",vnfc.getVnfcType());
        Assert.assertEquals(true,vnfc.isMandatory());
        Assert.assertEquals("Active",vnfc.getResilienceType());
    }


}
