// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

public class SolutionPartReflectionContainerPersistenceExpert {
	private static Logger log = Logger.getLogger(SolutionPartReflectionContainerPersistenceExpert.class);
	public static final String RAVENHURST_SOLUTION_PART_CONTAINER = ".solution";	
	
	public static File getLocationForBundle( File location, String bundleId) {
		return new File( location, bundleId + RAVENHURST_SOLUTION_PART_CONTAINER);
	}
	
	
	public static List<String> decode(File location, String bundleId) {
		File containerFile = getLocationForBundle(location, bundleId);
		List<String> result = new ArrayList<String>();
		try {
			String contents = IOTools.slurp( containerFile, "UTF-8");
			String [] lines = contents.split("\n");
			for (String line : lines) {
				line = line.trim();
				if (line.startsWith(";") || line.length() == 0)
					continue;
				result.add( line);
			}
			
		} catch (IOException e) {
			log.error("cannot read file [" + containerFile.getAbsolutePath() + "]", e);
		}
		return result;
	}
	
	
	public static void encode( File location, String bundleId, List<String> files) {
		File containerFile = getLocationForBundle(location, bundleId);
		containerFile.getParentFile().mkdirs();
		StringBuilder builder = new StringBuilder();
		for (String file : files) {
			if (builder.length() > 0) {
				builder.append("\n");
			}
			builder.append( file);
		}
		try {
			IOTools.spit(containerFile,  builder.toString(), "UTF-8", false);
		} catch (IOException e) {
			log.error("cannot write to file [" + containerFile.getAbsolutePath() + "]", e);
		}
	}
	
}
