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

public class ClassData {

	private AccessModifier accessNature;
	private boolean staticNature;
	private boolean abstractNature;
	private boolean synchronizedNature;
	private Set<String> annotations;
	private List<FieldData> fieldData;
	
	public AccessModifier getAccessNature() {
		return accessNature;
	}
	public void setAccessNature(AccessModifier accessNature) {
		this.accessNature = accessNature;
	}
	public boolean getStaticNature() {
		return staticNature;
	}
	public void setStaticNature(boolean staticNature) {
		this.staticNature = staticNature;
	}
	public boolean getAbstractNature() {
		return abstractNature;
	}
	public void setAbstractNature(boolean abstractNature) {
		this.abstractNature = abstractNature;
	}
	public boolean getSynchronizedNature() {
		return synchronizedNature;
	}
	public void setSynchronizedNature(boolean synchronizedNature) {
		this.synchronizedNature = synchronizedNature;
	}
	public Set<String> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(Set<String> annotations) {
		this.annotations = annotations;
	}
	public List<FieldData> getFieldData() {
		return fieldData;
	}
	public void setFieldData(List<FieldData> fieldData) {
		this.fieldData = fieldData;
	}
	
	
}
