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
package edu.jhuapl.exterminator.grammar.coq.term;

import java.util.List;
import java.util.Objects;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;

public class Arg extends CoqToken {
	
	private final Ident identDefine;

	private final Term term;
	
	public Arg(CoqFTParser parser, CoqParser.ArgContext ctx) {
		super(parser, ctx);
		if(ctx.ident() != null) {
			this.identDefine = new Ident(parser, ctx.ident());
		} else {
			this.identDefine = null;
		}
		this.term = Term.make(parser, ctx.term());
	}
	
	public Arg(CoqFTParser parser, CoqParser.TermContext tctx) {
		super(parser, tctx);
		this.identDefine = null;
		this.term = Term.make(parser, tctx);
	}
	
	public Arg(Term term) {
		super(term.fullText());
		this.identDefine = null;
		this.term = term;
	}
	
	protected Arg(Arg copy) {
		super(copy);
		this.identDefine = copy.identDefine == null ? null : copy.identDefine.clone();
		this.term = copy.term.clone();
	}
	
	public Ident getIdentDefine() {
		return identDefine;
	}
	
	public Term getTerm() {
		return term;
	}
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(identDefine, term);
	}
	
	@Override
	public boolean isTerminalNode() {
		return identDefine == null && term.isTerminalNode();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public int hashCode() {
		return Objects.hash(identDefine, term);
	}
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof Arg)) return false;
		
		Arg a = (Arg)anObj;
		return Objects.equals(identDefine, a.identDefine) &&
				Objects.equals(term, a.term);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{Arg");
		if(identDefine != null) sb.append(" define=").append(identDefine);
		sb.append(" term=").append(term);
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public Arg clone() {
		return new Arg(this);
	}
	
}
