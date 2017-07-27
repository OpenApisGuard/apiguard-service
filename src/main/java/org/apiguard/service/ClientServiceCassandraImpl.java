package org.apiguard.service;

import com.datastax.driver.core.utils.UUIDs;
import org.apiguard.cassandra.entity.ClientEntity;
import org.apiguard.cassandra.repo.ClientRepo;
import org.apiguard.service.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

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
