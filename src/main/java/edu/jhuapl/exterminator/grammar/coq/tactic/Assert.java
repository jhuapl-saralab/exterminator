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
import edu.jhuapl.exterminator.grammar.coq.form.Form;
import edu.jhuapl.exterminator.grammar.coq.term.Ident;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Assert extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.ASSERT() != null;
    }

    /*
    |   ASSERT TOK_LPAREN ident TOK_COLON form TOK_RPAREN
    |   ASSERT form (BY tactic)?
    |   ASSERT TOK_LPAREN ident TOK_DEFINE term TOK_RPAREN
    |   ASSERT form AS intro_pattern (BY tactic)?
     */

    private final Ident ident;

    private final Form form;

    private final TacticExpr tactic;

    private final Term term;

    private final IntroPattern asPattern;

    public Assert(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        if(ctx.ident() != null && ctx.ident().size() == 1) {
            this.ident = new Ident(parser, ctx.ident(0));
        } else {
            this.ident = null;
        }

        if(ctx.form() != null) {
            this.form = Form.make(parser, ctx.form());
        } else {
            this.form = null;
        }

        if(ctx.tactic() != null) {
            this.tactic = TacticExpr.make(parser, ctx.tactic().expr());
        } else {
            this.tactic = null;
        }

        if(ctx.term() != null && ctx.term().size() == 1) {
            this.term = Term.make(parser, ctx.term(0));
        } else {
            this.term = null;
        }

        if(ctx.intro_pattern() != null && ctx.intro_pattern().size() == 1) {
            this.asPattern = new IntroPattern(parser, ctx.intro_pattern(0));
        } else {
            this.asPattern = null;
        }
    }

    protected Assert(Assert copy) {
        super(copy);
        this.ident = copy.ident == null ? null : copy.ident.clone();
        this.form = copy.form == null ? null : copy.form.clone();
        this.tactic = copy.tactic == null ? null : copy.tactic.clone();
        this.term = copy.term == null ? null : copy.term.clone();
        this.asPattern = copy.asPattern == null ? null : copy.asPattern.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(ident, form, tactic, term, asPattern);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc,
                CoqDoc.makeKeywordNode(doc, "assert"),
                CoqDoc.makeWhitespaceNode(doc));
        List<Element> elems = new ArrayList<>();
        
        if(ident != null) {
            elems.add(CoqDoc.makeKeywordNode(doc, "("));
            elems.add(CoqDoc.makeIdentifierNode(doc, ident));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            if(form != null) {
                elems.add(CoqDoc.makeKeywordNode(doc, ":"));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(form.makeCoqDocTerm(doc));
            } else {
                elems.add(CoqDoc.makeKeywordNode(doc, ":="));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeParenthesizedTermNode(doc, term));
            }
            elems.add(CoqDoc.makeKeywordNode(doc, ")"));
            
        } else {
            elems.add(form.makeCoqDocTerm(doc));
            
            if(asPattern != null) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeKeywordNode(doc, "as"));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(asPattern.makeCoqDocTerm(doc));
            }
            
            if(tactic != null) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeKeywordNode(doc, "by"));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(tactic.makeCoqDocTerm(doc));
            }
        }

        return CoqDoc.mergeTermNodes(elem, elems);
    }

    @Override
    public String getDescription() {
        return "<Assert description>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Assert)) return false;

        Assert a = (Assert)anObj;
        return Objects.equals(ident, a.ident) &&
                Objects.equals(form, a.form) &&
                Objects.equals(tactic, a.tactic) &&
                Objects.equals(term, a.term) &&
                Objects.equals(asPattern, a.asPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident, form, tactic, term, asPattern);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{Assert");

        if(ident != null) {
            sb.append(" ident=").append(ident);
            if(form != null) {
                sb.append(" : form=").append(form);
            } else {
                sb.append(" := term=").append(term);
            }

        } else {
            sb.append(" form=").append(form);
            if(asPattern != null) sb.append(" as=").append(asPattern);
            if(tactic != null) sb.append(" by=").append(tactic);
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public Assert clone() {
        return new Assert(this);
    }

}
