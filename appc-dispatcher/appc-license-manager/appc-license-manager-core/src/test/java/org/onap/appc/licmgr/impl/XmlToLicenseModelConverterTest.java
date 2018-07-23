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
import org.onap.appc.licmgr.impl.xml.XmlToLicenseModelConverter;


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
        converter.apply((a, b) -> {
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
            .isThrownBy(() -> converter.apply((a, b) -> {
                throw new XMLStreamException();
            }, anyString(), null));
        then(xmlStreamReader).should().close();
    }
}