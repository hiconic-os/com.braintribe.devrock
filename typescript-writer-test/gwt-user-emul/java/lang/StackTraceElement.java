// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2019 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package java.lang;

import java.JsAnnotationsPackageNames;

import java.io.Serializable;
import java.util.Objects;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * Included for hosted mode source compatibility. Partially implemented
 * 
 * @skip
 */
@JsType(namespace = JsAnnotationsPackageNames.JAVA_LANG)
@SuppressWarnings("unusable-by-js")
public final class StackTraceElement implements Serializable {

  private String className;

  private String fileName;

  private int lineNumber;

  private String methodName;

  public StackTraceElement() {
  }

  @JsIgnore
  public StackTraceElement(String className, String methodName,
      String fileName, int lineNumber) {
	this();
    assert className != null;
    assert methodName != null;
    this.className = className;
    this.methodName = methodName;
    this.fileName = fileName;
    this.lineNumber = lineNumber;
  }

  public String getClassName() {
    return className;
  }

  public String getFileName() {
    return fileName;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getMethodName() {
    return methodName;
  }
  
  //Emulation, which always returns false
  public boolean isNativeMethod() {
      return false;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof StackTraceElement) {
      StackTraceElement st = (StackTraceElement) other;
      return lineNumber == st.lineNumber
          && Objects.equals(methodName, st.methodName)
          && Objects.equals(className, st.className)
          && Objects.equals(fileName, st.fileName);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(lineNumber, className, methodName, fileName);
  }

  @Override
  public String toString() {
    return className + "." + methodName + "("
        + (fileName != null ? fileName : "Unknown Source")
        + (lineNumber >= 0 ? ":" + lineNumber : "") + ")";
  }
}
