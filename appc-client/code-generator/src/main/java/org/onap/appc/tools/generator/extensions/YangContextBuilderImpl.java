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

package org.onap.appc.tools.generator.extensions;

import com.google.common.base.Optional;
import org.onap.appc.tools.generator.api.ContextBuilder;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class YangContextBuilderImpl implements ContextBuilder {

    @Override
    public Map<String, Object> buildContext(URL sourceYangURL, String contextConf) throws IOException {


        Optional<SchemaContext> sc = null;
        try ( YangTextSchemaContextResolver yangContextResolver =
              YangTextSchemaContextResolver.create("yang-context-resolver")) {
            yangContextResolver.registerSource(sourceYangURL);
            sc = yangContextResolver.getSchemaContext();
        } catch (SchemaSourceException | IOException | YangSyntaxErrorException e) {
            throw new IOException(String.format("Exception occurred while processing sourceFileName %s ",sourceYangURL),e);
        }

        Map<String, Object> map = new HashMap<>();
        if ( null != sc && sc.isPresent()) {
            Set<Module> modules = sc.get().getModules();
            for (final Module module : modules) {

                Module proxyModule = (new PackagePrivateReflectBug()).buildProxyObjects(module);
                map.put("module", proxyModule);
            }
        }

        return map;
    }


    /**
     * The Wrap Proxy uses java Proxy to work around bug JDK-8193172.  The Issue is that if a super class is package
     * private its public methods cause IllegalAccessException when using java reflect to access those methods from a
     * subclass which is package public.
     * <p>
     * Example exception:
     * Caused by: java.lang.IllegalAccessException: Class freemarker.ext.beans.BeansWrapper cannot access a
     * member of class org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AbstractEffectiveModule
     * with modifiers "public final"
     */
    private static class  PackagePrivateReflectBug {

        /**
         * Recursive method that traverses the yang model and wraps objects that extend package private classes
         * with java proxy objects.
         * @param candidateObj
         * @param <T>
         * @return
         */
        private <T> T buildProxyObjects(Object candidateObj) {

            //can't proxy null so just return
            if (candidateObj == null) {
                return null;
            }


            Class<?> candidateClass = candidateObj.getClass();

            //prevent a recursive proxy
            if (Proxy.isProxyClass(candidateClass)) {
                return (T) candidateObj;
            }

            //Can�t create a proxy an Array,  so replace each
            //element the array with a proxy if needed.
            if (candidateClass.isArray()) {
                Object[] sourceArray = (Object[]) candidateObj;
                for (int i = 0; i < sourceArray.length; i++) {
                    sourceArray[i] = buildProxyObjects(sourceArray[i]);
                }
                return (T) candidateObj;
            }

            //Evaluate if this class must be wrapped in a proxy or not.
            if (!isCandidateForProxy(candidateClass)) {
                return (T) candidateObj;
            }

            //Collect the interfaces for the proxy.  Proxy only work with classes
            //that implement interfaces if there are none the obj cannot be wrapped
            //with a proxy.
            HashSet<Class<?>> interfaceSet = new HashSet<>();
            collectInterfaces(candidateClass, interfaceSet);
            if (interfaceSet.isEmpty()) {
                return (T) candidateObj;
            }
            Class<?>[] interfaces = new Class<?>[interfaceSet.size()];
            interfaceSet.toArray(interfaces);

            //wrap the Object in a proxy
            return (T) Proxy.newProxyInstance(
                    candidateClass.getClassLoader(),
                    interfaces,
                    (proxy, method, args) -> {
                        Object returnObj = method.invoke(candidateObj, args);
                        returnObj = buildProxyObjects(returnObj);
                        return returnObj;
                    }
            );
        }


        /**
         * This method determines if the specified class is a candidate for proxy.
         *
         * @param fromClass
         * @return true - if the specifed class is a Candidate.
         */
        private boolean isCandidateForProxy(Class<?> fromClass) {

            //a
            Class<?>[] includeClasses = {
                    java.util.Collection.class,
                    java.util.Map.class,
                    org.opendaylight.yangtools.yang.model.api.Module.class

            };

            for (Class<?> toClass : includeClasses) {
                if (toClass.isAssignableFrom(fromClass)) {
                    return true;
                }
            }

            if (isExtendsPackagePrivateSuperClass(fromClass)) {
                return true;
            }

            return false;
        }

        /**
         * Test if any of the super packages are package private.
         *
         * @param clazz
         * @return
         */
        private boolean isExtendsPackagePrivateSuperClass(Class<?> clazz) {
            if (Object.class.equals(clazz)) {
                return false;
            }

            int classModifiers = clazz.getModifiers();
            if (Modifier.isPublic(classModifiers)) {
                return isExtendsPackagePrivateSuperClass(clazz.getSuperclass());
            }

            return true;
        }

        /**
         * Collect all of the interfaces this class can be cast too.  Tavers the class hierarchy to include interfaces
         * from the super classes.
         *
         * @param clazz
         * @param classSet
         */
        private void collectInterfaces(Class<?> clazz, Set<Class<?>> classSet) {
            Class<?>[] interfaces = clazz.getInterfaces();
            classSet.addAll(Arrays.asList(interfaces));
            Class<?> superClass = clazz.getSuperclass();

    //
            if (!Object.class.equals(superClass)) {
                collectInterfaces(superClass, classSet);
            }
        }
    }
}
