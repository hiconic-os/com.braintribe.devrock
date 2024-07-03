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
