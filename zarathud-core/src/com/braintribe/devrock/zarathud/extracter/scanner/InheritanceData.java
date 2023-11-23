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

public class InheritanceData {

		private String name = null;
		private String superClass;
		private List<String> interfaces;
		private boolean abstractClass = false;
		private boolean interfaceClass = false;		
		
		public String getSuperClass() {
			return superClass;
		}
		public void setSuperClass(String superClass) {
			this.superClass = superClass;
		}
		public List<String> getInterfaces() {
			return interfaces;
		}
		public void setInterfaces(List<String> interfaces) {
			this.interfaces = interfaces;
		}
		public boolean isAbstractClass() {
			return abstractClass;
		}
		public void setAbstractClass(boolean abstractClass) {
			this.abstractClass = abstractClass;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public boolean isInterfaceClass() {
			return interfaceClass;
		}
		public void setInterfaceClass(boolean interfaceClass) {
			this.interfaceClass = interfaceClass;
		}
		
		
		
		
}
