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

import java.util.Collection;
import java.util.Objects;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.jhuapl.exterminator.coq.XMLUtils;
import edu.jhuapl.exterminator.grammar.coq.term.ID;
import edu.jhuapl.exterminator.grammar.coq.term.Term;

public class CoqDoc {

    public static interface CoqDocable {
        public Element makeCoqDocTerm(Document doc);
        public String fullText();
    }

    ///////////////////////////////////////////////////////////////////////////

    private static final DocumentBuilder BUILDER;

    static {
        try {
            BUILDER = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch(ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document makeDocument() {
        return BUILDER.newDocument();
    }

    ///////////////////////////////////////////////////////////////////////////

    private static boolean isElementNode(Node node, String tagName) {
        Objects.requireNonNull(node);
        if(!(node instanceof Element)) return false;
        Element elem = (Element)node;
        return elem.getTagName().equalsIgnoreCase(tagName);
    }

    private static boolean isNonemptyElementNode(Node node, String tagName) {
        if(!isElementNode(node, tagName)) return false;
        else return ((Element)node).getChildNodes().getLength() > 0;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static final String NODE_WS = "whitespace";

    public static final String WS_ATTR_TYPE = "type";

    public static final String WS_TYPE_NEWLINE = "newline",
            WS_TYPE_INDENT = "indent";

    public static Element makeWhitespaceNode(Document doc) {
        Objects.requireNonNull(doc);
        return doc.createElement(NODE_WS);
    }

    public static Element makeWhitespaceNewlineNode(Document doc) {
        Element elem = makeWhitespaceNode(doc);
        elem.setAttribute(WS_ATTR_TYPE, WS_TYPE_NEWLINE);
        return elem;
    }

    public static Element makeIndentedNode(Document doc, Node indentedNode, Node... additionalIndentedNodes) {
        Objects.requireNonNull(indentedNode);
        Element elem = makeWhitespaceNode(doc);
        elem.setAttribute(WS_ATTR_TYPE, WS_TYPE_INDENT);
        elem.appendChild(indentedNode);
        if(additionalIndentedNodes != null) {
            for(Node node : additionalIndentedNodes) {
                if(node == null) continue;
                elem.appendChild(node);
            }
        }
        return elem;
    }

    public static boolean isWhitespaceNode(Node node) {
        return isElementNode(node, NODE_WS);
    }

    private static boolean isWhitespaceNode(Node node, String type) {
        return isWhitespaceNode(node) &&
                ((Element)node).hasAttribute(WS_ATTR_TYPE) &&
                type.equalsIgnoreCase(((Element)node).getAttribute(WS_ATTR_TYPE));
    }

    public static boolean isWhitespaceNewlineNode(Node node) {
        return isWhitespaceNode(node, WS_TYPE_NEWLINE);
    }

    public static boolean isWhitespaceIndentNode(Node node) {
        return isWhitespaceNode(node, WS_TYPE_INDENT);
    }

    private static void writeWhitespaceNode(DefaultStyledDocument doc,
            StyleContext styles, Node node, int indent,
            Collection<IdentifierLocation> idLocations, boolean flagged) throws BadLocationException {
        if(isWhitespaceNewlineNode(node)) {
            doc.insertString(doc.getLength(), "\n", styles.getStyle(STYLE_WS));
            if(indent > 0) {
                doc.insertString(doc.getLength(), indent(indent), styles.getStyle(STYLE_WS));
            }

        } else if(isWhitespaceIndentNode(node)) {
            doc.insertString(doc.getLength(), indent(1), styles.getStyle(STYLE_WS));
            NodeList children = node.getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
                write(doc, styles, children.item(i), indent + 1, idLocations, flagged);
            }

        } else {
            doc.insertString(doc.getLength(), " ", styles.getStyle(STYLE_WS));
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    public static final String NODE_TEXT = "text";

    public static Element makeTextNode(Document doc, String text) {
        Objects.requireNonNull(doc);
        Objects.requireNonNull(text);
        if(text.trim().isEmpty()) {
            return makeWhitespaceNode(doc);
        } else {
            Element txtElem = doc.createElement(NODE_TEXT);
            txtElem.appendChild(doc.createTextNode(text));
            return txtElem;
        }
    }

    public static boolean isTextNode(Node node) {
        return isElementNode(node, NODE_TEXT);
    }

    public static String getTextFromTextNode(Node node) {
        return node.getTextContent();
    }

    public static Element appendTextToTextNode(Element elem, String text) {
        Objects.requireNonNull(elem);
        Objects.requireNonNull(text);
        elem.setTextContent(getTextFromTextNode(elem) + text);
        return elem;
    }

    public static Element mergeTextNodes(Element parent) {
        NodeList children = parent.getChildNodes();
        Element currentText = null;
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(isTextNode(child)) {
                if(currentText == null) {
                    currentText = (Element)child;
                } else {
                    // remove child and append its text
                    appendTextToTextNode(currentText, getTextFromTextNode(child));
                    parent.removeChild(child);
                }

            } else {
                currentText = null;
            }
        }
        return parent;
    }

    //	public static List<Element> makeWhitespaceAndTextNodes(Document doc, String text) {
    //		Objects.requireNonNull(doc);
    //		Objects.requireNonNull(text);
    //		if(text.trim().isEmpty()) {
    //			return Arrays.asList(makeWhitespaceNode(doc));
    //		}
    //		List<Element> list = new ArrayList<>();
    //		if(Character.isWhitespace(text.charAt(0))) {
    //			list.add(makeWhitespaceNode(doc));
    //		}
    //		String[] split = text.trim().split("\\s+");
    //		for(int i = 0; i < split.length; i++) {
    //			if(i > 0) list.add(makeWhitespaceNode(doc));
    //			list.add(makeTextNode(doc, split[i]));
    //		}
    //		if(Character.isWhitespace(text.charAt(text.length() - 1))) {
    //			list.add(makeWhitespaceNode(doc));
    //		}
    //		return list;
    //	}

    private static void writeTextNode(DefaultStyledDocument doc,
            StyleContext styles, Node node, int indent,
            Collection<IdentifierLocation> idLocations, boolean flagged)
                    throws BadLocationException {
        String text = CoqDoc.getTextFromTextNode(node);
        Style style = styles.getStyle(STYLE_TEXT);
        if(flagged) style.addAttributes(styles.getStyle(STYLE_FLAGGED));
        doc.insertString(doc.getLength(), text, style);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static final String NODE_KEYWORD = "keyword";

    public static final String KEYWORD_ATTR_NAME = "name";

    public static Element makeKeywordNode(Document doc, String keyword) {
        Objects.requireNonNull(doc);
        Objects.requireNonNull(keyword);
        Element keywordElem = doc.createElement(NODE_KEYWORD);
        keywordElem.setAttribute(KEYWORD_ATTR_NAME, keyword);
        return keywordElem;
    }

    public static boolean isKeywordNode(Node node) {
        return isElementNode(node, NODE_KEYWORD) &&
                ((Element)node).hasAttribute(KEYWORD_ATTR_NAME);
    }

    public static String getKeywordFromKeywordNode(Node node) {
        return ((Element)node).getAttribute(KEYWORD_ATTR_NAME);
    }

    private static void writeKeywordNode(DefaultStyledDocument doc,
            StyleContext styles, Node node, int indent,
            Collection<IdentifierLocation> idLocations, boolean flagged)
                    throws BadLocationException {
        Style style = styles.getStyle(STYLE_KEYWORD);
        if(flagged) style.addAttributes(styles.getStyle(STYLE_FLAGGED));
        doc.insertString(doc.getLength(), getKeywordFromKeywordNode(node), style);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static final String NODE_IDENTIFIER = "identifier";

    public static final String IDENTIFIER_ATTR_NAME = KEYWORD_ATTR_NAME;

    public static Element makeIdentifierNode(Document doc, String id) {
        Objects.requireNonNull(doc);
        Objects.requireNonNull(id);
        Element idElem = doc.createElement(NODE_IDENTIFIER);
        idElem.setAttribute(IDENTIFIER_ATTR_NAME, id);
        return idElem;
    }

    public static Element makeIdentifierNode(Document doc, ID id) {
        Objects.requireNonNull(doc);
        Objects.requireNonNull(id);
        return makeIdentifierNode(doc, id.getFullName());
    }

    public static boolean isIdentifierNode(Node node) {
        return isElementNode(node, NODE_IDENTIFIER) &&
                ((Element)node).hasAttribute(IDENTIFIER_ATTR_NAME);
    }

    public static String getIDFromIdentifierNode(Node node) {
        return ((Element)node).getAttribute(IDENTIFIER_ATTR_NAME);
    }

    private static void writeIdentifierNode(DefaultStyledDocument doc,
            StyleContext styles, Node node, int indent,
            Collection<IdentifierLocation> idLocations, boolean flagged)
                    throws BadLocationException {
        String id = CoqDoc.getIDFromIdentifierNode(node);
        int offset = doc.getLength();
        Style style = styles.getStyle(STYLE_IDENTIFIER);
        if(flagged) style.addAttributes(styles.getStyle(STYLE_FLAGGED));
        doc.insertString(offset, id, style);
        if(idLocations != null) {
            idLocations.add(new IdentifierLocation(offset, id.length(), id));
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    public static final String NODE_TERM = "term";

    public static Element makeTermNode(Document doc, Element... children) {
        Objects.requireNonNull(doc);
        Element elem = doc.createElement(NODE_TERM);
        if(children != null) {
            for(Element child : children) {
                if(child == null) continue;

                // if it's a term, append its children
                if(isTermNode(child)) {
                    NodeList termChildren = child.getChildNodes();
                    for(int i = 0; i < termChildren.getLength(); i++) {
                        elem.appendChild(termChildren.item(i).cloneNode(true));
                    }
                } else {
                    elem.appendChild(child.cloneNode(true));
                }
            }
        }

        return elem;
    }

    public static Element makeParenthesizedTermNode(Document doc, Term term) {
        if(term.shouldParenthesize()) {
            return makeTermNode(doc,
                    makeKeywordNode(doc, "("),
                    term.makeCoqDocTerm(doc),
                    makeKeywordNode(doc, ")"));
        } else {
            return term.makeCoqDocTerm(doc);
        }
    }

    public static Element mergeTermNodes(Element term,
            Collection<Element> terms) {
        Objects.requireNonNull(term);
        Objects.requireNonNull(terms);
        for(Element t : terms) {
            if(t == null) continue;

            if(isTermNode(t)) {
                NodeList children = t.getChildNodes();
                for(int i = 0; i < children.getLength(); i++) {
                    term.appendChild(children.item(i).cloneNode(true));
                }

            } else {
                term.appendChild(t.cloneNode(true));
            }
        }
        mergeTextNodes(term);
        return term;
    }

    public static boolean isTermNode(Node node) {
        return isNonemptyElementNode(node, NODE_TERM);
    }

    private static void writeTermNode(DefaultStyledDocument doc,
            StyleContext styles, Node node, int indent,
            Collection<IdentifierLocation> idLocations, boolean flagged)
                    throws BadLocationException {
        NodeList children = ((Element)node).getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            write(doc, styles, children.item(i), indent, idLocations, flagged);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    public static final String NODE_FLAGGED = "flagged";
    
    public static Element makeFlaggedNode(Document doc, Node firstNode, Node... additionalNodes) {
        Objects.requireNonNull(doc);
        Objects.requireNonNull(firstNode);
        Element elem = doc.createElement(NODE_FLAGGED);
        elem.appendChild(firstNode);
        if(additionalNodes != null) {
            for(Node node : additionalNodes) {
                if(node == null) continue;
                elem.appendChild(node);
            }
        }
        return elem;
    }
    
    public static boolean isFlaggedNode(Node node) {
        return isNonemptyElementNode(node, NODE_FLAGGED);
    }
    
    private static void writeFlaggedNode(DefaultStyledDocument doc,
            StyleContext styles, Node node, int indent,
            Collection<IdentifierLocation> idLocations, boolean flagged)
                    throws BadLocationException {
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            write(doc, styles, children.item(i), indent + 1, idLocations, true);
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    public static final String STYLE_WS = NODE_WS,
            STYLE_TEXT = NODE_TEXT,
            STYLE_IDENTIFIER = NODE_IDENTIFIER,
            STYLE_KEYWORD = NODE_KEYWORD,
            STYLE_FLAGGED = NODE_FLAGGED;

    public static DefaultStyledDocument makeDocument(CoqDocable term,
            StyleContext styles, Collection<IdentifierLocation> idLocations,
            String optionalSuffix) {
        DefaultStyledDocument document = new DefaultStyledDocument();

        try {
            write(document, styles,
                    term.makeCoqDocTerm(CoqDoc.makeDocument()), 0,
                    idLocations, false);
            if(optionalSuffix != null) {
                document.insertString(document.getLength(), " ",
                        styles.getStyle(STYLE_WS));
                document.insertString(document.getLength(), optionalSuffix,
                        styles.getStyle(STYLE_TEXT));
            }
        } catch(BadLocationException e) {
            throw new RuntimeException(e);
        }

        return document;
    }

    private static String indent(int indent) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < indent; i++) {
            sb.append("    ");
        }
        return sb.toString();
    }

    private static void write(DefaultStyledDocument doc, StyleContext styles,
            Node node, int indent, Collection<IdentifierLocation> idLocations,
            boolean flagged)
                    throws BadLocationException {
        if(isWhitespaceNode(node)) {
            writeWhitespaceNode(doc, styles, node, indent, idLocations, flagged);
        } else if(isTextNode(node)) {
            writeTextNode(doc, styles, node, indent, idLocations, flagged);
        } else if(isKeywordNode(node)) {
            writeKeywordNode(doc, styles, node, indent, idLocations, flagged);
        } else if(isIdentifierNode(node)) {
            writeIdentifierNode(doc, styles, node, indent, idLocations, flagged);
        } else if(isTermNode(node)) {
            writeTermNode(doc, styles, node, indent, idLocations, flagged);
        } else if(isFlaggedNode(node)) {
            writeFlaggedNode(doc, styles, node, indent, idLocations, true);
        } else {
            throw new IllegalArgumentException("Unknown node type: " + XMLUtils.nodeToString(node));
        }
    }

    public static class IdentifierLocation {

        public final int offset, length;

        public final String id;

        public IdentifierLocation(int offset, int length, String id) {
            this.offset = offset;
            this.length = length;
            this.id = id;
        }

    }

}
