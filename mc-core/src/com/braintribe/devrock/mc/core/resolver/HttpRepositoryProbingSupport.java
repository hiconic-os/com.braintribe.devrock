// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.devrock.mc.core.resolver;

import java.io.IOException;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.mc.api.repository.RepositoryProbingSupport;
import com.braintribe.devrock.model.mc.reason.configuration.HasRepository;
import com.braintribe.devrock.model.mc.reason.configuration.RepositoryAccessError;
import com.braintribe.devrock.model.mc.reason.configuration.RepositoryDoesNotSupportHttpMethod;
import com.braintribe.devrock.model.mc.reason.configuration.RepositoryErroneous;
import com.braintribe.devrock.model.mc.reason.configuration.RepositoryUnauthenticated;
import com.braintribe.devrock.model.mc.reason.configuration.RepositoryUnauthorized;
import com.braintribe.devrock.model.mc.reason.configuration.RepositoryUnavailable;
import com.braintribe.devrock.model.repository.RepositoryProbingMethod;
import com.braintribe.devrock.model.repository.RepositoryRestSupport;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.gm.reason.TemplateReasonBuilder;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.changes.RepositoryProbeStatus;
import com.braintribe.model.artifact.changes.RepositoryProbingResult;

/**
 * a {@link RepositoryProbingSupport} for http/https based repositories
 * @author pit / dirk
 *
 */
public class HttpRepositoryProbingSupport extends HttpRepositoryBase implements RepositoryProbingSupport {
	private static final Logger logger = Logger.getLogger(HttpRepositoryProbingSupport.class);
	private RepositoryProbingMethod probingMethod = RepositoryProbingMethod.head;
	
	@Configurable
	public void setProbingMethod(RepositoryProbingMethod probingMethod) {
		if (probingMethod != null) {
			this.probingMethod = probingMethod;
		}
	}

	@Override
	public RepositoryProbingResult probe() {
		int MAX_TRIES= 3;
		Exception exception = null;
		for (int i = 0; i < MAX_TRIES; i++) {
			if (i > 0) {
				try {
					Thread.sleep(i * 2000);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			try {
				try {
					URI.create(root);
				} catch (IllegalArgumentException e) {
					return RepositoryProbingResult.create(RepositoryProbeStatus.erroneous, Reasons.build(InvalidArgument.T).text("Repository probing path is not a valid url: " + root).toReason(), null, null);
				}
				CloseableHttpResponse response = null;
				switch (probingMethod) {
					case none:
						return RepositoryProbingResult.create(RepositoryProbeStatus.unprobed, null, null, null);
					case get:
						response = getResponse(new HttpGet( root));
						break;
					case options:
						response = getResponse(new HttpOptions( root));
						break;
					default:
					case head:
						response = getResponse(new HttpHead( root));
						break;			
				}
				
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
	
				Reason failure;
				RepositoryProbeStatus status;
				switch (statusCode) {
					case 200:
					case 204:
						status = RepositoryProbeStatus.available;
						failure = null;
						break;
					default:
						status = failureStatus(statusCode);
						String reasonPhrase = statusLine.getReasonPhrase();
						failure = failureBuilder(statusCode, repositoryId, probingMethod) //
								.cause(IoError
										.create(root + " responded with HTTP status " + statusCode + (reasonPhrase == null ? "" : ": " + reasonPhrase)))
								.toReason();
						break;
				}
				// TODO : check if this is ok to pre-initialize 
				// rest api
				RepositoryRestSupport restApi = identifyRestSupport(response);
				
				String changesUrl = null;
				Header rhHeader = response.getFirstHeader("X-Artifact-Repository-Changes-Url");
				if (rhHeader != null) {
					changesUrl = rhHeader.getValue();
				}
						
				return RepositoryProbingResult.create(status, failure, changesUrl, restApi);
							
			} catch (IOException e) {
				exception = e;
				logger.warn("failed try to probe repository " + repositoryId + ": " + getFirstMessage(e));
				// continue for retries
			}
		}
		
		String msg = "Error while probing repository " + repositoryId;
		logger.error(msg, exception);
		
		CommunicationError failure = CommunicationError.create(msg);
		failure.getReasons().add(InternalError.from(exception, getFirstMessage(exception)));

		return RepositoryProbingResult.create(RepositoryProbeStatus.erroneous, failure, null, null);
	}

	private String getFirstMessage(Exception exception) {
		Throwable curException = exception;
		
		while (curException != null) {
			String msg = curException.getMessage();
			
			if (msg != null)
				return msg;
			
			curException = curException.getCause();
		}
		
		return "Unkown error -> see logs";
	}

	private RepositoryProbeStatus failureStatus(int statusCode) {
		switch (statusCode) {
			case 401:
				return RepositoryProbeStatus.unauthenticated;
			case 403:
				return RepositoryProbeStatus.unauthorized;
			case 404:
				return RepositoryProbeStatus.unavailable;
			default:
				return RepositoryProbeStatus.erroneous;
		}
	}

	private TemplateReasonBuilder<? extends RepositoryAccessError> failureBuilder(int statusCode, String repository, RepositoryProbingMethod method) {
		switch (statusCode) {
		case 401:
			return TemplateReasons.build(RepositoryUnauthenticated.T).assign(HasRepository::setRepository, repository);
		case 403:
			return TemplateReasons.build(RepositoryUnauthorized.T).assign(HasRepository::setRepository, repository);
		case 404:
			return TemplateReasons.build(RepositoryUnavailable.T).assign(HasRepository::setRepository, repository);
		case 405:
			return TemplateReasons.build(RepositoryDoesNotSupportHttpMethod.T) //
					.assign(HasRepository::setRepository, repository) //
					.assign(RepositoryDoesNotSupportHttpMethod::setMethod, method.name().toUpperCase()); 
		default:
			return TemplateReasons.build(RepositoryErroneous.T).assign(HasRepository::setRepository, repository);
		}
	}

	/**
	 * tries to identify artifactory from the response
	 * @param response - the {@link CloseableHttpResponse} as sent by the repository's server
	 * @return - the {@link RepositoryRestSupport}, either evaluated or default
	 */
	private RepositoryRestSupport identifyRestSupport(CloseableHttpResponse response) {
		// older artifactory 
		Header serverHeader = response.getFirstHeader("Server");
		if (serverHeader != null) {
			String value = 	serverHeader.getValue();
			if (value != null) {
				if (value.startsWith( "Artifactory/")) {					
					return RepositoryRestSupport.artifactory;
				}
			}
		}
		// newer artifactory  
		Header jfrogHeader = response.getFirstHeader("X-JFrog-Version");
		if (jfrogHeader != null) {
			String value = 	jfrogHeader.getValue();
			if (value != null) {
				if (value.startsWith( "Artifactory/")) {
					return RepositoryRestSupport.artifactory;
				}
			}
		}
		// fallback : just existance of header with ID 
		Header idHeader = response.getFirstHeader(" X-Artifactory-Id");
		if (idHeader != null) {
			return RepositoryRestSupport.artifactory;
		}
		// fallback : just existance of header with node ID
		Header nodeHeader = response.getFirstHeader("X-Artifactory-Node-Id");
		if (nodeHeader != null) {
			return RepositoryRestSupport.artifactory;
		}
						
		return RepositoryRestSupport.none;
	}

	@Override
	public String repositoryId() {
		return repositoryId;
	}

	
}
