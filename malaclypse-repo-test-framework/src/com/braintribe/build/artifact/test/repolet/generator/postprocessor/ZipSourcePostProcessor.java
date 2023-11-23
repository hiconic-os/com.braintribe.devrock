// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet.generator.postprocessor;

import java.io.File;
import java.util.function.Predicate;

import com.braintribe.build.artifact.test.repolet.generator.RepoletContentGeneratorException;
import com.braintribe.build.artifact.test.repolet.generator.filter.PostProcessFileFilter;

/**
 * program that removes all sources contained within a processed zip-
 * @author pit
 *
 */
public class ZipSourcePostProcessor extends AbstractZipPostProcessor {
	
	private Predicate<File> filter;
	
	public void setFilter(Predicate<File> filter) {
		this.filter = filter;
	}
	
	@Override
	protected void postProcess( File directory) {
		File [] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				postProcess( file);
			}
			else {
				if (!filter.test(file)) {
					file.delete();
				}
			}
		}
	}
	
	public static void main( String [] args) {
		ZipSourcePostProcessor postProcessor = new ZipSourcePostProcessor();
		if (args.length % 3 != 0) {
			System.out.println("Usage : <in file> <outfile> sources/nosources");
			return;
		}
		
		for (int i = 0; i <= args.length - 3; i=i+3) {
			File in = new File( args[i]);
			File out = new File( args[i+1]);
			
			PostProcessFileFilter filter;
			
			if (args[i+2].equalsIgnoreCase( "sources")) {
				filter = new PostProcessFileFilter( "-sources.jar", false);
			}
			else {
				filter = new PostProcessFileFilter( "-sources.jar", true);
			}
			try {
				postProcessor.setFilter(filter);
				postProcessor.postProcess(in, out);
			} catch (RepoletContentGeneratorException e) {
				System.err.println("cannot postprocess " + e);
			}
		}
		
	}

}
