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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import edu.jhuapl.exterminator.coq.CoqTop;
import edu.jhuapl.exterminator.grammar.coq.Require;
import edu.jhuapl.exterminator.grammar.coq.tactic.Tactic;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.grammar.coq.term.Some;
import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.utils.Logger;

public class ProgramStateTest {

    public static final Path SWAP_FILE = Paths.get("../Swap.v").toAbsolutePath();;

    public static Program getSwap() throws IOException {
        Assert.assertTrue(Files.isReadable(SWAP_FILE));

        Program program = new Program(SWAP_FILE, new Logger.NullLogger());
        Assert.assertEquals(1, program.getFunctions().size());

        return program;
    }

    public static CoqTop getSwapCoq() throws IOException {
        CoqTop coq = CoqTop.instance();
        coq.addToLoadPath(SWAP_FILE.getParent());

        return coq;
    }

    @Test
    public void test() throws IOException {
        CoqTop coq = getSwapCoq();
        Program program = getSwap();
        ProgramState state = new ProgramState(program);

        // interp
        for(Require require : program.getRequires()) {
            coq.interp(require.fullText());
        }
        coq.interp(program.getMainFunction().getAssertion().fullText());
        coq.interp(program.getMainFunction().getProof().getHeader());

        // tactics
        for(Tactic tactic : program.getMainFunction().getTactics()) {
            coq.interp(tactic.fullText());
            state.update(coq.goal());

            for(String goalID : state.getFGGoalIDs()) {
                Assert.assertEquals(3, state.getGoal(goalID).getVars().size());
                for(ID var : state.getGoal(goalID).getVars()) {
                    ProgramState.VarInfo info = state.getGoal(goalID).getVarInfo(var);
                    if(var.getFullName().equals("x")) {
                        Term t = info.getStoreBoundTerm();
                        if(t instanceof Some) {
                            Some some = (Some)t;
                            Assert.assertTrue(some.getTerm() instanceof ID);
                            Assert.assertEquals("vx", ((ID)some.getTerm()).getFullName());
                        } else {
                            Assert.assertTrue(t instanceof ID);
                            Assert.assertEquals("vx", ((ID)t).getFullName());
                        }

                    } else if(var.getFullName().equals("y")) {
                        Term t = info.getStoreBoundTerm();
                        if(t instanceof Some) {
                            Some some = (Some)t;
                            Assert.assertTrue(some.getTerm() instanceof ID);
                            Assert.assertEquals("vy", ((ID)some.getTerm()).getFullName());
                        } else {
                            Assert.assertTrue(t instanceof ID);
                            Assert.assertEquals("vy", ((ID)t).getFullName());
                        }
                    } else if(var.getFullName().equals("z")) {
                        Assert.assertNull(info.getStoreBoundTerm());
                    } else {
                        Assert.fail("Unexpected var: " + var);
                    }
                }
            }

            // only after the first tactic is it valid
            break;
        }

        coq.interp(program.getMainFunction().getProof().getFooter());
    }

}
