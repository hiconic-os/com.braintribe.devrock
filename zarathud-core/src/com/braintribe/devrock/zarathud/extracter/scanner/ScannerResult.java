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

import java.util.ArrayList;
import java.util.List;

/**
 * contains the result of {@link BasicResourceScanner#scanJar(java.io.File)}'s run }
 * @author pit
 *
 */
public class ScannerResult {

	private List<String> classes = new ArrayList<String>();
	private String moduleName;
	
	/**
	 * the names of the classes within the {@link ScannerResult}
	 * @return - a {@link List} of {@link String} with the name of the class
	 */
	public List<String> getClasses() {
		return classes;
	}
	public void setClasses(List<String> classes) {
		this.classes = classes;
	}
	/**
	 * the associated gwt module name of the {@link ScannerResult}
	 * @return
	 */
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	/**
	 * merges a second {@link ScannerResult} into the existing one
	 * @param result - the {@link ScannerResult} to merge
	 * @return - this, i.e. the existing {@link ScannerResult}
	 */
	public ScannerResult merge( ScannerResult result) {
		classes.addAll(result.getClasses());
		String moduleName = result.getModuleName();
		if (moduleName != null)
			this.moduleName = moduleName;
		return this;
	}
}

