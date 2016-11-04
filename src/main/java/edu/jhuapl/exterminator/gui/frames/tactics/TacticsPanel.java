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
package edu.jhuapl.exterminator.gui.frames.tactics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import edu.jhuapl.exterminator.grammar.coq.tactic.Tactic;
import edu.jhuapl.exterminator.gui.CoqDocText;
import edu.jhuapl.exterminator.gui.ExterminatorGUI;
import edu.jhuapl.exterminator.slmech.Tactics;

public class TacticsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private final ExterminatorGUI gui;
    
    private Tactics currentTactics;
    
    private final JScrollPane executedScroll, remainingScroll;

    private final JPanel executedPanel, nextPanel, remainingPanel;
    
    private final LinkedList<TacticPanel> executedTactics;
    
    private final LinkedList<TacticPanel> remainingTactics;
    
    private final JLabel promptToFinishLabel, finishedLabel;
    
    private TacticPanel nextTactic;
    
    public TacticsPanel(ExterminatorGUI gui) {
        super();
        
        this.gui = gui;
        this.currentTactics = null;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        this.executedPanel = new JPanel();
        executedScroll = new JScrollPane(executedPanel);
        add(executedScroll);
        
        executedPanel.setLayout(new BoxLayout(executedPanel, BoxLayout.Y_AXIS));
        
        this.nextPanel = new JPanel(new BorderLayout());
        add(nextPanel);
        
        nextPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(2, 2, 3, 3)));
        nextPanel.setBackground(Color.YELLOW);
        
        JLabel label = new JLabel("    >    ");
        nextPanel.add(label, BorderLayout.WEST);
        
        this.remainingPanel = new JPanel();
        remainingScroll = new JScrollPane(remainingPanel);
        add(remainingScroll);
        
        remainingPanel.setLayout(new BoxLayout(remainingPanel, BoxLayout.Y_AXIS));
        
        this.executedTactics = new LinkedList<>();
        this.remainingTactics = new LinkedList<>();
        this.promptToFinishLabel = new JLabel("Click the finish button to check proof!");
        this.finishedLabel = new JLabel("FINISHED!");
        this.nextTactic = null;
        
        nextPanel.setVisible(false);
    }
    
    public void clear() {
        currentTactics = null;
        
        executedPanel.removeAll();
        if(nextTactic != null) {
            nextPanel.remove(nextTactic);
        }
        remainingPanel.removeAll();
        executedTactics.clear();
        remainingTactics.clear();
        nextTactic = null;
        
        nextPanel.setVisible(false);
    }
    
    public void setTactics(Tactics tactics) {
        if(tactics == currentTactics) return;
        
        clear();
        if(tactics == null) return;
        
        currentTactics = tactics;
        if(tactics.getCurrentTacticsSize() == 0) return;
        
        List<Tactic> t = tactics.getCurrentTactics();
        
        nextPanel.setVisible(true);
        nextPanel.add(nextTactic = new TacticPanel(0, t.get(0)),
                BorderLayout.CENTER);
        
        for(int i = 1; i < t.size(); i++) {
            TacticPanel tp = new TacticPanel(i, t.get(i));
            remainingPanel.add(tp);
            remainingTactics.add(tp);
        }
    }
    
    public void updateTactics() {
        if(currentTactics == null) return;
        int diff = currentTactics.getNextTacticIndex() - executedTactics.size();
        if(diff == 0) return;
        else if(diff < 0) retreat(0 - diff);
        else advance(diff);
        
        // FIXME find way to auto-scroll executedScroll
        
//        revalidate();
//        if(executedTactics.size() > 0) {
////            System.out.println(executedTactics.getLast());
////            Rectangle rect = executedTactics.getLast().getBounds();
////            System.out.println(rect);
////            SwingUtilities.convertPoint(executedTactics.getLast(), rect.getLocation(), executedScroll);
////            System.out.println(rect);
////            executedScroll.scrollRectToVisible(rect);
//            executedTactics.getLast().scrollRectToVisible(executedTactics.getLast().getBounds());
//        }
//        revalidate();
    }
    
    public void finished() {
        nextPanel.remove(promptToFinishLabel);
        nextPanel.add(finishedLabel, BorderLayout.CENTER);
    }
    
    public void tacticWasInserted(int insertedIndex) {
        if(currentTactics == null || nextTactic == null) return;
        
        TacticPanel insertedTP = new TacticPanel(insertedIndex,
                currentTactics.getCurrentTacticAt(insertedIndex));
        
        if(insertedIndex == nextTactic.index) {
            // stick it at the end of executed and then reverse
            executedTactics.add(insertedTP);
            executedPanel.add(insertedTP);
            
            nextTactic.setIndex(nextTactic.getIndex() + 1);
            
            for(TacticPanel tp : remainingTactics) {
                tp.setIndex(tp.getIndex() + 1);
            }
            
            retreat(1);
            
        } else if(insertedIndex > executedTactics.size()) {
            int index = insertedIndex - executedTactics.size() - 1;
            remainingTactics.add(index, insertedTP);
            remainingPanel.add(insertedTP, index);
            
            for(int i = index + 1; i < remainingTactics.size(); i++) {
                TacticPanel tp = remainingTactics.get(i);
                tp.setIndex(tp.getIndex() + 1);
            }
        }
        
        revalidate();
    }
    
    public void tacticWasDeleted(int deletedIndex) {
        if(currentTactics == null || nextTactic == null) return;
        
        if(deletedIndex == nextTactic.index) {
            // update indices
            for(TacticPanel tactic : remainingTactics) {
                tactic.setIndex(tactic.getIndex() - 1);
            }
            
            TacticPanel tp = nextTactic;
            advance(1);
            // remove it from lists
            executedTactics.remove(tp);
            executedPanel.remove(tp);
            
        } else if(deletedIndex > executedTactics.size()) {
            // go through and delete it from remaining tactics
            int index = deletedIndex - executedTactics.size() - 1;
            TacticPanel tp = remainingTactics.remove(index);
            remainingPanel.remove(tp);
            for(int i = index; i < remainingTactics.size(); i++) {
                tp = remainingTactics.get(i);
                tp.setIndex(tp.getIndex() - 1);
            }
        }
        
        revalidate();
    }
    
    private void advance(int diff) {
        if(diff == 0 || nextTactic == null) return;
        
        // move current to executed
        nextPanel.remove(nextTactic);
        executedTactics.add(nextTactic);
        executedPanel.add(nextTactic);
        nextTactic.setExecuted(true);
        
        // get next
        nextTactic = remainingTactics.poll();
        if(nextTactic == null) {
            // we're almost done!
            nextPanel.add(promptToFinishLabel, BorderLayout.CENTER);
            
        } else {
            remainingPanel.remove(nextTactic);        
            nextPanel.add(nextTactic, BorderLayout.CENTER);
            
            // keep going
            advance(diff - 1);
        }
    }
    
    private void retreat(int diff) {        
        if(diff == 0 || executedTactics.size() == 0) return;
        
        if(nextTactic == null) {
            // we were at the end
            nextPanel.remove(promptToFinishLabel);
        } else {
            // move next down
            nextPanel.remove(nextTactic);
            remainingTactics.add(0, nextTactic);
            remainingPanel.add(nextTactic, 0);
            nextTactic = null;
        }
        
        // get last executed
        TacticPanel tp = executedTactics.pollLast();
        tp.setExecuted(false);
        executedPanel.remove(tp);
        nextTactic = tp;
        nextPanel.add(nextTactic, BorderLayout.CENTER);
        
        // keep going
        retreat(diff - 1);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    private class TacticPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private int index;
        
        private final Tactic tactic;
        
        private final JTextField line;
        
        private final CoqDocText text;
        
        private final Color originalBG;
        
        private final JPanel buttonPanel;
        
        public TacticPanel(int index, Tactic tactic) {
            super(new BorderLayout());
            this.index = index;
            this.tactic = tactic;
            this.line = new JTextField(3);
            line.setEditable(false);
            line.setText(Integer.toString(index + 1));
            add(line, BorderLayout.WEST);
            this.text = new CoqDocText(tactic, ".");
            text.setEditable(false);
            add(text, BorderLayout.CENTER);
            this.originalBG = text.getBackground();
            
            this.buttonPanel = new JPanel();
            add(buttonPanel, BorderLayout.EAST);
            
            JButton addAboveButton = new JButton("+ \u25b2");
            addAboveButton.setToolTipText("Add new tactic above this one");
            addAboveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    addAbove();
                }
            });
            
            JButton addBelowButton = new JButton("+ \u25bc");
            addBelowButton.setToolTipText("Add new tactic below this one");
            addBelowButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    addBelow();
                }
            });
            
            JButton deleteButton = new JButton("X");
            deleteButton.setToolTipText("Delete this tactic");
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    delete();
                }
            });
            
            JPanel temp = new JPanel();
            temp.setLayout(new BoxLayout(temp, BoxLayout.Y_AXIS));
            temp.add(addAboveButton);
            temp.add(addBelowButton);
            
            buttonPanel.add(temp);
            buttonPanel.add(deleteButton);
            
            Insets i = addAboveButton.getMargin();
            i.top = i.bottom = 0;
            addAboveButton.setMargin(i);
            addBelowButton.setMargin(i);
            
            buttonPanel.setBorder(BorderFactory.createLineBorder(
                    UIManager.getColor("TextField.shadow")));
        }
        
        public int getIndex() { return index; }
        
        public void setIndex(int newIndex) {
            this.index = newIndex;
            this.line.setText(Integer.toString(index + 1));
        }
        
        public void setExecuted(boolean hasExecuted) {
            text.setBackground(hasExecuted ? Color.GRAY : originalBG);
            buttonPanel.setVisible(!hasExecuted);
        }
        
        private void addAbove() {
            gui.newTacticAtIndex(index);
        }
        
        private void addBelow() {
            gui.newTacticAtIndex(index + 1);
        }
        
        private void delete() {
            gui.deleteTactic(index);
        }
        
        @Override
        public String toString() {
            return "{TacticsPanel index=" + index + " tactic=" + tactic + "}";
        }
        
    }
    
}
