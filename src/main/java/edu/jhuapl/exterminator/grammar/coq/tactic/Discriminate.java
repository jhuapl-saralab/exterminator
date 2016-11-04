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
import edu.jhuapl.exterminator.grammar.coq.term.Num;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Discriminate extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.EDISCRIMINATE() != null || ctx.DISCRIMINATE() != null;
    }
    
    /*
    |   (EDISCRIMINATE|DISCRIMINATE) term (WITH bindings_list)?
    |   (EDISCRIMINATE|DISCRIMINATE) NUM
    |   DISCRIMINATE
     */

    private final boolean isEDiscriminate;

    private final Term term;

    private final Num num;

    private final BindingsList withBindingsList;

    public Discriminate(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        this.isEDiscriminate = ctx.EDISCRIMINATE() != null;

        if(ctx.term() != null && ctx.term().size() == 1) {
            this.term = Term.make(parser, ctx.term(0));
        } else {
            this.term = null;
        }

        if(ctx.NUM() != null && ctx.NUM().size() == 1) {
            this.num = new Num(parser, ctx.NUM(0));
        } else {
            this.num = null;
        }

        if(ctx.bindings_list() != null && ctx.bindings_list().size() == 1) {
            this.withBindingsList = new BindingsList(parser, ctx.bindings_list(0));
        } else {
            this.withBindingsList = null;
        }
    }

    protected Discriminate(Discriminate copy) {
        super(copy);
        this.isEDiscriminate = copy.isEDiscriminate;
        this.term = copy.term == null ? null : copy.term.clone();
        this.num = copy.num == null ? null : copy.num.clone();
        this.withBindingsList = copy.withBindingsList == null ? null : copy.withBindingsList.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(term, num);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc,
                isEDiscriminate ?
                        CoqDoc.makeKeywordNode(doc, "ediscriminate")
                        : CoqDoc.makeKeywordNode(doc, "discriminate"));
        List<Element> elems = new ArrayList<>();
        
        if(term != null) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeParenthesizedTermNode(doc, term));
        }
        
        if(withBindingsList != null) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "with"));
            elems.add(withBindingsList.makeCoqDocTerm(doc));
        }
        
        if(num != null) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeTextNode(doc, num.fullText()));
        }

        return CoqDoc.mergeTermNodes(elem, elems);
    }

    @Override
    public String getDescription() {
        return "<Discriminate description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Discriminate)) return false;

        Discriminate d = (Discriminate)anObj;
        return Objects.equals(isEDiscriminate, d.isEDiscriminate) &&
                Objects.equals(term, d.term) &&
                Objects.equals(num, d.num) &&
                Objects.equals(withBindingsList, d.withBindingsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEDiscriminate, term, num, withBindingsList);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        if(isEDiscriminate) sb.append("E");
        sb.append("Discriminate");

        if(term != null) sb.append(" term=").append(term);
        if(num != null) sb.append(" num=").append(num);
        if(withBindingsList != null) sb.append(" with=").append(withBindingsList);

        sb.append("}");
        return sb.toString();
    }

    @Override
    public Discriminate clone() {
        return new Discriminate(this);
    }

}
