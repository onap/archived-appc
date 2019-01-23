/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.sdc.artifacts.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;

public class ArtifactProcessorUtility {

	public static INotificationData getNotificationData()
			throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {

		INotificationData notificationData = (INotificationData) getObject("org.onap.sdc.impl.NotificationDataImpl");

		List<IArtifactInfo> serviceArtifacts = getServiceArtifacts();

		invokeMethod(notificationData, "setServiceArtifacts", serviceArtifacts);
		return notificationData;
	}

	public static List<IResourceInstance> getResources()
			throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
		List<IResourceInstance> resources = new ArrayList<>();
		IResourceInstance resource = (IResourceInstance) getObject("org.onap.sdc.impl.JsonContainerResourceInstance");

		List<IArtifactInfo> serviceArtifacts = getServiceArtifacts();
		invokeMethod(resource, "setArtifacts", serviceArtifacts);
		invokeMethod(resource, "setResourceName", "Vnf");
		invokeMethod(resource, "setResourceVersion", "1.0");

		resources.add(resource);
		return resources;
	}

	private static void invokeMethod(Object object, String methodName, Object... arguments)
			throws IllegalAccessException, InvocationTargetException {
		Method[] methods = object.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (methodName.equalsIgnoreCase(method.getName())) {
				method.setAccessible(true);
				method.invoke(object, arguments);
			}
		}
	}

	private static Object getObject(String fqcn)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor constructor = Arrays.asList(Class.forName(fqcn).getDeclaredConstructors()).stream()
				.filter(constructor1 -> constructor1.getParameterCount() == 0).collect(Collectors.toList()).get(0);
		constructor.setAccessible(true);
		return constructor.newInstance();
	}

	public static List<IArtifactInfo> getServiceArtifacts()
			throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
		List<IArtifactInfo> serviceArtifacts = new ArrayList<>();
		IArtifactInfo artifactInfo = (IArtifactInfo) getObject("org.onap.sdc.impl.ArtifactInfoImpl");
		invokeMethod(artifactInfo, "setArtifactType", "VF_LICENSE");
		invokeMethod(artifactInfo, "setArtifactUUID", "abcd-efgh-ijkl");
		serviceArtifacts.add(artifactInfo);
		return serviceArtifacts;
	}
}
