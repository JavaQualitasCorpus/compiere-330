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
package org.compiere.wstore;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.compiere.model.*;
import org.compiere.util.*;


/**
 * 	Location Servlet
 *	
 *  @author Jorg Janke
 *  @version $Id: LocationServlet.java,v 1.5 2006/07/30 00:53:21 jjanke Exp $
 */
public class LocationServlet extends HttpServlet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**	Logging						*/
    private CLogger			log = CLogger.getCLogger(getClass());

    /**
     * 	Initialize global variables
     *  @param config servlet configuration
     *  @throws javax.servlet.ServletException
     */
    @Override
	public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        if (!WebEnv.initWeb(config))
            throw new ServletException("LocationServlet.init");
    }	//	init

    /**
     * Get Servlet information
     * @return Info
     */
    @Override
	public String getServletInfo()
    {
        return "Compiere Location Servlet";
    }	//	getServletInfo

    /**
     * Clean up resources
     */
    @Override
	public void destroy()
    {
        log.info("destroy");
    }   //  destroy

    /**
     *  Process the initial HTTP Get request.
     *  Reads the Parameter Amt and optional C_Invoice_ID
     *
     *  @param request request
     *  @param response response
     *  @throws ServletException
     *  @throws java.io.IOException
     */
    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        log.info("From " + request.getRemoteHost() + " - " + request.getRemoteAddr());
        doPost(request, response);
    }   //  doGet

    /**
     *  Process the HTTP Post request.
     *
     *  @param request request
     *  @param response response
     *  @throws ServletException
     *  @throws IOException
     */
    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        log.info("From " + request.getRemoteHost() + " - " + request.getRemoteAddr());
        request.getSession(true);
        Ctx ctx = JSPEnv.getCtx(request);
        MLocation loc = new MLocation (ctx, 0, null);
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/xml; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();


        String cmd = request.getParameter("cmd");
        if(cmd == null)
        {
            out.println("<error>Unknown Request: NULL</error>");
        }else{
            String selected = request.getParameter("selected");
            int selectedID = 0;
            try{
                selectedID = Integer.parseInt(selected);
            }catch(Exception e){
                selectedID = 0;
            }

            if(cmd.equalsIgnoreCase("countries"))// should probably put these in enum
            {
                out.println("<countries>");
                MCountry[] countries = MCountry.getCountries(loc.getCtx());
                for(MCountry country : countries)
                {
                    int id = country.getC_Country_ID();
                    out.print("<country id='"+id+"'");
                    if(id == selectedID) out.print(" selected='true'");
                    out.println(">"+country.getName()+"</country>");
                }
                out.println("</countries>");
            }else if(cmd.equalsIgnoreCase("regions")){
                String country = request.getParameter("country");
                try{
                    int countryId = Integer.parseInt(country);

                    out.println("<regions country='" + countryId + "'>");
                    MRegion[] regions = MRegion.getRegions(loc.getCtx(), countryId);
                    if((regions.length > 0) && (selectedID == 0))
                        selectedID = regions[0].getC_Region_ID();

                    for(MRegion region : regions)
                    {
                        int id = region.getC_Region_ID();
                        out.print("<region id='"+id+"'");
                        if(id == selectedID) out.print(" selected='true'");
                        out.println(">"+region.getName()+"</region>");
                    }
                    out.println("</regions>");
                }catch(Exception e){
                    out.println("<error>Unknown Country: " + country + "</error>");
                }
            }else{
                out.println("<error>Unknown Request: "+cmd+"</error>");
            }
        }

        out.flush();
        out.close();
    }   //  doPost
}
