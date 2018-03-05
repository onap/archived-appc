/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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
package org.onap.appc.flow.controller.node;

import java.util.function.Function;

/**
 * Wrapper which allows to mock static calls of System.getenv()
 *
 * @see System#getenv()
 */
class EnvVariables {

  private Function<String, String> envSupplier;

  EnvVariables() {
    envSupplier = System::getenv;
  }

  String getenv(String variable) {
    return envSupplier.apply(variable);
  }
}
