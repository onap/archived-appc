package org.openecomp.appc.ccadaptor;

import org.junit.Assert;
import org.junit.Test;

public class TestEncryptionTool {

	@Test
	public void testEncrypt() {
		EncryptionTool tool = EncryptionTool.getInstance();
		String value = tool.encrypt("encrypt");
		Assert.assertEquals("enc:JjEZHlg7VQ==", value);
		System.out.println(value);
	}

	@Test
	public void testDecrypt() {
		EncryptionTool tool = EncryptionTool.getInstance();
		String value = tool.decrypt("enc:JjEZHlg7VQ==");
		Assert.assertEquals("encrypt", value);
		System.out.println(value);
	}

}
