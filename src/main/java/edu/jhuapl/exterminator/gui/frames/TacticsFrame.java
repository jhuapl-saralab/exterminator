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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.exterminator.grammar.coq.sentence.Proof;
import edu.jhuapl.exterminator.grammar.coq.tactic.Tactic;
import edu.jhuapl.exterminator.gui.ExterminatorEventListener;
import edu.jhuapl.exterminator.gui.ExterminatorGUI;
import edu.jhuapl.exterminator.gui.GUIUtils;
import edu.jhuapl.exterminator.gui.action.ApplyNextTacticAction;
import edu.jhuapl.exterminator.gui.action.FinishTacticsAction;
import edu.jhuapl.exterminator.gui.action.RewindAction;
import edu.jhuapl.exterminator.gui.frames.tactics.TacticsPanel;
import edu.jhuapl.exterminator.slmech.ProgramState;

public class TacticsFrame extends ExterminatorFrame {

    // FIXME take into account multiple proofs are possible

    private static final long serialVersionUID = 1L;
    
    final ExterminatorGUI gui;

    private final JLabel header, footer;
    
    private final TacticsPanel tactics;

    public TacticsFrame(ExterminatorGUI parent, RewindAction rewindAction,
            ApplyNextTacticAction applyNextTacticAction, FinishTacticsAction finishAction,
            boolean show) {
        super(parent, "Tactics", show);
        
        this.gui = parent;

        JPanel content = new JPanel();
        setContentPane(content);
        content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        content.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        content.add(panel, BorderLayout.CENTER);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(GUIUtils.leftAlignedPanelWith(header = new JLabel()));
        panel.add(Box.createVerticalStrut(3));

        panel.add(tactics = new TacticsPanel(parent));

        panel.add(Box.createVerticalStrut(3));
        panel.add(GUIUtils.leftAlignedPanelWith(footer = new JLabel()));

        panel = new JPanel();
        content.add(panel, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(Box.createHorizontalGlue());
        panel.add(new JButton(rewindAction));
        panel.add(Box.createHorizontalStrut(10));
        panel.add(new JButton(applyNextTacticAction));
        panel.add(Box.createHorizontalStrut(10));
        panel.add(new JButton(finishAction));
        panel.add(Box.createHorizontalGlue());

        ///////////////////////////////////////////////////////////////////////

        parent.addListener(new ExterminatorEventListener.Adapter() {
//            @Override
//            public void progressPanelShown() {
//                tactics.setVisible(false);
//            }
//            
//            @Override
//            public void progressPanelHidden() {
//                tactics.setVisible(true);
//            }
            
            @Override
            public void programStateUpdated(ProgramState state) {
                update(state);
            }

            @Override
            public void tacticSuccessfullyApplied(Tactic tactic, int nextIndex,
                    int tacticsSize) {
                tactics.updateTactics();
            }

            @Override
            public void tacticSuccessfulyRewound(int nextIndex,
                    int tacticsSize) {
                tactics.updateTactics();
            }
            
            @Override
            public void tacticSuccessfullyInserted(int insertedIndex, int nextIndex, int tacticsSize) {
                tactics.tacticWasInserted(insertedIndex);
            }
            
            @Override
            public void tacticSuccessfullyDeleted(int deletedIndex, int newIndex, int size) {
                tactics.tacticWasDeleted(deletedIndex);
            }
            
            @Override
            public void proofSuccessfullyFinished() {
                tactics.finished();
            }
        });

        ///////////////////////////////////////////////////////////////////////

        setPreferredSize(new Dimension(500, 400));

        pack();
    }

    private void update(ProgramState program) {
        Proof proof = program.getProgram().getMainFunction().getProof();
        if(proof == null) {
            header.setText("");
            footer.setText("");
            tactics.clear();
        } else {
            header.setText(proof.getHeader());
            footer.setText(proof.getFooter());
            tactics.setTactics(program.getTactics());
        }
    }

}
