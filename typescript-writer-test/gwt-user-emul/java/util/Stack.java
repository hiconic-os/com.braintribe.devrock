// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package java.util;

import java.JsAnnotationsPackageNames;

import fake.java.util.Stack;
import jsinterop.annotations.JsType;

/**
 * Maintains a last-in, first-out collection of objects. <a
 * href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/Stack.html">[Sun
 * docs]</a>
 * 
 * @param <E> element type.
 */
@JsType(namespace = JsAnnotationsPackageNames.JAVA_UTIL)
@SuppressWarnings("unusable-by-js")
public class Stack<E> extends java.util.Vector<E> {

	  @Override
	  public Object clone() {
	    Stack<E> s = new Stack<E>();
	    s.addAll(this);
	    return s;
	  }

	  public boolean empty()  { return false; }

	  public E peek() {
	    int sz = size();
	    if (sz > 0) {
	      return get(sz - 1);
	    } else {
	      throw new RuntimeException();
	    }
	  }

	  public E pop() {
	    int sz = size();
	    if (sz > 0) {
	      return remove(sz - 1);
	    } else {
	      throw new RuntimeException();
	    }
	  }

	  public E push(E o) {
	    add(o);
	    return o;
	  }

	  public int search(Object o)  { return 0; }

	}
