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
package com.braintribe.devrock.mc.core.configuration;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.braintribe.devrock.mc.api.repository.RepositoryProbingSupport;
import com.braintribe.devrock.mc.api.repository.configuration.ArtifactChangesSynchronization;
import com.braintribe.devrock.mc.core.filters.ArtifactFilters;
import com.braintribe.devrock.model.repository.IndexedMavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.AllMatchingArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.AutoArtifactFilter;
import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.GroupsArtifactFilter;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.changes.RepositoryProbingResult;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

public class RepositoryConfigurationAutoFilterTest {
	@Rule
	public TemporaryFolder temporary = new TemporaryFolder();

	@Test
	public void explicitAllAllowsUnindexedArtifactsButStillInvalidatesChanges() {
		Synchronization changes = new Synchronization();
		AllMatchingArtifactFilter configured = AllMatchingArtifactFilter.T.create();
		Repository resolved = probe(configured, changes);
		assertTrue(ArtifactFilters.forDenotation(resolved.getArtifactFilter()).matchesGroup("unindexed"));
		assertEquals(1, changes.queries);
		assertEquals(changes.changed, changes.purged.get(resolved));
	}

	@Test
	public void unspecifiedFilterStillUsesIndex() {
		Repository resolved = probe(null, new Synchronization());
		assertTrue(ArtifactFilters.forDenotation(resolved.getArtifactFilter()).matchesGroup("indexed"));
		assertFalse(ArtifactFilters.forDenotation(resolved.getArtifactFilter()).matchesGroup("unindexed"));
	}

	@Test
	public void nestedAutoExtendsIndexAndLeavesConfigurationUntouched() {
		DisjunctionArtifactFilter configured = DisjunctionArtifactFilter.T.create();
		AutoArtifactFilter auto = AutoArtifactFilter.T.create();
		configured.getOperands().add(auto);
		GroupsArtifactFilter extra = GroupsArtifactFilter.T.create();
		extra.getGroups().add("extra");
		configured.getOperands().add(extra);
		Repository resolved = probe(configured, new Synchronization());
		assertTrue(ArtifactFilters.forDenotation(resolved.getArtifactFilter()).matchesGroup("indexed"));
		assertTrue(ArtifactFilters.forDenotation(resolved.getArtifactFilter()).matchesGroup("extra"));
		assertFalse(ArtifactFilters.forDenotation(resolved.getArtifactFilter()).matchesGroup("unindexed"));
		assertSame(auto, configured.getOperands().get(0));
	}

	@Test
	public void unchangedIndexStillResolvesAuto() {
		Synchronization changes = new Synchronization();
		changes.changed = Collections.emptyList();
		Repository resolved = probe(AutoArtifactFilter.T.create(), changes);
		assertFalse(ArtifactFilters.forDenotation(resolved.getArtifactFilter()).matchesGroup("unindexed"));
	}

	@Test
	public void unsupportedIndexLeavesAutoAsPassThrough() {
		Synchronization changes = new Synchronization();
		changes.changed = null;
		Repository resolved = probe(AutoArtifactFilter.T.create(), changes);
		assertTrue(ArtifactFilters.forDenotation(resolved.getArtifactFilter()).matchesGroup("unindexed"));
		assertEquals(Collections.emptyList(), changes.purged.get(resolved));
	}

	private Repository probe(ArtifactFilter filter, Synchronization changes) {
		RepositoryConfiguration configuration = RepositoryConfiguration.T.create();
		configuration.setCachePath(temporary.getRoot().getAbsolutePath());
		IndexedMavenHttpRepository repository = IndexedMavenHttpRepository.T.create();
		repository.setName("virtual");
		repository.setUrl("https://repo.example/maven/virtual");
		repository.setCachable(false);
		repository.setArtifactFilter(filter);
		configuration.getRepositories().add(repository);

		RepositoryConfigurationProbing probing = new RepositoryConfigurationProbing();
		probing.setRepositoryConfigurationSupplier(() -> configuration);
		probing.setChangesSynchronization(changes);
		probing.setArtifactFilterSupplier(changes::getFilterForRepository);
		probing.setRepositoryProbingSupportSupplier(repo -> new RepositoryProbingSupport() {
			@Override public RepositoryProbingResult probe() { return RepositoryProbingResult.T.create(); }
			@Override public String repositoryId() { return repo.getName(); }
		});
		probing.postConstruct();
		try {
			RepositoryConfiguration result = probing.get();
			assertFalse(result.hasFailed());
			assertSame(filter, repository.getArtifactFilter());
			return result.getRepositories().get(0);
		} finally {
			probing.preDestroy();
		}
	}

	private static class Synchronization implements ArtifactChangesSynchronization {
		int queries;
		List<VersionedArtifactIdentification> changed = Collections.singletonList(
				VersionedArtifactIdentification.create("indexed", "library", "1.0"));
		Map<Repository, List<VersionedArtifactIdentification>> purged;
		@Override public Maybe<List<VersionedArtifactIdentification>> queryContents(Repository repository) {
			throw new UnsupportedOperationException();
		}
		@Override public Maybe<List<VersionedArtifactIdentification>> queryChanges(File localRepo, Repository repository) {
			queries++;
			return Maybe.complete(changed);
		}
		@Override public void purge(File localRepo, Map<Repository, List<VersionedArtifactIdentification>> artifacts) {
			purged = artifacts;
		}
		@Override public ArtifactFilter getFilterForRepository(File localRepo, Repository repository) {
			GroupsArtifactFilter filter = GroupsArtifactFilter.T.create();
			filter.getGroups().add("indexed");
			return filter;
		}
	}
}
