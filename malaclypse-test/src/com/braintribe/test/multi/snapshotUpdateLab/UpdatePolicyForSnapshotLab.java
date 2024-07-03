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
package com.braintribe.test.multi.snapshotUpdateLab;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.w3c.dom.Document;

import com.braintribe.build.artifact.representations.artifact.maven.metadata.MavenMetaDataCodec;
import com.braintribe.build.artifact.test.repolet.Repolet;
import com.braintribe.build.artifact.test.repolet.ZipBasedSwitchingRepolet;
import com.braintribe.codec.CodecException;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.meta.MavenMetaData;
import com.braintribe.model.artifact.meta.SnapshotVersion;
import com.braintribe.model.artifact.meta.Versioning;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;


public class UpdatePolicyForSnapshotLab extends AbstractUpdatePolicyForSnapshotLab {
	private String [] result = new String[] {"com.braintribe.test.dependencies.repositoryRoleTest:A#1.1-SNAPSHOT", "com.braintribe.test.dependencies.repositoryRoleTest:B#1.1-SNAPSHOT"};		
	
	protected static File settings = new File( "res/snapshotUpdateLab/contents/settings.snapshotUpdate.xml");

	@Override
	protected String[] getResultsForFirstRun() {
		return result;
	}

	@Override
	protected String[] getResultsForSecondRun() {
		return result;
	}
	
	@BeforeClass
	public static void before() {
		before( settings);
	}

	@Override
	protected void tweakEnvironment() {
		Repolet repolet = launchedRepolets.get( "snapshot");
		if (repolet instanceof ZipBasedSwitchingRepolet) {
			ZipBasedSwitchingRepolet switchingRepolet = (ZipBasedSwitchingRepolet) repolet;
			try {
				switchingRepolet.switchContents();
			} catch (ArchivesException e) {
				Assert.fail("cannot switch repolet's content as " + e.getMessage());
			}
		}
	}

	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
		for (Solution solution : solutions) {
			String tag = getSnapshotId(solution);
			if (tag == null) {
				Assert.fail("no snapshot tag found");
			}
			else {
				// compare with what?
				MavenMetaData mavenMetaData = getMavenMetaData(solution, "braintribe.Snapshot");
				if (mavenMetaData == null) {
					Assert.fail("no metadata found");
				}
				else {
					Versioning versioning = mavenMetaData.getVersioning();					
					List<Date> updates = new ArrayList<Date>();
					Map<Date, String> dateToValueMap = new HashMap<Date, String>();
					for (SnapshotVersion snapshotVersion : versioning.getSnapshotVersions()) {
						Date updated = snapshotVersion.getUpdated();
						updates.add( updated);
						dateToValueMap.put( updated, snapshotVersion.getValue());
					}
					// find latest
					updates.sort( new Comparator<Date>() {
						@Override
						public int compare(Date o1, Date o2) {					
							return o1.compareTo(o2);
						}						
					});	
					String value = dateToValueMap.get( updates.get(0));
					if (!tag.equalsIgnoreCase( value)){
						Assert.fail("expected is [" + tag + "] found is [" + value + "]");
					}					
				}
			}
		}
	}


	
	private String getSnapshotId( Solution solution) {
		PartTuple pomTuple = PartTupleProcessor.createPomPartTuple();
	
		for (Part part :solution.getParts()) {
			if (PartTupleProcessor.equals(pomTuple,  part.getType())) {
				File file = new File( part.getLocation());
				try {
					Document doc = DomParser.load().from(file);
					return DomUtils.getElementValueByPath( doc.getDocumentElement(), "properties/snapshotTag", false);
				} catch (DomParserException e) {
					return null;
				}
			}
		}
		return null;
	}
	
	private MavenMetaData getMavenMetaData(Solution identification, final String repoId) {
		File location;		
		location = new File( localRepository, identification.getGroupId().replace('.',  File.separatorChar) + File.separator + identification.getArtifactId() + File.separator + VersionProcessor.toString( identification.getVersion()));		
		File [] files = location.listFiles( new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.equalsIgnoreCase( "maven-metadata-" + repoId + ".xml"))
					return true;
				return false;
			}
		});

		MavenMetaDataCodec mavenMetaDataCodec = new MavenMetaDataCodec();
		for (File file : files) {
			Document document;
			try {
				document = DomParser.load().from(file);
			} catch (DomParserException e) {
				Assert.fail("cannot read file [" + file.getAbsolutePath() + "] as " + e.getMessage());
				return null;
			}
			MavenMetaData metadata;
			try {
				metadata = mavenMetaDataCodec.decode(document);
				return metadata;
			} catch (CodecException e) {
				Assert.fail("cannot decode file [" + file.getAbsolutePath() + "] as " +  e.getMessage());
				return null;
			}
		}
		return null;
	}
	
	

}
