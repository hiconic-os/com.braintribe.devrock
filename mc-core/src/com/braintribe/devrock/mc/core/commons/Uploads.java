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
package com.braintribe.devrock.mc.core.commons;

import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;

import com.braintribe.exception.Exceptions;

public class Uploads {

	
	private void provokeAuthentication(CloseableHttpClient httpClient, HttpClientContext context, String target) {
		try {
			HttpHead httpSpearHeadDelete = new HttpHead(target );
			httpClient.execute( httpSpearHeadDelete, context);
		} catch (Exception e) {
			Exceptions.unchecked(e, "cannot provoke authentication", IllegalStateException::new);
		} 
		
	}
}
