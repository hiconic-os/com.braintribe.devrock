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
