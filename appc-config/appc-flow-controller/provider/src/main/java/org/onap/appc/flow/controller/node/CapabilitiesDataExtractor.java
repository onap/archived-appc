/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * Copyright (C) 2019 AT&T intellectual property. All rights reserved.
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

import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VF_MODULE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VM;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNF;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNFC;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.onap.appc.flow.controller.dbervices.FlowControlDBService;
import org.onap.appc.flow.controller.interfaceData.Capabilities;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class CapabilitiesDataExtractor {

   private static final EELFLogger log = EELFManager.getInstance().getLogger(CapabilitiesDataExtractor.class);

   private final FlowControlDBService dbService;
   private final ObjectMapper mapper;

   public CapabilitiesDataExtractor() {
      this(FlowControlDBService.initialise());
   }

   /**
    * Ctor for tests, prefer to use default one
    */
   public CapabilitiesDataExtractor(FlowControlDBService dbService) {
      this.dbService = dbService;

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
   }

   Capabilities getCapabilitiesData(SvcLogicContext ctx) throws SvcLogicException, IOException {

      String fn = "FlowExecutorNode.getCapabilitiesData";
      String capabilitiesData = dbService.getCapabilitiesData(ctx);
      log.info(fn + ":capabilitiesDataInput:" + capabilitiesData);

      Capabilities capabilities = new Capabilities();
      if (StringUtils.isBlank(capabilitiesData)) {
         return capabilities;
      }

      JsonNode capabilitiesNode = mapper.readTree(capabilitiesData);
      JsonNode capNode = capabilitiesNode.get("capabilities");
      log.info("capabilitiesNode:" + capabilitiesNode.toString());

      capabilities.getVfModule().addAll(extractParameterList(capNode, VF_MODULE));
      capabilities.getVnfc().addAll(extractParameterList(capNode, VNFC));
      capabilities.getVnf().addAll(extractParameterList(capNode, VNF));
      capabilities.getVm().putAll(extractParameterMap(capNode, VM));

      log.info("Capabilities Output:" + capabilities.toString());

      return capabilities;
   }

   private <T> List<T> extractParameterList(JsonNode root, String parameter) throws IOException {
      JsonNode parameterNode = root.get(parameter);
      if (parameterNode == null) {
         return new ArrayList<>();
      }
      return mapper.readValue(parameterNode.toString(), new TypeReference<List<T>>() {
      });
   }

   private <T> HashMap<T, List<T>> extractParameterMap(JsonNode root, String parameter) throws IOException {
      JsonNode parameterNode = root.get(parameter);
      HashMap<T, List<T>> hm = new HashMap<T, List<T>>();
      if (parameterNode == null || !parameterNode.isArray()) {
         return hm;
      }
      for (JsonNode n : parameterNode) {
         Iterator<Map.Entry<String, JsonNode>> fldIter = n.fields();
         while (fldIter.hasNext()) {
            Map.Entry<String, JsonNode> currentEntry = fldIter.next();
            if (currentEntry.getValue().isArray()) {
               Iterator<JsonNode> nodeIter = currentEntry.getValue().elements();
               List<String> listOfT = new ArrayList<String>();
               while (nodeIter.hasNext()) {
                  listOfT.add((nodeIter.next().asText()));
               }
               hm.put((T) currentEntry.getKey(), (List<T>) listOfT);
            }
         }
      }
      return hm;
   }
}
