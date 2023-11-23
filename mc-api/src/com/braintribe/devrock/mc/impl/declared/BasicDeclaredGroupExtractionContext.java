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
package com.braintribe.devrock.mc.impl.declared;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.declared.DeclaredGroupExtractionContext;
import com.braintribe.devrock.mc.api.declared.DeclaredGroupExtractionContextBuilder;

public class BasicDeclaredGroupExtractionContext implements DeclaredGroupExtractionContext, DeclaredGroupExtractionContextBuilder {
	
	private String location;
	private boolean includeMemberArtifacts;
	private boolean includeParentArtifact;
	private List<Pair<String, Map<String, String>>> mgtDependentDeps;
	private boolean simplyRange;
	private boolean sort;
	private Predicate<String> inclusionFilter;
	private Predicate<String> exclusionFilter;
	private boolean enforceRanges;
	private boolean includeSelfreferences;

	@Override
	public String getGroupLocation() {	
		return location;
	}

	/* 
	 * location 
	 */
	@Override
	public DeclaredGroupExtractionContextBuilder location(String location) {
		this.location = location;
		return this;
	}
	@Override
	public DeclaredGroupExtractionContextBuilder location(File location) {
		this.location = location.getAbsolutePath();
		return this;
	}

	/*
	 * include member artifacts in the output 
	 */
	@Override
	public boolean includeMembers() {
		return includeMemberArtifacts;
	}
	@Override
	public DeclaredGroupExtractionContextBuilder includeMembers(boolean include) {
		includeMemberArtifacts = include;
		return this;
	}
	/*
	 * include parent artifact in output 
	 */
	@Override
	public DeclaredGroupExtractionContextBuilder includeParent(boolean include) {
		includeParentArtifact = include;
		return this;
	}
	@Override
	public boolean includeParent() {
		return includeParentArtifact;
	}

	/*
	 * range simplification 
	 */
	@Override
	public DeclaredGroupExtractionContextBuilder simplifyRange(boolean simplifyRange) {	
		this.simplyRange = simplifyRange;
		return this;
	}	
	@Override
	public boolean simplifyRangeToLowerBoundary() {
		return simplyRange;
	}

	
	/*
	 * sorting - group versions, members (if any)
	 */
	@Override
	public DeclaredGroupExtractionContextBuilder sort(boolean sort) {	
		this.sort = sort;
		return this;
	}	
	@Override
	public boolean sort() {	
		return sort;
	}
	
	

	@Override
	public DeclaredGroupExtractionContextBuilder enforceRanges(boolean enforceRanges) {
		this.enforceRanges = enforceRanges;
		return this;
	}

	@Override
	public boolean enforceRanges() { 
		return enforceRanges;
	}

	/*
	 * inclusion filters
	 */
	@Override
	public DeclaredGroupExtractionContextBuilder inclusions(Predicate<String> inclusionFilter) {
		if (this.inclusionFilter == null) {
			this.inclusionFilter = inclusionFilter;
		}
		else {
			this.inclusionFilter = this.inclusionFilter.or(inclusionFilter);
		}
		return this;
	}
	@Override
	public DeclaredGroupExtractionContextBuilder inclusions(String regexp) {
		Predicate<String> filter = new Predicate<String>() {
			@Override
			public boolean test(String t) {			
				return t.matches(regexp);
			}			
		};		
		return inclusions(filter);
	}
	@Override
	public Predicate<String> inclusionFilter() {
		if (this.inclusionFilter != null)
			return inclusionFilter;
		return (k) -> true;
	}

	/*
	 * exclusion filters
	 */
	@Override
	public DeclaredGroupExtractionContextBuilder exclusions(Predicate<String> exclusionFilter) {
		if (this.exclusionFilter == null) {
			this.exclusionFilter = exclusionFilter;
		}
		else {
			this.exclusionFilter = this.exclusionFilter.or(exclusionFilter);
		}		
		return this;
	}
	@Override
	public DeclaredGroupExtractionContextBuilder exclusions(String regexp) {
		Predicate<String> filter = new Predicate<String>() {
			@Override
			public boolean test(String t) {			
				return t.matches(regexp);
			}			
		};		
		return exclusions(filter);			
	}
	@Override
	public Predicate<String> exclusionFilter() {
		if (this.exclusionFilter != null)
			return exclusionFilter;
		return (k) -> false;
	}
	
	

	@Override
	public DeclaredGroupExtractionContextBuilder includeSelfreferences(boolean includeSelfreferences) {
		
		this.includeSelfreferences = includeSelfreferences;
		return this;
	}

	@Override
	public boolean includeSelfreferences() {
		return includeSelfreferences;
	}

	/*
	 * internal - dependencies that require depmgt sections (incomplete)
	 */
	@Override
	public List<Pair<String, Map<String, String>>> getManagementDependentDependencies() {		
		return mgtDependentDeps;
	}
	@Override
	public void setManagementDependentDependencies(List<Pair<String, Map<String, String>>> deps) {
		this.mgtDependentDeps = deps;	
	}

	@Override
	public DeclaredGroupExtractionContext done() {
		return this;
	}

	
	
}
