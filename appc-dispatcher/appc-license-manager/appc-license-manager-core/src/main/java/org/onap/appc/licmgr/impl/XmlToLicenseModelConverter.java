/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia
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

package org.onap.appc.licmgr.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.onap.appc.licmgr.objects.LicenseModelBuilder;

class XmlToLicenseModelConverter {

    private XMLInputFactory factory;

    XmlToLicenseModelConverter(XMLInputFactory factory) {
        this.factory = factory;
    }

    void convert(XMLStreamConsumer<XMLStreamReader, LicenseModelBuilder> consumer, String xml, LicenseModelBuilder builder)
        throws XMLStreamException, IOException {

        XMLStreamReader reader = null;
        try (InputStream inputStream = new ByteArrayInputStream(xml.getBytes())) {
            reader = factory.createXMLStreamReader(inputStream);
            consumer.accept(reader, builder);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @FunctionalInterface
    public interface XMLStreamConsumer<T, W> {
        void accept(T t, W w) throws XMLStreamException;
    }
}
