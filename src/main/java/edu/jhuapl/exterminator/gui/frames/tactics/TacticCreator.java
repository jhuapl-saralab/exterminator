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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.jhuapl.exterminator.coq.message.CoqHintsMessage;
import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.tactic.Tactic;
import edu.jhuapl.exterminator.gui.CoqDocText;
import edu.jhuapl.exterminator.gui.ExterminatorGUI;
import edu.jhuapl.exterminator.gui.GUIUtils;

public class TacticCreator extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JRadioButton coqRadio, expertRadio, customRadio;
    
    private final HintModel coqHints, expertHints;
    
    private final JComboBox<String> coqHintsChooser, expertHintsChooser;
    
    private final JTextField custom;
    
    private final JLabel selectedDisplay;
    
    private final JTextArea selectedDescription;
    
    private final JPanel selectedTacticPane;
    
    private final JButton okayButton;
    
    private Tactic tactic;
    
    public TacticCreator(ExterminatorGUI gui, CoqHintsMessage hintsMsg) {
        super(gui, "New Tactic", true);
        
        JPanel content = new JPanel(new BorderLayout());
        setContentPane(content);
        
        content.add(GUIUtils.centeredPanelWith(
                new JLabel("Select a suggested tactic, or enter your own:")),
                BorderLayout.NORTH);
        
        JPanel center = new JPanel();
        content.add(center, BorderLayout.CENTER);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        ButtonGroup bg = new ButtonGroup();

        coqRadio = new JRadioButton("Coq Hints:");
        coqRadio.setSelected(true);
        bg.add(coqRadio);
        
        center.add(GUIUtils.leftAlignedPanelWith(coqRadio));
        
        List<? extends CoqHintsMessage.Hint> hints = hintsMsg.getGoals();
        this.coqHints = new HintModel(hints.size());
        this.coqHintsChooser = new JComboBox<>(coqHints);
        
        for(int i = 0; i < hints.size(); i++) {
            CoqHintsMessage.Hint hint = hints.get(i);
            coqHints.set(i, hint.getName(), hint.getCode());
        }
        
        coqHintsChooser.setSelectedIndex(0);
        
        center.add(GUIUtils.leftAlignedPanelWith(new JLabel("       "), coqHintsChooser));
        
        expertRadio = new JRadioButton("Expert Hints:");
        expertRadio.setSelected(false);
        bg.add(expertRadio);
        
        center.add(GUIUtils.leftAlignedPanelWith(expertRadio));
        
        this.expertHints = new HintModel(0);
        this.expertHintsChooser = new JComboBox<>(expertHints);
        
        center.add(GUIUtils.leftAlignedPanelWith(new JLabel("       "), expertHintsChooser));
        
        customRadio = new JRadioButton("Custom:");
        customRadio.setSelected(false);
        bg.add(customRadio);
        
        center.add(GUIUtils.leftAlignedPanelWith(customRadio));
        
        custom = new JTextField();
        Dimension d = custom.getMaximumSize();
        d.height = custom.getPreferredSize().height;
        custom.setMaximumSize(d); // don't let it get taller
        
        center.add(GUIUtils.leftAlignedPanelWith(new JLabel("       "), custom));

//        
//        left.add(Box.createVerticalStrut(10));
//        left.add(new JLabel("Custom:"));
//        
//        temp = new JPanel();
//        temp.setLayout(new BoxLayout(temp, BoxLayout.X_AXIS));
//        left.add(temp);
//        
//        this.custom = new JTextField();
//        temp.add(custom);
//        temp.add(Box.createHorizontalGlue());
//        
//        button = new JButton(" >> ");
//        temp.add(button);
//        
//        button.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent event) {
//                selectFromCustom();
//            }
//        });
//        
//        JPanel center = new JPanel();
//        content.add(center, BorderLayout.CENTER);
//        
//        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
//        center.add(Box.createVerticalGlue());
        
        JButton selectButton = new JButton("Select Tactic");
        center.add(GUIUtils.centeredPanelWith(selectButton));
        
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                select();
            }
        });
//        
        center.add(selectedDisplay = new JLabel("Selected Tactic Name Will Appear Here"));
        center.add(selectedDescription = new JTextArea("Selected tactic description will appear here."));
        
        selectedDescription.setRows(7);
//        
//        selectedDescription.setEditable(false);
//        
        center.add(selectedTacticPane = new JPanel(new BorderLayout()));
        
        selectedTacticPane.add(Box.createVerticalStrut(100), BorderLayout.CENTER);
//        
//        center.add(Box.createVerticalGlue());
        
        JPanel bottom = new JPanel();
        content.add(bottom, BorderLayout.SOUTH);
        
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        
        final String link = "https://coq.inria.fr/distrib/current/refman/tactic-index.html";
        JButton refButton = new JButton("Browse to Online Reference Manual");
        bottom.add(refButton);
        
        refButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(URI.create(link));
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        
        bottom.add(Box.createHorizontalStrut(10));
        bottom.add(Box.createHorizontalGlue());
        
        okayButton = new JButton("OK");
        bottom.add(okayButton);
        
        okayButton.setEnabled(false);
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        bottom.add(Box.createHorizontalStrut(10));
        
        JButton cancelButton = new JButton("Cancel");
        bottom.add(cancelButton);
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tactic = null;
                dispose();
            }
        });
        
        pack();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(gui);
    }
    
    private void select() {
        if(coqRadio.isSelected()) {
            selectFromCoqHints();
        } else if(expertRadio.isSelected()) {
            selectFromExpertHints();
        } else if(customRadio.isSelected()) {
            selectFromCustom();
        }
    }
    
    private void selectFromCoqHints() {
        int i = coqHintsChooser.getSelectedIndex();
        if(i >= 0) {
            selectTactic(coqHints.display[i], coqHints.code[i]);
        }
    }
    
    private void selectFromExpertHints() {
        int i = expertHintsChooser.getSelectedIndex();
        if(i >= 0) {
            selectTactic(expertHints.display[i], expertHints.code[i]);
        }
    }
    
    private void selectFromCustom() {
        String str = custom.getText().trim();
        if(str.isEmpty()) return;
        if(!str.endsWith(".")) {
            str += ".";
        }
        selectTactic("Custom Tactic", str);
    }
    
    private void selectTactic(String display, String code) {
        this.tactic = null;
        
        if(code.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tactic is empty, try again.", "Tactic Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            this.tactic = CoqFTParser.parseTactic(code, true);
        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(TacticCreator.this,
                    "Creating tactic failed.  Probably a parse error.  See stack trace.",
                    "Tactic Error", JOptionPane.ERROR_MESSAGE);
            okayButton.setEnabled(false);
            return;
        }
        
        selectedDisplay.setText(display);
        selectedDescription.setText(tactic.getDescription());
        selectedTacticPane.removeAll();
        selectedTacticPane.add(new CoqDocText(tactic, "."), BorderLayout.CENTER);
        
        okayButton.setEnabled(true);
    }
    
    private class HintModel extends DefaultComboBoxModel<String> {

        private static final long serialVersionUID = 1L;
        
        private final String[] display, code;
        
        public HintModel(int size) {
            this.display = new String[size];
            this.code = new String[size];
        }
        
        public void set(int index, String display, String code) {
            this.display[index] = display;
            this.code[index] = code;
        }

        @Override
        public String getElementAt(int index) {
            return display[index];
        }

        @Override
        public int getSize() {
            return display.length;
        }
        
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    public static Tactic getTacticFromUser(ExterminatorGUI gui, CoqHintsMessage hints) {
        TacticCreator creator = new TacticCreator(gui, hints);
        creator.setVisible(true);
        // block until the user closes
        return creator.tactic;
    }
    
}
