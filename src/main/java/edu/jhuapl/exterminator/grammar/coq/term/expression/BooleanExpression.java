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
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public interface BooleanExpression extends CoqDoc.CoqDocable, Cloneable {
	
	public static enum Operator {
		EQUALS("="),
		
		EQUALS_EQUALS("=="),
		
		STRICTLY_LESS_THAN("<"),
		
		VAL_LTB("Val.ltb"),
		
		LESS_THAN_OR_EQUAL_TO("<="),
		
		LESS_THAN_QUESTION("<?"),
		
		STRICTLY_GREATER_THAN(">"),
		
		VAL_GTB("Val.gtb"),
		
		GREATER_THAN_OR_EQUAL_TO(">="),
		
		GREATER_THAN_QUESTION(">?"),
		
		AND("&"),
		
		AND_AND("&&"),
		
		BOOL_AND("/\\"),
		
		BOOL_AND_2("∧"),
		
		//OR("|"),
		
		OR_OR("||"),
		
		BOOL_OR("\\/"),
		
		BOOL_OR_2("∨"),
		
		NOT_BANG("!"),
		
		NOT_TILDE("~"),
		
		IMPLIES("->"),
		
		NA("N/A");
		
		private final String display;
		
		private Operator(String display) {
			this.display = display;
		}
		
		public String getDisplay() { return display; }
		
		@Override
		public String toString() { return getDisplay(); }
		
		public static Operator fromDisplay(String str) {
			if(str == null) return null;
			for(Operator op : values()) {
				if(str.equals(op.getDisplay())) return op;
			}
			
			throw new IllegalArgumentException("No operator matched " + str);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public Operator getOperator();

	public Term getLeft();
	
	public Term getRight();
	
	public Term asTerm();
	
	///////////////////////////////////////////////////////////////////////////
	
	public static class Factory {
		
//		public static BooleanExpression make(Term term) {
//			if(term instanceof BooleanExpression)
//				return (BooleanExpression)term;
//			
//			if(AndExpression.applies(term))
//				return new AndExpression(term);
//			if(EqualsExpression.applies(term))
//				return new EqualsExpression(term);
//			
//			throw new IllegalArgumentException("Unknown boolean expression: " + term);
//		}
		
		public static boolean applies(CoqParser.TermContext ctx) {
			return AndExpression.applies(ctx) ||
					OrExpression.applies(ctx) ||
					EqualsExpression.applies(ctx) ||
					GreaterThanExpression.applies(ctx) ||
					LessThanExpression.applies(ctx) ||
					NotExpression.applies(ctx) ||
					Implies.applies(ctx) ||
					TrueExpression.applies(ctx) ||
					FalseExpression.applies(ctx);
		}
		
		public static BooleanExpression make(CoqFTParser parser,
				CoqParser.TermContext ctx) {
			if(NotExpression.applies(ctx))
				return new NotExpression(parser, ctx);
			if(AndExpression.applies(ctx))
				return new AndExpression(parser, ctx);
			if(OrExpression.applies(ctx))
				return new OrExpression(parser, ctx);
			if(Implies.applies(ctx))
				return new Implies(parser, ctx);
			if(EqualsExpression.applies(ctx))
				return new EqualsExpression(parser, ctx);
			if(GreaterThanExpression.applies(ctx))
				return new GreaterThanExpression(parser, ctx);
			if(LessThanExpression.applies(ctx))
				return new LessThanExpression(parser, ctx);
			if(TrueExpression.applies(ctx))
				return new TrueExpression(parser, ctx);
			if(FalseExpression.applies(ctx))
				return new FalseExpression(parser, ctx);
				
			throw new IllegalArgumentException("Unknown boolean expression: " + ctx);
		}
		
		public static BooleanExpression convert(Expression e) {
			if(e == null) return null;
			
			BooleanExpression be = NotExpression.convert(e);
			if(be != null) return be;
			
			be = AndExpression.convert(e);
			if(be != null) return be;
			
			be = OrExpression.convert(e);
			if(be != null) return be;
			
//			be = Implies.convert(e);
//			if(be != null) return be;
			
			be = EqualsExpression.convert(e);
			if(be != null) return be;
			
			be = GreaterThanExpression.convert(e);
			if(be != null) return be;
			
			be = LessThanExpression.convert(e);
			if(be != null) return be;
			
			return null;
		}
		
	}
	
}
