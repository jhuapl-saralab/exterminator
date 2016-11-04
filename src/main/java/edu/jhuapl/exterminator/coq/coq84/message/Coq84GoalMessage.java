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
package edu.jhuapl.exterminator.coq.coq84.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.jhuapl.exterminator.coq.CoqUtils;
import edu.jhuapl.exterminator.coq.coq84.Coq84Message;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84GoalCommand;
import edu.jhuapl.exterminator.coq.message.CoqGoalMessage;
import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Coq84GoalMessage extends Coq84Message implements CoqGoalMessage {
	
	private final Coq84GoalCommand command;
		
	private final Goals84 fg, bg;
	
	public Coq84GoalMessage(Coq84GoalCommand command, Document doc) {
		super(doc);
		
		this.command = Objects.requireNonNull(command);
		
		if(value.getChildNodes().getLength() != 1) {
			throw new IllegalArgumentException("Value has too many children.");
		}
		
		Node goals = CoqUtils.parseOptionNode(value.getFirstChild());
		
		if(goals == null) {
			this.fg = null;
			this.bg = null;
		} else {
			if(!goals.getNodeName().equalsIgnoreCase("goals")) {
				throw new IllegalArgumentException("Info node is not of type goals.");
			}
			if(goals.getChildNodes().getLength() != 2) {
				throw new IllegalArgumentException("Malformed goals node: " + goals);
			}
			
			this.fg = new Goals84(goals.getFirstChild(), true);
			this.bg = new Goals84(goals.getLastChild(), false);
		}
	}
	
	@Override
	public Goals84 getFG() { return fg; }
	
	@Override
	public Goals84 getBG() { return bg; }
	
	@Override
	public Coq84GoalCommand getCommand() {
		return command;
	}
	
	public static class Goals84 implements Goals {
		
		private final List<Goal84> goals;
		
		public Goals84(Node goals, boolean isForeground) {
			this.goals = new ArrayList<>();
			for(Node goal : CoqUtils.parseList(goals)) {
				this.goals.add(new Goal84(goal, isForeground));
			}
		}
		
		@Override
		public List<? extends Goal> getGoals() {
			return Collections.unmodifiableList(goals);
		}
		
	}
	
	public static class Goal84 implements Goal {
		
		private final String id;
		
		private final boolean isForeground;
		
		private final List<Term> hypothesis_terms;
		private final List<String> hypotheses;
		
		private final Term conclusion;
		
		private Goal84(Node goal, boolean isForeground) {
			if(!goal.getNodeName().equalsIgnoreCase("goal")) {
				throw new IllegalArgumentException("Goal node is not correct type: " + goal);
			}
			if(goal.getChildNodes().getLength() != 3) {
				throw new IllegalArgumentException("Malformed goal node: " + goal);
			}

			Node node = goal.getFirstChild();
			this.id = CoqUtils.parseString(node);
			this.isForeground = isForeground;
			
			node = node.getNextSibling();
			this.hypotheses = CoqUtils.parseStringList(node);
			
			this.hypothesis_terms = new ArrayList<Term>(this.hypotheses.size());
			for(String h : hypotheses){
				try{
					this.hypothesis_terms.add(CoqFTParser.parseTerm(h, true));
				}catch(Exception e){
					/* Hrm.... */
					System.err.println("Failed to parse hypothesis \""+h+"\" as a term (abandoning it).");
					e.printStackTrace();
				}
			}
			
			node = node.getNextSibling();
			
			String str = CoqUtils.parseString(node);
			this.conclusion = CoqFTParser.parseTerm(str,
					str.length() < 1000);
		}
		
		@Override
		public String getID() { return id; }
		
		@Override
		public boolean isForeground() { return isForeground; }
		
		@Override
		public List<String> getHypotheses() {
			return Collections.unmodifiableList(hypotheses);
		}
		
		@Override
		public List<Term> getHypothesisTerms() {
			return Collections.unmodifiableList(hypothesis_terms);
		}
		
		@Override
		public Term getConclusion() { return conclusion; }
		
	}

}
