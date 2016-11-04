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

public class StoreBound extends Term {

    public static boolean applies(CoqParser.TermContext ctx) {
        return ctx.term_storebound() != null;
    }

    /*
qualid TOK_STORE_BOUND (NUM | term)
     */

    private final Qualid left;

    private final Term right;

    public StoreBound(CoqFTParser parser, CoqParser.TermContext ctx) {
        this(parser, ctx.term_storebound());
    }

    public StoreBound(CoqFTParser parser, CoqParser.Term_storeboundContext ctx) {
        super(parser, ctx);

        this.left = new Qualid(parser, ctx.qualid(0));
        if(ctx.NUM() != null) {
            this.right = new Num(parser, ctx.NUM());
        } else if(ctx.qualid().size() == 2) {
            this.right = new Qualid(parser, ctx.qualid(1));
        } else if(ctx.term_some() != null) {
            this.right = new Some(parser, ctx.term_some());
        } else {
            this.right = Term.make(parser, ctx.term());
        }
    }

    protected StoreBound(StoreBound copy) {
        super(copy);
        this.left = copy.left.clone();
        this.right = copy.right.clone();
    }

    public Qualid getLeft() { return left; }

    public Term getRight() { return right; }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(left, right);
    }

    @Override
    public boolean isTerminalNode() { return false; }

    @Override
    public Element makeCoqDocTerm(Document doc) {
        return CoqDoc.makeTermNode(doc, left.makeCoqDocTerm(doc),
                CoqDoc.makeWhitespaceNode(doc),
                CoqDoc.makeKeywordNode(doc, "≐"),
                CoqDoc.makeWhitespaceNode(doc),
                right.makeCoqDocTerm(doc));
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof StoreBound)) return false;

        StoreBound s = (StoreBound)anObj;
        return Objects.equals(left, s.left) &&
                Objects.equals(right, s.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{StoreBound left=").append(left);
        sb.append(" right=").append(right).append("}");
        return sb.toString();
    }

    @Override
    public StoreBound clone() {
        return new StoreBound(this);
    }

}
