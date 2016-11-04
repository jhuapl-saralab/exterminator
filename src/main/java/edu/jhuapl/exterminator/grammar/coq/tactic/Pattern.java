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
package edu.jhuapl.exterminator.grammar.coq.tactic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;
import edu.jhuapl.exterminator.grammar.coq.term.Ident;
import edu.jhuapl.exterminator.grammar.coq.term.Num;
import edu.jhuapl.exterminator.grammar.coq.term.Qualid;

public class Pattern extends CoqToken implements CoqDocable {

    /*
pattern : qualid pattern*
    |   pattern AS ident
    |   pattern TOK_PERCENT ident
    |   UNDERSCORE
    |   NUM
    |   TOK_LPAREN or_pattern (TOK_COMMA or_pattern)* TOK_RPAREN
    ;
     */

    private final Qualid qualid;

    private final List<Pattern> qualidPatterns;

    private final boolean isUnderscore;

    private final Num num;

    private final Pattern pattern;

    private final Ident ident;

    private final boolean isAs, isMod;

    private final List<List<Pattern>> orPatterns;

    public Pattern(CoqFTParser parser, CoqParser.PatternContext ctx) {
        super(parser, ctx);
        this.qualid = ctx.qualid() == null ? null : new Qualid(parser, ctx.qualid());
        this.qualidPatterns = new ArrayList<>();
        if(this.qualid != null) {
            for(CoqParser.PatternContext pattern : ctx.pattern()) {
                this.qualidPatterns.add(new Pattern(parser, pattern));
            }
        }
        this.isUnderscore = ctx.UNDERSCORE() != null;
        this.num = ctx.NUM() == null ? null : new Num(parser, ctx.NUM());
        this.isAs = ctx.AS() != null;
        this.isMod = ctx.TOK_PERCENT() != null;
        this.pattern = (isAs || isMod) ? new Pattern(parser, ctx.pattern(0)) : null;
        this.ident = ctx.ident() == null ? null : new Ident(parser, ctx.ident());
        this.orPatterns = new ArrayList<>();
        if(ctx.or_pattern() != null && ctx.or_pattern().size() > 0) {
            for(CoqParser.Or_patternContext or : ctx.or_pattern()) {
                List<Pattern> orp = new ArrayList<>();
                for(CoqParser.PatternContext p : or.pattern()) {
                    orp.add(new Pattern(parser, p));
                }
                this.orPatterns.add(orp);
            }
        }
    }

    protected Pattern(Pattern copy) {
        super(copy);
        this.qualid = copy.qualid == null ? null : copy.qualid.clone();
        this.qualidPatterns = new ArrayList<>(copy.qualidPatterns.size());
        for(Pattern pattern : copy.qualidPatterns) {
            this.qualidPatterns.add(pattern.clone());
        }
        this.isUnderscore = copy.isUnderscore;
        this.num = copy.num == null ? null : copy.num.clone();
        this.pattern = copy.pattern == null ? null : copy.pattern.clone();
        this.ident = copy.ident == null ? null : copy.ident.clone();
        this.isAs = copy.isAs;
        this.isMod = copy.isMod;
        this.orPatterns = new ArrayList<>(copy.orPatterns.size());
        for(List<Pattern> patterns : copy.orPatterns) {
            List<Pattern> patternsCopy = new ArrayList<>(patterns.size());
            for(Pattern pattern : patterns) {
                patternsCopy.add(pattern.clone());
            }
            this.orPatterns.add(patternsCopy);
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        List<CoqToken> list = makeList(qualid, qualidPatterns, num, pattern, ident);
        for(List<Pattern> pattern : orPatterns) {
            list.addAll(pattern);
        }
        return list;
    }

    @Override
    public boolean isTerminalNode() { return false; }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc);
        List<Element> elems = new ArrayList<>();
        if(qualid != null) {
            if(qualidPatterns.size() > 0) {
                elems.add(CoqDoc.makeKeywordNode(doc, "("));
            }
            elems.add(CoqDoc.makeIdentifierNode(doc, qualid));
            if(qualidPatterns.size() > 0) {
                for(Pattern pattern : qualidPatterns) {
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                    elems.add(pattern.makeCoqDocTerm(doc));
                }
                elems.add(CoqDoc.makeKeywordNode(doc, ")"));
            }
        } else if(isAs) {
            elems.add(CoqDoc.makeKeywordNode(doc, "("));
            elems.add(pattern.makeCoqDocTerm(doc));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "as"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeIdentifierNode(doc, ident));
            elems.add(CoqDoc.makeKeywordNode(doc, ")"));
        } else if(isMod) {
            elems.add(CoqDoc.makeKeywordNode(doc, "("));
            elems.add(pattern.makeCoqDocTerm(doc));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "%"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeIdentifierNode(doc, ident));
            elems.add(CoqDoc.makeKeywordNode(doc, ")"));
        } else if(isUnderscore) {
            elems.add(CoqDoc.makeKeywordNode(doc, "_"));
        } else if(num != null) {
            elems.add(CoqDoc.makeTextNode(doc, num.fullText()));
        } else {
            elems.add(CoqDoc.makeKeywordNode(doc, "("));
            for(int i = 0; i < orPatterns.size(); i++) {
                if(i > 0) {
                    elems.add(CoqDoc.makeKeywordNode(doc, ","));
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                }
                List<Pattern> patterns = orPatterns.get(i);
                if(patterns.size() > 0) {
                    elems.add(CoqDoc.makeKeywordNode(doc, "("));
                }
                for(int j = 0; j < patterns.size(); j++) {
                    if(j > 0) {
                        elems.add(CoqDoc.makeWhitespaceNode(doc));
                        elems.add(CoqDoc.makeKeywordNode(doc, "|"));
                        elems.add(CoqDoc.makeWhitespaceNode(doc));
                    }
                    elems.add(patterns.get(j).makeCoqDocTerm(doc));
                }
                if(patterns.size() > 0) {
                    elems.add(CoqDoc.makeKeywordNode(doc, ")"));
                }
            }
            elems.add(CoqDoc.makeKeywordNode(doc, ")"));
        }
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Pattern)) return false;

        Pattern p = (Pattern)anObj;
        return Objects.equals(qualid, p.qualid) &&
                Objects.equals(qualidPatterns, p.qualidPatterns) &&
                Objects.equals(isUnderscore, p.isUnderscore) &&
                Objects.equals(num, p.num) &&
                Objects.equals(pattern, p.pattern) &&
                Objects.equals(ident, p.ident) &&
                Objects.equals(isAs, p.isAs) &&
                Objects.equals(isMod, p.isMod) &&
                Objects.equals(orPatterns, p.orPatterns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualid, qualidPatterns, isUnderscore, num, pattern,
                ident, isAs, isMod, orPatterns);
    }

    @Override
    public String toString() {
        if(qualid != null) {
            return "{Pattern qualid=" + qualid + " patterns=" + qualidPatterns + "}";
        } else if(isAs) {
            return "{Pattern pattern=" + pattern + " as ident=" + ident + "}";
        } else if(isMod) {
            return "{Pattern pattern=" + pattern + " % ident=" + ident + "}";
        } else if(isUnderscore) {
            return "{Pattern _}";
        } else if(num != null) {
            return "{Pattern num=" + num + "}";
        } else {
            return "{Pattern orPatterns=" + orPatterns + "}";
        }
    }

    @Override
    public Pattern clone() {
        return new Pattern(this);
    }

}
