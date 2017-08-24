package org.openecomp.appc.design.validator;

import org.junit.Test;
import org.openecomp.appc.design.services.util.EscapeUtils;

public class TestEscapeUtils {

	@Test
	public void EscapeUtils(){
		
		EscapeUtils escapeUtils = new EscapeUtils();
		
		String str = escapeUtils.escapeSql("\\'Test Data\\'");
		
		System.out.println(str);
		
		
	}
}
