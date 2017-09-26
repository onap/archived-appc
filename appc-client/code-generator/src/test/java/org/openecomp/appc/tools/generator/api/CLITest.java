package org.openecomp.appc.tools.generator.api;

import org.junit.Assert;
import org.junit.Test;

public class CLITest {
    @Test
    public void missingSourceFileTest()  {
        CLI cli = new CLI();
        try {
            String[] input = new String[1];
            cli.main(input);
        } catch (Exception e) {
            Assert.assertEquals("Source file is missing. Please add argument 'client <source file> <destination file> <model template>'",e.getMessage());
        }
    }
    @Test
    public void missingDestinationFileTest()  {
        CLI cli = new CLI();
        try {
            String[] input = {"sourceFilePath",null};
            cli.main(input);
        } catch (Exception e) {
            Assert.assertEquals("Destination file name is missing. Please add argument 'client sourceFilePath <destination> <model template> <builder> <conf file>'",e.getMessage());
        }
    }
    @Test
    public void missingTemplateFileTest()  {
        CLI cli = new CLI();
        try {
            String[] input = {"sourceFilePath","destinationPath",null};
            cli.main(input);
        } catch (Exception e) {
            Assert.assertEquals("template file name is missing. Please add argument 'client sourceFilePath destinationPath <model template> <builder> <conf file>'",e.getMessage());
        }
    }
    @Test
    public void missingBuilderNameTest()  {
        CLI cli = new CLI();
        try {
            String[] input = {"sourceFilePath","destinationPath","templateFileName",null};
            cli.main(input);
        } catch (Exception e) {
            Assert.assertEquals("builder FQDN is missing. Please add argument 'client sourceFilePath destinationPath templateFileName <builder> <conf file>'",e.getMessage());
        }
    }
    @Test
    public void missingContextConfFileNameTest()  {
        CLI cli = new CLI();
        try {
            String[] input = {"sourceFilePath","destinationPath","templateFileName","builderFQDN",null};
            cli.main(input);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(),"context conf file is missing. Please add argument 'client sourceFilePath destinationPath templateFileName builderFQDN <conf file>'");
        }
    }
}
