package org.openecomp.appc.sdc.artifacts.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.appc.sdc.artifacts.object.SDCArtifact;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.powermock.api.mockito.PowerMockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestArtifactProcessor {

    AbstractArtifactProcessor abstractArtifactProcessor;

    @Before
    public void setup() throws Exception{
        IDistributionClient client =  PowerMockito.mock(IDistributionClient.class);
        EventSender eventSender = PowerMockito.mock(EventSender.class);
        abstractArtifactProcessor = Mockito.spy(new ToscaCsarArtifactProcessor(client,eventSender,getNotificationData(),getResources().get(0)
                ,getServiceArtifacts().get(0),null));
    }

    @Test
    public void testGetArtifactObject(){
        SDCArtifact artifact=abstractArtifactProcessor.getArtifactObject("test");
        Assert.assertEquals("abcd-efgh-ijkl",artifact.getArtifactUUID());
        Assert.assertEquals("VF_LICENSE",artifact.getArtifactType());
        Assert.assertEquals("Vnf",artifact.getResourceName());
        Assert.assertEquals("1.0",artifact.getResourceVersion());
        Assert.assertEquals("test",artifact.getArtifactContent());
    }

    @Test
    public void testFactoryForLicense() throws Exception{
        IDistributionClient client =  PowerMockito.mock(IDistributionClient.class);
        EventSender eventSender = PowerMockito.mock(EventSender.class);
        ArtifactProcessorFactory factory=new ArtifactProcessorFactory();
        Assert.assertTrue(factory.getArtifactProcessor(client,eventSender,getNotificationData(),getResources().get(0)
                ,getServiceArtifacts().get(0),null) instanceof LicenseArtifactProcessor);
    }

    @Test
    public void testFactoryForConfig() throws Exception{
        IDistributionClient client =  PowerMockito.mock(IDistributionClient.class);
        EventSender eventSender = PowerMockito.mock(EventSender.class);
        ArtifactProcessorFactory factory=new ArtifactProcessorFactory();
        Assert.assertTrue(factory.getArtifactProcessor(client,eventSender,getNotificationData(),getResources().get(0)
                ,getServiceArtifactsForConfig().get(0),null) instanceof ConfigArtifactProcessor);
    }

    private INotificationData getNotificationData() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        INotificationData notificationData = (INotificationData)getObject("org.openecomp.sdc.impl.NotificationDataImpl");

        List<IArtifactInfo> serviceArtifacts = getServiceArtifacts();

        invokeMethod(notificationData, "setServiceArtifacts", serviceArtifacts);
        return notificationData;
    }

    private List<IResourceInstance> getResources() throws ClassNotFoundException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        List<IResourceInstance> resources = new ArrayList<>();
        IResourceInstance resource = (IResourceInstance)getObject("org.openecomp.sdc.impl.JsonContainerResourceInstance");

        List<IArtifactInfo> serviceArtifacts = getServiceArtifacts();
        invokeMethod(resource,"setArtifacts",serviceArtifacts);
        invokeMethod(resource,"setResourceName","Vnf");
        invokeMethod(resource,"setResourceVersion","1.0");

        resources.add(resource);
        return resources;
    }

    private void invokeMethod(Object object, String methodName,Object... arguments) throws IllegalAccessException, InvocationTargetException {
        Method[] methods = object.getClass().getDeclaredMethods();
        for(Method method:methods){
            if(methodName.equalsIgnoreCase(method.getName())){
                method.setAccessible(true);
                method.invoke(object,arguments);
            }
        }
    }

    private Object getObject(String fqcn) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor constructor = Class.forName(fqcn).getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private List<IArtifactInfo> getServiceArtifacts() throws ClassNotFoundException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        List<IArtifactInfo> serviceArtifacts = new ArrayList<>();
        IArtifactInfo artifactInfo = (IArtifactInfo)getObject("org.openecomp.sdc.impl.ArtifactInfoImpl");
        invokeMethod(artifactInfo,"setArtifactType","VF_LICENSE");
        invokeMethod(artifactInfo,"setArtifactUUID","abcd-efgh-ijkl");
        serviceArtifacts.add(artifactInfo);
        return serviceArtifacts;
    }

    private List<IArtifactInfo> getServiceArtifactsForConfig() throws ClassNotFoundException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        List<IArtifactInfo> serviceArtifacts = new ArrayList<>();
        IArtifactInfo artifactInfo = (IArtifactInfo)getObject("org.openecomp.sdc.impl.ArtifactInfoImpl");
        invokeMethod(artifactInfo,"setArtifactType","APPC_CONFIG");
        invokeMethod(artifactInfo,"setArtifactUUID","abcd-efgh-ijkl");
        serviceArtifacts.add(artifactInfo);
        return serviceArtifacts;
    }

}
