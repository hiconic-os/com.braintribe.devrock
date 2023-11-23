// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

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
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Wraps native <code>byte</code> as an object.
 */
@JsType(namespace = JsAnnotationsPackageNames.JAVA_LANG)
@SuppressWarnings("unusable-by-js")
public final class Byte extends Number implements Comparable<Byte> {

  public static final byte MIN_VALUE = (byte) 0x80;
  public static final byte MAX_VALUE = (byte) 0x7F;
  public static final int SIZE = 8;
  public static final int BYTES = SIZE / Byte.SIZE;
  public static final Class<Byte> TYPE = byte.class;

  /**
   * Use nested class to avoid clinit on outer.
   */
  private static class BoxedValues {
    // Box all values according to JLS
    private static Byte[] boxedValues = new Byte[256];
  }

  public static int compare(byte x, byte y) {
    return x - y;
  }

  public static Byte decode(String s) throws NumberFormatException {
    return Byte.valueOf((byte) __decodeAndValidateInt(s, MIN_VALUE, MAX_VALUE));
  }

  public static int hashCode(byte b) {
    return b;
  }

  public static byte parseByte(String s) throws NumberFormatException {
    return parseByte(s, 10);
  }

  @JsMethod(name="parseByteWithRadix")
  public static byte parseByte(String s, int radix)
      throws NumberFormatException {
    return (byte) __parseAndValidateInt(s, radix, MIN_VALUE, MAX_VALUE);
  }

  public static String toString(byte b) {
    return String.valueOf(b);
  }

  @JsMethod(name="valueOfByte")
  public static Byte valueOf(byte b) {
    int rebase = b + 128;
    Byte result = BoxedValues.boxedValues[rebase];
    if (result == null) {
      result = BoxedValues.boxedValues[rebase] = new Byte(b);
    }
    return result;
  }

  public static Byte valueOf(String s) throws NumberFormatException {
    return valueOf(s, 10);
  }

  @JsMethod(name="valueOfWithRadix")
  public static Byte valueOf(String s, int radix) throws NumberFormatException {
    return Byte.valueOf(Byte.parseByte(s, radix));
  }

  private final transient byte value;

  public Byte(byte value) {
    this.value = value;
  }

  @JsIgnore
  public Byte(String s) {
    this(parseByte(s));
  }

  @Override
  public byte byteValue() {
    return value;
  }

  @Override
  public int compareTo(Byte b) {
    return compare(value, b.value);
  }

  @Override
  public double doubleValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof Byte) && (((Byte) o).value == value);
  }

  @Override
  public float floatValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return hashCode(value);
  }

  @Override
  public int intValue() {
    return value;
  }

  @Override
  public long longValue() {
    return value;
  }

  @Override
  public short shortValue() {
    return value;
  }

  @Override
  public String toString() {
    return toString(value);
  }
}
