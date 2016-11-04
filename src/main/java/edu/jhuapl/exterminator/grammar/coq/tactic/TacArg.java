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
package edu.jhuapl.exterminator.grammar.coq.tactic;

import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;
import edu.jhuapl.exterminator.grammar.coq.term.Qualid;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class TacArg extends CoqToken implements CoqDocable {

    /*
	tacarg : qualid
    |   TOK_LPAREN TOK_RPAREN
    |   LTAC TOK_COLON atom
    |   term
    ;
     */

    private final Qualid qualid;

    private final boolean isParens;

    private final Atom ltacAtom;

    private final Term term;

    public TacArg(CoqFTParser parser, CoqParser.TacargContext ctx) {
        super(parser, ctx);
        this.qualid = ctx.qualid() == null ? null : new Qualid(parser, ctx.qualid());
        this.isParens = ctx.TOK_LPAREN() != null && ctx.TOK_RPAREN() != null;
        this.ltacAtom = ctx.atom() == null ? null : new Atom(parser, ctx.atom());
        this.term = ctx.term() == null ? null : Term.make(parser, ctx.term());
    }

    protected TacArg(TacArg copy) {
        super(copy);
        this.qualid = copy.qualid == null ? null : copy.qualid.clone();
        this.isParens = copy.isParens;
        this.ltacAtom = copy.ltacAtom == null ? null : copy.ltacAtom.clone();
        this.term = copy.term == null ? null : copy.term.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(qualid, ltacAtom, term);
    }

    @Override
    public boolean isTerminalNode() { return false; }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        if(qualid != null) {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeIdentifierNode(doc, qualid));
        } else if(isParens) {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "()"));
        } else if(ltacAtom != null) {
            return CoqDoc.makeTermNode(doc,
                    CoqDoc.makeKeywordNode(doc, "ltac"),
                    CoqDoc.makeWhitespaceNode(doc),
                    CoqDoc.makeKeywordNode(doc, ":"),
                    CoqDoc.makeWhitespaceNode(doc),
                    ltacAtom.makeCoqDocTerm(doc));
        } else {
            return term.makeCoqDocTerm(doc);
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof TacArg)) return false;

        TacArg t = (TacArg)anObj;
        return Objects.equals(qualid, t.qualid) &&
                Objects.equals(isParens, t.isParens) &&
                Objects.equals(ltacAtom, t.ltacAtom) &&
                Objects.equals(term, t.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualid, isParens, ltacAtom, term);
    }

    @Override
    public String toString() {
        if(qualid != null) {
            return "{TacArg qualid=" + qualid + "}";
        } else if(isParens) {
            return "{TacArg ()}";
        } else if(ltacAtom != null) {
            return "{TacArg ltac : atom=" + ltacAtom + "}";
        } else {
            return "{TacArg term=" + term + "}";
        }
    }

    @Override
    public TacArg clone() {
        return new TacArg(this);
    }

}
