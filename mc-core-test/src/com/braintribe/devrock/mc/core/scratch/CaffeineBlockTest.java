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
