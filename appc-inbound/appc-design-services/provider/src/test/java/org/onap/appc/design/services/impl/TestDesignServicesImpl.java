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

package org.onap.appc.design.services.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.design.dbervices.DesignDBService;
import org.onap.appc.design.xinterface.XInterfaceService;
import org.onap.appc.design.xinterface.XResponseProcessor;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.DbserviceInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.ValidatorInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.XinterfaceserviceInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev170627.design.request.DesignRequest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DesignDBService.class,XInterfaceService.class,XResponseProcessor.class})
public class TestDesignServicesImpl {

  private DesignServicesImpl designServicesImpl;
  private DbserviceInput dbserviceInput;
  private XinterfaceserviceInput xinterfaceserviceInput;
  private ValidatorInput validatorInput;
  private DesignRequest designRequest;
  private DesignDBService designDBService;
  private XInterfaceService xInterfaceService;
  private XResponseProcessor xResponseProcessor;


  @Before
  public void setUp() throws Exception {
    designServicesImpl = new DesignServicesImpl();
    dbserviceInput = Mockito.mock(DbserviceInput.class);
    designRequest = Mockito.mock(DesignRequest.class);
    designDBService = Mockito.mock(DesignDBService.class);
    xInterfaceService = Mockito.mock(XInterfaceService.class);
    PowerMockito.mockStatic(DesignDBService.class);
    PowerMockito.mockStatic(XResponseProcessor.class);
    PowerMockito.mockStatic(XInterfaceService.class);
    xResponseProcessor = PowerMockito.mock(XResponseProcessor.class);
    xinterfaceserviceInput = Mockito.mock(XinterfaceserviceInput.class);
    validatorInput = Mockito.mock(ValidatorInput.class);
    when(dbserviceInput.getDesignRequest()).thenReturn(designRequest);
    when(xinterfaceserviceInput.getDesignRequest()).thenReturn(designRequest);
    when(validatorInput.getDesignRequest()).thenReturn(designRequest);
    when(designRequest.getRequestId()).thenReturn("123");
    when(designRequest.getAction()).thenReturn("getDesigns");
    when(designRequest.getPayload()).thenReturn("{\"artifact-contents\":\"\",\"userID\":\"user\"}");
  }

  @Test
  public void testDbService() throws Exception {
    PowerMockito.when(DesignDBService.initialise()).thenReturn(designDBService);
    when(designDBService.execute(eq("getDesigns"), anyString(), eq("123"))).thenReturn("success");
    assertEquals("400",
        designServicesImpl.dbservice(dbserviceInput).get().getResult().getStatus().getCode());
  }

  @Test
  public void testDbServiceWithException() throws Exception {
    PowerMockito.when(DesignDBService.initialise()).thenReturn(designDBService);
    when(designDBService.execute(eq("getDesigns"), anyString(), eq("123")))
        .thenThrow(new Exception());
    assertEquals("401",
        designServicesImpl.dbservice(dbserviceInput).get().getResult().getStatus().getCode());
  }

  @Test
  public void testXinterfaceservice() throws Exception {
    PowerMockito.when(XInterfaceService.getInstance()).thenReturn(xInterfaceService);
    PowerMockito.when(XResponseProcessor.getInstance()).thenReturn(xResponseProcessor);
    when(xResponseProcessor.parseResponse(anyString(), eq("getDesigns"))).thenReturn("success");
    assertEquals("400", designServicesImpl.xinterfaceservice(xinterfaceserviceInput).get()
        .getResult().getStatus().getCode());
  }
  
  @Test
  public void testXinterfaceserviceWithException() throws Exception {
    PowerMockito.when(XInterfaceService.getInstance()).thenReturn(xInterfaceService);
    PowerMockito.when(XResponseProcessor.getInstance()).thenReturn(xResponseProcessor);
    when(xResponseProcessor.parseResponse(anyString(), eq("getDesigns"))).thenThrow(new Exception());
    assertEquals("401", designServicesImpl.xinterfaceservice(xinterfaceserviceInput).get()
        .getResult().getStatus().getCode());
  }

  @Test
  public void testValidatorJson() throws Exception {
    when(designRequest.getDataType()).thenReturn("JSON");
    assertEquals("400",
        designServicesImpl.validator(validatorInput).get().getResult().getStatus().getCode());
  }

  @Test
  public void testValidatorXml() throws Exception {
    when(designRequest.getPayload()).thenReturn("<artifact-contents></artifact-contents>");
    when(designRequest.getDataType()).thenReturn("XML");
    assertEquals("400",
        designServicesImpl.validator(validatorInput).get().getResult().getStatus().getCode());
  }

  @Test
  public void testValidatorYaml() throws Exception {
    when(designRequest.getPayload()).thenReturn("artifact-contents: 34843");
    when(designRequest.getDataType()).thenReturn("YAML");
    assertEquals("400",
        designServicesImpl.validator(validatorInput).get().getResult().getStatus().getCode());
  }

  @Test
  public void testValidatorVelocity() throws Exception {
    when(designRequest.getPayload()).thenReturn("artifact-contents: 34843");
    when(designRequest.getDataType()).thenReturn("VELOCITY");
    assertEquals("400",
        designServicesImpl.validator(validatorInput).get().getResult().getStatus().getCode());
  }

  @Test
  public void testValidatorInvalid() throws Exception {
    when(designRequest.getDataType()).thenReturn("XYZ");
    assertEquals("401",
        designServicesImpl.validator(validatorInput).get().getResult().getStatus().getCode());
  }

}
