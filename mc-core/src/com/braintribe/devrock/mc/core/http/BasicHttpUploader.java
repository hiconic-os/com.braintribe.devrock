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
package com.braintribe.devrock.mc.core.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
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
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.repository.HttpUploader;
import com.braintribe.devrock.mc.api.repository.UploadContext;
import com.braintribe.devrock.mc.core.commons.McConversions;
import com.braintribe.devrock.mc.core.resolver.common.AnalysisArtifactResolutionPreparation;
import com.braintribe.devrock.model.mc.reason.MetadataUploadFailed;
import com.braintribe.devrock.model.mc.reason.PartUploadFailed;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.exception.CommunicationException;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.CommunicationError;
import com.braintribe.logging.Logger;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.ArtifactResolution;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.generic.session.OutputStreamer;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.version.Version;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.NullOutputStream;

public class BasicHttpUploader implements HttpUploader {
	private static final List<String> hashTypes = Arrays.asList("sha1", "MD5", "SHA-256");
	private static Logger log = Logger.getLogger(BasicHttpUploader.class);
	private CloseableHttpClient httpClient;	
	private static int MAX_RETRIES = 3;

	private Map<String, Pair<String,String>> hashAlgToHeaderKeyAndExtension = new LinkedHashMap<>();
	{
		hashAlgToHeaderKeyAndExtension.put( "MD5", Pair.of("X-Checksum-Md5", "md5"));
		hashAlgToHeaderKeyAndExtension.put( "SHA-1", Pair.of( "X-Checksum-Sha1", "Sha1"));
		hashAlgToHeaderKeyAndExtension.put( "SHA-256", Pair.of( "X-Checksum-Sha256", "Sha256"));
	}
	
	@Required @Configurable
	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	public ArtifactResolution upload(MavenHttpRepository repository, Artifact artifact) {
		return upload(repository, Collections.singletonList(artifact));
	}
	
	public ArtifactResolution upload(UploadContext uploadContext, MavenHttpRepository repository, Iterable<? extends Artifact> artifacts) {
		String authProvokeUrl = ArtifactAddressBuilder.build().root(repository.getUrl()).versionedArtifact(artifacts.iterator().next()).toPath().toSlashPath();
		
		String host;
		try {
			host = new URI(repository.getUrl()).getHost();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
		HttpClientContext context = HttpClientContext.create();
		
		if (repository.getUser() != null && repository.getPassword() != null) {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials( new AuthScope( host, AuthScope.ANY_PORT), new UsernamePasswordCredentials( repository.getUser(), repository.getPassword()));	
			context.setCredentialsProvider( credentialsProvider);
		}
						
		provokeAuthentication( httpClient, context, authProvokeUrl);
		
		ArtifactResolution resolution = ArtifactResolution.T.create();
		
		// actual upload 
		for (Artifact artifact: artifacts) {
			Artifact resolutionArtifact = Artifact.T.create();
			resolutionArtifact.setGroupId(artifact.getGroupId());
			resolutionArtifact.setArtifactId(artifact.getArtifactId());
			resolutionArtifact.setVersion(artifact.getVersion());

			resolution.getTerminals().add(resolutionArtifact);
			resolution.getSolutions().add(resolutionArtifact);
		
			MavenMetaData versionedMetaData = buildVersionedMetaData(artifact);

			String versionedMetaDataUrl = ArtifactAddressBuilder.build().root(repository.getUrl()).versionedArtifact(artifact).metaData().toPath().toSlashPath();

			Reason metaUploadError = uploadIfNeeded(context, versionedMetaDataUrl, out -> DeclaredMavenMetaDataMarshaller.INSTANCE.marshall(out, versionedMetaData));
			
			for (Map.Entry<String, Part> entry: artifact.getParts().entrySet()) {
				Part part = entry.getValue();
				String partKey = entry.getKey();
				Resource resource = part.getResource();
				String url = ArtifactAddressBuilder.build().root(repository.getUrl()).versionedArtifact(artifact).part(part).toPath().toSlashPath();
				
				Reason error = uploadIfNeeded(context, url, resource::writeToStream);

				Part resolutionPart = Part.T.create();
				resolutionPart.setClassifier(part.getClassifier());
				resolutionPart.setType(part.getType());
				resolutionArtifact.getParts().put(partKey, resolutionPart);

				if (error != null) {
					Reason reason = Reasons.build(PartUploadFailed.T).text("Part [" + part.asString() + "] could not be uploaded to url [" + url + "]").cause(error).toReason();
					resolutionPart.setFailure(reason);
					AnalysisArtifactResolutionPreparation.acquireCollatorReason(artifact).getReasons().add(reason);
				}
			}
			
			// update or create artifact metadata
			updateMetaData(context, repository.getUrl(), artifact);
			
			if (resolutionArtifact.hasFailed()) {
				AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolution).getReasons().add(resolutionArtifact.getFailure());
			}
			
			uploadContext.progressListener().onArtifactUploaded(resolutionArtifact);
		}
		
		return resolution;			
	}

	private void updateMetaData(HttpClientContext context, String rootUrl, Artifact artifact) {
		String metaDataUrl = ArtifactAddressBuilder.build().root(rootUrl).artifact(artifact).metaData().toPath().toSlashPath();
		MavenMetaData existingMavenMetaData = readOrPrimeMavenMetaData(context, metaDataUrl, artifact);
		Reason error = updateMetaDataIfRequired(context, existingMavenMetaData, metaDataUrl, artifact);
		if (error != null) {
			Reason uploadFailure = Reasons.build(MetadataUploadFailed.T).text("Uploading artifact metadata for " + artifact.asString() + " to " + metaDataUrl + " failed").cause(error).toReason();
			AnalysisArtifactResolutionPreparation.acquireCollatorReason(artifact).getReasons().add(uploadFailure);
		}
	}

	private Reason updateMetaDataIfRequired(HttpClientContext context, MavenMetaData mavenMetaData,
			String metaDataUrl, Artifact artifact) {
		Versioning versioning = mavenMetaData.getVersioning();
		if (versioning == null) {
			versioning = Versioning.T.create();
			mavenMetaData.setVersioning(versioning);			
		}

		Version version = Version.parse(artifact.getVersion());
		
		List<Version> versions = versioning.getVersions();
		Set<Version> sortedVersions = new TreeSet<>(versions);
		
		if (!sortedVersions.add(version))
			return null;
		
		versions.clear();
		versions.addAll(sortedVersions);		

		Date lastUpdated = new Date();
		String lastUpdatedStr = McConversions.formatMavenMetaDataDate(lastUpdated);
		versioning.setLastUpdated(lastUpdatedStr);
		versioning.setLatest(version);
		
		return uploadIfNeeded(context, metaDataUrl, out -> DeclaredMavenMetaDataMarshaller.INSTANCE.marshall(out, mavenMetaData));
	}

	private MavenMetaData readOrPrimeMavenMetaData(HttpClientContext context, String metaDataUrl, Artifact artifact) {
		
		HttpGet httpGet = new HttpGet(metaDataUrl);

		try {
			HttpResponse response = httpClient.execute( httpGet, context);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode >= 200 && statusCode < 300) {
				try (InputStream in = response.getEntity().getContent()) {
					return (MavenMetaData)DeclaredMavenMetaDataMarshaller.INSTANCE.unmarshall(in);
				}
			}
			else if (statusCode == 404) {
				MavenMetaData artifactMetaData = MavenMetaData.T.create();
				artifactMetaData.setGroupId( artifact.getGroupId());
				artifactMetaData.setArtifactId( artifact.getArtifactId());
				
				Versioning versioning = Versioning.T.create();
				artifactMetaData.setVersioning(versioning);
				
				return artifactMetaData;
			}
			else {
				throw new CommunicationException("Error [" + response.getStatusLine() + "] while reading existing maven-metadata.xml from " + metaDataUrl);
			}
		}
		catch (IOException e) {
			throw new CommunicationException("Error while reading existing maven-metadata.xml from " + metaDataUrl, e);
		}
	}

	private MavenMetaData buildVersionedMetaData(Artifact artifact) {
		// TODO: how to treat SNAPSHOTS
		MavenMetaData versionedMetaData = MavenMetaData.T.create();
		
		versionedMetaData.setGroupId(artifact.getGroupId());
		versionedMetaData.setArtifactId(artifact.getArtifactId());
		versionedMetaData.setVersion(Version.parse(artifact.getVersion()));
		return versionedMetaData;
	}
	
	/**
	 * Uploads the given resource to the given url. 
	 * @return -1 if the resource was already there
	 */
	private Reason uploadIfNeeded(HttpClientContext context, String url, OutputStreamer outputStreamer) {
		Map<String, String> hashes = generateHash( outputStreamer, hashTypes);
		
		boolean targetExists = false;
		Pair<String, String> hashAlgAndValuePairOfExistingFile = null;
		
	
		// test if it's there already..
		HttpHead headRequest =  new HttpHead(url);
		try {
			HttpResponse headResponse = httpClient.execute( headRequest, context);
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
				String sourceHash = hashes.get( hashAlgAndValuePairOfExistingFile.first);
				if (sourceHash != null) {
					if (sourceHash.equals(hashAlgAndValuePairOfExistingFile.second))
						return null;
				}
			}
		
			deleteTarget(httpClient, context, url);
		}
		
		HttpPut filePut = new HttpPut( url);
					
		OutputStreamerEntity streamEntity = new OutputStreamerEntity(outputStreamer);
		filePut.setHeader("X-Checksum-Sha1", hashes.get("sha1"));
		filePut.setHeader("X-Checksum-MD5", hashes.get("md5"));
		filePut.setHeader("X-Checksum-SHA256", hashes.get("SHA-256"));
		
		filePut.setEntity( streamEntity);			
		long before = System.nanoTime();
		int statusCode = 200;

		// prepare looping on fail 
		boolean done = false;
		int tries = 0;
		List<Exception> bufferedExceptions = new ArrayList<>(MAX_RETRIES);
		do {
			try {
				StatusLine httpStatusLine = put( httpClient, filePut, context);
				long after = System.nanoTime();
				statusCode = httpStatusLine.getStatusCode();
				if (statusCode >= 200 && statusCode < 300) {
					return null;
				} 
			}
			catch (Exception e) {
				bufferedExceptions.add(e);
			}
			// if failed, only try max retries before giving up
			if (!done) {
				done = ++tries > MAX_RETRIES;
			}
		} while (!done);

		if (!bufferedExceptions.isEmpty()) {
			CommunicationError reason = Reasons.build(CommunicationError.T).text("Error while uploading to " + url).toReason();
			
			for (Exception ex: bufferedExceptions) {
				reason.getReasons().add(com.braintribe.gm.model.reason.essential.InternalError.from(ex));
			}
				
			return reason;
		}
		
		return Reasons.build(CommunicationError.T).text("Upload to " + url + " failed with status code "+  statusCode).toReason();
	}
	

	private HttpEntity deleteTarget(CloseableHttpClient httpclient, HttpClientContext context, String url) {
		HttpEntity entity = null;				
		try {
			HttpDelete httpDelete = new HttpDelete( url);
			HttpResponse response = httpclient.execute( httpDelete, context);
			int statusCode = response.getStatusLine().getStatusCode();
			entity = response.getEntity();
			if (statusCode == 404) {
				if (log.isDebugEnabled()) {
					log.debug("target [" + url + "] doesn't exist");
				}
			}
			else if ((statusCode >= 200) && (statusCode < 300)) {
				if (log.isDebugEnabled()){
					log.debug("target [" + url + "] successfully deleted");
				}
			}
			else {
				log.warn( "cannot delete [" + url + "] as statuscode's [" + statusCode + "]");		
			}											
			
		} catch (Exception e) {
			log.warn( "cannot delete [" + url + "]", e);
		}
		finally {
			try {
				if (entity != null)
					EntityUtils.consume( entity);
			} catch (IOException e) {
				String msg = "can't consume http entity as " + e;
				log.error(msg, e);						
			}		
		}
		return entity;
	}
	
	private void provokeAuthentication(CloseableHttpClient httpClient, HttpClientContext context, String target) {
		try {
			HttpHead httpSpearHeadDelete = new HttpHead(target );
			httpClient.execute( httpSpearHeadDelete, context);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} 
		
	}

	private Map<String, String> generateHash(OutputStreamer outputStreamer, List<String> types) {
		Map<String, String> result = new HashMap<>();
		List<MessageDigest> digests = types.stream().map( t -> {
			try {
				return MessageDigest.getInstance( t);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("no digest found for [" + t + "]");
			}
		}).collect( Collectors.toList());

		OutputStream out = NullOutputStream.getInstance();
		
		for (MessageDigest digest: digests) {
			out = new DigestOutputStream(out, digest);	
		}
		
		try {
			outputStreamer.writeTo(out);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		for (int i = 0; i < types.size(); i++)  {
			MessageDigest digest = digests.get( i);
			byte [] digested = digest.digest();
			result.put( types.get(i), StringTools.toHex(digested));
		}

		return result;
	}

	private static StatusLine put(CloseableHttpClient client, HttpEntityEnclosingRequestBase request, HttpContext httpContext) {
		try (CloseableHttpResponse httpResponse = client.execute( request, httpContext)) {
			StatusLine httpStatusLine = httpResponse.getStatusLine();									
			return httpStatusLine;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * @param response - the {@link HttpResponse} as returned by the server 
	 * @return - a {@link Pair} consting of the hash type and hash value
	 * @throws IOException
	 */
	private Pair<String,String> determineRequiredHashMatch(HttpResponse response) throws IOException {
		// only check if relevant
		// search for the hashes in the headers, take the first one matching
	
		for (Entry<String, Pair<String,String>> entry : hashAlgToHeaderKeyAndExtension.entrySet()) {
			String first = entry.getValue().first();
			Header header = response.getFirstHeader( first);
			if (header != null) {
				return Pair.of( entry.getKey(), header.getValue());
			}
		}
				
		
		return null;					
	}
}
