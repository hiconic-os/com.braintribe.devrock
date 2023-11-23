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
package com.braintribe.devrock.test.repolet.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.string.DateCodec;
import com.braintribe.devrock.model.artifactory.FolderInfo;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.utils.lcd.LazyInitialized;

public abstract class AbstractLauncherTest implements LauncherTrait {

	protected File res = new File("res");
	protected File root = getRoot();
	protected static final String RAVENHURST_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	protected static final String RAVENHURST_PARAMETER = "?timestamp=";
	protected static final String testDate1AsString = "2020-02-25T10:10:33.836+0200";
	protected static final String testDate2AsString = "2020-02-27T10:10:33.836+0200";
	protected static final String testDate3AsString = "2020-02-29T10:10:33.836+0200";

	protected LazyInitialized<CloseableHttpClient> httpClient = new LazyInitialized<>(this::client);
	protected final static JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
	protected final static GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults().setInferredRootType( FolderInfo.T).build();


	protected DateCodec dateCodec = new DateCodec(RAVENHURST_DATE_FORMAT);

	protected Launcher launcher;

	protected abstract File getRoot();

	protected void runBeforeBefore() {
	}
	protected void runBeforeAfter() {
	}
	protected void runAfterBefore() {
	}
	protected void runAfterAfter() {
	}

	@Before
	public void runBefore() {
		runBeforeBefore();
		runBefore(launcher);
		runBeforeAfter();
	}

	@After
	public void runAfter() {
		runAfterBefore();
		runAfter(launcher);
		runAfterAfter();
	}

	protected CloseableHttpClient client() {
		DefaultHttpClientProvider bean = new DefaultHttpClientProvider();
		bean.setSocketTimeout(60_000);
		try {
			CloseableHttpClient httpClient = bean.provideHttpClient();
			return httpClient;
		} catch (Exception e) {
			throw new IllegalStateException("", e);
		}
	}

	protected CloseableHttpResponse getHeadResponse(String url) throws IOException {
		HttpRequestBase requestBase = new HttpHead(url);
		HttpClientContext context = HttpClientContext.create();
		CloseableHttpResponse response = httpClient.get().execute(requestBase, context);
		return response;
	}

	protected CloseableHttpResponse getOptionsResponse(String url) throws IOException {
		HttpRequestBase requestBase = new HttpOptions(url);
		HttpClientContext context = HttpClientContext.create();
		CloseableHttpResponse response = httpClient.get().execute(requestBase, context);
		return response;
	}

	protected CloseableHttpResponse getGetResponse(String url) throws IOException {
		HttpRequestBase requestBase = new HttpGet(url);
		HttpClientContext context = HttpClientContext.create();
		CloseableHttpResponse response = httpClient.get().execute(requestBase, context);
		return response;
	}

	/**
	 * @param expectedValues
	 *            - a {@link List} of expected {@link String}
	 * @param foundValues
	 *            - a {@link List} of found {@link String}
	 */
	protected void validate(List<String> expectedValues, List<String> foundValues) {
		List<String> matching = new ArrayList<String>();
		List<String> missing = new ArrayList<String>();
		for (String expected : expectedValues) {
			if (foundValues.contains(expected)) {
				matching.add(expected);
			} else {
				missing.add(expected);
			}
		}
		Assert.assertTrue("missing values [" + missing.stream().collect(Collectors.joining(",")) + "]", missing.size() == 0);
		List<String> excess = new ArrayList<>(foundValues);
		excess.removeAll(expectedValues);
		Assert.assertTrue("excess values [" + excess.stream().collect(Collectors.joining(",")) + "]", excess.size() == 0);

	}
}
