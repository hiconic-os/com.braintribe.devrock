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
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.api.resolver.ChecksumPolicy;
import com.braintribe.devrock.mc.core.commons.HtmlContentParser;
import com.braintribe.devrock.mc.core.commons.PartReflectionCommons;
import com.braintribe.devrock.mc.core.download.PartDownloadInputStream;
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
 * a HTTP based {@link ArtifactDataResolver}, for standard remote repos for
 * instance
 * 
 * @author pit
 *
 */
public class HttpRepositoryArtifactDataResolver extends HttpRepositoryBase
		implements ArtifactVersionsResolverTrait, ArtifactDataResolver {
	private static Logger log = Logger.getLogger(HttpRepositoryArtifactDataResolver.class);
	private static Pattern githubUrlPattern = Pattern // TODO support ${token}@mvn.pkg....
			.compile("https:\\/\\/maven.pkg.github.com\\/([^\\/]+)\\/([^\\/]+).*");

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
	 * @return - either {@link Optional} with {@link BasicArtifactDataResolution} or
	 *         empty {@link Optional}
	 */
	protected Maybe<ArtifactDataResolution> resolve(ArtifactAddressBuilder builder) {
		return resolve(builder, false);
	}
	
	protected Maybe<ArtifactDataResolution> resolve(ArtifactAddressBuilder builder, ArtifactIdentification artifact, Version version, PartIdentification part) {
		return resolve(builder, false, artifact, version, part);
	}
	
	protected Maybe<ArtifactDataResolution> resolve(ArtifactAddressBuilder builder, boolean ignoreHash) {
		return resolve(builder, ignoreHash, null, null, null);
	}
	
	protected Maybe<ArtifactDataResolution> resolve(ArtifactAddressBuilder builder, boolean ignoreHash, ArtifactIdentification artifact, Version version, PartIdentification part) {
		HttpArtifactDataResolution res = new HttpArtifactDataResolution(builder, ignoreHash, artifact, version, part);
		return Maybe.complete(res);
	}

	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(ArtifactIdentification identification) {
		return resolve(ArtifactAddressBuilder.build().root(root).artifact(identification).metaData(), identification, null, null);
	}

	@Override
	public Maybe<ArtifactDataResolution> resolveMetadata(CompiledArtifactIdentification identification) {
		return resolve(ArtifactAddressBuilder.build().root(root).compiledArtifact(identification).metaData(), ArtifactIdentification.from(identification), identification.getVersion(), null);
	}

	@Override
	public Maybe<ArtifactDataResolution> resolvePart(CompiledArtifactIdentification identification,
			PartIdentification partIdentification, Version partVersionOverrride) {
		ArtifactIdentification ai = ArtifactIdentification.from(identification);
		if (partVersionOverrride == null)
			return resolve(ArtifactAddressBuilder.build().root(root).compiledArtifact(identification)
					.part(partIdentification), ai, partVersionOverrride, partIdentification);
		else
			return resolve(ArtifactAddressBuilder.build().root(root).compiledArtifact(identification)
					.part(partIdentification, partVersionOverrride), ai, identification.getVersion(), partIdentification);
	}

	/**
	 * internal implementation of {@link ArtifactDataResolution}
	 */
	private class HttpArtifactDataResolution implements ArtifactDataResolution {
		private final ArtifactAddressBuilder builder;
		private final String url;
		private final boolean ignoreHash;
		private final PartIdentification part;
		private final Version version;
		private final ArtifactIdentification artifact;

		public HttpArtifactDataResolution(ArtifactAddressBuilder builder, boolean ignoreHash, ArtifactIdentification artifact, Version version, PartIdentification part) {
			this.builder = builder;
			this.artifact = artifact;
			this.version = version;
			this.part = part;
			this.url = builder.toPath().toSlashPath();
			this.ignoreHash = ignoreHash;
		}

		@Override
		public Resource getResource() {
			Resource resource = Resource.createTransient(this::openInputStream);
			resource.setName(builder.getFileName());
			return resource;
		}

		@Override
		public Maybe<InputStream> openStream() {
			try {
				return tryOpenInputStream();

			} catch (UnknownHostException e) {
				log.debug("Unknown host: " + url);
				return Reasons.build(UnknownRepositoryHost.T) //
						.text("Unknown host: " + url) //
						.toMaybe();

			} catch (Exception e) {
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
				return Reasons.build(CommunicationError.T).text("Error while transferring data from url " + url)
						.cause(InternalError.from(e)).toReason();
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
		 * hash files seem to have this format :
		 * {@code [<file name><whitespace>]<hash>}, so if data contains whitespace
		 * characters, the part *AFTER* the last whitespace is taken..
		 * 
		 * @param hashOnServer - the values as returned from the server
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
		 * @param response - the {@link HttpResponse} as returned by the server
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
			// search for the hashes in the hash files as stored on the server, take the
			// first one present
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
						throw new CommunicationException("error while downloading hash [" + hashFile
								+ "], Http status [" + hashResponse.getStatusLine() + "]");
					}
				}
			}
			return null;
		}

		/**
		 * lenient input stream supplier, checks on CRC if configured
		 * 
		 * @return - an {@link InputStream} or null if not backed by data on the server
		 * @throws IOException - if anything goes wrong
		 */
		private Maybe<InputStream> tryOpenInputStream() throws IOException {
			CloseableHttpResponse response = getResponse(url);

			int statusCode = response.getStatusLine().getStatusCode();

			if (log.isDebugEnabled()) {
				String phrase = response.getStatusLine().getReasonPhrase();
				phrase = phrase != null ? " (" + phrase + ")" : "";
				log.debug("received status " + statusCode + phrase + " from url " + url);
			}

			if (statusCode >= 200 && statusCode < 300) {
				HttpEntity entity = response.getEntity();

				Pair<String, String> hashAlgAndValuePair = ignoreHash ? null : determineRequiredHashMatch(response);
				MessageDigest messageDigest = hashAlgAndValuePair != null //
						? createMessageDigest(hashAlgAndValuePair.getFirst()) //
						: null;

				InputStream inputStream = new PartDownloadInputStream( //
						entity.getContent(), //
						repositoryId, //
						artifact, version, part, //
						builder.toRelativePath().toSlashPath(), //
						messageDigest, digest -> {

							String hash = StringTools.toHex(digest.digest());
							if (hash.equalsIgnoreCase(hashAlgAndValuePair.getSecond()))
								return;

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
						});

				return Maybe.complete(inputStream);
				
			} else {
				response.close();

				return statusProblemMaybe(response);
			}
		}

		/**
		 * @param hashAlg - name of hashing algo
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
		 * @throws IOException - if no input stream was able to be retrieved
		 */
		private InputStream openInputStream() throws IOException {
			return tryOpenInputStream().get();
		}

		private <T> Maybe<T> statusProblemMaybe(CloseableHttpResponse response) {
			return HttpRepositoryArtifactDataResolver.statusProblemMaybe(url, response);
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
				return Reasons.build(CommunicationError.T).text("error while testing existance of [" + url + "]")
						.cause(InternalError.from(e)).toMaybe();
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

	record GitHubInfo(String org, String repo) {
	}

	private GitHubInfo hasGitHubInfo() {
		if (!activateGithubHandling)
			return null;

		Matcher matcher = githubUrlPattern.matcher(this.root);

		if (!matcher.matches())
			return null;

		String org = matcher.group(1);
		String repo = matcher.group(2);

		return new GitHubInfo(org, repo);
	}

	@Override
	public Maybe<List<PartReflection>> getPartsOfReasoned(
			CompiledArtifactIdentification compiledArtifactIdentification) {
		GitHubInfo gitHubInfo = hasGitHubInfo();

		if (gitHubInfo != null)
			return getPartsOfFromGitHub(gitHubInfo, compiledArtifactIdentification);

		return getPartsOfFromStandardRepo(compiledArtifactIdentification);
	}

	private Maybe<List<PartReflection>> getPartsOfFromGitHub(GitHubInfo gitHubInfo, CompiledArtifactIdentification compiledArtifactIdentification) {
		Maybe<String> htmlDataMaybe = getGitHubPartOverviewContent(gitHubInfo, compiledArtifactIdentification);

		if (htmlDataMaybe.isUnsatisfiedBy(NotFound.T))
			return Maybe.complete(Collections.emptyList());

		if (htmlDataMaybe.isUnsatisfied())
			return htmlDataMaybe.whyUnsatisfied().asMaybe();

		String htmlData = htmlDataMaybe.get();

		List<String> filenamesFromHtml = parseFilenamesFromGitHubHtml(htmlData, compiledArtifactIdentification);
		List<PartReflection> partReflections = PartReflectionCommons.transpose(compiledArtifactIdentification,
				repositoryId, filenamesFromHtml);

		return Maybe.complete(partReflections);
	}

	private Maybe<String> getGitHubPartOverviewContent(GitHubInfo gitHubInfo, CompiledArtifactIdentification compiledArtifactIdentification) {
		final String org = gitHubInfo.org();
		final String repo = gitHubInfo.repo();
		final String groupId = compiledArtifactIdentification.getGroupId();
		final String artifactId = compiledArtifactIdentification.getArtifactId();
		final String version = compiledArtifactIdentification.getVersion().asString();

		final String idUrl = "https://api.github.com/orgs/" + org + "/packages/maven/" + groupId + "." + artifactId;

		log.debug(() -> "Trying to get artifact ID via GitHub API call" + idUrl);

		final Maybe<String> idJsonMaybe;
		
		try {
			idJsonMaybe = readText(idUrl, "UTF-8");

		} catch (IOException e) {
			return InternalError.from(e, "Error while reading GitHub id JSON").asMaybe();
		}
		
		if (idJsonMaybe.isUnsatisfied()) {
			log.debug(() -> "Failed to get artifact ID via GitHub API call " + idUrl + " : " + idJsonMaybe.whyUnsatisfied().stringify(true));
			return idJsonMaybe.whyUnsatisfied().asMaybe();
		}

		String idJson = idJsonMaybe.get();
		
		String gitHubArtifactId = null;
		try (JsonParser jsonParser = new JsonFactory().createParser(idJson)) {

			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				String name = jsonParser.getCurrentName();
				if ("id".equals(name)) {
					jsonParser.nextToken();
					gitHubArtifactId = jsonParser.getText();
					break;
				}
			}
			
		} catch (IOException e) {
			return InternalError.from(e, "Error while parsing GitHub id JSON").asMaybe();
		}
		
		try {
			if (gitHubArtifactId == null)
				return Reasons.build(NotFound.T).text("GitHub package artifact id not found in JSON").toMaybe();
			
			log.debug("Identified artifact ID " + artifactId);
			
			// https://github.com/hiconic-os/maven-repo-dev/packages/1997637?version=2.0.8
			final String partsUrl = "https://github.com/" + org + "/" + repo + "/packages/" + gitHubArtifactId + "?version="
					+ version;
			
			log.debug(() -> "Trying to get part information via " + partsUrl);
			
			return readText(partsUrl, "UTF-8");
		}
		catch (IOException e) {
			return InternalError.from(e, "Error while reading GitHub part overview").asMaybe();
		}
	}

	private Maybe<List<PartReflection>> getPartsOfFromStandardRepo(CompiledArtifactIdentification compiledArtifactIdentification) {
		Maybe<ArtifactDataResolution> htmlContent = getPartOverview(compiledArtifactIdentification);

		if (htmlContent.isUnsatisfiedBy(NotFound.T))
			return Maybe.complete(Collections.emptyList());

		if (htmlContent.isUnsatisfied())
			return htmlContent.whyUnsatisfied().asMaybe();

		try {
			Maybe<InputStream> inMaybe = htmlContent.get().openStream();

			if (inMaybe.isUnsatisfiedBy(NotFound.T))
				return Maybe.complete(Collections.emptyList());

			if (inMaybe.isUnsatisfied())
				return inMaybe.whyUnsatisfied().asMaybe();

			String htmlData = IOTools.slurp(inMaybe.get(), "UTF-8");
			List<String> filenamesFromHtml = HtmlContentParser.parseFilenamesFromHtml(htmlData);

			List<PartReflection> partReflections = PartReflectionCommons.transpose(compiledArtifactIdentification,
					repositoryId, filenamesFromHtml);
			return Maybe.complete(partReflections);

		} catch (Exception e) {
			String tracebackId = UUID.randomUUID().toString();
			String msg = "Exception while parsing repo part list reflection for " + compiledArtifactIdentification
					+ " (tracebackId=" + tracebackId + ")";
			log.error(msg, e);
			return InternalError.from(e, msg).asMaybe();
		}
	}

	private List<String> parseFilenamesFromGitHubHtml(String htmlData,
			CompiledArtifactIdentification compiledArtifactIdentification) {
		String artifactId = compiledArtifactIdentification.getArtifactId();
		String version = compiledArtifactIdentification.getVersion().toString();
		String expectedStart = artifactId + "-" + version;
		List<String> result = new ArrayList<>();
		StringTools.getLines(htmlData).forEach(line -> {
			line = line.trim();
			// "." -> e.g. example-artifacts-1.2.3.pom
			// "-" -> e.g. example-artifacts-1.2.3-asset.man
			if ((line.startsWith(expectedStart + ".") || line.startsWith(expectedStart + "-"))
					&& !line.matches(".+\\.(md5|sha\\d+)")) {
				result.add(line);
			}
		});
		return result;
	}

	@Override
	public Maybe<ArtifactDataResolution> getPartOverview(CompiledArtifactIdentification compiledArtifactIdentification) {
		ArtifactAddressBuilder artifactAddress = ArtifactAddressBuilder.build() //
				.root(root)
				.artifact(compiledArtifactIdentification) //
				.file(compiledArtifactIdentification.getVersion().asString());

		return resolve(artifactAddress, true);
	}

}
