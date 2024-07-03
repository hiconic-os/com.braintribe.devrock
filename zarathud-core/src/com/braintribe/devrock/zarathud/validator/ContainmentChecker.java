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
package com.braintribe.devrock.zarathud.validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.zarathud.ZarathudException;
import com.braintribe.devrock.zarathud.commons.SimpleTypeRegistry;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.zarathud.data.AbstractClassEntity;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.Artifact;
import com.braintribe.model.zarathud.data.ClassEntity;
import com.braintribe.model.zarathud.data.InterfaceEntity;
import com.braintribe.model.zarathud.data.MethodEntity;

/**
 * helper to check whether a model's suited for "representation by containment"
 * Basically, a model can be represented in XML when it can be expressed in pure containment. That means, the no entity may have a shared property (i.e. entity A has a property of entity C and so does entity B) as this MAY mean the very same instance is referenced several times. Furthermore, no cycle may exist (i.e entity A references entity B which references entity A). 
 * Failing such a test is not an error, but it can be used as a test to find whether the model is suited to be exposed to XML, as in case of a web services. 
 * What complicates the issue is that the type hierarchy is involved, so references to a common base type is to be treated as if a concrete type is referenced.
 *  
 * @author pit
 *
 */
public class ContainmentChecker {

	private static Logger log = Logger.getLogger(ContainmentChecker.class);
	private BasicPersistenceGmSession session;
	private ValidationContextLogger contextLogger;
	private Artifact candidateArtifact;
	
	private boolean shortCircuit = true;

		
	@Required
	public void setSession(BasicPersistenceGmSession session) {
		this.session = session;
	}
	@Required
	public void setCandidateArtifact(Artifact candidateArtifact) {
		this.candidateArtifact = candidateArtifact;
	}
	
	@Required
	public void setContextLogger(ValidationContextLogger contextLogger) {
		this.contextLogger = contextLogger;
	}
	@Configurable
	public void setShortCircuit(boolean shortCircuit) {
		this.shortCircuit = shortCircuit;
	}
	
	/**
	 * get all referenced types of an {@link AbstractClassEntity} (Generic class or interface)
	 * @param entity - {@link AbstractClassEntity}
	 * @return - {@link Set} of {@link AbstractClassEntity}
	 * @throws ZarathudException - arrgh
	 */
	private Set<AbstractClassEntity> getReferencedTypes( AbstractClassEntity entity) throws ZarathudException {
		Set<AbstractClassEntity> result = new HashSet<AbstractClassEntity>();
		// get the access tuples
		Set<MethodEntity> methods = entity.getMethods();
		if (methods == null || methods.size() == 0)
			return result;
		Set<AccessTuple> tuples = AccessTupleBuilder.tuplizeMethods(methods);	
		result.addAll( getReferencedTypes(tuples));
		
		return result;
	}
	
	/**
	 * extract all referenced types of {@link Set} of {@link AccessTuple}, by looking at the setter.. (only works for valid models anyhow)
	 * @param tuples - the {@link Set} of {@link AccessTuple} to parse 
	 * @return - a {@link Set} of {@link AbstractClassEntity} 
	 * @throws ZarathudException - arrgh
	 */
	private Set<AbstractClassEntity> getReferencedTypes( Set<AccessTuple> tuples) throws ZarathudException {
		Set<AbstractClassEntity> result = new HashSet<AbstractClassEntity>();
		for (AccessTuple tuple : tuples) {
			// extract the type of the getter/setter?
			MethodEntity methodEntity = tuple.getGetter();
			if (methodEntity == null)
				continue;
			AbstractEntity type = methodEntity.getReturnType();
			String typeName = type.getDesc();

			// void signature - screwed model anyhow 
			if (typeName.equalsIgnoreCase("V")) {
				continue;
			}
			// simple type 
			if (SimpleTypeRegistry.isCodeForSimpleType(typeName))
				continue;
			
			typeName = typeName.substring(1, typeName.length() -1);
			if (SimpleTypeRegistry.isSimpleType(typeName)) 
				continue;
			
			// collection 
			if (SimpleTypeRegistry.isCollectionType(typeName)){
				List<String> collectionTypes = SimpleTypeRegistry.getCollectionElementTypes( typeName, methodEntity.getSignature());
				for (String name : collectionTypes) {
					// simple type 
					if (SimpleTypeRegistry.isCodeForSimpleType( name))
						continue;
					// 
					if (SimpleTypeRegistry.isSimpleType( name))
						continue;

					// not a known simple type - may still be a non generic type (RT type for instance)
					AbstractClassEntity abstractClassEntity = getTypeFromString(name);
					if (abstractClassEntity == null) {
						log.warn("type [" + typeName + "] is not stored, deemed not to be a GenericEntity");
						continue;
					}
					if (Boolean.TRUE.equals( abstractClassEntity.getGenericNature())) {
						result.add( abstractClassEntity);
					}
				}
				continue;
			}
			// not a known simple type - may still be a non generic type (RT type for instance)
			AbstractClassEntity abstractClassEntity = getTypeFromString( typeName);
			if (abstractClassEntity == null) {
				log.warn("type [" + typeName + "] is not stored, deemed not to be a GenericEntity");
				continue;
			}
			if (Boolean.TRUE.equals( abstractClassEntity.getGenericNature())) {
				result.add( abstractClassEntity);
			}
		}		
		return result;
	}
	
	/**
	 * query the session for a type with the given name AND belonging to the candidate 
	 * @param name - the name as found in the signatures 
	 * @return - the {@link AbstractClassEntity} or null 
	 * @throws ZarathudException - arrgh
	 */
	private AbstractClassEntity getTypeFromString( String name) throws ZarathudException{
			
		// run a query
		EntityQuery baseArtifactQuery = EntityQueryBuilder.from( AbstractClassEntity.class).where()
				.conjunction()
					.property( "artifact").eq().entity(candidateArtifact)
					.property( "name").eq( name.replace("/", "."))					
				.close()
			.done();
		
		try {	
			AbstractClassEntity result = session.query().entities(baseArtifactQuery).unique();
			return result;
		} catch (GmSessionException e) {
			String msg="cannot query for the type with name [" + name + "]";
			log.error( msg, e);
			throw new ZarathudException(msg, e);
		}					
	}
	
	/**
	 * build a map of entities to their properties 
	 * @param entries - the {@link Set} or {@link AbstractEntity} to scan
	 * @return - a {@link Map} of {@link AbstractClassEntity} to {@link Set} of {@link AbstractClassEntity} : the properties of each entity 
	 * @throws ZarathudException - arrgh 
	 */
	private Map<AbstractClassEntity, Set<AbstractClassEntity>> buildMap(Set<AbstractEntity> entries) throws ZarathudException {
		Map<AbstractClassEntity, Set<AbstractClassEntity>> typeToPropertyMap = new HashMap<AbstractClassEntity, Set<AbstractClassEntity>>();
		for (AbstractEntity abstractEntity : entries) {
			Set<AbstractClassEntity> propertyTypes = null; 
			if (abstractEntity instanceof AbstractClassEntity) {
				AbstractClassEntity abstractClassEntity = (AbstractClassEntity) abstractEntity;
				// if it's not a generic entity, we don't care anyhow 
				if (Boolean.TRUE.equals( abstractClassEntity.getGenericNature()) == false)
					continue;
				propertyTypes = getReferencedTypes( abstractClassEntity);
				typeToPropertyMap.put( abstractClassEntity, propertyTypes);
			}
		}
		return typeToPropertyMap;
	}
	
	/**
	 * check the class hierarchy - if suspect's a super type of the entity 
	 * @param suspect - {@link AbstractClassEntity} to test for
	 * @param start - the {@link ClassEntity} that is the starting point for the test
	 * @return - true if the suspect matches a super type 
	 */
	private boolean isInHierarchy( AbstractClassEntity suspect, ClassEntity start) {
		ClassEntity superType = start.getSuperType();
		if (superType == null)
			return false;
		if (superType == suspect)
			return true;
		
		return isInHierarchy(suspect, superType);
	}
	/**
	 * checks the interface's hierarchy - if suspects in a super interface of the interface
	 * @param suspect - the interface to test for 
	 * @param start - the {@link InterfaceEntity} that is the start point 
	 * @return - true if the suspect matches itself or a super type 
	 */
	private boolean isInHierarchy( InterfaceEntity suspect, InterfaceEntity start) {
		Set<InterfaceEntity> superInterfaces = start.getSuperInterfaces();
		if (superInterfaces == null || superInterfaces.size() == 0)
			return false;
		for (InterfaceEntity superInterface : superInterfaces) {
			if (superInterface == suspect)
				return true;
			if (isInHierarchy(suspect, superInterface)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * tests if the suspect is an interface the class implements 
	 * @param suspect - the {@link InterfaceEntity} to scan for
	 * @param start - the {@link ClassEntity} that implements the interface 
	 * @return - true if the suspects in the list of interfaces (or one of the super interfaces)
	 */
	private boolean isInInterfaces( InterfaceEntity suspect, ClassEntity start) {
		InterfaceEntity interfaceEntity = suspect;
		Set<InterfaceEntity> implementedInterfaces = start.getImplementedInterfaces();
		if (implementedInterfaces == null || implementedInterfaces.size() == 0)
			return false;
		for (InterfaceEntity implementedInterface : implementedInterfaces) {
			if (implementedInterface == interfaceEntity)
				return true;
			if (isInHierarchy( suspect, implementedInterface))
				return true;
		}
		return false;
	}
	
	/**
	 * scans whether another type has a property of the same type or one property has a super type of the type 
	 * @param map - the {@link Map} of {@link AbstractClassEntity} to {@link Set} of {@link AbstractClassEntity} : the type to property map
	 * @param suspect - the {@link AbstractClassEntity} to scan for 
	 * @param owner - the owning {@link AbstractClassEntity} : to skip over it 
	 * @return - boolean if the suspect's (directly or via type hierarchy) referenced somewhere
	 */
	private boolean checkHit( Map<AbstractClassEntity, Set<AbstractClassEntity>> map, AbstractClassEntity suspect, AbstractClassEntity owner, boolean shortCircuit) {
		boolean result = false;
		for (Entry<AbstractClassEntity, Set<AbstractClassEntity>> entry : map.entrySet()) {
			AbstractClassEntity current = entry.getKey();
			if (current == owner)
				continue;
			Set<AbstractClassEntity> set = entry.getValue();
			for (AbstractClassEntity property : set) {
				if (property == suspect) {
					log.debug("[" + owner.getName() + "]'s property type [" + suspect.getName() + "] is also referenced by type [" + current.getName() + "]");
					contextLogger.logContainmentPropertyAlreadyReferenced( owner, suspect, current);
					result = true;
					if (shortCircuit) {
						return true;
					}
				}
				if (property instanceof ClassEntity) {
					// property is a class 
					ClassEntity classEntity = (ClassEntity) property;
					// suspect's a class -> check in class hierarchy 
					if (suspect instanceof ClassEntity) {
						if (isInHierarchy(suspect, classEntity)) {
							log.debug("[" + owner.getName() + "]'s property type [" + suspect.getName() + "] is a base type of a property referenced by type [" + current.getName() + "]");
							contextLogger.logContainmentPropertyAlreadyReferencedPerSuperType( owner, suspect, current);
							result = true;
							if (shortCircuit) {
								return true;
							}					
						}
					}
					// suspect's an interface -> check in interface hierarchy
					if (suspect instanceof InterfaceEntity) {
						// property is an interface 
						if (isInInterfaces((InterfaceEntity) suspect, classEntity)) {
							log.debug("[" + owner.getName() + "]'s property type [" + suspect.getName() + "] is an interface type of a property referenced by type [" + current.getName() + "]");
							contextLogger.logContainmentPropertyAlreadyReferencedPerInterface( owner, suspect, current);
							result = true;
							if (shortCircuit) {
								return true;
							}					
						}
					}
					
				}
				if (property instanceof InterfaceEntity) {				
					InterfaceEntity interfaceEntityProperty = (InterfaceEntity) property;
					// suspect's a class, property's an interface -> nothing to test: a class can't be the base of an interface   
					if (suspect instanceof ClassEntity) {
						/* 
						if (isInInterfaces( interfaceEntityProperty, (ClassEntity) suspect)) {
							log.debug("[" + owner.getName() + "]'s property type [" + suspect.getName() + "] is an implements interface of a property referenced by type [" + current.getName() + "]");
							return true;					
						}
						*/
					}
					// suspect's an interface -> check in interface hierarchy of the property : suspect may not be a base interface of the property 
					if (suspect instanceof InterfaceEntity) {
						if (isInHierarchy((InterfaceEntity) suspect, interfaceEntityProperty)) {
							log.debug("[" + owner.getName() + "]'s property type [" + suspect.getName() + "] is an super interface of a property referenced by type [" + current.getName() + "]");
							contextLogger.logContainmentPropertyAlreadyReferencedPerSuperInterface( owner, suspect, current);
							result = true;
							if (shortCircuit) {
								return true;
							}					
						}
					}
					
				}
			}
		}
		return result;
	}
	/**
	 * tests if a model can be represented in a containment only representation<br/>
	 * this is important if we want to use the model via a web service.
	 * @param entries - a {@link Set} of {@link AbstractEntity} to test
	 */
	public boolean checkSuitableForRepresentationPerContainment(Set<AbstractEntity> entries) throws ZarathudException {
 		
		boolean result = true;
		
		Map<AbstractClassEntity, Set<AbstractClassEntity>> map = buildMap( entries);				
		
		for (AbstractEntity abstractEntity : entries) {
			if (abstractEntity instanceof AbstractClassEntity == false) {
				continue;
			}
			Set<AbstractClassEntity> properties = map.get(abstractEntity);
			if (properties == null)
				continue;
			for (AbstractClassEntity property : properties) {			
				AbstractClassEntity classEntity = (AbstractClassEntity) abstractEntity;
				if (checkHit(map, property, classEntity, shortCircuit)) {
					result = false;
					if (shortCircuit) 
						return false;
				}			
			}
		}
		return result;
	}
}
