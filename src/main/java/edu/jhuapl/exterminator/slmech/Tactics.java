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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import edu.jhuapl.exterminator.grammar.coq.tactic.Tactic;

public class Tactics implements Cloneable {

    private final List<Tactic> originalTactics;
    
    private final LinkedList<Tactic> currentTactics;
    
    private int nextTacticIndex;
    
    public Tactics(List<Tactic> originalTactics) {
        Objects.requireNonNull(originalTactics);
        this.originalTactics = new ArrayList<>(originalTactics.size());
        this.currentTactics = new LinkedList<>();
        for(Tactic tactic : originalTactics) {
            this.originalTactics.add(tactic.clone());
            this.currentTactics.add(tactic.clone());
        }
        this.nextTacticIndex = 0;
    }
    
    private Tactics(Tactics copy) {
        this.originalTactics = new ArrayList<>(copy.originalTactics.size());
        for(Tactic tactic : copy.originalTactics) {
            this.originalTactics.add(tactic.clone());
        }
        this.currentTactics = new LinkedList<>();
        for(Tactic tactic : copy.currentTactics) {
            this.currentTactics.add(tactic.clone());
        }
        this.nextTacticIndex = copy.nextTacticIndex;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    // data model
    
    public int getNextTacticIndex() {
        return nextTacticIndex;
    }
    
    public int getCurrentTacticsSize() {
        return currentTactics.size();
    }
    
    public List<Tactic> getCurrentTactics() {
        return Collections.unmodifiableList(currentTactics);
    }
    
    public Tactic getCurrentTacticAt(int index) {
        return currentTactics.get(index);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    // controlling execution
    
    public Tactic getNextTactic() {
        if(nextTacticIndex < currentTactics.size()) {
            return currentTactics.get(nextTacticIndex);
        } else {
            return null;
        }
    }
    
    public void incrementNextTacticIndex(int amount) {
        nextTacticIndex += amount;
    }
    
    public void decrementNextTacticIndex(int amount) {
        nextTacticIndex -= amount;
        if(nextTacticIndex < 0) nextTacticIndex = 0;
    }
    
    public void insertTacticAt(int index, Tactic tactic) {
        if(index >= nextTacticIndex && index <= currentTactics.size()) {
            currentTactics.add(index, tactic);
        }
    }
    
    public void deleteTacticAt(int index) {
        if(index >= 0 && index < currentTactics.size()) {
            currentTactics.remove(index);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public Tactics clone() {
        return new Tactics(this);
    }
    
}
