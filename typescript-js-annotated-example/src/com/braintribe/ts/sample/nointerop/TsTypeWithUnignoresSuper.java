package com.braintribe.ts.sample.nointerop;

import jsinterop.annotations.JsType;

@JsType(namespace = "$tf.test")
public interface TsTypeWithUnignoresSuper {

	Object create();

}
