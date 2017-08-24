package org.openecomp.appc.ccadaptor;

import org.junit.Test;

public class TestDebugLog {

	@Test
	public void testAppendToFile() {
		DebugLog.appendToFile("appendData");
	}

	@Test
	public void TestGetDateTime() {
		String DateTime = DebugLog.getDateTime();
		System.out.println(DateTime);

	}

}
