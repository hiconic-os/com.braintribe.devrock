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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.repository.UploadContext;
import com.braintribe.devrock.mc.core.commons.McConversions;
import com.braintribe.devrock.mc.core.resolver.common.AnalysisArtifactResolutionPreparation;
import com.braintribe.devrock.model.mc.reason.PartUploadFailed;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
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

public class BasicFileSystemUploader {
	private static final List<String> hashTypes = Arrays.asList("sha1", "MD5", "SHA-256");
	private static Logger log = Logger.getLogger(BasicFileSystemUploader.class);

	private Map<String, Pair<String,String>> hashAlgToHeaderKeyAndExtension = new LinkedHashMap<>();
	{
		hashAlgToHeaderKeyAndExtension.put( "MD5", Pair.of("X-Checksum-Md5", "md5"));
		hashAlgToHeaderKeyAndExtension.put( "SHA-1", Pair.of( "X-Checksum-Sha1", "Sha1"));
		hashAlgToHeaderKeyAndExtension.put( "SHA-256", Pair.of( "X-Checksum-Sha256", "Sha256"));
	}
	
	public ArtifactResolution upload(MavenFileSystemRepository repository, Artifact artifact) {
		return upload(UploadContext.build().done(), repository, Collections.singletonList(artifact));
	}
	
	public ArtifactResolution upload(UploadContext uploadContext, MavenFileSystemRepository repository, Iterable<? extends Artifact> artifacts) {
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

			String versionedMetaDataUrl = ArtifactAddressBuilder.build().root(repository.getRootPath()).versionedArtifact(artifact).metaData().toPath().toFilePath();

			upload(versionedMetaDataUrl, out -> DeclaredMavenMetaDataMarshaller.INSTANCE.marshall(out, versionedMetaData));
			
			for (Map.Entry<String, Part> entry: artifact.getParts().entrySet()) {
				Part part = entry.getValue();
				String partKey = entry.getKey();
				Resource resource = part.getResource();
				String url = ArtifactAddressBuilder.build().root(repository.getRootPath()).versionedArtifact(artifact).part(part).toPath().toFilePath();
				
				Reason uploadReason = upload(url, resource::writeToStream);

				Part resolutionPart = Part.T.create();
				resolutionPart.setClassifier(part.getClassifier());
				resolutionPart.setType(part.getType());
				resolutionArtifact.getParts().put(partKey, resolutionPart);

				if (uploadReason != null) {
					resolutionPart.setFailure(uploadReason);
					AnalysisArtifactResolutionPreparation.acquireCollatorReason(artifact).getReasons().add(uploadReason);
				}
			}
			
			// update or create artifact metadata
			updateMetaData(repository.getRootPath(), artifact);
			
			if (resolutionArtifact.hasFailed()) {
				AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolution).getReasons().add(resolutionArtifact.getFailure());
			}
			
			uploadContext.progressListener().onArtifactUploaded(resolutionArtifact);
		}
		
		return resolution;			
	}

	private void updateMetaData(String rootPath, Artifact artifact) {
		String metaDataUrl = ArtifactAddressBuilder.build().root(rootPath).artifact(artifact).metaData().toPath().toFilePath();
		MavenMetaData existingMavenMetaData = readOrPrimeMavenMetaData(metaDataUrl, artifact);
		updateMetaDataIfRequired(existingMavenMetaData, metaDataUrl, artifact);
	}

	private void updateMetaDataIfRequired(MavenMetaData mavenMetaData,
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
			return;
		
		versions.clear();
		versions.addAll(sortedVersions);		

		Date lastUpdated = new Date();
		String lastUpdatedStr = McConversions.formatMavenMetaDataDate(lastUpdated);
		versioning.setLastUpdated(lastUpdatedStr);
		versioning.setLatest(version);
		
		upload(metaDataUrl, out -> DeclaredMavenMetaDataMarshaller.INSTANCE.marshall(out, mavenMetaData));
	}

	private MavenMetaData readOrPrimeMavenMetaData(String metaDataUrl, Artifact artifact) {
		
		File file = new File(metaDataUrl);
		
		if (!file.exists()) {
			MavenMetaData artifactMetaData = MavenMetaData.T.create();
			artifactMetaData.setGroupId( artifact.getGroupId());
			artifactMetaData.setArtifactId( artifact.getArtifactId());
			
			Versioning versioning = Versioning.T.create();
			artifactMetaData.setVersioning(versioning);
			
			return artifactMetaData;
		}
		
		try (InputStream in = new BufferedInputStream(new FileInputStream(metaDataUrl))) {
			return (MavenMetaData)DeclaredMavenMetaDataMarshaller.INSTANCE.unmarshall(in);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Error while reading existing maven-metadata.xml from " + metaDataUrl, e);
		}
	}

	private MavenMetaData buildVersionedMetaData(Artifact artifact) {
		MavenMetaData versionedMetaData = MavenMetaData.T.create();
		
		versionedMetaData.setGroupId(artifact.getGroupId());
		versionedMetaData.setArtifactId(artifact.getArtifactId());
		versionedMetaData.setVersion(Version.parse(artifact.getVersion()));
		return versionedMetaData;
	}
	
	private Reason upload(String url, OutputStreamer outputStreamer) {
		File file = new File(url);
		file.getParentFile().mkdirs();
		
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(url))) {
			outputStreamer.writeTo(out);
		} catch (IOException e) {
			return Reasons.build(PartUploadFailed.T) //
					.text("Part [" + url + "] could not be written.") //
					.cause(InternalError.from(e)) //
					.toReason();
		}
		
		return null;
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
}
