// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
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
package com.braintribe.devrock.zarathud.extracter;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.braintribe.devrock.zarathud.ZarathudException;
import com.braintribe.devrock.zarathud.extracter.scanner.ScannerResult;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.processing.service.api.UnsupportedRequestTypeException;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.zarathud.data.AbstractClassEntity;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.AnnotationEntity;
import com.braintribe.model.zarathud.data.Artifact;
import com.braintribe.model.zarathud.data.EnumEntity;

public interface ExtractionRegistry {

	/**
	 * set the classloader to be used during extraction 
	 * @param classLoader - an {@link URLClassLoader}
	 */
	//void setClassLoader(URLClassLoader classLoader);
		
	void setUrlToArtifactMap( Map<URL, Artifact> urlToArtifactMap);

	/**
	 * setup: initialize 
	 */
	void initialize();

	/**
	 * set the solutions that make up the classpath. The solutions must contain a JAR-part with valid location 
	 * @param solutions - a {@link Collection} of {@link Solution} making up the classpath
	 */
	void setSolutionList(Collection<Solution> solutions);
	
	/**
	 * finds the entry with this name  
	 * @param name - the name of the entry 
	 * @return
	 */
	AbstractEntity getEntry(String name);

	/**
	 * add an entry to the registry
	 * @param entry - an {@link AbstractEntity}
	 */
	void addEntry(AbstractEntity entry);

	/**
	 * dump the stored entries 
	 * @return - a {@link Collection} of {@link AbstractEntity}
	 */
	Collection<AbstractEntity> getEntries();

	/**
	 * return all entities that are relevant for the extraction, i,e 
	 * <li>locally defined entities</li>
	 * <li>direct dependents</li>
	 * @return - a {@link Collection} of {@link AbstractEntity}
	 */
	Collection<AbstractEntity> getRelevantEntries();

	/**
	 * analyze an annotation  
	 * @param annotationName - the name of the annotation 
	 * @return - the created {@link AnnotationEntity}
	 * @throws ZarathudException - thrown if anything goes wrong
	 */
	AnnotationEntity analyzeAnnotation(String annotationName) throws ZarathudException;

	/**
	 * acquire a {@link AbstractEntity} from the name 
	 * @param name - the name of the resource
	 * @param lenient - true if lenient, false if strict
	 * @return - the {@link AbstractEntity} 
	 * @throws ZarathudException - if anything goes wrong 
	 */
	AbstractEntity acquireClassResource(String name, boolean lenient) throws ZarathudException;

	/**
	 * analyze a class/interface as specified by the name of the resource  
	 * @param className - the name of the resource as a {@link String}
	 * @throws ZarathudException - if anything goes wrong
	 */
	AbstractEntity analyzeClassResource(String className) throws ZarathudException;

	/**
	 * checks if the entity with the given name is an enum
	 * @param fullName - the name to check 
	 * @return - true if the {@link AbstractEntity} is an {@link EnumEntity}, false otherwise
	 */
	boolean isEnum(String fullName);

	/**
	 * checks if the entity is generic 
	 * @param fullName - the name to check 
	 * @return - true if the entity is an {@link AbstractClassEntity} and isGeneric. 
	 */
	boolean isGeneric(String fullName);

	/**
	 * analyzes whether the entity with the given name is generic 
	 * if it hasn't been analyzed yet, it analyzes its parents (while setting their 
	 * genericity values recursively)
	 * @param fullName - the name of the resource 
	 * @return - true if it is generic, false otherwise 
	 */
	boolean analyzeGenericity(String fullName) throws UnsupportedRequestTypeException;

	/**
	 * iterate over the map of stored {@link AbstractEntity} and analyze their genericity 
	 */
	void analyzeGenericity() throws UnsupportedRequestTypeException;

	/**
	 * get all enums stored 
	 * @return - {@link Collection} of {@link EnumEntity}
	 */
	Collection<EnumEntity> getEnums();

	/**
	 * get all generic entities 
	 * @return - {@link Collection} of {@link AbstractClassEntity} that are generic
	 */
	Collection<AbstractClassEntity> getGenericEntities();

	/**
	 * add artifact binding to classes from artifact 
	 * @param result - the {@link ScannerResult}
	 * @param artifact - the declaring {@link Artifact}
	 * @throws ZarathudException - arrrgh
	 */
	void addArtifactBinding(ScannerResult result, Artifact artifact) throws ZarathudException;

	/**
	 * add artifact binding to classes from jar 
	 * @param scannerResult - the {@link ScannerResult}
	 * @param jar - the {@link File} that represents the jar 
	 * @throws ZarathudException - arrgh 
	 */
	void addArtifactBinding(ScannerResult scannerResult, File jar) throws ZarathudException;

	/**
	 * adds the parent artifact to all {@link AbstractEntity} that are locally defined 
	 * @param artifact - the {@link Artifact} to set
	 */
	void addArtifactToRelevantEntities(Artifact artifact);

	/**
	 * get a stored {@link AbstractEntity} from the storage 
	 * @param fullName - the name of the entity 
	 * @return - the {@link AbstractEntity} stored under that name 
	 * @throws ZarathudException - if nothing's found with that name.
	 */
	AbstractEntity getRegistryEntry(String fullName) throws ZarathudException;

	/**
	 * return the module name of the {@link AbstractEntity}
	 * @param fullName - the name of the {@link AbstractEntity}
	 * @return - the GWT module name, or null if none's set 
	 * @throws ZarathudException - if there's no such thing stored
	 */
	String getModuleName(String fullName) throws ZarathudException;

	/**
	 * return the binding artifact for a resource 
	 * @param fullName - the name as a {@link String}
	 * @return - the {@link BindingArtifact}
	 * @throws ZarathudException - if not found
	 */
	Artifact getBindingArtifact(String fullName) throws ZarathudException;

	/**
	 * @return
	 */
	Set<String> getUnscannedEntities();
	
	/**
	 * optionally set the {@link BasicPersistenceGmSession} to create instances into
	 * @param session - the {@link BasicPersistenceGmSession} 
	 */
	void setSession( BasicPersistenceGmSession session);
	
	Artifact acquireArtifact(String signature);
	

}