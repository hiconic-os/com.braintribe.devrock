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
package com.braintribe.devrock.mc.core.compiler;

import java.io.File;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;

public abstract class AbstractCompilerTest implements HasCommonFilesystemNode{
		
	protected File repo; // = new File( getRoot(), "repo");
	protected File preparedInitialRepository;// = new File( getRoot(), "initial");
	
	protected File input;
	protected File output;
			
	{
		Pair<File,File> pair = filesystemRoots(getRoot());
		input = pair.first;
		output = pair.second;
		
		preparedInitialRepository = new File( input, "initial");
		repo = new File( output, "repo");		
	}
	
	protected abstract String getRoot();

	protected void runBefore() {
		TestUtils.ensure(repo);			
	}

	
}
