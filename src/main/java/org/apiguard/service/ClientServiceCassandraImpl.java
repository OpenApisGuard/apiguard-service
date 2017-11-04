package org.apiguard.service;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import org.apiguard.cassandra.entity.ClientEmailEntity;
import org.apiguard.cassandra.entity.ClientEntity;
import org.apiguard.cassandra.repo.ClientEmailRepo;
import org.apiguard.cassandra.repo.ClientRepo;
import org.apiguard.service.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

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

	@Autowired
	ClientEmailRepo clientEmailRepo;

	public ClientEntity addClient(String clientId, String email, String firstName, String lastName) throws ClientException {
		if (clientRepo.exists(clientId)) {
			throw new ClientException("Client id: " + clientId + " is already in use by another account.");
		}

		if (! StringUtils.isEmpty(email) && existsEmail(email)) {
			throw new ClientException("Email: " + email + " is already in use by another account.");
		}

		Date now = new Date();
		ClientEntity ce = new ClientEntity(UUIDs.timeBased().toString(), now, now, clientId, email, firstName, lastName);
		ClientEmailEntity cee = new ClientEmailEntity(UUIDs.timeBased().toString(), now, now, clientId, email);
		try {
			clientRepo.save(ce);
			if (! StringUtils.isEmpty(email)) {
				clientEmailRepo.save(cee);
			}

			return ce;
		}
		catch (Exception e) {
			// rollback
			clientRepo.delete(ce);

			if (! StringUtils.isEmpty(email)) {
				clientEmailRepo.delete(cee);
			}

			throw new ClientException("Internal error when saving client: " + clientId + ". " + e.getMessage());
		}
	}

	public ClientEntity getClient(String clientId) {
		return clientRepo.findOne(clientId);
	}

	public List<ClientEntity> getClients(int offset, int count) {
		/**
		 * Not supported below 2.0.0 GA
		 */
//		PageRequest pageRequest = new PageRequest(count, offset);
//        Page<ClientEntity> clients = clientRepo.findAll(pageRequest);
//        if (clients != null && clients.getSize() > 0) {
//            return clients.getContent();
//        }

		Iterable<ClientEntity> all = clientRepo.findAll();
		return Lists.newArrayList(all);
    }

	public boolean exists(String clientId) {
		return clientRepo.exists(clientId);
	}

	public boolean existsEmail(String email) {
		return clientEmailRepo.findOne(email) != null;
	}
}
