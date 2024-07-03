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
package com.braintribe.test.multi.framework;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFakeClientFactoryBase {

	protected Map<String, String []> contentMap;
	protected Map<String, SnapshotTuple[]> tupleMap;
	protected Map<String, Boolean> expansiveMap;
	
	protected AbstractFakeClientFactoryBase() {
		
		expansiveMap = new HashMap<String, Boolean>();
		expansiveMap.put( "fake.1", true);
		expansiveMap.put( "fake.2", false);
		expansiveMap.put( "fake.3", true);
		expansiveMap.put( "fake.4", true);
		
		//
		// direct data 
		//
		contentMap = new HashMap<String, String []>();
		contentMap.put( "fake.1", new String [] {"test.a.b.c:Abc#1.0", "test.a.b.c:Abc#1.1","test.d.e.f:Def#1.0",});
		contentMap.put( "fake.2", new String [] {"test.m.n.o.p:Mnop#1.0", "test.q.r.s.t:Qrst#1.0", "test.x.y.z:Xyz#1.0", });
		contentMap.put( "fake.3", new String [] {"test.x.y.z:Xyz#1.0", "test.x.y.z:Xyz#1.1", "test.x.y.z:Xyz#1.2", });
		
		tupleMap = new HashMap<String, SnapshotTuple[]>();
		Date dateOne = new Date();
		Date dateTwo = new Date();
		dateTwo.setTime( dateOne.getTime() - 100000);		
		
		SnapshotTuple tuple1 = new SnapshotTuple( new int[] {1, 2}, new Date [] {dateOne, dateTwo}, "test.u.v.w:Uvw#1.0-SNAPSHOT", "Uvw-1\\.0-.*\\.pom", "Uvw-1.0-xx.pom");		
		SnapshotTuple tuple2 = new SnapshotTuple( new int[] {1, 2}, new Date [] {dateOne, dateTwo}, "test.u.v.w:Uvw#1.1-SNAPSHOT", "Uvw-1\\.1-.*\\.pom", "Uvw-1.1-xx.pom");
		tupleMap.put( "fake.4", new SnapshotTuple[] { tuple1, tuple2});
	}
	
	protected String [] getContentsForKey( String key) {
		return contentMap.get(key);
	}
	
	protected SnapshotTuple [] getTuplesForKey( String key) {
		return tupleMap.get(key);
	}
	
	protected boolean getExpansive(String key) {
		return expansiveMap.get(key);
	}

}
