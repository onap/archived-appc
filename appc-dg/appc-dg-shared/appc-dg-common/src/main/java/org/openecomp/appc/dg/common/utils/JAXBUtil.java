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

package org.openecomp.appc.dg.common.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;



public class JAXBUtil {

    public static <T> T toObject(String xml, Class<T> type) throws JAXBException {

        //create JAXB context
        JAXBContext context = JAXBContext.newInstance(type);

        //Create Unmarshaller using JAXB context
        Unmarshaller unmarshaller = context.createUnmarshaller();

        InputStream xmlInputStream = new ByteArrayInputStream(xml.getBytes());
        BufferedReader reader = new BufferedReader(new InputStreamReader(xmlInputStream));

        return type.cast(unmarshaller.unmarshal(reader));

    }
}
