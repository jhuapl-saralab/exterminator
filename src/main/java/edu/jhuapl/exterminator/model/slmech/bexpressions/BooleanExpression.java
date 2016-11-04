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
package edu.jhuapl.exterminator.model.slmech.bexpressions;

import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.model.exceptions.UnknownBooleanExpressionException;

public abstract class BooleanExpression {
	protected final Term term;
	
	protected BooleanExpression(Term t){
		this.term = t;
	}
	
	/*
	public static BooleanExpression ofTerm(Term t) throws UnknownBooleanExpressionException {
		BooleanExpression ret;
		switch(t.getHead()){
		case "bbool": ret = new ImmediateBExpr(t);
		break;
		case "beq": ret = new EqualBExpr(t);
		break;
		case "band": ret = new AndBExpr(t);
		break;
		case "bor": ret = new OrBExpr(t);
		break;
		case "bnot": ret = new NotBExpr(t);
		break;
		case "blt": ret = new LessThanBExpr(t);
		break;
		case "bgt": ret = new GreaterThanBExpr(t);
		break;
		case "ble": ret = new LessEqualBExpr(t);
		break;
		case "bge": ret = new GreaterEqualBExpr(t);
		break;
		default: throw new UnknownBooleanExpressionException(t);
		}
		return ret;
	}
	*/
}
