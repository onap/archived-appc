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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onap.appc.rankingframework.RankedAttributesContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

class BacktraceStrategy implements Strategy {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(BacktraceStrategy.class);

    @Override
    public <R> R resolve(CompositeNode<R> rootNode, List<String> rankedNames, RankedAttributesContext context) {

        logEntryPath(rankedNames, context);

        Set<String> visited = new HashSet<>();

        CompositeNode<R> parentNode = rootNode;
        int depth = 0;
        boolean stop = false;
        R result = null;

        String attribute = null;
        Object value = null;

        do {
            if (value == null) {
                attribute = rankedNames.get(depth);
                value = Utils.value(context.getAttributeValue(attribute));
            }

            Node<R> childNode = getChildNode(parentNode, visited, attribute, value);

            if (childNode != null) {
                switch (childNode.type()) {
                    case COMPOSITE:
                        depth++;
                        value = null;
                        parentNode = (CompositeNode<R>) childNode;
                        break;
                    case LEAF:
                        log("Result node has been resolved succesfully - '%s'", childNode);
                        result = ((LeafNode<R>) childNode).result();
                        stop = true;
                        break;
                    default:
                        throw new IllegalStateException(childNode.type().name());
                }
            } else {
                if (!(Constants.DEFAULT_MATCH).equals(value)) {
                    logger.debug("Exact match didn't work, trying the default option, if any");
                    value = Constants.DEFAULT_MATCH;
                } else if (depth > 0) {
                    log("Exact match didn't work and no default option available beneath '%s' - moving out",
                            parentNode);
                    depth--;
                    value = null;
                    parentNode = parentNode.parent();
                } else {
                    logger.debug("Didn't success to resolve the path - stopping without result");
                    stop = true;
                }
            }
        } while (!stop);

        return result;
    }

    private <R> Node<R> getChildNode(CompositeNode<R> parentNode, Set<String> visited, String attribute, Object value) {

        Node<R> childNode = parentNode.children().get(value);
        if (childNode != null) {
            log("Found matching node '%s' - checking it out", childNode);

            if (!visited.add(childNode.id())) {
                log("The matching node '%s' was checked before - ignoring it", childNode);
                childNode = null;
            }
        } else {
            log("Node '%s/{%s = %s}' not found  - falling back",
                    parentNode, attribute, value != null ? value : "NULL");
        }
        return childNode;
    }

    private void logEntryPath(List<String> rankedNames, RankedAttributesContext context){
        if (logger.isDebugEnabled()) {
            StringBuilder buff = new StringBuilder(128);
            for (String name : rankedNames) {
                buff.append("/{")
                        .append(name)
                        .append(" = ")
                        .append(Utils.value(context.getAttributeValue(name)))
                        .append('}');
            }
            logger.debug(String.format("Trying to resolve path: %s", buff));
        }
    }

    private void log(String log, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(log, args));
        }
    }
}
