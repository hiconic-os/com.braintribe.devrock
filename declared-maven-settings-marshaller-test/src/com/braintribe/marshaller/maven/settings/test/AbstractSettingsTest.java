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
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.experimental.categories.Category;

import com.braintribe.marshaller.artifact.maven.settings.DeclaredMavenSettingsMarshaller;
import com.braintribe.model.artifact.maven.settings.Settings;
import com.braintribe.testing.category.KnownIssue;


@Category(KnownIssue.class)
public abstract class AbstractSettingsTest implements Validator {
	protected File contents = new File( "res/input");
	private DeclaredMavenSettingsMarshaller marshaller = new DeclaredMavenSettingsMarshaller();
	
	public void unmarshallAndValidate(File file) {
		try (InputStream in = new FileInputStream( file)){			
			Settings settings = marshaller.unmarshall(in);
			if (!validate( settings)) {
				Assert.fail( "settings read from [" + file.getAbsolutePath() + "] are not valid");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e + "] thrown");
		}
		
	}

}
