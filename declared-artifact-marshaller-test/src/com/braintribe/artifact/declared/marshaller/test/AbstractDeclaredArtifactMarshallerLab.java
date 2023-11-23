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
package com.braintribe.artifact.declared.marshaller.test;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.common.lcd.equalscheck.IgnoreCaseEqualsCheck;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.declared.DeclaredDependency;
import com.braintribe.model.artifact.declared.DistributionManagement;
import com.braintribe.model.artifact.declared.ProcessingInstruction;
import com.braintribe.model.artifact.declared.Relocation;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;


public abstract class AbstractDeclaredArtifactMarshallerLab {
	protected DeclaredArtifactMarshaller marshaller = new DeclaredArtifactMarshaller();
	protected StaxMarshaller staxMarshaller = new StaxMarshaller();
	protected File input = new File( "res/input");
	private IgnoreCaseEqualsCheck check = new IgnoreCaseEqualsCheck();
	
	protected DeclaredArtifact read( File file) {
		try ( InputStream in = new FileInputStream(file)) {
			return (DeclaredArtifact) marshaller.unmarshall( in);
		} catch (Exception e) {
			Assert.fail( "cannot read [" + file.getAbsolutePath() + "] " + e.getMessage());
			return null;
		}		
	}
	protected void validate( String msg, Object found, Object expected) {
		Assert.assertTrue( msg + " found [" + found + "], expected [" + expected + "]", !((found == null && expected != null) && (found != null && expected == null)));		
	}
	
	
	protected void validate( String msg, Collection<?> found, Collection<?> expected) {
		validate( msg, (Object) found, (Object) expected);
		if (found != null && expected != null) {
			Assert.assertTrue( msg + "length : expected [" + expected.size() + "], found [" + found.size() + "]", expected.size() == found.size());
		}
	}
	protected void validate( String msg, ArtifactIdentification found, ArtifactIdentification expected) {
		validate( msg, (Object) found, (Object) expected);
		if (found == null || expected == null)
			return;
		validate( msg, (Object) found.getGroupId(), (Object) expected.getGroupId());
		Assert.assertTrue(msg + " groupId : expected [" + expected.getGroupId() + "], found [" + found.getGroupId(), check.equals( found.getGroupId(), expected.getGroupId()));
		Assert.assertTrue(msg + " artifactId : expected [" + expected.getArtifactId() + "], found [" + found.getArtifactId(), check.equals( found.getArtifactId(), expected.getArtifactId()));		
	}
	
	protected void validate( String msg, VersionedArtifactIdentification found, VersionedArtifactIdentification expected) {
		validate( msg, (ArtifactIdentification) found, (ArtifactIdentification) expected);
		if (found == null || expected == null)
			return;
		Assert.assertTrue(msg + " version : expected [" + expected.getVersion() + "], found [" + found.getVersion(), check.equals( found.getVersion(), expected.getVersion()));
	}
	
	protected void validate( DistributionManagement found, DistributionManagement expected) {
		if (found == null && expected == null)
			return;
		if (expected == null) {
			Assert.fail( " distribution mgt : expected nothing, found any");
			return;
		}
		validate( found.getRelocation(), expected.getRelocation());
	}
	
	protected void validate( Relocation found, Relocation expected) {
		if (found == null && expected == null)
			return;
		if (expected == null) {
			Assert.fail("relocation : expected nothing, found any");
			return;
		}
		validate( "relocation ", (VersionedArtifactIdentification) found, (VersionedArtifactIdentification) expected);
		validate( "relocation msg", found.getMessage(), expected.getMessage());
	}
	
	protected void validate( String msg, ProcessingInstruction found, ProcessingInstruction expected) {
		Assert.assertTrue( "target : expected [" + expected.getTarget() + "], found [" + found.getTarget(), check.equals( found.getTarget(),  expected.getTarget()));
		Assert.assertTrue( "data : expected [" + expected.getData() + "], found [" + found.getData(), check.equals( found.getData(),  expected.getData()));
	}
	

	protected void validate(String msg, DeclaredDependency found, DeclaredDependency expected) {
		validate( msg, (VersionedArtifactIdentification) found, (VersionedArtifactIdentification) expected);
		Assert.assertTrue( "scope : expected [" + expected.getScope() + "], found [" + found.getScope(), check.equals(found.getScope(), expected.getScope()));
		
		validate( "exclusions", found.getExclusions(), expected.getExclusions());		
		validate( "pi", found.getProcessingInstructions(), expected.getProcessingInstructions());
	}

	private void validate(String msg, List<?> found, List<?> expected) {
		validate( msg, (Collection<?>) found, (Collection<?>) expected);
		if (found != null && expected != null) {
			for (int i = 0; i < found.size(); i++) {
				
				Object f = found.get(i);			
				Object e = expected.get(i);
				validate( msg, f, expected.get(i));
				
				if (f instanceof DeclaredDependency) {
					validate( msg, (DeclaredDependency) f, (DeclaredDependency) e);
				}
				else if (f instanceof ArtifactIdentification) {
					validate( msg, (ArtifactIdentification) f, (ArtifactIdentification) e);
				}
				else if (f instanceof ProcessingInstruction) {
					validate( msg, (ProcessingInstruction) f, (ProcessingInstruction) e);
				}			
			}	
		}
	}
	
	protected void validate( DeclaredArtifact found, File expectedData) {
		DeclaredArtifact expected = found;
		if (expectedData.exists()) {
			try (InputStream in = new FileInputStream(expectedData)) {
				expected = (DeclaredArtifact) staxMarshaller.unmarshall(in);
			} catch (Exception e) {
				Assert.fail( "cannot read expected data from [" + expectedData.getAbsolutePath() + "] as " + e.getMessage());
				return;
			}
		}
		validate( "result", (Object) found, (Object) expected);
		if (found == null || expected == null)
			return;
		validate( "header", (VersionedArtifactIdentification) found, (VersionedArtifactIdentification) expected);
		Assert.assertTrue("packaging : expected [" + expected.getPackaging() + "], found [" + found.getPackaging(), check.equals( found.getPackaging(), expected.getPackaging()));
		
		validate( "parent", found.getParentReference(), expected.getParentReference());
		validate( "distributionmgt", found.getDistributionManagement(), expected.getDistributionManagement());
		
		validate( "dependencies", found.getDependencies(), expected.getDependencies());
		validate( "managed dependencies", found.getManagedDependencies(), expected.getManagedDependencies());
		
		if (!expectedData.exists()) {
			try (OutputStream out = new FileOutputStream(expectedData)) {
				staxMarshaller.marshall(out, expected);				
			} catch (Exception e) {
				Assert.fail( "cannot write missing expected data to [" + expectedData.getAbsolutePath() + "] as " + e.getMessage());
			}
			Assert.fail("no comparioson data was found and was created as [" + expectedData.getAbsolutePath() + "]");
		}
	}
}
