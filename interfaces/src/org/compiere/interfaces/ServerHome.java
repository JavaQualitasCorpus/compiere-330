/*
 * Generated by XDoclet - Do not edit!
 */
package org.compiere.interfaces;

/**
 * Home interface for compiere/Server.
 */
public interface ServerHome
   extends javax.ejb.EJBHome
{
   public static final String COMP_NAME="java:comp/env/ejb/compiere/Server";
   public static final String JNDI_NAME="compiere/Server";

   public org.compiere.interfaces.Server create()
      throws javax.ejb.CreateException,java.rmi.RemoteException;

}
