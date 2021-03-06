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
import edu.jhuapl.exterminator.grammar.coq.term.Qualid;

public class Function extends TacticExpr {
    
    /*
    |   qualid tacarg+
     */

    public static boolean applies(CoqParser.Tacexpr1Context ctx) {
        return ctx.qualid() != null && ctx.tacarg() != null && ctx.tacarg().size() > 0;
    }

    private final Qualid qualid;

    private final List<TacArg> args;

    public Function(CoqFTParser parser, CoqParser.Tacexpr1Context ctx) {
        super(parser, ctx);
        this.qualid = new Qualid(parser, ctx.qualid());
        this.args = new ArrayList<>();
        for(CoqParser.TacargContext arg : ctx.tacarg()) {
            this.args.add(new TacArg(parser, arg));
        }
    }

    protected Function(Function copy) {
        super(copy);
        this.qualid = copy.qualid.clone();
        this.args = new ArrayList<>(copy.args.size());
        for(TacArg arg : copy.args) {
            this.args.add(arg.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(qualid, args);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc,
                CoqDoc.makeIdentifierNode(doc, qualid));
        List<Element> elems = new ArrayList<>();
        for(TacArg arg : args) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(arg.makeCoqDocTerm(doc));
        }
        return CoqDoc.mergeTermNodes(elem, elems);
    }

    @Override
    public String getDescription() {
        return "This tactic applies a function to the given arguments.";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Function)) return false;

        Function f = (Function)anObj;
        return Objects.equals(qualid, f.qualid) &&
                Objects.equals(args, f.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualid, args);
    }

    @Override
    public String toString() {
        return "{TacticFunction qualid=" + qualid + " args=" + args + "}";
    }

    @Override
    public Function clone() {
        return new Function(this);
    }

}
