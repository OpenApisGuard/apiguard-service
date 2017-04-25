package org.apiguard.service;

import com.datastax.driver.core.utils.UUIDs;
import org.apiguard.cassandra.entity.ApiEntity;
import org.apiguard.cassandra.entity.ApiNameEntity;
import org.apiguard.cassandra.repo.ApiNameRepo;
import org.apiguard.cassandra.repo.ApiRepo;
import org.apiguard.constants.AuthType;
import org.apiguard.service.exceptions.ApiException;
import org.apiguard.service.utils.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class ApiServiceCassandraImpl implements ApiService<ApiEntity>{
	
	@Autowired
	ApiRepo apiRepo;

	@Autowired
	ApiNameRepo apiNameRepo;

	public ApiEntity addApi(String name, String reqUri, String downstreamUri) throws ApiException {
		boolean reqUriExists = apiRequestUriExists(reqUri);
		if (reqUriExists || apiNameExists(name)) {
			String msg = "Api name: " + name + " already exists.";
			if (reqUriExists) {
				msg = "Request uri: " + reqUri + " already exists.";
			}
			throw new ApiException(msg);
		}
		
		UUID id = UUIDs.timeBased();
		Date now = new Date();
		ApiEntity apiDomain = new ApiEntity(id, now, now, name, reqUri, downstreamUri);
		ApiNameEntity apiNameDomain = new ApiNameEntity(id, now, now, name, reqUri);
		
		try {
			apiRepo.save(apiDomain);
			apiNameRepo.save(apiNameDomain);
		}
		catch(Exception e) {
			apiRepo.delete(apiDomain);
			apiNameRepo.delete(apiNameDomain);
			throw new ApiException("Internal error when saving api: " + e.getMessage());
		}
		return apiDomain;
	}
	
	public ApiEntity getApiByReqUri(String reqUri) {
		return apiRepo.findOne(reqUri);
	}

	public ApiEntity getApiByName(String name) {
		ApiEntity res = null;
		ApiNameEntity apiName = apiNameRepo.findOne(name);
		if (apiName != null) {
			res = apiRepo.findOne(apiName.getReqUri());
		}
		
		return res;
	}

	public ApiNameEntity getApiNameByName(String name) {
		return apiNameRepo.findOne(name);
	}

	public List<ApiEntity> getAllApis() {
		Iterable<ApiEntity> apis = apiRepo.findAll();
		return ListUtils.getList(apis);
	}

	public ApiEntity updateApi(String name, String reqUri, String downstreamUri) throws ApiException {
		// TODO Auto-generated method stub
		return null;
	}

	public ApiEntity updateApiAuth(String reqUri, AuthType method, boolean enable) throws ApiException {
		ApiEntity api = getApiByReqUri(reqUri);
		if (api == null) {
			throw new ApiException("Unable to add auth to API because API is not valid.");
		}
		
		switch (method) {
		case BASIC:
			api.setBasicAuth(enable);
			break;
		case HMAC:
			api.setHmacAuth(enable);
			break;
		case JWT:
			api.setJwtAuth(enable);
			break;
		case KEY:
			api.setKeyAuth(enable);
			break;
		case LDAP:
			api.setLdapAuth(enable);
			break;
		case OAUTH2:
			api.setOAuth2Auth(enable);
			break;
		case SIGNATURE:
			api.setSignatureAuth(enable);
			break;
		case DIGITAL_SIGNATURE:
			api.setDigitalAuth(enable);
		}
		
		api.setAuthRequired(api.isBasicAuth() || api.isHmacAuth() || api.isJwtAuth() || api.isKeyAuth() || api.isLdapAuth()
				|| api.isOAuth2Auth() || api.isDigitalAuth() || api.isSignatureAuth());
		api.setLastUpdateDate(new Date());
		ApiEntity res = apiRepo.save(api);
		return res;
	}

	public boolean apiRequestUriExists(String reqUri) {
		return apiRepo.exists(reqUri);
	}

	public boolean apiNameExists(String name) {
		return apiNameRepo.exists(name);
	}

	public boolean deleteApi(String name) throws ApiException {
		ApiNameEntity apiNameDomain = getApiNameByName(name);
		if (apiNameDomain == null) {
			throw new ApiException("Api name: " + name + " does not exist.");
		}
		
		try {
			ApiEntity apiDomain = getApiByReqUri(apiNameDomain.getReqUri());
			apiRepo.delete(apiDomain);
			apiNameRepo.delete(apiNameDomain);
		}
		catch(Exception e) {
			throw new ApiException("Internal error when deleting api: " + e.getMessage());
		}
		return false;
	}
}