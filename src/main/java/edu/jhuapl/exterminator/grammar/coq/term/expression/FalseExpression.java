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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.term.Qualid;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class FalseExpression extends Term implements BooleanExpression {
	
	public static final String TEXT = "FALSE";

	public static boolean applies(CoqParser.TermContext ctx) {
		if(!Qualid.applies(ctx)) return false;
		else return ctx.getText().trim().equalsIgnoreCase(TEXT);
	}
	
	public FalseExpression(CoqFTParser parser, CoqParser.TermContext ctx) {
		super(parser, ctx);
	}
	
	protected FalseExpression(FalseExpression copy) {
		super(copy);
	}

	@Override
	public Element makeCoqDocTerm(Document doc) {
		return CoqDoc.makeKeywordNode(doc, TEXT);
	}

	@Override
	public Operator getOperator() {
		return Operator.NA;
	}

	@Override
	public Term getLeft() {
		return null;
	}

	@Override
	public Term getRight() {
		return null;
	}

	@Override
	public Term asTerm() {
		return this;
	}

	@Override
	public List<CoqToken> getChildren() {
		return Collections.emptyList();
	}
	
	@Override
	public boolean isTerminalNode() { return true; }
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof FalseExpression)) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(TEXT);
	}
	
	@Override
	public String toString() {
		return "{" + TEXT + "}";
	}
	
	@Override
	public FalseExpression clone() {
		return new FalseExpression(this);
	}
	
}
