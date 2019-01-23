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
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.adapter.message.EventSender;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.utils.DistributionActionResultEnum;
import com.att.eelf.configuration.EELFLogger;

/**
 * This class tests the Abstract Artifact Processor
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractArtifactProcessor {

  private AbstractArtifactProcessor abstractArtifactProcessor;

  private EELFLogger logger;

  private IDistributionClient client;

  private IDistributionClientDownloadResult distributionClientDownloadResult;

  private IArtifactInfo artifactInfo;

  private INotificationData notificationData;

  private EventSender eventSender;

  private IResourceInstance resource;

  /**
   * Setup the test environment by loading a new Abstract artifact Processor
   */
  @Before
  public void setup() {
    abstractArtifactProcessor = Mockito.mock(AbstractArtifactProcessor.class, CALLS_REAL_METHODS);
    logger = Mockito.mock(EELFLogger.class);
    Mockito.doCallRealMethod().when(abstractArtifactProcessor).run();
    Whitebox.setInternalState(abstractArtifactProcessor, "logger", logger);
    client = Mockito.mock(IDistributionClient.class);
    distributionClientDownloadResult = Mockito.mock(IDistributionClientDownloadResult.class);
    artifactInfo = Mockito.mock(IArtifactInfo.class);
    notificationData = Mockito.mock(INotificationData.class);
    eventSender = Mockito.mock(EventSender.class);
    resource = Mockito.mock(IResourceInstance.class);
    Whitebox.setInternalState(abstractArtifactProcessor, "client", client);
    Whitebox.setInternalState(abstractArtifactProcessor, "artifact", artifactInfo);
    Whitebox.setInternalState(abstractArtifactProcessor, "notification", notificationData);
    Whitebox.setInternalState(abstractArtifactProcessor, "eventSender", eventSender);
    Whitebox.setInternalState(abstractArtifactProcessor, "resource", resource);
  }

  /**
   * This method tests the abstract Artifacts method success scenario
   */
  @Test
  public void testRun() {
    when(client.download(anyObject())).thenReturn(distributionClientDownloadResult);
    when(distributionClientDownloadResult.getDistributionActionResult())
        .thenReturn(DistributionActionResultEnum.SUCCESS);
    when(client.sendDownloadStatus(anyObject())).thenReturn(distributionClientDownloadResult);
    when(distributionClientDownloadResult.getArtifactPayload()).thenReturn(new byte[1]);
    abstractArtifactProcessor.run();
    verify(abstractArtifactProcessor, times(1)).run();
  }

  /**
   * This method tests the abstract Artifacts method failure scenario
   */
  @Test
  public void testRunWithDownloadFailed() {
    when(client.download(anyObject())).thenReturn(distributionClientDownloadResult);
    when(distributionClientDownloadResult.getDistributionActionResult())
        .thenReturn(DistributionActionResultEnum.FAIL);
    when(client.sendDownloadStatus(anyObject())).thenReturn(distributionClientDownloadResult);
    when(distributionClientDownloadResult.getArtifactPayload()).thenReturn(new byte[1]);
    abstractArtifactProcessor.run();
    verify(abstractArtifactProcessor, times(1)).run();
  }

  /**
   * This method tests the abstract Artifacts method failure scenario
   */
  @Test
  public void testRunWithException() {
    when(client.download(anyObject())).thenReturn(distributionClientDownloadResult);
    when(distributionClientDownloadResult.getDistributionActionResult())
        .thenReturn(DistributionActionResultEnum.SUCCESS);
    when(client.sendDownloadStatus(anyObject())).thenReturn(distributionClientDownloadResult);
    when(distributionClientDownloadResult.getArtifactPayload()).thenThrow(new RuntimeException());
    abstractArtifactProcessor.run();
    verify(abstractArtifactProcessor, times(1)).run();
  }
}
