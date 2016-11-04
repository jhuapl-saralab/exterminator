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
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;

public class UnknownTactic extends TacticExpr {

    private final CoqParser.Atomic_tacticContext ctx;

    public UnknownTactic(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);
        this.ctx = ctx;
    }

    protected UnknownTactic(UnknownTactic copy) {
        super(copy);
        this.ctx = copy.ctx;
    }

    @Override
    public List<CoqToken> getChildren() {
        // need to implement the correct tactic
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        return CoqDoc.makeTermNode(doc, CoqDoc.makeTextNode(doc, fullText()));
    }
    
    @Override
    public String getDescription() {
        return "This tactic is currently unknown to Exterminator.";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof UnknownTactic)) return false;

        UnknownTactic u = (UnknownTactic)anObj;
        return Objects.equals(ctx, u.ctx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ctx);
    }

    @Override
    public String toString() {
        return "{UnknownTactic ctx=" + ctx.toStringTree(Arrays.asList(CoqParser.ruleNames)) + "}";
    }

    @Override
    public UnknownTactic clone() {
        return new UnknownTactic(this);
    }

}
