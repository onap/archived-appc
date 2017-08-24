package org.openecomp.appc.design.validator;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.design.services.util.ArtifactHandlerClient;

import junit.framework.Assert;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class TestArifactHandlerClient {
	
	@Test
	public void testCreateArtifactData(){
		try{
		String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
		String payload = " { \"userID\": \"00000\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  "  
		+ content + 
		 " } ";
		String requestID ="0000";
		ArtifactHandlerClient ahi = new ArtifactHandlerClient();
		String value =  ahi.createArtifactData(payload, requestID);
		System.out.println(value);
		Assert.assertTrue(!value.isEmpty());;
		
		
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	public void testExecute() throws Exception{
		try{
		String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
		String payload = " { \"userID\": \"00000\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  "  
		+ content + 
		 " } ";
		String rpc = "Test_Configure";
		
			ArtifactHandlerClient ahi = new ArtifactHandlerClient();
		 ahi.execute(payload, rpc);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
