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
package com.braintribe.devrock.mc.core.view;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.PlaceholderSupport;
import com.braintribe.codec.marshaller.api.ScalarsFirst;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.mc.api.download.PartEnricher;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.api.view.RepositoryViewResolutionContext;
import com.braintribe.devrock.mc.api.view.RepositoryViewResolutionResult;
import com.braintribe.devrock.mc.api.view.RepositoryViewResolver;
import com.braintribe.devrock.mc.core.selectors.RepositorySelectorExpert;
import com.braintribe.devrock.mc.core.selectors.RepositorySelectors;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
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
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.config.yaml.YamlConfigurations;
import com.braintribe.gm.model.reason.HasFailure;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.encryption.Md5Tools;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.stream.NullOutputStream;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

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
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;

	private Function<File, ReadWriteLock> lockSupplier;
	private PartEnricher partEnricher;
	
	@Configurable @Required
	public void setTransitiveDependencyResolver(TransitiveDependencyResolver transitiveDependencyResolver) {
		this.transitiveDependencyResolver = transitiveDependencyResolver;
	}
	
	@Configurable @Required
	public void setPartEnricher(PartEnricher partEnricher) {
		this.partEnricher = partEnricher;
	}
	
	@Configurable 
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	@Override
	public Maybe<RepositoryViewResolutionResult> resolveRepositoryViews(RepositoryViewResolutionContext context,
			Iterable<? extends CompiledTerminal> terminals) {
		return new StatefulRepositoryViewResolver(context, terminals).resolve();
	}
	
	private record MappedRepositoryViewResolution(RepositoryViewResolution repositoryViewResolution, Map<RepositoryView, AnalysisArtifact> artifactMap) {}

	private class StatefulRepositoryViewResolver implements RepositoryViewResolutionResult {
		private RepositoryViewResolutionContext context;
		private Iterable<? extends CompiledTerminal> compiledTerminals;
		private AnalysisArtifactResolution resolution;
		private RepositoryConfiguration mergedRepositoryConfiguration;
		private LazyInitialized<Maybe<MappedRepositoryViewResolution>> lazyRepositoryViewResolution = new LazyInitialized<>(this::loadViews);
		
		public StatefulRepositoryViewResolver(RepositoryViewResolutionContext context,
				Iterable<? extends CompiledTerminal> terminals) {
			super();
			this.context = context;
			this.compiledTerminals = terminals;
		}
		
		public Maybe<RepositoryViewResolutionResult> resolve() {
			TransitiveResolutionContext trContext = TransitiveResolutionContext.build().done();

			resolution = transitiveDependencyResolver.resolve(trContext, compiledTerminals);
			
			if (resolution.hasFailed()) {
				return Maybe.incomplete(this, resolution.getFailure());
			}
			
			mergedRepositoryConfiguration = acquireMergedRepositoryConfiguration();
			
			return Maybe.complete(this);
		}
	
		private Maybe<MappedRepositoryViewResolution> loadViews() {
			if (resolution.hasFailed())
				return Maybe.empty(resolution.getFailure());
						
			PartEnrichingContext peCtx = PartEnrichingContext.build().enrichPart(REPOSITORY_VIEW_PART_IDENTIFICATION).done();
			partEnricher.enrich(peCtx, resolution);
			
			List<String> terminals;
			List<String> viewsSolutions = new ArrayList<String>();
			Map<RepositoryView, AnalysisArtifact> repositoryViews = new LinkedHashMap<>();

			terminals = resolution.getTerminals().stream().map(AnalysisTerminal::asString).collect(Collectors.toList());

			for (AnalysisArtifact solution : resolution.getSolutions()) {
				Maybe<RepositoryView> maybe = readRepositoryView(solution);
				
				if (maybe.isUnsatisfied())
					return maybe.whyUnsatisfied().asMaybe();
				
				RepositoryView readRepositoryView = maybe.get();
				if (readRepositoryView != null) {
					repositoryViews.put(readRepositoryView, solution);
				}
				viewsSolutions.add(solution.asString());
			}

			RepositoryViewResolution repositoryViewResolution = createRepositoryViewResolution(repositoryViews, terminals);
			
			return Maybe.complete(new MappedRepositoryViewResolution(repositoryViewResolution, repositoryViews));
		}
		
		private <T extends HasFailure> T convertFailureSensitive(EntityType<T> type, Maybe<T> maybe) {
			if (maybe.isUnsatisfied()) {
				T failed = type.create();
				failed.setFailure(maybe.whyUnsatisfied());
				return failed;
			}
			return maybe.get();
		}
		
		private RepositoryConfiguration acquireMergedRepositoryConfiguration() {
			String hash = buildSolutionHash(resolution);
			
			File effectiveConfigFile = determineEffectiveConfigFile(hash);
			
			if (effectiveConfigFile.exists()) {
				Maybe<RepositoryConfiguration> maybeConfig = readYaml(RepositoryConfiguration.T, effectiveConfigFile);
				return convertFailureSensitive(RepositoryConfiguration.T, maybeConfig);
			}
			
			Maybe<MappedRepositoryViewResolution> resolutionMaybe = lazyRepositoryViewResolution.get();
			
			if (resolutionMaybe.isUnsatisfied())
				return null;
			
			MappedRepositoryViewResolution mappedResolution = resolutionMaybe.get();
			
			RepositoryConfiguration mergedRepositoryConfiguration = createMergedRepositoryConfiguration(mappedResolution.artifactMap(), false);

			if (mergedRepositoryConfiguration.hasFailed())
				return mergedRepositoryConfiguration;
			
			writeEffectiveRepositoryConfiguration(mergedRepositoryConfiguration, effectiveConfigFile);
			
			return mergedRepositoryConfiguration;
		}
		
		private String hashConfig(RepositoryConfiguration baseRepositoryConfiguration) {
			YamlMarshaller hashMarshaller = new YamlMarshaller();
			
			try {
				MessageDigest digest = MessageDigest.getInstance("MD5");
				try (DigestOutputStream hashOut = new DigestOutputStream(NullOutputStream.nullOutputStream(), digest)) {
					hashMarshaller.marshall(hashOut, baseRepositoryConfiguration, GmSerializationOptions.deriveDefaults().set(PlaceholderSupport.class, true).build());
				}
				
				return StringTools.toHex(digest.digest());
			}
			catch (Exception e) {
				throw Exceptions.unchecked(e);
			}
		}
		
		private String buildSolutionHash(AnalysisArtifactResolution result) {
			String baseHash = hashConfig(context.baseConfiguration());
			
			StringBuilder builder = new StringBuilder();
			builder.append(baseHash);
			builder.append("\n");
			
			for (var solution: result.getSolutions()) {
				builder.append(solution.asString());
				builder.append("\n");
			}
			
			String md5 = Md5Tools.getMd5(builder.toString());
			return md5;
		}
		
		private File determineEffectiveConfigFile(String hash) {
			String viewConfigName = context.viewConfigName();
			
			String fileName = viewConfigName + "-effective-" + hash + ".yaml";
			
			File effectivConfigfolder = context.effectiveRepoConfigFolder();
			
			if (effectivConfigfolder == null)
				effectivConfigfolder = new File(new File(System.getProperty("java.io.tmpdir")), "repository-views");
			
			File repoConfigFile = new File(effectivConfigfolder, fileName);
			
			return repoConfigFile;
		}
		
		private void writeEffectiveRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration, File effectiveConfigFile) {
			effectiveConfigFile.getParentFile().mkdirs();
			
			Lock lock = lockSupplier.apply(effectiveConfigFile).writeLock();
			
			lock.lock();
			
			try (OutputStream out = new BufferedOutputStream(new FileOutputStream(effectiveConfigFile))) {
				new YamlMarshaller().marshall( //
						out, //
						repositoryConfiguration, // 
						GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high) //
						.set(ScalarsFirst.class, true)
						.set(PlaceholderSupport.class, true)
						.build() //
				);
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			finally {
				lock.unlock();
			}
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
			
			// Order repositories by their type and filter qualities
			Comparator<Repository> comparator = Comparator //
					.comparing(this::getRepositoryTypePrio) //
					.thenComparing(this::getRepositoryFilterPrio);
			
			mergedRepositoryConfigurationRepositories.sort(comparator);
			
			return mergedRepositoryConfiguration;
		}
		
		private int getRepositoryTypePrio(Repository repo) {
			if (repo instanceof MavenFileSystemRepository)
				return 0;

			return 1;
		}
		
		private int getRepositoryFilterPrio(Repository repo) {
			if (repo.getArtifactFilter() != null)
				return 0;
			
			return 1;
		}
		

		@Override
		public AnalysisArtifactResolution getAnalysisResolution() {
			return resolution;
		}

		@Override
		public RepositoryViewResolution getRepositoryViewResolution() {
			return lazyRepositoryViewResolution.get().get().repositoryViewResolution();
		}
		
		@Override
		public Maybe<RepositoryViewResolution> getRepositoryViewResolutionReasoned() {
			var resolution = lazyRepositoryViewResolution.get();
			
			if (resolution.isUnsatisfied())
				return resolution.whyUnsatisfied().asMaybe();
			
			return Maybe.complete(resolution.get().repositoryViewResolution());
		}

		@Override
		public RepositoryConfiguration getMergedRepositoryConfiguration() {
			return mergedRepositoryConfiguration;
		}
		
	}

	private Maybe<RepositoryView> readRepositoryView(AnalysisArtifact analysisArtifact) {
		final Optional<Resource> partFile = findPartResource(analysisArtifact, REPOSITORY_VIEW_PART_IDENTIFICATION);
		
		if (partFile.isPresent()) {
			return readRepositoryViewYaml(partFile.get());
		}
		// this is expected (e.g. for parents)
		return Maybe.complete(null);
	}
	
	private Maybe<RepositoryView> readRepositoryViewYaml(Resource resource) {
		return readYaml(RepositoryView.T, resource::openStream, null);
	}
	
	private <T extends GenericEntity> Maybe<T> readYaml(EntityType<T> type, File file) {
		return readYaml(type, () -> new BufferedInputStream(new FileInputStream(file)), file);
	}
	
	private <T extends GenericEntity> Maybe<T> readYaml(EntityType<T> type, InputStreamProvider inputStreamProvider, File file) {
		return YamlConfigurations.read(type).placeholders().from(inputStreamProvider);
	}
	
	private static Optional<Resource> findPartResource(AnalysisArtifact solution, PartIdentification partIdentification) {
		Part part = solution.getParts().get(partIdentification.asString());
		if (part == null)
			return Optional.empty();

		Resource r = part.getResource();
		
		return Optional.ofNullable(r);
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
			
			if (isTerminalSolution(analysisArtifact))
				repositoryViewResolution.getTerminals().add(repositoryViewSolution);
		}
		
		return repositoryViewResolution;
	}
	
	private static boolean isTerminalSolution(AnalysisArtifact analysisArtifact) {
		for (AnalysisDependency dependency : analysisArtifact.getDependers()) {
			if (dependency.getDepender() == null)
				return true;
		}
		
		return false;
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
				if (!mergedRepository.entityType().getProperties().contains(property))
					continue;
				
				Object propertyValue = property.get(targetRepository);
				
				if (VdHolder.isVdHolder(propertyValue)) {
					VdHolder vdHolder = (VdHolder)propertyValue;
					if (vdHolder.isAbsenceInformation)
						continue;
					
					property.setVd(mergedRepository, vdHolder.vd);
				}
				else
					property.set(mergedRepository, propertyValue);
			}
		}

		for (Property property : sourceRepository.entityType().getProperties()) {
			if (property.getDeclaringType() == GenericEntity.T) {
				continue; // Won't process GenericEntity properties (like globalId, partition)
			}
			
			if (!mergedRepository.entityType().getProperties().contains(property))
				continue;
			
			Object propertyValue = property.get(sourceRepository);
			
			if (VdHolder.isVdHolder(propertyValue)) {
				VdHolder vdHolder = (VdHolder)propertyValue;
				
				if (vdHolder.isAbsenceInformation) 
					continue;
				
				property.setVd(mergedRepository, vdHolder.vd);
			}
			else {
				if (property.getName().equals(Repository.artifactFilter)) {
					mergeArtifactFilter((ArtifactFilter) propertyValue, mergedRepository);
				} else {
					property.set(mergedRepository, propertyValue);
				}
			}
			
			// Won't process absent properties
			if (!property.isAbsent(sourceRepository) && mergedRepository.entityType().getProperties().contains(property)) {
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

	@Required
	public void setLockSupplier(Function<File, ReadWriteLock> lockSupplier) {
		this.lockSupplier = lockSupplier;
	}
}
