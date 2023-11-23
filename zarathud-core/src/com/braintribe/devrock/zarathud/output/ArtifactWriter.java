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
package com.braintribe.devrock.zarathud.output;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.devrock.zarathud.extracter.filter.StandardFilter;
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
 * @author pit
 *
 */
public class ArtifactWriter {
	
	private static final String STRING = " ";
	private static final String DELIMITER_ARGUMENT = " ";
	private static final String DELIMITER_EXCEPTION = " ";
	private static final String TABS="\t\t\t\t\t\t\t\t\t\t\t\t";
	private boolean descOnly = false;	

	public void dump( Writer writer, Artifact artifact) throws IOException{
		dump(writer, artifact, null);
	}
	
	/**
	 * main method to be called 
	 * @param writer - the {@link Writer} to use 
	 * @param artifact - the {@link Artifact} to be dump
	 * @throws IOException - thrown if it can't write.. 
	 */
	public void dump( Writer writer, Artifact artifact, Predicate<AbstractEntity> processingFilter) throws IOException {
		
		Predicate<AbstractEntity> filter = null;
		if (processingFilter != null) {
			filter = processingFilter;
		}
		else {
			filter = new StandardFilter(artifact);
		}
		writeln( writer, artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + artifact.getVersion(), 0);
		
		String gwtModule = artifact.getGwtModule();
		if (gwtModule != null) {
			writeln( writer, "gwt module : " + gwtModule, 1);
		}
		
		Set<AbstractEntity> entries = artifact.getEntries();
		writeln( writer, "" + entries.size() + " classes or interfaces", 1);
		
		
		entries.stream().filter( filter).forEach( s -> { 
				try {
					dump( writer, s, 1);
				} catch (IOException e) {
					throw new IllegalStateException("cannot dump entity", e);
				}
			});
		
		
		List<Artifact> declaredDependencies = artifact.getDeclaredDependencies();
		writeln( writer, "declared dependencies", 1);
		for (Artifact declaredDependency : declaredDependencies) {
			writeln( writer, toString(declaredDependency), 2);
		}
		
		List<Artifact> actualDependencies = artifact.getActualDependencies();
		writeln( writer, "actual dependencies", 1);
		for (Artifact actualDependency : actualDependencies) {
			writeln( writer, toString(actualDependency), 2);
		}
				
	}
	

	/**
	 * a writer for the generic {@link AbstractEntity}
	 * @param writer - the {@link Writer} to use
	 * @param entry - 
	 * @param indent
	 * @throws IOException
	 */
	private void dump(Writer writer, AbstractEntity entry, int indent) throws IOException {

		
		if (entry instanceof InterfaceEntity) {
			dump( writer, (InterfaceEntity) entry, indent+1);
		}
		else if (entry instanceof ClassEntity) {
			dump( writer, (ClassEntity) entry, indent+1);
		}
		else if (entry instanceof EnumEntity) {
			dump( writer, (EnumEntity) entry, indent+1);
		}
		else {
			writeln( writer, "unknown entity " + entry.getName() + " found", indent+1);
		}
	}
	
	/**
	 * writer for {@link AbstractClassEntity}
	 * @param writer
	 * @param entry
	 * @param indent
	 * @throws IOException
	 */
	private void dump( Writer writer, AbstractClassEntity entry, int indent) throws IOException {
		if (entry.getGenericNature()) {
			writeln( writer, "generic entity nature", indent +1);
		}
		if (entry.getDirectDependency()) {
			writeln( writer, "direct dependency", indent +1);
		}
		if (entry.getDefinedLocal()) {
			writeln( writer, "locally declared", indent +1);
		}
		dumpAnnotations( writer, entry.getAnnotations(), indent + 1);		
		dumpMethods( writer, entry.getMethods(), indent + 1);		
	}
	
	/**
	 * writer for annotations
	 * @param writer
	 * @param annotations
	 * @param indent
	 * @throws IOException
	 */
	private void dumpAnnotations( Writer writer, Set<AnnotationEntity> annotations, int indent) throws IOException {
		if (annotations.size() == 0) {
			return;
		}
		writeln( writer, "" + annotations.size() + " annotations", indent +1);
		for (AnnotationEntity entry : annotations) {
			dump( writer, entry, indent + 2);
		}		
	}
	
	/**
	 * writer for methods
	 * @param writer
	 * @param methods
	 * @param indent
	 * @throws IOException
	 */
	private void dumpMethods( Writer writer, Set<MethodEntity> methods, int indent) throws IOException {
		if (methods.size() == 0) {
			return;
		}
		writeln( writer, "" + methods.size() + " methods", indent);
		for (MethodEntity entry : methods) {
			dump( writer, entry, indent + 1);
		}
		
	}
	
	/**
	 * writer for fields
	 * @param writer
	 * @param fields
	 * @param indent
	 * @throws IOException
	 */
	private void dumpFields(Writer writer, List<FieldEntity> fields, int indent) throws IOException {
		if (fields.size() == 0) {
			return;
		}
		writeln( writer, "" + fields.size() + " fields", indent + 1);
		for (FieldEntity entry : fields) {
			dump( writer, entry, indent+2);
		}		
	}
	
	/**
	 * writing a field 
	 * @param writer - the {@link Writer} to use
	 * @param field - the {@link FieldEntity} to dump
	 * @param indent
	 * @throws IOException
	 */
	private void dump( Writer writer, FieldEntity field, int indent) throws IOException {
	
		writeln( writer, field.getName(), indent);
		AbstractEntity type = field.getType();
		if (type == null) {
			writeln( writer, "signature " + field.getSignature() + "(n/a)", indent + 1);
		}
		else {
			//writeln( writer, type.getName() + "(" + toString( type.getArtifact()) + ")", indent + 1);			
			writeln( writer, dumpTypeReference(type), indent + 1);
		}
		if (type instanceof AbstractClassEntity) {
			AbstractClassEntity ace = (AbstractClassEntity) type;
			if (ace.getArrayNature()) {
				if (ace.getTwoDimensionality()) {
					writeln( writer, "two dimensional array", indent + 1);
				}
				else {
					writeln( writer, "one dimensional array", indent + 1);
				}
			}
		}
		writeln( writer, "desc " + field.getDesc(), indent + 1);
		writeln( writer, "initialized " + field.getInitializerPresentFlag(), indent + 1);
	}
	
	/**
	 * writing an annotation 
	 * @param writer
	 * @param entry
	 * @param indent
	 * @throws IOException
	 */
	private void dump( Writer writer, AnnotationEntity entry, int indent) throws IOException {
		writeln( writer, entry.getName() + "(" + toString( entry.getArtifact()) + ")", indent);
		for (Entry<String, AnnotationValueContainer> slot : entry.getMembers().entrySet()) {
			writeln( writer, slot.getKey(), indent +1);
			AnnotationValueContainer container = slot.getValue();			
			dump(writer, container, indent);
		}
		
	}

	/**
	 * writing the annotation's content 
	 * @param writer
	 * @param container
	 * @param indent
	 * @throws IOException
	 */
	private void dump(Writer writer, AnnotationValueContainer container, int indent) throws IOException {
		AnnotationValueContainerType containerType = container.getContainerType();
		
		switch (containerType) {
			case annotation:
				dump( writer, container.getAnnotation(), indent + 2);
				break;
			case collection:
				for (AnnotationValueContainer child : container.getChildren()) {
					dump( writer, child, indent + 3);
				}				
				break;
			case s_boolean:
				writeln( writer, "" + container.getSimpleBooleanValue(), indent+2);
				break;
			case s_date:
				writeln( writer, "" + container.getSimpleDateValue(), indent+2);
				break;
			case s_double:
				writeln( writer, "" + container.getSimpleDoubleValue(), indent+2);
				break;
			case s_float:
				writeln( writer, "" + container.getSimpleFloatValue(), indent+2);
				break;
			case s_int:
				writeln( writer, "" + container.getSimpleIntegerValue(), indent+2);
				break;
			case s_long:
				writeln( writer, "" + container.getSimpleLongValue(), indent+2);
				break;
			case s_string:
				writeln( writer, "" + container.getSimpleStringValue(), indent+2);
				break;
			default:
				break;		
		}
	}
	
	/**
	 * @param writer
	 * @param entry
	 * @param indent
	 * @throws IOException
	 */
	private void dump( Writer writer, MethodEntity entry, int indent) throws IOException {
		
		StringBuilder builder = new StringBuilder(); 
		AccessModifier accessModifier = entry.getAccessModifier();
		builder.append( accessModifier.toString().toLowerCase());
		
		if (entry.getAbstractNature()) {
			builder.append(" abstract");
		}
		if (entry.getStaticNature()) {
			builder.append(" static");
		}		
		if (entry.getSynchronizedNature()) {
			builder.append( " synchronized");
		}
		
		
		AbstractEntity returnType = entry.getReturnType();
				
		builder.append( STRING + dumpTypeReference(returnType));						
		
		
		builder.append( STRING + entry.getName());
		
		builder.append( "(");
			
		List<AbstractEntity> argumentTypes = entry.getArgumentTypes();
		if (argumentTypes.size() > 0) {
			StringBuilder argumentBuilder = new StringBuilder();
			
			for (AbstractEntity argument: argumentTypes) {
				if (argumentBuilder.length() > 0) {
					argumentBuilder.append(DELIMITER_ARGUMENT);
				}
				argumentBuilder.append( dumpTypeReference(argument));
			}
		
			builder.append( argumentBuilder.toString());
		}
		builder.append(")");
		
		Set<ClassEntity> exceptions = entry.getExceptions();
		if (exceptions.size() > 0) {
			builder.append(" throws ");
			StringBuilder exceptionBuilder = new StringBuilder();
			for (ClassEntity exception : exceptions) {
				if (exceptionBuilder.length() > 0)
					exceptionBuilder.append(DELIMITER_EXCEPTION);
				exceptionBuilder.append( exception.getName());
			}
			builder.append( exceptionBuilder.toString());			
		}			
		writeln( writer, builder.toString(), indent+1);
		dumpAnnotations(writer, entry.getAnnotations(), indent + 1);
	}
	
	
	
	/**
	 * @param writer
	 * @param entry
	 * @param indent
	 * @throws IOException
	 */
	private void dump(Writer writer, InterfaceEntity entry, int indent) throws IOException {			
		writeln( writer, entry.getName(), indent);
		dump( writer, (AbstractClassEntity) entry, indent+1);
		//
		
		Set<InterfaceEntity> superInterfaces = entry.getSuperInterfaces();
		if (superInterfaces.size() > 0) {
			writeln( writer, "" + superInterfaces.size() + " inherited interfaces",indent + 1);
			for (InterfaceEntity interfaceEntry : superInterfaces) {
				//dump( writer, interfaceEntry, indent + 2);
				writeln( writer, interfaceEntry.getName() + "(" + toString( interfaceEntry.getArtifact()) + ")", indent +2);
			}			
		}
		
		Set<InterfaceEntity> subInterfaces = entry.getSubInterfaces();
		if (subInterfaces.size() > 0) {
			writeln( writer, "" + subInterfaces.size() + " interfaces inheriting from",indent + 1);
			for (InterfaceEntity interfaceEntry : subInterfaces) {
				//dump( writer, interfaceEntry, indent + 2);
				writeln( writer, interfaceEntry.getName() + "(" + toString( interfaceEntry.getArtifact()) + ")", indent +2);
			}			
		}
		Set<ClassEntity> implementingClasses = entry.getImplementingClasses();
		if (implementingClasses.size() > 0) {
			writeln( writer, "" + implementingClasses.size() + " classes implementing",indent + 1);
			for (ClassEntity classEntry : implementingClasses) {
				writeln( writer, classEntry.getName() + "(" + toString( classEntry.getArtifact()) + ")", indent+2);
			}
		}
	}
		
	
	/**
	 * @param writer
	 * @param entry
	 * @param indent
	 * @throws IOException
	 */
	private void dump(Writer writer, ClassEntity entry, int indent) throws IOException {
		AccessModifier modifier = entry.getAccessModifier();
		if (modifier != null) {
			writeln( writer, modifier.toString().toLowerCase() + STRING + entry.getName(), indent+1);
		}
		else {
			writeln( writer, "n/a" + STRING + entry.getName(), indent+1);
		}
		
		dump( writer, (AbstractClassEntity) entry, indent+1);
		entry.getAccessModifier();
		dumpFields( writer, entry.getFields(), indent+1);
		
		ClassEntity superType = entry.getSuperType();
		if (superType != null) {
			writeln( writer, "super type : " + superType.getName() + "(" + toString( superType.getArtifact()) + ")", indent+1);
		}
		
		
		Set<ClassEntity> subTypes = entry.getSubTypes();
		if (subTypes.size() > 0) {
			writeln( writer, "" + subTypes.size() + " deriving types",indent + 1);
			for (ClassEntity subType : subTypes) {
				writeln( writer, subType.getName() + "(" + toString( subType.getArtifact()) + ")", indent + 2);
			}
			
			
		}
		
		Set<InterfaceEntity> implementedInterfaces = entry.getImplementedInterfaces();
		if (implementedInterfaces.size() > 0) {
			writeln( writer, "implementing " + implementedInterfaces.size() + " interfaces",indent + 2);
			for (InterfaceEntity implementedInterface : implementedInterfaces) {
				writeln( writer, implementedInterface.getName() + "(" + toString( implementedInterface.getArtifact()) + ")", indent +3);
			}
		}
	
	}

	/**
	 * 
	 * @param writer
	 * @param entry
	 * @param indent
	 * @throws IOException
	 */
	private void dump(Writer writer, EnumEntity entry, int indent) throws IOException {
		
		writeln( writer, entry.getName(), indent);
		StringBuilder builder = new StringBuilder();
		for (String value : entry.getValues()) {
			if (builder.length() > 0) {
				builder.append( ",");			
			}
			builder.append( value);
		}
		writeln( writer, builder.toString(), indent + 1);
	}
	
	
	/**
	 * @param writer
	 * @param string
	 * @param indent
	 * @throws IOException
	 */
	private void writeln( Writer writer, String string, int indent) throws IOException {
		String tab = "";
		if (indent > 0) {
			tab = TABS.substring(0, indent);
		}
		writer.write( tab + string + "\n");
	}
	
	/**
	 * @param artifact
	 * @return
	 */
	private String toString( Artifact artifact) {
		if (artifact == null) {
			return "n/a";
		}
		if (artifact.getGroupId() != null && artifact.getVersion() != null) {
			return artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + artifact.getVersion();
		}
		return artifact.getArtifactId();
	}

	/**
	 * @param abstractType
	 * @return
	 */
	private String dumpTypeReference( AbstractEntity abstractType) {
		if (abstractType == null) {
			return "-nil-";
		}
		if (abstractType instanceof AbstractClassEntity) {
			AbstractClassEntity abstractClassType = (AbstractClassEntity) abstractType;
			
			if (abstractClassType.getParameterization().size() > 0) {
				StringBuilder builder = new StringBuilder();
				if (descOnly) {
					builder.append( abstractType.getName());
				}
				else {
					builder.append( abstractType.getName() + dumpArtifactReference( abstractType));
				}
				builder.append( '<');
				for (AbstractEntity parameter : abstractClassType.getParameterization()) {
					builder.append( dumpTypeReference( parameter));
				}
				builder.append( '>');
				return builder.toString();
			}
			else {
				if (descOnly) {
					return abstractType.getName();
				}
				else {
					return abstractType.getName() + dumpArtifactReference( abstractType);
				}
			}
		}
		else {
			if (descOnly) {
				return abstractType.getName();
			}
			else {
				return abstractType.getName() + dumpArtifactReference( abstractType);
			}
		}		
	}

	/**
	 * @param abstractType
	 * @return
	 */
	private String dumpArtifactReference(AbstractEntity abstractType) {		
		Artifact artifact = abstractType.getArtifact();	
		if (artifact.getGroupId() != null) {
			return "(" + toString( artifact)  + ")";
		}
		if (artifact.getArtifactId().equalsIgnoreCase("rt")) {
			return "";
		}
		return "(" + toString( artifact)  + ")";
	}

}
