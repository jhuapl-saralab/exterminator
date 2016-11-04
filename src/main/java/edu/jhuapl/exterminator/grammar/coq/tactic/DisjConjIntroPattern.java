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

public class DisjConjIntroPattern extends CoqToken implements CoqDocable {
    
    /*
disj_conj_intro_pattern: isDisj=TOK_LBRACKET TOK_PIPE? pattern+ (TOK_PIPE pattern+)* TOK_RBRACKET
    |   isConj=TOK_LPAREN pattern (TOK_COMMA pattern)* TOK_RPAREN
    |   isBinary=TOK_LPAREN pattern (TOK_AMP pattern)* TOK_RPAREN
    |   TOK_LBRACKET TOK_RBRACKET EQN naming_intro_pattern // ??????????
    ;
     */

    private final List<Pattern> disjPatterns;

    private final List<Pattern> conjPatterns;

    private final List<Pattern> binaryPatterns;

    private final NamingIntroPattern eqnPattern;

    public DisjConjIntroPattern(CoqFTParser parser, CoqParser.Disj_conj_intro_patternContext ctx) {
        super(parser, ctx);
        this.disjPatterns = new ArrayList<>();
        if(ctx.isDisj != null) {
            // FIXME we need to find the right locations for pipes
            for(CoqParser.PatternContext pattern : ctx.pattern()) {
                this.disjPatterns.add(new Pattern(parser, pattern));
            }
        }
        this.conjPatterns = new ArrayList<>();
        if(ctx.isConj != null) {
            for(CoqParser.PatternContext pattern : ctx.pattern()) {
                this.conjPatterns.add(new Pattern(parser, pattern));
            }
        }
        this.binaryPatterns = new ArrayList<>();
        if(ctx.isBinary != null) {
            for(CoqParser.PatternContext pattern : ctx.pattern()) {
                this.binaryPatterns.add(new Pattern(parser, pattern));
            }
        }
        this.eqnPattern = ctx.naming_intro_pattern() == null ? null :
            new NamingIntroPattern(parser, ctx.naming_intro_pattern());
    }

    protected DisjConjIntroPattern(DisjConjIntroPattern copy) {
        super(copy);
        this.disjPatterns = new ArrayList<>(copy.disjPatterns.size());
        for(Pattern pattern : copy.disjPatterns) {
            this.disjPatterns.add(pattern.clone());
        }
        this.conjPatterns = new ArrayList<>(copy.conjPatterns.size());
        for(Pattern pattern : copy.conjPatterns) {
            this.conjPatterns.add(pattern.clone());
        }
        this.binaryPatterns = new ArrayList<>(copy.binaryPatterns.size());
        for(Pattern pattern : copy.binaryPatterns) {
            this.binaryPatterns.add(pattern.clone());
        }
        this.eqnPattern = copy.eqnPattern == null ? null : copy.eqnPattern.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(disjPatterns, conjPatterns, binaryPatterns, eqnPattern);
    }

    @Override
    public boolean isTerminalNode() { return false; }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc);
        List<Element> elems = new ArrayList<>();
        if(disjPatterns.size() > 0) {
            elems.add(CoqDoc.makeKeywordNode(doc, "["));
            for(int i = 0; i < disjPatterns.size(); i++) {
                if(i > 0) {
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                    elems.add(CoqDoc.makeKeywordNode(doc, "|"));
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                }
                elems.add(disjPatterns.get(i).makeCoqDocTerm(doc));
            }
            elems.add(CoqDoc.makeKeywordNode(doc, "]"));
        } else if(conjPatterns.size() > 0) {
            elems.add(CoqDoc.makeKeywordNode(doc, "("));
            for(int i = 0; i < conjPatterns.size(); i++) {
                if(i > 0) {
                    elems.add(CoqDoc.makeKeywordNode(doc, ","));
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                }
                elems.add(conjPatterns.get(i).makeCoqDocTerm(doc));
            }
            elems.add(CoqDoc.makeKeywordNode(doc, ")"));
        } else if(binaryPatterns.size() > 0) {
            elems.add(CoqDoc.makeKeywordNode(doc, "("));
            for(int i = 0; i < binaryPatterns.size(); i++) {
                if(i > 0) {
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                    elems.add(CoqDoc.makeKeywordNode(doc, "&"));
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                }
                elems.add(binaryPatterns.get(i).makeCoqDocTerm(doc));
            }
            elems.add(CoqDoc.makeKeywordNode(doc, ")"));
        } else {
            elems.add(CoqDoc.makeKeywordNode(doc, "[]eqn"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(eqnPattern.makeCoqDocTerm(doc));
        }
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof DisjConjIntroPattern)) return false;

        DisjConjIntroPattern d = (DisjConjIntroPattern)anObj;
        return Objects.equals(disjPatterns, d.disjPatterns) &&
                Objects.equals(conjPatterns, d.conjPatterns) &&
                Objects.equals(binaryPatterns, d.binaryPatterns) &&
                Objects.equals(eqnPattern, d.eqnPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(disjPatterns, conjPatterns, binaryPatterns,
                eqnPattern);
    }

    @Override
    public String toString() {
        if(disjPatterns.size() > 0) {
            return "{DisjConjIntroPattern disj=" + disjPatterns + "}";
        } else if(conjPatterns.size() > 0) {
            return "{DisjConjIntroPattern conj=" + conjPatterns + "}";
        } else if(binaryPatterns.size() > 0) {
            return "{DisjConjIntroPattern binary=" + binaryPatterns + "}";
        } else {
            return "{DisjConjIntroPattern []eqn:" + eqnPattern + "}";
        }
    }

    @Override
    public DisjConjIntroPattern clone() {
        return new DisjConjIntroPattern(this);
    }

}
