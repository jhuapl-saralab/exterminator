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

import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

public class CoqSyntaxException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public static boolean DEBUG_SHOW_FULL_TEXT = true;

	public CoqSyntaxException(CoqFTParser parser, Token offendingToken,
			int line, int charPositionInLine, String msg,
			RecognitionException e) {
		super(makeMessage(parser, offendingToken, line, charPositionInLine,
				msg), e);
	}
	
	private static String makeMessage(CoqFTParser parser, Token offendingToken,
			int line, int charPositionInLine, String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("ANTLR SYNTAX ERROR\n");
		
		sb.append("Offending line:\n");
		sb.append(underlineError(parser, offendingToken, line,
				charPositionInLine)).append("\n\n");
		
		sb.append("Rule stack:\n");
		List<String> stack = parser.getRuleInvocationStack();
		Collections.reverse(stack);
		sb.append(stack).append("\n\n");
		
		sb.append("Message:\n");
		sb.append("line ").append(line).append(":");
		sb.append(charPositionInLine).append(" ").append(msg);
		
		if(DEBUG_SHOW_FULL_TEXT) {
			sb.append("\n\nFull text:\n");
			CommonTokenStream tokens = (CommonTokenStream)parser.getInputStream();
			sb.append(tokens.getTokenSource().getInputStream().toString());
		}
		
		return sb.toString();
	}
	
	protected static String underlineError(CoqFTParser parser,
			Token offendingToken, int line, int charPositionInLine) {
		StringBuilder sb = new StringBuilder();
		
		CommonTokenStream tokens = (CommonTokenStream)parser.getInputStream();
		String input = tokens.getTokenSource().getInputStream().toString();
		String[] lines = input.split("\n");
		String errorLine = lines[line - 1];
		
		sb.append(errorLine).append('\n');
		
		for (int i=0; i<charPositionInLine; i++) sb.append(" ");
		int start = offendingToken.getStartIndex();
		int stop = offendingToken.getStopIndex();
		if ( start>=0 && stop>=0 ) {
			for (int i=start; i<=stop; i++) sb.append("^");
		}
		return sb.toString();
	}
}
