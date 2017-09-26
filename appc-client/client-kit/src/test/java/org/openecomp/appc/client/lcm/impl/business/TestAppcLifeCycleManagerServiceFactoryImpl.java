package org.openecomp.appc.client.lcm.impl.business;


import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.appc.client.lcm.api.ApplicationContext;
import org.openecomp.appc.client.lcm.api.LifeCycleManagerStateful;
import org.openecomp.appc.client.lcm.exceptions.AppcClientException;
import org.openecomp.appc.client.lcm.impl.business.LCMRequestProcessor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestAppcLifeCycleManagerServiceFactoryImpl {

    AppcLifeCycleManagerServiceFactoryImpl appcLifeCycleManagerServiceFactory=new AppcLifeCycleManagerServiceFactoryImpl();

    @Ignore
    public void testCreateLifeCycleManagerStateful() throws AppcClientException{
        LifeCycleManagerStateful lifeCycleManagerStateful;
        ApplicationContext applicationContext=new ApplicationContext();
        applicationContext.setApplicationID("AppID");
        applicationContext.setMechID("mechId");
        String folder="src/test/resources/data";
        Properties properties =getProperties(folder);
        lifeCycleManagerStateful=appcLifeCycleManagerServiceFactory.createLifeCycleManagerStateful(applicationContext,properties);

        Assert.assertNotNull(lifeCycleManagerStateful);

    }

    public static Properties getProperties(String folder) {
        Properties prop = new Properties();

        InputStream conf = null;
        try {
            conf = new FileInputStream(folder + "client-simulator.properties");
        } catch (FileNotFoundException e) {

        }
        if (conf != null) {
            try {
                prop.load(conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("client-simulator.properties"));
            } catch (Exception e) {
                throw new RuntimeException("### ERROR ### - Could not load properties to test");
            }
        }
        return prop;
    }
}
