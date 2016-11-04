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

import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Implies extends Term implements BooleanExpression {

	public static boolean applies(CoqParser.TermContext ctx) {
		return ctx.TOK_IMPLIES() != null && ctx.left_term != null &&
				ctx.right_term != null;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	/*
left=term TOK_IMPLIES right=term
	 */
	
	private final Term left, right;
	
	public Implies(CoqFTParser parser, CoqParser.TermContext ctx) {
		super(parser, ctx);
		
		if(ctx.left_args == null || ctx.left_args.arg().size() == 0) {
			this.left = Term.make(parser, ctx.left_term);
		} else {
			Expression e = new Expression(parser, ctx.left_term, ctx.left_args.arg());
			BooleanExpression be = BooleanExpression.Factory.convert(e);
			this.left = be != null ? (Expression)be : e;
		}
		
		if(ctx.right_args == null || ctx.right_args.arg().size() == 0) {
			this.right = Term.make(parser, ctx.right_term);
		} else {
			Expression e = new Expression(parser, ctx.right_term, ctx.right_args.arg());
			BooleanExpression be = BooleanExpression.Factory.convert(e);
			this.right = be != null ? (Expression)be : e;
		}
	}
	
	protected Implies(Implies copy) {
		super(copy);
		this.left = copy.left.clone();
		this.right = copy.right.clone();
	}
	
	@Override
	public Term getLeft() { return left; }
	
	@Override
	public Term getRight() { return right; }
	
	@Override
	public Operator getOperator() {	return Operator.IMPLIES; }

	@Override
	public Term asTerm() { return this; }
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(left, right);
	}
	
	@Override
	public Element makeCoqDocTerm(Document doc) {
		return AbstractBooleanExpression.makeCoqDocTerm(doc, this);
	}
	
	@Override
	public boolean isTerminalNode() { return false; }
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof Implies)) return false;
		
		Implies i = (Implies)anObj;
		return Objects.equals(left, i.left) &&
				Objects.equals(right, i.right);
	}
	
	@Override
	public String toString() {
		return "{Implies left=" + left + " right=" + right + "}";
	}
	
	@Override
	public Implies clone() {
		return new Implies(this);
	}
	
}
