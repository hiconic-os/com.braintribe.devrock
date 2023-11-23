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
