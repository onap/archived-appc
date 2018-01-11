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
package org.onap.appc.ccadaptor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class DebugLog {

    public static final String LOG_FILE = "/tmp/rt.log";
    private final Path path;

    public DebugLog(Path path) {
        this.path = path;
    }

    public void printRTAriDebug(String methodName, String message) {
        writeToLogFile(methodName, message);
    }

    private String currentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private void appendToFile(File logPath, String dataToWrite) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(logPath, true))) {
            out.write(dataToWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void outputStackTrace(Exception e) {
        String fn = "DebugLog.outputStackTrace";
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        writeToLogFile(fn, "Stack trace::: " + stackTrace);
    }

    private void writeToLogFile(String methodName, String message) {
        File logPath = path.toFile();
        if (logPath.exists()) {
            StringBuilder logMessageBuilder = createLogMessage(methodName, message);
            appendToFile(logPath, logMessageBuilder.toString());
        }
    }

    private StringBuilder createLogMessage(String methodName, String message) {
        StringBuilder logMessageBuilder = new StringBuilder();
        logMessageBuilder
            .append(currentDateTime())
            .append(" ")
            .append(methodName)
            .append(" ")
            .append(message)
            .append('\n');
        return logMessageBuilder;
    }
}
