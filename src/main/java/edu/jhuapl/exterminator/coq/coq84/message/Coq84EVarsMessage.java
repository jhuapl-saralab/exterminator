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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.jhuapl.exterminator.coq.CoqUtils;
import edu.jhuapl.exterminator.coq.coq84.Coq84Message;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84EVarsCommand;
import edu.jhuapl.exterminator.coq.message.CoqEVarsMessage;

public class Coq84EVarsMessage extends Coq84Message implements CoqEVarsMessage {
	
	private final Coq84EVarsCommand command;
	
	private final List<EVar84> evars;

	public Coq84EVarsMessage(Coq84EVarsCommand command, Document doc) {
		super(doc);
		
		this.command = Objects.requireNonNull(command);
		
		this.evars = new ArrayList<>();
		
		if(value.getChildNodes().getLength() != 1) {
			throw new IllegalArgumentException("Malformed evars: " + value);
		}
		
		Node node = CoqUtils.parseOptionNode(value.getFirstChild());
		if(node != null) {
			List<Node> nodes = CoqUtils.parseList(node);
			if(nodes != null) {
				for(Node evar : nodes) {
					evars.add(new EVar84(evar));
				}
			}
		}
	}
	
	@Override
	public List<? extends EVar> getEVars() {
		return Collections.unmodifiableList(evars);
	}
	
	@Override
	public Coq84EVarsCommand getCommand() {
		return command;
	}
	
	public static class EVar84 implements EVar {
		
		private final String str;
		
		public EVar84(Node node) {
			if(!node.getNodeName().equalsIgnoreCase("evar")) {
				throw new IllegalArgumentException("Node is not evar: " + node);
			}
			
			this.str = node.getTextContent();
		}
		
		@Override
		public String getEVarString() { return str; }
	}

}
