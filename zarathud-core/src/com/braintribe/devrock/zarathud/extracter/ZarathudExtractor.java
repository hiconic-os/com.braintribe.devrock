// ============================================================================
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
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
package com.braintribe.devrock.zarathud.extracter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.zarathud.ZarathudException;
import com.braintribe.devrock.zarathud.commons.IdentificationWrapperCodec;
import com.braintribe.devrock.zarathud.extracter.registry.BasicExtractionRegistry;
import com.braintribe.devrock.zarathud.extracter.registry.DependencyExtracter;
import com.braintribe.devrock.zarathud.extracter.scanner.BasicResourceScanner;
import com.braintribe.devrock.zarathud.extracter.scanner.ScannerResult;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartType;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.processing.service.api.UnsupportedRequestTypeException;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.zarathud.data.AbstractClassEntity;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.Artifact;
import com.braintribe.model.zarathud.data.ClassEntity;
import com.braintribe.model.zarathud.data.EnumEntity;
import com.braintribe.model.zarathud.data.InterfaceEntity;

/**
 * the {@link ZarathudExtractor}: analyzes the jar reflecting the passed {@link BindingArtifact}<br/>
 * <br/>
 * <b>returns</b> an {@link Artifact} that contains information about:<br/>
 * all enum as {@link EnumEntity}<br/>
 * all classes as {@link ClassEntity}<br/>
 * all interfaces as {@link InterfaceEntity}<br/>  
 * <br/>
 * <b>requires</b>:<br/>
 * an instance of the {@link BasicResourceScanner}<br/>
 * an instance of the {@link BasicExtractionRegistry}<br/>
 * a {@link Collection} of {@link Solution} including the one that is reflecting the {@link BindingArtifact}<br/>  
 * 
 * @author pit
 *
 */
public class ZarathudExtractor {
	private static Logger log = Logger.getLogger(ZarathudExtractor.class);
	
	private Collection<Solution> classpathSolutions;
	private Collection<Dependency> declaredDependencies;
	
	private ResourceScanner resourceScanner = new BasicResourceScanner();
	private ExtractionRegistry registry = new BasicExtractionRegistry();
	private String classesDirectory;
	
	private Map<URL, Artifact> urlToBindingArtifactMap = new HashMap<>();
	private BasicPersistenceGmSession session;
	private DependencyExtracter dependencyExtracter = new DependencyExtracter();
	
	@Required @Configurable
	public void setClasspath(Collection<Solution> solutions) {
		this.classpathSolutions = solutions;
	}
	
	@Required @Configurable
	public void setDeclared(Collection<Dependency> solutions) {
		this.declaredDependencies = solutions;
	}
	
	@Configurable
	public void setResourceScanner(ResourceScanner resourceScanner) {
		this.resourceScanner = resourceScanner;
	}
	@Configurable
	public void setRegistry(ExtractionRegistry registry) {
		this.registry = registry;
	}
	
	@Configurable
	public void setSession(BasicPersistenceGmSession session) {
		this.session = session;
	}
	
	@Configurable
	public void setClassesDirectory(String classesDirectory) {
		this.classesDirectory = classesDirectory;
	}
	/**
	 * analyze a jar - i.e. extract all relevant information of the jar 
	 * @param terminalArtifact - the {@link BindingArtifact} that this jar reflects 
	 * @return - the {@link Artifact} with all relevant information attached 
	 * @throws ZarathudException - thrown if anything goes wrong
	 */
	public Artifact analyseJar(Artifact terminalArtifact) throws ZarathudException{
	
		// setup registry
		registry.setSolutionList(classpathSolutions);
		if (session != null) {
			registry.setSession( session);
		}
		registry.initialize();		
		
		
		// find the solution that is the jar of the terminal
		Solution solutionToScan = null;
		File jarToScan = null;
		
		// if a directory for classes is been passed, add it to the urls 
		if (classesDirectory != null) {
			File classesDirectoryFile = new File( classesDirectory);
			try {
				URL buildDirectoryUrl = classesDirectoryFile.toURI().toURL();
				//jars.add( buildDirectoryUrl);
				urlToBindingArtifactMap.put( buildDirectoryUrl, terminalArtifact);
				
			} catch (MalformedURLException e) {
				String msg= String.format("cannot build URL from file [%s]", classesDirectoryFile);
				log.warn( msg, e);
			}			
		}
		
		for (Solution solution : classpathSolutions) {
			
			// note the main jar - if any 
			if (
					classesDirectory == null &&
					solution.getGroupId().equalsIgnoreCase( terminalArtifact.getGroupId()) &&
					solution.getArtifactId().equalsIgnoreCase( terminalArtifact.getArtifactId()) && 
					VersionProcessor.toString(solution.getVersion()).equalsIgnoreCase( terminalArtifact.getVersion())
				)  {
				solutionToScan = solution;
				jarToScan = extractJarFromSolution(solution);					
			}
			 
			File jar = extractJarFromSolution(solution);
			try {
				URL url = jar.toURI().toURL();			
				urlToBindingArtifactMap.put(url, toArtifact(solution));
			} catch (MalformedURLException e) {
				String msg="cannot build URL from file [" + jar.getAbsolutePath() + "]";
				log.warn( msg, e);
			}
		}
		ScannerResult result;
		
		if (classesDirectory == null) {
			// no classes directory -> the terminal must be part of the solution list 
			if ( solutionToScan == null) {
				String msg="cannot find corresponding jar for terminal [" + terminalArtifact.getGroupId() + ":" + terminalArtifact.getArtifactId() + "#" + terminalArtifact.getVersion();
				log.error( msg);
				throw new ZarathudException(msg);			
			}
			// scan the jar
			try {
				result = resourceScanner.scanJar( jarToScan);				
			} catch (IOException e) {
				String msg="cannot scan jar [" + jarToScan.getAbsolutePath() + "]";
				log.error( msg, e);
				
				throw new ZarathudException(msg, e);
			} 	
		}
		else {
			// scan the directory 
			try {
				File buildDirectoryFile = new File( classesDirectory);				
				result = resourceScanner.scanFolder( buildDirectoryFile);
			} catch (Exception e) {
				String msg= String.format("cannot scan folder [%s]", classesDirectory);
				log.error( msg, e);
				throw new ZarathudException(msg, e);
			}			
		}
		registry.addArtifactBinding(result, terminalArtifact);
		
		registry.setUrlToArtifactMap(urlToBindingArtifactMap);
				
		for (String className : result.getClasses()) {		
			AbstractEntity zarathudEntity = registry.acquireClassResource(className, false);
			zarathudEntity.setDefinedLocal(true);
		}
		
		// now scan all classes that are referenced, but externally - they remain to be scanned..
		Set<String> resourcesToScan = registry.getUnscannedEntities();
		Set<String> unresolvedResources = new HashSet<String>();
		while (resourcesToScan.size() > 0) {			
			for (String name : resourcesToScan) {
				// try to analyze the resource 
				AbstractEntity zarathudEntity;
				try {
					zarathudEntity = registry.analyzeClassResource(name);
				} catch (Exception e) {
					// if it can't be found, it's most PROBABLY a Java Runtime class  
					zarathudEntity = registry.acquireClassResource(name, true);					
					zarathudEntity.setArtifact( registry.acquireArtifact(name));					
					unresolvedResources.add( name);
				}				
				zarathudEntity.setDirectDependency(true);
			}			
			resourcesToScan = registry.getUnscannedEntities();
			// remove all we couldn't resolve in the first place
			resourcesToScan.removeAll(unresolvedResources);
		}
					 		
		// analyze generic nature 
		try {
			registry.analyzeGenericity();
		} catch (UnsupportedRequestTypeException e) {
			String msg="cannot analyze the genericEntities";
			log.error( msg, e);
			throw new ZarathudException(msg, e);
		}
		
		
		
		// create the container entity 
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( terminalArtifact.getGroupId());
		artifact.setArtifactId( terminalArtifact.getArtifactId());
		artifact.setVersion( terminalArtifact.getVersion());

		
		
		// add terminal artifact (the owner of all locals)
		//registry.addArtifactToRelevantEntities(artifact);
		
		Set<AbstractEntity> entries = new HashSet<AbstractEntity>(); 
		// get all entries
		entries.addAll(registry.getEntries());
	
		// get only locally defined entries from the registry.. 		
		//entries.addAll( registry.getRelevantEntries());
				
		// wire type hierarchy
		wireTypeHierarchy( entries);						

		artifact.setEntries(entries);
		// add declared dependencies
		List<Artifact> declaredArtifactDependencies = collectDeclaredDependencies( classpathSolutions, declaredDependencies);				
		artifact.setDeclaredDependencies( declaredArtifactDependencies);
		
		// add actual dependencies via traversion
		/*List<Artifact> actualDependencies = dependencyExtracter.collectDependencies(artifact);
		artifact.setActualDependencies(actualDependencies);
		*/
		return artifact;
	}
		
	
	/**
	 * extract the relevant jar from the solution 
	 * @param solution - the {@link Solution} to get the jar as file from
	 * @return - the file that points to the jar 
	 */
	private File extractJarFromSolution( Solution solution) {
		
		for (Part part : solution.getParts()) {
		
			PartType partType = PartTupleProcessor.toPartType( part.getType());
			if (partType == PartType.JAR)
				return new File( part.getLocation());
					
		}
		return null;
	}

	
	/**
	 * wire the type hierarchy : add sub type declarations to all {@link ClassEntity} and {@link InterfaceEntity},
	 * in case of {@link ClassEntity} also link the interfaces the class implements 
	 * @param entries - a {@link Set} of {@link AbstractEntity} to wire
	 */
	private void wireTypeHierarchy( Collection<AbstractEntity> entries) {
		for (AbstractEntity abstractEntity : entries) {
			// class entities
			if (abstractEntity instanceof ClassEntity) {
				ClassEntity classEntity = (ClassEntity) abstractEntity;
				// only one super type 
				ClassEntity superType = classEntity.getSuperType();
				if (superType != null) {
					Set<ClassEntity> subTypes = superType.getSubTypes();					
					log.debug( "adding [" + classEntity.getName() + "] as sub type to [" + superType.getName() + "]");
					subTypes.add( classEntity);
				}
				// implementing interfaces 
				Set<InterfaceEntity> implementedInterfaces = classEntity.getImplementedInterfaces();
				if (implementedInterfaces != null && implementedInterfaces.size() > 0) {
					for (InterfaceEntity implementedInterface : implementedInterfaces) {
						Set<ClassEntity> implementingClasses = implementedInterface.getImplementingClasses();
						log.debug( "adding [" + classEntity.getName() + "] as implementing to [" + implementedInterface.getName() + "]");
						implementingClasses.add( classEntity);
					}
				}		
				continue;
			}
			// interface 
			if (abstractEntity instanceof InterfaceEntity) {
				InterfaceEntity interfaceEntity = (InterfaceEntity) abstractEntity;
				Set<InterfaceEntity> superInterfaces = interfaceEntity.getSuperInterfaces();
				if (superInterfaces != null && superInterfaces.size() > 0) {
					for (InterfaceEntity superInterface : superInterfaces) {
						Set<InterfaceEntity> subInterfaces = superInterface.getSubInterfaces();						
						log.debug( "adding [" + interfaceEntity.getName() + "] as sub interface to [" + superInterface.getName() + "]");
						subInterfaces.add( interfaceEntity);
					}
				}
				continue;
			}
		}
	}
		
	
	
	
	/**
	 * convert a solution to an artifact
	 * @param solution - the {@link Solution}
	 * @return - a matching {@link Artifact}
	 */
	private Artifact toArtifact( Solution solution) {
		Artifact artifact;
		if (session != null) {
			artifact = session.create( Artifact.T);
		}
		else {
			artifact = Artifact.T.create();
		}		
		artifact.setGroupId( solution.getGroupId());
		artifact.setArtifactId( solution.getArtifactId());
		artifact.setVersion( VersionProcessor.toString( solution.getVersion()));		
		return artifact;
	}

	/**
	 * use the classpath to resolve the direct dependencies of the terminal 
	 * @param classpathSolutions - the solutions that make up the classpath
	 * @param declaredDependencies - the dependencies as declared in the pom
	 * @return - a {@link List} of {@link Artifact} that represent the resolved dependencies
	 */
	private List<Artifact> collectDeclaredDependencies(Collection<Solution> classpathSolutions, Collection<Dependency> declaredDependencies) {
		Map<Identification, Artifact> map = CodingMap.createHashMapBased( new IdentificationWrapperCodec());
		
		classpathSolutions.stream().forEach( s -> {
			map.put( s, toArtifact(s));
		});
		
		return declaredDependencies.stream().map( d -> {
			return map.get(d);		
		}).collect(Collectors.toList());
		
	}
	
}
