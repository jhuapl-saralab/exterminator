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

import org.antlr.v4.runtime.tree.TerminalNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.term.Ident;
import edu.jhuapl.exterminator.grammar.coq.term.Num;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Simpl extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.SIMPL() != null;
    }

    /*
    |   SIMPL (IN ident+)?
    |   SIMPL (term|simplIdent=ident) (AT NUM+)? (IN ident+)?
     */

    private final Term term;

    private final Ident ident;

    private final List<Num> atNums;

    private final List<Ident> inIdents;

    public Simpl(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        if(ctx.term() != null && ctx.term().size() == 1) {
            this.term = Term.make(parser, ctx.term(0));
        } else {
            this.term = null;
        }

        if(ctx.simplIdent != null) {
            this.ident = new Ident(parser, ctx.simplIdent);
        } else {
            this.ident = null;
        }

        this.atNums = new ArrayList<>();
        if(ctx.NUM() != null && ctx.NUM().size() > 0) {
            for(TerminalNode num : ctx.NUM()) {
                this.atNums.add(new Num(parser, num));
            }
        }

        this.inIdents = new ArrayList<>();
        int index = ident != null ? 1 : 0;
        if(ctx.ident() != null && ctx.ident().size() > index) {
            for(; index < ctx.ident().size(); index++) {
                this.inIdents.add(new Ident(parser, ctx.ident(index)));
            }
        }
    }

    protected Simpl(Simpl copy) {
        super(copy);
        this.term = copy.term == null ? null : copy.term.clone();
        this.ident = copy.ident == null ? null : copy.ident.clone();
        this.atNums = new ArrayList<>(copy.atNums.size());
        for(Num num : copy.atNums) {
            this.atNums.add(num.clone());
        }
        this.inIdents = new ArrayList<>(copy.inIdents.size());
        for(Ident ident : copy.inIdents) {
            this.inIdents.add(ident.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(term, ident, atNums, inIdents);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc, CoqDoc.makeKeywordNode(doc, "simpl"));
        List<Element> elems = new ArrayList<>();
        
        if(term != null) {
            elems.add(CoqDoc.makeWhitespaceNewlineNode(doc));
            elems.add(CoqDoc.makeParenthesizedTermNode(doc, term));
        } else if(ident != null) {
            elems.add(CoqDoc.makeWhitespaceNewlineNode(doc));
            elems.add(CoqDoc.makeIdentifierNode(doc, ident));
        }
        
        if(atNums.size() > 0) {
            elems.add(CoqDoc.makeWhitespaceNewlineNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "at"));
            for(Num num : atNums) {
                elems.add(CoqDoc.makeWhitespaceNewlineNode(doc));
                elems.add(num.makeCoqDocTerm(doc));
            }
        }
        
        if(inIdents.size() > 0) {
            elems.add(CoqDoc.makeWhitespaceNewlineNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "in"));
            for(Ident ident : inIdents) {
                elems.add(CoqDoc.makeWhitespaceNewlineNode(doc));
                elems.add(CoqDoc.makeIdentifierNode(doc, ident));
            }
        }
        
        if(elems.isEmpty()) {
            return elem;
        } else {
            return CoqDoc.mergeTermNodes(elem, elems);
        }
    }

    @Override
    public String getDescription() {
        return "<Simpl description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Simpl)) return false;

        Simpl s = (Simpl)anObj;
        return Objects.equals(term, s.term) &&
                Objects.equals(ident, s.ident) &&
                Objects.equals(atNums, s.atNums) &&
                Objects.equals(inIdents, s.inIdents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, ident, atNums, inIdents);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{Simpl");
        if(term != null) sb.append(" term=").append(term);
        else if(ident != null) sb.append(" ident=").append(ident);
        if(atNums.size() > 0) sb.append(" atNums=").append(atNums);
        if(inIdents.size() > 0) sb.append(" inIdents=").append(inIdents);
        return sb.toString();
    }

    @Override
    public Simpl clone() {
        return new Simpl(this);
    }

}
