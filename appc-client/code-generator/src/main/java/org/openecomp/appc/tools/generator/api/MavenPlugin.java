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

package org.openecomp.appc.tools.generator.api;

import org.openecomp.appc.tools.generator.impl.ModelGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Paths;

@Mojo(
        name = "generate",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class MavenPlugin extends AbstractMojo {

    @Parameter(property = "templateName", required = true)
    private String templateName;

    @Parameter(property = "sourceFileName")
    private String sourceFileName;

    @Parameter(property = "outputFileName")
    private String outputFileName;

    @Parameter(property = "contextBuilderClassName", required = true)
    private String contextBuilderClassName;

    @Parameter(property = "contextConfigFileName")
    private String contextConfigFileName;

    @Parameter (property = "project")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ModelGenerator generator = new ModelGenerator();
        try {
            trace("\t === Called MavenPlugin on builder <" + contextBuilderClassName +">\n");
            generator.execute(sourceFileName,outputFileName,templateName,contextBuilderClassName,contextConfigFileName);
            String workDirectory = getWorkDirectory(outputFileName);
            project.addCompileSourceRoot(workDirectory);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private String getWorkDirectory(String outputFileName) throws IOException {
        String workDirPath = Paths.get(outputFileName.toString()).getParent().toString();
        return workDirPath;
    }

    private void trace(String message) {
        getLog().info(message);
    }
}
