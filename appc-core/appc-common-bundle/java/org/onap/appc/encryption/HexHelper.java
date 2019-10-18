/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

    private static final char[] HEX_TABLE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F'};

    /**
     * The logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HexHelper.class);

    private static Map<Character, Integer> textToHex;

    static {
        textToHex = new HashMap<>();
        textToHex.put('0', 0);
        textToHex.put('1', 1);
        textToHex.put('2', 2);
        textToHex.put('3', 3);
        textToHex.put('4', 4);
        textToHex.put('5', 5);
        textToHex.put('6', 6);
        textToHex.put('7', 7);
        textToHex.put('8', 8);
        textToHex.put('9', 9);
        textToHex.put('A', 10);
        textToHex.put('B', 11);
        textToHex.put('C', 12);
        textToHex.put('D', 13);
        textToHex.put('E', 14);
        textToHex.put('F', 15);
    }

    /**
     * Default private constructor prevents instantiation
     */
    private HexHelper() {
        // no-op
    }

    /**
     * Converts an array of bytes to the equivalent string representation using hexadecimal notation
     *
     * @param bytes The bytes to be converted to a hexadecimal string
     * @return The string representation
     */
    public static String convertBytesToHex(byte[] bytes) throws EncryptionException{

        if (bytes == null)
            throw new EncryptionException("Given byte array is null");

        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            char tempChar;
            // Get the first 4 bits (high) Do bitwise logical AND to get rid of
            // low nibble. Shift results to right by 4 and get char
            // representation
            tempChar = HEX_TABLE[(aByte & 0xf0) >>> 4];
            builder.append(tempChar);

            // Get the last 4 bits (low) Do bitwise logical AND to get rid of
            // high nibble. Get char representation
            tempChar = HEX_TABLE[aByte & 0x0f];
            builder.append(tempChar);
        }
        return builder.toString();
    }

    /**
     * Converts a hexadecimal string representation of a binary value to an array of bytes
     *
     * @param hexValue The hex representation string to be converted
     * @return The array of bytes that contains the binary value
     */
    @SuppressWarnings("nls")
    public static byte[] convertHexToBytes(String hexValue) throws EncryptionException {

        if (hexValue ==null)
            throw new EncryptionException("Given hex value is null");

        byte[] bytes;
        byte high;
        byte low;
        char hexChar;

        StringBuilder builder = new StringBuilder(hexValue.toUpperCase());
        if (builder.length() % 2 != 0) {
            LOG.warn("Invalid HEX value length. The length of the value has to be a multiple of 2."
                + " Prepending '0' value.");
            builder.insert(0, '0');
        }
        int hexLength = builder.length();
        int byteLength = hexLength / 2;

        bytes = new byte[byteLength];
        try {
            for (int index = 0; index < hexLength; index += 2) {
                hexChar = builder.charAt(index);
                high = textToHex.get(hexChar).byteValue();
                high = (byte) (high << 4);
                hexChar = builder.charAt(index + 1);
                low = textToHex.get(hexChar).byteValue();
                high = (byte) (high | low);
                bytes[index / 2] = high;
            }
        }
        catch (NullPointerException e){
            LOG.error("Given string contains not hexadecimal values", e);
            throw new EncryptionException("Given string contains not hexadecimal values");
        }

        return bytes;
    }
}
