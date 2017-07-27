package org.apiguard.service;

import com.datastax.driver.core.utils.UUIDs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apiguard.HttpSignature;
import org.apiguard.cassandra.entity.*;
import org.apiguard.cassandra.repo.*;
import org.apiguard.commons.utils.EncryptUtil;
import org.apiguard.constants.AuthType;
import org.apiguard.crypto.Algorithm;
import org.apiguard.crypto.exception.CryptoException;
import org.apiguard.entity.Api;
import org.apiguard.security.jwt.service.JwtService;
import org.apiguard.security.ldap.exceptions.ApiGuardLdapException;
import org.apiguard.security.ldap.service.LdapService;
import org.apiguard.service.exceptions.ApiAuthException;
import org.apiguard.service.exceptions.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;

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

@Component
public class ApiAuthServiceCassandraImpl implements ApiAuthService {

    private static final Logger log = LogManager.getLogger(ApiAuthServiceCassandraImpl.class);

    @Autowired
    ApiService<ApiEntity> apiService;

    @Autowired
    ClientService<ClientEntity> clientService;

    @Autowired
    LdapService ldapService;

    @Autowired
    JwtService jwtService;

    @Autowired
    KeyAuthRepo keyAuthRepo;

    @Autowired
    BasicAuthRepo basicAuthRepo;

    @Autowired
    SignatureAuthRepo signatureAuthRepo;

    @Autowired
    LdapAuthRepo ldapAuthRepo;

    @Autowired
    JwtAuthRepo jwtAuthRepo;

    @Value("${signature.auth.secret}")
    private String secret;

    @Value("${signature.auth.salt}")
    private String salt;

    @Value("${token.prefix}")
    private String tokenPrefix;

    private static TextEncryptor encryptor;

    @PostConstruct
    public void setup() {
        encryptor = Encryptors.queryableText(secret, salt);
    }

    public KeyAuthEntity addKeyAuth(String requestUri, String clientId, String key) throws ApiAuthException {
        log.debug("Adding key auth to requestUri: " + requestUri, ", with clientId: " + clientId);
        ApiEntity api = apiService.getApiByReqUri(requestUri);
        if (api == null) {
            log.error("Api requst uri: " + requestUri + " not found.");
            throw new ApiAuthException("Api requst uri: " + requestUri + " not found.");
        }

        ClientEntity client = clientService.getClient(clientId);
        if (client == null) {
            log.error("Client: " + client + " not found.");
            throw new ApiAuthException("Client: " + client + " not found.");
        }

        Date now = new Date();
        KeyAuthEntity keyAuth = new KeyAuthEntity(UUID.randomUUID().toString(), now, now, requestUri, key, clientId);
        keyAuthRepo.save(keyAuth);

        log.debug("Added key auth to requestUri: " + requestUri, ", with clientId: " + clientId);

        try {
            Api addApi = apiService.updateApiAuth(requestUri, AuthType.KEY, true);
        } catch (ApiException e) {
            log.error("Failed to update api auth with requestUri: " + requestUri);
            throw new ApiAuthException(e.getMessage(), e);
        }

        return keyAuth;
    }

    public KeyAuthEntity getKeyAuth(String key, String requestUri) throws ApiAuthException {
        KeyAuthId id = new KeyAuthId(requestUri, key);
        return keyAuthRepo.findOne(id);
    }

    public BasicAuthEntity addBasicAuth(String requestUri, String clientId, String password)
            throws ApiAuthException {
        ApiEntity api = apiService.getApiByReqUri(requestUri);
        if (api == null) {
            throw new ApiAuthException("Api requst uri: " + requestUri + " not found.");
        }

        ClientEntity client = clientService.getClient(clientId);
        if (client == null) {
            throw new ApiAuthException("Client: " + client + " not found.");
        }

        Date now = new Date();
        BasicAuthEntity basicAuth = new BasicAuthEntity(UUID.randomUUID().toString(), now, now, requestUri, clientId, EncryptUtil.getEncryptedString(password));
        basicAuthRepo.save(basicAuth);

        try {
            Api addApi = apiService.updateApiAuth(requestUri, AuthType.BASIC, true);
        } catch (ApiException e) {
            throw new ApiAuthException(e.getMessage(), e);
        }

        return basicAuth;
    }

    public LdapAuthEntity addLdapAuth(String requestUri, String clientId, String ldapUrl,
                                      String adminDn, String adminPassword, String userBase, String userAttr, Integer cacheExpireInSecond)
            throws ApiAuthException {
        ApiEntity api = apiService.getApiByReqUri(requestUri);
        if (api == null) {
            throw new ApiAuthException("Api requst uri: " + requestUri + " not found.");
        }

        ClientEntity client = clientService.getClient(clientId);
        if (client == null) {
            throw new ApiAuthException("Client: " + client + " not found.");
        }

        Date now = new Date();
        LdapAuthEntity ldapAuth = new LdapAuthEntity(UUID.randomUUID().toString(), now, now, requestUri, clientId,
                ldapUrl, adminDn, encryptor.encrypt(adminPassword), userBase, userAttr, cacheExpireInSecond);
        ldapAuthRepo.save(ldapAuth);

        try {
            Api addApi = apiService.updateApiAuth(requestUri, AuthType.LDAP, true);
        } catch (ApiException e) {
            throw new ApiAuthException(e.getMessage(), e);
        }

        return ldapAuth;
    }

    public SignatureAuthEntity addHttpSignatureAuth(String requestUri, String clientId, String clientAlias, String secret)
            throws ApiAuthException, CryptoException {
        ApiEntity api = apiService.getApiByReqUri(requestUri);
        if (api == null) {
            throw new ApiAuthException("Api requst uri: " + requestUri + " not found.");
        }

        ClientEntity client = clientService.getClient(clientId);
        if (client == null) {
            throw new ApiAuthException("Client: " + client + " not found.");
        }

        Date now = new Date();
        SignatureAuthEntity signatureAuth = null;

        try {
            signatureAuth = new SignatureAuthEntity(UUID.randomUUID().toString(), now, now, requestUri, clientId, clientAlias, encryptor.encrypt(secret));
            signatureAuthRepo.save(signatureAuth);

            Api addApi = apiService.updateApiAuth(requestUri, AuthType.SIGNATURE, true);
        } catch (ApiException e) {
            throw new ApiAuthException(e.getMessage(), e);
        } catch (Exception e) {
            throw new CryptoException(e);
        }

        return signatureAuth;
    }

    public JwtAuthEntity addJwtAuth(String requestUri, String clientId, boolean notBefore, boolean expires) throws ApiAuthException {
        ApiEntity api = apiService.getApiByReqUri(requestUri);
        if (api == null) {
            throw new ApiAuthException("Api requst uri: " + requestUri + " not found.");
        }

        ClientEntity client = clientService.getClient(clientId);
        if (client == null) {
            throw new ApiAuthException("Client: " + client + " not found.");
        }

        Date now = new Date();
        String secret = UUIDs.random().toString();
        JwtAuthEntity jwtAuth = new JwtAuthEntity(UUID.randomUUID().toString(), now, now, requestUri, clientId, secret);

        jwtAuthRepo.save(jwtAuth);

        try {
            Api addApi = apiService.updateApiAuth(requestUri, AuthType.JWT, true);
        } catch (ApiException e) {
            throw new ApiAuthException(e.getMessage(), e);
        }

        return jwtAuth;
    }

    //-------- validations --------- //

    public boolean keyAuthMatches(String requestUri, String key) {
        KeyAuthId id = new KeyAuthId(requestUri, key);
        return keyAuthRepo.exists(id);
    }

    public boolean basicAuthMatches(String requestUri, String clientId, String password) {
        BasicAuthId id = new BasicAuthId(requestUri, clientId);
        BasicAuthEntity res = basicAuthRepo.findOne(id);
        if (res == null) {
            return false;
        }
        return EncryptUtil.verify(password, res.getPassword());
    }

    public boolean signatureAuthMatches(String requestUri, String clientId, String clientAlias, String algorithm, String stringToSign, String signature) throws CryptoException {
        try {
            SignatureAuthId id = new SignatureAuthId(requestUri, clientId, clientAlias);
            SignatureAuthEntity res = signatureAuthRepo.findOne(id);
            if (res == null) {
                return false;
            }
            String serverSign = HttpSignature.signWithBase64(encryptor.decrypt(res.getSecret()), stringToSign, Algorithm.getAlgorithmByName(algorithm));
            return serverSign.equals(signature);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public boolean jwtAuthMatches(String token) {
        String claims = jwtAuthValidateAndGetPayload(token);
        if (StringUtils.isEmpty(claims)) {
            return false;
        }

        return true;
    }

    public String jwtAuthValidateAndGetPayload(String token) {
        try {
            String issuer = jwtService.getIssuer(token);
            JwtAuthEntity res = jwtAuthRepo.findOne(issuer);
            if (res == null) {
                log.warn("Jwt auth validation failed because token: " + token + " is not found.");
                return null;
            }

            String secret = res.getSecret();
            String claimsAndVerify = jwtService.getClaimsAndVerify(token, secret);

            if (res.isNotBefore() && jwtService.isBefore(claimsAndVerify)) {
                log.warn("Jwt auth validation failed with token: " + token + ", because token is invalid with nbf criteria.");
                return null;
            }

            if (res.isExpires() && jwtService.isExpired(claimsAndVerify)) {
                log.warn("Jwt auth validation failed with token: " + token + ", because token is expired.");
                return null;
            }

            return claimsAndVerify;
        } catch (Exception e) {
            log.warn("Jwt auth validation failed with token: " + token + ", with error: " + e.getMessage(), e);
            return null;
        }
    }

    public String jwtAuthGetPayload(String token) {
        return jwtService.getClaims(token);
    }

    public boolean ldapAuthMatches(String requestUri, String clientId, String password) {
        LdapAuthId id = new LdapAuthId(requestUri, clientId);
        LdapAuthEntity res = ldapAuthRepo.findOne(id);
        if (res == null) {
            return false;
        }

        try {

            if (isExpired(res.getCacheExpireInSecond(), res.getLastLoginDate())) {
                boolean authenticated = ldapService.authenticate(res.getLdapUrl(), res.getAdminDn(), encryptor.decrypt(res.getAdminPassword()),
                        res.getUserBase(), res.getUserAttr(), clientId, password);
                if (!authenticated) {
                    return false;
                }

                res.setLastLoginDate(new Date());
                res.setToken(EncryptUtil.getEncryptedString(getToken(password)));
                ldapAuthRepo.save(res);
                return true;
            } else if (EncryptUtil.verify(password, getToken(res.getToken()))) {
                return true;
            } else {
                return false;
            }

        } catch (ApiGuardLdapException e) {
            log.warn(e.getMessage());
            log.trace(e);
            return false;
        }
    }

    private boolean isExpired(Integer cacheExpiredInSec, Date lastLoginDate) {
        if (cacheExpiredInSec == null || lastLoginDate == null) {
            return true;
        }
        return System.currentTimeMillis() > (lastLoginDate.getTime() + (cacheExpiredInSec * 1000));
    }

    private String getToken(String str) {
        return tokenPrefix + str;
    }
}
