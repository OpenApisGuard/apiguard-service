package org.apiguard.service;

import java.util.Date;
import java.util.UUID;

import org.apiguard.cassandra.entity.ClientEntity;
import org.apiguard.cassandra.repo.ClientRepo;
import org.apiguard.service.exceptions.ApiException;
import org.apiguard.service.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.utils.UUIDs;

@Component("cassandraClientService")
public class ClientServiceCassandraImpl implements ClientService<ClientEntity>{
	
	@Autowired
	ClientRepo clientRepo;

	@Override
	public ClientEntity addClient(String clientId) throws ClientException {
		if (clientRepo.exists(clientId)) {
			throw new ClientException("Client id: " + clientId + " already exists.");
		}
		
		UUID id = UUIDs.timeBased();
		Date now = new Date();
		ClientEntity ce = new ClientEntity(id, now, now, clientId);
		clientRepo.save(ce);
		return ce;
	}

	@Override
	public ClientEntity getClient(String clientId) {
		return clientRepo.findOne(clientId);
	}

	@Override
	public boolean exists(String clientId) {
		return clientRepo.exists(clientId);
	}
}
