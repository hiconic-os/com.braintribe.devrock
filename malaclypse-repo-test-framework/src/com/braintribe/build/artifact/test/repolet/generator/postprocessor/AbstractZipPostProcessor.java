// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet.generator.postprocessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.braintribe.build.artifact.test.repolet.generator.RepoletContentGeneratorException;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;

public abstract class AbstractZipPostProcessor {
	
	protected abstract void postProcess(File directory);
	
	public void postProcess( File in, File out) throws RepoletContentGeneratorException {
		
		File directory;
		try {
			directory = Files.createTempDirectory( "unpack_").toFile();
			directory.deleteOnExit();
		} catch (IOException e) {
			String msg ="cannot create temporary unpack directory";
			throw new RepoletContentGeneratorException(msg, e);
		}
		try {
			Archives.zip().from( in).unpack(directory);
		} catch (ArchivesException e) {
			String msg ="cannot unpack [" + in.getAbsolutePath() + "] to [" + directory.getAbsolutePath() + "]";
			throw new RepoletContentGeneratorException(msg, e);
		}
		
		postProcess( directory);
		
		try {
			Archives.zip().pack(directory).to( out);
		} catch (ArchivesException e) {
			String msg ="cannot pack [" + directory.getAbsolutePath() + "] to [" + in.getAbsolutePath() + "]";
			throw new RepoletContentGeneratorException(msg, e);
		}					
		
	}

}
