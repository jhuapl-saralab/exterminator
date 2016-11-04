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
package edu.jhuapl.exterminator.grammar.coq.form;

import java.util.Arrays;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;

public abstract class Form extends CoqToken implements CoqDocable {

    protected Form(CoqFTParser parser, CoqParser.FormContext ctx) {
        super(parser, ctx);
    }

    protected Form(Form copy) {
        super(copy);
    }

    @Override
    public abstract Form clone();

    public static Form make(CoqFTParser parser, CoqParser.FormContext ctx) {
        if(ctx.TOK_LPAREN() != null) return make(parser, ctx.inner);

        if(BooleanForm.applies(ctx)) {
            return new BooleanForm(parser, ctx);
        }

        throw new IllegalArgumentException("Unknown form: " +
                ctx.toStringTree(Arrays.asList(CoqParser.ruleNames)));
    }

    /*
form : U_TRUE
    |   U_FALSE
    |   TOK_TILDE form
    |   form TOK_AND form
    |   form TOK_OR form
    |   form TOK_IMPLIES form
    |   form TOK_IFF form
    |   FORALL ident TOK_COLON type TOK_COMMA form
    |   EXISTS ident (TOK_COLON specif)? TOK_COMMA form
    |   EXISTS2 ident (TOK_COLON specif)? TOK_COMMA form TOK_AMP form
    |   term TOK_EQUAL term
    |   term TOK_EQUAL term TOK_COL_GT specif
    |   boolean_term=term
    ;
     */

}
