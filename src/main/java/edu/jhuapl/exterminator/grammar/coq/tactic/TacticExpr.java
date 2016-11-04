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

import java.util.Arrays;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;

public abstract class TacticExpr extends CoqToken implements CoqDocable {

    protected TacticExpr(CoqFTParser parser, ParserRuleContext context) {
        super(parser, context);
    }

    protected TacticExpr(TacticExpr copy) {
        super(copy);
    }

    @Override
    public boolean isTerminalNode() { return false; }

    @Override
    public abstract TacticExpr clone();
    
    public abstract String getDescription();

    public static TacticExpr make(CoqFTParser parser, CoqParser.ExprContext ctx) {

        if(Sequence.applies(ctx)) {
            return new Sequence(parser, ctx);
        } else if(ctx.tacexpr3() != null) {
            return make(parser, ctx.tacexpr3());
        }

        throw new IllegalArgumentException("Unknown tactic expr: " +
                ctx.toStringTree(Arrays.asList(CoqParser.ruleNames)));
    }

    public static TacticExpr make(CoqFTParser parser, CoqParser.Tacexpr3Context ctx) {

        if(TacExpr3.applies(ctx)) {
            return new TacExpr3(parser, ctx);
        } else if(ctx.tacexpr2() != null) {
            return make(parser, ctx.tacexpr2());
        }

        throw new IllegalArgumentException("Unknown tactic expr3: " +
                ctx.toStringTree(Arrays.asList(CoqParser.ruleNames)));
    }

    public static TacticExpr make(CoqFTParser parser, CoqParser.Tacexpr2Context ctx) {

        if(TacExpr2.applies(ctx)) {
            return new TacExpr2(parser, ctx);
        } else if(ctx.tacexpr1() != null) {
            return make(parser, ctx.tacexpr1());
        }

        throw new IllegalArgumentException("Unknown tactic expr2: " +
                ctx.toStringTree(Arrays.asList(CoqParser.ruleNames)));
    }

    public static TacticExpr make(CoqFTParser parser, CoqParser.Tacexpr1Context ctx) {

        if(ctx.atomic_tactic() != null) {
            return make(parser, ctx.atomic_tactic());
        } else if(Function.applies(ctx)) {
            return new Function(parser, ctx);
        } else if(Atom.applies(ctx)) {
            return new Atom(parser, ctx);
        }

        throw new IllegalArgumentException("Unknown tactic expr1: " +
                ctx.toStringTree(Arrays.asList(CoqParser.ruleNames)));
    }

    public static TacticExpr make(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {

        if(Exact.applies(ctx)) {
            return new Exact(parser, ctx);
        } else if(Assumption.applies(ctx)) {
            return new Assumption(parser, ctx);
        } else if(Apply.applies(ctx)) {
            return new Apply(parser, ctx);
        } else if(Constructor.applies(ctx)) {
            return new Constructor(parser, ctx);
        } else if(Intro.applies(ctx)) {
            return new Intro(parser, ctx);
        } else if(Clear.applies(ctx)) {
            return new Clear(parser, ctx);
        } else if(Rename.applies(ctx)) {
            return new Rename(parser, ctx);
        } else if(Assert.applies(ctx)) {
            return new Assert(parser, ctx);
        } else if(Generalize.applies(ctx)) {
            return new Generalize(parser, ctx);
        } else if(Admit.applies(ctx)) {
            return new Admit(parser, ctx);
        } else if(Contradiction.applies(ctx)) {
            return new Contradiction(parser, ctx);
        } else if(Destruct.applies(ctx)) {
            return new Destruct(parser, ctx);
        } else if(Discriminate.applies(ctx)) {
            return new Discriminate(parser, ctx);
        } else if(Injection.applies(ctx)) {
            return new Injection(parser, ctx);
        } else if(Inversion.applies(ctx)) {
            return new Inversion(parser, ctx);
        } else if(Rewrite.applies(ctx)) {
            return new Rewrite(parser, ctx);
        } else if(Reflexivity.applies(ctx)) {
            return new Reflexivity(parser, ctx);
        } else if(Subst.applies(ctx)) {
            return new Subst(parser, ctx);
        } else if(Compute.applies(ctx)) {
            return new Compute(parser, ctx);
        } else if(Simpl.applies(ctx)) {
            return new Simpl(parser, ctx);
        } else if(Unfold.applies(ctx)) {
            return new Unfold(parser, ctx);
        } else if(Auto.applies(ctx)) {
            return new Auto(parser, ctx);
        } else if(Intuition.applies(ctx)) {
            return new Intuition(parser, ctx);
        } else {
//            throw new IllegalArgumentException("Unknown atomic tactic: " +
//                    ctx.toStringTree(Arrays.asList(CoqParser.ruleNames)));
            return new UnknownTactic(parser, ctx);
        }
    }

}
