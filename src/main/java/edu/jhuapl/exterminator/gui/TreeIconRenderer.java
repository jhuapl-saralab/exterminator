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

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

public class TreeIconRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private static final int MAX_HEIGHT_WIDTH = 15;
	
	private static final Map<Class<?>, ImageIcon> ICONS = new HashMap<>();
	
	public static void registerIcon(Class<?> type, ImageIcon icon) {
		Objects.requireNonNull(type);
		synchronized(ICONS) {
			ICONS.put(type, icon);
		}
	}
	
	public static void clearIcon(Class<?> type) {
		Objects.requireNonNull(type);
		synchronized(ICONS) {
			ICONS.remove(type);
		}
	}
	
	public static boolean hasIconFor(Class<?> type) {
		Objects.requireNonNull(type);
		synchronized(ICONS) {
			return ICONS.containsKey(type);
		}
	}
	
	public static ImageIcon getIconFor(Class<?> type) {
		Objects.requireNonNull(type);
		synchronized(ICONS) {
			return ICONS.get(type);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public static interface IconProvider {
		public ImageIcon getIcon();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private final boolean useDefaultImageIfNoneDefined;
	
	public TreeIconRenderer(boolean useDefaultImageIfNoneDefined) {
		this.useDefaultImageIfNoneDefined = useDefaultImageIfNoneDefined;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, hasFocus);
		
		if(value instanceof IconProvider) {
			label.setIcon(GUIUtils.scale(((IconProvider)value).getIcon(),
					MAX_HEIGHT_WIDTH));
			return label;
		}
		
		if(!hasIconFor(value.getClass()) && useDefaultImageIfNoneDefined) {
			return label; 
		}
		
		ImageIcon image = getIconFor(value.getClass());
		if(image != null) {
			image = GUIUtils.scale(image, MAX_HEIGHT_WIDTH);
		}
		label.setIcon(image);
		return label;
	}

}
