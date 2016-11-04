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
package edu.jhuapl.exterminator.grammar.coq.sentence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.tactic.Tactic;

public class Proof extends CoqToken {

    private final String header, footer;

    private final List<Tactic> tactics;

    public Proof(CoqFTParser parser, CoqParser.ProofContext ctx) {
        super(parser, ctx);

        this.header = ctx.header.getText() + ".";

        this.tactics = new ArrayList<>();
        for(CoqParser.Tactic_invocationContext tactic : ctx.tactic_invocation()) {
            tactics.add(new Tactic(parser, tactic));
        }

        this.footer = ctx.footer.getText() + ".";
    }

    protected Proof(Proof copy) {
        super(copy);
        this.header = copy.header;
        this.footer = copy.footer;
        this.tactics = new ArrayList<>(copy.tactics.size());
        for(Tactic tactic : copy.tactics) {
            this.tactics.add(tactic.clone());
        }
    }

    public String getHeader() { return header; }

    public List<Tactic> getTactics() { return tactics; }

    public String getFooter() { return footer; }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(tactics);
    }

    @Override
    public boolean isTerminalNode() { return false; }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Proof)) return false;

        Proof p = (Proof)anObj;
        return Objects.equals(tactics, p.tactics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tactics);
    }

    @Override
    public String toString() {
        return "{Proof tactics=" + tactics + "}";
    }

    @Override
    public Proof clone() {
        return new Proof(this);
    }
}
