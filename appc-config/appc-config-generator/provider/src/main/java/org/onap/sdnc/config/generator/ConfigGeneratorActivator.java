/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.sdnc.config.generator;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.LinkedList;
import java.util.List;
import org.onap.sdnc.config.generator.convert.ConvertNode;
import org.onap.sdnc.config.generator.merge.MergeNode;
import org.onap.sdnc.config.generator.pattern.PatternNode;
import org.onap.sdnc.config.generator.reader.ReaderNode;
import org.onap.sdnc.config.generator.writer.FileWriterNode;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class ConfigGeneratorActivator implements BundleActivator {

    private static final String STR_REGISTERING_SERVICE = "Registering service ";
    private static final String STR_REGISTERING_SERVICE_SUCCESS = "Registering service successful for ";
    private List<ServiceRegistration> registrations = new LinkedList<>();

    private static final EELFLogger log =
        EELFManager.getInstance().getLogger(ConfigGeneratorActivator.class);

    @Override
    public void start(BundleContext ctx) throws Exception {

        ConvertNode convertNode = new ConvertNode();
        log.info(STR_REGISTERING_SERVICE + convertNode.getClass().getName());
        registrations.add(ctx.registerService(convertNode.getClass().getName(), convertNode, null));
        log.info(STR_REGISTERING_SERVICE_SUCCESS + convertNode.getClass().getName());

        MergeNode mergeNode = new MergeNode();
        log.info(STR_REGISTERING_SERVICE + mergeNode.getClass().getName());
        registrations.add(ctx.registerService(mergeNode.getClass().getName(), mergeNode, null));
        log.info(STR_REGISTERING_SERVICE_SUCCESS + mergeNode.getClass().getName());

        PatternNode patternNode = new PatternNode();
        log.info(STR_REGISTERING_SERVICE + patternNode.getClass().getName());
        registrations.add(ctx.registerService(patternNode.getClass().getName(), patternNode, null));
        log.info(STR_REGISTERING_SERVICE_SUCCESS + patternNode.getClass().getName());

        ReaderNode readerNode = new ReaderNode();
        log.info(STR_REGISTERING_SERVICE + readerNode.getClass().getName());
        registrations.add(ctx.registerService(readerNode.getClass().getName(), readerNode, null));
        log.info(STR_REGISTERING_SERVICE_SUCCESS + readerNode.getClass().getName());

        FileWriterNode writerNode = new FileWriterNode();
        log.info(STR_REGISTERING_SERVICE + writerNode.getClass().getName());
        registrations.add(ctx.registerService(writerNode.getClass().getName(), writerNode, null));
        log.info(STR_REGISTERING_SERVICE_SUCCESS + writerNode.getClass().getName());

    }

    @Override
    public void stop(BundleContext arg0) throws Exception {
        for (ServiceRegistration registration : registrations) {
            registration.unregister();
        }
    }
}
