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
package edu.jhuapl.exterminator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import edu.jhuapl.exterminator.coq.XMLUtils;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.gui.frames.ProgramStateFrame;
import edu.jhuapl.exterminator.slmech.ProgramState;

public class CoqDocText extends JTextPane {

    private static final long serialVersionUID = 1L;

    private static final StyleContext TEXT_STYLE;

    private static final Style LINE_NUMBER_STYLE, VARIABLE_STYLE;

    static {
        TEXT_STYLE = new StyleContext();

        // this is the default
        Style text = TEXT_STYLE.addStyle(CoqDoc.STYLE_TEXT, null);
        StyleConstants.setFontSize(text, 18);
        StyleConstants.setForeground(text, Color.BLACK);
        TEXT_STYLE.addStyle(StyleContext.DEFAULT_STYLE, text);

        Style identifier = TEXT_STYLE.addStyle(CoqDoc.STYLE_IDENTIFIER, text);
        StyleConstants.setBold(identifier, true);

        Style keywords = TEXT_STYLE.addStyle(CoqDoc.STYLE_KEYWORD, text);
        StyleConstants.setForeground(keywords, Color.GRAY);
        
        Style flagged = TEXT_STYLE.addStyle(CoqDoc.STYLE_FLAGGED, text);
        StyleConstants.setForeground(flagged, Color.RED);
        StyleConstants.setBold(flagged, true);


        LINE_NUMBER_STYLE = TEXT_STYLE.addStyle("line number", text);
        StyleConstants.setForeground(LINE_NUMBER_STYLE,
                UIManager.getColor("TextField.inactiveForeground"));

        VARIABLE_STYLE = TEXT_STYLE.addStyle("variable", identifier);
        StyleConstants.setForeground(VARIABLE_STYLE, Color.BLUE);
        StyleConstants.setUnderline(VARIABLE_STYLE, true);
    }

    ///////////////////////////////////////////////////////////////////////////

    private final CoqDocable term;

    private final List<CoqDoc.IdentifierLocation> allIdentifierLocations;

    private final Map<CoqDoc.IdentifierLocation, JFrame> variableLocationTooltips;

    private boolean showingXML;

    private final DefaultStyledDocument xmlDocument, textDocument;

    private final HoverListener hover;

    public CoqDocText(CoqDocable term, String optionalSuffix) {
        super();

        this.term = term;
        this.allIdentifierLocations = new ArrayList<>();
        this.variableLocationTooltips = new HashMap<>();
        this.showingXML = true;
        this.xmlDocument = CoqDoc.makeDocument(term, TEXT_STYLE, allIdentifierLocations, optionalSuffix);
        this.textDocument = new DefaultStyledDocument();

        setDocument(xmlDocument);
        setEditable(false);

        try {
            textDocument.insertString(textDocument.getLength(), term.fullText(),
                    TEXT_STYLE.getStyle(CoqDoc.STYLE_TEXT));

            int xmlLines = newlineCount(xmlDocument.getText(0, xmlDocument.getLength())),
                    textLines = newlineCount(textDocument.getText(0, textDocument.getLength()));
            int maxLines = Math.max(xmlLines, textLines);
            for(int line = xmlLines; line < maxLines; line++) {
                xmlDocument.insertString(xmlDocument.getLength(), "\n",
                        TEXT_STYLE.getStyle(CoqDoc.STYLE_WS));
            }
            for(int line = textLines; line < maxLines; line++) {
                textDocument.insertString(textDocument.getLength(), "\n",
                        TEXT_STYLE.getStyle(CoqDoc.STYLE_WS));
            }
        } catch(BadLocationException e) {
            throw new RuntimeException(e);
        }

        final JPopupMenu popup = makePopupMenu();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if(e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        addMouseMotionListener(hover = new HoverListener());
    }

    public void clearHover() {
        hover.clear();
    }

    private JPopupMenu makePopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem copyMenu = popup.add(new JMenuItem("Copy"));
        copyMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copy();
            }
        });

        JMenuItem switchMenu = popup.add(new JMenuItem("Switch View"));
        switchMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDocument(showingXML ? textDocument : xmlDocument);
                showingXML = !showingXML;
            }
        });

        JMenuItem printMenu = popup.add(new JMenuItem("Print Term"));
        printMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("=========================");
                System.out.println("Term full text:");
                System.out.println(term.fullText());
                System.out.println();
                System.out.println("Term toString():");
                System.out.println(term);
                System.out.println();
                System.out.println("Term coqdoc:");
                System.out.println(XMLUtils.nodeToString(
                        term.makeCoqDocTerm(CoqDoc.makeDocument())));
                System.out.println("=========================");
            }
        });

        return popup;
    }

    public CoqDocable getTerm() { return term; }

    public JTextPane makeLineWidget(int line) {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        setLineWidget(pane, line);

        return pane;
    }

    public void setLineWidget(JTextPane pane, int line) {
        StringBuilder sb = new StringBuilder();
        int numLines = newlineCount(getText()) + 1;
        for(int i = 0; i < numLines; i++) {
            if(i > 0) sb.append("\n");
            sb.append(line);
        }
        pane.setText(sb.toString());

        StyledDocument doc = pane.getStyledDocument();
        doc.setCharacterAttributes(0, doc.getLength(), LINE_NUMBER_STYLE, true);
    }

    public void updateVarInformation(ProgramState.GoalState goal) {
        hover.clear();
        for(JFrame frame : variableLocationTooltips.values()) {
            frame.setVisible(false);
            frame.dispose();
        }
        variableLocationTooltips.clear();

        if(goal == null) return;

        for(CoqDoc.IdentifierLocation loc : allIdentifierLocations) {
            ID foundVar = null;
            for(ID var : goal.getVars()) {
                if(loc.id.equals(var.getFullName())) {
                    foundVar = var;
                    break;
                }
            }

            if(foundVar != null) {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(3, 3, 3, 3),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createEtchedBorder(),
                                BorderFactory.createEmptyBorder(3, 3, 3, 3))));
                panel.add(new JLabel("Information for variable " + loc.id),
                        BorderLayout.NORTH);
                panel.add(ProgramStateFrame.makeTreeFor(goal, foundVar, true),
                        BorderLayout.CENTER);

                JFrame frame = new JFrame();
                frame.setContentPane(panel);
                frame.setUndecorated(true);
                frame.pack();
                frame.setResizable(false);
                frame.setAlwaysOnTop(true);
                frame.setAutoRequestFocus(false);
                frame.setVisible(false);

                variableLocationTooltips.put(loc, frame);

                xmlDocument.setCharacterAttributes(loc.offset, loc.length,
                        VARIABLE_STYLE, true);
            } else {
                xmlDocument.setCharacterAttributes(loc.offset, loc.length,
                        TEXT_STYLE.getStyle(CoqDoc.STYLE_IDENTIFIER), true);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private static int newlineCount(String str) {
        int total = 0;
        int i = str.indexOf('\n');
        while(i >= 0) {
            total++;
            i = str.indexOf('\n', i + 1);
        }
        return total;
    }

    ///////////////////////////////////////////////////////////////////////////

    private class HoverListener extends MouseInputAdapter {

        private Component last;

        public HoverListener() {
            this.last = null;
        }

        public void clear() {
            if(last != null) {
                last.setVisible(false);
                last = null;
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            clear();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if(variableLocationTooltips.size() == 0) {
                clear();
                return;
            }

            if(e.getY() <= 2 || e.getX() <= 2) {
                clear();
                return;
            }

            int offset = viewToModel(e.getPoint());
            if(offset < 0) {
                clear();
                return;
            }

            Component hover = getHoverVar(offset);
            if(hover == null) {
                clear();
                return;
            }


            if(hover != last) {
                if(last != null) last.setVisible(false);
                last = hover;

                // only need to pop up a new guy if it's different
                Point p = CoqDocText.this.getPopupLocation(e);
                if(p == null) {
                    p = e.getLocationOnScreen();
                } else {
                    SwingUtilities.convertPointToScreen(p, CoqDocText.this);
                }

                last.setLocation(p);
                last.setVisible(true);
            }
        }

        private Component getHoverVar(int offset) {
            for(CoqDoc.IdentifierLocation loc : variableLocationTooltips.keySet()) {
                if(offset >= loc.offset && offset < loc.offset + loc.length) {
                    return variableLocationTooltips.get(loc);
                }
            }
            return null;
        }

    }

}
