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
import edu.jhuapl.exterminator.grammar.coq.term.Ident;
import edu.jhuapl.exterminator.grammar.coq.term.Num;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class Inversion extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.INVERSION() != null || ctx.INVERSION_CLEAR() != null;
    }

    /*
    |   INVERSION (ident|NUM) (AS intro_pattern)?
    |   INVERSION_CLEAR ident (AS intro_pattern)? (IN ident+)?
    |   INVERSION ident (AS intro_pattern)? IN ident+
    |   DEPENDENT INVERSION ident (AS intro_pattern)?
    |   DEPENDENT (INVERSION|INVERSION_CLEAR) ident (AS intro_pattern)? WITH term
    |   SIMPLE INVERSION ident (AS intro_pattern)?
    |   INVERSION ident USING ident TOK_APOSTROPHE (IN ident+)?
     */

    private final boolean isClear;

    private final boolean isDependent;

    private final boolean isSimple;

    private final Ident ident;

    private final Num num;

    private final Ident usingIdent;

    private final IntroPattern asPattern;

    private final Term withTerm;

    private final List<Ident> inIdents;

    public Inversion(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        this.isClear = ctx.INVERSION_CLEAR() != null;
        this.isDependent = ctx.DEPENDENT() != null;
        this.isSimple = ctx.SIMPLE() != null;

        if(ctx.ident() != null && ctx.ident().size() >= 1) {
            this.ident = new Ident(parser, ctx.ident(0));
        } else {
            this.ident = null;
        }

        if(ctx.NUM() != null && ctx.NUM().size() == 1) {
            this.num = new Num(parser, ctx.NUM(0));
        } else {
            this.num = null;
        }

        if(ctx.USING() != null) {
            this.usingIdent = new Ident(parser, ctx.ident(1));
        } else {
            this.usingIdent = null;
        }

        if(ctx.intro_pattern() != null && ctx.intro_pattern().size() == 1) {
            this.asPattern = new IntroPattern(parser, ctx.intro_pattern(0));
        } else {
            this.asPattern = null;
        }

        if(ctx.term() != null && ctx.term().size() == 1) {
            this.withTerm = Term.make(parser, ctx.term(0));
        } else {
            this.withTerm = null;
        }

        this.inIdents = new ArrayList<>();
        int index = usingIdent == null ? 1 : 2;
        if(ctx.ident() != null && ctx.ident().size() > index) {
            for(; index < ctx.ident().size(); index++) {
                inIdents.add(new Ident(parser, ctx.ident(index)));
            }
        }
    }

    protected Inversion(Inversion copy) {
        super(copy);
        this.isClear = copy.isClear;
        this.isDependent = copy.isDependent;
        this.isSimple = copy.isSimple;
        this.ident = copy.ident == null ? null : copy.ident.clone();
        this.num = copy.num == null ? null : copy.num.clone();
        this.usingIdent = copy.usingIdent == null ? null : copy.usingIdent.clone();
        this.asPattern = copy.asPattern == null ? null : copy.asPattern.clone();
        this.withTerm = copy.withTerm == null ? null : copy.withTerm.clone();
        this.inIdents = new ArrayList<>(copy.inIdents.size());
        for(Ident ident : copy.inIdents) {
            this.inIdents.add(ident.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(ident, num, usingIdent, asPattern, withTerm, inIdents);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        // FIXME real implementation
        return CoqDoc.makeTermNode(doc, CoqDoc.makeTextNode(doc, fullText()));
    }

    @Override
    public String getDescription() {
        return "<Inversion description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Inversion)) return false;

        Inversion i = (Inversion)anObj;
        return Objects.equals(isClear, i.isClear) &&
                Objects.equals(isDependent, i.isDependent) &&
                Objects.equals(isSimple, i.isSimple) &&
                Objects.equals(ident, i.ident) &&
                Objects.equals(num, i.num) &&
                Objects.equals(usingIdent, i.usingIdent) &&
                Objects.equals(asPattern, i.asPattern) &&
                Objects.equals(withTerm, i.withTerm) &&
                Objects.equals(inIdents, i.inIdents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isClear, isDependent, isSimple, ident, num,
                usingIdent, asPattern, withTerm, inIdents);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{Inversion");

        if(isClear) sb.append("Clear");

        if(isDependent) sb.append(" dependent");
        if(isSimple) sb.append(" simple");

        if(ident != null) sb.append(" ident=").append(ident);
        if(num != null) sb.append(" num=").append(num);

        if(usingIdent != null) sb.append(" using=").append(usingIdent);
        if(asPattern != null) sb.append(" as=").append(asPattern);

        if(inIdents.size() > 0) sb.append(" in=").append(inIdents);

        if(withTerm != null) sb.append(" with=").append(withTerm);

        sb.append("}");
        return sb.toString();
    }

    @Override
    public Inversion clone() {
        return new Inversion(this);
    }

}
