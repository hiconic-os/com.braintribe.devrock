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
package com.braintribe.devrock.renderer.reason.template;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import com.braintribe.utils.IOTools;

/**
 * retrieve the contents of a template file 
 * @author pit
 *
 */
public class FilebasedTemplateSupplier implements Supplier<String>{

	private String cachedContents;
	private File template;
	
	public FilebasedTemplateSupplier( File template) {
		this.template = template;
		
	}

	@Override
	public String get() throws RuntimeException {
		if (cachedContents != null)
			return cachedContents;
		try {
			cachedContents = IOTools.slurp(template, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException( "cannot load file [" + template.getAbsolutePath() + "]", e);
		}
		return cachedContents;
	}
	
	
	
}
