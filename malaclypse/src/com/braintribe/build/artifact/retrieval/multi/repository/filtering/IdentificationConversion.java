package com.braintribe.build.artifact.retrieval.multi.repository.filtering;

import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.version.Version;

/**
 * transpose mc-legacy's modelled artifact identifications to mc-ng's identifications 
 * @author pit
 *
 */
public class IdentificationConversion {

	/**
	 * @param identification - the old-style {@link Identification}
	 * @return - the new-style {@link ArtifactIdentification}
	 */
	public static ArtifactIdentification toArtifactIdentification( Identification identification) {
		ArtifactIdentification ai = ArtifactIdentification.T.create();
		ai.setGroupId(identification.getGroupId());
		ai.setArtifactId( identification.getArtifactId());
		return ai;
	}
	
	/**
	 * @param artifact - the old-style {@link Artifact}
	 * @return - the new-style {@link CompiledArtifactIdentification}
	 */
	public static CompiledArtifactIdentification toCompiledArtifactIdentification( Artifact artifact) {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.T.create();
		cai.setGroupId( artifact.getGroupId());
		cai.setArtifactId( artifact.getArtifactId());
		cai.setVersion( Version.parse( VersionProcessor.toString( artifact.getVersion())));
		return cai;
	}
	
	public static CompiledArtifactIdentification toCompiledArtifactIdentification( Identification identification, com.braintribe.model.artifact.version.Version version) {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.T.create();
		cai.setGroupId( identification.getGroupId());
		cai.setArtifactId( identification.getArtifactId());
		cai.setVersion( Version.parse( VersionProcessor.toString( version)));
		return cai;
	}
	
	/**
	 * @param artifact - the old-style {@link Artifact}
	 * @param tuple - the old-style {@link PartTuple}
	 * @return - the new-style {@link CompiledPartIdentification}
	 */
	public static CompiledPartIdentification toCompiledPartIdentification( Artifact artifact, PartTuple tuple) {
		CompiledPartIdentification cpi = CompiledPartIdentification.T.create();
		cpi.setGroupId( artifact.getGroupId());
		cpi.setArtifactId( artifact.getArtifactId());
		cpi.setVersion( Version.parse( VersionProcessor.toString( artifact.getVersion())));
		cpi.setClassifier( tuple.getClassifier());
		cpi.setType( tuple.getType());
		return cpi;
	}
}
