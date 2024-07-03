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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Required;
import com.braintribe.devrock.zarathud.ContextLogger;
import com.braintribe.logging.Logger;
import com.braintribe.model.zarathud.ContextMessageClassification;
import com.braintribe.model.zarathud.ValidationContext;
import com.braintribe.model.zarathud.ValidationContextMessage;
import com.braintribe.model.zarathud.data.AbstractClassEntity;
import com.braintribe.model.zarathud.data.AbstractEntity;
import com.braintribe.model.zarathud.data.ClassEntity;
import com.braintribe.model.zarathud.data.FieldEntity;
import com.braintribe.model.zarathud.data.MethodEntity;


/**
 * helper class that creates message for the validation run - on a single artifact
 * 
 * @author pit
 *
 */
public class ValidationContextLogger extends ContextLogger {

	private static Logger log = Logger.getLogger(ValidationContextLogger.class);
	private ValidationContext validationContext;
	
	@Required
	public void setValidationContext(ValidationContext validationContext) {
		this.validationContext = validationContext;
	}
	/*
	 * 
	 * validating messages logs
	 *   
	 */	
	public void logInitializedField(FieldEntity field) {
		ClassEntity owner = field.getOwner();		
		String msg =String.format("Class [%s]'s field [%s] has an initializer", owner.getName(), field.getName());
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.FieldInitialized);		
	}
	
	public void logMissingSetter( AccessTuple tuple) {
		String msg= String.format("[%s] [%s]'s field [%s] has no setter",  zarathudEntityToType(tuple.getOwner()), tuple.getOwner().getName(), tuple.getSuffix());
		log.debug( msg);
		addMessage( tuple.getOwner(), msg, ContextMessageClassification.MissingSetter);
	}
	public void logMissingGetter( AccessTuple tuple) {
		String msg= String.format("[%s] [%s]'s field [%s] has no getter",  zarathudEntityToType(tuple.getOwner()), tuple.getOwner().getName(), tuple.getSuffix());
		log.debug( msg);
		addMessage( tuple.getOwner(), msg, ContextMessageClassification.MissingGetter);
	}
	 
	public void logGetterHasArguments( MethodEntity method) {
		AbstractEntity owner = method.getOwner();
		String msg = String.format("[%s] [%s]'s [%s] getter has arguments [%s]",zarathudEntityToType(owner), owner.getName(), method.getName(), method.getArgumentTypes().toString());
		//String msg= zarathudEntityToType(owner) + " [" + owner.getName() + "]'s [" + method.getName() + "]  has arguments : [" + method.getArgumentTypes().toString() + "]";
		log.debug( msg);
		addMessage( method.getOwner(), msg, ContextMessageClassification.WrongSignature);
	}
	
	public void logGetterReturnsVoid( MethodEntity method) {
		AbstractEntity owner = method.getOwner();
		String msg = String.format("[%s] [%s]'s [%s] returns void", zarathudEntityToType(owner), owner.getName(), method.getName());
		//String msg= zarathudEntityToType(owner) + " [" + owner.getName() + "]'s [" + method.getName() + "] returns void";
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.WrongSignature);
	}
	
	
	public void logSetterHasNoArguments( MethodEntity method) {
		AbstractEntity owner = method.getOwner();
		String msg = String.format("[%s] [%s]'s [%s] has no arguments", zarathudEntityToType(owner), owner.getName(), method.getName());
		//String msg= zarathudEntityToType(owner) + " [" + owner.getName() + "]'s [" + method.getName() + "] has no arguments";
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.WrongSignature);
	}
	
	public void logSetterHasTooManyArguments( MethodEntity method) {
		AbstractEntity owner = method.getOwner();
		String msg = String.format("[%s] [%s]'s [%s] getter has too many arguments [%s]",zarathudEntityToType(owner), owner.getName(), method.getName(), method.getArgumentTypes().toString());
		//String msg= zarathudEntityToType(owner) + " [" + owner.getName() + "]'s [" + method.getName() + "] has too many arguments : [" + method.getArgumentTypes().toString() + "]";
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.WrongSignature);
	}
	
	public void logSetterDoesNotReturnVoid( MethodEntity method){
		AbstractEntity owner = method.getOwner();
		String msg = String.format( "[%s] [%s]'s [%s] returns type [%s]", zarathudEntityToType(owner), owner.getName(), method.getName(), method.getReturnType());
		//String msg= zarathudEntityToType(owner) + " [" + owner.getName() + "]'s [" + method.getName() + "] returns value : [" + method.getReturnType() + "]";
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.WrongSignature);
	}
		
	public void logSetterGetterTypesDoNotMatch( AccessTuple tuple) {
		AbstractEntity owner = tuple.getOwner();
		String returnType = getSimpleTypeForDesc( tuple.getGetter().getReturnType().getDesc());
		String msg = String.format( "[%s] [%s]'s has a type mismatch : [%s] -> [%s] vs [%s] -> [%s]", zarathudEntityToType(owner), owner.getName(), tuple.getGetter().getName(), returnType, tuple.getSetter().getName(), tuple.getSetter().getArgumentTypes());
		//String msg= "types of " + zarathudEntityToType(owner) + " [" + owner.getName() + "] do not match: [" + tuple.getGetter().getName() +"] -> [" +returnType + "], [" + tuple.getSetter().getName() + "->" + tuple.getSetter().getArgumentTypes() + "]";
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.TypeMismatch);
	}
	
	public void logTypeNotFound(AccessTuple tuple, String signature) {
		AbstractEntity owner = tuple.getOwner();
		String msg = String.format( "[%s] [%s]'s field [%s]'s type is not found [%s]", zarathudEntityToType(owner), owner.getName(), tuple.getSuffix(), signature);
		//String msg= "types of " + zarathudEntityToType(owner) + " [" + owner.getName() + "], field [ " + tuple.getSuffix() + "]'s type is not found -> [" + signature + "]";
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.InvalidTypes);
	}
	public void logCollectionElementIsCollectionType( AccessTuple tuple, String collectionType,  String elementType) {
		AbstractEntity owner = tuple.getOwner();	
		String msg = String.format("[%s] [%s]'s property [%s]'s collection of type [%s] has an element type that is itself a collection type: %s", zarathudEntityToType( owner), owner.getName(), tuple.getSuffix(), collectionType, elementType);		
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.CollectionInCollection);
	}
	
	public void logMethodIsMissingAnnotation( MethodEntity method, String annotation,String signature) {
		AbstractEntity owner = method.getOwner();
		String msg = String.format("[%s] [%s]'s method [%s] with relevant type [%s] requires annotation [%s]", zarathudEntityToType(owner), owner.getName(),  method.getName(), signature, annotation);
		//String msg= zarathudEntityToType(owner) + " [" + owner.getName() + "]'s [" + method.getName() + "] with relevant type [" + signature +"] requires an annotation [" + annotation + "]";
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.MissingAnnotation);
	}
	
	public void logTypeMismatchInPropertyHierarchy( AbstractClassEntity entity, AccessTuple base, AccessTuple collision) {
		String msg = String.format("Mismatched type hierarchy detected in [%s], property [%s] - types [%s] vs [%s]", entity.getName(), base.getSuffix(), base.getOwner().getName(), collision.getOwner().getName());
		log.debug( msg);
		addMessage( entity, msg, ContextMessageClassification.TypeMismatchInPropertyHierarchy);
	}
	
	public void logUseOfProhibitedMethod( MethodEntity method) {
		AbstractEntity owner = method.getOwner();
		String msg= String.format("[%s] [%s]'s method [%s] is a prohibited method", zarathudEntityToType(owner), owner.getName(), method.getName());
		//String msg= zarathudEntityToType(owner) + " [" + owner.getName() + "]'s [" + method.getName() + "] is a prohibited method";
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.InvalidMethods);
	}
	
	public void logUseOfInvalidMethod( MethodEntity method) {
		AbstractEntity owner = method.getOwner();
		String msg = String.format( "[%s] [%s]'s method [%s] is not an allowed method", zarathudEntityToType(owner),owner.getName(), method.getName());
		//String msg= zarathudEntityToType(owner) + " [" + owner.getName() + "]'s [" + method.getName() + "] is not an allowed method";
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.InvalidMethods);
	}
	
	public void logNotGenericEntity( AbstractClassEntity owner) {
		String msg = String.format( "[%s] [%s] is not an GenericEntity and is not allowed to appear in a model type artifact", zarathudEntityToType(owner), owner.getName());
		//String msg= zarathudEntityToType(owner) + " [" + owner.getName() + "]'s is not a GenericEntity and not allowed in a model type artifact";
		log.debug( msg);
		addMessage( owner, msg, ContextMessageClassification.InvalidTypes);
	}
	
	public void logMultipleIdProperties( AbstractClassEntity owner, Set<AccessTuple> idProperties) {
		String msg = String.format( "[%s] [%s] has an invalid number of id properties", zarathudEntityToType( owner), owner.getName());
		//String msg = zarathudEntityToType( owner) + "[" + owner.getName() + "] has an invalid number of id properties :";
		for (AccessTuple tuple : idProperties) {
			msg += String.format( " field [%s] of [%s] [%s]",tuple.getSuffix(), zarathudEntityToType( tuple.getOwner()), tuple.getOwner().getName()); 
		}
		addMessage( owner, msg, ContextMessageClassification.MultipleIdProperties);
	}
	
	public void logMethodIdPropertyMissing(MethodEntity entity) {
		AbstractClassEntity owner = entity.getOwner();
		String msg = String.format("[%s] [%s]'s functiokn [%s] has a missing [%s]",zarathudEntityToType( owner), owner.getName(), entity.getName(), ValidationTokens.ANNO_ID_PROPERTY);
		//String msg = zarathudEntityToType( owner) + "[" + owner.getName() + "]'s function [" + entity.getName() + "] has a missing [" + ValidationTokens.ANNO_ID_PROPERTY + "]";
		
		addMessage( owner, msg, ContextMessageClassification.MissingIdProperty);
	}
	
	public void logTransientInInstantiableInterface( AbstractClassEntity owner) {
		String msg = String.format( "[%s] [%s] has an annotation [%s] and methods show [%s]", zarathudEntityToType(owner), owner.getName(), ValidationTokens.ANNO_IMPLEMENT_ABSTRACT_PROPERTIES, ValidationTokens.ANNO_TRANSIENT);
		//String msg = zarathudEntityToType( owner) + "[" + owner.getName() + "] has an annotation [" + ValidationTokens.ANNO_IMPLEMENT_ABSTRACT_PROPERTIES + "] and methods show [" + ValidationTokens.ANNO_TRANSIENT + "]";
		addMessage( owner, msg, ContextMessageClassification.InvalidMethods);
	}
	
	public void logMissingIdPropertyForPersistence( AbstractClassEntity entity, AccessTuple tuple) {
		String msg;
		if (tuple == null) {
			msg=String.format("[%s] [%s] can be instantiated, yet has no id property -> unsuitable for persistence", zarathudEntityToType( entity), entity.getName());
		} 
		else {
			msg=String.format("[%s] [%s] can be instantiated, yet its property [%s] is of type [%s] which no id property -> unsuitable for persistence",zarathudEntityToType( entity), entity.getName(), tuple.getSuffix(), tuple.getGetter().getReturnType());
		}
		log.debug(msg);
		addMessage(entity, msg, ContextMessageClassification.MissingIdProperty);
	}
	
	public void logContainmentPropertyAlreadyReferenced( AbstractClassEntity owner, AbstractClassEntity suspect, AbstractClassEntity current){
		String msg = String.format("[%s]'s property type [%s] is also referenced by type [%s]", owner.getName(), suspect.getName(), current.getName());
		//String msg = "[" + owner.getName() + "]'s property type [" + suspect.getName() + "] is also referenced by type [" + current.getName() + "]";
		addMessage( owner, msg, ContextMessageClassification.ContainmentError);
	}
	
	public void logContainmentPropertyAlreadyReferencedPerSuperType( AbstractClassEntity owner, AbstractClassEntity suspect, AbstractClassEntity current){
		String msg = String.format("[%s]'s property type [%s] is a base type of a property referenced by type [%s]", owner.getName(), suspect.getName(), current.getName());
		//String msg = "[" + owner.getName() + "]'s property type [" + suspect.getName() + "] is a base type of a property referenced by type [" + current.getName() + "]";
		addMessage( owner, msg, ContextMessageClassification.ContainmentError);
	}
	public void logContainmentPropertyAlreadyReferencedPerInterface( AbstractClassEntity owner, AbstractClassEntity suspect, AbstractClassEntity current){
		String msg = String.format("[%s]'s property type [%s] is an interface type of a property referenced by type [%s]", owner.getName(), suspect.getName(), current.getName());
		//String msg = "[" + owner.getName() + "]'s property type [" + suspect.getName() + "] is an interface type of a property referenced by type [" + current.getName() + "]";
		addMessage( owner, msg, ContextMessageClassification.ContainmentError);
	}
	public void logContainmentPropertyAlreadyReferencedPerSuperInterface( AbstractClassEntity owner, AbstractClassEntity suspect, AbstractClassEntity current){
		String msg = String.format("[%s]'s property type [%s] is a super interface of a property referenced by type [%s]", owner.getName(), suspect.getName(), current.getName());
		//String msg = "[" + owner.getName() + "]'s property type [" + suspect.getName() + "] is an super interface of a property referenced by type [" + current.getName() + "]";
		addMessage( owner, msg, ContextMessageClassification.ContainmentError);
	}
	
	
	/*
	 * 
	 * helpers
	 *  
	 */	
	private void addMessage( AbstractEntity focus, String msg, ContextMessageClassification classification) {
		ValidationContextMessage message = session != null ? session.create( ValidationContextMessage.T) : ValidationContextMessage.T.create();
		message.setFocus(focus);
		message.setMessage(msg);
		message.setClassification(classification);
		validationContext.getMessages().add(message);
	}
	
	
	public static String [] dumpToString(ValidationContext context) {
		List<String> result = new ArrayList<String>( );
		List<ValidationContextMessage> messages = context.getMessages();
		if (messages == null || messages.size() == 0)
			return result.toArray( new String[0]);
		
		for (ValidationContextMessage message : messages) {
			result.add( message.getMessage());
		}
		return result.toArray( new String[0]);
	}
}
