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

package org.openecomp.appc.mdsal.impl;

import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.mdsal.MDSALStore;
import org.openecomp.appc.mdsal.exception.MDSALStoreException;
import org.openecomp.appc.mdsal.objects.BundleInfo;
import org.openecomp.appc.mdsal.operation.ConfigOperation;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Implementation of MDSALStore
 */
public class MDSALStoreImpl implements MDSALStore{

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(MDSALStoreImpl.class);

    MDSALStoreImpl(){
        ConfigOperation.setUrl(Constants.CONFIG_URL);
        ConfigOperation.setAuthentication(null,null);
    }


    @Override
    public boolean isModulePresent(String moduleName, Date revision) {

        if(logger.isDebugEnabled()){
            logger.debug("isModulePresent invoked with moduleName = " +moduleName + " , revision = " +revision);
        }

        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        /**
         * SchemaContext interface of ODL provides APIs for querying details of yang modules
         * loaded into MD-SAL store, but its limitation is, it only returns information about
         * static yang modules loaded on server start up, it does not return information about
         * the yang modules loaded dynamically. Due to this limitation, we are checking the
         * presence of OSGI bundle instead of yang module. (Note: Assuming OSGI bundle is named
         * with the yang module name).
         */

        Bundle bundle = bundleContext.getBundle(moduleName);
        if(logger.isDebugEnabled()){
            logger.debug("isModulePresent returned = " + (bundle != null));
        }
        return bundle != null;
    }

    @Override
    public void storeYangModule(String yang, BundleInfo bundleInfo) throws MDSALStoreException {

        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        byte[] byteArray = createBundleJar(yang, Constants.BLUEPRINT, bundleInfo);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray)){
            Bundle bundle = bundleContext.installBundle(bundleInfo.getLocation(), inputStream);
            bundle.start();
        } catch (Exception e) {
            logger.error("Error storing yang module: " + yang + ". Error message: " + e.getMessage());
            throw new MDSALStoreException("Error storing yang module: " + yang + " " + e.getMessage(), e);
        }
    }

    @Override
    public void storeJson( String module , String requestId ,String configJSON) throws MDSALStoreException {

        try {
            ConfigOperation.storeConfig(configJSON , module , org.openecomp.appc.Constants.YANG_BASE_CONTAINER, org.openecomp.appc.Constants.YANG_VNF_CONFIG_LIST,requestId,org.openecomp.appc.Constants.YANG_VNF_CONFIG);
        } catch (APPCException e) {
            throw new MDSALStoreException("Exception while storing config json to MDSAL store." +e.getMessage(), e);
        }
    }

    private byte[] createBundleJar(String yang, String blueprint, BundleInfo bundleInfo) throws MDSALStoreException {

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, Constants.MANIFEST_VALUE_VERSION);
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_NAME), bundleInfo.getName());
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_SYMBOLIC_NAME), bundleInfo.getName());
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_DESCRIPTION), bundleInfo.getDescription());
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_MANIFEST_VERSION), Constants.MANIFEST_VALUE_BUNDLE_MAN_VERSION);
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_VERSION), String.valueOf(bundleInfo.getVersion()));
        manifest.getMainAttributes().put(new Attributes.Name(Constants.MANIFEST_ATTR_BUNDLE_BLUEPRINT), Constants.MANIFEST_VALUE_BUNDLE_BLUEPRINT);

        byte[] retunValue;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest)) {
            jarOutputStream.putNextEntry(new JarEntry("META-INF/yang/"));
            jarOutputStream.putNextEntry(new JarEntry("META-INF/yang/"+bundleInfo.getName()+".yang"));
            jarOutputStream.write(yang.getBytes());
            jarOutputStream.closeEntry();

            jarOutputStream.putNextEntry(new JarEntry("OSGI-INF/blueprint/"));
            jarOutputStream.putNextEntry(new JarEntry(Constants.MANIFEST_VALUE_BUNDLE_BLUEPRINT));
            jarOutputStream.write(blueprint.getBytes());
            jarOutputStream.closeEntry();
            jarOutputStream.close();
            retunValue = outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error creating bundle jar: " + bundleInfo.getName() + ". Error message: " + e.getMessage());
            throw new MDSALStoreException("Error creating bundle jar: " + bundleInfo.getName() + " " + e.getMessage(), e);
        }
        return retunValue;
    }
}
