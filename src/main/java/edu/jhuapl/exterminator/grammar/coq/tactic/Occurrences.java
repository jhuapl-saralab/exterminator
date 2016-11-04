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

import org.antlr.v4.runtime.tree.TerminalNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.CoqParser;
import edu.jhuapl.exterminator.grammar.coq.CoqToken;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc;
import edu.jhuapl.exterminator.grammar.coq.document.CoqDoc.CoqDocable;
import edu.jhuapl.exterminator.grammar.coq.term.Num;

public class Occurrences extends CoqToken implements CoqDocable {

    /*
occurrences : TOK_DASH? NUM+;
     */
    
    private final boolean hasDash;

    private final List<Num> nums;

    public Occurrences(CoqFTParser parser, CoqParser.OccurrencesContext ctx) {
        super(parser, ctx);

        this.hasDash = ctx.TOK_DASH() != null;
        this.nums = new ArrayList<>();
        if(ctx.NUM() != null && ctx.NUM().size() > 0) {
            for(TerminalNode num : ctx.NUM()) {
                this.nums.add(new Num(parser, num));
            }
        }
    }

    protected Occurrences(Occurrences copy) {
        super(copy);
        this.hasDash = copy.hasDash;
        this.nums = new ArrayList<>(copy.nums.size());
        for(Num num : copy.nums) {
            this.nums.add(num.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(nums);
    }

    @Override
    public boolean isTerminalNode() { return false; }
    
    /*
occurrences : TOK_DASH? NUM+;
     */
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc);
        List<Element> elems = new ArrayList<>();
        if(hasDash) {
            elems.add(CoqDoc.makeKeywordNode(doc, "-"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
        }
        elems.add(nums.get(0).makeCoqDocTerm(doc));
        for(int i = 1; i < nums.size(); i++) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(nums.get(i).makeCoqDocTerm(doc));
        }
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Occurrences)) return false;

        Occurrences o = (Occurrences)anObj;
        return Objects.equals(hasDash, o.hasDash) &&
                Objects.equals(nums, o.nums);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasDash, nums);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{Occurrences ");
        if(hasDash) sb.append("- ");
        sb.append("nums=").append(nums).append("}");
        return sb.toString();
    }

    // occurrences : TOK_DASH? NUM+;

    @Override
    public Occurrences clone() {
        return new Occurrences(this);
    }

}
