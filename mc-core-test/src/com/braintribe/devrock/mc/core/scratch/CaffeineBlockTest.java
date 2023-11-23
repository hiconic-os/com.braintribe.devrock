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
package com.braintribe.devrock.mc.core.scratch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;


@Category(KnownIssue.class)
public class CaffeineBlockTest {
	
	LoadingCache<String,String> cache = Caffeine.newBuilder().build(this::load);

	@Test
	public void blockTest() {
		
		
		try {
			ExecutorService threadPool = Executors.newFixedThreadPool(4);
			
			Future<?> f1 = threadPool.submit( this::accessHash1);
			Future<?> f2 = threadPool.submit( this::accessHash1);
			Future<?> f3 = threadPool.submit( this::accessHash2);
			Future<?> f4 = threadPool.submit( this::accessHash2);
			
			f1.get();
			f2.get();
			f3.get();
			f4.get();
			
			
			threadPool.shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public void accessHash1() {
		try {
			System.out.println(cache.get( "foobar"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	public void accessHash2() {
		try {
			System.out.println(cache.get( "fixfox"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public String load( String in) {
		System.out.println("loading : " + in);
		try {
			if (in.equals( "foobar"))
				Thread.sleep( 3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "->" + in;
	}
	
	
}
