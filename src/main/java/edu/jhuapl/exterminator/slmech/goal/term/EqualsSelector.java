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
package edu.jhuapl.exterminator.slmech.goal.term;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.grammar.coq.term.TypeCast;
import edu.jhuapl.exterminator.grammar.coq.term.expression.EqualsExpression;

public class EqualsSelector implements TermSelector<List<EqualsExpression>> {
	
	private final ID var;
	
	private final Map<ID, ID> simpleTermTypes;
	
	public EqualsSelector(ID var, Map<ID, ID> simpleTermTypes) {
		this.var = Objects.requireNonNull(var);
		this.simpleTermTypes = Objects.requireNonNull(simpleTermTypes);
	}

	@Override
	public List<EqualsExpression> select(Term term) {
		if(term instanceof EqualsExpression) {
			EqualsExpression e = (EqualsExpression)term;
			Term left = e.getLeft(), right = e.getRight();
			
			if(left.equals(var) || right.equals(var) ||
					left.contains((CoqToken)var) ||
					right.contains((CoqToken)var)) {
				List<EqualsExpression> list = new ArrayList<>();
				
				// if this is a storebound term, ignore that expression
				ID rsb = StoreboundSelector.getRight(e, simpleTermTypes);
				if(rsb == null || !var.equals(rsb)) {
					list.add(e);
				}
				
				List<EqualsExpression> l = select(left);
				if(l != null) list.addAll(l);
				
				l = select(right);
				if(l != null) list.addAll(l);
				
				return list;
			}
		} else if(term instanceof TypeCast) {
			TypeCast tc = (TypeCast)term;
			
			Term t = tc.getTerm();
			// let's only do this for "H1: blah" terms
			if(!(t instanceof ID)) return null;
			
			t = tc.getType();
			return select(t);
		}

		return null;
	}

}
