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
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import edu.jhuapl.exterminator.gui.CoqDocText;
import edu.jhuapl.exterminator.gui.ExterminatorEventListener;
import edu.jhuapl.exterminator.gui.ExterminatorGUI;
import edu.jhuapl.exterminator.gui.GUIUtils;
import edu.jhuapl.exterminator.gui.action.OpenAction;
import edu.jhuapl.exterminator.slmech.Code;
import edu.jhuapl.exterminator.slmech.Program;
import edu.jhuapl.exterminator.slmech.ProgramState;

public class CodeFrame extends ExterminatorFrame {

    // FIXME take into account multiple proofs are possible

    private static final long serialVersionUID = 1L;

    private final JTextField loadedFile;

    private final JComboBox<String> goalIDChooser;

    private final ItemListener goalIDChooserListener;

    private final DoubleCodePanel codePanel;

    private final JPanel conclusion;

    private final JPanel st1, pre, st2, post;

    private boolean progressPanelShowing;

    private boolean doMatchOnReshow;

    public CodeFrame(final ExterminatorGUI parent, OpenAction openAction,
            boolean show) {
        super(parent, "Code", show);

        JPanel content = new JPanel(new BorderLayout());
        setContentPane(content);
        content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        int spacing = 10;

        JPanel panel = new JPanel(new BorderLayout());
        content.add(panel, BorderLayout.NORTH);

        panel.add(new JLabel("Loaded File:"), BorderLayout.WEST);
        panel.add(GUIUtils.leftAlignedPanelWith(
                Box.createHorizontalStrut(spacing),
                loadedFile = new JTextField(),
                Box.createHorizontalStrut(spacing)), BorderLayout.CENTER);
        loadedFile.setEditable(false);
        loadedFile.setFont(loadedFile.getFont().deriveFont(Font.BOLD));
        panel.add(new JButton(openAction), BorderLayout.EAST);
        panel.add(Box.createVerticalStrut(spacing), BorderLayout.SOUTH);

        JXTaskPaneContainer taskContainer = new JXTaskPaneContainer();
        content.add(new JScrollPane(taskContainer), BorderLayout.CENTER);

        JXTaskPane taskPane = new JXTaskPane();
        taskContainer.add(taskPane);
        taskPane.setTitle("Program:");
        taskPane.setCollapsed(false);

        taskPane.add(codePanel = new DoubleCodePanel());
        goalIDChooser = new JComboBox<>();
        goalIDChooser.setVisible(false);

        JPanel header;
        codePanel.setLeftHeader(header = GUIUtils.leftAlignedPanelWith(
                new JLabel("Original Program")));
        GUIUtils.setFixedHeight(header, goalIDChooser.getPreferredSize().height);
        codePanel.setRightHeader(header = GUIUtils.leftAlignedPanelWith(
                new JLabel("Goal "), goalIDChooser,
                new JLabel(" Completes Program")));
        GUIUtils.setFixedHeight(header, goalIDChooser.getPreferredSize().height);

        taskPane = new JXTaskPane();
        taskContainer.add(taskPane);
        taskPane.setTitle("Goal Conclusion");
        taskPane.setCollapsed(false);

        taskPane.add(conclusion = new JPanel(new BorderLayout()));

        taskPane = new JXTaskPane();
        taskContainer.add(taskPane);
        taskPane.setTitle("Conditions and States");
        taskPane.setCollapsed(true);

        JXTaskPaneContainer condTaskContainer = new JXTaskPaneContainer();
        taskPane.add(condTaskContainer);

        taskPane = new JXTaskPane();
        condTaskContainer.add(taskPane);
        taskPane.setTitle("State 1:");
        taskPane.setCollapsed(true);
        taskPane.add(st1 = new JPanel(new BorderLayout()));

        taskPane = new JXTaskPane();
        condTaskContainer.add(taskPane);
        taskPane.setTitle("Pre-Condition:");
        taskPane.setCollapsed(true);
        taskPane.add(pre = new JPanel(new BorderLayout()));

        taskPane = new JXTaskPane();
        condTaskContainer.add(taskPane);
        taskPane.setTitle("State 2:");
        taskPane.setCollapsed(true);
        taskPane.add(st2 = new JPanel(new BorderLayout()));

        taskPane = new JXTaskPane();
        condTaskContainer.add(taskPane);
        taskPane.setTitle("Post-Condition:");
        taskPane.setCollapsed(true);
        taskPane.add(post = new JPanel(new BorderLayout()));

        ///////////////////////////////////////////////////////////////////////

        goalIDChooser.addItemListener(goalIDChooserListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    parent.goalSelected((String)goalIDChooser.getSelectedItem());
                }
            }
        });

        this.progressPanelShowing = false;
        this.doMatchOnReshow = false;

        parent.addListener(new ExterminatorEventListener.Adapter() {
            @Override
            public void progressPanelShown() {
                progressPanelShowing = true;
            }

            @Override
            public void progressPanelHidden() {
                progressPanelShowing = false;
                if(doMatchOnReshow) {
                    codePanel.match();
                }
                doMatchOnReshow = false;
            }

            @Override
            public void programLoaded(Program program) {
                setProgram(program);
            }

            @Override
            public void goalSelected(ProgramState state, String goalID) {
                setGoal(state, goalID);
            }
        });

        ///////////////////////////////////////////////////////////////////////

        pack();
    }

    private void setProgram(Program program) {
        Code code = program.getMainFunction().getCode();

        loadedFile.setText(program.getFile().toString());
        pre.removeAll();
        pre.add(new CoqDocText(code.getPrecondition(), null), BorderLayout.CENTER);
        st1.removeAll();
        st1.add(new CoqDocText(code.getState1(), null), BorderLayout.CENTER);
        post.removeAll();
        post.add(new CoqDocText(code.getPostcondition(), null), BorderLayout.CENTER);
        st2.removeAll();
        st2.add(new CoqDocText(code.getState2(), null), BorderLayout.CENTER);

        codePanel.setLeftCode(code.getProg());
        codePanel.clearRightCode();
    }

    private void setGoal(ProgramState state, String goalID) {
        List<String> currentIDs = new ArrayList<>(),
                newIDs = new ArrayList<>(state.getFGGoalIDs());
        for(int i = 0; i < goalIDChooser.getItemCount(); i++) {
            currentIDs.add(goalIDChooser.getItemAt(i));
        }
        Collections.sort(currentIDs);
        Collections.sort(newIDs);

        String currentID = (String)goalIDChooser.getSelectedItem();
        if(!currentIDs.equals(newIDs)) {
            goalIDChooser.removeItemListener(goalIDChooserListener);
            goalIDChooser.removeAllItems();
            for(String id : newIDs) {
                goalIDChooser.addItem(id);
            }
            goalIDChooser.setVisible(goalIDChooser.getItemCount() > 0);
            goalIDChooser.addItemListener(goalIDChooserListener);
        }

        ProgramState.GoalState goal = state.getGoal(goalID);

        codePanel.clearRightCode();
        conclusion.removeAll();

        if(goal != null) {
            if(!goalID.equals(currentID)) {
                goalIDChooser.setSelectedItem(goalID);
            }

            if(goal.getCompletes() != null) {
                codePanel.setRightCode(goal.getCompletes().getProg(), !progressPanelShowing);
                if(progressPanelShowing) doMatchOnReshow = true;
            }

            if(goal.getConclusion() != null) {
                conclusion.add(new CoqDocText(goal.getConclusion().getTerm(), null));
            }
        }

        codePanel.updateVarInformation(goal);
    }

}
