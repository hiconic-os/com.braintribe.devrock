// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.quickscan.agnostic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Element;

import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.build.quickscan.QuickImportScanException;
import com.braintribe.build.quickscan.QuickImportScanner;
import com.braintribe.build.quickscan.notification.QuickImportScanPhaseBroadcaster;
import com.braintribe.build.quickscan.notification.QuickImportScanPhaseListener;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.utils.xml.dom.DomUtils;

/**
 * scans the local file system - handles parents, yet doesn't make any assumptions about the structure of the working copy,
 * i.e. iterates multiple times to resolve all missing data.
 *  
 * @author pit
 *
 */
public class LocationAgnosticQuickImportScanner implements QuickImportScanner, QuickImportScanPhaseBroadcaster, QuickImportScanPhaseListener{
	private static Logger log = Logger.getLogger(LocationAgnosticQuickImportScanner.class);
	private ScanRegistry scanRegistry;	
	private Set<QuickImportScanPhaseListener> listeners = new HashSet<QuickImportScanPhaseListener>();
	private ProcessAbortSignaller scanAbortSignaller;
	private SourceRepository sourceRepository;

	
	
	@Override @Configurable @Required
	public void setSourceRepository(SourceRepository sourceRepository) {
		this.sourceRepository = sourceRepository;		
	}

	@Override
	public void addListener(QuickImportScanPhaseListener listener) {	
		listeners.add(listener);
	}

	@Override
	public void removeListener(QuickImportScanPhaseListener listener) {
		listeners.remove(listener);
	}
	@Override @Configurable
	public void setScanAbortSignaller(ProcessAbortSignaller scanAbortSignaller) {
		this.scanAbortSignaller = scanAbortSignaller;
	}

	@Override
	public List<SourceArtifact> scanLocalWorkingCopy(String workingCopy) {
		scanRegistry = new ScanRegistry();
	
		// scan the file system for tuples of *.pom and .project
		acknowledgeEnumerationPhase();
		File root = new File( workingCopy);		
		// scanning 
		long before = System.nanoTime();
		List<ScanTuple> tuples = listFiles( root);
		long after = System.nanoTime();
		if (log.isDebugEnabled()) {
			log.debug( "Scanning took [" + ((after - before)/1E6) + "] ms for [" + tuples.size() + "] projects");
		}
		
		// priming
		long beforePriming = after;
		for (ScanTuple scanTuple : tuples) {
			scanTuple.prime(sourceRepository);
		}
		long afterPriming = System.nanoTime();
		if (log.isDebugEnabled()) {
			log.debug( "priming took [" + ((afterPriming - beforePriming)/1E6) + "] ms for [" + tuples.size() + "] projects");
		}
		
		// analyze the found data, i.e. resolve the artifact's data 
		boolean doneProcessing = true;
		int resolvedNum = 0;
		int numPhase = 0;
		long beforeResolving = System.nanoTime();
				
		do {
			acknowledgeScanPhase( ++numPhase, tuples.size());
			Iterator<ScanTuple> iterator = tuples.iterator();		
			while (iterator.hasNext()) {
				ScanTuple tuple = iterator.next();
				try {
					if (!resolve( tuple)) {
						doneProcessing = false;
						scanRegistry.addUnResolved(tuple);
					}
					else {
						scanRegistry.addResolved( tuple);
						iterator.remove();
					}
				} catch (QuickImportScanException e) {
					log.warn("failed on [" + tuple.getPom().getAbsolutePath() + "] : [" + e.getLocalizedMessage() + "]");
					iterator.remove();
				} catch (VersionProcessingException e) {
					log.warn("failed on [" + tuple.getPom().getAbsolutePath() + "] : [" + e.getLocalizedMessage() + "]");
					iterator.remove();
				}
			}
			// done check..
			if (tuples.size() == 0) {
				log.debug("after [" + numPhase + "] phases, sucessfully completing scan with [" + scanRegistry.getResolved().size()  +"] artifacts resolved");
				break;
			}
			// abort check : no changes in last run, no use to proceed any further
			if (scanRegistry.getResolved().size() == resolvedNum) {
				doneProcessing = true;
				//
				log.warn("after [" + numPhase + "] phases, aborting scan with [" + tuples.size()  +"] artifacts unresolved");		
				for (ScanTuple tuple : tuples) {
					acknowledgeScanError( "remaining unresolved after [" + numPhase + "] phases", tuple.getPom().getAbsolutePath());
					log.warn("Unresolved: [" + tuple.getProject()+ "]");
				}
			}		
			else {
				resolvedNum = scanRegistry.getResolved().size();
			}
		} while (!doneProcessing);
		
	   //
		long afterResolving = System.nanoTime();
		if (log.isDebugEnabled()) {
			log.debug( "resolving took [" + ((afterResolving - beforeResolving)/1E6) + "] ms in [" + numPhase + "] phases for [" + scanRegistry.getResolved().size() + "] projects");
			log.debug("overall : [" + (afterResolving - before) / 1E6 + "] ms");
		}
		
		// extract the resolved data 
		List<SourceArtifact> sourceArtifacts = new ArrayList<SourceArtifact>();		
		sourceArtifacts.addAll( scanRegistry.getResolved());				
		return sourceArtifacts;
	}
	
	/**
	 * process a scan tuple 
	 * @param tuple - the {@link ScanTuple} to process 
	 * @return - true if it has been resolved, false otherwise
	 * @throws QuickImportScanException
	 * @throws VersionProcessingException
	 */
	private boolean resolve(ScanTuple tuple) throws QuickImportScanException, VersionProcessingException {
		// if all's present, resolve it instantly 
		SourceArtifact sourceArtifact = tuple.getSourceArtifact();
		
		if (	sourceArtifact.getGroupId() != null && !sourceArtifact.getGroupId().contains("$") && 
				sourceArtifact.getVersion() != null && !sourceArtifact.getVersion().contains( "$")
			) {
			tuple.setResolvedVersion( VersionProcessor.createFromString(sourceArtifact.getVersion()));
			acknowledgeResolved(sourceArtifact);
			return true;
		}
		
		// check if parent's here 		
		Element parentE = DomUtils.getElementByPath( tuple.getDocument().getDocumentElement(), "parent", false);
		if (parentE == null) {
			acknowledgeScanError( "resolving error : incomplete, yet no parent declaration", tuple.getPom().getAbsolutePath());
			throw new QuickImportScanException( "[" + tuple.getPom().getAbsolutePath() + "] is incomplete in its declaration, yet has no parent");
		}
	
		String group = tuple.resolve( DomUtils.getElementValueByPath( parentE, "groupId", false));
		String artifact = tuple.resolve( DomUtils.getElementValueByPath( parentE, "artifactId", false));		
		String parentRange = tuple.resolve( DomUtils.getElementValueByPath( parentE, "version", false));
		
		if (	group == null || group.contains("$") || 
				artifact==null || artifact.contains("$") || 
				parentRange == null || parentRange.contains( "$")
			){
			acknowledgeScanError( "resolving error : incomplete parent declaration", tuple.getPom().getAbsolutePath());
			throw new QuickImportScanException( "[" + tuple.getPom().getAbsolutePath() + "] has an incomplete parent declaration");
		}
				
		tuple.setScanKey( group + ":" + artifact + "#" + parentRange);
		tuple.setParentVersionRange( VersionRangeProcessor.createFromString( parentRange));
		
		// resolve parent 
		SourceArtifact parentKey = SourceArtifact.T.create();
		parentKey.setGroupId(group);
		parentKey.setArtifactId(artifact);
		parentKey.setVersion(parentRange);
		
		ScanTuple parent = null;
		if (!parentRange.contains( "[") && !parentRange.contains( "(")) {
			// direct version, so a fine look up can be made 
			parent = scanRegistry.getResolved(parentKey);					
		}
		else {
			// only try to make a look-up if all parents are resolved
			if (scanRegistry.areAllParentsResolved(parentKey)) {
				// resolve - get the highest matching parent from the registry
				parent = scanRegistry.getResolvedParent(parentKey, tuple.getParentVersionRange());
			}
		}
		// a parent has been found 
		if (parent != null) {
			String groupId = parent.resolve(sourceArtifact.getGroupId());
			if (groupId == null) {
				groupId = parent.getSourceArtifact().getGroupId();
			}
			sourceArtifact.setGroupId(groupId);
			String version = parent.resolve(sourceArtifact.getVersion());
			if (version == null) {
				version = parent.getSourceArtifact().getVersion();
			}
			sourceArtifact.setVersion(version);
			tuple.setResolvedVersion( VersionProcessor.createFromString(version));

			// combine properties from parent to current properties 
			accumulateProperties( tuple, parent);
			
			// resolved now, so return success 
			if (groupId != null && version != null) {
				acknowledgeResolved(sourceArtifact);
				return true;
			}
			//
			tuple.setScanKey( parent.getScanKey());
		}		
		return false;
	}

	/**
	 * find all tuples of *.pom and .project and turn the into {@link ScanTuple}
	 * @param directory - the {@link File} that points to the directory to scan 
	 * @return - the {@link List} of found {@link ScanTuple}
	 */
	private List< ScanTuple> listFiles( File directory) {
		List<ScanTuple> result = new ArrayList<ScanTuple>();
		if (scanAbortSignaller != null && scanAbortSignaller.abortScan()) {
			return result;
		}
		List<File> directories = new ArrayList<File>();
		if (log.isDebugEnabled()) {
			log.debug("found [" + directory.getAbsolutePath() + "]");
		}
		for (File file : directory.listFiles()) {
		
			if (file.getName().startsWith(".svn"))
				continue;						
			
			if (file.isDirectory()) {			
				directories.add( file);						
			} else {
				if (file.getName().equalsIgnoreCase("pom.xml")) {
						File project = new File( file.getParentFile(), ".project");
						File ant = new File( file.getParentFile(), "build.xml");
						if (project.exists()) {
							ScanTuple tuple = new ScanTuple( project, file, ant);
							try {
								//tuple.prime( sourceRepository);
								result.add(tuple);	
								acknowledgeDetected(file.getAbsolutePath());
							} catch (QuickImportScanException e) {
								log.error( "cannot prime tuple [" + tuple.getProject().getAbsolutePath() + "," + tuple.getPom().getAbsolutePath() + "]");
								acknowledgeScanError( "priming error", file.getAbsolutePath());
							}
						}
						else {					
						}

					return result;
				}
			}			
		}
		for (File dir : directories) {
			result.addAll( listFiles( dir));
		}

		return result;
	}
	
	/**
	 * import the parent's properties into the child, make sure not to override 
	 * @param tuple - the {@link ScanTuple} representing the child
	 * @param parent - the {@link ScanTuple} that is the parent
	 */
	private void accumulateProperties( ScanTuple tuple, ScanTuple parent) {
		for (Entry<String, String> entry : parent.getProperties().entrySet()) {
			if (tuple.getProperties().containsKey(entry.getKey()))
				continue;
			tuple.getProperties().put(entry.getKey(), entry.getValue());
		}
	}

	public File getProjectFile( SourceArtifact sourceArtifact) {
		return scanRegistry.getResolved(sourceArtifact).getProject();
	}

	public File getPomFile( SourceArtifact sourceArtifact) {
		return scanRegistry.getResolved(sourceArtifact).getPom();
	}
	
	/*
	 * listener implementations 
	 */

	@Override
	public void acknowledgeEnumerationPhase() {
		for (QuickImportScanPhaseListener listener : listeners) {
			listener.acknowledgeEnumerationPhase();
		}
	}

	@Override
	public void acknowledgeScanPhase(int phase, int remaining) {
		for (QuickImportScanPhaseListener listener : listeners) {
			listener.acknowledgeScanPhase(phase, remaining);
		}		
	}

	@Override
	public void acknowledgeDetected(String file) {
		for (QuickImportScanPhaseListener listener : listeners) {
			listener.acknowledgeDetected(file);
		}		
	}

	@Override
	public void acknowledgeScanError(String msg, String file) {
		for (QuickImportScanPhaseListener listener : listeners) {
			listener.acknowledgeScanError(msg, file);
		}
	}
	

	@Override
	public void acknowledgeUnresolved(int phases, String file) {
		for (QuickImportScanPhaseListener listener : listeners) {
			listener.acknowledgeUnresolved(phases, file);
		}	
	}

	@Override
	public void acknowledgeResolved(SourceArtifact sourceArtifact) {
		for (QuickImportScanPhaseListener listener : listeners) {
			listener.acknowledgeResolved( sourceArtifact);
		}
	}

	
	
	
}
