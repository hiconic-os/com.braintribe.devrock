package com.braintribe.build.model;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.build.model.entity.Entity;

/**
 * Model type reflection based on the standard Java Class-File API.
 * <p>
 * It intentionally reads class files without loading or initializing the
 * classes from the model-under-construction.
 */
public class ModelClassFileReflection implements ModelReflection {
	private static final ClassDesc GENERIC_ENTITY = ClassDesc.of("com.braintribe.model.generic.GenericEntity");
	private static final ClassDesc FORWARD_DECLARATION = ClassDesc.of("com.braintribe.model.generic.annotation.ForwardDeclaration");
	private static final ClassDesc GM_SYSTEM_INTERFACE = ClassDesc.of("com.braintribe.model.generic.annotation.GmSystemInterface");

	private final Map<String, Entity> nameToEntityMap = new HashMap<>();
	private final ClassLoader classLoader;

	public ModelClassFileReflection(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public static ModelReflection scan(ClassLoader classLoader) {
		return new ModelClassFileReflection(classLoader);
	}

	@Override
	public Entity load(String className) {
		Entity cached = nameToEntityMap.get(className);
		if (cached != null)
			return cached;

		String resourceName = className.replace('.', '/') + ".class";
		try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
			if (inputStream == null) {
				System.out.println("No resource found matching:" + className);
				return null;
			}

			return read(className, inputStream.readAllBytes());
		} catch (IOException e) {
			throw new IllegalStateException("Error while reading class-file resource: " + resourceName, e);
		}
	}

	private Entity read(String className, byte[] bytes) {
		ClassModel classModel = ClassFile.of().parse(bytes);
		Entity entity = new Entity();
		entity.setName(className);

		// Cache before following interfaces. This also makes malformed cyclic
		// hierarchies terminate deterministically.
		nameToEntityMap.put(className, entity);

		if (hasAnnotation(classModel, GM_SYSTEM_INTERFACE))
			return entity;

		List<String> interfaces = classModel.interfaces().stream() //
				.map(ModelClassFileReflection::className) //
				.toList();
		entity.setInterfaces(interfaces);
		entity.setSuperType(classModel.superclass().map(ModelClassFileReflection::className).orElse(null));
		entity.setIsEnum(classModel.flags().has(java.lang.reflect.AccessFlag.ENUM));
		entity.setIsGenericEntity(interfaces.stream().anyMatch(this::isGenericEntity));
		entity.setForwardDeclaration(stringAnnotationValue(classModel, FORWARD_DECLARATION));

		return entity;
	}

	private boolean isGenericEntity(String interfaceName) {
		if (GENERIC_ENTITY.equals(ClassDesc.of(interfaceName)))
			return true;

		Entity entity = load(interfaceName);
		return entity != null && entity.getIsGenericEntity();
	}

	private static boolean hasAnnotation(ClassModel classModel, ClassDesc annotationType) {
		return visibleAnnotations(classModel).stream().anyMatch(annotation -> annotation.classSymbol().equals(annotationType));
	}

	private static String stringAnnotationValue(ClassModel classModel, ClassDesc annotationType) {
		return visibleAnnotations(classModel).stream() //
				.filter(annotation -> annotation.classSymbol().equals(annotationType)) //
				.findFirst() //
				.flatMap(annotation -> annotation.elements().stream() //
						.filter(element -> element.name().equalsString("value")) //
						.findFirst()) //
				.map(AnnotationElement::value) //
				.filter(AnnotationValue.OfString.class::isInstance) //
				.map(AnnotationValue.OfString.class::cast) //
				.map(AnnotationValue.OfString::stringValue) //
				.orElse(null);
	}

	private static List<Annotation> visibleAnnotations(ClassModel classModel) {
		return classModel.findAttribute(Attributes.runtimeVisibleAnnotations()) //
				.map(RuntimeVisibleAnnotationsAttribute::annotations) //
				.orElseGet(List::of);
	}

	private static String className(ClassEntry classEntry) {
		return classEntry.asInternalName().replace('/', '.');
	}
}
