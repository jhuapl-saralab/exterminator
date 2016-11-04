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
import edu.jhuapl.exterminator.grammar.coq.term.CoqString;
import edu.jhuapl.exterminator.grammar.coq.term.Ident;
import edu.jhuapl.exterminator.grammar.coq.term.Num;
import edu.jhuapl.exterminator.grammar.coq.term.Qualid;

public class Unfold extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.UNFOLD() != null;
    }
    
    /*
    |   UNFOLD qualid (TOK_COMMA qualid)* (IN ident+)?
    |   UNFOLD qualid AT NUM (TOK_COMMA NUM)* (TOK_COMMA qualid AT NUM (TOK_COMMA NUM)*)* (IN ident+)?
    |   UNFOLD STRING (TOK_PERCENT key=ident)? (IN ident+)?
    |   UNFOLD STRING (IN ident+)?
    |   UNFOLD (qualid|STRING) AT NUM (TOK_COMMA NUM)* (TOK_COMMA (qualid|STRING) AT NUM (TOK_COMMA NUM)*)* (IN ident+)?
     */

    private final List<Qualid> qualids;

    private final List<CoqString> strings;

    private final Ident modKeyIdent;

    private final List<Num> atNums;

    private final List<Ident> inIdents;

    public Unfold(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);
        this.qualids = new ArrayList<>();
        for(CoqParser.QualidContext qualid : ctx.qualid()) {
            this.qualids.add(new Qualid(parser, qualid));
        }
        this.strings = new ArrayList<>();
        for(TerminalNode string : ctx.STRING()) {
            this.strings.add(new CoqString(parser, string));
        }
        this.modKeyIdent = ctx.key == null ? null : new Ident(parser, ctx.key);
        this.atNums = new ArrayList<>();
        for(TerminalNode num : ctx.NUM()) {
            this.atNums.add(new Num(parser, num));
        }
        this.inIdents = new ArrayList<>();
        for(CoqParser.IdentContext ident : ctx.ident()) {
            this.inIdents.add(new Ident(parser, ident));
        }
    }

    protected Unfold(Unfold copy) {
        super(copy);
        this.qualids = new ArrayList<>(copy.qualids.size());
        for(Qualid qualid : copy.qualids) {
            this.qualids.add(qualid.clone());
        }
        this.strings = new ArrayList<>(copy.strings.size());
        for(CoqString str : copy.strings) {
            this.strings.add(str.clone());
        }
        this.modKeyIdent = copy.modKeyIdent == null ? null : copy.modKeyIdent.clone();
        this.atNums = new ArrayList<>(copy.atNums.size());
        for(Num num : copy.atNums) {
            this.atNums.add(num.clone());
        }
        this.inIdents = new ArrayList<>(copy.inIdents.size());
        for(Ident ident : copy.inIdents) {
            this.inIdents.add(ident.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(qualids, strings, modKeyIdent, atNums, inIdents);
    }
    
    /*
    |   UNFOLD qualid (TOK_COMMA qualid)* (IN ident+)?
    |   UNFOLD qualid AT NUM (TOK_COMMA NUM)* (TOK_COMMA qualid AT NUM (TOK_COMMA NUM)*)* (IN ident+)?
    |   UNFOLD STRING (TOK_PERCENT key=ident)? (IN ident+)?
    |   UNFOLD STRING (IN ident+)?
    |   UNFOLD (qualid|STRING) AT NUM (TOK_COMMA NUM)* (TOK_COMMA (qualid|STRING) AT NUM (TOK_COMMA NUM)*)* (IN ident+)?
     */
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        // FIXME real implementation
        return CoqDoc.makeTermNode(doc, CoqDoc.makeTextNode(doc, fullText()));
    }

    @Override
    public String getDescription() {
        return "<Unfold description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Unfold)) return false;

        Unfold u = (Unfold)anObj;
        return Objects.equals(qualids, u.qualids) &&
                Objects.equals(strings, u.strings) &&
                Objects.equals(modKeyIdent, u.modKeyIdent) &&
                Objects.equals(atNums, u.atNums) &&
                Objects.equals(inIdents, u.inIdents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualids, strings, modKeyIdent, atNums, inIdents);
    }

    /*
    |   UNFOLD qualid (TOK_COMMA qualid)* (IN IDENT+)?
    |   UNFOLD qualid AT NUM (TOK_COMMA NUM)* (TOK_COMMA qualid AT NUM (TOK_COMMA NUM)*)* (IN IDENT+)?
    |   UNFOLD STRING (TOK_PERCENT key=IDENT)? (IN IDENT+)?
    |   UNFOLD (qualid|STRING) AT NUM (TOK_COMMA NUM)* (TOK_COMMA (qualid|STRING) AT NUM (TOK_COMMA NUM)*)* (IN IDENT+)?
     */

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{Unfold");

        // FIXME

        if(qualids.size() > 0) {

        }

        if(inIdents.size() > 0) {
            sb.append(" in idents=");
            sb.append(inIdents);
        }

        sb.append("}");

        return sb.toString();
    }

    @Override
    public Unfold clone() {
        return new Unfold(this);
    }

}
