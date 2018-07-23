package org.onap.appc.licmgr.impl.xml;

import javax.xml.stream.XMLInputFactory;

public class XmlInputFactoryWrapper {
    public XMLInputFactory getFactory(){
        return XMLInputFactory.newInstance();
    }
}
