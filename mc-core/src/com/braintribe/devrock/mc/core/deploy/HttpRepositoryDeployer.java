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
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
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
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.core.http.OutputStreamerEntity;
import com.braintribe.devrock.model.mc.reason.PartUploadFailed;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.exception.CommunicationException;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.encryption.Md5Tools;
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
		private StringBuilder protocol = new StringBuilder();
		private int protocolLevel = 0;

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
		
		private void appendProtocolLevel(int level) {
			synchronized(protocol) {
				protocolLevel = Math.max(protocolLevel, level);
			}
		}
		
		private void appendToProtocol(String line, int level) {
			synchronized(protocol) {
				protocolLevel = Math.max(protocolLevel, level);
				protocol.append(line);
				protocol.append("\n");
			}
		}
		
		@Override
		public Maybe<Resource> transfer(ArtifactAddress address, OutputStreamer outputStreamer, boolean hashWorthy) {
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
			
			if (reason != null) {
				return reason.asMaybe();
			}

			if (hashWorthy) {
				for (Map.Entry<String, Pair<String, String>> entry : hashAlgToHeaderKeyAndExtension.entrySet()) {
					String algKey = entry.getKey();
					String hash = hashes.get(algKey);
					String extension = entry.getValue().second();
					String hashUrl = url + "." + extension;
	
					Reason hashUploadReason = putFile(hashUrl, out -> out.write(hash.getBytes("US-ASCII")), null);
	
					if (hashUploadReason != null)
						return hashUploadReason.asMaybe();
				}
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
			Map<Object, Reason> errors = new LinkedHashMap<>();

			for (int tries = 0; tries < MAX_RETRIES; tries++) {
				appendToProtocol("trying upload to " + url, 0);

				StatusLine httpStatusLine = null;
				String message = null;
				String md5 = null;

				try {
					HttpPut filePut = new HttpPut(url);

					OutputStreamerEntity streamEntity = new OutputStreamerEntity(streamer, true);
					if (putConfigurer != null)
						putConfigurer.accept(filePut);

					filePut.setEntity(streamEntity);

					Pair<StatusLine, String> result = put(httpClient, filePut, context);
					
					md5 = streamEntity.getMd5();
					httpStatusLine = result.first();
					message = result.second();
				}
				catch (RuntimeException e) {
					appendToProtocol("failed with exception when uploading (try " + (tries + 1) + "/"+ MAX_RETRIES +") to " + url + ": " + e.getMessage(), 1);
					
					String msg = getFirstMessage(e);
					
					errors.put(e.getClass(), InternalError.from(e, msg));

					continue;
				}
				 
				int statusCode = httpStatusLine.getStatusCode();

				if (statusCode >= 200 && statusCode < 300) {
					appendToProtocol("successfully uploaded to " + url, 0);
					return null;
				}
				else {
					Reason error = Reasons.build(CommunicationError.T) //
							.text("Unexpected HTTP status code: " + // 
									httpStatusLine.getStatusCode() + " " + httpStatusLine.getReasonPhrase() + // 
									", message: " + message).toReason();

					appendToProtocol("failed when trying upload to " + url + " with reason: " + error.stringify(), 1);

					if (statusCode >= 500 && statusCode < 600) {
						// this extra check
						if (exists(url, md5)) {
							appendToProtocol("successfully uploaded to " + url + ". Note: Actually received a 500 status code but still succesfully checked existence.", 0);
							return null;
						}
						
						errors.put(statusCode, error);
						continue;
					}
					
					if (statusCode == 409) {
						if (exists(url, md5)) {
							appendToProtocol("successfully uploaded to " + url + ". Note: Actually received a 409 status code but still succesfully checked existence.", 0);
							return null;
						}
					}
					
					appendProtocolLevel(2);
					
					return Reasons.build(PartUploadFailed.T) //
								.text("Upload to [" + url + "] failed after " + (tries + 1) + " tries.") //
								.cause(error).toReason();
				}
			}
			
			appendToProtocol("failed after " + MAX_RETRIES + " tries when uploading to " + url, 2);
			
			if (errors.size() == 1)
				return errors.values().iterator().next();
			
			return Reasons.build(PartUploadFailed.T) //
					.text("Upload to [" + url + "] failed after " + MAX_RETRIES + " tries.") //
					.causes(errors.values()).toReason();
		}
		
		private boolean exists(String url, String expectedMd5) {
			HttpGet httpGet = new HttpGet(url);

			try {
				CloseableHttpResponse response = httpClient.execute(httpGet, context);
				int statusCode = response.getStatusLine().getStatusCode();
				
				if (statusCode == 404) {
					appendToProtocol("existence check for " + url + " returned 404", 0);
					return false;
				}
				
				if (!(statusCode >= 200 && statusCode < 300)) {
					String message = readBody(response);
					appendToProtocol("existence check for " + url + " failed with " + response.getStatusLine() + ", message: " + message, 1);
					return false;
				}
				
				MessageDigest digest = MessageDigest.getInstance("MD5");
				
				try (InputStream in = response.getEntity().getContent(); OutputStream out = new DigestOutputStream(NullOutputStream.getInstance(), digest)) {
					IOTools.transferBytes(in, out);
				}
				
				String actualMd5 = StringTools.toHex(digest.digest());
				
				boolean hashMatch = expectedMd5.equals(actualMd5);
				
				if (!hashMatch)
					appendToProtocol("existence check for " + url + " showed different hashes", 1);
				
				return hashMatch;
				
			} catch (Exception e) {
				appendToProtocol("existence check for " + url + " failed with an exception: " + e.getClass().getSimpleName() + ": " + getFirstMessage(e), 1);
				return false;
			}
		}

		/* copied from com.braintribe.devrock.mc.core.resolver.HttpRepositoryProbingSupport in mc-core for later consolidation into exception tools */
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

		@Override
		public Maybe<Void> onUploadComplete(Artifact artifact) {
			// write publish-complete part to have a marker which shows that the publish was able to write all parts 
			ArtifactAddress completionPart = newAddressBuilder().versionedArtifact(artifact).part(PartIdentifications.publishComplete);
			String date = new Date().toInstant().toString();
			return transfer(completionPart, o -> writeCompletionPart(o, date), false) //
					.flatMap(r -> Maybe.complete(null));
		}
		
		private void writeCompletionPart(OutputStream out, String date) throws IOException {
			try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
				writer.write(date);
			}
		}
	
		@Override
		public void close() {
			switch (protocolLevel) {
			case 1: log.warn(protocol.toString()); break;
			case 2: log.error(protocol.toString()); break;
			default:
				break;
			}
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
			} else if (statusCode == 405) {
				// 405 Invalid Method
				// It seems deleting is not supported. A real case of that kind is given in github packages maven repo implementation
				// We ignore as it is still valid to overwrite certain files (e.g. maven-metadata.xml) otherwise it will fail 
				// with the actual overwriting upload
			} else if ((statusCode >= 200) && (statusCode < 300)) {
				if (log.isDebugEnabled()) {
					log.debug("target [" + url + "] successfully deleted");
				}
			}
			else {
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

	private static Pair<StatusLine, String> put(CloseableHttpClient client, HttpEntityEnclosingRequestBase request, HttpContext httpContext) {
		try (CloseableHttpResponse httpResponse = client.execute(request, httpContext)) {
			StatusLine httpStatusLine = httpResponse.getStatusLine();
			
			String message = readBody(httpResponse);
			return Pair.of(httpStatusLine, message);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private static String readBody(CloseableHttpResponse httpResponse) throws IOException {
		HttpEntity entity = httpResponse.getEntity();
		Header contentEncoding = entity.getContentEncoding();
		
		String encoding = contentEncoding != null? contentEncoding.getValue(): "UTF-8";
		
		try (InputStream in = entity.getContent()) {
			String message = IOTools.slurp(in, encoding);
			return message;
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
