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
import java.sql.*;

import org.compiere.apps.*;
import org.compiere.common.constants.*;
import org.compiere.framework.*;
import org.compiere.grid.ed.*;
import org.compiere.minigrid.*;
import org.compiere.model.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;
import org.compiere.util.*;


/**
 *  Info InOut
 *
 *  @author Jorg Janke
 *  @version  $Id: InfoInOut.java,v 1.2 2006/07/30 00:51:27 jjanke Exp $
 */
public class InfoInOut extends Info
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *  Detail Protected Contructor
	 *  @param frame parent frame
	 *  @param modal modal
	 *  @param WindowNo window no
	 *  @param value query value
	 *  @param multiSelection multiple selections
	 *  @param whereClause where clause
	 */
	protected InfoInOut(Frame frame, boolean modal, int WindowNo, String value,
		boolean multiSelection, String whereClause)
	{
		super (frame, modal, WindowNo, "i", "M_InOut_ID", multiSelection, whereClause);
		log.info( "InfoInOut");
		setTitle(Msg.getMsg(Env.getCtx(), "InfoInOut"));
		//
		try
		{
			statInit();
			p_loadedOK = initInfo ();
		}
		catch (Exception e)
		{
			return;
		}
		//
		int no = p_table.getRowCount();
		setStatusLine(Integer.toString(no) + " " + Msg.getMsg(Env.getCtx(), "SearchRows_EnterQuery"), false);
		setStatusDB(Integer.toString(no));
		if (value != null && value.length() > 0)
		{
			fDocumentNo.setValue(value);
			executeQuery();
		}
		//
		pack();
		//	Focus
		fDocumentNo.requestFocus();
	}   //  InfoInOut


	//  Static Info
	private CLabel lDocumentNo = new CLabel(Msg.translate(Env.getCtx(), "DocumentNo"));
	private CTextField fDocumentNo = new CTextField(10);
	private CLabel lDescription = new CLabel(Msg.translate(Env.getCtx(), "Description"));
	private CTextField fDescription = new CTextField(10);
	private CLabel lPOReference = new CLabel(Msg.translate(Env.getCtx(), "POReference"));
	private CTextField fPOReference = new CTextField(10);
	//
//	private CLabel lOrg_ID = new CLabel(Msg.translate(Env.getCtx(), "AD_Org_ID"));
//	private VLookup fOrg_ID;
	private CLabel lBPartner_ID = new CLabel(Msg.translate(Env.getCtx(), "BPartner"));
	private VLookup fBPartner_ID;
	//
	private CLabel lDateFrom = new CLabel(Msg.translate(Env.getCtx(), "MovementDate"));
	private VDate fDateFrom = new VDate("DateFrom", false, false, true, DisplayTypeConstants.Date, Msg.translate(Env.getCtx(), "DateFrom"));
	private CLabel lDateTo = new CLabel("-");
	private VDate fDateTo = new VDate("DateTo", false, false, true, DisplayTypeConstants.Date, Msg.translate(Env.getCtx(), "DateTo"));
	private VCheckBox fIsSOTrx = new VCheckBox ("IsSOTrx", false, false, true, Msg.getElement(Env.getCtx(), "IsSOTrx"), "", false);

	/**  Array of Column Info    */
	private static final Info_Column[] s_inOutLayout = {
		new Info_Column(" ", "i.M_InOut_ID", IDColumn.class),
		new Info_Column(Msg.translate(Env.getCtx(), "C_BPartner_ID"), "(SELECT Name FROM C_BPartner bp WHERE bp.C_BPartner_ID=i.C_BPartner_ID)", String.class),
		new Info_Column(Msg.translate(Env.getCtx(), "MovementDate"), "i.MovementDate", Timestamp.class),
		new Info_Column(Msg.translate(Env.getCtx(), "DocumentNo"), "i.DocumentNo", String.class),
		new Info_Column(Msg.translate(Env.getCtx(), "Description"), "i.Description", String.class),
		new Info_Column(Msg.translate(Env.getCtx(), "POReference"), "i.POReference", String.class),
		new Info_Column(Msg.translate(Env.getCtx(), "IsSOTrx"), "i.IsSOTrx", Boolean.class)
	};

	/**
	 *	Get Layout
	 *	@return array of Column_Info
	 */
	@Override
	protected Info_Column[] getInfoColumns()
	{
		return s_inOutLayout;
	}	//	getInfoColumns

	/**
	 *	Static Setup - add fields to parameterPanel
	 *  @throws Exception if Lookups cannot be initialized
	 */
	private void statInit() throws Exception
	{
		Ctx ctx = Env.getCtx();
		lDocumentNo.setLabelFor(fDocumentNo);
		fDocumentNo.setBackground(CompierePLAF.getInfoBackground());
		fDocumentNo.addActionListener(this);
		lDescription.setLabelFor(fDescription);
		fDescription.setBackground(CompierePLAF.getInfoBackground());
		fDescription.addActionListener(this);
		lPOReference.setLabelFor(lPOReference);
		fPOReference.setBackground(CompierePLAF.getInfoBackground());
		fPOReference.addActionListener(this);
		fIsSOTrx.setSelected(ctx.isSOTrx(p_WindowNo));
		fIsSOTrx.addActionListener(this);
		//
	//	fOrg_ID = new VLookup("AD_Org_ID", false, false, true,
	//		MLookupFactory.create(Env.getCtx(), 3486, m_WindowNo, DisplayType.TableDir, false),
	//		DisplayType.TableDir, m_WindowNo);
	//	lOrg_ID.setLabelFor(fOrg_ID);
	//	fOrg_ID.setBackground(CompierePLAF.getInfoBackground());
		fBPartner_ID = new VLookup("C_BPartner_ID", false, false, true,
			MLookupFactory.get (Env.getCtx(), p_WindowNo, 3499, DisplayTypeConstants.Search));
		lBPartner_ID.setLabelFor(fBPartner_ID);
		fBPartner_ID.setBackground(CompierePLAF.getInfoBackground());
		//
		lDateFrom.setLabelFor(fDateFrom);
		fDateFrom.setBackground(CompierePLAF.getInfoBackground());
		fDateFrom.setToolTipText(Msg.translate(Env.getCtx(), "DateFrom"));
		lDateTo.setLabelFor(fDateTo);
		fDateTo.setBackground(CompierePLAF.getInfoBackground());
		fDateTo.setToolTipText(Msg.translate(Env.getCtx(), "DateTo"));
		//
		parameterPanel.setLayout(new ALayout());
		//  First Row
		parameterPanel.add(lDocumentNo, new ALayoutConstraint(0,0));
		parameterPanel.add(fDocumentNo, null);
		parameterPanel.add(lBPartner_ID, null);
		parameterPanel.add(fBPartner_ID, null);
		parameterPanel.add(fIsSOTrx, new ALayoutConstraint(0,5));
		//  2nd Row
		parameterPanel.add(lDescription, new ALayoutConstraint(1,0));
		parameterPanel.add(fDescription, null);
		parameterPanel.add(lDateFrom, null);
		parameterPanel.add(fDateFrom, null);
		parameterPanel.add(lDateTo, null);
		parameterPanel.add(fDateTo, null);
		//  3rd Row
		parameterPanel.add(lPOReference, new ALayoutConstraint(2,0));
		parameterPanel.add(fPOReference, null);
	//	parameterPanel.add(lOrg_ID, null);
	//	parameterPanel.add(fOrg_ID, null);
	}	//	statInit

	/**
	 *	General Init
	 *	@return true, if success
	 */
	private boolean initInfo ()
	{
		Ctx ctx = Env.getCtx();
		//  Set Defaults
		String bp = ctx.getContext(p_WindowNo, "C_BPartner_ID");
		if (bp != null && bp.length() != 0)
			fBPartner_ID.setValue(Integer.valueOf(bp));

		//  prepare table
		StringBuffer where = new StringBuffer("i.IsActive='Y'");
		if (p_whereClause.length() > 0)
			where.append(" AND ").append(Util.replace(p_whereClause, "M_InOut.", "i."));
		prepareTable("M_InOut i",
			where.toString(),
			"2,3,4");

		return true;
	}	//	initInfo

	/*************************************************************************/

	/**
	 *	Construct SQL Where Clause and define parameters.
	 *  (setParameters needs to set parameters)
	 *  Includes first AND
	 * 	@return where clause
	 */
	@Override
	String getSQLWhere()
	{
		StringBuffer sql = new StringBuffer();
		if (fDocumentNo.getText().length() > 0)
			sql.append(" AND UPPER(i.DocumentNo) LIKE ?");
		if (fDescription.getText().length() > 0)
			sql.append(" AND UPPER(i.Description) LIKE ?");
		if (fPOReference.getText().length() > 0)
			sql.append(" AND UPPER(i.POReference) LIKE ?");
		//
		if (fBPartner_ID.getValue() != null)
			sql.append(" AND i.C_BPartner_ID=?");
		//
		if (fDateFrom.getValue() != null || fDateTo.getValue() != null)
		{
			Timestamp from = (Timestamp)fDateFrom.getValue();
			Timestamp to = (Timestamp)fDateTo.getValue();
			if (from == null && to != null)
				sql.append(" AND TRUNC(i.MovementDate,'DD') <= ?");
			else if (from != null && to == null)
				sql.append(" AND TRUNC(i.MovementDate,'DD') >= ?");
			else if (from != null && to != null)
				sql.append(" AND TRUNC(i.MovementDate,'DD') BETWEEN ? AND ?");
		}
		sql.append(" AND i.IsSOTrx=?");

	//	log.fine( "InfoInOut.setWhereClause", sql.toString());
		return sql.toString();
	}	//	getSQLWhere

	/**
	 *  Set Parameters for Query.
	 *  (as defined in getSQLWhere)
	 *  @param pstmt statement
	 *  @param forCount for counting records
	 *  @throws SQLException
	 */
	@Override
	void setParameters(PreparedStatement pstmt, boolean forCount) throws SQLException
	{
		int index = 1;
		if (fDocumentNo.getText().length() > 0)
			pstmt.setString(index++, getSQLText(fDocumentNo));
		if (fDescription.getText().length() > 0)
			pstmt.setString(index++, getSQLText(fDescription));
		if (fPOReference.getText().length() > 0)
			pstmt.setString(index++, getSQLText(fPOReference));
		//
		if (fBPartner_ID.getValue() != null)
		{
			Integer bp = (Integer)fBPartner_ID.getValue();
			pstmt.setInt(index++, bp.intValue());
			log.fine("BPartner=" + bp);
		}
		//
		if (fDateFrom.getValue() != null || fDateTo.getValue() != null)
		{
			Timestamp from = (Timestamp)fDateFrom.getValue();
			Timestamp to = (Timestamp)fDateTo.getValue();
			log.fine("Date From=" + from + ", To=" + to);
			if (from == null && to != null)
				pstmt.setTimestamp(index++, to);
			else if (from != null && to == null)
				pstmt.setTimestamp(index++, from);
			else if (from != null && to != null)
			{
				pstmt.setTimestamp(index++, from);
				pstmt.setTimestamp(index++, to);
			}
		}
		pstmt.setString(index++, fIsSOTrx.isSelected() ? "Y" : "N");
	}   //  setParameters

	/**
	 *  Get SQL WHERE parameter
	 *  @param f field
	 *  @return sql part
	 */
	private String getSQLText (CTextField f)
	{
		String s = f.getText().toUpperCase();
		if (!s.endsWith("%"))
			s += "%";
		log.fine( "String=" + s);
		return s;
	}   //  getSQLText

	private int getAD_Window_ID(String tableName, Query query) 
	{
		return (ZoomTarget.getZoomAD_Window_ID(tableName, 0, query.getWhereClause(), true));
	}

	/**
	 *	Zoom
	 */
	@Override
	void zoom()
	{
		log.info( "InfoInOut.zoom");
		Integer M_InOut_ID = getSelectedRowKey();
		if (M_InOut_ID == null)
			return;
		Query query = new Query("M_InOut");
		query.addRestriction("M_InOut_ID", Query.EQUAL, M_InOut_ID);
		query.setRecordCount(1);
		int AD_WindowNo = getAD_Window_ID("M_InOut", query);
		zoom (AD_WindowNo, query);
	}	//	zoom

	/**
	 *	Has Zoom
	 *  @return true
	 */
	@Override
	boolean hasZoom()
	{
		return true;
	}	//	hasZoom

}   //  InfoInOut
