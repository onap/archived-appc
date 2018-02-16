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

package org.onap.sdnc.dg.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicParser;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DGXMLLoadNActivate {

    private static final Logger logger = LoggerFactory.getLogger(DGXMLLoadNActivate.class);
    private static final String STRING_ENCODING = "utf-8";

    private final SvcLogicStore store;

    public DGXMLLoadNActivate(String propfile) throws DGXMLException, SvcLogicException {
        if (StringUtils.isBlank(propfile)) {
            throw new DGXMLException(propfile + " Profile file is not defined");
        }
        this.store = SvcLogicStoreFactory.getSvcLogicStore(propfile);
    }

    protected DGXMLLoadNActivate(SvcLogicStore store) {
        this.store = store;
    }

    public void loadDGXMLFile(String dgXMLpath) throws SvcLogicException {
        if (dgXMLpath != null) {
            SvcLogicParser.load(dgXMLpath, this.store);
        }
    }

    private void loadDGXMLDir(String xmlPath) {
        try {
            logger.info(
                "******************** Loading DG into Database *****************************");
            List<String> errors = new ArrayList<>();
            if (this.store != null) {
                File xmlDir = new File(xmlPath);
                if (xmlDir.isDirectory()) {
                    String[] extensions = new String[]{"xml", "XML"};
                    List<File> files = (List<File>) FileUtils.listFiles(xmlDir, extensions, true);
                    tryLoadXmls(errors, files);
                } else {
                    throw new DGXMLException(xmlPath + " is not a valid XML Directory");
                }
            } else {
                throw new DGXMLException("Failed to initialise SvcLogicStore");
            }

            if (!errors.isEmpty()) {
                throw new DGXMLException(errors.toString());
            }
        } catch (Exception e) {
            logger.error("Failed to load DGXML directories", e);
        }
    }

    private void tryLoadXmls(List<String> errors, List<File> files) throws IOException {
        for (File file : files) {
            logger.info("Loading DG XML file :" + file.getCanonicalPath());
            try {
                SvcLogicParser.load(file.getCanonicalPath(), this.store);
            } catch (Exception e) {
                logger.error("Failed to load XML " + file.getCanonicalPath(), e);
                errors.add("Failed to load XML " + file.getCanonicalPath()
                    + ", Exception : " + e.getMessage());
            }
        }
    }

    public void activateDg(String activateFilePath) {
        logger.info(
            "******************** Activating DG into Database *****************************");
        try {
            List<String> errors = new ArrayList<>();
            if (this.store != null) {
                File activateFile = new File(activateFilePath);
                if (activateFile.isFile()) {
                    List<String> fileLines = FileUtils.readLines(activateFile, STRING_ENCODING);
                    tryActivateDG(errors, fileLines);
                } else {
                    throw new DGXMLException(activateFile + " is not a valid Activate file Path");
                }
            } else {
                throw new DGXMLException("Failed to initialise SvcLogicStore");
            }

            if (!errors.isEmpty()) {
                throw new DGXMLException(errors.toString());
            }
        } catch (Exception e) {
            logger.error("Failed to activade DG", e);
        }
    }

    private void tryActivateDG(List<String> errors, List<String> fileLines) {
        if (fileLines != null) {
            for (String line : fileLines) {
                if (line != null && !line.trim().startsWith("#")) {
                    String[] lineArray = line.trim().split(":");
                    doActivateDG(errors, line, lineArray);
                }
            }
        }
    }

    private void doActivateDG(List<String> errors, String line, String[] lineArray) {
        try {
            if (lineArray != null && lineArray.length >= 4) {
                String module = lineArray[0];
                String rpc = lineArray[1];
                String version = lineArray[2];
                String mode = lineArray[3];
                if (StringUtils.isNotBlank(module)
                    && StringUtils.isNotBlank(rpc)
                    && StringUtils.isNotBlank(version)
                    && StringUtils.isNotBlank(mode)) {
                    logger.info("Activating DG :" + line);
                    SvcLogicGraph graph =
                        this.store.fetch(module, rpc, version, mode);
                    tryActivateStore(line, graph);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to Activate " + line, e);
            errors.add("Failed to Activate " + line + ", " + e.getMessage());
        }
    }

    private void tryActivateStore(String line, SvcLogicGraph graph) throws SvcLogicException, DGXMLException {
        if (graph != null) {
            logger.info("Found Graph :" + line + " Activating ...");
            store.activate(graph);
        } else {
            throw new DGXMLException("Failed to fetch from Database");
        }
    }


    public static void main(String[] args) {
        try {
            String xmlPath;
            String propertyPath;
            String activateFile;

            if (args != null && args.length >= 3) {
                xmlPath = args[0];
                activateFile = args[1];
                propertyPath = args[2];
            } else {
                throw new DGXMLException(
                    "Sufficient inputs for DGXMLLoadNActivate are missing <xmlpath> <activatefile> <dbPropertyfile>");
            }

            DGXMLLoadNActivate dgXMLLoadDB = new DGXMLLoadNActivate(propertyPath);
            dgXMLLoadDB.loadDGXMLDir(xmlPath);
            dgXMLLoadDB.activateDg(activateFile);
        } catch (Exception e) {
            logger.error("Arguments missing", e);
        } finally {
            System.exit(1);
        }
    }
}
