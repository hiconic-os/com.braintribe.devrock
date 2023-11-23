// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt;

import java.util.Collection;
import java.util.SortedSet;

public interface ModuleCheckProtocol {
	public String getModuleName();
	public Collection<String> getClassesDependingClass(String className);
	public SortedSet<ModuleClassDependency> getUnsatisfiedDependencies();
}
