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
package com.braintribe.devrock.zarathud.test;

import org.junit.Test;

import com.braintribe.devrock.zarathud.extracter.registry.BasicExtractionRegistry;
import com.braintribe.devrock.zarathud.extracter.registry.MethodSignature;

public class MethodSignatureAnalyzerLab {
	private void testDisemination(String ls) {
		BasicExtractionRegistry registry = new BasicExtractionRegistry();
		MethodSignature methodSignature = registry.extractMethodSignature( ls);
		System.out.println( methodSignature.returnType);
		System.out.println( methodSignature.argumentTypes);
	}
	
	@Test
	public void testParameterT() {
		String ls = "<T:Ljava/lang/Object;>()TT;";
		testDisemination( ls);
	}
	
	
	@Test
	public void testArgumentParameterT() {
		String ls = "<T:Ljava/lang/Object;>(TT;)Ljava/lang/String;";
		testDisemination( ls);
	}
	
	
	
	@Test
	public void testComplexArgumentParameterT() {
		String ls = "<T::Lcom/braintribe/devrock/test/zarathud/commons/CommonParameter;>(TT;)TT;";
		testDisemination( ls);
	}

	@Test
	public void testComplexArgumentParameterTE() {
		String ls = "<E:TT;>(TE;)TE;";
		testDisemination( ls);
	}
	
	//
	
	//
	
	
	@Test
	public void testComplexArgumentParameterTEX() {
		String ls = "<E:Ljava/lang/Object;>Ljava/util/List<TE;>;";
		testDisemination( ls);
	}
	
	
	@Test
	public void testParameterizedSignature() {
		String ls = "(Ljava/util/List<Lcom/braintribe/devrock/test/zarathud/four/FourParameter;>;Ljava/util/List<Lcom/braintribe/devrock/test/zarathud/two/TwoClass;>;)Ljava/util/List<Ljava/lang/String;>;";
		testDisemination( ls);
	}
	
}
