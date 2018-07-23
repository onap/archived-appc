package org.onap.appc.licmgr.impl.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.onap.appc.licmgr.objects.LicenseModelBuilder;

public class XmlToLicenseModelConverter {

    private XMLInputFactory factory;

    public XmlToLicenseModelConverter(XMLInputFactory factory) {
        this.factory = factory;
    }

    public void apply(CheckedBiConsumer<XMLStreamReader, LicenseModelBuilder> consumer, String xml, LicenseModelBuilder builder)
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
}
