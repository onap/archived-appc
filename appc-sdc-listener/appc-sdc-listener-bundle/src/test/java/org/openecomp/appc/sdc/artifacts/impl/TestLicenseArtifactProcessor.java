package org.openecomp.appc.sdc.artifacts.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.sdc.artifacts.helper.ArtifactStorageService;
import org.openecomp.appc.sdc.artifacts.object.SDCArtifact;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
public class TestLicenseArtifactProcessor {

    private LicenseArtifactProcessor artifactProcessor;
    private ArtifactStorageService storageService;

    @Before
    public void setup() throws Exception{
        IDistributionClient client =  PowerMockito.mock(IDistributionClient.class);
        EventSender eventSender = PowerMockito.mock(EventSender.class);
        storageService = PowerMockito.mock(ArtifactStorageService.class);
        artifactProcessor = Mockito.spy(new LicenseArtifactProcessor(client,eventSender,getNotificationData(),getResources().get(0)
                ,getServiceArtifacts().get(0),null));
        Whitebox.setInternalState(artifactProcessor,"artifactStorageService", storageService);
        PowerMockito.doCallRealMethod().when(artifactProcessor).processArtifact((SDCArtifact)Matchers.anyObject());
        PowerMockito.doNothing().when(storageService).storeSDCArtifact(Matchers.anyObject());
    }

    @Test(expected = org.openecomp.appc.exceptions.APPCException.class)
    public void testProcessArtifactWithMissingData() throws APPCException {
        SDCArtifact artifact=new SDCArtifact();
        artifact.setResourceVersion("RESOURCE VERSION");
        artifact.setArtifactUUID("123-456-789");
        artifactProcessor.processArtifact(artifact);
    }
    @Test
    public void testProcessArtifact() throws APPCException {
        PowerMockito.when(storageService.retrieveSDCArtifact(anyString(),anyString(),anyString())).thenReturn(null);
        SDCArtifact artifact=new SDCArtifact();
        artifact.setResourceVersion("RESOURCE VERSION");
        artifact.setArtifactUUID("123-456-789");
        artifact.setResourceName("Resource Name");
        artifactProcessor.processArtifact(artifact);
        verify(storageService,Mockito.times(1)).storeSDCArtifact(anyObject());
    }
    @Test
    public void testProcessArtifactWithDuplicateArtifact() throws APPCException {
        SDCArtifact artifact=new SDCArtifact();
        artifact.setResourceVersion("RESOURCE VERSION");
        artifact.setArtifactUUID("123-456-789");
        artifact.setResourceName("Resource Name");
        PowerMockito.when(storageService.retrieveSDCArtifact(anyString(),anyString(),anyString())).thenReturn(artifact);
        artifactProcessor.processArtifact(artifact);
        verify(storageService,Mockito.times(0)).storeSDCArtifact(anyObject());
    }

    private INotificationData getNotificationData() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        org.openecomp.sdc.api.notification.INotificationData notificationData = (INotificationData)getObject("org.openecomp.sdc.impl.NotificationDataImpl");

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
        Constructor constructor = Arrays.asList(Class.forName(fqcn).getDeclaredConstructors())
                .stream()
                .filter(constructor1 -> constructor1.getParameterCount()==0)
                .collect(Collectors.toList())
                .get(0);
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private List<IArtifactInfo> getServiceArtifacts() throws ClassNotFoundException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        List<IArtifactInfo> serviceArtifacts = new ArrayList<>();
        IArtifactInfo artifactInfo = (IArtifactInfo)getObject("org.openecomp.sdc.impl.ArtifactInfoImpl");
        invokeMethod(artifactInfo,"setArtifactType","TOSCA_CSAR");
        invokeMethod(artifactInfo,"setArtifactUUID","abcd-efgh-ijkl");
        serviceArtifacts.add(artifactInfo);
        return serviceArtifacts;
    }
}
