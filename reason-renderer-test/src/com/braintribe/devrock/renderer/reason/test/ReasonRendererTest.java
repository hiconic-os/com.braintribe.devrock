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
package com.braintribe.devrock.renderer.reason.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.mc.reason.IncompleteArtifactResolution;
import com.braintribe.devrock.model.mc.reason.IncompleteResolution;
import com.braintribe.devrock.renderer.reason.BasicReasonRenderer;
import com.braintribe.devrock.renderer.reason.template.FilebasedTemplateSupplier;
import com.braintribe.devrock.test.HasCommonFilesystemNode;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.utils.IOTools;

public class ReasonRendererTest implements HasCommonFilesystemNode {

	File input;
	File output;
	{	
		Pair<File,File> pair = filesystemRoots("html");
		input = pair.first;
		output = pair.second;			
	}
	
	
	@Test
	public void test() {
		File template = new File( input, "standard.template.vm");
		Map<String, Supplier<String>> map = new HashMap<>();
		String key ="standard";
		map.put( key, new FilebasedTemplateSupplier(template));
		
		BasicReasonRenderer renderer = new BasicReasonRenderer( map);
		
		Reason reason = Reasons
				.build( IncompleteResolution.T)
				.text( "resolution failed")
				.causes( 
						Reasons.build( IncompleteArtifactResolution.T).text( "artifact a is incomplete").toReason(),
						Reasons.build( IncompleteArtifactResolution.T).text( "artifact b is incomplete").toReason()
				)
				.toReason();
		
		String renderedReason = renderer.render(reason, "standard");
		
		try {
			IOTools.spit( new File(output, "reason.html"), renderedReason, "UTF-8", false);
		} catch (IOException e) {
		}
		
	}
}
