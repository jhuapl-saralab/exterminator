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

public class Statements extends Term {
	
	public static boolean applies(CoqParser.TermContext ctx) {
		return ctx.statements() != null;
	}
	
	/*
statements : term (TOK_SEMICOLON_SEMICOLON term)+ TOK_SEMICOLON_SEMICOLON? ;
	 */
	
	private final List<Term> terms;
	
	public Statements(CoqFTParser parser, CoqParser.TermContext ctx) {
		this(parser, ctx.statements());
	}
	
	public Statements(CoqFTParser parser, CoqParser.StatementsContext ctx) {
		super(parser, ctx);
		
		this.terms = new ArrayList<>();
		for(int i = 0; i < ctx.term().size(); i++) {
			this.terms.add(Term.make(parser, ctx.term(i)));
		}
		
//		System.out.println("=====");
//		for(int i = 0; i < ctx.getChildCount(); i++) {
//			System.out.println(ctx.getChild(i).getClass());
//			if(ctx.getChild(i) instanceof ParserRuleContext) {
//				System.out.println(parser.getFullText((ParserRuleContext)ctx.getChild(i)));
//			} else {
//				System.out.println(ctx.getChild(i));
//			}
//		}
//		System.out.println("=====");
//		
//		this.terms = new ArrayList<>(1 + ctx.term().size());
//		this.terms.add(Term.make(parser, ctx.statement));
//		for(int i = 1; i < ctx.term().size(); i++) {
//			this.terms.add(Term.make(parser, ctx.term(i)));
//		}
	}
	
	protected Statements(Statements copy) {
		super(copy);
		this.terms = new ArrayList<>(copy.terms.size());
		for(Term term : copy.terms) {
			this.terms.add(term.clone());
		}
	}
	
	public List<Term> getTerms() {
		return Collections.unmodifiableList(terms);
	}
	
	public int size() {
		return terms.size();
	}
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(terms);
	}
	
	@Override
	public boolean shouldParenthesize() { return true; }
	
	@Override
	public boolean isTerminalNode() { return false; }
	
	@Override
	public Element makeCoqDocTerm(Document doc) {
		Element term = CoqDoc.makeTermNode(doc,
				terms.get(0).makeCoqDocTerm(doc));
		List<Element> elems = new ArrayList<>();
		for(int i = 1; i < terms.size(); i++) {
			elems.add(CoqDoc.makeKeywordNode(doc, ";;"));
			elems.add(CoqDoc.makeWhitespaceNewlineNode(doc));
			elems.add(terms.get(i).makeCoqDocTerm(doc));
		}
		return CoqDoc.mergeTermNodes(term, elems);
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public int hashCode() {
		return Objects.hash(terms);
	}
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof Statements)) return false;
		
		Statements s = (Statements)anObj;
		return Objects.equals(terms, s.terms);
	}
	
	@Override
	public String toString() {
		return "{Statements=" + terms + "}";
	}
	
	@Override
	public Statements clone() {
		return new Statements(this);
	}
	
}
