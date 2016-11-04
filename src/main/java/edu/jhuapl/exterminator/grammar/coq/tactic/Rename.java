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

public class Rename extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.RENAME() != null;
    }

    /*
    |   RENAME ident INTO ident (TOK_COMMA ident INTO ident)*
     */

    private final List<Ident> idents;

    private final List<Ident> intoIdents;

    public Rename(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        this.idents = new ArrayList<>();
        this.intoIdents = new ArrayList<>();

        for(int i = 0; i < ctx.ident().size() - 1; i += 2) {
            this.idents.add(new Ident(parser, ctx.ident(i)));
            this.intoIdents.add(new Ident(parser, ctx.ident(i + 1)));
        }
    }

    protected Rename(Rename copy) {
        super(copy);
        this.idents = new ArrayList<>(copy.idents.size());
        for(Ident ident : copy.idents) {
            this.idents.add(ident.clone());
        }
        this.intoIdents = new ArrayList<>(copy.intoIdents.size());
        for(Ident ident : copy.intoIdents) {
            this.intoIdents.add(ident.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(idents, intoIdents);
    }
    
    @Override
    public Rename clone() {
        return new Rename(this);
    }
    
    /*
    |   RENAME ident INTO ident (TOK_COMMA ident INTO ident)*
     */

    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc,
                CoqDoc.makeKeywordNode(doc, "rename"),
                CoqDoc.makeWhitespaceNode(doc),
                CoqDoc.makeIdentifierNode(doc, idents.get(0)),
                CoqDoc.makeWhitespaceNode(doc),
                CoqDoc.makeKeywordNode(doc, "into"),
                CoqDoc.makeWhitespaceNode(doc),
                CoqDoc.makeIdentifierNode(doc, intoIdents.get(0)));
        if(idents.size() == 1) {
            return elem;
        }
        
        List<Element> elems = new ArrayList<>();
        for(int i = 1; i < idents.size(); i++) {
            elems.add(CoqDoc.makeKeywordNode(doc, ","));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeIdentifierNode(doc, idents.get(i)));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "into"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeIdentifierNode(doc, intoIdents.get(i)));
        }
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    @Override
    public String getDescription() {
        return "<Rename description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Rename)) return false;

        Rename r = (Rename)anObj;
        return Objects.equals(idents, r.idents) &&
                Objects.equals(intoIdents, r.intoIdents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idents, intoIdents);
    }

    @Override
    public String toString() {
        return "{Rename idents=" + idents + " intoIdents=" + intoIdents + "}";
    }

}
