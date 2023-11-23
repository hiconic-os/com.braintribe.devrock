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
package com.braintribe.devrock.mc.core.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.testing.category.KnownIssue;

/**
 * simple test bed to analyze an issue with the YAML marshaller :
 * constructs like this fail : 
 *   ?*11: &31 !com.braintribe.model.artifact.analysis.AnalysisArtifact
 *   
 *   property is a map of 
 * @author pit
 *
 */
@Category( KnownIssue.class)
public class YamlMarshallerIssuesLab implements HasCommonFilesystemNode {
	protected File input;
	protected File output;
			
	{
		Pair<File,File> pair = filesystemRoots("basics/yaml");
		input = pair.first;
		output = pair.second;								
	}

	@Test
	public void test() {
		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setV2(true);
		
		AnalysisArtifactResolution resolution = null;
		try (InputStream in = new FileInputStream( new File( input, "map.issue.yaml"))) {
			resolution = (AnalysisArtifactResolution) marshaller.unmarshall(in);
		}
		catch (Exception e) {
			Assert.fail("may not fail at this time, still : " + e.getLocalizedMessage());
		}
		System.out.println( resolution);	
	}
}
