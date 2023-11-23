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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.bias;

import java.io.File;

import org.junit.Test;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests a local bias on com.braintribe.devrock.test:a and a 'functional' local bias on 
 * com.braintribe.devrock.test:b (archive blocked)
 * @author pit
 *
 */
public class CompoundBiasTransitiveResolverTest extends AbstractTransitiveResolverBiasTest {

	@Override
	protected RepoletContent archiveInput() {
		return defaultArchiveInput();
	}

	@Override
	protected File biasFileInput() {		
		return new File( input, "compoundBias.bias.txt");
	}
	

	@Override
	protected File settings() {
		return new File( input, "settings.xml");
	}

	@Test
	public void test() {
		AnalysisArtifactResolution resolution = run(terminal, standardResolutionContext);
		Validator validator = new Validator();
		validator.validateExpressive( new File( input, "compoundBias.validation.txt"), resolution);
		validator.assertResults();
	}
}
