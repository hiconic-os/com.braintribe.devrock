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
package com.braintribe.test.multi.updatePolicyLab;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.cache.SolutionWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.coding.IdentificationWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.MavenMetadataPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.test.multi.MetadataValidationVisitor;
import com.braintribe.test.multi.updatePolicyLab.MetaDataValidationExpectation.ValidTimestamp;

public class CommonMetadataValidationVisitor implements MetadataValidationVisitor {
	
	protected List<MetaDataValidationExpectation> expectations;
	protected Map<MetaDataValidationExpectation, MetadataValidationResult> results = new HashMap<>();
	protected String[] relevantRepositoryIds;
	
	protected Map<Identification, MetaDataValidationExpectation> unversionedArtifactToExpectation = CodingMap.createHashMapBased( new IdentificationWrapperCodec());
	protected Map<Solution, MetaDataValidationExpectation> versionedArtifactToExpectation = CodingMap.createHashMapBased( new SolutionWrapperCodec());
	
	protected Date before;
	protected Date after;
	
	protected LockFactory lockFactory = new FilesystemSemaphoreLockFactory();
	
	@Configurable @Required
	public void setExpectations(List<MetaDataValidationExpectation> expectations) {
		this.expectations = expectations;
		
		// build maps 
		for (MetaDataValidationExpectation expectation : this.expectations) {
			Solution artifact = NameParser.parseCondensedSolutionName( expectation.name);
			unversionedArtifactToExpectation.put(artifact, expectation);
			versionedArtifactToExpectation.put( artifact, expectation);
		}
	}
	
	@Configurable @Required
	public void setRelevantRepositoryIds(String [] relevantRepositoryIds) {
		this.relevantRepositoryIds = relevantRepositoryIds;
	}
	
	
	@Override
	public String[] relevantRepositoryIds() {
		return relevantRepositoryIds;
	}

	public Collection<MetadataValidationResult> getResults() {
		return results.values();
	}
	
	@Configurable @Required
	public void setBefore(Date before) {
		this.before = before;
	}
	
	@Configurable @Required
	public void setAfter(Date after) {
		this.after = after;
	}
	
	protected MavenMetaData getMetadata( File file) throws RepositoryPersistenceException {
		return MavenMetadataPersistenceExpert.decode(lockFactory, file);
	}

	
	private class Tuple {
		public boolean valid;
		public String reason;
	}

	private Tuple validateFile(File metadatafile, ValidTimestamp timestap) {
		Tuple tuple = new Tuple();
		Date modified = new Date(metadatafile.lastModified());
		
		
		switch (timestap)  {
			case within : {
				
				if (
						(modified.before(after) || modified.compareTo(after) == 0) && 
						(modified.after( before) || modified.compareTo( before) == 0)
					) {
					tuple.valid = true;
					tuple.reason = "modified value [" + modified.getTime() + "] is within [" + before.getTime() + "," + after.getTime() + "]";
				}
				else {
					tuple.valid = false;
					tuple.reason = "modified value [" + modified.getTime() + "] is not within [" + before.getTime() + "," + after.getTime() + "]"; 
				}
				return tuple;
			}
			case after:
				if (modified.after(before) || modified.compareTo(before) == 0) {
					tuple.valid = true;
					tuple.reason = "modified value [" + modified.getTime() + "] is after [" + before.getTime() + "]";
				}	
				else {
					tuple.valid = false;
					tuple.reason = "modified value [" + modified.getTime() + "] is not after [" + before.getTime() + "]";
				}
				return tuple;
			case before:
				if (modified.before(after) || modified.compareTo( after) == 0)  {
					tuple.valid = true;
					tuple.reason = "modified value [" + modified.getTime() + "] is before [" + after.getTime() + "]";
				}
				else {
					tuple.valid = false;
					tuple.reason = "modified value [" + modified.getTime() + "] is not before [" + after.getTime() + "]";
				}
				return tuple;				
			default:
				throw new IllegalStateException();
			}		
	}

	@Override
	public void visitVersionedArtifactMetadata(Solution versionedArtifact, File metadatafile) {
		MetaDataValidationExpectation expectation = versionedArtifactToExpectation.get(versionedArtifact);
		if (expectation != null) {
			if (expectation != null) {
				MetadataValidationResult result = results.get(expectation);
				if (result == null) {
					result = new MetadataValidationResult();
					results.put(expectation, result);
				}
				Tuple tuple = validateFile(metadatafile, expectation.versionedTimestampInterpretation);
				result.name = expectation.name;
				result.versionedArtifactIsValid = tuple.valid;
				result.versionedFailReason = tuple.reason;
			}
		}
	}
	
	@Override
	public void visitUnversionedArtifactMetadata(Identification unversionedArtifact, File metadatafile) {
		MetaDataValidationExpectation expectation = unversionedArtifactToExpectation.get(unversionedArtifact);
		if (expectation != null) {
			
			MetadataValidationResult result = results.get(expectation);
			if (result == null) {
				result = new MetadataValidationResult();
				results.put(expectation, result);
			}
			Tuple tuple = validateFile(metadatafile, expectation.unversionedTimestampInterpretation);
			result.name = expectation.name;
			result.unversionedArtifactIsValid = tuple.valid;
			result.unversionedFailReason= tuple.reason;
			
		}
		// here you'd call some code to validate the contents 
		
	}
	
	
	
	
}
