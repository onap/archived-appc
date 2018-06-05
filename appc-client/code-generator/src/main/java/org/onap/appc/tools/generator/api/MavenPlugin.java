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

package org.onap.appc.tools.generator.api;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.onap.appc.tools.generator.impl.ModelGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

@Mojo(
        name = "generate-sources",
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

            //the source file may be in the class path
            //or on the file system
            URL sourceFileURL = lookupURL(sourceFileName);

            //prefix with the project absolute path to the output file
            outputFileName = toAbsoluteFile(outputFileName);
            String workDirectory = Paths.get(outputFileName).getParent().toString();
            generator.execute(sourceFileURL,outputFileName,templateName,contextBuilderClassName,contextConfigFileName);
            project.addCompileSourceRoot(workDirectory);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(),e);
        }
    }


    /**
     * Converts the file to absolute path.  If the file does not exist prefix the maven project absolute path.
     * @param filePath
     * @return
     */
    private String toAbsoluteFile(String filePath){

        File file = new File(filePath);

        //if the file already exist just return the absolutePath
        if(file.exists()){
            return file.getAbsolutePath();
    }


        //prefix with the project absolute path to the output file
        if(!file.isAbsolute()){
            File projectDir = new File(this.project.getBuild().getDirectory()).getParentFile();
            filePath = projectDir.getAbsolutePath() + "/" + filePath;
        }

        return filePath;
    }

    /**
     * Tries three lookups
     * First try to lookup the file in the classpath.
     * else try relative path
     * else try prefixing the relative path with the maven project path.

     * @param filePath - A String denoting the source yang file path.
     * @return URL - to the source yang file
     * @throws MalformedURLException
     */
    private URL lookupURL(String filePath) throws IOException {
        //check out the class path first
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL sourceYangURL = classLoader.getResource(filePath);

        if (sourceYangURL != null) {
            return sourceYangURL;
        }

        String errorMessage = String.format(
                "YANG file <%s> not found in classpath or on the file system."
                ,filePath
        );

        //check the file system first
        File sourceFile = new File(toAbsoluteFile(filePath));
        if (!sourceFile.exists()) {
            throw new FileNotFoundException(errorMessage);
        }
        try {
            sourceYangURL = sourceFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IOException(errorMessage,e);
        }
        return sourceYangURL;
    }


    private void trace(String message) {
        getLog().info(message);
    }
}
