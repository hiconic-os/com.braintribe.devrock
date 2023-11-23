/*
 * Copyright 2008 Google Inc.
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
import java.io.PrintStream;
import java.io.Serializable;



import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/api/java/lang/Throwable.html">the
 * official Java API doc</a> for details.
 */
@JsType(namespace = JsAnnotationsPackageNames.JAVA_LANG)
@SuppressWarnings("unusable-by-js")
public class Throwable implements Serializable {

  private static final Object UNINITIALIZED = "__noinit__";

  /*
   * NOTE: We cannot use custom field serializers because we need the client and
   * server to use different serialization strategies to deal with this type.
   * The client uses the generated field serializers which can use JSNI. That
   * leaves the server free to special case Throwable so that only the
   * detailMessage field is serialized.
   * See SerializabilityUtil.
   */
  private String detailMessage;
  private transient Throwable cause;
  private transient Throwable[] suppressedExceptions;
  private transient StackTraceElement[] stackTrace = new StackTraceElement[0];
  private transient boolean disableSuppression;
  private transient boolean writetableStackTrace = true;

  @JsProperty
  private transient Object backingJsObject = UNINITIALIZED;

  @JsIgnore
  public Throwable() {
    this((Object) null);
    fillInStackTrace();
    initializeBackingError();
  }

  @JsIgnore
  public Throwable(String message) {
    this((Object) null);
    this.detailMessage = message;
    fillInStackTrace();
    initializeBackingError();
  }

  @JsIgnore
  public Throwable(String message, Throwable cause) {
	  this((Object) null);
	  this.cause = cause;
	  this.detailMessage = message;
	  fillInStackTrace();
	  initializeBackingError();
  }

  @JsIgnore
  public Throwable(Throwable cause) {
	  this((Object) null);
	  this.detailMessage = (cause == null) ? null : cause.toString();
	  this.cause = cause;
	  fillInStackTrace();
	  initializeBackingError();
  }

  /**
   * Constructor that allows subclasses disabling exception suppression and stack traces.
   * Those features should only be disabled in very specific cases.
   */
  protected Throwable(String message, Throwable cause, boolean enableSuppression,
      boolean writetableStackTrace) {
	  this((Object) null);
	  this.cause = cause;
	  this.detailMessage = message;
	  this.writetableStackTrace = writetableStackTrace;
	  this.disableSuppression = !enableSuppression;
	  if (writetableStackTrace) {
	    fillInStackTrace();
	  }
	  initializeBackingError();
  }

  Throwable(Object backingJsObject) {
      if (backingJsObject != null) {
        fillInStackTrace();
	    setBackingJsObject(backingJsObject);
	    detailMessage = String.valueOf(backingJsObject);
      }
  }

  private void initializeBackingError()  { /* NOOP */ }

  // TODO(goktug): set 'name' property to class name and 'message' to detailMessage instead when
  // they are respected by dev tools logging.
  Object createError(String msg) {
    return new NativeError(msg);
  }

  private static native Object fixIE(Object e) /*-{
    // In IE -unlike every other browser-, the stack property is not defined until you throw it.
    if (!("stack" in e)) {
      try { throw e; } catch(ignored) {}
    }
    return e;
  }-*/;

  private native void captureStackTrace() /*-{
    @com.google.gwt.core.client.impl.StackTraceCreator::captureStackTrace(*)(this);
  }-*/;

  public Object getBackingJsObject() {
    return backingJsObject;
  }

  private void setBackingJsObject(Object backingJsObject)  { /* NOOP */ }

  private void linkBack(Object error)  { /* NOOP */ }

  /**
   * Call to add an exception that was suppressed. Used by try-with-resources.
   */
  public final void addSuppressed(Throwable exception) {
    
    

    if (disableSuppression) {
      return;
    }

    if (suppressedExceptions == null) {
      suppressedExceptions = new Throwable[] { exception };
    } else {
      // TRICK: This is not correct Java (would give an OOBE, but it works in JS and
      // this code will only be executed in JS.
      suppressedExceptions[suppressedExceptions.length] = exception;
    }
  }

  /**
   * Populates the stack trace information for the Throwable.
   *
   * @return this
   */
  public Throwable fillInStackTrace() {
    if (writetableStackTrace) {
      // If this is the first run, let constructor initialize it.
      // (We need to initialize the backingJsObject from constructor as our own implementation of
      // fillInStackTrace is not guaranteed to be executed.)
      if (backingJsObject != UNINITIALIZED) {
        initializeBackingError();
      }

      // Invalidate the cached trace
      this.stackTrace = null;
    }
    return this;
  }

  public Throwable getCause() {
    return cause;
  }

  public String getLocalizedMessage()  { 
	  return null; 
  }

  public String getMessage()  { 
	  return null;
  }

  /**
   * Returns the stack trace for the Throwable if it is available.
   * <p> Availability of stack traces in script mode depends on module properties and browser.
   * See: https://code.google.com/p/google-web-toolkit/wiki/WebModeExceptions#Emulated_Stack_Data
   */
  public StackTraceElement[] getStackTrace() {
    if (stackTrace == null) {
      stackTrace = constructJavaStackTrace();
    }
    return stackTrace;
  }

  private native StackTraceElement[] constructJavaStackTrace() /*-{
    return @com.google.gwt.core.client.impl.StackTraceCreator::constructJavaStackTrace(*)(this);
  }-*/;

  /**
   * Returns the array of Exception that this one suppressedExceptions.
   */
  public final Throwable[] getSuppressed() {
    if (suppressedExceptions == null) {
      suppressedExceptions = new Throwable[0];
    }

    return suppressedExceptions;
  }

  public Throwable initCause(Throwable cause) {
    this.cause = cause;
    return this;
  }

  public void printStackTrace()  { 
	  /* NOOP */ 
  }

  @JsIgnore
  public void printStackTrace(PrintStream out)  { 
	  /* NOOP */ 
  }

  private void printStackTraceImpl(PrintStream out, String prefix, String ident)  { 
	  /* NOOP */ 
  }

  private void printStackTraceItems(PrintStream out, String ident)  { 
	  /* NOOP */ 
  }

  public void setStackTrace(StackTraceElement[] stackTrace)  { 
	  /* NOOP */ 
  }

  @Override
  public String toString() {
	  return null /*X*/;
  }

  // A private method to avoid polymorphic calls from constructor.
  private String toString(String message) {
	  return null;
  }

  @JsMethod
  public static Throwable of(Object e) {
	  return null;
  }

  @JsType(isNative = true, name = "Error", namespace = JsPackage.GLOBAL)
  private static class NativeError {
    NativeError(String msg) { }
  }

  @JsType(isNative = true, name = "TypeError", namespace = JsPackage.GLOBAL)
  private static class NativeTypeError { }
}
