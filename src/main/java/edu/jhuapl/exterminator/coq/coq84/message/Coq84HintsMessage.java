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
import edu.jhuapl.exterminator.coq.coq84.command.Coq84HintsCommand;
import edu.jhuapl.exterminator.coq.message.CoqHintsMessage;

public class Coq84HintsMessage extends Coq84Message implements CoqHintsMessage {
	
	private final Coq84HintsCommand command;
	
	private final List<List<Hint84>> hypotheses;
		
	private final List<Hint84> goals;

	public Coq84HintsMessage(Coq84HintsCommand command, Document doc) {
		super(doc);
		
		this.command = Objects.requireNonNull(command);

		if(value.getChildNodes().getLength() != 1) {
			throw new IllegalArgumentException("Malformed hint: " + value);
		}
		
		Node node = CoqUtils.parseOptionNode(value.getFirstChild());
		
		if(node == null) {
			hypotheses = new ArrayList<>(0);
			goals = new ArrayList<>(0);
			
		} else {
			List<Node> hypList = CoqUtils.parseList(CoqUtils.parsePairFirst(node));
			List<Node> goalsList = CoqUtils.parseList(CoqUtils.parsePairSecond(node));
			
			hypotheses = new ArrayList<>(hypList.size());
			for(Node hypNode : hypList) {
				List<Node> list = CoqUtils.parseList(hypNode);
				List<Hint84> hints = new ArrayList<>();
				for(Node hint : list) {
					hints.add(new Hint84(hint));
				}
				hypotheses.add(hints);
			}
			
			goals = new ArrayList<>(goalsList.size());
			for(Node hint : goalsList) {
				goals.add(new Hint84(hint));
			}
		}
	}
	
	@Override
	public List<List<? extends Hint>> getHypotheses() {
		List<List<? extends Hint>> unmodifiableHypotheses = new ArrayList<>(hypotheses.size());
		for(List<Hint84> list : hypotheses) {
			unmodifiableHypotheses.add(Collections.unmodifiableList(list));
		}
		
		return Collections.unmodifiableList(unmodifiableHypotheses);
	}
	
	@Override
	public List<? extends Hint> getGoals() {
		return Collections.unmodifiableList(goals);
	}
	
	@Override
	public Coq84HintsCommand getCommand() {
		return command;
	}
	
	public static class Hint84 implements Hint {
		
		private final String name;
		
		private final String code;
		
		private Hint84(Node node) {
			this(CoqUtils.parseString(CoqUtils.parsePairFirst(node)),
					CoqUtils.parseString(CoqUtils.parsePairSecond(node)));
		}
		
		public Hint84(String name, String code) {
			this.name = Objects.requireNonNull(name);
			this.code = Objects.requireNonNull(code);
		}
		
		@Override
		public String getName() { return name; }
		
		@Override
		public String getCode() { return code; }
		
	}
	
}
