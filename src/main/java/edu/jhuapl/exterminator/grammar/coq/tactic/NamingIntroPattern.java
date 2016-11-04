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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;
import edu.jhuapl.exterminator.grammar.coq.term.Ident;

public class NamingIntroPattern extends CoqToken implements CoqDocable {
    
    /*
naming_intro_pattern: TOK_QUESTION ident?
    | ident
    ;
     */

    private final boolean hasQuestionMark;

    private final Ident ident;

    public NamingIntroPattern(CoqFTParser parser, CoqParser.Naming_intro_patternContext ctx) {
        super(parser, ctx);
        this.hasQuestionMark = ctx.TOK_QUESTION() != null;
        this.ident = ctx.ident() == null ? null : new Ident(parser, ctx.ident());
    }

    protected NamingIntroPattern(NamingIntroPattern copy) {
        super(copy);
        this.hasQuestionMark = copy.hasQuestionMark;
        this.ident = copy.ident == null ? null : copy.ident.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(ident);
    }

    @Override
    public boolean isTerminalNode() { return false; }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc);
        List<Element> elems = new ArrayList<>();
        if(hasQuestionMark) {
            elems.add(CoqDoc.makeKeywordNode(doc, "?"));
        }
        if(hasQuestionMark && ident != null) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
        }
        if(ident != null) {
            elems.add(CoqDoc.makeIdentifierNode(doc, ident));
        }
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof NamingIntroPattern)) return false;

        NamingIntroPattern n = (NamingIntroPattern)anObj;
        return Objects.equals(hasQuestionMark, n.hasQuestionMark) &&
                Objects.equals(ident, n.ident);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasQuestionMark, ident);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{NamingIntroPattern");
        if(hasQuestionMark) sb.append(" ?");
        if(ident != null) sb.append(" ident=").append(ident);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public NamingIntroPattern clone() {
        return new NamingIntroPattern(this);
    }

}
