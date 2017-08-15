/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
 * ================================================================================
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

package org.openecomp.sdnc.config.generator;

import java.util.LinkedList;
import java.util.List;

import org.openecomp.sdnc.config.generator.convert.ConvertNode;
import org.openecomp.sdnc.config.generator.merge.MergeNode;
import org.openecomp.sdnc.config.generator.pattern.PatternNode;
import org.openecomp.sdnc.config.generator.reader.ReaderNode;
import org.openecomp.sdnc.config.generator.writer.FileWriterNode;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigGeneratorActivator implements BundleActivator{

    private List<ServiceRegistration> registrations = new LinkedList<ServiceRegistration>();


    private static final EELFLogger log = EELFManager.getInstance().getLogger(ConfigGeneratorActivator.class);

    @Override
    public void start(BundleContext ctx) throws Exception
    {

        ConvertNode convertNode = new ConvertNode();
        log.info("Registering service "+ convertNode.getClass().getName());
        registrations.add(ctx.registerService(convertNode.getClass().getName(), convertNode, null));
        log.info("Registering service sccessful for  "+ convertNode.getClass().getName());

        MergeNode mergeNode = new MergeNode();
        log.info("Registering service "+ mergeNode.getClass().getName());
        registrations.add(ctx.registerService(mergeNode.getClass().getName(), mergeNode, null));
        log.info("Registering service sccessful for "+ mergeNode.getClass().getName());

        PatternNode patternNode = new PatternNode();
        log.info("Registering service "+ patternNode.getClass().getName());
        registrations.add(ctx.registerService(patternNode.getClass().getName(), patternNode, null));
        log.info("Registering service sccessful for "+ patternNode.getClass().getName());

        ReaderNode readerNode = new ReaderNode();
        log.info("Registering service "+ readerNode.getClass().getName());
        registrations.add(ctx.registerService(readerNode.getClass().getName(), readerNode, null));
        log.info("Registering service sccessful for "+ readerNode.getClass().getName());
        
        FileWriterNode writerNode = new FileWriterNode();
        log.info("Registering service "+ writerNode.getClass().getName());
        registrations.add(ctx.registerService(writerNode.getClass().getName(), writerNode, null));
        log.info("Registering service sccessful for "+ writerNode.getClass().getName());

    }
    @Override
    public void stop(BundleContext arg0) throws Exception
    {
        for (ServiceRegistration registration: registrations)
        {
            registration.unregister();
            registration = null;
        }

    }

}
