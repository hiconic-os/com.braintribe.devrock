// ============================================================================
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
package com.braintribe.artifacts.test.maven.settings;

import java.io.File;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.artifacts.test.maven.framework.FakeExternalPropertyResolver;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenProfileActivationExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenProfileActivationExpertImpl;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.model.maven.settings.Activation;
import com.braintribe.model.maven.settings.ActivationFile;
import com.braintribe.model.maven.settings.ActivationOS;
import com.braintribe.model.maven.settings.ActivationProperty;

public class ActivationTest {
	private static Activation pos_activationPerDefault;
	private static Activation neg_activationPerDefault;
	
	private static Activation pos_activationPerJdk;
	private static Activation neg_activationPerJdk;
	
	private static Activation pos_activationPerOs;
	private static Activation neg_activationPerOs;
	
	
	private static Activation pos_activationPerFileExisting;
	private static Activation neg_activationPerFileExisting;
	
	private static Activation pos_activationPerFileMissing;
	private static Activation neg_activationPerFileMissing;
	
	private static Activation pos_activationPerProperty;
	private static Activation neg_activationPerProperty;
	
	private static Activation pos_activationPerNegatedProperty;
	private static Activation neg_activationPerNegatedProperty;
	
	private static Activation pos_activationPerNegatedPropertyValue;
	private static Activation neg_activationPerNegatedPropertyValue;
	
	
	private static MavenProfileActivationExpert activationExpert;
	

	@BeforeClass
	public static void setup() {
		
		// default 
		pos_activationPerDefault = Activation.T.create();
		pos_activationPerDefault.setActiveByDefault(true);
	
		neg_activationPerDefault = Activation.T.create();
		neg_activationPerDefault.setActiveByDefault(false);
		
		// jdk
		pos_activationPerJdk = Activation.T.create();
		pos_activationPerJdk.setActiveByDefault(false);
		String javaVersion = System.getProperty("java.version");
		pos_activationPerJdk.setJdk( javaVersion);
		
		neg_activationPerJdk = Activation.T.create();
		neg_activationPerJdk.setActiveByDefault(false);
		neg_activationPerJdk.setJdk( "1.1");
		
		// os 
		neg_activationPerOs = Activation.T.create();
		neg_activationPerOs.setActiveByDefault(false);
		ActivationOS os = ActivationOS.T.create();
		os.setFamily("notMyOsFamily");
		os.setName( "notMyOs");
		os.setVersion( "notMyVersion");
		os.setArch( "notMyArch");
		neg_activationPerOs.setOs(os);
			 		
		pos_activationPerOs = Activation.T.create();
		pos_activationPerOs.setActiveByDefault(false);
		os = ActivationOS.T.create();
		os.setFamily("myOsFamily");
		os.setName( "myOs");
		os.setVersion( "myVersion");
		os.setArch( "myArch");
		pos_activationPerOs.setOs(os);
		
		// file existing
		pos_activationPerFileExisting = Activation.T.create();
		pos_activationPerFileExisting.setActiveByDefault(false);
		ActivationFile file = ActivationFile.T.create();
		file.setExists("res/maven/activation/existing.txt");
		pos_activationPerFileExisting.setFile(file);
		
		neg_activationPerFileExisting = Activation.T.create();
		neg_activationPerFileExisting.setActiveByDefault(false);
		file = ActivationFile.T.create();
		file.setExists("res/maven/activation/non.existing.txt");
		neg_activationPerFileExisting.setFile(file);
		
		// file missing
		pos_activationPerFileMissing = Activation.T.create();
		pos_activationPerFileMissing.setActiveByDefault(false);
		file = ActivationFile.T.create();
		file.setMissing("res/maven/activation/non.existing.txt");
		pos_activationPerFileMissing.setFile(file);
		
		neg_activationPerFileMissing = Activation.T.create();
		neg_activationPerFileMissing.setActiveByDefault(false);
		file = ActivationFile.T.create();
		file.setMissing("res/maven/activation/existing.txt");
		neg_activationPerFileMissing.setFile(file);

		
		// property (system environment property only here)
		pos_activationPerProperty = Activation.T.create();
		pos_activationPerProperty.setActiveByDefault(false);
		ActivationProperty property = ActivationProperty.T.create();
		property.setName("username");
		property.setValue( "pit");
		pos_activationPerProperty.setProperty( property);
		
		
		neg_activationPerProperty = Activation.T.create();
		neg_activationPerProperty.setActiveByDefault(false);
		property = ActivationProperty.T.create();
		property.setName("username");
		property.setValue( "anybody else");
		neg_activationPerProperty.setProperty( property);
		
		
		pos_activationPerNegatedProperty = Activation.T.create();
		pos_activationPerNegatedProperty.setActiveByDefault(false);
		property = ActivationProperty.T.create();
		property.setName("!nonExistingProperty");		
		pos_activationPerNegatedProperty.setProperty( property);
		
		neg_activationPerNegatedProperty = Activation.T.create();
		neg_activationPerNegatedProperty.setActiveByDefault(false);
		property = ActivationProperty.T.create();
		property.setName("!username");		
		neg_activationPerNegatedProperty.setProperty( property);
		
		pos_activationPerNegatedPropertyValue = Activation.T.create();
		pos_activationPerNegatedPropertyValue.setActiveByDefault(false);
		property = ActivationProperty.T.create();
		property.setName("username");
		property.setValue("!pot");
		pos_activationPerNegatedPropertyValue.setProperty( property);
		
		neg_activationPerNegatedPropertyValue = Activation.T.create();
		neg_activationPerNegatedPropertyValue.setActiveByDefault(false);
		property = ActivationProperty.T.create();
		property.setName("username");
		property.setValue("!pit");
		neg_activationPerNegatedPropertyValue.setProperty( property);
		
		
		
		
		MavenSettingsExpertFactory factory = new MavenSettingsExpertFactory();
		factory.setSettingsPeristenceExpert( new FakeMavenSettingsPersistenceExpertImpl( new File("res/maven/activation/settings.xml")));					

		MavenSettingsReader reader = factory.getMavenSettingsReader();
		reader.setExternalPropertyResolverOverride( new FakeExternalPropertyResolver( new File("res/maven/activation/fake.activation.properties")));
		
		activationExpert = new MavenProfileActivationExpertImpl();
		activationExpert.setPropertyResolver( reader);
		
	}
	
	@Test
	public void testDefaultActivation() {
		Assert.assertTrue( "profile is not active", activationExpert.isActive(null, pos_activationPerDefault));
		Assert.assertTrue( "profile is not active", activationExpert.isActive(null, neg_activationPerDefault));
	}
	@Test
	public void testOsActivation() {
		Assert.assertTrue( "profile is not active", activationExpert.isActive(null, pos_activationPerOs));
		Assert.assertTrue( "profile is active", !activationExpert.isActive(null, neg_activationPerOs));
	}
	
	@Test
	public void testJdkActivation() {
		Assert.assertTrue( "profile is not active", activationExpert.isActive(null, pos_activationPerJdk));
		Assert.assertTrue( "profile is active", !activationExpert.isActive(null, neg_activationPerJdk));
	}
	
	@Test
	public void testExistingFileActivation() {
		Assert.assertTrue( "profile is not active", activationExpert.isActive(null, pos_activationPerFileExisting));
		Assert.assertTrue( "profile is active", !activationExpert.isActive(null, neg_activationPerFileExisting));
	}
	@Test
	public void testMissingFileActivation() {
		Assert.assertTrue( "profile is not active", activationExpert.isActive(null, pos_activationPerFileMissing));
		Assert.assertTrue( "profile is active", !activationExpert.isActive(null, neg_activationPerFileMissing));
	}
	@Test
	public void testPropertyActivation() {
		Assert.assertTrue( "positive : profile is not active", activationExpert.isActive(null, pos_activationPerProperty));
		Assert.assertTrue( "negative : profile is active", !activationExpert.isActive(null, neg_activationPerProperty));
		
		Assert.assertTrue( "negated property : profile is not active", activationExpert.isActive(null, pos_activationPerNegatedProperty));
		Assert.assertTrue( "negated property : profile is active", !activationExpert.isActive(null, neg_activationPerNegatedProperty));
		
		Assert.assertTrue( "negated property value : profile is not active", activationExpert.isActive(null, pos_activationPerNegatedPropertyValue));
		Assert.assertTrue( "negated property value : profile is active", !activationExpert.isActive(null, neg_activationPerNegatedPropertyValue));
		
	}
	

}
