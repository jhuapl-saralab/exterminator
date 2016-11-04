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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class GUIUtils {

    public static JPanel leftAlignedPanelWith(Component ... components) {
        return leftAlignedPanelWith(0, components);
    }

    public static JPanel leftAlignedPanelWith(int spacing, Component ... components) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        boolean first = true;
        for(Component component : components) {
            if(!first && spacing > 0) {
                panel.add(Box.createHorizontalStrut(spacing));
            }
            panel.add(component);
            first = false;
        }
        panel.add(Box.createHorizontalGlue());
        return panel;
    }
	
    public static JPanel centeredPanelWith(Component... components) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        for(Component c : components) {
            panel.add(c);
        }
        return panel;
    }

    public static ImageIcon scale(ImageIcon icon, int maxWidthAndHeight) {
        return scale(icon, maxWidthAndHeight, maxWidthAndHeight);
    }

    public static ImageIcon scale(ImageIcon icon, int maxWidth, int maxHeight) {
        if(icon == null) return null;

        if(maxWidth >= icon.getIconWidth() && maxHeight >= icon.getIconHeight()) return icon;

        int width = Math.min(maxWidth, icon.getIconWidth()),
                height = Math.min(maxHeight, icon.getIconHeight());

        float widthFactor = (float)width / icon.getIconWidth(),
                heightFactor = (float)height / icon.getIconHeight();

        if(widthFactor < heightFactor) {
            height = Math.round(widthFactor * icon.getIconHeight());
        } else if(heightFactor < widthFactor) {
            width = Math.round(heightFactor * icon.getIconWidth());
        }

        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        return new ImageIcon(newImg);
    }
    
    public static <T extends Component> T setFixedHeight(T component, int height) {
    	Dimension d = component.getMinimumSize();
    	d.height = height;
    	component.setMinimumSize(d);
    	
    	d = component.getMaximumSize();
    	d.height = height;
    	component.setMaximumSize(d);

    	d = component.getPreferredSize();
    	d.height = height;
    	component.setPreferredSize(d);
    	
    	return component;
    }

    public static Color changeAlpha(Color c, double newAlpha) {
        if(newAlpha < 0) newAlpha = 0;
        if(newAlpha > 1) newAlpha = 1;
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)Math.round(newAlpha * 255));
    }
    
}
