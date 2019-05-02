package org.onap.sdnc.config.generator.tool;

import org.codehaus.jettison.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

public class TestJSONTool {
    
    @Test
    public void testEscapeInternalJson() {
        String testData = "{\"test1\":\"value1\",\"test2\":\"{\"key1\":\"value\"}\"}";
        String expectedOutput = "{\"test1\":\"value1\",\"test2\":\"{\\\"key1\\\":\\\"value\\\"}\"}";
        try {
            Assert.assertEquals(expectedOutput, JSONTool.escapeInternalJson(testData));
        } catch (JSONException e) {
            Assert.fail();
        }
    }
    
    @Test
    public void testEscapeInternalJson_alreadyEscaped() {
        String testData = "{\"test1\":\"value1\",\"test2\":\"{\\\"key1\\\":\\\"value\\\"}\"}";
        String expectedOutput = "{\"test1\":\"value1\",\"test2\":\"{\\\"key1\\\":\\\"value\\\"}\"}";
        try {
            Assert.assertEquals(expectedOutput, JSONTool.escapeInternalJson(testData));
        } catch (JSONException e) {
            Assert.fail();
        }
    }
    
    @Test
    public void testEscapeInternalJson_withNewLines() {
        String testData = "{\"test1\":\"value1\",\"test2\":\"\n{\"key1\":\"value\"\n}\"}";
        String expectedOutput = "{\"test1\":\"value1\",\"test2\":\"\n{\\\"key1\\\":\\\"value\\\"\n}\"}";
        try {
            Assert.assertEquals(expectedOutput, JSONTool.escapeInternalJson(testData));
        } catch (JSONException e) {
            Assert.fail();
        }
    }

}
