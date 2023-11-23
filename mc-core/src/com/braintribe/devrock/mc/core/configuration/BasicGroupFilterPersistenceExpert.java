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
package com.braintribe.devrock.mc.core.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.GroupsArtifactFilter;
import com.braintribe.logging.Logger;

/**
 * expert to handle the persistence of the group filter data 
 * 
 * @author pit
 *
 */
public class BasicGroupFilterPersistenceExpert {
	private static Logger log = Logger.getLogger(BasicGroupFilterPersistenceExpert.class);
	private static final String GROUP_FILTER = "group-index";	
	private Function<File, ReadWriteLock> lockSupplier;
	
	@Configurable @Required
	public void setLockSupplier(Function<File, ReadWriteLock> lockSupplier) {
		this.lockSupplier = lockSupplier;
	}
	
	/**
	 * determine the name of the file with respective persisted data 
	 * @param localRepository - {@link File} pointing to the root of the local repository 
	 * @param repository - the {@link Repository}
	 * @return - the {@link File} containing the data 
	 */
	private  File deriveFile( File localRepository, Repository repository) {
		return new File( localRepository, GROUP_FILTER + "-" + repository.getName() + ".txt");
	}
	
	/**
	 * checks if the persistence file exists
	 * @param localRepository - {@link File} pointing to the root of the local repository 
	 * @param repository - the {@link Repository}
	 * @return - true if it exists, false otherwise 
	 */
	public boolean groupFileExists(File localRepository, Repository repository) {
		return deriveFile(localRepository, repository).exists();
	}
		
	/**
	 * gets the content of the group file 
	 * @param localRepository - {@link File} pointing to the root of the local repository 
	 * @param repository - the {@link Repository}
	 * @return - a List of the group ids
	 */
	private SortedSet<String> loadGroupFilterings( File localRepository, Repository repository){
		File file = deriveFile(localRepository, repository);	

		ReadWriteLock readWriteLock = lockSupplier.apply(file);
		
		Lock readLock = readWriteLock.readLock();
		
		readLock.lock();
		try {
			return loadGroupFilteringsFromFile(file, repository);
		} 
		finally {
			readLock.unlock();
		}
	}
	
	
	private SortedSet<String> loadGroupFilteringsFromFile( File file, Repository repository){
		SortedSet<String> result = new TreeSet<>();
		
		if (!file.exists()) {
			return new TreeSet<>();
		}
		
		try (BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream(file), "UTF-8"))) {				
			String line;
			while ((line = reader.readLine()) != null) 	{			
				String trimmed = line.trim();
				if (trimmed.length() > 0)
					result.add( trimmed);				
			}
		} catch (Exception e) {
			throw new RuntimeException("cannot read group index file [" + file.getAbsolutePath() + "] assigned to repository [" + repository.getName() + "]", e);				
		}
		
		return result;
	}
	
	/**
	 * completely writes the group index data for the passed repository 
	 * @param localRepository - {@link File} pointing to the root of the local repository 
	 * @param repository - the {@link Repository}
	 * @param groups - the groups to write 
	 */
	public void writeGroupFilterings( File localRepository, Repository repository, Collection<String> groups) {
		if (groups.size() == 0)
			return;
		
		File file = deriveFile(localRepository, repository);
		ReadWriteLock readWriteLock = lockSupplier.apply(file);
		
		Lock writeLock = readWriteLock.writeLock();
		
		writeLock.lock();
		try ( 
				OutputStream out = new FileOutputStream(file);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter bw = new BufferedWriter(writer);
			) {
			for (String group : groups) {
				bw.write(group); 
				bw.write("\n");
			}
		}
		catch( Exception e) {
			log.error("cannot write group index file [" + file.getAbsolutePath() + "] assigned to repository [" + repository.getName() + "]");
		}		
		finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * writes partial information (additional data) to the persisted data 
	 * @param localRepository - {@link File} pointing to the root of the local repository 
	 * @param repository - the {@link Repository}
	 * @param groups - the groups to add 
	 */
	public void appendGroupFilterings( File localRepository, Repository repository, Collection<String> groups) {
		File file = deriveFile(localRepository, repository);
		ReadWriteLock readWriteLock = lockSupplier.apply(file);
		
		Lock writeLock = readWriteLock.writeLock();
		
		writeLock.lock();
		try {
			Set<String> allGroups = loadGroupFilteringsFromFile(file, repository);
			allGroups.addAll(groups);
			
			try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
				for (String group : allGroups) {
					bw.write(group); 
					bw.write("\n");
				}
			}
			catch (Exception e) {
				log.error("cannot append to group index file [" + file.getAbsolutePath() + "] assigned to repository [" + repository.getName() + "]");
			}
		}
		finally {
			writeLock.unlock();
		}
	}
	
	
	/**
	 * get the respective fully instrumented {@link ArtifactFilter} for the passed {@link Repository} 
	 * @param localRepository - {@link File} pointing to the root of the local repository 
	 * @param repository - the {@link Repository}
	 * @return - a fully instrumented {@link GroupsArtifactFilter}
	 */
	public ArtifactFilter getGroupFilter(File localRepository, Repository repository) {
		GroupsArtifactFilter gaf = GroupsArtifactFilter.T.create();
		Collection<String> groups = loadGroupFilterings(localRepository, repository);		
		gaf.getGroups().addAll( groups);
		return gaf;
	}

}
