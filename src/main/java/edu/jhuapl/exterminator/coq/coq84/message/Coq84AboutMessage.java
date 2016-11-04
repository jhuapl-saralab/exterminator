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
package edu.jhuapl.exterminator.coq.coq84.message;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.jhuapl.exterminator.coq.CoqUtils;
import edu.jhuapl.exterminator.coq.coq84.Coq84Message;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84AboutCommand;
import edu.jhuapl.exterminator.coq.message.CoqAboutMessage;

public class Coq84AboutMessage extends Coq84Message implements CoqAboutMessage {
	
	private final Coq84AboutCommand command;
	
	private final Node info;
	
	private final String version, protocol, release, compile;

	public Coq84AboutMessage(Coq84AboutCommand command, Document doc) {
		super(doc);
		
		this.command = Objects.requireNonNull(command);
		
		if(value.getChildNodes().getLength() != 1) {
			throw new IllegalArgumentException("Value has too many children.");
		}
		this.info = value.getFirstChild();
		if(!info.getNodeName().equalsIgnoreCase("coq_info")) {
			throw new IllegalArgumentException("Info node is not of type coq_info.");
		}
		
		if(info.getChildNodes().getLength() != 4) {
			throw new IllegalArgumentException("Malformed info node: " + info);
		}
		
		Node node = info.getFirstChild();
		this.version = CoqUtils.parseString(node);
		
		node = node.getNextSibling();
		this.protocol = CoqUtils.parseString(node);
		
		node = node.getNextSibling();
		this.release = CoqUtils.parseString(node);
		
		node = node.getNextSibling();
		this.compile = CoqUtils.parseString(node);
	}
	
	@Override
	public String getVersion() { return version; }
	
	@Override
	public String getProtocol() { return protocol; }
	
	@Override
	public String getRelease() { return release; }
	
	@Override
	public String getCompile() { return compile; }
	
	@Override
	public Coq84AboutCommand getCommand() {
		return command;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("version=\"").append(getVersion()).append("\",");
		sb.append("protocol=\"").append(getProtocol()).append("\",");
		sb.append("release=\"").append(getRelease()).append("\",");
		sb.append("compile=\"").append(getCompile()).append("\"");
		sb.append("}");
		return sb.toString();
	}

}
