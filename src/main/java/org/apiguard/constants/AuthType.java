package org.apiguard.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum AuthType {

	BASIC(0, "basic-auth"),
	KEY(1, "key-auth"),
	HMAC(2, "hmac-auth"),
	OAUTH2(3, "oauth2-auth"), 
	JWT(4, "jwt-auth"), 
	LDAP(5, "ldap-auth");

	private int id;
	private String name;

	private AuthType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	private static Map<Integer, AuthType> idMap;
	private static Map<String, AuthType> nameMap;

	static {
		final AuthType[] values = AuthType.values();
		final Map<Integer, AuthType> idEnumMap = new HashMap<Integer, AuthType>();
		final Map<String, AuthType> nameEnumMap = new HashMap<String, AuthType>();

		for (final AuthType status : values) {
			idEnumMap.put(status.getId(), status);
			nameEnumMap.put(status.getName(), status);
		}
		idMap = Collections.unmodifiableMap(idEnumMap);
		nameMap = Collections.unmodifiableMap(nameEnumMap);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static AuthType getAuthById(final Integer id) {
		return idMap.get(id);
	}

	public static AuthType getAuthByName(final String name) {
		return nameMap.get(name);
	}

}
