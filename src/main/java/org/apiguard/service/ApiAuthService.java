package org.apiguard.service;

import org.apiguard.cassandra.entity.*;
import org.apiguard.crypto.exception.CryptoException;
import org.apiguard.service.exceptions.ApiAuthException;

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

public interface ApiAuthService {

	public KeyAuthEntity addKeyAuth(String requestUri, String clientId, String key) throws ApiAuthException;
	
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
