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
package com.braintribe.model.artifact.processing.artifact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;

/**
 * a class with several helper functions, mostly comparisons, containment functions<br/>
 * as a hommage to Michi Lafite ;-)
 * @author pit
 *
 */
public class ArtifactProcessor {

	private static Logger log = Logger.getLogger(ArtifactProcessor.class);
	
	public static Comparator<Artifact> artifactComparator = Comparator.comparing(Artifact::getVersion, VersionProcessor.comparator);
	
	/**
	 * create part from an identification 
	 * @param artifact - the {@link Identification} with group and artifact id 
	 * @param version - the {@link Version} to use
	 * @param type - {@link PartTuple} that declares the type of the part 
	 * @return - the {@link Part} created 
	 */
	public static Part createPartFromIdentification( Identification artifact, Version version, PartTuple type) {
		//log.info("@@@@ Creating part from identification [" + NameParser.buildName( artifact) + "], version ");
		
		Part part = Part.T.create();
		transferIdentification( part, artifact);
		part.setType( type);
		part.setVersion( version);
		
		return part;		
	}
	
	/**
	 * transfers the {@link Identification}'s data to a second one
	 * @param target - target {@link Identification}
	 * @param source - source {@link Identification}
	 */
	public static void transferIdentification( Identification target, Identification source) {
		target.setGroupId( source.getGroupId());
		target.setArtifactId( source.getArtifactId());
		target.setClassifier( source.getClassifier());
		target.setRevision( source.getRevision());
	}
	
	/**
	 * transfers the identification and the version between two {@link Artifact}
	 * @param target - the target {@link Artifact}
	 * @param source - the source {@link Artifact}
	 */
	public static void transferIdentification( Artifact target, Artifact source) {
		transferIdentification((Identification) target, source);
		target.setVersion( source.getVersion());
	}
	
	/**
	 * create an identification from a condensed name
	 * @param name - the condensed name as a {@link String}
	 * @return - the created {@link Identification}
	 * @throws NameParserException -
	 */
	public static Identification createIdentifaction( String name) throws NameParserException {
		Part part = NameParser.parseName(name);
		Identification identification = Identification.T.create();
		transferIdentification(identification, part);
		return identification;
	}
	
	/**
	 * null save equals on two strings
	 * @param one - first {@link String}
	 * @param two - second {@link String}
	 * @return - true if the strings were equal
	 */
	private static boolean saveEquals( String one, String two) {
		if (
				(one == null) &&
				(two == null)
			) 
			return true;
		if (
				(
					(one == null) &&
					(two != null)
				) ||
				(
					(one != null) && 
					(two == null)
				)
			)
				return false;
		return one.equalsIgnoreCase( two);
	}
	
	/**
	 * null save equals on two strings
	 * @param one - first {@link String}
	 * @param pattern - second {@link String}
	 * @return - true if the strings were equal
	 */
	private static boolean saveMatches( String one, String pattern) {
		if (
				(one == null) &&
				(pattern == null)
			) 
			return true;
		if (
				(
					(one == null) &&
					(pattern != null)
				) ||
				(
					(one != null) && 
					(pattern == null)
				)
			)
				return false;
		return one.matches( pattern);
	}
	
	
	
	/**
	 * equals on {@link Identification}<br/>
	 * tests group and artifact id
	 * @param id1 - first {@link Identification}
	 * @param id2 - second {@link Identification}
	 * @return - true if they match 
	 */
	public static boolean identificationEquals( Identification id1, Identification id2) {
		if (saveEquals( id1.getGroupId(), id2.getGroupId()) == false)
			return false;
		if (saveEquals( id1.getArtifactId(), id2.getArtifactId()) == false)
			return false;
		if (saveEquals( id1.getClassifier(), id2.getClassifier()) == false)
			return false;
		if (saveEquals( id1.getRevision(), id2.getRevision()) == false)
			return false;		
		return true;		
	}
	/**
	 * equals on {@link Identification} with regexp pattern<br/>
	 * tests group and artifact id
	 * @param id1 - first {@link Identification}
	 * @param id2 - second {@link Identification}
	 * @return - true if they match 
	 */
	public static boolean identificationMatches( Identification id1, Identification id2) {
		if (saveMatches(id1.getGroupId(), id2.getGroupId()) == false)
			return false;
		if (saveMatches( id1.getArtifactId(), id2.getArtifactId()) == false)
			return false;
		if (saveMatches( id1.getClassifier(), id2.getClassifier()) == false)
			return false;
		if (saveMatches( id1.getRevision(), id2.getRevision()) == false)
			return false;		
		return true;		
	}
	
	
	
	/**
	 * tests if a collection of {@link Identification} contain another {@link Identification}
	 * @param ids - the {@link Collection} of {@link Identification}
	 * @param suspect - the {@link Identification} to look for  
	 * @return - true if ids contains suspect 
	 */
	public static boolean contains( Collection<Identification> ids, Identification suspect) {
		for (Identification id : ids) {
			if (identificationEquals(id, suspect))
				return true;
		}
		return false;
	}
	
	/**
	 * equals on {@link Artifact} <br/>
	 * tests group id, artifact id, classifier, revision, version 
	 * @param id1 - first {@link Artifact}
	 * @param id2 - second {@link Artifact}
	 * @return - true if artifact match
	 */
	public static boolean artifactEquals( Artifact id1, Artifact id2) {
		if (saveEquals( id1.getGroupId(), id2.getGroupId()) == false)
			return false;
		if (saveEquals( id1.getArtifactId(), id2.getArtifactId()) == false)
			return false;
		if (saveEquals( id1.getClassifier(), id2.getClassifier()) == false)
			return false;
		if (saveEquals( id1.getRevision(), id2.getRevision()) == false)
			return false;		
		
		if (!VersionProcessor.matches( id1.getVersion(), id2.getVersion()))
			return false;		
		return true;		
	}
	
	/**
	 * test of {@link Solution} as {@link Artifact}
	 * @param s1 - first {@link Solution}
	 * @param s2 - second {@link Solution}
	 * @return - true if the solutions match 
	 */
	public static boolean solutionEquals( Solution s1, Solution s2){
		return artifactEquals( s1, s2);
	}
	/**
	 * tests if a {@link Collection} of {@link Solution} contains a {@link Solution}
	 * @param ids - the {@link Collection} of {@link Solution}
	 * @param suspect - the {@link Solution} to look for
	 * @return - true if ids contains suspect
	 */
	public static boolean contains( Collection<Solution> ids, Solution suspect) {
		for (Solution id : ids) {
			if (solutionEquals(id, suspect))
				return true;
		}
		return false;
	}
	
	/**
	 * tests if a {@link Collection} of {@link Artifact} contains an {@link Artifact}
	 * @param ids - the {@link Collection} of {@link Artifact}
	 * @param suspect - the {@link Artifact} to look for
	 * @return - true if ids contains suspect
	 */
	public static boolean contains( Collection<Artifact> ids, Artifact suspect ) {
		for (Artifact id : ids) {
			if (artifactEquals(id, suspect))
				return true;
		}
		return false;
	}
	
	/**
	 * tests if a {@link Collection} of {@link Dependency} contains a {@link Dependency} <br/>
	 * tests {@link Identification}, then {@link VersionRange} on equality
	 * @param ids - the {@link Collection} of {@link Dependency}
	 * @param dependency - the {@link Dependency} to look for
	 * @return - true if ids contains dependency 
	 */
	public static boolean contains( Collection<Dependency> ids, Dependency dependency) {
		for (Dependency id : ids) {
			if (!identificationEquals(id, dependency))
				continue;
			VersionRange range1 = id.getVersionRange();
			VersionRange range2 = dependency.getVersionRange();
			return VersionRangeProcessor.equals(range1, range2);
		}
		return false;
	}
	
	
	/**
	 * returns the first matching occurrence of a {@link Solution} in a {@link Collection} of {@link Solution}
	 * @param ids - the {@link Collection} of {@link Solution}
	 * @param suspect - the {@link Solution} that acts as a template
	 * @return - the first matching {@link Solution} if any 
	 */
	public static Solution filter( Collection<Solution> ids, Solution suspect) {
		for (Solution id : ids) {
			if (solutionEquals(id, suspect))
				return id;
		}
		return null;
	}
	
	/**
	 * tests if a {@link Collection} of {@link Part} contains a {@link Part}
	 * @param ids - the {@link Collection} of {@link Part}
	 * @param suspect - the {@link Part} to look for 
	 * @return - true if ids contains suspect
	 */
	public static boolean contains( Collection<Part> ids, Part suspect) {
		for (Part id : ids) {
			if (partEquals(id, suspect))
				return true;
		}
		return false;
	}
	
	/**
	 * equals on {@link Part}<br/>
	 * tests {@link Solution} and then {@link PartTuple} via {@link PartTupleProcessor}
	 * @param part - first {@link Part}
	 * @param suspect - second {@link Part} 
	 * @return - true if match
	 */
	public static boolean partEquals( Part part, Part suspect) {
		// PGA this seems to be unused, as I cannot find any Part that can be casted to a Solution
		if (solutionEquals( (Solution) part, (Solution) suspect) == false)
			return false;
		return PartTupleProcessor.equals(part.getType(),suspect.getType());

	}
	/**
	 * returns the first matching occurrence of the {@link Identification} in the {@link Collection} of {@link Identification} 
	 * @param ids - the {@link Collection} of {@link Identification}
	 * @param id - the {@link Identification} to look for
	 * @return - true if ids contains suspect
	 */
	public static Identification get(Collection<Identification> ids, Identification id) {
		for (Identification suspect : ids) {
			if (identificationEquals( suspect, id))
				return suspect;
		}
		return null;
	}
	
	/**
	 * returns the first matching occurrence of the {@link Artifact} in the {@link Collection} of {@link Artifact} 
	 * @param ids - the {@link Collection} of {@link Artifact}
	 * @param id - the {@link Artifact} to look for
	 * @return - true if ids contains suspect
	 */
	public static Artifact get(Collection<Artifact> ids, Artifact id) {
		for (Artifact suspect : ids) {
			if (artifactEquals( suspect, id))
				return suspect;
		}
		return null;
	}
	
	
	/**
	 * tests if two {@link Dependency} have equal scope and optional flag
	 * @param id1 - first {@link Dependency}
	 * @param id2 - second {@link Dependency}
	 * @return - true if both scope and optional flag match 
	 */
	public static boolean testScopeAndOptional( Dependency id1, Dependency id2) {
		//
		// must be of the same scope 
		//
		//
		if (saveEquals( id1.getScope(), id2.getScope()) == false)
			return false;
			
		// must be optional on the same level.. 		
		if ( id1.getOptional() != id2.getOptional())
			return false;
		
		return true;		
	}
	
	/**
	 * finest equals test on {@link Dependency} <br/>
	 * calls {@link #coarseDependencyEquals(Dependency, Dependency)}, and then {@link #testScopeAndOptional(Dependency, Dependency)} 
	 * @param id1 - first {@link Dependency}
	 * @param id2 - second {@link Dependency}
	 * @return - true if a full match was possible
	 */
	public static boolean fineDependencyEquals ( Dependency id1, Dependency id2) {
		if (coarseDependencyEquals(id1, id2) == false)
			return false;
		return testScopeAndOptional(id1, id2);
	}
	
	/**
	 * equals test on {@link Dependency}<br/>
	 * tests  group id, artifact id, classifier, revision, {@link VersionRange} via the {@link VersionRangeProcessor}
	 * @param id1 - first {@link Dependency}
	 * @param id2 - second {@link Dependency}
	 * @return - true if a match was possible 
	 */
	public static boolean coarseDependencyEquals( Dependency id1, Dependency id2) {
		if (saveEquals( id1.getGroupId(), id2.getGroupId()) == false)
			return false;
		if (saveEquals( id1.getArtifactId(), id2.getArtifactId()) == false)
			return false;
		if (saveEquals( id1.getClassifier(), id2.getClassifier()) == false)
			return false;
		if (saveEquals( id1.getRevision(), id2.getRevision()) == false)
			return false;		
		if (!VersionRangeProcessor.equals( id1.getVersionRange(), id2.getVersionRange()))
			return false;		
		
		return true;		
	}
	
	/**
	 * equals test on {@link Dependency}<br/>
	 * tests group id, artifact id, revision and {@link VersionRange} via {@link VersionRangeProcessor}
	 * @param id1 - first {@link Dependency}
	 * @param id2 - second {@link Dependency}
	 * @return - true if a match was possible
	 */
	public static boolean coarsestDependencyEquals( Dependency id1, Dependency id2) {
		if (saveEquals( id1.getGroupId(), id2.getGroupId()) == false)
			return false;
		if (saveEquals( id1.getArtifactId(), id2.getArtifactId()) == false)
			return false;		
		if (saveEquals( id1.getRevision(), id2.getRevision()) == false)
			return false;		
		if (!VersionRangeProcessor.equals( id1.getVersionRange(), id2.getVersionRange()))
			return false;		
		
		return true;
		
	}
	
	/**
	 * returns the first matching {@link Dependency} in {@link Collection} of {@link Dependency}<br/>
	 * based on  {@link #coarseDependencyEquals(Dependency, Dependency)} 
	 * @param ids - the {@link Collection} of {@link Dependency}
	 * @param suspect - the template {@link Dependency} to look for 
	 * @return - the first  matching {@link Dependency}
	 */
	public static Dependency coarseContains( Collection<Dependency> ids, Dependency suspect) {
		for (Dependency id : ids) {
			if (coarseDependencyEquals(id, suspect))
				return id;
		}
		return null;
	}
	
	/**
	 * returns the first matching {@link Dependency} in {@link Collection} of {@link Dependency}<br/>
	 * based on  {@link #fineDependencyEquals(Dependency, Dependency)} 
	 * @param ids - the {@link Collection} of {@link Dependency}
	 * @param suspect - the template {@link Dependency} to look for 
	 * @return - the first  matching {@link Dependency}
	 */
	public static Dependency fineContains( Collection<Dependency> ids, Dependency suspect) {
		for (Dependency id : ids) {
			if (fineDependencyEquals(id, suspect))
				return id;
		}
		return null;
	}
	
	
	/**
	 * removes all {@link Dependency} from a {@link Collection} of {@link Dependency} that match a template {@link Dependency} <br/>
	 * based on {@link #coarseDependencyEquals(Dependency, Dependency)}
	 * @param dependencies - the {@link Collection} of {@link Dependency}
	 * @param suspect - the template {@link Dependency}
	 */
	public static void coarseDependencyRemove( Collection<Dependency> dependencies, Dependency suspect) {
		Collection<Dependency> drops = new ArrayList<Dependency>();
		for (Dependency dependency : dependencies) {
			if (ArtifactProcessor.coarseDependencyEquals( dependency, suspect)) { 
				drops.add( dependency);
				log.debug("%%% dropping dependency [" + NameParser.buildName(dependency) + "]");
			}
		}
		dependencies.removeAll( drops);
	}
	
	/**
	 * a compare on {@link Identification} 
	 * @param id1 - first {@link Identification}
	 * @param id2 - second {@link Identification}
	 * @return -  an int
	 */
	public static int compare( Identification id1, Identification id2) {
		String g1 = id1.getGroupId();
		String g2 = id2.getGroupId();
		if (
				(g1 == null) &&
				(g2 == null)
			)
			return 0;
		if (
				(g1 != null) &&
				(g2 == null)
			)
				return 1;
		
		if (
				(g1 == null) &&
				(g2 != null)
			)
				return -1;
		
		int retval= g1.compareTo( g2);
		
		if (retval != 0)
			return retval;
		
		String a1 = id1.getArtifactId();
		String a2 = id2.getArtifactId();
		
		if (
				(a1 == null) &&
				(a2 == null)
			)
			return 0;
		if (
				(a1 != null) &&
				(a2 == null)
			)
				return 1;
		if (
				(a1 == null) &&
				(a2 != null)				
			)
				return -1;
		retval = a1.compareTo( a2);
		return retval;
	}
	
	/**
	 * compare on two {@link Artifact}<br/>
	 * calls {@link #compare(Identification, Identification)}, and then compares the {@link Version} via {@link VersionProcessor}
	 * @param a1 - first {@link Artifact}
	 * @param a2 - second {@link Artifact}
	 * @return - the resulting int value 
	 */
	public static int compare( Artifact a1, Artifact a2) {
		int retval = compare ((Identification) a1, (Identification) a2);
		if (retval == 0) {	
			return VersionProcessor.compare(a1.getVersion(), a2.getVersion());					
		}			
		return retval;
	}
	
	/**
	 * compare on two {@link Dependency}<br/>
	 * calls {@link #compare(Identification, Identification)}, and then compares the {@link VersionRange} via {@link VersionRangeProcessor}
	 * @param a1 - first {@link Dependency}
	 * @param a2 - second {@link Dependency}
	 * @return - the resulting int value 
	 */
	public static int compare( Dependency a1, Dependency a2) {
		int retval = compare ((Identification) a1, (Identification) a2);
		if (retval == 0) {		
			if (retval == 0) {	
				return VersionRangeProcessor.compare(a1.getVersionRange(), a2.getVersionRange());					
			}					
		}			
		return retval;
	}
	
	
	
	/**
	 * creates a dependency with basic flags initialized
	 * @return - the created {@link Dependency} 
	 */
	public static Dependency createDependency() {
		Dependency dependency = Dependency.T.create();
		dependency.setUndetermined( false);
		dependency.setUnresolved( false);
		return dependency;
	}
	
	/**
	 * returns the artifact from the given artifacts that has the highest version
	 */
	public static <T extends Artifact> T getHighest(Iterable<T> artifacts) {
		Objects.requireNonNull(artifacts, "artifacts parameter must not be null");
		T highestArtifact = null;
		
		for (T solution: artifacts) {
			if (highestArtifact == null) {
				highestArtifact = solution;
			}
			else if (VersionProcessor.isHigher(highestArtifact.getVersion(), solution.getVersion())) {
					highestArtifact = solution;
			}
		}
		return highestArtifact;
	}

}
