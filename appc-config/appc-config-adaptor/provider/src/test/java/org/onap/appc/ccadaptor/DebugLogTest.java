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

import static junit.framework.TestCase.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DebugLogTest {

    @BeforeClass
    public static void createEmptyLogFile() throws IOException {
        Path logPath = Paths.get(buildTestResourcePath("rt.log"));
        Files.createFile(logPath);
    }

    @AfterClass
    public static void removeLog() throws IOException {
        Path existingLogPath = Paths.get(buildTestResourcePath("rt.log"));
        Files.delete(existingLogPath);
    }

    //@Test
    public void printRTAriDebug_shouldNotDoAnything_whenLogFileDoesNotExist() {
        // GIVEN
        Path nonExistingLogPath = Paths.get(buildTestResourcePath("nonExisting.log"));

        // WHEN
       // DebugLog debugLog = new DebugLog(nonExistingLogPath);
        DebugLog debugLog = new DebugLog();
        debugLog.printRTAriDebug("testMethod", "Custom Debug Message");

        // THEN
        assertTrue(Files.notExists(nonExistingLogPath));
    }

    //@Test
    public void printRTAriDebug_shouldWriteMessageToLogWithDate_whenLogFileExists() throws IOException {
        // GIVEN
        Path existingLogPath = Paths.get(buildTestResourcePath("rt.log"));

        // WHEN
        //DebugLog debugLog = new DebugLog(existingLogPath);
        DebugLog debugLog = new DebugLog();
        debugLog.printRTAriDebug("testMethod", "Custom Debug Message");

        // THEN
        String logEntry = readLogEntry(existingLogPath);
        assertTrue(logEntry.matches("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2} testMethod Custom Debug Message"));
    }

    private static String buildTestResourcePath(String resourceName) {
        String path = DebugLogTest.class.getClassLoader().getResource("./").getPath();
        return path + resourceName;
    }

    private String readLogEntry(Path path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            return br.readLine();
        }
    }
}
