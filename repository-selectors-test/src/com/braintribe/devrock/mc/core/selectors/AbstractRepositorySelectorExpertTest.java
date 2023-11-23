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

import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repositoryview.RepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ByNameRegexRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ByNameRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ByTypeRepositorySelector;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Abstract super class for {@link RepositorySelectorExpert} tests.
 *
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 */
public abstract class AbstractRepositorySelectorExpertTest {
	
	protected RepositorySelector nameSelector(String name) {
		ByNameRepositorySelector repositorySelector = ByNameRepositorySelector.T.create();
		repositorySelector.setName(name);
		return repositorySelector;
	}

	protected RepositorySelector regexNameSelector(String regex) {
		ByNameRegexRepositorySelector repositorySelector = ByNameRegexRepositorySelector.T.create();
		repositorySelector.setRegex(regex);
		return repositorySelector;
	}
	
	protected <R extends Repository> RepositorySelector typeSelector(EntityType<R> type, boolean includeSubTypes) {
		ByTypeRepositorySelector byTypeRepositorySelector = ByTypeRepositorySelector.T.create();
		byTypeRepositorySelector.setType(type.getShortName());
		byTypeRepositorySelector.setIncludeSubtypes(includeSubTypes);
		return byTypeRepositorySelector;
	}

	protected Repository createRepository(EntityType<? extends Repository> type, String name) {
		Repository repo = type.create();
		repo.setName(name);
		return repo;
	}

	protected List<Repository> createRepositories(EntityType<? extends Repository> type, String... names) {
		return CommonTools.getList(names).stream().map(name -> this.createRepository(type, name)).collect(Collectors.toList());
	}
	
	protected List<Repository> createRepositories(String... names) {
		return CommonTools.getList(names).stream().map(name -> {
			Repository repository = Repository.T.create();
			repository.setName(name);
			return repository;
		}).collect(Collectors.toList());
	}
}
