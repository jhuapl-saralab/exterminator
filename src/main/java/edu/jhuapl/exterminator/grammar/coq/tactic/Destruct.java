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
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Destruct extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.DESTRUCT() != null || ctx.EDESTRUCT() != null ||
                ctx.CASE() != null || ctx.ECASE() != null ||
                ctx.CASE_EQ() != null;
    }

    /*
    |   DESTRUCT term (TOK_COMMA term)*
    |   DESTRUCT term AS disj_conj_intro_pattern
    |   DESTRUCT term EQN naming_intro_pattern
    |   DESTRUCT term WITH wbl=bindings_list
    |   EDESTRUCT term
    |   DESTRUCT term1=term USING term2=term (WITH wbl=bindings_list)?
    |   DESTRUCT term IN goal_occurrences
    |   (DESTRUCT|EDESTRUCT) term1=term WITH bindings_list1=bindings_list AS disj_conj_intro_pattern EQN naming_intro_pattern USING term2=term WITH bindings_list2=bindings_list IN goal_occurrences
    |   SIMPLE DESTRUCT (simpleDestructIdent=ident|simpleDestructTerm=term)
    |   (CASE|ECASE) term (WITH bindings_list)?
    |   CASE_EQ term
     */

    private final boolean isEDestruct, isCase, isECase, isCaseEq;

    private final boolean isSimple;

    private final List<Term> terms;

    private final DisjConjIntroPattern disjConjPattern;

    private final NamingIntroPattern namingPattern;

    private final BindingsList bindings;

    private final Term usingTerm1, usingTerm2;

    private final GoalOccurrences inGoalOccurrences;

    private final Ident simpleIdent;

    private final Term simpleTerm;

    private final Term eqnTerm1, eqnTerm2;

    private final BindingsList eqnBindings1, eqnBindings2;

    public Destruct(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        this.isEDestruct = ctx.EDESTRUCT() != null;
        this.isCase = ctx.CASE() != null;
        this.isECase = ctx.ECASE() != null;
        this.isCaseEq = ctx.CASE_EQ() != null;

        this.isSimple = ctx.SIMPLE() != null;
        this.terms = new ArrayList<>();
        if(ctx.term() != null) {
            for(CoqParser.TermContext term : ctx.term()) {
                this.terms.add(Term.make(parser, term));
            }
        }
        this.disjConjPattern = ctx.disj_conj_intro_pattern() == null ? null :
            new DisjConjIntroPattern(parser, ctx.disj_conj_intro_pattern());
        this.namingPattern = ctx.naming_intro_pattern() == null ? null :
            new NamingIntroPattern(parser, ctx.naming_intro_pattern());
        this.bindings = ctx.wbl == null ? null : new BindingsList(parser, ctx.wbl);
        this.usingTerm1 = ctx.term1 == null ? null : Term.make(parser, ctx.term1);
        this.usingTerm2 = ctx.term2 == null ? null : Term.make(parser, ctx.term2);
        this.inGoalOccurrences = ctx.goal_occurrences() == null ? null :
            new GoalOccurrences(parser, ctx.goal_occurrences());
        this.simpleIdent = ctx.simpleDestructIdent == null ? null :
            new Ident(parser, ctx.simpleDestructIdent);
        this.simpleTerm = ctx.simpleDestructTerm == null ? null :
            Term.make(parser, ctx.simpleDestructTerm);
        this.eqnTerm1 = ctx.term1 == null ? null : Term.make(parser, ctx.term1);
        this.eqnTerm2 = ctx.term2 == null ? null : Term.make(parser, ctx.term2);
        this.eqnBindings1 = ctx.bindings_list1 == null ? null :
            new BindingsList(parser, ctx.bindings_list1);
        this.eqnBindings2 = ctx.bindings_list2 == null ? null :
            new BindingsList(parser, ctx.bindings_list2);
    }

    protected Destruct(Destruct copy) {
        super(copy);
        this.isEDestruct = copy.isEDestruct;
        this.isCase = copy.isCase;
        this.isECase = copy.isECase;
        this.isCaseEq = copy.isCaseEq;
        this.isSimple = copy.isSimple;
        this.terms = new ArrayList<>(copy.terms.size());
        for(Term term : copy.terms) {
            this.terms.add(term.clone());
        }
        this.disjConjPattern = copy.disjConjPattern == null ? null : copy.disjConjPattern.clone();
        this.namingPattern = copy.namingPattern == null ? null : copy.namingPattern.clone();
        this.bindings = copy.bindings == null ? null : copy.bindings.clone();
        this.usingTerm1 = copy.usingTerm1 == null ? null : copy.usingTerm1.clone();
        this.usingTerm2 = copy.usingTerm2 == null ? null : copy.usingTerm2.clone();
        this.inGoalOccurrences = copy.inGoalOccurrences == null ? null : copy.inGoalOccurrences.clone();
        this.simpleIdent = copy.simpleIdent == null ? null : copy.simpleIdent.clone();
        this.simpleTerm = copy.simpleTerm == null ? null : copy.simpleTerm.clone();
        this.eqnTerm1 = copy.eqnTerm1 == null ? null : copy.eqnTerm1.clone();
        this.eqnTerm2 = copy.eqnTerm2 == null ? null : copy.eqnTerm2.clone();
        this.eqnBindings1 = copy.eqnBindings1 == null ? null : copy.eqnBindings1.clone();
        this.eqnBindings2 = copy.eqnBindings2 == null ? null : copy.eqnBindings2.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(terms, disjConjPattern, namingPattern, bindings,
                usingTerm1, usingTerm2, inGoalOccurrences, simpleIdent,
                simpleTerm, eqnTerm1, eqnTerm2, eqnBindings1, eqnBindings2);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc);
        List<Element> elems = new ArrayList<>();
        
        if(isSimple) {
            elems.add(CoqDoc.makeKeywordNode(doc, "simple"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "destruct"));
            
            if(simpleIdent != null) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeIdentifierNode(doc, simpleIdent));
                
            } else if(simpleTerm != null) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeParenthesizedTermNode(doc, simpleTerm));
            }
            
            return CoqDoc.mergeTermNodes(elem, elems);
        }
        
        if(isCaseEq) {
            return CoqDoc.makeTermNode(doc,
                    CoqDoc.makeKeywordNode(doc, "case_eq"),
                    CoqDoc.makeWhitespaceNode(doc),
                    CoqDoc.makeParenthesizedTermNode(doc,terms.get(0)));
        }

        if(isEDestruct) elems.add(CoqDoc.makeKeywordNode(doc, "edestruct"));
        else if(isCase) elems.add(CoqDoc.makeKeywordNode(doc, "case"));
        else if(isECase) elems.add(CoqDoc.makeKeywordNode(doc, "ecase"));
        else elems.add(CoqDoc.makeKeywordNode(doc, "destruct"));
        
        elems.add(CoqDoc.makeWhitespaceNode(doc));
        
        if(usingTerm1 != null) {
            elems.add(CoqDoc.makeParenthesizedTermNode(doc, usingTerm1));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "using"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeParenthesizedTermNode(doc, usingTerm2));
            
            if(bindings != null) {
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(CoqDoc.makeKeywordNode(doc, "with"));
                elems.add(CoqDoc.makeWhitespaceNode(doc));
                elems.add(bindings.makeCoqDocTerm(doc));
            }
            
            return CoqDoc.mergeTermNodes(elem, elems);
        }
        
        if(eqnTerm1 != null) {
            /*
IN goal_occurrences
             */
            elems.add(CoqDoc.makeParenthesizedTermNode(doc, eqnTerm1));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "with"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(eqnBindings1.makeCoqDocTerm(doc));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "as"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(disjConjPattern.makeCoqDocTerm(doc));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "eqn"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(namingPattern.makeCoqDocTerm(doc));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "using"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeParenthesizedTermNode(doc, eqnTerm2));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "with"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(eqnBindings2.makeCoqDocTerm(doc));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "in"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(inGoalOccurrences.makeCoqDocTerm(doc));
            
            return CoqDoc.mergeTermNodes(elem, elems);
        }
        
        elems.add(CoqDoc.makeParenthesizedTermNode(doc, terms.get(0)));
        
        if(bindings != null) {
            elems.add(CoqDoc.makeKeywordNode(doc, "with"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(bindings.makeCoqDocTerm(doc));
            
            return CoqDoc.mergeTermNodes(elem, elems);
        }
        
        if(disjConjPattern != null) {
            elems.add(CoqDoc.makeKeywordNode(doc, "as"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(disjConjPattern.makeCoqDocTerm(doc));
            
            return CoqDoc.mergeTermNodes(elem, elems);
        }
        
        if(namingPattern != null) {
            elems.add(CoqDoc.makeKeywordNode(doc, "eqn"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(namingPattern.makeCoqDocTerm(doc));
            
            return CoqDoc.mergeTermNodes(elem, elems);
        }
        
        if(inGoalOccurrences != null) {
            elems.add(CoqDoc.makeKeywordNode(doc, "in"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(inGoalOccurrences.makeCoqDocTerm(doc));
            
            return CoqDoc.mergeTermNodes(elem, elems);
        }
        
        for(int i = 1; i < terms.size(); i++) {
            elems.add(CoqDoc.makeKeywordNode(doc, "eqn"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeParenthesizedTermNode(doc, terms.get(i)));
        }
        
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    @Override
    public String getDescription() {
        return "<Destruct description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Destruct)) return false;

        Destruct d = (Destruct)anObj;
        return Objects.equals(isEDestruct, d.isEDestruct) &&
                Objects.equals(isCase, d.isCase) &&
                Objects.equals(isECase, d.isECase) &&
                Objects.equals(isCaseEq, d.isCaseEq) &&
                Objects.equals(isSimple, d.isSimple) &&
                Objects.equals(terms, d.terms) &&
                Objects.equals(disjConjPattern, d.disjConjPattern) &&
                Objects.equals(namingPattern, d.namingPattern) &&
                Objects.equals(bindings, d.bindings) &&
                Objects.equals(usingTerm1, d.usingTerm1) &&
                Objects.equals(usingTerm2, d.usingTerm2) &&
                Objects.equals(inGoalOccurrences, d.inGoalOccurrences) &&
                Objects.equals(simpleIdent, d.simpleIdent) &&
                Objects.equals(simpleTerm, d.simpleTerm) &&
                Objects.equals(eqnTerm1, d.eqnTerm1) &&
                Objects.equals(eqnTerm2, d.eqnTerm2) &&
                Objects.equals(eqnBindings1, d.eqnBindings1) &&
                Objects.equals(eqnBindings2, d.eqnBindings2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEDestruct, isCase, isECase, isCaseEq, isSimple,
                terms, disjConjPattern, namingPattern, bindings, usingTerm1,
                usingTerm2, inGoalOccurrences, simpleIdent, simpleTerm,
                eqnTerm1, eqnTerm2, eqnBindings1, eqnBindings2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{");

        if(isEDestruct) sb.append("EDestruct");
        else if(isCase) sb.append("Case");
        else if(isECase) sb.append("ECase");
        else if(isCaseEq) sb.append("CaseEq");
        else sb.append("Destruct");

        if(isSimple) sb.append(" simple");

        if(terms.size() == 1) {
            sb.append(" term=").append(terms.get(0));
        } else if(terms.size() > 1 && usingTerm1 == null && eqnTerm1 == null) {
            sb.append(" terms=").append(terms);
        }

        if(simpleIdent != null) {
            sb.append(" ident=").append(simpleIdent);
        } else if(simpleTerm != null) {
            sb.append(" term=").append(simpleTerm);
        }

        if(usingTerm1 != null && usingTerm2 != null) {
            sb.append(" term1=").append(usingTerm1).append(" using term2=");
            sb.append(usingTerm2);
        }

        if(eqnTerm1 != null && eqnBindings1 != null) {
            sb.append(" term1=").append(eqnTerm1).append(" bindings1=");
            sb.append(eqnBindings1);
        }

        if(disjConjPattern != null) {
            sb.append(" as disjConj=").append(disjConjPattern);
        }

        if(namingPattern != null) {
            sb.append(" eqn: naming=").append(namingPattern);
        }

        if(bindings != null) {
            sb.append(" with=").append(bindings);
        }

        if(eqnTerm2 != null && eqnBindings2 != null) {
            sb.append(" using term2=").append(eqnTerm2).append(" bindings2=");
            sb.append(eqnBindings2);
        }

        if(inGoalOccurrences != null) {
            sb.append(" in goalOccurrences=").append(inGoalOccurrences);
        }

        sb.append("}");

        return sb.toString();
    }

    @Override
    public Destruct clone() {
        return new Destruct(this);
    }

}
