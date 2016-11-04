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

import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;

public class Qualid extends Term implements ID {

    public static boolean applies(CoqParser.TermContext ctx) {
        return ctx.qualid() != null /*&&
				ctx.TOK_STORE_BOUND() == null*/;
    }

    public static String getID(CoqParser.TermContext ctx) {
        return ctx.qualid().getText();
    }

    private final Qualid first;

    private final Ident ident;

    public Qualid(CoqFTParser parser, CoqParser.TermContext ctx) {
        this(parser, ctx.qualid());
    }

    public Qualid(CoqFTParser parser, CoqParser.QualidContext ctx) {
        super(parser, ctx);
        if(ctx.qualid() != null) {
            this.first = new Qualid(parser, ctx.qualid());
        } else {
            this.first = null;
        }

        this.ident = new Ident(parser, ctx.ident());
    }

    protected Qualid(Qualid copy) {
        super(copy);
        this.first = copy.first == null ? null : copy.first.clone();
        this.ident = copy.ident.clone();
    }

    public Qualid getFirst() { return first; }

    public Ident getIdent() { return ident; }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public String getFullName() {
        if(first != null) {
            return first.getFullName() + "." + ident.getFullName();
        } else {
            return ident.getFullName();
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(first, ident);
    }

    @Override
    public CoqToken asToken() { return this; }

    @Override
    public Element makeCoqDocTerm(Document doc) {
        if(first == null) {
            return ident.makeCoqDocTerm(doc);
        } else {
            return CoqDoc.makeTermNode(doc, CoqDoc.makeIdentifierNode(doc, this));
        }
    }

    @Override
    public boolean isTerminalNode() { return true; }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        return Objects.hash(first, ident);
    }

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null) return false;

        if(anObj instanceof Qualid) {
            Qualid q = (Qualid)anObj;
            return Objects.equals(first, q.first) &&
                    Objects.equals(ident, q.ident);
        } else if(anObj instanceof Ident) {
            if(first != null) return false;

            Ident i = (Ident)anObj;
            return Objects.equals(ident, i);
        } else if(anObj instanceof Name) {
            if(first != null) return false;

            Name n = (Name)anObj;
            if(n.isUnderscore()) return ident.getFullName().equals("_");

            return Objects.equals(ident, n.getIdent());
        } else if(anObj instanceof ID) {
            throw new UnsupportedOperationException("This needs to be implemented.");
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "{Qualid=\"" + getFullName() + "\"}";
    }

    @Override
    public Qualid clone() {
        return new Qualid(this);
    }

}
