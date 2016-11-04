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
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;
import edu.jhuapl.exterminator.grammar.coq.term.Ident;
import edu.jhuapl.exterminator.grammar.coq.term.Num;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class BindingsList extends CoqToken implements CoqDocable {

    /*
bindings_list: (TOK_LPAREN (ident|NUM) TOK_DEFINE term TOK_RPAREN)+
    | term+
    ;
     */

    private final List<Definition> definitions;

    private final List<Term> terms;

    public BindingsList(CoqFTParser parser, CoqParser.Bindings_listContext ctx) {
        super(parser, ctx);
        this.definitions = new ArrayList<>();
        this.terms = new ArrayList<>();
        if(ctx.TOK_DEFINE() != null && ctx.TOK_DEFINE().size() > 0) {
            for(int i = 0; i < ctx.TOK_DEFINE().size(); i++) {
                this.definitions.add(new Definition(parser, ctx.ident(i),
                        ctx.NUM(i), ctx.term(i)));
            }
        } else {
            for(CoqParser.TermContext term : ctx.term()) {
                this.terms.add(Term.make(parser, term));
            }
        }
    }

    protected BindingsList(BindingsList copy) {
        super(copy);
        this.definitions = new ArrayList<>(copy.definitions.size());
        for(Definition def : copy.definitions) {
            this.definitions.add(def.clone());
        }
        this.terms = new ArrayList<>(copy.terms.size());
        for(Term term : copy.terms) {
            this.terms.add(term.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(definitions, terms);
    }

    @Override
    public boolean isTerminalNode() { return false; }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc);
        List<Element> elems = new ArrayList<>();
        
        if(definitions.size() > 0) {
            for(Definition def : definitions) {
                elems.add(CoqDoc.makeKeywordNode(doc, "("));
                if(def.ident != null) {
                    elems.add(CoqDoc.makeIdentifierNode(doc, def.ident));
                } else {
                    elems.add(CoqDoc.makeTextNode(doc, def.num.fullText()));
                }
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeKeywordNode(doc, ":="));
                elems.add(CoqDoc.makeParenthesizedTermNode(doc, def.term));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeKeywordNode(doc, ")"));
            }
            
        } else {
            elems.add(CoqDoc.makeKeywordNode(doc, "("));
            for(int i = 0; i < terms.size(); i++) {
                if(i > 0) {
                    elems.add(CoqDoc.makeTextNode(doc, ","));
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                }
                elems.add(CoqDoc.makeParenthesizedTermNode(doc, terms.get(i)));
            }
            elems.add(CoqDoc.makeKeywordNode(doc, ")"));
        }
        
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof BindingsList)) return false;

        BindingsList b = (BindingsList)anObj;
        return Objects.equals(definitions, b.definitions) &&
                Objects.equals(terms, b.terms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(definitions, terms);
    }

    @Override
    public String toString() {
        if(definitions.size() > 0) {
            return "{BindingsList " + definitions + "}";
        } else {
            return "{BindingsList " + terms + "}";
        }
    }

    @Override
    public BindingsList clone() {
        return new BindingsList(this);
    }

    ///////////////////////////////////////////////////////////////////////////

    public class Definition implements Cloneable {

        private final Ident ident;

        private final Num num;

        private final Term term;

        public Definition(CoqFTParser parser, CoqParser.IdentContext ident,
                TerminalNode num, CoqParser.TermContext term) {
            this.ident = ident == null ? null : new Ident(parser, ident);
            this.num = num == null ? null : new Num(parser, num);
            this.term = Term.make(parser, term);
        }

        protected Definition(Definition copy) {
            this.ident = copy.ident == null ? null : copy.ident.clone();
            this.num = copy.num == null ? null : copy.num.clone();
            this.term = copy.term.clone();
        }

        public boolean contains(CoqToken token) {
            if(ident != null && (ident.equals(token) || ident.contains(token))) return true;
            if(num != null && (num.equals(token) || num.contains(token))) return true;
            if(term != null && (term.equals(token) || term.contains(token))) return true;

            return false;
        }

        ///////////////////////////////////////////////////////////////////////

        @Override
        public boolean equals(Object anObj) {
            if(anObj == this) return true;
            if(anObj == null || !(anObj instanceof Definition)) return false;

            Definition d = (Definition)anObj;
            return Objects.equals(ident, d.ident) &&
                    Objects.equals(num, d.num) &&
                    Objects.equals(term, d.term);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ident, num, term);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{Definition ");
            if(ident != null) sb.append("ident=").append(ident).append(" ");
            if(num != null) sb.append("num=").append(num).append(" ");
            sb.append("term=").append(term).append("}");
            return sb.toString();
        }

        // (TOK_LPAREN (ident|NUM) TOK_DEFINE term TOK_RPAREN)

        @Override
        public Definition clone() {
            return new Definition(this);
        }

    }

}
