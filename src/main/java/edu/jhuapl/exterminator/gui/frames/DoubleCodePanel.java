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
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;
import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.gui.CoqDocText;
import edu.jhuapl.exterminator.gui.GUIUtils;
import edu.jhuapl.exterminator.slmech.ProgramState;

public class DoubleCodePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JPanel leftPanel, leftTermPanel, rightPanel, rightTermPanel;

    private final List<JTextPane> rightLines;

    private final List<CoqDocText> leftTerms, rightTerms;

    public DoubleCodePanel() {
        super(new BorderLayout());

        leftPanel = new JPanel(new BorderLayout());
        rightPanel = new JPanel(new BorderLayout());

        JSplitPane split;
        add(split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, rightPanel), BorderLayout.CENTER);

        leftPanel.add(leftTermPanel = new JPanel(new GridBagLayout()),
                BorderLayout.CENTER);
        rightPanel.add(rightTermPanel = new JPanel(new GridBagLayout()),
                BorderLayout.CENTER);

        split.setOneTouchExpandable(true);
        split.setDividerLocation(0.5);
        split.setResizeWeight(0.5);

        this.rightLines = new ArrayList<>();
        this.leftTerms = new ArrayList<>();
        this.rightTerms = new ArrayList<>();
    }

    public void setLeftHeader(Component component) {
        leftPanel.add(component, BorderLayout.NORTH);
    }

    public void clearLeftCode() {
        for(CoqDocText text : leftTerms) {
            text.clearHover();
        }
        leftTermPanel.removeAll();
        leftTerms.clear();
    }

    public void setLeftCode(List<Term> code) {
        clearLeftCode();

        if(code == null) return;

        setCode(code, leftTermPanel, leftTerms, null);
    }

    public void setRightHeader(Component component) {
        rightPanel.add(component, BorderLayout.NORTH);
    }

    public void clearRightCode() {
        for(CoqDocText text : rightTerms) {
            text.clearHover();
        }
        rightTermPanel.removeAll();
        rightTerms.clear();
        rightLines.clear();
    }

    public void setRightCode(List<Term> code, boolean matchToLeft) {
        clearRightCode();

        if(code == null) return;

        GridBagConstraints gbc = center(0);
        gbc.gridwidth = 2;
        gbc.weighty *= 2;
        rightTermPanel.add(Box.createVerticalGlue(), gbc);

        setCode(code, rightTermPanel, rightTerms, rightLines);

        if(matchToLeft) match();
    }

    private void setCode(List<Term> code, JPanel termPanel,
            List<CoqDocText> terms, List<JTextPane> lines) {
        for(int i = 0; i < code.size(); i++) {
            Term term = code.get(i);

            int row = i;
            if(termPanel == rightTermPanel) row++;

            CoqDocText text = new CoqDocText(term, ";;");
            JTextPane line = text.makeLineWidget(i + 1);

            termPanel.add(line, left(row));
            termPanel.add(text, center(row));

            if(lines != null) lines.add(line);
            terms.add(text);
        }
    }

    public void updateVarInformation(ProgramState.GoalState goal) {
        for(CoqDocText term : leftTerms) {
            term.updateVarInformation(goal);
        }
        for(CoqDocText term : rightTerms) {
            term.updateVarInformation(goal);
        }
    }

    public void match() {
        if(leftTerms == null || leftTerms.size() == 0) return;

        Color bg = new JTextPane().getBackground();
        for(CoqDocText text : leftTerms) {
            text.setBackground(bg);
        }

        if(rightTerms == null || rightTerms.size() == 0) return;

        if(rightTerms.size() > leftTerms.size()) {
            throw new IllegalArgumentException("Goal completes is longer than program.");
        }

        for(JTextPane text : rightTerms) {
            text.setBackground(bg);
        }

        for(int i = 0; i < rightTerms.size(); i++) {
            int leftIndex = leftTerms.size() - i - 1,
                    rightIndex = rightTerms.size() - i - 1,
                    line = leftTerms.size() - i;
            CoqDocable left = leftTerms.get(leftIndex).getTerm(),
                    right = rightTerms.get(rightIndex).getTerm();
            CoqDocText leftText = leftTerms.get(leftIndex),
                    rightText = rightTerms.get(leftIndex);
            rightText.setLineWidget(rightLines.get(rightIndex), line);
            if(!left.equals(right)) {
                leftText.setBackground(Color.GREEN);
                rightText.setBackground(Color.GREEN);
            }
            int maxHeight = Math.max(
                    leftText.getHeight(), rightText.getHeight());
            GUIUtils.setFixedHeight(leftText, maxHeight);
            GUIUtils.setFixedHeight(rightText, maxHeight);
        }

        for(int i = 0; i < (leftTerms.size() - rightTerms.size()); i++) {
            leftTerms.get(i).setBackground(Color.YELLOW);
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private static GridBagConstraints left(int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        return gbc;
    }

    private static GridBagConstraints center(int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        return gbc;
    }

}
