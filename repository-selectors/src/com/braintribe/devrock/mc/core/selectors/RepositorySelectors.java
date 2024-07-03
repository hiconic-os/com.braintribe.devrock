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
package com.braintribe.devrock.mc.core.selectors;

import java.util.function.Function;

import com.braintribe.devrock.model.repositoryview.RepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.AllMatchingRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ByNameRegexRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ByNameRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ByTypeRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ConjunctionRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.DisjunctionRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.NegationRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.NoneMatchingRepositorySelector;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;

public class RepositorySelectors {

	private static PolymorphicDenotationMap<RepositorySelector, Function<RepositorySelector, RepositorySelectorExpert>> experts = new PolymorphicDenotationMap<>();

	static {
		addExpert(ByNameRepositorySelector.T, ByNameRepositorySelectorExpert::new);
		addExpert(ByNameRegexRepositorySelector.T, ByNameRegexRepositorySelectorExpert::new);
		addExpert(ByTypeRepositorySelector.T, ByTypeRepositorySelectorExpert::new);
		addExpert(DisjunctionRepositorySelector.T, DisjunctionRepositorySelectorExpert::new);
		addExpert(ConjunctionRepositorySelector.T, ConjunctionRepositorySelectorExpert::new);
		addExpert(NegationRepositorySelector.T, NegationRepositorySelectorExpert::new);
		addExpert(NoneMatchingRepositorySelector.T, selector -> NoneMatchingRepositorySelectorExpert.instance);
		addExpert(AllMatchingRepositorySelector.T, selector -> AllMatchingRepositorySelectorExpert.instance);
	}

	/**
	 * Creates and returns a {@link RepositorySelectorExpert repository expert} based on the passed
	 * <code>repository selector</code>. If no <code>repository selector</code> is specified (i.e. is
	 * <code>null</code>), the {@link ByNameRepositorySelectorExpert} is used as expert.
	 */
	public static RepositorySelectorExpert forDenotation(RepositorySelector repositorySelector) {
		if (repositorySelector != null) {
			return forDenotationRecursively(repositorySelector);
		}
		// for convenience we return a filter that matches everything in case no filter is specified
		return AllMatchingRepositorySelectorExpert.instance;
	}

	static RepositorySelectorExpert forDenotationRecursively(RepositorySelector repositorySelector) {
		if (repositorySelector == null) {
			throw new IllegalArgumentException(
					"Cannot return " + RepositorySelectorExpert.class.getSimpleName() + " for unspecified repository selector (i.e. <null>)!");
		}
		return experts.get(repositorySelector).apply(repositorySelector);
	}

	private static <F extends RepositorySelector> void addExpert(EntityType<F> entityType, Function<? super F, RepositorySelectorExpert> expert) {
		experts.put(entityType, (Function<RepositorySelector, RepositorySelectorExpert>) expert);
	}
}
