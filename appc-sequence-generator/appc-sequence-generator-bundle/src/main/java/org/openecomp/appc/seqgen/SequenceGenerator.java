/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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
 */

package org.openecomp.appc.seqgen;


import org.openecomp.appc.dg.objects.VnfcFlowModel;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.seqgen.objects.SequenceGeneratorInput;
import org.openecomp.appc.seqgen.objects.Transaction;

import java.util.List;
import java.util.Map;

/**
 * Sequence Generator API generates runtime sequence for LCM operations execution
 */
public interface SequenceGenerator {
    /**
     *
     * @param input Sequence Generator Input containing request info, vnf capabilites and tunable parameters
     * @return returns runtime sequence for LCM operation execution
     * @throws APPCException
     */
    List<Transaction> generateSequence(SequenceGeneratorInput input) throws APPCException;
}
