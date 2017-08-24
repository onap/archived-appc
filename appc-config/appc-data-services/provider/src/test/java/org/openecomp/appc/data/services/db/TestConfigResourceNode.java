package org.openecomp.appc.data.services.db;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.openecomp.appc.data.services.AppcDataServiceConstant;
import org.openecomp.appc.data.services.db.DGGeneralDBService;
import org.openecomp.appc.data.services.node.ConfigResourceNode;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.After;
import org.junit.Before;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.SvcLogicResource.QueryStatus;
import org.openecomp.sdnc.sli.resource.sql.SqlResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;

public class TestConfigResourceNode {
	
	@Test(expected = Exception.class)
	public void testGetUploadConfig1() throws SvcLogicException{
	SvcLogicContext ctx = new SvcLogicContext();
	ctx.setAttribute("test","test");
		ConfigResourceNode dbService = new ConfigResourceNode() ;
		Map<String,String> map = new HashMap<String,String>();
	     dbService.getConfigFileReference(map,ctx);
		//System.out.println(status);
	
		
	}

@Test(expected = Exception.class)

public void testGetUploadConfig2() throws SvcLogicException{
SvcLogicContext ctx = new SvcLogicContext();
ctx.setAttribute("test","test");
	ConfigResourceNode dbService = new ConfigResourceNode() ;
	Map<String,String> map = new HashMap<String,String>();
     dbService.getTemplate(map,ctx);
	//System.out.println(status);
}

@Test(expected = Exception.class)

public void testGetUploadConfig3() throws SvcLogicException{
SvcLogicContext ctx = new SvcLogicContext();
ctx.setAttribute("test","test");
	ConfigResourceNode dbService = new ConfigResourceNode() ;
	Map<String,String> map = new HashMap<String,String>();
     dbService.getVnfcReference(map, ctx);
	//System.out.println(status);

	
}

@Test(expected = Exception.class)

public void testGetUploadConfig4() throws SvcLogicException{
SvcLogicContext ctx = new SvcLogicContext();
ctx.setAttribute("test","test");
	ConfigResourceNode dbService = new ConfigResourceNode() ;
	Map<String,String> map = new HashMap<String,String>();
     dbService.getSmmChainKeyFiles(map, ctx);
	//System.out.println(status);

	
}

@Test(expected = Exception.class)

public void testGetUploadConfig5() throws SvcLogicException{
SvcLogicContext ctx = new SvcLogicContext();
ctx.setAttribute("test","test");
	ConfigResourceNode dbService = new ConfigResourceNode() ;
	Map<String,String> map = new HashMap<String,String>();
     dbService.getDownloadConfigTemplateByVnf(map, ctx);
	//System.out.println(status);

	
}

@Test(expected = Exception.class)
public void testGetUploadConfig6() throws SvcLogicException{
SvcLogicContext ctx = new SvcLogicContext();
ctx.setAttribute("test","test");
	ConfigResourceNode dbService = new ConfigResourceNode() ;
	Map<String,String> map = new HashMap<String,String>();
     dbService.getCommonConfigInfo(map, ctx);
	//System.out.println(status);

	
}

@Test(expected = Exception.class)
public void testGetUploadConfig7() throws SvcLogicException{
SvcLogicContext ctx = new SvcLogicContext();
ctx.setAttribute("test","test");
	ConfigResourceNode dbService = new ConfigResourceNode() ;
	Map<String,String> map = new HashMap<String,String>();
     dbService.updateUploadConfig(map, ctx);
	//System.out.println(status);

	
}


}
