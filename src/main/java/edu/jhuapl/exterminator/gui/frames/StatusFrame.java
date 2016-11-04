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
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import edu.jhuapl.exterminator.coq.message.CoqAboutMessage;
import edu.jhuapl.exterminator.coq.message.CoqStatusMessage;
import edu.jhuapl.exterminator.gui.ExterminatorEventListener;
import edu.jhuapl.exterminator.gui.ExterminatorGUI;
import edu.jhuapl.exterminator.slmech.ProgramState;
import edu.jhuapl.exterminator.utils.Logger;

public class StatusFrame extends ExterminatorFrame implements Logger {

	private static final long serialVersionUID = 1L;
	
	private final JTextArea log, raw;
	
	private final StatusPanel status;
	
	public StatusFrame(final ExterminatorGUI parent, boolean show) {
		super(parent, "Status", show);
		
		JTabbedPane pane = new JTabbedPane();
		setContentPane(pane);
		
		pane.addTab("Log", new JScrollPane(log = new JTextArea()));
		log.setEditable(false);
		
		pane.addTab("Raw CoqTop", new JScrollPane(raw = new JTextArea()));
		raw.setEditable(false);
		
		pane.addTab("Status", status = new StatusPanel());
		
		///////////////////////////////////////////////////////////////////////
		
		parent.addListener(new ExterminatorEventListener.Adapter() {
			@Override
			public void programStateUpdated(ProgramState state) {
				status.update(parent.status());
			}
		});
		
		///////////////////////////////////////////////////////////////////////
		
		setPreferredSize(new Dimension(500, 180));
		pack();
	}

	@Override
	public void log(Serializable message) {
		log.append(message.toString());
		log.append("\n");
		log.setCaretPosition(log.getDocument().getLength());
	}
	
	public void appendRaw(Serializable message) {
		raw.append(message.toString());
		raw.append("\n");
		raw.setCaretPosition(raw.getDocument().getLength());
	}
	
	public static JPanel aboutPanel(CoqAboutMessage about) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		panel.add(new JLabel("<html><b>ABOUT COQTOP</b></html>"));
		panel.add(new JLabel("Version: " + about.getVersion()));
		panel.add(new JLabel("Protocol: " + about.getProtocol()));
		panel.add(new JLabel("Release: " + about.getRelease()));
		panel.add(new JLabel("Compile: " + about.getCompile()));
		
		return panel;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private class StatusPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		
		private final JTextArea text;

		public StatusPanel() {
			super();
			
			setLayout(new BorderLayout());
			
			add(new JScrollPane(text = new JTextArea()), BorderLayout.CENTER);
			text.setEditable(false);
		}
		
		public void update(CoqStatusMessage message) {
			text.setText("");
			
			if(!message.statusIsGood()) {
				text.setText("MESSAGE IS INVALID");
				return;
			}
			
			text.append("Path\n");
			text.append(message.getPath().toString());
			text.append("\n\n");
			
			text.append("Proof Name\n");
			text.append(message.getProofName());
			text.append("\n\n");
			
			text.append("All Proofs\n");
			text.append(message.getAllProofs().toString());
			text.append("\n\n");
			
			text.append("State Num\n");
			text.append(Integer.toString(message.getStateNum()));
			text.append("\n\n");
			
			text.append("Proof Num\n");
			text.append(Integer.toString(message.getProofNum()));
			text.append("\n\n");
			
			text.setCaretPosition(0);
		}
		
	}

}
