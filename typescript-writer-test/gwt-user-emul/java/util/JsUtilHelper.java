// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package java.util;

/**
 * @author peter.gazdik
 */
/* package */ class JsUtilHelper {

	public static native <K, V> JsMap<K, V> toJsMap(Map<K, V> map) /*-{
	    var result = new Map();
	    var it = map.@Map::entrySet()().@Set::iterator()();

	    while (it.@Iterator::hasNext()()) {
	    	var e = it.@Iterator::next()();
	    	result.set(e.getKey(), e.getValue());
	    }

	    return result;
	}-*/;

	public static native <E> JsSet<E> toJsSet(Set<E> set) /*-{
	    var result = new Set();
	    var it = set.@Set::iterator()();

	    while (it.@Iterator::hasNext()())
	    	result.add(it.@Iterator::next()());
	
	    return result;
	}-*/;
}
