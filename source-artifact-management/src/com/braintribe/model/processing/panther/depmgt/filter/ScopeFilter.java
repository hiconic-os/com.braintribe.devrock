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
package com.braintribe.model.processing.panther.depmgt.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.model.artifact.Dependency;

public class ScopeFilter implements Predicate<Dependency> {

	private Set<String> includedScopes;
	private Set<String> excludedScopes;
	private Set<String> includedPackaging;
	private Set<String> excludedPackaging;
	private boolean includeNullScope = true;
	private boolean includeNullPackaging = true;
	private boolean optional = true;
	
	private ScopeFilter() {
		//prevent direct instantiation
	}
	
	public static ScopeFilter create(boolean includeNullScope, boolean includeNullPackaging) {
		ScopeFilter sf = new ScopeFilter();
		if (includeNullScope) {
			sf = sf.includeScope((String) null);
		} else {
			sf = sf.excludeScope((String) null);
		}
		if (includeNullPackaging) {
			sf = sf.includePackaging((String) null);
		} else {
			sf = sf.excludePackaging((String) null);
		}
		return sf;
	}
	
	public ScopeFilter includePackaging(String... packaging) {
		if (packaging == null) {
			includeNullPackaging = true;
			return this;
		}
		if (includedPackaging == null) {
			includedPackaging = new HashSet<String>();
		}
		for (String s : packaging) {
			if (s == null) {
				includeNullPackaging = true;
			} else {
				includedPackaging.add(s.toLowerCase().trim());
			}
		}
		return this;
	}
	
	public ScopeFilter excludePackaging(String... packaging) {
		if (packaging == null) {
			includeNullPackaging = false;
			return this;
		}
		if (excludedPackaging == null) {
			excludedPackaging = new HashSet<String>();
		}
		for (String s : packaging) {
			if (s == null) {
				includeNullPackaging = false;
			} else {
				excludedPackaging.add(s.toLowerCase().trim());
			}
		}
		return this;
	}
	
	public ScopeFilter includeScope(String... scope) {
		if (scope == null) {
			includeNullScope = true;
			return this;
		}
		if (includedScopes == null) {
			includedScopes = new HashSet<String>();
		}
		for (String s : scope) {
			if (s == null) {
				includeNullScope = true;
			} else {
				includedScopes.add(s.toLowerCase().trim());
			}
		}
		return this;
	}
	public ScopeFilter excludeScope(String... scope) {
		if (scope == null) {
			includeNullScope = false;
			return this;
		}
		if (excludedScopes == null) {
			excludedScopes = new HashSet<String>();
		}
		for (String s : scope) {
			if (s == null) {
				includeNullScope = false;
			} else {
				excludedScopes.add(s.toLowerCase().trim());
			}
		}
		return this;
	}
	
	public ScopeFilter optional(boolean optional) {
		this.optional = optional;
		return this;
	}

	@Override
	public boolean test(Dependency dep) {
		if (dep == null) {
			return false;
		}
		boolean allowedByScope = true;
		String scope = dep.getScope();
		if (scope != null) {
			scope = scope.toLowerCase().trim();
			boolean included = (includedScopes != null && !includedScopes.isEmpty() ? includedScopes.contains(scope) || includedScopes.contains("*") : true);
			boolean excluded = (excludedScopes != null && !excludedScopes.isEmpty() ? excludedScopes.contains(scope) : false);
			allowedByScope = included && !excluded;
		} else {
			allowedByScope = includeNullScope;
		}
		
		boolean allowedByPackaging = true;
		String packaging = dep.getPackagingType();
		if (packaging != null) {
			packaging = packaging.toLowerCase().trim();
			boolean included = (includedPackaging != null && !includedPackaging.isEmpty() ? includedPackaging.contains(packaging) || includedPackaging.contains("*") : true);
			boolean excluded = (excludedPackaging != null && !excludedPackaging.isEmpty() ? excludedPackaging.contains(packaging) : false);
			allowedByPackaging = included && !excluded;
		} else {
			allowedByPackaging = includeNullPackaging;
		}
		
		boolean allowedByOptional = true;
		if (dep.getOptional()) {
			if (!optional) {
				allowedByOptional = false;
			}
		}
		
		//dep.getPackagingType()
		
		return allowedByScope && allowedByOptional && allowedByPackaging;
	}

	public static ScopeFilter standardScopes() {
		return ScopeFilter.create(true, true).includeScope("runtime", "compile").includePackaging(null, "jar", "bundle", "pom").optional(false);
	}
	public static ScopeFilter allScopes() {
		return ScopeFilter.create(true, true).includeScope("*").includePackaging("*").optional(true);
	}

}
