/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.sdc.artifacts.impl;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.adapter.message.EventSender;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.sdc.artifacts.object.SDCArtifact;
import org.onap.appc.sdc.listener.ProviderOperations;
import org.onap.appc.sdc.listener.ProviderResponse;
import org.onap.appc.sdc.listener.Util;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * This class contains test methods for ConfigArtifact Processor.
 *
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ProviderOperations.class, Util.class})
public class TestConfigArtifcatProcessor {

  private ConfigArtifactProcessor configArtifactProcessor;

  private SDCArtifact sdcArtifact;

  private URI uri = URI.create("http://localhost:8080");

  private IDistributionClient client;

  private EventSender eventSender;

  private ProviderResponse response;

  private INotificationData notificationData;

  private IResourceInstance resourceInstance;
  
  private IArtifactInfo artifactInfo;

  @Before
  public void setup() throws APPCException {
    client = Mockito.mock(IDistributionClient.class);
    notificationData = Mockito.mock(INotificationData.class);
    resourceInstance = Mockito.mock(IResourceInstance.class);
    artifactInfo = Mockito.mock(IArtifactInfo.class);
    sdcArtifact = Mockito.mock(SDCArtifact.class);
    eventSender = Mockito.mock(EventSender.class);
    configArtifactProcessor = new ConfigArtifactProcessor(client, eventSender, notificationData,
        resourceInstance, artifactInfo, uri);
    response = new ProviderResponse(200, "{\"key\":\"value\"}");
  }

  /**
   * This method tests the process Artifacts method success scenario.
   * 
   * @throws APPCException if the there are any exception in sdc artifacts
   */
  @Test
  public void testProcessArtifact() throws APPCException {
    PowerMockito.mockStatic(ProviderOperations.class);
    PowerMockito.mockStatic(Util.class);
    when(ProviderOperations.post(anyObject(), anyString(), anyObject())).thenReturn(response);
    when(Util.parseResponse(anyObject())).thenReturn(true);
    ConfigArtifactProcessor configArtifactProcessorSpy = Mockito.spy(configArtifactProcessor);
    configArtifactProcessorSpy.processArtifact(sdcArtifact);
    verify(configArtifactProcessorSpy, times(1)).processArtifact(sdcArtifact);
  }

  /**
   * This method tests the process Artifacts method failure scenario.
   * 
   * @throws APPCException if the there are any exception in sdc artifacts
   */
  @Test(expected = APPCException.class)
  public void testProcessArtifactFail() throws APPCException {
    PowerMockito.mockStatic(ProviderOperations.class);
    when(ProviderOperations.post(anyObject(), anyString(), anyObject())).thenReturn(response);
    configArtifactProcessor.processArtifact(sdcArtifact);
  }

  /**
   * This method tests the process Artifacts method failure scenario when the uri is null.
   * 
   * @throws APPCException if the there are any exception in sdc artifacts or in creating
   *         notification data, resource data & service artifacts
   */
  @Test
  public void testProcessArtifactWithNoURI() throws APPCException {
    configArtifactProcessor = new ConfigArtifactProcessor(client, eventSender, notificationData,
        resourceInstance, artifactInfo, null);
    ConfigArtifactProcessor configArtifactProcessorSpy = Mockito.spy(configArtifactProcessor);
    configArtifactProcessorSpy.processArtifact(sdcArtifact);
    verify(configArtifactProcessorSpy, times(1)).processArtifact(sdcArtifact);
  }
}
