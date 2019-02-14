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

package org.onap.appc.data.services.db;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DGGeneralDBService.class)
public class TestGeneralDataService {

  private GeneralDataService generalDataService;
  private Map<String, String> inParams;
  private SvcLogicContext ctx;
  private DGGeneralDBService dGGeneralDBService;

  @Before
  public void setUp() throws SvcLogicException {
    PowerMockito.mockStatic(DGGeneralDBService.class);
    dGGeneralDBService = Mockito.mock(DGGeneralDBService.class);
    ctx = new SvcLogicContext();
    generalDataService = new GeneralDataService();
    inParams = new HashMap<>();
    inParams.put("message", "message");
    PowerMockito.when(DGGeneralDBService.initialise()).thenReturn(dGGeneralDBService);
  }

  @Test
  public void testSaveTransactionLog() throws SvcLogicException {
    PowerMockito.when(dGGeneralDBService.saveConfigTransactionLog(anyObject(), eq("")))
        .thenReturn(QueryStatus.SUCCESS);
    generalDataService.saveTransactionLog(inParams, ctx);
  }

  @Test(expected = Exception.class)
  public void testSaveTransactionLogFailure() throws SvcLogicException {
    PowerMockito.when(dGGeneralDBService.saveConfigTransactionLog(anyObject(), eq("")))
        .thenReturn(QueryStatus.FAILURE);
    generalDataService.saveTransactionLog(inParams, ctx);
  }

  @Test(expected = SvcLogicException.class)
  public void testSaveTransactionLogException() throws SvcLogicException {
    generalDataService.saveTransactionLog(inParams, null);
  }

}
