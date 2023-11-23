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
package com.braintribe.devrock.mc.core.declared.commons;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.devrock.mc.core.commons.VersionIntervalsEqualitySupport;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.DependencyIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.processing.core.commons.EntityHashingComparator;
import com.braintribe.model.version.Version;


/**
 * sports the most commonly {@link EntityHashingComparator} for malaclypse
 * @author pit
 *
 */
public interface HashComparators {
	
	/**
	 * a {@link EntityHashingComparator} for {@link ArtifactIdentification}
	 */
	static final HashingComparator<ArtifactIdentification> artifactIdentification = EntityHashingComparator
			.build( ArtifactIdentification.T)
			.addField( ArtifactIdentification.groupId)
			.addField( ArtifactIdentification.artifactId)
			.done();

	static final HashingComparator<ArtifactIdentification> versionlessArtifactIdentification = artifactIdentification;

	/**
	 * a {@link EntityHashingComparator} for {@link VersionedArtifactIdentification}
	 */
	static final HashingComparator<VersionedArtifactIdentification> versionedArtifactIdentification = EntityHashingComparator
			.build( VersionedArtifactIdentification.T)
			.addField( ArtifactIdentification.groupId)
			.addField( ArtifactIdentification.artifactId)
			.addField( VersionedArtifactIdentification.version)
			.done();
	
	 
	/**
	 *  a {@link EntityHashingComparator} for {@link CompiledDependencyIdentification}
	 */
	static final HashingComparator<CompiledDependencyIdentification> compiledDependencyIdentification = EntityHashingComparator
				.build( CompiledDependencyIdentification.T)
				.addField( ArtifactIdentification.groupId)
				.addField( ArtifactIdentification.artifactId)
				.addField( VersionIntervalsEqualitySupport::new)				
				.done();
	
	static final HashingComparator<CompiledDependency> compiledDependency = EntityHashingComparator
			.build( CompiledDependency.T)
			.addField( ArtifactIdentification.groupId)
			.addField( ArtifactIdentification.artifactId)
			.addField( VersionIntervalsEqualitySupport::new)				
			.addField( CompiledDependency.scope)
			.addField( CompiledDependency.classifier)
			.addField( CompiledDependency.type)
			.done();
	
	static final HashingComparator<CompiledDependency> scopelessCompiledDependency = EntityHashingComparator
			.build( CompiledDependency.T)
			.addField( ArtifactIdentification.groupId)
			.addField( ArtifactIdentification.artifactId)
			.addField( VersionIntervalsEqualitySupport::new)				
			.addField( CompiledDependency.classifier)
			.addField( CompiledDependency.type)
			.done();
	
	
	static final HashingComparator<DependencyIdentification> unversionedDeclaredDependency = EntityHashingComparator
			.build( DependencyIdentification.T)
			.addField( ArtifactIdentification.groupId)
			.addField( ArtifactIdentification.artifactId)
			.addField( PartIdentification.classifier)
			.addField( PartIdentification.type)
			.done();
	
	/**
	 * a {@link EntityHashingComparator} for {@link CompiledArtifactIdentification} that <b>INCLUDES</b> the version 
	 */
	static final HashingComparator<CompiledArtifactIdentification> compiledArtifactIdentification = EntityHashingComparator
			.build( CompiledArtifactIdentification.T)
			.addField( ArtifactIdentification.groupId)
			.addField( ArtifactIdentification.artifactId)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.major)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.minor)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.revision)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.qualifier)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.buildNumber)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.nonConform)
			.done();
	
	
	/**
	 * a {@link EntityHashingComparator} for {@link Version} 
	 */
	static final HashingComparator<Version> version = EntityHashingComparator
			.build( Version.T)			
			.addField( Version.major)
			.addField( Version.minor)
			.addField( Version.revision)
			.addField( Version.qualifier)
			.addField( Version.buildNumber)
			.addField(  Version.nonConform)
			.done();
	
	
	/**
	 * a {@link EntityHashingComparator} for {@link CompiledPartIdentification} that <b>INCLUDES</b> the version 
	 */
	static final HashingComparator<CompiledPartIdentification> compiledPartIdentification = EntityHashingComparator
			.build( CompiledPartIdentification.T)
			.addField( ArtifactIdentification.groupId)
			.addField( ArtifactIdentification.artifactId)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.major)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.minor)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.revision)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.qualifier)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.buildNumber)
			.addPropertyPathField( CompiledArtifactIdentification.version, Version.nonConform)
			.addField( PartIdentification.type)
			.addField( PartIdentification.classifier)
			.done();
	
	
	/**
	 * a {@link EntityHashingComparator} for {@link PartIdentification}
	 */
	static final HashingComparator<PartIdentification> partIdentification = EntityHashingComparator
			.build( PartIdentification.T)
			.addField( PartIdentification.type)
			.addField( PartIdentification.classifier)
			.done();

	
	/**
	 * a {@link EntityHashingComparator} for {@link AnalysisArtifact}
	 */
	static final HashingComparator<AnalysisArtifact> analysisArtifact = EntityHashingComparator
			.build(AnalysisArtifact.T)
			.addField( AnalysisArtifact.groupId)
			.addField( AnalysisArtifact.artifactId)
			.addField( AnalysisArtifact.version)
			.done();
}
