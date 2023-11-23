// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================
 
package com.braintribe.build.artifact.representations.artifact.pom.codec.sax;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.build.artifact.representations.artifact.pom.VersionCodec;
import com.braintribe.build.artifact.representations.artifact.pom.VersionRangeCodec;
import com.braintribe.codec.Codec;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Distribution;
import com.braintribe.model.artifact.Exclusion;
import com.braintribe.model.artifact.License;
import com.braintribe.model.artifact.Property;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.xml.parser.experts.ComplexEntityExpertFactory;
import com.braintribe.xml.parser.experts.EnumValueExpertFactory;
import com.braintribe.xml.parser.experts.ManipulationParserExpertFactory;
import com.braintribe.xml.parser.experts.PropertyExpertFactory;
import com.braintribe.xml.parser.experts.StringValueExpertFactory;
import com.braintribe.xml.parser.experts.VirtualCollectionExpertFactory;
import com.braintribe.xml.parser.experts.VirtualComplexEntityExpertFactory;
import com.braintribe.xml.parser.registry.AbstractContentExpertRegistry;

public class SaxArtifactPomExpertRegistry extends AbstractContentExpertRegistry {

	public SaxArtifactPomExpertRegistry() {
		
		// add conversion codecs 
		Map<String, Codec<GenericEntity, String>> codecs = new HashMap<String, Codec<GenericEntity,String>>();
		codecs.put("com.braintribe.model.artifact.version.Version", new VersionCodec());
		codecs.put("com.braintribe.model.artifact.version.VersionRange", new VersionRangeCodec());
		
		
		addExpertFactory("project", new ComplexEntityExpertFactory( "com.braintribe.model.artifact.Solution", null, codecs));
		addExpertFactory("project/groupId", new StringValueExpertFactory());
		addExpertFactory("project/artifactId", new StringValueExpertFactory());
		addExpertFactory("project/version", new StringValueExpertFactory());
		addExpertFactory("project/packaging", new StringValueExpertFactory());
		
		addExpertFactory("project/parent", new ComplexEntityExpertFactory( "com.braintribe.model.artifact.Dependency", null, codecs));
		addExpertFactory("project/parent/groupId", new StringValueExpertFactory());
		addExpertFactory("project/parent/artifactId", new StringValueExpertFactory());
		addExpertFactory("project/parent/version", new StringValueExpertFactory( "versionRange"));
		
		addExpertFactory("project/licenses", new VirtualCollectionExpertFactory<License>( "licenses", null));
		addExpertFactory("project/licenses/license", new ComplexEntityExpertFactory("com.braintribe.model.artifact.License", null));
		addExpertFactory("project/licenses/license/name", new StringValueExpertFactory());
		addExpertFactory("project/licenses/license/url", new StringValueExpertFactory());
		addExpertFactory("project/licenses/license/distribution", new EnumValueExpertFactory(Distribution.class.getName()));
		
		addExpertFactory("project/properties", new VirtualCollectionExpertFactory<Property>( "properties", null));
		addExpertFactory("project/properties/*", new PropertyExpertFactory("com.braintribe.model.artifact.Property"));
		
		addExpertFactory("project/dependencies", new VirtualCollectionExpertFactory<Dependency>( "dependencies", null, null));		
		addExpertFactory("project/dependencies/dependency", new ComplexEntityExpertFactory( "com.braintribe.model.artifact.Dependency", null, codecs));
		addExpertFactory("project/dependencies/dependency/groupId", new StringValueExpertFactory());
		addExpertFactory("project/dependencies/dependency/artifactId", new StringValueExpertFactory());
		addExpertFactory("project/dependencies/dependency/version", new StringValueExpertFactory( "versionRange"));
		addExpertFactory("project/dependencies/dependency/type", new StringValueExpertFactory("packagingType"));
		addExpertFactory("project/dependencies/dependency/scope", new StringValueExpertFactory());
		addExpertFactory("project/dependencies/dependency/optional", new StringValueExpertFactory("optional"));
		addExpertFactory("project/dependencies/dependency/classifier", new StringValueExpertFactory());
		// processing instruction for the model declaration builder 
		addExpertFactory("project/dependencies/dependency/?group", new StringValueExpertFactory("group"));
		// enriching 
		addExpertFactory("project/dependencies/dependency/?enrich", new ManipulationParserExpertFactory());
		// virtual part
		addExpertFactory("project/dependencies/dependency/?part", new VirtualPartExpertFactory());
		
		addExpertFactory("project/dependencies/dependency/exclusions", new VirtualCollectionExpertFactory<Exclusion>("exclusions", null));
		addExpertFactory("project/dependencies/dependency/exclusions/exclusion", new ComplexEntityExpertFactory( "com.braintribe.model.artifact.Exclusion", null));
		addExpertFactory("project/dependencies/dependency/exclusions/exclusion/groupId", new StringValueExpertFactory());
		addExpertFactory("project/dependencies/dependency/exclusions/exclusion/artifactId", new StringValueExpertFactory());
		
		
		addExpertFactory("project/dependencyManagement", new VirtualComplexEntityExpertFactory( null, null));
		addExpertFactory("project/dependencyManagement/dependencies", new VirtualCollectionExpertFactory<Dependency>( "managedDependencies", null));		
		addExpertFactory("project/dependencyManagement/dependencies/dependency", new ComplexEntityExpertFactory( "com.braintribe.model.artifact.Dependency", null, codecs));
		addExpertFactory("project/dependencyManagement/dependencies/dependency/groupId", new StringValueExpertFactory());
		addExpertFactory("project/dependencyManagement/dependencies/dependency/artifactId", new StringValueExpertFactory());
		addExpertFactory("project/dependencyManagement/dependencies/dependency/version", new StringValueExpertFactory( "versionRange"));
		addExpertFactory("project/dependencyManagement/dependencies/dependency/type", new StringValueExpertFactory("packagingType"));
		addExpertFactory("project/dependencyManagement/dependencies/dependency/scope", new StringValueExpertFactory());
		addExpertFactory("project/dependencyManagement/dependencies/dependency/optional", new StringValueExpertFactory("optional"));
		addExpertFactory("project/dependencyManagement/dependencies/dependency/classifier", new StringValueExpertFactory());
		addExpertFactory("project/dependencyManagement/dependencies/dependency/exclusions", new VirtualCollectionExpertFactory<Exclusion>( "exclusions", null));
		addExpertFactory("project/dependencyManagement/dependencies/dependency/exclusions/exclusion", new ComplexEntityExpertFactory( "com.braintribe.model.artifact.Exclusion", null));
		addExpertFactory("project/dependencyManagement/dependencies/dependency/exclusions/exclusion/groupId", new StringValueExpertFactory());
		addExpertFactory("project/dependencyManagement/dependencies/dependency/exclusions/exclusion/artifactId", new StringValueExpertFactory());

		addExpertFactory("project/distributionManagement", new VirtualComplexEntityExpertFactory( null, null));
		addExpertFactory("project/distributionManagement/relocation", new ComplexEntityExpertFactory( "com.braintribe.model.artifact.Solution", "redirection", codecs));
		addExpertFactory("project/distributionManagement/relocation/groupId", new StringValueExpertFactory());
		addExpertFactory("project/distributionManagement/relocation/artifactId", new StringValueExpertFactory());
		addExpertFactory("project/distributionManagement/relocation/version", new StringValueExpertFactory());

		
	}
}
