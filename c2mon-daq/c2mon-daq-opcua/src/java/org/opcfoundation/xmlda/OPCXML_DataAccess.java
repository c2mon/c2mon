

/**
 * OPCXML_DataAccess.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:22:40 CEST)
 */

    package org.opcfoundation.xmlda;

    /*
     *  OPCXML_DataAccess java interface
     */

    public interface OPCXML_DataAccess {
          

        /**
          * Auto generated method signature
          * 
                    * @param read0
                
         */

         
                     public org.opcfoundation.xmlda.ReadResponse read(

                        org.opcfoundation.xmlda.Read read0)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param read0
            
          */
        public void startread(

            org.opcfoundation.xmlda.Read read0,

            final org.opcfoundation.xmlda.OPCXML_DataAccessCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param write2
                
         */

         
                     public WriteResponse write(

                        org.opcfoundation.xmlda.Write write2)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param write2
            
          */
        public void startwrite(

            org.opcfoundation.xmlda.Write write2,

            final org.opcfoundation.xmlda.OPCXML_DataAccessCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param subscribe4
                
         */

         
                     public org.opcfoundation.xmlda.SubscribeResponse subscribe(

                        org.opcfoundation.xmlda.Subscribe subscribe4)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param subscribe4
            
          */
        public void startsubscribe(

            org.opcfoundation.xmlda.Subscribe subscribe4,

            final org.opcfoundation.xmlda.OPCXML_DataAccessCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param subscriptionPolledRefresh6
                
         */

         
                     public org.opcfoundation.xmlda.SubscriptionPolledRefreshResponse subscriptionPolledRefresh(

                        org.opcfoundation.xmlda.SubscriptionPolledRefresh subscriptionPolledRefresh6)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param subscriptionPolledRefresh6
            
          */
        public void startsubscriptionPolledRefresh(

            org.opcfoundation.xmlda.SubscriptionPolledRefresh subscriptionPolledRefresh6,

            final org.opcfoundation.xmlda.OPCXML_DataAccessCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getStatus8
                
         */

         
                     public org.opcfoundation.xmlda.GetStatusResponse getStatus(

                        org.opcfoundation.xmlda.GetStatus getStatus8)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getStatus8
            
          */
        public void startgetStatus(

            org.opcfoundation.xmlda.GetStatus getStatus8,

            final org.opcfoundation.xmlda.OPCXML_DataAccessCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param subscriptionCancel10
                
         */

         
                     public org.opcfoundation.xmlda.SubscriptionCancelResponse subscriptionCancel(

                        org.opcfoundation.xmlda.SubscriptionCancel subscriptionCancel10)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param subscriptionCancel10
            
          */
        public void startsubscriptionCancel(

            org.opcfoundation.xmlda.SubscriptionCancel subscriptionCancel10,

            final org.opcfoundation.xmlda.OPCXML_DataAccessCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getProperties12
                
         */

         
                     public org.opcfoundation.xmlda.GetPropertiesResponse getProperties(

                        org.opcfoundation.xmlda.GetProperties getProperties12)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getProperties12
            
          */
        public void startgetProperties(

            org.opcfoundation.xmlda.GetProperties getProperties12,

            final org.opcfoundation.xmlda.OPCXML_DataAccessCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param browse14
                
         */

         
                     public org.opcfoundation.xmlda.BrowseResponse browse(

                        org.opcfoundation.xmlda.Browse browse14)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param browse14
            
          */
        public void startbrowse(

            org.opcfoundation.xmlda.Browse browse14,

            final org.opcfoundation.xmlda.OPCXML_DataAccessCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    