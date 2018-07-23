package org.onap.appc.licmgr.impl.xml;

import javax.xml.stream.XMLStreamException;

@FunctionalInterface
public interface CheckedBiConsumer<T, W> {
    void accept(T t, W w) throws XMLStreamException;
}
