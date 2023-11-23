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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.FilesystemError;
import com.braintribe.gm.model.reason.essential.NotFound;

/**
 * purge any *.lck files and return - if required - reasoning why it didn't properly work
 * @author pit
 *
 */
public class FilesystemLockPurger {
	private File repoRoot;
	
	@Configurable @Required
	public void setRepositoryRoot(String repositoryRoot) {
		this.repoRoot = new File(repositoryRoot);
	}
	
	/**
	 * deletes all *.lck files in the directory structure
	 * @param repoRoot - the main entry point (most likely a local repository)
	 * @return - a {@link Maybe} with a {@link Pair} of the number of lock-files and their {@link File}s
	 */
	public Maybe<Pair<Integer, List<File>>> purgeFilesytemLockFiles() {
		if (!repoRoot.exists()) { // no local repository there
			return Maybe.empty( Reasons.build( NotFound.T).text( "Passed local repository doesn't exist :" + repoRoot.getAbsolutePath()).toReason());
		}
		List<File> scannedLockFiles = scan(repoRoot);
		int numLockFiles = scannedLockFiles.size();
		
		List<File> noDeletion = new ArrayList<>();
		List<File> removed = new ArrayList<>();
		for (File file : scannedLockFiles) {
			try {
				file.delete();
				removed.add( file);
			} catch (Exception e) {
				noDeletion.add(file);
			}
		}
		
		
		if (numLockFiles == 0) { // no files to be purged
			return Maybe.complete(Pair.of( numLockFiles, Collections.emptyList()));
		}
		else if (noDeletion.size() > 0) { // some files couldn't be purged
			Reason umbrellaReason = Reasons.build(FilesystemError.T).text("cannot fully purge local repository at :" + repoRoot.getAbsolutePath()).toReason();
			for (File file : noDeletion) {
				umbrellaReason.getReasons().add( Reasons.build( FilesystemError.T).text("cannot delete file :" + file.getAbsolutePath()).toReason());
			}
			return Maybe.incomplete(Pair.of( numLockFiles, scannedLockFiles), umbrellaReason);
		}
		else { // everything went fine
			return Maybe.complete( Pair.of(numLockFiles, removed));
		}				
	}
	
	/**
	 * recursively scan for *.lck files below the passed directory
	 * @param repoRoot - the entry point of this scan
	 * @return - a {@link List} of matching files
	 */
	private List<File> scan(File repoRoot) {
		List<File> result = new ArrayList<>();
		File[] files = repoRoot.listFiles();
		if (files != null && files.length > 0) {
			for (File file : files) {
				if (file.isDirectory()) {
					result.addAll( scan( file));
				}
				else {
					if (file.getName().endsWith(".lck")) {
						result.add(file);		
					}				
				}
			}
		}		
		return result;
	}
}
