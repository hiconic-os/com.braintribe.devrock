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
package com.braintribe.devrock.mc.core.resolver.js;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.js.JsDependencyResolver;
import com.braintribe.devrock.mc.api.js.JsLibraryLinker;
import com.braintribe.devrock.mc.api.js.JsLibraryLinkingContext;
import com.braintribe.devrock.mc.api.js.JsLibraryLinkingResult;
import com.braintribe.devrock.mc.api.js.JsResolutionContext;
import com.braintribe.devrock.mc.api.js.JsResolutionListener;
import com.braintribe.devrock.mc.api.js.NormalizedJsEnrichment;
import com.braintribe.devrock.mc.api.resolver.DeclaredArtifactCompiler;
import com.braintribe.devrock.mc.core.commons.SymbolicLinker;
import com.braintribe.devrock.mc.core.commons.ZipTools;
import com.braintribe.devrock.mc.core.resolver.common.AnalysisArtifactResolutionPreparation;
import com.braintribe.devrock.model.mc.reason.MissingAnyJsLibPart;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

import tribefire.cortex.asset.resolving.ng.impl.ArtifactOutputs;

public class BasicJsLibraryLinker implements JsLibraryLinker, JsResolutionConstants {
	private static final String KEY_PRETTY = "pretty";
	private static final String KEY_MIN = "min";
	private static final String USER_HOME = "user.home";
	private static final String JS_LIBRARIES_DEFAULT = ".devrock/js-libraries";
	private static final String JS_LIBRARIES_DEV_ENV = "artifacts/js-libraries";
	private static Logger log = Logger.getLogger(BasicJsLibraryLinker.class);
	private YamlMarshaller marshaller;
	{
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	private static final String dynamicMsgLinking = "resolved artifacts: %3d\nresolved libs: %3d\nlinked libs: %3d\nignored libs: %3d\n";
	private static final String dynamicMsgCopying = "resolved artifacts: %3d\nresolved libs: %3d\ncopied libs: %3d\nignored libs: %3d\n";

	private JsDependencyResolver jsDependencyResolver;
	private DeclaredArtifactCompiler declaredArtifactCompiler;
	private Function<File, ReadWriteLock> lockProvider;
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	private File developmentEnvironmentRoot;

	@Required
	public void setLockProvider(Function<File, ReadWriteLock> lockProvider) {
		this.lockProvider = lockProvider;
	}
	
	@Configurable
	public void setDevelopmentEnvironmentRoot(File developmentEnvironmentRoot) {
		this.developmentEnvironmentRoot = developmentEnvironmentRoot;
	}

	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	@Required
	public void setJsDependencyResolver(JsDependencyResolver jsDependencyResolver) {
		this.jsDependencyResolver = jsDependencyResolver;
	}
	
	@Required
	public void setDeclaredArtifactCompiler(DeclaredArtifactCompiler declaredArtifactCompiler) {
		this.declaredArtifactCompiler = declaredArtifactCompiler;
	}

	@Override
	public JsLibraryLinkingResult linkLibraries(JsLibraryLinkingContext context, File projectFolder) {
		File pomFile = new File(projectFolder, "pom.xml");
		File libFolder = new File(projectFolder, "lib");
		
		Maybe<CompiledArtifact> artifactMaybe = declaredArtifactCompiler.compileReasoned(pomFile);
		
		if (artifactMaybe.isUnsatisfied())
			return new EmptyLibraryLinkingResult(artifactMaybe.whyUnsatisfied());
		
		return new StatefulLinker(context, Collections.singleton(CompiledTerminal.from(artifactMaybe.get())), libFolder).linkLibraries();
	}
	
	private static class EmptyLibraryLinkingResult implements JsLibraryLinkingResult {
		private final AnalysisArtifactResolution resolution;
		
		public EmptyLibraryLinkingResult(Reason whyEmpty) {
			super();
			resolution = AnalysisArtifactResolution.T.create();
			resolution.setFailure(whyEmpty);
		}

		@Override
		public List<File> getLibraryFolders() {
			return Collections.emptyList();
		}
		
		@Override
		public Map<AnalysisArtifact, File> getLibraryFoldersByArtifact() {
			return Collections.emptyMap();
		}
		
		@Override
		public AnalysisArtifactResolution getResolution() {
			return resolution;
		}
		
	}
	
	@Override
	public JsLibraryLinkingResult linkLibraries(JsLibraryLinkingContext context, Iterable<CompiledTerminal> terminals, File targetDir) {
		return new StatefulLinker(context, terminals, targetDir).linkLibraries();
	}
	
	private class BasicJsLibraryLinkingResult implements JsLibraryLinkingResult {

		private final Map<AnalysisArtifact, File> libraryFoldersByArtifact;
		private final List<File> libraryFolders;
		private final AnalysisArtifactResolution resolution;

		public BasicJsLibraryLinkingResult(Map<AnalysisArtifact, File> libraryFoldersByArtifact,
				List<File> libraryFolders, AnalysisArtifactResolution resolution) {
			super();
			this.libraryFoldersByArtifact = libraryFoldersByArtifact;
			this.libraryFolders = libraryFolders;
			this.resolution = resolution;
		}

		@Override
		public Map<AnalysisArtifact, File> getLibraryFoldersByArtifact() {
			return libraryFoldersByArtifact;
		}
	
		@Override
		public List<File> getLibraryFolders() {
			return libraryFolders;
		}
	
		@Override
		public AnalysisArtifactResolution getResolution() {
			return resolution;
		}
		
	}
	
	private class StatefulLinker implements JsResolutionListener {
		private final JsLibraryLinkingContext context;
		private final File libFolder;

		private int resolvedArtifacts;
		private int resolvedLibs;
		private int establishedLibs;
		private int ignoredLibs;
		
		private final boolean lenient;
		private final Object outputMonitor = new Object();
		private final boolean useSymbolicLink;
		private final Iterable<CompiledTerminal> terminals;
		private final File libraryCacheFolder;
		//private ExceptionCollector exceptionCollector = new ExceptionCollector("JsResolver processing");
		private final List<Reason> occurredErrors = new ArrayList<>();

		private final Map<AnalysisArtifact, File> linkedFoldersByArtifact = new HashMap<>();
		private final List<File> linkedFolders = new ArrayList<>();
		
		public StatefulLinker(JsLibraryLinkingContext context, Iterable<CompiledTerminal> terminals, File libFolder) {
			this.context = context;
			this.terminals = terminals;
			this.libFolder = libFolder;
			this.useSymbolicLink = context.useSymbolikLinks();
			this.lenient = context.lenient();
			
			this.libraryCacheFolder = determineLibraryCacheFolder();
		}
		
		private File determineLibraryCacheFolder() {
			File cacheDir = context.libraryCacheFolder();
			
			if (cacheDir != null)
				return cacheDir;
			
			cacheDir = findDevEnvLibraryCacheFolder();
			
			if (cacheDir != null)
				return cacheDir;
			
			return determineDefaultLibraryCacheFolder();
		}
		
		private File findDevEnvLibraryCacheFolder() {
			if (developmentEnvironmentRoot != null) {
				File file = UniversalPath.from(developmentEnvironmentRoot).pushSlashPath(JS_LIBRARIES_DEV_ENV).toFile();
				
				if (file.exists())
					return file;
			}
			
			return null;
		}
		
		private File determineDefaultLibraryCacheFolder() {
			String userhome = virtualEnvironment.getProperty(USER_HOME);
			return UniversalPath.start(userhome).pushSlashPath(JS_LIBRARIES_DEFAULT).toFile();
		}
		
		private void addError(Reason reason) {
			synchronized (occurredErrors) {
				occurredErrors.add(reason);
			}
		}
		
		private void addError(AnalysisArtifact artifact, Reason reason) {
			Reason collatorReason = AnalysisArtifactResolutionPreparation.acquireCollatorReason(artifact);
			collatorReason.getReasons().add(reason);
			addError(reason);
		}
		

		private void registerLinkedFolder(File folder) {
			linkedFolders.add(folder);
		}
		
		private void registerLinkedArtifactFolder(AnalysisArtifact artifact, File folder) {
			registerLinkedFolder(folder);
			linkedFoldersByArtifact.put(artifact, folder);
		}
		
		@Override
		public void onArtifactResolved(AnalysisArtifact artifact) {
			increaseResolvedArtifacts();
		}
		
		@Override
		public void onArtifactEnriched(AnalysisArtifact artifact, PartIdentification partIdentification) {
			increaseResolvedLibs();
		}
		
		public JsLibraryLinkingResult linkLibraries() {
			
			libFolder.mkdir();

			Map<File, String> linkFolders = context.linkFolders();
			
			// source folders linkage 
			if (linkFolders != null) {
				for (Map.Entry<File, String> entry: linkFolders.entrySet()) {
					File folder = entry.getKey();
					String linkName = entry.getValue();
					File linkFile = new File( libFolder, linkName);
					
					folder.mkdirs();
					
					try {
						SymbolicLinker.ensureSymbolicLink(linkFile, folder, true);
						registerLinkedFolder(linkFile);
					} catch (RuntimeException e) {
						addError(InternalError.from(e, "Error while linking folder [" + folder.getAbsolutePath() + "] as library symbol link [" + linkName + "]"));
					}
				}
			}

			JsResolutionContext jsResolutionContext = JsResolutionContext.build() //
					.lenient(true) //
					.enrichmentNormalized(context.preferPrettyOverMin()? NormalizedJsEnrichment.preferPretty: NormalizedJsEnrichment.preferMin) //
					.includeAggregatorsInSolutions(false) //
					.listener(this)
					.done();
			
			AnalysisArtifactResolution resolution = null;
			
			try {
				// resolve the dependencies
				resolution = jsDependencyResolver.resolve(jsResolutionContext, terminals);
				
				// throw error in case of none leniency
				if (!lenient && resolution.hasFailed())
					throw new IllegalStateException(resolution.getFailure().stringify());
				
				// iterate over solutions
				for (AnalysisArtifact artifact : resolution.getSolutions()) {
					
					// determine if it's a codebase or a standard artifact
					Part jsPart = artifact.getParts().get(PART_KEY_JS);
					
					if (jsPart != null) {
						// actual repository backed artifact
						handleRepositoryArtifact(artifact);
					}
					else {
						// codebase backed artifact
						handleCodebaseArtifact(artifact);
					}
				}
			}
			finally {
				resetOutput();
			}
			
			if (!occurredErrors.isEmpty()) {
				List<Reason> filteredReasons = occurredErrors.stream().filter(r -> !(r instanceof MissingAnyJsLibPart)).collect(Collectors.toList());
				
				if (!filteredReasons.isEmpty()) {
					Reason collatorReason = AnalysisArtifactResolutionPreparation.acquireCollatorReason(resolution);
					collatorReason.getReasons().addAll(filteredReasons);
				}
			}
			
			if (!context.lenient() && resolution.hasFailed()) {
				throw new IllegalStateException(resolution.getFailure().stringify());
			}
			
			return new BasicJsLibraryLinkingResult(linkedFoldersByArtifact, linkedFolders, resolution);
		}

		
		private void updateOutput() {
			String msg = String.format(useSymbolicLink? dynamicMsgLinking: dynamicMsgCopying, 
					resolvedArtifacts, resolvedLibs, establishedLibs, ignoredLibs);
			ConsoleOutputs.print(ConsoleOutputs.configurableSequence().resetPosition(true).append(msg));
		}
		
		
		private void resetOutput() {
			synchronized (outputMonitor) {
				resolvedArtifacts = 0;
				resolvedLibs = 0;
				establishedLibs = 0;
				ignoredLibs = 0;
				ConsoleOutputs.print("\n\n\n\n");
			}
		}
		
		private void increaseResolvedArtifacts() {
			synchronized (outputMonitor) {
				resolvedArtifacts++;
				updateOutput();
			}
		}
		private void increaseResolvedLibs() {
			synchronized (outputMonitor) {
				resolvedLibs++;
				updateOutput();
			}
		}
		private void increaseIgnoredLibs() {
			synchronized (outputMonitor) {
				ignoredLibs++;
				updateOutput();
			}
		}
		private void increaseEstablishedLibs() {
			synchronized (outputMonitor) {
				establishedLibs++;
				updateOutput();
			}
		}
		
		
		/**
		 * Unpacks the resolved zip part to the js artifact cache if required
		 * @return true if the artifact carried an unpackable zip part resource otherwise false
		 */
		private boolean unpack(AnalysisArtifact artifact, Part libPart, File folder, String minOrPretty) {
			
			Resource zipResource = Optional.ofNullable(libPart).map(Part::getResource).orElse(null);
			
			if (zipResource == null)
				return false;
			
			File artifactFolder = folder.getParentFile();
			artifactFolder.mkdirs();
			
			// if folder exists already, we must check for overwrites
			File touchedDateFile = new File( artifactFolder, minOrPretty + ".touched");
			
			Date zipDate = null;
			
			// TODO: think about avoiding locks in cases where only reads would have happened
			
			LazyInitialized<Lock> locker = new LazyInitialized<>(() -> {
				Lock lock = lockProvider.apply(folder).writeLock();
				lock.lock();
				return lock;
			}, l -> l.unlock());
			
			try {
				if (folder.exists()) {
					FileResource fileResource = (FileResource)zipResource;
					
					File zipFile = new File(fileResource.getPath());
					zipDate = new Date(zipFile.lastModified());

					// check update requirement low cost
					if (isUpToDate(fileResource, zipDate, touchedDateFile))
						return true;
					
					// acquire lock
					locker.get();

					// recheck update requirement in locked mode again to be sure
					if (isUpToDate(fileResource, zipDate, touchedDateFile))
						return true;
				}
				else {
					folder.mkdir();
				}
				
				locker.get();
			
				if (folder.exists())
					delete( folder);
				
				File downloadFolder = new File( artifactFolder, folder.getName() + ".download");
				downloadFolder.mkdirs();		
				
				try {
					ZipTools.unzip(zipResource::openStream, downloadFolder);
					makeFilesReadonly(downloadFolder);
				} catch (RuntimeException e) {
					downloadFolder.delete();
					String msg = "cannot unpack [" + getBestName(zipResource) + "] to [" + downloadFolder.getAbsolutePath() + "] related to [" + artifact.asString() + "] as " + e.getMessage();
					addError(artifact, InternalError.from(e, msg));
				}
				
				if (!downloadFolder.renameTo(folder)) {
					String msg = "cannot rename [" + downloadFolder.getAbsolutePath() + "] to [" + folder.getAbsolutePath() + "] related to [" + artifact.asString() + "]";
					addError(artifact, InternalError.create(msg));
				}
				
				if (zipDate != null) {
					try (OutputStream out = new FileOutputStream( touchedDateFile)){
						marshaller.marshall(out, zipDate);				
					}	
					catch( Exception e) {
						String msg = "cannot store data for [" + getBestName(zipResource) + "] requested by [" + artifact.asString() + "]";
						log.error(msg);
					}
				}
			}
			finally {
				locker.close();
			}
			
			return true;
		}
		
		private boolean isUpToDate(FileResource resource, Date date, File touchedDateFile) {
			if (touchedDateFile.exists()) {
				try (InputStream in = new FileInputStream( touchedDateFile)) {
					Date unpackedDate = (Date) marshaller.unmarshall(in);
					if (unpackedDate.compareTo(date) == 0) {
						return true;
					}		
				}
				catch (Exception e) {
					String msg = "cannot read data for [" + touchedDateFile.getAbsolutePath() + "]";
					log.error(msg);
				}
			}
			
			return false;
		}
		
		private String getBestName(Resource resource) {
			if (resource instanceof FileResource) {
				return ((FileResource)resource).getPath();
			}
			else 
				return resource.getName();
		}

		private void makeFilesReadonly(File file) {
			if (file.isDirectory()) {
				for (File child: file.listFiles()) {
					makeFilesReadonly(child);
				}
			}
			else {
				file.setWritable(false, false);
			}
		}

		/**
		 * handle a locally present js-project 
		 * @param artifact - the current {@link AnalysisArtifact} (direct or transitive dependency of terminal)
		 */
		private void handleCodebaseArtifact(AnalysisArtifact artifact) {
			Part pomPart = artifact.getParts().get(PartIdentifications.pomPartKey);
			Resource pomResource = pomPart.getResource();
			
			if (!(pomResource instanceof FileResource))
				throw new IllegalStateException("pom resource of asumed codebase artifact [" + artifact.asString()
						+ "] must be a FileResource but was " + pomResource.type().getTypeSignature());

			File pomFile = new File(((FileResource)pomResource).getPath());
			File projectDistFolder = new File( pomFile.getParentFile(), "src");
			projectDistFolder.mkdir();

			for (String libEntryName : SymbolicLinker.determineLinkName( artifact)) {
				ConsoleOutputs.println(ConsoleOutputs.sequence( //
						ConsoleOutputs.text(context.outputPrefix()), //
						ConsoleOutputs.text("Linking to codebase: "), //
						ArtifactOutputs.solution(artifact) //
				));
				File libEntryFile = new File( libFolder, libEntryName);
				copyOrLinkLib(artifact, libEntryFile, projectDistFolder, true);
				increaseEstablishedLibs();
				registerLinkedArtifactFolder(artifact, libEntryFile);
			}						
		}

		/**
		 * handle a remote artifact that is the product of a published js-project 
		 * @param artifact - the current {@link AnalysisArtifact} (direct or transitive dependency of terminal)
		 */
		private void handleRepositoryArtifact(AnalysisArtifact artifact) {
			//
			// js-repository handling
			//
			// check if artifact exists in jsRepository 
			String jsArtifactName = artifact.getGroupId() + "." + artifact.getArtifactId() + "-" + artifact.getVersion();
			
			Part libPart = artifact.getParts().get(PART_KEY_JS);
			
			if (libPart == null) {
				addError(artifact, Reasons.build(MissingAnyJsLibPart.T).text("Found neither min:js.zip nor :js.zip part under expected part key " + PART_KEY_JS + " for artifact [" + artifact.asString() + "]").toReason());
				increaseIgnoredLibs();
				return;
			}
			
			// pretty or min unpacking?
			String key = KEY_MIN.equals(libPart.getClassifier())? KEY_MIN: KEY_PRETTY;
			
			File jsArtifactFolder = new File( libraryCacheFolder, jsArtifactName);
			File jsFolder = new File( jsArtifactFolder, key);
			
			if (!unpack(artifact, libPart, jsFolder, key))
				return;
			
			// lib entry, symbol links
			Set<String> libEntryNames = SymbolicLinker.determineLinkName( artifact);
		
			for (String libEntryName : libEntryNames) {
				File libEntryFile = new File( libFolder, libEntryName);
				
				copyOrLinkLib(artifact, libEntryFile, jsFolder, false);

				increaseEstablishedLibs();
				
				registerLinkedArtifactFolder(artifact, libEntryFile);
			}
		}

		private void copyOrLinkLib(AnalysisArtifact artifact, File libEntryFile,
				File target, boolean relativize) {
			try {
				if (useSymbolicLink) {
					SymbolicLinker.ensureSymbolicLink(libEntryFile, target, relativize);
				}
				else {
					if (libEntryFile.exists())
						FileTools.deleteRecursivelySymbolLinkAware(libEntryFile);
					
					SymbolicLinker.createCopy( target, libEntryFile);
				}
					
			} catch (RuntimeException e) {
				String activity = useSymbolicLink? "linking": "copying";

				Reason reason = InternalError.from(e, //
						"Error while " + activity + " folder [" + libEntryFile.getAbsolutePath() + "] for artifact [" + artifact.asString() + "] as library [" + target.getName() + "]");
				addError(artifact, reason);
			}
		}
	}
	
	/**
	 * @param file - a single file or a directory
	 */
	private static void delete( File file) {
		if (file == null || file.exists() == false)
			return;
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				if (child.isDirectory()) {
					delete( child);
				} 
				child.delete();			
			}
		}		
		file.delete();		
	}
}
