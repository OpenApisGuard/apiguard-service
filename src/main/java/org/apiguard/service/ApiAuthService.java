package org.apiguard.service;

import org.apiguard.cassandra.entity.BasicAuthEntity;
import org.apiguard.cassandra.entity.KeyAuthEntity;
import org.apiguard.cassandra.entity.SignatureAuthEntity;
import org.apiguard.crypto.exception.CryptoException;
import org.apiguard.service.exceptions.ApiAuthException;

public interface ApiAuthService {

	public KeyAuthEntity addKeyAuth(String requestUri, String clientId, String key) throws ApiAuthException;

	public KeyAuthEntity getKeyAuth(String requestUri, String key) throws ApiAuthException;
	
	public BasicAuthEntity addBasicAuth(String requestUri, String clientId, String password) throws ApiAuthException;

	public SignatureAuthEntity addHttpSignatureAuth(String requestUri, String clientId, String clientAlias, String secret) throws ApiAuthException, CryptoException;
	
	public boolean keyAuthMatches(String requestUri, String key);
	
	public boolean basicAuthMatches(String requestUri, String clientId, String password);

	public boolean signatureAuthMatches(String requestUri, String clientId, String clientAlias, String algorithm, String stringToSign, String signature) throws CryptoException;

}
