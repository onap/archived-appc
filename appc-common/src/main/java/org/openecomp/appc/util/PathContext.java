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


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

class PathContext {

	private StringBuilder path = new StringBuilder(128);

	private LinkedList<Integer> indexes = new LinkedList<>();
	private Map<String, String> entries = new LinkedHashMap<>();
	private int offset = 0;

	private final String delimiter;

	PathContext() {
		this(".");
	}

	PathContext(String delimiter) {
		this.delimiter = delimiter;
	}

	private void push(String elem, boolean delimit) {
		if (elem == null) {
			throw new IllegalArgumentException();
		}

		int length = elem.length();

		if (delimit && !indexes.isEmpty()) {
			path.append(delimiter);
			length += delimiter.length();
		}

		path.append(elem);
		offset += length;
		indexes.addLast(Integer.valueOf(length));
	}

	private void pop() {
		if (indexes.isEmpty()) {
			throw new IllegalStateException();
		}
		offset -= indexes.removeLast();
		path.setLength(offset);
	}

	void pushToken(String token) {
		push(token, true);
	}

	void popToken() {
		pop();
	}

	void pushModifier(String modifier) {
		push(modifier, false);
	}

	void popModifier() {
		pop();
	}

	String getPath() {
		return path.substring(0, offset);
	}

	void entry(String name, String value) {
		entries.put(name, value);
	}

	Map<String, String> entries() {
		return Collections.unmodifiableMap(entries);
	}
}
