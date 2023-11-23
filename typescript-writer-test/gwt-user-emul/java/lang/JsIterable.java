// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package java.lang;

import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@JsType(name = "Iterable", namespace = "globalThis", isNative = true)
public interface JsIterable<E> {
	// JsIterator<T> [Symbol.iterator]();
}
