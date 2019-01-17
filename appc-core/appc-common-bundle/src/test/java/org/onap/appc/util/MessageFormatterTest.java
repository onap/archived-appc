/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class MessageFormatterTest {

    @Test
    public void format_should_return_empty_string_when_given_null_or_empty_message_template() {
        assertEquals(StringUtils.EMPTY, MessageFormatter.format(null, Maps.newHashMap()));
        assertEquals(StringUtils.EMPTY, MessageFormatter.format(StringUtils.EMPTY, Maps.newHashMap()));
    }

    @Test
    public void should_return_same_string_when_given_null_or_empty_params() {
        String message = "message";

        assertEquals(message, MessageFormatter.format(message, null));
        assertEquals(message, MessageFormatter.format(message, Maps.newHashMap()));
    }

    @Test
    public void should_return_same_string_when_given_non_dollar_string() {
        String msg = "vnfid";

        Map<String, Object> respMsg = new HashMap<>();
        respMsg.put("vnfid", "SYNC_NEW201");

        assertEquals(msg, MessageFormatter.format(msg, respMsg));
    }


    @Test
    public void should_replace_dollar_sign_statement_with_map_value() {
        String message = "${vnfid} some sample text ${pnfid} additional sample text";

        Map<String, Object> respMsg = new HashMap<>();
        respMsg.put("vnfid", "SYNC_NEW201");
        respMsg.put("pnfid", "TEST-ID");

        assertEquals("SYNC_NEW201 some sample text TEST-ID additional sample text",
            MessageFormatter.format(message, respMsg));
    }

    @Test
    public void getParamsNamesList_should_return_null_when_given_null_or_empty_message_template() {
        assertEquals(null, MessageFormatter.getParamsNamesList(null));
        assertEquals(null, MessageFormatter.getParamsNamesList(StringUtils.EMPTY));

        assertEquals(null, MessageFormatter.getParamsNamesSet(null));
        assertEquals(null, MessageFormatter.getParamsNamesSet(StringUtils.EMPTY));
    }

    @Test
    public void should_recognize_params_inside_message_string() {
        String message = "${vnfid} some sample text ${pnfid} additional sample text";

        List<String> resultList = MessageFormatter.getParamsNamesList(message);

        assertEquals(2, resultList.size());
        assertTrue(resultList.contains("vnfid"));
        assertTrue(resultList.contains("pnfid"));

        Set<String> resultSet = MessageFormatter.getParamsNamesSet(message);

        assertEquals(2, resultList.size());
        assertTrue(resultSet.contains("vnfid"));
        assertTrue(resultSet.contains("pnfid"));
    }
}
