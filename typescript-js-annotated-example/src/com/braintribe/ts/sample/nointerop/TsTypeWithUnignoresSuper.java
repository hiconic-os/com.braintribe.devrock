package com.braintribe.ts.sample.nointerop;

import jsinterop.annotations.JsType;

@JsType(namespace = "hc.test")
public interface TsTypeWithUnignoresSuper {

	Object create();

}
