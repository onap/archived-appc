package org.onap.appc.flow.controller.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class PropertiesLoader {

  private PropertiesLoader() {}

  static Properties load(String path) throws IOException {
    Properties props = new Properties();
    try (InputStream propStream = new FileInputStream(path)) {
      props.load(propStream);
    }
    return props;
  }

}
