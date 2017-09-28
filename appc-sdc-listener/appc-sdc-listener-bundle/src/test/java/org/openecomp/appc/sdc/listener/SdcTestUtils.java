package org.openecomp.appc.sdc.listener;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SdcTestUtils {

    @Test
    public void testToSdcStoreDocumentInput() throws Exception{
        Assert.assertEquals(readInput("/output/TestUtilResponse.json"), Util.toSdcStoreDocumentInput
                (getNotificationData(),getResources(),getServiceArtifact(),"Mock data"));
    }

    @Test
    public void testProviderResponse(){
        ProviderResponse response=new ProviderResponse(200,"Success");
        Assert.assertEquals(200,response.getStatus());
        Assert.assertEquals("Success",response.getBody());
    }

    private INotificationData getNotificationData() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        INotificationData notificationData = (INotificationData)getObject("org.openecomp.sdc.impl.NotificationDataImpl");

        List<IArtifactInfo> serviceArtifacts = getServiceArtifacts();

        invokeMethod(notificationData, "setServiceArtifacts", serviceArtifacts);
        invokeMethod(notificationData,"setServiceUUID","4564-4567-7897");
        invokeMethod(notificationData,"setDistributionID","Distribution ID Mock");
        invokeMethod(notificationData,"setServiceDescription","Service Description Mock");
        invokeMethod(notificationData,"setServiceName","Service Name Mock");

        return notificationData;
    }

    private IResourceInstance getResources() throws ClassNotFoundException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        IResourceInstance resource = (IResourceInstance)getObject("org.openecomp.sdc.impl.JsonContainerResourceInstance");

        List<IArtifactInfo> serviceArtifacts = getServiceArtifacts();
        invokeMethod(resource,"setArtifacts",serviceArtifacts);
        invokeMethod(resource,"setResourceName","Vnf");
        invokeMethod(resource,"setResourceVersion","1.0");
        invokeMethod(resource,"setResourceUUID","Resource UUID");

        return resource;
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
        invokeMethod(artifactInfo,"setArtifactType","TOSCA_CSAR");
        invokeMethod(artifactInfo,"setArtifactUUID","abcd-efgh-ijkl");
        serviceArtifacts.add(artifactInfo);
        return serviceArtifacts;
    }
    private IArtifactInfo getServiceArtifact() throws ClassNotFoundException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        IArtifactInfo artifactInfo = (IArtifactInfo)getObject("org.openecomp.sdc.impl.ArtifactInfoImpl");
        invokeMethod(artifactInfo,"setArtifactType","TOSCA_CSAR");
        invokeMethod(artifactInfo,"setArtifactUUID","abcd-efgh-ijkl");
        return artifactInfo;
    }
    private String readInput(String inputFile) throws URISyntaxException {
        File file = new File(this.getClass().getResource(inputFile).toURI());
        byte[] bFile = new byte[(int) file.length()];
        try(FileInputStream fileInputStream = new FileInputStream(file)){
            fileInputStream.read(bFile);
        } catch (Exception e){
            e.printStackTrace();
        }
        return new String(bFile);
    }
}
