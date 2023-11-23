// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

class ModuleCheckProtocolImpl implements ModuleCheckProtocol {
	private String moduleName;
	private Multimap<String, String> classDependencies = HashMultimap.create();
	private SortedSet<ModuleClassDependency> unsatisfiedDependencies = new TreeSet<ModuleClassDependency>(new Comparator<ModuleClassDependency>() {
		@Override
		public int compare(ModuleClassDependency o1, ModuleClassDependency o2) {
			return o1.getClassName().compareToIgnoreCase(o2.getClassName());
		}
	}); 
	
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public String getModuleName() {
		return moduleName;
	}
	
	@Override
	public Collection<String> getClassesDependingClass(String className) {
		return classDependencies.get(className);
	}
	
	public void registerClassDependency(String classDepending, String classDepended) {
		classDependencies.put(classDepended, classDepending);
	}
	
	public void registerClassDependencies(String classDepending, Iterable<String> classesDepended) {
		for (String classDepended: classesDepended)
			classDependencies.put(classDepended, classDepending);
	}
	
	public void addAll(Collection<? extends ModuleClassDependency> unsatisfied) {
		unsatisfiedDependencies.addAll(unsatisfied);
	}
	
	public SortedSet<ModuleClassDependency> getUnsatisfiedDependencies() {
		return unsatisfiedDependencies;
	}
}
