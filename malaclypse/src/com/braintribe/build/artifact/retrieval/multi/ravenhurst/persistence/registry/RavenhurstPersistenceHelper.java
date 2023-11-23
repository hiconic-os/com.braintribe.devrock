// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemLock;
import com.braintribe.logging.Logger;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.utils.date.ExtSimpleDateFormat;
import com.braintribe.utils.paths.PathList;

public class RavenhurstPersistenceHelper {
	private static Logger log = Logger.getLogger(RavenhurstPersistenceHelper.class);
	private static ExtSimpleDateFormat dateFormat = new ExtSimpleDateFormat( "yyyy.MM.dd.HH.mm.ss");
	public static final String UPDATE_DATA_STORAGE = "updateInfo"; 
	/**
	 * lists all files that exist for a certain {@link RavenhurstBundle}
	 * @param bundle - {@link RavenhurstBundle}
	 * @return - a {@link List} of {@link File}
	 */
	/**
	 * file is now different per repository id, each in a separate directory, and each information stored
	 * in a separate file. 
	 * @param bundle - the {@link RavenhurstBundle} to get the storage name for
	 * @return - a {@link File} that can be written to 
	 * @throws RepresentationException
	 */
	public static File deriveDumpFile(String localRepository, RavenhurstBundle bundle) throws RepresentationException {
		// can't use the rh response date as this may be identical now
		/*
		RavenhurstResponse ravenhurstResponse = bundle.getRavenhurstResponse();
		Date date = null;
		if (ravenhurstResponse != null) {
			date = ravenhurstResponse.getResponseDate();
		}
		else {
			date = new Date();
		}
		*/
		Date date = new Date();
		String suffix = dateFormat.format(date);
		File directory = PathList.create().push( localRepository).push( UPDATE_DATA_STORAGE).push( bundle.getRepositoryId()).toFile();
		directory.mkdirs();
		File bundleDump = new File( directory, suffix + ".interogation");
		return bundleDump;
	}
	
	
	/**
	 * read the file name and reflect the date from the name 
	 * @param file - the interrogation protocol {@link File} in question
	 * @return - the {@link Date} extracted from its name 
	 * @throws ParseException - if we can't parse 
	 */
	public static Date retrieveDateFromName( File file) throws ParseException {
		String name = file.getName();
		
		int endIndex = name.indexOf( ".interogation");
		if (endIndex <= 0) {
			return null;
		}
		String dateAsString = name.substring(0, endIndex);
		
		synchronized (dateFormat) {
			Date date = dateFormat.parse(dateAsString);
			return date;			
		}
	}
	

	
	/**
	 * retrieve all interrogation files for a bundle
	 * @param localRepository - the path to the local repository 
	 * @param bundle - the {@link RavenhurstBundle} 
	 * @return - a {@link List} of all interrogation {@link File}
	 */
	public static List<File> getPersistedFilesForBundle(String localRepository, RavenhurstBundle bundle) {
	
		File directory = PathList.create().push( localRepository).push( UPDATE_DATA_STORAGE).push( bundle.getRepositoryId()).toFile();
		if (!directory.exists())
			return Collections.emptyList();
		
		List<File> result = new ArrayList<>();
		
		File [] files = directory.listFiles();
		for (File file : files) {
			// don't parse lock files 
			if (
					!file.getName().endsWith( FilesystemLock.FILESYSTEM_LOCK_SUFFIX) &&
					!file.getName().equalsIgnoreCase( ".index")
				) {
				result.add( file);
			}
		}
		
		// sort so newest file is first 
		result.sort( new Comparator<File>() {

			@Override
			public int compare(File arg0, File arg1) {
				try {
					Date a = retrieveDateFromName(arg0);
					Date b = retrieveDateFromName(arg1);

					return a.compareTo(b);
				}
				catch (Exception e) {
					throw new IllegalStateException("boing", e);
				}
			}
		});
		
		return result;
	}
	
	
	/**
	 * 
	 * @param reader
	 * @param bundle
	 * @param prunePeriod
	 * @return
	 * @throws RepresentationException
	 */
	public static List<String> purge( MavenSettingsReader reader, RavenhurstBundle bundle, int prunePeriod) throws RepresentationException {		
		// load from file 
		List<File> files = RavenhurstPersistenceHelper.getPersistedFilesForBundle( reader.getLocalRepository(null), bundle);
		if (files == null || files.size() == 0) {
			return Collections.emptyList();
		}
				
		if (prunePeriod == 0) {
			return Collections.emptyList();
		}
		 			
		List<String> errormsg = new ArrayList<>();
		
		Date now = new Date();
		long nowInMillis = now.getTime();
		long i = ((long) prunePeriod) * 24 * 60 * 60 * 1000;
		long thresholdInMillis = nowInMillis - i;
		Date threshold = new Date( thresholdInMillis);
		
		for (File file : files) {
			Date fileDate;
			try {
				fileDate = RavenhurstPersistenceHelper.retrieveDateFromName(file);
			} catch (ParseException e) {
				String msg="cannot extract date from file [" + file.getAbsolutePath() + "]'s name";
				log.warn( msg, e);
				errormsg.add(msg + " as " + e.getMessage());
				continue;
			}
			if (fileDate != null && fileDate.before(threshold)) {
				try {
					file.delete();
				} catch (Exception e) {
					String msg="cannot delete file [" + file.getAbsolutePath() + "]";
					log.warn( msg, e);
					errormsg.add(msg + " as " + e.getMessage());
				}
			}
			
		}
		return errormsg;
	}


	/**
	 * @param localRepositoryAsString
	 * @param bundle
	 * @return
	 */
	public static File deriveIndexFile(String localRepositoryAsString, RavenhurstBundle bundle) {
		File directory = PathList.create().push( localRepositoryAsString).push( UPDATE_DATA_STORAGE).push( bundle.getRepositoryId()).toFile();
		directory.mkdirs();
		File bundleDump = new File( directory, ".index");		
		return bundleDump;
	}


	public static void main(String[] args) {
		RavenhurstBundle bundle = RavenhurstBundle.T.create();
		bundle.setRepositoryId( "core-dev");
		
		List<File> files = RavenhurstPersistenceHelper.getPersistedFilesForBundle( "c:/users/pit/.m2/repository-groups", bundle);
		System.out.println( files.stream().map( f -> f.getName()).collect( Collectors.joining("\n")));
	}
}
