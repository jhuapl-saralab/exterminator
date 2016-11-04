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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CoqUtils {

	public static List<Node> parseList(Node node) {
		Objects.requireNonNull(node);
		
		if(!node.getNodeName().equalsIgnoreCase("list")) {
			throw new IllegalArgumentException("Node is not list: " + node);
		}
		
		int n = node.getChildNodes().getLength();
		List<Node> nodes = new ArrayList<>(n);
		for(int i = 0; i < n; i++) {
			nodes.add(node.getChildNodes().item(i));
		}
		return nodes;
	}
	
	public static List<String> parseStringList(Node node) {
		List<Node> nodes = parseList(node);
		
		List<String> strings = new ArrayList<>(nodes.size());
		for(Node child : nodes) {
			strings.add(parseString(child));
		}
		return strings;
	}
	
	public static String parseOptionString(Node node) {
		Node str = parseOptionNode(node);
		if(str == null) {
			return null;
		} else {
			return parseString(str);
		}
	}
	
	public static Node parseOptionNode(Node node) {
		Objects.requireNonNull(node);
		
		if(!(node instanceof Element) || !node.getNodeName().equalsIgnoreCase("option")) {
			throw new IllegalArgumentException("Node is not option: " + node);
		}
		
		Element e = (Element)node;
		if(e.hasAttribute("val")) {
			String val = e.getAttribute("val");
			if(val.equalsIgnoreCase("some")) {
				if(!e.hasChildNodes()) {
					throw new IllegalArgumentException("Doesn't have children: " + e);
				}
				return e.getFirstChild();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static String parseString(Node node) {
		Objects.requireNonNull(node);
		
		if(!node.getNodeName().equalsIgnoreCase("string")) {
			throw new IllegalArgumentException("Node is not string: " + node);
		}
		
		return node.getTextContent();
	}
	
	public static int parseInt(Node node) {
		Objects.requireNonNull(node);
		
		if(!node.getNodeName().equalsIgnoreCase("int")) {
			throw new IllegalArgumentException("Node is not int: " + node);
		}
		
		return Integer.parseInt(node.getTextContent());
	}
	
	public static Node parsePairFirst(Node node) {
		Objects.requireNonNull(node);
		
		if(!node.getNodeName().equalsIgnoreCase("pair")) {
			throw new IllegalArgumentException("Node is not pair: " + node);
		}
		if(node.getChildNodes().getLength() != 2) {
			throw new IllegalArgumentException("Malformed pair: " + node);
		}
		
		return node.getFirstChild();
	}
	
	public static Node parsePairSecond(Node node) {
		Objects.requireNonNull(node);
		
		if(!node.getNodeName().equalsIgnoreCase("pair")) {
			throw new IllegalArgumentException("Node is not pair: " + node);
		}
		if(node.getChildNodes().getLength() != 2) {
			throw new IllegalArgumentException("Malformed pair: " + node);
		}
		
		return node.getLastChild();
	}
	
}
