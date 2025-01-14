package com.braintribe.build.artifacts.ravenhurst.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("deprecation")
public class TestHttpServletResponse implements HttpServletResponse {

	private StringWriter sw = new StringWriter();
	private PrintWriter pw = new PrintWriter(sw);

	public String getResult() {
		pw.flush();
		return sw.toString();
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return pw;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		// intentionally left empty
	}

	@Override
	public void setContentLength(int len) {
		// intentionally left empty
	}

	@Override
	public void setContentType(String type) {
		// intentionally left empty
	}

	@Override
	public void setBufferSize(int size) {
		// intentionally left empty
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		// intentionally left empty
	}

	@Override
	public void resetBuffer() {
		// intentionally left empty
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {
		// intentionally left empty
	}

	@Override
	public void setLocale(Locale loc) {
		// intentionally left empty
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public void addCookie(Cookie cookie) {
		// intentionally left empty
	}

	@Override
	public boolean containsHeader(String name) {
		return false;
	}

	@Override
	public String encodeURL(String url) {
		return null;
	}

	@Override
	public String encodeRedirectURL(String url) {
		return null;
	}

	@Override
	public String encodeUrl(String url) {
		return null;
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return null;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		// intentionally left empty
	}

	@Override
	public void sendError(int sc) throws IOException {
		// intentionally left empty
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		// intentionally left empty
	}

	@Override
	public void setDateHeader(String name, long date) {
		// intentionally left empty
	}

	@Override
	public void addDateHeader(String name, long date) {
		// intentionally left empty
	}

	@Override
	public void setHeader(String name, String value) {
		// intentionally left empty
	}

	@Override
	public void addHeader(String name, String value) {
		// intentionally left empty
	}

	@Override
	public void setIntHeader(String name, int value) {
		// intentionally left empty
	}

	@Override
	public void addIntHeader(String name, int value) {
		// intentionally left empty
	}

	@Override
	public void setStatus(int sc) {
		// intentionally left empty
	}

	@Override
	public void setStatus(int sc, String sm) {
		// intentionally left empty
	}

	@Override
	public void setContentLengthLong(long len) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}
}
