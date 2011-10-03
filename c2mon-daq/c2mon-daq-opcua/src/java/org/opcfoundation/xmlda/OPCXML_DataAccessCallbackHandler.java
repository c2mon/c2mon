
/**
 * OPCXML_DataAccessCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:22:40 CEST)
 */

    package org.opcfoundation.xmlda;

    /**
     *  OPCXML_DataAccessCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class OPCXML_DataAccessCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public OPCXML_DataAccessCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public OPCXML_DataAccessCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for read method
            * override this method for handling normal response from read operation
            */
           public void receiveResultread(
                    org.opcfoundation.xmlda.ReadResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from read operation
           */
            public void receiveErrorread(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for write method
            * override this method for handling normal response from write operation
            */
           public void receiveResultwrite(
                    WriteResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from write operation
           */
            public void receiveErrorwrite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for subscribe method
            * override this method for handling normal response from subscribe operation
            */
           public void receiveResultsubscribe(
                    org.opcfoundation.xmlda.SubscribeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from subscribe operation
           */
            public void receiveErrorsubscribe(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for subscriptionPolledRefresh method
            * override this method for handling normal response from subscriptionPolledRefresh operation
            */
           public void receiveResultsubscriptionPolledRefresh(
                    org.opcfoundation.xmlda.SubscriptionPolledRefreshResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from subscriptionPolledRefresh operation
           */
            public void receiveErrorsubscriptionPolledRefresh(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getStatus method
            * override this method for handling normal response from getStatus operation
            */
           public void receiveResultgetStatus(
                    org.opcfoundation.xmlda.GetStatusResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getStatus operation
           */
            public void receiveErrorgetStatus(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for subscriptionCancel method
            * override this method for handling normal response from subscriptionCancel operation
            */
           public void receiveResultsubscriptionCancel(
                    org.opcfoundation.xmlda.SubscriptionCancelResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from subscriptionCancel operation
           */
            public void receiveErrorsubscriptionCancel(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getProperties method
            * override this method for handling normal response from getProperties operation
            */
           public void receiveResultgetProperties(
                    org.opcfoundation.xmlda.GetPropertiesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getProperties operation
           */
            public void receiveErrorgetProperties(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for browse method
            * override this method for handling normal response from browse operation
            */
           public void receiveResultbrowse(
                    org.opcfoundation.xmlda.BrowseResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from browse operation
           */
            public void receiveErrorbrowse(java.lang.Exception e) {
            }
                


    }
    