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

package org.openecomp.appc.util;

import java.lang.reflect.Array;
import java.util.Map;

public class ObjectMapper {

	private ObjectMapper() {
	}

	private static void dispatch(PathContext context, Object obj) {

		if (obj == null) {
			return;
		}

		final Class<?> cls = obj.getClass();

		if (cls.isPrimitive()
				|| String.class.isAssignableFrom(cls)
				|| Number.class.isAssignableFrom(cls)
				|| Boolean.class.isAssignableFrom(cls)) {
			handlePrimitive(context, obj);
		} else if (cls.isArray()) {
			handleArray(context, obj);
		} else if (Map.class.isAssignableFrom(cls)) {
			handleMap(context, (Map<?, ?>) obj);
		} else if (Iterable.class.isAssignableFrom(cls)) {
			handleCollection(context, Iterable.class.cast(obj));
		} else {
			throw new IllegalArgumentException(obj.getClass().getName());
		}
	}

	public static Map<String, String> map(Object obj) {
		PathContext context = new PathContext();
		dispatch(context, obj);
		return context.entries();
	}

	private static void handleMap(PathContext context, Map<?, ?> val) {
		for (Map.Entry<?, ?> entry : val.entrySet()) {
			context.pushToken(entry.getKey().toString());
			dispatch(context, entry.getValue());
			context.popToken();
		}
	}

	private static void handleCollection(PathContext context, Iterable<?> val) {
		int index = 0;
		for (Object elem : val) {
			handleElement(context, index++, elem);
		}
	}

	private static void handleArray(PathContext context, Object val) {
		for (int i = 0, n = Array.getLength(val); i < n; i++) {
			handleElement(context, i, Array.get(val, i));
		}
	}

	private static void handleElement(PathContext context, int index, Object val) {
		if (val == null) {
			return;
		}

		String modifier = new StringBuilder().append('[').append(Integer.valueOf(index)).append(']').toString();

		context.pushModifier(modifier);
		dispatch(context, val);
		context.popModifier();
	}

	private static void handlePrimitive(PathContext context, Object val) {
		context.entry(context.getPath(), val.toString());
	}
}
