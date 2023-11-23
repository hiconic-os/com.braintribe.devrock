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

