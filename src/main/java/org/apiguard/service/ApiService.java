package org.apiguard.service;

import org.apiguard.cassandra.entity.ApiEntity;
import org.apiguard.constants.AuthType;
import org.apiguard.service.exceptions.ApiException;

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

public interface ApiService<T> {

	public T addApi(String name, String reqUri, String downstreamUri) throws ApiException;

	public void deleteApi(String name) throws ApiException;

	public T updateApi(String name, String reqUri, String downstreamUri) throws ApiException;

	public T updateApiAuth(String reqUri, AuthType method, boolean enable) throws ApiException;

	public T getApiByReqUri(String reqUri);

	public T getApiByName(String name);
	
	public boolean apiRequestUriExists(String reqUri);

	public boolean apiNameExists(String name);
	
	public List<ApiEntity> getAllApis();
}
