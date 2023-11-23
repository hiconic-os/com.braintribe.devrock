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
package com.braintribe.artifacts.codebase;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.resolving.ChainableDependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.ResolvingException;
import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationListener;
import com.braintribe.build.quickscan.agnostic.LocationAgnosticQuickImportScanner;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.panther.ProjectNature;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.model.processing.query.fluent.ConditionBuilder;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;

public class AgnosticDependencyResolverImpl implements DependencyResolver, ChainableDependencyResolver {
	private static final String URL_PROTOCOL_FILE = "file";	
	
	private DependencyResolver delegate;
	private List<SourceRepository> sourceRepositories;
	private ReentrantReadWriteLock smoodInitializationlock = new ReentrantReadWriteLock();
	boolean reinitializeSmood = false;
	private Smood smood;

	
	@Configurable @Required
	public void setSourceRepositories(List<SourceRepository> sourceRepositories) {
		this.sourceRepositories = sourceRepositories;
	}
	
	@Override
	public void addListener(DependencyResolverNotificationListener listener) {}

	@Override
	public void removeListener(DependencyResolverNotificationListener listener) {}

	@Override
	public Part resolvePom(String walkScopeId, Identification id, Version version) throws ResolvingException {
		String groupId = id.getGroupId();
		String artifactId = id.getArtifactId();
		String expression = groupId + ":" + artifactId + "#" + VersionProcessor.toString(version);
		List<SourceArtifact> result = runPartialSourceArtifactQuery( expression);
		if (result == null || result.size() == 0) {
			if (delegate != null) {
				return delegate.resolvePom(walkScopeId, id, version);
			}
			else {
				return null;
			}
		}
		// find the pom in the same directory as the first matching artifact
		File pomFile = determineAssociatedFile(result.get(0), "pom.xml");
		if (pomFile == null) {
			return null;		
		}
		
		Part pomPart = PartProcessor.createPartFromIdentification(id, version, PartTupleProcessor.createPomPartTuple());
		pomPart.setLocation( pomFile.getAbsolutePath());
		return pomPart;
	
	}

	@Override
	public Part resolvePomPart(String walkScopeId, Part pomPart) throws ResolvingException {
		String expression = pomPart.getGroupId() + ":" + pomPart.getArtifactId() + "#" + VersionProcessor.toString(pomPart.getVersion());
		List<SourceArtifact> result = runSourceArtifactQuery( expression);
		if (result == null || result.size() == 0) {
			if (delegate != null) {
				return delegate.resolvePomPart(walkScopeId, pomPart);
			}
			else {
				return null;
			}
		}
		// find the pom in the same directory as the first matching artifact
		File pomFile = determineAssociatedFile(result.get(0), "pom.xml");
		if (pomFile == null) {
			return null;		
		}
		pomPart.setLocation( pomFile.getAbsolutePath());
		return pomPart;
	}
	
	private Set<Solution> resolveDependency(String walkScopeId, Dependency dependency, boolean topOnly) throws ResolvingException {
		String groupId = dependency.getGroupId();
		String artifactId = dependency.getArtifactId();
		String expression = groupId + ":" + artifactId;
		List<SourceArtifact> result = runPartialSourceArtifactQuery( expression);
		if (result == null || result.size() == 0) {
			return Collections.emptySet();
		}
		
		if (topOnly) {
			
		}
			
		VersionRange range = dependency.getVersionRange();
		List<Solution> solutions = new ArrayList<Solution>();
		for (SourceArtifact sourceArtifact : result) {
			Version version = VersionProcessor.createFromString( sourceArtifact.getVersion());
			if (VersionRangeProcessor.matches(range, version)) {
				Solution solution = Solution.T.create();
				solution.setGroupId( sourceArtifact.getGroupId());
				solution.setArtifactId( sourceArtifact.getArtifactId());
				solution.setVersion( version);
				solutions.add( solution);
				
				Part part = PartProcessor.createPartFromIdentification(solution, version, PartTupleProcessor.createPomPartTuple());
				File pomFile = determineAssociatedFile( sourceArtifact, "pom.xml");
				part.setLocation( pomFile.getAbsolutePath());
				solution.getParts().add(part);
			}
		}

		// sort ascending acc version
		solutions.sort( new Comparator<Solution>() {

			@Override
			public int compare(Solution o1, Solution o2) {			
				return VersionProcessor.compare(o1.getVersion(), o2.getVersion());
			}
		});
		
		if (topOnly && !solutions.isEmpty()) { 
			// take the highest from the list
			Solution m = solutions.get( solutions.size()-1);
			Set<Solution> picked = new HashSet<>();
			picked.add( m);
			return picked;
		}
		
		// add any others from the delegate 
		if (delegate != null) {
			Set<Solution> delegatesSolutions;			
			if (topOnly) {
				delegatesSolutions = delegate.resolveTopDependency(walkScopeId, dependency);				
			} else {
				delegatesSolutions = delegate.resolveMatchingDependency(walkScopeId, dependency);
			}
			
			if (delegatesSolutions != null && !delegatesSolutions.isEmpty()) {
				solutions.addAll(delegatesSolutions);									
			}
		}
 		return new HashSet<>(solutions);
	}
	
	
	

	@Override
	public Set<Solution> resolveDependency(String walkScopeId, Dependency dependency) throws ResolvingException {		
		return resolveTopDependency(walkScopeId, dependency);
	}

	@Override
	public Set<Solution> resolveTopDependency(String walkScopeId, Dependency dependency) throws ResolvingException {	
		return resolveDependency(walkScopeId, dependency, true);
	}

	@Override
	public Set<Solution> resolveMatchingDependency(String walkScopeId, Dependency dependency) throws ResolvingException {
		return resolveDependency(walkScopeId, dependency, false);
	}

	@Override
	public void setDelegate(DependencyResolver delegate) {
		this.delegate = delegate;
	}
	
	public Smood getSmood() {
		Lock lock = smoodInitializationlock.writeLock();				
		if (smood == null || reinitializeSmood) {
			try {
				lock.lock();		
				smood = new Smood( EmptyReadWriteLock.INSTANCE);
				smood.initialize( getScannedSourceArtifacts());
				reinitializeSmood = false;
			}
			finally {
				lock.unlock();
			}				
		}			
		return smood;		
	}

	private List<SourceArtifact> getScannedSourceArtifacts() throws IllegalStateException{
		List<SourceArtifact> result = new ArrayList<SourceArtifact>();
		LocationAgnosticQuickImportScanner scanner = new LocationAgnosticQuickImportScanner();
		long before = System.nanoTime();
		for (SourceRepository sourceRepository : sourceRepositories) {
			result.addAll(getScannedSourceArtifacts(scanner, sourceRepository));
		}
		long after = System.nanoTime();
		System.out.println("scanned [" + sourceRepositories.size() + "] repositories with combined ["+ result.size() + "] artifacts in [" + (after-before)/1E6 + "] ms");
		return result;
	}
	
	private List<SourceArtifact> getScannedSourceArtifacts( LocationAgnosticQuickImportScanner scanner, SourceRepository sourceRepository) {
		scanner.setSourceRepository(sourceRepository);
		String repoUrlAsString = sourceRepository.getRepoUrl();
		URL repoUrl;
		try {
			repoUrl = new URL( repoUrlAsString);
		} catch (MalformedURLException e) {
			throw new IllegalStateException("url is invalid", e);
		}
		long before = System.nanoTime();
		List<SourceArtifact> result = scanner.scanLocalWorkingCopy( repoUrl.getFile());
		long after = System.nanoTime();
		System.out.println("scanned [" + result.size() + "] artifacts in [" + (after-before)/1E6 + "] ms");
		return result;		
	}
	
	
	private void processSourceArtifact( String txt, JunctionBuilder<EntityQueryBuilder> junctionBuilder) {
		
		Artifact artifact = NameParser.parseCondensedArtifactName(txt);
		JunctionBuilder<JunctionBuilder<EntityQueryBuilder>> builder = junctionBuilder.conjunction();
		builder.property( "groupId").eq( artifact.getGroupId());
		builder.property( "artifactId").eq( artifact.getArtifactId());
		builder.property( "version").eq( VersionProcessor.toString(artifact.getVersion()));
		builder.value( ProjectNature.eclipse).in().property( "natures");			
		builder.close();	
	}
	
	
	public List<SourceArtifact> runSourceArtifactQuery( String txt) {
		
		EntityQueryBuilder entityQueryBuilder = EntityQueryBuilder.from( SourceArtifact.class);
		ConditionBuilder<EntityQueryBuilder> conditionBuilder = entityQueryBuilder.where();
		
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = conditionBuilder.disjunction();
		String [] txts = txt.split( "\\|");
		if (txts.length > 1) {		
			for (String value : txts) {
				processSourceArtifact(value, junctionBuilder);
			}			
		} else {
			processSourceArtifact(txts[0], junctionBuilder);
		}
		junctionBuilder.close();
		
		EntityQuery query = entityQueryBuilder.done();
		
		try {			
			 List<?> bland = getSmood().queryEntities(query).getEntities();
			 @SuppressWarnings("unchecked")
			List<SourceArtifact> result = (List<SourceArtifact>) bland; 						 				
			return result != null ? new ArrayList<SourceArtifact>(result) : new ArrayList<SourceArtifact>();					
		} catch (Exception e) {
			
		}		
		return new ArrayList<SourceArtifact>();
	}
	
	private void processPartialSourceArtifact( String txt, JunctionBuilder<EntityQueryBuilder> junctionBuilder) {	
		String [] parts = txt.split( ":");
		if (parts.length != 2)
			return;
		
		JunctionBuilder<JunctionBuilder<EntityQueryBuilder>> builder = junctionBuilder.conjunction();
		builder.property( "groupId").eq( parts[0]);
		builder.property( "artifactId").eq( parts[1]);
		builder.value( ProjectNature.eclipse).in().property( "natures");			
		builder.close();	
	}
	/**
	 * run a query for a partial {@link SourceArtifact} - groupId, artifactId
	 * @param txt
	 * @return
	 */
	public List<SourceArtifact> runPartialSourceArtifactQuery( String txt) {
		
		EntityQueryBuilder entityQueryBuilder = EntityQueryBuilder.from( SourceArtifact.class);
		ConditionBuilder<EntityQueryBuilder> conditionBuilder = entityQueryBuilder.where();
		
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = conditionBuilder.disjunction();
		String [] txts = txt.split( "\\|");
		if (txts.length > 1) {		
			for (String value : txts) {
				processPartialSourceArtifact(value, junctionBuilder);
			}			
		} else {
			processPartialSourceArtifact(txts[0], junctionBuilder);
		}
		junctionBuilder.close();
		
		EntityQuery query = entityQueryBuilder.done();
		
		try {			
			 List<?> bland = getSmood().queryEntities(query).getEntities();
			 @SuppressWarnings("unchecked")
			List<SourceArtifact> result = (List<SourceArtifact>) bland; 						 				
			return result != null ? new ArrayList<SourceArtifact>(result) : new ArrayList<SourceArtifact>();					
		} catch (Exception e) {
			
		}		
		return new ArrayList<SourceArtifact>();
	}
	
	private File determineAssociatedFile( SourceArtifact s, String filename) throws ResolvingException {
		
		File projectFile;
		URL url;
		SourceRepository sourceRepository = s.getRepository();		
		if (sourceRepository == null) {
			String msg="cannot process [" + sourceArtifactToString(s) + "] as no source repository information is attached";
			throw new ResolvingException(msg);
		}
		try {
			url = new URL( sourceRepository.getRepoUrl() + "/" + s.getPath());
		} catch (MalformedURLException e1) {
			String msg="cannot determine origin of [" + sourceArtifactToString(s) + "] as the URL is invalid";					
			throw new ResolvingException(msg);
		}
		
		if (url.getProtocol().equalsIgnoreCase(URL_PROTOCOL_FILE)) {		
			projectFile = new File( url.getFile(), filename);
			return projectFile;
		}
		else {
			String msg="cannot find associated file [" +  filename +  "] for [" + sourceArtifactToString(s) + "] as protocol isn't supported";
			throw new ResolvingException(msg);
		}
		
	}

	private static String sourceArtifactToString( SourceArtifact artifact){
		 return artifact.getGroupId() +":" + artifact.getArtifactId() + "#" + artifact.getVersion(); 
	}
	
	
}
