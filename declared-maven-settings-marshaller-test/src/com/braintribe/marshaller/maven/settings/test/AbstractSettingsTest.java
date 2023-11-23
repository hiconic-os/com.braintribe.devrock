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
