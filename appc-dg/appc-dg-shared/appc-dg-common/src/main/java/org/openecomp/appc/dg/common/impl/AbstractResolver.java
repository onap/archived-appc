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

package org.openecomp.appc.dg.common.impl;

import org.openecomp.appc.rankingframework.RankedAttributesResolver;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.concurrent.locks.ReentrantLock;

abstract class AbstractResolver {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AbstractResolver.class);

    private long interval;

    private volatile long lastUpdate = 0l;
    private volatile boolean isUpdateInProgress = false;
    private volatile RankedAttributesResolver<FlowKey> dgResolver;

    private final ReentrantLock INIT_LOCK = new ReentrantLock();

    AbstractResolver(int interval) {
        this.interval = interval * 1000;
    }

    private RankedAttributesResolver<FlowKey> createResolver(String resolverType) {
        AbstractResolverDataReader reader = ResolverDataReaderFactory.createResolverDataReader(resolverType);
        return reader.read();
    }

    private boolean isExpired() {
        return (System.currentTimeMillis() - lastUpdate) > interval;
    }

    protected RankedAttributesResolver<FlowKey> resolver(String resolverType) {

        /*
         * In general case, the method implementation is non-blocking. The first
         * thread that identifies data expiration will be used to refresh it. In
         * meanwhile, any other thread will get the old instance without waiting
         * for the updated one. The only exception is the very first time when
         * previous instance doesn't exist - in such a cases all the threads
         * will be waiting on INIT_LOCK while one of them initializes the
         * resolver instance. NOTE: The initialization is intentionally
         * implemented in lazy manner to make sure the bundle is initialized
         * properly on startup regardless whether or not the data is correct.
         * Afterwards, the resolver may be instantiated as many times as needed.
         */

        try {

            if (dgResolver == null) {
                INIT_LOCK.lock();
                if (dgResolver != null) {
                    INIT_LOCK.unlock();
                }
            }

            if (!isUpdateInProgress && isExpired()) {

                boolean doUpgrade = false;

                synchronized (this) {
                    if (!isUpdateInProgress) {
                        isUpdateInProgress = true;
                        doUpgrade = true;
                    }
                }

                if (doUpgrade) {

                    logger.info("DG resolver configuration data has expired - initiating refresh");

                    try {
                        RankedAttributesResolver<FlowKey> temp = createResolver(resolverType);
                        dgResolver = temp;
                        lastUpdate = System.currentTimeMillis();

                        logger.info("DG resolver configuration data has been refreshed successfully");
                    } finally {
                        isUpdateInProgress = false;
                    }
                }
            }
        } finally {
            if (INIT_LOCK.isHeldByCurrentThread()) {
                INIT_LOCK.unlock();
            }
        }

        return dgResolver;
    }
    protected abstract FlowKey resolve(final String...args);
}
