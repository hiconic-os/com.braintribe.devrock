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
package com.braintribe.devrock.mc.core.commons;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.reason.essential.NotFound;

/**
 * simple tool to track-down index files of specified repositories and marking them as out-of-date 
 * @author pit
 *
 */
public class RepositoryOutdater {
	
	private final static String SUFFIX_OUTDATED = ".outdated";
	
	private File localRepository;
	private List<String> repositories;
	
	/**
	 * the index files that need to be refreshed 
	 */
	private List<String> filesToScanFor; 
	{
		filesToScanFor = new ArrayList<>();
		filesToScanFor.add( "maven-metadata-${repo}.xml");
		filesToScanFor.add( "part-availability-${repo}.txt");
		filesToScanFor.add( "part-availability-${repo}.artifactory.json");
	}
	
	/**
	 * @param localRepository - the {@link File} that represents the root of the local repository 
	 */
	@Configurable @Required
	public void setLocalRepository(File localRepository) {
		this.localRepository = localRepository;
	}
	
	/**
	 * @param repositories - a {@link List} of repository ids 
	 */
	@Configurable @Required
	public void setRepositories(List<String> repositories) {
		this.repositories = repositories;
	}
	
	
	/**
	 * @return - null if everything went fine, a {@link Reason} detailing the issue otherwise 
	 */
	public Reason outdateRepositories() {
		
		if (localRepository == null) {
			return Reasons.build(InvalidArgument.T).text("a local repository path must be passed" ).toReason();
		}
		if (!localRepository.exists()) {
			return Reasons.build(NotFound.T).text("specified local repository path doesn't exist: " + localRepository.getAbsolutePath()).toReason();
		}
		
		if (repositories == null || repositories.size() == 0) {
			return Reasons.build(InvalidArgument.T).text("at least one repository to be set out-of-date must be passed" ).toReason();
		}
		Set<String> matches = new HashSet<>();
		for (String repositoryName : repositories) {
			for (String fileToScanFor : filesToScanFor) {
				matches.add( fileToScanFor.replace("${repo}", repositoryName));
			}
		}
		
		Pair<List<String>, List<String>> pair = outdateFiles( localRepository, matches);
		if (pair.second.size() > 0) {
			Reasons.build(null);
		}
		
		return null;
	}

	/**
	 * actually do the outdating - i.e. create '.outdated' files 
	 * @param folder - the {@link File} pointing to the folder
	 * @param matches - the name of the files to outdate
	 * @return - a {@link Pair} of Files outdated and Files were it failed to create a .outdated for
	 */
	private Pair<List<String>, List<String>> outdateFiles(File folder, Collection<String> matches) {
		File [] files = folder.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				String name = pathname.getName().toLowerCase();
				for (String suspect : matches) {
					// as suspect may contain wild-cards, hence matches rather than direct comparison
					if (name.matches(suspect)) {
						return true;
					}
				}				
				return false;
			}
		});
		
		Pair<List<String>, List<String>> result = null;
		List<String> done = new ArrayList<>();
		List<String> failed = new ArrayList<>();
		
		for (File file : files) {
			// directory : recursive call 
			if (file.isDirectory()) {
				Pair<List<String>, List<String>> pair = outdateFiles(file, matches);
				if (result == null) {
					result = pair;
				}
				else {
					result.first.addAll( pair.first);
					result.second.addAll( pair.second);
				}
			}
			else {
				// single file : mark as outdated 
				File outdated = new File( file.getParentFile(), file.getName() + SUFFIX_OUTDATED);
				if (outdated.exists()) {
					continue;
				}
				try {
					Files.createFile( outdated.toPath());
					done.add( outdated.getAbsolutePath());
				} catch (IOException e) {
					done.add( outdated.getAbsolutePath());
				}				
			}
		}
		
		// no result yet, we're the first, create it 
		if (result == null) {
			result = Pair.of( done, failed);
		}
		else { // add the ones we managed 
			result.first.addAll(done);
			result.second.addAll(failed);
		}
		return result;
	}
}
