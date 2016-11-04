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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public abstract class CoqToken implements Cloneable {

	protected final FullText fullText;
	
	public CoqToken(CoqFTParser parser, ParserRuleContext context) {
		this.fullText = new FullText(parser.getFullText(context));
	}
	
	public CoqToken(TerminalNode node) {
		this.fullText = new FullText(node.getText());
	}
	
	public CoqToken(Token node) {
		this.fullText = new FullText(node.getText());
	}
	
	protected CoqToken(String fullText) {
		// please only use this if you know what you're doing
		this.fullText = new FullText(fullText);
	}
	
	protected CoqToken(CoqToken copy) {
		this.fullText = copy.fullText;
	}
	
	public String fullText() { return fullText.text; }

	public abstract List<CoqToken> getChildren();
	
	public boolean contains(CoqToken token) {
		Objects.requireNonNull(token);
		
		for(CoqToken child : getChildren()) {
			if(child != null && (child.equals(token) || child.contains(token)))
				return true;
		}
		
		return false;
	}
	
	public List<CoqToken> getParentsOf(CoqToken token) {
		Objects.requireNonNull(token);
		
		List<CoqToken> list = new ArrayList<>();
		
		boolean addThis = false;
		for(CoqToken child : getChildren()) {
			if(child == null) continue;
			
			if(child.equals(token)) {
				addThis = true;
			} else {
				list.addAll(child.getParentsOf(token));
			}
		}
		
		if(addThis && !list.contains(this)) list.add(this);
		
		return list;
	}
	
	public boolean shouldParenthesize() {
		return ! isTerminalNode();
	}
	
	public abstract boolean isTerminalNode();
	
	@Override
	public abstract CoqToken clone();
	
	///////////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked")
	protected static <T> List<T> makeList(Object... objects) {
		List<T> list = new ArrayList<>();
		if(objects == null || objects.length == 0) return list;
		for(Object object : objects) {
			if(object == null) continue;
			if(object instanceof Collection) {
				list.addAll((Collection<T>)object);
			} else if(object instanceof Map<?, ?>) {
				for(Map.Entry<?, ?> entry : ((Map<?, ?>)object).entrySet()) {
					list.add((T)entry.getKey());
					list.add((T)entry.getValue());
				}
			} else {
				list.add((T)object);
			}
		}
		return list;
	}
	
	protected class FullText {
		
		private String text;
		
		public FullText(String text) {
			this.text = Objects.requireNonNull(text);
		}
		
		public void append(String text) {
			Objects.requireNonNull(text);
			this.text += text;
		}
		
		@Override
		public boolean equals(Object anObj) {
			if(anObj == this) return true;
			if(anObj == null) return false;
			if(anObj instanceof FullText) {
				return text.equals(((FullText)anObj).text);
			} else if(anObj instanceof String) {
				return text.equals((String)anObj);
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return text.hashCode();
		}
		
		@Override
		public String toString() {
			return text;
		}
		
	}
}
