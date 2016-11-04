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

public class Completes extends Term {

	public static boolean applies(CoqParser.TermContext ctx) {
		return ctx.term_completes() != null;
	}
	
	private final Term st1;
	
	private final Term st2;
	
	private final Statements statements;
	
	public Completes(CoqFTParser parser, CoqParser.TermContext ctx) {
		this(parser, ctx.term_completes());
	}
	
	public Completes(CoqFTParser parser, CoqParser.Term_completesContext ctx) {
		super(parser, ctx);
		
		this.st1 = Term.make(parser, ctx.term(0));
		this.st2 = Term.make(parser, ctx.term(1));
		this.statements = new Statements(parser, ctx.statements());
	}
	
	protected Completes(Completes copy) {
		super(copy);
		this.st1 = copy.st1.clone();
		this.st2 = copy.st2.clone();
		this.statements = copy.statements.clone();
	}
	
	public Term getSt1() { return st1; }
	
	public Term getSt2() { return st2; }
	
	public Statements getStatements() { return statements; }

	@Override
	public Element makeCoqDocTerm(Document doc) {
		return CoqDoc.makeTermNode(doc,
				CoqDoc.makeKeywordNode(doc, "completes"),
				CoqDoc.makeWhitespaceNode(doc),
				CoqDoc.makeParenthesizedTermNode(doc, st1),
				CoqDoc.makeWhitespaceNode(doc),
				CoqDoc.makeParenthesizedTermNode(doc, st2),
				CoqDoc.makeWhitespaceNode(doc),
				CoqDoc.makeParenthesizedTermNode(doc, statements));
	}

	@Override
	public Term clone() {
		return new Completes(this);
	}

	@Override
	public List<CoqToken> getChildren() {
		return makeList(st1, st2, statements);
	}

	@Override
	public boolean isTerminalNode() {
		return false;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof Completes)) return false;
		
		Completes c = (Completes)anObj;
		return Objects.equals(st1, c.st1) &&
				Objects.equals(st2, c.st2) &&
				Objects.equals(statements, c.statements);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(st1, st2, statements);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{Completes st1=").append(st1);
		sb.append(" st2=").append(st2);
		sb.append(" statements=").append(statements).append("}");
		return sb.toString();
	}
}
