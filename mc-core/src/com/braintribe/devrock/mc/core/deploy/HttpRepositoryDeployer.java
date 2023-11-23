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
package com.braintribe.devrock.mc.core.deploy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddress;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.core.http.OutputStreamerEntity;
import com.braintribe.devrock.model.mc.reason.PartUploadFailed;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.exception.CommunicationException;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.NullOutputStream;

public class HttpRepositoryDeployer extends AbstractArtifactDeployer<MavenHttpRepository> {
	private static Map<String, Pair<String, String>> hashAlgToHeaderKeyAndExtension = new LinkedHashMap<>();

	static {
		hashAlgToHeaderKeyAndExtension.put("SHA-1", Pair.of("X-Checksum-Sha1", "sha1"));
		hashAlgToHeaderKeyAndExtension.put("MD5", Pair.of("X-Checksum-Md5", "md5"));
		hashAlgToHeaderKeyAndExtension.put("SHA-256", Pair.of("X-Checksum-Sha256", "sha256"));
	}

	private static Logger log = Logger.getLogger(HttpRepositoryDeployer.class);
	private CloseableHttpClient httpClient;
	private static int MAX_RETRIES = 3;

	@Required
	@Configurable
	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	protected TransferContext openTransferContext() {
		return new HttpTransferContext();
	}

	private class HttpTransferContext implements TransferContext {
		private HttpClientContext context = HttpClientContext.create();
		private boolean authProvoked;

		@Override
		public ArtifactAddressBuilder newAddressBuilder() {
			return ArtifactAddressBuilder.build().root(repository.getUrl());
		}

		@Override
		public ArtifactAddress metaDataAddress(Artifact artifact, boolean versioned) {
			return versioned ? newAddressBuilder().versionedArtifact(artifact).metaData() : newAddressBuilder().artifact(artifact).metaData();
		}

		private void ensureAuthentication(ArtifactAddress artifactAddress) {
			if (authProvoked)
				return;
			
			synchronized (this) {
				if (authProvoked)
					return;

				String authProvokeUrl = ArtifactAddressBuilder.build().root(repository.getUrl())//
						.groupId(artifactAddress.getGroupId())//
						.artifactId(artifactAddress.getArtifactId())//
						.version(artifactAddress.getVersion()) //
						.toPath().toSlashPath();
	
				String host;
				try {
					host = new URI(repository.getUrl()).getHost();
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
				if (repository.getUser() != null && repository.getPassword() != null) {
					CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
					credentialsProvider.setCredentials(new AuthScope(host, AuthScope.ANY_PORT),
							new UsernamePasswordCredentials(repository.getUser(), repository.getPassword()));
					context.setCredentialsProvider(credentialsProvider);
				}
				
				boolean optimized = "true".equals(System.getenv("DEVROCK_HTTP_DIRECT_AUTH"));
				
				if (optimized) {
					RequestConfig config = RequestConfig.custom()
			                .setAuthenticationEnabled(true)
			                .build();
					context.setRequestConfig(config);
				}
				else {
					provokeAuthentication(authProvokeUrl);
				}
				
	

				authProvoked = true;
			}
		}

		private void provokeAuthentication(String target) {
			try {
				HttpHead httpSpearHeadDelete = new HttpHead(target);
				httpClient.execute(httpSpearHeadDelete, context);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public Maybe<InputStream> openInputStreamReasoned(ArtifactAddress address) {
			ensureAuthentication(address);
			String url = address.toPath().toSlashPath();
			HttpGet httpGet = new HttpGet(url);

			try {
				HttpResponse response = httpClient.execute(httpGet, context);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode >= 200 && statusCode < 300) {
					return Maybe.complete(response.getEntity().getContent());
				} else if (statusCode == 404) {
					return Reasons.build(NotFound.T).text("Resource at " + url + " not found").toMaybe();
				} else {
					return Reasons.build(CommunicationError.T).text("Opening resource from url " + url + " failed with HTTP status code").toMaybe();
				}
			} catch (IOException e) {
				return Reasons.build(IoError.T).text("Error while reading from [" + url + "]: " + e.getMessage()).toMaybe();
			}
		}
		
		@Override
		public Optional<InputStream> openInputStream(ArtifactAddress address) {
			ensureAuthentication(address);
			String url = address.toPath().toSlashPath();
			HttpGet httpGet = new HttpGet(url);

			try {
				HttpResponse response = httpClient.execute(httpGet, context);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode >= 200 && statusCode < 300) {
					return Optional.of(response.getEntity().getContent());
				} else if (statusCode == 404) {
					return Optional.empty();
				} else {
					throw new CommunicationException("Error [" + response.getStatusLine() + "] while reading from " + url);
				}
			} catch (IOException e) {
				throw new CommunicationException("Error while reading from " + url, e);
			}
		}

		@Override
		public Maybe<Resource> transfer(ArtifactAddress address, OutputStreamer outputStreamer) {
			ensureAuthentication(address);
			String url = address.toPath().toSlashPath();
			Map<String, String> hashes = generateHash(outputStreamer, hashAlgToHeaderKeyAndExtension.keySet());

			boolean targetExists = false;
			Pair<String, String> hashAlgAndValuePairOfExistingFile = null;

			// test if it's there already..
			HttpHead headRequest = new HttpHead(url);
			try {
				HttpResponse headResponse = httpClient.execute(headRequest, context);
				int headStatusCode = headResponse.getStatusLine().getStatusCode();
				if (headStatusCode == 200) {
					targetExists = true;
					try {
						// TODO: think about using all existing hashes to make it even more resilient
						hashAlgAndValuePairOfExistingFile = determineRequiredHashMatch(headResponse);
					} catch (IOException e) {
						String msg = "cannot extract hashes from header of existing [" + url + "]";
						log.error(msg, e);
					}
				}

			} catch (Exception e) {
				log.warn("cannot determine if target [" + url + "] exists. Assuming not to exist");
			}

			if (targetExists) {
				if (hashAlgAndValuePairOfExistingFile != null) {
					String sourceHash = hashes.get(hashAlgAndValuePairOfExistingFile.first);
					if (sourceHash != null) {
						if (sourceHash.equals(hashAlgAndValuePairOfExistingFile.second))
							return Maybe.complete(null);
					}
				}

				deleteTarget(httpClient, context, url);
			}

			Reason reason = putFile(url, outputStreamer, filePut -> {
				for (Map.Entry<String, Pair<String, String>> entry : hashAlgToHeaderKeyAndExtension.entrySet()) {
					filePut.setHeader(entry.getValue().first(), hashes.get(entry.getKey()));
				}
			});

			if (reason != null)
				return reason.asMaybe();

			for (Map.Entry<String, Pair<String, String>> entry : hashAlgToHeaderKeyAndExtension.entrySet()) {
				String algKey = entry.getKey();
				String hash = hashes.get(algKey);
				String extension = entry.getValue().second();
				String hashUrl = url + "." + extension;

				Reason hashUploadReason = putFile(hashUrl, out -> out.write(hash.getBytes("US-ASCII")), null);

				if (hashUploadReason != null)
					return hashUploadReason.asMaybe();
			}

			InputStreamProvider isp = () -> {
				HttpGet getRequest = new HttpGet(url);
				HttpResponse getResponse = httpClient.execute(getRequest, context);

				int headStatusCode = getResponse.getStatusLine().getStatusCode();
				if (headStatusCode >= 200 && headStatusCode < 300) {
					return getResponse.getEntity().getContent();
				} else {
					throw new IOException("Could not open url " + url + ": " + getResponse.getStatusLine().toString());
				}
			};

			return Maybe.complete(Resource.createTransient(isp));
		}

		private Reason putFile(String url, OutputStreamer streamer, Consumer<HttpPut> putConfigurer) {
			HttpPut filePut = new HttpPut(url);

			OutputStreamerEntity streamEntity = new OutputStreamerEntity(streamer);
			if (putConfigurer != null)
				putConfigurer.accept(filePut);

			filePut.setEntity(streamEntity);

			StatusLine httpStatusLine = null;

			for (int tries = 0; tries < MAX_RETRIES; tries++) {
				httpStatusLine = put(httpClient, filePut, context);
				int statusCode = httpStatusLine.getStatusCode();

				if (statusCode >= 200 && statusCode < 300) {
					return null;
				}
			}

			return Reasons.build(PartUploadFailed.T).text("Upload to [" + url + "] failed with: " + httpStatusLine).toReason();
		}

	}

	private HttpEntity deleteTarget(CloseableHttpClient httpclient, HttpClientContext context, String url) {
		HttpEntity entity = null;
		try {
			HttpDelete httpDelete = new HttpDelete(url);
			HttpResponse response = httpclient.execute(httpDelete, context);
			int statusCode = response.getStatusLine().getStatusCode();
			entity = response.getEntity();
			if (statusCode == 404) {
				if (log.isDebugEnabled()) {
					log.debug("target [" + url + "] doesn't exist");
				}
			} else if ((statusCode >= 200) && (statusCode < 300)) {
				if (log.isDebugEnabled()) {
					log.debug("target [" + url + "] successfully deleted");
				}
			} else {
				log.warn("cannot delete [" + url + "] as statuscode's [" + statusCode + "]");
			}

		} catch (Exception e) {
			log.warn("cannot delete [" + url + "]", e);
		} finally {
			try {
				if (entity != null)
					EntityUtils.consume(entity);
			} catch (IOException e) {
				String msg = "can't consume http entity as " + e;
				log.error(msg, e);
			}
		}
		return entity;
	}

	private void provokeAuthentication(CloseableHttpClient httpClient, HttpClientContext context, String target) {
		try {
			HttpHead httpSpearHeadDelete = new HttpHead(target);
			httpClient.execute(httpSpearHeadDelete, context);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

	}

	private Map<String, String> generateHash(OutputStreamer outputStreamer, Collection<String> types) {
		Map<String, String> result = new HashMap<>();

		List<Pair<MessageDigest, String>> digests = types.stream().map(t -> {
			try {
				return Pair.of(MessageDigest.getInstance(t), t);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("no digest found for [" + t + "]");
			}
		}).collect(Collectors.toList());

		OutputStream out = NullOutputStream.getInstance();

		for (Pair<MessageDigest, String> digestPair : digests) {
			MessageDigest digest = digestPair.first();
			out = new DigestOutputStream(out, digest);
		}

		try {
			outputStreamer.writeTo(out);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		for (int i = 0; i < digests.size(); i++) {
			Pair<MessageDigest, String> digestPair = digests.get(i);
			MessageDigest digest = digestPair.first();
			String algKey = digestPair.second();

			byte[] digested = digest.digest();
			result.put(algKey, StringTools.toHex(digested));
		}

		return result;
	}

	private static StatusLine put(CloseableHttpClient client, HttpEntityEnclosingRequestBase request, HttpContext httpContext) {
		try (CloseableHttpResponse httpResponse = client.execute(request, httpContext)) {
			StatusLine httpStatusLine = httpResponse.getStatusLine();
			return httpStatusLine;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * @param response
	 *            - the {@link HttpResponse} as returned by the server
	 * @return - a {@link Pair} consting of the hash type and hash value
	 * @throws IOException
	 */
	private Pair<String, String> determineRequiredHashMatch(HttpResponse response) throws IOException {
		// only check if relevant
		// search for the hashes in the headers, take the first one matching

		for (Entry<String, Pair<String, String>> entry : hashAlgToHeaderKeyAndExtension.entrySet()) {
			String hashHeaderName = entry.getValue().first();
			Header header = response.getFirstHeader(hashHeaderName);

			if (header != null) {
				return Pair.of(entry.getKey(), header.getValue());
			}
		}

		return null;
	}
}
