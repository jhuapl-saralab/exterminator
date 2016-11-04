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

import org.w3c.dom.Document;

import edu.jhuapl.exterminator.coq.CoqUtils;
import edu.jhuapl.exterminator.coq.command.CoqRewindCommand;
import edu.jhuapl.exterminator.coq.coq84.Coq84Message;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84RewindCommand;
import edu.jhuapl.exterminator.coq.message.CoqRewindMessage;

public class Coq84RewindMessage extends Coq84Message implements CoqRewindMessage {
	
	private final Coq84RewindCommand command;
	
	private final int extraBacktracking;

	public Coq84RewindMessage(Coq84RewindCommand command, Document doc) {
		super(doc);
		
		this.command = command;
		
		if(statusIsGood()) {
			if(value.getChildNodes().getLength() != 1) {
				throw new IllegalArgumentException("Malformed rewind message: " + doc);
			}
			
			this.extraBacktracking = CoqUtils.parseInt(value.getFirstChild());
		} else {
			this.extraBacktracking = 0;
		}
	}

	@Override
	public CoqRewindCommand getCommand() {
		return command;
	}

	@Override
	public int getExtraBacktracking() {
		return extraBacktracking;
	}
	
}
