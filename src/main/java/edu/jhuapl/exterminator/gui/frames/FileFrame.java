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
package edu.jhuapl.exterminator.gui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import edu.jhuapl.exterminator.gui.ExterminatorGUI;
import edu.jhuapl.exterminator.gui.TextLineNumber;

public class FileFrame extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	
	private final DefaultHighlightPainter EXEC_HIGHLIGHTER = new DefaultHighlightPainter(Color.YELLOW);
	
	private final ExterminatorGUI parent;
	
	private final JToolBar toolBar;
	
	private final Action actionOpen, actionInterpNext;//, actionInterpToCursor;//, actionInterpNextLine;
	
	private final JTextPane text;
	
	private int currentPos;

	public FileFrame(ExterminatorGUI parent) {
		super("File Editor", true, false, true, true);
		
		this.parent = parent;
		this.currentPos = 0;
		
		setLayout(new BorderLayout());
		
		///////////////////////////////////////////////////////////////////////
		
		toolBar = new JToolBar();
		add(toolBar, BorderLayout.PAGE_START);
		
		actionOpen = new OpenAction();
		toolBar.add(actionOpen);

		actionInterpNext = new InterpNext();
		toolBar.add(actionInterpNext);
		
//		actionInterpToCursor = new InterpUntilCursorAction();
//		toolBar.add(actionInterpToCursor);
		
//		actionInterpNextLine = new InterpNextLineAction();
//		toolBar.add(actionInterpNextLine);
		
		///////////////////////////////////////////////////////////////////////
		
		text = new JTextPane();
		text.setEditable(false);
		
		JScrollPane pane = new JScrollPane(text);
		pane.setRowHeaderView(new TextLineNumber(text));
		add(pane, BorderLayout.CENTER);
		
		///////////////////////////////////////////////////////////////////////
		
		setPreferredSize(new Dimension(400, 500));
				
		pack();
	}
	
	public Action getActionOpen() { return actionOpen; }
	
	//public Action getActionInterpNextLine() { return actionInterpNextLine; }
	
	public void load(Path file) {
//		try {
//			this.fileLines = Files.readAllLines(file, Charset.defaultCharset());
//		} catch(IOException e) {
//			throw new RuntimeException(e);
//		}
		this.currentPos = 0;

		try(BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset())) {
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}

			text.setText(sb.toString());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private class OpenAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		
		public OpenAction() {
			super("Open");
			putValue(SHORT_DESCRIPTION, "Open a Coq file");
			putValue(MNEMONIC_KEY, KeyEvent.VK_O);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
			        KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			int ret = chooser.showOpenDialog(parent);
			if(ret == JFileChooser.APPROVE_OPTION) {
				load(chooser.getSelectedFile().toPath());
			}
		}
		
	}
	
//	private class InterpNextLineAction extends AbstractAction {
//		
//		private static final long serialVersionUID = 1L;
//		
//		public InterpNextLineAction() {
//			super("Interp Next Line");
//			putValue(SHORT_DESCRIPTION, "Interpret the next line");
//		}
//		
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			currentLine++;
//			if(currentLine < fileLines.size()) {
//				String line = fileLines.get(currentLine);
//				if(line.trim().isEmpty()) return;
//				parent.interpLine(currentLine, line);
//			}
//		}
//		
//	}
	
//	private class InterpUntilCursorAction extends AbstractAction {
//		
//		private static final long serialVersionUID = 1L;
//		
//		public InterpUntilCursorAction() {
//			super("Interp Until Cursor");
//			putValue(SHORT_DESCRIPTION, "Interpret until the cursor");
//		}
//		
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			int cursor = text.getCaretPosition();
//			try {
//				String str = text.getText(currentPos, cursor - currentPos);
//				currentPos = cursor;
//				parent.interp(str);
//			} catch(BadLocationException e1) {
//				throw new RuntimeException(e1);
//			}
//		}
//		
//	}
	
	private class InterpNext extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		
		public InterpNext() {
			super("Interp Next");
			putValue(SHORT_DESCRIPTION, "Interpret next statement.");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				String str = text.getText(currentPos, text.getDocument().getLength() - currentPos);
				int i = str.indexOf(".\n");
				str = str.substring(0, i + 2).trim();
				currentPos += i + 2;
				parent.interp(str, true);

				text.getHighlighter().addHighlight(0, currentPos, 
						EXEC_HIGHLIGHTER);
				text.setCaretPosition(currentPos);
				
			} catch(BadLocationException e1) {
				throw new RuntimeException(e1);
			}
		}
		
	}
	
}
