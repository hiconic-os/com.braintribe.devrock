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
package com.braintribe.devrock.mc.core.repository.partavailability.passive;

import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.api.repository.local.ArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.api.repository.local.PartAvailabilityAccess;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.filters.AllMatchingArtifactFilterExpert;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.devrock.mc.core.resolver.BasicArtifactDataResolution;
import com.braintribe.devrock.mc.core.resolver.BasicVersionInfo;
import com.braintribe.devrock.model.mc.reason.UnresolvedPart;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.model.reason.essential.UnsupportedOperation;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.version.Version;

/**
 * a fake resolver delegate that can return a second level maven-metadata file 
 * 
 * @author pit
 *
 */
public class FakeArtifactPartResolverPersistenceDelegate implements ArtifactPartResolverPersistenceDelegate{
	
	private File firstLevelMetadatafileToDeliver;
	private File secondLevelMetadatafileToDeliver;
	private Map<EqProxy<PartIdentification>, File> payloadfilesToDeliver = new HashMap<>();
	private Duration duration;
	private Version versionToRepresent;

	/**
	 * @param firstLevelMetadatafileToDeliver - the first level metadata (the metadata overview)
	 * @param secondLevelMetadataFileToDeliver - the second level metadata (relevant for snapshots)
	 * @param payloadfilesToDeliver - {@link Map} of {@link PartIdentification} to {@link File}
	 * @param duration - the {@link Duration} 
	 */
	public FakeArtifactPartResolverPersistenceDelegate(Version version, File firstLevelMetadatafileToDeliver, File secondLevelMetadataFileToDeliver, Map<PartIdentification, File> payloadfilesToDeliver, Duration duration) {
		this.versionToRepresent = version;
		this.firstLevelMetadatafileToDeliver = firstLevelMetadatafileToDeliver;
		this.secondLevelMetadatafileToDeliver = secondLevelMetadataFileToDeliver;
		if (payloadfilesToDeliver != null) {
			for (Map.Entry<PartIdentification, File> entry : payloadfilesToDeliver.entrySet()) {
				this.payloadfilesToDeliver.put( HashComparators.partIdentification.eqProxy( entry.getKey()), entry.getValue());
			}
		}
		this.duration = duration;
	} 
	
	/**
	 * @param file - the file to turn into a resource
	 * @return
	 */
	private Resource toResource( File file) {
		Resource resource = Resource.createTransient(() -> new FileInputStream( file));
		return resource;
	}

	@Override
	public ArtifactDataResolver resolver() {		
		return new ArtifactDataResolver() {
			
			@Override
			public Maybe<ArtifactDataResolution> resolvePart(CompiledArtifactIdentification identification, PartIdentification partIdentification, Version partVersionOverride) {
				File file = payloadfilesToDeliver.get( HashComparators.partIdentification.eqProxy( partIdentification));
				if (file == null)
					return TemplateReasons.build(UnresolvedPart.T).enrich(r -> r.setPart(PartIdentification.from(partIdentification))).toMaybe();
				else {
					BasicArtifactDataResolution adr = new BasicArtifactDataResolution(toResource( file));
					return Maybe.complete( adr);
				}
			}
			
			@Override
			public Maybe<ArtifactDataResolution> resolveMetadata(CompiledArtifactIdentification identification) {
				if (secondLevelMetadatafileToDeliver == null)
					return Reasons.build(NotFound.T).toMaybe();
				
				BasicArtifactDataResolution adr = new BasicArtifactDataResolution(toResource( secondLevelMetadatafileToDeliver));
				return Maybe.complete( adr);
			}
			
			@Override
			public Maybe<ArtifactDataResolution> resolveMetadata(ArtifactIdentification identification) {				
				if (firstLevelMetadatafileToDeliver == null)
					return Reasons.build(NotFound.T).toMaybe();
				
				BasicArtifactDataResolution adr = new BasicArtifactDataResolution(toResource( firstLevelMetadatafileToDeliver));
				return Maybe.complete( adr);
			}

			@Override
			public List<VersionInfo> getVersions(ArtifactIdentification artifactIdentification) {
				return  Collections.singletonList( new BasicVersionInfo(versionToRepresent));				
			}
			
			@Override
			public Maybe<List<VersionInfo>> getVersionsReasoned(ArtifactIdentification artifactIdentification) {
				return Maybe.complete(getVersions(artifactIdentification));
			}

			@Override
			public Maybe<ArtifactDataResolution> getPartOverview( CompiledArtifactIdentification compiledArtifactIdentification) {
				return Reasons.build(UnsupportedOperation.T).text("not implemented").toMaybe();			
			}

			@Override
			public List<PartReflection> getPartsOf(CompiledArtifactIdentification compiledArtifactIdentification) {			
				return Collections.emptyList();
			}									
		};
	}

	@Override
	public Duration updateInterval() {
		return duration;
	}

	@Override
	public String repositoryId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArtifactFilterExpert artifactFilter() {	
		return AllMatchingArtifactFilterExpert.instance;
	}

	@Override
	public PartAvailabilityAccess createPartAvailabilityAccess(
			CompiledArtifactIdentification compiledArtifactIdentification, File localRepository,
			Function<File, ReadWriteLock> lockProvider,
			BiFunction<ArtifactIdentification, Version, Boolean> versionPredicate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean cacheDefaultMetadataFile() {	
		return true;
	}

	@Override
	public boolean isLocalDelegate() {		
		return false;
	}

	@Override
	public boolean isCachable() {
		return true;
	}

	@Override
	public boolean isOffline() {	
		return false;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}
	
	
}
