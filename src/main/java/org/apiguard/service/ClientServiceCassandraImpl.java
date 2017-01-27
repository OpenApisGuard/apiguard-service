package org.apiguard.service;

import org.apiguard.cassandra.entity.ClientEntity;
import org.apiguard.cassandra.repo.ClientRepo;
import org.apiguard.service.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("cassandraClientService")
public class ClientServiceCassandraImpl implements ClientService<ClientEntity>{
	
	@Autowired
	ClientRepo clientRepo;

	@Override
	public ClientEntity addClient(String clientId) throws ClientException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientEntity getClient(String clientId) {
		// TODO Auto-generated method stub
		return null;
	}


	
}
