/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018-2019 IBM.
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

package org.onap.appc.tools.generator.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


public class CodeGenWriterTest {
    
    private CodeGenWriter codeGenWriter;
    
    @Before
    public void setUp() throws IOException
    {
        codeGenWriter = new CodeGenWriter("destination");
    }
    
    @Test
    public void writeTest() throws IOException {
        char[] cbuf = {'t','_','_','t'};
        int off = 1;
        int len = 3;
        codeGenWriter.write(cbuf,off,len);
        codeGenWriter.flush();
        codeGenWriter.close();
        Assert.assertNotNull(codeGenWriter);
    }
    
    @Test(expected= NullPointerException.class)
    public void writeTest1() throws IOException {
        char[] cbuf = {'t','_','_','t'};
        codeGenWriter.setDeleteFile(true);
        codeGenWriter.setDelimiterBeginFound(true);
        int off = 1;
        int len = 3;
        codeGenWriter.write(cbuf,off,len);
        
    }
    
    @Test
    public void writeTestNegative() throws IOException {
        codeGenWriter.setDeleteFile(true);
        codeGenWriter.setDelimiterBeginFound(true);
        char[] cbuf = {};
        int off = 0;
        int len = 0;
        codeGenWriter.write(cbuf,off,len);
        codeGenWriter.flush();
        codeGenWriter.close();
        Assert.assertNotNull(codeGenWriter);
    }

}
