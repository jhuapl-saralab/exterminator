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
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import edu.jhuapl.exterminator.Environment;
import edu.jhuapl.exterminator.coq.CoqMessage;
import edu.jhuapl.exterminator.coq.CoqTop;
import edu.jhuapl.exterminator.coq.XMLUtils;
import edu.jhuapl.exterminator.coq.message.CoqAboutMessage;
import edu.jhuapl.exterminator.coq.message.CoqAddToLoadPathMessage;
import edu.jhuapl.exterminator.coq.message.CoqEVarsMessage;
import edu.jhuapl.exterminator.coq.message.CoqGoalMessage;
import edu.jhuapl.exterminator.coq.message.CoqHintsMessage;
import edu.jhuapl.exterminator.coq.message.CoqInterpMessage;
import edu.jhuapl.exterminator.coq.message.CoqRewindMessage;
import edu.jhuapl.exterminator.coq.message.CoqStatusMessage;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.Require;
import edu.jhuapl.exterminator.grammar.coq.sentence.Proof;
import edu.jhuapl.exterminator.grammar.coq.tactic.Tactic;
import edu.jhuapl.exterminator.gui.action.AboutAction;
import edu.jhuapl.exterminator.gui.action.AdjustFramesAction;
import edu.jhuapl.exterminator.gui.action.ApplyNextTacticAction;
import edu.jhuapl.exterminator.gui.action.FinishTacticsAction;
import edu.jhuapl.exterminator.gui.action.OpenAction;
import edu.jhuapl.exterminator.gui.action.QuitAction;
import edu.jhuapl.exterminator.gui.action.RewindAction;
import edu.jhuapl.exterminator.gui.action.ShowHideFrameAction;
import edu.jhuapl.exterminator.gui.frames.CodeFrame;
import edu.jhuapl.exterminator.gui.frames.EVarsFrame;
import edu.jhuapl.exterminator.gui.frames.ExterminatorFrame;
import edu.jhuapl.exterminator.gui.frames.GoalFrame;
import edu.jhuapl.exterminator.gui.frames.HintsFrame;
import edu.jhuapl.exterminator.gui.frames.ProgramStateFrame;
import edu.jhuapl.exterminator.gui.frames.StatusFrame;
import edu.jhuapl.exterminator.gui.frames.TacticsFrame;
import edu.jhuapl.exterminator.gui.frames.tactics.TacticCreator;
import edu.jhuapl.exterminator.slmech.Function;
import edu.jhuapl.exterminator.slmech.Program;
import edu.jhuapl.exterminator.slmech.ProgramState;
import edu.jhuapl.exterminator.slmech.Tactics;
import edu.jhuapl.exterminator.utils.Logger;

public class ExterminatorGUI extends JFrame implements Logger {

    static {
        System.setProperty("sun.java2d.opengl", "true");

        // set some defaults for swingx

        UIManager.put("TaskPaneContainer.useGradient", Boolean.FALSE);
        UIManager.put("TaskPaneContainer.background", new JPanel().getBackground());
    }

    private static final long serialVersionUID = 1L;

    public static final DefaultHighlightPainter EXEC_HIGHLIGHTER =
            new DefaultHighlightPainter(Color.YELLOW);

    private final CoqTop coq;

    private final List<ExterminatorEventListener> listeners;

    private final JDesktopPane desktop;

    private final StatusPanel statusBar;

    private final CodeFrame codeFrame;

    private final TacticsFrame tacticsFrame;

    private final ProgramStateFrame stateFrame;
    
    private final HintsFrame hintsFrame;

    private final StatusFrame statusFrame;

    private final ProgressPanel progress;

    private Program program;

    private ProgramState programState;

    public ExterminatorGUI() throws IOException {
        super("Exterminator");

        setLayout(new BorderLayout());

        this.coq = CoqTop.instance();

        this.listeners = new ArrayList<>();

        this.desktop = new JDesktopPane();
        add(desktop, BorderLayout.CENTER);

        this.statusBar = new StatusPanel();
        add(statusBar, BorderLayout.SOUTH);

        ///////////////////////////////////////////////////////////////////////

        OpenAction openAction = new OpenAction(this);
        ApplyNextTacticAction applyNextTacticAction = new ApplyNextTacticAction(this);
        applyNextTacticAction.setEnabled(false);
        RewindAction rewindAction = new RewindAction(this);
        rewindAction.setEnabled(false);
        FinishTacticsAction finishAction = new FinishTacticsAction(this);
        finishAction.setEnabled(false);

        // do this first for the logging
        statusFrame = add(new StatusFrame(this, true));

        codeFrame = add(new CodeFrame(this, openAction, true));

        tacticsFrame = add(new TacticsFrame(this, rewindAction,
                applyNextTacticAction, finishAction, true));

        stateFrame = add(new ProgramStateFrame(this, true));

        GoalFrame goalFrame = add(new GoalFrame(this, false));
        EVarsFrame evarsFrame = add(new EVarsFrame(this, false));
        hintsFrame = add(new HintsFrame(this, false));

        ///////////////////////////////////////////////////////////////////////

        {
            JMenuBar menuBar = new JMenuBar();
            setJMenuBar(menuBar);

            JMenu menu = new JMenu("File");
            menuBar.add(menu);

            menu.add(openAction);
            menu.add(new QuitAction(this));

            menu = new JMenu("View");
            menuBar.add(menu);

            menu.add(new AdjustFramesAction(this));
            menu.add(new JCheckBox(new ShowHideFrameAction(this, goalFrame, "Goal")));
            menu.add(new JCheckBox(new ShowHideFrameAction(this, evarsFrame, "EVars")));
            menu.add(new JCheckBox(new ShowHideFrameAction(this, hintsFrame, "Hints")));

            menu = new JMenu("Tactics");
            menuBar.add(menu);

            menu.add(applyNextTacticAction);
            menu.add(rewindAction);
            menu.add(finishAction);

            menu = new JMenu("Help");
            menuBar.add(menu);

            menu.add(new AboutAction(this));
        }

        this.progress = new ProgressPanel();
        setGlassPane(progress);
        progress.setVisible(false);

        ///////////////////////////////////////////////////////////////////////

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(Environment.Icon.LOGO_SMALL.getImageIcon().getImage());

        int spacing = 100;

        Dimension d = Environment.getScreenSize();
        d.width -= 4 * spacing;
        d.height -= 2 * spacing;

        setPreferredSize(d);
        setLocation(spacing, spacing);
        pack();

        reloadStatus();
        adjustFrames();
    }

    public void adjustFrames() {
        int spacing = 5;

        statusFrame.setSize(desktop.getWidth() - (2 * spacing),
                statusFrame.getHeight() - (2 * spacing));
        statusFrame.setLocation(spacing,
                desktop.getHeight() - statusFrame.getHeight() - spacing);

        codeFrame.setLocation(spacing, spacing);
        codeFrame.setSize((desktop.getWidth() * 3/5) - (2 * spacing),
                desktop.getHeight() - statusFrame.getHeight() - (4 * spacing));

        tacticsFrame.setLocation((desktop.getWidth() * 3/5) + 1 + spacing, spacing);
        tacticsFrame.setSize((desktop.getWidth() * 2/5) - (2 * spacing),
                ((desktop.getHeight() - statusFrame.getHeight()) / 2) - (3 * spacing));

        stateFrame.setLocation((desktop.getWidth() * 3/5) + 1 + spacing,
                ((desktop.getHeight() - statusFrame.getHeight()) / 2) + 1);
        stateFrame.setSize(tacticsFrame.getSize());
    }

    private <T extends ExterminatorFrame> T add(T frame) {
        desktop.add(frame);
        return frame;
    }

    public void addListener(ExterminatorEventListener listener) {
        if(listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void dispose() {
        System.exit(0);
    }

    public void setStatusLabel(String text) {
        statusBar.setStatusLabel(text);
    }

    @Override
    public void log(Serializable message) {
        statusFrame.log(message);
    }

    /*
	public OpenAction getActionOpen() { return actionOpen; }

	public ApplyNextTacticAction getActionApplyNextTactic() { return actionApplyNextTactic; }

	public RewindAction getActionRewind() { return actionRewind; }
     */

    //public Program getProgram() { return program; }

    //public ProgramState getProgramState() { return programState; }

    protected void runLongOperation(String operationName, final Runnable runnable) {
        progress.init(operationName);
        progress.setVisible(true);
        setEnabled(false);

        for(ExterminatorEventListener listener : listeners) {
            listener.progressPanelShown();
        }

        Thread thread = Executors.defaultThreadFactory().newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                    progress.setVisible(false);
                    setEnabled(true);
                    for(ExterminatorEventListener listener : listeners) {
                        listener.progressPanelHidden();
                    }
                }
            }
        });
        thread.start();
    }

    public void progressSetMessage(String message) {
        progress.setMessage(message);
    }

    public void progressSetSubMessage(String message) {
        progress.setSubMessage(message);
    }

    public void load(final Path file) {
        runLongOperation("Loading " + file, new Runnable() {
            @Override
            public void run() {
                Cursor c = ExterminatorGUI.this.getCursor();
                ExterminatorGUI.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                boolean needToRestart = program != null;

                progressSetMessage("Parsing Coq file...");
                try {
                    program = new Program(file, statusFrame);
                    programState = new ProgramState(program);
                } catch(IOException | IllegalArgumentException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ExterminatorGUI.this,
                            "Loading failed.  See stack trace.",
                            "Load Error", JOptionPane.ERROR_MESSAGE);
                    ExterminatorGUI.this.setCursor(c);
                    return;
                }

                if(needToRestart) {
                    progressSetMessage("Restarting Coq...");
                    try {
                        coq.terminateAndRestart();
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                // we need to add that directory to the loadpath
                CoqAddToLoadPathMessage msg = sentCommandGotMessage(
                        coq.addToLoadPath(file.getParent()));
                log(msg.getCommand().getCode());

                progressSetMessage("Updating GUI...");

                // update listeners
                for(ExterminatorEventListener listener : listeners) {
                    listener.programLoaded(program);
                }

                try {
                    sendPreamble();
                } catch(RuntimeException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ExterminatorGUI.this,
                            "Sending preamble failed.  See stack trace.",
                            "Interp Error", JOptionPane.ERROR_MESSAGE);
                }

                ExterminatorGUI.this.setCursor(c);
            }
        });
    }

    private void sendPreamble() {
        // send requires
        progressSetMessage("Sending preamble (requires)...");
        boolean success;
        for(Require code : program.getRequires()) {
            success = interp(code, false).statusIsGood();
            if(!success) return;
        }

        // TODO execute other functions

        // send program
        progressSetMessage("Sending preamble (main function)...");
        Function main = program.getMainFunction();
        success = interp(main.getAssertion(), false).statusIsGood();
        if(!success) return;

        // send proof header
        progressSetMessage("Sending preamble (proof header)...");
        success = interp(main.getProof().getHeader(), true).statusIsGood();
        if(!success) return;

        log("sent preamble (assertion + begin proof)");
    }

    public void goalSelected(String goalID) {
        if(programState == null) return;

        for(ExterminatorEventListener listener : listeners) {
            listener.goalSelected(programState, goalID);
        }
    }

    public CoqInterpMessage interp(CoqToken coq, boolean reloadStatus) {
        return interp(coq.fullText(), reloadStatus);
    }

    public CoqInterpMessage interp(String code, boolean reloadStatus) {
        progressSetSubMessage("Interpreting Coq command...");
        CoqInterpMessage message = sentCommandGotMessage(coq.interp(code));

        if(reloadStatus) {
            reloadStatus();
        }

        return message;
    }

    public void applyNextTactic() {
        if(programState == null) return;

        runLongOperation("Applying Tactic", new Runnable() {
            @Override
            public void run() {
                Tactic tactic = programState.getTactics().getNextTactic();
                progressSetMessage("Interpreting tactic...");
                CoqInterpMessage message = interp(tactic, true);
                if(message.statusIsGood()) {
                    log("applied next tactic (" + tactic.fullText() + ")");
                    programState.getTactics().incrementNextTacticIndex(1);
                    progressSetMessage("Updating GUI...");
                    for(ExterminatorEventListener listener : listeners) {
                        int index = programState.getTactics().getNextTacticIndex(),
                                size = programState.getTactics().getCurrentTacticsSize();
                        listener.tacticSuccessfullyApplied(tactic, index, size);
                    }
                }
            }
        });
    }

    public void rewind() {
        if(programState == null) return;
        
        runLongOperation("Rewinding Last Tactic", new Runnable() {
            @Override
            public void run() {
                CoqRewindMessage message = sentCommandGotMessage(coq.rewind(1));

                if(message.statusIsGood()) {
                    programState.getTactics().decrementNextTacticIndex(1 + message.getExtraBacktracking());

                    int newIndex = programState.getTactics().getNextTacticIndex(),
                            size = programState.getTactics().getCurrentTacticsSize();

                    for(ExterminatorEventListener listener : listeners) {
                        listener.tacticSuccessfulyRewound(newIndex, size);
                    }

                    reloadStatus();
                }
            }
        });
    }
    
    public void finishTactics() {
        if(programState == null) return;
        
        runLongOperation("Finishing Tactics", new Runnable() {
            @Override
            public void run() {
                Proof proof = programState.getProgram().getMainFunction().getProof();
                String end = proof.getFooter();
                CoqInterpMessage message = interp(end, true);
                if(message.statusIsGood()) {
                    log("applied finisher (" + end + ")");

                    for(ExterminatorEventListener listener : listeners) {
                        listener.proofSuccessfullyFinished();
                    }
                    
                    JOptionPane.showMessageDialog(ExterminatorGUI.this,
                            "Proof successfully finished!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);


                } else {
                    JOptionPane.showMessageDialog(ExterminatorGUI.this,
                            "Proof was not successful:\n" + message.getErrorMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    public void deleteTactic(int index) {
        if(programState == null) return;
        
        Tactics tactics = programState.getTactics();
        if(index < tactics.getNextTacticIndex()) return;
        if(index >= tactics.getCurrentTacticsSize()) return;
        
        tactics.deleteTacticAt(index);
        int newIndex = tactics.getNextTacticIndex(),
                size = tactics.getCurrentTacticsSize();
        for(ExterminatorEventListener listener : listeners) {
            listener.tacticSuccessfullyDeleted(index, newIndex, size);
        }
    }
    
    public void newTacticAtIndex(int index) {
        if(programState == null) return;
        
        Tactics tactics = programState.getTactics();
        if(index < tactics.getNextTacticIndex()) return;
        if(index > tactics.getCurrentTacticsSize()) return;
        
        Tactic tactic = TacticCreator.getTacticFromUser(this, hintsFrame.getCurrentHints());
        if(tactic == null) return;
        tactics.insertTacticAt(index, tactic);
        int newIndex = tactics.getNextTacticIndex(),
                size = tactics.getCurrentTacticsSize();
        for(ExterminatorEventListener listener : listeners) {
            listener.tacticSuccessfullyInserted(index, newIndex, size);
        }
    }

    public CoqAboutMessage about() {
        return sentCommandGotMessage(coq.about());
    }

    public CoqStatusMessage status() {
        return sentCommandGotMessage(coq.status());
    }

    public CoqGoalMessage goal() {
        return sentCommandGotMessage(coq.goal());
    }

    public CoqEVarsMessage evars() {
        return sentCommandGotMessage(coq.evars());
    }

    public CoqHintsMessage hints() {
        return sentCommandGotMessage(coq.hints());
    }

    private <T extends CoqMessage> T sentCommandGotMessage(T message) {
        statusFrame.appendRaw("=======================");
        statusFrame.appendRaw("SENDING");
        statusFrame.appendRaw(XMLUtils.docToString(message.getCommand().getDoc()));
        statusFrame.appendRaw("-----------------------");
        statusFrame.appendRaw("RESPONSE");
        statusFrame.appendRaw(XMLUtils.docToString(message.getDoc()));
        statusFrame.appendRaw("=======================");

        if(!message.statusIsGood()) {
            JOptionPane.showMessageDialog(this,
                    message.getErrorMessage().trim(),
                    "Action Failed", JOptionPane.ERROR_MESSAGE);
        }

        return message;
    }

    public void reloadStatus() {
        if(program == null) return;

        progressSetSubMessage("Updating goal information...");
        CoqGoalMessage goal = goal();

        progressSetSubMessage("Updating program state...");
        programState.update(goal);

        // update listeners
        progressSetSubMessage("Updating GUI...");
        for(ExterminatorEventListener listener : listeners) {
            listener.programStateUpdated(programState);
        }

        goalSelected(stateFrame.getCurrentlySelectedGoal());
    }

    ///////////////////////////////////////////////////////////////////////////

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ExterminatorGUI gui;
                try {
                    gui = new ExterminatorGUI();
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
                gui.setVisible(true);
                if(args != null && args.length > 0) {
                    gui.load(Paths.get(args[0]));
                }
            }
        });
    }

}
