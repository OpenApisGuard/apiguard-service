package org.apiguard.service;

import org.apiguard.cassandra.entity.*;
import org.apiguard.crypto.exception.CryptoException;
import org.apiguard.service.exceptions.ApiAuthException;

public interface ApiAuthService {

	public KeyAuthEntity addKeyAuth(String requestUri, String clientId, String key) throws ApiAuthException;

	public KeyAuthEntity getKeyAuth(String requestUri, String key) throws ApiAuthException;
	
	public BasicAuthEntity addBasicAuth(String requestUri, String clientId, String password) throws ApiAuthException;

	public SignatureAuthEntity addHttpSignatureAuth(String requestUri, String clientId, String clientAlias, String secret) throws ApiAuthException, CryptoException;

	public LdapAuthEntity addLdapAuth(String requestUri, String clientId, String ldapUrl,
									  String adminDn, String adminPassword, String userBase, String userAttr, Integer cacheExpireInSecond) throws ApiAuthException;

	public JwtAuthEntity addJwtAuth(String requestUri, String clientId, boolean notBefore, boolean expires) throws ApiAuthException;

	public boolean keyAuthMatches(String requestUri, String key);
	
	public boolean basicAuthMatches(String requestUri, String clientId, String password);

	public boolean signatureAuthMatches(String requestUri, String clientId, String clientAlias, String algorithm, String stringToSign, String signature) throws CryptoException;

	public boolean ldapAuthMatches(String requestUri, String clientId, String password);

	public boolean jwtAuthMatches(String token);

	public String jwtAuthGetPayload(String token);

	public String jwtAuthValidateAndGetPayload(String token);

}
