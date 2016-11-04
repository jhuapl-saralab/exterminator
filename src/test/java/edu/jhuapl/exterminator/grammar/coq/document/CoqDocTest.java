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
package edu.jhuapl.exterminator.grammar.coq.document;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.jhuapl.exterminator.coq.XMLUtils;
import edu.jhuapl.exterminator.grammar.coq.CoqFTParser;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class CoqDocTest {

    public static void assertEquals(Node expected, Node actual) {
        Assert.assertTrue((expected == null) == (actual == null));
        if(expected == null && actual == null) return;
        Assert.assertTrue("Expected:\n" + XMLUtils.nodeToString(expected) +
                "\nbut was:\n" + XMLUtils.nodeToString(actual),
                expected.isEqualNode(actual));
    }

    public static void assertEquals(String expected, Node actual) {
        Assert.assertTrue((expected == null) == (actual == null));
        if(expected == null && actual == null) return;
        try {
            assertEquals(XMLUtils.docFromString(expected).getDocumentElement(),
                    actual);
        } catch(SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static void assertEquals(Node expected, String actual) {
        Assert.assertTrue((expected == null) == (actual == null));
        if(expected == null && actual == null) return;
        try {
            assertEquals(expected,
                    XMLUtils.docFromString(actual).getDocumentElement());
        } catch(SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testLocal() throws SAXException {
        String str = "local x";
        Term term = CoqFTParser.parseTerm(str, true);
        assertEquals(
                "<term><keyword name='local'/><whitespace/><identifier name='x'/></term>",
                term.makeCoqDocTerm(CoqDoc.makeDocument()));
    }

    @Test
    public void testExpression() {
        String str = "prev ≔ 0";
        Term term = CoqFTParser.parseTerm(str, true);
        assertEquals(
                "<term><identifier name='prev'/><whitespace/>" +
                        "<keyword name='≔'/><whitespace/><text>0</text></term>",
                        term.makeCoqDocTerm(CoqDoc.makeDocument()));

        str = "__tmp ≔ (table + _first)";
        term = CoqFTParser.parseTerm(str, true);
        assertEquals(
                "<term><identifier name='__tmp'/><whitespace/>" +
                        "<keyword name='≔'/><whitespace/>" +
                        "<keyword name='('/><identifier name='table'/><whitespace/>" +
                        "<keyword name='+'/><whitespace/>" +
                        "<identifier name='_first'/><keyword name=')'/></term>",
                        term.makeCoqDocTerm(CoqDoc.makeDocument()));
    }

    @Test
    public void testIfThenElse() {
        String str = "ifelse (x < y)\n(res ≔ y)\n(res ≔ x)";
        Term term = CoqFTParser.parseTerm(str, true);
        assertEquals(
                "<term><keyword name='IF'/><whitespace type='newline'/>" +
                        "<whitespace type='indent'><term><identifier name='x'/>" +
                        "<whitespace/><keyword name='&lt;'/><whitespace />" +
                        "<identifier name='y'/></term></whitespace><whitespace type='newline'/>" +
                        "<keyword name='THEN'/><whitespace type='newline'/>" +
                        "<whitespace type='indent'><term><identifier name='res'/>" +
                        "<whitespace/><keyword name='≔'/><whitespace/>" +
                        "<identifier name='y'/></term></whitespace><whitespace type='newline'/>" +
                        "<keyword name='ELSE'/><whitespace type='newline'/>" +
                        "<whitespace type='indent'><term><identifier name='res'/>" +
                        "<whitespace/><keyword name='≔'/><whitespace/>" +
                        "<identifier name='x'/></term></whitespace></term>",
                        term.makeCoqDocTerm(CoqDoc.makeDocument()));
    }
}
