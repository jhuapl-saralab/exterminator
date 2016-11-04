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

import java.util.List;
import java.util.Objects;

import edu.jhuapl.exterminator.grammar.coq.sentence.Assertion;
import edu.jhuapl.exterminator.grammar.coq.sentence.AssertionAndProof;
import edu.jhuapl.exterminator.grammar.coq.sentence.Proof;
import edu.jhuapl.exterminator.grammar.coq.tactic.Tactic;

public class Function {

    private final Program program;

    private final Assertion assertion;

    private final Proof proof;

    private final Code code;

    public Function(Program program, AssertionAndProof assertionAndProof) {
        this.program = Objects.requireNonNull(program);
        Objects.requireNonNull(assertionAndProof);
        this.assertion = assertionAndProof.getAssertion();
        this.proof = assertionAndProof.getProof();
        this.code = new Code(assertion);
    }

    public Program getProgram() { return program; }

    public Assertion getAssertion() { return assertion; }

    public Proof getProof() { return proof; }

    public List<Tactic> getTactics() { return proof.getTactics(); }

    public Code getCode() { return code; }

}
