package org.apiguard.service;

import org.apiguard.service.exceptions.ApiAuthException;

public interface ApiAuthService<T> {

	public T addKeyAuth(String key, String requestUri, String clientId) throws ApiAuthException;

	public T getKeyAuth(String key, String requestUri) throws ApiAuthException;

}
