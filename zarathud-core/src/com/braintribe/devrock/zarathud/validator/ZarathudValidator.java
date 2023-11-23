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
package com.braintribe.devrock.zarathud.validator;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.zarathud.ZarathudException;
import com.braintribe.devrock.zarathud.commons.SimpleTypeRegistry;
import com.braintribe.logging.Logger;
import com.braintribe.model.denotation.zarathud.ValidationMode;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.zarathud.ContextResultClassification;
import com.braintribe.model.zarathud.ValidationContext;
import com.braintribe.model.zarathud.ValidationContextMessage;
import com.braintribe.model.zarathud.data.AbstractClassEntity;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.AnnotationEntity;
import com.braintribe.model.zarathud.data.Artifact;
import com.braintribe.model.zarathud.data.ClassEntity;
import com.braintribe.model.zarathud.data.EnumEntity;
import com.braintribe.model.zarathud.data.FieldEntity;
import com.braintribe.model.zarathud.data.InterfaceEntity;
import com.braintribe.model.zarathud.data.MethodEntity;

/**
 * validates a single {@link Artifact} as held in the {@link BasicPersistenceGmSession}
 * <br/>
 * the {@link Artifact} is identified by group id, artifact id, version - so the session may contain more than one artifact. 
 * returns a {@link ValidationContext} that contains the relevant data  
 * @author pit
 *
 */
public class ZarathudValidator {
	private static Logger log = Logger.getLogger(ZarathudValidator.class);
	private static final String desc_void = "V";
	
	
	private ValidationContext validatorContext;
	private ValidationContextLogger contextLogger;
	private BasicPersistenceGmSession session;
	
	int validationKind = ValidationModeConverter.STANDARD; 
	
	private Artifact candidateArtifact;
	
	private String [] prohibitedMethods = {"equals", "hashCode"};
	
	@Configurable
	public void setSession(BasicPersistenceGmSession session) {
		this.session = session;
	}
	
	@Configurable
	public void setMode(ValidationMode mode) {
		validationKind = ValidationModeConverter.validationModeToBinary(mode);
	}
	
	/**
	 * build internal structures needed later - is run only once for each instance.  
	 */
	
	
	/**
	 * run the validation of the entities defined in the session single artifact
	 * @param groupId - the group id of the {@link Artifact}
	 * @param artifactId - the artifact id of the {@link Artifact}
	 * @param version  - the version of the {@link Artifact}
	 * @return - the {@link ValidationContext} with the results 
	 * @throws ZarathudException - if anything goes wrong 
	 */	
	public ValidationContext validateCandidate(String groupId, String artifactId, String version) throws ZarathudException {
		
		
		validatorContext = ValidationContext.T.create();
		validatorContext.setDate( new Date());
		
		contextLogger = new ValidationContextLogger();
		contextLogger.setValidationContext(validatorContext);

		// prime context	
		validatorContext.setOverallResult( ContextResultClassification.Clean);

		
		// query the artifact to check .. 
		EntityQuery baseArtifactQuery = EntityQueryBuilder.from( Artifact.class).where()
				.conjunction()
					.property( "groupId").eq( groupId)
					.property( "artifactId").eq( artifactId)
					.property( "version").eq( version)					
				.close()
			.done();
		
		// get artifact 
		candidateArtifact = null;
		try {	
			candidateArtifact = session.query().entities(baseArtifactQuery).unique();
			validatorContext.setArtifact(candidateArtifact);
		} catch (GmSessionException e) {
			String msg="cannot find base artifact";
			log.error( msg, e);
			throw new ZarathudException(msg, e);
		}
		
		// 
		// iterate over all entities 
		//
		for (AbstractEntity entity : candidateArtifact.getEntries()) {
 
			// only validated directly defined entries (artifact may contain direct dependencies, that are NOT declared in the artifact itself)
			if (entity.getDefinedLocal() == false)
				continue;
			if (entity instanceof ClassEntity) {
				// classes
				handle( (ClassEntity) entity);
			} else  
				if (entity instanceof InterfaceEntity) {
					// interfaces
					handle( (InterfaceEntity) entity);
				} else
					// handle enum entity 
					if (entity instanceof EnumEntity){
						handle( (EnumEntity) entity);
					} else {
						// anything else
						log.warn( "unsupported type [" + entity.getClass().getName());
					}							
		}
		// check if a test for containment's requested 
		if (
				(validationKind & ValidationModeConverter.CONTAINMENT) == ValidationModeConverter.CONTAINMENT ||
				(validationKind & ValidationModeConverter.QUICKCONTAINMENT) == ValidationModeConverter.QUICKCONTAINMENT
				
			) {
				boolean result = checkSuitableForRepresentationPerContainment( candidateArtifact.getEntries(), contextLogger);
				if (result) {
					log.debug("Model is suited for representation by containment");
				} else {
					log.warn("Model is NOT suited for representation by containment");					
				}			
		}
		
		
		// collect all messages, if any and decide what overall value to set
		for (ValidationContextMessage message : validatorContext.getMessages()) {
			switch (message.getClassification()) {
			case FieldInitialized:
				if (validatorContext.getOverallResult() == ContextResultClassification.Clean)
					validatorContext.setOverallResult( ContextResultClassification.Warnings);
				break;
			case InvalidMethods:
				validatorContext.setOverallResult( ContextResultClassification.Errors);
				break;
			case MultipleIdProperties:
				validatorContext.setOverallResult( ContextResultClassification.Errors);
				break;
			case InvalidTypes:
				validatorContext.setOverallResult( ContextResultClassification.Errors);
				break;
			case Miscellaneous:
				if (validatorContext.getOverallResult() == ContextResultClassification.Clean)
					validatorContext.setOverallResult( ContextResultClassification.Warnings);
				break;
			case MissingIdProperty:
				if (validatorContext.getOverallResult() == ContextResultClassification.Clean)
					validatorContext.setOverallResult( ContextResultClassification.Warnings);
				break;
			case MissingAnnotation:
				if (validatorContext.getOverallResult() == ContextResultClassification.Clean)
					validatorContext.setOverallResult( ContextResultClassification.Warnings);
				break;
			case MissingGetter:
				validatorContext.setOverallResult( ContextResultClassification.Errors);
				break;
			case MissingSetter:
				validatorContext.setOverallResult( ContextResultClassification.Errors);
				break;
			case TypeMismatch:
				validatorContext.setOverallResult( ContextResultClassification.Errors);
				break;
			case WrongSignature:
				validatorContext.setOverallResult( ContextResultClassification.Errors);
				break;
			case ContainmentError:
				if (validatorContext.getOverallResult() == ContextResultClassification.Clean)
					validatorContext.setOverallResult( ContextResultClassification.Warnings);
				break;
			case CollectionInCollection :
				validatorContext.setOverallResult( ContextResultClassification.Errors);
				break;
			default:
				log.warn("unsupported message classification [" + message.getClassification() + "] found");
				break;
				
			}
		}
		return validatorContext;
	}
	
	/**
	 * handle enums 
	 * @param entity - the {@link EnumEntity}
	 * @throws ZarathudException - if anything goes wrong
	 */
	@SuppressWarnings("unused")
	private void handle( EnumEntity entity) throws ZarathudException {
		log.debug("Analyzing enum " + entity.getName());
	}
	
	/**
	 * handles classes 
	 * @param entity - the {@link ClassEntity} to check 
	 * @throws ZarathudException - if anything goes wrong 
	 */
	private void handle( ClassEntity entity) throws ZarathudException {
		log.debug("Analyzing class " + entity.getName());
		
		if ((validationKind & ValidationModeConverter.MODEL) == ValidationModeConverter.MODEL) {
			// if this is a generic entity, we do some special stuff 
			if (entity.getGenericNature() == true) {
				// check for field initializers (none present for now)
				List<FieldEntity> fields = entity.getFields();
				if (fields != null && fields.size() > 0) {
					for (FieldEntity field: fields) {
						if (field.getInitializerPresentFlag()) {
							contextLogger.logInitializedField( field);
						}
					}
				}
				//, we check the setter / getters
				handleMethods( entity.getMethods());
				// check the id properties
				handleIdProperties( entity);
			} else {
				// not recognized as a GenericEntity -> this is a bug 
				contextLogger.logNotGenericEntity( entity);
			}				
		}
		
		if ((validationKind & ValidationModeConverter.PERSISTENCE) == ValidationModeConverter.PERSISTENCE) {
			checkSuitableForPersistence(entity);
		}
	}
			
	
	
	/**
	 * handles interfaces 
	 * @param entity - the {@link InterfaceEntity}E to check 
	 * @throws ZarathudException - if anything goes wrong 
	 */
	private void handle( InterfaceEntity entity) throws ZarathudException {
		log.debug("Analyzing interface " + entity.getName());
		if ((validationKind & ValidationModeConverter.MODEL) == ValidationModeConverter.MODEL) {
			if (entity.getGenericNature()) {
				Set<MethodEntity> methods = entity.getMethods();
				handleMethods( methods);
				// check the id properties
				handleIdProperties( entity);
				
				// check use of ImplementAbstractProperties & Transient Annotations 
				// check if any of the properties has a transient, and then check if the interface has a ImplementAbstractProperties
				// retrieve all annotations of the type up to here - recursively through the type hierarchy  
				Set<AnnotationEntity> annotations = getAnnotations(entity);
				if (annotations != null && annotations.size() > 0) {
					if (annotationPresentInSet(annotations, ValidationTokens.ANNO_IMPLEMENT_ABSTRACT_PROPERTIES)){
						// retrieve all tuples with the transient annotation - recursively throught the type hierarchy 
						Set<AccessTuple> transientTuples = getTuplesWithAnnotation(entity, ValidationTokens.ANNO_TRANSIENT);
						if (transientTuples != null && transientTuples.size() > 0) {
							//System.out.println("Transient and ImplementAbstractProperties used in conjunction");
							contextLogger.logTransientInInstantiableInterface(entity);
						}
					}
				}
			} else {
				// not recognized as a GenericEntity -> this is a bug 
				if (entity.getName().endsWith( ValidationTokens.PACKAGE_INFO) == false)
					contextLogger.logNotGenericEntity( entity);
			}
		}
		if ((validationKind & ValidationModeConverter.PERSISTENCE) == ValidationModeConverter.PERSISTENCE) {
			checkSuitableForPersistence(entity);
		}
	}
		
	
	
	
	/**
	 * analyzes tuples: signatures must be valid, types must be GE or simple type or marked with transient annotation
	 * @param tuple - {@link AccessTuple} to check 
	 * @throws ZarathudException - if anything goes wrong
	 */
	private void handleTuples( AccessTuple tuple) throws ZarathudException {
		MethodEntity setter = tuple.getSetter();
		MethodEntity getter = tuple.getGetter();
		
		String getterDesc = null;
		String setterDesc = null;
		
		//
		// check completeness: both must be set + correct types and values of arguments/return values 
		// 
		if (getter == null) {			
			contextLogger.logMissingGetter( tuple);
		} else {
			// check: argument must be void
			List<AbstractEntity> arguments = getter.getArgumentTypes();
			if (arguments != null && arguments.size() > 0) {
				contextLogger.logGetterHasArguments( getter);
			}
			getterDesc = getter.getReturnType().getDesc();
			// no void return
			if (getterDesc.equalsIgnoreCase( desc_void)) {
				contextLogger.logGetterReturnsVoid(getter);
			}
		}
		if (setter == null) {			
			contextLogger.logMissingSetter( tuple);
		} else {
			// check return value must be void.
			String returnValue = setter.getReturnType().getDesc();
			if (returnValue.equalsIgnoreCase( desc_void) == false) {
				contextLogger.logSetterDoesNotReturnVoid( setter);
			}
			// check arguments 
			List<AbstractEntity> arguments = setter.getArgumentTypes();
			if (arguments == null || arguments.size() == 0) {
				contextLogger.logSetterHasNoArguments(setter);
			} else
				if (arguments.size()> 1) {
					contextLogger.logSetterHasTooManyArguments(setter);
				} else {
					setterDesc = arguments.get(0).getDesc();
				}						
		}
		// compare the two types 
		if (getterDesc != null && setterDesc != null) {
			// referenced types must match 
			if (setterDesc.equalsIgnoreCase( getterDesc) == false) {
				contextLogger.logSetterGetterTypesDoNotMatch(tuple);
			}
			// referenced type must be GE or simple or marked transient
			String commonDesc = getterDesc;
			
			if (
					commonDesc.equalsIgnoreCase( "V") == false &&
					SimpleTypeRegistry.getSimpleTypeFromDesc( commonDesc) == null
				) {
				 				
				// no low level type, so match them 
				commonDesc = commonDesc.substring(1, commonDesc.length()-1); // drop the L
				if (SimpleTypeRegistry.getSimpleTypeSignatureFromDesc( commonDesc) == null) {
					// is it a collection type? 
					if (SimpleTypeRegistry.isCollectionType( commonDesc)) {					
						checkCollectionType( commonDesc, tuple); 											
					} else {
						// no simple type - lookup 
						String signature = commonDesc.replace("/", ".");						
						checkComplexType( signature, tuple);
					}	
				}
			}
		}							
	}
	
	
	
	/**
	 * check a collection type by tokenizing the element types and checking them in turn
	 * @param collectionType - the collection type as string 
	 * @param tuple - the {@link AccessTuple} 
	 * @throws ZarathudException - arrgh
	 */
	private void checkCollectionType( String collectionType, AccessTuple tuple) throws ZarathudException {
		// ()Ljava/util/List<Lcom/braintribe/model/malaclypse/cfg/Repository;>;
		String fullSignature = tuple.getGetter().getSignature();
		fullSignature = fullSignature.substring( 3 + collectionType.length(), fullSignature.length()-1);
		// use a tokenizer to check all values - also the map. 
		StringTokenizer tokenizer = new StringTokenizer( fullSignature, "<>;");		
		while (tokenizer.hasMoreTokens()) {
			String sig = tokenizer.nextToken();
			sig = sig.substring(1, sig.length()); 
			if (SimpleTypeRegistry.isCollectionType(sig)) {
				contextLogger.logCollectionElementIsCollectionType( tuple, collectionType, sig);
				continue;
			}
			if (SimpleTypeRegistry.isSimpleType(sig) == false) 
				checkComplexType( sig.replace("/", "."), tuple);	
		}
	}
	/**
	 * check a complex type 
	 * @param signature - {@link String} with the signature 
	 * @param tuple - the {@link AccessTuple}
	 * @throws ZarathudException - arrgh
	 */
	private void checkComplexType( String signature, AccessTuple tuple) throws ZarathudException {
		// look up the type  
		MethodEntity getter = tuple.getGetter();
		MethodEntity setter = tuple.getSetter();
		try {
			// is it one defined in the session? 
			EntityQuery abstractArtifactQuery = EntityQueryBuilder.from( AbstractEntity.class).where().conjunction()
					.property( "name").eq( signature)
					.disjunction()
						.property("artifact").eq().entity(candidateArtifact)
						.property("definedLocal").eq( false)
					.close()
				.close()
			.done();
			// run the query 
			AbstractEntity abstractEntity = session.query().entities(abstractArtifactQuery).unique();			
			
			if (abstractEntity == null) {
				// not a type defined in the extraction, might be JRE?
				try {
					@SuppressWarnings("rawtypes")
					Class clazz = Class.forName( signature);
					log.debug("Class [" + clazz.getName() +"] resolved via CP (from JT)");
					// definitively not generic, check transient annotations 
					if (annotationPresentInSet( getter.getAnnotations(), ValidationTokens.ANNO_TRANSIENT) == false) {
						contextLogger.logMethodIsMissingAnnotation( getter, ValidationTokens.ANNO_TRANSIENT, signature);
					}
					if (annotationPresentInSet(setter.getAnnotations(), ValidationTokens.ANNO_TRANSIENT) == false) {
						contextLogger.logMethodIsMissingAnnotation( setter, ValidationTokens.ANNO_TRANSIENT, signature);
					}
				} catch (ClassNotFoundException e) {
					contextLogger.logTypeNotFound( tuple, signature);
				}																		
			} else {
				if (abstractEntity instanceof AbstractClassEntity) {
					AbstractClassEntity baseEntity = (AbstractClassEntity) abstractEntity;
					// check generic nature of defined type 
					if (baseEntity.getGenericNature() == false) {
						// not generic, check transient annotations 
						if (annotationPresentInSet( getter.getAnnotations(), ValidationTokens.ANNO_TRANSIENT) == false) {
							contextLogger.logMethodIsMissingAnnotation( getter, ValidationTokens.ANNO_TRANSIENT, signature);
						}
						if (annotationPresentInSet(setter.getAnnotations(), ValidationTokens.ANNO_TRANSIENT) == false) {
							contextLogger.logMethodIsMissingAnnotation( setter, ValidationTokens.ANNO_TRANSIENT, signature);
						}
					}
				}
			}
		} catch (GmSessionException e) {
			String msg ="cannot query for type [" + signature + "]";
			log.error( msg, e);
			throw new ZarathudException(msg, e);
		}
	
	}
	
	/**
	 *  walk through the type hierarchy and counts all occurrences of the IdProperty, 
	 *  creates validation error messages if more than one access tuple with the 
	 *  IdProperty annotation is found.
	 * @param entity - the {@link AbstractClassEntity} to check 
	 * @throws ZarathudException - if anything goes wrong 
	 */
	private void handleIdProperties(AbstractClassEntity entity) throws ZarathudException {
		//
		Set<AccessTuple> idPropertyTuples = getTuplesWithAnnotation(entity, ValidationTokens.ANNO_ID_PROPERTY);
		if (idPropertyTuples.size() > 1) {
			contextLogger.logMultipleIdProperties(entity, idPropertyTuples);
		}
	}
	
	/**
	 * doesn't need to be tested, as JAVA will not allow this case<br/>
	 * any getter overload will require that the return types match. 
	 * @param entity - {@link AbstractClassEntity}
	 * @throws ZarathudException - arrgh
	 */
	@SuppressWarnings("unused")
	private void checkMatchingTupleHierarchy( AbstractClassEntity entity) throws ZarathudException {
		Set<AccessTuple> propertyTuples = getTuplesWithAnnotation( entity, null);
		if (propertyTuples == null || propertyTuples.size() == 0)
			return;
		// sort by name .. 
		for (AccessTuple tuple : propertyTuples) {
			String name = tuple.getSuffix();
			String returnType = tuple.getGetter().getReturnType().getDesc();
			for (AccessTuple suspect : propertyTuples) {
				if (suspect == tuple)
					continue;
				String suspectName = suspect.getSuffix();
				if (name.equalsIgnoreCase( suspectName)) {
					// types must match 
					String suspectReturnType = suspect.getGetter().getReturnType().getDesc();
					if (returnType.equalsIgnoreCase( suspectReturnType) == false) {
						contextLogger.logTypeMismatchInPropertyHierarchy( entity, tuple, suspect);
					}
				}
			}
		}
	}
	
	/**
	 * helper to find if an annotation is present in the {@link Set} of {@link AnnotationEntity}<br/>
	 * <b>an {@link AnnotationEntity} has the full signature of the annotation!</b>
	 * @param annotations - the {@link Set} of {@link AnnotationEntity} to scan 
	 * @param suspect - the name of the annotation 
	 * @return - true if present or false if not present 
	 */
	public  boolean annotationPresentInSet( Set<AnnotationEntity> annotations, String suspect) {
		if (annotations == null || annotations.size() == 0)
			return false;
		for (AnnotationEntity annotation : annotations) {
			String annotationSignature = annotation.getName();
			// match of full qualified name first 
			if (annotationSignature.equalsIgnoreCase( suspect))
				return true;
			// match on simple name of the annotation as a fall back  
			if (annotationSignature.endsWith(suspect))
				return true;			
		}
		return false;
	}
	

	/**
	 * splits the methods into {@link AccessTuple}, and sorts out other functions, then analyzes them
	 * @param methods - a {@link Set} of {@link MethodEntity}
	 * @throws ZarathudException - if anything goes wrong
	 */
	private void handleMethods( Set<MethodEntity> methods) throws ZarathudException {
		// pair into getter/setter
		Set<AccessTuple> tuples = AccessTupleBuilder.tuplizeMethods(methods);		
		for (AccessTuple tuple: tuples) {
			//log.info(tuple.getGetter().getName() + " & " + tuple.getSetter().getName());
			handleTuples( tuple);
		}
		Set<MethodEntity> noTupleMethods = AccessTupleBuilder.getUnassignedMethods(tuples, methods);		
		for (MethodEntity method : noTupleMethods) {
			String name = method.getName();
			if (name.equalsIgnoreCase("<init>"))
				continue;
			boolean prohibited = false;
			for (String prohibitedMethod : prohibitedMethods) {
				if (name.equalsIgnoreCase(prohibitedMethod)) {
					contextLogger.logUseOfProhibitedMethod( method);
					prohibited = true;
					break;
				}
			}
			if (prohibited == false) {
				contextLogger.logUseOfInvalidMethod( method);
				log.debug("problematic method [" + method.getName() + "] of [" + method.getOwner().getName() + "] found");
			}			
		}
	}
	

	
	/**
	 * extracts all access tuples in the hierarchy that have a certain annotation attached (both getter/setter),
	 * if annotation's null, ALL tuples in the hierarchy are returned. 
	 * @param entity - the {@link AbstractClassEntity} to extract the {@link AccessTuple}
	 * @param annotation - the annotation as string to look for, or null if not filtering on annotation should occur
	 * @return - the {@link Set} of {@link AccessTuple}
	 * @throws ZarathudException - if anything goes wrong 
	 */
	private  Set<AccessTuple> getTuplesWithAnnotation( AbstractClassEntity entity, String annotation) throws ZarathudException {
		Set<AccessTuple> idPropertyTuples = new HashSet<AccessTuple>();
		Set<MethodEntity> methods = entity.getMethods();
		if (methods != null && methods.size() > 0)
			idPropertyTuples.addAll( getTuplesWithAnnoation(methods, annotation));
		
		if (entity instanceof ClassEntity) {
			ClassEntity classEntity = (ClassEntity) entity;
			// check for any methods that have the specified annotation
			
			// collect via super types
			ClassEntity superEntity = classEntity.getSuperType();
			if (superEntity != null) {
				idPropertyTuples.addAll( getTuplesWithAnnotation( superEntity, annotation));
			}
			// collect via interfaces 
			Set<InterfaceEntity> interfaces = classEntity.getImplementedInterfaces();
			if (interfaces != null && interfaces.size() > 0) {
				for (InterfaceEntity interfaceEntity : interfaces) {
					if (Boolean.TRUE.equals( interfaceEntity.getGenericNature())) {
						idPropertyTuples.addAll( getTuplesWithAnnotation( interfaceEntity, annotation));
					}
				}
			}
		} 
		else if (entity instanceof InterfaceEntity) {
			InterfaceEntity interfaceEntity = (InterfaceEntity) entity;
			Set<InterfaceEntity> superInterfaces = interfaceEntity.getSuperInterfaces();
			if (superInterfaces != null && superInterfaces.size() > 0) {
				for (InterfaceEntity suspect : superInterfaces) {
					idPropertyTuples.addAll( getTuplesWithAnnotation(suspect, annotation));
				}
			}
		}

		return idPropertyTuples;
	}
	
	/**
	 * returns all {@link AccessTuple} with certain annotation in the {@link Set} of {@link MethodEntity} of an {@link AbstractClassEntity}, 
	 * if annotation's null, ALL tuples are returned. 
	 * @param methods - a {@link Set} of {@link MethodEntity}
	 * @param annotation - the annotation as string to look for, or null if not filtering on annotation should occur
	 * @return - a {@link Set} of {@link AccessTuple} 
	 * @throws ZarathudException - if anything goes wrong 
	 */
	private  Set<AccessTuple> getTuplesWithAnnoation( Set<MethodEntity> methods, String annotation) throws ZarathudException {
		Set<AccessTuple> idPropertyTuples = new HashSet<AccessTuple>();
		
		Set<AccessTuple> tuples = AccessTupleBuilder.tuplizeMethods(methods);		
		for (AccessTuple tuple: tuples) {
		
			// look the annotations of the tuple.. 
			boolean idGetter = false;
			boolean idSetter = false;
			
			MethodEntity getter = tuple.getGetter();
			if (getter != null) {
				if (annotation != null) {
					idGetter = checkAnnotationOnMethod(getter, annotation);
				}
				else {
					idGetter = true;
				}
			}
			MethodEntity setter = tuple.getSetter();
			if (setter != null) {
				if (annotation != null) {
					idSetter = checkAnnotationOnMethod( setter, annotation);				
				} else {
					idSetter = true;
				}
			}
			if (!idGetter && !idSetter) {
				continue;
			}
			idPropertyTuples.add( tuple);
			
			if (idGetter && idSetter == false) {
				// report missing idProperty on setter
				contextLogger.logMethodIdPropertyMissing( setter);
				
				idPropertyTuples.add( tuple);
			}
			if (idGetter == false && idSetter) {
				// report missing idProperty on getter
				contextLogger.logMethodIdPropertyMissing( getter);
			}
		}
		return idPropertyTuples;
	}
	
	/**
	 * tests whether the method has an IdProperty attached 
	 * @param methodEntity - the {@link MethodEntity} to test 
	 * @return - true if the method has the IdProperty annotation attached 
	 */
	private  boolean checkAnnotationOnMethod( MethodEntity methodEntity, String annotation) {
		Set<AnnotationEntity> annotations = methodEntity.getAnnotations();
		if (annotations != null && annotations.size() > 0) {
			if (annotationPresentInSet(annotations, annotation))
				return true;
		}
		return false;
	}
	
	
	/**
	 * get all annotations (also in type hierarchy) for the given class or interface 
	 * @param entity - the {@link AbstractClassEntity} to accumulate all {@link AnnotationEntity}
	 * @return - a {@link Set} of all accumulated {@link AnnotationEntity}
	 */
	private Set<AnnotationEntity> getAnnotations(AbstractClassEntity entity) {
		Set<AnnotationEntity> result = new HashSet<AnnotationEntity>();
		Set<AnnotationEntity> locals = entity.getAnnotations();
		if (locals != null && locals.size() > 0)
			result.addAll( locals);
		if (entity instanceof ClassEntity) {
			ClassEntity classEntity = (ClassEntity) entity;
			// check for any methods that have the specified annotation
			
			// collect via super types
			ClassEntity superEntity = classEntity.getSuperType();
			if (superEntity != null) {
				result.addAll( getAnnotations( superEntity));
			}
			// collect via interfaces 
			Set<InterfaceEntity> interfaces = classEntity.getImplementedInterfaces();
			if (interfaces != null && interfaces.size() > 0) {
				for (InterfaceEntity interfaceEntity : interfaces) {
					if (Boolean.TRUE.equals( interfaceEntity.getGenericNature())) {
						result.addAll( getAnnotations( interfaceEntity));
					}
				}
			}
		} 
		else if (entity instanceof InterfaceEntity) {
			InterfaceEntity interfaceEntity = (InterfaceEntity) entity;
			Set<InterfaceEntity> superInterfaces = interfaceEntity.getSuperInterfaces();
			if (superInterfaces != null && superInterfaces.size() > 0) {
				for (InterfaceEntity suspect : superInterfaces) {
					result.addAll( getAnnotations( suspect));
				}
			}
		}
		return result;
	}
	
	
	
	
	/**
	 * tests if a model can be represented in a containment only representation<br/>
	 * this is important if we want to use the model via a web service.
	 * @param entries - a {@link Set} of {@link AbstractEntity} to test
	 * @return - true if suited, false otherwise 	
	 */
	private boolean checkSuitableForRepresentationPerContainment(Set<AbstractEntity> entries, ValidationContextLogger logger) throws ZarathudException {		
		ContainmentChecker containmentChecker = new ContainmentChecker();
		containmentChecker.setSession(session);
		containmentChecker.setCandidateArtifact(candidateArtifact);				
		containmentChecker.setContextLogger(logger);
		
		if ((validationKind & ValidationModeConverter.QUICKCONTAINMENT) == ValidationModeConverter.QUICKCONTAINMENT)
			containmentChecker.setShortCircuit(true);
		else
			containmentChecker.setShortCircuit(false);
		
		return containmentChecker.checkSuitableForRepresentationPerContainment(entries);
	}
	
	
	/**
	 * checks if the {@link AbstractClassEntity} can be instantiated in a persistence level, i.e whether is has an 
	 * id property, and all complex types referenced by it have it too .  
	 * @param entity - the {@link AbstractClassEntity} to check 
	 * @return - true if can be instantiated... 
	 */
	private boolean checkSuitableForPersistence(AbstractClassEntity entity) throws ZarathudException {
		if (entity instanceof InterfaceEntity) {
			Set<AnnotationEntity> annotations = getAnnotations(entity);
			if (annotations != null && annotations.size() > 0) {
				if (annotationPresentInSet(annotations, ValidationTokens.ANNO_IMPLEMENT_ABSTRACT_PROPERTIES) == false){
					return true;
				}
			}
		} 
		else {
			ClassEntity classEntity = (ClassEntity) entity;
			if (classEntity.getAbstractNature()) {
				return true;
			}
		}
		// either interface marked with IAP, or non abstract class -> must be checked..
		// a) check presence of id property
		Set<AccessTuple> idPropertyTuples = getTuplesWithAnnotation(entity, ValidationTokens.ANNO_ID_PROPERTY);
		if (idPropertyTuples == null || idPropertyTuples.size() == 0) {
			contextLogger.logMissingIdPropertyForPersistence(entity, null);
			return false;
		}
		// b) check presence of id property in all referenced complex types.. 
		Set<AccessTuple> tuples = getTuplesWithAnnotation(entity, null);
		for (AccessTuple tuple : tuples) {
			String commonDesc = tuple.getGetter().getReturnType().getDesc();
			
			if (
					commonDesc.equalsIgnoreCase( "V") == false &&
					SimpleTypeRegistry.getSimpleTypeFromDesc( commonDesc) == null
				) {
				 				
				// no low level type, so match them 
				commonDesc = commonDesc.substring(1, commonDesc.length()-1); // drop the L
				if (SimpleTypeRegistry.getSimpleTypeSignatureFromDesc( commonDesc) == null) {
					// is it a collection type? 
					if (SimpleTypeRegistry.isCollectionType( commonDesc)) {					
						checkCollectionType( commonDesc, tuple); 											
					} else {
						// no simple type - lookup 
						String signature = commonDesc.replace("/", ".");
						
						EntityQuery abstractArtifactQuery = EntityQueryBuilder.from( AbstractEntity.class).where().conjunction()
								.property( "name").eq( signature)
								.disjunction()
									.property("artifact").eq().entity(candidateArtifact)
									.property("definedLocal").eq( false)
								.close()
							.close()
						.done();
						try {
							// run the query 
							AbstractEntity abstractEntity = session.query().entities(abstractArtifactQuery).unique();
							if (abstractEntity instanceof AbstractClassEntity == false)
									continue;
							AbstractClassEntity abstractClassEntity = (AbstractClassEntity) abstractEntity;
							if (checkSuitableForPersistence(abstractClassEntity) == false) {
								contextLogger.logMissingIdPropertyForPersistence(abstractClassEntity, tuple);
								return false;
							}
						} catch (GmSessionException e) {
							String msg = String.format("cannot scan for property [%s]'s complex type [%s]", tuple.getSuffix(), signature);
							log.error("cannot scan for complex type");
							throw new ZarathudException(msg, e);
						}
						
					}	
				}
			}					
		}
		
		return true;
	}
	
}
