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
package org.compiere.process;


import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.util.*;


/**
 *  Generate Order from Project Phase
 *
 *	@author Jorg Janke
 *	@version $Id: ProjectPhaseGenOrder.java,v 1.2 2006/07/30 00:51:01 jjanke Exp $
 */
public class ProjectPhaseGenOrder  extends SvrProcess
{
	private int		m_C_ProjectPhase_ID = 0;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) {
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message (clear text)
	 *  @throws Exception if not successful
	 */
	@Override
	protected String doIt() throws Exception
	{
		m_C_ProjectPhase_ID = getRecord_ID();
		log.info("doIt - C_ProjectPhase_ID=" + m_C_ProjectPhase_ID);
		if (m_C_ProjectPhase_ID == 0)
			throw new IllegalArgumentException("C_ProjectPhase_ID == 0");
		MProjectPhase fromPhase = new MProjectPhase (getCtx(), m_C_ProjectPhase_ID, get_TrxName());
		MProject fromProject = ProjectGenOrder.getProject (getCtx(), fromPhase.getC_Project_ID(), get_TrxName());
		MOrder order = new MOrder (fromProject, true, MOrder.DocSubTypeSO_OnCredit);
		order.setDescription(order.getDescription() + " - " + fromPhase.getName());
		if (!order.save())
			throw new Exception("Could not create Order");

		//	Create an order on Phase Level
		if (fromPhase.getM_Product_ID() != 0)
		{
			MOrderLine ol = new MOrderLine(order);
			ol.setLine(fromPhase.getSeqNo());
			StringBuffer sb = new StringBuffer (fromPhase.getName());
			if (fromPhase.getDescription() != null && fromPhase.getDescription().length() > 0)
				sb.append(" - ").append(fromPhase.getDescription());
			ol.setDescription(sb.toString());
			//
			ol.setM_Product_ID(fromPhase.getM_Product_ID(), true);
			ol.setQty(fromPhase.getQty());
			ol.setPrice();
			if (fromPhase.getPriceActual() != null && fromPhase.getPriceActual().compareTo(Env.ZERO) != 0)
				ol.setPrice(fromPhase.getPriceActual());
			ol.setTax();
			if (!ol.save())
				log.log(Level.SEVERE, "doIt - Lines not generated");
			return "@C_Order_ID@ " + order.getDocumentNo() + " (1)";
		}

		//	Project Tasks
		int count = 0;
		MProjectTask[] tasks = fromPhase.getTasks ();
		for (MProjectTask element : tasks) {
			MOrderLine ol = new MOrderLine(order);
			ol.setLine(element.getSeqNo());
			StringBuffer sb = new StringBuffer (element.getName());
			if (element.getDescription() != null && element.getDescription().length() > 0)
				sb.append(" - ").append(element.getDescription());
			ol.setDescription(sb.toString());
			//
			ol.setM_Product_ID(element.getM_Product_ID(), true);
			ol.setQty(element.getQty());
			ol.setPrice();
			ol.setTax();
			if (ol.save())
				count++;
		}	//	for all lines
		if (tasks.length != count)
			log.log(Level.SEVERE, "doIt - Lines difference - ProjectTasks=" + tasks.length + " <> Saved=" + count);

		return "@C_Order_ID@ " + order.getDocumentNo() + " (" + count + ")";
	}	//	doIt

}	//	ProjectPhaseGenOrder
