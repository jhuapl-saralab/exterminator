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

public class IntroPattern extends CoqToken implements CoqDocable {
    
    /*
intro_pattern: naming_intro_pattern
    | disj_conj_intro_pattern
    | UNDERSCORE
    | TOK_LARROW
    | TOK_IMPLIES
    ;
     */

    private final NamingIntroPattern naming;

    private final DisjConjIntroPattern disjConj;

    private final boolean isUnderscore, isLArrow, isRArrow;

    public IntroPattern(CoqFTParser parser, CoqParser.Intro_patternContext ctx) {
        super(parser, ctx);
        this.naming = ctx.naming_intro_pattern() == null ? null :
            new NamingIntroPattern(parser, ctx.naming_intro_pattern());
        this.disjConj = ctx.disj_conj_intro_pattern() == null ? null :
            new DisjConjIntroPattern(parser, ctx.disj_conj_intro_pattern());
        this.isUnderscore = ctx.UNDERSCORE() != null;
        this.isLArrow = ctx.TOK_LARROW() != null;
        this.isRArrow = ctx.TOK_IMPLIES() != null;
    }

    protected IntroPattern(IntroPattern copy) {
        super(copy);
        this.naming = copy.naming == null ? null : copy.naming.clone();
        this.disjConj = copy.disjConj == null ? null : copy.disjConj.clone();
        this.isUnderscore = copy.isUnderscore;
        this.isLArrow = copy.isLArrow;
        this.isRArrow = copy.isRArrow;
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(naming, disjConj);
    }

    @Override
    public boolean isTerminalNode() {
        return isUnderscore || isLArrow || isRArrow;
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        if(naming != null) {
            return naming.makeCoqDocTerm(doc);
        } else if(disjConj != null) {
            return disjConj.makeCoqDocTerm(doc);
        } else if(isUnderscore) {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "_"));
        } else if(isLArrow) {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "<-"));
        } else {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "->"));
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof IntroPattern)) return false;

        IntroPattern i = (IntroPattern)anObj;
        return Objects.equals(naming, i.naming) &&
                Objects.equals(disjConj, i.disjConj) &&
                Objects.equals(isUnderscore, i.isUnderscore) &&
                Objects.equals(isLArrow, i.isLArrow) &&
                Objects.equals(isRArrow, i.isRArrow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(naming, disjConj, isUnderscore, isLArrow,
                isRArrow);
    }

    @Override
    public String toString() {
        if(naming != null) {
            return "{IntroPattern " + naming + "}";
        } else if(disjConj != null) {
            return "{IntroPattern " + disjConj + "}";
        } else if(isUnderscore) {
            return "{IntroPattern _}";
        } else if(isLArrow) {
            return "{IntroPattern <-}";
        } else {
            return "{IntroPattern ->}";
        }
    }

    @Override
    public IntroPattern clone() {
        return new IntroPattern(this);
    }

}
