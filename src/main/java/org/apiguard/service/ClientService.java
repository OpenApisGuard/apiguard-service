package org.apiguard.service;

import org.apiguard.service.exceptions.ClientException;

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

public interface ClientService<T> {

	public T addClient(String clientId, String email, String firstName, String lastName) throws ClientException;

	public T getClient(String clientId);

	public List<T> getClients(int offset, int count);

	public boolean exists(String clientId);

	public boolean existsEmail(String email);

}
