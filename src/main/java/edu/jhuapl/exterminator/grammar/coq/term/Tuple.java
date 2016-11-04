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
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;

public class Tuple extends Term {

    public static boolean applies(CoqParser.TermContext ctx) {
        return ctx.term_tuple() != null;
    }

    /*
term_tuple : TOK_LTUPLE term (TOK_COMMA term)* TOK_RTUPLE ;
     */

    private final List<Term> terms;

    public Tuple(CoqFTParser parser, CoqParser.TermContext ctx) {
        this(parser, ctx.term_tuple());
    }

    public Tuple(CoqFTParser parser, CoqParser.Term_tupleContext ctx) {
        super(parser, ctx);
        this.terms = new ArrayList<>(ctx.term().size());
        for(CoqParser.TermContext term : ctx.term()) {
            this.terms.add(Term.make(parser, term));
        }
    }

    protected Tuple(Tuple copy) {
        super(copy);
        this.terms = new ArrayList<>(copy.terms.size());
        for(Term term : copy.terms) {
            this.terms.add(term.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(terms);
    }

    @Override
    public boolean shouldParenthesize() { return false; }

    @Override
    public boolean isTerminalNode() { return false; }

    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element term = CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "〈"));
        List<Element> elems = new ArrayList<>();
        for(Term t : terms) {
            if(!elems.isEmpty()) {
                elems.add(CoqDoc.makeKeywordNode(doc, ","));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
            }
            elems.add(t.makeCoqDocTerm(doc));
        }
        elems.add(CoqDoc.makeKeywordNode(doc, "〉"));
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
        if(anObj == null || !(anObj instanceof Tuple)) return false;

        Tuple t = (Tuple)anObj;
        return Objects.equals(terms, t.terms);
    }

    @Override
    public String toString() {
        return "{Tuple=" + terms + "}";
    }

    @Override
    public Tuple clone() {
        return new Tuple(this);
    }
}
