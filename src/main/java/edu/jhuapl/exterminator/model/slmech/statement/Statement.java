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
import edu.jhuapl.exterminator.model.exceptions.UnknownStatementException;

public abstract class Statement {
	protected final Term term;
	
	protected Statement(Term t){
		this.term = t;
	}
	
	/*
	public static Statement ofTerm(Term t) throws UnknownStatementException, MalformedStatementException {
		Statement ret;
		switch(t.getHead()){
		case "assign": ret = new AssignStmt(t);
		break;
		case "heapload": ret = new LoadStmt(t);
		break;
		case "heapwrite": ret = new WriteStmt(t);
		break;
		case "pcons": ret = new PConsStmt(t);
		break;
		case "dispose": ret = new DisposeStmt(t);
		break;
		case "skip": ret = new SkipStmt(t);
		break;
		case "ifelse": ret = new IfStmt(t);
		break;
		case "while": ret = new WhileStmt(t);
		break;
		case "abort": ret = new AbortStmt(t);
		break;
		case "atomic": ret = new AtomicStmt(t);
		break;
		case "local": ret = new LocalStmt(t);
		break;
		case "endscope": ret = new EndScopeStmt(t);
		break;
		case "ret": ret = new RetStmt(t);
		break;
		case "seq": ret = new SeqStmt(t);
		break;
		default:
			throw new UnknownStatementException(t);
		}
		return ret;
	}
	*/
}
