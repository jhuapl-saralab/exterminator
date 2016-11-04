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

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.jhuapl.exterminator.coq.message.CoqEVarsMessage;
import edu.jhuapl.exterminator.gui.ExterminatorEventListener;
import edu.jhuapl.exterminator.gui.ExterminatorGUI;
import edu.jhuapl.exterminator.slmech.ProgramState;

public class EVarsFrame extends ExterminatorFrame {

	private static final long serialVersionUID = 1L;
	
	private final JTextArea text;
	
	public EVarsFrame(final ExterminatorGUI parent, boolean show) {
		super(parent, "EVars", show);
		
		setLayout(new BorderLayout());
		
		add(new JScrollPane(text = new JTextArea()), BorderLayout.CENTER);
		text.setEditable(false);
		
		///////////////////////////////////////////////////////////////////////
		
		parent.addListener(new ExterminatorEventListener.Adapter() {
			@Override
			public void programStateUpdated(ProgramState state) {
				CoqEVarsMessage message = parent.evars();
				
				text.setText("");
				
				for(CoqEVarsMessage.EVar evar : message.getEVars()) {
					text.append("=============================\n");
					text.append(evar.getEVarString() + "\n");
					text.append("=============================\n\n");
				}
				
				text.setCaretPosition(0);
			}
		});
		
		///////////////////////////////////////////////////////////////////////
		
		setPreferredSize(new Dimension(400, 300));
		pack();
	}
	
}
