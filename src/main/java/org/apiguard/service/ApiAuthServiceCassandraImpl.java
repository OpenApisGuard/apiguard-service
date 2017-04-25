package org.apiguard.service;

import com.datastax.driver.core.utils.UUIDs;
import org.apiguard.HttpSignature;
import org.apiguard.cassandra.entity.*;
import org.apiguard.cassandra.repo.BasicAuthRepo;
import org.apiguard.cassandra.repo.KeyAuthRepo;
import org.apiguard.cassandra.repo.SignatureAuthRepo;
import org.apiguard.commons.utils.EncryptUtil;
import org.apiguard.constants.AuthType;
import org.apiguard.crypto.Algorithm;
import org.apiguard.crypto.exception.CryptoException;
import org.apiguard.entity.Api;
import org.apiguard.service.exceptions.ApiAuthException;
import org.apiguard.service.exceptions.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Component
public class ApiAuthServiceCassandraImpl implements ApiAuthService {
	
	@Autowired
	ApiService<ApiEntity> apiService;

	@Autowired
	ClientService<ClientEntity> clientService;

	@Autowired
	KeyAuthRepo keyAuthRepo;
	
	@Autowired
	BasicAuthRepo basicAuthRepo;

	@Autowired
    SignatureAuthRepo signatureAuthRepo;

	@Value("${signature.auth.secret}")
	private String secret;

	@Value("${signature.auth.salt}")
    private String salt;

	private static TextEncryptor encryptor;

    @PostConstruct
    public void setup() {
        encryptor = Encryptors.queryableText(secret, salt);
    }

	public KeyAuthEntity addKeyAuth(String requestUri, String clientId, String key) throws ApiAuthException {
		ApiEntity api = apiService.getApiByReqUri(requestUri);
		if (api == null) {
			throw new ApiAuthException("Api requst uri: " + requestUri + " not found.");
		}
		
		ClientEntity client = clientService.getClient(clientId);
		if (client == null) {
			throw new ApiAuthException("Client: " + client + " not found.");
		}
		
		Date now = new Date();
		KeyAuthEntity keyAuth = new KeyAuthEntity(UUIDs.timeBased(), now, now, requestUri, key, clientId);
		keyAuthRepo.save(keyAuth);

		try {
            Api addApi = apiService.updateApiAuth(requestUri, AuthType.KEY, true);
        }
        catch(ApiException e) {
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
		BasicAuthEntity basicAuth = new BasicAuthEntity(UUIDs.timeBased(), now, now, requestUri, clientId, EncryptUtil.getEncryptedString(password));
		basicAuthRepo.save(basicAuth);

        try {
            Api addApi = apiService.updateApiAuth(requestUri, AuthType.BASIC, true);
        } catch (ApiException e) {
            throw new ApiAuthException(e.getMessage(), e);
        }

        return basicAuth;
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
            signatureAuth = new SignatureAuthEntity(UUIDs.timeBased(), now, now, requestUri, clientId, clientAlias, encryptor.encrypt(secret));
            signatureAuthRepo.save(signatureAuth);

            Api addApi = apiService.updateApiAuth(requestUri, AuthType.SIGNATURE, true);
        }
        catch (ApiException e) {
            throw new ApiAuthException(e.getMessage(), e);
        }
        catch (Exception e) {
            throw new CryptoException(e);
        }

        return signatureAuth;
	}

	public boolean keyAuthMatches(String requestUri, String key) {
		KeyAuthId id = new KeyAuthId(requestUri, key);
		return keyAuthRepo.exists(id);
	}

	public boolean basicAuthMatches(String requestUri, String clientId, String password) {
		BasicAuthId id = new BasicAuthId(requestUri, clientId);
		BasicAuthEntity res = basicAuthRepo.findOne(id);
		return EncryptUtil.verify(password, res.getPassword());
	}

    public boolean signatureAuthMatches(String requestUri, String clientId, String clientAlias, String algorithm, String stringToSign, String signature) throws CryptoException {
	    try {
            SignatureAuthId id = new SignatureAuthId(requestUri, clientId, clientAlias);
            SignatureAuthEntity res = signatureAuthRepo.findOne(id);
            String serverSign = HttpSignature.signWithBase64(encryptor.decrypt(res.getSecret()), stringToSign, Algorithm.getAlgorithmByName(algorithm));
            return serverSign.equals(signature);
        }
        catch (Exception e) {
	        throw new CryptoException(e);
        }
    }
}
