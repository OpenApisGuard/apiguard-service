package org.apiguard.service;

import org.apiguard.service.exceptions.ClientException;

public interface ClientService<T> {

	public T addClient(String clientId) throws ClientException;

	public T getClient(String clientId);

}
