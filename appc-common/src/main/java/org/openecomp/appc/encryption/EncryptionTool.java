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

import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;

import javax.crypto.Cipher;

import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to encapsulate the encryption and decryption support in one place and to provide a utility to
 * encrypt and decrypt data.
 */
public class EncryptionTool {

    /**
     * This lock object is used ONLY if the singleton has not been set up.
     */
    private static final Object lock = new Object();

    /**
     * The salt is used to initialize the PBE (password Based Encrpytion) algorithm.
     */
    private static final byte[] DEFAULT_SALT = {
        (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99
    };

    /**
     * The prefix we insert onto any data we encrypt so that we can tell if it is encrpyted later and therefore decrypt
     * it
     */
    @SuppressWarnings("nls")
    public static final String ENCRYPTED_VALUE_PREFIX = "enc:";

    /**
     * The instance of the encryption utility object
     */
    private static EncryptionTool instance = null;

    /**
     * The iteration count used to initialize the PBE algorithm and to generate the key spec
     */
    private static final int ITERATION_COUNT = 20;

    /**
     * The logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EncryptionTool.class);

    /**
     * The secret passphrase (PBE) that we use to perform encryption and decryption. The algorithm we are using is a
     * symmetrical cipher.
     */
    private static char[] secret = {
        'C', '_', 'z', 'l', '!', 'K', '!', '4', '?', 'O', 'z', 'E', 'K', 'E', '>', 'U', 'R', '/', '%', 'Y', '\\', 'f',
        'b', '"', 'e', 'n', '{', '"', 'l', 'U', 'F', '+', 'E', '\'', 'R', 'T', 'p', '1', 'V', '4', 'l', 'a', '9', 'w',
        'v', '5', 'Z', '#', 'i', 'V', '"', 'd', 'l', '!', 'L', 'M', 'g', 'L', 'Q', '{', 'v', 'v', 'K', 'V'
    };

    /**
     * The algorithm to encrypt and decrpyt data is "Password (or passphrase) Based Encryption with Message Digest #5
     * and the Data Encryption Standard", i.e., PBEWithMD5AndDES.
     */
    @SuppressWarnings("nls")
    private static final String SECURITY_ALGORITHM = "PBEWITHMD5AND256BITAES";// "PBEWithMD5AndDES";

    /**
     * The decryption cipher object
     */
    private Cipher decryptCipher = null;

    /**
     * The encryption cipher object
     */
    private Cipher encryptCipher = null;

    private BasicTextEncryptor encryptor;

    /**
     * Get an instance of the EncryptionTool
     *
     * @return The encryption tool to be used
     */
    public static final EncryptionTool getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new EncryptionTool();
                }
            }
        }
        return instance;
    }

    /**
     * Create the EncryptionTool instance
     */
    @SuppressWarnings("nls")
    private EncryptionTool() {
        String out = "Found the following security algorithms:";
        for (Provider p : Security.getProviders()) {
            for (Service s : p.getServices()) {
                String algo = s.getAlgorithm();
                out +=
                    String.format("\n  -Algorithm [ %s ] in provider [ %s ] and service [ %s ]", algo, p.getName(),
                        s.getClassName());
            }
        }
        LOG.debug(out);
    }

    /**
     * Decrypt the provided encrypted text
     *
     * @param cipherText
     *            THe cipher text to be decrypted. If the ciphertext is not encrypted, then it is returned as is.
     * @return the clear test of the (possibly) encrypted value. The original value if the string is not encrypted.
     */
    @SuppressWarnings("nls")
    public synchronized String decrypt(String cipherText) {
        if (isEncrypted(cipherText)) {
            String encValue = cipherText.substring(ENCRYPTED_VALUE_PREFIX.length());
            byte[] plainByte = Base64.decodeBase64(encValue.getBytes());
            byte[] decryptByte = xorWithSecret(plainByte);
            return new String(decryptByte);
        } else {
            return cipherText;
        }

    }

    /**
     * Encrypt the provided clear text
     *
     * @param clearText
     *            The clear text to be encrypted
     * @return the encrypted text. If the clear text is empty (null or zero length), then an empty string is returned.
     *         If the clear text is already encrypted, it is not encrypted again and is returned as is. Otherwise, the
     *         clear text is encrypted and returned.
     */
    @SuppressWarnings("nls")
    public synchronized String encrypt(String clearText) {
        if (clearText != null) {
            byte[] encByte = xorWithSecret(clearText.getBytes());
            String encryptedValue = new String(Base64.encodeBase64(encByte));
            return ENCRYPTED_VALUE_PREFIX + encryptedValue;
        } else {
            return null;
        }
    }

    /**
     * Is a value encrypted? A value is considered to be encrypted if it begins with the
     * {@linkplain #ENCRYPTED_VALUE_PREFIX encrypted value prefix}.
     *
     * @param value
     *            the value to check.
     * @return true/false;
     */
    private static boolean isEncrypted(final String value) {
        return value != null && value.startsWith(ENCRYPTED_VALUE_PREFIX);
    }

    /**
     * XORs the input byte array with the secret key, padding 0x0 to the end of the secret key if the input is longer
     * and returns a byte array the same size as input
     *
     * @param inp
     *            The byte array to be XORed with secret
     * @return A byte array the same size as inp or null if input is null.
     */
    private byte[] xorWithSecret(byte[] inp) {
        if (inp == null) {
            return null;
        }

        byte[] secretBytes = new String(secret).getBytes();
        int size = inp.length;

        byte[] out = new byte[size];
        for (int i = 0; i < size; i++) {
            out[i] = (byte) ((inp[i]) ^ (secretBytes[i % secretBytes.length]));
        }
        return out;
    }

}
