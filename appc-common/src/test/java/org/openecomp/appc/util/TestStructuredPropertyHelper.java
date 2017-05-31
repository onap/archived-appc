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

package org.openecomp.appc.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.util.StructuredPropertyHelper;
import org.openecomp.appc.util.StructuredPropertyHelper.Node;

/**
 * This class is used to test the structured property helper class.
 * <p>
 * A structured property is one where the name is constructed from a compound set of elements, concatenated by a period,
 * and optionally being enumerated using a sequence number suffix. A java package name is an example of a structured
 * name, where each element of the name represents a directory or name in the namespace hierarchy. Property names may
 * also be structured. This class constructs a graph of the structured properties and this test case is used to verify
 * its operation.
 * </p>
 *
 */
public class TestStructuredPropertyHelper {

    /**
     * The properties to be parsed
     */
    private Properties properties;

    /**
     * The result of parsing the properties
     */
    private List<Node> nodes = new ArrayList<>();

    /**
     * Initialize the test environment
     */
    @SuppressWarnings("nls")
    @Before
    public void setup() {
        nodes.clear();

        properties = new Properties();

        properties.setProperty("provider1.name", "provider1Name");
        properties.setProperty("provider1.type", "provider1type");
        properties.setProperty("provider1.URL", "provider1URL");
        properties.setProperty("provider2.name", "provider2Name");
        properties.setProperty("provider2.type", "provider2type");
        properties.setProperty("provider2.URL", "provider2URL");
        properties.setProperty("provider003.name", "provider3Name");
        properties.setProperty("provider003.type", "provider3type");
        properties.setProperty("provider003.URL", "provider3URL");

        properties.setProperty("node1.level1.value1.key", "1.1.1");
        properties.setProperty("node1.level1.value2.key", "1.1.2");
        properties.setProperty("node1.level1.value3.key", "1.1.3");
        properties.setProperty("node1.level2.value1.key", "1.2.1");
        properties.setProperty("node1.level2.value2.key", "1.2.2");
        properties.setProperty("node1.level2.value3.key", "1.2.3");
        properties.setProperty("node1.level3.value1.key", "1.3.1");
        properties.setProperty("node1.level3.value2.key", "1.3.2");
        properties.setProperty("node1.level3.value3.key", "1.3.3");
        properties.setProperty("node2.level1.value1.key", "2.1.1");
        properties.setProperty("node2.level1.value2.key", "2.1.2");
        properties.setProperty("node2.level1.value3.key", "2.1.3");
        properties.setProperty("node2.level2.value1.key", "2.2.1");
        properties.setProperty("node2.level2.value2.key", "2.2.2");
        properties.setProperty("node2.level2.value3.key", "2.2.3");
        properties.setProperty("node2.level3.value1.key", "2.3.1");
        properties.setProperty("node2.level3.value2.key", "2.3.2");
        properties.setProperty("node2.level3.value3.key", "2.3.3");
        properties.setProperty("node3.level1.value1.key", "3.1.1");
        properties.setProperty("node3.level1.value2.key", "3.1.2");
        properties.setProperty("node3.level1.value3.key", "3.1.3");
        properties.setProperty("node3.level2.value1.key", "3.2.1");
        properties.setProperty("node3.level2.value2.key", "3.2.2");
        properties.setProperty("node3.level2.value3.key", "3.2.3");
        properties.setProperty("node3.level3.value1.key", "3.3.1");
        properties.setProperty("node3.level3.value2.key", "3.3.2");
        properties.setProperty("node3.level3.value3.key", "3.3.3");

        properties.setProperty("other.property", "bogus");
        properties.setProperty("yet.another.property", "bogus");
        properties.setProperty("simpleProperty", "bogus");

    }

    /**
     * Test that a simple namespace works
     */
    @SuppressWarnings("nls")
    @Test
    public void testSimpleNamespace() {
        nodes = StructuredPropertyHelper.getStructuredProperties(properties, "provider");

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());

        assertEquals(3, nodes.size());

        List<Node> children;
        for (Node node : nodes) {
            switch (node.getName()) {
                case "provider1":
                    assertNull(node.getValue());
                    children = node.getChildren();
                    assertNotNull(children);
                    assertEquals(3, children.size());
                    for (Node child : children) {
                        switch (child.getName()) {
                            case "URL":
                                assertEquals("provider1URL", child.getValue());
                                break;
                            case "type":
                                assertEquals("provider1type", child.getValue());
                                break;
                            case "name":
                                assertEquals("provider1Name", child.getValue());
                                break;
                            default:
                                fail("Unknown child of " + node.getName() + " with value " + child.toString());
                        }
                    }
                    break;
                case "provider2":
                    assertNull(node.getValue());
                    children = node.getChildren();
                    assertNotNull(children);
                    assertEquals(3, children.size());
                    for (Node child : children) {
                        switch (child.getName()) {
                            case "URL":
                                assertEquals("provider2URL", child.getValue());
                                break;
                            case "type":
                                assertEquals("provider2type", child.getValue());
                                break;
                            case "name":
                                assertEquals("provider2Name", child.getValue());
                                break;
                            default:
                                fail("Unknown child of " + node.getName() + " with value " + child.toString());
                        }
                    }
                    break;
                case "provider3":
                    /*
                     * Note that the helper normalizes any ordinal suffixes (003 became 3)
                     */
                    assertNull(node.getValue());
                    children = node.getChildren();
                    assertNotNull(children);
                    assertEquals(3, children.size());
                    for (Node child : children) {
                        switch (child.getName()) {
                            case "URL":
                                assertEquals("provider3URL", child.getValue());
                                break;
                            case "type":
                                assertEquals("provider3type", child.getValue());
                                break;
                            case "name":
                                assertEquals("provider3Name", child.getValue());
                                break;
                            default:
                                fail("Unknown child of " + node.getName() + " with value " + child.toString());
                        }
                    }
                    break;
                default:
                    fail("Unknown provider " + node.toString());
            }
        }
        // System.out.println(nodes);
    }

    /**
     * Test a multi-dimensional namespace (3X3X3)
     */
    @SuppressWarnings("nls")
    @Test
    public void testMultiLevelNamespace() {
        nodes = StructuredPropertyHelper.getStructuredProperties(properties, "node");

        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());

        assertEquals(3, nodes.size());
        for (Node node : nodes) {
            assertNull(node.getValue());
            List<Node> children = node.getChildren();
            assertNotNull(children);
            assertEquals(3, children.size());
            for (Node child : children) {
                assertNull(child.getValue());
                List<Node> grandChildren = child.getChildren();
                assertNotNull(grandChildren);
                assertEquals(3, grandChildren.size());
                for (Node greatGrandChild : grandChildren) {
                    assertNull(greatGrandChild.getValue());
                    List<Node> greatGrandChildren = greatGrandChild.getChildren();
                    assertNotNull(greatGrandChildren);
                    assertEquals(1, greatGrandChildren.size());
                }
            }
        }
        // System.out.println(nodes);
    }
}
