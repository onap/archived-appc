/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.encryption;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HexHelper utility used for encryption/decryption
 */
public final class HexHelper {

    @SuppressWarnings({
        "javadoc", "nls"
    })
    public static final String CM_PATH = "@(#) [viewpath]/[item]";

    @SuppressWarnings({
        "nls", "javadoc"
    })
    public static final String CM_PROJECT = "@(#) [environment] [baseline]";

    @SuppressWarnings({
        "javadoc", "nls"
    })
    public static final String CM_VERSION = "@(#) [version] [crtime]";

    private static final char[] HEX_TABLE = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F' };

    /**
     * The logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HexHelper.class);

    private static Map<Character, Integer> TextToHex;

    static {
        TextToHex = new HashMap<>();
        TextToHex.put(Character.valueOf('0'), Integer.valueOf(0));
        TextToHex.put(Character.valueOf('1'), Integer.valueOf(1));
        TextToHex.put(Character.valueOf('2'), Integer.valueOf(2));
        TextToHex.put(Character.valueOf('3'), Integer.valueOf(3));
        TextToHex.put(Character.valueOf('4'), Integer.valueOf(4));
        TextToHex.put(Character.valueOf('5'), Integer.valueOf(5));
        TextToHex.put(Character.valueOf('6'), Integer.valueOf(6));
        TextToHex.put(Character.valueOf('7'), Integer.valueOf(7));
        TextToHex.put(Character.valueOf('8'), Integer.valueOf(8));
        TextToHex.put(Character.valueOf('9'), Integer.valueOf(9));
        TextToHex.put(Character.valueOf('A'), Integer.valueOf(10));
        TextToHex.put(Character.valueOf('B'), Integer.valueOf(11));
        TextToHex.put(Character.valueOf('C'), Integer.valueOf(12));
        TextToHex.put(Character.valueOf('D'), Integer.valueOf(13));
        TextToHex.put(Character.valueOf('E'), Integer.valueOf(14));
        TextToHex.put(Character.valueOf('F'), Integer.valueOf(15));
    }

    /**
     * Default private constructor prevents instantiation
     */
    private HexHelper() {
        // no-op
    }

    /**
     * Converts an array of bytes to the equivalent string representation using hexadecimal notation and returning that
     * representation in a StringBuffer.
     * 
     * @param bytes
     *            The bytes to be converted to a hexadecimal string
     * @return The string representation
     */
    public static StringBuffer convertBytesToHexSB(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        int byteLen = bytes.length;
        for (int index = 0; index < byteLen; index++) {
            char tempChar;
            // Get the first 4 bits (high) Do bitwise logical AND to get rid of
            // low nibble. Shift results to right by 4 and get char
            // representation
            tempChar = HEX_TABLE[((bytes[index] & 0xf0) >>> 4)];
            sb.append(tempChar);

            // Get the last 4 bits (low) Do bitwise logical AND to get rid of
            // high nibble. Get char representation
            tempChar = HEX_TABLE[(bytes[index] & 0x0f)];
            sb.append(tempChar);
        }
        return sb;
    }

    /**
     * Converts a hexadecimal string representation of a binary value to an array of bytes
     * 
     * @param hexValue
     *            The hex representation string to be converted
     * @return The array of bytes that contains the binary value
     */
    @SuppressWarnings("nls")
    public static byte[] convertHexToBytes(String hexValue) {
        byte[] bytes = null;
        byte high;
        byte low;
        char hexChar;

        StringBuffer buffer = new StringBuffer(hexValue.toUpperCase());
        if (buffer.length() % 2 != 0) {
            LOG.warn("Invalid HEX value length. "
                + "The length of the value has to be a multiple of 2. Prepending '0' value.");
            buffer.insert(0, '0');
        }
        int hexLength = buffer.length();
        int byteLength = hexLength / 2;

        bytes = new byte[byteLength];
        for (int index = 0; index < hexLength; index += 2) {
            hexChar = buffer.charAt(index);
            high = (TextToHex.get(Character.valueOf(hexChar))).byteValue();
            high = (byte) (high << 4);
            hexChar = buffer.charAt(index + 1);
            low = (TextToHex.get(Character.valueOf(hexChar))).byteValue();
            high = (byte) (high | low);
            bytes[index / 2] = high;
        }
        return bytes;
    }
}
