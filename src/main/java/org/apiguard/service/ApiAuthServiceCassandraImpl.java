package org.apiguard.service;

import java.util.Date;

import org.apiguard.cassandra.entity.ApiEntity;
import org.apiguard.cassandra.entity.BasicAuthEntity;
import org.apiguard.cassandra.entity.BasicAuthId;
import org.apiguard.cassandra.entity.ClientEntity;
import org.apiguard.cassandra.entity.KeyAuthEntity;
import org.apiguard.cassandra.entity.KeyAuthId;
import org.apiguard.cassandra.repo.BasicAuthRepo;
import org.apiguard.cassandra.repo.KeyAuthRepo;
import org.apiguard.commons.utils.PasswordUtil;
import org.apiguard.service.exceptions.ApiAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.utils.UUIDs;

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

	@Override
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
		
		return keyAuth;
	}

	@Override
	public KeyAuthEntity getKeyAuth(String key, String requestUri) throws ApiAuthException {
		KeyAuthId id = new KeyAuthId(requestUri, key);
		return keyAuthRepo.findOne(id);
	}

	@Override
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
		BasicAuthEntity basicAuth = new BasicAuthEntity(UUIDs.timeBased(), now, now, requestUri, clientId, PasswordUtil.getEncryptedPassword(password));
		basicAuthRepo.save(basicAuth);
		return basicAuth;
	}

	@Override
	public boolean keyAuthMatches(String requestUri, String key) {
		KeyAuthId id = new KeyAuthId(requestUri, key);
		return keyAuthRepo.exists(id);
	}

	@Override
	public boolean basicAuthMatches(String requestUri, String clientId, String password) {
		BasicAuthId id = new BasicAuthId(requestUri, clientId);
		BasicAuthEntity res = basicAuthRepo.findOne(id);
		return PasswordUtil.verifyPassword(password, res.getPassword());
	}
}
