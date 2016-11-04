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

public class Name extends CoqToken implements ID {
	
	private final Ident ident;
	
	private final boolean isUnderscore;
	
	public Name(CoqFTParser parser, CoqParser.NameContext ctx) {
		super(parser, ctx);
		if(ctx.ident() != null) {
			this.ident = new Ident(parser, ctx.ident());
			this.isUnderscore = false;
		} else {
			this.ident = null;
			this.isUnderscore = true;
		}
	}
	
	protected Name(Name copy) {
		super(copy);
		this.ident = copy.ident == null ? null : copy.ident.clone();
		this.isUnderscore = copy.isUnderscore;
	}
	
	public boolean isUnderscore() { return isUnderscore; }
	
	public Ident getIdent() { return ident; }

	@Override
	public String getFullName() {
		return isUnderscore ? "_" : ident.getFullName();
	}
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(ident);
	}
	
	@Override
	public CoqToken asToken() { return this; }
	
	public Element makeCoqDocTerm(Document doc) {
		if(isUnderscore) {
			return CoqDoc.makeTermNode(doc, CoqDoc.makeIdentifierNode(doc, "_"));
		} else {
			return ident.makeCoqDocTerm(doc);
		}
	}
	
	@Override
	public boolean isTerminalNode() { return true; }
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public int hashCode() {
		return Objects.hash(ident, isUnderscore);
	}
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null) return false;
		
		if(anObj instanceof Name) {
			Name n = (Name)anObj;
			return Objects.equals(ident, n.ident) &&
					Objects.equals(isUnderscore, n.isUnderscore);
		} else if(anObj instanceof Ident) {
			Ident i = (Ident)anObj;
			if(isUnderscore) return i.getFullName().equals("_");
			else return ident.equals(i);
		} else if(anObj instanceof Qualid) {
			Qualid q = (Qualid)anObj;
			if(q.getFirst() != null) return false;
			
			Ident i = q.getIdent();
			if(isUnderscore) return i.getFullName().equals("_");
			else return ident.equals(i);
		} else if(anObj instanceof ID) {
			throw new UnsupportedOperationException("This needs to be implemented.");
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "{Name=" + (isUnderscore ? "\"_\"" : ident) + "}";
	}
	
	@Override
	public Name clone() {
		return new Name(this);
	}

}
