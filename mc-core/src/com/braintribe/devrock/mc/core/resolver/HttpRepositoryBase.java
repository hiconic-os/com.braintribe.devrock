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
 * @author pit / dirk
 *
 */
public class HttpRepositoryBase {
	private Logger logger = Logger.getLogger(HttpRepositoryBase.class);
	protected String root;
	protected String userName;
	protected String password;
	protected CloseableHttpClient httpClient;
	protected String repositoryId = "unknown";
	
	@Configurable @Required
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
	@Configurable @Required
	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	@Configurable
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}
	
	protected CloseableHttpResponse getResponse( String url, boolean headOnly) throws IOException {		
		return getResponse( headOnly ? new HttpHead( url) : new HttpGet(url));
	}
	
	/**
	 * @param url - the URL to use for the HttpGet
	 * @return - a fresh {@link CloseableHttpResponse}
	 * @throws IOException
	 */
	protected CloseableHttpResponse getResponse( HttpRequestBase requestBase) throws IOException{
		
		HttpClientContext context = HttpClientContext.create();

		if (userName != null && password != null) {
			String host = requestBase.getURI().getHost();
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials( new AuthScope( host, AuthScope.ANY_PORT), new UsernamePasswordCredentials( userName, password));	
			context.setCredentialsProvider( credentialsProvider);
		}						
		CloseableHttpResponse response = httpClient.execute( requestBase, context);
		return response;
	}
	
	
	protected CloseableHttpResponse getResponse( String url) throws IOException{
		int maxRetries = 3;
		int retry = 0;
		while (true) {
			try {
				return getResponse(url, false);
			}
			catch (IOException e) {
				if ((retry++) > maxRetries)
					throw e;
				
				logger.warn("failed try " + retry + " of " + maxRetries + " to open a http request to: " + url, e);
			}
		}
	}
}
