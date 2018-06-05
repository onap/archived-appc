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

package org.onap.sdnc.config.audit;

public class SliAuditConstant {

    public static final String STRING_ENCODING = "utf-8";
    public static final String Y = "Y";
    public static final String N = "N";
    public static final String DATA_TYPE_TEXT = "TEXT";
    public static final String DATA_TYPE_JSON = "JSON";
    public static final String DATA_TYPE_XML = "XML";
    public static final String DATA_TYPE_SQL = "SQL";

    public static final String INPUT_PARAM_JSON_DATA = "jsonData";
    public static final String INPUT_PARAM_IS_ESCAPED = "isEscaped";
    public static final String INPUT_PARAM_BLOCK_KEYS = "blockKeys";
    public static final String INPUT_PARAM_LOG_DATA = "logData";
    public static final String INPUT_PARAM_CHECK_DATA = "checkData";
    public static final String INPUT_PARAM_ESCAPE_DATA = "escapeData";
    public static final String INPUT_PARAM_UNESCAPE_DATA = "unEscapeData";
    public static final String INPUT_PARAM_DATA_TYPE = "dataType";
    public static final String INPUT_PARAM_FILE_NAME = "fileName";

    public static final String INPUT_PARAM_TEMPLATE_DATA = "templateData";
    public static final String INPUT_PARAM_TEMPLATE_FILE = "templateFile";
    public static final String INPUT_PARAM_TEMPLATE_TYPE = "templateType";
    public static final String INPUT_PARAM_DO_PRETTY_OUTPUT = "doPrettyOutput";
    public static final String INPUT_PARAM_REQUEST_DATA = "requestData";
    public static final String INPUT_PARAM_RESPONSE_PRIFIX = "responsePrefix";


    public static final String OUTPUT_PARAM_MERGED_DATA = "mergedData";
    public static final String OUTPUT_PARAM_TRANSFORMED_DATA = "transformedData";
    public static final String OUTPUT_PARAM_FILE_DATA = "fileData";
    public static final String OUTPUT_PARAM_PARSED_ERROR = "parsedError";
    public static final String OUTPUT_PARAM_DATA_TYPE = "dataType";
    public static final String OUTPUT_PARAM_STATUS = "status";
    public static final String OUTPUT_PARAM_ERROR_MESSAGE = "error-message";
    public static final String OUTPUT_PARAM_ESCAPE_DATA = "escapeData";
    public static final String OUTPUT_PARAM_UNESCAPE_DATA = "unEscapeData";

    public static final String OUTPUT_STATUS_SUCCESS = "success";
    public static final String OUTPUT_STATUS_FAILURE = "failure";

}
