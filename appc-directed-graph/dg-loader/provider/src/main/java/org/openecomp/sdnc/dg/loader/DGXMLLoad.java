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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicParser;
import org.openecomp.sdnc.sli.SvcLogicStore;
import org.openecomp.sdnc.sli.SvcLogicStoreFactory;

public class DGXMLLoad {
    private final static Logger logger = LoggerFactory.getLogger(DGXMLLoad.class);
    private final SvcLogicStore store;
    public static String STRING_ENCODING = "utf-8";

    public DGXMLLoad(String propfile) throws Exception{
        if(StringUtils.isBlank(propfile)){
            throw new Exception(propfile + " Profile file is not defined");
        }
        this.store = SvcLogicStoreFactory.getSvcLogicStore(propfile);
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
                if(xmlDir != null && xmlDir.isDirectory()){
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

    public static void main(String[] args) {
        try {
            String xmlPath = null;
            String propertyPath = null;

            if(args != null && args.length >= 2){
                xmlPath = args[0];
                propertyPath = args[1];
            }else{
                throw new Exception("Sufficient inputs for DGXMLLoadNActivate are missing <xmlpath> <dbPropertyfile>");
            }

            DGXMLLoad dgXMLLoadDB = new DGXMLLoad(propertyPath);
            dgXMLLoadDB.loadDGXMLDir(xmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.exit(1);
        }
    }

}
