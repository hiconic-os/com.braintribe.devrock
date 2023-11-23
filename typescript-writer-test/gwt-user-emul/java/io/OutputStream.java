// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

// CHECKSTYLE_OFF: Copyrighted to the Android Open Source Project.
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
// CHECKSTYLE_ON

package java.io;



import java.JsAnnotationsPackageNames;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * A writable sink for bytes.
 *
 * <p>Most clients will use output streams that write data to the file system
 * ({@link FileOutputStream}), the network ({@link java.net.Socket#getOutputStream()}/{@link
 * java.net.HttpURLConnection#getOutputStream()}), or to an in-memory byte array
 * ({@link ByteArrayOutputStream}).
 *
 * <p>Use {@link OutputStreamWriter} to adapt a byte stream like this one into a
 * character stream.
 *
 * <p>Most clients should wrap their output stream with {@link
 * BufferedOutputStream}. Callers that do only bulk writes may omit buffering.
 *
 * <h3>Subclassing OutputStream</h3>
 * Subclasses that decorate another output stream should consider subclassing
 * {@link FilterOutputStream}, which delegates all calls to the target output
 * stream.
 *
 * <p>All output stream subclasses should override <strong>both</strong> {@link
 * #write(int)} and {@link #write(byte[],int,int) write(byte[],int,int)}. The
 * three argument overload is necessary for bulk access to the data. This is
 * much more efficient than byte-by-byte access.
 *
 * @see InputStream
 *
 * <p>The implementation provided by this class behaves as described in the Java
 * API documentation except for {@link write(int)} which throws an exception of
 * type {@link java.lang.UnsupportedOperationException} instead of being
 * abstract.
 */
@JsType(namespace = JsAnnotationsPackageNames.JAVA_IO)
@SuppressWarnings("unusable-by-js")
public abstract class OutputStream implements Closeable, Flushable {

    /**
     * Default constructor.
     */
    public OutputStream() {
    }

    /**
     * Closes this stream. Implementations of this method should free any
     * resources used by the stream. This implementation does nothing.
     *
     * @throws IOException
     *             if an error occurs while closing this stream.
     */
    @Override
    public void close() throws IOException {
        /* empty */
    }

    /**
     * Flushes this stream. Implementations of this method should ensure that
     * any buffered data is written out. This implementation does nothing.
     *
     * @throws IOException
     *             if an error occurs while flushing this stream.
     */
    @Override
    public void flush() throws IOException {
        /* empty */
    }

    /**
     * Equivalent to {@code write(buffer, 0, buffer.length)}.
     */
    @JsMethod(name = "writeBuffer")
    public void write(byte[] buffer) throws IOException {
        // Note that GWT will throw a JavascriptException rather than a NullPointerException if we
        // skip this check and the buffer array is null. This way we ensure that this implementation
        // behaves in the same way as the classes that are emulated.
        
        write(buffer, 0, buffer.length);
    }

    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * position {@code offset} to this stream.
     *
     * @param buffer
     *            the buffer to be written.
     * @param offset
     *            the start position in {@code buffer} from where to get bytes.
     * @param count
     *            the number of bytes from {@code buffer} to write to this
     *            stream.
     * @throws IOException
     *             if an error occurs while writing to this stream.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if
     *             {@code offset + count} is bigger than the length of
     *             {@code buffer}.
     */
    @JsMethod(name = "writeBufferOffset")
    public void write(byte[] buffer, int offset, int count) throws IOException {
        for (int i = offset; i < offset + count; i++) {
            write(buffer[i]);
        }
    }

    /**
     * Writes a single byte to this stream. Only the least significant byte of
     * the integer {@code oneByte} is written to the stream.
     *
     * @param oneByte
     *            the byte to be written.
     * @throws IOException
     *             if an error occurs while writing to this stream.
     */
    public abstract void write(int oneByte) throws IOException;
}
