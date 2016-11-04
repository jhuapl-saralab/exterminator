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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;

public class Fun extends Term {

	public static boolean applies(CoqParser.TermContext ctx) {
		return ctx.term_fun() != null;
	}
	
	/*
term_fun : FUN binders TOK_EQUAL_GT term ;
	 */
	
	private final List<Binder> binders;
	
	private final Term term;
	
	public Fun(CoqFTParser parser, CoqParser.TermContext ctx) {
		this(parser, ctx.term_fun());
	}
	
	public Fun(CoqFTParser parser, CoqParser.Term_funContext ctx) {
		super(parser, ctx);
		this.binders = new ArrayList<>(ctx.binders().binder().size());
		for(CoqParser.BinderContext bctx : ctx.binders().binder()) {
			this.binders.add(new Binder(parser, bctx));
		}
		this.term = Term.make(parser, ctx.term());
	}
	
	protected Fun(Fun copy) {
		super(copy);
		this.binders = new ArrayList<>(copy.binders.size());
		for(Binder binder : copy.binders) {
			this.binders.add(binder.clone());
		}
		this.term = copy.term.clone();
	}
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(binders, term);
	}
	
	@Override
	public Element makeCoqDocTerm(Document doc) {
		Element term = CoqDoc.makeTermNode(doc,
				CoqDoc.makeKeywordNode(doc, "fun"));
		
		List<Element> elems = new ArrayList<>();
		for(Binder binder : binders) {
			elems.add(CoqDoc.makeWhitespaceNode(doc));
			elems.add(binder.makeCoqDocTerm(doc));
		}
		
		elems.add(CoqDoc.makeWhitespaceNode(doc));
		elems.add(CoqDoc.makeKeywordNode(doc, "=>"));
		elems.add(CoqDoc.makeWhitespaceNode(doc));
		elems.add(this.term.makeCoqDocTerm(doc));
		
		return CoqDoc.mergeTermNodes(term, elems);
	}
	
	@Override
	public boolean isTerminalNode() { return false; }
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public int hashCode() {
		return Objects.hash(binders, term);
	}
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof Fun)) return false;
		
		Fun f = (Fun)anObj;
		return Objects.equals(binders, f.binders) &&
				Objects.equals(term, f.term);
	}
	
	@Override
	public String toString() {
		return "{Fun binders=" + binders + " term=" + term + "}";
	}
	
	@Override
	public Fun clone() {
		return new Fun(this);
	}
	
}
