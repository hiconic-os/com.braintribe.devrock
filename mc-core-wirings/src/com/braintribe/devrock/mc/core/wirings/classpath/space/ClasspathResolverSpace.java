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
package com.braintribe.devrock.mc.core.wirings.classpath.space;

import com.braintribe.devrock.mc.core.resolver.classpath.BasicClasspathDependencyResolver;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

/**
 * implementation of the {@link ClasspathResolverContract}
 * @author pit / dirk
 *
 */
@Managed
public class ClasspathResolverSpace implements ClasspathResolverContract {

	@Import
	private TransitiveResolverContract transitiveResolver;
	
	@Override
	@Managed
	public BasicClasspathDependencyResolver classpathResolver() {
		BasicClasspathDependencyResolver bean = new BasicClasspathDependencyResolver();
		bean.setPartEnricher(transitiveResolver.dataResolverContract().partEnricher());
		bean.setTransitiveDependencyResolver(transitiveResolver.transitiveDependencyResolver());
		return bean;
	}

	@Override
	public TransitiveResolverContract transitiveResolverContract() {	
		return transitiveResolver;
	}
	

}
