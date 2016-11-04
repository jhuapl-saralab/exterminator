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
package edu.jhuapl.exterminator.grammar.coq.term;

import java.util.Arrays;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;
import edu.jhuapl.exterminator.grammar.coq.term.expression.BooleanExpression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.Expression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.IfElseExpression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.WhileExpression;

public abstract class Term extends CoqToken implements CoqDocable {

    protected Term(CoqFTParser parser, ParserRuleContext context) {
        super(parser, context);
    }

    protected Term(Token node) {
        super(node);
    }

    protected Term(TerminalNode node) {
        super(node);
    }

    protected Term(String fullText) {
        // only use this if you know what you're doing
        super(fullText);
    }

    protected Term(Term copy) {
        super(copy);
    }

    @Override
    public abstract Term clone();

    public static boolean hasInner(CoqParser.TermContext ctx) {
        return ctx.inner != null;
    }

    public static CoqParser.TermContext getInner(CoqParser.TermContext ctx) {
        return ctx.inner;
    }

    public static Term make(CoqFTParser parser, CoqParser.TermContext ctx) {
        if(hasInner(ctx))
            return make(parser, getInner(ctx));

        if(Some.applies(ctx))
            return new Some(parser, ctx);
        if(None.applies(ctx))
            return None.singleton(parser, ctx);
        if(NamedFields.applies(ctx))
            return new NamedFields(parser, ctx);
        if(Forall.applies(ctx))
            return new Forall(parser, ctx);
        if(Fun.applies(ctx))
            return new Fun(parser, ctx);
        if(TypeCast.applies(ctx))
            return new TypeCast(parser, ctx);
        if(Local.applies(ctx))
            return new Local(parser, ctx);
        if(BooleanExpression.Factory.applies(ctx))
            return BooleanExpression.Factory.make(parser, ctx).asTerm();
        if(IfElseExpression.applies(ctx))
            return new IfElseExpression(parser, ctx);
        if(WhileExpression.applies(ctx))
            return new WhileExpression(parser, ctx);
        if(Expression.applies(ctx)) {
            Expression e = new Expression(parser, ctx);
            BooleanExpression be = BooleanExpression.Factory.convert(e);
            if(be != null) return be.asTerm();
            else return e;
        }
        if(Refterm.applies(ctx))
            return new Refterm(parser, ctx);
        if(Deref.applies(ctx))
            return new Deref(parser, ctx);
        if(Num.applies(ctx))
            return new Num(parser, ctx);
        if(Tuple.applies(ctx))
            return new Tuple(parser, ctx);
        if(Completes.applies(ctx))
            return new Completes(parser, ctx);
        if(Statements.applies(ctx))
            return new Statements(parser, ctx);
        if(SepConjunction.applies(ctx))
            return new SepConjunction(parser, ctx);
        if(StoreBound.applies(ctx))
            return new StoreBound(parser, ctx);
        if(Qualid.applies(ctx))
            return new Qualid(parser, ctx);

        throw new IllegalArgumentException("Unknown term type:\n" +
                ctx.toStringTree(Arrays.asList(CoqParser.ruleNames)) + "\n" +
                parser.getFullText(ctx));
    }

}
