/* 
 * ============LICENSE_START======================================================= 
 * ONAP : APPC 
 * ================================================================================ 
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved. 
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
import org.onap.ccsdk.sli.core.sli.SvcLogicParser;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DGXMLLoader {

    private static final Logger logger = LoggerFactory.getLogger(DGXMLLoader.class);
    public static final String STRING_ENCODING = "utf-8";

    private final SvcLogicStore store;

    public DGXMLLoader(String propfile) throws DGXMLException, SvcLogicException {
        if (StringUtils.isBlank(propfile)) {
            throw new DGXMLException(propfile + " Profile file is not defined");
        }
        this.store = SvcLogicStoreFactory.getSvcLogicStore(propfile);
    }

    protected DGXMLLoader(SvcLogicStore store) {
        this.store = store;
    }

    public void loadDGXMLFile(String dgXMLpath) throws SvcLogicException {
        if (dgXMLpath != null) {
            SvcLogicParser.load(dgXMLpath, this.store);
        }
    }

    public void loadDGXMLDir(String xmlPath) {
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
                SvcLogicParser.load(file.getCanonicalPath(), store);
            } catch (Exception e) {
                logger.error("Failed to load XML " + file.getCanonicalPath(), e);
                errors.add("Failed to load XML " + file.getCanonicalPath()
                    + ", Exception : " + e.getMessage());
            }
        }
    }
}
