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
package org.compiere.apps.search;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

import org.compiere.apps.*;
import org.compiere.minigrid.*;
import org.compiere.swing.*;
import org.compiere.util.*;


/**
 *	Display Product Attribute Instance Info
 *
 *  @author     Jorg Janke
 *  @version    $Id: PAttributeInstance.java,v 1.3 2006/07/30 00:51:27 jjanke Exp $
 */
public class PAttributeInstance extends CDialog 
	implements ListSelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 	Constructor
	 * 	@param parent frame parent
	 * 	@param title title
	 * 	@param M_Warehouse_ID warehouse key name pair
	 * 	@param M_Locator_ID locator
	 * 	@param M_Product_ID product key name pair
	 * 	@param C_BPartner_ID bp
	 */
	public PAttributeInstance(JFrame parent, String title,
		int M_Warehouse_ID, int M_Locator_ID, int M_Product_ID, int C_BPartner_ID)
	{
		super (parent, Msg.getMsg(Env.getCtx(), "PAttributeInstance") + ": " + title, true);
		init (M_Warehouse_ID, M_Locator_ID, M_Product_ID, C_BPartner_ID);
		AEnv.showCenterWindow(parent, this);
	}	//	PAttributeInstance
	
	/**
	 * 	Constructor
	 * 	@param parent dialog parent
	 * 	@param title title
	 * 	@param M_Warehouse_ID warehouse key name pair
	 * 	@param M_Locator_ID locator
	 * 	@param M_Product_ID product key name pair
	 * 	@param C_BPartner_ID bp
	 */
	public PAttributeInstance(JDialog parent, String title,
		int M_Warehouse_ID, int M_Locator_ID, int M_Product_ID, int C_BPartner_ID)
	{
		super (parent, Msg.getMsg(Env.getCtx(), "PAttributeInstance") + title, true);
		init (M_Warehouse_ID, M_Locator_ID, M_Product_ID, C_BPartner_ID);
		AEnv.showCenterWindow(parent, this);
	}	//	PAttributeInstance

	/**
	 * 	Initialization
	 *	@param M_Warehouse_ID wh
	 *	@param M_Locator_ID loc
	 *	@param M_Product_ID product
	 *	@param C_BPartner_ID partner
	 */
	private void init (int M_Warehouse_ID, int M_Locator_ID, int M_Product_ID, int C_BPartner_ID)
	{
		log.info("M_Warehouse_ID=" + M_Warehouse_ID 
			+ ", M_Locator_ID=" + M_Locator_ID
			+ ", M_Product_ID=" + M_Product_ID);
		m_M_Warehouse_ID = M_Warehouse_ID;
		m_M_Locator_ID = M_Locator_ID;
		m_M_Product_ID = M_Product_ID;
		try
		{
			jbInit();
			dynInit(C_BPartner_ID);
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "", e);
		}
		Dimension size = getPreferredSize();
		size.width = 1060;
		setPreferredSize(size);
	}	// init	

	private CPanel mainPanel = new CPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private CPanel northPanel = new CPanel();
	private BorderLayout northLayout = new BorderLayout();
	private JScrollPane centerScrollPane = new JScrollPane();
	private ConfirmPanel confirmPanel = new ConfirmPanel (true);
	private CCheckBox showAll = new CCheckBox (Msg.getMsg(Env.getCtx(), "ShowAll"));
	//
	private MiniTable 			m_table = new MiniTable();
	//	Parameter
	private int			 		m_M_Warehouse_ID;
	private int			 		m_M_Locator_ID;
	private int			 		m_M_Product_ID;
	//
	private int					m_M_AttributeSetInstance_ID = -1;
	private String				m_M_AttributeSetInstanceName = null;
	private String				m_sql;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(PAttributeInstance.class);

	/**
	 * 	Static Init
	 * 	@throws Exception
	 */
	private void jbInit() throws Exception
	{
		mainPanel.setLayout(mainLayout);
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		//	North
		northPanel.setLayout(northLayout);
		northPanel.add(showAll, BorderLayout.EAST);
		showAll.addActionListener(this);
		mainPanel.add(northPanel, BorderLayout.NORTH);
		//	Center
		mainPanel.add(centerScrollPane, BorderLayout.CENTER);
		centerScrollPane.getViewport().add(m_table, null);
		//	South
		mainPanel.add(confirmPanel, BorderLayout.SOUTH);
		confirmPanel.addActionListener(this);
	}	//	jbInit

	/**	Table Column Layout Info			*/
	private static ColumnInfo[] s_layout = new ColumnInfo[] 
	{
		new ColumnInfo(" ", "s.M_AttributeSetInstance_ID", IDColumn.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "Description"), "asi.Description", String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "Lot"), "asi.Lot", String.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "SerNo"), "asi.SerNo", String.class), 
		new ColumnInfo(Msg.translate(Env.getCtx(), "GuaranteeDate"), "asi.GuaranteeDate", Timestamp.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "M_Locator_ID"), "l.Value", KeyNamePair.class, "s.M_Locator_ID"),
		new ColumnInfo(Msg.translate(Env.getCtx(), "QtyOnHand"), "s.QtyOnHand", Double.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "QtyReserved"), "s.QtyReserved", Double.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "QtyOrdered"), "s.QtyOrdered", Double.class),
		//	See RV_Storage
		//jz new ColumnInfo(Msg.translate(Env.getCtx(), "GoodForDays"), "(TRUNC(asi.GuaranteeDate,'DD')-TRUNC(SysDate,'DD'))-p.GuaranteeDaysMin", Integer.class, true, true, null),
		//new ColumnInfo(Msg.translate(Env.getCtx(), "ShelfLifeDays"), "TRUNC(asi.GuaranteeDate,'DD')-TRUNC(SysDate,'DD')", Integer.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "GoodForDays"), "daysBetween(TRUNC(asi.GuaranteeDate,'DD'),TRUNC(SysDate,'DD'))-p.GuaranteeDaysMin", Integer.class, true, true, null),
		new ColumnInfo(Msg.translate(Env.getCtx(), "ShelfLifeDays"), "daysBetween(TRUNC(asi.GuaranteeDate,'DD'),TRUNC(SysDate,'DD'))", Integer.class),
		//jz new ColumnInfo(Msg.translate(Env.getCtx(), "ShelfLifeRemainingPct"), "CASE WHEN p.GuaranteeDays > 0 THEN TRUNC(((TRUNC(asi.GuaranteeDate,'DD')-TRUNC(SysDate,'DD'))/p.GuaranteeDays)*100) ELSE 0 END", Integer.class),
		new ColumnInfo(Msg.translate(Env.getCtx(), "ShelfLifeRemainingPct"), "CASE WHEN p.GuaranteeDays > 0 THEN daysBetween(TRUNC(asi.GuaranteeDate,'DD'),TRUNC(SysDate,'DD'))/p.GuaranteeDays*100 ELSE 0 END", Integer.class),
	};
	/**	From Clause							*/
	private static String s_sqlFrom = "M_Storage s"
		+ " INNER JOIN M_Locator l ON (s.M_Locator_ID=l.M_Locator_ID)"
		+ " INNER JOIN M_Product p ON (s.M_Product_ID=p.M_Product_ID)"
		+ " LEFT OUTER JOIN M_AttributeSetInstance asi ON (s.M_AttributeSetInstance_ID=asi.M_AttributeSetInstance_ID)";
	/** Where Clause						*/
	private static String s_sqlWhere = "l.M_Warehouse_ID=? AND s.M_Product_ID=?"; 

	private String	m_sqlNonZero = " AND (s.QtyOnHand<>0 OR s.QtyReserved<>0 OR s.QtyOrdered<>0)";
	private String	m_sqlMinLife = "";

	/**
	 * 	Dynamic Init
	 * 	@param C_BPartner_ID BP
	 */
	private void dynInit(int C_BPartner_ID)
	{
		log.config("C_BPartner_ID=" + C_BPartner_ID);
		if (C_BPartner_ID != 0)
		{
			int ShelfLifeMinPct = 0;
			int ShelfLifeMinDays = 0;
			String sql = "SELECT bp.ShelfLifeMinPct, bpp.ShelfLifeMinPct, bpp.ShelfLifeMinDays "
				+ "FROM C_BPartner bp "
				+ " LEFT OUTER JOIN C_BPartner_Product bpp"
				+	" ON (bp.C_BPartner_ID=bpp.C_BPartner_ID AND bpp.M_Product_ID=?) "
				+ "WHERE bp.C_BPartner_ID=?";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql, (Trx) null);
				pstmt.setInt(1, m_M_Product_ID);
				pstmt.setInt(2, C_BPartner_ID);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
				{
					ShelfLifeMinPct = rs.getInt(1);		//	BP
					int pct = rs.getInt(2);				//	BP_P
					if (pct > 0)	//	overwrite
						ShelfLifeMinDays = pct;
					ShelfLifeMinDays = rs.getInt(3);
				}
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, sql, e);
			}
			try
			{
				if (pstmt != null)
					pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				pstmt = null;
			}
			if (ShelfLifeMinPct > 0)
			{
				//jz daysbetween???
				//m_sqlMinLife = " AND COALESCE(TRUNC(((TRUNC(asi.GuaranteeDate,'DD')-TRUNC(SysDate,'DD'))/p.GuaranteeDays)*100),0)>=" + ShelfLifeMinPct;
				m_sqlMinLife = " AND COALESCE(TRUNC((daysBetween(TRUNC(asi.GuaranteeDate,'DD'),TRUNC(SysDate,'DD'))/p.GuaranteeDays)*100),0)>=" + ShelfLifeMinPct;
				log.config( "PAttributeInstance.dynInit - ShelfLifeMinPct=" + ShelfLifeMinPct);
			}
			if (ShelfLifeMinDays > 0)
			{
				//jz m_sqlMinLife += " AND COALESCE((TRUNC(asi.GuaranteeDate,'DD')-TRUNC(SysDate,'DD')),0)>=" + ShelfLifeMinDays;
				m_sqlMinLife += " AND COALESCE(daysBetween(TRUNC(asi.GuaranteeDate,'DD'),TRUNC(SysDate,'DD')),0)>=" + ShelfLifeMinDays;
				log.config( "PAttributeInstance.dynInit - ShelfLifeMinDays=" + ShelfLifeMinDays);
			}
		}	//	BPartner != 0

		m_sql = m_table.prepareTable (s_layout, s_sqlFrom, 
			s_sqlWhere, false, "s")
			+ " ORDER BY asi.GuaranteeDate, s.QtyOnHand";	//	oldest, smallest first
		//
		m_table.setRowSelectionAllowed(true);
		m_table.setMultiSelection(false);
		m_table.addMouseListener(this);
		m_table.getSelectionModel().addListSelectionListener(this);
		//
		refresh();
	}	//	dynInit

	/**
	 * 	Refresh Query
	 */
	private void refresh()
	{
		String sql = m_sql;
		int pos = m_sql.lastIndexOf(" ORDER BY ");
		if (!showAll.isSelected())
		{
			sql = m_sql.substring(0, pos) 
				+ m_sqlNonZero;
			if (m_sqlMinLife.length() > 0)
				sql += m_sqlMinLife;
			sql += m_sql.substring(pos);
		}
		//
		log.finest(sql);
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			pstmt.setInt(1, m_M_Warehouse_ID);
			pstmt.setInt(2, m_M_Product_ID);
			ResultSet rs = pstmt.executeQuery();
			m_table.loadTable(rs);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		enableButtons();
	}	//	refresh

	/**
	 * 	Action Listener
	 *	@param e event 
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals(ConfirmPanel.A_OK))
			dispose();
		else if (e.getActionCommand().equals(ConfirmPanel.A_CANCEL))
		{
			dispose();
			m_M_AttributeSetInstance_ID = -1;
			m_M_AttributeSetInstanceName = null;
		}
		else if (e.getSource() == showAll)
		{
			refresh();
		}
	}	//	actionPerformed
 
	/**
	 * 	Table selection changed
	 *	@param e event
	 */
	public void valueChanged (ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting())
			return;
		enableButtons();
	}	//	valueChanged

	/**
	 * 	Enable/Set Buttons and set ID
	 */
	private void enableButtons()
	{
		m_M_AttributeSetInstance_ID = -1;
		m_M_AttributeSetInstanceName = null;
		m_M_Locator_ID = 0;
		int row = m_table.getSelectedRow();
		boolean enabled = row != -1;
		if (enabled)
		{
			Integer ID = m_table.getSelectedRowKey();
			if (ID != null)
			{
				m_M_AttributeSetInstance_ID = ID.intValue();
				m_M_AttributeSetInstanceName = (String)m_table.getValueAt(row, 1);
				//
				Object oo = m_table.getValueAt(row, 5);
				if (oo instanceof KeyNamePair)
				{
					KeyNamePair pp = (KeyNamePair)oo;
					m_M_Locator_ID = pp.getKey();
				}
			}
		}
		confirmPanel.getOKButton().setEnabled(enabled);
		log.fine("M_AttributeSetInstance_ID=" + m_M_AttributeSetInstance_ID 
			+ " - " + m_M_AttributeSetInstanceName
			+ "; M_Locator_ID=" + m_M_Locator_ID);
	}	//	enableButtons

	/**
	 *  Mouse Clicked
	 *  @param e event
	 */
	@Override
	public void mouseClicked(MouseEvent e)
	{
		//  Double click with selected row => exit
		if (e.getClickCount() > 1 && m_table.getSelectedRow() != -1)
		{
			enableButtons();
			dispose();
		}
	}   //  mouseClicked


	/**
	 * 	Get Attribute Set Instance
	 *	@return M_AttributeSetInstance_ID or -1
	 */
	public int getM_AttributeSetInstance_ID()
	{
		return m_M_AttributeSetInstance_ID;
	}	//	getM_AttributeSetInstance_ID

	/**
	 * 	Get Instance Name
	 * 	@return Instance Name
	 */
	public String getM_AttributeSetInstanceName()
	{
		return m_M_AttributeSetInstanceName;
	}	//	getM_AttributeSetInstanceName

	/**
	 * 	Get Locator
	 *	@return M_Locator_ID or 0
	 */
	public int getM_Locator_ID()
	{
		return m_M_Locator_ID;
	}	//	getM_Locator_ID

}	//	PAttributeInstance
