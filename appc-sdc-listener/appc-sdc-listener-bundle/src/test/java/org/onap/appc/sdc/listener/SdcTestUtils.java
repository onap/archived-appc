/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.sdc.listener;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.powermock.api.mockito.PowerMockito;

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

    @Test
    public void testParseResponse() throws Exception {
        Util.parseResponse(readInput("/output/TestUtilResponse.json"));
    }

    @Test
    public void testBuildDistributionStatusMessage() throws Exception {
        Util.buildDistributionStatusMessage
        (getClient(), getNotificationData(), getServiceArtifact(),
                DistributionStatusEnum.DOWNLOAD_OK);
    }

    @Test
    public void testGetTimestamp() {
        IDistributionStatusMessage distStatusMsg = null;
        try {
            distStatusMsg = Util.buildDistributionStatusMessage
                (getClient(), getNotificationData(), getServiceArtifact(),
                DistributionStatusEnum.DOWNLOAD_OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNotEquals(0, distStatusMsg.getTimestamp());
    
    }

    @Test
    public void testGetStatus() {
        IDistributionStatusMessage distStatusMsg = null;
        try {
            distStatusMsg = Util.buildDistributionStatusMessage
                (getClient(), getNotificationData(), getServiceArtifact(),
                DistributionStatusEnum.DOWNLOAD_OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNotEquals(DistributionStatusEnum.DOWNLOAD_OK, distStatusMsg.getStatus());
    
    }

    private INotificationData getNotificationData() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException {

        INotificationData notificationData = (INotificationData)getObject("org.onap.sdc.impl.NotificationDataImpl");

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
        IResourceInstance resource = (IResourceInstance)getObject("org.onap.sdc.impl.JsonContainerResourceInstance");

        List<IArtifactInfo> serviceArtifacts = getServiceArtifacts();
        invokeMethod(resource,"setArtifacts",serviceArtifacts);
        invokeMethod(resource,"setResourceName","Vnf");
        invokeMethod(resource,"setResourceVersion","1.0");
        invokeMethod(resource,"setResourceUUID","Resource UUID");

        return resource;
    }

    private void invokeMethod(Object object, String methodName,Object... arguments) throws IllegalAccessException, 
        InvocationTargetException {
        Method[] methods = object.getClass().getDeclaredMethods();
        for(Method method:methods){
            if(methodName.equalsIgnoreCase(method.getName())){
                method.setAccessible(true);
                method.invoke(object,arguments);
            }
        }
    }

    private Object getObject(String fqcn) throws ClassNotFoundException, InstantiationException, 
        IllegalAccessException, InvocationTargetException {
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
        IArtifactInfo artifactInfo = (IArtifactInfo)getObject("org.onap.sdc.impl.ArtifactInfoImpl");
        invokeMethod(artifactInfo,"setArtifactType","TOSCA_CSAR");
        invokeMethod(artifactInfo,"setArtifactUUID","abcd-efgh-ijkl");
        serviceArtifacts.add(artifactInfo);
        return serviceArtifacts;
    }

    private IArtifactInfo getServiceArtifact() throws ClassNotFoundException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        IArtifactInfo artifactInfo = (IArtifactInfo)getObject("org.onap.sdc.impl.ArtifactInfoImpl");
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

    private IDistributionClient getClient()  {
        return PowerMockito.mock(IDistributionClient.class);
    }
}
