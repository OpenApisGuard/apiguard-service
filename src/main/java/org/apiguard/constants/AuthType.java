package org.apiguard.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/*
 * Copyright 2017 the original author or authors.
 *
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
 */

public enum AuthType {

	BASIC(0, "basic-auth", "basic"),
	KEY(1, "key-auth", "apiKey"),
	HMAC(2, "hmac-auth", "hmac"),
	OAUTH2(3, "oauth2-auth", "oauth2"),
	JWT(4, "jwt-auth", "bearer"),
	LDAP(5, "ldap-auth", "ldap"),
	SIGNATURE(6, "signature-auth", "signature"),
	DIGITAL_SIGNATURE(7, "digital-signature-auth", "digital");

	private int id;
	private String name;
	private String key;

	private AuthType(int id, String name, String key) {
		this.id = id;
		this.name = name;
		this.key = key;
	}

	private static Map<Integer, AuthType> idMap;
	private static Map<String, AuthType> nameMap;
	private static Map<String, AuthType> keyMap;

	static {
		final AuthType[] values = AuthType.values();
		final Map<Integer, AuthType> idEnumMap = new HashMap<Integer, AuthType>();
		final Map<String, AuthType> nameEnumMap = new HashMap<String, AuthType>();
		final Map<String, AuthType> keyEnumMap = new HashMap<String, AuthType>();

		for (final AuthType status : values) {
			idEnumMap.put(status.getId(), status);
			nameEnumMap.put(status.getName(), status);
			keyEnumMap.put(status.getKey(), status);
		}
		idMap = Collections.unmodifiableMap(idEnumMap);
		nameMap = Collections.unmodifiableMap(nameEnumMap);
		keyMap = Collections.unmodifiableMap(keyEnumMap);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getKey() {
		return key;
	}

	public static AuthType getAuthById(final Integer id) {
		return idMap.get(id);
	}

	public static AuthType getAuthByName(final String name) {
		return nameMap.get(name);
	}

	public static AuthType getAuthByKey(final String key) {
		return keyMap.get(key);
	}
}
