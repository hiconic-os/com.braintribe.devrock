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
package com.braintribe.devrock.zarathud.extracter.registry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.asm.Type;
import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.zarathud.ZarathudException;
import com.braintribe.devrock.zarathud.commons.SimpleTypeRegistry;
import com.braintribe.devrock.zarathud.extracter.ExtractionRegistry;
import com.braintribe.devrock.zarathud.extracter.scanner.AnnotationTuple;
import com.braintribe.devrock.zarathud.extracter.scanner.AsmClassDepScanner;
import com.braintribe.devrock.zarathud.extracter.scanner.AsmClassDepScannerException;
import com.braintribe.devrock.zarathud.extracter.scanner.ClassData;
import com.braintribe.devrock.zarathud.extracter.scanner.FieldData;
import com.braintribe.devrock.zarathud.extracter.scanner.InheritanceData;
import com.braintribe.devrock.zarathud.extracter.scanner.MethodData;
import com.braintribe.devrock.zarathud.extracter.scanner.ScannerResult;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.service.api.UnsupportedRequestTypeException;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.zarathud.data.AbstractClassEntity;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.AccessModifier;
import com.braintribe.model.zarathud.data.AnnotationEntity;
import com.braintribe.model.zarathud.data.AnnotationValueContainer;
import com.braintribe.model.zarathud.data.AnnotationValueContainerType;
import com.braintribe.model.zarathud.data.Artifact;
import com.braintribe.model.zarathud.data.ClassEntity;
import com.braintribe.model.zarathud.data.EnumEntity;
import com.braintribe.model.zarathud.data.FieldEntity;
import com.braintribe.model.zarathud.data.InterfaceEntity;
import com.braintribe.model.zarathud.data.MethodEntity;

/**
 * drives the {@link AsmClassDepScanner} to extract information about classes, interfaces, annotations<br/>
 * both classes and interfaces have their 
 * <br/>
 * requires:<br/>
 * an instance of the {@link URLClassLoader} that contains all relevant jars of the CP<br/>
 * an instance of the {@link BasicPersistenceGmSession} to create the entities of the ZarathudModel into<br/>
 * <br/>
 * 
 * @author pit
 *
 */
public class BasicExtractionRegistry implements ExtractionRegistry {
	private static Logger log = Logger.getLogger(BasicExtractionRegistry.class);
	
	private Map<String, AbstractEntity> nameToEntryMap = new HashMap<>();
	private Map<String, GmType> nameToSimpleTypeMap = new HashMap<>();	
	private Map<String, Artifact> jarToBindingArtifact = new HashMap<>();	
	private Map<String, Artifact> nameToBindingArtifact = new HashMap<>();

	
	private BasicPersistenceGmSession session;
	private URLClassLoader classLoader;
	
	private BasicModelOracle modelOracle;
	private String genericEntityResoureceName;

	private Map<String, Artifact> urlToArtifactMap = new HashMap<>();
	
	private Artifact javaArtifact;
	private Artifact unknownArtifact;
	
	
	@Override
	public void setUrlToArtifactMap(Map<URL, Artifact> urlToArtifactMap) {
		for (Entry<URL, Artifact> entry : urlToArtifactMap.entrySet()) {
			this.urlToArtifactMap.put( entry.getKey().toString(), entry.getValue());
		}
		URL [] urls = urlToArtifactMap.keySet().toArray( new URL[0]);
		classLoader = new URLClassLoader(urls, getClass().getClassLoader());
	}

	/**
	 * optional session to be used to create the {@link GenericEntity} within.
	 * @param session - a {@link BasicPersistenceGmSession} 
	 */
	@Configurable
	public void setSession(BasicPersistenceGmSession session) {
		this.session = session;
	}
	
	private BasicModelOracle acquireRootModelOracle() {
		if (modelOracle != null) {
			return modelOracle;
		}
		GenericModelTypeReflection reflection = GMF.getTypeReflection();
		Model model = reflection.getModel("com.braintribe.gm:root-model");
		GmMetaModel metaModel = model.getMetaModel();
		
		modelOracle = new BasicModelOracle( metaModel);
		
		return modelOracle;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#setup()
	 */
	@Override
	public void initialize() {

		// only initialize if required 
		if (nameToSimpleTypeMap.size() > 0)
			return;
		// a) create the simple types 
		for (SimpleType simpleType : SimpleTypes.TYPES_SIMPLE) {
			GmSimpleType gmSimpleType = acquireRootModelOracle().findGmType( simpleType);		
			nameToSimpleTypeMap.put(simpleType.getTypeName(), gmSimpleType);
		}
		// b) the base type
		GmBaseType baseType = acquireRootModelOracle().findGmType( BaseType.INSTANCE); 
		nameToSimpleTypeMap.put("object", baseType);
		
		
		GmType gmGenericEntityType = acquireRootModelOracle().findGmType( GenericEntity.T);
		genericEntityResoureceName = gmGenericEntityType.getTypeSignature().replace('.', '/');
	}
	
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#setSolutionList(java.util.Collection)
	 */
	@Override
	public void setSolutionList( Collection<Solution> solutions){
		PartTuple jarTuple = PartTupleProcessor.createJarPartTuple();
		for (Solution solution : solutions) {
			for (Part part : solution.getParts()){
				if (!PartTupleProcessor.equals(jarTuple,  part.getType()))
					continue;				
				Artifact artifact = create(Artifact.T);
				artifact.setGroupId( solution.getGroupId());
				artifact.setArtifactId( solution.getArtifactId());
				artifact.setVersion( VersionProcessor.toString( solution.getVersion()));				
				jarToBindingArtifact.put( part.getLocation(),artifact);
				log.debug("adding binding information for [" + part.getLocation() + "] to [" + artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + artifact.getVersion());				
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#getEntry(java.lang.String)
	 */
	@Override
	public AbstractEntity getEntry( String name) {
		return nameToEntryMap.get( name);
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#addEntry(com.braintribe.model.zarathud.AbstractEntity)
	 */
	@Override
	public void addEntry( AbstractEntity entry) {
		if (entry instanceof AbstractClassEntity) {
			/*
			AbstractClassEntity ace = (AbstractClassEntity) entry;
			if (ace.getParameterNature() || ace.getParameterization().size() > 0) {
				// build a key from the parameterization 
				nameToEntryMap.put( entry.getS(), entry);
				return;
			}
			*/
		}
		nameToEntryMap.put( entry.getName(), entry);
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#getEntries()
	 */
	@Override
	public Collection<AbstractEntity> getEntries() {
		return nameToEntryMap.values();
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#getRelevantEntries()
	 */
	@Override
	public Collection<AbstractEntity> getRelevantEntries() {
		Collection<AbstractEntity> result = new ArrayList<AbstractEntity>();
		for (AbstractEntity entity : nameToEntryMap.values()) {
			if (entity.getDefinedLocal()) {
				result.add( entity);
			} 
			else if (entity.getDirectDependency()){
				result.add( entity);
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#analyzeAnnotation(java.lang.String)
	 */
	@Override
	public AnnotationEntity analyzeAnnotation( String annotationName) throws ZarathudException {
		
		try {
			Artifact artifact = nameToBindingArtifact.get( annotationName);
			String resourceName = annotationName.replace('.', '/') + ".class";
			
			URL resourceUrl = classLoader.findResource( resourceName);
			
			if (resourceUrl == null) {
				// some annotations (java.lang.Deprecated for instance) cannot be found ??!! 
				// but as it's a standard rt.jar annotation, we don't care anyhow
				if (
						!annotationName.startsWith( "java/") &&
						!annotationName.startsWith( "javax/")
					) {
					throw new ZarathudException( "Cannot find resource [" + resourceName + "]");
				}
				else {
					AnnotationEntity annotationEntity = create( AnnotationEntity.T);
					annotationEntity.setName( annotationName);
					annotationEntity.setArtifact( acquireJavaArtifact());			
					return annotationEntity;
				}
			}
			
			// if it's not one of the main classes, it's a referenced class, and we deduce the artifact from the resource URL
			if (artifact == null) {
				String file = resourceUrl.getFile();
				String key = file.substring(0, file.indexOf('!'));
				artifact = urlToArtifactMap.get( key);
			}
			
			InheritanceData inheritenceData = AsmClassDepScanner.getInheritanceData( ensureInputStream(resourceUrl));
			AnnotationEntity annotationEntity = create( AnnotationEntity.T);
			annotationEntity.setName( inheritenceData.getName());
			annotationEntity.setArtifact(artifact);			
			return annotationEntity;
			
		} catch (IOException e) {
			String msg="cannot open url connection";
			log.error( msg, e);
			throw new ZarathudException(msg, e);
		} catch (AsmClassDepScannerException e) {
			String msg="cannot analyze annotation";
			log.error( msg, e);
			throw new ZarathudException(msg, e);
		}

		
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#acquireClassResource(java.lang.String, boolean)
	 */
	@Override
	public AbstractEntity acquireClassResource( String name, boolean lenient) throws ZarathudException {
		AbstractEntity entity = getEntry(name);
		if (entity == null) {
			try {
				entity = analyzeClassResource( name);
			} catch (ZarathudException e) {
				if (lenient) {
					log.warn("url class loader can't find resource [" + name + "]");
					
					AbstractClassEntity type = create( AbstractClassEntity.T);
					type.setScannedFlag( true);
					type.setName( name);
					addEntry(type);
					type.setArtifact( acquireArtifact( name));
					return type;										
				}
				throw e;
			}			
		}
		return entity;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#analyzeClassResource(java.lang.String)
	 */
	@Override
	public AbstractEntity analyzeClassResource( String className) throws ZarathudException {
		
		try {
			
				Artifact artifact = nameToBindingArtifact.get( className);
				String resourceName = className.replace('.', '/') + ".class";
				
				if (classLoader == null) {
					throw new ZarathudException( "No classloader defined to find [" + resourceName + "]");
				}
				
				URL resourceUrl = classLoader.findResource( resourceName);
				
				if (resourceUrl == null) {
					throw new ZarathudException( "Cannot find resource [" + resourceName + "]");
				}				
				
				// if it's not one of the main classes, it's a referenced class, and we deduce the artifact from the resource URL
				if (artifact == null) {
					String file = resourceUrl.getFile();
					String key = file.substring(0, file.indexOf('!'));
					artifact = urlToArtifactMap.get( key);
				}
				
				InheritanceData inheritenceData = AsmClassDepScanner.getInheritanceData( ensureInputStream(resourceUrl));
				Set<AnnotationTuple> annotationTuples = AsmClassDepScanner.getClassAnnotationMembers( ensureInputStream(resourceUrl));
				
				String superSuspect = inheritenceData.getSuperClass();
				AbstractEntity entry = null;
				
				 				
					if (superSuspect.equalsIgnoreCase( "java.lang.Enum")) {
						//TODO: artifact information missing in this setup 
						entry = acquireEnumEntity( className);						
						EnumEntity enumEntity = (EnumEntity) entry;
						ClassData enumData = AsmClassDepScanner.getClassData( ensureInputStream(resourceUrl));
						
						List<FieldData> data = enumData.getFieldData();
						if (
								data != null && 
								data.size() > 0
							) {
							Set<String> values = enumEntity.getValues();
							for (FieldData field : data) {
								String name = field.getName();
								if (name.equalsIgnoreCase( "$VALUES") == false) {
									values.add( name);
								}
							}							
						}												
						
					} 
					else {
						if (inheritenceData.isInterfaceClass()) {
							entry = acquireInterfaceEntity( className);						
						} 
						else {							
							entry = acquireClassEntity(className);
							ClassEntity classEntity = (ClassEntity) entry;							
							ClassData classData = AsmClassDepScanner.getClassData( ensureInputStream(resourceUrl));
							classEntity.setAccessModifier( AccessModifier.valueOf( classData.getAccessNature().toString()));
							classEntity.setStaticNature( classData.getStaticNature());
							classEntity.setAbstractNature( classData.getAbstractNature());
							classEntity.setSynchronizedNature( classData.getSynchronizedNature());
							
							if (superSuspect.equalsIgnoreCase("java.lang.Object") == false) {
								ClassEntity superEntry = acquireClassEntity(superSuspect);
								classEntity.setSuperType( superEntry);
							}
							
							// fields
							List<FieldData> data = classData.getFieldData();
							if (
									data != null && 
									data.size() > 0
								) {
								List<FieldEntity> fields = classEntity.getFields();
								for (FieldData fieldData : data) {
									FieldEntity fieldEntity = buildFieldEntry(fieldData);
									fieldEntity.setOwner(classEntity);									
									fields.add( fieldEntity);										
								}
								classEntity.setFields(fields);
							}
						}
						// annotations on class and interface level
						if (annotationTuples != null && annotationTuples.size() > 0) {
							Set<AnnotationEntity> annotations = ((AbstractClassEntity)entry).getAnnotations();
							for (AnnotationTuple tuple : annotationTuples) {
								AnnotationEntity annotation = acquireAnnotation(tuple);								
								annotations.add( annotation);
							}																	
						}
						
						
						// we might get a hit on the generic entity itself, which needs to be set as generic.. strange, but true. 						
						if (resourceName.startsWith( genericEntityResoureceName)) {
							AbstractClassEntity baseEntity = (AbstractClassEntity) entry;
							baseEntity.setGenericNature(true);
						}
						
						List<String> interfaces = inheritenceData.getInterfaces();
						if (
								(interfaces != null) &&
								(interfaces.size() > 0)
							) {
							
							if (interfaces.contains( genericEntityResoureceName)) {								
								((AbstractClassEntity) entry).setGenericNature(true);
							}
							Set<InterfaceEntity> interfaceEntities = new HashSet<InterfaceEntity>();
							for (String interfaceName : interfaces) {
								InterfaceEntity acquiredInterfaceEntity = acquireInterfaceEntity( interfaceName.replace( "/", "."));
								
								interfaceEntities.add( acquiredInterfaceEntity);
							}

							if (entry instanceof ClassEntity) {
								((ClassEntity) entry).getImplementedInterfaces().addAll( interfaceEntities);
							} else {
								((InterfaceEntity) entry).getSuperInterfaces().addAll(interfaceEntities);
							}
						}		
						// methods 
						List<MethodData> methodList = AsmClassDepScanner.getMethodData( ensureInputStream( resourceUrl));
						if (methodList != null && methodList.size()>0) {
							Set<MethodEntity> entries = ((AbstractClassEntity) entry).getMethods();
							for (MethodData methodData : methodList) {
								entries.add( buildMethodEntry( (AbstractClassEntity) entry, methodData));
							}							
						}
						
					}
				
				
				entry.setArtifact(artifact);
				entry.setScannedFlag(true);
				//addEntry( entry);
				return entry;
				
			} catch (AsmClassDepScannerException e) {
				String msg = "cannot analyze class [" + className + "] as " + e;
				log.error( msg, e);
				throw new ZarathudException(msg, e);
			} catch (IOException e) {
				String msg = " cannot load class bytes of class [" + className + "] as " + e;
				log.error( msg, e);
				throw new ZarathudException(msg, e);
			} 	
		
		
	}
	
	
	private InputStream ensureInputStream( URL url) throws IOException {
		return url.openStream();
	}
	
	private AnnotationEntity acquireAnnotation( AnnotationTuple tuple) throws ZarathudException {
		String desc = tuple.getDesc();
		AnnotationEntity annotation = acquireAnnotation( desc);
		Map<String, Object> members = tuple.getValues();
		if (members != null && members.size() > 0) {
		
			Map<String, AnnotationValueContainer> complexValues = annotation.getMembers();
			
			for (Entry<String, Object> member : members.entrySet()) {
				String key = member.getKey();
				Object value = member.getValue();
				AnnotationValueContainer container = acquireAnnotationValueContainer(value);
				complexValues.put( key, container);
			}						
		}
		return annotation;
	}

	private AnnotationValueContainer acquireAnnotationValueContainer( Object value) throws ZarathudException {
		AnnotationValueContainer result = AnnotationValueContainer.T.create();
		
		if (value instanceof String) {
			result.setContainerType( AnnotationValueContainerType.s_string);
			result.setSimpleStringValue( (String) value);
			return result;
		}

		if (value instanceof Boolean) {
			result.setContainerType( AnnotationValueContainerType.s_boolean);
			result.setSimpleBooleanValue( (Boolean) value);
			return result;
		}

		if (value instanceof Integer) {
			result.setContainerType( AnnotationValueContainerType.s_int);
			result.setSimpleIntegerValue( (Integer) value);
			return result;
		}
		
		if (value instanceof Long) {
			result.setContainerType( AnnotationValueContainerType.s_long);
			result.setSimpleLongValue( (Long) value);
			return result;
		}

		if (value instanceof Float) {
			result.setContainerType( AnnotationValueContainerType.s_float);
			result.setSimpleFloatValue( (Float) value);
			return result;
		}
		
		if (value instanceof Double) {
			result.setContainerType( AnnotationValueContainerType.s_double);
			result.setSimpleDoubleValue( (Double) value);
			return result;
		}
		if (value instanceof Date) {
			result.setContainerType( AnnotationValueContainerType.s_date);
			result.setSimpleDateValue( (Date) value);
			return result;
		}
		
		if (value instanceof Collection<?>) {
			@SuppressWarnings("unchecked")
			Collection<Object> collection = (Collection<Object>) value;
			result.setContainerType( AnnotationValueContainerType.collection);
			List<AnnotationValueContainer> contents = result.getChildren();
			for (Object obj : collection) {
				contents.add( acquireAnnotationValueContainer(obj));
			}			
			return result;
		}
	
		if (value instanceof AnnotationTuple) {
			AnnotationEntity annotation = acquireAnnotation( (AnnotationTuple) value);
			result.setContainerType( AnnotationValueContainerType.annotation);
			result.setAnnotation(annotation);
			return result;
		}
		
		if (value instanceof Type) {
			result.setContainerType( AnnotationValueContainerType.s_string);
			Type valueType = (Type) value;
			result.setSimpleStringValue( valueType.toString());
			return result;
		}
		
		log.warn( "type [" + value.getClass() + "] is not supported");
		return null;	
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#isEnum(java.lang.String)
	 */
	@Override
	public boolean isEnum( String fullName) {
		AbstractEntity entry = nameToEntryMap.get( fullName);
		if (entry == null) 
			return false;
		return ( entry instanceof EnumEntity);
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#isGeneric(java.lang.String)
	 */
	@Override
	public boolean isGeneric( String fullName) {
		AbstractEntity entry = nameToEntryMap.get( fullName);
		if (entry == null) 
			return false;
		if ( entry instanceof AbstractClassEntity) {
			AbstractClassEntity classEntry = (AbstractClassEntity) entry;
			return classEntry.getGenericNature();
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#analyzeGenericity(java.lang.String)
	 */
	@Override
	public boolean analyzeGenericity( String fullName) throws UnsupportedRequestTypeException {
		AbstractEntity entry = nameToEntryMap.get( fullName);
		
		if (
				(entry == null) ||
				(entry instanceof AbstractClassEntity == false)
			) {
			return false;
		}
			
		Boolean genericity = ((AbstractClassEntity)entry).getGenericNature();
		// if not null, it has been determined already
		if (genericity != null) {
			return genericity;
		}
		
		// class
		if (entry instanceof ClassEntity) {
			ClassEntity classEntry = (ClassEntity) entry;
		
					
			// if not, we must find out... 
			// check our super class
			ClassEntity superType = classEntry.getSuperType();
			
			if (superType != null) {
				String superClassName = superType.getName();
				genericity = analyzeGenericity(superClassName);
			}
			// not generic? so it might be an interface it implements.. 
			if (
					(genericity == null) ||
					(genericity == false)
				){ 		
				// check our interfaces
				Set<InterfaceEntity> interfaces = classEntry.getImplementedInterfaces();
				if (interfaces != null) {
					for (InterfaceEntity interfaceEntity : interfaces) {
						genericity = analyzeGenericity(interfaceEntity.getName());
						if (genericity == true)
							break;					 
					}
				}
			}
				
			if (genericity == null) {
				genericity = false;
			}			
			classEntry.setGenericNature( genericity);
			return genericity;
					
		} else 
			// interfaces 
			if (entry instanceof InterfaceEntity){ 
				InterfaceEntity interfaceEntity = (InterfaceEntity) entry;
				Set<InterfaceEntity> interfaces = interfaceEntity.getSuperInterfaces();
				if (interfaces != null) {
					for (InterfaceEntity superInterfaceEntity : interfaces) {
						genericity = analyzeGenericity(superInterfaceEntity.getName());
						if (genericity == true)
							break;					 
					}
				}
				if (genericity == null) {
					genericity = false;
				}			
				interfaceEntity.setGenericNature( genericity);
				return genericity;								
			} 
			else if (entry instanceof AbstractClassEntity){ 
				return false;
			}
			else { 
				throw new UnsupportedRequestTypeException( "entity [" + entry.getClass().getName() + "] isn't supported. Ever");
			}
		
						
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#analyzeGenericity()
	 */
	@Override
	public void analyzeGenericity() throws UnsupportedRequestTypeException {
		for (Entry<String, AbstractEntity> entry : nameToEntryMap.entrySet()) {
			analyzeGenericity( entry.getKey());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#getEnums()
	 */
	@Override
	public Collection<EnumEntity> getEnums() {
		Collection<EnumEntity> result = new HashSet<EnumEntity>();
		for (AbstractEntity entry : nameToEntryMap.values()) {
			if (entry instanceof EnumEntity) {
				result.add( (EnumEntity) entry);
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#getGenericEntities()
	 */
	@Override
	public Collection<AbstractClassEntity> getGenericEntities() {
		Collection<AbstractClassEntity> result = new HashSet<AbstractClassEntity>();
		for (AbstractEntity entry : nameToEntryMap.values()) {
			if (entry instanceof AbstractClassEntity) {
				AbstractClassEntity classEntry = (AbstractClassEntity) entry;
				if (classEntry.getGenericNature())
					result.add( classEntry);
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#addArtifactBinding(com.braintribe.devrock.zarathud.scanner.ScannerResult, com.braintribe.model.zarathud.Artifact)
	 */
	@Override
	@SuppressWarnings("unused")
	public void addArtifactBinding( ScannerResult result, Artifact artifact) throws ZarathudException {			
		artifact.setGwtModule( result.getModuleName());
		for (String className : result.getClasses()) {
			nameToBindingArtifact.put( className, artifact);
		}							
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#addArtifactBinding(com.braintribe.devrock.zarathud.scanner.ScannerResult, java.io.File)
	 */
	@Override
	public void addArtifactBinding( ScannerResult scannerResult, File jar) throws ZarathudException {
		Artifact artifact = jarToBindingArtifact.get( jar.getAbsolutePath());
		if (artifact != null)
			addArtifactBinding( scannerResult, artifact);
		else {
			String msg = "no artifact associated with jar [" + jar.getAbsolutePath() + "]";
			log.warn(msg);
		}
	}
	
		
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#addArtifactToRelevantEntities(com.braintribe.model.zarathud.Artifact)
	 */
	@Override
	public void addArtifactToRelevantEntities( Artifact artifact) {
		for (AbstractEntity entity : nameToEntryMap.values()) {
			if (
					entity.getDefinedLocal() == true ||
					entity.getDirectDependency() == true
				) {
				entity.setArtifact(artifact);
			}
		}
	}
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#getRegistryEntry(java.lang.String)
	 */
	@Override
	public AbstractEntity getRegistryEntry( String fullName) throws ZarathudException {
		AbstractEntity entry = nameToEntryMap.get( fullName);
		if (entry == null) {
			String msg = "cannot find a registry entry for [" + fullName + "]";
			log.error( msg, null);
			throw new ZarathudException(msg);
		}
		return entry;
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#getModuleName(java.lang.String)
	 */
	@Override
	public String getModuleName( String fullName) throws ZarathudException {
		
		return getRegistryEntry( fullName).getModuleName();
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#getBindingArtifact(java.lang.String)
	 */
	@Override
	public Artifact getBindingArtifact( String fullName) throws ZarathudException {
		
		return getRegistryEntry( fullName).getArtifact();
	}
	
	/**
	 * builds a {@link FieldEntity} from the {@link FieldData} as defined by the {@link AsmClassDepScanner}
	 * @param data - the {@link FieldData}
	 * @return - the corresponding {@link FieldEntity}
	 */
	private FieldEntity buildFieldEntry( FieldData data) {
		FieldEntity fieldEntity = create( FieldEntity.T);
		fieldEntity.setName( data.getName());
		String desc = data.getDesc();
		fieldEntity.setDesc( desc);
		String signature = data.getSignature();
		// if no signature comes from ASM, we must generate it
		if (signature == null) {
			signature = toClassName(desc);
		}
		fieldEntity.setSignature( signature);
		fieldEntity.setInitializerPresentFlag( data.getIntializer() != null);
		fieldEntity.setType( analyzeDesc( data.getDesc(), null));
		return fieldEntity;
	}
	/**
	 * builds a {@link MethodEntity} from the {@link MethodData} as defined by the {@link AsmClassDepScanner}
	 * @param data -the {@link MethodData}
	 * @return - the corresponding {@link MethodEntity}
	 * @throws ZarathudException - arrgh
	 */
	private MethodEntity buildMethodEntry( AbstractClassEntity owner, MethodData data) throws ZarathudException {
		MethodEntity method = create( MethodEntity.T);
		method.setName(data.getMethodName());
		method.setDesc( data.getDesc());
		method.setSignature( data.getSignature());
		method.setOwner(owner);
		
		
		MethodSignature ms;
		if (data.getSignature() != null) {
			ms = extractMethodSignature( data.getSignature());
		} else {
			ms = extractMethodSignature( data.getDesc());
		}	
		if (ms == null) {
			log.warn("cannot process [" + owner.getName() + "]'s function [" + data.getMethodName() + "] signature : [" + data.getSignature() + "]");
			return method;
		}
		// 
		String returnValue = data.getReturnType();
		if (returnValue != null) {
			method.setReturnType( ms.returnType);			
		}
		
		if (ms.argumentTypes != null) {
			method.getArgumentTypes().addAll( ms.argumentTypes);
		}
		
		// access modifiers and natures 
		method.setAccessModifier( AccessModifier.valueOf( data.getAccessNature().toString()));
		method.setStaticNature( data.getStaticNature());
		method.setAbstractNature( data.getAbstractNature());
		method.setSynchronizedNature( data.getSynchronizedNature());

		// exceptions 
		Collection<String> exceptions = data.getExceptions();
		if (exceptions != null && exceptions.size() > 0) {
			Set<ClassEntity> exceptionEntities = method.getExceptions(); 			
			for (String exception : exceptions) {
				exception = exception.replace("/", ".");
				exceptionEntities.add( acquireClassEntity(exception));
			}
		}		
		// annotations
		Set<String> annotationNames = data.getAnnotations();
		if (annotationNames != null && annotationNames.size() > 0) {
			Set<AnnotationEntity> annotations = method.getAnnotations();			
			for (String annotationName : annotationNames) {
				AnnotationEntity annotation = acquireAnnotation( annotationName);
				annotations.add( annotation);				
			}
		}
		return method;
	}
	
	
	

	/**
	 * acquire an {@link EnumEntity}
	 * @param name
	 * @return
	 */
	private EnumEntity acquireEnumEntity( String name) {
		EnumEntity entry = (EnumEntity) nameToEntryMap.get( name);
		if (entry == null) {
			entry = create( EnumEntity.T);
			entry.setName(name);
			entry.setScannedFlag(false);
			addEntry(entry);
		}
		return entry;
	}

	/**
	 * acquire a {@link ClassEntity}: if not found, it is created as a shell
	 * @param name - the name of of {@link ClassEntity} as a {@link String}
	 * @return - the {@link ClassEntity}, either found or created 
	 */
	private ClassEntity acquireClassEntity( String name) {
		ClassEntity entry = (ClassEntity) nameToEntryMap.get( name);
		if (entry == null) {
			entry = create( ClassEntity.T);
			entry.setName(name);
			entry.setScannedFlag( false);
			addEntry(entry);
		}
		return entry;
	}
		
	
	/**
	 * acquire an {@link InterfaceEntity}: if not found, it is created as a shell 
	 * @param name - the name of the {@link InterfaceEntity} as a {@link String}
	 * @return - the {@link InterfaceEntity}, either found or created
	 */
	private InterfaceEntity acquireInterfaceEntity( String name) {
		InterfaceEntity entry = (InterfaceEntity) nameToEntryMap.get( name);
		if (entry == null) {
			entry = create( InterfaceEntity.T);
			entry.setName(name);
			entry.setScannedFlag(false);
			addEntry(entry);
		}
		return entry;
	}

	/**
	 * acquire an {@link AnnotationEntity} : if not found, it is resolved and scanned
	 * @param annotationName - the name as it appears in the class/method data 
	 * @return - the {@link AnnotationEntity}, either found or created
	 * @throws ZarathudException - thrown if scanning goes wrong
	 */
	private AnnotationEntity acquireAnnotation( String annotationName) throws ZarathudException {
		String className = annotationName.substring(1, annotationName.length()-1);
		 
		AnnotationEntity annotation = (AnnotationEntity) nameToEntryMap.get( className);
		if (annotation == null) {
			annotation = analyzeAnnotation( className);
			annotation.setScannedFlag(false);
		}
		return annotation;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.devrock.zarathud.extracter.ExtractionRegistry#getUnscannedEntities()
	 */
	@Override
	public Set<String> getUnscannedEntities() {
		Set<String> result = new HashSet<String>();
		for (Entry<String, AbstractEntity> entry : nameToEntryMap.entrySet()) {
			AbstractEntity entity = entry.getValue();
			if (Boolean.TRUE.equals( entity.getScannedFlag())) {
				continue;			
			}
			result.add( entity.getName());
		}
		return result;
			
	}
	
	private <T extends GenericEntity> T create( EntityType<T> entityType) {
		if (session != null) {
			return session.create(entityType);
		}
		return entityType.create();
	}
	
	/**
	 * safely extract a class name from a desc
	 * @param desc - the signature or desc
	 * @return - a signature.
	 */
	private String toClassName( String desc) {
		if (desc.length() > 1) {
			int startIndex = 0;
			if (desc.startsWith( "L"))
				startIndex = 1;
			else
				startIndex = 0;
			int endIndex = desc.length();
			if (desc.endsWith( ";"))
				endIndex = desc.length() -1;
			else 
				endIndex = desc.length();
			return desc.substring( startIndex, endIndex).replace('/', '.');
		}
		return null;
	}
	
	/**
	 * extract the method signature into return type (fully qualified) and argumenttypes (fully qualfied)
	 * @param signature - the desc 
	 * @return - an instance of the {@link MethodSignature}
	 */
	public  MethodSignature extractMethodSignature(String signature) {
		//(Ljava/util/List<Lcom/braintribe/devrock/test/zarathud/four/FourParameter;>;Ljava/util/List<Lcom/braintribe/devrock/test/zarathud/two/TwoClass;>;)Ljava/util/List<Ljava/lang/String;>;
		//<T:Ljava/lang/Object;>()TT;                                                                                        
		if (signature == null) {
			return null;
		}
		Map<String, AbstractClassEntity> ttypes = new HashMap<>();		
		
		MethodSignature methodSignature = new MethodSignature();
		// template definition 
		if (signature.startsWith( "<")) {
			int endGen = signature.indexOf( ">");
			ttypes.putAll(extractGenericParameters( signature));	
			signature = signature.substring( endGen+1);		
		}
		// arguments 
		if (signature.startsWith( "(")) {
			int index = signature.indexOf(')');
			String argumentPart = signature.substring(1, index);
			String remainder = signature.substring( index+1);			 
			String returnTypeDesc = remainder;
			AbstractEntity returnType = null;
			if (returnTypeDesc.startsWith( "T") && ttypes.containsKey( returnTypeDesc)) {
				returnType = ttypes.get(returnTypeDesc);
			}
			else if (returnTypeDesc.length() >=2 && returnTypeDesc.charAt(1) != 'L') {
				// fake type 
				returnType = createParameter(returnTypeDesc.substring(0, 1), "Ljava/lang/Object");
			}
			else {
				// analyze desc and turn it into a realthing
				returnType = analyzeDesc( returnTypeDesc, null);
			}
			methodSignature.returnType = returnType;
			
			
			StringBuilder builder = new StringBuilder();
							
			int generics = 0;
			methodSignature.argumentTypes = new ArrayList<>();
			for (int i = 0; i < argumentPart.length(); i++) {
				char c = argumentPart.charAt(i);
				if (c == '<') {
					generics++;
				}
				if (c == '>') {
					generics--;
				}
				builder.append( c);
				if (c == ';' && generics == 0) {
					// closing
					// simple, toplevel argument desc
					String argumentDesc = builder.toString();
					AbstractEntity argumentType = null;
					if (argumentDesc.startsWith( "T") && ttypes.containsKey( argumentDesc)) {
						argumentType = ttypes.get(argumentDesc);
					}
					else {
						argumentType = analyzeDesc( argumentDesc, null);
					}
					methodSignature.argumentTypes.add(argumentType);
					builder = new StringBuilder();
				}
			}
			
			return methodSignature;		
		}
		return methodSignature;
	}
	
	
	private Map<String, AbstractClassEntity> extractGenericParameters( String signature) {
		int endGen = signature.indexOf( ">");
		StringBuilder builder = new StringBuilder();
		boolean escaped = false;
		List<String> params = new ArrayList<>();
		for (int i = 1; i < endGen; i++) {
			char c = signature.charAt(i);
			builder.append(c);
			
			if (c == '<') {
				escaped = true;
			}
			else if (c == '>') {
				escaped = false; 
			}
			else if (c == ';' && !escaped) {
				params.add( builder.toString());
				builder = new StringBuilder();
			}			
		}
					
		Map<String, AbstractClassEntity> result = new HashMap<>();
		for (String param : params) {
			
			String keyName = param.substring( 0, param.indexOf( ':'));			
			String typeName = param.substring( keyName.length()+2); 
			if (	typeName.startsWith( ":") ||
					typeName.startsWith( "+") || 
					typeName.startsWith("-")
				) {
				typeName = typeName.substring(1);				                                                    
			}			
			AbstractClassEntity ttype = createParameter(keyName, typeName);		
			result.put( "T"+keyName+";", ttype);
		}
		return result;
	}

	private AbstractClassEntity createParameter(String keyName, String typeName) {
		AbstractClassEntity ttype = create( AbstractClassEntity.T);
		ttype.setName( keyName);
		ttype.setParameterNature(true);
		ttype.setArtifact( acquireJavaArtifact());		           	
		analyzeDesc( typeName, ttype);
		return ttype;
	}
	
	private static List<String> extractMultipleSignatures( String desc) {
		List<String> result = new ArrayList<>();
		StringBuilder builder	 = new StringBuilder();
		int generics = 0;
		for (int i = 0; i < desc.length(); i++) {
			char c = desc.charAt(i);
			if (c == '<') {
				generics++;
			}
			if (c == '>') {
				generics--;
			}
			builder.append( c);
			if (c == ';' && generics == 0) {
				// closing
				// simple, toplevel argument desc
				String argumentDesc = builder.toString();			
				result.add( argumentDesc);
				builder = new StringBuilder();
			}
		}
		return result;	
	}
	
	/**
	 * turns the desc of a type into an qualified type 
	 * @param desc - the desc as extracted 
	 * @return
	 */
	public AbstractEntity analyzeDesc(String desc, AbstractEntity owner) {
		/*
		 * Ljava/util/List<Lcom/braintribe/devrock/test/zarathud/four/FourParameter;>;
		 * Ljava/util/Map<Lcom/braintribe/devrock/test/zarathud/four/FourParameter;Lcom/braintribe/devrock/test/zarathud/four/FourParameter;>;
		 * Lcom/braintribe/devrock/test/zarathud/four/FourParameter<Lcom/braintribe/devrock/test/zarathud/four/ParameterT;>;
		 * [Lcom/braintribe/devrock/test/zarathud/four/FourParameter;
		 * "<E:Ljava/lang/Object;>Ljava/util/List<TE;>;"
		 */
		
		// simple type, no generics, no array, on type only
		if (!desc.contains( ">") && !desc.contains( "[")) {
			AbstractEntity type;
			if (desc.equalsIgnoreCase("V")) {
				type = getEntry( "void");
				if (type == null) {
					type = create( AbstractEntity.T);
					type.setDesc(desc);
					type.setScannedFlag( true);
					type.setName("void");
					type.setArtifact( acquireJavaArtifact());
					addEntry( type);
				}
			}
			else {
				String signature;
				if (SimpleTypeRegistry.isCodeForSimpleType(desc)) {
					 signature = SimpleTypeRegistry.getSimpleTypeFromDesc(desc);				
				}
				else {
					signature = toClassName( desc);
				}
				type = acquireClassResource(signature, true);
				if (type.getDesc() == null) {
					type.setDesc( desc);
				}								
			}
			if (owner != null && owner instanceof AbstractClassEntity) {
				((AbstractClassEntity) owner).getParameterization().add(type); 
			}
			return type;
		}
		
		// array 
		if (desc.startsWith( "[")) {
			AbstractClassEntity collection = create( AbstractClassEntity.T);
			collection.setArrayNature(true);
			collection.setName("array");
			collection.setArtifact( acquireJavaArtifact());
			String arrayElementDesc;
			if (desc.startsWith( "[[")) {
				arrayElementDesc = desc.substring(2);
				collection.setTwoDimensionality(true);
			}
			else {
				arrayElementDesc = desc.substring(1);
				collection.setTwoDimensionality(false);
			}
			// 
			AbstractEntity elementType = analyzeDesc(arrayElementDesc, null);
			collection.getParameterization().add(elementType);
			if (owner != null && owner instanceof AbstractClassEntity) {
				((AbstractClassEntity) owner).getParameterization().add( collection); 
			}
			return collection;			
		}
		
		// type with generics 
		// 
		if (desc.matches( "<.*:.*")) {
			System.out.println("detected..");
			// cannot reuse type as it's parameterized..?? 
			
			return null;
		}
		// type with parameter
		int startGenChar = desc.indexOf( '<');
		int endGenChar = desc.lastIndexOf( ">");
		String containerDesc = desc.substring(0,  startGenChar);
		String containerSignature = toClassName(containerDesc);
				
		AbstractEntity containerType = acquireClassResource( containerSignature, true);
		if (containerType == null) {
			containerType = create( AbstractClassEntity.T);
			containerType.setDesc(containerDesc);
			containerType.setName(containerSignature);
			containerType.setScannedFlag( true);
			containerType.setArtifact( acquireArtifact( containerSignature));
		}
		if (owner != null && owner instanceof AbstractClassEntity) {
			((AbstractClassEntity) owner).getParameterization().add( containerType); 
		}
		String containedDesc = desc.substring( startGenChar + 1, endGenChar);
		// multiple types on this level are possible.. 
		// 
		List<String> containedDescs = extractMultipleSignatures(containedDesc);
		for (String d : containedDescs) {			
			analyzeDesc( d, containerType);
		}
				
		return containerType;
	}
	
	private boolean isJavaType( String signature) {
		//TODO : find a better way to do that.. 
		if (
				signature.startsWith( "java.") ||
				signature.startsWith( "java/") ||
				signature.startsWith( "javax.") ||
				signature.startsWith( "javax/")) {
			return true;
		}
		return false;
	}

	@Override
	public Artifact acquireArtifact(String signature) {
		if (isJavaType(signature)) {
			return acquireJavaArtifact();
		}
		else if (signature.equalsIgnoreCase("void")) {
			return acquireJavaArtifact();
		}
		return acquireUnknownSourceArtifact();
	}

	private Artifact acquireJavaArtifact() {
		if (javaArtifact == null) {
			javaArtifact = create( Artifact.T);
			javaArtifact.setArtifactId("rt");
		}
		return javaArtifact;
	}

	private Artifact acquireUnknownSourceArtifact() {
		if (unknownArtifact == null) {
			unknownArtifact = create( Artifact.T);
			unknownArtifact.setArtifactId("unknown");
		}
		return unknownArtifact;	
	}

	
	

	
	
}
