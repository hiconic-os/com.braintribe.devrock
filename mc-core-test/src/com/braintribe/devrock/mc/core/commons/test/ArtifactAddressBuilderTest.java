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
package com.braintribe.devrock.mc.core.commons.test;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * tests for the {@link ArtifactAddressBuilder}
 * 
 * @author pit
 *
 */
public class ArtifactAddressBuilderTest {
	
	private static final String ROOT = "<root>";
	private static final String GRP = "<group>";
	private static final String ART = "<artifact>";
	private static final String VRS = "<version>";
	private static final String VERS = "1.0";
	private static final String REPO = "<repo>";
	private static final String CLSF = "<clsf>";
	private static final String TYPE = "<type>";
	private static final String HSHT = "<hash>";
			
	

	/**
	 * simple name and parameter sequence test
	 */
	@Test
	public void simpleArtifactExplicit() {
		ArtifactAddressBuilder builder = ArtifactAddressBuilder.build();
		
		String seq1 = builder.root( ROOT).groupId(GRP).artifactId( ART).toPath().toSlashPath();
		String seq2 = builder.artifactId( ART).groupId(GRP).root( ROOT).toPath().toSlashPath();
		
		Assert.assertTrue("[" + seq1 + "] doesn't match [" + seq2 + "]", seq1.equalsIgnoreCase(seq2));
	}
	
	/**
	 * tests centered around parts 
	 */
	@Test
	public void partsExplicit() {
		ArtifactAddressBuilder builder = ArtifactAddressBuilder.build();
		
		String fp1 = builder.groupId(GRP).artifactId(ART).version(VRS).metaData().toPath().toSlashPath();
		String tp1 = "<group>/<artifact>/<version>/maven-metadata.xml";
		assertTrue( "expected [" + tp1 + "], found [" + fp1 + "]", tp1.compareTo( fp1) == 0);
				
		String fp2 = builder.groupId(GRP).artifactId(ART).version(VRS).metaData(REPO).toPath().toSlashPath();
		String tp2 = "<group>/<artifact>/<version>/maven-metadata-<repo>.xml";		
		assertTrue( "expected [" + tp2 + "], found [" + fp2 + "]", tp2.compareTo( fp2) == 0);
				
		String fp3 = builder.groupId(GRP).artifactId(ART).version(VRS).part(PartIdentification.create(CLSF,TYPE)).toPath().toSlashPath();
		String tp3 = "<group>/<artifact>/<version>/<artifact>-<version>-<clsf>.<type>";
		assertTrue( "expected [" + tp3 + "], found [" + fp3 + "]", tp3.compareTo( fp3) == 0);
		
		String fp4 = builder.groupId(GRP).artifactId(ART).version(VRS).part(PartIdentification.create(CLSF,TYPE)).metaExt( HSHT).toPath().toSlashPath();
		String tp4 = "<group>/<artifact>/<version>/<artifact>-<version>-<clsf>.<type>.<hash>";		
		assertTrue( "expected [" + tp4 + "], found [" + fp4 + "]", tp4.compareTo( fp4) == 0);				
	}
	
	/**
	 * tests centered around complex parameters such as {@link ArtifactIdentification}, {@link VersionedArtifactIdentification}, {@link CompiledArtifactIdentification}
	 */
	@Test
	public void complexParameters() {
		ArtifactIdentification ai = ArtifactIdentification.parse( GRP + ":" + ART);		
		String aiV = ArtifactAddressBuilder.build().artifact(ai).toPath().toSlashPath();
		String aiT = "<group>/<artifact>";		
		assertTrue( "expected [" + aiT + "], found [" + aiV + "]", aiT.compareTo( aiV) == 0);
		
		VersionedArtifactIdentification vai = VersionedArtifactIdentification.parse( GRP + ":" + ART + "#" + VRS);		
		String viV = ArtifactAddressBuilder.build().versionedArtifact(vai).toPath().toSlashPath();
		String viT = "<group>/<artifact>/<version>";		
		assertTrue( "expected [" + viT + "], found [" + viV + "]", viT.compareTo( viV) == 0);
		
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse( GRP + ":" + ART + "#" + VERS);		
		String ciV = ArtifactAddressBuilder.build().compiledArtifact(cai).toPath().toSlashPath();
		String ciT = "<group>/<artifact>/1.0";		
		assertTrue( "expected [" + ciT + "], found [" + ciV + "]", ciT.compareTo( ciV) == 0);
		
		
	}
	
	
	

}
