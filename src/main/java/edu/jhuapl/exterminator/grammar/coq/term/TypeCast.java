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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;

public class TypeCast extends Term {
	
	public static boolean applies(CoqParser.TermContext ctx) {
		return ctx.typeCast != null;
	}
	
	/*
term typeCast=TOK_COLON type
	 */
	
	private final Term term, type;
	
	public TypeCast(CoqFTParser parser, CoqParser.TermContext ctx) {
		super(parser, ctx);
		
		this.term = Term.make(parser, ctx.term(0));
		this.type = Term.make(parser, ctx.type().term());
	}
	
	protected TypeCast(TypeCast copy) {
		super(copy);
		this.term = copy.term.clone();
		this.type = copy.type.clone();
	}
	
	public Term getTerm() { return term; }
	
	public Term getType() { return type; }
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(term, type);
	}
	
	@Override
	public boolean isTerminalNode() { return false; }
	
	@Override
	public Element makeCoqDocTerm(Document doc) {
		return CoqDoc.makeTermNode(doc,
				term.makeCoqDocTerm(doc),
				CoqDoc.makeWhitespaceNode(doc),
				CoqDoc.makeKeywordNode(doc, ":"),
				CoqDoc.makeWhitespaceNode(doc),
				type.makeCoqDocTerm(doc));
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof TypeCast)) return false;
		
		TypeCast tc = (TypeCast)anObj;
		return Objects.equals(term, tc.term) &&
				Objects.equals(type, tc.type);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(term, type);
	}
	
	@Override
	public String toString() {
		return "{TypeCast term=" + term + " type=" + type + "}";
	}
	
	@Override
	public TypeCast clone() {
		return new TypeCast(this);
	}

}
