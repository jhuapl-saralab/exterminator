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
import edu.jhuapl.exterminator.grammar.coq.term.Qualid;

public class Compute extends TacticExpr {

    /*
COMPUTE (TOK_DASH? qualid+)? (IN ident+)?
     */

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.COMPUTE() != null;
    }

    private final boolean hasDash;

    private final List<Qualid> qualids;

    private final List<Ident> idents;

    public Compute(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);
        this.hasDash = ctx.TOK_DASH() != null && ctx.TOK_DASH().size() > 0;
        this.qualids = new ArrayList<>();
        if(ctx.qualid() != null) {
            for(CoqParser.QualidContext qualid : ctx.qualid()) {
                this.qualids.add(new Qualid(parser, qualid));
            }
        }
        this.idents = new ArrayList<>();
        if(ctx.ident() != null) {
            for(CoqParser.IdentContext ident : ctx.ident()) {
                this.idents.add(new Ident(parser, ident));
            }
        }
    }

    protected Compute(Compute copy) {
        super(copy);
        this.hasDash = copy.hasDash;
        this.qualids = new ArrayList<>(copy.qualids.size());
        for(Qualid qualid : copy.qualids) {
            this.qualids.add(qualid.clone());
        }
        this.idents = new ArrayList<>(copy.idents.size());
        for(Ident ident : copy.idents) {
            this.idents.add(ident.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(qualids, idents);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        /*
        COMPUTE (TOK_DASH? qualid+)? (IN ident+)?
             */
        Element elem = CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "compute"));
        List<Element> elems = new ArrayList<>();
        
        if(hasDash) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "-"));
        }
        
        if(qualids.size() > 0) {
            if(qualids.size() > 1) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeKeywordNode(doc, "("));
            }
            for(int i = 0; i < qualids.size(); i++) {
                if(i > 0) {
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                }
                elems.add(CoqDoc.makeIdentifierNode(doc, qualids.get(i)));
            }
            if(qualids.size() > 1) {
                elems.add(CoqDoc.makeKeywordNode(doc, ")"));
            }
        }
        
        if(idents.size() > 0) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "in"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            if(idents.size() > 1) {
                elems.add(CoqDoc.makeKeywordNode(doc, "("));
            }
            for(int i = 0; i < idents.size(); i++) {
                if(i > 0) {
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                }
                elems.add(CoqDoc.makeIdentifierNode(doc, idents.get(i)));
            }
            if(idents.size() > 1) {
                elems.add(CoqDoc.makeKeywordNode(doc, ")"));
            }
        }
        
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    @Override
    public String getDescription() {
        return "<Compute description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Compute)) return false;

        Compute c = (Compute)anObj;
        return Objects.equals(hasDash, c.hasDash) &&
                Objects.equals(qualids, c.qualids) &&
                Objects.equals(idents, c.idents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasDash, qualids, idents);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{Compute ");
        if(hasDash) sb.append("- ");
        if(qualids.size() > 0) {
            sb.append("qualids=").append(qualids).append(" ");
        }
        if(idents.size() > 0) {
            sb.append("in idents=").append(idents);
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Compute clone() {
        return new Compute(this);
    }

}
