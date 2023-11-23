// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.artifact.representations.type;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

/**
 * a simple reader that reads a line delimited list of type signatures from a file
 * a hash tag (#) is an end-of-line comment. 
 * 
 * @author Pit
 *
 */
public class TypeExclusionList {
	private static Logger log = Logger.getLogger(TypeExclusionList.class);
	
	List<String> exclusions;	
	
	public TypeExclusionList() {
		exclusions = new ArrayList<String>();
	}
	public TypeExclusionList( String asString) {
		fromString( asString);
	}
	
	public TypeExclusionList( Collection<String> exclusionsAsString) {
		fromList(exclusionsAsString);
	}
	
	public boolean contains( String name) {
		return exclusions.contains(name);
	}
	
	public void fromList(Collection<String> exclusionsAsString) {
		exclusions = new ArrayList< String>( exclusionsAsString.size());
		exclusions.addAll(exclusionsAsString);
	}
	
	public void fromString( String asString) {
		String [] exclusionsAsString = asString.split( "\n");
		exclusions = new ArrayList< String>( exclusionsAsString.length);
		for (String exclusionAsString : exclusionsAsString) {
			if (exclusionAsString.startsWith( "#"))
				continue;
			if (exclusionAsString.length() > 0) {
				try {				
					exclusions.add( exclusionAsString);
				} catch (UnsupportedOperationException e) {
					log.error( "cannot add exclusion from String [" + exclusionAsString + "] as it's invalid", null);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("# Exclusion list : list of classes to be excluded from deployed model");
		for (String name : exclusions) {
			builder.append( "\n");
			builder.append( name);
		}
		return builder.toString();
	}
	
	public void read( File file) throws RepresentationException {
		try {
			String contents = IOTools.slurp(file, "UTF-8");
			fromString(contents);
		} catch (IOException e) {
			String msg = "cannot read exclusion list from file [" + file +"]";
			log.error( msg, e);
			throw new RepresentationException(msg, e);
		}
	}
	
	public void write( File file) throws RepresentationException {
		String contents = toString();
		try {
			IOTools.spit(file, contents, "UTF-8", false);
		} catch (IOException e) {
			String msg = "cannot write exclusion list to file [" + file +"]";
			log.error( msg, e);
			throw new RepresentationException(msg, e);
		}
	}
}
