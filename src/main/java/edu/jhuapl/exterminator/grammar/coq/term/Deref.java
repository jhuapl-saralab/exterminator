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

public class Deref extends Term {

	public static boolean applies(CoqParser.TermContext ctx) {
		return ctx.deref() != null;
	}
	
	/*
deref : TOK_LBRACKET qualid TOK_RBRACKET;
	 */
	
	private final Qualid qualid;
	
	public Deref(CoqFTParser parser, CoqParser.TermContext ctx) {
		this(parser, ctx.deref());
	}
	
	public Deref(CoqFTParser parser, CoqParser.DerefContext ctx) {
		super(parser, ctx);
		this.qualid = new Qualid(parser, ctx.qualid());
	}
	
	protected Deref(Deref copy) {
		super(copy);
		this.qualid = copy.qualid.clone();
	}
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(qualid);
	}
	
	@Override
	public Element makeCoqDocTerm(Document doc) {
		return CoqDoc.makeTermNode(doc,
				CoqDoc.makeKeywordNode(doc, "["),
				CoqDoc.makeWhitespaceNode(doc),
				qualid.makeCoqDocTerm(doc),
				CoqDoc.makeWhitespaceNode(doc),
				CoqDoc.makeKeywordNode(doc, "]"));
	}
	
	@Override
	public boolean shouldParenthesize() { return false; }
	
	@Override
	public boolean isTerminalNode() { return false; }
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public int hashCode() {
		return Objects.hash(qualid);
	}
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof Deref)) return false;
		
		Deref d = (Deref)anObj;
		return Objects.equals(qualid, d.qualid);
	}
	
	@Override
	public String toString() {
		return "{Deref qualid=" + qualid + "}";
	}
	
	@Override
	public Deref clone() {
		return new Deref(this);
	}
	
}
