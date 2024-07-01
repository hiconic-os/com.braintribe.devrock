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
package com.braintribe.devrock.zarathud.comparator;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.cfg.Required;
import com.braintribe.devrock.zarathud.ZarathudException;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.zarathud.ComparisonContext;
import com.braintribe.model.zarathud.ContextResultClassification;
import com.braintribe.model.zarathud.data.AbstractClassEntity;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.AccessModifier;
import com.braintribe.model.zarathud.data.AnnotationEntity;
import com.braintribe.model.zarathud.data.Artifact;
import com.braintribe.model.zarathud.data.ClassEntity;
import com.braintribe.model.zarathud.data.EnumEntity;
import com.braintribe.model.zarathud.data.InterfaceEntity;
import com.braintribe.model.zarathud.data.MethodEntity;

/**
 * compares two {@link Artifact}, the base and the publishing candidate ..  
 * <br/>
 * <br/>
 * requires : <br/>
 * a {@link BasicPersistenceGmSession} that contains the base artifact<br/>
 * a {@link BasicPersistenceGmSession} that contains the candidate artifact<br/>
 * the {@link ComparisonContext} to store the comparison result and detail information.<br/>
 * 
 * @author pit
 *
 */
public class ZarathudComparator {

	private static Logger log = Logger.getLogger(ZarathudComparator.class);
	
	private BasicPersistenceGmSession baseSession;
	private BasicPersistenceGmSession candidateSession;
	private ComparisonContext comparisonContext;
	private ComparisonContextLogger contextLogger;
	
	@Required
	public void setBaseSession(BasicPersistenceGmSession baseSession) {
		this.baseSession = baseSession;
	}	
	@Required
	public void setCandidateSession(BasicPersistenceGmSession candidateSession) {
		this.candidateSession = candidateSession;
	}
	
	/**
	 * actual does the comparison 
	 * @return - true if candidate's valid, false otherwise
	 * @throws ZarathudException - if anything catastrophic goes wrong
	 */
	public ComparisonContext analyzeCandidate() throws ZarathudException {
		
		
		comparisonContext = ComparisonContext.T.create();
		comparisonContext.setDate( new Date());
		
		contextLogger = new ComparisonContextLogger();
		contextLogger.setComparisonContext(comparisonContext);
		
		// prime context
		comparisonContext.setOverallResult( ContextResultClassification.Clean);
		
	
		
		EntityQuery baseArtifactQuery = EntityQueryBuilder.from( Artifact.class).done();
		
		// get base artifact 
		Artifact baseArtifact = null;
		try {	
			baseArtifact = baseSession.query().entities(baseArtifactQuery).unique();
			comparisonContext.setBaseArtifact(baseArtifact);
		} catch (GmSessionException e) {
			String msg="cannot find base artifact";
			log.error( msg, e);
			throw new ZarathudException(msg, e);
		}
		
		Artifact candidateArtifact = null;
		try {		
			candidateArtifact = candidateSession.query().entities(baseArtifactQuery).unique();
			comparisonContext.setCandidateArtifact( candidateArtifact);
		} catch (GmSessionException e) {
			String msg="cannot find candidate artifact";
			log.error( msg, e);
			throw new ZarathudException(msg, e);
		}		
		
		// 
		// classes.. 
		//
		for (AbstractEntity entity : baseArtifact.getEntries()) {
			String name = entity.getName();
			AbstractEntity candidate = getEntityPerName(candidateSession, name);
			
			//
			// check : is there a candidate with the name
			//
			if (candidate == null) {
			
				contextLogger.logMissingEntityInCandidate( entity);
				comparisonContext.setOverallResult( ContextResultClassification.Errors);
				continue;
			}
			//
			// check : is it of the same type 
			//
			EntityType<? extends AbstractEntity> baseEntityType = entity.entityType();
			EntityType<? extends AbstractEntity> candidateEntityType = candidate.entityType();
			
			if (baseEntityType != candidateEntityType) {
				contextLogger.logTypeNotMatching( entity, candidate);
				comparisonContext.setOverallResult( ContextResultClassification.Errors);
				continue;
			}
			
			//
			// check : type specifica
			//
			if (entity instanceof EnumEntity) {
				// enum
				handleEnum( (EnumEntity) entity, (EnumEntity) candidate);
			} else 
				if (entity instanceof AnnotationEntity) {
					// annotation 
					handleAnnotation( (AnnotationEntity) entity, (AnnotationEntity) candidate);
				} else 
					if (entity instanceof ClassEntity) {
						// class
						handleClass( (ClassEntity) entity, (ClassEntity) candidate);
					} else
						if (entity instanceof InterfaceEntity ){
							/// interface
							handleInterface( (InterfaceEntity) entity, (InterfaceEntity) candidate);
						} else {
							// no such type - throw exception 
							String msg = "type [" + entity.getClass().getName() + "] isn't supported";
							log.error( msg);
							throw new ZarathudException(msg);						
						}
		}
		
		
		log.info("Comparison of candidate [" + artifactToString(candidateArtifact) + "] with base [" + artifactToString(baseArtifact) +"]: " + comparisonContext.getOverallResult().toString());
		
		return comparisonContext;
	}
	
	/**
	 * handle check on two {@link EnumEntity}<br/>
	 * the values of the base must exist in the candidate. More may of course exist. 
	 * @param baseEnum - the original {@link EnumEntity} 
	 */
	private void handleEnum( EnumEntity baseEnum, EnumEntity candidateEnum) {
		for (String value : baseEnum.getValues()) {
			for (String suspect : candidateEnum.getValues()) {
				boolean found = false;
				if (value.equalsIgnoreCase(suspect)) {
					found = true;
					break;
				}
				if (found == false) {
					// log
					contextLogger.logMissingEnumValue(baseEnum, candidateEnum, value);
					comparisonContext.setOverallResult( ContextResultClassification.Errors);
					return;
				}					
			}
		}				 
	}
	
	/**
	 * currently just for the sake of completeness - the annotation must only be present, which it is.
	 * @param baseAnnotation - the {@link AnnotationEntity} from the base 
	 * @param candidateAnnotation - the {@link AnnotationEntity} from the candidate 
	 */
	private void handleAnnotation( AnnotationEntity baseAnnotation, AnnotationEntity candidateAnnotation) {		
	}
	
	/**
	 * processes a class 
	 * @param baseClass - the {@link ClassEntity} that is the base 
	 * @param candidateClass - the {@link ClassEntity} that is the candidate 
	 * @throws ZarathudException - if anything goes wrong
	 */
	private void handleClass( ClassEntity baseClass, ClassEntity candidateClass) throws ZarathudException {
		// test if all methods of the base exist and are valid.
		Set<MethodEntity> methods = baseClass.getMethods();
		if (methods == null || methods.size() == 0)
			return;
		
		for (MethodEntity baseMethod : methods) {			
			MethodEntity candidateMethod = getCandidateMethod(candidateSession, baseMethod, candidateClass);
			AccessModifier baseAccessModifier = baseMethod.getAccessModifier(); 
			if (candidateMethod == null) {
				// if the base method's not private, it must exist in the candidate 
				if (baseAccessModifier != AccessModifier.PRIVATE) {
					contextLogger.logMissingMethod(baseMethod, candidateClass);
					comparisonContext.setOverallResult( ContextResultClassification.Errors);			
				}
				continue;
			}
			// if it's not a private method in the base, it must be checked. 
			if (baseAccessModifier != AccessModifier.PRIVATE) {
				boolean valid = compareMethod( baseMethod, candidateMethod);
				if (valid == false)
					comparisonContext.setOverallResult( ContextResultClassification.Errors);
				}
		}	
	}
	

	/**
	 * processes an interface 
	 * @param baseInterface - the {@link InterfaceEntity} that is base 
	 * @param candidateInterface - the {@link InterfaceEntity} that is the candidate 
	 * @throws ZarathudException - if anything goes wrong 
	 */
	private void handleInterface( InterfaceEntity baseInterface, InterfaceEntity candidateInterface) throws ZarathudException {
		// test if all methods of the base exist and are valid.
		Set<MethodEntity> methods = baseInterface.getMethods();
		if (methods == null || methods.size() == 0)
			return;
		for (MethodEntity baseMethod : methods) {
			MethodEntity candidateMethod = getCandidateMethod(candidateSession, baseMethod, candidateInterface);
			if (candidateMethod == null) {
				contextLogger.logMissingMethod(baseMethod, candidateInterface);
				comparisonContext.setOverallResult( ContextResultClassification.Errors);
				continue;
			}						
			boolean valid = compareMethod( baseMethod, candidateMethod);
			if (valid == false)
				comparisonContext.setOverallResult( ContextResultClassification.Errors);
		}	
	}
	
	/**
	 * compares two methods - 
	 * a) finds the method, tests by name and desc. <br/>
	 * b) compares and local 
	 * @param baseMethod - the {@link MethodEntity} from the base
	 * @param candidateMethod - the {@link MethodEntity} from the candidate 	
	 */	
	private boolean compareMethod( MethodEntity baseMethod, MethodEntity candidateMethod) {
				
		// access modifiers
		AccessModifier baseAccess = baseMethod.getAccessModifier();
		AccessModifier candidateAccess = candidateMethod.getAccessModifier();
		
		boolean result = true;
		if (!matchAccessModifiers(baseAccess, candidateAccess)) {
			contextLogger.logMismatchedAccessModifier(baseMethod, candidateMethod);
			result = false;
		}
		
		
		
		// annotations
		if (!matchAnnotations( baseMethod, baseMethod.getAnnotations(), candidateMethod.getAnnotations())) {
			result = false;		
		}
					
		// exceptions
		if (!matchExceptions( baseMethod, baseMethod.getExceptions(), candidateMethod.getExceptions())) {	
			result = false; 
		}
				
		return result;
	}
	
	/**
	 * matches two {@link AccessModifier}:<br/>
	 * base PUBLIC : candidate may only be PUBLIC as well<br/>
	 * base PROTECTED: candidate may be PROTECTED or PUBLIC<br/>
	 * base PRIVATE : candidate may be PRIVATE, PROTECTED or PUBLIC<br/>
	 * @param baseAccess - the {@link AccessModifier} of the base
	 * @param candidateAccess - the {@link AccessModifier} of the candidate 
	 * @return true if match
	 */
	private boolean matchAccessModifiers( AccessModifier baseAccess, AccessModifier candidateAccess) {
		switch (baseAccess) {
			case PUBLIC:
				if (candidateAccess != AccessModifier.PUBLIC)
					return false;
				break;
			case PROTECTED:
				if (candidateAccess != AccessModifier.PUBLIC && candidateAccess != AccessModifier.PROTECTED)
					return false;
				break;
			case PRIVATE:
				if (candidateAccess != AccessModifier.PUBLIC && candidateAccess != AccessModifier.PROTECTED && candidateAccess != AccessModifier.PRIVATE)
					return false;
				break;
		}
		return true;
	
	}
	
	/**
	 * matches {@link AnnotationEntity}:<br/>
	 * base's annotations must exist in candidate, and the candidate may not introduce new annotations 
	 * @param owner - the {@link GenericEntity}, can be a {@link MethodEntity} or a {@link AbstractClassEntity}
	 * @param baseAnnotations - the {@link Set} of the {@link AnnotationEntity} of the base 
	 * @param candidateAnnotations - the {@link Set} of the {@link AnnotationEntity} of the candidate
	 * @return - true if everything matches, false otherwise 
	 */
	private boolean matchAnnotations(GenericEntity owner, Set<AnnotationEntity> baseAnnotations, Set<AnnotationEntity> candidateAnnotations) {
		if (baseAnnotations == null || baseAnnotations.size() == 0) {
			if (candidateAnnotations == null || candidateAnnotations.size() == 0)
				return true;		 
			return false;		
		}
		boolean result = true;
		// check existing annotations 
		Set<AnnotationEntity> additionalCandidateAnnotations = new HashSet<AnnotationEntity>( candidateAnnotations);		
		for (AnnotationEntity baseAnnotation : baseAnnotations) {
			boolean found = false;
			for (AnnotationEntity candidateAnnotation : candidateAnnotations) {
				if (baseAnnotation.getName().equalsIgnoreCase( candidateAnnotation.getName())) {
					found = true;
					additionalCandidateAnnotations.remove(candidateAnnotation);
					break;
				}
			}
			if (!found) {
				contextLogger.logMissingAnnotations(owner, null, baseAnnotation);
				result = false;				
			} 
		}
		// 
		if (additionalCandidateAnnotations.size() > 0) {
			for (AnnotationEntity annotation : additionalCandidateAnnotations) {
				contextLogger.logAdditionalAnnotation(owner, null, annotation);
			}
			result = false;
		}
		return result;
	}
	
	/**
	 * matches exceptions<br/>
	 * all exceptions of base must exits in candidate, and no more may be introduced 
	 * @param owner - the {@link MethodEntity} that owns the exceptions 
	 * @param baseExceptions - the {@link Set} of {@link ClassEntity} that represent the exceptions of the base
	 * @param candidateExceptions - the {@link Set} of {@link ClassEntity} that represent the exceptions of the candidate
	 * @return - true if success...
	 */
	private boolean matchExceptions( MethodEntity owner, Set<ClassEntity> baseExceptions, Set<ClassEntity> candidateExceptions) {
		if (baseExceptions == null || baseExceptions.size() == 0) {
			if (candidateExceptions == null || candidateExceptions.size() == 0)
				return true;
			return false;
		}		
		
		boolean result = true;
		Set<ClassEntity> additionalCandidateExceptions = new HashSet<ClassEntity>( candidateExceptions);		
		for (ClassEntity baseException : baseExceptions) {
			boolean found = false;
			for (ClassEntity candidateException : candidateExceptions) {
				if (baseException.getName().equalsIgnoreCase( candidateException.getName())) {
					found = true;
					additionalCandidateExceptions.remove(candidateException);
					break;
				}
			}
			if (found == false) {
				contextLogger.logMissingException(owner, null, baseException);
				result = false;
			}
		}
		if (additionalCandidateExceptions.size() > 0) {
			result = false;
			for (ClassEntity exception : additionalCandidateExceptions) {
				contextLogger.logAdditionalException( null, null, owner,  exception);
			}
		}
		
		return result;
	}
	
	/**
	 * helper that formats the {@link Artifact} to a condensed string (&lt;group id&gt;:&lt;artifact id&gt;#&lt;version&gt;) 
	 * @param artifact - the {@link Artifact} 
	 * @return - the resulting string 
	 */
	private String artifactToString( Artifact artifact) {
		return artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + artifact.getVersion();
	}
	
	
	/**
	 * run a query on the {@link AbstractEntity} in the session - may only exist once 
	 * @param session - the {@link BasicPersistenceGmSession} that contains the data 
	 * @param name - the name of the {@link AbstractEntity} 
	 * @return - T the actual entity extending the {@link AbstractEntity}
	 * @throws ZarathudException - if anything goes wrong 
	 */	
	private <T extends AbstractEntity> T getEntityPerName( BasicPersistenceGmSession session, String name) throws ZarathudException {
		try {
			return session.query().entities( EntityQueryBuilder.from( AbstractEntity.class).where().property( "name").eq( name).done()).unique();
		} catch (GmSessionException e) {
			String msg="cannot query for [" + AbstractEntity.class.getName() + "] with name [" + name + "]";
			log.error( msg, e);
			throw new ZarathudException(msg, e);				
		}
	}
	
	/**
	 * queries for a candidate {@link MethodEntity} with the same name and desc as the base method 
	 * @param session - the candidate {@link BasicPersistenceGmSession}
	 * @param baseMethod - the {@link MethodEntity} that is the base 
	 * @param owner - the {@link AbstractClassEntity} that is the owner (in the candidate session )
	 * @return - the {@link MethodEntity} or null if not found 
	 * @throws ZarathudException
	 */
	@SuppressWarnings("javadoc")
	private MethodEntity getCandidateMethod( BasicPersistenceGmSession session, MethodEntity baseMethod, AbstractClassEntity owner) throws ZarathudException {
		try {
			return session.query().entities( EntityQueryBuilder.from( MethodEntity.class).where()
					.conjunction()
						.property( "name").eq( baseMethod.getName())
						.property( "desc").eq( baseMethod.getDesc())
						.property( "owner").eq().entity(owner)
					.close().done()).unique();						
		} catch (GmSessionException e) {
			String msg="cannot query for candidate method with name [" + baseMethod.getName() + ":" + baseMethod.getDesc() + "]";
			log.error( msg, e);
			throw new ZarathudException(msg, e);				
		}
		
	}		
	
}
