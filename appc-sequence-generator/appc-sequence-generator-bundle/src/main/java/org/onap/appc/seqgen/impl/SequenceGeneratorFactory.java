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


package org.onap.appc.seqgen.impl;

import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.seqgen.SequenceGenerator;

public class SequenceGeneratorFactory {

    private static class ReferenceHolder {
        private static SequenceGeneratorFactory instance = new SequenceGeneratorFactory();
        private ReferenceHolder(){

        }
    }

    private SequenceGeneratorFactory(){

    }

    public static SequenceGeneratorFactory getInstance(){
        return ReferenceHolder.instance;
    }

    public SequenceGenerator createSequenceGenerator(VNFOperation operation) throws APPCException {
        switch (operation){
            case Start:
                return new StartSequenceGenerator();
            case Stop:
                return  new StopSequenceGenerator();
            case Restart:
                return new RestartSequenceGenerator();
            default:
                    throw new APPCException("Sequence Generator does not support operation "  + operation.name());
        }
    }
}
