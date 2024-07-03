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
