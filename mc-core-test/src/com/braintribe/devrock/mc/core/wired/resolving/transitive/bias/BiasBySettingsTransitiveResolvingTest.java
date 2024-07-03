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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.bias;

import java.io.File;

import org.junit.Test;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class BiasBySettingsTransitiveResolvingTest extends AbstractTransitiveResolverBiasTest {

	@Override
	protected RepoletContent archiveInput() {
		return defaultArchiveInput();
	}

	@Override
	protected File biasFileInput() {		
		return null;
	}
	@Override
	protected File settings() {
		return new File( input, "bias.by.settings.xml");
	}

	@Test
	public void test() {
		AnalysisArtifactResolution resolution = run(terminal, standardResolutionContext);
		Validator validator = new Validator();
		validator.validateExpressive( new File( input, "bias.by.settings.validation.txt"), resolution);
		validator.assertResults();
	}

}
