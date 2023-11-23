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
package com.braintribe.devrock.mc.core.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.braintribe.devrock.model.repository.filters.AllMatchingArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ConjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.GroupsArtifactFilter;
import com.braintribe.devrock.model.repository.filters.JunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.LockArtifactFilter;
import com.braintribe.devrock.model.repository.filters.NegationArtifactFilter;
import com.braintribe.devrock.model.repository.filters.NoneMatchingArtifactFilter;
import com.braintribe.devrock.model.repository.filters.QualifiedArtifactFilter;
import com.braintribe.devrock.model.repository.filters.StandardDevelopmentViewArtifactFilter;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;

/**
 * Creates {@link ArtifactFilterExpert}s for given {@link ArtifactFilter}s, see {@link #forDenotation(ArtifactFilter)}.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public class ArtifactFilters {

	private static PolymorphicDenotationMap<ArtifactFilter, Function<ArtifactFilter, ArtifactFilterExpert>> experts = new PolymorphicDenotationMap<>();

	static {
		addExpert(QualifiedArtifactFilter.T, QualifiedArtifactFilterExpert::new);
		addExpert(LockArtifactFilter.T, LockArtifactFilterExpert::new);
		addExpert(GroupsArtifactFilter.T, GroupsArtifactFilterExpert::new);
		addExpert(StandardDevelopmentViewArtifactFilter.T, ArtifactFilters::standardDevelopmentView);
		addExpert(AllMatchingArtifactFilter.T, (filter) -> AllMatchingArtifactFilterExpert.instance);
		addExpert(NoneMatchingArtifactFilter.T, (filter) -> NoneMatchingArtifactFilterExpert.instance);
		addExpert(NegationArtifactFilter.T, ArtifactFilters::negation);
		addExpert(DisjunctionArtifactFilter.T, ArtifactFilters::disjunction);
		addExpert(ConjunctionArtifactFilter.T, ArtifactFilters::conjunction);
	}

	/**
	 * Creates and returns a {@link ArtifactFilterExpert filter expert} based on the passed <code>filter</code>. If no
	 * <code>filter</code> is specified (i.e. <code>filter</code> is <code>null</code>), the
	 * {@link AllMatchingArtifactFilterExpert} is used as expert.
	 */
	public static ArtifactFilterExpert forDenotation(ArtifactFilter filter) {
		ArtifactFilterExpert filterExpert;
		if (filter != null) {
			filterExpert = forDenotationRecursively(filter);
		} else {
			// for convenience we return a filter that matches everything in case no filter is specified
			filterExpert = AllMatchingArtifactFilterExpert.instance;
		}

		// Instead of directly returning our filterExpert we add a wrapper which checks the
		// passed identifications and verifies that mandatory properties are set.
		ArtifactFilterExpert result = new IdentificationCheckingArtifactFilterExpertWrapper(filterExpert);

		return result;
	}
	
	static ArtifactFilterExpert forDenotationRecursively(ArtifactFilter filter) {
		if (filter == null) {
			throw new IllegalArgumentException(
					"Cannot return " + ArtifactFilterExpert.class.getSimpleName() + " for unspecified filter (i.e. <null>)!");
		}
		
		return experts.get(filter).apply(filter);
	}
	
	private static <F extends ArtifactFilter> void addExpert(EntityType<F> entityType, Function<? super F, ArtifactFilterExpert> expert) {
		experts.put(entityType, (Function<ArtifactFilter, ArtifactFilterExpert>) expert);
	}

	private static StandardDevelopmentViewArtifactFilterExpert standardDevelopmentView(StandardDevelopmentViewArtifactFilter filter) {
		if (filter.getRestrictOnArtifactInsteadOfGroupLevel() == null) {
			throw new IllegalArgumentException(
					"Cannot return " + ArtifactFilterExpert.class.getSimpleName() + " for filter " + filter + ": property " +
			StandardDevelopmentViewArtifactFilter.restrictOnArtifactInsteadOfGroupLevel + " is not specified (i.e. <null>)!");
		}
		ArtifactFilterExpert restrictionFilterExpert = ArtifactFilters.forDenotationRecursively(filter.getRestrictionFilter());

		return new StandardDevelopmentViewArtifactFilterExpert(restrictionFilterExpert, filter.getRestrictOnArtifactInsteadOfGroupLevel());
	}

	private static NegationArtifactFilterExpert negation(NegationArtifactFilter filter) {
		ArtifactFilterExpert filterExpertOperand = ArtifactFilters.forDenotationRecursively(filter.getOperand());
		return new NegationArtifactFilterExpert(filterExpertOperand);
	}

	private static DisjunctionArtifactFilterExpert disjunction(DisjunctionArtifactFilter filter) {
		return new DisjunctionArtifactFilterExpert(filterExpertOperands(filter));
	}

	private static ConjunctionArtifactFilterExpert conjunction(ConjunctionArtifactFilter filter) {
		return new ConjunctionArtifactFilterExpert(filterExpertOperands(filter));
	}

	private static List<ArtifactFilterExpert> filterExpertOperands(JunctionArtifactFilter filter) {
		List<ArtifactFilterExpert> filterExpertOperands = new ArrayList<ArtifactFilterExpert>();
		for (ArtifactFilter operand : filter.getOperands()) {
			ArtifactFilterExpert filterExpertOperand = ArtifactFilters.forDenotationRecursively(operand);
			filterExpertOperands.add(filterExpertOperand);
		}
		return filterExpertOperands;
	}

	/**
	 * A simple {@link ArtifactFilterExpert} wrapper which checks the passed identifications and verifies that mandatory
	 * properties (see {@link ArtifactFilterExpert}) are set. Otherwise it throws a {@link IllegalArgumentException}
	 * with a meaningful error message.
	 */
	private static class IdentificationCheckingArtifactFilterExpertWrapper implements ArtifactFilterExpert {

		final ArtifactFilterExpert delegate;

		private IdentificationCheckingArtifactFilterExpertWrapper(ArtifactFilterExpert delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean matchesGroup(String groupId) {
			if (groupId == null) {
				throw new IllegalArgumentException("The passed group id must not be null!");
			}

			return delegate.matchesGroup(groupId);
		}		
		
		@Override
		public boolean matches(ArtifactIdentification identification) {
			if (identification == null) {
				throw new IllegalArgumentException("The passed " + ArtifactIdentification.T.getShortName() + " must not be null!");
			}

			if (identification.getGroupId() == null) {
				throw new IllegalArgumentException("The group id of the passed " + ArtifactIdentification.T.getShortName() + " must not be null!");
			}

			if (identification.getArtifactId() == null) {
				throw new IllegalArgumentException("The artifact id of the passed " + ArtifactIdentification.T.getShortName() + " must not be null!");
			}

			return delegate.matches(identification);
		}

		@Override
		public boolean matches(CompiledArtifactIdentification identification) {
			if (identification == null) {
				throw new IllegalArgumentException("The passed " + CompiledArtifactIdentification.T.getShortName() + " must not be null!");
			}

			if (identification.getGroupId() == null) {
				throw new IllegalArgumentException(
						"The group id of the passed " + CompiledArtifactIdentification.T.getShortName() + " must not be null!");
			}

			if (identification.getArtifactId() == null) {
				throw new IllegalArgumentException(
						"The artifact id of the passed " + CompiledArtifactIdentification.T.getShortName() + " must not be null!");
			}

			if (identification.getVersion() == null) {
				throw new IllegalArgumentException(
						"The version of the passed " + CompiledArtifactIdentification.T.getShortName() + " must not be null!");
			}

			return delegate.matches(identification);
		}

		@Override
		public boolean matches(CompiledPartIdentification identification) {
			if (identification == null) {
				throw new IllegalArgumentException("The passed " + CompiledPartIdentification.T.getShortName() + " must not be null!");
			}

			if (identification.getGroupId() == null) {
				throw new IllegalArgumentException(
						"The group id of the passed " + CompiledPartIdentification.T.getShortName() + " must not be null!");
			}

			if (identification.getArtifactId() == null) {
				throw new IllegalArgumentException(
						"The artifact id of the passed " + CompiledPartIdentification.T.getShortName() + " must not be null!");
			}

			if (identification.getVersion() == null) {
				throw new IllegalArgumentException("The version of the passed " + CompiledPartIdentification.T.getShortName() + " must not be null!");
			}

			return delegate.matches(identification);
		}
	}
}
