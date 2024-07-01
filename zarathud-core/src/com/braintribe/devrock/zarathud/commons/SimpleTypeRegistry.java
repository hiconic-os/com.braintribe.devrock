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
package com.braintribe.devrock.zarathud.commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;

/**
 * little helper class to manage simple and collection types 
 * 
 * @author pit
 *
 */
public class SimpleTypeRegistry {
	private Map<String, String> nameToSimpleTypeSignatureMap = new HashMap<>();
	private Map<String, String> descToSimpleTypeSignatureMap = new HashMap<>();
	private List<String> collectionTypes = new ArrayList<>();
	private List<String> arrays = new ArrayList<>();
		
	private static SimpleTypeRegistry instance;
	private BasicModelOracle modelOracle;
	
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
	
	private void setup() {
		// only run it once
		if (nameToSimpleTypeSignatureMap.size () > 0)
			return;
		
		
		for (SimpleType simpleType : SimpleTypes.TYPES_SIMPLE) {
			GmSimpleType gmSimpleType = acquireRootModelOracle().findGmType( simpleType);
			String simpleTypeDesc = simpleType.getJavaType().getName().replace(".", "/");
			nameToSimpleTypeSignatureMap.put( simpleTypeDesc, gmSimpleType.getTypeSignature());					
		}
		
		// b) the base typ
		GmBaseType baseType = acquireRootModelOracle().findGmType( BaseType.INSTANCE); 
		nameToSimpleTypeSignatureMap.put("java/lang/Object", baseType.getTypeSignature());
		
		// simplest types
		descToSimpleTypeSignatureMap.put("I", "java/lang/Integer");
		descToSimpleTypeSignatureMap.put("J", "java/lang/Long");
		
		descToSimpleTypeSignatureMap.put("F", "java/lang/Float");
		descToSimpleTypeSignatureMap.put("D", "java/lang/Double");
		
		descToSimpleTypeSignatureMap.put("Z", "java/lang/Boolean");
		descToSimpleTypeSignatureMap.put("B", "java/lang/Byte");
		
		descToSimpleTypeSignatureMap.put("C", "java/lang/Char");
		descToSimpleTypeSignatureMap.put("S", "java/lang/Short");
		descToSimpleTypeSignatureMap.put("Object", "java/lang/Object");
		
		
		String [] collectionTypes = {"java/util/List", "java/util/Set", "java/util/Map"};
		for (String type : collectionTypes) {
			this.collectionTypes.add( type);
		}
		
		arrays.add( "[");
		arrays.add( "[[");
	}
	
	private static SimpleTypeRegistry getInstance() {
		if (instance == null) {
			instance = new SimpleTypeRegistry();
			instance.setup();
		}
		return instance;
	}
	
	public static String getSimpleTypeFromDesc( String desc) {		
		if (!desc.startsWith("L")) {
			return getInstance().descToSimpleTypeSignatureMap.get( desc);
		}
		else {		
			String key = desc.substring(1, desc.length()-1);
			return getInstance().descToSimpleTypeSignatureMap.get( key);
		}
	}
	public static boolean isCodeForSimpleType( String desc) {
		if (!desc.startsWith( "L")) {
			return getInstance().descToSimpleTypeSignatureMap.containsKey(desc);
		}
		else {
			String key = desc.substring(1, desc.length()-1);
			return getInstance().descToSimpleTypeSignatureMap.containsKey(key);
		}
	}
	
	public static String getSimpleTypeSignatureFromDesc( String desc) {		
		if (!desc.startsWith( "L")) {
			return getInstance().nameToSimpleTypeSignatureMap.get( desc);
		}
		else {
			String key = desc.substring(1, desc.length()-1);
			return getInstance().nameToSimpleTypeSignatureMap.get( key);
		}
	}
	
	public static boolean isSimpleType( String type) {
		return getInstance().nameToSimpleTypeSignatureMap.containsKey( type);
	}
	
	public static boolean isCollectionType( String type) {
		return getInstance().collectionTypes.contains(type);
	}
	
	public static boolean isArray( String type) {
		// [I, [[I... 
		return getInstance().arrays.contains(type.substring(0, type.length()-1));
	}
	
	/**
	 * returns a list of type signatures, linear collections have one entry, maps have two 
	 * @param collectionType - the detected collection type (list, set, map)
	 * @param fullSignature - the full signature to be scanned 
	 * @return - a list with the maximum two signatures of the elements of the colleciton 
	 */
	@Deprecated
	public static List<String> getCollectionElementTypes( String collectionType, String fullSignature) {
		// ()Ljava/util/List<Lcom/braintribe/model/malaclypse/cfg/Repository;>;
		// ()Ljava/util/Map<Lcom/braintribe/test/model/SimpleTypeEntity;Lcom/braintribe/test/model/SimpleTypeEntity;>;
				 		
		String signature;
		
		// drop ()L or L
		if (fullSignature.startsWith( "()")) {
			signature = fullSignature.substring( 3);			
		}
		else {
			signature = fullSignature.substring( 1);
		}
		// drop ;
		signature = signature.substring(0, signature.length()-1);
						
		// use a tokenizer to check all values - also the map.
		List<String> result = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer( signature, "<>;");		
		while (tokenizer.hasMoreTokens()) {
			String sig = tokenizer.nextToken();
			if (sig.startsWith( "L")) {
				sig = sig.substring(1, sig.length());
			}
			if (SimpleTypeRegistry.isSimpleType(sig) == false) 
				result.add( sig.replace("/", "."));	
		}
		return result;
	}
	

}
