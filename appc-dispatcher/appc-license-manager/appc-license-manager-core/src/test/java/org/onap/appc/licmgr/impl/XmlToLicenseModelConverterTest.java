/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class XmlToLicenseModelConverterTest {

    @Mock
    private XMLStreamReader xmlStreamReader;

    @Mock
    private XMLInputFactory xmlInputFactory;

    @Test
    public void apply_shouldCloseXMLStreamReader_whenNoExceptionIsThrown()
        throws XMLStreamException, IOException {

        // GIVEN
        XmlToLicenseModelConverter converter = new XmlToLicenseModelConverter(xmlInputFactory);
        given(xmlInputFactory.createXMLStreamReader(any(InputStream.class))).willReturn(xmlStreamReader);

        // WHEN
        converter.convert((a, b) -> {
        }, anyString(), null);

        // THEN
        then(xmlStreamReader).should().close();
    }

    @Test
    public void apply_shouldCloseXMLStreamReader_whenExceptionIsThrown()
        throws XMLStreamException {
        // GIVEN
        XmlToLicenseModelConverter converter = new XmlToLicenseModelConverter(xmlInputFactory);
        given(xmlInputFactory.createXMLStreamReader(any(InputStream.class))).willReturn(xmlStreamReader);

        // WHEN THEN
        assertThatExceptionOfType(XMLStreamException.class)
            .isThrownBy(() -> converter.convert((a, b) -> {
                throw new XMLStreamException();
            }, anyString(), null));
        then(xmlStreamReader).should().close();
    }
}