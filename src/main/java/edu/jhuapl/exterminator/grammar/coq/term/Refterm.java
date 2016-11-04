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

public class Refterm extends Term {

	public static boolean applies(CoqParser.TermContext ctx) {
		return ctx.ref_left != null && ctx.ref_right != null;
	}
	
	/*
ref_left=term TOK_COLON_COLON ref_right=term
	 */
	
	private final Term left, right;
	
	public Refterm(CoqFTParser parser, CoqParser.TermContext ctx) {
		super(parser, ctx);
		this.left = Term.make(parser, ctx.ref_left);
		this.right = Term.make(parser, ctx.ref_right);
	}
	
	protected Refterm(Refterm copy) {
		super(copy);
		this.left = copy.left.clone();
		this.right = copy.right.clone();
	}
	
	public Term getLeft() { return left; }
	
	public Term getRight() { return right; }
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(left, right);
	}
	
	@Override
	public boolean isTerminalNode() { return false; }
	
	@Override
	public Element makeCoqDocTerm(Document doc) {
		return CoqDoc.makeTermNode(doc,
				left.makeCoqDocTerm(doc),
				CoqDoc.makeWhitespaceNode(doc),
				CoqDoc.makeKeywordNode(doc, "::"),
				CoqDoc.makeWhitespaceNode(doc),
				right.makeCoqDocTerm(doc));
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof Refterm)) return false;
		
		Refterm r = (Refterm)anObj;
		return Objects.equals(left, r.left) &&
				Objects.equals(right, r.right);
	}
	
	@Override
	public String toString() {
		return "{Refterm left=" + left + " right=" + right + "}";
	}
	
	@Override
	public Refterm clone() {
		return new Refterm(this);
	}
	
}
