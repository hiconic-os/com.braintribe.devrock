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
package com.braintribe.artifacts.test.maven.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.utils.IOTools;

public class FakeExternalPropertyResolver implements VirtualPropertyResolver {
	private Properties properties = new Properties();
	
	public FakeExternalPropertyResolver(File file) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			properties.load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			IOTools.closeQuietly(inputStream);
		}
		
	}

	@Override
	public String getSystemProperty(String key) {		
		String value = properties.getProperty(key);
		if (value == null) {
			return System.getProperty(key);
		}
		return value;
	}

	@Override
	public String getEnvironmentProperty(String key) {
		String propertyValue = properties.getProperty(key);
		if (propertyValue == null)
			return System.getenv(key);
		return propertyValue;
	}

	@Override
	public boolean isActive() {	
		return true;
	}

	@Override
	public String resolve(String expression) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
