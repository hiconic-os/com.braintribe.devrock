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
