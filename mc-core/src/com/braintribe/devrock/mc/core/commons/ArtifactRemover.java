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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.mc.reason.InvalidRepositoryConfigurationLocation;
import com.braintribe.devrock.model.mc.reason.MalformedMavenMetadata;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.IoError;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.version.Version;
import com.braintribe.utils.paths.UniversalPath;

/**
 * simple tool to remove an artifact from a local filesystem repository
 * (local-repo / install-repo)
 * 
 * @author pit
 *
 */
public class ArtifactRemover {

	/**
	 * removes all artifacts passed from the filesystem repository passed
	 * 
	 * @param vais - a {@link List} of {@link VersionedArtifactIdentification} to
	 *             remove
	 * @param root - the root directory
	 * @return - if at least one issue was found, a List of {@link Reason} or null
	 *         if all went fine
	 */
	public static Pair<List<Reason>, List<File>> removeArtifactsFromFilesystemRepo(List<VersionedArtifactIdentification> vais, File root) {

		List<Reason> fails = new ArrayList<>();
		List<File> nonDeletable = new ArrayList<>();
		for (VersionedArtifactIdentification vai : vais) {
			Pair<Reason,File> pair = removeArtifactFromFilesystemRepo(vai, root);		
			if (pair.first != null) {
				fails.add( pair.first);
			}
			nonDeletable.add( pair.second);
			
		}
		if (fails.size() > 0)
			return Pair.of(fails, nonDeletable);

		return Pair.of(null, nonDeletable);
	}

	/**
	 * @param map - a map of {@link VersionedArtifactIdentification} to {@link File}, the repo root of the artifact
	 * @return - if at least one issue was found, a List of {@link Reason} or null
	 *         if all went fine 
	 */
	public static Pair<List<Reason>, List<File>> removeArtifactsFromFilesystemRepo( Map<VersionedArtifactIdentification,File> map) {
		List<Reason> fails = new ArrayList<>();
		List<File> nonDeletable = new ArrayList<>();
		for (Map.Entry<VersionedArtifactIdentification, File> entry : map.entrySet()) {
			Pair<Reason,File> pair = removeArtifactFromFilesystemRepo(entry.getKey(), entry.getValue());		
			if (pair.first != null) {
				fails.add( pair.first);
			}
			nonDeletable.add( pair.second);
			
		}
		if (fails.size() > 0)
			return Pair.of(fails, nonDeletable);

		return Pair.of(null, nonDeletable);
	}

	/**
	 * removes the artifact passed from a filesystem repository passed
	 * 
	 * @param vai  - the {@link VersionedArtifactIdentification} that identifies the
	 *             artifact
	 * @param root - the root directory
	 * @return - null if everything went fine, a {@link Reason}otherwise
	 */
	public static Pair<Reason,File> removeArtifactFromFilesystemRepo(VersionedArtifactIdentification vai, File root) {

		// directory checks - main artifact
		File artifactDir = UniversalPath.from(root).pushDottedPath(vai.getGroupId()).push(vai.getArtifactId()).toFile();
		if (!artifactDir.exists()) {
			// no directory for artifact
			return Pair.of( Reasons.build(InvalidRepositoryConfigurationLocation.T)
					.text("no artifact directory found : " + artifactDir.getAbsolutePath()).toReason(), null);
		}
		String versionToRemoveAsString = vai.getVersion();
		Version versionToRemove = Version.parse(versionToRemoveAsString);

		// directory check - actual versioned artifact
		File versionedArtifactDir = UniversalPath.from(artifactDir).push(versionToRemoveAsString).toFile();
		if (!versionedArtifactDir.exists()) {
			// no actual directory for pc-artifact
			return Pair.of(Reasons.build(InvalidRepositoryConfigurationLocation.T)
					.text("no versioned artifact directory found : " + versionedArtifactDir.getAbsolutePath())
					.toReason(), null);
		}

		// metadata - loading
		File metadataFile = new File(artifactDir, "maven-metadata.xml");
		if (!metadataFile.exists()) {
			//
			return Pair.of( Reasons.build(InvalidRepositoryConfigurationLocation.T)
					.text("no metadata found : " + metadataFile.getAbsolutePath()).toReason(), null);
		}

		MavenMetaData md = null;
		try (InputStream in = new FileInputStream(metadataFile)) {
			md = (MavenMetaData) DeclaredMavenMetaDataMarshaller.INSTANCE.unmarshall(in);
		} catch (Exception e) {
			// can't unmarshall
			// e.printStackTrace();
		}
		if (md == null) {
			// no md at all
			return Pair.of(TemplateReasons.build(MalformedMavenMetadata.T)
					.assign(MalformedMavenMetadata::setMetadata, metadataFile.getAbsolutePath()).toReason(), null);
		}

		// metadata - manipulation
		Versioning versioning = md.getVersioning();
		if (versioning != null) {
			List<Version> versions = versioning.getVersions();
			int size = versions.size();
			// find
			boolean found = false;
			List<Version> retain = new ArrayList<>();
			for (Version version : versions) {
				if (version.compareTo(versionToRemove) == 0) {
					found = true;
					continue;
				}
				retain.add(version);
			}
			if (found && size == 1) {
				// only our version in file -> delete it
				metadataFile.delete();
			} else {
				// more than one version, only delete one
				if (found) {
					versioning.setVersions(retain);

					try (OutputStream out = new FileOutputStream(metadataFile)) {
						DeclaredMavenMetaDataMarshaller.INSTANCE.marshall(out, md);
					} catch (Exception e) {
						return Pair.of( Reasons.build(IoError.T)
								.text("cannot updated metadata as touched file can't be marshalled: "
										+ versionedArtifactDir.getAbsolutePath())
								.toReason(), null);
					}
				}
			}
		}
		File notDeleted = null;
		// finally, delete all files .. 
		
		// no metadata file in artifactDir -> delete it, otherwise only delete the versioned artifact dir
		if (metadataFile.exists()) {
			if (delete(versionedArtifactDir) != null) {
				notDeleted = versionedArtifactDir;
			}			
		}
		else  {
			if (delete(artifactDir) != null) {
				notDeleted = artifactDir;
			}			
		}
				
		
		return Pair.of( null, notDeleted);
	}
	
	
	/**
	 * rather clumsy fix : traverses the structure below and checks whether it's a pom file,
	 * hence to detect whether a PC has been *reinstalled* after it was purged before the 
	 * restart that triggers the delete. 
	 * @param file
	 * @return
	 */
	public static boolean canBeSafelyDeleted(File file) {
		if (file == null || !file.exists()) {
			return false;
		}
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				if (child.isDirectory()) {
					if (!canBeSafelyDeleted(child)) {
						return false;
					}
				}
				else {
					if (child.getName().endsWith(".pom")) {
						return false;
					}
				}
			}
		}
		if (file.getName().endsWith(".pom")) {
			return false;
		}
		return true;
	}

	/**
	 * deletes a file or directory (recursively in that case)
	 * 
	 * @param file - the file or directory to delete
	 */
	public static List<File> delete(File file) {
		List<File> files = new ArrayList<>();
		if (file == null || file.exists() == false)
			return files;
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				if (child.isDirectory()) {
					files.addAll( delete(child));
				}
				else {
					boolean deletedChildDirectory = child.delete();
					if (!deletedChildDirectory) {
						files.add(child);
					}
				}
			}
		}
		boolean deletedFile = file.delete();
		if (!deletedFile) {
			files.add(file);
		}
		return files;
	}
}
