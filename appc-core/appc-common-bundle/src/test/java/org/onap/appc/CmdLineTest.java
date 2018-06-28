/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.appc;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class CmdLineTest {
    @Test
    public void testMain() {
        String argv[];
        argv = new String[] {"encrypt","abc","ghi"};
        CmdLine.main(argv);
        argv = new String[0];
        CmdLine.main(argv);
        argv = new String[] {"encrypt","abc"};
        CmdLine.main(argv);
        
        CmdLine cmdLine = new CmdLine();
        assertNotNull(cmdLine);
    }
}
