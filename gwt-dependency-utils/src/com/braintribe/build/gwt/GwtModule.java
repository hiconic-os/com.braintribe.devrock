// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


/**
 * 
 */
package com.braintribe.build.gwt;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Document;

public class GwtModule implements Comparable<GwtModule> {
	private String sourcePackage;
	private Set<String> superSourcePackages = new HashSet<String>();
	private String moduleName;
	private Set<GwtModule> inheritedModules = new TreeSet<GwtModule>();
	private Document document;
	private boolean explicitSourcePackage;
	
	public GwtModule(String moduleName) {
		super();
		this.moduleName = moduleName;
	}

	public void setExplicitSourcePackage(boolean explicitSourcePackage) {
		this.explicitSourcePackage = explicitSourcePackage;
	}

	public boolean isExplicitSourcePackage() {
		return explicitSourcePackage;
	}
	
	
	public void setDocument(Document document) {
		this.document = document;
	}
	
	public Document getDocument() {
		return document;
	}
	
	public void setSourcePackage(String sourcePackage) {
		this.sourcePackage = sourcePackage;
	}
	
	public void addInheritedModules(Set<GwtModule> inheritedModules) {
		this.inheritedModules.addAll(inheritedModules);
	}
	
	public String getSourcePackage() {
		return sourcePackage;
	}
	
	public String getModuleName() {
		return moduleName;
	}
	
	public Set<GwtModule> getInheritedModules() {
		return inheritedModules;
	}
	
	@Override
	public String toString() {
		return getModuleName() + "[sourcePackage=" + getSourcePackage() + "]";
	}
	
	public int compareTo(GwtModule o) {
		return toString().compareTo(o.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((moduleName == null) ? 0 : moduleName.hashCode());
		result = prime * result
				+ ((sourcePackage == null) ? 0 : sourcePackage.hashCode());
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
		GwtModule other = (GwtModule) obj;
		if (moduleName == null) {
			if (other.moduleName != null)
				return false;
		} else if (!moduleName.equals(other.moduleName))
			return false;
		if (sourcePackage == null) {
			if (other.sourcePackage != null)
				return false;
		} else if (!sourcePackage.equals(other.sourcePackage))
			return false;
		return true;
	}
	
	
	
}
