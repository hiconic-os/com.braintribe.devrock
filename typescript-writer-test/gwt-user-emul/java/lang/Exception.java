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
package java.lang;

import java.JsAnnotationsPackageNames;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/api/java/lang/Exception.html">the
 * official Java API doc</a> for details.
 */
@JsType(namespace = JsAnnotationsPackageNames.JAVA_LANG)
@SuppressWarnings("unusable-by-js")
public class Exception extends Throwable {

  @JsIgnore
  public Exception() {
	  this(null, null, true, true);
  }

  @JsIgnore
  public Exception(String message) {
	  this(message, null, true, true);
  }

  @JsIgnore
  public Exception(String message, Throwable cause) {
	  this(message, cause, true, true);
  }

  @JsIgnore
  public Exception(Throwable cause) {
	  this(null, cause, true, true);
  }

  protected Exception(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  Exception(Object backingJsObject) {
    super(backingJsObject);
  }
}
