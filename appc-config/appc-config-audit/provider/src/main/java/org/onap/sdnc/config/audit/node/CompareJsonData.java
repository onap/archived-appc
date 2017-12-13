/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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
 */

package org.openecomp.sdnc.config.audit.node;

import java.io.IOException;
import java.util.Map;


import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class CompareJsonData implements CompareDataInterface {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(CompareJsonData.class);

    String payloadX;
    String payloadY;

    public CompareJsonData(String payloadX, String payloadY)
    {
        this.payloadX = payloadX;
        this.payloadY = payloadY;
    }

    @Override
    public boolean compare() throws Exception
    {

        ObjectMapper dataMapper = new ObjectMapper();
        boolean match = false;
        try
        {
            Map<String, Object> controlData = (Map<String, Object>)(dataMapper.readValue(payloadX, Map.class));
            Map<String, Object>  testData = (Map<String, Object>)(dataMapper.readValue(payloadY, Map.class));

            log.debug("Control Data :" + controlData);
            log.debug("testData Data :" + testData);

           if(controlData.equals(testData))
               match=true;
        }
        catch(JsonParseException e)
        {
            throw new Exception(e.getMessage());
        }
        catch(JsonMappingException e)
        {
            throw new Exception(e.getMessage());
        }
        catch(IOException ioe)
        {
            throw new Exception(ioe.getMessage());
        }

        return match;
    }



}
