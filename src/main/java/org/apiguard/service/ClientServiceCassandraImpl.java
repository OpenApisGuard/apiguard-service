package org.apiguard.service;

import com.datastax.driver.core.utils.UUIDs;
import org.apiguard.cassandra.entity.ClientEntity;
import org.apiguard.cassandra.repo.ClientRepo;
import org.apiguard.service.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component("cassandraClientService")
public class ClientServiceCassandraImpl implements ClientService<ClientEntity>{
	
	@Autowired
	ClientRepo clientRepo;

	public ClientEntity addClient(String clientId) throws ClientException {
		if (clientRepo.exists(clientId)) {
			throw new ClientException("Client id: " + clientId + " already exists.");
		}
		
		Date now = new Date();
		ClientEntity ce = new ClientEntity(UUIDs.timeBased().toString(), now, now, clientId);
		clientRepo.save(ce);
		return ce;
	}

	public ClientEntity getClient(String clientId) {
		return clientRepo.findOne(clientId);
	}

	public boolean exists(String clientId) {
		return clientRepo.exists(clientId);
	}
}
