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
package com.braintribe.model.processing.panther;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.panther.ProjectNature;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.model.processing.panther.depmgt.Poms;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;


public class Sam {
	private static final HashSet<ProjectNature> DEFAULT_PROJECT_NATURES = new HashSet<ProjectNature>(Arrays.asList(ProjectNature.ant, ProjectNature.eclipse));
	private static final int BULK_SIZE = 20;
	private static final String POM_XML = "pom.xml";
	private static final String DOT_PROJECT = ".project";
	private static final Logger logger = Logger.getLogger(Sam.class);
	private String defaultRepoUrl = "https://svn.braintribe.com/repo/master/Development/artifacts/";
	private String revision;
	private Map<String, SourceArtifact> sourceArtifacts;
	private PersistenceGmSession session;
	private String sourceRepositoryName = "braintribe-core-development";
	private SourceRepository sourceRepository = null;
	private Supplier<PersistenceGmSession> sessionProvider;

	@Required
	public void setSessionProvider(Supplier<PersistenceGmSession> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	public static void main(String[] args) {
		try {
			Options options = new Options();
			options.addOption("r", "revision", true, "revision to which the source artifact access is to be updated");

			CommandLineParser parser = new GnuParser();
			CommandLine commandLine = parser.parse(options, args);

			String revision = commandLine.getOptionValue('r');

			Sam sam = new Sam();
			sam.setRevision(revision);
			sam.incrementalUpdate();
		} catch (Exception e) {
			throw new RuntimeException("error while parsing options", e);
		}
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public SourceArtifact getExistingSourceArtifact(String groupId, String artifactId, String version) throws Exception {
		SourceArtifact artifact = session
				.query()
				.entities(
						EntityQueryBuilder.from(SourceArtifact.class).where().conjunction().property("repository").eq()
								.entity(getSourceRepository()).property("groupId").eq(groupId).property("artifactId")
								.eq(artifactId).property("versionId").eq(version).close().done()).first();

		return artifact;
	}

	public Map<String, SourceArtifact> getSourceArtifacts() throws Exception {
		if (sourceArtifacts == null) {
			sourceArtifacts = new HashMap<String, SourceArtifact>(); // CodingSet.createHashSetBased(new
																		// SourceArtifactComparator(), true);

			PersistenceGmSession session = getSession();

			List<SourceArtifact> artifacts = session
					.query()
					.entities(
							EntityQueryBuilder.from(SourceArtifact.class).where().property("repository").eq()
									.entity(getSourceRepository()).done()).list();

			for (SourceArtifact artifact : artifacts) {
				String key = artifact.getGroupId() + ":" + artifact.getArtifactId() + "#" + artifact.getVersion();
				sourceArtifacts.put(key, artifact);
			}
		}

		return sourceArtifacts;
	}

	public String getRevision() throws SvnException, GmSessionException, RuntimeException {
		if (revision == null) {
			SvnInfo svnInfo = new SvnInfo(getArtifactsRootUrl());
			revision = svnInfo.getRevision();
		}

		return revision;
	}

	public void update() throws Exception {
		SourceRepository sourceRepository = getSourceRepository();

		if (sourceRepository.getLastUpdatedRevision() == null) {
			globalUpdate();
		} else {
			incrementalUpdate();
		}
	}

	protected SourceRepository getSourceRepository() throws GmSessionException, RuntimeException {
		if (sourceRepository == null) {
			PersistenceGmSession session = getSession();

			sourceRepository = session
					.query()
					.entities(
							EntityQueryBuilder.from(SourceRepository.class).where().property("name")
									.eq(sourceRepositoryName).done()).first();

			if (sourceRepository == null) {
				sourceRepository = session.create(SourceRepository.T);
				sourceRepository.setName(sourceRepositoryName);
				sourceRepository.setRepoUrl(defaultRepoUrl);
				session.commit();
			}
		}

		return sourceRepository;
	}
	
	public void globalUpdate() throws Exception {
		SourceRepository sourceRepository = getSourceRepository();
		InputStream in = SvnUtil.streamedList(getArtifactsRootUrl(), getRevision(), true);

		Map<String, SourceArtifact> existingSourceArtifacts = getSourceArtifacts();
		Set<SourceArtifact> obsoleteArtifacts = new HashSet<SourceArtifact>(existingSourceArtifacts.values());

		int createdArtifacts = 0;
		int artifacts = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, SvnUtil.getConsoleEncoding()))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				ArtifactAddress address = ArtifactAddress.parseFromPomFilePath(line);
				
				
				if (address != null) {
					logger.debug("detected artifact in source repository: " + address.getQualifiedName());
					SourceArtifact sourceArtifact = existingSourceArtifacts.get(address.getQualifiedName());
					
					if (sourceArtifact == null) {
						// create new source artifact
						sourceArtifact = createSourceArtifact(address);
						logger.debug("created artifact: " + address.getQualifiedName());

						createdArtifacts++;
						artifacts++;
					}
					else {
						// update existing source artifact
						obsoleteArtifacts.remove(sourceArtifact);
						boolean grouped = sourceArtifact.getGrouped();
						String path = sourceArtifact.getPath();
						Set<ProjectNature> natures = sourceArtifact.getNatures();
						
						boolean currentGrouped = address.getOrganizationKind() == OrganizationKind.grouped;
						String currentPath = address.getPathAsString();
						Set<ProjectNature> currentNatures = getProjectNatures(address);
						
						boolean updated = false;
						
						if (currentGrouped != grouped) {
							sourceArtifact.setGrouped(currentGrouped);
							updated = true;
						}
						
						if (!currentPath.equals(path)) {
							sourceArtifact.setPath(currentPath);
							updated = true;
						}
						
						if (!(currentNatures.size() == natures.size() && currentNatures.containsAll(natures))) {
							sourceArtifact.setNatures(currentNatures);
							updated = true;
						}
						
						if (updated) {
							createdArtifacts++;
							artifacts++;
							logger.debug("updated artifact: " + address.getQualifiedName());
						}
					}

					if (createdArtifacts == BULK_SIZE) {
						logger.debug("committing " + BULK_SIZE + " operations. Total count so far: " + artifacts);
						session.commit();
						createdArtifacts = 0;
					}

				}
			}
			
			// final commit;
			if (createdArtifacts > 0) {
				logger.debug("committing " + createdArtifacts + " operations. Total count: " + artifacts);
				session.commit();
			}
		}
		
		// find and delete obsolete artifacts
		for (SourceArtifact obsoleteArtifact : obsoleteArtifacts) {
			logger.debug("deleting obsolete artifact in source repository: " + obsoleteArtifact.getGroupId() + ":" + obsoleteArtifact.getArtifactId() + "#" + obsoleteArtifact.getVersion());
			session.deleteEntity(obsoleteArtifact);
		}
		
		sourceRepository.setLastUpdatedRevision(getRevision());
		
		session.commit();
	}

	private Set<ProjectNature> getProjectNatures(ArtifactAddress address) throws Exception {
		String url = getArtifactsRootUrl() + address.getPathAsString() + "/" + POM_XML;
		try {
			try (InputStream in = SvnUtil.streamedCat(url, getRevision())) {
				String naturesCdl = Poms.readProperties(in, (p,v) -> p.equals("natures")? v: null);
				
				if (naturesCdl == null)
					return DEFAULT_PROJECT_NATURES; 
				
				
				return Stream
					.of(naturesCdl.split(","))
					.map(this::parseProjectNatureLenient)
					.filter(n -> n != null)
					.collect(Collectors.toSet());
			}
		} catch (Exception e) {
			logger.error("error while reading pom for project natures: " + url, e);
			return DEFAULT_PROJECT_NATURES;
		}
	}
	
	private ProjectNature parseProjectNatureLenient(String s) {
		try {
			return ProjectNature.valueOf(s.trim());
		} catch (Exception e) {
			return null;
		}
	}
	
	public void incrementalUpdate() throws Exception {
		SourceRepository repository = getSourceRepository();
		String url = repository.getRepoUrl();
		SvnInfo info = new SvnInfo(url);
		String root = info.getRoot();
		
		String artifactsPath = url.substring(root.length() + 1);
		if (!artifactsPath.endsWith("/"))
			artifactsPath += "/";
		
		Pattern pattern = Pattern.compile("^   ([ADM]) /" + artifactsPath + "(.*)$");
		String toRevision = getRevision();
		String fromRevision = repository.getLastUpdatedRevision();
		
		InputStream in = SvnUtil.streamedLog(getArtifactsRootUrl(), fromRevision, toRevision);
		
		Map<String, SourceArtifact> existingSourceArtifacts = getSourceArtifacts();
		int operations = 0;
		int artifacts = 0;
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, SvnUtil.getConsoleEncoding()))) {
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				// preprocess line
				Matcher matcher = pattern.matcher(line);
				if (!matcher.find())
					continue;
				
				char mode = matcher.group(1).charAt(0);
				String path = matcher.group(2);
				
				ArtifactAddress address = ArtifactAddress.parseFromPomFilePath(line);
				
				if (address != null) {
					switch (mode) {
						case 'A': {
							logger.debug("incrementally detected artifact in source repository: " + address.getQualifiedName());
							createSourceArtifact(address);
							operations++;
							artifacts++;
							logger.debug("created artifact: " + address.getQualifiedName());
							break;
						}
						case 'D': {
							logger.debug("incrementally detected artifact removal in source repository: " + address.getQualifiedName());
							SourceArtifact sourceArtifact = getExistingSourceArtifact(address.getGroupId(), address.getArtifactId(), address.getVersion());
							if (sourceArtifact != null) {
								session.deleteEntity(sourceArtifact);
								operations++;
								logger.debug("deleted artifact: " + address.getQualifiedName());
							}
						}
						case 'M': {
							logger.debug("incrementally detected artifact change in source repository: " + address.getQualifiedName());
							SourceArtifact sourceArtifact = getExistingSourceArtifact(address.getGroupId(), address.getArtifactId(), address.getVersion());
							if (sourceArtifact == null) {
								logger.debug("incrementally detected artifact in source repository: " + address.getQualifiedName());
								createSourceArtifact(address);
								operations++;
								artifacts++;
								logger.debug("incrementally created artifact: " + address.getQualifiedName());
								break;
							}
							else {
								Set<ProjectNature> currentNatures = getProjectNatures(address);
								Set<ProjectNature> natures = sourceArtifact.getNatures();
								
								if (!(currentNatures.size() == natures.size() && currentNatures.containsAll(natures))) {
									sourceArtifact.setNatures(currentNatures);
									operations++;
									logger.debug("incrementally updated artifact: " + address.getQualifiedName());
								}

							}
						}
					}
					
				}
				
				if (operations == BULK_SIZE) {
					logger.debug("committing " + operations + " operations (creation or deletion). Total count so far: " + artifacts);
					session.commit();
				}
			}
		}
		
		sourceRepository.setLastUpdatedRevision(getRevision());
		operations++;
		logger.debug("committing final " + operations + " operations (creation or deletion). Total count: " + artifacts);
		session.commit();
	}

	private SourceArtifact createSourceArtifact(ArtifactAddress address) throws Exception {
		SourceArtifact sourceArtifact = session.create(SourceArtifact.T);
		sourceArtifact.setRepository(sourceRepository);
		sourceArtifact.setGroupId(address.getGroupId());
		sourceArtifact.setArtifactId(address.getArtifactId());
		sourceArtifact.setVersion(address.getVersion());
		sourceArtifact.setPath(address.getPathAsString());
		sourceArtifact.setGrouped(address.getOrganizationKind() == OrganizationKind.grouped);
		sourceArtifact.setNatures(getProjectNatures(address));
		
		return sourceArtifact;
	}

	protected String getArtifactsRootUrl() throws GmSessionException, RuntimeException {
		SourceRepository sourceRepository = getSourceRepository();
		return sourceRepository.getRepoUrl();
	}

	public PersistenceGmSession getSession() throws RuntimeException {
		if (session == null) {
			session = openSession();
		}

		return session;
	}

	protected PersistenceGmSession openSession() throws RuntimeException {
		return sessionProvider.get();
	}

	private static class SourceArtifactComparator implements HashingComparator<SourceArtifact> {

		private String a, g, v;

		@Override
		public boolean compare(SourceArtifact o1, SourceArtifact o2) {
			String a1 = o1.getArtifactId();
			String a2 = o2.getArtifactId();

			int res = a1.compareTo(a2);
			if (res != 0)
				return false;

			String g1 = o1.getGroupId();
			String g2 = o2.getGroupId();

			res = g1.compareTo(g2);

			if (res != 0)
				return false;

			String v1 = o1.getVersion();
			String v2 = o2.getVersion();

			return v1.compareTo(v2) == 0;
		}

		@Override
		public int computeHash(SourceArtifact e) {
			String a = e.getArtifactId();
			String g = e.getGroupId();
			String v = e.getVersion();
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((g == null) ? 0 : g.hashCode());
			result = prime * result + ((v == null) ? 0 : v.hashCode());
			return result;
		}

	}
}
