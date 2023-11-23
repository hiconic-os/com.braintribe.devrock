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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Predicate;

import org.junit.Test;

import com.braintribe.model.artifact.Dependency;

public class ScopeFilterTest {

	@Test
	public void testStandardScopesFilter() throws Exception {
		Predicate<Dependency> filter = ScopeFilter.standardScopes();
		Dependency d;
		
		d = Dependency.T.create();
		d.setScope("runtime");
		
		assertThat(filter.test(d)).isEqualTo(true);
		
		d = Dependency.T.create();
		d.setScope("optional");
		
		assertThat(filter.test(d)).isEqualTo(false);

		d = Dependency.T.create();
		
		assertThat(filter.test(d)).isEqualTo(true);

	}

	@Test
	public void testAllScopesFilter() throws Exception {

		Predicate<Dependency> filter = ScopeFilter.allScopes();
		Dependency d;
		
		d = Dependency.T.create();
		d.setScope("runtime");
		
		assertThat(filter.test(d)).isEqualTo(true);
		
		d = Dependency.T.create();
		d.setScope("optional");
		
		assertThat(filter.test(d)).isEqualTo(true);
	
		d = Dependency.T.create();
		
		assertThat(filter.test(d)).isEqualTo(true);
	}

	
	@Test
	public void testScopeFilterOptionalOnly() throws Exception {
		
		Predicate<Dependency> filter = ScopeFilter.create(false, true).includeScope("optional");
		
		Dependency d = Dependency.T.create();
		
		assertThat(filter.test(d)).isEqualTo(false);
		
		d = Dependency.T.create();
		d.setScope("optional");
		
		assertThat(filter.test(d)).isEqualTo(true);

		d = Dependency.T.create();
		d.setScope("runtime");
		
		assertThat(filter.test(d)).isEqualTo(false);

	}

	@Test
	public void testScopeFilterOptionaAndNull() throws Exception {
		
		Predicate<Dependency> filter = ScopeFilter.create(true, true).includeScope("optional");
		
		Dependency d = Dependency.T.create();
		
		assertThat(filter.test(d)).isEqualTo(true);
		
		d = Dependency.T.create();
		d.setScope("optional");
		
		assertThat(filter.test(d)).isEqualTo(true);

		d = Dependency.T.create();
		d.setScope("runtime");
		
		assertThat(filter.test(d)).isEqualTo(false);

	}

	@Test
	public void testPackaging() throws Exception {
		
		Predicate<Dependency> filter = ScopeFilter.create(true, false).includeScope("*").includePackaging("pom");
		
		Dependency d = Dependency.T.create();
		d.setPackagingType("pom");
		
		assertThat(filter.test(d)).isEqualTo(true);
		
		d.setPackagingType(null);
		
		assertThat(filter.test(d)).isEqualTo(false);
	}
}
