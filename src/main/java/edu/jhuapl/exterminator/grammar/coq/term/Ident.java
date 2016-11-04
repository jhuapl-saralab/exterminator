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

public class Ident extends CoqToken implements ID {

    private final boolean isCIdent;

    private final String ident;

    //	public Ident(String ident) {
    //		this.ident = ident;
    //	}
    //	
    //	public Ident(TerminalNode node) {
    //		this.ident = node.getText();
    //	}
    //	
    //	public Ident(Token token) {
    //		this.ident = token.getText();
    //	}

    public Ident(CoqFTParser parser, CoqParser.IdentContext ctx) {
        super(parser, ctx);
        this.isCIdent = ctx.c_ident() != null;
        this.ident = ctx.getText().trim();
    }

    protected Ident(Ident copy) {
        super(copy);
        this.isCIdent = copy.isCIdent;
        this.ident = copy.ident;
    }

    ///////////////////////////////////////////////////////////////////////////

    public boolean isCIdent() { return isCIdent; }

    @Override
    public String getFullName() {
        return ident;
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList();
    }

    @Override
    public CoqToken asToken() { return this; }

    public Element makeCoqDocTerm(Document doc) {
        if(isCIdent) {
            return CoqDoc.makeIdentifierNode(doc, this);
        } else {
            return CoqDoc.makeKeywordNode(doc, getFullName());
        }
    }

    @Override
    public boolean isTerminalNode() { return true; }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null) return false;

        if(anObj instanceof Ident) {
            Ident i = (Ident)anObj;
            return Objects.equals(ident, i.ident);
        } else if(anObj instanceof Qualid) {
            Qualid q = (Qualid)anObj;
            if(q.getFirst() != null) return false;

            Ident i = q.getIdent();
            return Objects.equals(ident, i.ident);
        } else if(anObj instanceof Name) {
            Name n = (Name)anObj;
            if(n.isUnderscore()) {
                return ident.equals("_");
            } else {
                Ident i = n.getIdent();
                return Objects.equals(ident, i.ident);
            }
        } else if(anObj instanceof ID) {
            throw new UnsupportedOperationException("This needs to be implemented.");
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident);
    }

    @Override
    public String toString() {
        return "{Ident=\"" + ident + "\"}";
    }

    @Override
    public Ident clone() {
        return new Ident(this);
    }

}
