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
package com.braintribe.devrock.zarathud.extracter.scanner;

import java.util.List;
import java.util.Set;

public class MethodData {

	private String methodName;
	
	private String signature;
	private String desc;
	private String returnType;
	private List<String> argumentTypes;
	private List<String> exceptions;
	private Set<String> annotations;
	
	private boolean staticNature;
	private boolean synchronizedNature;
	private boolean abstractNature;
	private AccessModifier accessNature;
	
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
		
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getReturnType() {
		return returnType;
	}
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
	public List<String> getArgumentTypes() {
		return argumentTypes;
	}
	public void setArgumentTypes(List<String> argumentTypes) {
		this.argumentTypes = argumentTypes;
	}
	public List<String> getExceptions() {
		return exceptions;
	}
	public void setExceptions(List<String> exceptions) {
		this.exceptions = exceptions;
	}
	public boolean getStaticNature() {
		return staticNature;
	}
	public void setStaticNature(boolean staticNature) {
		this.staticNature = staticNature;
	}
	public boolean getSynchronizedNature() {
		return synchronizedNature;
	}
	public void setSynchronizedNature(boolean synchronizedNature) {
		this.synchronizedNature = synchronizedNature;
	}
	public boolean getAbstractNature() {
		return abstractNature;
	}
	public void setAbstractNature(boolean abstractNature) {
		this.abstractNature = abstractNature;
	}
	public AccessModifier getAccessNature() {
		return accessNature;
	}
	public void setAccessNature(AccessModifier accessNature) {
		this.accessNature = accessNature;
	}
	public Set<String> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(Set<String> annotations) {
		this.annotations = annotations;
	}
		
}
