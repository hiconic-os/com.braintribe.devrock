// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package java.lang;

import java.util.Iterator;

/**
 * @author peter.gazdik
 */
/* package */ class JsIterableHelper {

	/** Uses {@link Iterator}. */
	public static native <T> JsIterable<T> iterable(Iterable<T> itbl) /*-{
	    var result = {};
	    result[Symbol.iterator] = function () {
	        it = itbl.@Iterable::iterator()();
	        return {
	            next: function() {
	                if (it.@Iterator::hasNext()()) {
	                    return { done: false, value: it.@Iterator::next()() };
	                } else {
	                    return { done: true };
	                }
	            }
	        };
	    };
	
	    return result;
	}-*/;

}
