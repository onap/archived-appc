/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications (C) 2019 Ericsson
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
package org.onap.appc.sdc.artifacts.impl;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.onap.appc.adapter.message.EventSender;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.sdc.artifacts.helper.ArtifactStorageService;
import org.onap.appc.sdc.artifacts.object.SDCArtifact;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
public class TestLicenseArtifactProcessor {

  private LicenseArtifactProcessor artifactProcessor;
  
  private ArtifactStorageService storageService;
  
  private INotificationData notificationDdata;

  private IResourceInstance resourceInstance;
  
  private IArtifactInfo artifactInfo;

  @Before
  public void setup() throws Exception {
    IDistributionClient client = PowerMockito.mock(IDistributionClient.class);
    EventSender eventSender = PowerMockito.mock(EventSender.class);
    storageService = PowerMockito.mock(ArtifactStorageService.class);
    artifactProcessor = Mockito.spy(new LicenseArtifactProcessor(client, eventSender, notificationDdata,
        resourceInstance, artifactInfo, null));
    Whitebox.setInternalState(artifactProcessor, "artifactStorageService", storageService);
    PowerMockito.doCallRealMethod().when(artifactProcessor)
        .processArtifact((SDCArtifact) Matchers.anyObject());
    PowerMockito.doNothing().when(storageService).storeSDCArtifact(Matchers.anyObject());
  }

  @Test(expected = org.onap.appc.exceptions.APPCException.class)
  public void testProcessArtifactWithMissingData() throws APPCException {
    SDCArtifact artifact = new SDCArtifact();
    artifact.setResourceVersion("RESOURCE VERSION");
    artifact.setArtifactUUID("123-456-789");
    artifactProcessor.processArtifact(artifact);
  }

  @Test
  public void testProcessArtifact() throws APPCException {
    PowerMockito.when(storageService.retrieveSDCArtifact(anyString(), anyString(), anyString()))
        .thenReturn(null);
    SDCArtifact artifact = new SDCArtifact();
    artifact.setResourceVersion("RESOURCE VERSION");
    artifact.setArtifactUUID("123-456-789");
    artifact.setResourceName("Resource Name");
    artifactProcessor.processArtifact(artifact);
    verify(storageService, Mockito.times(1)).storeSDCArtifact(anyObject());
  }

  @Test
  public void testProcessArtifactWithDuplicateArtifact() throws APPCException {
    SDCArtifact artifact = new SDCArtifact();
    artifact.setResourceVersion("RESOURCE VERSION");
    artifact.setArtifactUUID("123-456-789");
    artifact.setResourceName("Resource Name");
    PowerMockito.when(storageService.retrieveSDCArtifact(anyString(), anyString(), anyString()))
        .thenReturn(artifact);
    artifactProcessor.processArtifact(artifact);
    verify(storageService, Mockito.times(0)).storeSDCArtifact(anyObject());
  }

  /**
   * This method tests the process Artifacts method failure scenario.
   * 
   * @throws APPCException if the there are any exception in sdc artifacts
   */
  @Test(expected = APPCException.class)
  public void testProcessArtifactNullSDCArtifact() throws APPCException {
    SDCArtifact artifact = new SDCArtifact();
    artifact.setResourceVersion("RESOURCE VERSION");
    artifact.setArtifactUUID("123-456-789");
    artifact.setResourceName("Resource Name");
    when(storageService.retrieveSDCArtifact(anyString(), anyString(), anyString()))
        .thenThrow(new APPCException());
    artifactProcessor.processArtifact(artifact);
  }
}
