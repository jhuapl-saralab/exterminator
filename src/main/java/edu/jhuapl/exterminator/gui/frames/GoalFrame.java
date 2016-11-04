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

import edu.jhuapl.exterminator.coq.message.CoqGoalMessage;
import edu.jhuapl.exterminator.grammar.coq.term.Qualid;
import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.grammar.coq.term.TypeCast;
import edu.jhuapl.exterminator.gui.ExterminatorEventListener;
import edu.jhuapl.exterminator.gui.ExterminatorGUI;
import edu.jhuapl.exterminator.slmech.ProgramState;

public class GoalFrame extends ExterminatorFrame {
	
	private static final long serialVersionUID = 1L;
	
	private final JTextArea fgText, bgText;
	
	public GoalFrame(ExterminatorGUI parent, boolean show) {
		super(parent, "Goal", show);
		
		JTabbedPane panel = new JTabbedPane();
		setContentPane(panel);
		
		panel.addTab("Foreground", new JScrollPane(fgText = new JTextArea()));
		panel.addTab("Background", new JScrollPane(bgText = new JTextArea()));
		
		///////////////////////////////////////////////////////////////////////
		
		parent.addListener(new ExterminatorEventListener.Adapter() {
			@Override
			public void programStateUpdated(ProgramState state) {
				update(fgText, state, state.getFGGoalIDs());
				update(bgText, state, state.getBGGoalIDs());
			}
		});
		
		///////////////////////////////////////////////////////////////////////
		
		setPreferredSize(new Dimension(400, 300));
		pack();
	}
	
	private void update(JTextArea text, ProgramState state, List<String> goalIDs) {
		text.setText("");
		
		if(state != null) {
			for(String goalID : goalIDs) {
				CoqGoalMessage.Goal goal = state.getGoal(goalID).getOriginalGoal();
				text.append("=======================\n");
				text.append("ID: ");
				text.append(goal.getID());
				text.append("\n\n");
				
				text.append("Hypotheses\n");
				int i=0;
				for(Term t : goal.getHypothesisTerms()){
					String h = t.toString();
					if(t instanceof TypeCast){
						TypeCast tt = (TypeCast)t;
						Term typ     = tt.getType();
						if(typ instanceof Qualid && ((Qualid)typ).getFullName().equals("var")){
							h = "Local: "+tt.getTerm();
						}else if((typ instanceof Qualid) && ((Qualid)typ).getFullName().equals("addr")){
							h = "Address: "+tt.getTerm();
						}
					}
					text.append("\t["+i+"]: "+h+"\n");
					i++;
				}
//				text.append(goal.getHypotheses().toString());
				text.append("\n\n");
				
				text.append("Conclusion\n");
				text.append(goal.getConclusion().fullText());
				text.append("=======================\n\n");
			}
		}
		
		text.setCaretPosition(0);
	}
	
}
