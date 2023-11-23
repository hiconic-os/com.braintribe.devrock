package com.braintribe.build.model;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.braintribe.build.model.entity.Entity;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ForwardDeclaration;

/**
 * class based reflection 
 * @author dirk
 */
public class ModelClassLoaderReflection implements ModelReflection {
	private Class<?> genericEntityClass = null;
	private Class<? extends Annotation> forwardDeclarationClass = null;
	private MethodHandle valueMethod = null;
	private ClassLoader classLoader;
	
	public static ModelReflection scan(ClassLoader classLoader) {
		ModelClassLoaderReflection tools = new ModelClassLoaderReflection();
		tools.classLoader = classLoader;
		
		try {
			tools.genericEntityClass = Class.forName(GenericEntity.class.getName(), false, classLoader);
			tools.forwardDeclarationClass = Class.forName(ForwardDeclaration.class.getName(), false, classLoader).asSubclass(Annotation.class);
			tools.valueMethod = MethodHandles.lookup().findVirtual(tools.forwardDeclarationClass, "value", MethodType.methodType(String.class));
			
			return tools;
		} catch (Exception e1) {
			throw Exceptions.unchecked(e1, "Error while loading special classes from secondary classloader: classpath of model hierarchy is broken", IllegalStateException::new);
		}
	}
	
	
	private boolean isGenericEntity(Class<?> clazz) {
		return genericEntityClass.isAssignableFrom(clazz);
	}
	
	
	private String isForward(Class<?> clazz) {
		Annotation forwardDeclaration = clazz.getAnnotation(forwardDeclarationClass);

		try {
			if (forwardDeclaration != null)
				return (String)valueMethod.invokeExact();
			else
				return null;
		} catch (Throwable e) {
			throw Exceptions.unchecked(e, "Error while reading ForwardDeclaration.value() from " + forwardDeclaration, IllegalStateException::new);
		}
	}
	
	@Override
	public Entity load(String className) {
		try {
			Class<?> suspect = Class.forName(className, false, classLoader);
			Entity cn = new Entity(className, isGenericEntity(suspect), suspect.isEnum(), isForward(suspect));						
			return cn;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while loading class from build folders: " + className, IllegalStateException::new);
		}
	}
}