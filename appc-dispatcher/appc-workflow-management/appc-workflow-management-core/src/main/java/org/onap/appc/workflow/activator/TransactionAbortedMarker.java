/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.workflow.activator;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TransactionAbortedMarker implements Runnable {

    private ScheduledExecutorService executor = null;

    public static final String PREFIX = "APPC";
    public static final String SUFFIX = "VM";
    private final EELFLogger logger = EELFManager.getInstance().getLogger(TransactionAbortedMarker.class);

    public TransactionAbortedMarker(ScheduledExecutorService executor){
        this.executor = executor;
    }

    @Override
    public void run() {
        try {
            TransactionRecorder recorder = lookupTransactionRecorder();

            File newAppcInstanceIdFile = File.createTempFile(PREFIX, SUFFIX);
            File parentDirectory = newAppcInstanceIdFile.getParentFile();
            if (logger.isDebugEnabled()) {
                logger.debug("New instance id file path" + newAppcInstanceIdFile.getAbsolutePath());
            }

            File[] allInstanceIdFiles = getAllInstanceIdFiles(parentDirectory);

            if (allInstanceIdFiles.length > 0) {
                File lastModifiedFile = getLastModifiedFile(allInstanceIdFiles);
                if (logger.isDebugEnabled()) {
                    logger.debug("Last Modified File" + lastModifiedFile.getName());
                }
                String prevAppcInstanceId = readInstanceId(lastModifiedFile);
                recorder.markTransactionsAborted(prevAppcInstanceId);
                boolean isFileDeleted = lastModifiedFile.delete();
                logger.debug("Previous file deleted  " + isFileDeleted);
            }
            String newAppcInstanceId = writeNewInstanceId(newAppcInstanceIdFile);
            recorder.setAppcInstanceId(newAppcInstanceId);
        } catch (TransactionRecorderServiceNotFoundException e) {
            logger.warn("Transaction Recorder Service Not Found, Next attempt after 30 seconds");
            executor.schedule(this,30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error on workflow manager bundle start-up" + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }



    protected TransactionRecorder lookupTransactionRecorder() throws TransactionRecorderServiceNotFoundException {
        String message = null;
        BundleContext bctx = FrameworkUtil.getBundle(TransactionRecorder.class).getBundleContext();
        if(bctx!=null){
            ServiceReference sref= bctx.getServiceReference(TransactionRecorder.class.getName());
            TransactionRecorder transactionRecorder;
            if (sref != null) {
                transactionRecorder = (TransactionRecorder) bctx.getService(sref);
                if (transactionRecorder != null) {
                    return transactionRecorder;
                }
            }
        }
        message = "Cannot find service org.onap.appc.transactionrecorder.TransactionRecorder";
        logger.warn(message);
        throw new TransactionRecorderServiceNotFoundException(message);
    }

    private String writeNewInstanceId(File newInstanceIdFile) throws IOException {
        String newAppcInstanceId = UUID.randomUUID().toString();
        try (FileWriter writer = new FileWriter(newInstanceIdFile)) {
            writer.write(newAppcInstanceId);
        }
        catch (IOException e){
            String message = "Error writing appc-instance-id";
            logger.error(message,e);
            throw new RuntimeException(message);
        }
        logger.debug("new appc-instance-id = " + newAppcInstanceId);
        return newAppcInstanceId;
    }

    private String readInstanceId(File lastModifiedFile) {
        String prevAppcInstanceId = null;
        BufferedReader buffReader;
        try (FileReader reader = new FileReader(lastModifiedFile)) {
            buffReader = new BufferedReader(reader);
            prevAppcInstanceId = buffReader.readLine();
        }
        catch (IOException e){
            String message ="Error reading previous appc-instance-id";
            logger.error(message,e);
            throw new RuntimeException(message);
        }
        logger.debug("previous appc-instance-id " + prevAppcInstanceId);
        return prevAppcInstanceId;
    }

    private File[] getAllInstanceIdFiles(File directory) {
        return directory.listFiles(pathname -> {
            if (pathname.getName().startsWith(PREFIX)
                    && pathname.getName().endsWith(SUFFIX)
                    && pathname.length()>0)
                return true;
            return false;
        });
    }

    private File getLastModifiedFile(File[] allInstanceIdFiles) {
        File lastModifiedFile = allInstanceIdFiles[0];
        long lastModified = allInstanceIdFiles[0].lastModified();
        for(File file:allInstanceIdFiles){
            if(file.lastModified() > lastModified){
                lastModified = file.lastModified();
                lastModifiedFile = file;
            }
        }
        return lastModifiedFile;
    }
}
