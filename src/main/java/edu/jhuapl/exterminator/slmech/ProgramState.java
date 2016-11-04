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
package edu.jhuapl.exterminator.slmech;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.jhuapl.exterminator.coq.message.CoqGoalMessage;
import edu.jhuapl.exterminator.coq.message.CoqHintsMessage;
import edu.jhuapl.exterminator.grammar.coq.term.Completes;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.grammar.coq.term.TypeCast;
import edu.jhuapl.exterminator.grammar.coq.term.expression.EqualsExpression;
import edu.jhuapl.exterminator.slmech.goal.GoalTermListSelector;
import edu.jhuapl.exterminator.slmech.goal.term.TermSelector;

public class ProgramState {

    private final Program program;

    private final Tactics tactics;

    private final List<String> fgGoalIDs;

    private final List<String> bgGoalIDs;

    private final Map<String, GoalState> goals;

    public ProgramState(Program program) {
        this.program = Objects.requireNonNull(program);
        this.tactics = new Tactics(program.getMainFunction().getProof().getTactics());
        this.fgGoalIDs = new ArrayList<>();
        this.bgGoalIDs = new ArrayList<>();
        this.goals = new HashMap<>();
    }

    public Program getProgram() { return program; }

    public List<String> getFGGoalIDs() {
        return Collections.unmodifiableList(fgGoalIDs);
    }

    public List<String> getBGGoalIDs() {
        return Collections.unmodifiableList(bgGoalIDs);
    }

    public GoalState getGoal(String goalID) {
        return goals.get(goalID);
    }
    
    public Tactics getTactics() { return tactics; }

//    public int getTacticsSize() { return tactics.size(); }
//
//    public int getTacticsIndex() { return tacticsIndex; }
//
//    public Tactic getNextTactic() { return tactics.get(tacticsIndex); }
//
//    public void incrementTacticsIndex(int steps) {
//        if(tacticsIndex + steps >= getTacticsSize() + 1) {
//            throw new IllegalArgumentException(steps + " steps goes too far");
//        }
//        tacticsIndex += steps;
//    }
//
//    public void decrementTacticsIndex(int steps) {
//        if(tacticsIndex - steps < 0) {
//            throw new IllegalArgumentException(steps + " steps goes too far");
//        }
//        tacticsIndex -= steps;
//    }

    public void update(CoqGoalMessage message) {
        fgGoalIDs.clear();
        bgGoalIDs.clear();
        goals.clear();

        if(!message.statusIsGood()) return;

        if(message.getFG() != null) {
            for(CoqGoalMessage.Goal goal : message.getFG().getGoals()) {
                GoalState goalState = new GoalState(goal, true);
                fgGoalIDs.add(goalState.getID());
                goals.put(goalState.getID(), goalState);
            }
        }

        if(message.getBG() != null) {
            for(CoqGoalMessage.Goal goal : message.getBG().getGoals()) {
                GoalState goalState = new GoalState(goal, false);
                bgGoalIDs.add(goalState.getID());
                goals.put(goalState.getID(), goalState);
            }
        }
    }

    private VarInfo getInfoFor(ID var, CoqGoalMessage.Goal goal, Map<ID, ID> simpleTermTypes) {
        VarInfo info = new VarInfo(var);

        GoalTermListSelector<Term> storebound = GoalTermListSelector.storebound(var, simpleTermTypes).handle(goal);
        List<Term> storeboundTerms = storebound.getResults();
        if(storeboundTerms.size() == 1) {
            info.setStoreBoundTerm(storeboundTerms.get(0));
        } else if(storeboundTerms.size() > 1) {
            throw new IllegalArgumentException("Two+ store bound terms defined for " + var);
        }

        List<List<EqualsExpression>> equals = GoalTermListSelector.equals(var, simpleTermTypes).handle(goal)
                .disregard(storebound).getResults();
        for(List<EqualsExpression> list : equals) {
            if(list != null) info.exprsWithVar.addAll(list);
        }

        return info;
    }
    
    public List<ExpertHint> getExpertHints() {
        // FIXME
        List<ExpertHint> hints = new ArrayList<>();
        hints.add(new ExpertHint("Intros", "intros."));
        return hints;
    }
    
    public static class ExpertHint implements CoqHintsMessage.Hint {

        private final String name, code;
        
        public ExpertHint(String name, String code) {
            this.name = name;
            this.code = code;
        }
        
        @Override
        public String getName() { return name; }

        @Override
        public String getCode() { return code; }       
    }

    public class GoalState {
        
        public static final String VAR_TYPE = "var";

        private final CoqGoalMessage.Goal original;

        private final String id;

        private final boolean isForeground;

        private final GoalTermListSelector<ID> vars, vals, addresses;

        private final GoalTermListSelector<TypeCast> completesTerms;

        private final CompletesState completes;

        private final GoalTermListSelector<TypeCast> typedTerms;

        private final GoalTermListSelector<Term> otherTerms;

        private final Map<ID, VarInfo> varInfo;

        private final Conclusion conclusion;

        public GoalState(CoqGoalMessage.Goal goal, boolean isForeground) {
            this.original = goal;
            this.id = Objects.requireNonNull(goal.getID());
            this.isForeground = isForeground;

            this.vars = GoalTermListSelector.typedTermID(VAR_TYPE).handle(goal);
            this.vals = GoalTermListSelector.typedTermID("val").handle(goal);
            this.addresses = GoalTermListSelector.typedTermID("addr").handle(goal);

            this.completesTerms = GoalTermListSelector.completes().handle(goal);

            GoalTermListSelector<TypeCast> tempTypedTerms = GoalTermListSelector.typeCasts().handle(goal);

            Map<ID, ID> simpleTypedTerms = new HashMap<>();
            for(TypeCast tc : tempTypedTerms.getResults()) {
                if((tc.getTerm() instanceof ID) && (tc.getType() instanceof ID)) {
                    simpleTypedTerms.put((ID)tc.getTerm(), (ID)tc.getType());
                }
            }

            this.typedTerms = tempTypedTerms.disregard(vars, vals, addresses, completesTerms);

            if(completesTerms.getResults().size() == 1) {
                this.completes = new CompletesState(completesTerms.getResults().get(0));
            } else {
                this.completes = null;
            }

            this.otherTerms = GoalTermListSelector.all().handle(goal)
                    .disregard(vars, vals, addresses, completesTerms, typedTerms);
            this.varInfo = new HashMap<>();

            for(ID var : vars.getResults()) {
                VarInfo info = getInfoFor(var, goal, simpleTypedTerms);
                varInfo.put(var, info);

                // recursively get info for the right side
                while(info.storeBoundTerm != null && info.storeBoundTerm instanceof ID) {
                    ID term = (ID)info.storeBoundTerm;
                    VarInfo termInfo = getInfoFor(term, goal, simpleTypedTerms);
                    info.setStoreBoundTermInfo(termInfo);
                    info = termInfo;
                }
            }

            // check if any point to the same storebound variable
            for(ID var : vars.getResults()) {
                VarInfo info = varInfo.get(var);
                if(info.getStoreBoundTerm() == null) continue;
                Term sb1 = info.getStoreBoundTerm();
                for(VarInfo otherInfo : varInfo.values()) {
                    if(otherInfo == info) continue;

                    Term sb2 = otherInfo.getStoreBoundTerm();
                    if(sb1.equals(sb2)) {
                        info.varEqualities.add(otherInfo.getVar());
                    }
                }
            }

            this.conclusion = new Conclusion(goal.getConclusion());
        }

        public CoqGoalMessage.Goal getOriginalGoal() { return original; }

        public String getID() { return id; }

        public boolean isForeground() { return isForeground; }

        public List<ID> getVars() {
            return vars.getResults();
        }

        public List<ID> getVals() {
            return vals.getResults();
        }

        public List<ID> getAddresses() {
            return addresses.getResults();
        }

        public CompletesState getCompletes() {
            return completes;
        }

        public List<TypeCast> getTypedTerms() {
            return typedTerms.getResults();
        }

        public List<Term> getOtherTerms() {
            return otherTerms.getResults();
        }

        public VarInfo getVarInfo(ID var) {
            return varInfo.get(var);
        }

        public Conclusion getConclusion() {
            return conclusion;
        }

    }

    public static class VarInfo {

        private final ID var;

        private final List<EqualsExpression> exprsWithVar;

        private Term storeBoundTerm;

        private VarInfo storeBoundTermInfo;

        private final List<ID> varEqualities;

        public VarInfo(ID var) {
            this.var = var;
            this.exprsWithVar = new ArrayList<>();
            this.varEqualities = new ArrayList<>();
        }

        public ID getVar() { return var; }

        public List<EqualsExpression> getExpressionsWithVar() {
            return Collections.unmodifiableList(exprsWithVar);
        }

        public Term getStoreBoundTerm() { return storeBoundTerm; }

        private void setStoreBoundTerm(Term term) {
            this.storeBoundTerm = term;
        }

        public VarInfo getStoreBoundTermInfo() { return storeBoundTermInfo; }

        private void setStoreBoundTermInfo(VarInfo info) {
            this.storeBoundTermInfo = info;
        }

        public List<ID> getVarEqualities() {
            return Collections.unmodifiableList(varEqualities);
        }

        ///////////////////////////////////////////////////////////////////////

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{VarInfo var=").append(var);
            sb.append(" storeBound=").append(storeBoundTerm);
            sb.append("}");
            return sb.toString();
        }

    }

    public static class CompletesState {

        public static TermSelector<TypeCast> termSelector() {
            return new TermSelector<TypeCast>() {
                @Override
                public TypeCast select(Term term) {
                    if(!(term instanceof TypeCast)) return null;

                    TypeCast tc = (TypeCast)term;
                    if(tc.getTerm() instanceof ID &&
                            tc.getType() instanceof Completes) {
                        return tc;
                    }

                    return null;

//					Expression e = (Expression)tc.getType();
//					if(e.isListOfIDs() || !(e.getTerm() instanceof ID) ||
//							!((ID)e.getTerm()).getFullName().equalsIgnoreCase("completes"))
//						return null;
//					
//					if(e.getArgs().size() != 3) return null;
//					
//					Term t = e.getArg(0).getTerm();
//					if(!(t instanceof NamedFields)) return null;
//					
//					return tc;
                }
            };
        }

        ///////////////////////////////////////////////////////////////////////

        private final TypeCast term;

        private final String id;

        private final Completes completes;

        private final List<Term> prog;

        public CompletesState(TypeCast term) {
            this.term = Objects.requireNonNull(term);

            this.id = ((ID)term.getTerm()).getFullName();
            this.completes = (Completes)term.getType();
            this.prog = Code.getProgFrom(completes.getStatements());
        }

        public TypeCast getTerm() { return term; }

        public String getID() { return id; }

        public Term getST1() { return completes.getSt1(); }

        public Term getST2() { return completes.getSt2(); }

        public List<Term> getProg() {
            return Collections.unmodifiableList(prog);
        }

    }

    public static class Conclusion {

        private final Term term;

        public Conclusion(Term term) {
            this.term = term;
        }

        public Term getTerm() { return term; }

    }

}
