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
