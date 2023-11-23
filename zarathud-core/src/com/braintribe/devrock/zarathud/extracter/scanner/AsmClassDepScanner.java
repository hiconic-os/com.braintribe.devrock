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
package com.braintribe.devrock.zarathud.extracter.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.braintribe.asm.ClassReader;
import com.braintribe.asm.Opcodes;
import com.braintribe.asm.Type;
import com.braintribe.asm.tree.AbstractInsnNode;
import com.braintribe.asm.tree.AnnotationNode;
import com.braintribe.asm.tree.ClassNode;
import com.braintribe.asm.tree.FieldInsnNode;
import com.braintribe.asm.tree.FieldNode;
import com.braintribe.asm.tree.InsnList;
import com.braintribe.asm.tree.LdcInsnNode;
import com.braintribe.asm.tree.LocalVariableNode;
import com.braintribe.asm.tree.MethodNode;
import com.braintribe.asm.tree.MultiANewArrayInsnNode;
import com.braintribe.asm.tree.TryCatchBlockNode;
import com.braintribe.asm.tree.TypeInsnNode;

/**
 * @author dirk
 *
 */
public abstract class AsmClassDepScanner {
	private static Package treePackage = ClassNode.class.getPackage();

	public interface ClassExtracter {
		public Collection<String> extractClasses(Object value); 
	}
	
	public static class ClassFromSignatureExtractor implements ClassExtracter {
		private static Pattern pattern = Pattern.compile("L([^;<:-]*)[;<]");
		
		@Override
		public Collection<String> extractClasses(Object value) {
			String signature = (String)value;
			List<String> result = new ArrayList<String>();

			Matcher matcher = pattern.matcher(signature);
			while (matcher.find()) {
				String className = matcher.group(1);
				if (className.length() == 0)
					continue;
				result.add(className);
			}
			
			return result;
		}
	}
	
	public static class OwnerExtractor implements ClassExtracter {
		
		@Override
		public Collection<String> extractClasses(Object value) {
			String typeInsnNodeOwner = (String)value;
			return Arrays.asList(typeInsnNodeOwner);
		}
	}
	/*
	public static class CstExtractor implements ClassExtracter {
		
		public Collection<String> extractClasses(Object value) {
			if (value instanceof Type) {
				Type type = (Type)value; 
				return Arrays.asList(type.getInternalName());
			}
			else return Collections.emptySet();
		}
	}*/
	
	public static class CstExtractor implements ClassExtracter {
		  private ClassFromSignatureExtractor componentTypeExtractor = new ClassFromSignatureExtractor();
		  
		  @Override
		public Collection<String> extractClasses(Object value) {
		   if (value instanceof Type) {
		    Type type = (Type)value;
		    String name = type.getInternalName();
		    
		    if (name.charAt(0) == '[') {
		     return componentTypeExtractor.extractClasses(name);
		    }
		    else
		     return Arrays.asList(name);
		   }
		   else return Collections.emptySet();
		  }
	}
	
	public static class ClassNameListExtractor implements ClassExtracter {
		@Override
		public Collection<String> extractClasses(Object value) {
			@SuppressWarnings("unchecked")
			List<String> classNameList = (List<String>)value;
			return classNameList;
		}
	}
	
	public static class ClassNameExtractor implements ClassExtracter {
		@Override
		public Collection<String> extractClasses(Object value) {
			String className = (String)value;
			return Arrays.asList(className);
		}
	}
	
	public static class ClassNameFromTypeInsnExtractor implements ClassExtracter {
		@Override
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
		//extracters.put(new FieldMatch(ClassNode.class, "signature"), new ClassFromSignatureExtractor());
		extracters.put(new FieldMatch(ClassNode.class, "interfaces"), new ClassNameListExtractor());
		extracters.put(new FieldMatch(ClassNode.class, "superName"), new ClassNameExtractor());
		extracters.put(new FieldMatch(FieldNode.class, "desc"), new ClassFromSignatureExtractor());
		extracters.put(new FieldMatch(FieldNode.class, "signature"), new ClassFromSignatureExtractor());
		extracters.put(new FieldMatch(MethodNode.class, "desc"), new ClassFromSignatureExtractor());
		extracters.put(new FieldMatch(MethodNode.class, "signature"), new ClassFromSignatureExtractor());
		extracters.put(new FieldMatch(MethodNode.class, "exceptions"), new ClassNameListExtractor());
		extracters.put(new FieldMatch(LocalVariableNode.class, "desc"), new ClassFromSignatureExtractor());
		extracters.put(new FieldMatch(LocalVariableNode.class, "signature"), new ClassFromSignatureExtractor());
		//extracters.put(new FieldMatch(TypeInsnNode.class, "desc"), new ClassNameFromTypeInsnExtractor());
		extracters.put(new FieldMatch(TypeInsnNode.class, "desc"), new CstExtractor());
		extracters.put(new FieldMatch(TryCatchBlockNode.class, "type"), new ClassNameExtractor());
		extracters.put(new FieldMatch(MultiANewArrayInsnNode.class, "desc"), new ClassFromSignatureExtractor());
		extracters.put(new FieldMatch(AnnotationNode.class, "desc"), new ClassFromSignatureExtractor());
		extracters.put(new FieldMatch(FieldInsnNode.class, "owner"), new OwnerExtractor());
		extracters.put(new FieldMatch(LdcInsnNode.class, "cst"), new CstExtractor());
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
	
	/**
	 * currently only retrieves the values of the first annotation it finds
	 * (is thought to be the GenericModel annotation, and its gwtModule member)
	 * @param in - the {@link InputStream} to be read (the binary class)
	 * @return - a {@link Set} of {@link AnnotationTuple} extracted 
	 * @throws AsmClassDepScannerException - arrgh
	 */
	public static Set<AnnotationTuple> getClassAnnotationMembers( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final Set<AnnotationTuple> tuples = new HashSet<AnnotationTuple>();
			traverse(classNode, new Visitor() {				
				@Override
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;						
						if (						
							 (node.visibleAnnotations == null) ||
							 (node.visibleAnnotations.size() == 0)
							)
							return;
						for (int i = 0; i < node.visibleAnnotations.size(); i++) {
							AnnotationNode annotationNode = node.visibleAnnotations.get( i);
							tuples.add( createTupleFromAnnotationNode(annotationNode));
						}
					}
				}
			});
			return tuples;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}
	}
	
	private static AnnotationTuple createTupleFromAnnotationNode( AnnotationNode annotationNode) {
		AnnotationTuple tuple = new AnnotationTuple();
		
		tuple.setDesc( annotationNode.desc);
		@SuppressWarnings("rawtypes")
		List values = annotationNode.values;
		if (values == null) {
			return tuple;
		}			
		for (int j = 0; j < values.size() - 1; j = j+2) {
			String memberName = (String) values.get( j);
			Object obj = values.get(j+1);
			if (obj instanceof AnnotationNode) {
				AnnotationTuple child = createTupleFromAnnotationNode( (AnnotationNode) obj);
				tuple.getValues().put( memberName, child);
			} 
			else if (obj instanceof Collection<?>) {
				@SuppressWarnings("unchecked")
				Collection<Object> collection = (Collection<Object>) obj;
				for (Object suspect : collection)
					if (suspect instanceof AnnotationNode) { 
						AnnotationTuple child = createTupleFromAnnotationNode((AnnotationNode) suspect);
						tuple.getValues().put( memberName, child);
					} else 
						tuple.getValues().put( memberName, suspect);
			} else {
				tuple.getValues().put( memberName, obj);
			}
		}
		return tuple;
	}
	
	/**
	 * extract the interface names of the class 
	 * @param in - the {@link InputStream} to be read (the binary class)
	 * @return - a {@link List} of {@link String} extracted 
	 * @throws AsmClassDepScannerException - arrgh
	 */
	public static List<String> getInterfaces( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final List<String> interfaceList = new ArrayList<String>(); 
			traverse(classNode, new Visitor() {
				@Override
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;						
						interfaceList.addAll( node.interfaces);						
					}
				}
			});
			return interfaceList;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}
	}
	
	/**
	 * extract the super types of a class 
	 * @param in - the {@link InputStream} to be read (the binary class)
	 * @return a {@link Map} name to super name 
	 * @throws AsmClassDepScannerException - arrgh
	 */
	public static Map<String, String> getSuper( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final Map<String, String> superMap = new HashMap<String, String>(); 
			traverse(classNode, new Visitor() {
				@Override
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;						
						superMap.put( node.name, node.superName);						
					}
				}
			});
			return superMap;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}		
	}
	
	public static ClassData getClassData( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final ClassData classData = new ClassData(); 
			traverse(classNode, new Visitor() {
				@Override			
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;						
						if ((node.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) {
							classData.setAbstractNature( true);
						}
						if ((node.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
							classData.setStaticNature( true);
						}
						if ((node.access & Opcodes.ACC_SYNCHRONIZED) == Opcodes.ACC_SYNCHRONIZED) {
							classData.setSynchronizedNature( true);
						}
						
						// access  
						if ((node.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
							classData.setAccessNature( AccessModifier.PUBLIC);
						} else 
							if ((node.access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED) {
								classData.setAccessNature( AccessModifier.PROTECTED);
							} else 
								classData.setAccessNature( AccessModifier.PRIVATE);
						
						// annotations - only names for now
						Set<String> annotations = new HashSet<String>();								
						List<AnnotationNode> annotationNodes = node.visibleAnnotations;
						if (annotationNodes != null && annotationNodes.size() > 0) {
							for (AnnotationNode annotationNode : annotationNodes) {															
								annotations.add(annotationNode.desc);
							}
						}
						annotationNodes = node.invisibleAnnotations;
						if (annotationNodes != null && annotationNodes.size() > 0) {
							for (AnnotationNode annotationNode : annotationNodes) {																		
								annotations.add(annotationNode.desc);										
							}
						}
						classData.setAnnotations(annotations);
						
						// fields
						List<FieldNode> fieldNodes = node.fields;
						if (
								fieldNodes != null &&
								fieldNodes.size() > 0
							) {
							List<FieldData> fieldDatas = new ArrayList<FieldData>();
							for (FieldNode fieldNode : fieldNodes) {
								FieldData fieldData = new FieldData();
								fieldData.setName( fieldNode.name);
								fieldData.setDesc( fieldNode.desc);
								fieldData.setIntializer( fieldNode.value);
								fieldData.setSignature( fieldNode.signature);
								fieldDatas.add(fieldData);								
							}
							classData.setFieldData(fieldDatas);
						}
						
					}
				}
			});
			return classData;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}		
	}
	
	public static List<MethodData> getMethodData( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final List<MethodData> methodData = new ArrayList<MethodData>();
			traverse(classNode, new Visitor() {
				@Override
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;						
						
						List<MethodNode> methods = node.methods;
						if (methods != null && methods.size() > 0) {
							for (MethodNode methodNode : methods) {																
								MethodData method = new MethodData();															
								method.setMethodName( methodNode.name);
								String desc = methodNode.desc;
								method.setDesc( desc);
								
								Type returnType = Type.getReturnType( desc);															
								method.setReturnType( returnType.toString());
								
								Type [] argumentTypes = Type.getArgumentTypes( desc);
								if (argumentTypes != null) {
									List<String> arguments = new ArrayList<String>( argumentTypes.length);
									for (Type argument : argumentTypes) {
										arguments.add( argument.toString());
									}
									method.setArgumentTypes( arguments);
								}
								
								method.setSignature( methodNode.signature);
								
								// annotations - only names for now
								Set<String> annotations = new HashSet<String>();								
								List<AnnotationNode> annotationNodes = methodNode.visibleAnnotations;
								if (annotationNodes != null && annotationNodes.size() > 0) {
									for (AnnotationNode annotationNode : annotationNodes) {															
										annotations.add(annotationNode.desc);
									}
								}
								annotationNodes = methodNode.invisibleAnnotations;
								if (annotationNodes != null && annotationNodes.size() > 0) {
									for (AnnotationNode annotationNode : annotationNodes) {																		
										annotations.add(annotationNode.desc);										
									}
								}
								
								method.setAnnotations(annotations);
								// exceptions 			
								List<String> exceptions = methodNode.exceptions;
								method.setExceptions(exceptions);
								
								if ((methodNode.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) {
									method.setAbstractNature( true);
								}
								if ((methodNode.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
									method.setStaticNature( true);
								}
								if ((methodNode.access & Opcodes.ACC_SYNCHRONIZED) == Opcodes.ACC_SYNCHRONIZED) {
									method.setSynchronizedNature( true);
								}
								
								// access  
								if ((methodNode.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
									method.setAccessNature( AccessModifier.PUBLIC);
								} else 
									if ((methodNode.access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED) {
										method.setAccessNature( AccessModifier.PROTECTED);
									} else 
										method.setAccessNature( AccessModifier.PRIVATE);
														
								methodData.add( method);		
								
							}
						}						
					}
				}
			});
			return methodData;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}		
	}
	/**
	 * @param in - the {@link InputStream} to access the data 
	 * @return {@link InheritanceData} extracted 
	 * @throws AsmClassDepScannerException - arrgh
	 */
	public static InheritanceData getInheritanceData( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final InheritanceData inheritanceData = new InheritanceData(); 
			traverse(classNode, new Visitor() {
				@Override
				public void visit(Object object) {
					if (object instanceof ClassNode) {
						ClassNode node = (ClassNode) object;						
						inheritanceData.setSuperClass( node.superName.replaceAll( "/", "."));
						inheritanceData.setInterfaces( node.interfaces);		
						if ((node.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) {
							inheritanceData.setAbstractClass( true);
						}
						if ((node.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE) {
							inheritanceData.setInterfaceClass( true);
						}
						inheritanceData.setName( node.name);
						
					}
				}
			});
			return inheritanceData;
		} catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}		
	}
	
	public static Set<String> getClassDependencies( InputStream in) throws AsmClassDepScannerException {
		try {
			ClassReader classReader = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			final Set<String> classes = new TreeSet<String>();
			traverse(classNode, new Visitor() {
				@Override
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
									for (String str : result) {
										if (str.startsWith( "[")) {
											System.out.println("bracket");
										}
									}
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
				normalizedClasses.add(clazz.replace('/', '.'));
			}
			return normalizedClasses;
		} catch (Exception e) {
			throw new AsmClassDepScannerException("cannot scan class bytes as " + e, e);
		} 
		
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw new AsmClassDepScannerException("cannot close class bytes stream as " + e, e);
				}
		}
	}
	
	public static Set<String> getClassDependencies(File classFile) throws AsmClassDepScannerException {
		try {				
			InputStream in = new FileInputStream(classFile);
			return getClassDependencies( in);
		} catch (FileNotFoundException e) {
			throw new AsmClassDepScannerException("cannot open class file stream as " + e, e);
		}		
	}
	
}
