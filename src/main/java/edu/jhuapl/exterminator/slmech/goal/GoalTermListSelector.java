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
package edu.jhuapl.exterminator.slmech.goal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.jhuapl.exterminator.coq.message.CoqGoalMessage.Goal;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.grammar.coq.term.TypeCast;
import edu.jhuapl.exterminator.grammar.coq.term.expression.EqualsExpression;
import edu.jhuapl.exterminator.slmech.ProgramState;
import edu.jhuapl.exterminator.slmech.goal.term.EqualsSelector;
import edu.jhuapl.exterminator.slmech.goal.term.StoreboundSelector;
import edu.jhuapl.exterminator.slmech.goal.term.SubclassSelector;
import edu.jhuapl.exterminator.slmech.goal.term.TypeTermIDSelector;
import edu.jhuapl.exterminator.slmech.goal.term.TermSelector;
import edu.jhuapl.exterminator.slmech.goal.term.TypeTermSelector;

public class GoalTermListSelector<T> implements GoalHandler {

    private final List<Term> selected;

    private final List<T> results;

    private TermSelector<T> selector;

    public GoalTermListSelector(TermSelector<T> selector) {
        this.selected = new ArrayList<>();
        this.results = new ArrayList<>();
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public GoalTermListSelector<T> handle(Goal goal) {
        results.clear();
        for(Term term : goal.getHypothesisTerms()) {
            T t = selector.select(term);
            if(t != null) {
                selected.add(term);
                results.add(t);
            }
        }
        return this;
    }

    public GoalTermListSelector<T> disregard(GoalTermListSelector<?> ... others) {
        if(others == null || others.length == 0) return this;

        Set<Term> disregard = new HashSet<>();
        for(GoalTermListSelector<?> other : others) {
            if(other == null) continue;
            disregard.addAll(other.selected);
        }

        if(disregard.isEmpty()) return this;

        List<Term> oldSelected = new ArrayList<>(selected);
        List<T> oldResults = new ArrayList<>(results);
        selected.clear();
        results.clear();

        for(int i = 0; i < oldSelected.size(); i++) {
            if(!disregard.contains(oldSelected.get(i))) {
                selected.add(oldSelected.get(i));
                results.add(oldResults.get(i));
            }
        }

        return this;
    }

    public List<T> getResults() {
        return Collections.unmodifiableList(results);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static GoalTermListSelector<Term> all() {
        return new GoalTermListSelector<Term>(new TermSelector<Term>() {
            @Override
            public Term select(Term term) {
                return term;
            }
        });
    }

    public static GoalTermListSelector<Term> typedTerm(String type) {
        return new GoalTermListSelector<Term>(new TypeTermSelector(type));
    }

    public static GoalTermListSelector<ID> typedTermID(String type) {
        return new GoalTermListSelector<ID>(new TypeTermIDSelector(type));
    }

    public static GoalTermListSelector<TypeCast> typeCasts() {
        return new GoalTermListSelector<TypeCast>(new SubclassSelector<TypeCast>(TypeCast.class));
    }

    public static GoalTermListSelector<Term> storebound(ID var, Map<ID, ID> simpleTermTypes) {
        return new GoalTermListSelector<Term>(new StoreboundSelector(var, simpleTermTypes));
    }

    public static GoalTermListSelector<List<EqualsExpression>> equals(ID var, Map<ID, ID> simpleTermTypes) {
        return new GoalTermListSelector<List<EqualsExpression>>(new EqualsSelector(var, simpleTermTypes));
    }

    public static GoalTermListSelector<TypeCast> completes() {
        return new GoalTermListSelector<TypeCast>(ProgramState.CompletesState.termSelector());
    }

}
