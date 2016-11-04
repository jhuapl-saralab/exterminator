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
package edu.jhuapl.exterminator.slmech;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.sentence.Assertion;
import edu.jhuapl.exterminator.grammar.coq.term.Completes;
import edu.jhuapl.exterminator.grammar.coq.term.Forall;
import edu.jhuapl.exterminator.grammar.coq.term.NamedFields;
import edu.jhuapl.exterminator.grammar.coq.term.Statements;
import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.grammar.coq.term.expression.BooleanExpression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.Expression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.Implies;

public class Code {

	private final Term term;
	
	private final Term pre;
	
	private final NamedFields st1;
	
	private final Term st2;
	
	private final List<Term> prog;
	
	private final Term post;
	
	public Code(Assertion assertion) {
		Objects.requireNonNull(assertion);
		
		if(assertion.getBinders() != null && !assertion.getBinders().isEmpty()) {
			throw new IllegalArgumentException("Malformed code: " + assertion);
		}
		
		this.term = Objects.requireNonNull(assertion.getTerm());
		
		if(!(term instanceof Forall) ||
				!(((Forall)term).getTerm() instanceof Implies)) {
			throw illegal();
		}

		Implies i = (Implies)((Forall)term).getTerm();
		
		Term left = i.getLeft(),
				right = i.getRight();
		
		if(!(left instanceof Expression) ||
				((Expression)left).getArgs().size() != 1 ||
				!(right instanceof Implies)) {
			throw illegal();
		}
		
		Expression e = (Expression)left;
		if(!(e.getArg(0).getTerm() instanceof NamedFields)) {
			throw illegal();
		}
		
		this.pre = e.getTerm();
		this.st1 = (NamedFields)e.getArg(0).getTerm();

		if(!(((Implies)right).getLeft() instanceof Completes)) {
			throw illegal();
		}
		
		Completes c = (Completes)((Implies)right).getLeft();

		Term t = c.getSt1();
		if(!st1.equals(t)) {
			throw illegal();
		}
		
		st2 = c.getSt2();
		
		// FIXME
//		if(!(e.getArg(2).getTerm() instanceof Statements)) {
//			throw illegal();
//		}
//		
		this.prog = getProgFrom(c.getStatements());
		
		right = ((Implies)right).getRight();
		
		if(right instanceof Expression && !(right instanceof BooleanExpression)) {
			e = (Expression)right;
			if(e.getArgs().size() != 1) {
				throw illegal();
			}
			this.post = e.getTerm();
			if(!st2.equals(e.getArg(0).getTerm())) {
				throw illegal();
			}
		} else {
			this.post = right;
		}
			
		
//		if(term instanceof Expression) {
//			Expression e = (Expression)term;
//			if(!(e.getTerm() instanceof Forall) || e.getArgs().size() > 2) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			this.pre = ((Forall)e.getTerm()).getTerm();
//			
//			if(!(e.getArg(0).getTerm() instanceof Implies)) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			Implies i = (Implies)e.getArg(0).getTerm();
//			
//			if(!(i.getLeft() instanceof Implies)) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			Implies iLeft = (Implies)i.getLeft();
//			
//			if(!(iLeft.getLeft() instanceof Localization)) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			this.st1 = (Localization)iLeft.getLeft();
//			
//			if(!(iLeft.getRight() instanceof Expression)) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			e = (Expression)iLeft.getRight();
//			
//			if(!(e.getTerm() instanceof ID) ||
//					!((ID)e.getTerm()).getFullName().equalsIgnoreCase("completes") ||
//					e.getArgs().size() != 3) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			if(!(e.getArg(0).getTerm() instanceof Localization) ||
//					!st1.equals((Localization)e.getArg(0).getTerm())) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			this.st2 = e.getArg(1).getTerm();
//			
//			if(!(e.getArg(2).getTerm() instanceof Statements)) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			this.prog = getProgFromFullText(e.getArg(2).getTerm());
//			this.post = i.getRight();
//			
////			this.st2 = e.getArg(0).getTerm();
////			
////			Term t = e.getTerm();
////			if(!(t instanceof Implies)) {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
////			
////			Implies i = (Implies)t;
////			
////			this.post = i.getRight();
////			
////			t = i.getLeft();
////			if(!(t instanceof Implies)) {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
////			
////			i = (Implies)t;
////			
////			t = i.getLeft();
////			if(!(t instanceof Expression)) {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
////			
////			Expression e2 = (Expression)t;
////			this.pre = e2.getTerm();
////			
////			if(e2.getArgs().size() != 0 ||
////					!(e2.getArg(0).getTerm() instanceof Localization)) {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
////			this.st1 = (Localization)e2.getArg(0).getTerm();
////			
////			t = i.getRight();
////			if(!(t instanceof Expression)) {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
////			
////			e = (Expression)t;
////			if(!(e.getTerm() instanceof ID) || e.getArgs().size() != 3) {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
////			
////			ID id = (ID)e.getTerm();
////			if(!"completes".equalsIgnoreCase(id.getFullName())) {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
////			
////			if(!(e.getArg(0).getTerm() instanceof Localization)) {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
////			
////			if(!st1.equals((Localization)e.getArg(0).getTerm())) {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
////			
////			t = e.getArg(1).getTerm();
////			if(!t.equals(st2)) {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
////			
////			t = e.getArg(2).getTerm();
////			if(!(t instanceof Statements)) {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
////			
////			this.prog = getProgFromFullText((Statements)t);
//			
//		} else if(term instanceof Implies) {
//			
//			Implies i = (Implies)term;
//			
////			System.out.println("i = " + i.fullText());
////			System.out.println("left = " + i.getLeft());
////			System.out.println("left = " + i.getLeft().fullText());
////			System.out.println("right = " + i.getRight().fullText());
//			
//			this.post = i.getRight();
//			
//			if(!(i.getLeft() instanceof Implies)) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			i = (Implies)i.getLeft();
//			
//			if(!(i.getLeft() instanceof Expression)) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			Expression e = (Expression)i.getLeft();
//
//			this.pre = e.getTerm();
//			
//			if(e.getArgs().size() != 1 ||
//				!(e.getArg(0).getTerm() instanceof Localization)) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			this.st1 = (Localization)e.getArg(0).getTerm();
//			
//			if(!(i.getRight() instanceof Expression)) {
//				throw new IllegalArgumentException("Malformed code: " + assertion);
//			}
//			
//			e = (Expression)i.getRight();
//			
////			if(e.getTerm() instanceof LocalizedTerm) {
////				lt = (LocalizedTerm)e.getTerm();
////				
////				if(!(lt.getTerm() instanceof ID) || !(((ID)lt.getTerm()).getFullName().equalsIgnoreCase("completes"))) {
////					throw new IllegalArgumentException("Malformed code: " + assertion);
////				}
////				
////				if(!lt.getLocalization().equals(this.st1)) {
////					throw new IllegalArgumentException("Malformed code: " + assertion);
////				}
////				
////				if(e.getArgs().size() != 2) {
////					throw new IllegalArgumentException("Malformed code: " + assertion);
////				}
////				
////				this.st2 = e.getArg(0).getTerm();
////				
////				if(!(e.getArg(1).getTerm() instanceof Statements)) {
////					throw new IllegalArgumentException("Malformed code: " + assertion);
////				}
////				
////				this.prog = getProgFromFullText((Statements)e.getArg(1).getTerm());
////			} else if(e.getTerm() instanceof ID && ((ID)e.getTerm()).getFullName().equalsIgnoreCase("completes")) {
//				if(e.getArgs().size() != 2 || !(e.getArg(1).getTerm() instanceof Localization)) {
//					throw new IllegalArgumentException("Malformed code: " + assertion);
//				}
//				
//				this.st2 = e.getArg(0).getTerm();
//				
//				System.out.println(term);
//				System.out.println(term.fullText());
//				
//				this.prog = new ArrayList<>();
//				
////			} else {
////				throw new IllegalArgumentException("Malformed code: " + assertion);
////			}
//			
//		} else {
//			throw new IllegalArgumentException("Malformed code: " + assertion);
//		}
	}
	
	private IllegalArgumentException illegal() {
		StringBuilder sb = new StringBuilder();
		sb.append("Malformed code.  Make sure you've properly parenthesized it!\n");
		sb.append("Expected: forall [binders], (P st1) -> (completes st1 st2 prog) -> (Q st2?)\n");
		sb.append(term);
		return new IllegalArgumentException(sb.toString());
	}
	
	public Term getPrecondition() { return pre; }
	
	public NamedFields getState1() { return st1; }
	
	public Term getState2() { return st2; }
	
	public List<Term> getProg() { return Collections.unmodifiableList(prog); }
	
	public Term getPostcondition() { return post; }
	
	public static List<Term> getProgFrom(CoqToken t) {
		if(t instanceof Statements) {
			return ((Statements)t).getTerms();
		} else {
			return getProgFromFullText(t);
		}
	}
	
	public static List<Term> getProgFromFullText(CoqToken t) {
		List<Term> terms = new ArrayList<>();
		String str = t.fullText();
		str = str.replaceAll("\\(\\*.*?\\*\\)", ""); // remove comments
		int parenCount = 0;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c == '(') {
				parenCount++;
				sb.append(c);
			} else if(c == ')') {
				parenCount--;
				sb.append(c);
			} else if(c == ';') {
				if(i < str.length() - 1 && str.charAt(i + 1) == ';' && parenCount == 0) {
					String termString = sb.toString().trim();
					if(!termString.isEmpty()) {
						terms.add(CoqFTParser.parseTerm(termString, true));
					}
					sb = new StringBuilder();
					i++; // skip the next ';'
				} else {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
//		for(String statement : str.split(";;")) {
//			try {
//				terms.add(CoqFTParser.parseTerm(statement.trim(), true));
//			} catch(IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
		return Collections.unmodifiableList(terms);
	}
	
//	public static List<Term> getProg(Statements s) {
//		List<Term> terms = new ArrayList<>();
//		for(Term term : s.getTerms()) {
//			if(term instanceof Statements) {
//				terms.addAll(getProg((Statements)term));
//			} else {
//				terms.add(term);
//			}
//		}
//		return terms;
//	}
//	
}
