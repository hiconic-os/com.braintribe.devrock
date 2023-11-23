// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.scope;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Exclusion;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.DependencyScope;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.ScopeTreatement;

/**
 * the instance that decides whether the passed scope is included or excluded in the walk  
 * 
 * @author pit
 *
 */
public class ScopeControl {
	private static final String SCOPE_PROVIDED = "PROVIDED";
	private static final String SCOPE_COMPILE = "COMPILE";
	
	private Map<String, DependencyScope> scopes = new HashMap<String, DependencyScope>();
	
	private boolean skipOptional = true;
	private boolean passthru = false;
	
	@Configurable
	public void setSkipOptional(boolean skipOptional) {
		this.skipOptional = skipOptional;
	}
	@Configurable
	public void addScope( DependencyScope scope) {
		scopes.put( scope.getName().toUpperCase(), scope);
	}
	
	@Configurable
	public void setPassthru(boolean passthru) {
		this.passthru = passthru;
	}
	public boolean isExcluded(String scopeName, int level, boolean optional) {
		// if set to pass through, everything's in 
		if (passthru) {
			return false;
		}
		if (level > 0 && optional && skipOptional)
			return true;
		if (scopeName == null) {
			scopeName = SCOPE_COMPILE;
		}
		else { // remove a preceeding or trailing blanks
			scopeName = scopeName.trim();
		}
		
		// no matter what mode we're processing, if it's a provide scope, it's only allowed for the terminal.
		if (scopeName.equalsIgnoreCase( SCOPE_PROVIDED) && level > 0) {
			return true;
		}
		DependencyScope scope = scopes.get(scopeName.toUpperCase());
		if (scope == null)
			return true;
		switch (scope.getScopeTreatement()) {
			case INCLUDE:
				return false;
			case EXCLUDE:
			default:
				return true;
		}
	}

	/**
	 * called if level == 0 (terminal level), turns dependency scoped as "provided" into exclusions  
	 * @param dependenciesToIterate - a {@link List} of {@link Dependency}
	 * @return
	 */
	public Set<Exclusion> infereExclusionsFromDependencies(List<Dependency> dependenciesToIterate) {
		// if set to pass through, no induced exclusions
		if (passthru) {
			return null;
		}
		Set<Exclusion> result = new HashSet<>();
		for (Dependency dependency : dependenciesToIterate) {
			String scope = dependency.getScope();
			// not defined -> compile scope 
			if (scope == null) {
				scope = SCOPE_COMPILE;
			}
			DependencyScope dependencyScope = scopes.get(scope.toUpperCase());
			// dependency scope is declared? 
			if (dependencyScope != null) {
				// is included, skip
				if (dependencyScope.getScopeTreatement() == ScopeTreatement.INCLUDE) {
					continue;
				}
			}
			// neither declared nor declared with INCLUDE -> add to exclusion list
			Exclusion exclusion = Exclusion.T.create();
			exclusion.setGroupId( dependency.getGroupId());
			exclusion.setArtifactId( dependency.getArtifactId());
			result.add(exclusion);
			
		}		
		return result.size() > 0 ? result : null;
	}
	
	
	
}
