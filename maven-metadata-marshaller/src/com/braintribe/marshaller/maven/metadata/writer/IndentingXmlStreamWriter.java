// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.marshaller.maven.metadata.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * currently still simple {@link XMLStreamWriter} implementation that features indenting
 * 
 * @author pit
 *
 */
public class IndentingXmlStreamWriter implements XMLStreamWriter {
	private Writer writer;
	private String encoding;
	
	private class StackEntry {
		public String tag;
		public boolean hasAttributes;
		public boolean hasContent;
		public boolean hasChild;
		public String indent;
	}
	
	private Stack<StackEntry> stack = new Stack<>();
	
	public IndentingXmlStreamWriter(OutputStream out, String encoding) throws XMLStreamException {
		this.encoding = encoding;
		try {
			writer = new OutputStreamWriter(out, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new XMLStreamException(e);
		}
	}
	

	@Override
	public void close() throws XMLStreamException {
		try {
			writer.close();
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public void flush() throws XMLStreamException {
		try {
			writer.flush();
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}

	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return null;
	}

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return null;
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return null;
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
	}

	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
	}

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
	}

	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
		StackEntry entry = stack.peek();
		entry.hasAttributes = true;
		write("" + localName + "= \"" + value + "\"");
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		write(">");
		StackEntry entry = stack.peek();
		if (entry.hasAttributes) {
			entry.hasAttributes = false;
		}
		write( text);
		entry.hasContent = true;

	}

	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
		StackEntry entry = stack.peek();		
		
		StackEntry peeked = stack.peek();
		if (!peeked.hasContent && !peeked.hasChild) {
			write(">"); 
		}
		write("\n");
		write( entry.indent + "\t");
		write( "<!-- " + data.trim() + " -->");
		peeked.hasChild = true;		
			
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		StackEntry entry = stack.pop();
		if (entry.hasAttributes) {
			write( ">");			
		}
		if (entry.hasChild) {
			write("\n");
			write( entry.indent);
			write("</" + entry.tag +">");
		}
		else {
			if (entry.hasContent) {
				write("</" + entry.tag +">");
			}
			else {
				write("/>");
			}
		}
		
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
	}

	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {	}

	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
	}

	@Override
	public void writeStartDocument() throws XMLStreamException {
		write("<?xml version='1.0' encoding='" + encoding + "'?>\n");
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		write("<?xml version='" + version + "' encoding='" + encoding + "'?>\n");
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		write("<?xml version='" + version + "' encoding='" + encoding + "'?>\n");
	}

	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		String indent;
		if (stack.isEmpty()) {
			indent = "";
		}
		else {
			StackEntry peeked = stack.peek();
			if (!peeked.hasContent && !peeked.hasChild) {
				write(">"); 
			}
				
			peeked.hasChild = true;
			indent = peeked.indent + "\t";
			write("\n");			
		}
		
		StackEntry entry = new StackEntry();
		entry.tag = localName;
		entry.indent = indent;
		stack.push(entry);
		
		write( indent);
		write( "<" + localName);
		
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		

	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
	}

	private void write(String content) throws XMLStreamException {
		try {
			writer.write(content);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}
}
