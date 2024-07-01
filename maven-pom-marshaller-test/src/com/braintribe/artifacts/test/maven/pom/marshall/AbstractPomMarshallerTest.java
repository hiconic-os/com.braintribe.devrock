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
package com.braintribe.artifacts.test.maven.pom.marshall;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;

import com.braintribe.artifacts.test.maven.pom.marshall.validator.Validator;
import com.braintribe.build.artifact.representations.artifact.pom.marshaller.ArtifactPomMarshaller;
import com.braintribe.model.artifact.Solution;

/**
 * abstract basic test runner 
 * 
 * @author pit
 *
 */
public abstract class AbstractPomMarshallerTest implements Validator{

	private File content = new File("res/marshaller");
	private File input = new File( content, "input");
	
	private ArtifactPomMarshaller marshaller = new ArtifactPomMarshaller();
	
	protected Solution read( String name) {
		File file = new File (input, name);
		try {
			Solution solution = marshaller.unmarshall( file);
			if (!validate( solution)) {
				Assert.fail( "validation failed");
			}
		} catch (XMLStreamException e) {
			Assert.fail("cannot unmarshall file [" + file.getAbsolutePath() + "] " + e);
		}
		return null;
	}
	
	
	
	

}
