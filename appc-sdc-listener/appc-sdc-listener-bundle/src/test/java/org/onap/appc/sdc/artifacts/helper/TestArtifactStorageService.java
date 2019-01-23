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

package org.onap.appc.sdc.artifacts.helper;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.sdc.artifacts.object.SDCArtifact;
import org.onap.appc.sdc.artifacts.object.SDCReference;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

/**
 * This class contains test methods for Artifact Storage Service.
 *
 */

public class TestArtifactStorageService {

	private ArtifactStorageService artifactStorageService;

	private ArtifactStorageService artifactStorageServiceSpy;

	private SDCArtifact sdcArtifact;

	private SDCReference sdcReference;

	private DbLibService dbLibService;

	private CachedRowSet cachedRowSet;

	@Before
	public void setup() throws Exception {
		artifactStorageService = new ArtifactStorageService();
		sdcArtifact = Mockito.mock(SDCArtifact.class);
		dbLibService = Mockito.mock(DbLibService.class);
		sdcReference = Mockito.mock(SDCReference.class);
		cachedRowSet = Mockito.mock(CachedRowSet.class);
		Whitebox.setInternalState(artifactStorageService, "dbLibService", dbLibService);
		artifactStorageServiceSpy = Mockito.spy(artifactStorageService);
	}

	/**
	 * This method tests the store Artifacts method success scenario.
	 * 
	 * @throws APPCException if the there are any exception in sdc artifacts
	 * @throws SQLException  if there are any sql exception while storing the sdc artifacts
	 */
	@Test
	public void testStoreSDCArtifact() throws APPCException, SQLException {
		when(dbLibService.writeData(anyObject(), anyObject(), anyObject())).thenReturn(true);
		artifactStorageServiceSpy.storeSDCArtifact(sdcArtifact);
		verify(artifactStorageServiceSpy, times(1)).storeSDCArtifact(sdcArtifact);
	}

	/**
	 * This method tests the store Artifacts method failure scenario.
	 * 
	 * @throws APPCException if the there are any exception in sdc artifacts
	 * @throws SQLException  if there are any sql exception while storing the sdc artifacts
	 */
	@Test(expected = APPCException.class)
	public void testStoreSDCArtifactWithException() throws APPCException, SQLException {
		when(dbLibService.writeData(anyObject(), anyObject(), anyObject())).thenThrow(new SQLException());
		artifactStorageService.storeSDCArtifact(sdcArtifact);
	}

	/**
	 * This method tests the store Artifacts with reference method success scenario.
	 * 
	 * @throws APPCException if the there are any exception in sdc artifacts
	 * @throws SQLException  if there are any sql exception while storing the sdc artifacts
	 */
	@Test
	public void testStoreSDCArtifactWithReference() throws APPCException, SQLException {
		when(dbLibService.writeData(anyObject(), anyObject(), anyObject())).thenReturn(true);
		when(dbLibService.getData(anyObject(), anyObject(), eq("sdnctl"))).thenReturn(cachedRowSet);
		when(cachedRowSet.first()).thenReturn(true);
		artifactStorageServiceSpy.storeSDCArtifactWithReference(sdcArtifact, sdcReference);
		verify(artifactStorageServiceSpy, times(1)).storeSDCArtifactWithReference(sdcArtifact, sdcReference);
	}

	/**
	 * This method tests the store Artifacts with reference method success scenario.
	 * 
	 * @throws APPCException if the there are any exception in sdc artifacts
	 * @throws SQLException  if there are any sql exception while storing the sdc artifacts
	 */
	@Test
	public void testArtifactWithExistingRef() throws APPCException, SQLException {
		when(dbLibService.writeData(anyObject(), anyObject(), anyObject())).thenReturn(true);
		when(dbLibService.getData(anyObject(), anyObject(), eq("sdnctl"))).thenReturn(cachedRowSet);
		ArtifactStorageService artifactStorageServiceSpy = Mockito.spy(artifactStorageService);
		artifactStorageServiceSpy.storeSDCArtifactWithReference(sdcArtifact, sdcReference);
		verify(artifactStorageServiceSpy, times(1)).storeSDCArtifactWithReference(sdcArtifact, sdcReference);
	}

	/**
	 * This method tests the store Artifacts with reference method failure scenario.
	 * 
	 * @throws APPCException if the there are any exception in sdc artifacts
	 * @throws SQLException  if there are any sql exception while storing the sdc artifacts
	 */
	@Test(expected = APPCException.class)
	public void testArtifactWithExceptionScenario1() throws APPCException, SQLException {
		when(dbLibService.getData(anyObject(), anyObject(), eq("sdnctl"))).thenThrow(new SQLException());
		artifactStorageService.storeSDCArtifactWithReference(sdcArtifact, sdcReference);
	}

	/**
	 * This method tests the store Artifacts with reference method failure scenario.
	 * 
	 * @throws APPCException if the there are any exception in sdc artifacts
	 * @throws SQLException  if there are any sql exception while storing the sdc artifacts
	 */
	@Test(expected = APPCException.class)
	public void testArtifactWithExceptionScenario2() throws APPCException, SQLException {
		when(dbLibService.writeData(anyObject(), anyObject(), anyObject())).thenThrow(new SQLException());
		when(dbLibService.getData(anyObject(), anyObject(), eq("sdnctl"))).thenReturn(cachedRowSet);
		artifactStorageService.storeSDCArtifactWithReference(sdcArtifact, sdcReference);
	}

	/**
	 * This method tests the retrieve Artifacts with reference method failure scenario
	 * scenario.
	 * 
	 * @throws APPCException if the there are any exception in sdc artifacts
	 * @throws SQLException  if there are any sql exception while storing the sdc artifacts
	 */
	@Test(expected = APPCException.class)
	public void testRetrieveSDCReference() throws APPCException, SQLException {
		when(dbLibService.getData(anyObject(), anyObject(), eq("sdnctl"))).thenThrow(new SQLException());
		artifactStorageService.retrieveSDCReference("vnfType", "category");
	}
}
