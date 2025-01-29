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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.api.resolver.ChecksumPolicy;
import com.braintribe.devrock.mc.core.commons.HtmlContentParser;
import com.braintribe.devrock.mc.core.commons.PartReflectionCommons;
import com.braintribe.devrock.model.mc.reason.MismatchingHash;
import com.braintribe.devrock.model.mc.reason.UnknownRepositoryHost;
import com.braintribe.exception.CommunicationException;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gm.model.security.reason.Forbidden;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.version.Version;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.BasicDelegateInputStream;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/*
 * requires 
 * 	repository information, base URL, credentials
 * 	settings cfg, handling CRC
 *  http handler 
 */

/**
 * a HTTP based {@link ArtifactDataResolver}, for standard remote repos for instance
 * 
 * @author pit
 *
 */
public class HttpRepositoryArtifactDataResolver extends HttpRepositoryBase implements ArtifactVersionsResolverTrait, ArtifactDataResolver {
	private static Logger log = Logger.getLogger(HttpRepositoryArtifactDataResolver.class);

	private ChecksumPolicy checksumPolicy = ChecksumPolicy.fail;
	private boolean ignoreHashHeaders;

	private boolean activateGithubHandling = false;

	private final Map<String, Pair<String, String>> hashAlgToHeaderKeyAndExtension = new LinkedHashMap<>();

	public HttpRepositoryArtifactDataResolver() {
		hashAlgToHeaderKeyAndExtension.put("MD5", Pair.of("X-Checksum-Md5", "md5"));
		hashAlgToHeaderKeyAndExtension.put("SHA-1", Pair.of("X-Checksum-Sha1", "Sha1"));
		hashAlgToHeaderKeyAndExtension.put("SHA-256", Pair.of("X-Checksum-Sha256", "Sha256"));

		String githubSuport = System.getenv("MC_GITHUB_SUPPORT");
		if (githubSuport != null) {
			activateGithubHandling = Boolean.valueOf(githubSuport).booleanValue();
		}
	}

	@Configurable
	public void setChecksumPolicy(ChecksumPolicy checksumPolicy) {
		this.checksumPolicy = checksumPolicy;
	}

	@Configurable
	public void setIgnoreHashHeaders(boolean ignoreHashHeaders) {
		this.ignoreHashHeaders = ignoreHashHeaders;
	}

	/**
	 * @return - either {@link Optional} with {@link BasicArtifactDataResolution} or empty {@link Optional}
	 */
	protected Maybe<ArtifactDataResolution> resolve(ArtifactAddressBuilder builder) {
		HttpArtifactDataResolution res = new HttpArtifactDataResolution(builder);
		return Maybe.complete(res);
	}

	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(ArtifactIdentification identification) {
		return resolve(ArtifactAddressBuilder.build().root(root).artifact(identification).metaData());
	}

	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(CompiledArtifactIdentification identification) {
		return resolve(ArtifactAddressBuilder.build().root(root).compiledArtifact(identification).metaData());
	}

	@Override
	public Maybe<ArtifactDataResolution> resolvePart(CompiledArtifactIdentification identification, PartIdentification partIdentification,
			Version partVersionOverrride) {
		if (partVersionOverrride == null)
			return resolve(ArtifactAddressBuilder.build().root(root).compiledArtifact(identification).part(partIdentification));
		else
			return resolve(ArtifactAddressBuilder.build().root(root).compiledArtifact(identification).part(partIdentification, partVersionOverrride));
	}

	/**
	 * internal implementation of {@link ArtifactDataResolution}
	 */
	private class HttpArtifactDataResolution implements ArtifactDataResolution {
		private final ArtifactAddressBuilder builder;
		private String url;
		private static Pattern githubPattern = Pattern.compile("https:\\/\\/maven.pkg.github.com\\/([^\\/]+)\\/([^\\/]+).*");
		// using {0,4} instead of * to avoid potential stack overflow warning (and because there is no need to support more version levels)
		private static Pattern versionPattern = Pattern.compile("\\d+(\\.\\d+){0,4}");
		
		public HttpArtifactDataResolution(ArtifactAddressBuilder builder) {
			this.builder = builder;
			this.url = builder.toPath().toSlashPath();
		}

		@Override
		public Resource getResource() {

			Matcher matcher = githubPattern.matcher(this.url);
			if (activateGithubHandling && matcher.matches()) {
				String org = matcher.group(1);
				String repo = matcher.group(2);
				log.debug(() -> "Identified a GitHub repository URL: " + this.url + ", Organization: " + org + ", Repository: " + repo);
				
				String fileName = this.builder.getFileName(); // file name could be the version, if URL ends with e.g. .../1.2.3
				if (fileName != null && versionPattern.matcher(fileName).matches()) {
					log.debug(() -> "URL is an artifact version URL.");
				
					String token = System.getenv("GITHUB_READ_PACKAGES_TOKEN");
					if (token == null) {
						token = System.getProperty("GITHUB_READ_PACKAGES_TOKEN");
					}
					if (token != null) {
						log.debug(() -> "Found GITHUB_READ_PACKAGES_TOKEN value.");
						// https://api.github.com/orgs/hiconic-os/packages/maven/com.braintribe.codec.dom-codecs
						final String idUrl = "https://api.github.com/orgs/" + org + "/packages/maven/" + builder.getGroupId() + "."
								+ builder.getArtifactId();
						log.debug(() -> "Trying to get artifact ID via " + idUrl);
	
						HttpGet idGet = new HttpGet(idUrl);
						idGet.setHeader("Accept", "application/vnd.github+json");
						idGet.setHeader("Authorization", "Bearer " + token);
						idGet.setHeader("X-GitHub-Api-Version", "2022-11-28");
						try (CloseableHttpResponse idResponse = getResponseWithRetry(idGet)) {
							int idStatusCode = idResponse.getStatusLine().getStatusCode();
							if (idStatusCode >= 200 && idStatusCode < 300) {
								String idJson = EntityUtils.toString(idResponse.getEntity(), "UTF-8");
								log.debug(() -> "Received status code " + idStatusCode + ", JSON Body has length " + idJson.length());
								JsonParser jsonParser = new JsonFactory().createParser(idJson);
								String artifactId = null;
								while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
									String name = jsonParser.getCurrentName();
									if ("id".equals(name)) {
										jsonParser.nextToken();
										artifactId = jsonParser.getText();
										break;
									}
								}
								if (artifactId != null) {
									log.debug("Identified artifact ID " + artifactId);
									// https://github.com/hiconic-os/maven-repo-dev/packages/1997637?version=2.0.8
									final String partsUrl = "https://github.com/" + org + "/" + repo + "/packages/" + artifactId + "?version="
											+ builder.getFileName();
									log.debug(() -> "Trying to get part information via " + partsUrl);
	
									HttpGet partsGet = new HttpGet(partsUrl);
									partsGet.setHeader("Accept", "application/vnd.github+json");
									partsGet.setHeader("Authorization", "Bearer " + token);
									partsGet.setHeader("X-GitHub-Api-Version", "2022-11-28");
	
									try (CloseableHttpResponse partsResponse = getResponseWithRetry(partsGet)) {
										int partsStatusCode = partsResponse.getStatusLine().getStatusCode();
										if (partsStatusCode >= 200 && partsStatusCode < 300) {
											String html = EntityUtils.toString(partsResponse.getEntity(), "UTF-8");
											log.debug(() -> "Received status code " + partsStatusCode + ", HTML Body has length " + html.length());
	
											Resource resource = Resource
													.createTransient(() -> new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
											resource.setName(builder.getFileName());
											this.url = partsUrl;
											return resource;
	
										} else {
											log.debug(() -> "Received status code " + partsStatusCode);
										}
									}
								} else {
									log.debug(() -> "Could not identify an artifact ID");
								}
							} else {
								log.debug(() -> "Received status code " + idStatusCode);
							}
						} catch (IOException ioe) {
							throw new UncheckedIOException(ioe);
						}
					} else {
						log.debug(() -> "Found no GITHUB_READ_PACKAGES_TOKEN value.");
					}
				} else {
					log.debug(() -> "URL is not an artifact version URL. No need for special support to look up parts.");
				}
			} else {
				log.debug(() -> "Does not seem to be a GitHub repository URL: " + this.url);
			}

			Resource resource = Resource.createTransient(this::openInputStream);
			resource.setName(builder.getFileName());
			return resource;
		}

		@Override
		public Maybe<InputStream> openStream() {
			try {
				return tryOpenInputStream();
			} 
			catch (UnknownHostException e) {
				log.debug("Unknown host: " + url);
				return Reasons.build(UnknownRepositoryHost.T).text("Unknown host: " + url) //
					.toMaybe();
			}
			catch (Exception e) {
				String tracebackId = UUID.randomUUID().toString();
				String msg = "Could not open input stream for: " + url + " (tracebackId=" + tracebackId + ")";
				log.error(msg, e);
				
				return Reasons.build(CommunicationError.T).text(msg).toMaybe();
			}
		}

		@Override
		public Reason tryWriteToReasoned(Supplier<OutputStream> supplier) {
			try {
				Maybe<InputStream> inMaybe = tryOpenInputStream();

				if (inMaybe.isUnsatisfied())
					return inMaybe.whyUnsatisfied();

				try (InputStream in = inMaybe.get(); OutputStream out = supplier.get()) {
					IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);
				}
			} catch (Exception e) {
				return Reasons.build(CommunicationError.T).text("Error while transferring data from url " + url).cause(InternalError.from(e))
						.toReason();
			}

			return null;
		}

		@Override
		public boolean tryWriteTo(Supplier<OutputStream> supplier) throws IOException {
			Reason reason = tryWriteToReasoned(supplier);

			if (reason != null)
				return false;

			return true;
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			try (InputStream in = openInputStream()) {
				IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_64K);
			}
		}
		/**
		 * hash files seem to have this format : {@code [<file name><whitespace>]<hash>}, so if data contains whitespace characters, the part *AFTER*
		 * the last whitespace is taken..
		 * 
		 * @param hashOnServer
		 *            - the values as returned from the server
		 * @return - the relevant part of the hash
		 */
		private String extractRelevantHashpartFromServerData(String hashOnServer) {
			int st = 0;
			int index = -1;
			for (int i = 0; i < hashOnServer.length(); i++) {
				char c = hashOnServer.charAt(i);
				if (Character.isWhitespace(c)) {
					st = 1;
				} else {
					if (st == 1) {
						index = i;
						break;
					}
				}
			}
			String result = st == 1 ? hashOnServer.substring(index) : hashOnServer;

			return result;
		}

		/**
		 * @param response
		 *            - the {@link HttpResponse} as returned by the server
		 * @return - a {@link Pair} consting of the hash type and hash value
		 */
		private Pair<String, String> determineRequiredHashMatch(HttpResponse response) throws IOException {
			// only check if relevant
			if (checksumPolicy == ChecksumPolicy.ignore)
				return null;

			// search for the hashes in the headers, take the first one matching
			if (!ignoreHashHeaders) {
				for (Entry<String, Pair<String, String>> entry : hashAlgToHeaderKeyAndExtension.entrySet()) {
					Header header = response.getFirstHeader(entry.getValue().first());
					if (header != null) {
						return Pair.of(entry.getKey(), header.getValue());
					}
				}
			}
			// search for the hashes in the hash files as stored on the server, take the first one present
			for (Entry<String, Pair<String, String>> entry : hashAlgToHeaderKeyAndExtension.entrySet()) {
				String hashFile = url + "." + entry.getValue().second();
				try (CloseableHttpResponse hashResponse = getResponse(hashFile)) {
					int statusCode = hashResponse.getStatusLine().getStatusCode();
					if (statusCode >= 200 && statusCode < 300) {
						HttpEntity entity = hashResponse.getEntity();

						try (InputStream in = entity.getContent()) {
							String hashOnServer = extractRelevantHashpartFromServerData(IOTools.slurp(in, "US-ASCII"));
							return Pair.of(entry.getKey(), hashOnServer);
						}
					} else if (statusCode != 404) {
						throw new CommunicationException(
								"error while downloading hash [" + hashFile + "], Http status [" + hashResponse.getStatusLine() + "]");
					}
				}
			}
			return null;
		}

		/**
		 * lenient input stream supplier, checks on CRC if configured
		 * 
		 * @return - an {@link InputStream} or null if not backed by data on the server
		 * @throws IOException
		 *             - if anything goes wrong
		 */
		private Maybe<InputStream> tryOpenInputStream() throws IOException {
			CloseableHttpResponse response = getResponse(url);

			int statusCode = response.getStatusLine().getStatusCode();
			
			if (log.isDebugEnabled()) {
				String phrase = response.getStatusLine().getReasonPhrase();
				phrase = phrase != null? " (" + phrase + ")": "";
				log.debug("received status " + statusCode + phrase + " from url " + url);
			}
			
			if (statusCode >= 200 && statusCode < 300) {
				HttpEntity entity = response.getEntity();

				Pair<String, String> hashAlgAndValuePair = determineRequiredHashMatch(response);
				MessageDigest messageDigest = hashAlgAndValuePair != null ? createMessageDigest(hashAlgAndValuePair.getFirst()) : null;

				InputStream inputStream = entity.getContent();

				if (messageDigest != null) {
					inputStream = new DigestInputStream(inputStream, messageDigest);
				}

				return Maybe.complete(new BasicDelegateInputStream(inputStream) {
					@Override
					public void close() throws IOException {
						super.close();
						response.close();
					}

					@Override
					public int read() throws IOException {
						int i = super.read();
						if (i < 0) {
							checkChecksum();
						}
						return i;
					}

					@Override
					public int read(byte[] b) throws IOException {
						int i = super.read(b);
						if (i < 0) {
							checkChecksum();
						}
						return i;
					}

					@Override
					public int read(byte[] b, int off, int len) throws IOException {
						int i = super.read(b, off, len);
						if (i < 0) {
							checkChecksum();
						}
						return i;
					}

					/**
					 * check the CRC of the stream right at time of closing
					 */
					private void checkChecksum() {
						if (messageDigest != null) {
							String hash = StringTools.toHex(messageDigest.digest());
							if (!hash.equalsIgnoreCase(hashAlgAndValuePair.getSecond())) {
								String msg = "checksum [" + hashAlgAndValuePair.first() + "] mismatch for [" + url + "], expected ["
										+ hashAlgAndValuePair.getSecond() + "], found [" + hash + "]";
								switch (checksumPolicy) {
									case fail:
										throw new ReasonException(TemplateReasons.build(MismatchingHash.T) //
												.assign(MismatchingHash::setUrl, url) //
												.assign(MismatchingHash::setHashAlgorithm, hashAlgAndValuePair.first)
												.assign(MismatchingHash::setExpectedHash, hashAlgAndValuePair.getSecond())
												.assign(MismatchingHash::setFoundHash, hash).toReason());
									case warn:
										log.warn(msg);
										break;
									case ignore:
									default:
										break;
								}
							}
						}

					}
				});
			} else {
				response.close();

				return statusProblemMaybe(response);
			}
		}

		/**
		 * @param hashAlg
		 *            - name of hashing algo
		 * @return - a matching {@link MessageDigest}
		 */
		private MessageDigest createMessageDigest(String hashAlg) {
			try {
				return MessageDigest.getInstance(hashAlg);
			} catch (NoSuchAlgorithmException e) {
				throw new UnsupportedOperationException(e);
			}
		}

		/**
		 * strict supplier of a input stream to the resource.
		 * 
		 * @return - a valid (crc'd) input stream
		 * @throws IOException
		 *             - if no input stream was able to be retrieved
		 */
		private InputStream openInputStream() throws IOException {
			Maybe<InputStream> inMaybe = tryOpenInputStream();

			if (inMaybe.isUnsatisfiedBy(NotFound.T)) {
				throw new NoSuchElementException("no such file [" + url + "]");
			}

			return inMaybe.get();
		}

		private <T> Maybe<T> statusProblemMaybe(CloseableHttpResponse response) {
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

		@Override
		public Maybe<Boolean> backed() {
			try {
				CloseableHttpResponse response = getResponse(this.url, true);
				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode >= 200 && statusCode < 300)
					return Maybe.complete(true);

				if (statusCode == 404)
					return Maybe.complete(false);

				return statusProblemMaybe(response);

			} catch (IOException e) {
				return Reasons.build(CommunicationError.T).text("error while testing existance of [" + url + "]").cause(InternalError.from(e))
						.toMaybe();
			}
		}

		@Override
		public boolean isBacked() {
			try {
				CloseableHttpResponse response = getResponse(this.url, true);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode >= 200 && statusCode < 300)
					return true;
			} catch (IOException e) {
				throw new UncheckedIOException("error while testing existance of [" + url + "]", e);
			}
			return false;
		}

		@Override
		public String repositoryId() {
			return repositoryId;
		}
	}

	@Override
	public List<PartReflection> getPartsOf(CompiledArtifactIdentification compiledArtifactIdentification) {
		Maybe<ArtifactDataResolution> htmlContent = getPartOverview(compiledArtifactIdentification);

		if (htmlContent.isSatisfied()) {
			try {
				String htmlData = IOTools.slurp(htmlContent.get().getResource().openStream(), "UTF-8");
				List<String> filenamesFromHtml = null;
				if (activateGithubHandling && this.root.startsWith("https://maven.pkg.github.com")) {
					filenamesFromHtml = parseFilenamesFromGitHubHtml(htmlData, compiledArtifactIdentification);
				} else {
					filenamesFromHtml = HtmlContentParser.parseFilenamesFromHtml(htmlData);
				}

				List<PartReflection> partReflections = PartReflectionCommons.transpose(compiledArtifactIdentification, repositoryId,
						filenamesFromHtml);
				return partReflections;
			} catch (IOException e) {
				// ignored
			}
		}

		Reason whyUnsatisfied = htmlContent.whyUnsatisfied();

		if (whyUnsatisfied instanceof NotFound)
			return null;

		throw new ReasonException(whyUnsatisfied);
	}

	private List<String> parseFilenamesFromGitHubHtml(String htmlData, CompiledArtifactIdentification compiledArtifactIdentification) {
		String artifactId = compiledArtifactIdentification.getArtifactId();
		String version = compiledArtifactIdentification.getVersion().toString();
		String expectedStart = artifactId + "-" + version;
		List<String> result = new ArrayList<>();
		StringTools.getLines(htmlData).forEach(line -> {
			line = line.trim();
			// "." -> e.g. example-artifacts-1.2.3.pom
			// "-" -> e.g. example-artifacts-1.2.3-asset.man
			if ((line.startsWith(expectedStart + ".") || line.startsWith(expectedStart + "-")) && !line.matches(".+\\.(md5|sha\\d+)")) {
				result.add(line);
			}
		});
		return result;
	}

	@Override
	public Maybe<ArtifactDataResolution> getPartOverview(CompiledArtifactIdentification compiledArtifactIdentification) {
		ArtifactAddressBuilder artifactAddress = ArtifactAddressBuilder.build().root(root).artifact(compiledArtifactIdentification)
				.file(compiledArtifactIdentification.getVersion().asString());
		return resolve(artifactAddress);
	}

}
