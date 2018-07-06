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

package org.onap.appc.ccadaptor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DebugLog {

    private static String fileName = "/tmp/rt.log";

    public static void main(String args[]) {
        DebugLog debugLog = new DebugLog();
        debugLog.printAriDebug("DebugLog", "The Message");
    }

    public static void printAriDebug(String fn, String messg) {
        String logMessg = getDateTime() + " " + fn + " " + messg;
        appendToFile(logMessg + "\n");

    }

    public static void printRTAriDebug(String fn, String messg) {
        // System.out.println (getDateTime() +" " +fn +" " + messg);
        String logMessg = getDateTime() + " " + fn + " " + messg;
        appendToFile(logMessg + "\n");
    }

    public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void appendToFile(String dataToWrite) {
        String fn = "DebugLog.appendToFile";
        try {
            // First check to see if a file 'fileName' exist, if it does
            // write to it. If it does not exist, don't write to it.
            File tmpFile = new File(fileName);
            if (tmpFile.exists()) {
                try(BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true))) {
                    out.write(dataToWrite);
                }
            }
        } catch (IOException e) {
            DebugLog.printRTAriDebug(fn, "writeToFile() exception: " + e);
            //System.err.println("writeToFile() exception: " + e);
            e.printStackTrace();
        }
    }

    public void outputStackTrace(Exception e) {
        String fn = "DebugLog.outputStackTrace";
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        DebugLog.printRTAriDebug(fn, "Stack trace::: " + stackTrace);
    }

    public static String getStackTraceString(Exception e) {
        String fn = "DebugLog.outputStackTrace";
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        return (stackTrace);
    }

}

