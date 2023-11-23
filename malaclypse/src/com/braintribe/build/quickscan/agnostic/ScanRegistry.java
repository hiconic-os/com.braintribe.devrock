// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.quickscan.agnostic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.panther.SourceArtifact;

/**
 * a registry for the different artifacts during the scan process  <br/>
 * has a standard (fine) map of {@link SourceArtifact} to {@link ScanTuple} map, plus two (coarse) maps that map only the artifactId to a list of {@link ScanTuple} <br/>
 * the former for standard lookup (with a given {@link Version}, the others to look-up intervalled {@link VersionRange}
 * 
 * @author pit
 *
 */
public class ScanRegistry {
	private static Logger log = Logger.getLogger(ScanRegistry.class);
	private Map<SourceArtifact, ScanTuple> resolved = CodingMap.createHashMapBased( new SourceArtifactWrapperCodec( SourceArtifactWrapperCodecMode.artifact));
	private Map<SourceArtifact, List<ScanTuple>> resolvedParents = CodingMap.createHashMapBased( new SourceArtifactWrapperCodec( SourceArtifactWrapperCodecMode.name));
	private Map<SourceArtifact, List<ScanTuple>> unresolvedParents = CodingMap.createHashMapBased( new SourceArtifactWrapperCodec( SourceArtifactWrapperCodecMode.name));
	
	/**
	 * add a {@link ScanTuple} as resolved (and remove it from the unresolved)
	 * @param scanTuple - the {@link ScanTuple}
	 */
	public void addResolved( ScanTuple scanTuple) {
		SourceArtifact sourceArtifact = scanTuple.getSourceArtifact();
		ScanTuple duplicate = resolved.get(sourceArtifact);
		if (duplicate != null) {
			log.warn(String.format("replacing %s with %s", duplicate.getPom().getAbsolutePath(), scanTuple.getPom().getAbsolutePath()));
			
		}
		resolved.put( sourceArtifact, scanTuple);
		List<ScanTuple> tuples = resolvedParents.get(sourceArtifact);
		if (tuples == null) {
			tuples = new ArrayList<ScanTuple>();
			resolvedParents.put( sourceArtifact, tuples);
		}
		unresolvedParents.remove(sourceArtifact);
		tuples.add( scanTuple);
	}
	
	/**
	 * add a {@link ScanTuple} as unresolved 
	 * @param scanTuple - the {@link ScanTuple}
	 * 
	 */
	public void addUnResolved( ScanTuple scanTuple) {
		SourceArtifact sourceArtifact = scanTuple.getSourceArtifact();
	
		List<ScanTuple> tuples = unresolvedParents.get(sourceArtifact);
		if (tuples == null) {
			tuples = new ArrayList<ScanTuple>();
			unresolvedParents.put( sourceArtifact, tuples);
		}
		tuples.add( scanTuple);
	}

	
	/**
	 * return true if all {@link SourceArtifact} with this name have been resolved
	 * @param sourceArtifact - the {@link SourceArtifact} im questions
	 * @return
	 */
	public boolean areAllParentsResolved( SourceArtifact sourceArtifact) {
		if (unresolvedParents.get(sourceArtifact) == null && resolvedParents.get(sourceArtifact) != null)
			return true;
		return false;
	}
	
	/**
	 * returns the scan tuple with the highest matching version 
	 * @param parent - the {@link SourceArtifact}, of which actually only groupid and artifactid are relevant
	 * @param range - the {@link VersionRange} that must match 
	 * @return
	 */
	public ScanTuple getResolvedParent( SourceArtifact parent, VersionRange range) {
		List<ScanTuple> tuples = resolvedParents.get( parent);
		if (tuples == null || tuples.size() == 0) {
			return null;
		}
		// create a map of Version to ScanTuple
		Map<Version, ScanTuple> versionToTupleMap = new HashMap<Version, ScanTuple>();
		List<Version> versions = new ArrayList<Version>();
		for (ScanTuple tuple : tuples) {
			versionToTupleMap.put( tuple.getResolvedVersion(), tuple);
			versions.add( tuple.getResolvedVersion());
		}
		// sort ascending 
		versions.sort( VersionProcessor.comparator);

		// traverse reverse (descending)
		for (int i = versions.size()-1; i >= 0; i--) {
			Version suspect = versions.get(i);
			// find the highest matching version.. 
			if (VersionRangeProcessor.matches(range, suspect)) {
				return versionToTupleMap.get(suspect);
			}
		}
		return null;
	}
	
	public ScanTuple getResolved( SourceArtifact sourceArtifact) {		
		return resolved.get(sourceArtifact);
	}
	public Set<SourceArtifact> getResolved() {
		return resolved.keySet();
	}

	
}
