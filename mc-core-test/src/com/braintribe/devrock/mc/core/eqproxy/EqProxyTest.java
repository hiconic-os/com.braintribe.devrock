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
package com.braintribe.devrock.mc.core.eqproxy;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.version.Version;

public class EqProxyTest {
	@Test
	public void testConcurrentHashSet() {
		Set<EqProxy<CompiledDependency>> deps = ConcurrentHashMap.newKeySet();
		
		CompiledDependency d1 = CompiledDependency.create("foo", "fix", Version.parse("1.0"), "provided", null, "jar");
		CompiledDependency d2 = CompiledDependency.create("foo", "foxy", Version.parse("1.0"), "runtime", null, "jar");
		CompiledDependency d3 = CompiledDependency.create("foo", "bar", Version.parse("1.0"), "provided", null, "jar");
		CompiledDependency d4 = CompiledDependency.create("foo", "foxy", Version.parse("1.0"), "compile", null, "jar");
		
		EqProxy<CompiledDependency> p1 = HashComparators.scopelessCompiledDependency.eqProxy(d1);
		EqProxy<CompiledDependency> p2 = HashComparators.scopelessCompiledDependency.eqProxy(d2);
		EqProxy<CompiledDependency> p3 = HashComparators.scopelessCompiledDependency.eqProxy(d3);
		EqProxy<CompiledDependency> p4 = HashComparators.scopelessCompiledDependency.eqProxy(d4);
		
		deps.add(p1);
		deps.add(p2);
		deps.add(p3);
		deps.add(p4);
		
		Assertions.assertThat(deps.size()).isEqualTo(3);
	}
}
