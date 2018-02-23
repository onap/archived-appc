package org.onap.appc.flow.controller.node;

import java.util.function.Function;

/**
 * Wrapper for accessing environment variables
 */
class EnvVariables {

  private Function<String, String> envSupplier;

  EnvVariables() {
    envSupplier = System::getenv;
  }

  /**
   * Allows to override environment variables in tests, prefer to use default constructor
   */
  EnvVariables(Function<String, String> envSupplier) {
    this.envSupplier = envSupplier;
  }

  String getenv(String variable) {
    return envSupplier.apply(variable);
  }
}
