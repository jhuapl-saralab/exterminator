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
import java.util.Collections;
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
import edu.jhuapl.exterminator.grammar.coq.term.Ident;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Apply extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.APPLY() != null || ctx.EAPPLY() != null || ctx.LAPPLY() != null;
    }

    /*
    |   APPLY term (TOK_COMMA term)* (IN inIdent=ident)?
    |   EAPPLY term
    |   SIMPLE APPLY term
    |   SIMPLE? (APPLY|EAPPLY) term (WITH bindings_list)? (TOK_COMMA term (WITH bindings_list)?)*
    |   LAPPLY term
    |   (APPLY|EAPPLY) term WITH bindings_list (TOK_COMMA term WITH bindings_list)* IN inIdent=ident (AS disj_conj_intro_pattern)?
    |   SIMPLE APPLY term IN inIdent=ident
     */

    private final boolean isEApply, isLApply, isSimple;

    private final List<Term> terms;

    private final Map<Term, BindingsList> withBindingsLists;

    private final Ident inIdent;

    private final DisjConjIntroPattern asPattern;

    public Apply(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        this.isEApply = ctx.EAPPLY() != null;
        this.isLApply = ctx.LAPPLY() != null;
        this.isSimple = ctx.SIMPLE() != null;

        this.terms = new ArrayList<>();
        this.withBindingsLists = new HashMap<>();
        for(int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if(child instanceof CoqParser.TermContext) {
                terms.add(Term.make(parser, (CoqParser.TermContext)child));
            } else if(child instanceof CoqParser.Bindings_listContext) {
                BindingsList bindings = new BindingsList(parser, (CoqParser.Bindings_listContext)child);
                withBindingsLists.put(terms.get(terms.size() - 1), bindings);
            }
        }

        if(ctx.inIdent != null) {
            this.inIdent = new Ident(parser, ctx.inIdent);
        } else {
            this.inIdent = null;
        }

        if(ctx.disj_conj_intro_pattern() != null) {
            this.asPattern = new DisjConjIntroPattern(parser, ctx.disj_conj_intro_pattern());
        } else {
            this.asPattern = null;
        }
    }

    protected Apply(Apply copy) {
        super(copy);
        this.isEApply = copy.isEApply;
        this.isLApply = copy.isLApply;
        this.isSimple = copy.isSimple;
        this.terms = new ArrayList<>(copy.terms.size());
        for(Term term : copy.terms) {
            this.terms.add(term.clone());
        }
        this.withBindingsLists = new HashMap<>();
        for(Map.Entry<Term, BindingsList> entry : copy.withBindingsLists.entrySet()) {
            this.withBindingsLists.put(entry.getKey().clone(), entry.getValue().clone());
        }
        this.inIdent = copy.inIdent == null ? null : copy.inIdent.clone();
        this.asPattern = copy.asPattern == null ? null : copy.asPattern.clone();
    }

    public List<Term> getTerms() {
        return Collections.unmodifiableList(terms);
    }

    public Map<Term, BindingsList> getWithBindingsLists() {
        return Collections.unmodifiableMap(withBindingsLists);
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(terms, withBindingsLists.keySet(),
                withBindingsLists.values(), inIdent, asPattern);
    }

    @Override
    public boolean contains(CoqToken token) {
        Objects.requireNonNull(token);

        for(Term term : terms) {
            if(term.equals(token) || term.contains(token)) return true;
        }

        for(Map.Entry<Term, BindingsList> entry : withBindingsLists.entrySet()) {
            if(entry.getKey().equals(token) || entry.getKey().contains(token)) return true;
            if(entry.getValue().equals(token) || entry.getValue().contains(token)) return true;
        }

        if(inIdent != null && (inIdent.equals(token) || inIdent.contains(token))) return true;
        if(asPattern != null && (asPattern.equals(token) || asPattern.contains(token))) return true;

        return false;
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc);
        List<Element> elems = new ArrayList<>();
        
        if(isSimple) {
            elems.add(CoqDoc.makeKeywordNode(doc, "simple"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
        }
        
        if(isEApply) {
            elems.add(CoqDoc.makeKeywordNode(doc, "eapply"));
        } else if(isLApply) {
            elems.add(CoqDoc.makeKeywordNode(doc, "lapply"));
        } else {
            elems.add(CoqDoc.makeKeywordNode(doc, "apply"));
        }
        elems.add(CoqDoc.makeWhitespaceNode(doc));
        
        for(int i = 0; i < terms.size(); i++) {
            if(i > 0) {
                elems.add(CoqDoc.makeTextNode(doc, ","));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
            }
            Term term = terms.get(i);
            elems.add(CoqDoc.makeParenthesizedTermNode(doc, term));
            
            if(withBindingsLists.containsKey(term)) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeKeywordNode(doc, "with"));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(withBindingsLists.get(term).makeCoqDocTerm(doc));
            }
        }
        
        if(inIdent != null) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "in"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeIdentifierNode(doc, inIdent));
        }
        
        if(asPattern != null) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "as"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(asPattern.makeCoqDocTerm(doc));
        }
        
        return CoqDoc.mergeTermNodes(elem, elems);
    }
    
    @Override
    public String getDescription() {
        return "<Apply description>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Apply)) return false;

        Apply a = (Apply)anObj;
        return Objects.equals(isEApply, a.isEApply) &&
                Objects.equals(isLApply, a.isLApply) &&
                Objects.equals(isSimple, a.isSimple) &&
                Objects.equals(terms, a.terms) &&
                Objects.equals(withBindingsLists, a.withBindingsLists) &&
                Objects.equals(inIdent, a.inIdent) &&
                Objects.equals(asPattern, a.asPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEApply, isLApply, isSimple, terms,
                withBindingsLists, inIdent, asPattern);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        if(isEApply) sb.append("E");
        else if(isLApply) sb.append("L");
        sb.append("Apply");

        if(isSimple) sb.append(" simple");

        for(int i = 0; i < terms.size(); i++) {
            sb.append(" term");
            if(terms.size() > 1) sb.append(i + 1);
            sb.append("=").append(terms.get(i));

            if(withBindingsLists.containsKey(terms.get(i))) {
                sb.append(" with");
                if(terms.size() > 1) sb.append(i + 1);
                sb.append("=").append(withBindingsLists.get(terms.get(i)));
            }
        }

        if(inIdent != null) {
            sb.append(" in=").append(inIdent);
        }

        if(asPattern != null) {
            sb.append(" as=").append(asPattern);
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public Apply clone() {
        return new Apply(this);
    }

}
