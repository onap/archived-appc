package org.openecomp.appc.ccadaptor;

import org.junit.Assert;
import org.junit.Test;

public class TestSshJcraftWrapper {
	
	@Test
	public void TestCheckIfReceivedStringMatchesDelimeter(){
		SshJcraftWrapper wrapper = new SshJcraftWrapper();
		wrapper.getTheDate();
		boolean result = wrapper.checkIfReceivedStringMatchesDelimeter("#", "config#", "config#");
		System.out.println(result);
	}
	
	@Test
	public void testRemoveWhiteSpaceAndNewLineCharactersAroundString(){
		SshJcraftWrapper wrapper = new SshJcraftWrapper();
		String nameSpace = wrapper.removeWhiteSpaceAndNewLineCharactersAroundString("namespace");
		Assert.assertEquals("namespace", nameSpace);
	}
	
}
