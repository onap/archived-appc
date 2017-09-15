/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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

package org.openecomp.sdnc.dg.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicParser;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;

public class DGXMLLoadNActivate {
    private final static Logger logger = LoggerFactory.getLogger(DGXMLLoadNActivate.class);
    private final SvcLogicStore store;
    public static String STRING_ENCODING = "utf-8";

    public DGXMLLoadNActivate(String propfile) throws Exception{
        if(StringUtils.isBlank(propfile)){
            throw new Exception(propfile + " Profile file is not defined");
        }
        this.store = SvcLogicStoreFactory.getSvcLogicStore(propfile);
    }

    protected DGXMLLoadNActivate(SvcLogicStore store) {
        this.store=store;
    }

    public void loadDGXMLFile(String dgXMLpath) throws SvcLogicException{
        if(dgXMLpath != null ){
            SvcLogicParser.load(dgXMLpath, this.store);
        }
    }

    private void loadDGXMLDir(String xmlPath) throws Exception {
        try {
            logger.info("******************** Loading DG into Database *****************************");
            List<String> errors = new ArrayList<String>();
            if(this.store != null){
                File xmlDir = new File(xmlPath);
                if(xmlDir.isDirectory()){
                    String[] extensions = new String[] { "xml", "XML" };
                    List<File> files = (List<File>) FileUtils.listFiles(xmlDir, extensions, true);
                    for (File file : files) {
                        logger.info("Loading DG XML file :" + file.getCanonicalPath());
                        try{
                            SvcLogicParser.load(file.getCanonicalPath(), this.store);
                        }catch (Exception e) {
                            errors.add("Failed to load XML "+file.getCanonicalPath() + ", Exception : "+e.getMessage());
                        }
                    }
                }else{
                    throw new Exception(xmlPath + " is not a valid XML Directory");
                }
            }else{
                throw new Exception("Failed to initialise SvcLogicStore");
            }

            if(errors.size() > 0){
                throw new Exception(errors.toString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void activateDg(String activateFilePath) throws Exception {
        logger.info("******************** Activating DG into Database *****************************");
        try {
            List<String> errors = new ArrayList<String>();
            if(this.store != null){
                File activateFile = new File(activateFilePath);
                if(activateFile.isFile()){
                    List<String> fileLines = FileUtils.readLines(activateFile,STRING_ENCODING);
                    if(fileLines != null ){
                        for (String line : fileLines) {
                            if(line != null && ! line.trim().startsWith("#")){
                                String lineArray[] = line.trim().split(":");
                                try {
                                    if(lineArray != null && lineArray.length >= 4){
                                        String module = lineArray[0];
                                        String rpc = lineArray[1];
                                        String version = lineArray[2];
                                        String mode = lineArray[3];
                                        if(StringUtils.isNotBlank(module) && StringUtils.isNotBlank(rpc)
                                                && StringUtils.isNotBlank(version) && StringUtils.isNotBlank(mode)){
                                            logger.info("Activating DG :" + line);
                                            SvcLogicGraph graph = this.store.fetch(module, rpc, version, mode);
                                            if(graph != null){
                                                logger.info("Found Graph :" + line + " Activating ...");
                                                this.store.activate(graph);
                                            }else{
                                                throw new Exception("Failed to fetch from Database");
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    errors.add("Failed to Activate "+line + ", "+e.getMessage());
                                }
                            }
                        }
                    }
                }else{
                    throw new Exception(activateFile + " is not a valid Activate file Path");
                }
            }else{
                throw new Exception("Failed to initialise SvcLogicStore");
            }

            if(errors.size() > 0){
                throw new Exception(errors.toString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }


    public static void main(String[] args) {
        try {
            String xmlPath = null;
            String propertyPath = null;
            String activateFile = null;

            if(args != null && args.length >= 3){
                xmlPath = args[0];
                activateFile = args[1];
                propertyPath = args[2];
            }else{
                throw new Exception("Sufficient inputs for DGXMLLoadNActivate are missing <xmlpath> <activatefile> <dbPropertyfile>");
            }

            //propertyPath = "/Users/bs2796/0Source/ecomp/bvc-3.2.2/others/properties/dblib.properties";
            //xmlPath = DGXMLLoadNActivate.class.getClassLoader().getResource(".").getPath() +"/xml" ;

            DGXMLLoadNActivate dgXMLLoadDB = new DGXMLLoadNActivate(propertyPath);
            dgXMLLoadDB.loadDGXMLDir(xmlPath);
            dgXMLLoadDB.activateDg(activateFile);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.exit(1);
        }
    }
}
