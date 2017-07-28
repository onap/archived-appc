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


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.appc.sdc.artifacts.helper.ArtifactStorageService;
import org.openecomp.appc.sdc.artifacts.impl.ArtifactProcessorFactory;
import org.openecomp.appc.sdc.artifacts.impl.ToscaCsarArtifactProcessor;
import org.openecomp.appc.sdc.artifacts.object.SDCArtifact;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.consumer.IDistributionStatusMessage;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IDistributionClient.class,
                EventSender.class,
                ArtifactStorageService.class,
                ToscaCsarArtifactProcessor.class,
                ArtifactProcessorFactory.class})
public class TestAsdcListener {

    IDistributionClient client;
    private EventSender eventSender;
    private INotificationCallback asdcCallback;
    private ArtifactStorageService storageService;
    private ToscaCsarArtifactProcessor artifactProcessor;


    @Before
    public void setup() throws Exception {
        client =  PowerMockito.mock(IDistributionClient.class);
        eventSender = PowerMockito.mock(EventSender.class);
        asdcCallback = new AsdcCallback(null,client);

        artifactProcessor = Mockito.spy(new ToscaCsarArtifactProcessor(client,eventSender,getNotificationData(),getResources().get(0)
                ,getServiceArtifacts().get(0),null));
        storageService = PowerMockito.mock(ArtifactStorageService.class);
        Whitebox.setInternalState(artifactProcessor,"artifactStorageService", storageService);

        PowerMockito.doCallRealMethod().when(artifactProcessor).processArtifact((IDistributionClientDownloadResult) Matchers.anyObject());
        PowerMockito.doCallRealMethod().when(artifactProcessor).run();


        PowerMockito.mockStatic(ArtifactProcessorFactory.class);
        PowerMockito.when(ArtifactProcessorFactory.getArtifactProcessor((IDistributionClient)Matchers.anyObject(), (EventSender)Matchers.anyObject(),
                (INotificationData)Matchers.anyObject(), (IResourceInstance)Matchers.anyObject(),
                (IArtifactInfo)Matchers.anyObject(), (URI)Matchers.anyObject())).thenReturn(artifactProcessor);

        Whitebox.setInternalState(asdcCallback,"eventSender", eventSender);
        PowerMockito.doReturn(readDownloadResult()).when(client).download((IArtifactInfo) Matchers.anyObject());
        PowerMockito.doReturn(null).when(client).sendDownloadStatus((IDistributionStatusMessage) Matchers.anyObject());

        PowerMockito.doReturn(null).when(storageService).retrieveSDCArtifact(Matchers.anyString(),Matchers.anyString(),Matchers.anyString());

        PowerMockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                System.out.print(invocationOnMock.getArguments()[0].toString());
                return null;
            }
        }).when(storageService).storeASDCArtifact((SDCArtifact)Matchers.anyObject());
    }

    private IDistributionClientDownloadResult readDownloadResult() throws IOException, URISyntaxException {
        DistributionClientDownloadResultImpl downloadResult = new DistributionClientDownloadResultImpl(DistributionActionResultEnum.SUCCESS,"Download success");
        File file = new File(this.getClass().getResource("/csar/service-ServiceAppc-csar.csar").toURI());

        byte[] bFile = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(bFile);
        fileInputStream.close();

        downloadResult.setArtifactPayload(bFile);
        return downloadResult;
    }


//    @Test
    public void testASDCListener() throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {


        INotificationData notificationData = getNotificationData();
        asdcCallback.activateCallback(notificationData);

//        pause();
    }

//    private void pause(){
//        try {
//            Thread.sleep(50000000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

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
        Constructor constructor = Class.forName(fqcn).getDeclaredConstructors()[0];
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
