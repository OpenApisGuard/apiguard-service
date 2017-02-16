package org.apiguard.service;

import org.apiguard.cassandra.entity.BasicAuthEntity;
import org.apiguard.cassandra.entity.KeyAuthEntity;
import org.apiguard.service.exceptions.ApiAuthException;

public interface ApiAuthService {

	public KeyAuthEntity addKeyAuth(String requestUri, String clientId, String key) throws ApiAuthException;

	public KeyAuthEntity getKeyAuth(String requestUri, String key) throws ApiAuthException;
	
	public BasicAuthEntity addBasicAuth(String requestUri, String clientId, String password) throws ApiAuthException;
	
	public boolean keyAuthMatches(String requestUri, String key);
	
	public boolean basicAuthMatches(String requestUri, String clientId, String password);

}
