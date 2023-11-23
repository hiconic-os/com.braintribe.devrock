// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.build.artifact.representations.artifact.pom.properties.PomPropertyResolver;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.ArtifactDeclarationType;
import com.braintribe.model.artifact.Exclusion;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Property;
import com.braintribe.model.artifact.Solution;


/**
 * expert to interpret maven properties  
 * @author pit
 *
 */
public class PomPropertyHandler {
	private static String MALACLYPSE_FLAT = "malaclypse-flat";
	private static String MALACLYPSE_EXCLUSIONS = "malaclypse-exclusions";
	private static String MALACLYPSE_DOMINANTS = "malaclypse-dominants";
	private static String MALACLYPSE_ARCHETYPE = "archetype";

	
	private static Property getNamedProperty( Collection<Property> properties, String name) {
		for (Property property : properties) {
			if (property.getName().equalsIgnoreCase(name))
				return property;
		}
		return null;
	}
	/**
	 * extract all RELEVANT data for malaclypse 	
	 */
	public static void attachToArtifact( String walkScopeId, Solution artifact, Collection<Property> properties, PomPropertyResolver propertyResolver)  {
		
		// flat		
		Property flatProperty = getNamedProperty(properties, MALACLYPSE_FLAT);
		if (
				flatProperty == null ||
				flatProperty.getValue() == null ||
				flatProperty.getValue() != "true"
			) {
			artifact.setDeclarationType( ArtifactDeclarationType.TRANSIENT);
		}
		else {
			artifact.setDeclarationType( ArtifactDeclarationType.FLAT);
		}
				
		Property exclusionProperty = getNamedProperty(properties, MALACLYPSE_EXCLUSIONS);
		if (exclusionProperty != null) {
			Set<Exclusion> exclusions = new HashSet<Exclusion>();
			
			String exclusionsAsString = exclusionProperty.getRawValue();
			String [] values = exclusionsAsString.split( "\\s+");
			for (String value : values) {
				value = value.trim();
				if (value.length() == 0)
					continue;
				String [] pairing = value.split (":");
				String grp;
				String art;
				if (pairing.length != 2) {
					grp = value;
					art = ".*";
				} else {
					grp = pairing[0];
					art = pairing[1];
				}
				Exclusion exclusion = Exclusion.T.create();
				exclusion.setGroupId( propertyResolver.expandValue( walkScopeId, artifact, grp));
				exclusion.setArtifactId( propertyResolver.expandValue(walkScopeId, artifact, art));
				exclusions.add( exclusion);				
			}
			artifact.setExclusions(exclusions);
		}
		
		//
		// dominants.
		//
		Property dominantProperty = getNamedProperty(properties, MALACLYPSE_DOMINANTS);
		if (dominantProperty != null) {
			Set<Identification> dominants = new HashSet<Identification>();
			String dominantsAsString = dominantProperty.getRawValue();
			String [] values = dominantsAsString.split( "\\s+");
			for (String value : values) {
				if (value.length() == 0)
					continue;
				value = value.trim();
				String [] pairing = value.split (":");
				String grp;
				String art;
				if (pairing.length != 2) {
					grp = value;
					art = ".*";
				} else {
					grp = pairing[0];
					art = pairing[1];
				}
				Identification identification = Identification.T.create();
				identification.setGroupId( propertyResolver.expandValue( walkScopeId, artifact, grp));
				identification.setArtifactId(propertyResolver.expandValue( walkScopeId, artifact, art));
				dominants.add( identification);				
			}
			artifact.setDominants(dominants);		
		}		
		
		// archetype 
		Property archetypeProperty = getNamedProperty(properties, MALACLYPSE_ARCHETYPE);
		if (archetypeProperty != null) {
			String archetype = archetypeProperty.getRawValue();
			artifact.setArchetype(archetype);
		}
	}		
 
	/**
	 * extract all OTHER dependencies that the malaclypse stuff 	
	 */
	public static void resolveProperties( String walkScopeId, Artifact artifact, Collection<Property> properties, PomPropertyResolver propertyResolver) {
	
		for (Property property  : properties) {
			String name = property.getName();
			if (name.equalsIgnoreCase(MALACLYPSE_FLAT) || name.equalsIgnoreCase(MALACLYPSE_EXCLUSIONS) || name.equalsIgnoreCase( MALACLYPSE_DOMINANTS))
				continue;
			String rawValue = property.getRawValue();
			property.setValue(propertyResolver.expandValue(walkScopeId, artifact, rawValue));
		}
		
	}
	
	

	
}
