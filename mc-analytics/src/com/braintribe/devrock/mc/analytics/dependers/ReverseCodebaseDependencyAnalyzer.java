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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.mc.core.configuration.StandaloneRepositoryConfigurationLoader;
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
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.util.Lists;


/**
 * little helper to get a 'depender analysis' across the source folders
 * 
 * the configuration is two-fold:
 * a) you need to give enough information for the analyzer to be able to compile all poms that need to be analyzed,
 * i.e. you need to specify all possible resources to do that. Minimally, it's the path to the 'local repository', 
 * but of course, you can add all pertinent remote repositories that may or may not be required for the compilation.
 * Of course, if - in order to compile (i.e. resolve all variables, dep-mgt data etc) - doesn't need any outside
 * artifact, no remote repository is required. 
 * b) you need to specify the 'source' repositories (aka Codebase repositories). The compiling process will only compile the
 * poms in the sources, and will only analyse and track within the sources. 
 *
 * you can either configure the path to local repository and codebases manually (no remote repos in that case), simply
 * configure a file pointing to a YAML formatted persisted repository-configuration, or just pass whatever repository-configuration 
 * that you currently have lying around. See the com.braintribe.devrock:mc-analytics-test for details. 
 * 
 * The result of the process is a {@link List} of {@link DependerAnalysisNode}s, where the nodes are interlinked (and the starting node would be 
 * the first node in the list and of course the only one without a 'precursor' node.
 * 
 *  NOTE: when you are using {@link CodebaseRepository} entities in the repository configuration and you specify it via file, you will need to specifiy
 *  the template structure for the codebase, hence you will need to use the three expressions : ${groupId},${artifactId},${version}. As these aren't 
 *  really variables, but mere template expressions for the use within the {@link CodebaseRepository}, they will not have a value assigned, and the 
 *  configuration loader will return an 'incomplete' maybe. 
 *  Currently, the {@link ReverseCodebaseDependencyAnalyzer} will (if it loads the configuration) ignore any 'unsatisified reasons' of type {@link UnresolvedProperty}, 
 *  if the {@link UnresolvedProperty#getPropertyName} is one of the expressions mentioned above.  
 *  
 *  
 * 
 * @author pit
 *
 */
public class ReverseCodebaseDependencyAnalyzer {
	
	private LazyInitialized<Maybe<Map<VersionedArtifactIdentification, CompiledArtifact>>> artifactMap = new LazyInitialized<>( this::getArtifactMap);
	private Map<File,String> codeBases;
	/**
	 * the codebases to be used during scan 
	 * @param codeBases - a {@link Map} of the {@link File} of the root of a codebase and template for it.
	 */
	@Configurable
	public void setCodeBases(Map<File, String> codeBases) {
		this.codeBases = codeBases;
	}

	private File localRepository = new File("f:/repository");	
	/**
	 * if no qualified {@link RepositoryConfiguration} or {@link File} pointing to a YAML file, 
	 * the local repository must be specified
	 * @param localRepository - the {@link File} pointing to the local repository 
	 */
	@Configurable
	public void setLocalRepository(File localRepository) {
		this.localRepository = localRepository;
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
	private Maybe<Map<VersionedArtifactIdentification, CompiledArtifact>> getArtifactMap() {
		
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
			Map<VersionedArtifactIdentification, CompiledArtifact> mapOfArtifacts = new HashMap<>();
			List<Reason> failureReasons = new ArrayList<>();
			
			double before = System.currentTimeMillis();
			
			List<CodebaseRepository> codebaseRepositories = repositoryConfiguration.getRepositories().stream().filter( r -> r instanceof CodebaseRepository).map( r -> (CodebaseRepository) r).collect(Collectors.toList());
			
			for (CodebaseRepository codebaseRepository : codebaseRepositories) {
				File root = new File(codebaseRepository.getRootPath());
				List<File> poms = extractPoms(root);
				for (File pom : poms) {
					
					Maybe<CompiledArtifact> maybe = dataResolverContract.declaredArtifactCompiler().compileReasoned(pom);
					if (maybe.isUnsatisfied()) {
						System.err.println("can't read [" + pom.getAbsolutePath() + "] as" + maybe.whyUnsatisfied().stringify());
						MalformedArtifactDescriptor reason = Reasons.build(MalformedArtifactDescriptor.T).text("invalid pom: " + pom.getAbsolutePath()).cause(maybe.whyUnsatisfied()).toReason();
						failureReasons.add(reason);
						continue;
					}
					CompiledArtifact compiledArtifact = maybe.get();
					VersionedArtifactIdentification vai = VersionedArtifactIdentification.create(compiledArtifact.getGroupId(), compiledArtifact.getArtifactId(), compiledArtifact.getVersion().asString());
					mapOfArtifacts.put(vai, compiledArtifact);					
				}
			}
			double afterIndexing = System.currentTimeMillis();
			if (verbose) {
				System.out.println( "Indexing of [" + mapOfArtifacts.size() + "] artifacts took [" + (afterIndexing-before) + "] ms");
			}
			
			if (failureReasons.isEmpty()) {
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
		if (localRepository == null) {
			return Maybe.empty(Reasons.build(InvalidArgument.T).text("a path to a local repository must be declared").toReason());			
		}
		if (codeBases == null) {
			return Maybe.empty(Reasons.build(InvalidArgument.T).text("the codebases must be declared").toReason());
		}
		
		RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.T.create();
		repositoryConfiguration.setCachePath( localRepository.getAbsolutePath());
		int i = 0;
		for (Map.Entry<File, String> entry : codeBases.entrySet()) {
			CodebaseRepository codebaseRepository = CodebaseRepository.T.create();
			codebaseRepository.setName("codebase-" + i++);
			codebaseRepository.setTemplate( entry.getValue());
			codebaseRepository.setRootPath(entry.getKey().getAbsolutePath());
			repositoryConfiguration.getRepositories().add(codebaseRepository);
		}
		return Maybe.complete(repositoryConfiguration);
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
			if (file.getName().equals("pom.xml")) {
				result.add( file);
				break; // first pom found's enough (all others are below this first)
			}			
			if (file.isDirectory()) {
				result.addAll( extractPoms(file));
			}
		}		
		return result;
	}
	
	/**
	 * returns all referencers of the given {@link ArtifactIdentification}
	 * @param map - the {@link Map} of {@link VersionedArtifactIdentification} to {@link CompiledArtifact}
	 * @param previous - the precursor {@link DependerAnalysisNode} 
	 * @param vai - the {@link ArtifactIdentification} (note: it's unversioned)
	 * @return - a {@link List} of {@link DependerAnalysisNode}
	 */
	private List<DependerAnalysisNode> getReferencers(Map<VersionedArtifactIdentification, CompiledArtifact> map, DependerAnalysisNode previous, ArtifactIdentification vai) {
		List<DependerAnalysisNode> vais = new ArrayList<>();
		for (Map.Entry<VersionedArtifactIdentification, CompiledArtifact> entry : map.entrySet()) {
			CompiledArtifact ca = entry.getValue();			
			if (ca.getDependencies().isEmpty()) {
				continue;
			}
			for (CompiledDependency cd : ca.getDependencies()) {
				if (cd.compareTo(vai) == 0) {										
					DependerAnalysisNode node = DependerAnalysisNode.from( ca, cd, previous);
					previous.getNextNodes().add(node);
					vais.add(node);
				}
			}
		}
		return vais;
	}
	
	
	/**
	 * recursively gets all dependers of the passed {@link ArtifactIdentification}, walking 'upwards' until no further dependers can be found 
	 * @param map - the {@link Map} of {@link VersionedArtifactIdentification} to {@link CompiledArtifact}
	 * @param previous - the precursor {@link DependerAnalysisNode} 
	 * @param vai - the {@link ArtifactIdentification} (note: it's unversioned)
	 * @return - a {@link List} of {@link DependerAnalysisNode}
	 */
	private List<DependerAnalysisNode> getDependersOf(Map<VersionedArtifactIdentification, CompiledArtifact> map, DependerAnalysisNode previous, ArtifactIdentification vai) {
	 		
		List<DependerAnalysisNode> referencers = getReferencers(map, previous, vai);
		referencers.sort( new Comparator<DependerAnalysisNode>() {
			@Override
			public int compare(DependerAnalysisNode n1, DependerAnalysisNode n2) {
				return n1.getVersionedArtifactIdentification().compareTo(n2.getVersionedArtifactIdentification());				
			}			
		});
		if (referencers.isEmpty()) {
			return Collections.emptyList();
		}			
		List<DependerAnalysisNode> result = new ArrayList<>();
		for (DependerAnalysisNode node : referencers) {						
			result.addAll( getDependersOf(map, node, node.getVersionedArtifactIdentification()));
		}
		referencers.addAll(result);
		
		return referencers;
	}
		
	
	/**
	 * resolves a reverse-dependency chain (a {@link List} of inter-linked {@link DependerAnalysisNode}
	 * @param ai - the {@link ArtifactIdentification} of the starting artifact in the sources
	 * @return - a {@link Maybe} of a {@link List} of {@link DependerAnalysisNode}
	 */
	public Maybe<List<DependerAnalysisNode>> resolve(ArtifactIdentification ai) {
		DependerAnalysisNode node = DependerAnalysisNode.T.create();
		node.setInitialArtifactIdentification(ai);
		
		Maybe<Map<VersionedArtifactIdentification, CompiledArtifact>> mapMaybe = artifactMap.get();
		if (mapMaybe.hasValue()) {
			Map<VersionedArtifactIdentification, CompiledArtifact> map = mapMaybe.value();
			List<DependerAnalysisNode> dependers = new ArrayList<>();
			dependers.add(node);					
			double before = System.currentTimeMillis();
			dependers.addAll( getDependersOf(map, node, ai));
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
		
		Map<DependerAnalysisNode, AnalysisArtifact> transposedNodes = new HashMap<>();		
		transposedNodes.put(startingPoint, at);
		
		for (DependerAnalysisNode node : nodes) {
			if (node == startingPoint) {
				continue;
			}
			// 
			AnalysisArtifact referenced = transposedNodes.get( node.getPreviousNode());
			
			AnalysisDependency ad = AnalysisDependency.from( node.getReferencingDependency());			
			referenced.getDependers().add(ad);
			
			AnalysisArtifact transposedNode = AnalysisArtifact.T.create();
			VersionedArtifactIdentification versionedArtifactIdentification = node.getVersionedArtifactIdentification();
			transposedNode.setGroupId( versionedArtifactIdentification.getGroupId());
			transposedNode.setArtifactId(versionedArtifactIdentification.getArtifactId());
			transposedNode.setVersion( versionedArtifactIdentification.getVersion());
			transposedNodes.put(node, transposedNode);
			ad.setDepender(transposedNode);
			
		}		
		// build origination
		resolution.setTerminals( Collections.singletonList( at));
		Origination origination = buildOrigination(repositoryConfiguration, initialArtifactIdentification);
		resolution.setOrigination(origination);
		
		return resolution;
	}
}
