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
package com.braintribe.devrock.mc.core.configuration.maven;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.braintribe.devrock.model.repository.ChecksumPolicy;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.MavenRepository;
import com.braintribe.model.artifact.maven.settings.Repository;
import com.braintribe.model.artifact.maven.settings.RepositoryPolicy;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;


/**
 * helper for the {@link MavenSettingsCompiler} 
 * 
 * @author pit
 *
 */
public class Transposer {
	 
	private static final String INTERVAL = "interval:";
	private static final String DAILY = "daily";
	private static final String ALWAYS = "always";
	private static final String NEVER = "never";

	private static TimeSpan transposeUpdateTimespan( RepositoryPolicy policy) {
		String updatePolicy = policy.getUpdatePolicy();
		TimeSpan updateTimespan = TimeSpan.T.create();
		if (updatePolicy != null) {
			switch( updatePolicy) {
			case NEVER:
				updateTimespan = null;
				break;
			case ALWAYS:
				updateTimespan.setValue(0);
				break;
			case DAILY:
				updateTimespan.setUnit(TimeUnit.day);
				updateTimespan.setValue(1);
				break;
			default : {
				if (updatePolicy.startsWith( INTERVAL)) {
					String value = updatePolicy.substring( INTERVAL.length());
					updateTimespan.setUnit( TimeUnit.minute);
					updateTimespan.setValue( Double.parseDouble(value));
				}
				break;
			}
			}					
			return updateTimespan;		
		} else {
			updateTimespan.setUnit(TimeUnit.day);
			updateTimespan.setValue(1);
			return updateTimespan;		
		}		
	}
	private static ChecksumPolicy transposeCheckSumPolicy( RepositoryPolicy policy) {
		String checksumPolicy = policy.getChecksumPolicy();
		if (checksumPolicy == null) {
			return  ChecksumPolicy.ignore;
		}
		else {
			return ChecksumPolicy.valueOf(checksumPolicy);
		}		
	}
	
	private static URI uriFromString(String s) {
		try {
			return new URI(s);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param repository - the Repository to transpose 
	 * @return - the {@link com.braintribe.devrock.model.repository.Repository} transposed
	 */
	public static List<com.braintribe.devrock.model.repository.Repository> transpose( Repository repository) {		
		List<com.braintribe.devrock.model.repository.Repository> result = new ArrayList<>();

		String scheme = Optional.ofNullable(repository.getUrl()).map(Transposer::uriFromString).map(URI::getScheme)
				.orElseThrow(() -> new IllegalStateException("Repository [" + repository.getId() + "] has an invalid url."));
		
		final Supplier<MavenRepository> repoSupplier;
		
		switch (scheme) {
		case "http":
		case "https":
			repoSupplier = () -> {
				MavenHttpRepository mavenHttpRepository = MavenHttpRepository.T.create();
				mavenHttpRepository.setUrl(repository.getUrl());
				return mavenHttpRepository;
			};
			
			break;
		case "file":
			repoSupplier = () -> {
				MavenFileSystemRepository mavenFileSystemRepository = MavenFileSystemRepository.T.create();
				mavenFileSystemRepository.setRootPath(new File(uriFromString(repository.getUrl())).getAbsolutePath());
				return mavenFileSystemRepository;
			};
			
			break;
		default:
			throw new IllegalStateException("Unsupported url scheme for repository url: " + repository.getUrl());
		}
		
		RepositoryPolicy releases = repository.getReleases();
		if (releases != null && releases.getEnabled() != null && releases.getEnabled()) {
			result.add(completeRepoConfiguration(repository, releases, repoSupplier, false));
		}
		
		RepositoryPolicy snapshots = repository.getSnapshots();
		if (snapshots != null && snapshots.getEnabled() != null && snapshots.getEnabled()) {
			result.add(completeRepoConfiguration(repository, snapshots, repoSupplier, true));
		}		
		
		return result;
	}
	private static MavenRepository completeRepoConfiguration(Repository repository, RepositoryPolicy policy,
			final Supplier<MavenRepository> repoSupplier, boolean snapshot) {
		
		MavenRepository configurationRepository = repoSupplier.get();
		TimeSpan updateTimespan = transposeUpdateTimespan( policy);		
		configurationRepository.setUpdateTimeSpan( updateTimespan);
		// if a timespan was declared (aka not 'never' in maven), then the repository is 'updateable'
		if (updateTimespan != null) {
			configurationRepository.setUpdateable(true);
		}
		else {
			configurationRepository.setUpdateable(false);	
		}
				
		if (configurationRepository instanceof MavenHttpRepository) {
			MavenHttpRepository mavenHttpRepository = (MavenHttpRepository)configurationRepository;
			mavenHttpRepository.setCheckSumPolicy( transposeCheckSumPolicy(policy));
		}
		
		configurationRepository.setName( repository.getId());
		configurationRepository.setSnapshotRepo(snapshot);
		
		return configurationRepository;
	}
	
	/**
	 * @param urlAsString - a {@link String} representation of an {@link URL}
	 * @return - true if the URL references an external host 
	 */
	public static boolean isExternalUri(String urlAsString) {
		URI uri = URI.create( urlAsString);			
		return isExternalUrl(uri);
	}
	
	/**
	 * @param uri - the {@link URI} to test
	 * @return - true if the URI references an external host
	 */
	public static boolean isExternalUrl( URI uri) {
		String protocol = uri.getScheme();
		if (protocol == null)
			return false;
		if (protocol.equalsIgnoreCase("file")) {
			return false;
		}
		
		String host = uri.getHost(); 
		if (host != null && host.startsWith( "localhost")) {
			return false;
		}	
		
		return true;
	}
	

	/**
	 * @param key - the key (mostly the id of a repository)
	 * @param expression - the expression (as from a mirror) 
	 * @param external - true if the passed id is of an external nature 
	 * @return - true if the id is noted, false if negated or not present 
	 */
	public static boolean isRepositoryNotedInPropertyValue(String key, String expression, boolean external) {
		String [] dynamics = expression.split( ",");
		// sort so that all negating are in front
		
		List<String> recombined = moveNegatorsToFrontInList(dynamics);
		
		for (String dynamic : recombined) {
			dynamic = dynamic.trim();
			boolean reverse = false;
			if (dynamic.startsWith( "!")) {
				dynamic = dynamic.substring(1);
				reverse = true;
			}			
			if (dynamic.startsWith( "external:")) {
				if (!external) {
					continue;
				}
				dynamic = dynamic.substring( "external:".length());
			}
		
			dynamic = dynamic.replaceAll("\\*", ".*");
			if (key.matches( dynamic))
				if (!reverse)
					return true;
				else
					return false;
		}
		return false;
	}
	/**
	 * @param expressions - makes sure the negators are first in the list (short-circuit logic) 
	 * @return - a rearragned {@link List} of the expressions
	 */
	private static List<String> moveNegatorsToFrontInList(String[] expressions) {
		List<String> tpList = new ArrayList<String>( Arrays.asList( expressions));
		List<String> negator = new ArrayList<String>();
		Iterator<String> iterator = tpList.iterator();
		while (iterator.hasNext())  {
			String s = iterator.next();
			if (s.startsWith( "!")) {
				iterator.remove();
				negator.add( s);
			}
		}
		List<String> recombined = new ArrayList<String>();
		recombined.addAll(negator);
		recombined.addAll(tpList);
		return recombined;
	}
}
