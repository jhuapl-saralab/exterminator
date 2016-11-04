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

import org.antlr.v4.runtime.tree.TerminalNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.term.Ident;
import edu.jhuapl.exterminator.grammar.coq.term.Num;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Generalize extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.GENERALIZE() != null;
    }

    /*
    |   GENERALIZE term (TOK_COMMA term)*
    |   GENERALIZE term atNums1=at_nums (AS ident (TOK_COMMA ident)* atNums2=at_nums AS asIdent=ident)?
    |   GENERALIZE term AS asIdent=ident
    |   GENERALIZE DEPENDENT term
     */

    private final boolean isDependent;

    private final List<Term> terms;

    private final List<Num> atNum1, atNum2;

    private final List<Ident> idents;

    private final Ident asIdent;

    public Generalize(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        this.isDependent = ctx.DEPENDENT() != null;

        this.terms = new ArrayList<>();
        for(CoqParser.TermContext term : ctx.term()) {
            this.terms.add(Term.make(parser, term));
        }

        this.atNum1 = new ArrayList<>();
        if(ctx.atNums1 != null) {
            for(TerminalNode num : ctx.atNums1.NUM()) {
                atNum1.add(new Num(parser, num));
            }
        }

        this.atNum2 = new ArrayList<>();
        if(ctx.atNums2 != null) {
            for(TerminalNode num : ctx.atNums2.NUM()) {
                atNum2.add(new Num(parser, num));
            }
        }

        this.idents = new ArrayList<>();
        for(CoqParser.IdentContext ident : ctx.ident()) {
            if(ident != ctx.asIdent) {
                this.idents.add(new Ident(parser, ident));
            }
        }

        if(ctx.asIdent != null) {
            this.asIdent = new Ident(parser, ctx.asIdent);
        } else {
            this.asIdent = null;
        }
    }

    protected Generalize(Generalize copy) {
        super(copy);
        this.isDependent = copy.isDependent;
        this.terms = new ArrayList<>(copy.terms.size());
        for(Term term : copy.terms) {
            this.terms.add(term.clone());
        }
        this.atNum1 = new ArrayList<>(copy.atNum1.size());
        for(Num num : copy.atNum1) {
            this.atNum1.add(num.clone());
        }
        this.atNum2 = new ArrayList<>(copy.atNum2.size());
        for(Num num : copy.atNum2) {
            this.atNum2.add(num.clone());
        }
        this.idents = new ArrayList<>(copy.idents.size());
        for(Ident ident : copy.idents) {
            this.idents.add(ident.clone());
        }
        this.asIdent = copy.asIdent == null ? null : copy.asIdent.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(terms, atNum1, atNum2, idents, asIdent);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc,
                CoqDoc.makeKeywordNode(doc, "generalize"),
                CoqDoc.makeWhitespaceNode(doc));
        List<Element> elems = new ArrayList<>();
        
        if(isDependent) {
            elems.add(CoqDoc.makeKeywordNode(doc, "dependent"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
        }
        
        elems.add(CoqDoc.makeParenthesizedTermNode(doc, terms.get(0)));
        
        if(atNum1.size() > 0) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "at"));
            for(Num num : atNum1) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeTextNode(doc, num.fullText()));
            }
        }
        
        if(idents.size() > 0) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "as"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            for(int i = 0; i < idents.size(); i++) {
                if(i > 0) {
                    elems.add(CoqDoc.makeKeywordNode(doc, ","));
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                }
                elems.add(CoqDoc.makeIdentifierNode(doc, idents.get(i)));
            }
        }
        
        if(atNum2.size() > 0) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "at"));
            for(Num num : atNum2) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeTextNode(doc, num.fullText()));
            }
        }
        
        if(asIdent != null) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "as"));
            elems.add(CoqDoc.makeIdentifierNode(doc, asIdent));
        }
        
        for(int i = 1; i < terms.size(); i++) {
            elems.add(CoqDoc.makeKeywordNode(doc, ","));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeParenthesizedTermNode(doc, terms.get(i)));
        }
        
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    @Override
    public String getDescription() {
        return "<Generalize description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Generalize)) return false;

        Generalize g = (Generalize)anObj;
        return Objects.equals(isDependent, g.isDependent) &&
                Objects.equals(terms, g.terms) &&
                Objects.equals(atNum1, g.atNum1) &&
                Objects.equals(atNum2, g.atNum2) &&
                Objects.equals(idents, g.idents) &&
                Objects.equals(asIdent, g.asIdent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isDependent, terms, atNum1, atNum2, idents, asIdent);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{Generalize");
        if(isDependent) sb.append(" dependent");

        sb.append(" terms=").append(terms);

        if(atNum1.size() > 0) sb.append(" at=").append(atNum1);

        if(idents.size() > 0) sb.append(" as=").append(idents);

        if(atNum2.size() > 0) sb.append(" at=").append(atNum2);

        if(asIdent != null) sb.append(" as=").append(asIdent);

        sb.append("}");
        return sb.toString();
    }

    @Override
    public Generalize clone() {
        return new Generalize(this);
    }

}
