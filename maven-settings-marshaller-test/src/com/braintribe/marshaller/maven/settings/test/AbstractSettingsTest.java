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
package com.braintribe.marshaller.maven.settings.test;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;

import com.braintribe.build.artifact.representations.artifact.maven.settings.marshaller.MavenSettingsMarshaller;
import com.braintribe.model.maven.settings.Settings;

public abstract class AbstractSettingsTest implements Validator {
	protected File contents = new File( "res/input");
	private MavenSettingsMarshaller marshaller = new MavenSettingsMarshaller();
	
	public void unmarshallAndValidate(File file) {
		try {
			Settings settings = marshaller.unmarshall(file);
			if (!validate( settings)) {
				Assert.fail( "settings read from [" + file.getAbsolutePath() + "] are not valid");
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");
		}
		
	}

}
