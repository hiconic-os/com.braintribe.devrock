// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt;


public class ModuleClassDependency {
	private String className;
	private String inheritedFrom;
	private String pathToSource;
	private boolean emulation;
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getInheritedFrom() {
		return inheritedFrom;
	}
	public void setInheritedFrom(String inheritedFrom) {
		this.inheritedFrom = inheritedFrom;
	}
	public String getPathToSource() {
		return pathToSource;
	}
	public void setPathToSource(String pathToSource) {
		this.pathToSource = pathToSource;
	}
	public boolean isEmulation() {
		return emulation;
	}
	
	public void setEmulation(boolean emulation) {
		this.emulation = emulation;
	}
}
