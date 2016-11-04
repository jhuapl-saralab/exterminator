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
package edu.jhuapl.exterminator.grammar.coq.term.expression;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class WhileExpression extends Expression {
	
	public static boolean applies(CoqParser.TermContext ctx) {
		return Expression.applies(ctx) &&
				ctx.expression_term.getText().equalsIgnoreCase("while") &&
				ctx.arg() != null;
	}

	private final Term conditional;
	
	private final Term doTerm;
	
	public WhileExpression(CoqFTParser parser, CoqParser.TermContext ctx) {
		super(parser, ctx);
		
		if(args.size() != 2) {
			throw new IllegalArgumentException(
					"while needs to have exactly two arguments: " +
							fullText());
		}
		
		if(args.get(0).getIdentDefine() != null ||
				args.get(1).getIdentDefine() != null) {
			throw new IllegalArgumentException("Args have define idents: " +
					fullText());
		}
		
//		Term t = args.get(0).getTerm();
//		if(!(t instanceof BooleanExpression)) {
//			throw new IllegalArgumentException("Term isn't a boolean expression: " + t);
//		}
		this.conditional = args.get(0).getTerm();
		this.doTerm = args.get(1).getTerm();
	}
	
	protected WhileExpression(WhileExpression copy) {
		super(copy);
		this.conditional = copy.conditional.clone();
		this.doTerm = copy.doTerm.clone();
	}
	
	public Term getConditional() { return conditional; }
	
	public Term getDoTerm() { return doTerm; }
	
	///////////////////////////////////////////////////////////////////////////
	
	public static final String KW_WHILE = "WHILE", KW_DO = "DO";
	
	@Override
	public Element makeCoqDocTerm(Document doc) {
		return CoqDoc.makeTermNode(doc,
				CoqDoc.makeKeywordNode(doc, KW_WHILE),
				CoqDoc.makeWhitespaceNewlineNode(doc),
				CoqDoc.makeIndentedNode(doc, getConditional().makeCoqDocTerm(doc)),
				CoqDoc.makeWhitespaceNewlineNode(doc),
				CoqDoc.makeKeywordNode(doc, KW_DO),
				CoqDoc.makeWhitespaceNewlineNode(doc),
				CoqDoc.makeIndentedNode(doc, getDoTerm().makeCoqDocTerm(doc)));
	}
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof WhileExpression)) return false;
		
		WhileExpression w = (WhileExpression)anObj;
		return Objects.equals(conditional, w.conditional) &&
				Objects.equals(doTerm, w.doTerm);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(conditional, doTerm);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{While conditional=").append(conditional);
		sb.append(" do=").append(doTerm).append("}");
		return sb.toString();
	}
	
	@Override
	public WhileExpression clone() {
		return new WhileExpression(this);
	}
	
}
