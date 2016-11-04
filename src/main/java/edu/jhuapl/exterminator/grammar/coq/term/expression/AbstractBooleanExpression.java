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
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.term.Arg;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public abstract class AbstractBooleanExpression extends Expression implements BooleanExpression {
	
	protected static List<Operator> getOps(Operator firstOperator,
			Operator... additionalOperators) {
		Objects.requireNonNull(firstOperator);
		
		List<Operator> ops = new ArrayList<>();
		ops.add(firstOperator);
		if(additionalOperators != null) {
			for(Operator op : additionalOperators) {
				if(op != null) {
					ops.add(op);
				}
			}
		}
		return ops;
	}
	
	protected static Operator containsOps(String str, List<Operator> ops) {
		for(Operator op : ops) {
			if(str.contains(op.getDisplay())) return op;
		}
		return null;
	}
	
	protected static Operator startsWithOps(String str, List<Operator> ops) {
		for(Operator op : ops) {
			if(str.startsWith(op.getDisplay())) return op;
		}
		return null;
	}
	
	protected static Operator equalsOps(String str, List<Operator> ops) {
		for(Operator op : ops) {
			if(str.equals(op.getDisplay())) return op;
		}
		return null;
	}
	
	protected static List<CoqParser.TermContext> getEndfixOperands(
			CoqParser.TermContext ctx, List<Operator> ops) {
		return getEndfixOperands(ctx.expression_term, ctx.arg(), ops);
	}
	
	protected static List<CoqParser.TermContext> getEndfixOperands(
			CoqParser.TermContext expressionCtx,
			List<CoqParser.ArgContext> args, List<Operator> ops) {
		if(equalsOps(expressionCtx.getText().trim(), ops) == null)
			return null;
		if(args.size() < 1 || args.size() > 2) return null;
		List<CoqParser.TermContext> list = new ArrayList<>();
		if(args.size() == 1) {
			CoqParser.TermContext t = args.get(0).term();
			if(!Expression.applies(t) || t.arg() == null || t.arg().size() != 1)
				return null;
			list.add(t.expression_term);
			list.add(t.arg(0).term());
		} else {
			list.add(args.get(0).term());
			list.add(args.get(1).term());
		}
		return list;
	}
	
	protected static boolean applies(CoqParser.TermContext ctx,
			Operator firstOperator, Operator... additionalOperators) {
		List<Operator> ops = getOps(firstOperator, additionalOperators);
		
//		if(Qualid.applies(ctx)) {
//			if(containsOps(ctx.getText(), ops) != null) return true;
//		}
		
		if(!Expression.applies(ctx)) return false;
				
		if(getEndfixOperands(ctx, ops) != null) {
			return true;
		}
		
		if(Expression.isArgChain(ctx)) {
			List<CoqParser.TermContext> args = Expression.getArgChain(ctx);
			for(int i = 1; i < args.size(); i++) {
				String s = args.get(i).getText().trim();
				if(equalsOps(s, ops) != null) {
					return true;
				}
			}
			
		} else {
			for(int i = 0; i < ctx.arg().size(); i++) {
				CoqParser.ArgContext arg = ctx.arg(i);
				if(arg.ident() != null) continue;
				String s = arg.term().getText().trim();
				if(equalsOps(s, ops) != null) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	protected static boolean isValidForConversion(Expression e,
			Operator firstOperator, Operator... additionalOperators) {
		if(e == null) return false;
		
		for(Arg arg : e.getArgs()) {
			if(arg.getIdentDefine() != null) return false;
		}
		
		if(e.getArgs().size() < 2) return false;
		
		List<Operator> ops = getOps(firstOperator, additionalOperators);
		
		if(e.getTerm() instanceof ID &&
				equalsOps(((ID)e.getTerm()).getFullName(), ops) != null) {
			return true;
		}

		for(Arg arg : e.getArgs()) {
			if(arg.getTerm() instanceof ID &&
					equalsOps(((ID)arg.getTerm()).getFullName(), ops) != null) {
				return true;
			}
		}
		
		return false;
	}
	
//	protected static boolean applies(Term term, Operator firstOperator,
//			Operator... additionalOperators) {
//		Objects.requireNonNull(term);
//		
//		if(term instanceof BooleanExpression) return true;
//		
//		List<Operator> ops = getOps(firstOperator, additionalOperators);
//		
//		if(term instanceof ID) {
//			String str = ((ID)term).getFullName();
//			return containsOps(str, ops) != null;
//		}
//		
//		if(term instanceof Expression) {
//			Expression e = (Expression)term;
//			if(e.getArgs().size() != 1) return false;
//			
//			String str1 = e.getTerm().fullText(),
//					str2 = e.getArg(0).fullText();
//			for(Operator op : ops) {
//				if(op.getDisplay().equals(str1) ^ op.getDisplay().equals(str2))
//					return true;
//			}
//		}
//		
//		return false;
//	}
	
	///////////////////////////////////////////////////////////////////////////

	protected final Operator op;
	
	protected final Term left, right;
	
	protected AbstractBooleanExpression(CoqFTParser parser,
			CoqParser.TermContext ctx, Operator firstOp,
			Operator... additionalOps) {
		super(parser, ctx);
		
		ConstructorData data = construct(parser, ctx, firstOp,
				additionalOps);
		this.op = data.op;
		this.left = data.left;
		this.right = data.right;
	}
	
	protected AbstractBooleanExpression(Expression conv, Operator firstOp,
			Operator... additionalOps) {
		super(conv);
				
		ConstructorData data = construct(firstOp, additionalOps);
		this.op = data.op;
		this.left = data.left;
		this.right = data.right;
	}
	
	protected AbstractBooleanExpression(AbstractBooleanExpression copy) {
		super(copy);
		this.op = copy.op;
		this.left = copy.left == null ? null : copy.left.clone();
		this.right = copy.right == null ? null : copy.right.clone();
	}
	
//	protected AbstractBooleanExpression(Term term, Operator firstOp,
//			Operator... additionalOps) {
//		super(term);
//		
//		List<Operator> ops = getOps(firstOp, additionalOps);
//		
//		if(term instanceof ID) {
//			String str = ((ID)term).getFullName();
//			String[] split = split(str, ops);
//			this.op = Operator.fromDisplay(splitStr(str, ops));
//			this.left = CoqFTParser.parseTerm(split[0], true);
//			this.right = CoqFTParser.parseTerm(split[1], true);
//
//		} else if(term instanceof Expression) {
//			Expression e = (Expression)term;
//			String str1 = e.getTerm().fullText(),
//					str2 = e.getArg(0).fullText();
//			String left, right;
//			if(containsOps(str1, ops) != null) {
//				this.op = containsOps(str1, ops);
//				String[] split = split(str1, ops);
//				left = split[0];
//				right = split[1] + " " + str2;
//			} else {
//				this.op = containsOps(str2, ops);
//				String[] split = split(str2, ops);
//				left = str1 + " " + split[0];
//				right = split[1];
//			}
//			this.left = CoqFTParser.parseTerm(left, true);
//			this.right = CoqFTParser.parseTerm(right, true);
//			
//		} else {
//			throw new IllegalArgumentException(term.toString());
//		}
//	}
	
	protected ConstructorData construct(CoqFTParser parser,
			CoqParser.TermContext ctx, Operator firstOp,
			Operator... additionalOps) {
		List<Operator> ops = getOps(firstOp, additionalOps);

		Operator fieldOp = null;
		Term fieldLeft, fieldRight;
		
		/*
if(Qualid.applies(ctx)) {
			String str = new Qualid(parser, ctx).getFullName();
			String[] split = split(str, ops);
			op = Operator.fromDisplay(splitStr(str, ops));
			this.left = CoqFTParser.parseTerm(split[0], true);
			this.right = CoqFTParser.parseTerm(split[1], true);

		} 
		 */
		
		List<CoqParser.TermContext> operands = getEndfixOperands(ctx, ops);
		if(operands != null) {
			fieldOp = equalsOps(ctx.expression_term.getText().trim(), ops);
			fieldLeft = Term.make(parser, operands.get(0));
			fieldRight = Term.make(parser, operands.get(1));
			
		} else if(isArgChain(ctx)) {
			List<CoqParser.TermContext> args = getArgChain(ctx);
			List<CoqParser.TermContext> left = new ArrayList<>(),
					right = new ArrayList<>();
			boolean isLeft = true;
			for(CoqParser.TermContext arg : args) {
				String s = arg.getText().trim();
				if(isLeft && equalsOps(s, ops) != null) {
					fieldOp = equalsOps(s, ops);
					isLeft = false;
				} else if(isLeft) {
					left.add(arg);
				} else {
					right.add(arg);
				}
			}
			
			fieldLeft = make(parser, left);
			fieldRight = make(parser, right);

		} else {
			List<CoqParser.ArgContext> left = new ArrayList<>(),
					right = new ArrayList<>();
			boolean isLeft = true;
			for(CoqParser.ArgContext arg : ctx.arg()) {
				String s = arg.getText().trim();
				if(isLeft && equalsOps(s, ops) != null) {
					fieldOp = equalsOps(s, ops);
					isLeft = false;
				} else if(isLeft) {
					left.add(arg);
				} else {
					right.add(arg);
				}
			}

			if(left.size() == 0) {
				fieldLeft = Term.make(parser, ctx.expression_term);
			} else {
				fieldLeft = new Expression(Term.make(parser, ctx.expression_term));
				for(CoqParser.ArgContext arg : left) {
					((Expression)this.left).args.add(new Arg(parser, arg));
				}
			}

			fieldRight = makeA(parser, right);
		}

		Objects.requireNonNull(fieldOp);
		Objects.requireNonNull(fieldLeft);
		Objects.requireNonNull(fieldRight);
		return new ConstructorData(fieldOp, fieldLeft, fieldRight);
	}
	
	protected ConstructorData construct(Operator firstOp,
			Operator... additionalOps) {
		List<Operator> ops = getOps(firstOp, additionalOps);

		Operator fieldOp = null;
		Term fieldLeft = null, fieldRight = null;
		
		if(getTerm() instanceof ID) {
			fieldOp = equalsOps(((ID)getTerm()).getFullName(), ops);
			if(fieldOp != null) {
				if(getArgs().size() != 2) {
					throw new IllegalArgumentException("Invalid conversion format: " + fullText());
				}
				fieldLeft = getArg(0).getTerm();
				fieldRight = getArg(1).getTerm();
			}
		}
		
		if(fieldOp == null) {
			// look for it in the args
			List<Term> left = new ArrayList<>(), right = new ArrayList<>();
			left.add(getTerm());
			for(Arg arg : getArgs()) {
				if(fieldOp == null && arg.getTerm() instanceof ID && (fieldOp = equalsOps(((ID)arg.getTerm()).getFullName(), ops)) != null) {
					// we found the field op
				} else if(fieldOp == null) {
					left.add(arg.getTerm());
				} else {
					right.add(arg.getTerm());
				}
			}
			
			if(left.size() == 0 || right.size() == 0) {
				throw new IllegalArgumentException("Invalid conversion format: " + fullText + "\n" + left + "\n" + right);
			}
			
			fieldLeft = make(left);
			fieldRight = make(right);
		}
		
		Objects.requireNonNull(fieldOp);
		Objects.requireNonNull(fieldLeft);
		Objects.requireNonNull(fieldRight);
		return new ConstructorData(fieldOp, fieldLeft, fieldRight);
	}
	
	@Override
	public Operator getOperator() { return op; }

	@Override
	public Term getLeft() { return left; }

	@Override
	public Term getRight() { return right; }

	@Override
	public Term asTerm() { return this; }
	
	@Override
	public Element makeCoqDocTerm(Document doc) {
		return makeCoqDocTerm(doc, this);
	}
	
	public static Element makeCoqDocTerm(Document doc, BooleanExpression expr) {
		return CoqDoc.makeTermNode(doc,
				CoqDoc.makeParenthesizedTermNode(doc, expr.getLeft()),
				CoqDoc.makeWhitespaceNode(doc),
				CoqDoc.makeKeywordNode(doc, expr.getOperator().toString()),
				CoqDoc.makeWhitespaceNode(doc),
				CoqDoc.makeParenthesizedTermNode(doc, expr.getRight()));
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean equals(Object anObj) {
		if(anObj == this) return true;
		if(anObj == null || !(getClass().isAssignableFrom(anObj.getClass()))) return false;
		
		AbstractBooleanExpression e = (AbstractBooleanExpression)anObj;
		return Objects.equals(getOperator(), e.getOperator()) &&
				Objects.equals(getLeft(), e.getLeft()) &&
				Objects.equals(getRight(), e.getRight());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getOperator(), getLeft(), getRight());
	}
	
	protected String toString(String type) {
		StringBuilder sb = new StringBuilder();
		sb.append("{").append(type).append(" left=").append(getLeft());
		sb.append(" ").append(getOperator());
		sb.append(" right=").append(getRight());
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return toString(getClass().getSimpleName());
	}
	
	@Override
	public abstract AbstractBooleanExpression clone();
	
	///////////////////////////////////////////////////////////////////////////
	
	private static Term make(CoqFTParser parser, List<CoqParser.TermContext> terms) {
		if(terms.size() == 0) throw new IllegalArgumentException("Malformed expression.");
		
		if(terms.size() == 1) {
			return Term.make(parser, terms.get(0));
		} else {
			Expression e = new Expression(Term.make(parser, terms.get(0)));
			for(int i = 1; i < terms.size(); i++) {
				e.args.add(new Arg(Term.make(parser, terms.get(i))));
			}
			BooleanExpression be = BooleanExpression.Factory.convert(e);
			if(be != null) return be.asTerm();
			else return e;
		}
	}
	
	private static Term makeA(CoqFTParser parser, List<CoqParser.ArgContext> terms) {
		if(terms.size() == 0) throw new IllegalArgumentException("Malformed expression.");
		
		CoqParser.ArgContext first = terms.get(0);
		if(first.ident() != null) throw new IllegalArgumentException("Malformed expression.");
		
		if(terms.size() == 1) {
			return Term.make(parser, first.term());
		} else {
			Expression e = new Expression(Term.make(parser, first.term()));
			for(int i = 1; i < terms.size(); i++) {
				Arg a = new Arg(parser, terms.get(i));
				e.appendArg(a);
			}
			BooleanExpression be = BooleanExpression.Factory.convert(e);
			if(be != null) return be.asTerm();
			else return e;
		}
	}
	
	protected static Term make(List<Term> terms) {
		if(terms.size() == 1) {
			return terms.get(0);
		} else {
			Expression e = new Expression(terms.get(0));
			// FIXME: this doesn't update the fullText
			for(int i = 1; i < terms.size(); i++) {
				e.args.add(new Arg(terms.get(i)));
			}
			BooleanExpression be = BooleanExpression.Factory.convert(e);
			if(be != null) return be.asTerm();
			else return e;
		}
	}
//	// order matters!
//	private static String[] split(String str, List<Operator> ops) {
//		if(ops == null || ops.size() == 0) return null;
//
//		String opStr = ops.get(0).getDisplay();
//		int i = str.indexOf(opStr);
//		if(i >= 0) {
//			return new String[] {
//					str.substring(0, i), str.substring(i + opStr.length()) };
//		} else {
//			return split(str, ops.subList(1, ops.size()));
//		}
//	}
//
//	private static String splitStr(String str, List<Operator> ops) {
//		if(ops == null || ops.size() == 0) return null;
//
//		String opStr = ops.get(0).getDisplay();
//		int i = str.indexOf(opStr);
//		if(i >= 0) {
//			return opStr;
//		} else {
//			return splitStr(str, ops.subList(1, ops.size()));
//		}
//	}
	
	protected static class ConstructorData {
		
		public Operator op;
		
		public Term left, right;
		
		public ConstructorData(Operator op, Term left, Term right) {
			this.op = op;
			this.left = left;
			this.right = right;
		}
		
	}
	
}
