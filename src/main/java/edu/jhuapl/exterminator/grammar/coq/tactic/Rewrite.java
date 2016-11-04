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
import edu.jhuapl.exterminator.grammar.coq.term.Binder;
import edu.jhuapl.exterminator.grammar.coq.term.Num;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Rewrite extends TacticExpr {
    
    /*
    |   REWRITE (TOK_IMPLIES|TOK_LARROW)? rewrite_term (TOK_COMMA (TOK_IMPLIES|TOK_LARROW)? rewrite_term)*
    |   REWRITE (TOK_IMPLIES|TOK_LARROW)? rewrite_term (WITH binder+)? IN clause
    |   REWRITE rewrite_term AT occurrences
    |   REWRITE rewrite_term BY tactic
    |   EREWRITE term
    |   CUTREWRITE TOK_IMPLIES term1=term TOK_EQUAL term2=term
     */

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.REWRITE() != null || ctx.EREWRITE() != null ||
                ctx.CUTREWRITE() != null;
    }

    private final boolean isERewrite;

    private final boolean isCut;

    private final boolean isLeftArrow, isRightArrow;

    private final RewriteTerm term;

    private final RewriteTerm term2;

    private final List<RewriteTerm> terms;

    private final List<Binder> binders;

    private final GoalOccurrences inClause;

    private final Occurrences atOccurrences;

    private final TacticExpr byTactic;

    public Rewrite(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        this.isERewrite = ctx.EREWRITE() != null;
        this.isCut = ctx.CUTREWRITE() != null;

        this.isLeftArrow = ctx.TOK_LARROW() != null && ctx.TOK_LARROW().size() > 0;
        this.isRightArrow = ctx.TOK_IMPLIES() != null && ctx.TOK_IMPLIES().size() > 0;

        if(isERewrite) {
            this.term = new RewriteTerm(parser, ctx.term(0));
            this.term2 = null;
        } else if(isCut) {
            this.term = new RewriteTerm(parser, ctx.term1);
            this.term2 = new RewriteTerm(parser, ctx.term2);
        } else {
            this.term = new RewriteTerm(parser, ctx.rewrite_term(0));
            this.term2 = null;
        }

        this.terms = new ArrayList<>();
        if(ctx.rewrite_term() != null && ctx.rewrite_term().size() > 1) {
            for(int i = 1; i < ctx.rewrite_term().size(); i++) {
                this.terms.add(new RewriteTerm(parser, ctx.rewrite_term(i)));
            }
        }

        this.binders = new ArrayList<>();
        if(ctx.binder() != null && ctx.binder().size() > 0) {
            for(CoqParser.BinderContext binder : ctx.binder()) {
                this.binders.add(new Binder(parser, binder));
            }
        }

        if(ctx.clause() != null) {
            this.inClause = new GoalOccurrences(parser, ctx.clause().goal_occurrences());
        } else {
            this.inClause = null;
        }

        if(ctx.occurrences() != null) {
            this.atOccurrences = new Occurrences(parser, ctx.occurrences());
        } else {
            this.atOccurrences = null;
        }

        if(ctx.tactic() != null) {
            this.byTactic = TacticExpr.make(parser, ctx.tactic().expr());
        } else {
            this.byTactic = null;
        }
    }

    protected Rewrite(Rewrite copy) {
        super(copy);

        this.isERewrite = copy.isERewrite;
        this.isCut = copy.isCut;
        this.isLeftArrow = copy.isLeftArrow;
        this.isRightArrow = copy.isRightArrow;
        this.term = copy.term.clone();
        this.term2 = copy.term2 == null ? null : copy.term2.clone();
        this.terms = new ArrayList<>(copy.terms.size());
        for(RewriteTerm term : copy.terms) {
            this.terms.add(term.clone());
        }
        this.binders = new ArrayList<>(copy.binders.size());
        for(Binder binder : copy.binders) {
            this.binders.add(binder.clone());
        }
        this.inClause = copy.inClause == null ? null : copy.inClause.clone();
        this.atOccurrences = copy.atOccurrences == null ? null : copy.atOccurrences.clone();
        this.byTactic = copy.byTactic == null ? null : copy.byTactic.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(term, term2, terms, binders, inClause, atOccurrences,
                byTactic);
    }

    @Override
    public boolean isTerminalNode() { return false; }
    
    /*
    |   REWRITE (TOK_IMPLIES|TOK_LARROW)? rewrite_term (TOK_COMMA (TOK_IMPLIES|TOK_LARROW)? rewrite_term)*
    |   REWRITE (TOK_IMPLIES|TOK_LARROW)? rewrite_term (WITH binder+)? IN clause
    |   REWRITE rewrite_term AT occurrences
    |   REWRITE rewrite_term BY tactic
    |   EREWRITE term
    |   CUTREWRITE TOK_IMPLIES term1=term TOK_EQUAL term2=term
     */
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        // FIXME real implementation
        return CoqDoc.makeTermNode(doc, CoqDoc.makeTextNode(doc, fullText()));
    }

    @Override
    public String getDescription() {
        return "<Rewrite description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Rewrite)) return false;

        Rewrite r = (Rewrite)anObj;
        return Objects.equals(isERewrite, r.isERewrite) &&
                Objects.equals(isCut, r.isCut) &&
                Objects.equals(isLeftArrow, r.isLeftArrow) &&
                Objects.equals(isRightArrow, r.isRightArrow) &&
                Objects.equals(term, r.term) &&
                Objects.equals(term2, r.term2) &&
                Objects.equals(terms, r.terms) &&
                Objects.equals(binders, r.binders) &&
                Objects.equals(inClause, r.inClause) &&
                Objects.equals(atOccurrences, r.atOccurrences) &&
                Objects.equals(byTactic, r.byTactic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isERewrite, isCut, isLeftArrow, isRightArrow,
                term, term2, terms, binders, inClause, atOccurrences,
                byTactic);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        if(isERewrite) {
            sb.append("ERewrite term=").append(term);

        } else if(isCut) {
            sb.append("CutRewrite term1=").append(term);
            sb.append(" term2=").append(term2);

        } else {
            sb.append("Rewrite");

            if(isLeftArrow) sb.append(" <-");
            else if(isRightArrow) sb.append(" ->");

            sb.append(" term=").append(term);

            if(terms.size() > 0) sb.append(" terms=").append(terms);

            if(binders.size() > 0) sb.append(" binders=").append(binders);

            if(inClause != null) sb.append(" inClause=").append(inClause);
            if(atOccurrences != null) sb.append(" atOccurrences=").append(atOccurrences);
            if(byTactic != null) sb.append(" byTactic=").append(byTactic);
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public Rewrite clone() {
        return new Rewrite(this);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class RewriteTerm extends CoqToken {

        private final boolean isQuestion, isExclamation;

        private final Num num;

        private final Term term;

        public RewriteTerm(CoqFTParser parser, CoqParser.Rewrite_termContext ctx) {
            super(parser, ctx);

            this.isQuestion = ctx.TOK_QUESTION() != null;
            this.isExclamation = ctx.TOK_EXCLAMATION() != null;

            if(ctx.NUM() != null) {
                this.num = new Num(parser, ctx.NUM());
            } else {
                this.num = null;
            }

            this.term = Term.make(parser, ctx.term());
        }

        public RewriteTerm(CoqFTParser parser, CoqParser.TermContext ctx) {
            super(parser, ctx);

            this.isQuestion = false;
            this.isExclamation = false;
            this.num = null;

            this.term = Term.make(parser, ctx);
        }

        protected RewriteTerm(RewriteTerm copy) {
            super(copy);
            this.isQuestion = copy.isQuestion;
            this.isExclamation = copy.isExclamation;
            this.num = copy.num == null ? null : copy.num.clone();
            this.term = copy.term.clone();
        }

        @Override
        public List<CoqToken> getChildren() {
            return makeList(num, term);
        }

        @Override
        public boolean isTerminalNode() { return false; }

        ///////////////////////////////////////////////////////////////////////

        @Override
        public boolean equals(Object anObj) {
            if(anObj == this) return true;
            if(anObj == null || !(anObj instanceof RewriteTerm)) return false;

            RewriteTerm r = (RewriteTerm)anObj;
            return Objects.equals(isQuestion, r.isQuestion) &&
                    Objects.equals(isExclamation, r.isExclamation) &&
                    Objects.equals(num, r.num) &&
                    Objects.equals(term, r.term);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isQuestion, isExclamation, num, term);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{RewriteTerm");
            if(num != null || isQuestion || isExclamation) {
                sb.append(" modifier=");
                if(num != null) sb.append(num);
                if(isQuestion) sb.append("?");
                if(isExclamation) sb.append("!");
            }
            sb.append(" term=").append(term).append("}");
            return sb.toString();
        }

        @Override
        public RewriteTerm clone() {
            return new RewriteTerm(this);
        }
    }

}
