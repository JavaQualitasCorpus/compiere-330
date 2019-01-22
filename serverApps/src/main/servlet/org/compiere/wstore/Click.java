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
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *	Click Counter.
 * 	Counts the click and forwards.
 * 	<code>
	http://www.compiere.com/wstore/click?target=www.yahoo.com
	http://www.compiere.com/wstore/click/www.yahoo.com
	http://www.compiere.com/wstore/click?www.yahoo.com
 *  </code>
 *
 *  @author Jorg Janke
 *  @version $Id: Click.java,v 1.2 2006/07/30 00:53:21 jjanke Exp $
 */
public class Click  extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**	Logging						*/
	private CLogger						log = CLogger.getCLogger(getClass());

	/** Name						*/
	static public final String			NAME = "click";

	/** Target Parameter			*/
	static public final String			PARA_TARGET = "target";
	/**	Fallback Target				*/
	static public final String			DEFAULT_TARGET = "http://www.compiere.org/";

	/**
	 *	Initialize global variables
	 *
	 *  @param config Configuration
	 *  @throws ServletException
	 */
	@Override
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("Click.init");
	}   //  init

	/**
	 * Get Servlet information
	 * @return Info
	 */
	@Override
	public String getServletInfo()
	{
		return "Compiere Click Servlet";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	@Override
	public void destroy()
	{
		log.fine("destroy");
	}   //  destroy

	
	/**************************************************************************
	 *  Process the HTTP Get request.
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		long time = System.currentTimeMillis();
		request.getSession(true);	//	force create session for ctx
		//
		String url = getTargetURL(request);
		if (!response.isCommitted ())
			response.sendRedirect(url);
		response.flushBuffer();
		log.fine("redirect - " + url);
		
		//	Save Click
		saveClick(request, url);
		//
		log.fine(url + " - " + (System.currentTimeMillis()-time) + "ms");
	}	//	doGet

	/**
	 *  Process the HTTP Post request
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
		doGet (request, response);
	}	//	doPost

	/**
	 * 	Get Target URL.
	 * 	1 - target parameter
	 *  3 - parameter
	 *  2 - path
	 *	@param request request
	 *	@return URL
	 */
	private String getTargetURL (HttpServletRequest request)
	{
		//	Get Named Parameter		-	/click?target=www...
		String url = WebUtil.getParameter(request, PARA_TARGET);
		//	Check parameters		-	/click?www...
		if (url == null || url.length() == 0)
		{
			Enumeration<?> e = request.getParameterNames ();
			if (e.hasMoreElements ())
				url = (String)e.nextElement ();
		}
		//	Check Path				-	/click/www...
		if (url == null || url.length() == 0)
		{
			url = request.getPathInfo ();
			if (url != null)
				url = url.substring(1);		//	cut off initial /
		}
		//	Still nothing
		if (url == null || url.length() == 0)
			url = DEFAULT_TARGET;
		//	add http protocol
		if (url.indexOf("://") == -1)
			url = "http://" + url;
		return url;
	}	//	getTargetURL

	/**
	 * 	Save Click
	 */
	private boolean saveClick (HttpServletRequest request, String url)
	{
		Ctx ctx = JSPEnv.getCtx(request);
		//
		MClick mc = new MClick (ctx, url, null);
		mc.setRemote_Addr(request.getRemoteAddr());
		mc.setRemote_Host(request.getRemoteHost());
		String ref = request.getHeader("referer");
		if (ref == null || ref.length() == 0)
			ref = request.getRequestURL().toString();
		mc.setReferrer(ref);
		//
		mc.setAcceptLanguage(request.getHeader("accept-language"));
		mc.setUserAgent(request.getHeader("user-agent"));
		//
		HttpSession session = request.getSession(false);
		if (session != null)
		{
			WebUser wu = (WebUser)session.getAttribute (WebUser.NAME);
			if (wu != null)
			{
				mc.setEMail (wu.getEmail());
				mc.setAD_User_ID (wu.getAD_User_ID());
			}
		}
		return mc.save();
	}	//	saveClick

}	//	Click
