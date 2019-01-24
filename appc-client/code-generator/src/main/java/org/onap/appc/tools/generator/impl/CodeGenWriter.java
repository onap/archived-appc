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

package org.onap.appc.tools.generator.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CodeGenWriter extends Writer {

    private FileWriter fileWriter;
    private boolean delimiterBeginFound;
    private Path basePath;
    private String outPath;
    private boolean deleteFile;
    private static final String DELIMITER = "__";
    private Pattern pattern;


    CodeGenWriter(String destination) throws IOException {
        super(destination);
        fileWriter = new FileWriter(destination);
        basePath = Paths.get(destination);
        delimiterBeginFound = false;
        outPath = "";
        deleteFile = false;
        pattern = Pattern.compile(DELIMITER);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        String bufferStr = new String(cbuf).substring(off, off + len);
        Matcher matcher = pattern.matcher(bufferStr);

        boolean isMatch = matcher.find();
        if (!isMatch) {
            if (!delimiterBeginFound) {
                fileWriter.write(cbuf, off, len);
            }
            else {
                outPath += bufferStr;
            }
        }
        else {
            if (!delimiterBeginFound) {
                delimiterBeginFound = true;
            }
            else {
                deleteFile = true;
                Path fileName = getNewFileName();
                Files.createDirectories(fileName.getParent());
                openNewFileWriter(fileName.toString());
                delimiterBeginFound = false;
                outPath = "";
            }
        }
    }

    @Override
    public void flush() throws IOException {
        fileWriter.flush();
    }

    @Override
    public void close() throws IOException {
        fileWriter.close();
        if (deleteFile) {
            Files.deleteIfExists(basePath);
        }
    }

    private Path getNewFileName() {
        String newRelativePath = this.outPath.replace(".", File.separator);
        return Paths.get(basePath.getParent().toString(), newRelativePath + ".java");
    }

    private void openNewFileWriter(String fileName) throws IOException {
        flush();
        close();
        fileWriter = new FileWriter(fileName);
    }

	public void setDelimiterBeginFound(boolean delimiterBeginFound) {
		this.delimiterBeginFound = delimiterBeginFound;
	}

	public void setDeleteFile(boolean deleteFile) {
		this.deleteFile = deleteFile;
	}
}
