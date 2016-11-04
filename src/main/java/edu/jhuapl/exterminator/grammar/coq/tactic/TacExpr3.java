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

public class TacExpr3 extends TacticExpr {

    public static boolean applies(CoqParser.Tacexpr3Context ctx) {
        return ctx.tacexpr3() != null &&
                (ctx.DO() != null || ctx.PROGRESS() != null ||
                ctx.REPEAT() != null || ctx.TRY() != null ||
                ctx.TIMEOUT() != null);
    }
    
    /*
tacexpr3 : DO (NUM | ident) tacexpr3
    |   PROGRESS tacexpr3
    |   REPEAT tacexpr3
    |   TRY tacexpr3
    |   TIMEOUT (NUM | ident) tacexpr3
    |   tacexpr2
    ;
     */

    private final boolean isDo, isProgress, isRepeat, isTry, isTimeout;

    private final Num num;

    private final Ident ident;

    private final TacticExpr inner;

    public TacExpr3(CoqFTParser parser, CoqParser.Tacexpr3Context ctx) {
        super(parser, ctx);
        this.isDo = ctx.DO() != null;
        this.isProgress = ctx.PROGRESS() != null;
        this.isRepeat = ctx.REPEAT() != null;
        this.isTry = ctx.TRY() != null;
        this.isTimeout = ctx.TIMEOUT() != null;
        this.num = ctx.NUM() == null ? null : new Num(parser, ctx.NUM());
        this.ident = ctx.ident() == null ? null : new Ident(parser, ctx.ident());
        this.inner = TacticExpr.make(parser, ctx.tacexpr3());
    }

    protected TacExpr3(TacExpr3 copy) {
        super(copy);
        this.isDo = copy.isDo;
        this.isProgress = copy.isProgress;
        this.isRepeat = copy.isRepeat;
        this.isTry = copy.isTry;
        this.isTimeout = copy.isTimeout;
        this.num = copy.num == null ? null : copy.num.clone();
        this.ident = copy.ident == null ? null : copy.ident.clone();
        this.inner = copy.inner.clone();
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(num, ident, inner);
    }
    
    /*
tacexpr3 : DO (NUM | ident) tacexpr3
    |   PROGRESS tacexpr3
    |   REPEAT tacexpr3
    |   TRY tacexpr3
    |   TIMEOUT (NUM | ident) tacexpr3
    |   tacexpr2
    ;
     */
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        // FIXME real implementation
        return CoqDoc.makeTermNode(doc, CoqDoc.makeTextNode(doc, fullText()));
    }

    @Override
    public String getDescription() {
        if(isDo) {
            return "<Do description.>";
        } else if(isProgress) {
            return "<Progress description.>";
        } else if(isRepeat) {
            return "<Repeat description.>";
        } else if(isTry) {
            return "<Try description.>";
        } else if(isTimeout) {
            return "<Timeout description.>";
        } else {
            return inner.getDescription();
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof TacExpr3)) return false;

        TacExpr3 t = (TacExpr3)anObj;
        return Objects.equals(isDo, t.isDo) &&
                Objects.equals(isProgress, t.isProgress) &&
                Objects.equals(isRepeat, t.isRepeat) &&
                Objects.equals(isTry, t.isTry) &&
                Objects.equals(isTimeout, t.isTimeout) &&
                Objects.equals(num, t.num) &&
                Objects.equals(ident, t.ident) &&
                Objects.equals(inner, t.inner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isDo, isProgress, isRepeat, isTry, isTimeout, num, ident, inner);
    }

    @Override
    public String toString() {
        if(isDo) {
            if(num != null) {
                return "{do num=" + num + " inner=" + inner + "}";
            } else if(ident != null) {
                return "{do ident=" + ident + " inner=" + inner + "}";
            } else {
                return "{do inner=" + inner + "}";
            }
        } else if(isProgress) {
            return "{progress inner=" + inner + "}";
        } else if(isRepeat) {
            return "{repeat inner=" + inner + "}";
        } else if(isTry) {
            return "{try inner=" + inner + "}";
        } else if(isTimeout) {
            if(num != null) {
                return "{timeout num=" + num + " inner=" + inner + "}";
            } else if(ident != null) {
                return "{timeout ident=" + ident + " inner=" + inner + "}";
            } else {
                return "{timeout inner=" + inner + "}";
            }
        } else {
            throw new IllegalArgumentException("shouldn't happen");
        }
    }

    @Override
    public TacExpr3 clone() {
        return new TacExpr3(this);
    }
}
