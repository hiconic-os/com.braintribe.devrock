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
