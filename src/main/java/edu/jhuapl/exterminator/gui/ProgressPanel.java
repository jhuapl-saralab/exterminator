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
package edu.jhuapl.exterminator.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private final JPanel inner;
	
	private final JLabel operationName;
	
	private final JProgressBar progress;
	
	private final JLabel message, subMessage, subSubMessage;
	
	public ProgressPanel() {
		super(new GridBagLayout());
		
		//setBackground(GUIUtils.changeAlpha(Color.DARK_GRAY, 0.5));
		setOpaque(false);
		
		inner = new JPanel();
		add(inner);
		
		inner.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)
				));
		
		inner.setLayout(new BorderLayout());
		
		this.operationName = new JLabel();
		inner.add(operationName, BorderLayout.NORTH);
		
		operationName.setFont(operationName.getFont().deriveFont(24.0f));
		operationName.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JPanel panel = new JPanel();
		inner.add(panel, BorderLayout.CENTER);
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		this.progress = new JProgressBar();
		panel.add(progress);
		
		progress.setAlignmentX(Component.CENTER_ALIGNMENT);

		this.message = new JLabel();
		panel.add(message);
		
		message.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		this.subMessage = new JLabel();
		panel.add(subMessage);
		
		subMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		this.subSubMessage = new JLabel();
		panel.add(subSubMessage);
		
		subSubMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
	}
	
	public void init(String operationName) {
		this.operationName.setText(operationName);
		setProgressIndeterminate();
		this.message.setText("");
		this.subMessage.setText("");
		this.subSubMessage.setText("");
	}
	
	public void setProgressIndeterminate() {
		this.progress.setIndeterminate(true);
	}
	
	public void setMessage(String message) {
		this.message.setText(message);
		this.subMessage.setText("");
		this.subSubMessage.setText("");
	}
	
	public void setSubMessage(String message) {
		this.subMessage.setText("       " + message);
		this.subSubMessage.setText("");
	}
	
	public void setSubSubMessage(String message) {
		this.subSubMessage.setText("              " + message);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g.create();
	    g2.setColor(Color.DARK_GRAY);
	    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
	    g2.fill(new Rectangle2D.Float(0, 0, getWidth(), getHeight()));
	    g2.dispose();
	    
		paintChildren(g);
	}
}
