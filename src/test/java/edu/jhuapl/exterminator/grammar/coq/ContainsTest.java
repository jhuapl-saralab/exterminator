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
package edu.jhuapl.exterminator.grammar.coq;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Assert;
import org.junit.Test;

import edu.jhuapl.exterminator.grammar.coq.term.Ident;
import edu.jhuapl.exterminator.grammar.coq.term.Name;
import edu.jhuapl.exterminator.grammar.coq.term.Qualid;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class ContainsTest {
	
	private static CoqFTParser parser(String str) {
		CoqLexer lexer = new CoqLexer(new ANTLRInputStream(str));
		CoqFTParser parser = new CoqFTParser(new CommonTokenStream(lexer));
		parser.setErrorHandler(new BailErrorStrategy());
		return parser;
	}
	
	private static Term parseTerm(String str) {
		CoqFTParser parser = parser(str);
		return Term.make(parser, parser.term());
	}
	
	private static Ident parseIdent(String str) {
		CoqFTParser parser = parser(str);
		return new Ident(parser, parser.ident());
	}
	
	private static Name parseName(String str) {
		CoqFTParser parser = parser(str);
		return new Name(parser, parser.name());
	}
	
	private static Qualid parseQualid(String str) {
		CoqFTParser parser = parser(str);
		return new Qualid(parser, parser.qualid());
	}
	
	@Test
	public void testIDEquals() {
		// ident x ident
		{
			Ident i1 = parseIdent("a");
			Ident i2 = parseIdent("a");
			
			Assert.assertTrue(i1.equals(i2));
			Assert.assertTrue(i2.equals(i1));
			
			Ident i3 = parseIdent("b");
			
			Assert.assertFalse(i1.equals(i3));
			Assert.assertFalse(i3.equals(i1));
		}

		// ident x name
		{
			Ident i1 = parseIdent("a");
			Name n1 = parseName("a");
			Name n2 = parseName("b");
			Name n3 = parseName("_");
			
			Assert.assertTrue(i1.equals(n1));
			Assert.assertTrue(n1.equals(i1));
			Assert.assertFalse(i1.equals(n2));
			Assert.assertFalse(n2.equals(i1));
			Assert.assertFalse(i1.equals(n3));
			Assert.assertFalse(n3.equals(i1));
			
			Ident i2 = parseIdent("_");
			
			Assert.assertFalse(i2.equals(n1));
			Assert.assertFalse(n1.equals(i2));
			Assert.assertFalse(i2.equals(n2));
			Assert.assertFalse(n2.equals(i2));
			Assert.assertTrue(i2.equals(n3));
			Assert.assertTrue(n3.equals(i2));
		}

		// ident x qualid
		{
			Ident i1 = parseIdent("a");
			Qualid q1 = parseQualid("a");
			Qualid q2 = parseQualid("b");
			Qualid q3 = parseQualid("a.a");
			
			Assert.assertTrue(i1.equals(q1));
			Assert.assertTrue(q1.equals(i1));
			Assert.assertFalse(i1.equals(q2));
			Assert.assertFalse(q2.equals(i1));
			Assert.assertFalse(i1.equals(q3));
			Assert.assertFalse(q3.equals(i1));
		}
		
		// name x name
		{
			Name n1 = parseName("_");
			Name n2 = parseName("_");
			Name n3 = parseName("a");
			
			Assert.assertTrue(n1.equals(n2));
			Assert.assertTrue(n2.equals(n1));
			Assert.assertFalse(n1.equals(n3));
			Assert.assertFalse(n3.equals(n1));
		}
		
		// name x qualid
		{
			Name n1 = parseName("_");

			Qualid q1 = parseQualid("_");
			Qualid q2 = parseQualid("a");
			Qualid q3 = parseQualid("_._");
			
			Assert.assertTrue(n1.equals(q1));
			Assert.assertTrue(q1.equals(n1));
			Assert.assertFalse(n1.equals(q2));
			Assert.assertFalse(q2.equals(n1));
			Assert.assertFalse(n1.equals(q3));
			Assert.assertFalse(q3.equals(n1));
			
			Name n2 = parseName("a");
			
			Qualid q4 = parseQualid("a.a");
			
			Assert.assertFalse(n2.equals(q1));
			Assert.assertFalse(q1.equals(n2));
			Assert.assertTrue(n2.equals(q2));
			Assert.assertTrue(q2.equals(n2));
			Assert.assertFalse(n2.equals(q4));
			Assert.assertFalse(q4.equals(n2));
		}
		
		// qualid x qualid
		{
			Qualid q1 = parseQualid("a");
			Qualid q2 = parseQualid("a");
			
			Assert.assertTrue(q1.equals(q2));
			Assert.assertTrue(q2.equals(q1));
			
			Qualid q3 = parseQualid("b");
			
			Assert.assertFalse(q1.equals(q3));
			Assert.assertFalse(q3.equals(q1));
			
			Qualid q4 = parseQualid("a.b");
			Qualid q5 = parseQualid("a.b");
			
			Assert.assertFalse(q1.equals(q4));
			Assert.assertFalse(q4.equals(q1));
			Assert.assertTrue(q4.equals(q5));
			Assert.assertTrue(q5.equals(q4));
			
			Qualid q6 = parseQualid("b.b");
			
			Assert.assertFalse(q1.equals(q6));
			Assert.assertFalse(q6.equals(q1));
		}
	}

	@Test
	public void testContains1() {
		Term term = parseTerm("(a b c)");
		
		Ident i = parseIdent("a");
		Assert.assertNotEquals(term, i);
		Assert.assertTrue(term.contains(i));
		
		i = parseIdent("b");
		Assert.assertNotEquals(term, i);
		Assert.assertTrue(term.contains(i));
		
		i = parseIdent("c");
		Assert.assertNotEquals(term, i);
		Assert.assertTrue(term.contains(i));
	}
	
}
