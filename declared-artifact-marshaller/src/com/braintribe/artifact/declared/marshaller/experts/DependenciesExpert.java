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
package com.braintribe.artifact.declared.marshaller.experts;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.artifact.declared.marshaller.PomReadContext;
import com.braintribe.model.artifact.declared.DeclaredDependency;

public class DependenciesExpert extends AbstractPomExpert implements HasPomTokens {

	public static List<DeclaredDependency> read(PomReadContext context, XMLStreamReader reader) throws XMLStreamException  {
		List<DeclaredDependency> dependencies = new ArrayList<>();
		reader.next();
		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT : {
					String tag = reader.getName().getLocalPart();
					switch (tag) {
						case DEPENDENCY:
							dependencies.add( DependencyExpert.read(context, reader));
							break;
						default:
							skip(reader);
							break;
					}
					break;
				}
				case XMLStreamConstants.END_ELEMENT: {
					return dependencies;
				}
			}
			reader.next();
		}
		return null;
	}

}
