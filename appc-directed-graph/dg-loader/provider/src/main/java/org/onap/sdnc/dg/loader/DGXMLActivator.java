package org.onap.sdnc.dg.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DGXMLActivator {

    private static final Logger logger = LoggerFactory.getLogger(DGXMLActivator.class);
    private static final String STRING_ENCODING = "utf-8";
    private final SvcLogicStore store;

    public DGXMLActivator(String propertiesFile) throws DGXMLException, SvcLogicException {
        if (StringUtils.isBlank(propertiesFile)) {
            throw new DGXMLException(propertiesFile + " Profile file is not defined");
        }
        this.store = SvcLogicStoreFactory.getSvcLogicStore(propertiesFile);
    }

    protected DGXMLActivator(SvcLogicStore store) {
        this.store = store;
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

            if (errors.isEmpty()) {
                throw new DGXMLException(errors.toString());
            }
        } catch (Exception e) {
            logger.error("Failed to activate DG", e);
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
            if (lineArray.length >= 4) {
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
            logger.info(
                "Found Graph :" + line + " Activating ...");
            this.store.activate(graph);
        } else {
            throw new DGXMLException("Failed to fetch from Database");
        }
    }

}
