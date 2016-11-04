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
import edu.jhuapl.exterminator.grammar.coq.term.Ident;

public class Subst extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.SUBST() != null;
    }

    /*
|   SUBST ident*
     */

    private final List<Ident> idents;

    public Subst(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        this.idents = new ArrayList<>();
        if(ctx.ident() != null && ctx.ident().size() > 0) {
            for(CoqParser.IdentContext ident : ctx.ident()) {
                this.idents.add(new Ident(parser, ident));
            }
        }
    }

    protected Subst(Subst copy) {
        super(copy);
        this.idents = new ArrayList<>(copy.idents.size());
        for(Ident ident : copy.idents) {
            this.idents.add(ident.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(idents);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc,
                CoqDoc.makeKeywordNode(doc, "subst"));
        if(idents.isEmpty()) {
            return elem;
        }
        List<Element> elems = new ArrayList<>();
        for(Ident ident : idents) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeIdentifierNode(doc, ident));
        }
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    @Override
    public String getDescription() {
        return "<Subst description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Subst)) return false;

        Subst s = (Subst)anObj;
        return Objects.equals(idents, s.idents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idents);
    }

    @Override
    public String toString() {
        return "{Subst idents=" + idents + "}";
    }

    @Override
    public Subst clone() {
        return new Subst(this);
    }

}
