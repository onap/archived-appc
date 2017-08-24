package org.openecomp.appc.ccadaptor;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TestXmlUtil {

	@Test
	public void testXml() {
		Map<String, String> varmap = new HashMap<String, String>();
		varmap.put("network.data", "ipv4");
		String xmlData = XmlUtil.getXml(varmap, "network");
		Assert.assertEquals("<data>ipv4</data>\n", xmlData);
		System.out.println(xmlData);
	}
}
