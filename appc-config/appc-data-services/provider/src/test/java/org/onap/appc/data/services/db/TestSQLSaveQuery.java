/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Copyright (C) 2018 IBM
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
 * 
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.data.services.db;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertEquals;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class TestSQLSaveQuery {

    private static final Logger LOG = LoggerFactory.getLogger(TestSQLSaveQuery.class);
    private static String CRYPT_KEY = "";

    @Test
    public void testSQLSaveQuery() {
        try {
            String message = FileUtils.readFileToString(new File("src/test/resources/query/sampledata.txt"));
            System.out.println("TestSQLSaveQuery.testSQLSaveQuery()" + message);
            SvcLogicContext ctx = new SvcLogicContext();
            ctx.setAttribute("request-id", "1234");
            String escapedMessage = StringEscapeUtils.escapeSql(message);
            ctx.setAttribute("log_message", escapedMessage);
            String key = "INSERT INTO CONFIG_TRANSACTION_LOG "
                    + " SET request_id = $request-id , message_type  =  'request' ,  message        =  $log_message ;";
            String resolvedContext = resolveCtxVars(key, ctx);
            ctx.setAttribute("log_message", null);
        } catch (IOException e) {

        }

    }
    
    @Test
    public void testSQLSaveQueryForNestedRequestId() throws IOException{
            String message = FileUtils.readFileToString(new File("src/test/resources/query/sampledata.txt"));
            SvcLogicContext ctx = new SvcLogicContext();
            ctx.setAttribute("request-id", "1234");
            String escapedMessage = StringEscapeUtils.escapeSql(message);
            ctx.setAttribute("log_message", escapedMessage);
            String key = "INSERT INTO CONFIG_TRANSACTION_LOG "
                    + " SET request_id = $[$request-id] , message_type  =  'request' ,  message        =  $log_message ;";
            String resolvedContext = resolveCtxVars(key, ctx);
            String expected="INSERT INTO CONFIG_TRANSACTION_LOG SET request_id = 'null' , message_type = 'request' , message = '' ;";
            assertEquals(expected.trim(),resolvedContext.trim());
    }
    
    @Test
    public void testSQLSaveQueryForCryptKey() throws IOException{
            String message = FileUtils.readFileToString(new File("src/test/resources/query/sampledata.txt"));
            SvcLogicContext ctx = new SvcLogicContext();
            ctx.setAttribute("request-id", "1234");
            String escapedMessage = StringEscapeUtils.escapeSql(message);
            ctx.setAttribute("log_message", escapedMessage);
            ctx.setAttribute("ctxVarName", "test_crypt_key");
            String key = "INSERT INTO CONFIG_TRANSACTION_LOG "
                    + " SET request_id = $request-id , message_type  =  'request' ,  message        =  $CRYPT_KEY ;";
            String resolvedContext = resolveCtxVars(key, ctx);
            String expected="INSERT INTO CONFIG_TRANSACTION_LOG SET request_id = '1234' , message_type = 'request' , message = '' ;";
            assertEquals(expected.trim(),resolvedContext.trim());
       
    }

    private String resolveCtxVars(String key, SvcLogicContext ctx) {
        if (key == null) {
            return (null);
        }
        if (key.startsWith("'") && key.endsWith("'")) {
            key = key.substring(1, key.length() - 1);
            LOG.debug("Stripped outer single quotes - key is now [" + key + "]");
        }
        String[] keyTerms = key.split("\\s+");
        StringBuffer sqlBuffer = new StringBuffer();
        for (int i = 0; i < keyTerms.length; i++) {
            sqlBuffer.append(resolveTerm(keyTerms[i], ctx));
            sqlBuffer.append(" ");
        }
        return (sqlBuffer.toString());
    }

    private String resolveTerm(String term, SvcLogicContext ctx) {
        if (term == null) {
            return (null);
        }
        LOG.debug("resolveTerm: term is " + term);
        if (term.startsWith("$") && (ctx != null)) {
            // Resolve any index variables.
            return ("'" + resolveCtxVariable(term.substring(1), ctx) + "'");

        } else {
            return (term);
        }

    }

    private String resolveCtxVariable(String ctxVarName, SvcLogicContext ctx) {

        if (ctxVarName.indexOf('[') == -1) {
            // Ctx variable contains no arrays
            if ("CRYPT_KEY".equals(ctxVarName)) {
                // Handle crypt key as special case. If it's set as a context
                // variable, use it. Otherwise, use
                // configured crypt key.
                String cryptKey = ctx.getAttribute(ctxVarName);
                if ((cryptKey != null) && (cryptKey.length() > 0)) {
                    return (cryptKey);
                } else {
                    return (CRYPT_KEY);
                }

            }
            return (ctx.getAttribute(ctxVarName));
        }
        // Resolve any array references
        StringBuffer sbuff = new StringBuffer();
        String[] ctxVarParts = ctxVarName.split("\\[");
        sbuff.append(ctxVarParts[0]);
        for (int i = 1; i < ctxVarParts.length; i++) {
            if (ctxVarParts[i].startsWith("$")) {
                int endBracketLoc = ctxVarParts[i].indexOf("]");
                if (endBracketLoc == -1) {
                    // Missing end bracket ... give up parsing
                    LOG.warn("Variable reference " + ctxVarName + " seems to be missing a ']'");
                    return (ctx.getAttribute(ctxVarName));
                }

                String idxVarName = ctxVarParts[i].substring(1, endBracketLoc);
                String remainder = ctxVarParts[i].substring(endBracketLoc);
                sbuff.append("[");
                sbuff.append(ctx.getAttribute(idxVarName));
                sbuff.append(remainder);

            } else {
                // Index is not a variable reference
                sbuff.append("[");
                sbuff.append(ctxVarParts[i]);
            }
        }
        return (ctx.getAttribute(sbuff.toString()));
    }

}
