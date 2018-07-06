/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.seqgen;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.seqgen.dbservices.SequenceGeneratorDBServices;
import org.onap.appc.seqgen.dgplugin.SequenceGeneratorPlugin;
import org.onap.appc.seqgen.dgplugin.impl.SequenceGeneratorPluginImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import static org.mockito.Mockito.mock;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class TestSequenceGeneratorPlugin {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestSequenceGeneratorPlugin.class);
    private SequenceGeneratorPlugin seqImpl;

    @Test
    public void testGenerateSequenceStart() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/start.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        seqImpl = new SequenceGeneratorPluginImpl();
        seqImpl.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/Start2.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceWODependencyInfo()throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/start-withoutDependency.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/start-withoutDependency.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceSingleVM()throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/start-singleVM-.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/start-singleVM-.json");
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
        String actualOutput = readOutput("/output/Start2.json");

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
        String actualOutput = readOutput("/output/Output-stop.json");

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
        String inputJSON = readInput("/input/stop-singleVM.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/stop-singleVM.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStopSingleVmPerVnfc() throws URISyntaxException, IOException{
        String inputJSON = readInput("/input/stop-singleVmPerVnfc.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/stop-singleVmPerVnfc.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceRestartNoDep() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/restartNodep.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readInput("/output/restart-NoDep.json");
        outputJSON.trim();
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceRestartNoDepSingleVM() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/NoDep-SingleVM.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readInput("/output/restart-Nodep-SingleVM.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceStartSingleVmPerVnfc() throws URISyntaxException, IOException{
        String inputJSON = readInput("/input/start-singleVmPerVnfc-.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/start-singleVmPerVnfc.json");
        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceVnfcNotPresentInInventory() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/CheckVNfcInInventory.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);
        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String outputJSON = context.getAttribute("output");
        String actualOutput = readOutput("/output/CheckVnfcInInventory.json");

        Assert.assertEquals(outputJSON.trim(),actualOutput.trim());
    }

    @Test
    public void testGenerateSequenceCheckMandatoryVnfc() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/CheckMandatoryVnfc.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.debug("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence VMs missing for the mandatory VNFC : [smp]");
    }

    @Test
    public void testGenerateSequenceCheckMissingDependencyInfo() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/MissingDependencyInfo.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.debug("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence Dependency model is missing following vnfc type(s): [smp]");
    }

    @Test
    public void testGenerateSequenceExtraVnfcInDependency() throws URISyntaxException, IOException {
        String inputJSON = readInput("/input/WrongDependencyModel.json");

        Map<String,String> params = new HashMap<>();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("inputJSON",inputJSON);

        SequenceGeneratorPlugin plugin = new SequenceGeneratorPluginImpl();
        plugin.generateSequence(params,context);

        String errorCode = context.getAttribute("error-code");
        String errorMessage = context.getAttribute("error-message");
        logger.debug("errorCode = " + errorCode);
        Assert.assertEquals(errorCode,"401");
        Assert.assertEquals(errorMessage,"Error generating sequence Dependency model missing vnfc type SMP");
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
        return output.substring(start,output.length());

    }
}

