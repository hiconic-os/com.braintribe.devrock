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
package com.braintribe.artifact.declared.marshaller.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.artifact.declared.marshaller.experts.TagResolver;
import com.braintribe.model.artifact.declared.ProcessingInstruction;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class TagResolverTest {
	private Map<String, String> comments;
	{
		comments = new HashMap<>();
		comments.put( "tag:asset", "tag:asset");
		comments.put( " tag:asset ", "tag:asset");

		comments.put( "tag : asset", "tag:asset");
		comments.put( " tag : asset ", "tag:asset");

		comments.put( "tag asset", "tag:asset");
		comments.put( " tag asset ", "tag:asset");

		comments.put( "tag  asset", "tag:asset");
		comments.put( " tag  asset ", "tag:asset");
		comments.put( " tag      asset ", "tag:asset");		
		comments.put( " notag ", null);	
		comments.put( "this is just a comment that we won't turn into a tag", null);
		
	}
	
	@Test 
	public void fromCommentTest() {
		boolean fail = false;
		List<String> messages = new ArrayList<>();
		for (Map.Entry<String, String> entry : comments.entrySet()) {
			try {
				String comment = entry.getKey();
				ProcessingInstruction pi = TagResolver.fromComment(comment, () -> ProcessingInstruction.T.create());
				String result = entry.getValue();
				if (result != null) {
					if (pi == null) {
						fail = true;
						messages.add( "expected PI from [" + comment + "] not to be null, but it is");
					}
					else {
						String piAsString = pi.getTarget() + ":" + pi.getData();
						if (!result.equals( piAsString)) {
							fail = true;
							messages.add( "expected PI from [" + comment + "] to be [" + result + "] but it's [" + piAsString + "]");		
						}
					}
				}
				else {
					if (pi != null) {
						fail = true;
						String piAsString = pi.getTarget() + ":" + pi.getData();
						messages.add( "expected PI from [" + comment + "] to be null, but it's " + piAsString);
					}
				}							
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (fail) {
			for (String msg : messages) {
				System.out.println( msg);
			}
			Assert.fail();
		}		
	}

}
