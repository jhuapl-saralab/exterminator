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

public class Intuition extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.INTUITION() != null;
    }

    private final TacticExpr tactic;

    public Intuition(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);
        this.tactic = ctx.tactic() == null ? null :
            TacticExpr.make(parser, ctx.tactic().expr());
    }

    protected Intuition(Intuition copy) {
        super(copy);
        this.tactic = copy.tactic == null ? null : copy.tactic.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(tactic);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        if(tactic != null) {
            return CoqDoc.makeTermNode(doc,
                    CoqDoc.makeKeywordNode(doc, "intuition"),
                    CoqDoc.makeWhitespaceNode(doc),
                    tactic.makeCoqDocTerm(doc));
        } else {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "intuition"));
        }
    }

    @Override
    public String getDescription() {
        return "<Intuition description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Intuition)) return false;

        Intuition i = (Intuition)anObj;
        return Objects.equals(tactic, i.tactic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tactic);
    }

    @Override
    public String toString() {
        if(tactic == null) {
            return "{Intuition}";
        } else {
            return "{Intuition tactic=" + tactic + "}";
        }
    }

    @Override
    public Intuition clone() {
        return new Intuition(this);
    }

}
