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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.codebase.CodebaseRepositoryModule;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.model.mc.reason.MalformedArtifactDescriptor;
import com.braintribe.devrock.model.mc.reason.analytics.IndexingFailedReason;
import com.braintribe.devrock.model.repository.CodebaseRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;


/**
 * an attempt to use a TDR to make a reverse dependency look-up
 * 
 *
 * @author pit
 *
 */
public class TransitiveResolverBasedReverseDependencyAnalyzer {

	private Map<File,String> codeBases;
	
	@Configurable  @Required
	public void setCodeBases(Map<File, String> codeBases) {
		this.codeBases = codeBases;
	}

	private File localRepository = new File("f:/repository");
	
	@Configurable @Required
	public void setLocalRepository(File localRepository) {
		this.localRepository = localRepository;
	}
	private boolean verbose;
	
	@Configurable
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	

	/**
	 * scans all poms and compiles them all into {@link CompiledArtifact}
	 * @return - a {@link Maybe} of a {@link Map} of {@link VersionedArtifactIdentification} to {@link CompiledArtifact}
	 */
	public Maybe<AnalysisArtifactResolution> resolve(String terminal) {
		
		int i = 0;
		List<CodebaseRepository> codebaseRepositories = new ArrayList<>(codeBases.size());
		for (Map.Entry<File, String> entry : codeBases.entrySet()) {
			CodebaseRepository codebaseRepository = CodebaseRepository.T.create();
			codebaseRepository.setName("codebase-" + i++);
			codebaseRepository.setTemplate( entry.getValue());
			codebaseRepository.setRootPath(entry.getKey().getAbsolutePath());
			codebaseRepositories.add(codebaseRepository);
		}
		CodebaseRepositoryModule module = new CodebaseRepositoryModule( codebaseRepositories);
		
		RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.T.create();
		repositoryConfiguration.setCachePath( localRepository.getAbsolutePath());
		
		try (				
				WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, module)
				.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(repositoryConfiguration))				
				.build();
				) {
			
			ArtifactDataResolverContract dataResolverContract = resolverContext.contract().transitiveResolverContract().dataResolverContract();

			// find all poms, compile them and build a database
			double before = System.currentTimeMillis();
			Map<VersionedArtifactIdentification, CompiledArtifact> mapOfArtifacts = new HashMap<>();
			List<Reason> failureReasons = new ArrayList<>();
			for (CodebaseRepository codebaseRepository : codebaseRepositories) {
				File root = new File(codebaseRepository.getRootPath());
				List<File> poms = extractPoms(root);
				for (File pom : poms) {
					if (verbose) {
						System.out.print("indexing :" + pom.getAbsolutePath());
					}
					Maybe<CompiledArtifact> maybe = dataResolverContract.declaredArtifactCompiler().compileReasoned(pom);
					if (maybe.isUnsatisfied()) {
						System.err.println("can't read [" + pom.getAbsolutePath() + "] as" + maybe.whyUnsatisfied().stringify());
						MalformedArtifactDescriptor reason = Reasons.build(MalformedArtifactDescriptor.T).text("invalid pom: " + pom.getAbsolutePath()).cause(maybe.whyUnsatisfied()).toReason();
						failureReasons.add(reason);
						continue;
					}
					CompiledArtifact compiledArtifact = maybe.get();
					if (verbose) {
						System.out.println("->" + compiledArtifact.asString());
					}
					VersionedArtifactIdentification vai = VersionedArtifactIdentification.create(compiledArtifact.getGroupId(), compiledArtifact.getArtifactId(), compiledArtifact.getVersion().asString());
					mapOfArtifacts.put(vai, compiledArtifact);
					
				}
			}
			double afterIndexing = System.currentTimeMillis();
			if (verbose) {
				System.out.println( "Indexing of [" + mapOfArtifacts.size() + "] artifacts took [" + (afterIndexing-before) + "] ms");
			}
			// build terminal with all artifacts in map
			
			TransitiveResolverContract transitiveResolverContract = resolverContext.contract().transitiveResolverContract();
			TransitiveResolutionContext context = TransitiveResolutionContext.build().lenient(true).done();
			
			List<CompiledTerminal> list = mapOfArtifacts.values() //
															.stream() //
															.filter( ca -> !ca.asString() //
															.startsWith(terminal)) // "com.braintribe.devrock:mc-core")) // 
															.map( ca -> CompiledTerminal.from(ca)) //
															.collect(Collectors.toList()); //
			
			AnalysisArtifactResolution artifactResolution = transitiveResolverContract.transitiveDependencyResolver().resolve(context, list);
			
			double afterResolution = System.currentTimeMillis();
			
			if (verbose) {
				System.out.println( "Transitive resolution of [" + mapOfArtifacts.size() + "] artifacts took [" + (afterResolution-afterIndexing) + "] ms");
			}
			
			boolean failed = !failureReasons.isEmpty() || artifactResolution.hasFailed(); 
			
			if (failed) {
				Reason umbrella = Reasons.build(IndexingFailedReason.T) //
									.text("scan had issues on :" + terminal) //
									.toReason(); //
				if (artifactResolution.getFailure() != null) {
					umbrella.getReasons().add(artifactResolution.getFailure());
				}
				if (failureReasons.isEmpty()) {
					umbrella.getReasons().addAll(failureReasons);
				}
				artifactResolution.setFailure(umbrella);
				
				return Maybe.incomplete( artifactResolution, umbrella);
			}
			else {
				return Maybe.complete( artifactResolution);
			}
			
		}	
		catch( Exception e) {
			e.printStackTrace();
			return Maybe.empty(com.braintribe.gm.model.reason.essential.InternalError.from( e));			
		}
	}
	
	/**
	 * scan for all pom.xml files (if one is found, no directories below will be scanned)
	 * @param folder - the {@link File} folder to scan
	 * @return - a {@link List} of {@link File} representing the pom files.
	 */
	private List<File> extractPoms( File folder) {
		List<File> result = new ArrayList<>();
		File [] files = folder.listFiles(); 
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
		
	
	

	
}
