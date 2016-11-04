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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;

public class NamedFields extends Term {
	
	public static boolean applies(CoqParser.TermContext ctx) {
		return ctx.named_fields() != null;
	}
	
	/*
named_fields : TOK_LBRACE_PIPE ( ident TOK_DEFINE term (TOK_SEMICOLON ident TOK_DEFINE term)* )? TOK_PIPE_RBRACE;
	 */

	//private final Term srf, lc, hpf, dm;
	
	private final LinkedHashMap<Ident, Term> fields;
	
	public NamedFields(CoqFTParser parser, CoqParser.TermContext ctx) {
		super(parser, ctx);
		this.fields = new LinkedHashMap<>();
		CoqParser.Named_fieldsContext c = ctx.named_fields();
		int n = c.ident().size();
		for(int i = 0; i < n; i++) {
			Ident ident = new Ident(parser, c.ident(i));
			Term term = Term.make(parser, c.term(i));
			if(fields.containsKey(ident) && !term.equals(fields.get(ident))) {
				throw new IllegalArgumentException("Duplicate identifier: " + ident);
			}
			fields.put(ident, term);
		}
	}
	
	protected NamedFields(NamedFields copy) {
		super(copy);
		this.fields = new LinkedHashMap<>();
		for(Map.Entry<Ident, Term> entry : copy.fields.entrySet()) {
			this.fields.put(entry.getKey().clone(), entry.getValue().clone());
		}
	}
	
	public int size() { return fields.size(); }
	
	@Override
	public List<CoqToken> getChildren() {
		return makeList(fields);
	}
	
	@Override
	public Element makeCoqDocTerm(Document doc) {
		Element elem = CoqDoc.makeTermNode(doc,
				CoqDoc.makeKeywordNode(doc, "{|"));
		List<Element> elems = new ArrayList<>();
		boolean first = true;
		for(Map.Entry<Ident, Term> entry : fields.entrySet()) {
			if(!first) {
				elems.add(CoqDoc.makeKeywordNode(doc, ";"));
			}
			first = false;
			elems.add(CoqDoc.makeWhitespaceNode(doc));
			elems.add(entry.getKey().makeCoqDocTerm(doc));
			elems.add(CoqDoc.makeWhitespaceNode(doc));
			elems.add(CoqDoc.makeKeywordNode(doc, ":="));
			elems.add(CoqDoc.makeWhitespaceNode(doc));
			elems.add(CoqDoc.makeParenthesizedTermNode(doc, entry.getValue()));
		}
		elems.add(CoqDoc.makeWhitespaceNode(doc));
		elems.add(CoqDoc.makeKeywordNode(doc, "|}"));
		
		return CoqDoc.mergeTermNodes(elem, elems);
	}
	
	@Override
	public boolean shouldParenthesize() { return false; }
	
	@Override
	public boolean isTerminalNode() { return false; }
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(anObj instanceof NamedFields)) return false;
		
		NamedFields l = (NamedFields)anObj;
		return Objects.equals(fields, l.fields);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(fields);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{NamedFields");
		for(Map.Entry<Ident, Term> entry : fields.entrySet()) {
			sb.append(' ');
			sb.append(entry.getKey()).append("=").append(entry.getValue());
		}
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public NamedFields clone() {
		return new NamedFields(this);
	}
	
}
