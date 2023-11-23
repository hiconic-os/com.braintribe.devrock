// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet.generator.postprocessor;

import java.io.File;
import java.util.Date;
import java.util.function.Predicate;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.LockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.MavenMetadataPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.build.artifact.test.repolet.generator.RepoletContentGeneratorException;
import com.braintribe.build.artifact.test.repolet.generator.filter.SimpleFileNameFilter;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.meta.Versioning;

/**
 * updates all versioning information (last updated in the zip) 
 * @author Pit
 *
 */
public class MetadataLastUpdatedPostProcessor extends AbstractZipPostProcessor {
	private Predicate<File> filter;
	private Date date = new Date();
	private LockFactory lockFactory = new FilesystemSemaphoreLockFactory();
	
	@Required @Configurable
	public void setFilter(Predicate<File> filter) {
		this.filter = filter;
	}
	
	@Configurable
	public void setDate(Date date) {
		this.date = date;
	}
	
	@Override
	protected void postProcess(File directory) {
		File [] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				postProcess( file);
			}
			else {
				if (filter.test(file)) {
					// touch metadata 
					try {
						MavenMetaData metadata = MavenMetadataPersistenceExpert.decode( lockFactory, file);
						Versioning versioning = metadata.getVersioning();
						if (versioning != null) {
							versioning.setLastUpdated( date);
							MavenMetadataPersistenceExpert.encode(lockFactory, metadata, file);
						}
					} catch (RepositoryPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		}

	}
	public static void main( String [] args) {
		MetadataLastUpdatedPostProcessor postProcessor = new MetadataLastUpdatedPostProcessor();
		if (args.length % 2 != 0) {
			System.out.println("Usage : <in file> <outfile>");
			return;
		}		
		
		for (int i = 0; i <= args.length - 2; i=i+3) {
			File in = new File( args[i]);
			File out = new File( args[i+1]);
			
			SimpleFileNameFilter filter = new SimpleFileNameFilter( "maven-metadata.xml");
			try {
				postProcessor.setFilter(filter);
				postProcessor.postProcess(in, out);
			} catch (RepoletContentGeneratorException e) {
				System.err.println("cannot postprocess " + e);
			}
		}
		
	}

}
