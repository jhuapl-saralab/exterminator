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
import edu.jhuapl.exterminator.grammar.coq.term.Num;
import edu.jhuapl.exterminator.grammar.coq.term.Qualid;

public class Atom extends TacticExpr {

    public static boolean applies(CoqParser.Tacexpr1Context ctx) {
        return ctx.atom() != null;
    }

    /*
atom : qualid
    |   TOK_LPAREN TOK_RPAREN
    |   NUM
    |   TOK_LPAREN expr TOK_RPAREN
    ;
     */

    private final Qualid qualid;

    private final boolean isParens;

    private final Num num;

    private final TacticExpr expr;

    public Atom(CoqFTParser parser, CoqParser.Tacexpr1Context ctx) {
        this(parser, ctx.atom());
    }

    public Atom(CoqFTParser parser, CoqParser.AtomContext ctx) {
        super(parser, ctx);
        this.qualid = ctx.qualid() == null ? null : new Qualid(parser, ctx.qualid());
        this.isParens = ctx.TOK_LPAREN() != null && ctx.TOK_RPAREN() != null &&
                ctx.expr() == null;
        this.num = ctx.NUM() == null ? null : new Num(parser, ctx.NUM());
        this.expr = ctx.expr() == null ? null : TacticExpr.make(parser, ctx.expr());
    }

    protected Atom(Atom copy) {
        super(copy);
        this.qualid = copy.qualid == null ? null : copy.qualid.clone();
        this.isParens = copy.isParens;
        this.num = copy.num == null ? null : copy.num.clone();
        this.expr = copy.expr == null ? null : copy.expr.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(qualid, num, expr);
    }

    @Override
    public boolean isTerminalNode() {
        return expr == null;
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        /*
        atom : qualid
            |   TOK_LPAREN TOK_RPAREN
            |   NUM
            |   TOK_LPAREN expr TOK_RPAREN
            ;
             */
        if(qualid != null) {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeIdentifierNode(doc, qualid));
        } else if(isParens) {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "()"));
        } else if(num != null) {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeTextNode(doc, num.fullText()));
        } else {
            return CoqDoc.makeTermNode(doc,
                    CoqDoc.makeKeywordNode(doc, "("),
                    expr.makeCoqDocTerm(doc),
                    CoqDoc.makeKeywordNode(doc, ")"));
        }
    }

    @Override
    public String getDescription() {
        return "<Atom description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Atom)) return false;

        Atom a = (Atom)anObj;
        return Objects.equals(qualid, a.qualid) &&
                Objects.equals(isParens, a.isParens) &&
                Objects.equals(num, a.num) &&
                Objects.equals(expr, a.expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualid, isParens, num, expr);
    }

    @Override
    public String toString() {
        if(qualid != null) {
            return "{Atom qualid=" + qualid + "}";
        } else if(isParens) {
            return "{Atom ()}";
        } else if(num != null) {
            return "{Atom num=" + num + "}";
        } else {
            return "{Atom (expr=" + expr + ")}";
        }
    }

    @Override
    public Atom clone() {
        return new Atom(this);
    }

}
