package com.braintribe.ts.sample.nointerop;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import jsinterop.annotations.custom.TsUnignoreMethod;

@JsType(namespace = "$tf.test")
public interface TsTypeWithUnignores extends TsTypeWithUnignoresSuper {

	@JsIgnore
	@TsUnignoreMethod
	String unignored();

	@JsIgnore
	@TsUnignoreMethod(jsMethod = @JsMethod(name = "unignoredAndMapped"))
	String unignored2();

	/**
	 * There was a bug that this method would be written out twice as:
	 * <ul>
	 * <li>create(): string;
	 * <li>create(): any ;
	 * </ul>
	 * But only with javac, not in Eclipse. Reason: the created bridge method would also have all these annotations when compiled with javac, but not
	 * with Eclipse. Adding checks in test to make sure it works, even though it would not be reproducible in Eclipse.
	 */
	@Override
	@JsIgnore
	@TsUnignoreMethod
	String create();

}
