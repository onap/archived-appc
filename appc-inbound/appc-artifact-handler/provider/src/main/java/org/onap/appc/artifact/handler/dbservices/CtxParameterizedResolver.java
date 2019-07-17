/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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


package org.onap.appc.artifact.handler.dbservices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class CtxParameterizedResolver {

    private static String CRYPT_KEY = "QtfJMKggVk";
    private static final EELFLogger log = EELFManager.getInstance().getLogger(CtxParameterizedResolver.class);
    
    protected static String resolveCtxVars(String key, SvcLogicContext ctx, ArrayList<String> arguments) {
        if (key == null) {
            return (null);
        }

        if (key.startsWith("'") && key.endsWith("'")) {
            key = key.substring(1, key.length() - 1);
            log.debug("Stripped outer single quotes - key is now [" + key + "]");
        }

        String[] keyTerms = key.split("\\s+");

        StringBuffer sqlBuffer = new StringBuffer();

        for (int i = 0; i < keyTerms.length; i++) {
            sqlBuffer.append(resolveTerm(keyTerms[i], ctx, arguments));
            sqlBuffer.append(" ");
        }

        return (sqlBuffer.toString());
    }

    private static String resolveTerm(String term, SvcLogicContext ctx, ArrayList<String> arguments) {
        if (term == null) {
            return (null);
        }

        log.trace("resolveTerm: term is " + term);

        if (term.startsWith("$") && (ctx != null)) {
            // Resolve any index variables.
            term = resolveCtxVariable(term.substring(1), ctx);
            // Escape single quote
            if (term != null) {
                term = term.replaceAll("'", "''");
            }
            //valueOf will store null values as a String "null"
            arguments.add(String.valueOf(term));
            return "?";
        } else {
            return (term);
        }

    }

    private static String resolveCtxVariable(String ctxVarName, SvcLogicContext ctx) {

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
                    log.warn("Variable reference " + ctxVarName + " seems to be missing a ']'");
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
        
        protected static void saveCachedRowSetToCtx(CachedRowSet results, SvcLogicContext ctx, String prefix, DbLibService dblibSvc)
                throws SQLException {
            if (ctx != null) {
                if ((prefix != null) && prefix.endsWith("[]")) {
                    // Return an array.
                    String pfx = prefix.substring(0, prefix.length() - 2);
                    int idx = 0;
                    do {
                        ResultSetMetaData rsMeta = results.getMetaData();
                        int numCols = rsMeta.getColumnCount();

                        for (int i = 0; i < numCols; i++) {
                            String colValue = null;
                            String tableName = rsMeta.getTableName(i + 1);
                            if (rsMeta.getColumnType(i + 1) == java.sql.Types.VARBINARY) {
                                colValue = decryptColumn(tableName, rsMeta.getColumnName(i + 1), results.getBytes(i + 1),
                                        dblibSvc);
                            } else {
                                colValue = results.getString(i + 1);
                            }
                            log.debug("Setting " + pfx + "[" + idx + "]."
                                    + rsMeta.getColumnLabel(i + 1).replaceAll("_", "-") + " = " + colValue);
                            ctx.setAttribute(pfx + "[" + idx + "]." + rsMeta.getColumnLabel(i + 1).replaceAll("_", "-"),
                                    colValue);
                        }
                        idx++;
                    } while (results.next());
                    log.debug("Setting " + pfx + "_length = " + idx);
                    ctx.setAttribute(pfx + "_length", "" + idx);
                } else {
                    ResultSetMetaData rsMeta = results.getMetaData();
                    int numCols = rsMeta.getColumnCount();

                    for (int i = 0; i < numCols; i++) {
                        String colValue = null;
                        String tableName = rsMeta.getTableName(i + 1);
                        if ("VARBINARY".equalsIgnoreCase(rsMeta.getColumnTypeName(i + 1))) {
                            colValue = decryptColumn(tableName, rsMeta.getColumnName(i + 1), results.getBytes(i + 1),
                                    dblibSvc);
                        } else {
                            colValue = results.getString(i + 1);
                        }
                        if (prefix != null) {
                            log.debug("Setting " + prefix + "." + rsMeta.getColumnLabel(i + 1).replaceAll("_", "-") + " = "
                                    + colValue);
                            ctx.setAttribute(prefix + "." + rsMeta.getColumnLabel(i + 1).replaceAll("_", "-"), colValue);
                        } else {
                            log.debug("Setting " + rsMeta.getColumnLabel(i + 1).replaceAll("_", "-") + " = " + colValue);
                            ctx.setAttribute(rsMeta.getColumnLabel(i + 1).replaceAll("_", "-"), colValue);
                        }
                    }
                }
            }
        }
        
        private static String decryptColumn(String tableName, String colName, byte[] colValue, DbLibService dblibSvc) {
            String strValue = new String(colValue);

            if (StringUtils.isAsciiPrintable(strValue)) {

                // If printable, not encrypted
                return (strValue);
            } else {
                ResultSet results = null;
                try (Connection conn =  dblibSvc.getConnection();
                   PreparedStatement stmt = conn.prepareStatement("SELECT CAST(AES_DECRYPT(?, ?) AS CHAR(50)) FROM DUAL")) {

                    stmt.setBytes(1, colValue);
                    stmt.setString(2, CRYPT_KEY);
                    results = stmt.executeQuery();

                    if ((results != null) && results.next()) {
                        strValue = results.getString(1);
                        log.debug("Decrypted value is " + strValue);
                    } else {
                        log.warn("Cannot decrypt " + tableName + "." + colName);
                    }
                } catch (Exception e) {
                    log.error("Caught exception trying to decrypt " + tableName + "." + colName, e);
                }finally {
                    if (results != null) {
                        try {
                            results.close();
                        } catch (SQLException se) {
                            log.error("Caught exception trying to close ResultSet",se);
                        }
                    }
                }
            }
            return (strValue);
        }
}
