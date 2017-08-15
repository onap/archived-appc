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

package org.openecomp.appc.seqgen;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.seqgen.dgplugin.SequenceGeneratorPlugin;
import org.openecomp.appc.seqgen.dgplugin.impl.SequenceGeneratorPluginImpl;
import org.openecomp.sdnc.sli.SvcLogicContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class TestSequenceGeneratorPlugin {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestSequenceGeneratorPlugin.class);

    @Test
    public void testGenerateSequenceStart() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/start.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/start.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }



    @Test
    public void testGenerateSequenceSingleVM()throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/start-single-vm.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/start-single-vm.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceNoStrategy() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/no-strategy.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/start.json");

        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStop() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/stop.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/stop.json");

        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceWrongNumber() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/wrongnumber.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.debug("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence Invalid Number for Wait Time 6a");
    }


    @Test
    public void testGenerateSequenceCyclic() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/cyclic.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.debug("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence There seems to be no Root/Independent node for Vnfc dependencies");
    }


    @Test
    public void testGenerateSequenceWrongAction() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/wrongaction.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.debug("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence Invalid Action start");
    }


    @Test
    public void testGenerateSequenceMissingRequestInfo() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/missingrequestinfo.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.debug("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence Request info is not provided in the input");
    }

    @Test
    public void testGenerateSequenceStopSingleVM() throws URISyntaxException, IOException{
        String inputJSON = readInput("/input/stop-single-vm.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/stop-single-vm.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStopSingleVmPerVnfc() throws URISyntaxException, IOException{
        String inputJSON = readInput("/input/stop-single-vm-per-vnfc.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/stop-single-vm-per-vnfc.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStartSingleVmPerVnfc() throws URISyntaxException, IOException{
        String inputJSON = readInput("/input/start-single-vm-per-vnfc.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/start-single-vm-per-vnfc.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    private String readInput(String inputFile) throws URISyntaxException, IOException {
        File file = new File(this.getClass().getResource(inputFile).toURI());

        byte[] bFile = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(bFile);
        fileInputStream.close();
        return new String(bFile);
    }
    private String readOutput(String outputFile) throws IOException,URISyntaxException {
        File file = new File(this.getClass().getResource(outputFile).toURI());

        byte[] bFile = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(bFile);
        fileInputStream.close();

        String output=new String(bFile);
        int start=output.indexOf("[");
        int last=output.length()-1;
        return output.substring(start,last);

    }
}
