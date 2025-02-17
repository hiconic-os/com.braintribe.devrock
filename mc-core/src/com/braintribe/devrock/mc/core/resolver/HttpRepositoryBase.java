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
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.model.mc.reason.UnknownRepositoryHost;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gm.model.security.reason.Forbidden;
import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

/**
 * common base for {@link HttpRepositoryArtifactDataResolver} and {@link HttpRepositoryProbingSupport}
 * 
 * @author pit / dirk
 *
 */
public class HttpRepositoryBase {
	private final Logger logger = Logger.getLogger(HttpRepositoryBase.class);
	protected String root;
	protected String userName;
	protected String password;
	protected CloseableHttpClient httpClient;
	protected String repositoryId = "unknown";

	@Configurable
	@Required
	public void setRoot(String root) {
		this.root = root;
	}
	@Configurable
	public void setUserName(String userName) {
		this.userName = userName;
	}
	@Configurable
	public void setPassword(String password) {
		this.password = password;
	}
	@Configurable
	@Required
	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Configurable
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	protected CloseableHttpResponse getResponse(String url, boolean headOnly) throws IOException {
		return getResponse(headOnly ? new HttpHead(url) : new HttpGet(url));
	}
	
	protected CloseableHttpResponse getResponse(HttpRequestBase requestBase) throws IOException {

	    if (userName != null && password != null) {
	        String auth = userName + ":" + password;
	        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
	        requestBase.setHeader(new BasicHeader("Authorization", "Basic " + encodedAuth));
	    }

	    return httpClient.execute(requestBase, HttpClientContext.create());
	}

	protected static <T> Maybe<T> statusProblemMaybe(HttpUriRequest request, CloseableHttpResponse response) {
		return statusProblemMaybe(request.getURI().toString(), response);
		
	}
	
	protected static <T> Maybe<T> statusProblemMaybe(String url, CloseableHttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();

		switch (statusCode) {
			case 404:
				return Reasons.build(NotFound.T).text("Resource not found  at url " + url).toMaybe();
			case 403:
				return Reasons.build(Forbidden.T).text("Forbidden access to resource at url " + url).toMaybe();
			case 401:
				return Reasons.build(AuthenticationFailure.T).text("Unauthenticated access to resource at url " + url).toMaybe();
			default:
				return Reasons.build(CommunicationError.T).text("Error [" + response.getStatusLine() + "] accessing resource at url " + url)
						.toMaybe();
		}
	}
	
	protected Maybe<String> readText(String url, String encoding) throws IOException {
		var maybe = openInputStream(url);
		
		if (maybe.isUnsatisfied())
			return maybe.whyUnsatisfied().asMaybe();
		
		try (InputStream in = maybe.get()) {
			return Maybe.complete(IOTools.slurp(in, encoding)); 
		}
	}
	
	protected Maybe<InputStream> openInputStream(String url) throws IOException {
		var responseMaybe = getResponseReasoned(url);
		
		if (responseMaybe.isUnsatisfied()) {
			return responseMaybe.whyUnsatisfied().asMaybe();
		}
		
		var response = responseMaybe.get();
		
		if (logger.isDebugEnabled()) {
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			String phrase = statusLine.getReasonPhrase();
			phrase = phrase != null? " (" + phrase + ")": "";
			logger.debug("received status " + statusCode + phrase + " from url " + url);
		}
		
		HttpEntity entity = response.getEntity();
		
		return Maybe.complete(entity.getContent());
	}
	
	protected Maybe<CloseableHttpResponse> getResponseReasoned(String url) {
		try {
			var response = getResponse(url);
			var statusLine = response.getStatusLine();
			var statusCode = statusLine.getStatusCode();
			
			if (statusCode >= 200 && statusCode < 300)
				return Maybe.complete(response);
			
			return statusProblemMaybe(url, response);
		}
		catch (UnknownHostException e) {
			logger.debug("Unknown host: " + url);
			return Reasons.build(UnknownRepositoryHost.T).text("Unknown host: " + url) //
				.toMaybe();
		}
		catch (Exception e) {
			String tracebackId = UUID.randomUUID().toString();
			String msg = "Could not open input stream for: " + url + " (tracebackId=" + tracebackId + ")";
			logger.error(msg, e);
			
			return Reasons.build(CommunicationError.T).text(msg).toMaybe();
		}
	}

	protected CloseableHttpResponse getResponse(String url) throws IOException {
		int maxRetries = 3;
		int retry = 0;
		while (true) {
			try {
				return getResponse(url, false);
			}
			catch (UnknownHostException e) {
				throw e;
			}
			catch (IOException e) {
				if ((++retry) > maxRetries)
					throw e;

				logger.warn("failed try " + retry + " of " + maxRetries + " to open a http request to: " + url, e);
			}
		}
	}

	protected CloseableHttpResponse getResponseWithRetry(HttpGet get) throws IOException {
		int maxRetries = 3;
		int retry = 0;
		while (true) {
			try {
				return getResponse(get);
			} 
			catch (UnknownHostException e) {
				throw e;
			}
			catch (IOException e) {
				if ((++retry) > maxRetries)
					throw e;

				logger.warn("failed try " + retry + " of " + maxRetries + " to open a http request to: " + get.getURI(), e);
			}
		}
	}
}
