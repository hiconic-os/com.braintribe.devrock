// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
