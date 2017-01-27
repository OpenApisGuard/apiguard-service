package org.apiguard.service;

import java.util.Date;

import org.apiguard.cassandra.entity.ApiEntity;
import org.apiguard.cassandra.entity.AuthKeyEntity;
import org.apiguard.cassandra.entity.ClientEntity;
import org.apiguard.cassandra.repo.ApiAuthRepo;
import org.apiguard.cassandra.repo.ApiAuthRepo.AuthId;
import org.apiguard.service.exceptions.ApiAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.utils.UUIDs;

@Component
public class ApiAuthServiceCassandraImpl implements ApiAuthService<AuthKeyEntity>{
	
	@Autowired
	ApiService<ApiEntity> apiService;

	@Autowired
	ClientService<ClientEntity> clientService;

	@Autowired
	ApiAuthRepo apiAuthRepo;

	@Override
	public AuthKeyEntity addKeyAuth(String key, String requestUri, String clientId) throws ApiAuthException {
		ApiEntity api = apiService.getApiByReqUri(requestUri);
		if (api == null) {
			throw new ApiAuthException("Api requst uri: " + requestUri + " not found.");
		}
		
		ClientEntity client = clientService.getClient(clientId);
		if (client == null) {
			throw new ApiAuthException("Client: " + client + " not found.");
		}
		
		AuthKeyEntity keyAuth = new AuthKeyEntity(UUIDs.timeBased(), new Date(), key, requestUri, clientId);
		apiAuthRepo.save(keyAuth);
		
		return keyAuth;
	}

	@Override
	public AuthKeyEntity getKeyAuth(String key, String requestUri) throws ApiAuthException {
		AuthId id = new AuthId(key, requestUri);
		return apiAuthRepo.findOne(id);
	}

	
}
