/*
 * Copyright (c) 2016, Johns Hopkins University Applied Physics
 * Laboratory All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.jhuapl.exterminator.coq;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtils {
	
	private static final DocumentBuilderFactory documentBuilderFactory;
	
	private final static DocumentBuilder documentBuilder;
	
	private final static TransformerFactory transformerFactory;
	
	private final static Transformer transformer;
	
	static {
		try {
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			transformerFactory = TransformerFactory.newInstance();
			transformer = makeTransformer();
			
		} catch(TransformerFactoryConfigurationError
				| ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Transformer makeTransformer() {
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
		} catch(TransformerConfigurationException e) {
			throw new RuntimeException(e);
		}
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		return transformer;
	}

	public static String docToString(Document doc) {
		StringWriter writer = new StringWriter();
		try {
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
		} catch(TransformerException e) {
			throw new RuntimeException(e);
		}
		return writer.getBuffer().toString().replaceAll("\n|\r", "");
	}
	
	public static String nodeToString(Node node) {
		StringWriter writer = new StringWriter();
		try {
			transformer.transform(new DOMSource(node), new StreamResult(writer));
		} catch(TransformerException e) {
			throw new RuntimeException(e);
		}
		return writer.getBuffer().toString().replaceAll("\n|\r", "");
	}
	
	public static Document docFromString(String str) throws SAXException {
		try {
			return documentBuilder.parse(new InputSource(new StringReader(str)));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Document makeDocument() {
		return documentBuilder.newDocument();
	}
	
}
