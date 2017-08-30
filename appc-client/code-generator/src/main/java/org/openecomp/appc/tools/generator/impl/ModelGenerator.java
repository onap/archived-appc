/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.tools.generator.impl;

import org.openecomp.appc.tools.generator.api.ContextBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class ModelGenerator {

    public void execute(String sourceFile, String destinationFile, String templateFile, String builderName, String contextConfName) throws IOException, ReflectiveOperationException {

        ContextBuilder contextBuilder = (ContextBuilder) Class.forName(builderName).newInstance();
        Map<String, Object> context = contextBuilder.buildContext(sourceFile, contextConfName);

        Path destinationPath = Paths.get(destinationFile);
        if (!Files.isDirectory(destinationPath))
            Files.createDirectories(destinationPath.getParent());
        else {
            Files.createDirectories(destinationPath);
        }

        this.generate(context, templateFile, destinationFile);
        System.out.println("\tFile <" + destinationFile + "> prepared successfully");
    }

    private void generate(Map<String, Object> context, String templateFile, String destinationFile) throws ReflectiveOperationException {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
            cfg.setClassForTemplateLoading(ModelGenerator.class, "/");
            Template template = cfg.getTemplate(templateFile);

            Writer out = new CodeGenWriter(destinationFile);
            template.process(context, out);
            out.close();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("Failed to generate file from template <" + templateFile + ">", e);
        }
    }

}
