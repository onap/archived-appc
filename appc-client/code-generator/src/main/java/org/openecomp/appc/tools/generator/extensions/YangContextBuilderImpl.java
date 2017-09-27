/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.tools.generator.extensions;

import org.openecomp.appc.tools.generator.api.ContextBuilder;
import com.google.common.base.Optional;

import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ModuleEffectiveStatementImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class YangContextBuilderImpl implements ContextBuilder {

    @Override
    public Map<String, Object> buildContext(String sourceFile, String contextConf) throws FileNotFoundException {
        InputStream source = new FileInputStream(sourceFile);
        if (source == null) {
            throw new FileNotFoundException("YANG file <" + sourceFile + ">not found");
        }

        YangTextSchemaContextResolver yangContextResolver = YangTextSchemaContextResolver
                .create("yang-context-resolver");
        Optional<SchemaContext> sc=null;
        try {
            yangContextResolver.registerSource(new URL("file:///" + sourceFile));
            sc = yangContextResolver.getSchemaContext();
        } catch (SchemaSourceException | IOException | YangSyntaxErrorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally{
        yangContextResolver.close();
        }
        Map<String, Object> map = new HashMap<>();
        if (sc.isPresent()) {

            Set<Module> modules = sc.get().getModules();
            for (Module module : modules) {
                ModuleEffectiveStatementImpl impl = (ModuleEffectiveStatementImpl) module;
                map.put("module", module);
            }

        }

        return map;
    }

    // @Override
    // public Map<String, Object> buildContext(String sourceFile, String
    // contextConf) throws FileNotFoundException {
    // InputStream source = new FileInputStream(sourceFile);
    // if (source == null) {
    // throw new FileNotFoundException("YANG file <" + sourceFile + ">not found");
    // }
    //
    // SchemaContext mSchema = parse(Collections.singletonList(source));
    //
    // Map<String, Object> map = new HashMap<>();
    // map.put("module", mSchema.getModules().iterator().next());
    // return map;
    // }
    //
    // private SchemaContext parse(List<InputStream> sources) {
    // YangParserImpl parser = new YangParserImpl();
    // Set<Module> modules = parser.parseYangModelsFromStreams(sources);
    // return parser.resolveSchemaContext(modules);
    // }

}
