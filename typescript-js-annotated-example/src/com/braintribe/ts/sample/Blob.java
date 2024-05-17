package com.braintribe.ts.sample;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Blob {
	protected Blob() {
		// To avoid constructor call in Java
	}

	public String type;
	public double size;

	public final native Blob slice(double start, double end, String mimeType);

	@JsOverlay
	public final Blob slice(int start, int end) {
		return slice(start, end, null);
	}

	@JsOverlay
	public final Blob slice(long start, long end) {
		return slice(start, end, null);
	}

	@JsOverlay
	public final String type() {
		return type;
	}

	@JsOverlay
	public final long size() {
		return (long) size;
	}

}