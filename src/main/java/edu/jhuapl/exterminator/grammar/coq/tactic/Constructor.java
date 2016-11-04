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
import edu.jhuapl.exterminator.grammar.coq.term.Num;

public class Constructor extends TacticExpr {

    public static boolean applies(CoqParser.Atomic_tacticContext ctx) {
        return ctx.CONSTRUCTOR() != null ||
                ctx.ECONSTRUCTOR() != null ||
                ctx.SPLIT() != null ||
                ctx.ESPLIT() != null ||
                ctx.EXISTS() != null ||
                ctx.EEXISTS() != null ||
                ctx.LEFT() != null ||
                ctx.ELEFT() != null ||
                ctx.CLASSICAL_LEFT() != null ||
                ctx.RIGHT() != null ||
                ctx.ERIGHT() != null ||
                ctx.CLASSICAL_RIGHT() != null;
    }

    /*
    |   (CONSTRUCTOR|ECONSTRUCTOR) NUM?
    |   CONSTRUCTOR NUM WITH bindings_list
    |   (SPLIT|ESPLIT) (WITH bindings_list)?
    |   (EXISTS|EEXISTS) bindings_list (TOK_COMMA bindings_list)*
    |   (LEFT|ELEFT) (WITH bindings_list)?
    |   (RIGHT|ERIGHT) (WITH bindings_list)?
     */

    private final boolean isConstructor, isEConstructor, isSplit, isESplit,
        isExists, isEExists, isLeft, isELeft, isClassicalLeft, isRight,
        isERight, isClassicalRight;

    private final Num num;

    private final List<BindingsList> bindingsLists;

    protected Constructor(CoqFTParser parser, CoqParser.Atomic_tacticContext ctx) {
        super(parser, ctx);

        this.isConstructor = ctx.CONSTRUCTOR() != null;
        this.isEConstructor = ctx.ECONSTRUCTOR() != null;
        this.isSplit = ctx.SPLIT() != null;
        this.isESplit = ctx.ESPLIT() != null;
        this.isExists = ctx.EXISTS() != null;
        this.isEExists = ctx.EEXISTS() != null;
        this.isLeft = ctx.LEFT() != null;
        this.isELeft = ctx.ELEFT() != null;
        this.isClassicalLeft = ctx.CLASSICAL_LEFT() != null;
        this.isRight = ctx.RIGHT() != null;
        this.isERight = ctx.ERIGHT() != null;
        this.isClassicalRight = ctx.CLASSICAL_RIGHT() != null;

        if(ctx.NUM() != null && ctx.NUM().size() == 1) {
            this.num = new Num(parser, ctx.NUM(0));
        } else {
            this.num = null;
        }

        this.bindingsLists = new ArrayList<>();
        if(ctx.bindings_list() != null && ctx.bindings_list().size() > 0) {
            for(CoqParser.Bindings_listContext bl : ctx.bindings_list()) {
                this.bindingsLists.add(new BindingsList(parser, bl));
            }
        }
    }

    protected Constructor(Constructor copy) {
        super(copy);
        this.isConstructor = copy.isConstructor;
        this.isEConstructor = copy.isEConstructor;
        this.isSplit = copy.isSplit;
        this.isESplit = copy.isESplit;
        this.isExists = copy.isExists;
        this.isEExists = copy.isEExists;
        this.isLeft = copy.isLeft;
        this.isELeft = copy.isELeft;
        this.isClassicalLeft = copy.isClassicalLeft;
        this.isRight = copy.isRight;
        this.isERight = copy.isERight;
        this.isClassicalRight = copy.isClassicalRight;
        this.num = copy.num == null ? null : copy.num.clone();
        this.bindingsLists = new ArrayList<>(copy.bindingsLists.size());
        for(BindingsList list : copy.bindingsLists) {
            this.bindingsLists.add(list.clone());
        }
    }

    @Override
    public List<CoqToken> getChildren() {
        return makeList(num, bindingsLists);
    }
    
    @Override
    public Element makeCoqDocTerm(Document doc) {
        Element elem = CoqDoc.makeTermNode(doc);
        List<Element> elems = new ArrayList<>();

        if(isConstructor) {
            elems.add(CoqDoc.makeKeywordNode(doc, "constructor"));
        } else if(isEConstructor) {
            elems.add(CoqDoc.makeKeywordNode(doc, "econstructor"));
        } else if(isSplit) {
            elems.add(CoqDoc.makeKeywordNode(doc, "split"));
        } else if(isESplit) {
            elems.add(CoqDoc.makeKeywordNode(doc, "esplit"));
        } else if(isExists) {
            elems.add(CoqDoc.makeKeywordNode(doc, "exists"));
        } else if(isEExists) {
            elems.add(CoqDoc.makeKeywordNode(doc, "eexists"));
        } else if(isLeft) {
            elems.add(CoqDoc.makeKeywordNode(doc, "left"));
        } else if(isELeft) {
            elems.add(CoqDoc.makeKeywordNode(doc, "eleft"));
        } else if(isClassicalLeft) {
            elems.add(CoqDoc.makeKeywordNode(doc, "classical"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "left"));
        } else if(isRight) {
            elems.add(CoqDoc.makeKeywordNode(doc, "right"));
        } else if(isERight) {
            elems.add(CoqDoc.makeKeywordNode(doc, "eright"));
        } else if(isClassicalRight) {
            elems.add(CoqDoc.makeKeywordNode(doc, "classical"));
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeKeywordNode(doc, "right"));
        } else {
            elems.add(CoqDoc.makeKeywordNode(doc, "constructor????????"));
        }

        if(num != null) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            elems.add(CoqDoc.makeTextNode(doc, num.fullText()));
        }

        if(bindingsLists.size() > 0) {
            elems.add(CoqDoc.makeWhitespaceNode(doc));
            if(bindingsLists.size() > 1) {
                elems.add(CoqDoc.makeKeywordNode(doc, "("));
            }
            for(int i = 0; i < bindingsLists.size(); i++) {
                if(i > 0) {
                    elems.add(CoqDoc.makeKeywordNode(doc, "("));
                    elems.add(CoqDoc.makeWhitespaceNode(doc));
                }
                elems.add(bindingsLists.get(i).makeCoqDocTerm(doc));
            }
            if(bindingsLists.size() > 1) {
                elems.add(CoqDoc.makeKeywordNode(doc, ")"));
            }
        }

        return CoqDoc.mergeTermNodes(elem, elems);
    }

    @Override
    public String getDescription() {
        return "<Constructor description.>";
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object anObj) {
        if(anObj == this) return true;
        if(anObj == null || !(anObj instanceof Constructor)) return false;

        Constructor c = (Constructor)anObj;
        return Objects.equals(isConstructor, c.isConstructor) &&
                Objects.equals(isEConstructor, c.isEConstructor) &&
                Objects.equals(isSplit, c.isSplit) &&
                Objects.equals(isESplit, c.isESplit) &&
                Objects.equals(isExists, c.isExists) &&
                Objects.equals(isEExists, c.isEExists) &&
                Objects.equals(isLeft, c.isLeft) &&
                Objects.equals(isELeft, c.isELeft) &&
                Objects.equals(isClassicalLeft, c.isClassicalLeft) &&
                Objects.equals(isRight, c.isRight) &&
                Objects.equals(isERight, c.isERight) &&
                Objects.equals(isClassicalRight, c.isClassicalRight) &&
                Objects.equals(num, c.num) &&
                Objects.equals(bindingsLists, c.bindingsLists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isConstructor, isEConstructor, isSplit, isESplit,
                isExists, isEExists, isLeft, isELeft, isClassicalLeft,
                isRight, isERight, isClassicalRight, num, bindingsLists);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        if(isConstructor) {
            sb.append("Constructor");
        } else if(isEConstructor) {
            sb.append("EConstructor");
        } else if(isSplit) {
            sb.append("Split");
        } else if(isESplit) {
            sb.append("ESplit");
        } else if(isExists) {
            sb.append("Exists");
        } else if(isEExists) {
            sb.append("EExists");
        } else if(isLeft) {
            sb.append("Left");
        } else if(isELeft) {
            sb.append("ELeft");
        } else if(isClassicalLeft) {
            sb.append("ClassicalLeft");
        } else if(isRight) {
            sb.append("Right");
        } else if(isERight) {
            sb.append("ERight");
        } else if(isClassicalRight) {
            sb.append("ClassicalRight");
        } else {
            sb.append("Constructor????????");
        }

        if(num != null) {
            sb.append(" num=").append(num);
        }

        if(bindingsLists.size() > 0) {
            sb.append(" with=").append(bindingsLists);
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public Constructor clone() {
        return new Constructor(this);
    }

}
