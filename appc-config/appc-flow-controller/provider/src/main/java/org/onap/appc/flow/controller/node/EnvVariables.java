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
