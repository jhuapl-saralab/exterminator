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
package edu.jhuapl.exterminator.grammar.coq.term.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.term.Arg;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Expression extends Term {

    public static boolean applies(CoqParser.TermContext ctx) {
        return ctx.expression_term != null && ctx.arg() != null && ctx.arg().size() > 0;
    }

    public static boolean isArgChain(CoqParser.TermContext ctx) {
        if(!applies(ctx)) return false;

        if(ctx.arg().size() != 1) return false;

        CoqParser.ArgContext arg = ctx.arg(0);

        if(arg.ident() != null) return false;

        CoqParser.TermContext term = arg.term();

        if(applies(term)) return isArgChain(term);
        else return true;
    }

    public static List<CoqParser.TermContext> getArgChain(CoqParser.TermContext ctx) {
        List<CoqParser.TermContext> terms = new ArrayList<>();
        if(isArgChain(ctx)) {
            terms.add(ctx.expression_term);
            terms.addAll(getArgChain(ctx.arg(0).term()));
        } else {
            terms.add(ctx);
        }
        return terms;
    }

    ///////////////////////////////////////////////////////////////////////////

    protected final String name;

    protected final Term term;

    protected final List<Arg> args;

    public Expression(CoqFTParser parser, CoqParser.TermContext ctx) {
        super(parser, ctx);
        this.term = Term.make(parser, ctx.expression_term);
        this.args = parseArgs(parser, ctx, ctx.arg());

        if(this.term instanceof ID) {
            this.name = ((ID)this.term).getFullName();
        } else {
            this.name = null;
        }
    }

    public Expression(CoqFTParser parser, CoqParser.TermContext termCtx, List<CoqParser.ArgContext> args) {
        super(parser, termCtx);

        this.term = Term.make(parser, termCtx);

        this.args = new ArrayList<>();
        for(CoqParser.ArgContext arg : args) {
            if(isArgChain(arg.term())) {
                for(CoqParser.TermContext arg2 : getArgChain(arg.term())) {
                    appendArg(new Arg(parser, arg2));
                }
            } else {
                appendArg(new Arg(parser, arg));
            }
        }

        if(this.term instanceof ID) {
            this.name = ((ID)this.term).getFullName();
        } else {
            this.name = null;
        }
    }

    protected Expression(Term term) {
        super(term.fullText());

        this.term = term;
        this.args = new ArrayList<>();

        if(this.term instanceof ID) {
            this.name = ((ID)this.term).getFullName();
        } else {
            this.name = null;
        }
    }

    protected Expression(Expression copy) {
        super(copy);
        this.name = copy.name;
        this.term = copy.term.clone();
        this.args = new ArrayList<>(copy.args.size());
        for(Arg arg : copy.args) {
            this.args.add(arg.clone());
        }
    }

    protected static String getFullText(CoqFTParser parser, CoqParser.TermContext termCtx, List<CoqParser.ArgContext> argCtxs) {
        StringBuilder sb = new StringBuilder();
        sb.append(parser.getFullText(termCtx));
        if(argCtxs != null) {
            for(CoqParser.ArgContext arg : argCtxs) {
                sb.append(' ').append(parser.getFullText(arg));
            }
        }
        return sb.toString();
    }

    public void appendArg(Arg arg) {
        args.add(arg);
        fullText.append(" " + arg.fullText());
    }

    private static List<Arg> parseArgs(CoqFTParser parser,
            CoqParser.TermContext ctx, List<CoqParser.ArgContext> ctxs) {
        List<Arg> args = new ArrayList<Arg>();
        if(ctxs == null || ctxs.size() == 0) return args;

        // check special case: if all args are expressions just treat
        // everything as args
        if(ctx != null && isArgChain(ctx)) {
            List<CoqParser.TermContext> terms = getArgChain(ctx);
            for(int i = 1; i < terms.size(); i++) {
                args.add(new Arg(parser, terms.get(i)));
            }
            return args;
        }

        // if we got here, treat it normally
        for(CoqParser.ArgContext arg : ctxs) {
            args.add(new Arg(parser, arg));
        }
        return args;
    }

    public Term getTerm() {
        return term;
    }

    public Arg getArg(int index) {
        return args.get(index);
    }

    public List<Arg> getArgs() {
        return Collections.unmodifiableList(args);
    }

    public boolean isListOfIDs() {
        if(!(term instanceof ID)) return false;
        for(Arg arg : args) {
            if(!(arg.getTerm() instanceof ID)) return false;
        }
        return true;
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(term, args);
    }

    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc, term.makeCoqDocTerm(doc));
        List<Element> argElems = new ArrayList<>();
        for(Arg arg : args) {
            argElems.add(CoqDoc.makeWhitespaceNode(doc));
            Term term = arg.getTerm();
            argElems.add(CoqDoc.makeParenthesizedTermNode(doc, term));
        }
        return CoqDoc.mergeTermNodes(elem, argElems);
    }

    @Override
    public boolean isTerminalNode() { return false; }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        return Objects.hash(name, term, args);
    }

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Expression)) return false;

        Expression e = (Expression)anObj;
        return Objects.equals(name, e.name) &&
                Objects.equals(term, e.term) &&
                Objects.equals(args, e.args);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{Expression");
        if(name != null) sb.append(" name=\"").append(name).append("\"");
        sb.append(" term=").append(term);
        sb.append(" args=").append(args);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Expression clone() {
        return new Expression(this);
    }

}
