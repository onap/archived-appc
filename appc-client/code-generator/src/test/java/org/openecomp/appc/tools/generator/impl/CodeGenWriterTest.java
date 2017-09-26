package org.openecomp.appc.tools.generator.impl;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;


public class CodeGenWriterTest {
    @Test
    public void writeTest() throws IOException {
        CodeGenWriter codeGenWriter = new CodeGenWriter("destination");
        char[] cbuf = {'t','e','s','t'};
        int off = 1;
        int len = 3;
        codeGenWriter.write(cbuf,off,len);
        codeGenWriter.flush();
        codeGenWriter.close();
        Assert.assertNotNull(codeGenWriter);
    }

}
