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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.term.Arg;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class NotExpression extends AbstractBooleanExpression {
	
//	public static boolean applies(Term term) {
//		if(!(term instanceof Expression)) return false;
//		
//		Expression e = (Expression)term;
//		if(e.getArgs().size() != 1) return false;
//		
//		String s = e.getTerm().fullText().trim();
//		return s.equals("!") || s.equals("~");
//	}

	public static boolean applies(CoqParser.TermContext ctx) {
		List<Operator> ops = getOps(Operator.NOT_BANG, Operator.NOT_TILDE);
		
//		if(Qualid.applies(ctx)) {
//			String str = ctx.getText().trim();
//			Operator op = startsWithOps(str, ops);
//			if(op != null) {
//				str = str.substring(op.getDisplay().length()).trim();
//				if(!str.isEmpty()) {
//					try {
//						Term t = CoqFTParser.parseTerm(str, true);
//						return t instanceof BooleanExpression;
//					} catch(IllegalArgumentException e) { }
//				}
//			}
//		}
		
		if(!Expression.applies(ctx)) return false;
		
		return ctx.arg() != null && ctx.arg().size() > 0 &&
				equalsOps(ctx.expression_term.getText(), ops) != null;
	}
	
	public static NotExpression convert(Expression e) {
		if(e == null) return null;
		
		if(!(e.getTerm() instanceof ID) || (e.getArgs().size() != 1)) return null;
		
		ID i = (ID)e.getTerm();
		if(equalsOps(i.getFullName(), getOps(Operator.NOT_BANG, Operator.NOT_TILDE)) == null) {
			return null;
		} else {
			return new NotExpression(e);
		}
	}
	
//	public static boolean applies(Term term) {
//		List<Operator> ops = getOps(Operator.NOT_BANG, Operator.NOT_TILDE);
//		
//		if(term instanceof ID) {
//			String str = ((ID)term).getFullName();
//			return startsWithOps(str, ops) != null;
//		}
//		
//		if(term instanceof Expression) {
//			return equalsOps(((Expression)term).getTerm().fullText(), ops) != null;
//		}
//		
//		return false;
//	}
	
	///////////////////////////////////////////////////////////////////////////
	
//	public NotConditional(Term term) {
//		super(term);
//		
//		Expression e = (Expression)term;
//		this.notStr = e.getTerm().fullText().trim();
//		this.inner = Conditional.make(e.getArg(0).getTerm());
//	}
	
	public NotExpression(CoqFTParser parser, CoqParser.TermContext ctx) {
		super(parser, ctx, Operator.NOT_BANG, Operator.NOT_TILDE);
		
//		if(!(left instanceof BooleanExpression)) {
//			throw new IllegalArgumentException("Inner expression is not boolean: " + left);
//		}
	}
	
	protected NotExpression(Expression conv) {
		super(conv, Operator.NOT_BANG, Operator.NOT_TILDE);
	}
	
	protected NotExpression(NotExpression copy) {
		super(copy);
	}
	
	@Override
	protected ConstructorData construct(CoqFTParser parser,
			CoqParser.TermContext ctx, Operator firstOp,
			Operator... additionalOps) {
//		if(args.size() != 1) {
//			throw new IllegalArgumentException("Needs exactly one arg: " + args);
//		}
		
		List<Operator> ops = getOps(Operator.NOT_BANG, Operator.NOT_TILDE);
//		
//		Operator op = null;
//		Term left = null, right = null;
		
//		if(Qualid.applies(ctx)) {
//			String str = new Qualid(parser, ctx).getFullName();
//			op = startsWithOps(str,
//					Arrays.asList(Operator.NOT_BANG, Operator.NOT_TILDE));
//			str = str.substring(op.getDisplay().length()).trim();
//			left = CoqFTParser.parseTerm(str, true);
//			
//		} else {
//			
//		}
		
		Term left;
		if(args.size() == 1) {
			left = getArg(0).getTerm();
		} else {
			List<Term> terms = new ArrayList<>();
			if(getArg(0).getIdentDefine() != null) {
				throw new IllegalArgumentException("Invalid expression: " + this);
			}
			for(Arg arg : args) {
				terms.add(arg.getTerm());
			}
			left = make(terms);
		}
		
		return new ConstructorData(
				equalsOps(getTerm().fullText(), ops),
				left, null);
	}
	
	@Override
	protected ConstructorData construct(Operator firstOp,
			Operator... additionalOps) {
		return construct(null, null, firstOp, additionalOps);
	}
	
	@Override
	public boolean shouldParenthesize() { return false; }
	
	@Override
	public Element makeCoqDocTerm(Document doc) {
		return CoqDoc.makeTermNode(doc,
				CoqDoc.makeKeywordNode(doc, getOperator().getDisplay()),
				CoqDoc.makeKeywordNode(doc, "("),
				getLeft().makeCoqDocTerm(doc),
				CoqDoc.makeKeywordNode(doc, ")"));
	}
	
	@Override
	public NotExpression clone() {
		return new NotExpression(this);
	}

}
