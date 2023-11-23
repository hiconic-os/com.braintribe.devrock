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
package com.braintribe.devrock.mc.core.configuration.bias;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.AllMatchingArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.NegationArtifactFilter;
import com.braintribe.devrock.model.repository.filters.NoneMatchingArtifactFilter;
import com.braintribe.devrock.model.repository.filters.QualifiedArtifactFilter;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.utils.IOTools;

/**
 * the compiler that turns 'legacy' style .pc_bias files into 'next gen' style filters
 * <br/>
 * the bias is represented by filters,
 * i.e. a standard pc_bias is turned into a dominance filter, i.e. if a repository is 
 * deemed to be dominant, only its present versions are taken, and others ignored (if it finds any content).
 * <br/>
 * blocking filters (marked with ! in the file) are turned into standard {@link QualifiedArtifactFilter} and negated. 
 * Attached to the standard {@link ArtifactFilter} as passed via {@link RepositoryConfiguration}  
 * 
 * @author pit
 *
 */
public class PcBiasCompiler {
	public static final String BIAS_FILENAME =".pc_bias";
	private Map<String, Pair<ArtifactFilter,ArtifactFilter>> map;
	private Function<File,ReadWriteLock> lockProvider;
	private File localRepository;
	
	/**
	 * @param root - the {@link File} pointing to the root of the local repository
	 */
	@Configurable @Required
	public void setLocalRepository(File root) {
		this.localRepository = root;
	}

	@Configurable @Required
	public void setLockSupplier(Function<File, ReadWriteLock> lockProvider) {
		this.lockProvider = lockProvider;
	}
		
	
	/**
	 * loads the default bias file (local-repository/.pc_bias)
	 */
	public boolean loadPcBias() {		
		return load( getBiasFile());
	}
	
	/**
	 * loads the specified bias file 
	 * @param file
	 */
	public boolean loadPcBias( File file) {
		return load( file); 
	}
	
	
	/**
	 * load bias file and convert into {@link ArtifactFilter}
	 * @param biasFile
	 */
	private boolean load(File biasFile) {
		map = new HashMap<>();
		
		if (!biasFile.exists()) {
			return false;
		}
		
		Lock lock = lockProvider.apply(biasFile).readLock();
		
		
		Map<String, List<ArtifactIdentification>> dominanceExpressions = new HashMap<>();
		Map<String, List<ArtifactIdentification>> blockingExpressions = new HashMap<>();
		Set<String> repositories = new HashSet<>();
		
		try {
			lock.lock();
		
			// parse file 
			String contents = IOTools.slurp( biasFile, "UTF-8");
			String [] lines = contents.split("\n");
			for (String line : lines) {
				line = line.trim();
				if (line.length() == 0)
					continue;
				
				// parse line 
				Pair<ArtifactIdentification, Pair<List<String>,List<String>>> processedFilters = parseLine(line);
				
				// convert to filter expressions 
				List<String> dominantRepositories = processedFilters.second.first;
				List<String> filteredRepositories = processedFilters.second.second;
				
				// add all repositories to get a set of all repositories in play 
				repositories.addAll( dominantRepositories);
				repositories.addAll(filteredRepositories);
				
				
				// positive expressions -> dominance 
				for (String dominantRepo : dominantRepositories) {
					List<ArtifactIdentification> ais = dominanceExpressions.computeIfAbsent( dominantRepo, (k) -> new ArrayList<>());
					ais.add( processedFilters.first);
				}
				// negative expressions -> blocked 
				for (String blockedRepo : filteredRepositories) {
					List<ArtifactIdentification> ais = blockingExpressions.computeIfAbsent( blockedRepo, (k) -> new ArrayList<>());
					ais.add( processedFilters.first);
				}												
			}	
			
			// dominance filters -> true if dominant, false otherwise
			for (String repo : repositories) { 
				List<ArtifactIdentification> aisInDominance = dominanceExpressions.get( repo);
				ArtifactFilter dominanceFilter = NoneMatchingArtifactFilter.T.create();
				if (aisInDominance != null && !aisInDominance.isEmpty()) {
					if (aisInDominance.size() > 1) {
						DisjunctionArtifactFilter daf = DisjunctionArtifactFilter.T.create();
						for (ArtifactIdentification ai : aisInDominance) {
							ArtifactFilter af = buildFilterFromArtifactIdentification(ai);
							daf.getOperands().add(af);
						}
						dominanceFilter = daf;
					}
					else {
						ArtifactIdentification ai = aisInDominance.get(0);
						dominanceFilter = buildFilterFromArtifactIdentification(ai);
					
					}
				}
				// blocking filter -> false if matches, otherwise true 
				List<ArtifactIdentification> aisInBlock = blockingExpressions.get( repo);
				ArtifactFilter blockingFilter = AllMatchingArtifactFilter.T.create();
				if (aisInBlock != null && !aisInBlock.isEmpty()) {
					NegationArtifactFilter naf = NegationArtifactFilter.T.create();
					if (aisInBlock.size() > 1) {
						DisjunctionArtifactFilter daf = DisjunctionArtifactFilter.T.create();
						for (ArtifactIdentification ai : aisInBlock) {
							ArtifactFilter af = buildFilterFromArtifactIdentification(ai);
							daf.getOperands().add(af);
						}
						naf.setOperand(daf);
					}
					else {
						ArtifactIdentification ai = aisInBlock.get(0);
						naf.setOperand( buildFilterFromArtifactIdentification(ai));
					}
					blockingFilter = naf;
				}				
				map.put( repo, Pair.of( dominanceFilter, blockingFilter));
			}
			return true;
		}
		catch( Exception e) {
			Exceptions.unchecked(e, "cannot load bias file [" +biasFile.getAbsolutePath() + "]", IllegalStateException::new);
		}
		finally {
			lock.unlock();
		}	
		return false;
	}

	/**
	 * @param ai - the {@link ArtifactIdentification}
	 * @return - a {@link QualifiedArtifactFilter} that represents the filter
	 */
	private QualifiedArtifactFilter buildFilterFromArtifactIdentification(ArtifactIdentification ai) {
		QualifiedArtifactFilter qaf = QualifiedArtifactFilter.T.create();
		String groupId = ai.getGroupId();
		qaf.setGroupId( groupId);
		if (!ai.getArtifactId().equals( ".*")) {			
			qaf.setArtifactId( ai.getArtifactId());
		}
		return qaf;
	}
	
	/**
	 * @param line
	 * @return - Pair of RepositoryId and Pair of 'active repositories' and 'inactive repositories'
	 */
	private Pair<ArtifactIdentification, Pair<List<String>, List<String>>> parseLine( String string) {
		/*
		 * <groupid>[:<artifactId>][;[!]<repository>[,[!]<repository>],...]
		 */
		String line = string.trim();
		int pScolon = line.indexOf(';');

		String artifactExpression = null;
		String repositoryExpression = null; 
		
		if (pScolon < 0) {
			// no semicolon ->  all is artifact expression  		
			artifactExpression = line;
		}
		else {
			// split into artifact & repository expression
			artifactExpression = line.substring(0, pScolon);
			repositoryExpression = line.substring( pScolon + 1);
		}
		
		ArtifactIdentification ai = ArtifactIdentification.T.create();
		int pColon = artifactExpression.indexOf(':');
		if (pColon < 0) {
			ai.setGroupId( artifactExpression);
			ai.setArtifactId( ".*");
		}
		else {
			ai.setGroupId( artifactExpression.substring(0, pColon));
			ai.setArtifactId( artifactExpression.substring(pColon+1));
		}
		
		List<String> activeRepositories = new ArrayList<>();
		List<String> inactiveRepositories = new ArrayList<>();
		
		if (repositoryExpression != null) {
			String [] repos = repositoryExpression.split( ",");
			for (String repo : repos) {
				if (repo.startsWith( "!")) {
					inactiveRepositories.add( repo.substring(1));					
				}
				else {
					activeRepositories.add( repo);
				}
			}
		}
		else {
			activeRepositories.add( "local");
		}
		
		return Pair.of( ai, Pair.of( activeRepositories, inactiveRepositories));
	}
	
	/**
	 * returns the filters as specified in the bias file
	 * @param repositoryId - the name (id) of the repository
	 * @return - a {@link Pair} of the {@link ArtifactFilter} expressing dominance and the {@link ArtifactFilter} expressing filter expressions
	 */
	public Pair<ArtifactFilter,ArtifactFilter> getBiasFilters(String repositoryId) {
		Pair<ArtifactFilter, ArtifactFilter> pair = map.get( repositoryId);
		if (pair == null) {
			// dominance : none matching, block filter: all matching
			return Pair.of( NoneMatchingArtifactFilter.T.create(), AllMatchingArtifactFilter.T.create());
		}
		else {
			return pair;
		}
	}
	
	public Pair<ArtifactFilter,ArtifactFilter> findBiasFilters(String repositoryId) {
		return map.get( repositoryId);
	}

	public File getBiasFile() {
		return new File( localRepository, BIAS_FILENAME);
	}
	
	
}
