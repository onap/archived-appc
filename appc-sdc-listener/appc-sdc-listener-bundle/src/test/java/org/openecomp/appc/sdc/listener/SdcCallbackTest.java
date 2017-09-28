/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * 
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.sdc.listener;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.appc.sdc.artifacts.helper.ArtifactStorageService;
import org.openecomp.appc.sdc.artifacts.helper.DependencyModelGenerator;
import org.openecomp.appc.sdc.artifacts.impl.ArtifactProcessorFactory;
import org.openecomp.appc.sdc.artifacts.impl.ToscaCsarArtifactProcessor;
import org.openecomp.appc.sdc.artifacts.object.SDCArtifact;
import org.openecomp.appc.sdc.artifacts.object.SDCReference;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.consumer.INotificationCallback;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.sdc.impl.DistributionClientDownloadResultImpl;
import org.openecomp.sdc.utils.DistributionActionResultEnum;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Matchers.anyObject;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IDistributionClient.class,
        EventSender.class,
        ArtifactStorageService.class,
        ToscaCsarArtifactProcessor.class,
        ArtifactProcessorFactory.class,
        DependencyModelGenerator.class})
public class SdcCallbackTest {

    private INotificationCallback sdcCallback;
    private ArtifactStorageService storageService;
    private ToscaCsarArtifactProcessor artifactProcessor;
    private String resourceContent;


    @Before
    public void setup() throws Exception {
        IDistributionClient client = PowerMockito.mock(IDistributionClient.class);
        EventSender eventSender = PowerMockito.mock(EventSender.class);
        sdcCallback = new SdcCallback(null, client);
        resourceContent=readInput("/output/resource-ResourceAppc-template.yml").replaceAll(System.lineSeparator(),"");
        artifactProcessor = Mockito.spy(new ToscaCsarArtifactProcessor(client, eventSender, getNotificationData(), getResources().get(0)
                , getServiceArtifacts().get(0), null));
        storageService = PowerMockito.mock(ArtifactStorageService.class);
        Whitebox.setInternalState(artifactProcessor,"artifactStorageService", storageService);
        DependencyModelGenerator dependencyModelGeneratorMock=PowerMockito.mock(DependencyModelGenerator.class);
        PowerMockito.when(dependencyModelGeneratorMock.getDependencyModel(Matchers.anyString(),Matchers.anyString()))
                .thenReturn("Dependency_model");
        Whitebox.setInternalState(artifactProcessor,"dependencyModelGenerator",dependencyModelGeneratorMock);

        PowerMockito.doCallRealMethod().when(artifactProcessor).processArtifact(anyObject());
        PowerMockito.doCallRealMethod().when(artifactProcessor).run();

        //PowerMockito.mockStatic(ArtifactProcessorFactory.class);
        ArtifactProcessorFactory artifactProcessorFactory=PowerMockito.mock(ArtifactProcessorFactory.class);
        PowerMockito.when(artifactProcessorFactory.getArtifactProcessor(anyObject(), anyObject(),
                anyObject(), anyObject(),
                anyObject(), anyObject())).thenReturn(artifactProcessor);

        Whitebox.setInternalState(sdcCallback,"eventSender", eventSender);
        PowerMockito.doReturn(readDownloadResult()).when(client).download(anyObject());
        PowerMockito.doReturn(null).when(client).sendDownloadStatus(anyObject());

        PowerMockito.doReturn(null).when(storageService).retrieveSDCArtifact(Matchers.anyString(),
                Matchers.anyString(),Matchers.anyString());

        PowerMockito.doAnswer(invocationOnMock -> {
            System.out.print(invocationOnMock.getArguments()[0].toString());
            return null;
        }).when(storageService).storeSDCArtifact(anyObject());
    }

    private IDistributionClientDownloadResult readDownloadResult() throws IOException, URISyntaxException {
        DistributionClientDownloadResultImpl downloadResult = new DistributionClientDownloadResultImpl
                (DistributionActionResultEnum.SUCCESS,"Download success");
        File file = new File(this.getClass().getResource("/csar/service-ServiceAppc-csar.csar").toURI());

        byte[] bFile = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(bFile);
        fileInputStream.close();

        downloadResult.setArtifactPayload(bFile);
        return downloadResult;
    }


    @Test
    public void testSDCListener() throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        INotificationData notificationData = getNotificationData();
        sdcCallback.activateCallback(notificationData);
        pause();
    }

    @Test
    public void testArtifacts() throws Exception {
        PowerMockito.doAnswer(invocationOnMock -> {
            SDCArtifact artifact =(SDCArtifact)invocationOnMock.getArguments()[0];
            SDCReference reference=(SDCReference)invocationOnMock.getArguments()[1];
            Assert.assertEquals("abcd-efgh-ijkl",artifact.getArtifactUUID());
            Assert.assertEquals("Resource-APPC",reference.getVnfType());
            Assert.assertEquals(resourceContent.trim(),artifact.getArtifactContent().replaceAll(System.lineSeparator(),""));
            return null;
        }).doAnswer(invocation -> {
            SDCArtifact artifact =(SDCArtifact)invocation.getArguments()[0];
            SDCReference reference=(SDCReference)invocation.getArguments()[1];
            Assert.assertEquals("Resource-APPC",reference.getVnfType());
            Assert.assertEquals("tosca_dependency_model",reference.getFileCategory());
            Assert.assertEquals("Dependency_model",artifact.getArtifactContent());
            Assert.assertEquals("Resource-APPC",artifact.getResourceName());
            return null;
        }).when(storageService).storeSDCArtifactWithReference(anyObject(),anyObject());

        artifactProcessor.processArtifact(readDownloadResult());
    }

    private void pause(){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
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

    private INotificationData getNotificationData() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {

        INotificationData notificationData = (INotificationData)getObject("org.openecomp.sdc.impl.NotificationDataImpl");

        List<IArtifactInfo> serviceArtifacts = getServiceArtifacts();

        invokeMethod(notificationData, "setServiceArtifacts", serviceArtifacts);
        return notificationData;
    }

    private List<IResourceInstance> getResources() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
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

    private List<IArtifactInfo> getServiceArtifacts() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<IArtifactInfo> serviceArtifacts = new ArrayList<>();
        IArtifactInfo artifactInfo = (IArtifactInfo)getObject("org.openecomp.sdc.impl.ArtifactInfoImpl");
        invokeMethod(artifactInfo,"setArtifactType","TOSCA_CSAR");
        invokeMethod(artifactInfo,"setArtifactUUID","abcd-efgh-ijkl");
        serviceArtifacts.add(artifactInfo);
        return serviceArtifacts;
    }
}
