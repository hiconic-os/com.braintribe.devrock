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
package com.braintribe.devrock.mc.core.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.api.view.RepositoryViewResolutionContext;
import com.braintribe.devrock.mc.api.view.RepositoryViewResolutionResult;
import com.braintribe.devrock.mc.api.view.RepositoryViewResolver;
import com.braintribe.devrock.mc.core.selectors.RepositorySelectorExpert;
import com.braintribe.devrock.mc.core.selectors.RepositorySelectors;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.StandardDevelopmentViewArtifactFilter;
import com.braintribe.devrock.model.repositoryview.ConfigurationEnrichment;
import com.braintribe.devrock.model.repositoryview.RepositoryView;
import com.braintribe.devrock.model.repositoryview.enrichments.ArtifactFilterEnrichment;
import com.braintribe.devrock.model.repositoryview.enrichments.RepositoryEnrichment;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewSolution;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.lcd.StringTools;

/**
 * Transitively resolves all view artifacts, reads the
 * {@link RepositoryView} instances (from <code>repositoryview.yaml</code>) and creates a merged
 * {@link RepositoryConfiguration}. Furthermore, it creates a {@link RepositoryViewResolution}.
 * 
 * @author ioannis.paraskevopoulos
 * @author michael.lafite
 * @author pit.steinlin
 * @author dirk.scheffler
 */
public class BasicRepositoryViewResolver implements RepositoryViewResolver {

	public static final PartIdentification REPOSITORY_VIEW_PART_IDENTIFICATION = PartIdentification.create("repositoryview", "yaml");

	private static final Logger logger = Logger.getLogger(BasicRepositoryViewResolver.class);
	
	private TransitiveDependencyResolver transitiveDependencyResolver;
	
	@Configurable @Required
	public void setTransitiveDependencyResolver(TransitiveDependencyResolver transitiveDependencyResolver) {
		this.transitiveDependencyResolver = transitiveDependencyResolver;
	}

	@Override
	public Maybe<RepositoryViewResolutionResult> resolveRepositoryViews(RepositoryViewResolutionContext context,
			Iterable<? extends CompiledTerminal> terminals) {
		return new StatefulRepositoryViewResolver(context, terminals).resolve();
	}
	
	private class StatefulRepositoryViewResolver {
		private final Map<RepositoryView, AnalysisArtifact> repositoryViews = new LinkedHashMap<>();
		private RepositoryViewResolutionContext context;
		private Iterable<? extends CompiledTerminal> compiledTerminals;
		
		public StatefulRepositoryViewResolver(RepositoryViewResolutionContext context,
				Iterable<? extends CompiledTerminal> terminals) {
			super();
			this.context = context;
			this.compiledTerminals = terminals;
		}

		public Maybe<RepositoryViewResolutionResult> resolve() {
			PartEnrichingContext peCtx = PartEnrichingContext.build().enrichPart(REPOSITORY_VIEW_PART_IDENTIFICATION).done();

			TransitiveResolutionContext trContext = TransitiveResolutionContext.build() //
					.enrich(peCtx).done();

			AnalysisArtifactResolution resolution = transitiveDependencyResolver.resolve(trContext, compiledTerminals);
			
			if (resolution.hasFailed()) {
				return Maybe.incomplete(new BasicRepositoryViewResolutionResult(resolution, null, null), resolution.getFailure());
			}
			
			List<String> terminals;
			List<String> viewsSolutions = new ArrayList<String>();

			terminals = resolution.getTerminals().stream().map(AnalysisTerminal::asString).collect(Collectors.toList());

			for (AnalysisArtifact solution : resolution.getSolutions()) {
				RepositoryView readRepositoryView = readRepositoryView(solution);
				if (readRepositoryView != null) {
					repositoryViews.put(readRepositoryView, solution);
				}
				viewsSolutions.add(solution.asString());
			}

			RepositoryConfiguration mergedRepositoryConfiguration = createMergedRepositoryConfiguration(repositoryViews, false);
			RepositoryViewResolution repositoryViewResolution = createRepositoryViewResolution(repositoryViews, terminals);
			
			RepositoryViewResolutionResult repositoryViewResolutionResult = new BasicRepositoryViewResolutionResult(
					resolution, repositoryViewResolution, mergedRepositoryConfiguration);
			
			return Maybe.complete(repositoryViewResolutionResult);
		}
		
		private RepositoryConfiguration createMergedRepositoryConfiguration(Map<RepositoryView, AnalysisArtifact> repositoryViews,
				boolean enableDevelopmentMode) {

			RepositoryConfiguration mergedRepositoryConfiguration = context.baseConfiguration();
			
			if (mergedRepositoryConfiguration == null) {
				mergedRepositoryConfiguration = RepositoryConfiguration.T.create();
			}
			
			List<Repository> mergedRepositoryConfigurationRepositories = mergedRepositoryConfiguration.getRepositories();

			// We silently remove the null key from the repositoryViews. This can happen if a YAML file of ReposisotyView
			// file is empty.
			repositoryViews.keySet().removeIf(Objects::isNull);
			validateRepositoryViews(repositoryViews);

			List<ConfigurationEnrichment> configurationEnrichments = new ArrayList<ConfigurationEnrichment>();

			for (RepositoryView repositoryView : repositoryViews.keySet()) {
				mergeRepositories(repositoryView.getRepositories(), mergedRepositoryConfigurationRepositories);
				configurationEnrichments.addAll(NullSafe.collection(repositoryView.getEnrichments()));
			}
			
			configurationEnrichments.addAll(context.enrichments());

			for (ConfigurationEnrichment configurationEnrichment : configurationEnrichments) {
				enrichRepositories(mergedRepositoryConfigurationRepositories, configurationEnrichment);
			}
			normalizeDisjunctionArtifactFilters(mergedRepositoryConfigurationRepositories);
			if (enableDevelopmentMode) {
				wrapFiltersWithStandardDevelopmentViewArtifactFilters(mergedRepositoryConfigurationRepositories);
			}
			return mergedRepositoryConfiguration;
		}
	}

	private static RepositoryView readRepositoryView(AnalysisArtifact analysisArtifact) {
		final Optional<File> partFile = findPartFile(analysisArtifact, REPOSITORY_VIEW_PART_IDENTIFICATION);
		if (partFile.isPresent()) {
			return readYamlFile(partFile.get());
		}
		// this is expected (e.g. for parents)
		return null;
	}
	
	private static <T extends GenericEntity> T readYamlFile(File file) {
		GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults() //
				.absentifyMissingProperties(true) //
				.build();
		return (T) FileTools.read(file).fromInputStream(it -> new YamlMarshaller().unmarshall(it, options));
	}

	private static Optional<File> findPartFile(AnalysisArtifact solution, PartIdentification partIdentification) {
		Part part = solution.getParts().get(partIdentification.asString());
		if (part == null)
			return Optional.empty();

		Resource r = part.getResource();
		if (!(r instanceof FileResource))
			throw new IllegalStateException("Resource for part type '" + part.getType() + "' is not a FileResource. Resource: " + r);

		FileResource fr = (FileResource) r;
		return Optional.of(new File(fr.getPath()));
	}

	static RepositoryViewResolution createRepositoryViewResolution(Map<RepositoryView, AnalysisArtifact> repositoryViews, List<String> terminals) {
		RepositoryViewResolution repositoryViewResolution = RepositoryViewResolution.T.create();
		Map<String, RepositoryViewSolution> nameToRepositoryViewSolutions = new HashMap<>();

		for (Map.Entry<RepositoryView, AnalysisArtifact> entry: repositoryViews.entrySet()) {
			RepositoryView repositoryView = entry.getKey(); 
			AnalysisArtifact analysisArtifact = entry.getValue(); 
			RepositoryViewSolution repositoryViewSolution = RepositoryViewSolution.T.create();

			nameToRepositoryViewSolutions.put(analysisArtifact.asString(), repositoryViewSolution);

			repositoryViewSolution.setArtifact(analysisArtifact.asString());
			repositoryViewSolution.setRepositoryView(repositoryView);
			repositoryViewSolution.setDependencies(extractDependencies(nameToRepositoryViewSolutions, analysisArtifact));
			repositoryViewResolution.getSolutions().add(repositoryViewSolution);
			if (terminals.contains(analysisArtifact.asString())) {
				repositoryViewResolution.getTerminals().add(repositoryViewSolution);
			}
		}

		return repositoryViewResolution;
	}

	private static List<RepositoryViewSolution> extractDependencies(Map<String, RepositoryViewSolution> nameToRepositoryViewSolutions,
			final AnalysisArtifact analysisArtifact) {
		List<RepositoryViewSolution> dependencies = analysisArtifact.getDependencies().stream() //
				.filter(d -> !d.getArtifactId().equals("parent")).map(d -> d.getSolution().asString())
				.map(name -> Optional.ofNullable(nameToRepositoryViewSolutions.get(name)) //
						.orElseThrow(() -> new IllegalStateException("Could not find artifact " + name + " while resolving repository views."))) //
				.collect(Collectors.toList());
		return dependencies;
	}

	private static void validateRepositoryViews(Map<RepositoryView, AnalysisArtifact> repositoryViews) {
		String validationChecksSummary = "";
		for (RepositoryView repositoryView : repositoryViews.keySet()) {
			String validationCheckInfo = "";
			for (Repository repository : NullSafe.iterable(repositoryView.getRepositories())) {
				if (repository == null) {
					validationCheckInfo += " - Repository should not be null.\n";
				} else if (CommonTools.isBlank(repository.getName())) {
					validationCheckInfo += " - Repository name should be set.\n";
				}
			}
			if (!StringTools.isEmpty(validationCheckInfo)) {
				AnalysisArtifact analysisArtifact = repositoryViews.get(repositoryView);
				String analysisArtifactInfo = analysisArtifact != null
						? "Check " + AnalysisArtifact.class.getSimpleName() + " " + analysisArtifact + "\n"
						: "";
				validationChecksSummary += analysisArtifactInfo + validationCheckInfo;
			}
		}

		if (!StringTools.isEmpty(validationChecksSummary)) {
			throw new IllegalStateException("Repository view(s) have not passed validation checks:\n" + validationChecksSummary);
		}
	}

	private static void enrichRepositories(List<Repository> repositories, ConfigurationEnrichment configurationEnrichment) {
		RepositorySelectorExpert expert = RepositorySelectors.forDenotation(configurationEnrichment.getSelector());

		for (int i = 0; i < NullSafe.size(repositories); i++) {
			Repository repository = repositories.get(i);
			if (expert.selects(repository)) {
				if (configurationEnrichment instanceof RepositoryEnrichment) {
					Repository enrichingRepo = ((RepositoryEnrichment) configurationEnrichment).getRepository();
					repositories.set(i, merge(enrichingRepo, repository));
				} else if (configurationEnrichment instanceof ArtifactFilterEnrichment) {
					ArtifactFilter artifactFilter = ((ArtifactFilterEnrichment) configurationEnrichment).getArtifactFilter();
					mergeArtifactFilter(artifactFilter, repository);
				} else {
					throw new IllegalStateException("Unsupported configuration enrichment '" + configurationEnrichment.getClass().getSimpleName());
				}
			}
		}
	}

	private static void mergeRepositories(List<Repository> sourceRepositories, List<Repository> targetRepositories) {
		for (Repository sourceRepository : NullSafe.iterable(sourceRepositories)) {
			Repository targetRepository = findRepository(sourceRepository, targetRepositories);
			if (targetRepository == null) {
				targetRepository = (Repository) sourceRepository.entityType().create();
				targetRepositories.add(merge(sourceRepository, targetRepository));
			} else {
				// we need to remove because merge may return a new instance
				targetRepositories.set(targetRepositories.indexOf(targetRepository), merge(sourceRepository, targetRepository));
			}
		}
	}

	static <R extends Repository> R createNewTargetRepository(EntityType<R> type, String name) {
		R result = type.create();
		result.setName(name);
		return result;
	}

	private static Repository findRepository(Repository repository, List<Repository> targetRepositories) {
		for (Repository targetRepository : NullSafe.iterable(targetRepositories)) {
			if (targetRepository.getName().equals(repository.getName())) {
				return targetRepository;
			}
		}
		return null;
	}

	private static void normalizeDisjunctionArtifactFilters(List<Repository> repositories) {
		Map<List<? extends ArtifactFilter>, DisjunctionArtifactFilter> reusableDisjunctionFilters = new LinkedHashMap<>();

		for (Repository repository : repositories) {
			if (repository.getArtifactFilter() instanceof DisjunctionArtifactFilter) {
				DisjunctionArtifactFilter disjunctionFilter = (DisjunctionArtifactFilter) repository.getArtifactFilter();
				if (disjunctionFilter.getOperands().size() == 1) {
					ArtifactFilter artifactFilter = disjunctionFilter.getOperands().get(0);
					repository.setArtifactFilter(artifactFilter);
				} else if (disjunctionFilter.getOperands().size() == 0) {
					// It's fine that we have no filter. Everything will be matched.
					repository.setArtifactFilter(null);
				} else {
					if (reusableDisjunctionFilters.containsKey(disjunctionFilter.getOperands())) {
						// There already exists another disjunction filter with the same operands. Let's re-use it.
						// (Purpose is to make the yaml representation of the repository configuration easier to read.)
						repository.setArtifactFilter(reusableDisjunctionFilters.get(disjunctionFilter.getOperands()));
					} else {
						// Remember this filter so that we can re-use it in subsequent loop iterations.
						reusableDisjunctionFilters.put(disjunctionFilter.getOperands(), disjunctionFilter);
					}
				}
			}
		}
	}

	private static void wrapFiltersWithStandardDevelopmentViewArtifactFilters(List<Repository> repositories) {
		Map<ArtifactFilter, StandardDevelopmentViewArtifactFilter> reusableStandardDevelopmentViewArtifactFilters = new LinkedHashMap<>();
		for (Repository repository : repositories) {
			if (repository.getArtifactFilter() != null) {
				if (reusableStandardDevelopmentViewArtifactFilters.containsKey(repository.getArtifactFilter())) {
					// There already exists another development view filter with the same delegate. Let's re-use it.
					// (Purpose is to make the yaml representation of the repository configuration easier to read.)
					repository.setArtifactFilter(reusableStandardDevelopmentViewArtifactFilters.get(repository.getArtifactFilter()));
				} else {
					StandardDevelopmentViewArtifactFilter filter = StandardDevelopmentViewArtifactFilter.T.create();
					filter.setRestrictionFilter(repository.getArtifactFilter());
					repository.setArtifactFilter(filter);

					// Remember this filter so that we can re-use it in subsequent loop iterations.
					reusableStandardDevelopmentViewArtifactFilters.put(filter.getRestrictionFilter(), filter);
				}
			}
		}
	}

	static <S extends Repository, T extends Repository> S merge(S sourceRepository, T targetRepository) {

		final S mergedRepository;
		if (sourceRepository.entityType().isAssignableFrom(targetRepository.entityType())) {
			mergedRepository = (S) targetRepository;
		} else {
			mergedRepository = (S) sourceRepository.entityType().create();
			for (Property property : targetRepository.entityType().getProperties()) {
				Object propertyValue = property.get(targetRepository);
				if (!property.isAbsent(targetRepository) && mergedRepository.entityType().getProperties().contains(property)) {
					property.set(mergedRepository, propertyValue);
				}
			}
		}

		for (Property property : sourceRepository.entityType().getProperties()) {
			if (property.getDeclaringType() == GenericEntity.T) {
				continue; // Won't process GenericEntity properties (like globalId, partition)
			}
			Object propertyValue = property.get(sourceRepository);
			// Won't process absent properties
			if (!property.isAbsent(sourceRepository) && mergedRepository.entityType().getProperties().contains(property)) {
				if (property.getName().equals(Repository.artifactFilter)) {
					mergeArtifactFilter((ArtifactFilter) propertyValue, mergedRepository);
				} else {
					property.set(mergedRepository, propertyValue);
				}
			}
		}
		return mergedRepository;
	}

	private static void mergeArtifactFilter(ArtifactFilter artifactFilter, Repository targetRepository) {

		if (artifactFilter == null) {
			return;
		}

		if (targetRepository.getArtifactFilter() == null) {
			targetRepository.setArtifactFilter(artifactFilter);
			return;
		}

		final DisjunctionArtifactFilter disjunctionFilter;
		if (targetRepository.getArtifactFilter() instanceof DisjunctionArtifactFilter) {
			disjunctionFilter = (DisjunctionArtifactFilter) targetRepository.getArtifactFilter();
		} else {
			disjunctionFilter = DisjunctionArtifactFilter.T.create();
			disjunctionFilter.getOperands().add(targetRepository.getArtifactFilter());
			targetRepository.setArtifactFilter(disjunctionFilter);
		}

		if (artifactFilter instanceof DisjunctionArtifactFilter) {
			disjunctionFilter.getOperands().addAll(((DisjunctionArtifactFilter) artifactFilter).getOperands());
		} else {
			disjunctionFilter.getOperands().add(artifactFilter);
		}
	}
}
