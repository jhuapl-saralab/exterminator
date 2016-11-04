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
package edu.jhuapl.exterminator.slmech.goal.term;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.term.Arg;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.grammar.coq.term.StoreBound;
import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.grammar.coq.term.TypeCast;
import edu.jhuapl.exterminator.grammar.coq.term.expression.EqualsExpression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.Expression;
import edu.jhuapl.exterminator.slmech.ProgramState;

public class StoreboundSelector implements TermSelector<Term> {
    
    // H# or SPROP#
    private static final Pattern HYPOTHESIS_STATEMENT_TERM_PATTERN = Pattern.compile("[A-Z]+\\d+");
    
    public static boolean isHypothesisStatement(Term term) {
        if(!(term instanceof TypeCast)) return false;
        TypeCast tc = (TypeCast)term;
        if(!(tc.getTerm() instanceof ID)) return false;
        String id = ((ID)tc.getTerm()).getFullName();
        return HYPOTHESIS_STATEMENT_TERM_PATTERN.matcher(id).matches();
    }
    
    public static final String STORE_TYPE = "store_f";
    
    public static final String LOCALS_TYPE = "locals";
    
    private static boolean isType(Map<ID, ID> simpleTermTypes, Term term, String type) {
        if(!(term instanceof ID)) return false;
        ID id = (ID)term;
        if(!simpleTermTypes.containsKey(id)) return false;
        ID val = simpleTermTypes.get(id);
        return type.equalsIgnoreCase(val.getFullName());
    }
    
    public static ID isStoreAccess(Map<ID, ID> simpleTermTypes, Term term) {
        if(!(term instanceof Expression)) return null;
        Expression e = (Expression)term;
        
        // either (store (locals var)) or (store (locals (var))
        if(e.getArgs().isEmpty() || e.getArgs().size() > 2) return null;

        if(!isType(simpleTermTypes, e.getTerm(), STORE_TYPE)) return null;
        
        Term t1, t2;
        if(e.getArgs().size() == 1) {
            // (store (locals (var))
            Arg a = e.getArg(0);
            if(a.getIdentDefine() != null || !(a.getTerm() instanceof Expression)) return null;
            e = (Expression)a.getTerm();
            if(e.getArgs().size() != 1) return null;
            t1 = e.getTerm();
            a = e.getArg(0);
            if(a.getIdentDefine() != null) return null;
            t2 = a.getTerm();
            
        } else {
            // (store (locals var))
            Arg a = e.getArg(0);
            if(a.getIdentDefine() != null) return null;
            t1 = a.getTerm();
            a = e.getArg(1);
            if(a.getIdentDefine() != null) return null;
            t2 = a.getTerm();
        }
        
        if(!isType(simpleTermTypes, t1, LOCALS_TYPE)) return null;
        
        if(!isType(simpleTermTypes, t2, ProgramState.GoalState.VAR_TYPE)) return null;
        
        // finally
        return (ID)t2;
    }

    private final ID var;

    private final EqualsSelector equals;

    private final Map<ID, ID> simpleTermTypes;

    public StoreboundSelector(ID var, Map<ID, ID> simpleTermTypes) {
        this.var = Objects.requireNonNull(var);
        this.equals = new EqualsSelector(var, simpleTermTypes);
        this.simpleTermTypes = Objects.requireNonNull(simpleTermTypes);
    }

    @Override
    public Term select(Term term) {
        for(CoqToken parent : term.getParentsOf((CoqToken)var)) {
            if(parent instanceof StoreBound) {
                StoreBound s = (StoreBound)parent;
                if(s.getLeft().equals(var)) {
                    return s.getRight();
                }
            }
        }

        List<EqualsExpression> exprs = equals.select(term);
        if(exprs != null) {
            for(EqualsExpression expr : exprs) {
                ID left = getLeft(expr, simpleTermTypes),
                        right = getRight(expr, simpleTermTypes);
                if(left == null || right == null) continue;

                if(var.equals(left)) {
                    return (Term)right;
                }
            }
        }
        
        if(isHypothesisStatement(term) && ((TypeCast)term).getType() instanceof EqualsExpression) {
            Term left = ((EqualsExpression)((TypeCast)term).getType()).getLeft();
            Term right = ((EqualsExpression)((TypeCast)term).getType()).getRight();
            
            ID storeAccessVar = isStoreAccess(simpleTermTypes, left);
            if(var.equals(storeAccessVar)) {
                return right;
            } else {
                return null;
            }
        }

        return null;
    }

    public static ID getLeft(EqualsExpression expr, Map<ID, ID> simpleTermTypes) {
        if(!(expr.getLeft() instanceof Expression)) return null;

        Expression left = (Expression)expr.getLeft();
        if(!left.isListOfIDs() || left.getArgs().size() != 2) return null;

        ID id1 = (ID)left.getTerm(),
                id2 = (ID)left.getArg(0).getTerm(),
                id3 = (ID)left.getArg(1).getTerm();
        ID type1 = simpleTermTypes.get(id1),
                type2 = simpleTermTypes.get(id2),
                type3 = simpleTermTypes.get(id3);
        if(type1 == null || type2 == null || type3 == null) return null;
        if(!"store_f".equalsIgnoreCase(type1.getFullName())) return null;
        if(!"locals".equalsIgnoreCase(type2.getFullName())) return null;
        if(!"var".equalsIgnoreCase(type3.getFullName())) return null;

        return id3;
    }

    public static ID getRight(EqualsExpression expr, Map<ID, ID> simpleTermTypes) {
        Term right = expr.getRight();
        if(right instanceof ID) return (ID)right;

        if(!(right instanceof Expression)) return null;

        Expression eRight = (Expression)right;
        if(!eRight.isListOfIDs() || eRight.getArgs().size() != 1) return null;

        ID id1 = (ID)eRight.getTerm(),
                id2 = (ID)eRight.getArg(0).getTerm();

        if(!"some".equalsIgnoreCase(id1.getFullName())) return null;

        return id2;
    }

}
