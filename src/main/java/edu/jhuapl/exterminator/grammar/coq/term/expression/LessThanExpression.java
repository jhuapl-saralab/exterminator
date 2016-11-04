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

public class LessThanExpression extends AbstractBooleanExpression {

	public static boolean applies(CoqParser.TermContext ctx) {
		return applies(ctx, Operator.LESS_THAN_OR_EQUAL_TO, Operator.STRICTLY_LESS_THAN, Operator.VAL_LTB, Operator.LESS_THAN_QUESTION);
	}
	
//	public static boolean applies(Term term) {
//		return applies(term, Operator.LESS_THAN_OR_EQUAL_TO, Operator.STRICTLY_LESS_THAN);
//	}
	
	public static LessThanExpression convert(Expression e) {
		if(isValidForConversion(e, Operator.LESS_THAN_OR_EQUAL_TO, Operator.STRICTLY_LESS_THAN, Operator.VAL_LTB, Operator.LESS_THAN_QUESTION)) {
			return new LessThanExpression(e);
		} else {
			return null;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	public LessThanExpression(CoqFTParser parser, CoqParser.TermContext ctx) {
		super(parser, ctx, Operator.LESS_THAN_OR_EQUAL_TO, Operator.STRICTLY_LESS_THAN, Operator.VAL_LTB, Operator.LESS_THAN_QUESTION);
	}
	
	protected LessThanExpression(Expression conv) {
		super(conv, Operator.LESS_THAN_OR_EQUAL_TO, Operator.STRICTLY_LESS_THAN, Operator.VAL_LTB, Operator.LESS_THAN_QUESTION);
	}
	
	protected LessThanExpression(LessThanExpression copy) {
		super(copy);
	}
	
	@Override
	protected ConstructorData construct(CoqFTParser parser,
			CoqParser.TermContext ctx, Operator firstOp,
			Operator... additionalOps) {
		ConstructorData data = super.construct(parser, ctx, firstOp, additionalOps);
		if(data.op == Operator.VAL_LTB) data.op = Operator.STRICTLY_LESS_THAN;
		return data;
	}
	
	@Override
	protected ConstructorData construct(Operator firstOp,
			Operator... additionalOps) {
		ConstructorData data = super.construct(firstOp, additionalOps);
		if(data.op == Operator.VAL_LTB) data.op = Operator.STRICTLY_LESS_THAN;
		return data;
	}
	
	@Override
	public LessThanExpression clone() {
		return new LessThanExpression(this);
	}
	
}
