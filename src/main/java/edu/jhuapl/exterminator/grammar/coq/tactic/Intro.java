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
import edu.jhuapl.exterminator.grammar.coq.term.Ident;
import edu.jhuapl.exterminator.grammar.coq.term.Num;

public class Intro extends TacticExpr {

    /*
    |   INTRO ident?
    |   INTROS ident*
    |   INTROS UNTIL (ident|NUM)
    |   INTRO ident1=ident? (AFTER|BEFORE) ident2=ident
    |   INTRO ident? AT (TOP|BOTTOM)
    |   INTROS intro_pattern+
     */

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.INTRO() != null || ctx.INTROS() != null;
    }

    private final boolean isIntros;

    private final List<Ident> idents;

    private final Ident ident1, ident2;

    private final Num num;

    private final boolean isUntil, isAfter, isBefore, isTop, isBottom;

    private final List<IntroPattern> patterns;

    public Intro(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);
        this.isIntros = ctx.INTROS() != null;
        this.idents = new ArrayList<>();
        for(CoqParser.IdentContext ident : ctx.ident()) {
            this.idents.add(new Ident(parser, ident));
        }
        this.ident1 = ctx.ident1 == null ? null : new Ident(parser, ctx.ident1);
        this.ident2 = ctx.ident2 == null ? null : new Ident(parser, ctx.ident2);
        this.num = ctx.NUM() == null || ctx.NUM().size() == 0 ? null : new Num(parser, ctx.NUM(0));
        this.isUntil = ctx.UNTIL() != null;
        this.isAfter = ctx.AFTER() != null;
        this.isBefore = ctx.BEFORE() != null;
        this.isTop = ctx.TOP() != null;
        this.isBottom = ctx.BOTTOM() != null;
        this.patterns = new ArrayList<>();
        for(CoqParser.Intro_patternContext pattern : ctx.intro_pattern()) {
            this.patterns.add(new IntroPattern(parser, pattern));
        }
    }

    protected Intro(Intro copy) {
        super(copy);
        this.isIntros = copy.isIntros;
        this.idents = new ArrayList<>(copy.idents.size());
        for(Ident ident : copy.idents) {
            this.idents.add(ident.clone());
        }
        this.ident1 = copy.ident1 == null ? null : copy.ident1.clone();
        this.ident2 = copy.ident2 == null ? null : copy.ident2.clone();
        this.num = copy.num == null ? null : copy.num.clone();
        this.isUntil = copy.isUntil;
        this.isAfter = copy.isAfter;
        this.isBefore = copy.isBefore;
        this.isTop = copy.isTop;
        this.isBottom = copy.isBottom;
        this.patterns = new ArrayList<>(copy.patterns.size());
        for(IntroPattern pattern : copy.patterns) {
            this.patterns.add(pattern.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(idents, ident1, ident2, num, patterns);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc,
                CoqDoc.makeKeywordNode(doc, isIntros ? "intros" : "intro"));
        List<Element> elems = new ArrayList<>();
        
        if(isUntil) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "until"));
        }
        
        if(isAfter || isBefore) {
            if(ident1 != null) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeIdentifierNode(doc, ident1));
            }
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, isAfter ? "after" : "before"));
            elems.add(CoqDoc.makeIdentifierNode(doc, ident2));
            
        } else if(num != null) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(num.makeCoqDocTerm(doc));
            
        } else if(idents.size() > 0) {
            for(Ident ident : idents) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeIdentifierNode(doc, ident));
            }
            if(isTop || isBottom) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeKeywordNode(doc, "at"));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeKeywordNode(doc, isTop ? "top" : "bottom"));
            }
        }
        
        if(patterns.size() > 0) {
            for(IntroPattern pattern : patterns) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(pattern.makeCoqDocTerm(doc));
            }
        }
        
        return CoqDoc.mergeTermNodes(elem, elems);
    }
    
    @Override
    public String getDescription() {
        return "<Intro description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Intro)) return false;

        Intro i = (Intro)anObj;
        return Objects.equals(isIntros, i.isIntros) &&
                Objects.equals(idents, i.idents) &&
                Objects.equals(ident1, i.ident1) &&
                Objects.equals(ident2, i.ident2) &&
                Objects.equals(num, i.num) &&
                Objects.equals(isUntil, i.isUntil) &&
                Objects.equals(isAfter, i.isAfter) &&
                Objects.equals(isBefore, i.isBefore) &&
                Objects.equals(isTop, i.isTop) &&
                Objects.equals(isBottom, i.isBottom) &&
                Objects.equals(patterns, i.patterns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isIntros, idents, ident1, ident2, num, isUntil,
                isAfter, isBefore, isTop, isBottom, patterns);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{Intro");
        if(isIntros)sb.append("s");

        if(isUntil) {
            sb.append(" until ");
            if(num != null) sb.append("num=").append(num);
            else sb.append("ident=").append(idents.get(0));
        } else if(isAfter || isBefore) {
            if(ident1 != null) sb.append(" ident1=").append(ident1);

            if(isAfter) sb.append(" after ");
            else sb.append(" before ");

            sb.append("ident2=").append(ident2);
        } else if(isTop || isBottom) {
            if(idents.size() > 0) sb.append(" ident=").append(idents.get(0));
            sb.append(" at ");

            if(isTop) sb.append("top");
            else sb.append("bottom");
        } else if(patterns.size() > 0) {
            sb.append(" patterns=").append(patterns);
        } else if(idents.size() > 0) {
            sb.append(" idents=").append(idents);
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public Intro clone() {
        return new Intro(this);
    }
    
}
