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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.junit.Assert;
import org.junit.Test;

import edu.jhuapl.exterminator.grammar.ANTLRUtils;
import edu.jhuapl.exterminator.grammar.coq.sentence.Proof;
import edu.jhuapl.exterminator.grammar.coq.tactic.Apply;
import edu.jhuapl.exterminator.grammar.coq.tactic.BindingsList;
import edu.jhuapl.exterminator.grammar.coq.tactic.DisjConjIntroPattern;
import edu.jhuapl.exterminator.grammar.coq.tactic.TacticExpr;
import edu.jhuapl.exterminator.grammar.coq.term.Arg;
import edu.jhuapl.exterminator.grammar.coq.term.Binder;
import edu.jhuapl.exterminator.grammar.coq.term.Completes;
import edu.jhuapl.exterminator.grammar.coq.term.Deref;
import edu.jhuapl.exterminator.grammar.coq.term.Forall;
import edu.jhuapl.exterminator.grammar.coq.term.Fun;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.grammar.coq.term.NamedFields;
import edu.jhuapl.exterminator.grammar.coq.term.None;
import edu.jhuapl.exterminator.grammar.coq.term.Num;
import edu.jhuapl.exterminator.grammar.coq.term.Qualid;
import edu.jhuapl.exterminator.grammar.coq.term.Refterm;
import edu.jhuapl.exterminator.grammar.coq.term.SepConjunction;
import edu.jhuapl.exterminator.grammar.coq.term.Some;
import edu.jhuapl.exterminator.grammar.coq.term.StoreBound;
import edu.jhuapl.exterminator.grammar.coq.term.Term;
import edu.jhuapl.exterminator.grammar.coq.term.TypeCast;
import edu.jhuapl.exterminator.grammar.coq.term.expression.AndExpression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.EqualsExpression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.Expression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.GreaterThanExpression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.Implies;
import edu.jhuapl.exterminator.grammar.coq.term.expression.LessThanExpression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.NotExpression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.OrExpression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.TrueExpression;
import edu.jhuapl.exterminator.grammar.coq.term.expression.WhileExpression;

public class GrammarTest {

    private CoqFTParser parser;

    private static CoqFTParser parser(String line) {
        CoqLexer lexer = new CoqLexer(new ANTLRInputStream(line));
        CoqFTParser parser = new CoqFTParser(new CommonTokenStream(lexer));
        return parser;
    }

    private List<CoqParser.CommandContext> parseProg(String line) {
        parser = parser(line);
        CoqParser.ProgContext ctx = parser.prog();
        Assert.assertNotNull(ctx);

        return ctx.command();
    }

    protected void print(ParserRuleContext ctx) {
        System.out.println(ANTLRUtils.tokenTree(ctx));
    }

    private CoqParser.TermContext parseTerm(String termLine) {
        //String line = "Definition Temp := " + termLine + " .";
        //printTokens(line);
        //		List<CoqParser.CommandContext> commands = parseProg(line);
        //		Assert.assertEquals(1, commands.size());
        //		Assert.assertNotNull(commands.get(0).sentence());
        //		Assert.assertNotNull(commands.get(0).sentence().definition());
        //		CoqParser.TermContext t = commands.get(0).sentence().definition().def_term;
        //		
        ////		print(t);
        ////		System.out.println(Expression.isArgChain(t));
        //		
        //		return t;
        parser = parser(termLine);
        CoqParser.TermContext ctx = parser.term();
        Assert.assertNotNull(ctx);

        return ctx;
    }

    @Test
    public void testRequire() {
        String line = "Require Import Lists.List.";
        List<CoqParser.CommandContext> commands = parseProg(line);
        Assert.assertEquals(1, commands.size());
        CoqParser.RequireContext require = commands.get(0).require();
        Assert.assertNotNull(require);
        Assert.assertEquals("Lists.List", require.module_name.getText());
    }

    @Test
    public void testNum() {
        String line = "0";
        CoqParser.TermContext term = parseTerm(line);
        Assert.assertTrue(Term.make(parser, term) instanceof Num);

        line = "-1";
        term = parseTerm(line);
        Assert.assertTrue(Term.make(parser, term) instanceof Num);
    }

    @Test
    public void testExpr() {
        String expr = "(store_bits table::str::nil l)";
        CoqParser.TermContext term = parseTerm(expr);
        //System.out.println(term.toStringTree(Arrays.asList(CoqParser.ruleNames)));
        Assert.assertTrue(Term.make(parser, term) instanceof Expression);

        expr = "(1 + 2)";
        term = parseTerm(expr);
        Assert.assertTrue(Term.make(parser, term) instanceof Expression);
    }

    @Test
    public void testExpr2() {
        String expr = "(lift_prop_sprop (eimm straddr = (vaddr 0)) ∨ (heap_string straddr sv))";
        CoqParser.TermContext term = parseTerm(expr);
        //System.out.println(term.toStringTree(Arrays.asList(CoqParser.ruleNames)));
        Assert.assertTrue(Term.make(parser, term) instanceof Expression);
    }

    @Test
    public void testExpr3() {
        String expr = "((prev == 0)|| ((prev + _next)%expr == e))";
        //String expr = "((prev + _next)%expr == e)";
        CoqParser.TermContext term = parseTerm(expr);
        //System.out.println(term.toStringTree(Arrays.asList(CoqParser.ruleNames)));
        Assert.assertTrue(Term.make(parser, term) instanceof Expression);
    }

    public void printTokens(String str) {
        CoqLexer lexer = new CoqLexer(new ANTLRInputStream(str));
        for(Token token = lexer.nextToken() ; token.getType() != Token.EOF ; token = lexer.nextToken()) {
            System.out.println(token + " " + lexer.getVocabulary().getSymbolicName(token.getType()));
        }
    }

    @Test
    public void testExpr4() {
        String expr = "(simpl (sprop_of_bexpr (!(e == 0)))\n((strcmp_nullable (e+_str)%expr str)==cmp))";
        //printTokens(expr);
        CoqParser.TermContext term = parseTerm(expr);
        //System.out.println(term.toStringTree(Arrays.asList(CoqParser.ruleNames)));
        Assert.assertTrue(Term.make(parser, term) instanceof Expression);     
    }

    @Test
    public void testExpr5() {
        String expr = "(store_bits (x::y::nil) (lc st))";
        //printTokens(expr);
        CoqParser.TermContext term = parseTerm(expr);
        //System.out.println(term.toStringTree(Arrays.asList(CoqParser.ruleNames)));
        Assert.assertTrue(Term.make(parser, term) instanceof Expression);     
    }

    @Test
    public void testExpr6() {
        String expr = "s | y";
        CoqParser.TermContext term = parseTerm(expr);
        //print(term);
        Term t = Term.make(parser, term);
        Assert.assertTrue(t instanceof Expression);
        Expression e = (Expression)t;
        Assert.assertTrue(e.getTerm() instanceof ID);
        Assert.assertEquals("s", ((ID)e.getTerm()).getFullName());
        Assert.assertEquals(2, e.getArgs().size());
        Arg a = e.getArgs().get(0);
        Assert.assertTrue(a.getTerm() instanceof ID);
        Assert.assertEquals("|", ((ID)a.getTerm()).getFullName());
        a = e.getArgs().get(1);
        Assert.assertTrue(a.getTerm() instanceof ID);
        Assert.assertEquals("y", ((ID)a.getTerm()).getFullName());
    }

    @Test
    public void testExpr7() {
        String expr = "{| srf := s'; lc := l; hpf := h; dm := d |} res = Some vy";
        CoqParser.TermContext ctx = parseTerm(expr);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof Expression);

        expr = "((Val.ltb v vy = true) ->\n" +
                "({| srf := s'; lc := l; hpf := h; dm := d |} res = Some vy))";
        ctx = parseTerm(expr);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof Implies);

        expr = "((Val.ltb v vy = true) ->\n" +
                "({| srf := s'; lc := l; hpf := h; dm := d |} res = Some vy)) /\\\n" +
                "((Val.ltb v vy = false) ->\n" +
                "({| srf := s'; lc := l; hpf := h; dm := d |} res = Some v))";
        ctx = parseTerm(expr);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof AndExpression);
    }

    @Test
    public void testImplies() {
        String str = "((Val.ltb vx vy) = true -> (res ≐ vy) st2)";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof Implies);

        str = "(Val.ltb vx vy = true -> (res ≐ vy) st2) /\\\n" +
                "(Val.ltb vx vy = false -> (res ≐ vx) st2)";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof AndExpression);

        AndExpression a = (AndExpression)term;
        Assert.assertTrue(a.getLeft() instanceof Implies);
        Assert.assertTrue(a.getRight() instanceof Implies);

        str = "forall a : Dom.elt, Dom.In a d -> Dom.In a d";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof Forall);
        Assert.assertTrue(((Forall)term).getTerm() instanceof Implies);
    }

    @Test
    public void testWhile() {
        String expr = "(! (e == (vaddr 0))) && (cmp < 0)";
        CoqParser.TermContext ctx = parseTerm(expr);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof AndExpression);

        expr = "(while ((! (e == (vaddr 0))) && (cmp < 0))\n" +
                "           ((prev ≔ e);;\n" +
                "            (__tmp ≔ (e + _next));;\n" +
                "            (e ≔[__tmp]);;\n" +
                "            (ifelse (! (e == 0))\n" +
                "                    ((__tmp ≔ (e + _str));;\n" +
                "                     (__tmp ≔[__tmp]);;\n" +
                "                     (cmp ≔ (strcmp_nullable __tmp str)))\n" +
                "                    skip)))";
        ctx = parseTerm(expr);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof WhileExpression);
    }

    @Test
    public void testExpr8() {
        String expr = "a % b";
        CoqParser.TermContext ctx = parseTerm(expr);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof Expression);

        expr = "  forall (d : domain),       \n" +
                "    (a)\n" +
                "    ->\n" +
                "    (completes {|srf := s ; lc := l; hpf := h; dm := d |}  st2\n" +
                "    (\n" +
                "    local retPtr;;\n" +
                "\n" +
                "    (ifelse (cmp == 0)\n" +
                "            (* found string in the table *)\n" +
                "            (\n" +
                "              (__tmp ≔ (e + _id));;\n" +
                "              (idx ≔[__tmp]);;\n" +
                "              (ifelse (! (0 == preexists))\n" +
                "                      ([preexists]≔ 1)\n" +
                "                      skip);;\n" +
                "              (ifelse (! (0 == retPtr))\n" +
                "                      ((__tmp ≔ (e + _str));;\n" +
                "                       (__tmp ≔[__tmp]);;\n" +
                "                       ([retPtr]≔ __tmp))\n" +
                "                      skip))\n" +
                "\n" +
                "            (* went through the table, came up empty *)\n" +
                "            (\n" +
                "              (__tmp6 ≔ (table + _nr_entries));;\n" +
                "              (__tmp7 ≔[__tmp6]);;\n" +
                "              (ifelse (__tmp7 == 300)\n" +
                "                      ret skip);;\n" +
                "              \n" +
                "              (ifelse (! (0==preexists))\n" +
                "                      ([preexists]≔ 0) skip);;\n" +
                "              \n" +
                "              〈 errno, new 〉 ≔ alloc( 3 );;\n" +
                "\n" +
                "              (ifelse (! (errno == ERR_ERROR)) (\n" +
                "                        (__tmp ≔ (new + _str));;   (* new->str = str *)\n" +
                "                        ([__tmp]≔ str);;\n" +
                "                        (__tmp ≔ (new + _id));;    (* new->id = table->nr_entries (orig) *)\n" +
                "                        ([__tmp]≔ __tmp7);;\n" +
                "                        (idx ≔__tmp7);;            (* idx = table->nr_entries (orig) *)\n" +
                "\n" +
                "                        (__tmp ≔ (__tmp7 + 1));;\n" +
                "                        ([__tmp6]≔ __tmp);;        (* table->nr_entries++ *)\n" +
                "\n" +
                "                        (__tmp ≔ (new + _next));;  (* new->next = e *)\n" +
                "                        ([__tmp]≔ e);;\n" +
                "                        (ifelse (! (0 == prev) )\n" +
                "                                ((__tmp ≔ (prev + _next));;([__tmp]≔ new))\n" +
                "                                ((__tmp ≔ (table + _first));;([__tmp]≔ new)));;\n" +
                "                        (ifelse (! (0 == retPtr))\n" +
                "                                ([retPtr]≔ str)\n" +
                "                                skip))\n" +
                "                      ( (errno ≔ ERR_ERROR);;\n" +
                "                        (idx ≔ (-1)%Z)))));; (* -1 *)\n" +
                "    ret;;\n" +
                "    skip))\n" +
                "\n" +
                "    -> (a)";

        //		expr = "  forall (table str retPtr preexists errno (* prev new *) e idx cmp \n" +
        //"          __tmp __tmp6 __tmp7 prev new : var)\n" +
        //"         (taddr : addr)\n" +
        //"         (straddr : addr)\n" +
        //"         (alst : list addr)\n" +
        //"         (elist : list table_entry)\n" +
        //"         (st1 st2 : state) sv (s : store_f)(h : heap_f)(l : locals)(d : domain),       \n" +
        //"       (* str points to a string with value sv*)\n" +
        //"    ((lift_prop_sprop (store_bits (table::str::nil) l) ☆\n" +
        //"     (str ≐ (vaddr straddr)) ☆\n" +
        //"     (lift_prop_sprop (eimm straddr = (vaddr 0)) ∨ \n" +
        //"                      (heap_string straddr sv)) ☆ \n" +
        //"     (* table points to a well-formed strtab *)\n" +
        //"     (table ≐ (vaddr taddr)) ☆\n" +
        //"     (strtab_wf taddr alst elist))\n" +
        //"      {|srf := s ; lc := l ; hpf := h ; dm := d|})\n" +
        //"    ->\n" +
        //"    (completes {|srf := s ; lc := l; hpf := h; dm := d |}  st2\n" +
        //"    (\n" +
        //"    local retPtr;;\n" +
        //"    local preexists;;\n" +
        //"    local errno;;\n" +
        //"    \n" +
        //"    local prev;;\n" +
        //"    local new;;\n" +
        //"    local e;;\n" +
        //"    local idx;;\n" +
        //"    local cmp;;\n" +
        //"\n" +
        //"    local __tmp;;\n" +
        //"    \n" +
        //"    local __tmp6;;\n" +
        //"    local __tmp7;;\n" +
        //"\n" +
        //"    (prev ≔ 0);;\n" +
        //"    (new ≔ 0);;\n" +
        //"    (__tmp ≔ (table + _first));;\n" +
        //"    (e ≔[ __tmp]);;\n" +
        //"    (idx ≔ (-1)%Z);; (* -1 *)\n" +
        //"    (cmp ≔ 1);;\n" +
        //"\n" +
        //"    (ifelse (str == (vaddr 0))\n" +
        //"            ((errno ≔ ERR_ERROR) ;;\n" +
        //"             ret)\n" +
        //"            skip);;\n" +
        //"\n" +
        //"    (ifelse (! (e ==  (vaddr 0)))\n" +
        //"            (\n" +
        //"              (__tmp ≔ (e+_str));;\n" +
        //"              (__tmp ≔[__tmp]);;\n" +
        //"              (cmp ≔ (strcmp_nullable __tmp str))\n" +
        //"                   )\n" +
        //"            skip);;\n" +
        //"\n" +
        //"    (* Loop invariant (informally):\n" +
        //"       e points to an element in the string table's list\n" +
        //"       prev != NULL -> prev->next = e\n" +
        //"       e != NULL -> cmp = strcmp(e->str, str)\n" +
        //"     *)\n" +
        //"    (while ((! (e == (vaddr 0))) && (cmp < 0))\n" +
        //"           ((prev ≔ e);;\n" +
        //"            (__tmp ≔ (e + _next));;\n" +
        //"            (e ≔[__tmp]);;\n" +
        //"            (ifelse (! (e == 0))\n" +
        //"                    ((__tmp ≔ (e + _str));;\n" +
        //"                     (__tmp ≔[__tmp]);;\n" +
        //"                     (cmp ≔ (strcmp_nullable __tmp str)))\n" +
        //"                    skip)));;\n" +
        //"\n" +
        //"    (ifelse (cmp == 0)\n" +
        //"            (* found string in the table *)\n" +
        //"            (\n" +
        //"              (__tmp ≔ (e + _id));;\n" +
        //"              (idx ≔[__tmp]);;\n" +
        //"              (ifelse (! (0 == preexists))\n" +
        //"                      ([preexists]≔ 1)\n" +
        //"                      skip);;\n" +
        //"              (ifelse (! (0 == retPtr))\n" +
        //"                      ((__tmp ≔ (e + _str));;\n" +
        //"                       (__tmp ≔[__tmp]);;\n" +
        //"                       ([retPtr]≔ __tmp))\n" +
        //"                      skip))\n" +
        //"\n" +
        //"            (* went through the table, came up empty *)\n" +
        //"            (\n" +
        //"              (__tmp6 ≔ (table + _nr_entries));;\n" +
        //"              (__tmp7 ≔[__tmp6]);;\n" +
        //"              (ifelse (__tmp7 == 300)\n" +
        //"                      ret skip);;\n" +
        //"              \n" +
        //"              (ifelse (! (0==preexists))\n" +
        //"                      ([preexists]≔ 0) skip);;\n" +
        //"              \n" +
        //"              〈 errno, new 〉 ≔ alloc( 3 );;\n" +
        //"\n" +
        //"              (ifelse (! (errno == ERR_ERROR)) (\n" +
        //"                        (__tmp ≔ (new + _str));;   (* new->str = str *)\n" +
        //"                        ([__tmp]≔ str);;\n" +
        //"                        (__tmp ≔ (new + _id));;    (* new->id = table->nr_entries (orig) *)\n" +
        //"                        ([__tmp]≔ __tmp7);;\n" +
        //"                        (idx ≔__tmp7);;            (* idx = table->nr_entries (orig) *)\n" +
        //"\n" +
        //"                        (__tmp ≔ (__tmp7 + 1));;\n" +
        //"                        ([__tmp6]≔ __tmp);;        (* table->nr_entries++ *)\n" +
        //"\n" +
        //"                        (__tmp ≔ (new + _next));;  (* new->next = e *)\n" +
        //"                        ([__tmp]≔ e);;\n" +
        //"                        (ifelse (! (0 == prev) )\n" +
        //"                                ((__tmp ≔ (prev + _next));;([__tmp]≔ new))\n" +
        //"                                ((__tmp ≔ (table + _first));;([__tmp]≔ new)));;\n" +
        //"                        (ifelse (! (0 == retPtr))\n" +
        //"                                ([retPtr]≔ str)\n" +
        //"                                skip))\n" +
        //"                      ( (errno ≔ ERR_ERROR);;\n" +
        //"                        (idx ≔ (-1)%Z)))));; (* -1 *)\n" +
        //"    ret;;\n" +
        //"    skip))\n" +
        //"\n" +
        //"(* (lift_srprop_sprop (table ≐ (@hd val 0 alst))) *)\n" +
        //"\n" +
        //"    -> (forall (neg_one : val)(NULL : addr),\n" +
        //"          (NULL =  0) ->\n" +
        //"          (eimm neg_one = ((-1)%Z)) ->\n" +
        //"          (\n" +
        //"            (* Error case *)\n" +
        //"             ((errno  ≐ ERR_ERROR) ☆\n" +
        //"              (idx ≐ neg_one) ☆\n" +
        //"              (str ≐ (vaddr straddr)) ☆\n" +
        //"              (lift_prop_sprop (eimm straddr = (vaddr 0)) ∨ \n" +
        //"               (heap_string straddr sv)) ☆\n" +
        //"              (table ≐ (vaddr taddr)) ☆\n" +
        //"              (strtab_wf taddr alst elist)) ∨\n" +
        //"            (* insert case *)\n" +
        //"            ((sexists (fun (elist' : list table_entry) =>\n" +
        //"               (sexists (fun (alst' : list addr) =>\n" +
        //"               (sexists (fun (a:addr) =>\n" +
        //"               (sexists (fun (n:nat) =>\n" +
        //"               (sexists (fun (strp:addr) =>                             \n" +
        //"                           (lift_prop_sprop (not (List.In sv (map entry_str elist)))) ☆\n" +
        //"                           (table ≐ (vaddr taddr)) ☆\n" +
        //"                           (str ≐ (vaddr strp)) ☆\n" +
        //"                           (strtab_wf taddr alst' elist') ☆\n" +
        //"                           (is_table_entry a (mk_entry strp sv (List.length elist))) ☆\n" +
        //"                           (lift_prop_sprop (alst' = (app (firstn n alst) (a::(skipn n alst))))) ☆\n" +
        //"                           (lift_prop_sprop\n" +
        //"                              (elist' = (app (firstn n elist)\n" +
        //"                                          ((mk_entry strp sv (List.length elist))::(skipn n elist))))) ☆\n" +
        //"                           (* function outputs. (idx is the return value) *)\n" +
        //"                           (sexists (fun (aidx : addr) =>\n" +
        //"                                       (idx ≐ (vaddr aidx)) ☆\n" +
        //"                                       (aidx ↦ vnat (List.length elist)) ☆ \n" +
        //"                           ((retPtr ≐ (vaddr NULL)) ∨ (sexists (fun (aretPtr : addr) => (retPtr ≐ (vaddr aretPtr)) ☆ \n" +
        //"                                               aretPtr ↦ (vaddr strp)))) ☆\n" +
        //"                           ((preexists ≐ (vaddr NULL)) ∨ (sexists (fun (apreexists : addr) => (preexists ≐ (vaddr apreexists)) ☆ (apreexists ↦ vnat 0))))\n" +
        //"\n" +
        //"                           ))))))))))))) ∨\n" +
        //"        (* preexisting case *)\n" +
        //"            ((sexists (fun (ent : table_entry) =>\n" +
        //"               (lift_prop_sprop (List.In ent elist)) ☆\n" +
        //"               (lift_prop_sprop ((entry_str ent) = sv)) ☆\n" +
        //"               (table ≐ vaddr taddr) ☆\n" +
        //"               (idx ≐ (entry_id ent)) ☆\n" +
        //"               (strtab_wf taddr alst elist) ☆\n" +
        //"               ((retPtr ≐ (vaddr NULL))  ∨  (sexists (fun (aretPtr : addr) => (retPtr ≐ (vaddr aretPtr)) ☆ \n" +
        //"                                               aretPtr ↦ (entry_straddr ent)))) ☆\n" +
        //"               ((preexists ≐ (vaddr NULL)) ∨ (sexists (fun (apreexists : addr) => (preexists ≐ (vaddr apreexists)) ☆ (apreexists ↦ vnat 0))))\n" +
        //"            )))) st2)";

        ctx = parseTerm(expr);
        term = Term.make(parser, ctx);
        //System.out.println(term);
        Assert.assertTrue(term instanceof Forall);
    }

    @Test
    public void testNamedFields() {
        String str = "{|srf := s ; lc := l ; hpf := h ; dm := d|}";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof NamedFields);

        str = "{| |}";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof NamedFields);
        NamedFields nf = (NamedFields)term;
        Assert.assertEquals(0, nf.size());

        str = "{| a := b |}";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof NamedFields);
        nf = (NamedFields)term;
        Assert.assertEquals(1, nf.size());

        str = "{| a := b; c := 1 + 2 |}";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof NamedFields);
        nf = (NamedFields)term;
        Assert.assertEquals(2, nf.size());
    }

    private Qualid parseQualid(String str) {
        parser = new CoqFTParser(new CommonTokenStream(new CoqLexer(new ANTLRInputStream(str))));
        parser.setErrorHandler(new BailErrorStrategy());
        return new Qualid(parser, parser.qualid());
    }

    @Test
    public void testQualid() {
        String str = "a";
        Qualid qualid = parseQualid(str);
        Assert.assertNotNull(qualid);
        Assert.assertEquals(str, qualid.getFullName());

        str = "a.b";
        qualid = parseQualid(str);
        Assert.assertNotNull(qualid);
        Assert.assertEquals(str, qualid.getFullName());

        str = "a.b.c";
        qualid = parseQualid(str);
        Assert.assertNotNull(qualid);
        Assert.assertEquals(str, qualid.getFullName());

        str = "a .b";
        qualid = parseQualid(str);
        Assert.assertNotNull(qualid);
        Assert.assertEquals("a", qualid.getFullName());

        str = "a . b";
        qualid = parseQualid(str);
        Assert.assertNotNull(qualid);
        Assert.assertEquals("a", qualid.getFullName());

        str = "a. b";
        qualid = parseQualid(str);
        Assert.assertNotNull(qualid);
        Assert.assertEquals("a", qualid.getFullName());

        str = "a.";
        qualid = parseQualid(str);
        Assert.assertNotNull(qualid);
        Assert.assertEquals("a", qualid.getFullName());

        str = "a.b .a";
        qualid = parseQualid(str);
        Assert.assertEquals("a.b", qualid.getFullName());
    }

    @Test
    public void testDeref() {
        String str = "[ __tmp]";
        CoqParser.TermContext term = parseTerm(str);
        Assert.assertTrue(Term.make(parser, term) instanceof Deref);
        //System.out.println(term.toStringTree(Arrays.asList(CoqParser.ruleNames)));
    }

    @Test
    public void testRefterm() {
        String str = "a::b";
        CoqParser.TermContext term = parseTerm(str);
        Assert.assertTrue(Term.make(parser, term) instanceof Refterm);

        str = "(a::(skipn n alst))";
        term = parseTerm(str);
        Assert.assertTrue(Term.make(parser, term) instanceof Refterm);

        str = "((mk_entry strp sv (List.length elist))::(skipn n elist))";
        term = parseTerm(str);
        Assert.assertTrue(Term.make(parser, term) instanceof Refterm);

        str = "( table :: str :: nil )";
        term = parseTerm(str);
        Assert.assertTrue(Term.make(parser, term) instanceof Refterm);
    }

    @Test
    public void testFun() {
        String str = "(fun st => (store_bits (x::y::nil) (lc st)))";
        CoqParser.TermContext term = parseTerm(str);
        Assert.assertTrue(Term.make(parser, term) instanceof Fun);
    }

    @Test
    public void testComplexExpression() {
        String str = "((sexists (fun (elist' : list table_entry) =>\n" +
                "(sexists (fun (alst' : list addr) =>\n" +
                "(sexists (fun (a:addr) =>\n" +
                "(sexists (fun (n:nat) =>\n" +
                "(sexists (fun (strp:addr) =>\n" +
                "(lift_prop_sprop (not (List.In sv (map entry_str elist)))) ☆\n" +
                "(table ≐ (vaddr taddr)) ☆\n" +
                "(str ≐ (vaddr strp)) ☆\n" +
                "(strtab_wf taddr alst' elist') ☆\n" +
                "(is_table_entry a (mk_entry strp sv (List.length elist))) ☆\n" +
                "(lift_prop_sprop (alst' = (app (firstn n alst) (a::(skipn n alst))))) ☆\n" +
                "(lift_prop_sprop\n" +
                "(elist' = (app (firstn n elist)\n" +
                "((mk_entry strp sv (List.length elist))::(skipn n elist))))) ☆\n" +
                "(* function outputs. (idx is the return value) *)\n" +
                "(sexists (fun (aidx : addr) =>\n" +
                "(idx ≐ (vaddr aidx)) ☆\n" +
                "(aidx ↦ vnat (List.length elist)) ☆\n" +
                "((retPtr ≐ (vaddr NULL)) ∨ (sexists (fun (aretPtr : addr) => (retPtr ≐ (vaddr aretPtr)) ☆\n" +
                "aretPtr ↦ (vaddr strp)))) ☆\n" +
                "((preexists ≐ (vaddr NULL)) ∨ (sexists (fun (apreexists : addr) => (preexists ≐ (vaddr apreexists)) ☆ (apreexists ↦ vnat 0))))\n" +
                ")))))))))))))";
        CoqParser.TermContext term = parseTerm(str);
        Assert.assertTrue(Term.make(parser, term) instanceof Expression);
    }

    @Test
    public void testSepConj() {
        String str = "(x ≐ vx ☆ y ≐ vy)";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof SepConjunction);
    }

    private Proof parseProof(String line) {
        CoqLexer lexer = new CoqLexer(new ANTLRInputStream(line));
        parser = new CoqFTParser(new CommonTokenStream(lexer));

        CoqParser.ProofContext ctx = parser.proof();
        Assert.assertNotNull(ctx);

        //System.out.println(ctx.toStringTree(Arrays.asList(CoqParser.ruleNames)));

        return new Proof(parser, ctx);
    }

    @Test
    public void testProof() {
        parseProof("Proof.\nintuition.\n(*Set Printing Coercions.*)\nintros.\nnova_star H.\nunfold lift_prop_sprop in *.\nQed.\n");
        parseProof("Proof.\n" +
                "assertLI H2 ((sexists (fun alstL:list addr =>\n" +
                "sexists (fun elistL =>\n" +
                "sexists (fun alstR:list addr =>\n" +
                "sexists (fun elistR =>\n" +
                "((prev == 0)|| ((prev + _next)%expr == e)) ☆\n" +
                "(e == (hd (val_of_nat 0) alstR)) ☆\n" +
                "(simpl (sprop_of_bexpr (!(e == 0)))\n" +
                "((strcmp_nullable (e+_str)%expr str)==cmp))  ☆\n" +
                "(simpl (sprop_of_bexpr (!(prev==0)))\n" +
                "(sprop_of_bexpr (bexpr_lt (strcmp_nullable (prev+_str)%expr str) 0))) ☆\n" +
                "(val_of_plus taddr _first ↦ hd (val_of_nat 0) alstL) ☆\n" +
                "(val_of_plus taddr _nr_entries ↦ expr_plus (Datatypes.length alstL)\n" +
                "(Datatypes.length alstR)) ☆\n" +
                "(poly_linked_list is_table_next is_table_entry alstL elistL\n" +
                "(hd (val_of_nat 0) alstR)) ☆\n" +
                "(lift_prop_sprop (Sorted sR (map entry_str elistL))) ☆\n" +
                "(lift_prop_sprop (NoDup (map entry_str elistL))) ☆\n" +
                "(lift_prop_sprop (NoDup (map entry_id elistL))) ☆\n" +
                "(fun st =>\n" +
                "(Forall (fun x =>\n" +
                "val_of_bexpr (sr st)\n" +
                "(bexpr_and ((expr_imm x) >= 0)\n" +
                "((expr_imm x) < List.length alst)) =\n" +
                "Some true) (map entry_id elistL))) ☆\n" +
                "(poly_linked_list is_table_next is_table_entry alstR elistR 0) ☆\n" +
                "(lift_prop_sprop (Sorted sR (map entry_str elistR))) ☆\n" +
                "(lift_prop_sprop (NoDup (map entry_str elistR))) ☆\n" +
                "(lift_prop_sprop (NoDup (map entry_id elistR))) ☆\n" +
                "(fun st =>\n" +
                "(Forall (fun x =>\n" +
                "l_of_bexpr (sr st)\n" +
                "(bexpr_and ((expr_imm x) >= 0)\n" +
                "((expr_imm x) < List.length alst)) =\n" +
                "Some true) (map entry_id elistR)))))))) s19).\n" +
                "Qed.");
    }

    private TacticExpr parseTactic(String str) {
        parser = new CoqFTParser(new CommonTokenStream(new CoqLexer(new ANTLRInputStream(str))));
        parser.setErrorHandler(new BailErrorStrategy());
        return TacticExpr.make(parser, parser.expr());
    }

    private Apply testApply(String str, int numTerms, int numBindings) {
        TacticExpr tactic = parseTactic(str);
        Assert.assertNotNull(tactic);
        Assert.assertTrue(tactic instanceof Apply);
        Apply apply = (Apply)tactic;
        Assert.assertEquals(numTerms, apply.getTerms().size());
        Assert.assertEquals(numBindings, apply.getWithBindingsLists().size());
        return apply;
    }

    private void testApplyMap(Apply apply, String... strs) {
        Map<String, String> map = new HashMap<>();
        for(Map.Entry<Term, BindingsList> entry : apply.getWithBindingsLists().entrySet()) {
            map.put(entry.getKey().fullText(), entry.getValue().fullText());
        }

        for(int i = 0; i < strs.length - 1; i += 2) {
            String key = strs[i], val = strs[i + 1];
            if(val != null) {
                Assert.assertTrue(map.containsKey(key));
                Assert.assertEquals(val, map.get(key));
            } else {
                Assert.assertFalse(map.containsKey(key));
            }
        }
    }

    @Test
    public void testApply() {
        Apply apply = testApply("apply a.", 1, 0);

        apply = testApply("apply a, b, c", 3, 0);

        apply = testApply("apply a with b.", 1, 1);
        testApplyMap(apply, "a", "b");

        apply = testApply("apply a, b with c, d.", 3, 1);
        testApplyMap(apply, "a", null, "b", "c", "d", null);

        apply = testApply("apply a with b, c, d with e.", 3, 2);
        testApplyMap(apply, "a", "b", "c", null, "d", "e");
    }

    @Test
    public void testTypeCast() {
        String str = "a : b";
        CoqParser.TermContext term = parseTerm(str);
        Assert.assertTrue(Term.make(parser, term) instanceof TypeCast);

        str = "a : b c";
        term = parseTerm(str);
        Assert.assertTrue(Term.make(parser, term) instanceof TypeCast);

        str = "H1 : NULL = 0";
        term = parseTerm(str);
        Assert.assertTrue(Term.make(parser, term) instanceof TypeCast);

        str = "H0 : completes {| srf := s; lc := l; hpf := h; dm := d |} st2 (ifelse (x < y) (res ≔ y) (res ≔ x);; ret;; skip)";
        term = parseTerm(str);
        Assert.assertTrue(Term.make(parser, term) instanceof TypeCast);
    }

    @Test
    public void testArgChain() {
        String str = "s | x = asdf vx";
        CoqParser.TermContext term = parseTerm(str);
        Assert.assertTrue(Expression.isArgChain(term));

        List<CoqParser.TermContext> terms = Expression.getArgChain(term);
        Assert.assertEquals(6, terms.size());
        for(int i = 0; i < terms.size(); i++) {
            Term t = Term.make(parser, terms.get(i));
            Assert.assertTrue(t instanceof ID);

            String expected = null;
            if(i == 0) expected = "s";
            else if(i == 1) expected = "|";
            else if(i == 2) expected = "x";
            else if(i == 3) expected = "=";
            else if(i == 4) expected = "asdf";
            else if(i == 5) expected = "vx";

            Assert.assertEquals(expected, ((ID)t).getFullName());
        }

        str = "(a b c) = (d e f)";
        term = parseTerm(str);
        Assert.assertTrue(Expression.isArgChain(term));

        terms = Expression.getArgChain(term);
        Assert.assertEquals(3, terms.size());

        Assert.assertTrue(Expression.isArgChain(terms.get(0).inner));
        Term t = Term.make(parser, terms.get(0));
        Assert.assertTrue(t instanceof Expression);

        Assert.assertFalse(Expression.isArgChain(terms.get(1)));
        t = Term.make(parser, terms.get(1));
        Assert.assertTrue(t instanceof ID);
        Assert.assertEquals("=", ((ID)t).getFullName());

        Assert.assertTrue(Expression.isArgChain(terms.get(2).inner));
        t = Term.make(parser, terms.get(2));
        Assert.assertTrue(t instanceof Expression);
    }

    @Test
    public void testEquals() {
        String str = "s | x = asdf vx";
        CoqParser.TermContext term = parseTerm(str);
        Term t = Term.make(parser, term);
        Assert.assertTrue(t instanceof EqualsExpression);

        Term left = ((EqualsExpression)t).getLeft(),
                right = ((EqualsExpression)t).getRight();
        Assert.assertTrue(left instanceof Expression);
        Assert.assertEquals(2, ((Expression)left).getArgs().size());

        Assert.assertTrue(right instanceof Expression);
        Assert.assertEquals(1, ((Expression)right).getArgs().size());

        str = "x = y";
        term = parseTerm(str);
        t = Term.make(parser, term);
        Assert.assertTrue(t instanceof EqualsExpression);

        left = ((EqualsExpression)t).getLeft();
        right = ((EqualsExpression)t).getRight();
        Assert.assertTrue(left instanceof ID);
        Assert.assertEquals("x", ((ID)left).getFullName());
        Assert.assertTrue(right instanceof ID);
        Assert.assertEquals("y", ((ID)right).getFullName());

        str = "(Val.ltb vx vy) = true";
        term = parseTerm(str);
        t = Term.make(parser, term);
        Assert.assertTrue(t instanceof EqualsExpression);

        str = "Val.ltb vx vy = true";
        term = parseTerm(str);
        t = Term.make(parser, term);
        Assert.assertTrue(t instanceof EqualsExpression);
    }

    @Test
    public void testExpr9() {
        String str = "(((Val.ltb vx vy) = true -> (res ≐ vy) st2) /\\\n" +
                "((Val.ltb vx vy) = false -> (res ≐ vx) st2))";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof AndExpression);

        Assert.assertTrue(((AndExpression)term).getLeft() instanceof Implies);
        Implies i = (Implies)((AndExpression)term).getLeft();
        Assert.assertTrue(i.getLeft() instanceof EqualsExpression);
        Assert.assertTrue(i.getRight() instanceof Expression);

        EqualsExpression ee = (EqualsExpression)i.getLeft();
        Assert.assertTrue(ee.getLeft() instanceof LessThanExpression);
        Assert.assertTrue(ee.getRight() instanceof TrueExpression);

        Assert.assertTrue(((AndExpression)term).getRight() instanceof Implies);
        i = (Implies)((AndExpression)term).getRight();
        Assert.assertTrue(i.getLeft() instanceof EqualsExpression);
        Assert.assertTrue(i.getRight() instanceof Expression);

        str = "Val.ltb vx vy = true";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof EqualsExpression);

        ee = (EqualsExpression)term;
        Assert.assertTrue(ee.getLeft() instanceof LessThanExpression);
        Assert.assertTrue(ee.getRight() instanceof TrueExpression);

        str = "(Val.ltb vx vy = true -> (res ≐ vy) st2) /\\\n" +
                "(Val.ltb vx vy = false -> (res ≐ vx) st2)";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof AndExpression);

        Assert.assertTrue(((AndExpression)term).getLeft() instanceof Implies);
        i = (Implies)((AndExpression)term).getLeft();
        Assert.assertTrue(i.getLeft() instanceof EqualsExpression);
        Assert.assertTrue(i.getRight() instanceof Expression);

        ee = (EqualsExpression)i.getLeft();
        Assert.assertTrue(ee.getLeft() instanceof LessThanExpression);
        Assert.assertTrue(ee.getRight() instanceof TrueExpression);

        Assert.assertTrue(((AndExpression)term).getRight() instanceof Implies);
        i = (Implies)((AndExpression)term).getRight();
        Assert.assertTrue(i.getLeft() instanceof EqualsExpression);
        Assert.assertTrue(i.getRight() instanceof Expression);
    }

    @Test
    public void testExpr10() {
        String str = "{| srf := s'; lc := l'; hpf := h; dm := Dom.diff d d |} y = asdf v1";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof EqualsExpression);

        Term left = ((EqualsExpression)term).getLeft(),
                right = ((EqualsExpression)term).getRight();
        Assert.assertTrue(left instanceof Expression);
        Assert.assertTrue(((Expression)left).getArgs().size() == 1);
        Assert.assertTrue(((Expression)left).getTerm() instanceof NamedFields);

        Assert.assertTrue(right instanceof Expression);
    }

    @Test
    public void testExpr11() {
        String str = "(lift_prop_sprop (In sv (map entry_str elist) -> False))";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof Expression);
    }

    @Test
    public void testOrExpr() {
        String str = "(retPtr ≐ 0 ∨ sexists (fun aretPtr : addr => retPtr ≐ aretPtr ☆ aretPtr ↦ strp))";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof OrExpression);

        str = "retPtr ≐ NULL ∨ sexists (fun aretPtr : addr => retPtr ≐ aretPtr ☆ aretPtr ↦ strp)";
        ctx = parseTerm(str);
        Expression e = new Expression(parser, ctx);
        Assert.assertNotNull(OrExpression.convert(e));
    }

    @Test
    public void testStoreBound() {
        String str = "(retPtr ≐ 0)";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof StoreBound);

        str = "(x ≐ vx)";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof StoreBound);
    }

    @Test
    public void testAndExpr() {
        String str = "(a /\\ b)";
        CoqParser.TermContext ctx = parseTerm(str);
        Assert.assertTrue(Term.make(parser, ctx) instanceof AndExpression);

        str = "((sint32_min <= vx) /\\ (vx >= sint32_max))";
        ctx = parseTerm(str);
        Assert.assertTrue(Term.make(parser, ctx) instanceof AndExpression);

        str = "(sint32_min <= vx <= sint32_max)";
        ctx = parseTerm(str);
        Assert.assertTrue(Term.make(parser, ctx) instanceof AndExpression);
    }

    @Test
    public void testLTGTExpr() {
        String str = "Val.ltb vx vy = false";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof EqualsExpression);

        str = "Val.ltb vx vy";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof LessThanExpression);

        str = "(vx <? vy)";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof LessThanExpression);

        str = "Val.gtb vx vy";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof GreaterThanExpression);

        str = "(vx >? vy)";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof GreaterThanExpression);
    }

    @Test
    public void testNot() {
        String str = "! a";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof NotExpression);

        str = "~ a";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof NotExpression);

        str = "(~ In sv (map entry_str elist))";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof NotExpression);
    }

    @Test
    public void testCompletes() {
        String str = "(completes s1 s2 (\n" + 
                "    local retPtr skip;;\n" + 
                "    local preexists skip;;\n" + 
                "    local errno skip;;\n" + 
                "    \n" + 
                "    local prev skip;;\n" + 
                "    local new skip;;\n" + 
                "    local e skip;;\n" + 
                "    local idx skip;;\n" + 
                "    local cmp skip;;\n" + 
                "\n" + 
                "    local __tmp skip;;\n" + 
                "    \n" + 
                "    local __tmp6 skip;;\n" + 
                "    local __tmp7 skip;;\n" + 
                "\n" + 
                "    (prev ≔ 0);;\n" + 
                "    (new ≔ 0);;\n" + 
                "    (__tmp ≔ (table + eimm _first));;\n" + 
                "    (e ≔[ __tmp]);;\n" + 
                "    (idx ≔ (-1)%Z);; (* -1 *)\n" + 
                "    (cmp ≔ 1);;\n" + 
                "\n" + 
                "    (ifelse (str == eimm  (Some (vaddr 0)))\n" + 
                "            ((errno ≔ eimm (Some ERR_ERROR)) ;;\n" + 
                "             ret)\n" + 
                "            skip);;\n" + 
                "\n" + 
                "    (ifelse (! (e ==  eimm (Some (vaddr 0))))\n" + 
                "            (\n" + 
                "              (__tmp ≔ (e+ (eimm _str)));;\n" + 
                "              (__tmp ≔[__tmp]);;\n" + 
                "              (cmp ≔ (strcmp_nullable __tmp str))\n" + 
                "                   )\n" + 
                "            skip);;\n" + 
                "\n" + 
                "    (* Loop invariant (informally):\n" + 
                "       e points to an element in the string table's list\n" + 
                "       prev != NULL -> prev->next = e\n" + 
                "       e != NULL -> cmp = strcmp(e->str, str)\n" + 
                "     *)\n" + 
                "    (while ((! (e == eimm (Some (vaddr 0)))) && (cmp < 0))\n" + 
                "           ((prev ≔ e);;\n" + 
                "            (__tmp ≔ (e + eimm _next));;\n" + 
                "            (e ≔[__tmp]);;\n" + 
                "            (ifelse (! (e == 0))\n" + 
                "                    ((__tmp ≔ (e + eimm _str));;\n" + 
                "                     (__tmp ≔[__tmp]);;\n" + 
                "                     (cmp ≔ (strcmp_nullable __tmp str)))\n" + 
                "                    skip)));;\n" + 
                "\n" + 
                "    (ifelse (cmp == 0)\n" + 
                "            (* found string in the table *)\n" + 
                "            (\n" + 
                "              (__tmp ≔ (e + eimm _id));;\n" + 
                "              (idx ≔[__tmp]);;\n" + 
                "              (ifelse (! (0 == preexists))\n" + 
                "                      ([preexists]≔ 1)\n" + 
                "                      skip);;\n" + 
                "              (ifelse (! (0 == retPtr))\n" + 
                "                      ((__tmp ≔ (e + eimm _str));;\n" + 
                "                       (__tmp ≔[__tmp]);;\n" + 
                "                       ([retPtr]≔ __tmp))\n" + 
                "                      skip))\n" + 
                "\n" + 
                "            (* went through the table, came up empty *)\n" + 
                "            (\n" + 
                "              (__tmp6 ≔ (table + eimm _nr_entries));;\n" + 
                "              (__tmp7 ≔[__tmp6]);;\n" + 
                "              (ifelse (__tmp7 == 300)\n" + 
                "                      ret skip);;\n" + 
                "              \n" + 
                "              (ifelse (! (0==preexists))\n" + 
                "                      ([preexists]≔ 0) skip);;\n" + 
                "              \n" + 
                "              〈 errno, new 〉 ≔ alloc( 3 );;\n" + 
                "\n" + 
                "              (ifelse (! (errno == eimm (Some ERR_ERROR))) (\n" + 
                "                        (__tmp ≔ (new + eimm _str));;   (* new->str = str *)\n" + 
                "                        ([__tmp]≔ str);;\n" + 
                "                        (__tmp ≔ (new + eimm _id));;    (* new->id = table->nr_entries (orig) *)\n" + 
                "                        ([__tmp]≔ __tmp7);;\n" + 
                "                        (idx ≔__tmp7);;            (* idx = table->nr_entries (orig) *)\n" + 
                "\n" + 
                "                        (__tmp ≔ (__tmp7 + 1));;\n" + 
                "                        ([__tmp6]≔ __tmp);;        (* table->nr_entries++ *)\n" + 
                "\n" + 
                "                        (__tmp ≔ (new + eimm _next));;  (* new->next = e *)\n" + 
                "                        ([__tmp]≔ e);;\n" + 
                "                        (ifelse (! (0 == prev) )\n" + 
                "                                ((__tmp ≔ (prev + eimm _next));;([__tmp]≔ new))\n" + 
                "                                ((__tmp ≔ (table + eimm _first));;([__tmp]≔ new)));;\n" + 
                "                        (ifelse (! (0 == retPtr))\n" + 
                "                                ([retPtr]≔ str)\n" + 
                "                                skip))\n" + 
                "                      ( (errno ≔ eimm (Some ERR_ERROR));;\n" + 
                "                        (idx ≔ (-1)%Z)))));; (* -1 *)\n" + 
                "    ret;;\n" + 
                "    skip))";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof Completes);

        Completes c = (Completes)term;
        Assert.assertEquals(23, c.getStatements().size());
    }

    @Test
    public void testBinders() {		
        String str = "(pvx : (sint32_min <= vx) /\\ (vx >= sint32_max))";
        parser = parser(str);
        CoqParser.BindersContext ctx = parser.binders();
        Assert.assertNotNull(ctx);
        Assert.assertEquals(1, ctx.binder().size());
        Binder binder = new Binder(parser, ctx.binder(0));
        Assert.assertEquals(1, binder.getNames().size());
        Assert.assertEquals("pvx", binder.getNames().get(0).getIdent().getFullName());
    }

    @Test
    public void testSome() {
        String str = "Some vx";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof Some);

        str = "Some (a + b)";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof Some);

        str = "Some";
        ctx = parseTerm(str);
        term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof ID);
    }

    @Test
    public void testNone() {
        String str = "None";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof None);
    }

    @Test
    public void testExpr12() {
        String str = "((fun st : state => store_bits (x :: y :: nil) st) ☆ x ≐ Some vx ☆ y ≐ Some vy)";
        CoqParser.TermContext ctx = parseTerm(str);
        Term term = Term.make(parser, ctx);
        Assert.assertTrue(term instanceof SepConjunction);
        SepConjunction s = (SepConjunction)term;
        Assert.assertEquals(3, s.getTerms().size());

        term = s.getTerm(0);
        Assert.assertTrue(term instanceof Fun);

        term = s.getTerm(1);
        Assert.assertTrue(term instanceof StoreBound);
        StoreBound sb = (StoreBound)term;
        Assert.assertTrue(sb.getLeft() instanceof ID);
        Assert.assertEquals("x", sb.getLeft().fullText());
        Assert.assertTrue(sb.getRight() instanceof Some);
        Assert.assertEquals("vx", ((Some)sb.getRight()).getTerm().fullText());

        term = s.getTerm(2);
        Assert.assertTrue(term instanceof StoreBound);
        sb = (StoreBound)term;
        Assert.assertTrue(sb.getLeft() instanceof ID);
        Assert.assertEquals("y", sb.getLeft().fullText());
        Assert.assertTrue(sb.getRight() instanceof Some);
        Assert.assertEquals("vy", ((Some)sb.getRight()).getTerm().fullText());
    }
    
    @Test
    public void testDisjConjIntroPattern() {
        /*
        disj_conj_intro_pattern: isDisj=TOK_LBRACKET TOK_PIPE? pattern+ (TOK_PIPE pattern+)* TOK_RBRACKET
            |   isConj=TOK_LPAREN pattern (TOK_COMMA pattern)* TOK_RPAREN
            |   isBinary=TOK_LPAREN pattern (TOK_AMP pattern)* TOK_RPAREN
            |   TOK_LBRACKET TOK_RBRACKET EQN naming_intro_pattern // ??????????
            ;
             */
        String str = "[a b]";
        CoqParser.Disj_conj_intro_patternContext ctx = parser(str).disj_conj_intro_pattern();
        Assert.assertNotNull(ctx);
        new DisjConjIntroPattern(parser, ctx);
        Assert.fail("Make test cases with pipes in different locations once it's fixed");
    }

    // TODO test clone() methods
}
