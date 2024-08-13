package com.braintribe.ts.sample.nointerop;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import jsinterop.annotations.custom.TsUnignoreMethod;

@JsType(namespace = "$tf.test")
public interface TsTypeWithUnignores {

	@JsIgnore
	@TsUnignoreMethod
	String unignored();

	@JsIgnore
	@TsUnignoreMethod(jsMethod = @JsMethod(name = "unignoredAndMapped"))
	String unignored2();

}
