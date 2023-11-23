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
package com.braintribe.devrock.repolet.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.devrock.model.repolet.content.Artifact;
import com.braintribe.devrock.model.repolet.content.Dependency;
import com.braintribe.devrock.model.repolet.content.Property;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.declared.DeclaredDependency;
import com.braintribe.model.artifact.declared.DistributionManagement;
import com.braintribe.model.artifact.declared.ProcessingInstruction;
import com.braintribe.model.artifact.declared.Relocation;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

public class FilesystemContentParser {
	private DeclaredArtifactMarshaller marshaller = new DeclaredArtifactMarshaller();
	
	class ContentParserContext {
		public File root;
		public List<File> poms = new ArrayList<>();
		public RepoletContent content;
		
		public ContentParserContext( File root) {
			this.root = root;			
			this.content = RepoletContent.T.create();
		}
	}
	
	

	public RepoletContent parse( File root) {
		
		if (!root.exists() || !root.isDirectory()) {
			System.out.println("directory [" + root.getAbsolutePath() + "] doesn't exist");
			return null;
		}
		
		ContentParserContext context = new ContentParserContext(root);
		findAllPoms( context, root);
		
		for (File pomFile : context.poms) {
			processPom( context, pomFile);
		}
		return postProcess( context);
		
	}

	private RepoletContent postProcess(ContentParserContext context) {		
		return context.content;
	}
	
	private String getCondensedNameFromPath( File root, File pomFile) {
		String version = pomFile.getParentFile().getName();
		File aDir = pomFile.getParentFile().getParentFile();
		String artifactId = aDir.getName();
		
		String path = aDir.getParentFile().getAbsolutePath().substring( root.getAbsolutePath().length() + 1);
		String grp = path.replace("\\", "/").replace( "/", ".");
		
		return grp + ":" + artifactId + "#" + version;
	}

	private void processPom(ContentParserContext context, File pomFile) {
		try (InputStream in = new FileInputStream( pomFile)) {
			DeclaredArtifact da = (DeclaredArtifact) marshaller.unmarshall(in);
			
		
			// real coordinates - from file : --> used for 'real entry'
			String coordinates = getCondensedNameFromPath(context.root, pomFile);
			
			Artifact artifact = Artifact.from( coordinates);  
			 
			// coordinates - from POM : --> used for overrides
			artifact.setVersionOverride( da.getVersion());
			// what about group & artifact id? 
						
			// parent :
			VersionedArtifactIdentification parentReference = da.getParentReference();			
			artifact.setParent(parentReference);
			
			artifact.setPackaging( da.getPackaging());
			
			DistributionManagement dm = da.getDistributionManagement();
			if (dm != null) {
				Relocation relocation = dm.getRelocation();
				if (relocation != null) {
					Dependency dependency = Dependency.create( relocation);
					artifact.setRedirection( dependency);
				}
			}

			// dependency mgnt
			List<DeclaredDependency> managedDependencies = da.getManagedDependencies();
			if (managedDependencies != null) { 
				managedDependencies.forEach( dd -> {
					Dependency d = transpose(dd);
					artifact.getManagedDependencies().add( d);
				});
			}
			
			// dependency
			List<DeclaredDependency> dependencies = da.getDependencies();
			if (dependencies != null) {
				dependencies.forEach( dd -> {
					Dependency d = transpose(dd);
					artifact.getDependencies().add( d);
				});
			}
			
			// properties
			Map<String, String> properties = da.getProperties();
			if (properties != null) {
				for (Map.Entry<String, String> entry : properties.entrySet()) {
					Property property = Property.create( entry.getKey(), entry.getValue());
					artifact.getProperties().add(property);
				}
			}
									
			// parts
			String [] prts = pomFile.getParentFile().list();
			String prefix = artifact.getArtifactId() + "-" + artifact.getVersion();
			for (String prt : prts) {
				String remainder = prt.substring( prefix.length());
				int p = remainder.indexOf( '.');
				String type = remainder.substring( p+1);
				String classifier = remainder.substring(0, p);
				if (classifier.startsWith( "-")) {
					classifier = classifier.substring(1);
				}
				else {
					classifier = null;
				}
				PartIdentification pi = PartIdentification.create(classifier, type);
				
				artifact.getParts().put(pi.asString(), null);
			}
			
			context.content.getArtifacts().add(artifact);
			
		}
		catch ( Exception e) {
			throw new IllegalStateException("cannot read [" + pomFile.getAbsolutePath() + "] ", e);
 		}
		
	}
	
	private Dependency transpose( DeclaredDependency dd) {
		Dependency d = Dependency.T.create();
		d.setGroupId( dd.getGroupId());
		d.setArtifactId( dd.getArtifactId());
		d.setVersion( dd.getVersion());
		d.setScope( dd.getScope());
		d.setClassifier( dd.getClassifier());
		d.setType( dd.getType());
		List<ProcessingInstruction> processingInstructions = dd.getProcessingInstructions();
		if (processingInstructions != null) {
			processingInstructions.forEach( pi -> {					
				d.getProcessingInstructions().put( pi.getTarget(), pi.getData());					
			});
		}
		return d;
	}

	private void findAllPoms(ContentParserContext context, File directory) {
		File [] files = directory.listFiles();
		if (files == null || files.length == 0) {
			return;
		}
		for (File file : files) {
			if (file.isDirectory()) {
				findAllPoms(context, file);
			}
			else if (file.getName().endsWith(".pom")) {
				context.poms.add( file);
				return; // found pom, no need to get lower
			}
		}		
	}
	
	
}
