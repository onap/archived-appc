/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.appc.rankingframework.impl;

import java.util.HashMap;

import org.onap.appc.rankingframework.ConfigurationEntry;
import org.onap.appc.rankingframework.ConfigurationSet;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

class RankedAttributesTreeBuilder {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RankedAttributesTreeBuilder.class);

    private RankedAttributesTreeBuilder() {
    }

    static <R> CompositeNode<R> build(ConfigurationSet<R> config) {

        Object[] names = config.getRankedAttributeNames().toArray();

        CompositeNode<R> root = new CompositeNode<>("ROOT", Constants.DEFAULT_MATCH, null,
                new HashMap<Object, Node<R>>());

        logDebugWhenEnabled(String.format("Building decision tree for ranked attributes: %s", config.getRankedAttributeNames()));

        for (ConfigurationEntry<R> entry : config.getEntries()) {
            process(entry, names, root);
        }

        return root;
    }

    private static <R> void process(ConfigurationEntry<R> entry, Object[] names, CompositeNode<R> root) {
        CompositeNode<R> parentNode = root;
        for (int i = 0; i < names.length; i++) {

            final String name = (String) names[i];

            final Object value = value(entry, name);

            if (i < names.length - 1) {
                CompositeNode<R> currentNode = (CompositeNode<R>) parentNode.children().get(value);
                if (currentNode == null) {
                    currentNode = new CompositeNode<>(name, value, parentNode, new HashMap<Object, Node<R>>());
                    parentNode.children().put(value, currentNode);
                }
                parentNode = currentNode;
            } else {
                LeafNode<R> currentNode = (LeafNode<R>) parentNode.children().get(value);
                if (currentNode == null) {
                    currentNode = new LeafNode<>(name, value, parentNode, entry.getResult());
                    parentNode.children().put(value, currentNode);

                    logDebugWhenEnabled(String.format("Branch has been created: %s", currentNode));
                } else {
                    logger.error(
                            String.format("Duplicated configuration entry has been detected for attribute '%s' with value '%s' - the node '%s'exists already",
                                    name,
                                    value,
                                    currentNode));
                    throw new IllegalArgumentException("Duplicated configuration entry: " + currentNode);
                }
            }
        }
    }

    private static void logDebugWhenEnabled(String message) {
        if(logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    private static <R> Object value(ConfigurationEntry<R> entry, String name) {
        Object value = entry.getAttributeValue(name);
        return Utils.value(value);
    }
}
