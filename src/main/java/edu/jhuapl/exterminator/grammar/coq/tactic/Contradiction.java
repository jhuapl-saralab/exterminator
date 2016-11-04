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
import edu.jhuapl.exterminator.grammar.coq.term.Ident;

public class Contradiction extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.CONTRADICTION() != null;
    }

    /*
    |   CONTRADICTION ident?
     */

    private final Ident ident;

    public Contradiction(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        if(ctx.ident() != null && ctx.ident().size() == 1) {
            this.ident = new Ident(parser, ctx.ident(0));
        } else {
            this.ident = null;
        }
    }

    protected Contradiction(Contradiction copy) {
        super(copy);
        this.ident = copy.ident == null ? null : copy.ident.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(ident);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        if(ident != null) {
            return CoqDoc.makeTermNode(doc,
                    CoqDoc.makeKeywordNode(doc, "contradiction"),
                    CoqDoc.makeWhitespaceNode(doc),
                    CoqDoc.makeIdentifierNode(doc, ident));
        } else {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "contradiction"));
        }
    }

    @Override
    public String getDescription() {
        return "<Contradiction description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Contradiction)) return false;

        Contradiction c = (Contradiction)anObj;
        return Objects.equals(ident, c.ident);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident);
    }

    @Override
    public String toString() {
        if(ident == null) return "{Contradiction}";
        else return "{Contradiction ident=" + ident + "}";
    }

    @Override
    public Contradiction clone() {
        return new Contradiction(this);
    }

}
