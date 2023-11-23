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
package com.braintribe.devrock.mc.core.artifactindex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.devrock.mc.core.repository.index.ArtifactIndex;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.WriterOutputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipes;

public class ArtifactIndexTest {
	@Test
	public void testUpdate() throws IOException {
		ArtifactIndex index = new ArtifactIndex(true);
		
		index.update("foo.bar:parent#1.0.1");
		index.update("foo.bar:x#1.0.1");
		
		StreamPipe pipe = StreamPipes.simpleFactory().newPipe("artifact-index-test");
		
		try (OutputStream out = pipe.openOutputStream()) {
			index.write(out);
		}
		

		final ArtifactIndex index2;
		try (InputStream in = pipe.openInputStream()) {
			index2 = ArtifactIndex.read(in, true);
		}
		
		//index2.write(System.out);
		
		index2.update("foo.bar:parent#1.0.2");
		index2.update("foo.bar:x#1.0.2");

		StringWriter stringWriter = new StringWriter();
		try (OutputStream out = new WriterOutputStream(stringWriter, "UTF-8")) {
			index2.write(out);
		}
		
		String content = stringWriter.toString();
		String lines[] = content.split("\\n");
		
		
		Assertions.assertThat(lines).isEqualTo(new String[] {
				"3 foo.bar:x#1.0.2 U", 
				"2 foo.bar:parent#1.0.2 U", 
				"1 foo.bar:x#1.0.1 U", 
				"0 foo.bar:parent#1.0.1 U"
		});
	}
}
