// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.changes.RepositoryProbeStatus;
import com.braintribe.model.artifact.changes.RepositoryProbingResult;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * a {@link RepositoryProbingSupport} for http/https based repositories
 * @author pit / dirk
 *
 */
public class HttpRepositoryProbingSupport extends HttpRepositoryBase implements RepositoryProbingSupport {
	
	private RepositoryProbingMethod probingMethod = RepositoryProbingMethod.head;
	
	@Configurable
	public void setProbingMethod(RepositoryProbingMethod probingMethod) {
		if (probingMethod != null) {
			this.probingMethod = probingMethod;
		}
	}

	@Override
	public RepositoryProbingResult probe() {		
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
					failure = TemplateReasons.build(failureType(statusCode)) //
							.assign(HasRepository::setRepository, repositoryId) //
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
			CommunicationError failure = CommunicationError.create("Error while probing repository " + repositoryId);
			failure.getReasons().add(InternalError.from(e));

			return RepositoryProbingResult.create(RepositoryProbeStatus.erroneous, failure, null, null);
		}
		
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

	private EntityType<? extends RepositoryAccessError> failureType(int statusCode) {
		switch (statusCode) {
			case 401:
				return RepositoryUnauthenticated.T;
			case 403:
				return RepositoryUnauthorized.T;
			case 404:
				return RepositoryUnavailable.T;
			default:
				return RepositoryErroneous.T;
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
