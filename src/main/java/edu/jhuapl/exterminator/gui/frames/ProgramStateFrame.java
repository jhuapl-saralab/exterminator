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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import edu.jhuapl.exterminator.Environment;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.grammar.coq.term.TypeCast;
import edu.jhuapl.exterminator.grammar.coq.term.expression.EqualsExpression;
import edu.jhuapl.exterminator.gui.ExterminatorEventListener;
import edu.jhuapl.exterminator.gui.ExterminatorGUI;
import edu.jhuapl.exterminator.gui.TreeIconRenderer;
import edu.jhuapl.exterminator.slmech.ProgramState;

public class ProgramStateFrame extends ExterminatorFrame {

	private static final long serialVersionUID = 1L;
	
	static {
		TreeIconRenderer.registerIcon(GoalRootNode.class,
				Environment.Icon.GOAL_SMALL.getImageIcon());
		TreeIconRenderer.registerIcon(VarNode.class,
				Environment.Icon.VARIABLE_SMALL.getImageIcon());
		TreeIconRenderer.registerIcon(StoreboundVarNode.class,
				Environment.Icon.DATABASE_SMALL.getImageIcon());
		TreeIconRenderer.registerIcon(EqualityNode.class,
				Environment.Icon.EQUALS_SMALL.getImageIcon());
		TreeIconRenderer.registerIcon(TokenNode.class,
				Environment.Icon.QUESTION_SMALL.getImageIcon());
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private final JTabbedPane tabs;
	
	private final Map<String, Integer> goalIDToTabIndices;
	
	private boolean goalFlag;
	
	private int lastIndex, lastIndexInGoalTab;

	public ProgramStateFrame(final ExterminatorGUI parent, boolean show) {
		super(parent, "Program State", show);
		
		setLayout(new BorderLayout());
		
		this.tabs = new JTabbedPane();
		add(tabs, BorderLayout.CENTER);
		
		this.goalIDToTabIndices = new HashMap<>();
		this.goalFlag = false;
		this.lastIndex = this.lastIndexInGoalTab = 0;
		
		tabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(goalFlag) return;
				
				if(tabs.getSelectedIndex() != lastIndex) {
					if(tabs.getTabComponentAt(lastIndex) != null) {
						int goalIndex = ((GoalTab)tabs.getTabComponentAt(lastIndex)).getSelectedIndex();
						((GoalTab)tabs.getSelectedComponent()).setSelectedIndex(goalIndex);
					}
					lastIndex = tabs.getSelectedIndex();
					parent.goalSelected(getCurrentlySelectedGoal());
				}
			}
		});
		
		///////////////////////////////////////////////////////////////////////
		
		parent.addListener(new ExterminatorEventListener.Adapter() {
			@Override
			public void programStateUpdated(ProgramState state) {
				goalFlag = true;
				tabs.removeAll();
				goalIDToTabIndices.clear();
				
				if(state == null) return;

				for(String goalID : state.getFGGoalIDs()) {
					goalIDToTabIndices.put(goalID, tabs.getTabCount());
					tabs.addTab("goal : " + goalID, new GoalTab(state, goalID));
				}
				
				if(lastIndex < tabs.getTabCount()) {
					tabs.setSelectedIndex(lastIndex);
					JTabbedPane inner = (GoalTab)tabs.getSelectedComponent();
					if(lastIndexInGoalTab < inner.getTabCount()) {
						inner.setSelectedIndex(lastIndexInGoalTab);
					}
				}
				goalFlag = false;
			}
			
			@Override
			public void goalSelected(ProgramState state, String goalID) {
				if(goalID != null) {
					goalFlag = true;
					tabs.setSelectedIndex(goalIDToTabIndices.get(goalID));
					goalFlag = false;
				}
			}
		});
		
		///////////////////////////////////////////////////////////////////////
		
		setPreferredSize(new Dimension(600, 700));
		
		pack();
	}
	
	public String getCurrentlySelectedGoal() {
		if(tabs.getTabCount() == 0) {
			return null;
		} else {
			return ((GoalTab)tabs.getSelectedComponent()).getGoalID();
		}
	}
	
	private class GoalTab extends JTabbedPane {

		private static final long serialVersionUID = 1L;
		
		private final String goalID;

		public GoalTab(ProgramState state, String goalID) {
			super();
			setTabPlacement(JTabbedPane.LEFT);
			
			this.goalID = goalID;
			
			// variables
			addTab("<html>Variables</html>", new VariablesPanel(state, goalID));

			JTextArea text = new JTextArea(state.getGoal(goalID).getConclusion().getTerm().fullText());
			text.setEditable(false);
			text.setCaretPosition(0);
			addTab("<html>Conclusion</html>", new JScrollPane(text));
			
			text = new JTextArea(state.getGoal(goalID).getCompletes() == null ? "" : state.getGoal(goalID).getCompletes().getTerm().fullText());
			text.setEditable(false);
			text.setCaretPosition(0);
			addTab("<html>Completes</html>", new JScrollPane(text));
				
			text = new JTextArea();
			text.setEditable(false);
			for(ID id : state.getGoal(goalID).getVals()) {
				text.append(id.getFullName());
				text.append("\n");
			}
			text.setCaretPosition(0);
			addTab("<html>Vals</html>", new JScrollPane(text));
			
			text = new JTextArea();
			text.setEditable(false);
			for(ID id : state.getGoal(goalID).getAddresses()) {
				text.append(id.getFullName());
				text.append("\n");
			}
			text.setCaretPosition(0);
			addTab("<html>Addresses</html>", new JScrollPane(text));
			
			text = new JTextArea();
			text.setEditable(false);
			for(TypeCast term : state.getGoal(goalID).getTypedTerms()) {
				text.append(term.fullText());
				text.append("\n");
				text.append(term.toString());
				text.append("\n\n");
			}
			text.setCaretPosition(0);
			addTab("<html>Typed<br />Terms</html>", new JScrollPane(text));
			
			text = new JTextArea();
			text.setEditable(false);
			for(Term term : state.getGoal(goalID).getOtherTerms()) {
				text.append(term.fullText());
				text.append("\n");
				text.append(term.toString());
				text.append("\n\n");
			}
			text.setCaretPosition(0);
			addTab("<html>Other<br />Terms</html>", new JScrollPane(text));
			
			addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					lastIndexInGoalTab = getSelectedIndex();
				}
			});
		}
		
		public String getGoalID() { return goalID; }
		
	}
	
	private class VariablesPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		
		private final JTree tree;
		
		private final TreeModel model;
		
		public VariablesPanel(ProgramState state, String goalID) {
			super(new BorderLayout());
			
			this.model = new DefaultTreeModel(new GoalRootNode(state, goalID));
			this.tree = makeTreeFor(model, true);
			
			tree.setCellRenderer(new TreeIconRenderer(false));
			
			add(new JScrollPane(tree), BorderLayout.CENTER);
		}
		
	}
	
	public static JTree makeTreeFor(TreeModel model, boolean expandRows) {
		JTree tree = new JTree(model);
		tree.setCellRenderer(new TreeIconRenderer(false));
		
		if(expandRows) {
			for(int row = 0; row < tree.getRowCount(); row++) {
				tree.expandRow(row);
			}
		}
		
		return tree;
	}
	
	public static JTree makeTreeFor(TreeNode root, boolean expandRows) {
		return makeTreeFor(new DefaultTreeModel(root), expandRows);
	}
	
	public static JTree makeTreeFor(ProgramState.GoalState state, ID var, boolean expandRows) {
		return makeTreeFor(new DefaultTreeModel(new VarNode(state, var)), expandRows);
	}
	
	private static class GoalRootNode implements TreeNode {
		
		private final String goalID;
		
		private final List<VarNode> vars;
		
		public GoalRootNode(ProgramState state, String goalID) {
			this.goalID = goalID;
			this.vars = new ArrayList<>();
			ProgramState.GoalState goalState = state.getGoal(goalID);
			List<ID> ids = goalState.getVars();
			for(int i = 0; i < ids.size(); i++) {
				vars.add(new VarNode(goalState, this, i, ids.get(i)));
			}
		}
		
		///////////////////////////////////////////////////////////////////////
		
		@Override
		public String toString() {
			return "goal : " + goalID;
		}
		
		///////////////////////////////////////////////////////////////////////
		
		@Override
		public TreeNode getParent() {
			return null;
		}
		
		@Override
		public int getIndex(TreeNode node) {
			return -1;
		}
		
		@Override
		public int getChildCount() {
			return vars.size();
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			return vars.get(childIndex);
		}
		
		@Override
		public Enumeration<VarNode> children() {
			return Collections.enumeration(vars);
		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		public boolean isLeaf() {
			return false;
		}

	}
	
	private static class VarNode implements TreeNode {
		
		protected final GoalRootNode parent;
		
		protected final int index;
		
		protected final CoqToken term;
		
		protected final ProgramState.VarInfo info;
		
		protected final List<TreeNode> children;
		
		public VarNode(ProgramState.GoalState state, ID var) {
			this(state, null, 0, var);
		}
		
		public VarNode(ProgramState.GoalState state, GoalRootNode parent, int index, ID var) {
			this(parent, index, (CoqToken)var, state.getVarInfo(var));
		}
		
		protected VarNode(GoalRootNode parent, int index,
				CoqToken term, ProgramState.VarInfo varInfo) {
			this.parent = parent;
			this.index = index;
			this.term = term;
			this.info = varInfo;
			
			this.children = new ArrayList<>();
			if(info != null) {
				List<ID> equalities = info.getVarEqualities();
				for(int i = 0; i < equalities.size(); i++) {
					children.add(new EqualityNode(this, i, (ID)term, equalities.get(i)));
				}

				List<EqualsExpression> exprs = info.getExpressionsWithVar();
				for(int i = 0; i < exprs.size(); i++) {
					children.add(new TokenNode(this, children.size() + i, exprs.get(i)));
				}

				if(info.getStoreBoundTerm() != null) {
					children.add(new StoreboundVarNode(this, children.size(),
							info.getStoreBoundTerm(),
							info.getStoreBoundTermInfo()));
				}
			}
		}
		
		///////////////////////////////////////////////////////////////////////
		
		@Override
		public String toString() {
			return term.fullText();
		}
		
		///////////////////////////////////////////////////////////////////////
		
		@Override
		public TreeNode getParent() {
			return parent;
		}
		
		@Override
		public int getIndex(TreeNode node) {
			return index;
		}
		
		@Override
		public int getChildCount() {
			return children.size();
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			return children.get(childIndex);
		}
		
		@Override
		public Enumeration<TreeNode> children() {
			return Collections.enumeration(children);
		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		public boolean isLeaf() {
			return false;
		}
		
	}
	
	private static class StoreboundVarNode extends VarNode {
		
		protected final VarNode parentVar;
		
		public StoreboundVarNode(VarNode parent, int index,
				CoqToken var, ProgramState.VarInfo varInfo) {
			super(null, index, var, varInfo);
			
			this.parentVar = parent;
		}
		
		@Override
		public String toString() {
			return "storebound term: " + term.fullText();
		}
		
		@Override
		public TreeNode getParent() {
			return parentVar;
		}
	}
	
	private static class EqualityNode implements TreeNode {
		
		private final VarNode parent;
		
		private final int index;
		
		private final ID left, right;
		
		public EqualityNode(VarNode parent, int index, ID left, ID right) {
			this.parent = parent;
			this.index = index;
			this.left = left;
			this.right = right;
		}
		
		///////////////////////////////////////////////////////////////////////
		
		public String toString() {
			return left.getFullName() + " = " + right.getFullName();
		}
		
		///////////////////////////////////////////////////////////////////////
		
		@Override
		public TreeNode getParent() {
			return parent;
		}
		
		@Override
		public int getIndex(TreeNode node) {
			return index;
		}
		
		@Override
		public int getChildCount() {
			return 0;
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			return null;
		}
		
		@Override
		public Enumeration<VarNode> children() {
			return null;
		}

		@Override
		public boolean getAllowsChildren() {
			return false;
		}

		@Override
		public boolean isLeaf() {
			return true;
		}
		
	}
	
	private static class TokenNode implements TreeNode {
		
		private final VarNode parent;
		
		private final int index;
		
		private final CoqToken token;
		
		public TokenNode(VarNode parent, int index, CoqToken token) {
			this.parent = parent;
			this.index = index;
			this.token = token;
		}
		
		///////////////////////////////////////////////////////////////////////
		
		public String toString() {
			return token.fullText();
		}
		
		///////////////////////////////////////////////////////////////////////
		
		@Override
		public TreeNode getParent() {
			return parent;
		}
		
		@Override
		public int getIndex(TreeNode node) {
			return index;
		}
		
		@Override
		public int getChildCount() {
			return 0;
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			return null;
		}
		
		@Override
		public Enumeration<VarNode> children() {
			return null;
		}

		@Override
		public boolean getAllowsChildren() {
			return false;
		}

		@Override
		public boolean isLeaf() {
			return true;
		}
		
	}

}
