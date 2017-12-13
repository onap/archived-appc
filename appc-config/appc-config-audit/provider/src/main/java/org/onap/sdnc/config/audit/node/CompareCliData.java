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

public class CompareCliData implements CompareDataInterface{

    String payloadX;
    String payloadY;    

    public CompareCliData(String payloadX, String payloadY)
    {
        super();
        this.payloadX = payloadX;
        this.payloadY = payloadY;
    }

    @Override
    public boolean compare() throws Exception
    {
            if(payloadX != null && payloadX.equals(payloadY))
                return true;
            else
                return false;
    }
}
