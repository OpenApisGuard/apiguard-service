package org.apiguard.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum AuthType {

	BASIC(0, "basic-auth", "username"),
	KEY(1, "key-auth", "apiKey"),
	HMAC(2, "hmac-auth", "Authorization-hmac"),
	OAUTH2(3, "oauth2-auth", "Authorization-oauth2"), 
	JWT(4, "jwt-auth", "Authorization-jwt"), 
	LDAP(5, "ldap-auth", "Authorization-ldap");

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
