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
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


public class CompareXmlData implements CompareDataInterface
{
    private static final EELFLogger log = EELFManager.getInstance().getLogger(CompareXmlData.class);


    String controlXml;
    String testXml;

    Document doc;

    public CompareXmlData(String controlXml, String testXml) {
        super();
        this.controlXml = controlXml;
        this.testXml = testXml;
    }

    @Override
    public boolean compare() throws Exception
    {

        log.debug("controlXml : " + controlXml);
        log.debug("testXml : " + testXml);

        doSetup();

        try
        {
            Diff diff = new Diff(getCompareDoc(controlXml), getCompareDoc(testXml));
            if(diff.similar())
                return true;
            else
                return false;
        }
        catch(SAXException se)
        {
            se.printStackTrace();
            throw new Exception(se.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    private void doSetup() throws ParserConfigurationException, SAXException, IOException
    {

        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
    }

    public Document getCompareDoc(String inXml) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        StringReader reader = new StringReader(inXml);
        InputSource inputSource = new InputSource(reader);
        Document doc = dBuilder.parse(inputSource);
        doc.getDocumentElement().normalize();

        return doc;
    }
}
