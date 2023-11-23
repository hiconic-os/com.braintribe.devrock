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
package com.braintribe.devrock.mc.api.commons;

import java.util.function.Supplier;

import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.provider.Holder;
import com.braintribe.utils.paths.UniversalPath;

/**
 * a builder to create names of artifact directories, files etc and so on et al 
 * @author pit / dirk
 *
 */
public interface ArtifactAddressBuilder extends ArtifactAddress {

	/**
	 * @return - a new instance of the {@link ArtifactAddressBuilder}
	 */
	static ArtifactAddressBuilder build() {
		return new BasicArtifactDataPathBuilder();
	}
	/**
	 * @param root - the root (or prefix) for the address
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder root( String root);
	
	/**
	 * @param hashType - the hash type (such as 'MD5' or 'SHA1' or 'ASC'
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder metaExt(String hashType);
	
	/**
	 * @param part - the {@link PartIdentification} that makeup the file name part 
	 * @return
	 */
	ArtifactAddressBuilder part(PartIdentification part);
	
	/**
	 * @param part - the {@link PartIdentification} that makeup the file name part
	 *  
	 * @return
	 */
	ArtifactAddressBuilder part(PartIdentification part, Version partVersionOverride);
	
	/**
	 * @param part - the {@link PartIdentification} that makeup the file name part 
	 * @return
	 */
	ArtifactAddressBuilder part(PartIdentification part, String partVersionOverride);
	
	
	/**
	 * @param file - sets the file name to whatever passed here 
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder file(String file);
	/**
	 * sets the file name to <b>maven-metadata-'repo'.xml</b>
	 * @param repo - the ID of the repository to name the metadata file (local style)
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder metaData(String repo);
	/**
	 * sets the file name to <b>maven-metadata.xml</b>
	 * @return - the {@link ArtifactAddressBuilder} itself 
	 */
	ArtifactAddressBuilder metaData();
	
	/**
	 * sets the file name to <b>part-available-'repo'.txt</b>
	 * @param repo - the ID of the repository to name the part-availability file (local style)
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder partAvailability(String repo);
	
	ArtifactAddressBuilder partAvailability(String repo, String extension);
	/**
	 * @param identification - a {@link CompiledArtifactIdentification}
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder compiledArtifact(CompiledArtifactIdentification identification);
	/**
	 * @param identification - a {@link VersionedArtifactIdentification}
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder versionedArtifact(VersionedArtifactIdentification identification);
	/**
	 * @param identification - a {@link ArtifactIdentification}
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder artifact(ArtifactIdentification identification);
	/**
	 * @param version - a {@link Version}
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder version(Version version);
	/**
	 * @param version - a String 
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder version(String version);
	
	/**
	 * @param artifactId
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder artifactId(String artifactId);
	/**
	 * @param groupId
	 * @return - the {@link ArtifactAddressBuilder} itself
	 */
	ArtifactAddressBuilder groupId(String groupId);
	
	/**
	 * @return - an {@link UniversalPath} built from the data contained
	 */
	UniversalPath toPath();
			
}

/**
 * 
 * @author pit/dirk
 *
 */
class BasicArtifactDataPathBuilder implements ArtifactAddressBuilder {
	private String root;
	private String groupId;
	private String artifactId;
	private String version;	
	
	private Supplier<String> fileName;
	private String metaExt;
	
	
	public ArtifactAddressBuilder root( String root) {
		this.root = root;
		return this;
	}
	
	@Override
	public ArtifactAddressBuilder groupId( String groupId) {
		this.groupId = groupId;
		return this;
	}
	
	@Override
	public ArtifactAddressBuilder artifactId( String artifactId) {
		this.artifactId = artifactId;
		return this;
	}
	
	@Override
	public ArtifactAddressBuilder version( String version) {
		this.version = version;
		return this;
	}
	@Override
	public ArtifactAddressBuilder version( Version version) {
		this.version = version.asString();
		return this;
	}

	@Override
	public ArtifactAddressBuilder artifact( ArtifactIdentification identification) {
		this.groupId = identification.getGroupId();
		this.artifactId = identification.getArtifactId();		
		return this;
	}
	@Override
	public ArtifactAddressBuilder versionedArtifact( VersionedArtifactIdentification identification) {
		artifact(  identification);
		this.version = identification.getVersion();
		return this;
	}
	
	@Override
	public ArtifactAddressBuilder compiledArtifact( CompiledArtifactIdentification identification) {
		artifact(  identification);
		this.version = identification.getVersion().asString();
		return this;
	}
	
	@Override
	public ArtifactAddressBuilder metaData() {		
		this.fileName = new Holder<>("maven-metadata.xml");
		return this;
	}
	
	@Override
	public ArtifactAddressBuilder metaData(String repo) {
		this.fileName = new Holder<>("maven-metadata-" + repo + ".xml");
		return this;
	}
	
	@Override
	public ArtifactAddressBuilder partAvailability(String repo) {
		return partAvailability(repo, "txt");
	}
	
	@Override
	public ArtifactAddressBuilder partAvailability(String repo, String extension) {
		this.fileName = new Holder<>("part-availability-" + repo + "." + extension);
		return this;
	}
	
	
	@Override
	public ArtifactAddressBuilder file(String file) {
		this.fileName = new Holder<>(file);
		return this;
	}
	
	

	@Override
	public ArtifactAddressBuilder part(PartIdentification part, Version partVersionOverride) {
		return part( part, partVersionOverride != null ? partVersionOverride.asString() : null);
	}

	@Override
	public ArtifactAddressBuilder part(PartIdentification part) {		
		return part( part, (String) null);
	}

	@Override
	public ArtifactAddressBuilder part( PartIdentification part, String partVersionOverride) {		
		this.fileName = () -> {
			StringBuilder sb = new StringBuilder();
			if (artifactId == null) {
				throw new IllegalStateException( "artifactId required when addressing part");
			}
			sb.append( artifactId);
			String partVersion = version;
			if (partVersionOverride != null)
				partVersion = partVersionOverride;
			
			if (partVersion == null) {				
				throw new IllegalStateException( "version required when addressing part");
			}
			sb.append( '-');		
			sb.append( partVersion);
			
			if (part.getClassifier() != null) {
				sb.append( '-');
				sb.append( part.getClassifier());
			}
			if (part.getType() != null) {
				sb.append( '.');
				sb.append( part.getType());
			}
			return sb.toString();
		};
		return this;
	}
	
	@Override
	public ArtifactAddressBuilder metaExt(String hashType) {
		this.metaExt = hashType;
		return this;
	}

	@Override
	public UniversalPath toPath() {
		UniversalPath path = UniversalPath.empty();
		if (root != null)
			path = path.push(root);
		if (groupId != null) 
			path = path.push(groupId, ".");
		if (artifactId != null)
			path = path.push( artifactId);
		if (version != null)
			path = path.push( version);
		
		if (fileName != null) {			
			String element = fileName.get();
			if (metaExt != null) {
				element = element + "." + metaExt;
			}
			path = path.push( element);			
		}
		return path;
	}

	@Override
	public String getRoot() {
		return root;
	}

	@Override
	public String getGroupId() {		
		return groupId;
	}

	@Override
	public String getArtifactId() {
		return artifactId;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getFileName() {
		if (fileName != null)
			return fileName.get();
		return null;
	}

	@Override
	public String getMetaExt() {
		return metaExt;
	}
	
	
	
}
