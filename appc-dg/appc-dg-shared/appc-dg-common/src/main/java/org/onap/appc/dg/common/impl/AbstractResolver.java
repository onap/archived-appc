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

package org.onap.appc.dg.common.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.concurrent.locks.ReentrantLock;
import org.onap.appc.rankingframework.RankedAttributesResolver;

abstract class AbstractResolver {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AbstractResolver.class);
    private static final long INTERVAL_MULTIPLIER = 1000L;

    private long interval;
    private volatile long lastUpdate = 0L;
    private volatile boolean isUpdateInProgress = false;
    private volatile RankedAttributesResolver<FlowKey> dgResolver;

    private final ReentrantLock initLock = new ReentrantLock();

    AbstractResolver(int interval) {
        this.interval = interval * INTERVAL_MULTIPLIER;
    }

    private RankedAttributesResolver<FlowKey> createResolver(String resolverType) {
        AbstractResolverDataReader reader = ResolverDataReaderFactory.createResolverDataReader(resolverType);

        if (reader == null) {
            throw new DataReaderException("Cannot read data since reader is null");
        }
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
         * will be waiting on initLock while one of them initializes the
         * resolver instance. NOTE: The initialization is intentionally
         * implemented in lazy manner to make sure the bundle is initialized
         * properly on startup regardless whether or not the data is correct.
         * Afterwards, the resolver may be instantiated as many times as needed.
         */
        try {
            if (dgResolver == null) {
                initLock.lock();
                if (dgResolver != null) {
                    initLock.unlock();
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
                    tryRefreshConfig(resolverType);
                }
            }
        } finally {
            if (initLock.isHeldByCurrentThread()) {
                initLock.unlock();
            }
        }
        return dgResolver;
    }

    private void tryRefreshConfig(String resolverType) {
        try {
            dgResolver = createResolver(resolverType);
            lastUpdate = System.currentTimeMillis();

            logger.info("DG resolver configuration data has been refreshed successfully");
        } finally {
            isUpdateInProgress = false;
        }
    }

    protected abstract FlowKey resolve(final String... args);
}
