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

package org.openecomp.appc.instar;

import java.util.LinkedList;
import java.util.List;

import org.openecomp.appc.instar.node.InstarClientNode;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class InstarClientActivator implements BundleActivator{

	private List<ServiceRegistration> registrations = new LinkedList<ServiceRegistration>();
	private static final EELFLogger log = EELFManager.getInstance().getLogger(InstarClientActivator.class);

	@Override
	public void start(BundleContext ctx) throws Exception
	{

		InstarClientNode instarClientNode = new InstarClientNode();
		log.info("Registering service "+ instarClientNode.getClass().getName());
		registrations.add(ctx.registerService(instarClientNode.getClass().getName(), instarClientNode, null));
		log.info("Registering service sccessful for  "+ instarClientNode.getClass().getName());

	}
	@Override
	public void stop(BundleContext arg0) throws Exception
	{
		for (ServiceRegistration registration: registrations)
		{
			registration.unregister();
			registration = null;
		}

	}

}
