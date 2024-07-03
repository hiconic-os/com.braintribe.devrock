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
package com.braintribe.test.parttuple;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;

public class PartTupleTest {

	
	private boolean compare( PartTuple one, PartTuple two) {
		return PartTupleProcessor.equals(one, two);
	}
	
	private boolean matches( PartTuple one, PartTuple two) {
		return PartTupleProcessor.matches(one, two);
	}
	
	@Test
	public void testPositiveDirectMatch() {
		PartTuple one = PartTupleProcessor.fromString("abcd", "jar");
		PartTuple two = PartTupleProcessor.fromString("abcd", "jar");
		Assert.assertTrue( String.format("Tuple [%s] doesn't match tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), compare(one, two));
	}	
	
	@Test
	public void testNegativeDirectMatch() {
		PartTuple one = PartTupleProcessor.fromString("efgh", "jar");
		PartTuple two = PartTupleProcessor.fromString("abcd", "jar");
		Assert.assertTrue( String.format("Tuple [%s] doesn't match tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), !compare(one, two));
	}
	
	@Test
	public void testPositiveClassifierWildCards() {
		PartTuple one = PartTupleProcessor.fromString("abcd", "jar");
		PartTuple two = PartTupleProcessor.fromString("a..d", "jar");
		Assert.assertTrue( String.format("Tuple [%s] doesn't match tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), matches(one, two));
	}
	@Test
	public void testNegativeClassifierWildCards() {
		PartTuple one = PartTupleProcessor.fromString("efgh", "jar");
		PartTuple two = PartTupleProcessor.fromString("a..d", "jar");
		Assert.assertTrue( String.format("Tuple [%s] doesn't match tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), !matches(one, two));
	}
	
	@Test
	public void testPositiveTypeWildCards() {
		PartTuple one = PartTupleProcessor.fromString("abcd", "jar");
		PartTuple two = PartTupleProcessor.fromString("abcd", "j.r");
		Assert.assertTrue( String.format("Tuple [%s] doesn't match tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), matches(one, two));
	}
	
	
	@Test
	public void testPositiveMinimalWildCards() {
		PartTuple one = PartTupleProcessor.fromString("abcd", "jar");
		PartTuple two = PartTupleProcessor.fromString(".*", ".*");
		Assert.assertTrue( String.format("Tuple [%s] doesn't match tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), matches(one, two));
	}
	@Test
	public void testNegativeMinimalWildCards() {
		PartTuple one = PartTupleProcessor.fromString(".*", ".*");
		PartTuple two = PartTupleProcessor.fromString("abcd", "jar");
		Assert.assertTrue( String.format("Tuple [%s] doesn't match tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), !matches(one, two));
	}
	
	@Test
	public void testPositiveMinimalFakeWildCards() {
		PartTuple one = PartTupleProcessor.fromString("abcd", "jar");
		PartTuple two = PartTupleProcessor.fromString("*", "*");
		Assert.assertTrue( String.format("Tuple [%s] doesn't match tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), matches(one, two));
	}

	
	@Test
	public void testNullClassifierValues() {
		PartTuple one = PartTupleProcessor.fromString(":jar");
		PartTuple two = PartTupleProcessor.fromString(":jar");
		Assert.assertTrue( String.format("Tuple [%s] doesn't match tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), compare(one, two));
	}
	@Test
	public void testNullExtensionsValues() {
		PartTuple one = PartTupleProcessor.fromString("cls:");
		PartTuple two = PartTupleProcessor.fromString("cls:");
		Assert.assertTrue( String.format("Tuple [%s] doesn't match tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), compare(one, two));
	}

	@Test
	public void testMixedNullClassifierValues() {
		PartTuple one = PartTupleProcessor.fromString(":jar");
		PartTuple two = PartTupleProcessor.fromString("sources:jar");
		Assert.assertTrue( String.format("Tuple [%s] matches tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), !compare(one, two));
	}
	@Test
	public void testMixedNullExtensionsValues() {
		PartTuple one = PartTupleProcessor.fromString("cls:");
		PartTuple two = PartTupleProcessor.fromString("cls:jar");
		Assert.assertTrue( String.format("Tuple [%s] match tuple [%s]", PartTupleProcessor.toString(one), PartTupleProcessor.toString(two)), !compare(one, two));
	}
	

	
	
}

