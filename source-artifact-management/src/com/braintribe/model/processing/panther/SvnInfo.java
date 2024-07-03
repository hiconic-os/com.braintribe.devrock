// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.model.processing.panther;

import java.io.File;
import java.io.StringReader;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class SvnInfo {
	private static final Pattern pathSplitPattern = Pattern.compile("/");
	private Document document;
	
	public SvnInfo(File target) throws SvnException {
		this(target.toString());
	}
	
	private static Element getFirstElement(Element parent, String tagName) {
		Node childNode = parent.getFirstChild();
		while (childNode != null) {
			if (childNode instanceof Element && 
				(tagName == null || childNode.getNodeName().equals(tagName)))
				return (Element)childNode;
			childNode = childNode.getNextSibling();
		}
		return null;
	}
	
	public static Element getElementByPath(Element parent, String path) {
		String tagNames[] = pathSplitPattern.split(path);
		Element element = parent;
		for (String tagName: tagNames) {
			element = getFirstElement(element, tagName);
			if (element == null) break;
		}
		
		return element;
	}

	public String getUrl() {
		Element element = getElementByPath(document.getDocumentElement(), "entry/url");
		if (element != null) return element.getTextContent();
		else return null;
	}
	
	public String getRoot() {
		Element element = getElementByPath(document.getDocumentElement(), "entry/repository/root");
		if (element != null) return element.getTextContent();
		else return null;
	}
	
	
	/**
	 * @return
	 */
	public String getRevision() {
		Element element = getElementByPath(document.getDocumentElement(), "entry");
		if (element != null) 
			return element.getAttribute("revision");
		else 
			return null;
	}
	public boolean isValid() {
		return getUrl() != null;
	}
	
	public SvnInfo(String target) throws SvnException {
		//String cmdPattern = "svn info --xml \"{0}\"";
		//String cmd = MessageFormat.format(cmdPattern, target);
		String[] cmd = {"svn", "info", "--xml", target};
		
		ProcessResults svnOutput = ProcessExecution.runCommand(cmd);
		
		if (svnOutput.getRetVal() == 0) {
			try {
				DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				document = docBuilder.parse(new InputSource(new StringReader(svnOutput.getNormalText())));
			} catch (Exception e) {
				throw new SvnException(e);
			}
		}
		else {
			throw new SvnException(svnOutput.getErrorText());
		}
	}
	
	public static void main(String[] args) {
		/*
		File file = new File("/Users/micheldocouto/Work/Development/ClientGwt/artifacts-build/com/braintribe/gwt/ExtJsResources");
		//String url = "http://vie-svn/svn/svn-master/Development/artifacts/com/braintribe/utils/IoUtils";

		SvnInfo info = new SvnInfo(file);
		System.out.println(info.getUrl());
		*/
		String url = "http://vie-svn/svn/svn-master/Development/artifacts/com/braintribe/build/artifacts/ArtifactContainer/ArtifactContainer/1.0";
		SvnInfo info = new SvnInfo( url);
		System.out.println("Revision is [" + info.getRevision() + "]");
		
	}
}
