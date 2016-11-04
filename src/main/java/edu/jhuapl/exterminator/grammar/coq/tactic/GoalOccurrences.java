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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;
import edu.jhuapl.exterminator.grammar.coq.term.Ident;

public class GoalOccurrences extends CoqToken implements CoqDocable {

    /*
goal_occurrences : ident at_occurrences? (TOK_COMMA ident at_occurrences?)* (TOK_SELECT (selectStar=TOK_MULT selectOccurrences=at_occurrences?)?)?
    |   star=TOK_MULT (TOK_SELECT (selectStar=TOK_MULT selectOccurrences=at_occurrences?)?)?
    ;
     */

    private final List<Ident> idents;

    private final Map<Ident, Occurrences> atOccurrences;

    private final boolean isStar, hasSelect, hasSelectStar;

    private final Occurrences selectAtOccurrences;

    public GoalOccurrences(CoqFTParser parser, CoqParser.Goal_occurrencesContext ctx) {
        super(parser, ctx);

        this.idents = new ArrayList<>();
        this.atOccurrences = new HashMap<>();
        for(int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if(child instanceof CoqParser.IdentContext) {
                idents.add(new Ident(parser, (CoqParser.IdentContext)child));
            } else if(child instanceof CoqParser.At_occurrencesContext && child != ctx.selectOccurrences) {
                Occurrences o = new Occurrences(parser, ((CoqParser.At_occurrencesContext)child).occurrences());
                atOccurrences.put(idents.get(idents.size() - 1), o);
            }
        }

        this.isStar = ctx.star != null;
        this.hasSelect = ctx.TOK_SELECT() != null;
        this.hasSelectStar = ctx.selectStar != null;

        if(ctx.selectOccurrences != null) {
            this.selectAtOccurrences = new Occurrences(parser, ctx.selectOccurrences.occurrences());
        } else {
            this.selectAtOccurrences = null;
        }
    }

    protected GoalOccurrences(GoalOccurrences copy) {
        super(copy);
        this.idents = new ArrayList<>(copy.idents.size());
        for(Ident ident : copy.idents) {
            this.idents.add(ident.clone());
        }
        this.atOccurrences = new HashMap<>();
        for(Map.Entry<Ident, Occurrences> entry : copy.atOccurrences.entrySet()) {
            this.atOccurrences.put(entry.getKey().clone(), entry.getValue().clone());
        }
        this.isStar = copy.isStar;
        this.hasSelect = copy.hasSelect;
        this.hasSelectStar = copy.hasSelectStar;
        this.selectAtOccurrences = copy.selectAtOccurrences == null ? null : copy.selectAtOccurrences.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(idents, atOccurrences.keySet(), atOccurrences.values(),
                selectAtOccurrences);
    }

    @Override
    public boolean isTerminalNode() { return false; }
    
    /*
goal_occurrences : ident at_occurrences? (TOK_COMMA ident at_occurrences?)* (TOK_SELECT (selectStar=TOK_MULT selectOccurrences=at_occurrences?)?)?
    |   star=TOK_MULT (TOK_SELECT (selectStar=TOK_MULT selectOccurrences=at_occurrences?)?)?
    ;
     */
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc);
        List<Element> elems = new ArrayList<>();
        
        if(isStar) {
            elems.add(CoqDoc.makeKeywordNode(doc, "*"));
        } else {
            boolean first = true;
            for(Ident ident : idents) {
                if(!first) {
                    elems.add(CoqDoc.makeKeywordNode(doc, ","));
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                }
                first = false;
                
                elems.add(CoqDoc.makeIdentifierNode(doc, ident));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(atOccurrences.get(ident).makeCoqDocTerm(doc));
            }
        }
        
        if(hasSelect) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "|-"));
            if(hasSelectStar) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeKeywordNode(doc, "*"));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(selectAtOccurrences.makeCoqDocTerm(doc));
            }
        }
        
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof GoalOccurrences)) return false;

        GoalOccurrences g = (GoalOccurrences)anObj;
        return Objects.equals(isStar, g.isStar) &&
                Objects.equals(idents, g.idents) &&
                Objects.equals(atOccurrences, g.atOccurrences) &&
                Objects.equals(hasSelect, g.hasSelect) &&
                Objects.equals(hasSelectStar, g.hasSelectStar) &&
                Objects.equals(selectAtOccurrences, g.selectAtOccurrences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isStar, idents, atOccurrences, hasSelect,
                hasSelectStar, selectAtOccurrences);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{GoalOccurrences");

        if(isStar) {
            sb.append(" *");

        } else {
            for(int i = 0; i < idents.size(); i++) {
                sb.append(" ident");
                if(idents.size() > 1) sb.append(i + 1);
                sb.append("=").append(idents.get(i));

                if(atOccurrences.containsKey(idents.get(i))) {
                    sb.append(" at");
                    if(idents.size() > 1) sb.append(i + 1);
                    sb.append("=").append(atOccurrences.get(idents.get(i)));
                }
            }
        }

        if(hasSelect) sb.append(" |-");
        if(hasSelectStar) sb.append(" *");
        if(selectAtOccurrences != null) sb.append(" at=").append(selectAtOccurrences);

        sb.append("}");
        return sb.toString();
    }

    @Override
    public GoalOccurrences clone() {
        return new GoalOccurrences(this);
    }
}
