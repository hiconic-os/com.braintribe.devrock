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
package com.braintribe.devrock.mc.core.parts.enricher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.model.artifactory.FileItem;
import com.braintribe.devrock.model.artifactory.FolderInfo;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class AnalysisArtifactResolutionArtifactoryAutoEnricher {
	private static final String PART_AVAILABILITY_PREFIX = "part-availability-";
	private static final String PART_AVAILABILITY_SUFFIX = ".artifactory.json";
	
	private static Logger log = Logger.getLogger(AnalysisArtifactResolutionArtifactoryAutoEnricher.class);
	private File localRepository;
	private static JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
	private static GmDeserializationOptions options = GmDeserializationOptions.defaultOptions.derive().setInferredRootType( FolderInfo.T).build();
	
	@Configurable @Required
	public void setLocalRepository(File localRepository) {
		this.localRepository = localRepository;
	}

	void enrich( AnalysisArtifactResolution resolution) {
		for (AnalysisArtifact artifact : resolution.getSolutions()) {
			
			File versionedArtifactDirectory = ArtifactAddressBuilder.build().root( localRepository.getAbsolutePath()).compiledArtifact( artifact.getOrigin()).toPath().toFile();
			
			File[] jsonFiles = versionedArtifactDirectory.listFiles( new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith( "artifactory.json"))
						return true;
					return false;
				}
			});
			
			if (jsonFiles == null || jsonFiles.length == 0) { 
				log.warn("artifact [" + artifact.asString() + "] isn't backed via any REST delivered part-availability file");
				continue;
			}
			Map<String, List<FileItem>> remoteContents = new HashMap<>(); 
			for (File jsonFile : jsonFiles) {
				String fileName = jsonFile.getName();
				int s = fileName.indexOf( PART_AVAILABILITY_PREFIX) + PART_AVAILABILITY_PREFIX.length();
				int e = fileName.indexOf( PART_AVAILABILITY_SUFFIX);
				String repoName =""; 
				//String repoName = fileName.substring( fileName.indexOf( PART_AVAILABILITY_PREFIX) + .length();
				try (InputStream in = new FileInputStream(jsonFile)) {
					FolderInfo folderInfo = (FolderInfo) marshaller.unmarshall( in, options);
					String path = folderInfo.getPath();
					for (FileItem fileItem : folderInfo.getChildren()) {
						
					}
				}
				catch (Exception e1) {
					log.warn("cannot unmarshall file [" + jsonFile.getAbsolutePath() + "] of [" + artifact.asString() + "]"); 
					continue;
				}
			}
			
		}
	}
}
