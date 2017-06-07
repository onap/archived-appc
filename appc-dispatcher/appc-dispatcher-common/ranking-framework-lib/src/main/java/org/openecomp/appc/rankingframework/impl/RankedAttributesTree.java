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

import java.util.List;

import org.openecomp.appc.rankingframework.RankedAttributesContext;
import org.openecomp.appc.rankingframework.RankedAttributesResolver;

class RankedAttributesTree<R> implements RankedAttributesResolver<R> {

    private final CompositeNode<R> root;
    private final Strategy strategy;
    private final List<String> rankedNames;

    RankedAttributesTree(CompositeNode<R> root, List<String> rankedNames, Strategy strategy) {
        this.root = root;
        this.rankedNames = rankedNames;
        this.strategy = strategy;
    }

    @Override
    public R resolve(RankedAttributesContext context) {
        return strategy.resolve(root, rankedNames, context);
    }
}
