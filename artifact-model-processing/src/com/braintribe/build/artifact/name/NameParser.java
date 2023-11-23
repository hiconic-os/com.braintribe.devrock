// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.artifact.name;

import static com.braintribe.model.artifact.processing.version.VersionRangeProcessor.addHotfixRangeIfMissing;

import java.io.File;
import java.util.Map;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.string.MapCodec;
import com.braintribe.codec.string.UrlEscapeCodec;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.PartType;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessorException;
import com.braintribe.model.artifact.processing.version.VersionMetricTuple;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * 
 * parses 
 * @author pit
 *
 */
public class NameParser {
		 
	
	private final static Logger log = Logger.getLogger(NameParser.class);
	
	public static final String DELIMITER_ARTIFACT = ":";
	public static final String DELIMITER_EXTENSION = ".";
	public static final String DELIMITER_CLASSIFIER = "|";	
	public static final String DELIMITER_REVISION = "~";
	public static final String DELIMITER_VERSION = "#";
	
	
	// parsing mode
	public static enum DeclarationMode { MODE_FUZZY, MODE_DETAILED}
	
	
	private static Part parseUrlEncodedName( String name) throws NameParserException {
		Part part = Part.T.create();		
	
		MapCodec<String, String> codec = new MapCodec<String, String>();
		codec.setDelimiter( "&");
		codec.setEscapeCodec( new UrlEscapeCodec());
		codec.setAssociationDelimiter( "=");
		
		Map<String, String> values = null;
		
		try {
			values = codec.decode( name);
		} catch (CodecException e) {
			String msg = "Cannot dissect name [" + name + "] as " + e;
			log.error( msg, e);
			throw new NameParserException( msg , e);
		}
	
				
		String value = values.get("groupId");
		if (value == null) {
			String msg = "Expression [" + name + "] doesn't contain a groupId";
			NameParserException e = new NameParserException( msg);
			log.error( msg, e);
			throw e;
		}
		part.setGroupId( value);
		value = values.get("artifactId");
		if (value == null) {
			String msg = "Expression [" + name + "] doesn't contain a artifactId";
			NameParserException e = new NameParserException( msg);
			log.error( msg, e);
			throw e;	
		}
		part.setArtifactId( value);
		
		value = values.get("version");
		if (value == null) {
			String msg = "Expression [" + name + "] doesn't contain a version";
			NameParserException e = new NameParserException( msg);
			log.error( msg, e);
			throw e;		
		}
		int revisionIndex = value.indexOf( DELIMITER_REVISION);
		try {
			if (revisionIndex != -1) {
				part.setVersion( VersionProcessor.createFromString( value.substring(0, revisionIndex)));
				part.setRevision( value.substring( revisionIndex + 1));
			} else {
				int snapshotIndex = value.indexOf( DELIMITER_CLASSIFIER);
				if (snapshotIndex != -1) {
					part.setVersion( VersionProcessor.createFromString( value.substring(0, snapshotIndex)));
					part.setRevision( value.substring( snapshotIndex + 1));
				} else {
					part.setVersion( VersionProcessor.createFromString( value));
				}
			}
		} catch (VersionProcessingException e) {
			String msg = "can't build version out of expression [" + value + "] as " + e;
			log.error( msg, e);
			throw new NameParserException( msg, e);
		}
		
		
		
		String classifier = values.get( "classifier");
		String type = values.get( "type");
		
		PartTuple partTuple = null;
		if (classifier != null) {
			partTuple = PartTupleProcessor.fromString( classifier + ":" + type);
		} else {
			partTuple = PartTupleProcessor.fromString( type);
		}
		part.setType( partTuple);
						
		return part;
	}
	
	public static Part parseName( String name) throws NameParserException {
		if (name.contains( "&"))
			return parseUrlEncodedName( name);
		else
			return parseCondensedName( name);
	}
	
	@Deprecated
	public static Artifact parseCondensedArtifact( String name) throws NameParserException {
		try {
			return parseCondensedName( Artifact.T, name);
		} catch (IllegalArgumentException e) {
			throw new NameParserException(e);
		}			
	}
	
	public static Artifact parseCondensedArtifactName( String name) throws IllegalArgumentException {
		return parseCondensedName( Artifact.T, name);			
	}
	public static Solution parseCondensedSolutionName( String name) throws IllegalArgumentException {
		return parseCondensedName( Solution.T, name);			
	}
	
	public static Part parseCondensedPartName( String name) throws IllegalArgumentException {
		return parseCondensedName( Part.T, name);			
	}
	
		
	
	private static <T extends Artifact> T parseCondensedName( EntityType<T> type, String name) throws IllegalArgumentException {
		T t = type.create();
		// split group id 				
		int artifactIndex = name.indexOf( DELIMITER_ARTIFACT);
		if (artifactIndex < 0) {
			throw new IllegalArgumentException("The name "+name+" does not contain an artifact delimiter ('"+DELIMITER_ARTIFACT+"')");
		}
		t.setGroupId( name.substring(0, artifactIndex));
		
		// get artifact 
		name = name.substring( artifactIndex + 1);
		int versionIndex = name.indexOf( DELIMITER_VERSION);
		if (versionIndex < 0) {
			throw new IllegalArgumentException("The name "+name+" does not contain a version delimiter ('"+DELIMITER_VERSION+"').");
		}
		t.setArtifactId( name.substring( 0, versionIndex));
		
		String version = null;
		
		// cut version.. 
		name = name.substring(versionIndex + 1);
		int classifierIndex = name.indexOf( DELIMITER_CLASSIFIER);
		if (classifierIndex != -1) {
			version = name.substring( 0, classifierIndex);
			t.setClassifier( name.substring( classifierIndex + 1));
		} else {
			version = name;
		}
		
		// look at version..
		int revisionIndex = version.indexOf( DELIMITER_REVISION);
		try {
			if (revisionIndex != -1) {
				t.setVersion( VersionProcessor.createFromString( version.substring(0, revisionIndex)));
				t.setRevision( version.substring( revisionIndex + 1));
			} else {
				t.setVersion( VersionProcessor.createFromString( version));
			}
		} catch (VersionProcessingException e) {
			String msg = "can't build version out of expression [" + version + "] as " + e;			
			throw new IllegalArgumentException(msg, e);
		}
		return t;
	}
	
	
	public static Part parseCondensedName( String name) throws NameParserException {
		Part part = Part.T.create();
		PartTuple partTuple = PartTuple.T.create();
		partTuple.setClassifier( "");
		part.setType(partTuple);
		
		
		// extract extension
		int extensionIndex = name.lastIndexOf( DELIMITER_EXTENSION);		
		if (extensionIndex < 0) {
			String msg = String.format("No delimiter [%s] for group vs artifact found in expression [%s]", DELIMITER_EXTENSION, name);
			log.error( msg);
			throw new NameParserException(msg);
		}
		String extension = name.substring( extensionIndex+1);		
		partTuple.setType( extension);
		
		name = name.substring(0, extensionIndex);
	
		// split group id 
		int artifactIndex = name.indexOf( DELIMITER_ARTIFACT);
		if (artifactIndex < 0) {
			String msg = String.format("No delimiter [%s] for group vs artifact found in expression [%s]", DELIMITER_ARTIFACT, name);
			log.error( msg);
			throw new NameParserException(msg);
		}
		part.setGroupId( name.substring(0, artifactIndex));
		
		// get artifact 
		name = name.substring( artifactIndex + 1);
		int versionIndex = name.indexOf( DELIMITER_VERSION);
		if (versionIndex < 0) {
			String msg = String.format("No delimiter [%s] for group vs artifact found in expression [%s]", DELIMITER_VERSION, name);
			log.error( msg);
			throw new NameParserException(msg);
		}
		part.setArtifactId( name.substring( 0, versionIndex));
		
		String version = null;
		
		// cut version.. 
		name = name.substring(versionIndex + 1);
		int classifierIndex = name.indexOf( DELIMITER_CLASSIFIER);
		if (classifierIndex != -1) {
			version = name.substring( 0, classifierIndex);
			part.setClassifier( name.substring( classifierIndex + 1));
			partTuple.setClassifier( name.substring( classifierIndex + 1));
		} else {
			version = name;
		}
		
		// look at version..
		int revisionIndex = version.indexOf( DELIMITER_REVISION);
		try {
			if (revisionIndex != -1) {
				part.setVersion( VersionProcessor.createFromString( version.substring(0, revisionIndex)));
				part.setRevision( version.substring( revisionIndex + 1));
			} else {
				part.setVersion( VersionProcessor.createFromString( version));
			}
		} catch (VersionProcessingException e) {
			String msg = "can't build version out of expression [" + version + "] as " + e;
			log.error( msg, e);
			throw new NameParserException(msg, e);
		}
	
					
		return part;
	}
	
	/**
	 * @param name
	 * @return
	 * @throws NameParserException
	 */
	public static Dependency parseCondensedDependencyName( String name) throws NameParserException {
		Dependency dependency = Dependency.T.create();			

		// split group id 
		int artifactIndex = name.indexOf( DELIMITER_ARTIFACT);
		if (artifactIndex < 0) {
			String msg = String.format("No delimiter [%s] for group vs artifact found in expression [%s]", DELIMITER_ARTIFACT, name);
			log.error( msg);
			throw new NameParserException(msg);
		}
		dependency.setGroupId( name.substring(0, artifactIndex));
		
		// get artifact 
		name = name.substring( artifactIndex + 1);
		int versionIndex = name.indexOf( DELIMITER_VERSION);
		if (versionIndex < 0) {
			String msg = String.format("No delimiter [%s] for group vs artifact found in expression [%s]", DELIMITER_VERSION, name);
			log.error( msg);
			throw new NameParserException(msg);
		}
		dependency.setArtifactId( name.substring( 0, versionIndex));
		
		String version = null;
		
		// cut version.. 
		name = name.substring(versionIndex + 1);
		int classifierIndex = name.indexOf( DELIMITER_CLASSIFIER);
		if (classifierIndex != -1) {
			version = name.substring( 0, classifierIndex);
			dependency.setClassifier( name.substring( classifierIndex + 1));
		} else {
			version = name;
		}
		
		// look at version..
		int revisionIndex = version.indexOf( DELIMITER_REVISION);
		try {
			if (revisionIndex != -1) {
				dependency.setVersionRange( VersionRangeProcessor.createFromString( version.substring(0, revisionIndex)));
				dependency.setRevision( version.substring( revisionIndex + 1));
			} else {
				dependency.setVersionRange( VersionRangeProcessor.createFromString( version));
			}
		} catch (VersionProcessingException e) {
			String msg = "can't build version out of expression [" + version + "] as " + e;
			log.error( msg, e);
			throw new NameParserException(msg, e);
		}
	
					
		return dependency;
	}
	
	public static Dependency parseCondensedDependencyNameAndAutoRangify( String name) throws NameParserException {
		return parseCondensedDependencyNameAndAutoRangify(name, false);
	}
	
	public static Dependency parseCondensedDependencyNameAndAutoRangify(String name, boolean orUseGiveRevision) throws NameParserException {
		Dependency dependency = parseCondensedDependencyName(name);
		VersionRange range = dependency.getVersionRange();
		if (range.getInterval()) {
			return dependency;
		}
		VersionRangeProcessor.autoRangify(range, orUseGiveRevision);
		
		return dependency;		 
	}
	
	/**
	 * parses a condensed identification in the form of <groupId>:<artifactId>
	 * @param name - the identication's expression
	 * @return - a created {@link Identification}
	 */
	public static Identification parseCondensedIdenfitication(String name) {
		Identification identification = Identification.T.create();			
		// split group id 
		int artifactIndex = name.indexOf( DELIMITER_ARTIFACT);
		if (artifactIndex < 0) {
			String msg = String.format("No delimiter [%s] for group vs artifact found in expression [%s]", DELIMITER_ARTIFACT, name);
			log.error( msg);
			throw new NameParserException(msg);
		}
		identification.setGroupId( name.substring(0, artifactIndex));
		
		// get artifact 
		name = name.substring( artifactIndex + 1);
		identification.setArtifactId( name);
					
		return identification;
	}	
	
	/**
	 * parses a standard name like <groupid>.<groudId>.<artifactId>-<version>-<classifier>[.<extension>]
	 * if not extension's given, it defaults to PartType.EXCLUSION
	 * @param fullname - the name to parse 
	 * @return - the {@link Part} that represents the name 
	 * @throws NameParserException - 
	 */
	public static Part parseSimpleCondensedName( String fullname) throws NameParserException {
		
		String [] values = fullname.split( "-");
		
		String leftPart = values[0];
		String classifier = null;
		String rightPart = null;
		if (values.length > 2) {
			rightPart = values[1];
			classifier = values[values.length - 1];
		} else { 
			rightPart= values[ values.length-1];
		}
			
		
		
		// left part
		String [] leftValues = leftPart.split( "\\.");
		int maxindex = leftValues.length-1;
		StringBuilder builder = new StringBuilder();
		for (int i =0; i <= maxindex-1; i++) {			
				if (builder.length() > 0)
					builder.append(".");
				builder.append( leftValues[i]);
		}
		String artifactId = leftValues[maxindex];
		String groupId = builder.toString();

		// right part
		int p = rightPart.lastIndexOf( ".");
		String extension = rightPart.substring(p+1);
		PartType foundPartType = null;
		for (PartType partType : PartType.values()) {
			if (extension.equalsIgnoreCase( partType.toString())) {
				foundPartType = partType;
				break;
			}
		}
		String version = null;
		if (foundPartType == null) {
			version = rightPart;
		} else {
			version = rightPart.substring(0, p);
		}
				
		Part part = Part.T.create();
		part.setGroupId(groupId);
		part.setArtifactId(artifactId);
		try {
			part.setVersion( VersionProcessor.createFromString(version));
		} catch (VersionProcessingException e) {
			throw new NameParserException( "cannot extraction version from [" + version + "] as " + e, e);
		}
		
		try {					
			if (foundPartType != null)
				part.setType( PartTupleProcessor.fromPartType(foundPartType));
			else
				part.setType( PartTupleProcessor.fromPartType( PartType.EXCLUSIONS));
			if (classifier != null)
				part.getType().setClassifier(classifier);
		} catch (PartTupleProcessorException e) {
			String msg = "cannot assign part tuple in [" + fullname + "] as " + e;
			log.error( msg, e);
			throw new NameParserException( msg, e);
		}
		
		return part;
	}
	 
	
	/**
	 * obsolete function - cannot really determine part in all possible cases..
	 * 
	 * @param fullPath - 
	 * @return - the {@link Part}
	 * @throws NameParserException - 
	 */
	public static Part parsePath( String fullPath) throws NameParserException {

		try {
			String pathToUse = fullPath.replaceAll( "\\\\", "/");
			int lastSlash = pathToUse.lastIndexOf( "/");
			String name = pathToUse.substring( lastSlash + 1);			
			
			int lastPoint = name.lastIndexOf( ".");			
			String part1 = name.substring(0, lastPoint);
			String extension = name.substring( lastPoint + 1);
			
			
			
			// part may be <artifactId>-<version>[-snapshot][-sources]
			String parts[] = part1.split( "-");
			String artifactId = parts[0];
			
			String qualifier = null;
			String version = null;
			
			switch (parts.length) {
				case 4: {
					qualifier = parts[3];
				}
				//$FALL-THROUGH$
				case 3: {
					if (parts[2].equalsIgnoreCase( "SNAPSHOT")) {
						version = parts[1] + "-" + parts[2];
					} else {
						if (parts[2].matches( "[0123456789\\.]*")) {
							version = parts[2];
							artifactId = parts[0] + "-" + parts[1];
						} else {
							qualifier = parts[2];
							version = parts[1];
						}
					}
					break;
				}
				case 2: {
					version = parts[1];
				}							
			}
			
			Part part = Part.T.create();
			int groupPos = pathToUse.indexOf( artifactId);
			if (groupPos == 0) { // somebody named the group exactly the same as the artifactId
				groupPos = pathToUse.indexOf( artifactId, 1);
			}
			String groupId = pathToUse.substring(0, groupPos-1);
			groupId = groupId.replaceAll( "/", ".");
			
			part.setGroupId( groupId);
			part.setArtifactId( artifactId);
			try {
				part.setVersion(VersionProcessor.createFromString( version));
			} catch (VersionProcessingException e) {	
				part.setVersion( VersionProcessor.createVersion());
			}
			
			part.setType( PartTupleProcessor.fromString(qualifier, extension));			
			return part;
		}
		catch (Throwable t) {
			throw new NameParserException( "can't extract ArtifactPart from path [" + fullPath + "] as " + t, t);
		}
	}
	
	public static String buildName( Identification identification)  {
		return buildName( identification, VersionProcessor.createVersion(), DeclarationMode.MODE_DETAILED);
	}
	
	public static String buildName( Solution solution)  {
		return buildName( solution, solution.getVersion(), DeclarationMode.MODE_DETAILED);
	}

	public static String buildName( Solution solution, DeclarationMode mode)  {
		return buildName( solution, solution.getVersion(), mode);
	}

	public static String buildName( Artifact artifact)  {
		return buildName( artifact, artifact.getVersion(), DeclarationMode.MODE_DETAILED);
	}

	public static String buildName( Artifact artifact, DeclarationMode mode)  {
		return buildName( artifact, artifact.getVersion(), mode);
	}
	
	public static String buildName( Identification identification, Version version)  {
		return buildName(identification, version, DeclarationMode.MODE_DETAILED);
	}

	public static String buildName( Identification identification, Version version, DeclarationMode mode) {
		
		String versionAsString = null;
		if (version == null || VersionProcessor.isUndefined(version))
			versionAsString = "";
		else
			versionAsString = VersionProcessor.toString(version);
				
		String rslt = null;
		if (versionAsString.length() > 0)
			rslt = identification.getGroupId() + DELIMITER_ARTIFACT + identification.getArtifactId() + DELIMITER_VERSION + versionAsString;
		else
			rslt = identification.getGroupId() + DELIMITER_ARTIFACT + identification.getArtifactId();
		String revision = identification.getRevision();
		if ((revision == null) || (mode == DeclarationMode.MODE_FUZZY)){
			revision = "";
		} else {
			revision = DELIMITER_REVISION + revision;
		}
		rslt = rslt + revision;
		
		return rslt;				
	}
	
	public static String buildName( Dependency dependency) {
		return buildName( dependency, dependency.getVersionRange());
	}
	public static String buildName( Dependency dependency, VersionRange version) {
		return buildName(dependency, version, DeclarationMode.MODE_DETAILED);
	}

	public static String buildName( Dependency dependency, VersionRange version, DeclarationMode mode)  {
		
		String versionAsString = null;
		if (version == null || Boolean.TRUE.equals(version.getUndefined())) 
			versionAsString = "UNDEFINED";
		else
			versionAsString = VersionRangeProcessor.toString(version);
				
		String rslt = null;
		if (versionAsString.length() > 0)
			rslt = dependency.getGroupId() + DELIMITER_ARTIFACT + dependency.getArtifactId() + DELIMITER_VERSION + versionAsString;
		else
			rslt = dependency.getGroupId() + DELIMITER_ARTIFACT + dependency.getArtifactId();
		
		String classifier = dependency.getClassifier();
		if (classifier != null && classifier.length() > 0) {
			rslt = rslt + DELIMITER_CLASSIFIER + classifier;
		}
		
		String revision = dependency.getRevision();
		if ((revision == null) || (mode == DeclarationMode.MODE_FUZZY)){
			revision = "";
		} else {
			revision = DELIMITER_REVISION + revision;
		}
		rslt = rslt + revision;
		
		return rslt;
					
	}

	
	public static String buildName( Part part){
		return buildName( part, DeclarationMode.MODE_DETAILED);
	}
	
	public static String buildName( Part part, DeclarationMode mode){
		
		
			String versionAsString = "";
			Version version = part.getVersion();
			if (	
					(version != null) && 
					(VersionProcessor.isUndefined( version) == false)
				)
					versionAsString = VersionProcessor.toString( part.getVersion());
		
			String rslt = null;
			if (versionAsString.length() > 0)
				rslt = part.getGroupId() + DELIMITER_ARTIFACT + part.getArtifactId() + DELIMITER_VERSION + versionAsString;
			else
				rslt = part.getGroupId() + DELIMITER_ARTIFACT + part.getArtifactId();
			String revision = part.getRevision();
			if ((revision == null) || (mode == DeclarationMode.MODE_FUZZY)){
				revision = "";
			} else {
				revision = DELIMITER_REVISION + revision;
			}
			rslt = rslt + revision;
			String classifier = part.getClassifier();			
			
			
			PartTuple partTuple = part.getType();
			
			// javadoc 
			String subClassifier = partTuple.getClassifier();
			String extension = partTuple.getType().toLowerCase();
								
			
				PartType partType =PartTupleProcessor.toPartType(partTuple);
				if (partType != null) { 
					switch (partType) {
						case POM:
							classifier = null;
							break;
						default:
							break;
					}
				}
				else { 			
					log.debug("cannot determine if part's a pom as the type is unknown");
				}
			
			if (
					(classifier != null) &&
					(classifier.length() > 0)
				){
				if (subClassifier.length() == 0)
					rslt = rslt + "-" + classifier + DELIMITER_EXTENSION + extension;
				else 
					rslt = rslt + "-" + classifier + "-" + subClassifier + DELIMITER_EXTENSION + extension;
			} else {
				if (subClassifier.length() == 0)
					rslt = rslt + DELIMITER_EXTENSION + extension;
				else
					rslt = rslt + "-" + subClassifier + DELIMITER_EXTENSION + extension;
				
			}	
			//
			// part.type..
			//
			
			return rslt;		
	}
	
	
	public static String buildPartialPath( Part part, String prefix){
		
		String version = null;
		Version partVersion = part.getVersion();			
		if (VersionProcessor.isUndefined( partVersion) == false) {				
			version = VersionProcessor.toString( partVersion);				
		}
		String rslt = null;
		if (version != null)
			rslt = part.getGroupId().replaceAll( "\\.", "/") + "/" + part.getArtifactId() + "/" + version + "/";
		else
			rslt = part.getGroupId().replaceAll( "\\.", "/") + "/" + part.getArtifactId() + "/";			
		if (prefix.endsWith( "/") || prefix.endsWith( "\\"))
			return prefix + rslt;
		else
			return prefix + "/" + rslt;					
		
	}
	
	public static String buildPartialWorkingCopyPath( Identification identification, Version version, String prefix) throws NameParserException {
		try {
			String versionAsString = null;			
			if (	
				(version != null) &&
				(VersionProcessor.isUndefined( version) == false)
				){				
				VersionMetricTuple tuple = VersionProcessor.getVersionMetric(version);
				versionAsString = String.format("%d.%d", tuple.major, tuple.minor);				
			}
			String rslt = null;
			if (versionAsString != null)
				rslt = identification.getGroupId().replaceAll( "\\.", "/") + "/" + identification.getArtifactId() + "/" + versionAsString + "/";
			else
				rslt = identification.getGroupId().replaceAll( "\\.", "/") + "/" + identification.getArtifactId() + "/";			
			if (prefix.endsWith( "/") || prefix.endsWith( "\\"))
				return prefix + rslt;
			else
				return prefix + "/" + rslt;
			
		} catch (VersionProcessingException e) {
			String msg = "cannot process version as " + e;
			log.error( msg, e);
			throw new NameParserException(msg, e);
		}
		
	}
	
	public static String buildPartialPath( Identification identification, Version version, String prefix){
	
			String versionAsString = null;			
			if (	
				(version != null) &&
				(VersionProcessor.isUndefined( version) == false)
				){				
				versionAsString = VersionProcessor.toString( version);				
			}
			String rslt = null;
			if (versionAsString != null)
				rslt = identification.getGroupId().replaceAll( "\\.", "/") + "/" + identification.getArtifactId() + "/" + versionAsString + "/";
			else
				rslt = identification.getGroupId().replaceAll( "\\.", "/") + "/" + identification.getArtifactId() + "/";			
			if (prefix.endsWith( "/") || prefix.endsWith( "\\"))
				return prefix + rslt;
			else
				return prefix + "/" + rslt;				
		
	}
	
	
	
	
		
	public static String buildExpression( Part part, String prefix){
		return buildExpression( part, prefix, DeclarationMode.MODE_DETAILED);
	}
	
	public static String buildExpression( Part part, String prefix, DeclarationMode mode){
	
		String version = null;
		if (VersionProcessor.isUndefined( part.getVersion()) == false)
			version = VersionProcessor.toString( part.getVersion());
		String rslt = null;
		if (version != null)
			rslt = part.getGroupId().replaceAll( "\\.", "/") + "/" + part.getArtifactId() + "/" + version + "/";
		else
			rslt = part.getGroupId().replaceAll( "\\.", "/") + "/" + part.getArtifactId() + "/";
		String name = part.getArtifactId() + "-" + version;
		String revision = part.getRevision();
		if ((revision == null) || (mode == DeclarationMode.MODE_FUZZY)){
			revision = "";
		} else {
			revision = DELIMITER_REVISION + revision;
		}
		
		String classifier = part.getClassifier();
		PartTuple partTuple = part.getType();
		String subClassifier = partTuple.getClassifier();
		String extension = partTuple.getType();
		
		
			PartType partType = PartTupleProcessor.toPartType( part.getType());
			if (partType != null) {
				if (					
						(partType != PartType.META) &&
						(partType != PartType.GLOBAL_META) &&
						(partType != PartType.PROJECT)					
					){
					
					
					// if part's a pom, suppress classifier 
					if (partType == PartType.POM)
						classifier = null;
												
					if (classifier!= null) {
						if (subClassifier.length() == 0)
							rslt = rslt + name + revision + "-" + classifier + DELIMITER_EXTENSION + extension;
						else
							rslt = rslt + name + revision + "-" + classifier + "-" + subClassifier + DELIMITER_EXTENSION + extension;
					} else {
						if (subClassifier.length() == 0)
							rslt = rslt + name + revision + DELIMITER_EXTENSION + extension;
						else
							rslt = rslt + name + revision + "-" + subClassifier + DELIMITER_EXTENSION + extension;
					}								
				} else {
					
					switch ( partType) {
						case META:
							rslt += "maven-metadata-local.xml";
							break;								
						case GLOBAL_META:
							rslt += "maven-metadata.xml";
							break;
						case PROJECT:
							rslt += ".project";
							break;
						default:
							break;
					}				
				}
			
		} else {					
			// supress classifier if pom 
			if (extension.equalsIgnoreCase( "pom")) {
				classifier = null;
			}				
			if (classifier == null) {					
				if (subClassifier.length() > 0) 					
					rslt = rslt + name + revision + "-" +  subClassifier + DELIMITER_EXTENSION + extension; 
				 else 
					rslt = rslt + name + revision + DELIMITER_EXTENSION + partTuple.getType();					
			} else {
				if (subClassifier.length() == 0)
					rslt = rslt + name + revision + "-" + classifier + DELIMITER_EXTENSION + extension;
				else
					rslt = rslt + name + revision + "-" + classifier + "-" + subClassifier + DELIMITER_EXTENSION + extension;
			}
		}
		
		
		if (prefix.endsWith( "/") || prefix.endsWith( "\\"))
			return prefix + rslt;
		else
			return prefix + "/" + rslt;				
	}
	
	public static String buildExpressionWithOutClassifier( Part part, String prefix, DeclarationMode mode) {		
		String version = null;
		if (VersionProcessor.isUndefined( part.getVersion()) == false)
			version = VersionProcessor.toString( part.getVersion());
		String rslt = null;
		if (version != null)
			rslt = part.getGroupId().replaceAll( "\\.", "/") + "/" + part.getArtifactId() + "/" + version + "/";
		else
			rslt = part.getGroupId().replaceAll( "\\.", "/") + "/" + part.getArtifactId() + "/";
		String name = part.getArtifactId() + "-" + version;
		String revision = part.getRevision();
		if ((revision == null) || (mode == DeclarationMode.MODE_FUZZY)){
			revision = "";
		} else {
			revision = DELIMITER_REVISION + revision;
		}
		
		PartTuple partTuple = part.getType();
		
		String subClassifier = partTuple.getClassifier();
		String extension = partTuple.getType();
		
		
			PartType partType = PartTupleProcessor.toPartType( part.getType());
			if (partType != null) {			
				if (					
						(partType != PartType.META) &&
						(partType != PartType.GLOBAL_META) &&
						(partType != PartType.PROJECT)					
					){									
					if (subClassifier.length() > 0 )
						rslt = rslt + name + revision + "-" + subClassifier + DELIMITER_EXTENSION + extension;
					else
						rslt = rslt + name + revision + DELIMITER_EXTENSION + extension;
					
				} else {
					
					switch ( partType) {
						case META:
							rslt += "maven-metadata-local.xml";
							break;								
						case GLOBAL_META:
							rslt += "maven-metadata.xml";
							break;
						case PROJECT:
							rslt += ".project";
							break;
						default:
							break;
					}				
				}
			}
			else {
			
				if (subClassifier.length() > 0)
					rslt = rslt + name + revision + "-" + subClassifier + DELIMITER_EXTENSION + part.getType().getType();
				else
					rslt = rslt + name + revision + DELIMITER_EXTENSION + part.getType().getType();
			}
			
			
		if (prefix.endsWith( "/") || prefix.endsWith( "\\"))
			return prefix + rslt;
		else
			return prefix + "/" + rslt;					
	}
	

	public static String buildFileName( Part part){
		return buildFileName( part, DeclarationMode.MODE_DETAILED);
	}
	

	public static String buildFileName( Part part, DeclarationMode mode){		
			
		String rslt = "";
						
		PartTuple partTuple = part.getType();
		// a classifier at a tuple has precendence, but a part can have one too (as inherited from the dependency, legacy)
		String classifier = partTuple.getClassifier();
		if (classifier == null || classifier.length() == 0) {				
			classifier = part.getClassifier();
		}
		
		String extension = partTuple.getType();
		
		
		PartType partType = PartTupleProcessor.toPartType( part.getType());
		if (partType != null) {			
			rslt = handleKnownPartType(part, mode, rslt, classifier, extension, partType);
		} else {
			// suppress classifier 
			rslt = handleUnknownPartType(part, mode, rslt, classifier, extension);
		}						
		
		return rslt;								
	}

	private static String handleUnknownPartType(Part part, DeclarationMode mode, String rslt, String classifier, String extension) {
		if (extension.equalsIgnoreCase( "pom"))
			classifier = null;
		
		switch ( mode) {			
			case MODE_FUZZY : {	
				if (classifier != null && classifier.length() > 0) {					
					rslt= part.getArtifactId() + "-" + part.getVersion() + "-" + classifier + DELIMITER_EXTENSION + extension;					
				} else {					
					rslt= part.getArtifactId() + "-" + part.getVersion() + DELIMITER_EXTENSION + extension;					
				}
				break;
				
				
			}
			case MODE_DETAILED: {
				String revision = part.getRevision();
				String retval = part.getArtifactId() + "-" + VersionProcessor.toString( part.getVersion());
				if (revision != null)
					retval += DELIMITER_REVISION + revision;
				
				if (classifier != null && classifier.length() > 0) {					
					retval += "-" + classifier;					
				}				
				
				rslt= retval + DELIMITER_EXTENSION + extension;
				break;
			}
			default:  {
				String msg = "DeclarationMode [" + mode + "] is unknown"; 
				log.warn( msg);				
			}
		}
		return rslt;
	}

	private static String handleKnownPartType(Part part, DeclarationMode mode, String rslt, String classifier, String extension, PartType partType) {
		switch (partType) {
			case SOURCES:						
				extension = PartType.JAR.toString().toLowerCase();
				break;
			case EXCLUSIONS:
				extension = PartType.EXCLUSIONS.toString().toLowerCase();
				break;			
			default:
				break;
		}
		// suppress classifier 
		if (partType == PartType.POM)
			classifier = null;
		
		switch ( mode) {			
			case MODE_FUZZY : {												
				if (classifier == null || classifier.length() == 0) {					 
						rslt= part.getArtifactId() + "-" + part.getVersion() + DELIMITER_EXTENSION + extension;					
				} else {				
					rslt= part.getArtifactId() + "-" + part.getVersion() + "-" + classifier + DELIMITER_EXTENSION + extension;					
				}
				break;
			}
			case MODE_DETAILED: {
				String revision = part.getRevision();
				String retval = part.getArtifactId() + "-" + VersionProcessor.toString( part.getVersion());
				if (revision != null)
					retval += DELIMITER_REVISION + revision;
									
				if (classifier != null && classifier.length() > 0) {				
					retval += "-" + classifier;					
				}
				rslt= retval + DELIMITER_EXTENSION + extension;
				break;
			}
			default:  {
				String msg = "DeclarationMode [" + mode + "] is unknown"; 
				log.warn( msg);				
			}
		}
		//
		switch (partType) {				
			case META:
				rslt = "maven-metadata-local.xml";
				break;
			case GLOBAL_META:
				rslt = "maven-metadata.xml";
				break;
			case PROJECT:
				rslt = ".project";
				break;		
			default:
				break;
		}
		return rslt;
	}
	
	public static boolean matchEclipseProjectNameToArtifact( String name, Artifact artifact){				
			
		// test one: .*<artifactId><version>[-SNAPSHOT].*
		String mask = ".*" + artifact.getArtifactId() + "-" + VersionProcessor.toString( artifact.getVersion()) + ".*";
		if (name.matches( mask))
			return true;
		// test two : .*<artifactId><version>.* (snapshot bug)
		//if (artifact.getVersion().endsWith( "-SNAPSHOT")) {				
			Version version = artifact.getVersion();
			//version = version.substring( 0, version.indexOf("-"));
			mask = ".*" + artifact.getArtifactId() + "-" + VersionProcessor.toString( version) + ".*";
			if (name.matches( mask))
				return true;
		//}				
			return false;

	}
	
	private static boolean isSuspectedVersion( String version) {
		String [] values = version.split( "\\.");
		boolean [] hits = new boolean [ values.length];
		for (int i = 0; i < values.length; i++) {
			String value = values[i];
			if (value.matches( "[0-9]*") == false) {
				hits[i] = false;
			} else {
				hits[i] = true;
			}
		}		
		for (boolean bool : hits) {
			if (bool)
				return true;
		}
		return false;
	}
	
	public static Artifact parseFileName( File file) {
		
		String fileName = file.getName();
		return parseFileName( fileName);
	}
	
	public static Artifact parseFileName( String fileName) {
		String extension = fileName.substring( fileName.lastIndexOf( "."));
		
		Artifact artifact = Artifact.T.create();
		
		String name = fileName.substring(0, fileName.length() - extension.length());
		//
		String [] slashGroups = name.split( "-");
		StringBuilder nameBuilder = new StringBuilder();
 
		String versionString = null;
		for (int i = 0; i < slashGroups.length; i++) {
			String suspect = slashGroups[i];
			if (isSuspectedVersion( suspect) == false) {
				if (nameBuilder.length() > 0)
					nameBuilder.append( "-");
				nameBuilder.append( suspect);
				continue;
			} 
			versionString = name.substring( nameBuilder.toString().length()+1);
			break;			
		}
		String gp = nameBuilder.toString();
				
		artifact.setGroupId( "");
		artifact.setArtifactId( gp);
		try {
			artifact.setVersion( VersionProcessor.createFromString(versionString));
		} catch (VersionProcessingException e) {
			artifact.setVersion( VersionProcessor.createVersion());
		}
		
		return artifact;
	}
	
	public static Dependency parseDependencyFromHotfixShorthand(String dependencyAsString) throws NameParserException {
		Dependency dependency = parseCondensedDependencyName(dependencyAsString);
		
		VersionRange versionRange = dependency.getVersionRange();
		dependency.setVersionRange(addHotfixRangeIfMissing(versionRange));
		
		return dependency;
	}

	

}
