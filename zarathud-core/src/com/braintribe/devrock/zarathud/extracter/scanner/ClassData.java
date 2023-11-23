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
