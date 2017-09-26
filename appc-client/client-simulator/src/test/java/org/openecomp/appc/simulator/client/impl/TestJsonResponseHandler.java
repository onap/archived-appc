package org.openecomp.appc.simulator.client.impl;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Assert;
import org.junit.Ignore;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TestJsonResponseHandler {
    String folder="/data/output/error.json";
    JsonResponseHandler responseHandler=new JsonResponseHandler();

    @Ignore
    public void testOnResponse() throws URISyntaxException, IOException{
        responseHandler.onResponse(getNode());
        List<File> files=getJsonFiles(folder);
        Assert.assertNotNull(files);

    }

    private String readData(String inputFile) throws URISyntaxException, IOException {
        File file = new File(this.getClass().getResource(inputFile).toURI());

        byte[] bFile = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(bFile);
        fileInputStream.close();
        return new String(bFile);
    }

    private String getNode() throws java.io.IOException{
        String jsonSring="{\"status\": {\"code\": \"200\"}}";
        return  jsonSring;
}
    public  List<File> getJsonFiles(String folder) throws FileNotFoundException {
        Path dir = Paths.get(folder);
        FileFilter fileFilter = new WildcardFileFilter("*.error");
        return new ArrayList<File>(Arrays.asList(dir.toFile().listFiles(fileFilter)));
    }
}
