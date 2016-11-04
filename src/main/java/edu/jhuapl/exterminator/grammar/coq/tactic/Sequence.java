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

public class Sequence extends TacticExpr {
    
    /*
expr TOK_SEMICOLON expr
    |   expr TOK_SEMICOLON TOK_LBRACE expr (TOK_PIPE expr)+ TOK_RBRACE
     */
    
    /*
expr : expr TOK_SEMICOLON expr
    |   expr TOK_SEMICOLON TOK_LBRACE expr (TOK_PIPE expr)+ TOK_RBRACE
    |   tacexpr3
    ;
     */

    public static boolean applies(CoqParser.ExprContext ctx) {
        return ctx.tacexpr3() == null && ctx.expr() != null &&
                ((ctx.TOK_LBRACE() == null && ctx.expr().size() == 2) ||
                        (ctx.TOK_LBRACE() != null && ctx.expr().size() >= 2));
    }

    private final List<TacticExpr> exprs;

    private final boolean isGeneral;

    public Sequence(CoqFTParser parser, CoqParser.ExprContext ctx) {
        super(parser, ctx);
        this.exprs = new ArrayList<>();
        for(CoqParser.ExprContext expr : ctx.expr()) {
            this.exprs.add(TacticExpr.make(parser, expr));
        }
        this.isGeneral = ctx.TOK_LBRACE() != null;
    }

    protected Sequence(Sequence copy) {
        super(copy);
        this.exprs = new ArrayList<>(copy.exprs.size());
        for(TacticExpr expr : copy.exprs) {
            this.exprs.add(expr.clone());
        }
        this.isGeneral = copy.isGeneral;
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(exprs);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc,
                exprs.get(0).makeCoqDocTerm(doc),
                CoqDoc.makeWhitespaceNode(doc),
                CoqDoc.makeKeywordNode(doc, ":"),
                CoqDoc.makeWhitespaceNode(doc));
        List<Element> elems = new ArrayList<>();
        if(isGeneral) {
            elems.add(CoqDoc.makeKeywordNode(doc, "{"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(exprs.get(1).makeCoqDocTerm(doc));
            for(int i = 2; i < exprs.size(); i++) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeKeywordNode(doc, "|"));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(exprs.get(i).makeCoqDocTerm(doc));
            }
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "}"));
        } else {
            elems.add(exprs.get(1).makeCoqDocTerm(doc));
        }
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    @Override
    public String getDescription() {
        return "<Sequence description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Sequence)) return false;

        Sequence e = (Sequence)anObj;
        return Objects.equals(exprs, e.exprs) &&
                Objects.equals(isGeneral, e.isGeneral);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exprs, isGeneral);
    }

    @Override
    public String toString() {
        if(isGeneral) {
            StringBuilder sb = new StringBuilder();
            sb.append("{expr=" + exprs.get(0) + "; [");
            for(int i = 1; i < exprs.size(); i++) {
                if(i > 1) sb.append("| ");
                sb.append("expr" + i + "=" + exprs.get(i));
            }
            sb.append("}");
            return sb.toString();
        } else {
            return "{Sequence expr1=" + exprs.get(0) + "; expr2=" + exprs.get(1) + "}";
        }
    }

    @Override
    public Sequence clone() {
        return new Sequence(this);
    }

}
