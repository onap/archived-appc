/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.flow.controller.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResponseActionTest {

  private ResponseAction responseAction;

  @Before
  public void setUp() {
    responseAction = new ResponseAction();
  }

  @Test
  public void get_set_jump() {
    String jump = "some_jump";
    responseAction.setJump(jump);
    Assert.assertEquals(jump, responseAction.getJump());
  }

  @Test
  public void get_set_retry() {
    String retry = "some_retry";
    responseAction.setRetry(retry);
    Assert.assertEquals(retry, responseAction.getRetry());
  }

  @Test
  public void get_set_wait() {
    String wait = "some_wait";
    responseAction.setWait(wait);
    Assert.assertEquals(wait, responseAction.getWait());
  }

  @Test
  public void get_set_intermediate_message() {
    responseAction.setIntermediateMessage(true);
    Assert.assertTrue(responseAction.isIntermediateMessage());
  }

  @Test
  public void get_set_ignore() {
    responseAction.setIgnore(true);
    Assert.assertTrue(responseAction.isIgnore());
  }

  @Test
  public void get_set_stop() {
    responseAction.setStop(true);
    Assert.assertTrue(responseAction.isStop());
  }

  @Test
  public void to_string() {
    responseAction.setStop(true);
    responseAction.setIntermediateMessage(true);
    responseAction.setIgnore(true);
    responseAction.setJump("some_jump");
    responseAction.setRetry("some_retry");
    responseAction.setWait("some_wait");
    Assert.assertEquals("ResponseAction [wait=some_wait, retry=some_retry, jump=some_jump, ignore=true, stop=true, intermediateMessage=true]", responseAction.toString());
  }

}