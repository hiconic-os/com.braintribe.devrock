// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.stream.XMLStreamException;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.maven.properties.OsPropertyResolver;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.codec.sax.ArtifactPomSaxCodec;
import com.braintribe.build.artifact.representations.artifact.pom.codec.sax.SaxArtifactPomExpertRegistry;
import com.braintribe.build.artifact.representations.artifact.pom.codec.stax.ArtifactPomStaxCodec;
import com.braintribe.build.artifact.representations.artifact.pom.codec.stax.StaxArtifactPomExpertRegistry;
import com.braintribe.build.artifact.representations.artifact.pom.codec.stax.staged.ArtifactPomStagedStaxCodec;
import com.braintribe.build.artifact.representations.artifact.pom.codec.stax.staged.StagedStaxArtifactPomExpertRegistry;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationBroadcaster;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.build.artifact.representations.artifact.pom.marshaller.ArtifactPomMarshaller;
import com.braintribe.build.artifact.representations.artifact.pom.properties.AbstractPropertyResolver;
import com.braintribe.build.artifact.representations.artifact.pom.properties.PomPropertyResolver;
import com.braintribe.build.artifact.retrieval.multi.cache.Cache;
import com.braintribe.build.artifact.retrieval.multi.coding.SolutionWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.resolving.ResolvingException;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Property;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.generic.reflection.GenericModelException;



/**
 * reads a pom, either to get the artifact's definition or its dependencies
 * handles variables, currently
 * ${project.groupId}, ${project.artifactId}, ${project.version}
 * 
 * @author pit
 *
 */
public class ArtifactPomReader extends AbstractPropertyResolver implements PomPropertyResolver, PomReaderNotificationBroadcaster, PomReaderNotificationListener {
	
	private final static Logger log = Logger.getLogger(ArtifactPomReader.class);
	private MavenSettingsReader settingsExpert;	
	private DependencyResolver dependencyResolver;	
	private StaxArtifactPomExpertRegistry staxRegistry;
	private SaxArtifactPomExpertRegistry saxRegistry;
	private StagedStaxArtifactPomExpertRegistry stagedStaxRegistry;
	private Cache cache;
	private boolean enforceParentResolving = true;
	private Set<PomReaderNotificationListener> listeners;	
	private boolean strict = false;
	private boolean detectParentLoops = false;
	private boolean identifyArtifactOnly = false;
	private ThreadLocal<Stack<String>> localAdHocResolvingStack = new ThreadLocal<Stack<String>>();
	private Map<String, String> parentReadMonitors = new ConcurrentHashMap<>();
	private boolean doNotAcceptSelfReferencesInPom = false;
	
	public enum CodecStyle {sax, stax, staged, marshall};
	
	private ArtifactPomStaxCodec pomStaxCodec;
	private ArtifactPomSaxCodec pomSaxCodec;
	private ArtifactPomStagedStaxCodec pomStagedStaxCodec;
	
	private ArtifactPomMarshaller marshaller = new ArtifactPomMarshaller();
	
	private CodecStyle codecStyle = CodecStyle.marshall;
	private boolean useFallbackForMissingVersionVariable = false;
	private boolean referenceLenient = false;
	private boolean expansionLenient = true;
	
	// deactivated on purpose : no support for platform dependent variable expanding
	//OsPropertyResolver osPropertyResolver = new BasicOsPropertyResolver();
	OsPropertyResolver osPropertyResolver = (v) -> null;
	
	/**
	 * if set to true, MC allows missing parents and missing import declarations in depMgt sections. It will still log and report an error, but not throw an exception.
	 * Otherwise, it will throw an exception with the aim of aborting a cmd-line build
	 * @param lenient - if true, be lenient, if false, throw exception 
	 */
	@Configurable
	public void setLeniency(boolean lenient) {
		this.referenceLenient = lenient;
	}
	
	/**
	 * if set to true, MC allows DEPENDENCIES to be invalid, i.e. incomplete in group/artifact/version/classifier/type/scope
	 * @param expansionLenient - if true. be lenient, if false, throw exception 
	 */
	@Configurable
	public void setExpansionLeniency(boolean expansionLenient) {
		this.expansionLenient = expansionLenient;
	}
		
	@Configurable @Required
	public void setCache(Cache cache) {
		this.cache = cache;
	}
	
	@Configurable
	public void setCodecStyle(CodecStyle codecToUse) {
		this.codecStyle = codecToUse;
	}
	
	@Configurable @Required
	public void setStaxRegistry(StaxArtifactPomExpertRegistry registry) {
		this.staxRegistry = registry;
	}
	
	@Configurable @Required
	public void setStagedStaxRegistry(StagedStaxArtifactPomExpertRegistry registry) {
		this.stagedStaxRegistry = registry;
	}
	
	@Configurable @Required
	public void setSaxRegistry(SaxArtifactPomExpertRegistry registry) {
		this.saxRegistry = registry;
	}
	
	@Configurable @Required
	public void setDependencyResolver(DependencyResolver dependencyResolver) {
		this.dependencyResolver = dependencyResolver;
	}
	
	@Configurable @Required
	public void setSettingsExpert(MavenSettingsReader settingsExpert) {
		this.settingsExpert = settingsExpert;
	}
	
	@Configurable
	public void setEnforceParentResolving(boolean enforceParentResolving) {
		this.enforceParentResolving = enforceParentResolving;
	}
	
	@Configurable
	public void setIdentifyArtifactOnly(boolean identifyArtifactOnly) {
		this.identifyArtifactOnly = identifyArtifactOnly;
	}
	
	@Configurable
	public void setDetectParentLoops(boolean detectParentLoops) {
		this.detectParentLoops = detectParentLoops;		
	}
			
	@Override
	public void addListener( PomReaderNotificationListener listener){
		if (listeners == null) { 
			listeners = new HashSet<PomReaderNotificationListener>();
		}
		listeners.add(listener);		
	}
	
	@Override
	public void removeListener( PomReaderNotificationListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
		if (listeners.isEmpty())
			listeners = null;
	}
	
	@Configurable	
	public void setOsPropertyResolver(OsPropertyResolver osPropertyInjector) {
		this.osPropertyResolver = osPropertyInjector;
	}
	
	private ArtifactPomStaxCodec getPomStaxCodec() {
		if (pomStaxCodec == null) {
			pomStaxCodec = new ArtifactPomStaxCodec();
			pomStaxCodec.setRegistry(staxRegistry);
		}
		return pomStaxCodec;
	}
	
	private ArtifactPomSaxCodec getPomSaxCodec() {
		if (pomSaxCodec == null) {
			pomSaxCodec = new ArtifactPomSaxCodec();
			pomSaxCodec.setRegistry(saxRegistry);
		}
		return pomSaxCodec;
	}
	
	private ArtifactPomStagedStaxCodec getPomStagedStaxCodec() {
		if (pomStagedStaxCodec == null) {
			pomStagedStaxCodec = new ArtifactPomStagedStaxCodec();
			pomStagedStaxCodec.setRegistry(stagedStaxRegistry);
		}
		return pomStagedStaxCodec;
	}
	
	
	/**
	 * resolve the artifact and read it <br/>
	 * careful: any properties set to the {@link Solution} passed are overwritten with the instance that 
	 * the underlying codec reads - ie. requestors for instance get lost
	 * @param walkScopeId - the scope id
	 * @param solution - the {@link Solution} to read
	 * @return - the {@link Solution} that was read 
	 * @throws PomReaderException - arrgh
	 */
	public Solution read( String walkScopeId, Solution solution) throws PomReaderException {		
		Solution resolvedSolution = resolveAndRead( walkScopeId, solution);
		// merge solution back (may not change the class unfortunately)
		if (resolvedSolution != null) {
			for (com.braintribe.model.generic.reflection.Property property : Solution.T.getProperties()) {
				property.set(solution, property.get(resolvedSolution));
			}
		}
		else {
			throw new PomReaderException( "Cannot resolve & read [" + NameParser.buildName(solution) + "]");
		}
		return solution;
	}
	
	/**
	 * resolve the artifact and read it 
	 * @param walkScopeId - the scope id
	 * @param groupId - the group id of the artifact
	 * @param artifactId - the artifact id of the artifact
	 * @param version - the version (as string) 
	 * @return - the read {@link Solution}
	 * @throws PomReaderException - arrgh 
	 */
	public Solution read( String walkScopeId, String groupId, String artifactId, String version) throws PomReaderException {	
		Solution solution = Solution.T.create();
		solution.setGroupId( groupId);
		solution.setArtifactId( artifactId);
		try {
			solution.setVersion (VersionProcessor.createFromString( version));
		} catch (VersionProcessingException e) {
			String msg = String.format( "cannot create version from [%s]", version);
			throw new PomReaderException(msg, e);
		} 
		// as this actually spawns a new reader, we must reimport the property map into this instance (required for access to the reader, not required for the artifact) 
		return resolveAndRead(walkScopeId, solution);				
	}
		

	/**
	 * the same..
	 * @param walkScopeId - the scope id 
	 * @param path - the qualified path to the pom file 
	 * @return - the {@link Solution} read 
	 * @throws PomReaderException - arrgh
	 */
	public Solution read(String walkScopeId, String path) throws PomReaderException{
		try {			
			return readPom( walkScopeId, new File(path));
		}
		catch (Exception e) {
			String msg = "Can't read pom [" + path + "] as " + e;
			log.error( msg, e);			
			throw new PomReaderException( msg, e);			
		}			
	}
	
	
	
	/**
	 * @param walkScopeId - the scope id
	 * @param file - the pom file 
	 * @return - the {@link Solution} read
	 * @throws PomReaderException - arrgh (I mean what else is it for? fun?
	 */
	private Solution getSolution(String walkScopeId, File file) throws PomReaderException {
		String fileName = file.getAbsolutePath();
		
		if (cache != null) {
			Solution test = cache.getSolution(walkScopeId, fileName);
			if (test != null)
				return test;
		}
		
		Solution solution = null;		
			
		try {			
			switch (codecStyle) {
			case sax:
				solution = getPomSaxCodec().decode(file);
				break;
			case staged:
				solution = getPomStagedStaxCodec().decode(file);	
				break;
			case stax:
				solution = getPomStaxCodec().decode(file);	
				break;
			case marshall:
			default:
				solution = marshaller.unmarshall(file);
				break;
			}
											
			Part pomPart = Part.T.create();
			ArtifactProcessor.transferIdentification(pomPart, solution);
			pomPart.setType( PartTupleProcessor.createPomPartTuple());
			pomPart.setLocation( file.getAbsolutePath());
			solution.getParts().add(pomPart);
			
		} catch (CodecException e) {
			acknowledgeReadErrorOnFile(walkScopeId, fileName, e.getLocalizedMessage());			
			String msg="cannot decode file ["+ fileName + "]";
			throw new PomReaderException(msg, e);
		}
		catch (XMLStreamException e) {
			acknowledgeReadErrorOnFile(walkScopeId, fileName, e.getLocalizedMessage());			
			String msg="cannot unmarshall file ["+ fileName + "]";
			throw new PomReaderException(msg, e);
		}
	
		return solution;
	}
	
	/**
	 * cheap identification : no lookup happens here 
	 * @param walkScopeId - the scope id
	 * @param contents - the pom as string 
	 * @return - the {@link Solution} identified 
	 * @throws PomReaderException - arrgh
	 */
	@Deprecated
	private Solution getSolution(String walkScopeId, String contents) throws PomReaderException {
		Artifact id = CheapPomReader.identifyPom(contents);
		
		Solution solution = null;
		if (cache != null) {
			solution = cache.getSolution( walkScopeId, id.getGroupId(), id.getArtifactId(), VersionProcessor.toString(id.getVersion()));		
		}
		if (solution != null)
			return solution;
		
		
		try {
			//solution = getPomCodec().decode( contents);		
		} catch (CodecException e) {
			acknowledgeReadErrorOnString( walkScopeId, contents, e.getLocalizedMessage());			
			String msg="cannot decode passed string";
			throw new PomReaderException(msg, e);
		}
		return solution;
	}
	
	private Object pomMarshallingMonitor = new Object();
	
	/**
	 * read the pom for the file passed (the pom's content)
	 * @param walkScopeId - the scope id 
	 * @param file - the file to read 
	 * @return - the {@link Solution} read 
	 * @throws PomReaderException - arrgh
	 */
	public Solution readPom( String walkScopeId, File file) throws PomReaderException {		
		// load model
		String fileName = file.getAbsolutePath();
		
		if (cache != null) {
			Solution test = cache.getSolution(walkScopeId, fileName);
			if (test != null)
				return test;
		}
		
		Solution solution = null;

		synchronized ( pomMarshallingMonitor) {
			
			if (cache != null) {
				solution = cache.getSolution(walkScopeId, fileName);
				if (solution != null) {
					return solution;
				}	
			}
			try {			
				solution = getSolution(walkScopeId, file);
				if (solution.getResolved())
					return solution;
				process( walkScopeId, solution);
							
				if (cache != null) {
					cache.addSolution(walkScopeId, solution, fileName);
				}		
				// add traversing event
				acknowledgeSolutionAssociation(walkScopeId, fileName, solution);
			
				return solution;
			} catch (Exception e) {
				throw new PomReaderException( e);
			}	
		}
	}
	
	/**
	 * read the pom for the file passed (the pom's content) 
	 * @param walkScopeId - the id of the scope 
	 * @param content - the content of the pom  
	 * @return - the {@link Solution} read 
	 * @throws PomReaderException - arrgh
	 */
	@Deprecated
	public Solution readPom( String walkScopeId, String content) throws PomReaderException {		
		// load model 
		try {
			Solution solution = getSolution( walkScopeId, content);
			if (solution.getResolved())
				return solution;
			process( walkScopeId, solution);							
			acknowledgeSolutionAssociation(walkScopeId, "<none>", solution);
			return solution;
		} catch (Exception e) {
			throw new PomReaderException( e);
		}				
	}
	
	
	/**
	 * resolve all properties, ensure all artifact data's set and all dependency data's set
	 *  @param walkScopeId - the id of the scope
	 * @param solution - the {@link Solution} to process 
	 * @return - the very same solution, but enriched/resolved 
	 * @throws PomReaderException - arrgh
	 */
	public Solution process(String walkScopeId,  Solution solution) throws PomReaderException {
		if (solution.getResolved())
			return solution;
	
		//
		// resolve all properties 
		//
		
		// 
		// convert properties 
		//
		Set<Property> properties = solution.getProperties();
		if (properties != null) {	
			PomPropertyHandler.attachToArtifact( walkScopeId, solution, properties, this);	
			PomPropertyHandler.resolveProperties( walkScopeId, solution, properties, this);
		}
		// parent reference
		ensureParentReferenceData( solution);
		
		// artifact data 
		ensureArtifactData( walkScopeId, solution);
		if (!identifyArtifactOnly) {
			ensureDependencyData(walkScopeId, solution);
		}
		
		
		// might be that at the time of reading the pom, the data wasn't know yet, 
		// so the part (the pom part) may be incomplete in its declarations. 
		ensurePartData( solution);
		
		// if we configured to enforce resolving the parent, force it 
		if (enforceParentResolving) {
			Solution parent = readParent( walkScopeId, solution);
			solution.setResolvedParent(parent);
		}
		
		solution.setResolved(true);
		// can only notify a parent relation here, as child is only resolved at this point.
		if (solution.getResolvedParent() != null && solution.getResolvedParent().getResolved()) {
			acknowledgeParentAssociation(walkScopeId, solution, solution.getResolvedParent());
		}
		return solution;	 
	}
	
	private void ensurePartData(Solution solution) {		
		for (Part part : solution.getParts()) {
			ArtifactProcessor.transferIdentification(part, solution);
		}
		
	}
	
	
	private String ensureParentProperty ( Solution solution, String expression) {
		while (requiresEvaluation(expression)) {
			String property = extract(expression);		
			String value = resolveProperty(solution, property);
			if (value != null) {
				expression = replace(property, value, expression);
			}
			else {
				throw new PomReaderException ("cannot resolve property [" + property + "] in parent declaration of [" + NameParser.buildName(solution) + "]");
			}
		}
		return expression;
			
	}

	private void ensureParentReferenceData(Solution solution) {
		Dependency parent = solution.getParent();
		if (parent == null) {
			return;
		}
		String grpId = parent.getGroupId();
		parent.setGroupId( ensureParentProperty(solution, grpId));
 
			
		String artId = parent.getArtifactId();
		parent.setArtifactId( ensureParentProperty(solution, artId));
		
		VersionRange range = parent.getVersionRange();
		String rangeAsString = range.getOriginalVersionRange();
		if (requiresEvaluation(rangeAsString)) {
			String resolvedValue = expandValue("", solution, rangeAsString);
			range = VersionRangeProcessor.createFromString(resolvedValue);
			parent.setVersionRange(range);			
		}
		
		
	}

	/**
	 * immediately resolve a dependency, automatically the highest matching version is taken. 
	 * @param walkScopeId - the id of the scope	
	 */
	private Solution resolveDependencyAdHoc( String walkScopeId, Dependency dependency) throws ResolvingException, PomReaderException {
		// if stack isn't empty, check if dependency already exists, and if so, throw
		Stack<String> stack = localAdHocResolvingStack.get();
		if (stack == null) {
			stack = new Stack<String>();
			localAdHocResolvingStack.set( stack);
		}
		try {
			String name = NameParser.buildName(dependency);
			if (!stack.isEmpty()) {
				if (stack.contains(name)) {
					throw new ResolvingException("direct parent loop detected on [" + name + "]");
				}
			}
			stack.push(name);
			// push dependency on stack 		
			Set<Solution> resolvedParentDependencies = dependencyResolver.resolveTopDependency(walkScopeId, dependency);
			if (resolvedParentDependencies.size() == 0) {
				return null;
			}
			List<Solution> resolvedParents = new ArrayList<Solution>( resolvedParentDependencies);
			Collections.sort( resolvedParents, new Comparator<Solution>() {
	
				@Override
				public int compare(Solution o1, Solution o2) {					
					if (VersionProcessor.isHigher( o1.getVersion(), o2.getVersion()))
						return 1;
					if (VersionProcessor.isLess( o1.getVersion(), o2.getVersion()))
						return -1;
					return 0;
				}
				
			});
			// add the highest..
			Solution parent = resolvedParents.get( resolvedParents.size()-1);
			return parent;
		}
		finally {
			// pop dependency from stack 
			if (!stack.isEmpty()) {
				stack.pop();
			}
		}
	}
	
	/**
	 * try to the read the parent pom - if any's present
	 * @param walkScopeId - the id of the scope
	 * @param child - the current artifact
	 * @return - the processed parent
	 * @throws PomReaderException - arrgh
	 */
	private Solution readParent(String walkScopeId, Artifact child) throws PomReaderException {		
		Dependency parentDependency = child.getParent();
		// no dependency 
		if (parentDependency == null)
			return null;
		
		Solution parentSolution = child.getResolvedParent();
		// processed solution attached 
		if (parentSolution != null && parentSolution.getResolved()) {		
			return parentSolution;
		}

		
		return resolveParentLocked(walkScopeId, child, parentDependency);		
	}

	private Solution resolveParentLocked(String walkScopeId, Artifact child, Dependency parentDependency) {
		String monitorName = NameParser.buildName(parentDependency);
		
		String monitor = this.parentReadMonitors.computeIfAbsent(monitorName, String::new);
		
		synchronized (monitor) {
			return resolveParent(walkScopeId, child, parentDependency);
		}
	}
	
	private Solution resolveParent(String walkScopeId, Artifact child, Dependency parentDependency) {
		// must find solution 		
		Solution parent = null;
		if (cache != null) {
			parent = cache.getResolvedParentSolution(walkScopeId, parentDependency);					
		}
		
		if (parent == null ) {
			// check if we're in a loop
						
			// 
			try {
				parent = resolveDependencyAdHoc(walkScopeId, parentDependency);
								
				if (parent != null) {					
					// check whether we should read this .. 
					cache.addResolvedParentSolution(walkScopeId, parentDependency, parent);
					// replace stub by actual solution			
					child.setResolvedParent(parent);
					if (parent.getResolved() == false) {
						Stack<Solution> stack = null;
						if (detectParentLoops) {
							stack = cache.getParentStackForScope(walkScopeId);
						}
						if (stack != null) {
							checkStackForParent( stack, parent);
							stack.push(parent);
						}
						try {
							parent = read(walkScopeId, parent);							
						}
						finally {
							if (stack != null) {
								stack.pop();
							}
						}
					}	
					// this is a direct parent relation, right?
				}
				else {	
					String msg = "cannot resolve parent reference within [" + NameParser.buildName(parentDependency) + "] from [" + NameParser.buildName(child) + "]";
					log.error( msg);
					acknowledgeParentAssociationError(walkScopeId, child,  parentDependency.getGroupId(), parentDependency.getArtifactId(), VersionRangeProcessor.toString( parentDependency.getVersionRange()));
					if (referenceLenient)
						return null;
					else {
						throw new PomReaderException( msg);
					}
				}
				} catch (ResolvingException e) {
					throw new PomReaderException( "cannot resolve parent for [" + NameParser.buildName(parentDependency) + "]", e);
				}
		}
		return parent;
	}

	private void checkStackForParent(Stack<Solution> stack, Solution parent) throws PomReaderException{
		Set<Solution> codingSet = CodingSet.createHashSetBased( new SolutionWrapperCodec());
	
		codingSet.addAll( stack.subList(0, stack.size()));
		if (codingSet.contains(parent)) {
			stack.push(parent);
			Iterator<Solution> iterator = stack.iterator();
			StringBuilder builder = new StringBuilder();
			while (iterator.hasNext()) {
				if (builder.length() > 0) {
					builder.append("->");
				}
				builder.append( NameParser.buildName( iterator.next()));
			}
			String msg = "parent loop detected : [" + builder.toString() + "]";
			log.error(msg);
			throw new PomReaderException( msg);
		}
	}

	
	/**
	 * build an artifact name 
	 * @param artifact - {@link Solution} to get the name of 
	 * @return - the condensed name 
	 */
	private String saveArtifactNameBuilder( Solution artifact) {
		String buildName ="";
			
		buildName = artifact.getGroupId() + ":" + artifact.getArtifactId() + "#";
		if (artifact.getVersion() != null)
			buildName += VersionProcessor.toString( artifact.getVersion());
		else {
			buildName += "<na>";
		}
		 
		return buildName;
	}

	/**
	 * make sure all relevant data of the artifact section's present <br/>
	 * looks up groupId, artifactId and version, ev from the parent structure  
	 */
	private void ensureArtifactData(String walkScopeId, Solution artifact) throws PomReaderException {
		//
		// artifact declaration
		//					
		boolean interpreted = false;
		String groupIdAsDeclared = artifact.getGroupId();
		String groupId = ensureGroupId(walkScopeId, artifact, groupIdAsDeclared);
		if (groupId == null) {
			throw new PomReaderException("cannot find a value for the expression [" + groupIdAsDeclared + "] standing in for groupId of [" + NameParser.buildName(artifact) + "]");
		}
		artifact.setGroupId(groupId);
		
		String artifactIdAsDeclared = artifact.getArtifactId();
		String artifactId = ensureArtifactId(walkScopeId, artifact, artifactIdAsDeclared);
		if (artifactId == null) {
			throw new PomReaderException("cannot find a value for the expression [" + artifactIdAsDeclared + "] standing in for artifactId of [" + NameParser.buildName(artifact) + "]");
		}
		artifact.setArtifactId(artifactId);
		
		String versionAsString = null;
		
		Version version = artifact.getVersion();
		if (version != null) {		
			versionAsString = VersionProcessor.toString( version);		
		}
		
		interpreted = true;			

		String expandedVersionAsString = expandValue(walkScopeId, artifact, versionAsString);
		if (expandedVersionAsString != null &&
			!expandedVersionAsString.equalsIgnoreCase(versionAsString)
		    ) {
			versionAsString = expandedVersionAsString;
		}
		// try parent section 	
		// try parent's version 
		if (versionAsString == null || versionAsString.equalsIgnoreCase( "${project.version}")) {
			versionAsString = lookupViaParentStructure( walkScopeId, artifact, "${project.version}");
		}
		
		// try dependency management as a last resort 
		if (versionAsString == null) {
			versionAsString = lookupVersionViaDependencyManagement( walkScopeId, artifact, groupId, artifactId);
		}

		if (versionAsString == null) {
			throw new PomReaderException("cannot find a value for the expression [" + versionAsString + "] standing in for a version of [" + NameParser.buildName(artifact) + "]");
		}
		
		if (interpreted) {
			artifact.setGroupId(groupId);
			artifact.setArtifactId(artifactId);
			try {
				artifact.setVersion( VersionProcessor.createFromString(versionAsString));
			} catch (VersionProcessingException e1) {
				String msg = String.format( "cannot generate version range from string [%s]", version);
				log.error( msg, e1);
				throw new PomReaderException(msg, e1);
			}
		}
		
		// packaging
		artifact.setPackaging( expandValue(walkScopeId, artifact, artifact.getPackaging()));
		
		
	}
		
	
	/**
	 * ensure all data in the dependency section is property resolved 	
	 */
	private void ensureDependencyData(String walkScopeId, Solution artifact) throws PomReaderException {
		// dependency management section
		for (Dependency dependency : artifact.getManagedDependencies()) {							
			ensureDependency(walkScopeId, artifact, dependency);		
		}
		
		// standard dependencies 
		for (Dependency dependency : artifact.getDependencies()) {							
			ensureDependency(walkScopeId, artifact, dependency);
			validate( artifact, dependency);
		}
	
	}
	
	

	/**
	 * detect simple (stupid) loops : if a dependency leads to the same artifact identification, abort the whole show
	 * @param artifact - the {@link Solution} whose dependencies we traverse
	 * @param dependency - the {@link Dependency} in question 
	 */
	private void validate(Solution artifact, Dependency dependency) {
		if (
				artifact.getGroupId().equalsIgnoreCase(dependency.getGroupId()) &&
				artifact.getArtifactId().equalsIgnoreCase( dependency.getArtifactId()) 
			) {
				String msg = "Solution [" + NameParser.buildName(artifact) + "] references itself via dependency";
				markDependencyAsInvalid(dependency, msg);
				// TODO reactivate but compare only Solution with Solution in order to detect cycles
				if (doNotAcceptSelfReferencesInPom) {
					throw new IllegalStateException(msg);
				}
		}
		
	}

	/**
	 * ensure a single dependency. if expansionLenient, no exception will be thrown, but the dependency marked as !isIdentified
	 * @param walkScopeId -
	 * @param artifact - the parent {@link Solution}
	 * @param dependency - the {@link Dependency}
	 */
	private void ensureDependency(String walkScopeId, Solution artifact, Dependency dependency) {
	
		// classifier 
		String declaredClassifier = dependency.getClassifier();
		if (declaredClassifier != null) {
			String resolvedClassifier = expandValue(walkScopeId, artifact, declaredClassifier);
			if (resolvedClassifier == null) {
				String msg = "cannot extract classifier [" + declaredClassifier + "] for dependency [" + dependency.getGroupId() + ":" + dependency.getArtifactId() + "] within [" + NameParser.buildName(artifact) + "]";
				markDependencyAsInvalid(dependency, msg);
			}
			else {
				dependency.setClassifier( resolvedClassifier);
			}
		}
		
		// type 
		String declaredType = dependency.getType();
		if (declaredType != null) {
			String resolvedType = expandValue(walkScopeId, artifact, declaredType);
			if (resolvedType == null) {
				String msg = "cannot extract type [" + declaredType + "] for dependency [" + dependency.getGroupId() + ":" + dependency.getArtifactId() + "] within [" + NameParser.buildName(artifact) + "]";
				markDependencyAsInvalid(dependency, msg);			
			}
			else {
				dependency.setType( resolvedType);
			}
		}
		
		// scope
		String declaredScope = dependency.getScope();
		if (declaredScope != null) {
			String resolvedScope = expandValue(walkScopeId, artifact, declaredScope);
			if (resolvedScope == null) {
				String msg = "cannot extract type [" + declaredScope + "] for dependency [" + dependency.getGroupId() + ":" + dependency.getArtifactId() + "] within [" + NameParser.buildName(artifact) + "]";
				markDependencyAsInvalid(dependency, msg);			
			}
			else {
				dependency.setScope( resolvedScope);
			}
		}
		
	
		// group id 
		// if missing, is resolved via project.group.
		// if present, but cannot be resolved:
		//	if lenient - resolved via project.group
		//  if strict - throw exception 
		String groupId;			
		String groupIdAsDeclared = dependency.getGroupId();
		
		groupId = ensureGroupId(walkScopeId, artifact, groupIdAsDeclared);
		if (groupId == null) {
			String msg = "cannot extract groupid for dependency [" + dependency.getGroupId() + ":" + dependency.getArtifactId() + "] within [" + NameParser.buildName(artifact) + "]";			
			markDependencyAsInvalid(dependency, msg);
		}
		else {
			dependency.setGroupId(groupId);
		}
		
		// artifact id 
		// if missing, is resolved via project.artifact.
		// if present, but cannot be resolved:
		//	if lenient - resolved via project.artifact
		//  if strict - throw exception
		String artifactId;
		String artifactIdAsDeclared = dependency.getArtifactId();
		artifactId = ensureArtifactId(walkScopeId, artifact, artifactIdAsDeclared);
		if (artifactId == null) {
			String msg = "cannot extract groupid for dependency [" + dependency.getGroupId() + ":" + dependency.getArtifactId() + "] within [" + NameParser.buildName(artifact) + "]";			
			markDependencyAsInvalid(dependency, msg);
		}
		else {
			dependency.setArtifactId(artifactId);
		}
		
		// version
		// if missing, lookup via dependency management
		// if present, but cannot be resolved  
		// 
		
		String versionAsDeclared = null;
		VersionRange version = dependency.getVersionRange();
		if (version != null) {						
			versionAsDeclared = VersionRangeProcessor.toString( version);
		}
		String versionAsString = versionAsDeclared;
		// a value's set 
		if (versionAsString != null) { 				
			// resolve it
			String resolvedVersionAsString = expandValue(walkScopeId, artifact, versionAsString);
			if (resolvedVersionAsString == null) {
				String msg = String.format( "cannot extract version range from [" + versionAsString + "] for dependency [" + groupId + ":" + artifactId + "] within [" + NameParser.buildName(artifact));
				markDependencyAsInvalid(dependency, msg);
			}
			else {
				try {
					dependency.setVersionRange( VersionRangeProcessor.createFromString(resolvedVersionAsString));
				} catch (VersionProcessingException e) {
					String msg = String.format( "cannot build a valid version range from [" + versionAsString + "] within [" + NameParser.buildName(artifact));
					markDependencyAsInvalid(dependency, msg);
					return;
				}
			}
		} 			
		else {		
			// no value -> check if it's somewhere in the parent-chain dependency management 
			
			String classifier = dependency.getClassifier();
			Dependency lookedUp = lookupDependencyViaDependencyManagement(walkScopeId, artifact, groupId, artifactId, classifier);
			if (lookedUp != null) {
				dependency.setGroupId( lookedUp.getGroupId());
				dependency.setArtifactId( lookedUp.getArtifactId());
				String versionRangeAsRetrieved = lookedUp.getVersionRange().getOriginalVersionRange();
				if (requiresEvaluation(versionRangeAsRetrieved)) {
					String expandedVersionRange = expandValue(walkScopeId, artifact, versionRangeAsRetrieved);
					if (expandedVersionRange == null) {
						String msg = String.format( "cannot build a valid version range from [" + versionRangeAsRetrieved + "] for dependency [" + groupId + ":" + artifactId + "] within [" + NameParser.buildName(artifact));					
						markDependencyAsInvalid(dependency, msg);
					}
					try {
						dependency.setVersionRange( VersionRangeProcessor.createFromString(expandedVersionRange));
					} catch (VersionProcessingException e) {
						String msg = String.format( "cannot build a valid version range from [" + versionAsString + "] within [" + NameParser.buildName(artifact));
						markDependencyAsInvalid(dependency, msg);
						return;
					}
				}
				else {
					dependency.setVersionRange( lookedUp.getVersionRange());
				}
				String lookupScope = lookedUp.getScope();
				if (dependency.getScope() == null && lookupScope != null) {
					dependency.setScope( lookupScope);
				}
				dependency.getExclusions().addAll(lookedUp.getExclusions());
			}
			
			else {
				if (useFallbackForMissingVersionVariable) {
					log.warn( "cannot extract valid dependency version for [" + dependency.getGroupId() + ":" + dependency.getArtifactId() + "] within [" + NameParser.buildName(artifact) + "] - fall back used.");
					// try to find a declaration like $version.<artifactid> 
					versionAsString = expandValue(walkScopeId, artifact, "${version." + artifactId +"}");
					// still no value -> use project or parent as look-ups
					if (versionAsString == null) {
						versionAsString = resolve(artifact, "${version}");
					}
					if (versionAsString == null) {
						versionAsString = resolve(artifact, "${parent.version}");
					}
					if (versionAsString != null) {
						try {
							dependency.setVersionRange( VersionRangeProcessor.createFromString(versionAsString));
						} catch (VersionProcessingException e) {
							String msg = String.format( "cannot build a valid version range from [" + versionAsString + "] within [" + NameParser.buildName(artifact));
							markDependencyAsInvalid(dependency, msg);
							return;
						}
					} 
					else {
						String msg = String.format( "cannot resolve internal dependency declaration of [" + NameParser.buildName(dependency) + "] of [" + NameParser.buildName(artifact));
						markDependencyAsInvalid(dependency, msg);
						return;
					}
				}
				else {
					String msg = String.format( "cannot extract valid dependency version for [" + dependency.getGroupId() + ":" + dependency.getArtifactId() + "] within [" + NameParser.buildName(artifact) + "]");
					markDependencyAsInvalid(dependency, msg);
					return;
				}
			}
		}
	}

	private void markDependencyAsInvalid(Dependency dependency, String msg) {
		if (!expansionLenient) {
			log.error( msg);
			throw new PomReaderException(msg);
		}
		else {
			log.warn( msg + " -> invalidated dependency");
			dependency.setIsInvalid(true);
			dependency.setInvalidationReason(msg);
		}
	}

	private String ensureArtifactId(String walkScopeId, Solution artifact, String artifactIdAsDeclared) throws PomReaderException {	
		String artifactId;
		if (artifactIdAsDeclared == null) {
			artifactId = expandValue( walkScopeId, artifact, "${project.getArtifactId}");
		}
		else {
			artifactId = expandValue( walkScopeId, artifact, artifactIdAsDeclared);
		}			
		if (artifactId == null) {	
			String artifactName = saveArtifactNameBuilder( artifact);
			acknowledgeVariableResolvingError( walkScopeId, artifact, artifactIdAsDeclared);
			if (strict) {
				throw new PomReaderException("cannot resolve dependency's artifact id [" + artifactIdAsDeclared + "] in [" + artifactName + "]");
			}
			else {
				artifactId = expandValue( walkScopeId, artifact, "${project.artifactId}");
			}
		}
		return artifactId;
	}

	private String ensureGroupId(String walkScopeId, Solution artifact, String groupIdAsDeclared) throws PomReaderException {
		String groupId;
		if (groupIdAsDeclared != null) {
			return expandValue(walkScopeId, artifact, groupIdAsDeclared);			
		}		
		if (artifact.getParent() != null) {
			String parentGroup = artifact.getParent().getGroupId();
			parentGroup = ensureParentProperty(artifact, parentGroup);
			return parentGroup;
		}
		groupId = expandValue( walkScopeId, artifact, "${project.groupId}");
		if (groupId == null) {
			groupId = lookupPropertyByReflectionViaParentStructure(walkScopeId, artifact, "groupId");
		}
		
		if (groupId == null) {
			String artifactName = saveArtifactNameBuilder( artifact);
			acknowledgeVariableResolvingError( walkScopeId, artifact, groupIdAsDeclared);
			if (strict) {
				throw new PomReaderException("cannot resolve dependency's group id [" + groupIdAsDeclared + "] in [" + artifactName + "]");
			}
			else {
				groupId = expandValue( walkScopeId, artifact, "${project.groupId}");
			}
		}
		return groupId;
	}

	

	/**
	 * checks if the pom as defined by the file is a redirected pom, see {@literal redirected( T artifact, Document pomDoc)}  
	 * @param location - the location of the {@link File} that contains the pom 
	 * @return - true if redirected, false otherwise 
	 * @throws PomReaderException - arrgh
	 */
	public Solution redirected(String walkScopeId, String location) throws PomReaderException {		
		try {					
			Solution suspect = null;
			if (cache != null) {
				suspect = cache.getSolution(walkScopeId, location);
			}
			if (suspect == null) {
				log.debug("reading for redirect [" + location + "]");
				suspect = read( walkScopeId, location);
			}
			Solution redirection = null;			
			if (cache != null) {
				redirection = cache.getRedirection(walkScopeId, suspect);
			}
			// not cached, get it from the solution itself  
			if (redirection == null) {
				redirection = suspect.getRedirection();
			}
			if (redirection == null)
				return null;
			

			// the pom which contains the relocation must contain all relevant artifact data (directly or indirectly) 
			ensureArtifactData(walkScopeId, suspect);
			
			// if any of the data is missing in the relocation, use the ones from the pom containing it 
			String grpId = redirection.getGroupId();
			if (grpId == null) {
				grpId = suspect.getGroupId();			
			}			
			redirection.setGroupId(grpId);
						
			String artId = redirection.getArtifactId();
			if (artId == null) {
				artId = suspect.getArtifactId();			
			}			
			redirection.setArtifactId(artId);
			
			Version vers = redirection.getVersion();
			if (vers == null) {
				redirection.setVersion( suspect.getVersion());
			}
										
			if (cache != null) {
				cache.addRedirection(walkScopeId, suspect, redirection);				
			}
			return redirection;
		}
		catch (Exception e) {
			String msg = "Can't read pom from location [" + location + "]";
			log.error( msg, e);
			throw new PomReaderException( msg, e);			
		}		
	}
	
	private String dropFirstToken( String variable) {
		int p = variable.indexOf( '.');
		if (p < 0)
			return null;
		return variable.substring( p+1);
	}
	
	private String dropFirstTokenAndWrap( String variable) {
		String v = dropFirstToken(variable);
		if (v != null) {
			return "${" + v + "}";
		}
		return null;
	}
	
	
	@Override
	public String expandValue(String walkScopeId, Artifact artifact, String line) {
		if (line == null)
			return null;
		
		String expression = line;
		String lastExpression = null;
		while (requiresEvaluation( expression)) {
			String variable = extract( expression);
			String value = null;
			// settings 
			if (variable.startsWith( "settings")) {				
				value = settingsExpert.resolveValue( dropFirstToken(variable));
			}
			// project or pom
			if (value == null) {
				if (variable.startsWith( "project") || variable.startsWith( "pom")) {
					value = expandValue( walkScopeId, artifact, dropFirstTokenAndWrap(variable)); 
				}
			}
			// parent
			if (value == null) {
				if (variable.startsWith( "parent")) {
					if (artifact.getParent() != null && artifact.getResolvedParent() == null) {
						artifact.setResolvedParent( readParent(walkScopeId, artifact));
					}
					Artifact parent = artifact.getResolvedParent();
					if (parent != null) {
						value = expandValue(walkScopeId, parent, dropFirstTokenAndWrap(variable));
					}
					/*
					else {
						signalResolvingError( parent, variable, expression);						
					}
					*/
				}
			}
			// env
			if (value == null) {
				if (variable.startsWith( "env")) {
					value = getEnvironmentProperty(dropFirstTokenAndWrap(variable));
				}
			}
			// no prefix, could be anything
			
			// try local property
			if (value == null) {
				value = resolveProperty( artifact, variable);
			}
			
			if (value == null) {
				// system property 				
				value = getSystemProperty(variable);	
				if (value == null) {
					// might be a property? 
					value = resolveProperty(artifact, variable);
					if (value == null) {
						try {
							value = lookupPropertyViaParentStructure(walkScopeId, artifact, variable);
						} catch (PomReaderException e) {
							log.error( "cannot resolve variable [" + variable + "]", e);
						}
					}	
					
					if (value == null) {
						value = resolvePropertyViaParentStructure(walkScopeId, artifact, variable);						
					}
					if (value == null) {
						// might be an improperly named project value, i.e. "project." is missing 
						value = resolveValue(artifact, variable);												
					}
					if (value == null) {
						value = settingsExpert.resolveProperty( variable);
					}
				
				}
			}
			if (value == null) {
				if (log.isDebugEnabled()) {
					log.debug("cannot resolve variable [" + variable + "] in expression [" + expression + "] found within [" + NameParser.buildName(artifact) + "]");
				}
				return null;
			}
			
			expression = replace(variable, value, expression);
			if (lastExpression != null && lastExpression.equals(expression)) {
				log.error("Loop on variable resolving detected : [" + expression + "] is cyclic in resolving, as [" + variable + "] resolves to [" + value +"]");
				return null;
			}
			lastExpression = expression;
		}
		return expression;
	}
	
	/*
	@Override
	public String expandValue(String walkScopeId, Artifact artifact, String line) {
		if (line == null)
			return null;
		
		String expression = line;
		String lastExpression = null;
		while (requiresEvaluation( expression)) {			
			String variable = extract( expression);
			String [] tokens = split( variable);
			String value = null;
			if (tokens[0].equalsIgnoreCase( "settings")) {
				value = settingsExpert.resolveValue( tokens[1]);
			}
			else if (tokens[0].equalsIgnoreCase( "project") || tokens[0].equalsIgnoreCase("pom")) {
				value = resolveProperty( artifact, variable);
				if (value == null) {
					value = resolveValue( artifact, tokens[1]);
				}
				if (value == null) {
					try {
						value = lookupPropertyByReflectionViaParentStructure( walkScopeId, artifact, tokens[1]);
					} catch (PomReaderException e) {
						throw new IllegalStateException(e);
					}
				}
			}
			else if (tokens[0].equalsIgnoreCase( "parent")) {
				// make sure the parent is loaded 
				if (artifact.getParent() != null && artifact.getResolvedParent() == null) {
					artifact.setResolvedParent( readParent(walkScopeId, artifact));
				}
				value = resolveValue( artifact, line);				
			}
			else if (tokens[0].equalsIgnoreCase( "env")) {
				// override here 
				value = getEnvironmentProperty(tokens[1]);
			}
			else {
				// system property 				
				value = getSystemProperty(variable);
			
				if (value == null) {				 			
					// might be a property? 
					value = resolveProperty(artifact, variable);
					if (value == null) {
						try {
							value = lookupPropertyViaParentStructure(walkScopeId, artifact, variable);
						} catch (PomReaderException e) {
							log.error( "cannot resolve variable [" + variable + "]", e);
						}
					}
					// finally, the pom parent
					if (value == null) {
						value = resolvePropertyViaParentStructure(walkScopeId, artifact, variable);						
					}
					if (value == null) {
						value = settingsExpert.resolveProperty( variable);
					}
					if (value == null) {
						value = osPropertyResolver.expand(variable);
					}
				
					if (value == null) {
						// might be an improperly named project value, i.e. "project." is missing 
						value = resolveValue(artifact, variable);						
					}												
				}				
			}	
			
			if (value == null) {
				if (log.isDebugEnabled()) {
					log.debug("cannot resolve variable [" + variable + "] in expression [" + expression + "] found within [" + NameParser.buildName(artifact) + "]");
				}
				return null;
			}
			
			expression = replace(variable, value, expression);
			if (lastExpression != null && lastExpression.equals(expression)) {
				log.error("Loop on variable resolving detected : [" + expression + "] is cyclic in resolving, as [" + variable + "] resolves to [" + value +"]");
				return null;
			}
			lastExpression = expression;
		}
		return expression;
	}
	*/
	
		
	private String resolvePropertyViaParentStructure( String walkScopeId, Artifact artifact, String property) {
		String value;
		try {
			Solution parent = readParent( walkScopeId, artifact);		
			if (parent != null) {
				value = resolveProperty( parent, property);
				if (value != null) {
					return value;
				}
				else {
					return resolvePropertyViaParentStructure(walkScopeId, parent, property);
				}
			}
			
		} catch (PomReaderException e1) {
			String msg = "cannot resolve property [" + property + "] from parent";
			log.error( msg, e1);
			e1.printStackTrace();
		}
		return null;
	}
	

	@Override
	public String resolveValue(Artifact artifact, String expression) {
		return resolve( artifact, expression);
	}
	
	 
		
	@Override
	public String resolveProperty(Artifact artifact, String property) {	
		Set<Property> properties = artifact.getProperties();
		for (Property suspect : properties) {
			if (suspect.getName().equalsIgnoreCase(property)) {
				String value = suspect.getValue();
				if (value != null)
					return value;
				value = suspect.getRawValue();				
				return value; // no value
			}
		}		
		return null;
	}

	/**
	 * resolve and read the artifact parameterized by the strings passed	
	 */
	@SuppressWarnings("unused")
	private Solution resolveAndRead( String walkScopeId, String groupId, String artifactId, String version) throws PomReaderException {
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( groupId);
		artifact.setArtifactId( artifactId);
		try {
			artifact.setVersion (VersionProcessor.createFromString( version));
		} catch (VersionProcessingException e) {
			String msg = String.format( "cannot create version from [%s]", version);
			throw new PomReaderException(msg, e);
		} 
		return resolveAndRead( walkScopeId, artifact);
	}
	
	/**
	 * use the dependency resolver to resolve the pom representing the artifact and read it 	
	 */
	private Solution resolveAndRead( String walkScopeId, Artifact artifact) throws PomReaderException {
		Part pomPart = null;
		PartTuple pomPartTuple = PartTupleProcessor.createPomPartTuple();
		// find if a pom part's already attached to the artifact
		for (Part part : artifact.getParts()) {
			if (PartTupleProcessor.equals( pomPartTuple, part.getType())) {
				pomPart = part;
				break;
			}			
		}
		// no pom part attached? resolve it 
		if (pomPart == null || pomPart.getLocation() == null) {		
			pomPart = ArtifactProcessor.createPartFromIdentification( artifact, artifact.getVersion(), pomPartTuple);
			if (dependencyResolver == null) {
				String warn = String.format( "no dependency resolver configured, cannot proceed reading the parent [%s]", NameParser.buildName( pomPart, pomPart.getVersion()));
				log.warn(warn);
				return null;
			} 
			try {
				pomPart = dependencyResolver.resolvePomPart(walkScopeId, pomPart);
			} catch (ResolvingException e) {				
				String msg = String.format( "cannot resolve [%s]", NameParser.buildName(pomPart));				
				throw new PomReaderException(msg, e);				
			}
		}
		// still no pom part? honk		
		if (pomPart == null || pomPart.getLocation() == null){
			if (log.isDebugEnabled()) {
				log.debug( "no location found for pom part of [" + NameParser.buildName(artifact) + "]");
			}
			return null;
		}
				
		Set<Part> parts = artifact.getParts();		
		if (PartProcessor.contains(parts, pomPart) == false) {
			parts.add(pomPart);
		}
		
		String location = pomPart.getLocation();
		Solution result = null;
		if (cache != null) {
			result = cache.getSolution(walkScopeId, location);
			if (result != null)
				return result;
		}			
		
		result = readPom( walkScopeId, new File( location));
		//
		if (result == null)
			return result;
		result.getParts().add(pomPart);
		if (cache != null) {
			cache.addSolution(walkScopeId, result, location);
		}
		return result;
		
	}
		
	/**
	 * looks for a version with the dependency management section of parents for instance 	
	 */
	private String lookupVersionViaDependencyManagement( String walkScopeId, Artifact artifact, String groupId, String artifactId) throws PomReaderException{
		for (Dependency dependency : artifact.getManagedDependencies()) {					 
			String scope = dependency.getScope();					
			if (scope != null && scope.equalsIgnoreCase( "import")) {
				// load importing artifact 
				String inj_groupId = expandValue( walkScopeId, artifact, dependency.getGroupId());
				String inj_artifactId = expandValue( walkScopeId, artifact, dependency.getArtifactId());				
				
				String inj_version = VersionRangeProcessor.toString( dependency.getVersionRange());
				if (inj_version == null)
					inj_version = "${project.version}";
				inj_version = expandValue( walkScopeId, artifact, inj_version);
				
				try {
					Dependency importedDependency = Dependency.T.create();
					importedDependency.setGroupId(inj_groupId);
					importedDependency.setArtifactId(inj_artifactId);
					importedDependency.setVersionRange( VersionRangeProcessor.createFromString(inj_version));
					Solution importedSolution = resolveDependencyAdHoc(walkScopeId, importedDependency);
					if (importedSolution == null) {								
						String msg = "cannot resolve importing dependency within [" + NameParser.buildName(artifact) + "] from [" + inj_groupId + ":" + inj_artifactId + "#" + inj_version + "]";
						log.error( msg);
						acknowledgeImportAssociationError(walkScopeId, artifact,  inj_groupId, inj_artifactId, inj_version);
						if (!referenceLenient) {
							throw new PomReaderException(msg);
						}
						return null;
					}
					// read?
					importedSolution = read(walkScopeId, importedSolution);
					artifact.getImported().add(importedSolution);
					log.debug("lookup version : importing dependencies within [" + NameParser.buildName(artifact) + "] from [" + inj_groupId + ":" + inj_artifactId + "#" + inj_version);
					String version = lookupVersionViaDependencyManagement(walkScopeId, importedSolution, groupId, artifactId);
					if (version != null) {
						// need to create an interim artifact to be able to signal the resolving via import
						Artifact msgArtifact = Artifact.T.create();
						ArtifactProcessor.transferIdentification(msgArtifact, artifact);
						msgArtifact.setVersion( VersionProcessor.createFromString(version));
						acknowledgeImportAssociation( walkScopeId, msgArtifact, importedSolution);
						return version;					
					}
				} catch (Exception e) {
					String msg= String.format("cannot read import-scoped artifact [%s:%s#%s] in parent pom [%s:%s#%s]", inj_groupId, inj_artifactId, inj_version, artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
					log.error( msg, e);
					throw new PomReaderException(msg, e);
				}
			} 
			else {
				if (groupId.equalsIgnoreCase( dependency.getGroupId()) && artifactId.equalsIgnoreCase( dependency.getArtifactId())) {
					String versionAsString = dependency.getVersionRange().getOriginalVersionRange();
					if (versionAsString.contains( "$")) { 
						versionAsString = expandValue( walkScopeId, artifact, versionAsString);
					}
					return versionAsString;
				}
			}
		}
				
		// load the parent lazily 		
		Artifact parent = readParent(walkScopeId, artifact);
		if (parent != null) {
			String returnValue = null;
			Stack<Solution> stack = null;
			if (detectParentLoops) {
				stack = cache.getLookupStackForScope(walkScopeId);				
			}
			if (stack != null) {
				checkStackForParent(stack, (Solution) parent);
				stack.push((Solution) parent);
			}
			try {
				returnValue = lookupVersionViaDependencyManagement(walkScopeId, parent, groupId, artifactId);
			}
			finally {
				if (stack != null) {
					stack.pop();
				}
			}
			return returnValue;
		}
		return null;	
	}
		
	private Dependency lookupDependencyViaDependencyManagement( String walkScopeId, Artifact artifact, String groupId, String artifactId, String classifier) throws PomReaderException{
	
		for (Dependency dependency : artifact.getManagedDependencies()) {					 
			String scope = dependency.getScope();					
			String managedDependencyGroupId = dependency.getGroupId();
			String managedDependencyArtifactId = dependency.getArtifactId();
			String managedDependencyVersion = VersionRangeProcessor.toString( dependency.getVersionRange());
			
			dependency.setGroupId( expandValue( walkScopeId, artifact, managedDependencyGroupId));
			dependency.setArtifactId( expandValue( walkScopeId, artifact, managedDependencyArtifactId));
			
			
		if (dependency.getGroupId() == null || dependency.getArtifactId() == null) {
				String msg= "cannot resolve managed dependency [" + managedDependencyGroupId + ":" + managedDependencyArtifactId + "#" + managedDependencyVersion + "]'s data in artifact [" + NameParser.buildName(artifact) + "]" ;
				log.error( msg);
				acknowledgeReadErrorOnArtifact( walkScopeId, artifact, msg);
				continue;
			}
			
			
			
			if (scope != null && scope.equalsIgnoreCase( "import")) {
				// load importing artifact 
				String inj_groupId = expandValue( walkScopeId, artifact, dependency.getGroupId());
				String inj_artifactId = expandValue( walkScopeId, artifact, dependency.getArtifactId());
				String inj_version = VersionRangeProcessor.toString( dependency.getVersionRange());
				inj_version = expandValue( walkScopeId, artifact, inj_version);
				
				try {
					// TODO : investigate whether a cache here would be better, e.g. whether the attachment works
					Dependency importedDependency = Dependency.T.create();
					importedDependency.setGroupId(inj_groupId);
					importedDependency.setArtifactId(inj_artifactId);
					importedDependency.setVersionRange( VersionRangeProcessor.createFromString(inj_version));
					Solution importedSolution = resolveDependencyAdHoc(walkScopeId, importedDependency);
					if (importedSolution == null) {								
						String msg = "cannot resolve importing dependencies within [" + NameParser.buildName(artifact) + "] from [" + inj_groupId + ":" + inj_artifactId + "#" + inj_version + "]";
						log.error( msg);
						acknowledgeImportAssociationError(walkScopeId, artifact,  inj_groupId, inj_artifactId, inj_version);
						return null;
					}
					// read?
					importedSolution = read(walkScopeId, importedSolution);
					artifact.getImported().add(importedSolution);
					acknowledgeImportAssociation( walkScopeId, artifact, importedSolution);
					log.debug("lookup dependency within [" + NameParser.buildName(artifact) + "] : importing dependencies from [" + inj_groupId + ":" + inj_artifactId + "#" + inj_version);
					Dependency lookedUp = lookupDependencyViaDependencyManagement( walkScopeId, importedSolution, groupId, artifactId, classifier);
					if (lookedUp != null) {
						return lookedUp;
					}
				} catch (Exception e) {
					String msg= String.format("cannot read import-scoped artifact [%s:%s#%s] in parent pom [%s:%s#%s]", inj_groupId, inj_artifactId, inj_version, artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
					log.error( msg, e);
					throw new PomReaderException(msg, e);
				} 
			} 
			else {
				// expand values .. 
				
				//
			
				
				if (matches(dependency, groupId, artifactId, classifier)) {
					try {						
						String versionRange = VersionRangeProcessor.toString(dependency.getVersionRange());
						if (requiresEvaluation(versionRange)) {
							String expandedVersionRange = expandValue( walkScopeId, artifact, versionRange);
							if (expandedVersionRange != null) {
								dependency.setVersionRange( VersionRangeProcessor.createFromString(expandedVersionRange));							
							}
							else {
								dependency.setVersionRange( VersionRangeProcessor.createFromString(versionRange));							
							}
						}
						else {
							dependency.setVersionRange( VersionRangeProcessor.createFromString(versionRange));
						}
					} catch (VersionProcessingException e) {
						e.printStackTrace();
					}
					return dependency;				
				}
			}
		}
				
		// load the parent lazily 		
		Artifact parent = readParent( walkScopeId, artifact);
		if (parent != null) {
			
			return lookupDependencyViaDependencyManagement( walkScopeId, parent, groupId, artifactId, classifier);
		}
		return null;	
	}

	private boolean matches( Dependency dependency, String groupId, String artifactId, String classifier) {
		if (
				!dependency.getGroupId().equalsIgnoreCase( groupId) || 
				!dependency.getArtifactId().equalsIgnoreCase( artifactId) 
			)		
			return false;
		
		if (classifier == null) {
			if (dependency.getClassifier() != null)
				return false;
		}
		else if (!classifier.equalsIgnoreCase( dependency.getClassifier())) {
			return false;		
		}
		return true;
	}
	
	
	/**
	 * look for a property via the parent structure 	 
	 */
	private String lookupViaParentStructure( String walkScopeId, Artifact artifact, String key) throws PomReaderException {	
		Solution parent = readParent(walkScopeId, artifact);
		if (parent != null) {
			String value = expandValue( walkScopeId, parent, key);
			if (value != null && !value.equalsIgnoreCase("null"))
				return value;
			return lookupViaParentStructure(walkScopeId, parent, key);
		}
		return null;
	}
	
	
	/**
	 * look for a property of the solution or in its parents 	
	 */
	private String lookupPropertyViaParentStructure( String walkScopeId, Artifact artifact, String propertyName) throws PomReaderException {
		String returnValue = null;
		Stack<Solution> stack = null;
		Solution parent = readParent(walkScopeId, artifact);

		if (parent != null) {
			if (detectParentLoops) {
				stack = cache.getLookupStackForScope(walkScopeId);
			}
			if (stack != null) {
				checkStackForParent(stack, parent);
				stack.push(parent);
			}
			try {
			for (Property property : parent.getProperties())  {
				if (property.getName().equalsIgnoreCase(propertyName)) {
					String value = property.getValue();
					if (value == null) {
						value = property.getRawValue();
					}
					if (value == null) {
						returnValue = lookupPropertyViaParentStructure(walkScopeId, parent, propertyName);
						break;
					}
					else if (requiresEvaluation( value)) {
						value = expandValue(walkScopeId, artifact, value);
						if (value == null) {
							returnValue = lookupPropertyViaParentStructure(walkScopeId, artifact, value);
							break;
						}
					}
					else {
						returnValue = value;
						break;
					}
					
				}
			}	
			}
			finally {
				if (stack != null) {
					stack.pop();
				}
			}
		}
		
		return returnValue;
	}
	
	private String lookupPropertyByReflectionViaParentStructure( String walkScopeId, Artifact artifact, String propertyName) throws PomReaderException {	
		Solution parent = readParent(walkScopeId, artifact);
		if (parent != null) {
			try {
				com.braintribe.model.generic.reflection.Property property = parent.entityType().getProperty(propertyName);
				Object rawValue = property.get(parent);
				String value = null;
				if (rawValue instanceof Version) {					
					value = VersionProcessor.toString( (Version) rawValue);					
				}
				else if (rawValue instanceof VersionRange) {					
					value = VersionRangeProcessor.toString( (VersionRange) rawValue);					
				}
				else {
					value = rawValue.toString();
				}
						
				if (value == null) {
					return lookupPropertyViaParentStructure(walkScopeId, parent, propertyName);					
				}
				else if (requiresEvaluation( value)) {
					value = expandValue(walkScopeId, artifact, value);
					if (value == null) {
						return lookupPropertyByReflectionViaParentStructure(walkScopeId, artifact, value);
					}
				}				
				else {
					return value;
				}
			} catch (GenericModelException e) {
				return lookupPropertyByReflectionViaParentStructure(walkScopeId, parent, propertyName);
			}			
		}
		return null;
	}

	@Override
	public void acknowledgeParentAssociation(String walkScopeId, Artifact child, Solution parent) {
		if (listeners != null) {
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeParentAssociation( walkScopeId, child, parent);
			}
		}			
		
	}
	
	@Override
	public void acknowledgeParentAssociationError(String walkScopeId, Artifact child, String groupId, String artifactId, String version) {
		if (listeners != null) {
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeParentAssociationError(walkScopeId, child, groupId, artifactId, version);
			}
		}			
		
	}

	@Override
	public void acknowledgeSolutionAssociation(String walkScopeId, String location, Artifact solution) {
		if (listeners != null) {
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeSolutionAssociation( walkScopeId, location, solution);
			}
		}			
	}

	@Override
	public void acknowledgeReadErrorOnFile(String walkScopeId, String name, String reason) {
		if (listeners != null) {
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeReadErrorOnFile( walkScopeId, name, reason);
			}
		}		
	}
	

	@Override
	public void acknowledgeReadErrorOnArtifact(String walkScopeId, Artifact artifact, String reason) {
		if (listeners != null) {
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeReadErrorOnArtifact( walkScopeId, artifact, reason);
			}
		}
		
	}

	@Override
	public void acknowledgeReadErrorOnString(String walkScopeId, String contents, String reason) {
		if (listeners != null) {
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeReadErrorOnString( walkScopeId, contents, reason);
			}
		}
		
	}

	@Override
	public void acknowledgeVariableResolvingError(String walkScopeId, Artifact artifact, String expression) {
		if (listeners != null) {
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeVariableResolvingError( walkScopeId, artifact, expression);
			}
		}			
	}

	@Override
	public void acknowledgeImportAssociation(String walkScopeId, Artifact requestingSolution, Solution requestedSolution) {
		if (listeners != null) {
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeImportAssociation(walkScopeId, requestingSolution, requestedSolution);
			}
		}			
	}
	


	@Override
	public void acknowledgeImportAssociationError(String walkScopeId, Artifact requestingSolution, String groupId, String artifactId, String version) {
		if (listeners != null) {
			for (PomReaderNotificationListener listener : listeners) {
				listener.acknowledgeImportAssociationError(walkScopeId, requestingSolution, groupId, artifactId, version);
			}
		}			
	}
	
	
	
		
}
