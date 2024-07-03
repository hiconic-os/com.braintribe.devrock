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
package com.braintribe.filter.test.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.NoneMatchingArtifactFilter;
import com.braintribe.devrock.model.repository.filters.QualifiedArtifactFilter;

/**
 * simple generator for YAML filters ... 
 * 
 * @author pit
 *
 */
public class FilterGenerator {
	private File output = new File("res/filter/input/filters");
	private YamlMarshaller marshaller = new YamlMarshaller();
	
	private void createYamlFile( String name, RepositoryConfiguration configuration) {
		try (
				OutputStream out = new FileOutputStream( new File( output, name))
			) {
			marshaller.marshall(out, configuration);
		}
		catch (Exception e) {
			throw new IllegalStateException("cannot write file [" + name + "]", e);
		}
	}

	
	/**
	 * creates a filter for versions 1.0.1 (all artifacts) and none local
	 */
	public void createSimpleFilter() {
		RepositoryConfiguration cfg = RepositoryConfiguration.T.create();
		// 
		Repository r1 = Repository.T.create();
		r1.setName( "archiveA");
		
		QualifiedArtifactFilter af1 = QualifiedArtifactFilter.T.create();
		af1.setGroupId("com.braintribe.devrock.test");
		af1.setArtifactId("a");
		af1.setVersion( "1.0.1");
		
		r1.setArtifactFilter(af1);
		cfg.getRepositories().add(r1);
		
	
		Repository r2 = Repository.T.create();
		r2.setName( "archiveB");
		
		QualifiedArtifactFilter af2 = QualifiedArtifactFilter.T.create();
		af2.setGroupId("com.braintribe.devrock.test");
		af2.setArtifactId("b");
		af2.setVersion( "1.0.1");
		
		r2.setArtifactFilter(af2);
		cfg.getRepositories().add(r2);
		
		Repository r3 = Repository.T.create();
		r3.setName( "archiveC");
		
		QualifiedArtifactFilter af3 = QualifiedArtifactFilter.T.create();
		af3.setGroupId("com.braintribe.devrock.test");
		af3.setArtifactId("c");
		af3.setVersion( "1.0.1");
		
		r3.setArtifactFilter(af3);
		cfg.getRepositories().add(r3);
		
		Repository rLocal = Repository.T.create();
		rLocal.setName( "local");
		
		ArtifactFilter afLocal = NoneMatchingArtifactFilter.T.create();		
		rLocal.setArtifactFilter(afLocal);
		cfg.getRepositories().add(rLocal);
	
		createYamlFile("simpleFilter.yaml", cfg);
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FilterGenerator generator = new FilterGenerator();
		generator.createSimpleFilter();
	}
	
}
