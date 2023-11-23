// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.braintribe.asm.ClassReader;
import com.braintribe.asm.Type;
import com.braintribe.asm.tree.AnnotationNode;
import com.braintribe.asm.tree.ClassNode;
import com.braintribe.asm.tree.FieldInsnNode;
import com.braintribe.asm.tree.FieldNode;
import com.braintribe.asm.tree.InsnList;
import com.braintribe.asm.tree.LdcInsnNode;
import com.braintribe.asm.tree.LocalVariableNode;
import com.braintribe.asm.tree.MethodInsnNode;
import com.braintribe.asm.tree.MethodNode;
import com.braintribe.asm.tree.MultiANewArrayInsnNode;
import com.braintribe.asm.tree.TryCatchBlockNode;
import com.braintribe.asm.tree.TypeInsnNode;


public abstract class AsmClassDepScanner {
	private static Package treePackage = ClassNode.class.getPackage();

	public interface ClassExtracter {
		public Collection<String> extractClasses(Object value); 
	}
	
	public static class ClassFromSignatureExtractor implements ClassExtracter {
		
		public Collection<String> extractClasses(Object value) {
			String genericSignature = (String)value;
			
			Set<String> result = new HashSet<String>();
			
			try {
				fillRawSignatures(genericSignature, result);
				
			} catch (Exception e) {
				throw new RuntimeException("Unexpected signature format: " + genericSignature);
			}
			
			return result;
		}

		private void fillRawSignatures(String genericSignature, Set<String> result) throws Exception {
			if (!genericSignature.contains("<")) {
				String rawSignatures = genericSignature.replace("[", "");
				
				String[] singleSignatures = rawSignatures.split(";");
				for (String signature: singleSignatures) {
					while (!signature.isEmpty() && !signature.startsWith("L"))
						
						signature = signature.substring(1);
					if (!signature.isEmpty()) {
						result.add(signature.substring(1));
					}
				}
				
				return;
			}
			
			int start = genericSignature.indexOf("<");
			int end = findMatchingEnd(genericSignature, start);
			
			// shorter is the original genericSignature without the first found generic parametrization (but there may be more, if inner class)
			// Ljava/lang/Map<Ljava/lang/String;Ljava/lang/Object;>; -> shorter=Ljava/lang/Map; 
			// Ljava/lang/Map<Ljava/lang/String;Ljava/lang/Object;>.Entry<Ljava/lang/String;Ljava/lang/Object;>; -> shorter=Ljava/lang/Map.Entry<Ljava/lang/String;Ljava/lang/Object;>; 
			String shorter = genericSignature.substring(0, start) + genericSignature.substring(end + 1);
			fillRawSignatures(shorter, result);
			
			// we now extract the content of the first found generic parametrization - is a comma separated.... 
			String genericParameters = genericSignature.substring(start + 1, end);
			fillRawSignatures(genericParameters, result);
		}

		private int findMatchingEnd(String genericSignature, int start) throws Exception {
			int counter = 1;
			char[] chars = genericSignature.toCharArray();
			for (int i = start+1; i< chars.length; i++) {
				if (chars[i] == '<') {
					counter++;
				} else if (chars[i] == '>') {
					counter--;
				}
				
				if (counter == 0) {
					return i;
				}
			}
			
			// this should never happen
			throw new Exception("Unexpected type signature:");
		}
	}
	
	public static class ClassNameListExtractor implements ClassExtracter {
		public Collection<String> extractClasses(Object value) {
			@SuppressWarnings("unchecked")
			List<String> classNameList = (List<String>)value;
			return classNameList;
		}
	}
	
	public static class ClassNameExtractor implements ClassExtracter {
		public Collection<String> extractClasses(Object value) {
			String className = (String)value;
			return Arrays.asList(className);
		}
	}
	
	public static class ClassNameFromTypeInsnExtractor implements ClassExtracter {
		public Collection<String> extractClasses(Object value) {
			String className = (String)value;
			if (className.startsWith("[")) {
				int s = className.indexOf('L') + 1;
				int e = className.length() - 1;
				className = className.substring(s, e);
				
			}

			return Arrays.asList(className);
		}
	}
	
	public static class CstExtractor implements ClassExtracter {
		private ClassFromSignatureExtractor componentTypeExtractor;
		
		public CstExtractor()
		{
			componentTypeExtractor = new ClassFromSignatureExtractor();
		}

	    public Collection<String> extractClasses(Object value)
	    {
	        if(value instanceof Type)
	        {
	            Type type = (Type)value;
	            String name = type.getInternalName();
	            if(name.charAt(0) == '[')
	                return componentTypeExtractor.extractClasses(name);
	            else
	                return Arrays.asList(new String[] {
	                    name
	                });
	        } else
	        {
	            return Collections.emptySet();
	        }
	    }
	
	}
	
	public static class OwnerExtractor
	    implements ClassExtracter
	{
		public OwnerExtractor()
		{
		}
	
	    public Collection<String> extractClasses(Object value)
	    {
			String className = (String)value;
			if (className.startsWith("[")) {
				int s = className.indexOf('L') + 1;
				int e = className.length() - 1;
				className = className.substring(s, e);
				
			}

			return Arrays.asList(className);
	    }
	
	}
	
	public static class FieldMatch {
		private Class<?> type;
		private String name;
		
		public FieldMatch(Class<?> type, String name) {
			super();
			this.type = type;
			this.name = name;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FieldMatch other = (FieldMatch) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
		
		
	}
	
	public interface Visitor {
		public void visit(Object object) throws IllegalStateException;
	}
	
	private static Map<FieldMatch, ClassExtracter> extracters = new HashMap<FieldMatch, ClassExtracter>();
	
	static {
        extracters.put(new FieldMatch(ClassNode.class, "interfaces"), new ClassNameListExtractor());
        extracters.put(new FieldMatch(ClassNode.class, "superName"), new ClassNameExtractor());
        extracters.put(new FieldMatch(FieldNode.class, "desc"), new ClassFromSignatureExtractor());
        extracters.put(new FieldMatch(FieldNode.class, "signature"), new ClassFromSignatureExtractor());
        extracters.put(new FieldMatch(MethodNode.class, "desc"), new ClassFromSignatureExtractor());
        extracters.put(new FieldMatch(MethodNode.class, "signature"), new ClassFromSignatureExtractor());
        extracters.put(new FieldMatch(MethodNode.class, "exceptions"), new ClassNameListExtractor());
        extracters.put(new FieldMatch(LocalVariableNode.class, "desc"), new ClassFromSignatureExtractor());
        extracters.put(new FieldMatch(LocalVariableNode.class, "signature"), new ClassFromSignatureExtractor());
        extracters.put(new FieldMatch(TypeInsnNode.class, "desc"), new CstExtractor());
        extracters.put(new FieldMatch(TryCatchBlockNode.class, "type"), new ClassNameExtractor());
        extracters.put(new FieldMatch(MultiANewArrayInsnNode.class, "desc"), new ClassFromSignatureExtractor());
        extracters.put(new FieldMatch(AnnotationNode.class, "desc"), new ClassFromSignatureExtractor());
        extracters.put(new FieldMatch(FieldInsnNode.class, "owner"), new OwnerExtractor());
        extracters.put(new FieldMatch(LdcInsnNode.class, "cst"), new CstExtractor());
        extracters.put(new FieldMatch(MethodInsnNode.class, "owner"), new OwnerExtractor());

	}
	
	public static void traverse(Object object, Visitor visitor) throws IllegalArgumentException, IllegalAccessException {
		if (object == null) return;
		else if (object instanceof Collection<?>) {
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
				traverse(insnList.get(i), visitor);
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
		// suppress the dreaded false positive "java.lang.NoSuchFieldError" - message 
		// the eclipse compiler adds this in his code, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=141810
		// as we're scanning GWT modules here, we KNOW that Eclipse will not compile them in any case
		if (object instanceof MethodNode) {
			MethodNode methodNode = (MethodNode) object;
			if (methodNode.name.startsWith( "$SWITCH_TABLE$"))
				return false;
		}
		return object != null && object.getClass().getPackage() == treePackage;
	}
	
	public static Set<String> getClassDependencies(File classFile) throws Exception {
		InputStream in = null;
		try {
			in = new FileInputStream(classFile);
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final Set<String> classes = new TreeSet<String>();
			traverse(classNode, new Visitor() {
				public void visit(Object object) {
					Class<?> clazz = object.getClass();
					
					for (Field field: clazz.getFields()) {
						Object fieldValue;
						try {
							fieldValue = field.get(object);
							if (fieldValue != null) {
								FieldMatch match = new FieldMatch(clazz, field.getName());
								ClassExtracter extracter = extracters.get(match);
								if (extracter != null) {
									Collection<String> result = extracter.extractClasses(fieldValue);									
									classes.addAll(result);
								}
							}
						}
						catch (RuntimeException e) {
							throw e;
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			});
			Set<String> normalizedClasses = new HashSet<String>(classes.size());
			for (String clazz: classes) {
				normalizedClasses.add(clazz.replace('.', '$').replace('/', '.'));
			}
			return normalizedClasses;
		}
		finally {
			if (in != null) 
				in.close();
		}
	}
		
	
}
