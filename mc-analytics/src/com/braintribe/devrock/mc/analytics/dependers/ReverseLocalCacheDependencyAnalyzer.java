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
package com.braintribe.devrock.mc.analytics.dependers;




import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.resolver.DeclaredArtifactCompiler;
import com.braintribe.devrock.mc.core.configuration.StandaloneRepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.mc.cfg.origination.Origination;
import com.braintribe.devrock.model.mc.cfg.origination.resolution.CodebaseRepositoryOrigination;
import com.braintribe.devrock.model.mc.cfg.origination.resolution.LocalCacheOrigination;
import com.braintribe.devrock.model.mc.cfg.origination.resolution.RemoteRepositoryOrigination;
import com.braintribe.devrock.model.mc.cfg.origination.resolution.ReverseDependencyLookupResult;
import com.braintribe.devrock.model.mc.reason.InvalidRepositoryConfiguration;
import com.braintribe.devrock.model.mc.reason.MalformedArtifactDescriptor;
import com.braintribe.devrock.model.mc.reason.McReason;
import com.braintribe.devrock.model.mc.reason.UnresolvedProperty;
import com.braintribe.devrock.model.mc.reason.analytics.IndexingFailedReason;
import com.braintribe.devrock.model.repository.CodebaseRepository;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.MavenRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.util.Lists;


/**
 * little helper to get a 'depender analysis' across the local cache (aka 'local repository')
 * 
 *  
 * 
 * @author pit
 *
 */
public class ReverseLocalCacheDependencyAnalyzer {
	
	private LazyInitialized<Maybe<Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact>>> artifactMap = new LazyInitialized<>( this::getArtifactMap);
	private Map<EqProxy<ArtifactIdentification>, List<Pair<VersionedArtifactIdentification, CompiledDependency>>> dependerMap = new ConcurrentHashMap<>();
	
	private Map<EqProxy<ArtifactIdentification>, List<DependerAnalysisNode>> visited = new ConcurrentHashMap<>();
	
	private YamlMarshaller marshaller = new YamlMarshaller();
	{
		marshaller.setWritePooled(true);		
	}
	
	private File indexFile;
	
	@Configurable
	public void setIndexFile(File indexFile) {
		this.indexFile = indexFile;
	}
	
	
	private boolean rebuildIndex;
	
	@Configurable
	public void setRebuildIndex(boolean rebuildIndex) {
		this.rebuildIndex = rebuildIndex;
	}
	
	private RepositoryConfiguration repositoryConfiguration;	
	/**
	 * @param repositoryConfiguration - a ready {@link RepositoryConfiguration}
	 */
	@Configurable
	public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
		this.repositoryConfiguration = repositoryConfiguration;
	}
	
	private File repositoryConfigurationFile;	
	/**
	 * @param repositoryConfigurationFile - a {@link File} pointing to the YAML formatted repository configuration
	 */
	@Configurable
	public void setRepositoryConfigurationFile(File repositoryConfigurationFile) {
		this.repositoryConfigurationFile = repositoryConfigurationFile;
	}
	
	private boolean verbose;	
	/**
	 * @param verbose - true if output message should appear
	 */
	@Configurable
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
		
	private Map<String,String> environmentOverrides = new HashMap<>();	
	/**
	 * @param environmentOverrides - a {@link Map} of env-variable Name ot env-variable Value
	 */
	@Configurable
	public void setEnvironmentOverrides(Map<String, String> environmentOverrides) {
		this.environmentOverrides = environmentOverrides;
	}
	
	private List<String> codebaseVariables = Lists.list("groupId", "artifactId", "version");			
	
	
	
	/**
	 * build a {@link OverridingEnvironment} with the environment overrides
	 * @param overrides
	 * @return
	 */
	protected OverridingEnvironment buildVirtualEnvironment(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		return ove;		
	}
	
	private VirtualEnvironment virtualEnvironment;
	
	/**
	 * a possible external {@link VirtualEnvironment}
	 * @param virtualEnvironment - a ready-made {@link VirtualEnvironment}
	 */
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	/**
	 * builds an {@link Origination} with all data from the passed {@link RepositoryConfiguration}
	 * @param configuration - the {@link RepositoryConfiguration}
	 * @param terminal - the {@link ArtifactIdentification} to reference in the {@link Origination}
	 * @return - the {@link Origination}
	 */
	private Origination buildOrigination( RepositoryConfiguration configuration, ArtifactIdentification terminal) {
		ReverseDependencyLookupResult origination = TemplateReasons.build(ReverseDependencyLookupResult.T)
		.assign(ReverseDependencyLookupResult::setDate, new Date())
		.assign(ReverseDependencyLookupResult::setIdentification, terminal)
		.toReason();
		
		// a) the local repository to use
		LocalCacheOrigination localCache = TemplateReasons.build( LocalCacheOrigination.T)
				.assign(LocalCacheOrigination::setLocation, configuration.getCachePath())
				.toReason();
		origination.getReasons().add(localCache);
		
		// b) all source repositories (name, location)
		List<CodebaseRepository> codebaseRepositories = configuration.getRepositories().stream().filter( r -> r instanceof CodebaseRepository).map( r -> (CodebaseRepository) r).collect(Collectors.toList());
		for (CodebaseRepository codebaseRepository : codebaseRepositories) {
			CodebaseRepositoryOrigination codebaseOrigination = TemplateReasons.build( CodebaseRepositoryOrigination.T)
					.assign( CodebaseRepositoryOrigination::setGroup, codebaseRepository.getName())
					.assign( CodebaseRepositoryOrigination::setLocation, codebaseRepository.getRootPath())
					.toReason();
			origination.getReasons().add(codebaseOrigination);
		}
		// c) eventually all backing-up repositories 
		List<MavenRepository> remoteRepositories = configuration.getRepositories().stream().filter( r -> r instanceof MavenRepository).map( r -> (MavenRepository) r).collect(Collectors.toList());
		for (MavenRepository mavenRepository : remoteRepositories) {
			RemoteRepositoryOrigination codebaseOrigination;
			
			if (mavenRepository instanceof MavenHttpRepository) { // HTTP remote
				MavenHttpRepository mavenHttpRepository = (MavenHttpRepository) mavenRepository;
				codebaseOrigination = TemplateReasons.build( RemoteRepositoryOrigination.T)
						.assign( RemoteRepositoryOrigination::setName, mavenHttpRepository.getName())
						.assign( RemoteRepositoryOrigination::setUrl, mavenHttpRepository.getUrl())
						.toReason();
			}
			else if (mavenRepository instanceof MavenFileSystemRepository) { // file system 
				MavenFileSystemRepository mavenFileRepository = (MavenFileSystemRepository) mavenRepository;
				codebaseOrigination = TemplateReasons.build( RemoteRepositoryOrigination.T)
						.assign( RemoteRepositoryOrigination::setName, mavenFileRepository.getName())
						.assign( RemoteRepositoryOrigination::setUrl, mavenFileRepository.getRootPath())
						.toReason();
			}
			else { // unknown type of repository
				codebaseOrigination = TemplateReasons.build( RemoteRepositoryOrigination.T)
						.assign(RemoteRepositoryOrigination::setName, mavenRepository.getName())
						.assign(RemoteRepositoryOrigination::setUrl, "<unknown>")
						.toReason();
			}			
			origination.getReasons().add(codebaseOrigination);
		}
		
		return origination;				
	}

	/**
	 * scans all poms and compiles them all into {@link CompiledArtifact}
	 * @return - a {@link Maybe} of a {@link Map} of {@link VersionedArtifactIdentification} to {@link CompiledArtifact}
	 */
	private Maybe<Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact>> getArtifactMap() {
		
		Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact> mapOfArtifacts;
		
		if (!rebuildIndex && indexFile != null && indexFile.exists()) {
		
			mapOfArtifacts = loadFromIndex();
		}
		
		mapOfArtifacts = new ConcurrentHashMap<>();
		
		Maybe<RepositoryConfiguration> repoConfigurationMaybe = acquireRepositoryConfiguration();
		if (!repoConfigurationMaybe.hasValue()) {
			return Maybe.empty( repoConfigurationMaybe.whyUnsatisfied());
		}
		
		if (repoConfigurationMaybe.isIncomplete()){ 			
			repositoryConfiguration = repoConfigurationMaybe.value();
			// check failure reasons
			Reason failure = repoConfigurationMaybe.whyUnsatisfied();			
			boolean onlyCodebaseVariableErrors = true;
			// scan : valid are the ones for the CodebaseRepositories: ${group},${artifact},${version}
			for (Reason subs : failure.getReasons()) {
				if (subs instanceof UnresolvedProperty) {
					UnresolvedProperty unresolvedProperty = (UnresolvedProperty) subs;
					String propertyName = unresolvedProperty.getPropertyName();
					if (!codebaseVariables.contains(propertyName)) {						
						onlyCodebaseVariableErrors = false;
					}												
				}
			}
			if (!onlyCodebaseVariableErrors) {
				return Maybe.empty( Reasons.build(InvalidRepositoryConfiguration.T).text("unexpected issues while reading the configuration file").cause(failure).toReason());
			}						
			
		}
		else {		
			repositoryConfiguration = repoConfigurationMaybe.get();
		}
		
						
		VirtualEnvironment ve = buildVirtualEnvironment( environmentOverrides);
			
		try (				
				WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE)
				.bindContract(VirtualEnvironmentContract.class, () -> ve)
				.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(repositoryConfiguration))				
				.build();
				) {
			
			ArtifactDataResolverContract dataResolverContract = resolverContext.contract().transitiveResolverContract().dataResolverContract();

			// find all poms, compile them and build a database		
			List<Reason> failureReasons = new ArrayList<>();
			
			double before = System.currentTimeMillis();
			File localRepository = new File(repositoryConfiguration.getCachePath()); 
			if (!localRepository.exists()) {
				return Maybe.empty( NotFound.create( "given local repository does not exist : " + repositoryConfiguration.getCachePath()));
			}
			
			 DeclaredArtifactCompiler declaredArtifactCompiler = dataResolverContract.declaredArtifactCompiler();
			
			// scan 
			List<File> poms = extractPoms(localRepository);
			ExecutorService executorService = Executors.newFixedThreadPool( 5);
			List<Future<Reason>> futures = new ArrayList<>( poms.size());
			final Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact> map = mapOfArtifacts;
			
			try {
				for (File pom : poms) {
					futures.add( executorService.submit( () -> process(declaredArtifactCompiler, pom, map)));				
				}
				
				for (Future<Reason> future : futures) {
					Reason reason = future.get();
					if (reason != null) {
						failureReasons.add(reason);
					}
				}
			}
			finally {
				executorService.shutdown();
			}
			
						
			double afterIndexing = System.currentTimeMillis();
			if (verbose) {
				System.out.println( "Indexing of [" + mapOfArtifacts.size() + "] artifacts took [" + (afterIndexing-before) + "] ms");
			}
			
			if (failureReasons.isEmpty()) {				
				dumpToIndex( mapOfArtifacts);
				return Maybe.complete( mapOfArtifacts);
			}
			else {
				Reason reason = Reasons.build(IndexingFailedReason.T).text("building of the database failed").toReason();
				return Maybe.incomplete( mapOfArtifacts, reason);
			}
			
		}	
		catch( Exception e) {
			e.printStackTrace();
			return Maybe.empty(com.braintribe.gm.model.reason.essential.InternalError.from( e));			
		}
	}
	
	/**
	 * process an artifact - identify it and add it to the indices (the VAI of the artifact to the CA, the AI of each dependency to a List of Pairs of VAI and CD)
	 * @param compiler - the {@link DeclaredArtifactCompiler}
	 * @param pom - the File 
	 * @param mapOfArtifacts - the map to add the identify pom to 
	 * @return - a failure {@link Reason} or null if everything's fine 
	 */
	private Reason process(DeclaredArtifactCompiler compiler, File pom, Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact> mapOfArtifacts) {
		Maybe<CompiledArtifact> maybe = compiler.compileReasoned(pom);
		if (maybe.isUnsatisfied()) {
			System.err.println("can't read [" + pom.getAbsolutePath() + "] as" + maybe.whyUnsatisfied().stringify());
			MalformedArtifactDescriptor reason = Reasons.build(MalformedArtifactDescriptor.T).text("invalid pom: " + pom.getAbsolutePath()).cause(maybe.whyUnsatisfied()).toReason();			
			return reason;
		}
		CompiledArtifact compiledArtifact = maybe.get();
		VersionedArtifactIdentification vai = VersionedArtifactIdentification.create(compiledArtifact.getGroupId(), compiledArtifact.getArtifactId(), compiledArtifact.getVersion().asString());
		mapOfArtifacts.put(HashComparators.versionedArtifactIdentification.eqProxy(vai), compiledArtifact);
		
		// index dependencies - build a map AI-proxy of the dependency, and a List of Pairs of the owner (VAI) and the actual dependency
		List<CompiledDependency> dependencies = compiledArtifact.getDependencies();
		for (CompiledDependency dependency : dependencies) {
			EqProxy<ArtifactIdentification> eqproxy = HashComparators.artifactIdentification.eqProxy( dependency);
			List<Pair<VersionedArtifactIdentification, CompiledDependency>> dependers = dependerMap.computeIfAbsent( eqproxy, k -> new ArrayList<>());
			Pair<VersionedArtifactIdentification, CompiledDependency> pair = Pair.of( vai, dependency);
			dependers.add( pair);					
		}
		return null;
	}

	
	/**
	 * @param indexFile2
	 * @return
	 */
	private Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact> loadFromIndex() {
		try (InputStream out = new FileInputStream(indexFile)) {
			double before = System.currentTimeMillis();
			@SuppressWarnings("unchecked")
			List<CompiledArtifact> dumpable= (List<CompiledArtifact>) marshaller.unmarshall(out);
			Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact> result = new HashMap<>( dumpable.size());
			for (CompiledArtifact entry : dumpable) {
				VersionedArtifactIdentification vai = VersionedArtifactIdentification.create( entry.getGroupId(), entry.getArtifactId(), entry.getVersion().asString());
				result.put( HashComparators.versionedArtifactIdentification.eqProxy(vai), entry);
			}
			double after = System.currentTimeMillis();
			System.out.println( "Loading the index with [" + result.size() + "] artifacts took [" + (after-before) + "] ms");
			
			return result;
		}
		catch (Exception e) {
			throw new IllegalStateException("cannot load index from: " + indexFile.getAbsolutePath(), e);
		}		
		
	}

	private void dumpToIndex(Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact> mapOfArtifacts) {
		List<CompiledArtifact> dumpable = new ArrayList<>( mapOfArtifacts.size());		
		for (Map.Entry<EqProxy<VersionedArtifactIdentification>, CompiledArtifact> entry : mapOfArtifacts.entrySet()) {
			dumpable.add( entry.getValue());
		}
		try (OutputStream out = new FileOutputStream(indexFile)) {
			marshaller.marshall(out, dumpable);
		}
		catch (Exception e) {
			throw new IllegalStateException("cannot dump index to: " + indexFile.getAbsolutePath(), e);
		}		
	}

	/**
	 * tries to acquire a {@link RepositoryConfiguration} via the different parameterization possibilities,
	 * - per RepositoryConfiguration instance
	 * - per YAML file
	 * - per declaration of the local repo & the codebases
	 * @return - a {@link Maybe} of the {@link RepositoryConfiguration}
	 */
	private Maybe<RepositoryConfiguration> acquireRepositoryConfiguration() {
		if (repositoryConfiguration != null) {
			return Maybe.complete( repositoryConfiguration);
		}
		if (repositoryConfigurationFile != null) {
			StandaloneRepositoryConfigurationLoader loader = new StandaloneRepositoryConfigurationLoader();			
			loader.setVirtualEnvironment( acquireVirtualEnvironment().get());
			Maybe<RepositoryConfiguration> maybe = loader.loadRepositoryConfiguration(repositoryConfigurationFile);			
			return maybe;
		}
		return Maybe.empty(Reasons.build(McReason.T).text("no configuration available").toReason());
	}
	
	/**
	 * @return - instantiated concrete {@link VirtualEnvironment}, either a 
	 * {@link StandardEnvironment} or a prepped {@link OverridingEnvironment}
	 */
	private Maybe<VirtualEnvironment> acquireVirtualEnvironment() {
		if (virtualEnvironment != null) {
			return Maybe.complete(virtualEnvironment);
		}
		if (environmentOverrides != null) {
			VirtualEnvironment ove = buildVirtualEnvironment(environmentOverrides);
			return Maybe.complete(ove);
		}
		return Maybe.complete( StandardEnvironment.INSTANCE);
	}

	/**
	 * scan for all pom.xml files (if one is found, no directories below will be scanned)
	 * @param folder - the {@link File} folder to scan
	 * @return - a {@link List} of {@link File} representing the pom files.
	 */
	private List<File> extractPoms( File folder) {
		List<File> result = new ArrayList<>();
		File [] files = folder.listFiles(); 
		if (files == null) {
			return result;
		}
		for (File file : files)	 {
			if (file.getName().endsWith( ".pom")) {
				result.add( file);
				break; // first pom found's enough (all others are below this first)
			}			
			if (file.isDirectory()) {
				result.addAll( extractPoms(file));
			}
		}		
		return result;
	}
	
	
	private List<DependerAnalysisNode> getDependersOfProxied(Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact> map, DependerAnalysisNode previous, ArtifactIdentification ai, String version) {	
 		
		EqProxy<ArtifactIdentification> eqProxy = HashComparators.artifactIdentification.eqProxy(ai);		
		List<DependerAnalysisNode> result = new ArrayList<>();
		List<Pair<VersionedArtifactIdentification, CompiledDependency>> dependers = dependerMap.get( eqProxy);
		
		if (dependers == null) {
			visited.put(eqProxy, result);
			return result;
		}
		
		// filter : if a version is passed, filter the dependers 
		if (version != null) {
			Version filterVersion = Version.parse(version);
			
			Iterator<Pair<VersionedArtifactIdentification, CompiledDependency>> iter = dependers.iterator();
			while (iter.hasNext()) {
				Pair<VersionedArtifactIdentification, CompiledDependency> pair = iter.next();
				
				VersionExpression dependencyVersionExpression = pair.second.getVersion();
				if (!dependencyVersionExpression.matches(filterVersion)) {
					iter.remove();
					System.out.println("No match: " + ai.asString() + "#" + version + " -> " + pair.second.asString());
				}	
				else {
					System.out.println("Match: " + ai.asString() + "#" + version + " -> " + pair.second.asString());
				}				
			}
		}
		
		
		
		
		for (Pair<VersionedArtifactIdentification, CompiledDependency> pair : dependers) {
			VersionedArtifactIdentification depender = pair.first;			
			List<DependerAnalysisNode> list = visited.get( eqProxy);
			if (list != null) {
				System.out.println("Already processed: " + ai.asString());
				return list;
			}
			ArtifactIdentification dai = (ArtifactIdentification) depender;
			CompiledArtifact ca = artifactMap.get().get().get( HashComparators.versionedArtifactIdentification.eqProxy(depender));
			CompiledDependency cd = pair.second;
			DependerAnalysisNode node = DependerAnalysisNode.from( ca, cd, previous);
			previous.getNextNodes().add(node);
			List<DependerAnalysisNode> dependersOfProxied = getDependersOfProxied(map, node, dai, depender.getVersion());
			result.addAll(dependersOfProxied);		
		}		
		visited.put(eqProxy, result);
		return result;
	}
		
	
	/**
	 * resolves a reverse-dependency chain (a {@link List} of inter-linked {@link DependerAnalysisNode}
	 * @param ai - the {@link ArtifactIdentification} of the starting artifact in the sources
	 * @return - a {@link Maybe} of a {@link List} of {@link DependerAnalysisNode}
	 */
	public Maybe<List<DependerAnalysisNode>> resolve(VersionedArtifactIdentification requestedArtifactIdentification) {
		
		Maybe<Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact>> mapMaybe = artifactMap.get();
		if (mapMaybe.hasValue()) {
			Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact> map = mapMaybe.value();
			
			// check whether we need to fully identify the artifact here .. 			
			String groupId = requestedArtifactIdentification.getGroupId();
			VersionedArtifactIdentification actualRequestArtifactIdentification;
			if (groupId == null) {
				actualRequestArtifactIdentification = scanForArtifact( map, requestedArtifactIdentification);
				if (actualRequestArtifactIdentification == null) {
					return Maybe.empty( Reasons.build( NotFound.T).text("no matching artifact found for: " + requestedArtifactIdentification.asString()).toReason());
				}
			}
			else  {
				actualRequestArtifactIdentification = requestedArtifactIdentification;
			}
			
			DependerAnalysisNode node = DependerAnalysisNode.T.create();
			node.setInitialArtifactIdentification(actualRequestArtifactIdentification);					
			
			List<DependerAnalysisNode> dependers = new ArrayList<>();
			dependers.add(node);					
			double before = System.currentTimeMillis();
			
			dependers.addAll( getDependersOfProxied(map, node, actualRequestArtifactIdentification, actualRequestArtifactIdentification.getVersion()));
			
			double after = System.currentTimeMillis();
			if (verbose) {
				System.out.println( "Analyzing dependers withhin [" + map.size() + "] artifacts took [" + (after-before) + "] ms");
			}
			if (verbose) {
				ReverseDependencyAnalysisPrinter.output(node);
			}
			
			return Maybe.complete( dependers);
		}		
		return Maybe.empty(mapMaybe.whyUnsatisfied());
	}
	
	
	
	
	private VersionedArtifactIdentification scanForArtifact( Map<EqProxy<VersionedArtifactIdentification>, CompiledArtifact> map, VersionedArtifactIdentification ai) {
		List<CompiledArtifactIdentification> result = new ArrayList<>();
		for (Map.Entry<EqProxy<VersionedArtifactIdentification>, CompiledArtifact> entry : map.entrySet()) {
			VersionedArtifactIdentification vai = entry.getKey().get();
			if (
					vai.getArtifactId().equals(ai.getArtifactId()) &&
					vai.getVersion().equals( ai.getVersion())
			   ) {
				result.add( CompiledArtifactIdentification.from(vai));
			}
		}
		
		if (result.size() == 0) {
			return null;
		}
		
		result.sort( new Comparator<CompiledArtifactIdentification>() {
			@Override
			public int compare(CompiledArtifactIdentification o1, CompiledArtifactIdentification o2) {			
				return o1.compareTo(o2);
			}			
		});
		
		
		CompiledArtifactIdentification highestVersionedArtifact = result.get( result.size() - 1);
		return VersionedArtifactIdentification.create( highestVersionedArtifact.getGroupId(), highestVersionedArtifact.getArtifactId(), highestVersionedArtifact.getVersion().asString());
				
	}

	/**
	 * @param nodes
	 * @return
	 */
	public AnalysisArtifactResolution transpose( List<DependerAnalysisNode> nodes) {
		AnalysisArtifactResolution resolution = AnalysisArtifactResolution.T.create();
		
		DependerAnalysisNode startingPoint = nodes.get(0);
		ArtifactIdentification initialArtifactIdentification = startingPoint.getInitialArtifactIdentification();
		

		AnalysisArtifact at = AnalysisArtifact.T.create();
		at.setGroupId( initialArtifactIdentification.getGroupId());
		at.setArtifactId(initialArtifactIdentification.getArtifactId());
		if (initialArtifactIdentification instanceof VersionedArtifactIdentification) {
			at.setVersion( ((VersionedArtifactIdentification) initialArtifactIdentification).getVersion());
		}
		
		Map<DependerAnalysisNode, AnalysisArtifact> transposedNodes = new HashMap<>();		
		transposedNodes.put(startingPoint, at);
		
		List<DependerAnalysisNode> nextNodes = startingPoint.getNextNodes();
		for (DependerAnalysisNode node : nextNodes) {			
			process(transposedNodes, node);						
		}
				
		// build origination
		resolution.setTerminals( Collections.singletonList( at));
		Origination origination = buildOrigination(repositoryConfiguration, initialArtifactIdentification);
		resolution.setOrigination(origination);
		
		return resolution;
	}

	private void process(Map<DependerAnalysisNode, AnalysisArtifact> transposedNodes, DependerAnalysisNode node) {
		AnalysisArtifact referenced = transposedNodes.get( node.getPreviousNode());			
		AnalysisDependency ad = AnalysisDependency.from( node.getReferencingDependency());			
		referenced.getDependers().add(ad);
		AnalysisArtifact artifact = transposeToAnalysisArtifact( node);
		transposedNodes.put(node, artifact);			
		ad.setDepender( artifact);
		
		for (DependerAnalysisNode child : node.getNextNodes()) {
			process( transposedNodes, child);
		}
	}

	
	
	private AnalysisArtifact transposeToAnalysisArtifact(DependerAnalysisNode node) {
		AnalysisArtifact artifact = AnalysisArtifact.T.create();
		VersionedArtifactIdentification versionedArtifactIdentification = node.getVersionedArtifactIdentification();
		artifact.setGroupId( versionedArtifactIdentification.getGroupId());
		artifact.setArtifactId(versionedArtifactIdentification.getArtifactId());
		artifact.setVersion( versionedArtifactIdentification.getVersion());		
		return artifact;
	}
	
	
}
