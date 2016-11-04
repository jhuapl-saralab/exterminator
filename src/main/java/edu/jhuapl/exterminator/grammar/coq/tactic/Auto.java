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

public class Auto extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.AUTO() != null || ctx.EAUTO() != null;
    }

    /*
    |   AUTO NUM?
    |   AUTO WITH (TOK_MULT | ident+)
    |   AUTO USING lemma (TOK_COMMA lemma)*
    |   EAUTO
     */

    private final boolean isEAuto;

    private final Num num;

    private final boolean withStar;

    private final List<Ident> withIdents;

    private final List<Ident> usingLemmas;

    public Auto(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        this.isEAuto = ctx.EAUTO() != null;

        if(ctx.NUM() != null && ctx.NUM().size() == 1) {
            this.num = new Num(parser, ctx.NUM(0));
        } else {
            this.num = null;
        }

        this.withStar = ctx.TOK_MULT() != null;

        this.withIdents = new ArrayList<>();
        if(ctx.ident() != null && ctx.ident().size() > 0) {
            for(CoqParser.IdentContext ident : ctx.ident()) {
                this.withIdents.add(new Ident(parser, ident));
            }
        }

        this.usingLemmas = new ArrayList<>();
        if(ctx.lemma() != null && ctx.lemma().size() > 0) {
            for(CoqParser.LemmaContext lemma : ctx.lemma()) {
                this.usingLemmas.add(new Ident(parser, lemma.ident()));
            }
        }
    }

    protected Auto(Auto copy) {
        super(copy);
        this.isEAuto = copy.isEAuto;
        this.num = copy.num == null ? null : copy.num.clone();
        this.withStar = copy.withStar;
        this.withIdents = new ArrayList<>(copy.withIdents.size());
        for(Ident ident : copy.withIdents) {
            this.withIdents.add(ident.clone());
        }
        this.usingLemmas = new ArrayList<>(copy.usingLemmas.size());
        for(Ident ident : copy.usingLemmas) {
            this.usingLemmas.add(ident.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(num, withIdents, usingLemmas);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        if(isEAuto) {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "eauto"));
        }
        
        Element elem = CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "auto"));
        List<Element> elems = new ArrayList<>();
        
        if(num != null) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeTextNode(doc, num.fullText()));
        }
        
        if(withStar || withIdents.size() > 0) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "with"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            if(withStar) {
                elems.add(CoqDoc.makeKeywordNode(doc, "*"));
            } else {
                if(withIdents.size() > 1) {
                    elems.add(CoqDoc.makeKeywordNode(doc, "("));
                }
                for(int i = 0; i < withIdents.size(); i++) {
                    if(i > 0) {
                        elems.add(CoqDoc.makeWhitespaceNode(doc));
                    }
                    elems.add(CoqDoc.makeIdentifierNode(doc, withIdents.get(i)));
                }
                if(withIdents.size() > 1) {
                    elems.add(CoqDoc.makeKeywordNode(doc, "("));
                }
            }
        }
        
        if(usingLemmas.size() > 0) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "using"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            if(usingLemmas.size() > 1) {
                elems.add(CoqDoc.makeKeywordNode(doc, "("));
            }
            for(int i = 0; i < usingLemmas.size(); i++) {
                if(i > 0) {
                    elems.add(CoqDoc.makeKeywordNode(doc, ","));
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                }
                elems.add(usingLemmas.get(i).makeCoqDocTerm(doc));
            }
            if(usingLemmas.size() > 1) {
                elems.add(CoqDoc.makeKeywordNode(doc, ")"));
            }
        }

        return CoqDoc.mergeTermNodes(elem, elems);
    }

    @Override
    public String getDescription() {
        return "<Auto description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Auto)) return false;

        Auto a = (Auto)anObj;
        return Objects.equals(isEAuto, a.isEAuto) &&
                Objects.equals(num, a.num) &&
                Objects.equals(withStar, a.withStar) &&
                Objects.equals(withIdents, a.withIdents) &&
                Objects.equals(usingLemmas, a.usingLemmas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEAuto, num, withStar, withIdents, usingLemmas);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if(isEAuto) sb.append("E");
        sb.append("Auto");

        if(num != null) sb.append(" num=").append(num);
        if(withStar) sb.append(" with=*");
        if(withIdents.size() > 0) sb.append(" with=").append(withIdents);
        if(usingLemmas.size() > 0) sb.append(" usingLemmas=").append(usingLemmas);

        sb.append("}");
        return sb.toString();
    }

    @Override
    public Auto clone() {
        return new Auto(this);
    }

}
