/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia Solutions and Networks
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
package org.onap.appc.encryption;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class HexHelperTest {

    private final List<Character> hexChars =
        newArrayList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B','C', 'D', 'E', 'F');

    @Test(expected = EncryptionException.class)
    public void convertHexToBytes_should_throw_given_null() throws EncryptionException {

        HexHelper.convertHexToBytes(null);
    }

    @Test(expected = EncryptionException.class)
    public void convertHexToBytes_should_throw_given_non_hexadecimal_string() throws EncryptionException {

        HexHelper.convertHexToBytes("125FET4A");
    }

    @Test
    public void convertHexToBytes_should_convert_hexadecimal_string_to_byte_array() throws EncryptionException {

        byte[] result = HexHelper.convertHexToBytes("125FE4A");

        assertNotEquals(0, result.length);
    }


    @Test(expected = EncryptionException.class)
    public void convertBytesToHex_should_throw_given_null() throws EncryptionException {

        HexHelper.convertBytesToHex(null);
    }


    @Test
    public void convertBytesToHex_should_convert_byte_array_to_hexadecimal_string() throws EncryptionException {

        String resultString = HexHelper.convertBytesToHex(new byte[]{24, -1, 85, 99});
        for (char ch : resultString.toCharArray()) {
            assertTrue(hexChars.contains(ch));
        }

        byte[] resultArray = HexHelper.convertHexToBytes("A56C9ED17");
        assertEquals("0A56C9ED17", HexHelper.convertBytesToHex(resultArray));
    }

}
