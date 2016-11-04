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
package edu.jhuapl.exterminator.grammar.coq.sentence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.term.Binder;
import edu.jhuapl.exterminator.grammar.coq.term.Ident;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Assertion extends CoqToken {

	public enum Type {
		THEOREM,
		
		LEMMA,
		
		REMARK,
		
		FACT,
		
		COROLLARY,
		
		PROPOSITION,
		
		DEFINITION,
		
		EXAMPLE;
	}
	
	private final Type type;
	
	private final Ident ident;
	
	private final List<Binder> binders;
	
	private final Term term;
	
	public Assertion(CoqFTParser parser, CoqParser.AssertionContext ctx) {
		super(parser, ctx);
		this.type = Type.valueOf(ctx.assertion_keyword().getText().toUpperCase());
		this.ident = new Ident(parser, ctx.ident());
		this.binders = new ArrayList<>();
		if(ctx.binders() != null && ctx.binders().binder() != null) {
			for(CoqParser.BinderContext binder : ctx.binders().binder()) {
				this.binders.add(new Binder(parser, binder));
			}
		}
		this.term = Term.make(parser, ctx.term());
	}
	
	protected Assertion(Assertion copy) {
		super(copy);
		this.type = copy.type;
		this.ident = copy.ident.clone();
		this.binders = new ArrayList<>(copy.binders.size());
		for(Binder binder : copy.binders) {
			this.binders.add(binder.clone());
		}
		this.term = copy.term.clone();
	}
	
	public Type getType() { return type; }
	
	public Ident getIdent() { return ident; }
	
	public List<Binder> getBinders() { return binders; }
	
	public Term getTerm() { return term; }
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(ident, binders, term);
	}
	
	@Override
	public boolean isTerminalNode() { return false; }
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public int hashCode() {
		return Objects.hash(type, ident, binders, term);
	}
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof Assertion)) return false;
		
		Assertion a = (Assertion)anObj;
		return Objects.equals(type, a.type) &&
				Objects.equals(ident, a.ident) &&
				Objects.equals(binders, a.binders) &&
				Objects.equals(term, a.term);
	}
	
	@Override
	public String toString() {
		return "{Assertion type=" + type + " ident=" + ident + " binders=" + binders + " term=" + term + "}";
	}
	
	@Override
	public Assertion clone() {
		return new Assertion(this);
	}
	
}
