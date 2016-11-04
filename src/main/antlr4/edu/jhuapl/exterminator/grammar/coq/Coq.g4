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
grammar Coq;

// Top level

COMMENT : '(*' .*? '*)' -> channel(HIDDEN);

prog : command+ EOF;

command
    : require
    | sentence
    ;

// Parser rules

type : term;
notation : .*?;


require : U_REQUIRE (U_IMPORT|U_EXPORT) module_name=qualid DOT;

module : U_MODULE (U_IMPORT|U_EXPORT)? module_name=ident DOT .*? U_END module_name=ident DOT;

U_REQUIRE: 'Require';
U_MODULE: 'Module';
U_IMPORT: 'Import';
U_EXPORT: 'Export';
U_END: 'End';

///

module_type : qualid
    |   module_type WITH U_DEFINITION qualid TOK_DEFINE term
    |   module_type WITH U_MODULE qualid TOK_DEFINE qualid
    |   qualid qualid+
    |   TOK_EXCLAMATION qualid qualid+
    ;

U_DEFINITION: 'Definition';
         
module_binding : TOK_LPAREN (U_IMPORT|U_EXPORT)? ident+ TOK_COLON module_type TOK_RPAREN;
         
module_bindings : module_binding+;
         
module_expression : qualid+
    |   TOK_EXCLAMATION qualid+
    ;

section: U_SECTION section_name=ident DOT .*? U_END section_name=ident DOT;

U_SECTION: 'Section';

////

record : record_keyword ident (binders)? (TOK_COLON sort)? TOK_DEFINE (ident)? TOK_LBRACE (field (TOK_SEMICOLON field)*)? TOK_RBRACE DOT;
         
record_keyword : U_RECORD | U_STRUCTURE
    | U_INDUCTIVE
    | U_COINDUCTIVE
    ;

U_RECORD: 'Record';
U_STRUCTURE: 'Structure';
U_INDUCTIVE: 'Inductive';
U_COINDUCTIVE: 'CoInductive';
         
field : name (binders)? TOK_COLON type (WHERE notation)?
    |   name (binders)? (TOK_COLON term)? TOK_DEFINE term
    ;

sentence : assumption
    |   definition
    |   inductive
    |   fixpoint
    |   assertion proof
    |   record
    |   goal
    ;
         
assumption : assumption_keyword assums DOT;

goal: U_GOAL form DOT;

U_GOAL: 'Goal';
         
assumption_keyword : U_AXIOM
    | U_CONJECTURE
    | U_PARAMETER
    | U_PARAMETERS
    | U_VARIABLE
    | U_VARIABLES
    | U_HYPOTHESIS
    | U_HYPOTHESES
    ;

U_AXIOM: 'Axiom';
U_CONJECTURE: 'Conjecture';
U_PARAMETER: 'Parameter';
U_PARAMETERS: 'Parameters';
U_VARIABLE: 'Variable';
U_VARIABLES: 'Variables';
U_HYPOTHESIS: 'Hypothesis';
U_HYPOTHESES: 'Hypotheses';
         
assums : ident+ TOK_COLON term
    | (TOK_LPAREN ident+ TOK_COLON term TOK_RPAREN)+
    ;
         
definition : U_DEFINITION ident (binders)? (TOK_COLON term)? TOK_DEFINE def_term=term DOT
    |   U_LET ident (binders)? (TOK_COLON term)? TOK_DEFINE term DOT
    ;

U_LET: 'Let';
         
inductive : U_INDUCTIVE ind_body (WITH ind_body)* DOT
    |   U_COINDUCTIVE ind_body (WITH ind_body)* DOT
    ;
         
ind_body : ident (binders)? TOK_COLON term TOK_DEFINE (TOK_PIPE? ident (binders)? (TOK_COLON term)? (TOK_PIPE ident (binders)? (TOK_COLON term)?)*)?;
         
fixpoint : U_FIXPOINT fix_body (WITH fix_body)* DOT
    |   U_COFIXPOINT cofix_body (WITH cofix_body)* DOT
    ;

U_FIXPOINT: 'Fixpoint';
U_COFIXPOINT: 'CoFixpoint';
         
assertion : assertion_keyword ident (binders)? TOK_COLON term DOT;
         
assertion_keyword : U_THEOREM
    | U_LEMMA
    | U_REMARK
    | U_FACT
    | U_COROLLARY
    | U_PROPOSITION
    | U_DEFINITION
    | U_EXAMPLE
    ;

U_THEOREM: 'Theorem';
U_LEMMA: 'Lemma';
U_REMARK: 'Remark';
U_FACT: 'Fact';
U_COROLLARY: 'Corollary';
U_PROPOSITION: 'Proposition';
U_EXAMPLE: 'Example';

term_forall : FORALL binders TOK_COMMA term ;
term_fun : FUN binders TOK_EQUAL_GT term ;
term_fix : FIX fix_bodies
    |      COFIX cofix_bodies
    ;
term_let : LET ident binders? (TOK_COLON term)? TOK_DEFINE term IN term
    |      LET FIX fix_body IN term
    |      LET COFIX cofix_body IN term
    |      LET TOK_LPAREN (name (TOK_COMMA name)*)? TOK_RPAREN dep_ret_type? TOK_DEFINE term IN term
    |      LET TOK_APOSTROPHE pattern (IN term)? TOK_DEFINE term (return_type)? IN term
    ;

term_local : LOCAL ident SKIP?;
term_tuple : TOK_LTUPLE term (TOK_COMMA term)* TOK_RTUPLE ;
term_match : MATCH match_item (TOK_COMMA match_item)* (return_type)? WITH         
             (TOK_PIPE? equation (TOK_PIPE equation)*)? END ;
term_pair : TOK_LPAREN term TOK_COMMA term TOK_RPAREN ;
term_completes : COMPLETES term term ((TOK_LPAREN statements TOK_RPAREN) | statements) ;
term_storebound : qualid TOK_STORE_BOUND (NUM | term_some | qualid | TOK_LPAREN term TOK_RPAREN);
term_some : U_SOME term ;
term_none : U_NONE ;

U_SOME : 'Some' ;
U_NONE : 'None' ;

statements : term (TOK_SEMICOLON_SEMICOLON term)+ TOK_SEMICOLON_SEMICOLON? ;

term : TOK_LPAREN inner=term TOK_RPAREN
    |  term_forall
    |  term_fun
    |  term_fix
    |  term_let
    |  term_completes
    |  TOK_LPAREN statements TOK_RPAREN
    |  IF term (dep_ret_type)? THEN term ELSE term
    |  num=NUM
    |  term typeCast=TOK_COLON type
    |  term TOK_LT_COL term
    |  term TOK_COL_GT
    |  <assoc=right> left_term=term left_args=optional_args TOK_IMPLIES right_term=term right_args=optional_args
    |  term_local
    |  term_pair
    |  TOK_LPAREN term (TOK_SEP_CONJ term)+ TOK_RPAREN
    |  term_storebound
    |  TOK_AT qualid term*
    |  term TOK_PERCENT ident
    |  term_match
    |  term_some
    |  term_none
    |  qualid
    |  ref_left=term TOK_COLON_COLON ref_right=term
    |  deref
    |  sort
    |  UNDERSCORE
    |  term TOK_PERIOD_LPAREN qualid TOK_RPAREN
    |  term TOK_PERIOD_LPAREN qualid arg+ TOK_RPAREN
    |  term TOK_PERIOD_LPAREN TOK_AT qualid term+ TOK_RPAREN
    |  named_fields
    |  term_tuple
    |  expression_term=term arg+?
    ;

optional_args : arg*;

LOCAL : 'local';
SKIP : 'skip';

named_fields : TOK_LBRACE_PIPE ( ident TOK_DEFINE term (TOK_SEMICOLON ident TOK_DEFINE term)* )? TOK_PIPE_RBRACE;

//localization : TOK_LBRACE_PIPE SRF TOK_DEFINE srf=term TOK_SEMICOLON LC TOK_DEFINE lc=term TOK_SEMICOLON HPF TOK_DEFINE hpf=term TOK_SEMICOLON DM TOK_DEFINE dm=term TOK_PIPE_RBRACE;     
//
//SRF: 'srf';
//LC: 'lc';
//HPF: 'hpf';
//DM: 'dm';

arg : term         
    |   ( ident TOK_DEFINE term )
    ;

binders : binder+;

binder : name
    |   TOK_LPAREN name+ TOK_COLON binderTerm=term TOK_RPAREN
    |   name+ TOK_COLON binderTerm=term
    |   TOK_LPAREN name (TOK_COLON binderTerm=term)? TOK_DEFINE defineTerm=term TOK_RPAREN
    ;       
                 
sort : (PROP | SET | TYPE);

fix_bodies : fix_body
    |   fix_body (WITH fix_body)+ FOR ident
    ;
    
cofix_bodies : cofix_body
    |   cofix_body (WITH cofix_body)+ FOR ident
    ;
         
fix_body : ident binders (annotation)? (TOK_COLON term) TOK_DEFINE term;

cofix_body : ident (binders)? (TOK_COLON term)? TOK_DEFINE term;
         
annotation : TOK_LBRACE STRUCT ident TOK_RBRACE;

STRUCT: 'struct';
         
match_item : term (AS name)? (IN term)?;
         
dep_ret_type : (AS name)? return_type;
         
return_type : RETURN term;
         
equation : mult_pattern (TOK_PIPE mult_pattern)* TOK_EQUAL_GT term;
         
mult_pattern : pattern (TOK_COMMA pattern)*;
         
pattern : qualid pattern*
    |   pattern AS ident
    |   pattern TOK_PERCENT ident
    |   UNDERSCORE
    |   NUM
    |   TOK_LPAREN or_pattern (TOK_COMMA or_pattern)* TOK_RPAREN
    ;
 
or_pattern : pattern (TOK_PIPE pattern)*;

proof : header=U_PROOF DOT tactic_invocation+ footer=U_QED DOT
    |   header=U_PROOF DOT tactic_invocation+ footer=U_DEFINED DOT
    |   header=U_PROOF DOT tactic_invocation+ footer=U_ADMITTED DOT
    ;

U_PROOF: 'Proof';
U_QED: 'Qed';
U_DEFINED: 'Defined';
U_ADMITTED: 'Admitted';

tactic_invocation : NUM TOK_COLON tactic DOT
    | tactic DOT
    ;

tactic : expr;
//tactic : atomic_tactic
//    |   TOK_LPAREN tactic TOK_RPAREN
//    |   tactic ORELSE tactic
//    |   REPEAT tactic
//    |   DO NUM tactic
//    |   INFO tactic
//    |   ABSTRACT tactic
//    |   ABSTRACT tactic USING ident
//    |   tactic TOK_SEMICOLON tactic+
//    ;

atomic_tactic : (EXACT|EEXACT) term
    |   (ASSUMPTION|EASSUMPTION)
    |   REFINE term
    |   APPLY term (TOK_COMMA term)* (IN inIdent=ident)?
    |   EAPPLY term
    |   SIMPLE APPLY term
    |   SIMPLE? (APPLY|EAPPLY) term (WITH bindings_list)? (TOK_COMMA term (WITH bindings_list)?)*
    |   LAPPLY term
    |   (APPLY|EAPPLY) term WITH bindings_list (TOK_COMMA term WITH bindings_list)* IN inIdent=ident (AS disj_conj_intro_pattern)?
    |   SIMPLE APPLY term IN inIdent=ident
    |   (CONSTRUCTOR|ECONSTRUCTOR) NUM?
    |   CONSTRUCTOR NUM WITH bindings_list
    |   (SPLIT|ESPLIT) (WITH bindings_list)?
    |   (EXISTS|EEXISTS) bindings_list (TOK_COMMA bindings_list)*
    |   (LEFT|ELEFT) (WITH bindings_list)?
    |   (RIGHT|ERIGHT) (WITH bindings_list)?
    |   CLASSICAL_LEFT
    |   CLASSICAL_RIGHT
    |   INTRO ident?
    |   INTROS ident*
    |   INTROS UNTIL (ident|NUM)
    |   INTRO ident1=ident? (AFTER|BEFORE) ident2=ident
    |   INTRO ident? AT (TOP|BOTTOM)
    |   INTROS intro_pattern+
    |   CLEAR
    |   CLEAR TOK_DASH? ident+
    |   CLEAR DEPENDENT ident
    |   CLEARBODY ident
    |   REVERT ident+
    |   REVERT DEPENDENT ident
    |   MOVE ident1=ident (AFTER|BEFORE) ident2=ident
    |   MOVE ident AT (TOP|BOTTOM)
    |   RENAME ident INTO ident (TOK_COMMA ident INTO ident)*
    |   SET TOK_LPAREN ident TOK_DEFINE term TOK_RPAREN (IN goal_occurrences)?
    |   SET TOK_LPAREN binder+ TOK_DEFINE term TOK_RPAREN
    |   SET term (IN goal_occurrences)?
    |   REMEMBER term AS ident ((EQN ident)|(IN goal_occurrences))?
    |   POSE TOK_LPAREN ident binder* TOK_DEFINE term TOK_RPAREN
    |   POSE term
    |   DECOMPOSE (SUM|RECORD) qualid+ term
    |   ASSERT TOK_LPAREN ident TOK_COLON form TOK_RPAREN
    |   ASSERT form (BY tactic)?
    |   ASSERT TOK_LPAREN ident TOK_DEFINE term TOK_RPAREN
    |   ASSERT form AS intro_pattern (BY tactic)?
    |   CUT form
    |   POSE PROOF term AS intro_pattern
    |   SPECIALIZE TOK_LPAREN ident term+ TOK_RPAREN
    |   SPECIALIZE ident WITH bindings_list
    |   GENERALIZE term (TOK_COMMA term)*
    |   GENERALIZE term atNums1=at_nums (AS ident (TOK_COMMA ident)* atNums2=at_nums AS asIdent=ident)?
    |   GENERALIZE term AS asIdent=ident
    |   GENERALIZE DEPENDENT term
    |   EVAR TOK_LPAREN ident TOK_COLON term TOK_RPAREN
    |   INSTANTIATE
    |   INSTANTIATE TOK_LPAREN NUM TOK_DEFINE term TOK_RPAREN (IN ident)?
    |   INSTANTIATE TOK_LPAREN NUM TOK_DEFINE term TOK_RPAREN IN TOK_LPAREN (U_VALUE|TYPE) OF ident TOK_RPAREN
    |   ADMIT
    |   ABSURD term
    |   CONTRADICTION ident?
    |   CONTRADICT ident
    |   EXFALSO
    |   DESTRUCT term (TOK_COMMA term)*
    |   DESTRUCT term AS disj_conj_intro_pattern
    |   DESTRUCT term EQN naming_intro_pattern
    |   DESTRUCT term WITH wbl=bindings_list
    |   EDESTRUCT term
    |   DESTRUCT term1=term USING term2=term (WITH wbl=bindings_list)?
    |   DESTRUCT term IN goal_occurrences
    |   (DESTRUCT|EDESTRUCT) term1=term WITH bindings_list1=bindings_list AS disj_conj_intro_pattern EQN naming_intro_pattern USING term2=term WITH bindings_list2=bindings_list IN goal_occurrences
    |   SIMPLE DESTRUCT (simpleDestructIdent=ident|simpleDestructTerm=term)
    |   (CASE|ECASE) term (WITH wbl=bindings_list)?
    |   CASE_EQ term
    |   INDUCTION term1=term (USING term2=term)? (WITH bindings_list)?
    |   INDUCTION term (AS disj_conj_intro_pattern)? EQN naming_intro_pattern
    |   EINDUCTION term
    |   INDUCTION term (TOK_COMMA term)* USING qualid
    |   INDUCTION term IN goal_occurrences
    |   (EINDUCTION|INDUCTION) term1=term WITH bindings_list1=bindings_list AS disj_conj_intro_pattern EQN naming_intro_pattern USING term2=term WITH bindings_list2=bindings_list IN goal_occurrences
    |   ELIM term1=term (USING term2=term)? (WITH bindings_list)?
    |   EELIM term
    |   (ELIM|EELIM) term1=term WITH bindings_list1=bindings_list USING term2=term WITH bindings_list2=bindings_list
    |   ELIMTYPE form
    |   SIMPLE INDUCTION (ident|NUM)
    |   DOUBLE INDUCTION (ident1=ident ident2=ident|num1=NUM num2=NUM)
    |   DEPENDENT INDUCTION ident (GENERALIZING ident+)?
    |   DEPENDENT DESTRUCTION ident
    |   FUNCTIONAL INDUCTION TOK_LPAREN qualid term+ TOK_RPAREN (AS disj_conj_intro_pattern USING term WITH bindings_list)?
    |   (EDISCRIMINATE|DISCRIMINATE) term (WITH bindings_list)?
    |   (EDISCRIMINATE|DISCRIMINATE) NUM
    |   DISCRIMINATE
    |   (EINJECTION|INJECTION) term (WITH bindings_list)? (AS intro_pattern+)?
    |   (EINJECTION|INJECTION) NUM (AS intro_pattern+)?
    |   INJECTION
    |   (EINJECTION|INJECTION) AS intro_pattern+?
    |   INVERSION (ident|NUM) (AS intro_pattern)?
    |   INVERSION_CLEAR ident (AS intro_pattern)? (IN ident+)?
    |   INVERSION ident (AS intro_pattern)? IN ident+
    |   DEPENDENT INVERSION ident (AS intro_pattern)?
    |   DEPENDENT (INVERSION|INVERSION_CLEAR) ident (AS intro_pattern)? WITH term
    |   SIMPLE INVERSION ident (AS intro_pattern)?
    |   INVERSION ident USING ident TOK_APOSTROPHE (IN ident+)?
    |   FIX ident NUM (WITH (TOK_LPAREN ident binder+ TOK_LBRACKET TOK_LBRACE ident TOK_APOSTROPHE TOK_RBRACE TOK_RBRACKET TOK_COLON type TOK_RPAREN)+)?
    |   COFIX ident (WITH (TOK_LPAREN ident binder+ TOK_COLON type TOK_RPAREN)+)?
    |   REWRITE (TOK_IMPLIES|TOK_LARROW)? rewrite_term (TOK_COMMA (TOK_IMPLIES|TOK_LARROW)? rewrite_term)*
    |   REWRITE (TOK_IMPLIES|TOK_LARROW)? rewrite_term (WITH binder+)? IN clause
    |   REWRITE rewrite_term AT occurrences
    |   REWRITE rewrite_term BY tactic
    |   EREWRITE term
    |   CUTREWRITE TOK_IMPLIES term1=term TOK_EQUAL term2=term
    |   REPLACE term1=term WITH term2=term clause? (BY tactic)?
    |   REPLACE (TOK_IMPLIES|TOK_LARROW)? term clause?
    |   REFLEXIVITY
    |   SYMMETRY (IN ident)?
    |   TRANSITIVITY term
    |   SUBST ident*
    |   (STEPL|STEPR) term (BY tactic)?
    |   CHANGE term (IN ident)?
    |   CHANGE term1=term (AT NUM+)? WITH term2=term (IN ident)?
    |   COMPUTE (TOK_DASH? qualid+)? (IN ident+)?
    |   LAZY (TOK_DASH? qualid+)? (IN ident+)?
    |   VM_COMPUTE (IN ident+)?
    |   RED (IN ident+)?
    |   HNF (IN ident+)?
    |   SIMPL (IN ident+)?
    |   SIMPL (term|simplIdent=ident) (AT NUM+)? (IN ident+)?
    |   UNFOLD qualid (TOK_COMMA qualid)* (IN ident+)?
    |   UNFOLD qualid AT NUM (TOK_COMMA NUM)* (TOK_COMMA qualid AT NUM (TOK_COMMA NUM)*)* (IN ident+)?
    |   UNFOLD STRING (TOK_PERCENT key=ident)? (IN ident+)?
    |   UNFOLD STRING (IN ident+)?
    |   UNFOLD (qualid|STRING) AT NUM (TOK_COMMA NUM)* (TOK_COMMA (qualid|STRING) AT NUM (TOK_COMMA NUM)*)* (IN ident+)?
    |   FOLD term+ (IN ident+)?
    |   PATTERN term (AT TOK_DASH? NUM+)? (TOK_COMMA term (AT TOK_DASH? NUM+)?)* (IN ident+)?
    |   AUTO NUM?
    |   AUTO WITH (TOK_MULT | ident+)
    |   AUTO USING lemma (TOK_COMMA lemma)*
    |   EAUTO
    |   TRIVIAL (WITH (TOK_MULT | ident+))?
    |   AUTOUNFOLD WITH ident+ (IN clause)?
    |   AUTOUNFOLD WITH TOK_MULT
    |   AUTOREWRITE WITH ident+ (IN qualid)? (USING tactic)
    |   AUTOREWRITE WITH ident+ IN clause
    |   TAUTO
    |   INTUITION tactic?
    |   RTAUTO
    |   FIRSTORDER tactic?
    |   FIRSTORDER (USING qualid (TOK_COMMA qualid)*)? (WITH ident+)?
    |   CONGRUENCE (NUM|WITH term+)?
    |   CONSTR_EQ term1=term term2=term
    |   UNIFY term1=term term2=term (WITH ident)?
    |   IS_EVAR term
    |   HAS_EVAR term
    |   IS_VAR term
    |   F_EQUAL
    |   DECIDE EQUALITY
    |   COMPARE term1=term term2=term
    |   SIMPLIFY_EQ ((term|NUM) (WITH bindings_list)?)?
    |   ESIMPLIFY_EQ (NUM | term (WITH bindings_list)?)
    |   DEPENDENT REWRITE (TOK_LARROW|TOK_IMPLIES) ident
    |   FUNCTIONAL INVERSION (ident|NUM) qualid?
    |   QUOTE ident (TOK_LBRACKET ident+ TOK_RBRACKET)?
    |   OMEGA
    |   RING
    |   RING_SIMPLIFY term+
    |   FIELD
    |   FIELD_SIMPLIFY term+
    |   FIELD_SIMPLIFY_EQ
    |   FOURIER
    ;

at_nums : AT NUM+;

EXACT: 'exact';
EEXACT: 'eexact';
ASSUMPTION: 'assumption';
EASSUMPTION: 'eassumption';
REFINE: 'refine';
APPLY: 'apply';
EAPPLY: 'eapply';
LAPPLY: 'lapply';
SIMPLE: 'simple';
CONSTRUCTOR: 'constructor';
ECONSTRUCTOR: 'econstructor';
SPLIT: 'split';
ESPLIT: 'esplit';
EEXISTS: 'eexists';
LEFT: 'left';
ELEFT: 'eleft';
RIGHT: 'right';
ERIGHT: 'eright';
INTRO: 'intro';
INTROS: 'intros';
UNTIL: 'until';
AFTER: 'after';
BEFORE: 'before';
TOP: 'top';
BOTTOM: 'bottom';
CLEAR: 'clear';
CLEARBODY: 'clearbody';
DEPENDENT: 'dependent';
REVERT: 'revert';
MOVE: 'move';
RENAME: 'rename';
INTO: 'into';
REMEMBER: 'remember';
EQN: 'eqn:';
POSE: 'pose';
DECOMPOSE: 'decompose';
SUM: 'sum';
RECORD: 'record';
ASSERT: 'assert';
BY: 'by';
CUT: 'cut';
PROOF: 'proof';
SPECIALIZE: 'specialize';
GENERALIZE: 'generalize';
EVAR: 'evar';
INSTANTIATE: 'instantiate';
U_VALUE: 'Value';
OF: 'of';
ADMIT: 'admit' | 'give_up';
ABSURD: 'absurd';
CONTRADICTION: 'contradiction';
CONTRADICT: 'contradict';
EXFALSO: 'exfalso';
DESTRUCT: 'destruct';
EDESTRUCT: 'edestruct';
CASE: 'case';
ECASE: 'ecase';
CASE_EQ: 'case_eq';
INDUCTION: 'induction';
EINDUCTION: 'einduction';
ELIM: 'elim';
EELIM: 'eelim';
ELIMTYPE: 'elimtype';
DOUBLE: 'double';
GENERALIZING: 'generalizing';
DESTRUCTION: 'destruction';
FUNCTIONAL: 'functional';
DISCRIMINATE: 'discriminate';
EDISCRIMINATE: 'ediscriminate';
EINJECTION: 'einjection';
INJECTION: 'injection';
INVERSION: 'inversion';
INVERSION_CLEAR: 'inversion_clear';
REWRITE: 'rewrite';
EREWRITE: 'erewrite';
CUTREWRITE: 'cutrewrite';
REPLACE: 'replace';
REFLEXIVITY: 'reflexivity';
SYMMETRY: 'symmetry';
TRANSITIVITY: 'transitivity';
SUBST: 'subst';
STEPL: 'stepl';
STEPR: 'stepr';
CHANGE: 'change';
COMPUTE: 'compute'|'cbv';
LAZY: 'lazy';
VM_COMPUTE: 'vm_compute';
RED: 'red';
HNF: 'hnf';
SIMPL: 'simpl';
UNFOLD: 'unfold';
FOLD: 'fold';
PATTERN: 'pattern';
AUTO: 'auto';
TRIVIAL: 'trivial';
EAUTO: 'eauto';
AUTOUNFOLD: 'autounfold';
AUTOREWRITE: 'autorewrite';
TAUTO: 'tauto';
INTUITION: 'intuition';
RTAUTO: 'rtauto';
FIRSTORDER: 'firstorder';
CONGRUENCE: 'congruence';
CONSTR_EQ: 'constr_eq';
UNIFY: 'unify';
IS_EVAR: 'is_evar';
IS_VAR: 'is_var';
HAS_EVAR: 'has_evar';
F_EQUAL: 'f_equal';
DECIDE: 'decide';
EQUALITY: 'equality';
COMPARE: 'compare';
SIMPLIFY_EQ: 'simplify_eq';
ESIMPLIFY_EQ: 'esimplify_eq';
QUOTE: 'quote';
CLASSICAL_LEFT: 'classical_left';
CLASSICAL_RIGHT: 'classical_right';
OMEGA: 'omega';
RING: 'ring';
RING_SIMPLIFY: 'ring_simplify';
FIELD: 'field';
FIELD_SIMPLIFY: 'field_simplify';
FIELD_SIMPLIFY_EQ: 'field_simplify_eq';
FOURIER: 'fourier';

lemma: ident;

clause: goal_occurrences;

rewrite_term: (TOK_QUESTION|(NUM TOK_QUESTION)|TOK_EXCLAMATION|(NUM TOK_EXCLAMATION)|NUM)? term;

bindings_list: (TOK_LPAREN (ident|NUM) TOK_DEFINE term TOK_RPAREN)+
    | term+
    ;
    
intro_pattern: naming_intro_pattern
    | disj_conj_intro_pattern
    | UNDERSCORE
    | TOK_LARROW
    | TOK_IMPLIES
    ;

naming_intro_pattern: TOK_QUESTION ident?
    | ident
    ;

disj_conj_intro_pattern: isDisj=TOK_LBRACKET TOK_PIPE? pattern+ (TOK_PIPE pattern+)* TOK_RBRACKET
    |   isConj=TOK_LPAREN pattern (TOK_COMMA pattern)* TOK_RPAREN
    |   isBinary=TOK_LPAREN pattern (TOK_AMP pattern)* TOK_RPAREN
    |   TOK_LBRACKET TOK_RBRACKET EQN naming_intro_pattern // ??????????
    ;

occurrence_clause : IN goal_occurrences;

goal_occurrences : ident at_occurrences? (TOK_COMMA ident at_occurrences?)* (TOK_SELECT (selectStar=TOK_MULT selectOccurrences=at_occurrences?)?)?
    |   star=TOK_MULT (TOK_SELECT (selectStar=TOK_MULT selectOccurrences=at_occurrences?)?)?
    ;
    
at_occurrences : AT occurrences;

occurrences : TOK_DASH? NUM+;

expr : expr TOK_SEMICOLON expr
    |   expr TOK_SEMICOLON TOK_LBRACE expr (TOK_PIPE expr)+ TOK_RBRACE
    |   tacexpr3
    ;

tacexpr3 : DO (NUM | ident) tacexpr3
    |   PROGRESS tacexpr3
    |   REPEAT tacexpr3
    |   TRY tacexpr3
    |   TIMEOUT (NUM | ident) tacexpr3
    |   tacexpr2
    ;

DO: 'do';
PROGRESS: 'progress';
REPEAT: 'repeat';
TRY: 'try';
TIMEOUT: 'timeout';

tacexpr2 : tacexpr1 TOK_PIPE_PIPE tacexpr3
    |   tacexpr1
    ;

tacexpr1 : FUN name+ TOK_EQUAL_GT atom
    |   LET record? let_clause (WITH let_clause)* IN atom
    |   MATCH GOAL WITH context_rule (TOK_PIPE context_rule)* END
    |   MATCH REVERSE GOAL WITH context_rule (TOK_PIPE context_rule)* END
    |   MATCH expr WITH match_rule (TOK_PIPE match_rule)* END
    |   LAZYMATCH REVERSE? GOAL WITH context_rule (TOK_PIPE context_rule)* END
    |   LAZYMATCH expr WITH match_rule (TOK_PIPE match_rule)* END
    |   ABSTRACT atom
    |   ABSTRACT atom USING ident
    |   FIRST TOK_LBRACE expr (TOK_PIPE expr)* TOK_RBRACE
    |   SOLVE TOK_LBRACE expr (TOK_PIPE expr)* TOK_RBRACE
    |   IDTAC message_token*
    |   FAIL NUM? message_token*
    |   FRESH STRING?
    |   CONTEXT ident TOK_LBRACE term TOK_RBRACE
    |   EVAL redexpr=tactic IN term
    |   TYPE OF term
    |   EXTERNAL STRING STRING tacarg+
    |   CONSTR TOK_COLON term
    |   atomic_tactic
    |   qualid tacarg+
    |   atom
    ;

GOAL: 'goal';
REVERSE: 'reverse';
LAZYMATCH: 'lazymatch';
ABSTRACT: 'abstract';
FIRST: 'first';
SOLVE: 'solve';
IDTAC: 'idtac';
FAIL: 'fail';
FRESH: 'fresh';
CONTEXT: 'context';
EVAL: 'eval';
TYPE: 'type';
EXTERNAL: 'external';
CONSTR: 'constr';

atom : qualid
    |   TOK_LPAREN TOK_RPAREN
    |   NUM
    |   TOK_LPAREN expr TOK_RPAREN
    ;

message_token : STRING | ident | NUM;

tacarg : qualid
    |   TOK_LPAREN TOK_RPAREN
    |   LTAC TOK_COLON atom
    |   term
    ;

LTAC: 'ltac';

let_clause : ident name* TOK_DEFINE expr;

context_rule : context_hyp+ TOK_SELECT cpattern TOK_EQUAL_GT expr
    |   TOK_SELECT cpattern TOK_EQUAL_GT expr
    |   UNDERSCORE TOK_EQUAL_GT expr
    ;

context_hyp : name TOK_COLON cpattern
    |   name TOK_DEFINE cpattern (TOK_COLON cpattern)?
    ;

match_rule : cpattern TOK_EQUAL_GT expr
    |   CONTEXT ident? TOK_LBRACKET cpattern TOK_RBRACKET TOK_EQUAL_GT expr
    |   APPCONTEXT ident? TOK_LBRACKET cpattern TOK_RBRACKET TOK_EQUAL_GT expr
    |   UNDERSCORE TOK_EQUAL_GT expr
    ;

APPCONTEXT: 'appcontext';

top : LOCAL? LTAC ltac_def (WITH ltac_def)*;

ltac_def : ident+ TOK_DEFINE expr
    |   qualid ident* TOK_COLON_COLON_EQUAL expr
    ;

cpattern : term
    |   TOK_QUESTION ident
    |   UNDERSCORE;

form : U_TRUE
    |   U_FALSE
    |   TOK_TILDE form
    |   form TOK_AND form
    |   form TOK_OR form
    |   form TOK_IMPLIES form
    |   form TOK_IFF form
    |   FORALL ident TOK_COLON type TOK_COMMA form
    |   EXISTS ident (TOK_COLON specif)? TOK_COMMA form
    |   EXISTS2 ident (TOK_COLON specif)? TOK_COMMA form TOK_AMP form
    |   term TOK_EQUAL term
    |   term TOK_EQUAL term TOK_COL_GT specif
    |   boolean_term=term
    |   TOK_LPAREN inner=form TOK_RPAREN
    ;

U_TRUE: 'True';
U_FALSE: 'False';

specif : specif TOK_MULT specif
    |   specif TOK_PLUS specif
    |   specif TOK_PLUS TOK_LBRACE specif TOK_RBRACE
    |   TOK_LBRACE specif TOK_RBRACE TOK_PLUS TOK_LBRACE specif TOK_RBRACE
    |   TOK_LBRACE ident TOK_COLON specif TOK_PIPE form TOK_RBRACE
    |   TOK_LBRACE ident TOK_COLON specif TOK_PIPE form TOK_AMP form TOK_RBRACE
    |   TOK_LBRACE ident TOK_COLON specif TOK_AMP specif TOK_RBRACE
    |   TOK_LBRACE ident TOK_COLON specif TOK_AMP specif TOK_AMP specif TOK_RBRACE
    ;

name : ident        
    |   UNDERSCORE
    ; 

qualid : qualid DOT_NO_WS ident
    | ident
    ;

DOT_NO_WS : {!Character.isWhitespace(_input.LA(-1)) && _input.LA(-1) != EOF && !Character.isWhitespace(_input.LA(2)) && _input.LA(2) != EOF}? DOT;

//refid : refid TOK_COLON_COLON ident
//    | ident
//    ;
//
//TOK_COLON_COLON_NO_WS : {!Character.isWhitespace(_input.LA(-1)) && _input.LA(-1) != EOF && !Character.isWhitespace(_input.LA(3)) && _input.LA(3) != EOF}? TOK_COLON_COLON;

//REFID : IDENT_ALL (TOK_COLON_COLON IDENT_ALL)*;

deref : TOK_LBRACKET qualid TOK_RBRACKET;

ident : c_ident | op_ident;

c_ident: C_IDENT | EXACT | EEXACT | ASSUMPTION |
        EASSUMPTION | REFINE | APPLY | EAPPLY | LAPPLY | SIMPLE |
        CONSTRUCTOR | ECONSTRUCTOR | SPLIT | ESPLIT | EEXISTS | LEFT |
        ELEFT | RIGHT | ERIGHT | INTRO | INTROS | UNTIL | AFTER | BEFORE |
        TOP | BOTTOM | CLEAR | CLEARBODY | DEPENDENT | REVERT | MOVE |
        RENAME | INTO | REMEMBER | EQN | POSE | DECOMPOSE | SUM | RECORD |
        ASSERT | BY | CUT | PROOF | SPECIALIZE | GENERALIZE | EVAR |
        INSTANTIATE | OF | ADMIT | ABSURD | CONTRADICTION |
        CONTRADICT | EXFALSO | DESTRUCT | EDESTRUCT | CASE | ECASE | CASE_EQ |
        INDUCTION | EINDUCTION | ELIM | EELIM | ELIMTYPE | DOUBLE |
        GENERALIZING | DESTRUCTION | FUNCTIONAL | DISCRIMINATE |
        EDISCRIMINATE | EINJECTION | INJECTION | INVERSION | INVERSION_CLEAR |
        REWRITE | EREWRITE | CUTREWRITE | REPLACE | REFLEXIVITY | SYMMETRY |
        TRANSITIVITY | SUBST | STEPL | STEPR | CHANGE | COMPUTE | LAZY |
        VM_COMPUTE | RED | HNF | SIMPL | UNFOLD | FOLD | PATTERN | AUTO |
        TRIVIAL | EAUTO | AUTOUNFOLD | AUTOREWRITE | TAUTO | INTUITION |
        RTAUTO | FIRSTORDER | CONGRUENCE | CONSTR_EQ | UNIFY | IS_EVAR |
        IS_VAR | HAS_EVAR | F_EQUAL | DECIDE | EQUALITY | COMPARE |
        SIMPLIFY_EQ | ESIMPLIFY_EQ | QUOTE | CLASSICAL_LEFT | CLASSICAL_RIGHT |
        OMEGA | RING | RING_SIMPLIFY | FIELD | FIELD_SIMPLIFY |
        FIELD_SIMPLIFY_EQ | FOURIER | DO | PROGRESS | REPEAT | TRY | TIMEOUT |
        GOAL | REVERSE | LAZYMATCH | ABSTRACT | FIRST | SOLVE | IDTAC | FAIL |
        FRESH | CONTEXT | EVAL | TYPE | EXTERNAL | CONSTR | LTAC | CONTEXT |
        APPCONTEXT | UNDERSCORE | U_TRUE | U_FALSE | U_SOME | U_NONE | SKIP
        ;

op_ident : //TOK_EQUAL | TOK_EXCLAMATION | TOK_PERCENT | TOK_AMP | TOK_AMP_AMP |
        //TOK_LPAREN | TOK_PARENS | TOK_RPAREN | TOK_MULT | TOK_PLUS |
        //TOK_PLUS_PLUS | TOK_COMMA | TOK_DASH | TOK_PERIOD_LPAREN |
        //TOK_PERIOD_PERIOD | TOK_SLASH | TOK_AND | TOK_COLON |
        //TOK_COLON_COLON_EQUAL | TOK_SEMICOLON | TOK_SEMICOLON_SEMICOLON |
        //TOK_LT | TOK_GT | TOK_LARROW | TOK_IFF | TOK_LT_EQUAL | TOK_NEQ |
        //TOK_EQUALEQUAL | TOK_EQUAL_UNDERSCORE_D | TOK_GT_RARROW | TOK_GT_EQUAL |
        //TOK_QUESTION | TOK_QUESTION_EQUAL | TOK_LBRACKET | TOK_OR |
        //TOK_RBRACKET | TOK_CARET | TOK_LBRACE | TOK_SELECT | TOK_PIPE_PIPE |
        //TOK_RBRACE | TOK_TILDE | TOK_APOSTROPHE | TOK_LTUPLE | TOK_RTUPLE |
          OP
        | TOK_EQUAL
        | TOK_LT
        | UNICODE+?
        ;
////     TOK_EQUAL_GT
////   | TOK_DEFINE
////   | TOK_LT_COL
////   | TOK_COL_LT
////   | TOK_COL_GT
////   | TOK_IMPLIES
////   | DOT
////   | TOK_AT
//   | TOK_PIPE
////   | TOK_LBRACE_PIPE
////   | TOK_PIPE_RBRACE
////   | TOK_COLON_COLON
////   | TOK_SEP_CONJ
////   | TOK_STORE_BOUND
////   | TOK_SEMICOLON_SEMICOLON
//   TOK_EQUAL TOK_EQUAL
//   | TOK_EQUAL
//   | TOK_EXCLAMATION
////   | TOK_PERCENT
//   | TOK_AMP
////   | TOK_LPAREN
////   | TOK_RPAREN
//   | TOK_MULT
//   | TOK_PLUS
////   | TOK_COMMA
//   | TOK_DASH
////   | TOK_PERIOD_LPAREN
//   | TOK_AND
////   | TOK_COLON
//   | TOK_COLON_COLON_EQUAL
////   | TOK_SEMICOLON
//   | TOK_LARROW
//   | TOK_IFF
////   | TOK_QUESTION
//   | TOK_LBRACKET
//   | TOK_OR
////   | TOK_RBRACKET
////   | TOK_LBRACE
//   | TOK_SELECT
//   | TOK_PIPE_PIPE
//   | TOK_RBRACE
//   | TOK_TILDE
//   | TOK_APOSTROPHE
//   | TOK_LTUPLE
//   | TOK_RTUPLE
//   | UNICODE+
//   ;

// Lexer rules

BLANK : (' ' | '\t' | '\r' | '\n')+ -> channel(HIDDEN);

UNDERSCORE : '_';
AS : 'as';
AT : 'at';
COFIX : 'cofix';
ELSE : 'else';
END : 'end';
EXISTS : 'exists';
EXISTS2 : 'exists2';
FIX : 'fix';
FOR : 'for';
FORALL : 'forall';
FUN : 'fun';
IF : 'if' | 'IF';
IN : 'in';
LET : 'let';
COMPLETES : 'completes';
MATCH : 'match';
MOD : 'mod';
PROP: 'Prop';
RETURN : 'return';
SET : 'Set';
THEN : 'then';
U_TYPE: 'Type';
USING : 'using';
WHERE : 'where';
WITH : 'with';

STRING : '"' (~'"'|'""')* '"'; 

NUM : TOK_DASH? DIGIT+;
fragment DIGIT : '0'..'9';


// don't interpret these as idents
TOK_EQUAL_GT : '=>';
TOK_DEFINE : ':=';
TOK_LT_COL : '<:';
TOK_COL_LT : ':<';
TOK_COL_GT : ':>';
TOK_IMPLIES : '->';
DOT : '.';
TOK_AT : '@';
TOK_PIPE : '|';
TOK_LBRACE_PIPE : '{|';
TOK_PIPE_RBRACE : '|}';
TOK_COLON_COLON : '::';
TOK_SEP_CONJ: '☆';
TOK_STORE_BOUND: '≐';

C_IDENT : C_IDENT_FIRST_LETTER C_IDENT_SUBSEQUENT_LETTER*;
C_IDENT_FIRST_LETTER : 'a'..'z' | 'A'..'Z' | '_';
C_IDENT_SUBSEQUENT_LETTER : {!Character.isWhitespace(_input.LA(-1))}? C_IDENT_FIRST_LETTER | '\'' | '0'..'9';

OP : TOK_EQUALEQUAL
//   | TOK_EQUAL
   | TOK_PLUS
   | TOK_PLUS_PLUS
   | TOK_EXCLAMATION
   | TOK_AMP
   | TOK_GT
//   | TOK_LT
   | TOK_PERCENT |
        TOK_MULT | TOK_APOSTROPHE | TOK_PIPE ~'}' | TOK_EXCLAMATION | TOK_OR |
        TOK_GT_EQUAL | TOK_AND | TOK_AMP_AMP | TOK_TILDE | TOK_LT_EQUAL
        //| '∨' | '∧' | '≔' | '↦' | '<?' | '>?'
        | '<?' | '>?'
        ;

TOK_EQUAL : '=';
TOK_EXCLAMATION: '!';
TOK_PERCENT : '%';
TOK_AMP : '&';
TOK_AMP_AMP : '&&';
TOK_LPAREN : '(';
TOK_PARENS : '()';
TOK_RPAREN : ')';
TOK_MULT : '*';
TOK_PLUS : '+';
TOK_PLUS_PLUS : '++';
TOK_COMMA : ',';
TOK_DASH : '-';
TOK_PERIOD_LPAREN : '.(';
TOK_PERIOD_PERIOD : '..';
TOK_SLASH : '/';
TOK_AND : '/\\';
TOK_COLON : ':';
TOK_COLON_COLON_EQUAL: '::=';
TOK_SEMICOLON : ';';
TOK_SEMICOLON_SEMICOLON : ';;';
TOK_LT : '<';
TOK_GT : '>';
TOK_LARROW : '<-';
TOK_IFF : '<->';
TOK_LT_EQUAL : '<=';
TOK_NEQ : '<>';
TOK_EQUALEQUAL : '==';
TOK_EQUAL_UNDERSCORE_D : '=_D';
TOK_GT_RARROW : '>->';
TOK_GT_EQUAL : '>=';
TOK_QUESTION : '?';
TOK_QUESTION_EQUAL : '?=';
TOK_LBRACKET : '[';
TOK_OR : '\\/';
TOK_RBRACKET : ']';
TOK_CARET : '^';
TOK_LBRACE : '{';
TOK_SELECT : '|-';
TOK_PIPE_PIPE : '||';
TOK_RBRACE : '}';
TOK_TILDE : '~';
TOK_APOSTROPHE : '\'';
TOK_LTUPLE : '〈';
TOK_RTUPLE : '〉';

UNICODE : {!Character.isWhitespace(_input.LA(1)) && !Character.isLetterOrDigit(_input.LA(1)) && !(_input.LA(1) == '_')}? ('\u0000'..'\uFFFE');
