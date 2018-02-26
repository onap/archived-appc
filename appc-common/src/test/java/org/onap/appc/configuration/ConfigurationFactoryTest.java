package org.onap.appc.configuration;

import org.junit.Assert;
import org.junit.Test;

import static org.onap.appc.configuration.ConfigurationFactory.getConfiguration;

public class ConfigurationFactoryTest {
    @Test
    public void should_returnDefaultConfiguration(){
        Configuration conf = null;

        Assert.assertTrue(getConfiguration() instanceof DefaultConfiguration);
    }
}