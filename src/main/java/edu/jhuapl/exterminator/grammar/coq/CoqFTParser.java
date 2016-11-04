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
package edu.jhuapl.exterminator.grammar.coq;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import edu.jhuapl.exterminator.grammar.coq.tactic.Tactic;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class CoqFTParser extends CoqParser {

    public static void registerErrorListener(final CoqFTParser parser) {
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                    Object offendingSymbol,
                    int line,
                    int charPositionInLine,
                    String msg,
                    RecognitionException e) {
                throw new CoqSyntaxException(parser,
                        (Token)offendingSymbol, line, charPositionInLine, msg,
                        e);
            }
        });
    }

    private final TokenStream input;

    private final Map<ParserRuleContext, String> fullText;

    public CoqFTParser(Path file) throws IOException {
        this(new CommonTokenStream(new CoqLexer(new ANTLRFileStream(file.toString()))));
    }

    public CoqFTParser(String s) {
        this(new CommonTokenStream(new CoqLexer(new ANTLRInputStream(s))));
    }

    public CoqFTParser(final CommonTokenStream input) {
        super(input);
        this.input = input;
        this.fullText = new HashMap<>();

        addParseListener(new CoqBaseListener() {

            //private Stack<ParserRuleContext> stack = new Stack<>();

            @Override
            public void enterEveryRule(ParserRuleContext ctx) {
                //stack.push(ctx);
                //System.out.println("~a " + stack.size());
                //System.out.println(stack);
                //if(ctx.toString().equals("[523 663 515 358 190 182]")) {
                //	System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
                //	System.out.println(ctx.getStart().getLine() + " " + ctx.getStart().getCharPositionInLine());
                //	System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
                //}
            }

            @Override
            public void exitEveryRule(ParserRuleContext ctx) {
                //ParserRuleContext last = stack.pop();
                //System.out.println(last.equals(ctx));
                //System.out.println("~b " + stack.size());
                updateFullText(ctx);
                //System.out.println("~c"); 
                //System.out.println(stack);
            }
        });

        registerErrorListener(this);
    }

    private void updateFullText(ParserRuleContext ctx) {
        fullText.put(ctx, input.getText(ctx.start, ctx.stop));
    }

    public String getFullText(ParserRuleContext ctx) {
        return fullText.get(ctx);
    }

    public Prog parseProg() {
        return new Prog(this, prog());
    }

    public static Term parseTerm(String s, boolean trySLL) {
        CoqFTParser p = new CoqFTParser(s);
        if(trySLL) {
            p.getInterpreter().setPredictionMode(PredictionMode.SLL);
            p.setErrorHandler(new BailErrorStrategy());
            try {
                return p.parseTerm();
            } catch(ParseCancellationException | CoqSyntaxException e) {
                p = new CoqFTParser(s);
            }
        }
        return p.parseTerm();
    }
    
    public static Tactic parseTactic(String s, boolean trySLL) {
        CoqFTParser p = new CoqFTParser(s);
        if(trySLL) {
            p.getInterpreter().setPredictionMode(PredictionMode.SLL);
            p.setErrorHandler(new BailErrorStrategy());
            try {
                return p.parseTactic();
            } catch(ParseCancellationException | CoqSyntaxException e) {
                p = new CoqFTParser(s);
            }
        }
        return p.parseTactic();
    }

    public Term parseTerm() {
        TermContext ctx = term();
        Term term = Term.make(this, ctx);
        return term;
    }
    
    public Tactic parseTactic() {
        Tactic_invocationContext tactic = tactic_invocation();
        return new Tactic(this, tactic);
    }
}
