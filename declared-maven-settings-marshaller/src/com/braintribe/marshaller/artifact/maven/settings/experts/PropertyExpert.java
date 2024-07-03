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
package com.braintribe.marshaller.artifact.maven.settings.experts;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.model.artifact.maven.settings.Property;

public class PropertyExpert extends AbstractSettingsExpert  {
	public static Property read(SettingsMarshallerContext context, String tag, XMLStreamReader reader) throws XMLStreamException{
		Property property = create( context, Property.T);
		property.setName(tag);
		property.setValue( extractString(context, reader));
		return property;
	}

}
