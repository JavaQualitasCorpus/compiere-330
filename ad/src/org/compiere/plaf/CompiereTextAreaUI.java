/******************************************************************************
 * Product: Compiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 3600 Bridge Parkway #102, Redwood City, CA 94065, USA      *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.plaf;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.*;


public class CompiereTextAreaUI extends BasicTextAreaUI
{
    /**
     *	Creates a UI for a JTextArea.
     * 	Tab is the normal focus traversal key - to enter Tab enter Ctrl-Tab 
     *
     *	@param ta a text area
     *	@return the UI
     */
    public static ComponentUI createUI(JComponent ta) 
    {
        return new CompiereTextAreaUI(ta);
    }
    
    /**
     * 	Constructor
     *	@param ta text area
     */
    public CompiereTextAreaUI (JComponent ta)
    {
    	if (ta instanceof JTextComponent)
    		m_editor = (JTextComponent)ta;
    }	//	CompiereTextAreaUI
    
    /**	The Editor				*/
    private JTextComponent 		m_editor = null;
    /** Tab Stroke				*/
    private static KeyStroke	s_stroke = KeyStroke.getKeyStroke (KeyEvent.VK_TAB, InputEvent.CTRL_MASK);
    /** Tab Action				*/
    private static Action		s_action = new DefaultEditorKit.InsertTabAction();
    
    /**
     * 	Create Keymap
     *	@return key Map
     */
    @Override
	protected Keymap createKeymap ()
    {
    	Keymap map = super.createKeymap ();
    	map.addActionForKeyStroke(s_stroke, s_action);
    	return map;
    }	//	createKeyMap
    
    /**
     * 	Property Change
     *	@param evt event
     */
    @Override
	protected void propertyChange (PropertyChangeEvent evt)
    {
    	String name = evt.getPropertyName();
    	if ("editable".equals(name))
    		updateFocusTraversalKeysX();
   	    else
   	    	super.propertyChange (evt);
    }	//	propertyChange
    
    /**
     * 	UpdateFocusTraversalKeysX
     */
	void updateFocusTraversalKeysX ()
	{
		if (m_editor == null)
			return;
		//
		EditorKit editorKit = getEditorKit (m_editor);
		if (editorKit != null && editorKit instanceof DefaultEditorKit)
		{
			Set<AWTKeyStroke> storedForwardTraversalKeys = m_editor.getFocusTraversalKeys (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
			Set<AWTKeyStroke> storedBackwardTraversalKeys = m_editor.getFocusTraversalKeys (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
			Set<AWTKeyStroke> forwardTraversalKeys = new HashSet<AWTKeyStroke>(storedForwardTraversalKeys);
			Set<AWTKeyStroke> backwardTraversalKeys = new HashSet<AWTKeyStroke>(storedBackwardTraversalKeys);
			//
			forwardTraversalKeys.add (KeyStroke.getKeyStroke (KeyEvent.VK_TAB, 0));
			forwardTraversalKeys.remove(s_stroke);
			backwardTraversalKeys.add (KeyStroke.getKeyStroke (KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
			//
			LookAndFeel.installProperty (m_editor, "focusTraversalKeysForward",	forwardTraversalKeys);
			LookAndFeel.installProperty (m_editor, "focusTraversalKeysBackward", backwardTraversalKeys);
		}
	}	//	updateFocusTraversalKeysX
    
}	//	CompiereTextAreaUI
