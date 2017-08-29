/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
 * ================================================================================
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

package org.openecomp.sdnc.dg.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdnc.sli.SvcLogicGraph;
import org.openecomp.sdnc.sli.SvcLogicStore;
import org.openecomp.sdnc.sli.SvcLogicStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DGXMLActivate {

    private final static Logger logger = LoggerFactory.getLogger(DGXMLLoadNActivate.class);
    private final SvcLogicStore store;
    public static String STRING_ENCODING = "utf-8";

    public DGXMLActivate(String propfile) throws Exception{
        if(StringUtils.isBlank(propfile)){
            throw new Exception(propfile + " Profile file is not defined");
        }
        this.store = SvcLogicStoreFactory.getSvcLogicStore(propfile);
    }


    public void activateDg(String activateFilePath) throws Exception {
        logger.info("******************** Activating DG into Database *****************************");
        try {
            List<String> errors = new ArrayList<String>();
            if(this.store != null){
                File activateFile = new File(activateFilePath);
                if(activateFile != null && activateFile.isFile()){
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
            String activateFile = null;
            String propertyPath = null;

            if(args != null && args.length >= 2){
                activateFile = args[0];
                propertyPath = args[1];
            }else{
                throw new Exception("Sufficient inputs for DGXMLActivate are missing <activatefile> <dbPropertyfile>");
            }

            DGXMLActivate dgXmlActivate = new DGXMLActivate(propertyPath);
            dgXmlActivate.activateDg(activateFile);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.exit(1);
        }
    }

}
