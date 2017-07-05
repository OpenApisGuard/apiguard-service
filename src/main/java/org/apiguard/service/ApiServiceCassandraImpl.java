package org.apiguard.service;

import org.apiguard.cassandra.entity.*;
import org.apiguard.cassandra.repo.*;
import org.apiguard.constants.AuthType;
import org.apiguard.service.exceptions.ApiException;
import org.apiguard.service.utils.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ApiServiceCassandraImpl implements ApiService<ApiEntity>{
	
	@Autowired
	ApiRepo apiRepo;

	@Autowired
	ApiNameRepo apiNameRepo;

	@Autowired
    ApiReqUriIndexRepo apiReqUriIndexRepo;

    @Autowired
	BasicAuthRepo basicAuthRepo;

    @Autowired
    ClientRepo clientRepo;

    @Autowired
    KeyAuthRepo keyAuthRepo;

    @Autowired
    SignatureAuthRepo signatureAuthRepo;

    @Autowired
	LdapAuthRepo ldapAuthRepo;

    @Autowired
	JwtAuthRepo jwtAuthRepo;

	public ApiEntity addApi(String name, String reqUri, String downstreamUri) throws ApiException {
		boolean reqUriExists = apiRequestUriExists(reqUri);
		if (reqUriExists || apiNameExists(name)) {
			String msg = "Api name: " + name + " already exists.";
			if (reqUriExists) {
				msg = "Request uri: " + reqUri + " already exists.";
			}
			throw new ApiException(msg);
		}

		if (!downstreamUri.startsWith("http://") && !downstreamUri.startsWith("https://") && !downstreamUri.startsWith("ftp://")) {
			throw new ApiException("Supported downstream protocols are: HTTP and FTP");
		}

        return updateApiEntity(name, reqUri, downstreamUri);
	}

    private ApiEntity updateApiEntity(String name, String reqUri, String downstreamUri) throws ApiException {
        String id = UUID.randomUUID().toString();
        Date now = new Date();
        ApiEntity apiDomain = new ApiEntity(id, now, now, name, reqUri, downstreamUri);
        ApiNameEntity apiNameDomain = new ApiNameEntity(id, now, now, name, reqUri);

        ApiReqUriIndexEntity reqUriInd = null;
        ApiReqUriIndexEntity apiReqRriInd = null;
        try {
            apiRepo.save(apiDomain);
            apiNameRepo.save(apiNameDomain);

            String prefix = reqUri.substring(0, reqUri.indexOf("/", 1) + 1);
            if (prefix == null) {
                throw new ApiException("Invalid request uri which has to start with / ");
            }

            reqUriInd = apiReqUriIndexRepo.findOne(prefix);
            List<String> matches = null;
            if (reqUriInd == null) {
                matches = new ArrayList<String>();
            }
            else {
                matches = reqUriInd.getMatches();
            }

            matches.add(reqUri);
            apiReqRriInd = new ApiReqUriIndexEntity(id, now, now, prefix, matches);
            apiReqUriIndexRepo.save(apiReqRriInd);
        }
        catch(ArrayIndexOutOfBoundsException e) {
            throw new ApiException("Invalid request uri which has to start with / ");
        }
        catch(Exception e) {
            apiRepo.delete(apiDomain);
            apiNameRepo.delete(apiNameDomain);

            if (reqUriInd == null && apiReqRriInd != null) {
                apiReqUriIndexRepo.delete(apiReqRriInd);
            }
            else { // rollback to previous
                apiReqUriIndexRepo.save(reqUriInd);
            }

            throw new ApiException("Internal error when saving api: " + e.getMessage());
        }
        return apiDomain;
    }

    public ApiEntity getApiByReqUri(String reqUri) {
		ApiEntity res = apiRepo.findOne(reqUri);

		if (res == null) {
			int ind = reqUri.indexOf("/", 1);
			if (ind > 0 && reqUri.startsWith("/")) {
				String prefix = reqUri.substring(0, ind+1);
                ApiReqUriIndexEntity reqUriInd = apiReqUriIndexRepo.findOne(prefix);

                if (reqUriInd == null) {
                    return res;
                }

                // do regex matching
                for(String cur : reqUriInd.getMatches()) {
                    Pattern pattern = Pattern.compile(cur);
                    Matcher matcher = pattern.matcher(reqUri);
                    if(matcher.matches()) {
                        return apiRepo.findOne(cur);
                    }
                }
			}
			else {
				return res;
			}
		}

		return res;
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
        boolean reqUriExists = apiRequestUriExists(reqUri);
        if (!reqUriExists || !apiNameExists(name)) {
            String msg = "Api name and/or req uri do not exist.";
            throw new ApiException(msg);
        }

        return updateApiEntity(name, reqUri, downstreamUri);
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

	public void deleteApi(String name) throws ApiException {
		ApiNameEntity apiNameDomain = getApiNameByName(name);
		if (apiNameDomain == null) {
			throw new ApiException("Api name: " + name + " does not exist.");
		}
		
		try {
            String reqUri = apiNameDomain.getReqUri();
            ApiEntity apiDomain = getApiByReqUri(reqUri);

            // remove all auths
            List<BasicAuthEntity> basicAuths = basicAuthRepo.findByReqUri(reqUri);
            if (basicAuths != null && !basicAuths.isEmpty()) {
                basicAuthRepo.delete(basicAuths);
            }

            List<KeyAuthEntity> keyAuths = keyAuthRepo.findByReqUri(reqUri);
            if (keyAuths != null && !keyAuths.isEmpty()) {
                keyAuthRepo.delete(keyAuths);
            }

            List<SignatureAuthEntity> sigAuths = signatureAuthRepo.findByReqUri(reqUri);
            if (sigAuths != null && !sigAuths.isEmpty()) {
                signatureAuthRepo.delete(sigAuths);
            }

			List<LdapAuthEntity> ldapAuths = ldapAuthRepo.findByReqUri(reqUri);
            if (ldapAuths != null && !ldapAuths.isEmpty()) {
            	ldapAuthRepo.delete(ldapAuths);
			}

			List<JwtAuthEntity> jwtAuths = jwtAuthRepo.findByReqUri(reqUri);
            if (jwtAuths != null && !jwtAuths.isEmpty()) {
				jwtAuthRepo.delete(jwtAuths);
			}

			// remove from index
            String prefix = reqUri.substring(0, reqUri.indexOf("/", 1) + 1);
            ApiReqUriIndexEntity reqUriInd = apiReqUriIndexRepo.findOne(prefix);
            List<String> matches = reqUriInd.getMatches();
            matches.remove(reqUri);
            if (matches.isEmpty()) {
                apiReqUriIndexRepo.delete(prefix);
            }
            else {
                apiReqUriIndexRepo.save(reqUriInd);
            }

            // remove actual api
            apiRepo.delete(apiDomain);
            apiNameRepo.delete(apiNameDomain);
        }
		catch(Exception e) {
			throw new ApiException("Internal error when deleting api: " + e.getMessage());
		}
	}
}