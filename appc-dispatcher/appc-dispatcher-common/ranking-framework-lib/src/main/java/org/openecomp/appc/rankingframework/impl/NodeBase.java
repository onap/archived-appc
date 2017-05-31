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

package org.openecomp.appc.rankingframework.impl;

import java.util.UUID;

abstract class NodeBase<R> implements Node<R> {

    private final String name;
    private final Object value;
    private final Type type;
    private final CompositeNode<R> parent;
    private final String id = UUID.randomUUID().toString();

    NodeBase(String name, Object value, CompositeNode<R> parent, Type type) {
        this.name = name;
        this.value = value;
        this.parent = parent;
        this.type = type;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public CompositeNode<R> parent() {
        return parent;
    }

    @Override
    public boolean isDefaultMatch() {
        return value.equals(Constants.DEFAULT_MATCH);
    }

    @Override
    public String toString() {
        if (!name.equals("ROOT")) {
            StringBuffer buff = new StringBuffer(128);
            if (parent != null) {
                buff.append(parent.toString());
            }
            buff.append("/{");
            buff.append(name).append(" = ").append(value);
            buff.append("}");

            return buff.toString();
        } else {
            return "";
        }
    }
}
