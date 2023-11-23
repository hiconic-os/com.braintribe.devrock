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
package com.braintribe.model.artifact.processing.part;

import static com.braintribe.utils.lcd.StringTools.equalsIgnoreCaseOrBothNull;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.PartType;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class PartTupleProcessor {
	private static Logger log = Logger.getLogger(PartTupleProcessor.class);
	private static String PARTTUPLE_DELIMITER = ":";

	public static PartTuple create() {
		PartTuple partTuple = PartTuple.T.create();
		partTuple.setClassifier( "");
		return partTuple;
	}
	
	/** How about using {@link KnownPartTuples} instead? */
	public static PartTuple create( PartType partType) {
		try {
			return fromPartType(partType);
		} catch (PartTupleProcessorException e) {
			String msg = "cannot produce a valid PartTuple from PartyType [" + partType.toString() + "]";
			log.error( msg, e);
			return null;
		}
	}
	
	public static String toString( PartTuple partTuple) {

		return partTuple.getClassifier() + PARTTUPLE_DELIMITER + partTuple.getType();
	}
	
	public static PartTuple fromString( String classifier, String type) {
		PartTuple partTuple = create();
		if (classifier != null)
			partTuple.setClassifier(classifier);
		partTuple.setType( type);
		return partTuple;
	}
	
	public static PartTuple fromString( String string) {
		PartTuple partTuple = PartTuple.T.create();
		int index = string.indexOf( ":");
		if (index >= 0) {
			String classifier = string.substring(0, index);
			partTuple.setClassifier( classifier);
			partTuple.setType( string.substring( index+1));
		} else {
			partTuple.setClassifier( "");
			partTuple.setType( string);
		}
		return partTuple;
	}
	
	/**
	 * deprecated : use {@link #equals(PartTuple, PartTuple)} instead
	 * @param one - first {@link PartTuple}
	 * @param two - second {@link PartTuple}
	 * @return - true if they match 
	 */
	@Deprecated
	public static boolean compare( PartTuple one, PartTuple two) {
		return equals( one, two);		
	}
	
	/**
	 * strictly matches (via String.equalsIgnoreCase) two {@link PartTuple} 
	 * @param one - first {@link PartTuple}
	 * @param two - second {@link PartTuple}
	 * @return - true if the tuple match, false otherwise
	 */
	public static boolean equals( PartTuple one, PartTuple two) {
		if (one == two)
			return true;
		else
			return equalsIgnoreCaseOrBothNull(one.getClassifier(), two.getClassifier()) && //
					equalsIgnoreCaseOrBothNull(one.getType(), two.getType());
	}
	
	/**
	 * matches two {@link PartTuple} with support for wild cards in both classifier and extension
	 * @param one - first {@link PartTuple}
	 * @param two - second {@link PartTuple}
	 * @return - true if the tuple match, false otherwise
	 */
	public static boolean matches( PartTuple one, PartTuple two) {
		// 
		String classifierOne = one.getClassifier();
		String classifierTwo = two.getClassifier();
		
		if (classifierOne == null && classifierTwo != null)
			return false;
		// expand fake wild card to regexp wildcard
		if (classifierTwo.equalsIgnoreCase("*"))
			classifierTwo = ".*";
		if (classifierOne.matches(classifierTwo) == false)
			return false;
		
		String typeOne = one.getType();
		String typeTwo = two.getType();
		
		if (typeOne == null && typeTwo == null) {
			return true;
		}
		if (typeOne == null && typeTwo != null)
			return false;
		
		if (typeOne != null && typeTwo == null)
			return false;
		
		// expand fake wild card to regexp wild card
		if (typeTwo.equalsIgnoreCase("*"))
			typeTwo = ".*";
		
		return (typeOne.matches(typeTwo));	
	}
	
	
	
	public static PartType toPartType( PartTuple tuple) {
		String classifier = tuple.getClassifier();
		String type = tuple.getType();
		
		if (type.equalsIgnoreCase( "jar")) {
			if (classifier.length() == 0) 
				return PartType.JAR;
			if (classifier.equalsIgnoreCase( "sources"))
				return PartType.SOURCES;
			if (classifier.equalsIgnoreCase( "src"))
				return PartType.SOURCES;
			if (classifier.equalsIgnoreCase("javadoc"))
				return PartType.JAVADOC;
			return null;
		}
		
		// javadocs 
		if (type.equalsIgnoreCase( "zip")) {
			if (classifier.equalsIgnoreCase( "javadoc"))
				return PartType.JAVADOC;
		}
		if (type.equalsIgnoreCase( "jdar")) {
			return PartType.JAVADOC;
		}
		if (type.equalsIgnoreCase( "javadoc")) {
			return PartType.JAVADOC;
		}
		
		if (type.equalsIgnoreCase( "xml")) {
			if (classifier.equalsIgnoreCase( "meta"))
				return PartType.META;
			if (classifier.equalsIgnoreCase( "global-meta"))
				return PartType.GLOBAL_META;
		}

		if (type.equalsIgnoreCase( "pom"))
			return PartType.POM;
		
		if (type.equalsIgnoreCase( "exclusions")) 
			return PartType.EXCLUSIONS;
		
		if (type.equalsIgnoreCase( "ant")) 
			return PartType.ANT;
		
		if (type.equalsIgnoreCase( "project"))
			return PartType.PROJECT;
		
		if (type.equalsIgnoreCase( "asc"))
			return PartType.ASC;
		
		if (type.equalsIgnoreCase( "md5"))
			return PartType.MD5;
		
		if (type.equalsIgnoreCase( "sha1"))
			return PartType.SHA1;
		
		if (classifier.equalsIgnoreCase("model") && type.equalsIgnoreCase("xml"))
			return PartType.MODEL;
		
		return null;
	}
	
	
	public static boolean isWellknownPartType( PartTuple tuple) {		
		PartType partType = toPartType(tuple);
		if (partType != null) {
			log.debug( "tuple [" + toString( tuple) + "] translates to [" + partType + "]");
			return true;
		}
		else {
			log.debug("not a well known part type");
			return false;				
		}
	}
	public static PartTuple createPomPartTuple() {
		return fromString( "pom");
	}
	public static PartTuple createProjectPartTuple() {
		return fromString( "project");
	}
	public static PartTuple createJarPartTuple() {
		return fromString( "jar");
	}
	
	
	// JAR, JAVADOC, META, GLOBAL_META, POM, SOURCES, PROJECT, MD5, SHA1, EXCLUSIONS, ANT
	public static PartTuple fromPartType( PartType partType) throws PartTupleProcessorException {
		
		switch( partType) {
			case JAR:
				return fromString( "jar");
			case POM:
				return fromString("pom");
			case JAVADOC:
				return fromString( "javadoc:jar");
			case SOURCES:
				return fromString( "sources:jar");
			case EXCLUSIONS:
				return fromString( "exclusions");
			case MD5:
				return fromString("md5");
			case ASC:
				return fromString("asc");
			case SHA1:
				return fromString( "sha1");
			case PROJECT:
				return fromString( "project");
			case ANT:
				return fromString("ant");
			case META:
				return fromString( "meta:xml");
			case GLOBAL_META:
				return fromString ("globalmeta:xml");
			case MODEL:
				return fromString("model:xml");
			case _UNKNOWN_:
			default:
				throw new PartTupleProcessorException( "Unknown part type [" + partType + "]");
		}
	}
	
//TODO : revise the use of this function as it is problematic .... 
	public static PartTuple extractPartTuple( String inFileName) throws PartTupleProcessorException {
		
		String fileName = inFileName.toLowerCase();	
		int p = fileName.lastIndexOf( ".");
		String extension = fileName.substring( p);
		
		if (extension.equalsIgnoreCase( ".exclusion")) {
			return PartTupleProcessor.fromPartType( PartType.EXCLUSIONS);
		}
		if (extension.equalsIgnoreCase( ".jar")) {
			if (
					(fileName.endsWith( "-sources.jar")) ||
					(fileName.endsWith( "-src.jar"))
				)					
				return PartTupleProcessor.fromString( "sources", "jar");
			// -javadoc.jar.. 
			if (fileName.endsWith("-javadoc.jar"))
				return PartTupleProcessor.fromString( "javadoc", "jar");
			return PartTupleProcessor.fromString( "jar");
		}
		
		if (extension.equalsIgnoreCase( ".zip")) {
			if (fileName.endsWith( "javadoc.zip"))
				return PartTupleProcessor.fromString( "javadoc", "zip");
		}		
		if (extension.equalsIgnoreCase( ".jdar")) {
			return PartTupleProcessor.fromString( "javadoc", "jdar");
		}
		if (extension.equalsIgnoreCase( ".javadoc")) {
			return PartTupleProcessor.fromString( "javadoc");
		}	
		// poms
		if (extension.equalsIgnoreCase( ".pom")) {
			return PartTupleProcessor.fromString( "pom");
		}
		
		if (extension.equalsIgnoreCase( ".xml")) {
			if (fileName.endsWith( "maven-metadata.xml"))
				return PartTupleProcessor.fromPartType( PartType.GLOBAL_META);
			if (fileName.endsWith("maven-metadata-local.xml"))
				return PartTupleProcessor.fromPartType( PartType.META);
			if (fileName.endsWith( "model.xml")) {
				return PartTupleProcessor.fromPartType( PartType.MODEL);
			}
		}		
		
		if (
				(extension.equalsIgnoreCase( ".md5")) ||
				(extension.equalsIgnoreCase( ".sha1")) ||
				(extension.equalsIgnoreCase( ".asc"))
			) 
			return null;
		
		if (extension.startsWith("."))
			return PartTupleProcessor.fromString(extension.substring(1));
		else
			return PartTupleProcessor.fromString(extension);
	}
	
	/**
	 * @param inFileName - the file's name 
	 * @return  - the derived {@link PartType}
	 */
	public static PartType extractPartType( String inFileName) {
		String fileName = inFileName.toLowerCase();
		
		int p = fileName.lastIndexOf( ".");
		String extension = fileName.substring( p);
		
		// jar 
		if (extension.equalsIgnoreCase( ".jar")) {
			if (
					(fileName.endsWith( "-sources.jar")) ||
					(fileName.endsWith( "-src.jar"))
				)
				return PartType.SOURCES;			
			// -javadoc.jar.. 
			if (fileName.endsWith("-javadoc.jar"))
				return PartType.JAVADOC;
			return PartType.JAR;
		}
					
		// poms
		if (extension.equalsIgnoreCase( ".pom")) {
			return PartType.POM;
		}
		
		// javadocs 
		if (extension.equalsIgnoreCase( ".zip")) {
			if (fileName.endsWith( "javadoc.zip"))
				return PartType.JAVADOC;
		}		
		if (extension.equalsIgnoreCase( ".jdar")) {
			return PartType.JAVADOC;
		}
		if (extension.equalsIgnoreCase( ".javadoc")) {
			return PartType.JAVADOC;
		}	
		if (extension.equalsIgnoreCase( ".xml")) {
			if (fileName.endsWith( "maven-metadata.xml"))
				return PartType.GLOBAL_META;
			if (fileName.endsWith("maven-metadata-local.xml"))
				return PartType.META;
			if (fileName.endsWith( "model.xml")) {
				return PartType.MODEL;
			}
		}		
		return null;
	}
	
	public static String isPartPresentInNameList( String [] names, Part part) throws PartTupleProcessorException {
		for (String name : names) {
			if (name.length() == 0)
				continue;
			PartTuple tuple = extractPartTuple(name);
			if (PartTupleProcessor.compare(tuple, part.getType()))
				return name;
		}
		return null;
	}
	
	public static boolean isPartNameMatched( String name, String expectedPartName){
		// direct match
		if (name.equalsIgnoreCase( expectedPartName))
			return true;
		// partial match 
		if (name.contains( expectedPartName)) {
			// the rest must be a directory...
			int len = expectedPartName.length();
			String remainder = name.substring(0, name.length()-len);
			if (
					remainder.endsWith( "/") || 
					remainder.endsWith("\\")
				)
				return true;
		}
		return false;
	}
	
	
	public static List<String> getFileNameCandidatesForPart( Part part) {
		return getFileNameCandidatesForPart(part, part.getType());
	}
	
	public static List<String> getFileNameCandidatesForPart( Artifact artifact, PartTuple tuple) {
		List<String> result = new ArrayList<String>();
		// standard file parts.. 	
		String artifactId = artifact.getArtifactId();
		String classifier = artifact.getClassifier();
		if (
				(classifier == null) ||
				(classifier.length() == 0)
				)
			classifier = null;
		
		String versionAsString = VersionProcessor.toString( artifact.getVersion());
		
		String tupleClassifier = tuple.getClassifier();		
		String tupleExtension = tuple.getType();
		
		if (tupleExtension.equalsIgnoreCase( "jar")) {
			// sources 
			if (tupleClassifier != null && tupleClassifier.equalsIgnoreCase( "sources")) {
				if (classifier == null) {
					result.add( artifactId + "-" + versionAsString + "-sources" + ".jar");
					result.add( artifactId + "-" + versionAsString + "-src" + ".jar");
				} else {
					result.add( artifactId + "-" + classifier + "-" + versionAsString + "-sources" + ".jar");
					result.add( artifactId + "-" + versionAsString + classifier + "-sources" + ".jar");
					result.add( artifactId + "-" + classifier + "-" + versionAsString + "-src" + ".jar");
					result.add( artifactId + "-" + versionAsString + "-" + classifier + "-src" + ".jar");
				}
				return result;
			}
			if (tupleClassifier != null && tupleClassifier.equalsIgnoreCase( "javadoc")){
				if (classifier == null) {
					result.add( artifactId + "-" + versionAsString + "-javadoc" + ".jar");
					result.add( artifactId + "-" + versionAsString + "-javadoc" + ".zip");
					result.add( artifactId + "-" + versionAsString + ".jdar");
				} else {
					result.add( artifactId + "-" + classifier + "-" + versionAsString + "-javadoc" + ".jar");
					result.add( artifactId + "-" + versionAsString + "-" + classifier + "-javadoc" + ".jar");
					result.add( artifactId + "-" + classifier + "-" + versionAsString + "-javadoc" + ".zip");
					result.add( artifactId + "-" + versionAsString + "-" + classifier + "-javadoc" + ".zip");
					result.add( artifactId + "-" + classifier + "-" + versionAsString  + ".jdar");
					result.add( artifactId + "-" + versionAsString + "-" + classifier +  ".jdar");
				}
				return result;
			}			
		}
		
		if (classifier == null) {
			if (
					(tupleClassifier == null) ||
					(tupleClassifier.length() == 0)
					)
				result.add( artifactId + "-" + versionAsString + "." + tupleExtension);
			else 
				result.add( artifactId + "-" + versionAsString + "-" + tupleClassifier + "." + tupleExtension);
		} else {
			if  (
					(tupleClassifier == null) ||
					(tupleClassifier.length() == 0)
					) {
				result.add( artifactId + "-" + classifier + "-" + versionAsString + "." + tupleExtension);
				result.add( artifactId + "-" + versionAsString + "-" + classifier + "." + tupleExtension);
			} else { 
				result.add( artifactId + "-" + classifier + "-" + versionAsString + "-" + tupleClassifier + "." + tupleExtension);
				result.add( artifactId + "-" + versionAsString + "-" +  classifier + "-" + tupleClassifier + "." + tupleExtension);
			}
		}
		
		return result;
		
	}

}
