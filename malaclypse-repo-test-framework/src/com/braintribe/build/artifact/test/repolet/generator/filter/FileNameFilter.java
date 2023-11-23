// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet.generator.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FileNameFilter implements Predicate<File> {
	
	private List<String> prefixes = new ArrayList<String>();
	private List<String> suffixes = new ArrayList<String>();
	private List<String> names = new ArrayList<String>();

	public FileNameFilter() {
		prefixes.add( ".update");
		prefixes.add( ".braintribe");
		
		names.add("ravenhurst.update.data");
		names.add("interrogation.rar");
	}
	
	public void setPrefixes(List<String> prefixes) {
		this.prefixes.addAll(prefixes);
	}
	
	public void setSuffixes(List<String> suffixes) {
		this.suffixes.addAll(suffixes);
	}
	
	public void setNames(List<String> names) {
		this.names.addAll(names);
	}
	@Override
	public boolean test(File file) {
		if (file.isFile()) {
			String name = file.getName();
			for (String prefix : prefixes) {
				if (name.startsWith(prefix)) 
					return false;
			}
			for (String suffix : suffixes) {
				if (name.endsWith( suffix))
					return false;
			}		
			
			for (String suspect : names) {
				if (name.endsWith( suspect))
					return false;
			}		
			
			
			if (name.startsWith( "maven-metadata") && !name.equalsIgnoreCase("maven-metadata.xml"))
				return false;
		}
		return true;
	}

}
