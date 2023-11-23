package com.braintribe.build.model;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.asm.ClassReader;
import com.braintribe.asm.tree.AbstractInsnNode;
import com.braintribe.asm.tree.AnnotationNode;
import com.braintribe.asm.tree.ClassNode;
import com.braintribe.asm.tree.InsnList;
import com.braintribe.asm.tree.LocalVariableNode;
import com.braintribe.build.model.entity.Entity;

/**
 * ASM based reflection
 * @author pit
 */
public class ModelAsmReflection implements ModelReflection {
	private static Package treePackage = ClassNode.class.getPackage();
	private static String enumName = "java/lang/Enum";
	private static String genericEntityName = "com/braintribe/model/generic/GenericEntity";
	private static String forwardAnnotationName = "com/braintribe/model/generic/annotation/ForwardDeclaration";
	
	private Map<String, Entity> nameToEntityMap = new HashMap<>();
	private ClassLoader classLoader;
	
	public ModelAsmReflection( ClassLoader classloader) {
		this.classLoader = classloader;
	}

	@Override
	public Entity load(String className) {		
		String resourceName = className.replace('.', '/') + ".class";
		try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
			if (inputStream != null) {
				return read( inputStream);				
			}		
		} catch (IOException e) {		
			e.printStackTrace();
		}
		System.out.println("No resource found matching:" + className);
		return null;								
	}

	public static ModelReflection scan(ClassLoader classLoader) {		
		ModelAsmReflection mar = new ModelAsmReflection(classLoader);				
		return mar;
	}
	
	private Entity read( InputStream in) {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final Entity en = new Entity();
			traverse(classNode, new Visitor() {
				@Override
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;
						
						String name = node.name;						
						
						nameToEntityMap.put(name, en);
					
						// genericity 
						List<String> interfaces = node.interfaces;				
						if (interfaces != null) {
							if (interfaces.contains( genericEntityName)) {
								en.setIsGenericEntity(true);
							}
							else {
								for (String interfaceName : interfaces) {
									if (isGenericEntity( interfaceName)) {
										en.setIsGenericEntity(true);
										break;
									}
								}
							}
						}
						// enum 
						String superType = node.superName;
						if (superType != null) {
							if (superType.equals( enumName)) {
								en.setIsEnum(true);
							}
							else {
								if (isEnum( superType)) {
									en.setIsEnum(true);
								}
							}						
						}
								
						// forwards
						List<AnnotationNode> visibleAnnotations = node.visibleAnnotations;
						if (visibleAnnotations != null) {
							for (AnnotationNode an : visibleAnnotations) {
								String desc = an.desc;
								List<Object> values = an.values;
								
								if (desc.equals( forwardAnnotationName)) {
									String forward = (String) values.get(0); // only one value
									en.setForwardDeclaration(forward);
								}
							}
						}						
					}
				}
			});
			
			return en;
		}
		catch (Exception e) {
			e.printStackTrace();			
		}
		return null;
	}

	
	protected boolean isEnum(String superType) {
		Entity en = nameToEntityMap.get(superType);
		if (en == null) {
			en = load(superType);			
		}		
		if (en.getIsEnum()) {
			return true;
		}
		String st = en.getSuperType();
		if (st != null) {
			return isEnum( st);
		}
		return false;
	}

	protected boolean isGenericEntity(String interfaceName) {
		if (interfaceName.equals( genericEntityName))
			return true;
		Entity en = nameToEntityMap.get(interfaceName);
		if (en == null) {
			en = load(interfaceName);			
		}
		if (en.getIsGenericEntity()) {
			return true;
		}
		List<String> interfaces = en.getInterfaces();
		if (interfaces != null) {
			for (String ifn : en.getInterfaces()) {
				isGenericEntity(ifn);
			}
		}
		return false;
	}


	public interface Visitor {
		public void visit(Object object) throws IllegalStateException;
	}
	
	public static void traverse(Object object, Visitor visitor) throws IllegalArgumentException, IllegalAccessException {
		if (object == null) 
			return;
		
		if (object instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>)object;
			for (Object element: collection) {
				if (isTreeObject(element)) {
					traverse(element, visitor);
				}
			}
		}
		else if (object instanceof InsnList) {
			InsnList insnList = (InsnList)object;
			for (int i = 0; i < insnList.size(); i++) {
				AbstractInsnNode abstractInsnNode = insnList.get(i);
				traverse(abstractInsnNode, visitor);
			}
		}
		else if (object.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(object); i++) {
				traverse(Array.get(object, i), visitor);
			}
		}
		else if (!isTreeObject(object)) {
			return;
		}

		
		if (object instanceof LocalVariableNode){
			LocalVariableNode lvn = (LocalVariableNode)object;
			if (lvn.name.equals("this"))
				return;
		}
		
		visitor.visit(object);
		
		Field[] fields = object.getClass().getFields();
		
		for (Field field: fields) {
			if ((field.getModifiers() & Modifier.STATIC) == 0) {
				Class<?> fieldType = field.getType();
				Object fieldValue = field.get(object);
				
				if (fieldType == AnnotationNode.class)
					System.out.println(fieldValue);
				
				if (fieldValue != null) {
					traverse(fieldValue, visitor);
				}
			}
		}
	}
	
	public static boolean isTreeObject(Object object) {
		return object != null && object.getClass().getPackage() == treePackage;
	}
}
