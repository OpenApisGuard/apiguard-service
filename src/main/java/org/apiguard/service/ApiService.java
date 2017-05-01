package org.apiguard.service;

import org.apiguard.cassandra.entity.ApiEntity;
import org.apiguard.constants.AuthType;
import org.apiguard.service.exceptions.ApiException;

import java.util.List;

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
