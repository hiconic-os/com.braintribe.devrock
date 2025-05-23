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


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.api.repository.local.ArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.api.repository.local.PartAvailability;
import com.braintribe.devrock.mc.api.repository.local.PartAvailabilityAccess;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.core.commons.Downloads;
import com.braintribe.devrock.mc.core.commons.FileCommons;
import com.braintribe.devrock.mc.core.commons.PartReflectionCommons;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.model.mc.reason.InaccessiblePart;
import com.braintribe.devrock.model.mc.reason.MetaDataDownloadFailed;
import com.braintribe.devrock.model.mc.reason.PartReflectionFailure;
import com.braintribe.devrock.model.mc.reason.UnaccessibleArtifactVersions;
import com.braintribe.devrock.model.mc.reason.UnresolvedPart;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.logging.Logger;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.version.Version;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.IOTools;
import com.braintribe.utils.lcd.LazyInitialized;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * the *beast* - the nexus of the resolving
 * 
 * @author pit/dirk
 *
 */
public class LocalRepositoryCachingArtifactResolver implements ReflectedArtifactResolver, LifecycleAware {
	private static final Logger logger = Logger.getLogger(LocalRepositoryCachingArtifactResolver.class);
	public static final int MAX_THREADS = 20;
	private final List<ArtifactPartResolverPersistenceDelegate> delegates = new ArrayList<>();
	private final LoadingCache<EqProxy<ArtifactIdentification>, Maybe<List<VersionInfo>>> versionsCache;
	
	private final LoadingCache<EqProxy<CompiledPartIdentification>, Maybe<ArtifactDataResolution>> resolutionCache;
	private final LoadingCache<EqProxy<CompiledArtifactIdentification>, List<PartAvailabilityAccess>> partAvailabilityAccessCache;
	private final LoadingCache<EqProxy<ArtifactIdentification>, Set<EqProxy<Version>>> localVersionsCache;
	private static 	DeclaredMavenMetaDataMarshaller metadataMarshaller = new DeclaredMavenMetaDataMarshaller();
	
	private File localRepository;
	private Function<File,ReadWriteLock> lockProvider;
	private ExecutorService es;
	private int maxThreads = MAX_THREADS;
	
	public LocalRepositoryCachingArtifactResolver() {
		versionsCache = Caffeine.newBuilder().build( this::loadVersions);		
		resolutionCache = Caffeine.newBuilder().build( this::loadPartData);
		localVersionsCache = Caffeine.newBuilder().build( this::loadLocalMetaData);
		partAvailabilityAccessCache = Caffeine.newBuilder().build( this::loadPartAvailabilityAccesses);
	}

	@Configurable
	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}
	
	/**
	 * @param root - the {@link File} pointing to the root of the local repository
	 */
	@Configurable @Required
	public void setLocalRepository(File root) {
		this.localRepository = root;
	}

	@Configurable @Required
	public void setLockProvider(Function<File, ReadWriteLock> lockProvider) {
		this.lockProvider = lockProvider;
	}
	
	/**
	 * @param delegates - the {@link ArtifactPartResolverPersistenceDelegate} that reflect all repositories 
	 */
	@Configurable  @Required
	public void setDelegates(List<ArtifactPartResolverPersistenceDelegate> delegates) {			
		this.delegates.addAll(delegates);
	}
	
	@Override
	public void postConstruct() {
		es = Executors.newFixedThreadPool( Math.max( delegates.size(), maxThreads));		
	}

	@Override
	public void preDestroy() {
		if (es != null) {
			es.shutdown();
		}
	}
	
	/**
	 * function to test whether a local version exists for the artifact 
	 * @param artifactIdentification the {@link ArtifactIdentification} 
	 * @param version - the {@link Version} to look for
	 * @return - true if exists locally, false otherwise
	 */
	private boolean doesLocalVersionExist( ArtifactIdentification artifactIdentification, Version version) {
		EqProxy<ArtifactIdentification> aiKey = HashComparators.artifactIdentification.eqProxy(artifactIdentification);
		EqProxy<Version> versionKey = HashComparators.version.eqProxy( version);				
		return localVersionsCache.get(aiKey).contains(versionKey);		
	}


	/**
	 * loader function for {@link PartAvailabilityAccess}
	 * @param eqProxy - the {@link EqProxy} of a {@link CompiledArtifactIdentification}
	 * @return - a {@link List} of created {@link PartAvailabilityAccess} that can handle the artifact's parts
	 */
	private List<PartAvailabilityAccess> loadPartAvailabilityAccesses( EqProxy<CompiledArtifactIdentification> eqProxy) {
			
		List<PartAvailabilityAccess> accesses = new ArrayList<>( delegates.size());
				
		CompiledArtifactIdentification cai = eqProxy.get();
		for (ArtifactPartResolverPersistenceDelegate delegate : delegates) {
			// if delegation entry is not relevant for this artifact, skip
			if (!delegate.artifactFilter().matches( cai)) {
				continue;
			}
			accesses.add( delegate.createPartAvailabilityAccess(cai, localRepository, lockProvider, this::doesLocalVersionExist));			
		}
		return accesses;	
	}
	
	private <T> Future<T> submit(Callable<T> callable) {
		AttributeContext ac = AttributeContexts.peek();
		
		Future<T> future = es.submit(() -> {
			AttributeContexts.push(ac);
			try {
				return callable.call();
			}
			finally {
				AttributeContexts.pop();
			}
		});
		
		return future;
	}
	
	private record EffectiveDelegates(List<ArtifactPartResolverPersistenceDelegate> dominantDelegates, List<ArtifactPartResolverPersistenceDelegate> normalDelegates) {}
	
	private EffectiveDelegates determineEffectiveDelegates(ArtifactIdentification artifactIdentification) {
		List<ArtifactPartResolverPersistenceDelegate> dominantDelegates = new LinkedList<>();
		List<ArtifactPartResolverPersistenceDelegate> normalDelegates = new LinkedList<>();
		
		for (ArtifactPartResolverPersistenceDelegate delegate: delegates) {
			// the delegate's not relevant for this artifact, skip it
			if (!delegate.artifactFilter().matches(artifactIdentification)) { 
				continue;
			}		

			if (delegate.repositoryDominanceFilter().matches(artifactIdentification)) {
				dominantDelegates.add(delegate);
			}
			else {
				normalDelegates.add(delegate);
			}
		}
		
		return new EffectiveDelegates(dominantDelegates, normalDelegates);
	}
	
	/**
	 * loader function for Caffeine cache  of the VersionInfo
	 * @param eqProxy - the {@link EqProxy} that wraps the {@link ArtifactIdentification}
	 */
	private Maybe<List<VersionInfo>> loadVersions( EqProxy<ArtifactIdentification> eqProxy) {
				
		// basic idea is that only the maven-metadata (source of the versions) of the dominant repository are really instantly resolved
		// and recessive repositories (any non-dominant repository) are only resolved if no dominant repository was able to answer
		// 
		ArtifactIdentification artifactIdentification = eqProxy.get();
		
		EffectiveDelegates effectiveDelegates = determineEffectiveDelegates(artifactIdentification);
		
		for (ArtifactPartResolverPersistenceDelegate delegate: effectiveDelegates.dominantDelegates()) {
			Maybe<List<VersionInfo>> versionsMaybe = acquireVersionInfo(artifactIdentification, delegate);
			
			if (versionsMaybe.isUnsatisfied())
				return TemplateReasons.build(UnaccessibleArtifactVersions.T) //
					.assign(UnaccessibleArtifactVersions::setArtifact, artifactIdentification) //
					.cause(versionsMaybe.whyUnsatisfied()) //
					.toMaybe();
			
			List<VersionInfo> versions = versionsMaybe.get();
			
			if (!versions.isEmpty())
				return versionsMaybe;
		}
		
		List<Pair<Future<Maybe<List<VersionInfo>>>, Integer>> futures = new ArrayList<>(delegates.size());
		
		for (ArtifactPartResolverPersistenceDelegate delegate: effectiveDelegates.normalDelegates()) {
			Future<Maybe<List<VersionInfo>>> future = submit(() -> acquireVersionInfo(artifactIdentification, delegate));
			futures.add(Pair.of(future, null));
		}
		
		ExceptionCollector exceptionCollector = new ExceptionCollector("Error while resolving maven-metadata for: " + artifactIdentification.asString());
		
		var lazyReason = new LazyInitialized<UnaccessibleArtifactVersions>(() -> {
			return TemplateReasons.build(UnaccessibleArtifactVersions.T) //
					.assign(UnaccessibleArtifactVersions::setArtifact, artifactIdentification) //
					.toReason();
		}); 
		
		// create a merged view on the avaiable versions of the recessive repos
		Map<EqProxy<Version>, BasicVersionInfo> map = new HashMap<>();
		
		for (Pair<Future<Maybe<List<VersionInfo>>>, Integer> futureAndDominance: futures) {
			try {
				Future<Maybe<List<VersionInfo>>> future = futureAndDominance.first();
				Maybe<List<VersionInfo>> versionInfosMaybe = future.get(); // get and eventually wait for retrieval
				
				if (versionInfosMaybe.isUnsatisfied()) {
					lazyReason.get().getReasons().add(versionInfosMaybe.whyUnsatisfied());
					continue;
				}
				
				List<VersionInfo> versionInfos = versionInfosMaybe.get();
				
				if (versionInfos.isEmpty())
					continue;
				
				for (VersionInfo versionInfo: versionInfos) {
					Version version = versionInfo.version();
					BasicVersionInfo mergedVersionInfo = map.computeIfAbsent( HashComparators.version.eqProxy(version), k -> new BasicVersionInfo(version));

					for (String repositoryId: versionInfo.repositoryIds())
						mergedVersionInfo.add(repositoryId);							
				}

			} catch (Exception e) {
				exceptionCollector.collect(e);
			}
		}

		exceptionCollector.throwIfNotEmpty();
		
		if (lazyReason.isInitialized())
			return lazyReason.get().asMaybe();
		
		// return a ascending sorted list
		List<VersionInfo> infos = new ArrayList<>( map.values());
		Collections.sort(infos);

		return Maybe.complete(infos);
	}
	

	/**
	 * simple class to collect exceptions during a process 
	 * @author pit / dirk
	 *
	 */
	private class ExceptionCollector {
		private List<Throwable> exceptions;
		private final String message;
		
		public ExceptionCollector(String message) {
			super();
			this.message = message;
		}

		public void collect(Throwable e) {
			if (exceptions == null)
				exceptions = new LinkedList<>();
			
			exceptions.add(e);
		}
		
		public void throwIfNotEmpty() {
			int size = exceptions != null? exceptions.size(): 0;
			
			switch (size) {
			case 0:
				return;
			case 1:
				throw Exceptions.unchecked(exceptions.get(0), message);
			default:
				RuntimeException e = new RuntimeException(message);
				exceptions.forEach(e::addSuppressed);
				throw e;
			}
		}
	}
	
	private Maybe<List<VersionInfo>> acquireVersionInfo(ArtifactIdentification artifactIdentification, ArtifactPartResolverPersistenceDelegate delegate) {
		Maybe<List<VersionInfo>> unfilteredResultMaybe = acquireUnfilteredVersionInfo(artifactIdentification, delegate);
		
		if (unfilteredResultMaybe.isUnsatisfied())
			return unfilteredResultMaybe.whyUnsatisfied().asMaybe();
		
		List<VersionInfo> unfilteredResult = unfilteredResultMaybe.get();
		
		return Maybe.complete(unfilteredResult.stream() //
				.filter(version -> isVersionReallyAvailable(version, artifactIdentification, delegate)) //
				.collect(Collectors.toList()));
	}

	// TODO this logic (filtering versions available for given delegate based on the delegate's filtere) is only tested by jinni tests, but not MC tests
	private boolean isVersionReallyAvailable(VersionInfo version, ArtifactIdentification artId, ArtifactPartResolverPersistenceDelegate delegate) {
		return delegate.artifactFilter().matches(CompiledArtifactIdentification.from(artId, version.version()));
	}

	private Maybe<List<VersionInfo>> acquireUnfilteredVersionInfo(ArtifactIdentification artifactIdentification, ArtifactPartResolverPersistenceDelegate delegate) {
		if (delegate.isCachable()) {
			Duration duration = delegate.updateInterval();
			MavenMetaData metadata = ensureMetaData(artifactIdentification, metadataMarshaller, delegate, duration);
			// construct the overview entry
			if (metadata != null) {
				List<VersionInfo> versionInfos = new ArrayList<>();
				
				Versioning versioning = metadata.getVersioning();
				
				if (versioning != null) {
					for (Version version : versioning.getVersions()) {
						BasicVersionInfo versionInfo = new BasicVersionInfo(version);
						versionInfo.add( delegate.repositoryId());
						versionInfos.add(versionInfo);
					}
				}
				
				return Maybe.complete(versionInfos);
			}
		}
		else {
			return delegate.resolver().getVersionsReasoned(artifactIdentification);
		}
		
		return Maybe.complete(Collections.emptyList());
	}
	
	/**
	 * make sure that a maven metadata file is correctly present, updated et al 
	 */
	private MavenMetaData ensureMetaData(ArtifactIdentification artifactIdentification, DeclaredMavenMetaDataMarshaller metadataMarshaller,
			ArtifactPartResolverPersistenceDelegate delegate, Duration duration) {
		logger.trace("ensuring metadata for " + ArtifactIdentification.asString(artifactIdentification) + " for " + delegate.repositoryId());

		File metadataFile = ArtifactAddressBuilder.build().root( localRepository.getAbsolutePath()).artifact(artifactIdentification).metaData( delegate.repositoryId()).toPath().toFile();
		
		ReadWriteLock lock = lockProvider.apply(metadataFile);
		
		// TODO : eventually update code to two stage locks : readlock while reading, write lock while writing
		Lock writeLock = lock.writeLock();
		
		writeLock.lock();
		
		try {
			boolean requiresUpdate = false;			
			if (!delegate.isLocalDelegate()) {
				// only check if update is required if delegate is not the 'local one'
				if (metadataFile.exists()) {
					logger.trace("metadata exists for " + ArtifactIdentification.asString(artifactIdentification) + " from " + delegate.repositoryId());
					requiresUpdate = FileCommons.requiresUpdate(metadataFile, duration, delegate.isDynamic());
				}
				else {			
					requiresUpdate = true;
				}
			}
			
			
			if (requiresUpdate) {				
				logger.trace("requiring update for metadata of " + ArtifactIdentification.asString(artifactIdentification) + " from " + delegate.repositoryId());
				// download and read at the same time..
				boolean downloaded = false;
				Reason error = null;
				
				Maybe<ArtifactDataResolution> metadataOptional = delegate.resolver().resolveMetadata(artifactIdentification);
				
				// there's a new metadata from remote 
				if (metadataOptional.isSatisfied()) {
					ArtifactDataResolution resolution = metadataOptional.get();
					
					logger.trace("downloading " + ArtifactIdentification.asString(artifactIdentification) + " metadata from " + resolution.repositoryId());
					
					Reason downloadFailure = Downloads.downloadReasoned(metadataFile, resolution::openStream);

					if (downloadFailure == null) {
						downloaded = true;
					}
					else {
						if (!(downloadFailure instanceof NotFound)) {
							error = downloadFailure;
						}
					}
					
				} else {
					Reason whyUnsatisfied = metadataOptional.whyUnsatisfied();
					
					if (!(whyUnsatisfied instanceof NotFound)) {
						error = whyUnsatisfied;
					}
				}
				
				if (error != null) {
					throw new ReasonException( //
							TemplateReasons.build(MetaDataDownloadFailed.T) //
							.assign(MetaDataDownloadFailed::setTargetPath, metadataFile.getAbsolutePath())//
							//.text("cannot update locally stored maven-metadata file  [" + metadataFile.getAbsolutePath() + "]") //
							.cause(error)
							.toReason());
				}
				
				if (downloaded) {
					// TODO: why this?
					// if successfully downloaded, delete the marker if there's one
					FileCommons.removeMarkerFile(lockProvider, metadataFile);				
				}
				// if we had to update, but couldn't, we just load the outdated file if it's there 
				if (!downloaded && !metadataFile.exists()) {					
					MavenMetaData metadata = createEmptyMetadata(artifactIdentification);
					// if we're told to store the 'empty' metadata, do so
					if (delegate.cacheDefaultMetadataFile()) {
						logger.trace("create empty metadata file for " + ArtifactIdentification.asString(artifactIdentification) + " from " + delegate.repositoryId());
						try (FileOutputStream out = new FileOutputStream(metadataFile)) {
							metadataMarshaller.marshall(out, metadata);				
						} catch (Exception e) {
							String msg = "cannot write default maven-metadata file  [" + metadataFile.getAbsolutePath() + "]";
							throw new IllegalStateException(msg);				
						}					
					}
					return metadata;
				}
			}
			if (metadataFile.exists()) {
				// no update, just read the file
				try ( InputStream in = new BufferedInputStream(new FileInputStream(metadataFile), IOTools.SIZE_64K)) {
					return (MavenMetaData) metadataMarshaller.unmarshall(in);
				}
				catch (Exception e) {
					String msg = "cannot read maven-metadata file  [" + metadataFile.getAbsolutePath() + "]";
					throw new IllegalStateException(msg);
				}						
			}
			else {
				// no local file -> return empty metadata
				MavenMetaData metadata = createEmptyMetadata(artifactIdentification);				
				return metadata;
			}
		}		
		finally {
			writeLock.unlock();
		}				
	}

	private MavenMetaData createEmptyMetadata(ArtifactIdentification artifactIdentification) {
		MavenMetaData metadata = MavenMetaData.T.create();
		metadata.setGroupId( artifactIdentification.getGroupId());
		metadata.setArtifactId( artifactIdentification.getArtifactId());
		return metadata;
	}
	
	/**
	 * loader function for {@link ArtifactDataResolution}
	 * @param partIdentification - an {@link EqProxy} of the {@link CompiledPartIdentification}
	 * @return - an {@link Optional} with the {@link ArtifactDataResolution} or an empty one
	 */
	private Maybe<ArtifactDataResolution> loadPartData( EqProxy<CompiledPartIdentification> partIdentification) {
		
		CompiledPartIdentification cpi = partIdentification.get();
		
		List<PartAvailabilityAccess> partAvailabilityAccesses = partAvailabilityAccessCache.get( HashComparators.compiledArtifactIdentification.eqProxy(cpi));
		
		LazyInitialized<Reason> availabilityProblem = new LazyInitialized<>(() -> TemplateReasons.build(UnresolvedPart.T).assign( UnresolvedPart::setPart, cpi).toReason());
		
		// TODO : potential for parallel resolving on UNKNOWN 
		for (PartAvailabilityAccess pa : partAvailabilityAccesses) {
			switch ( pa.getAvailability(cpi)) {
				case available: {
					return ensureExistingPart( pa, cpi);
				}
				case unavailable: 
					continue;					
				case unknown: 
					Maybe<ArtifactDataResolution> candidate = tryUnknownPart( pa, cpi);
					if (candidate.isSatisfied()) {
						return candidate;
					}
					else if (candidate.isUnsatisfiedBy(NotFound.T)) {
						continue;
					}
					else {
						availabilityProblem.get().getReasons().add(
							TemplateReasons.build(InaccessiblePart.T) //
								.assign( InaccessiblePart::setPart, cpi) //
								.assign( InaccessiblePart::setRepositoryId,  pa.repository().getName())
								//.text("Unaccessible part [" + PartIdentification.asString(cpi) + "] for repo " + pa.repository().getName()) //
								.cause(candidate.whyUnsatisfied()) //
								.toReason()
						);
						break;
					}
				default:
					break;				
			}			
		}
		
		if (availabilityProblem.isInitialized())
			return availabilityProblem.get().asMaybe();
		
				
		return TemplateReasons.build(UnresolvedPart.T)
								.enrich(r -> r.setPart(PartIdentification.from(cpi)))
								.enrich(r -> r.setArtifact(CompiledArtifactIdentification.from(cpi)))
								.toMaybe();							
	}

	/**
	 * loader functions for local maven metadata
	 */
	private Set<EqProxy<Version>> loadLocalMetaData( EqProxy<ArtifactIdentification> eqProxy) {
		ArtifactIdentification ai = eqProxy.get();
		File file = ArtifactAddressBuilder.build().root( localRepository.getAbsolutePath()).artifact(ai).metaData("local").toPath().toFile();

		ReadWriteLock lock = lockProvider.apply(file);
		Lock readLock = lock.readLock();
		readLock.lock();

		try {
			if (file.exists()) {
				Set<EqProxy<Version>> versions = new HashSet<>(); 
				try (InputStream in = new FileInputStream( file)){
					MavenMetaData md = (MavenMetaData) metadataMarshaller.unmarshall(in);
					Versioning versioning = md.getVersioning();
					if (versioning == null) {
						return Collections.emptySet();
					}
					for (Version version : versioning.getVersions()) {
						versions.add( HashComparators.version.eqProxy(version));
					}
					return versions;
				}
				catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
			else {
				return Collections.emptySet();
			}
		}
		finally {
			readLock.unlock();
		}
	}
	

	/**
	 * called if a part is marked as 'unknown' 
	 * @param pa - the corresponding {@link PartAvailabilityAccess} to 
	 * @param cpi - the {@link CompiledPartIdentification}
	 * @return - 
	 */
	private Maybe<ArtifactDataResolution> tryUnknownPart(PartAvailabilityAccess pa, CompiledPartIdentification cpi) {		
		File part = ArtifactAddressBuilder.build().root(localRepository.getAbsolutePath()).compiledArtifact(cpi).part(cpi, pa.getActualVersion()).toPath().toFile();

		Maybe<ArtifactDataResolution> optional = pa.repoDelegate().resolver().resolvePart(cpi, cpi, pa.getActualVersion());
		if (!optional.isSatisfied()) {
			pa.setAvailablity(cpi, PartAvailability.unavailable);
			return optional;
		}
		
		ArtifactDataResolution resolve = optional.get();
		if (part.exists()) {
			if (!resolve.isBacked()) {
				pa.setAvailablity(cpi, PartAvailability.unavailable);
				return Reasons.build(NotFound.T).toMaybe();
			}
		}
		else {
			try {
				Reason reason = downloadPart(cpi, part, resolve);
 
				if (reason != null) {
					if (reason instanceof NotFound) {
						pa.setAvailablity(cpi, PartAvailability.unavailable);
					}
					
					return reason.asMaybe();	
				}
			}
			catch (Exception e) {
				return InternalError.from(e, "could not determine existence of " + cpi.asString()).asMaybe();
			}
		}
		
		pa.setAvailablity(cpi, PartAvailability.available);
		FileResource resource = FileResource.T.create();
		resource.setPath( part.getAbsolutePath());
		resource.setName( part.getName());						
		BasicArtifactDataResolution basicArtifactDataResolution = new BasicArtifactDataResolution(resource);
		basicArtifactDataResolution.setRepositoryId(pa.repository().getName());
		return Maybe.complete(basicArtifactDataResolution);		
	}

	private Maybe<ArtifactDataResolution> ensureExistingPart(PartAvailabilityAccess pa, CompiledPartIdentification cpi) {
		Version actualVersion = pa.getActualVersion();	
		if (!pa.repoDelegate().isCachable()) {
			return pa.repoDelegate().resolver().resolvePart(cpi, cpi, actualVersion);
		}
		
		File part = ArtifactAddressBuilder.build().root(localRepository.getAbsolutePath()).compiledArtifact(cpi).part(cpi, pa.getActualVersion()).toPath().toFile();
		if (!part.exists()) {	
			// versions can be used.. CAI version : 1.0.1-SNAPSHOT, CPI version 1.0.1-20210702.145253-10815
			Maybe<ArtifactDataResolution> optional = pa.repoDelegate().resolver().resolvePart(cpi, cpi, actualVersion);
			if (!optional.isSatisfied())  {
				if (!pa.repository().getOffline()) {					
					String msg = "cannot resolve part [" + cpi.asString() + "] (actual part version : " + actualVersion.asString() + ") repository [" + pa.repoDelegate().repositoryId() + "]";
					throw new IllegalStateException(msg);
				}
				else {
					return TemplateReasons.build(UnresolvedPart.T)
											.enrich(r -> r.setPart(PartIdentification.from(cpi)))
											.enrich(r -> r.setArtifact(CompiledArtifactIdentification.from(cpi)))
											.toMaybe();
				}
			}
			ArtifactDataResolution resolve = optional.get();
			Reason reason = downloadPart(cpi, part, resolve);
			if (reason != null) {
				return Maybe.empty(reason);
			}
		
		}
		
		FileResource resource = FileResource.T.create();
		resource.setPath( part.getAbsolutePath());
		resource.setName( part.getName());						
		resource.setFileSize(part.length());
		
		BasicArtifactDataResolution basicArtifactDataResolution = new BasicArtifactDataResolution(resource);
		basicArtifactDataResolution.setRepositoryId(pa.repository().getName());
		return Maybe.complete(basicArtifactDataResolution);

	}
	
	private Reason downloadPart(CompiledPartIdentification cdi, File part, ArtifactDataResolution artifactDataResolution) {
		logger.trace("downloading " + cdi.asString() + " from " + artifactDataResolution.repositoryId());
		return Downloads.downloadLocked(cdi, artifactDataResolution.repositoryId(), part, artifactDataResolution::openStream, lockProvider);
	}

	@Override
	public Maybe<ArtifactDataResolution> resolvePart(CompiledArtifactIdentification identification, PartIdentification partIdentification, Version partVersionOverride) {
		// TODO: don't locally cache parts from uncacheable repos 
		// TODO 2 : check if that works with SNAPSHOTS
		CompiledPartIdentification cpi = CompiledPartIdentification.from(identification, partIdentification);		
		Maybe<ArtifactDataResolution> optional = resolutionCache.get( HashComparators.compiledPartIdentification.eqProxy(cpi));	
		return optional;
	}
	
	@Override
	public Maybe<List<VersionInfo>> getVersionsReasoned(ArtifactIdentification artifactIdentification) {
		return versionsCache.get( HashComparators.artifactIdentification.eqProxy(artifactIdentification));
	}

	@Override
	public List<VersionInfo> getVersions(ArtifactIdentification artifactIdentification) {	
		return getVersionsReasoned(artifactIdentification).get();
	}
	@Override
	public Maybe<List<PartReflection>> getAvailablePartsOfReasoned( CompiledArtifactIdentification compiledArtifactIdentification) {
		List<PartReflection> result = new ArrayList<>();
		
		var lazyReason = new LazyInitialized<Reason>(() -> TemplateReasons.build(PartReflectionFailure.T).toReason());
		
		// TODO : check if locally installed ?
		List<PartAvailabilityAccess> partAvailabilityAccesses = partAvailabilityAccessCache.get( HashComparators.compiledArtifactIdentification.eqProxy(compiledArtifactIdentification));
		for (PartAvailabilityAccess pa : partAvailabilityAccesses) {			
			ArtifactPartResolverPersistenceDelegate repoDelegate = pa.repoDelegate();

			if (!repoDelegate.artifactFilter().matches(compiledArtifactIdentification))
				continue;
			
			if (!repoDelegate.isLocalDelegate()){
				var partsOfMaybe = repoDelegate.resolver().getPartsOfReasoned(compiledArtifactIdentification);
				
				if (partsOfMaybe.isUnsatisfied()) {
					if (partsOfMaybe.isUnsatisfiedBy(NotFound.T))
						continue;
					else
						lazyReason.get().getReasons().add(partsOfMaybe.whyUnsatisfied());
				}

				List<PartReflection> partsOf = partsOfMaybe.get();
				result.addAll( partsOf);

				if (repoDelegate.repositoryDominanceFilter().matches(compiledArtifactIdentification))
					break;

			} else {
				// handle local repository here 
				@Nullable
				Set<EqProxy<Version>> set = localVersionsCache.get( HashComparators.artifactIdentification.eqProxy( compiledArtifactIdentification));
				if (set == null || !set.contains( HashComparators.version.eqProxy(compiledArtifactIdentification.getVersion()))) {
					continue;
				}
				Set<CompiledPartIdentification> availableParts = pa.getAvailableParts();
				result.addAll( PartReflectionCommons.transpose(availableParts, "local"));
			}					
		}
		
		if (lazyReason.isInitialized())
			return Maybe.incomplete(result, lazyReason.get());
					
		return Maybe.complete(result);
	}
	
	
}


