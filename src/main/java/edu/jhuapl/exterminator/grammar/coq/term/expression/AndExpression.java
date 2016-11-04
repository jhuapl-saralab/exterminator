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
package edu.jhuapl.exterminator.grammar.coq.term.expression;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;

public class AndExpression extends AbstractBooleanExpression {
	
	public static boolean applies(CoqParser.TermContext ctx) {
		return applies(ctx, Operator.AND_AND, Operator.AND, Operator.BOOL_AND, Operator.BOOL_AND_2);
	}
	
//	public static boolean applies(Term term) {
//		return applies(term, Operator.AND_AND, Operator.AND);
//	}
	
	public static AndExpression convert(Expression e) {
		if(isValidForConversion(e, Operator.AND_AND, Operator.AND, Operator.BOOL_AND, Operator.BOOL_AND_2)) {
			return new AndExpression(e);
		} else {
			return null;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public AndExpression(CoqFTParser parser, CoqParser.TermContext ctx) {
		super(parser, ctx, Operator.AND_AND, Operator.AND, Operator.BOOL_AND, Operator.BOOL_AND_2);
	}
	
	protected AndExpression(Expression conv) {
		super(conv, Operator.AND_AND, Operator.AND, Operator.BOOL_AND, Operator.BOOL_AND_2);
	}
	
	protected AndExpression(AndExpression copy) {
		super(copy);
	}
	
//	public AndExpression(Term term) {
//		super(term, Operator.AND_AND, Operator.AND);
//	}

	@Override
	public AndExpression clone() {
		return new AndExpression(this);
	}
	
}
