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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.adapter.message.EventSender;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.sdc.artifacts.helper.ArtifactStorageService;
import org.onap.appc.sdc.artifacts.helper.DependencyModelGenerator;
import org.onap.appc.sdc.artifacts.object.SDCArtifact;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.powermock.reflect.Whitebox;

/**
 * This class contains test methods for ToscaCsarArtifact Processor
 *
 */

public class TestToscaCsarArtifactProcessor {

    private ToscaCsarArtifactProcessor toscaCsarArtifactProcessor;
    
    private DependencyModelGenerator dependencyModelGenerator;
    
    private ArtifactStorageService storageService;
    
    private SDCArtifact sdcArtifact;
    
    private IDistributionClientDownloadResult distributionClientDownloadResult; 
    
    private IDistributionClient client;
    
    private EventSender eventSender;
    

	@Before
	public void setup() throws Exception {
		client = Mockito.mock(IDistributionClient.class);
		eventSender = Mockito.mock(EventSender.class);
		storageService = Mockito.mock(ArtifactStorageService.class);
		distributionClientDownloadResult = Mockito.mock(IDistributionClientDownloadResult.class);
		dependencyModelGenerator = Mockito.mock(DependencyModelGenerator.class);
		toscaCsarArtifactProcessor = new ToscaCsarArtifactProcessor(client, eventSender, ArtifactProcessorUtility.getNotificationData(),
				ArtifactProcessorUtility.getResources().get(0), ArtifactProcessorUtility.getServiceArtifacts().get(0), null);
		Whitebox.setInternalState(toscaCsarArtifactProcessor,"artifactStorageService", storageService);
		Whitebox.setInternalState(toscaCsarArtifactProcessor,"dependencyModelGenerator", dependencyModelGenerator);
	}

	/**
	 * This method tests the process Artifacts method success scenario.
	 * 
	 * @throws APPCException if the there are any exception in sdc artifacts
	 */
	@Test
	public void testProcessArtifact() throws APPCException {
		ToscaCsarArtifactProcessor toscaCsarArtifactProcessorSpy = Mockito.spy(toscaCsarArtifactProcessor);
		sdcArtifact = new SDCArtifact();
		sdcArtifact.setResourceVersion("RESOURCE VERSION");
		sdcArtifact.setArtifactUUID("123-456-789");
		sdcArtifact.setResourceName("Resource Name");
		when(dependencyModelGenerator.getDependencyModel(anyString(), anyString())).thenReturn("dependencyModel");
		toscaCsarArtifactProcessorSpy.processArtifact(sdcArtifact);
		verify(toscaCsarArtifactProcessorSpy, times(1)).processArtifact(sdcArtifact);
	}

	/**
	 * This method tests the process Artifacts method failure scenario.
	 * 
	 * @throws APPCException if the there are any exception in sdc artifacts
	 */
	@Test(expected = APPCException.class)
	public void testProcessArtifactFailWithNullValues() throws APPCException {
		sdcArtifact = new SDCArtifact();
		toscaCsarArtifactProcessor.processArtifact(sdcArtifact);
	}
	
	/**
	 * This method tests the process Artifacts method failure scenario
	 * 
	 * @throws Exception if the there are any exception in sdc artifacts or in creating notification data, resource data & service artifacts
	 */
	@Test(expected = APPCException.class)
	public void testProcessArtifactFail() throws Exception {
		sdcArtifact = new SDCArtifact();
		sdcArtifact.setResourceVersion("RESOURCE VERSION");
		sdcArtifact.setArtifactUUID("123-456-789");
		sdcArtifact.setResourceName("Resource Name");
		when(dependencyModelGenerator.getDependencyModel(anyString(), anyString())).thenThrow(new APPCException());
		toscaCsarArtifactProcessor.processArtifact(sdcArtifact);
	}
	
	/**
	 * This method tests the process Artifacts method success scenario.
	 * 
	 * @throws APPCException if the there are any exception in sdc artifacts
	 */
	//@Test
	public void testProcessArtifactDistributionClient() throws APPCException {
		when(distributionClientDownloadResult.getArtifactPayload()).thenReturn(new byte[1]);
		ToscaCsarArtifactProcessor toscaCsarArtifactProcessorSpy = Mockito.spy(toscaCsarArtifactProcessor);
		toscaCsarArtifactProcessorSpy.processArtifact(distributionClientDownloadResult);
		verify(toscaCsarArtifactProcessorSpy, times(1)).processArtifact(distributionClientDownloadResult);
	}
}
