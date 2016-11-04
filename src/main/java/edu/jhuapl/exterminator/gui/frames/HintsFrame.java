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
package edu.jhuapl.exterminator.gui.frames;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import edu.jhuapl.exterminator.coq.message.CoqHintsMessage;
import edu.jhuapl.exterminator.gui.ExterminatorEventListener;
import edu.jhuapl.exterminator.gui.ExterminatorGUI;
import edu.jhuapl.exterminator.slmech.ProgramState;

public class HintsFrame extends ExterminatorFrame {

    private static final long serialVersionUID = 1L;

    private final JTextArea hypotheses, goals;
    
    private CoqHintsMessage currentHints;

    public HintsFrame(final ExterminatorGUI parent, boolean show) {
        super(parent, "Hints", show);

        JTabbedPane tabbedPane = new JTabbedPane();
        setContentPane(tabbedPane);

        tabbedPane.addTab("Hypotheses", new JScrollPane(hypotheses = new JTextArea()));
        hypotheses.setEditable(false);

        tabbedPane.addTab("Goals", new JScrollPane(goals = new JTextArea()));
        goals.setEditable(false);

        ///////////////////////////////////////////////////////////////////////

        parent.addListener(new ExterminatorEventListener.Adapter() {
            @Override
            public void programStateUpdated(ProgramState state) {
                CoqHintsMessage message = parent.hints();
                currentHints = message;

                hypotheses.setText("");
                goals.setText("");

                for(List<? extends CoqHintsMessage.Hint> hints : message.getHypotheses()) {
                    hypotheses.append("============================\n");
                    for(CoqHintsMessage.Hint hint : hints) {
                        hypotheses.append("(" + hint.getName() + ", " + hint.getCode() + ")\n");
                    }
                    hypotheses.append("============================\n\n");
                }

                for(CoqHintsMessage.Hint hint : message.getGoals()) {
                    goals.append("(" + hint.getName() + ", " + hint.getCode() + ")\n");
                }
            }
        });

        ///////////////////////////////////////////////////////////////////////

        setPreferredSize(new Dimension(400, 300));
        pack();
    }
    
    public CoqHintsMessage getCurrentHints() {
        return currentHints;
    }

}
