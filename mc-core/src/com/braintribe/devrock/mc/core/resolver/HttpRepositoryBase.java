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
import java.net.UnknownHostException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;

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

		HttpClientContext context = HttpClientContext.create();

		if (userName != null && password != null) {
			String host = requestBase.getURI().getHost();
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(new AuthScope(host, AuthScope.ANY_PORT), new UsernamePasswordCredentials(userName, password));
			context.setCredentialsProvider(credentialsProvider);
		}
		CloseableHttpResponse response = httpClient.execute(requestBase, context);
		return response;
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
				if ((retry++) > maxRetries)
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
			} catch (IOException e) {
				if ((retry++) > maxRetries)
					throw e;

				logger.warn("failed try " + retry + " of " + maxRetries + " to open a http request to: " + get.getURI(), e);
			}
		}
	}
}
