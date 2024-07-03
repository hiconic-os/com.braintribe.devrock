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
