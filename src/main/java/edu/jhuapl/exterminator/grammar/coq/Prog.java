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
package edu.jhuapl.exterminator.grammar.coq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Prog extends CoqToken implements Iterable<Command> {

	private final List<Command> commands;
	
	public Prog(CoqFTParser parser, CoqParser.ProgContext ctx) {
		super(parser, ctx);
		commands = new ArrayList<>(ctx.command().size());
		for(CoqParser.CommandContext command : ctx.command()) {
			commands.add(Command.make(parser, command));
		}
	}
	
	protected Prog(Prog copy) {
		super(copy);
		this.commands = new ArrayList<>(copy.commands.size());
		for(Command command : copy.commands) {
			this.commands.add(command.clone());
		}
	}

	@Override
	public Iterator<Command> iterator() {
		return commands.iterator();
	}
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(commands);
	}
	
	@Override
	public boolean isTerminalNode() { return false; }
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof Prog)) return false;
		
		Prog p = (Prog)anObj;
		return Objects.equals(commands, p.commands);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(commands);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Command command : commands) {
			if(sb.length() > 0) sb.append('\n');
			sb.append(command);
		}
		return sb.toString();
	}
	
	@Override
	public Prog clone() {
		return new Prog(this);
	}
	
}
