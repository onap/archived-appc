/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.appc.mdsal.exception;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestMDSALStoreException {

    @Test
    public void testConstructorNoArgument() throws Exception {
        MDSALStoreException mDSALStoreException = new MDSALStoreException();
        assertTrue(mDSALStoreException.getCause() == null);
        assertTrue(mDSALStoreException.getLocalizedMessage() == null);
        assertTrue(mDSALStoreException.getMessage() == null);
    }

    @Test
    public void testConstructorWithMessaqge() throws Exception {
        String message = "testing message";
        MDSALStoreException mDSALStoreException = new MDSALStoreException(message);
        assertTrue(mDSALStoreException.getCause() == null);
        assertEquals(message, mDSALStoreException.getLocalizedMessage());
        assertEquals(message, mDSALStoreException.getMessage());
    }

    @Test
    public void testConstructorWithThrowable() throws Exception {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        MDSALStoreException mDSALStoreException = new MDSALStoreException(throwable);
        assertEquals(throwable, mDSALStoreException.getCause());
        assertTrue(mDSALStoreException.getLocalizedMessage().contains(message));
        assertTrue(mDSALStoreException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        MDSALStoreException mDSALStoreException =new MDSALStoreException(message, throwable);
        assertEquals(throwable, mDSALStoreException.getCause());
        assertTrue(mDSALStoreException.getLocalizedMessage().contains(message));
        assertTrue(mDSALStoreException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithMessage_Throwable_SuppressionAndStackTrace() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        MDSALStoreException mDSALStoreException =new MDSALStoreException(message, throwable,true,true);
        assertEquals(throwable, mDSALStoreException.getCause());
        assertTrue(mDSALStoreException.getLocalizedMessage().contains(message));
        assertTrue(mDSALStoreException.getMessage().contains(message));
        
    }
}
