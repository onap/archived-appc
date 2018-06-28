/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia Solutions and Networks
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ============LICENSE_END=========================================================
 */


package org.onap.appc.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.onap.appc.util.StringHelper;


public class StringHelperTest {

    //TODO write more tests for convertToRegex method

    @Test
    public void convertToRegex_should_return_regex_matching_all_string_when_given_null_or_empty_string(){
        assertEquals(".*", StringHelper.convertToRegex(null));
        assertEquals(".*", StringHelper.convertToRegex(""));
        assertEquals(".*", StringHelper.convertToRegex("  "));
    }
    
    @Test
    public void convertToRegex_should_return_proper_regex_when_we_provide_a_proper_string_expression(){
        String expected=".*test\\.jpg.test123\\.jpeg$";
        assertEquals(expected,StringHelper.convertToRegex("*test.jpg+test123.jpeg"));
    }
    
    @Test
    public void test_ResolveToType_with_null_as_input(){
        assertNull(StringHelper.resolveToType(null));
    }
    
    @Test
    public void test_ResolveToType_with_integer_as_input(){
    	Integer expected=-112;
    	assertEquals(expected,StringHelper.resolveToType("-112"));
    }
    
    @Test
    public void test_ResolveToType_with_double_as_input(){
    	Double expected=-112.12;
    	assertEquals(expected,StringHelper.resolveToType("-112.12"));
    }
    
    @Test
    public void test_ResolveToType_with_boolean_as_input(){
    	Boolean expected=true;
    	assertEquals(expected,StringHelper.resolveToType("true"));
    }
  
    @Test
    public void test_ResolveToType_with_date_as_input(){
       assertTrue(StringHelper.resolveToType("1994-11-05T08:15:30-05:00") instanceof Date);
    }
    

    @Test
    public void getShortenedString_should_return_null_when_given_null(){
        assertNull(StringHelper.getShortenedString(null, 2));
    }

    @Test
    public void getShortenedString_should_return_given_string_when_length_is_lower_than_4(){
        assertEquals("str", StringHelper.getShortenedString("str", 3));
        assertEquals("str", StringHelper.getShortenedString("str", 2));
        assertEquals("test", StringHelper.getShortenedString("test", 3));
    }

    @Test
    public void getShortenedString_should_shorten_string_and_append_ellipsis(){

        assertEquals("s...", StringHelper.getShortenedString("sample", 4));
        assertEquals("test...", StringHelper.getShortenedString("test string", 7));
    }

    @Test
    public void isNotNullNotEmpty_should_return_true_if_string_is_not_null_or_not_empty(){
        assertFalse(StringHelper.isNotNullNotEmpty(null));
        assertFalse(StringHelper.isNotNullNotEmpty(""));
        assertFalse(StringHelper.isNotNullNotEmpty(" "));
        assertTrue(StringHelper.isNotNullNotEmpty("test"));
    }

    @Test
    public void isNullOrEmpty_should_return_true_if_string_is_null_or_empty(){
        assertTrue(StringHelper.isNullOrEmpty(null));
        assertTrue(StringHelper.isNullOrEmpty(""));
        assertTrue(StringHelper.isNullOrEmpty(" "));
        assertFalse(StringHelper.isNullOrEmpty("test"));
    }

    @Test
    public void areEqual_should_return_true_when_both_null(){
        assertTrue(StringHelper.areEqual(null, null));
    }

    @Test
    public void areEqual_should_return_false_when_one_string_is_null(){
        assertFalse(StringHelper.areEqual(null, "test"));
    }

    @Test
    public void areEqual_should_compare_two_string(){
        assertFalse(StringHelper.areEqual("test2", "test"));
        assertFalse(StringHelper.areEqual("test", "Test"));
    }

    @Test
    public void equalsIgnoreCase_should_compare_two_string_case_insensitive(){
        assertFalse(StringHelper.equalsIgnoreCase("test2", "test"));
        assertTrue(StringHelper.equalsIgnoreCase("test", "Test"));
    }

    @Test
    public void mangleName_should_pad_string_when_given_null_or_empty(){
        assertEquals("aaaa", StringHelper.mangleName(null, 3, 6));
        assertEquals("aaaa", StringHelper.mangleName(StringUtils.EMPTY, 3, 6));

        assertEquals("aa", StringHelper.mangleName(null, 1, 6));
        assertEquals("aa", StringHelper.mangleName(StringUtils.EMPTY, 1, 6));

        assertEquals("aaaaaaaaa", StringHelper.mangleName(null, 8, 12));
        assertEquals("aaaaaaaaa", StringHelper.mangleName(StringUtils.EMPTY, 8, 12));

    }

    @Test
    public void mangleName_should_remove_all_illegal_characters(){
        assertEquals("ab45", StringHelper.mangleName("ab45 ", 3, 6));
        assertEquals("ab45", StringHelper.mangleName("ab!45", 3, 6));
        assertEquals("ab45", StringHelper.mangleName("a b!45", 3, 6));
    }

    @Test
    public void mangleName_should_convert_all_character_to_lowercase(){
        assertEquals("test", StringHelper.mangleName("TeSt", 3, 6));
        assertEquals("abb45fgr", StringHelper.mangleName("abB!4 5FGR", 6, 8));
    }

    @Test
    public void mangleName_should_pad_string_when_result_is_too_short(){
        assertEquals("testaaa", StringHelper.mangleName("TeSt", 6, 10));
        assertEquals("abb45fgraaaaa", StringHelper.mangleName("abB!4 5FGR", 12, 15));
    }

    @Test
    public void mangleName_should_truncate_string_when_too_long(){
        assertEquals("tst", StringHelper.mangleName("TeSt", 0, 3));
        assertEquals("tt", StringHelper.mangleName("TeSt", 0, 2));
        assertEquals("agr", StringHelper.mangleName("abb45fgr", 0, 3));
        assertEquals("abgr", StringHelper.mangleName("abb45fgr", 0, 4));
    }

    @Test
    public void normalizeString_should_return_null_when_given_null_or_empty_string(){
        assertNull(StringHelper.normalizeString(null));
        assertNull(StringHelper.normalizeString(StringUtils.EMPTY));
    }

    @Test
    public void normalizeString_should_trim_string(){
        assertEquals("this is test sequence",
            StringHelper.normalizeString("  this is test sequence "));
        assertEquals("this  is test   sequence",
            StringHelper.normalizeString("  this  is test   sequence   "));
    }

    @Test
    public void stripCRLFs_should_return_null_when_given_null() {
        assertNull(StringHelper.stripCRLF(null));
    }

    @Test
    public void stripCRLF_should_strip_all_CRLF_and_LF() {
        assertEquals(StringUtils.EMPTY, StringHelper.toUnixLines(StringUtils.EMPTY));
        assertEquals("this is test sequence", StringHelper.stripCRLF("this is test sequence"));
        assertEquals("this is testsequence", StringHelper.stripCRLF("this is test\nsequence"));
        assertEquals("this istestsequence", StringHelper.stripCRLF("this is\ntest\r\nsequence"));
        assertEquals("this istestsequence", StringHelper.stripCRLF("this is\r\ntest\n\rsequence"));
    }

    @Test
    public void toDOSLines_should_return_null_when_given_null() {
        assertNull(StringHelper.toDOSLines(null));
    }

    @Test
    public void toUnixLines_should_replace_LF_with_CRLF() {
        assertEquals(StringUtils.EMPTY, StringHelper.toUnixLines(StringUtils.EMPTY));
        assertEquals("this is test sequence", StringHelper.toDOSLines("this is test sequence"));
        assertEquals("this is test\r\nsequence", StringHelper.toDOSLines("this is test\nsequence"));
        assertEquals("this is test\rsequence", StringHelper.toDOSLines("this is test\rsequence"));
        assertEquals("this is\r\ntest\n\rsequence", StringHelper.toDOSLines("this is\r\ntest\n\rsequence"));
    }

    @Test
    public void toUnixLines_should_return_null_when_given_null() {
        assertNull(StringHelper.toUnixLines(null));
    }

    @Test
    public void toUnixLines_should_replace_CRLF_with_LF() {
        assertEquals(StringUtils.EMPTY, StringHelper.toUnixLines(StringUtils.EMPTY));
        assertEquals("this is test sequence", StringHelper.toUnixLines("this is test sequence"));
        assertEquals("this is test\nsequence", StringHelper.toUnixLines("this is test\nsequence"));
        assertEquals("this is\ntest\nsequence", StringHelper.toUnixLines("this is\r\ntest\n\rsequence"));
    }

    @Test
    public void translate_should_return_null_when_given_null_sequence() {
        assertNull(StringHelper.translate(null, "abc", "def"));
    }

    @Test
    public void translate_should_translate_sequence() {

        assertEquals(StringUtils.EMPTY, StringHelper.translate(StringUtils.EMPTY, "abc", "def"));
        assertEquals("ahis is absa sbqubccb",
            StringHelper.translate("this is test sequence", "ten", "abc"));
    }


    @Test
    public void translate_should_translate_sequence_given_replacement_longer_then_match() {
        assertEquals("ahis is absa sbqubccb",
            StringHelper.translate("this is test sequence", "ten", "abcde"));
    }

    @Test
    public void translate_should_translate_sequence_given_replacement_shorter_then_match() {
        assertEquals("ahas as absa sbqubccb",
            StringHelper.translate("this is test sequence", "teni", "abc"));
    }

    @Test
    public void validIdentifier_should_return_null_when_given_null() {
        assertNull(StringHelper.validIdentifier(null));
    }


    @Test
    public void validIdentifier_should_return_valid_identifier() {
        assertEquals(StringUtils.EMPTY, StringHelper.validIdentifier(StringUtils.EMPTY));
        assertEquals("abcd", StringHelper.validIdentifier("abcd"));
        assertEquals("abc_", StringHelper.validIdentifier("abc!"));
        assertEquals("ab_cd", StringHelper.validIdentifier("ab cd"));
        assertEquals("ab_cd_", StringHelper.validIdentifier("ab cd!"));
    }

    @Test
    public void verify_should_return_null_when_given_null_sequence() {
        assertNull(StringHelper.verify(null, "abc", 'r'));
    }

    @Test
    public void verify_should_return_empty_string_when_given_empty_sequence() {
        assertEquals(StringUtils.EMPTY, StringHelper.verify("", "abc", 'r'));
    }

    @Test
    public void verify_should_replace_illegal_characters() {
        assertEquals("trir ir tert rerterre",
            StringHelper.verify("this is test sentence", "iet ", 'r'));
    }

    @Test
    public void toList_should_return_empty_string_when_given_null_or_empty_list() {
        assertEquals(StringUtils.EMPTY, StringHelper.asList((List<String>) null));
        assertEquals(StringUtils.EMPTY, StringHelper.asList((Lists.newArrayList())));
    }

    @Test
    public void toList_should_return_element_when_given_one_element_list() {
        assertEquals("test", StringHelper.asList(Lists.newArrayList("test")));
    }

    @Test
    public void toList_should_convert_to_string_given_list() {
        assertEquals("test, test2, test3",
            StringHelper.asList(Lists.newArrayList("test", "test2", "test3")));
    }

    @Test
    public void toList_should_return_empty_string_when_given_null_or_empty_map() {
        assertEquals(StringUtils.EMPTY, StringHelper.asList((Map<String, String>) null));
        assertEquals(StringUtils.EMPTY, StringHelper.asList((Maps.newHashMap())));
    }

    @Test
    public void toList_should_return_entry_when_given_one_entry_map() {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("key1", "value1");

        assertEquals("key1=value1", StringHelper.asList(testMap));
    }

    @Test
    public void toList_should_convert_to_string_given_map() {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("key1", "value1");
        testMap.put("key2", "value2");

        assertEquals("key1=value1, key2=value2", StringHelper.asList(testMap));
    }

    @Test
    public void toList_should_return_string_representation_of_empty_array_when_given_null() {
        String value = StringHelper.asList((String[]) null);
        assertNotNull(value);
        assertEquals("[]", value);
    }

    @Test
    public void toList_should_return_string_representation_of_empty_array_when_given_empty_array() {
        String value = StringHelper.asList(new String[]{});
        assertNotNull(value);
        assertEquals("[]", value);
    }

    @Test
    public void toList_should_return_string_representation_of_one_element_array() {
        String value = StringHelper.asList("one");
        assertNotNull(value);
        assertEquals("[one]", value);
    }

    @Test
    public void toList_should_return_string_representation_of_array() {
        String value = StringHelper.asList("one", "two", "three", "four", "five");
        assertNotNull(value);
        assertEquals("[one,two,three,four,five]", value);
    }

    @Test
    public void propertiesToString_should_return_null_when_given_null_properties() {

        assertEquals(null, StringHelper.propertiesToString(null));
    }

    @Test
    public void propertiesToString_should_return_string_representation_of_empty_array_when_given_empty_properties() {

        Properties props = new Properties();

        String result = StringHelper.propertiesToString(props);
        assertNotNull(result);
        assertEquals("[ ]", result);
    }

    @Test
    public void propertiesToString_should_return_convert_properties_to_string() {
        Properties props = new Properties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");

        String result = StringHelper.propertiesToString(props);

        assertTrue(result.startsWith("[ "));
        assertTrue(result.contains("key1"));
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("key2"));
        assertTrue(result.contains("value2"));
        assertTrue(result.endsWith(" ]"));
        assertTrue(result.lastIndexOf(",") < result.length() - 3);
    }
}
