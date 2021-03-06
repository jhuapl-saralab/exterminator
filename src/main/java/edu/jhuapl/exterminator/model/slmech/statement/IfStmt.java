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
package edu.jhuapl.exterminator.model.slmech.statement;

import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.model.exceptions.MalformedStatementException;
import edu.jhuapl.exterminator.model.exceptions.UnknownBooleanExpressionException;
import edu.jhuapl.exterminator.model.exceptions.UnknownStatementException;
import edu.jhuapl.exterminator.model.slmech.bexpressions.BooleanExpression;

public class IfStmt extends Statement {
	
	/*
	private final BooleanExpression guard;
	private final Statement ifclause;
	private final Statement elseclause;
	*/
	
	protected IfStmt(Term t) throws MalformedStatementException, UnknownStatementException {
		super(t);
		/*
		if(t.getBodyCount() != 3){
			throw new MalformedStatementException(t);
		}
		try{
			guard = BooleanExpression.ofTerm(t.getBodyElement(0));
		}catch(UnknownBooleanExpressionException e){
			throw new MalformedStatementException(t);
		}
		ifclause = Statement.ofTerm(t.getBodyElement(1));
		elseclause = Statement.ofTerm(t.getBodyElement(2));
		*/
	}
}
