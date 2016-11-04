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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;

public class Binder extends CoqToken implements CoqDocable {
	
	/*
binder : name
    |   TOK_LPAREN name+ TOK_COLON binderTerm=term TOK_RPAREN
    |   name+ TOK_COLON binderTerm=term
    |   TOK_LPAREN name (TOK_COLON binderTerm=term)? TOK_DEFINE defineTerm=term TOK_RPAREN
    ;    
	 */
	
	private final List<Name> names;
	
	private final Term binderTerm;
	
	private final Term defineTerm;

	public Binder(CoqFTParser parser, CoqParser.BinderContext ctx) {
		super(parser, ctx);
		this.names = new ArrayList<>(ctx.name().size());
		for(CoqParser.NameContext name : ctx.name()) {
			names.add(new Name(parser, name));
		}
		
		if(ctx.binderTerm != null) {
			this.binderTerm = Term.make(parser, ctx.binderTerm);
		} else {
			this.binderTerm = null;
		}
		
		if(ctx.defineTerm != null) {
			this.defineTerm = Term.make(parser, ctx.defineTerm);
		} else {
			this.defineTerm = null;
		}
	}
	
	protected Binder(Binder copy) {
		super(copy);
		this.names = new ArrayList<>(copy.names.size());
		for(Name name : copy.names) {
			this.names.add(name.clone());
		}
		this.binderTerm = copy.binderTerm == null ? null : copy.binderTerm.clone();
		this.defineTerm = copy.defineTerm == null ? null : copy.defineTerm.clone();
	}
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(names, binderTerm, defineTerm);
	}
	
	public List<Name> getNames() {
		return Collections.unmodifiableList(names);
	}
	
	@Override
	public boolean isTerminalNode() { return false; }
	
	/*
binder : name
    |   TOK_LPAREN name+ TOK_COLON binderTerm=term TOK_RPAREN
    |   name+ TOK_COLON binderTerm=term
    |   TOK_LPAREN name (TOK_COLON binderTerm=term)? TOK_DEFINE defineTerm=term TOK_RPAREN
    ;    
	 */
	
	@Override
	public Element makeCoqDocTerm(Document doc) {
		if(names.size() == 1 && binderTerm == null && defineTerm == null) {
			// name case
			return names.get(0).makeCoqDocTerm(doc);
		}
		
		Element term = CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "("));
		List<Element> elems = new ArrayList<>();
		
		for(int i = 0; i < names.size(); i++) {
			if(i > 0) elems.add(CoqDoc.makeWhitespaceNode(doc));
			elems.add(names.get(i).makeCoqDocTerm(doc));
		}
		
		if(binderTerm != null) {
			elems.add(CoqDoc.makeWhitespaceNode(doc));
			elems.add(CoqDoc.makeKeywordNode(doc, ":"));
			elems.add(CoqDoc.makeWhitespaceNode(doc));
			elems.add(binderTerm.makeCoqDocTerm(doc));
		}
		
		if(defineTerm != null) {
			elems.add(CoqDoc.makeWhitespaceNode(doc));
			elems.add(CoqDoc.makeKeywordNode(doc, ":="));
			elems.add(CoqDoc.makeWhitespaceNode(doc));
			elems.add(defineTerm.makeCoqDocTerm(doc));
		}
		
		
		elems.add(CoqDoc.makeKeywordNode(doc, ")"));
		return CoqDoc.mergeTermNodes(term, elems);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public int hashCode() {
		return Objects.hash(names, binderTerm, defineTerm);
	}
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof Binder)) return false;
		
		Binder b = (Binder)anObj;
		return Objects.equals(names, b.names) &&
				Objects.equals(binderTerm, b.binderTerm) &&
				Objects.equals(defineTerm, b.defineTerm);
	}
	
	@Override
	public String toString() {
		return "{Binder names=" + names + " binderTerm=" + binderTerm + " defineTerm=" + defineTerm + "}";
	}
	
	@Override
	public Binder clone() {
		return new Binder(this);
	}
	
}
