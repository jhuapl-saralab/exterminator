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
package edu.jhuapl.exterminator.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.jhuapl.exterminator.grammar.coq.tactic.Tactic;
import edu.jhuapl.exterminator.gui.ExterminatorEventListener;
import edu.jhuapl.exterminator.gui.ExterminatorGUI;
import edu.jhuapl.exterminator.slmech.Program;

public class ApplyNextTacticAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private final ExterminatorGUI gui;

    public ApplyNextTacticAction(ExterminatorGUI gui) {
        super("Apply Next Tactic");
        this.gui = gui;
        putValue(SHORT_DESCRIPTION, "Apply the next tactic");
        //			putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        //			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
        //			        KeyEvent.VK_O, ActionEvent.CTRL_MASK));

        gui.addListener(new ExterminatorEventListener.Adapter() {
            @Override
            public void programLoaded(Program program) {
                setEnabled(program.getMainFunction().getTactics().size() > 0);
            }
            
            @Override
            public void tacticSuccessfulyRewound(int nextIndex, int tacticsSize) {
                setEnabled(true);
            }

            @Override
            public void tacticSuccessfullyApplied(Tactic tactic, int nextIndex,
                    int tacticsSize) {
                setEnabled(nextIndex < tacticsSize);
            }
            
            @Override
            public void tacticSuccessfullyInserted(int insertedIndex, int nextIndex, int tacticsSize) {
                setEnabled(nextIndex < tacticsSize);
            }
            
            @Override
            public void tacticSuccessfullyDeleted(int deletedIndex, int nextIndex,
                    int tacticsSize) {
                setEnabled(nextIndex < tacticsSize);
            }
            
            @Override
            public void proofSuccessfullyFinished() {
                setEnabled(false);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(!isEnabled()) return;

        gui.applyNextTactic();
    }
}
