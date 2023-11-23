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
package com.braintribe.devrock.mc.core.compiler.configuration.origination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationLoaded;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationLocated;
import com.braintribe.gm.model.reason.Reason;

/**
 * a collection of helper function for reasons
 * @author pit
 *
 */
public class ReasoningHelper {
	
	private static final char SEQUENCE_END_SINGLE_QUOTE = '\'';
	private static final char SEQUENCE_START_SINGLE_QUOTE = '\'';
	private static final char SEQUENCE_END_BRACKET = ']';
	private static final char SEQUENCE_START_BRACKET = '[';


	/**
	 * @param parentReason - the {@link Reason} entry point
	 * @return - a flat list of all reasons found within the hierarchy
	 */
	public static List<Reason> extractAllReasons( Reason parentReason) {
		if (parentReason.getReasons().isEmpty()) {
			return Collections.emptyList();
		}
		List<Reason> reasons = new ArrayList<>();
		for (Reason reason : parentReason.getReasons()) {
			reasons.add( reason);
			reasons.addAll( extractAllReasons( reason));
		}
		return reasons;
	}
	
	/**
	 * @param parentReason - the {@link Reason} entry point
	 * @param filter - a {@link Predicate} to filter {@link Reason}s
	 * @return - a flat list of all reasons found within the hierarchy
	 */
	public static List<Reason> extractAllReasons( Reason parentReason, Predicate<Reason> filter) {
		if (parentReason.getReasons().isEmpty()) {
			return Collections.emptyList();
		}
		List<Reason> reasons = new ArrayList<>();
		for (Reason reason : parentReason.getReasons()) {
			if (filter.test(reason)) {
				reasons.add( reason);
			}
			reasons.addAll( extractAllReasons( reason, filter));
		}
		return reasons;
	}
	
	public static Pair<Character,Character> determineDelimiters( String msg) {
		char startChar = SEQUENCE_START_SINGLE_QUOTE;
		char endChar = SEQUENCE_END_SINGLE_QUOTE;
		if (msg.contains( "" + SEQUENCE_END_BRACKET) && (msg.contains("" + SEQUENCE_START_BRACKET))) {
			startChar = SEQUENCE_START_BRACKET;
			endChar = SEQUENCE_END_BRACKET;
		}
		return Pair.of(startChar, endChar);
	}
	
	/**
	 * extract the declaration
	 * @param origination
	 * @return
	 */
	public static String getDeclarationFile( Reason origination) {
		RepositoryConfigurationLoaded declaration = origination.getReasons() //
														.stream() //
															.filter( r -> r instanceof RepositoryConfigurationLoaded) //
															.map( r -> (RepositoryConfigurationLoaded) r) //
															.findFirst() // 
															.orElse(null); //
		/*
		String msg = declaration.getText();
		Pair<Character,Character> chars = determineDelimiters(msg);
		
		int p = msg.indexOf( chars.first);
		if (p<0) {
			Assert.fail("invalid format for [" + RepositoryConfigurationLoaded.T.getTypeName() + "] reason " + msg);		
		}
		int q = msg.indexOf( chars.second, p+1);
		if (q < 0) {
			Assert.fail("invalid format for [" + RepositoryConfigurationLoaded.T.getTypeName() + "] reason " + msg);
		}
		
		String path = msg.substring(p+1, q);		
		return path;
		*/
		if (declaration == null) {
			Assert.fail("Can't find a [" + RepositoryConfigurationLoaded.T.getTypeName() + "] reason in the passed reason structure");
			return null;
		}
		else {
			return declaration.getUrl();
		}
		
	}
	
	/**
	 * extract the pointer's variable reference
	 * @param origination
	 * @return
	 */
	public static List<String> getDeclarations( Reason origination) {
		List<RepositoryConfigurationLoaded> declarations = extractAllReasons(origination) //
															.stream() // 
																.filter( r -> r instanceof RepositoryConfigurationLoaded) //
																.map( r -> (RepositoryConfigurationLoaded) r) //
																.collect( Collectors.toList()); //
		
		List<String> vars = new ArrayList<>( declarations.size());
		
		for (RepositoryConfigurationLoaded declaration : declarations) {
			/*
			String msg = declaration.getText();
			Pair<Character,Character> chars = determineDelimiters(msg);
			int p = msg.indexOf( chars.first);
			if (p<0) {
				Assert.fail("invalid format for [" + RepositoryConfigurationLoaded.T.getTypeName() + "] reason " + msg);		
			}
			int q = msg.indexOf( chars.second, p+1);
			if (q < 0) {
				Assert.fail("invalid format for [" + RepositoryConfigurationLoaded.T.getTypeName() + "] reason " + msg);
			}
			
			String path = msg.substring(p+1, q);
			vars.add(path);
			*/
			vars.add( declaration.getUrl());
			
		}
		return vars;
	}
	
	
	
	/**
	 * extract the pointer's variable reference
	 * @param origination
	 * @return
	 */
	public static List<String> getPointers( Reason origination) {
		List<RepositoryConfigurationLocated> pointers = extractAllReasons(origination) //
									.stream() //
										.filter( r -> r instanceof RepositoryConfigurationLocated) //
										.map( r -> (RepositoryConfigurationLocated) r) //
										.collect( Collectors.toList()); //
		
		List<String> vars = new ArrayList<>( pointers.size());
		
		for (RepositoryConfigurationLocated pointer : pointers) {
			/*
			String msg = pointer.getText();
			Pair<Character,Character> chars = determineDelimiters(msg);
			int p = msg.indexOf( chars.first);
			if (p<0) {
				Assert.fail("invalid format for [" + RepositoryConfigurationLocated.T.getTypeName() + "] reason " + msg);	
			}
			int q = msg.indexOf(chars.second, p+1);
			if (q < 0) {
				Assert.fail("invalid format for [" + RepositoryConfigurationLocated.T.getTypeName() + "] reason " + msg);	
			}
			
			String path = msg.substring(p+1, q);
			vars.add(path);
			*/
			vars.add( pointer.getExpression());
		}
		return vars;
	}
	
}
