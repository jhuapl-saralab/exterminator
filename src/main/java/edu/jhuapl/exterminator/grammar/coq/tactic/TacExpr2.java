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

public class TacExpr2 extends TacticExpr {

    public static boolean applies(CoqParser.Tacexpr2Context ctx) {
        return ctx.tacexpr3() != null && ctx.tacexpr1() != null &&
                ctx.TOK_PIPE_PIPE() != null;
    }

    private final TacticExpr expr1, expr3;

    public TacExpr2(CoqFTParser parser, CoqParser.Tacexpr2Context ctx) {
        super(parser, ctx);
        this.expr1 = TacticExpr.make(parser, ctx.tacexpr1());
        this.expr3 = TacticExpr.make(parser, ctx.tacexpr3());
    }

    protected TacExpr2(TacExpr2 copy) {
        super(copy);
        this.expr1 = copy.expr1.clone();
        this.expr3 = copy.expr3.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(expr1, expr3);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        if(expr3 != null) {
            return CoqDoc.makeTermNode(doc,
                    expr1.makeCoqDocTerm(doc),
                    CoqDoc.makeWhitespaceNode(doc),
                    CoqDoc.makeKeywordNode(doc, "||"),
                    CoqDoc.makeWhitespaceNode(doc),
                    expr3.makeCoqDocTerm(doc));
        } else {
            return expr1.makeCoqDocTerm(doc);
        }
    }

    @Override
    public String getDescription() {
        return "This tactic ORs two tactics together.";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof TacExpr2)) return false;

        TacExpr2 t = (TacExpr2)anObj;
        return Objects.equals(expr1, t.expr1) &&
                Objects.equals(expr3, t.expr3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr1, expr3);
    }

    @Override
    public String toString() {
        return "{or tacexpr1=" + expr1 + " tacexpr3=" + expr3 + "}";
    }

    @Override
    public TacExpr2 clone() {
        return new TacExpr2(this);
    }

}
