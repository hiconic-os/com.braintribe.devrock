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
