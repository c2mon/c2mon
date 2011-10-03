
/**
 * SubscriptionPolledRefreshResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package org.opcfoundation.xmlda;
            

            /**
            *  SubscriptionPolledRefreshResponse bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class SubscriptionPolledRefreshResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://opcfoundation.org/webservices/XMLDA/1.0/",
                "SubscriptionPolledRefreshResponse",
                "ns1");

            

                        /**
                        * field for SubscriptionPolledRefreshResult
                        */

                        
                                    protected org.opcfoundation.xmlda.ReplyBase localSubscriptionPolledRefreshResult ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubscriptionPolledRefreshResultTracker = false ;

                           public boolean isSubscriptionPolledRefreshResultSpecified(){
                               return localSubscriptionPolledRefreshResultTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.opcfoundation.webservices.xmlda._1_0.ReplyBase
                           */
                           public  org.opcfoundation.xmlda.ReplyBase getSubscriptionPolledRefreshResult(){
                               return localSubscriptionPolledRefreshResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubscriptionPolledRefreshResult
                               */
                               public void setSubscriptionPolledRefreshResult(org.opcfoundation.xmlda.ReplyBase param){
                            localSubscriptionPolledRefreshResultTracker = param != null;
                                   
                                            this.localSubscriptionPolledRefreshResult=param;
                                    

                               }
                            

                        /**
                        * field for InvalidServerSubHandles
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localInvalidServerSubHandles ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localInvalidServerSubHandlesTracker = false ;

                           public boolean isInvalidServerSubHandlesSpecified(){
                               return localInvalidServerSubHandlesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getInvalidServerSubHandles(){
                               return localInvalidServerSubHandles;
                           }

                           
                        


                               
                              /**
                               * validate the array for InvalidServerSubHandles
                               */
                              protected void validateInvalidServerSubHandles(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param InvalidServerSubHandles
                              */
                              public void setInvalidServerSubHandles(java.lang.String[] param){
                              
                                   validateInvalidServerSubHandles(param);

                               localInvalidServerSubHandlesTracker = param != null;
                                      
                                      this.localInvalidServerSubHandles=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addInvalidServerSubHandles(java.lang.String param){
                                   if (localInvalidServerSubHandles == null){
                                   localInvalidServerSubHandles = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localInvalidServerSubHandlesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localInvalidServerSubHandles);
                               list.add(param);
                               this.localInvalidServerSubHandles =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for RItemList
                        * This was an Array!
                        */

                        
                                    protected org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList[] localRItemList ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRItemListTracker = false ;

                           public boolean isRItemListSpecified(){
                               return localRItemListTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.opcfoundation.webservices.xmlda._1_0.SubscribePolledRefreshReplyItemList[]
                           */
                           public  org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList[] getRItemList(){
                               return localRItemList;
                           }

                           
                        


                               
                              /**
                               * validate the array for RItemList
                               */
                              protected void validateRItemList(org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param RItemList
                              */
                              public void setRItemList(org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList[] param){
                              
                                   validateRItemList(param);

                               localRItemListTracker = param != null;
                                      
                                      this.localRItemList=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.opcfoundation.webservices.xmlda._1_0.SubscribePolledRefreshReplyItemList
                             */
                             public void addRItemList(org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList param){
                                   if (localRItemList == null){
                                   localRItemList = new org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList[]{};
                                   }

                            
                                 //update the setting tracker
                                localRItemListTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localRItemList);
                               list.add(param);
                               this.localRItemList =
                             (org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList[])list.toArray(
                            new org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList[list.size()]);

                             }
                             

                        /**
                        * field for Errors
                        * This was an Array!
                        */

                        
                                    protected org.opcfoundation.xmlda.OPCError[] localErrors ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localErrorsTracker = false ;

                           public boolean isErrorsSpecified(){
                               return localErrorsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.opcfoundation.webservices.xmlda._1_0.OPCError[]
                           */
                           public  org.opcfoundation.xmlda.OPCError[] getErrors(){
                               return localErrors;
                           }

                           
                        


                               
                              /**
                               * validate the array for Errors
                               */
                              protected void validateErrors(org.opcfoundation.xmlda.OPCError[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Errors
                              */
                              public void setErrors(org.opcfoundation.xmlda.OPCError[] param){
                              
                                   validateErrors(param);

                               localErrorsTracker = param != null;
                                      
                                      this.localErrors=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.opcfoundation.webservices.xmlda._1_0.OPCError
                             */
                             public void addErrors(org.opcfoundation.xmlda.OPCError param){
                                   if (localErrors == null){
                                   localErrors = new org.opcfoundation.xmlda.OPCError[]{};
                                   }

                            
                                 //update the setting tracker
                                localErrorsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localErrors);
                               list.add(param);
                               this.localErrors =
                             (org.opcfoundation.xmlda.OPCError[])list.toArray(
                            new org.opcfoundation.xmlda.OPCError[list.size()]);

                             }
                             

                        /**
                        * field for DataBufferOverflow
                        * This was an Attribute!
                        */

                        
                                    protected boolean localDataBufferOverflow ;
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getDataBufferOverflow(){
                               return localDataBufferOverflow;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DataBufferOverflow
                               */
                               public void setDataBufferOverflow(boolean param){
                            
                                            this.localDataBufferOverflow=param;
                                    

                               }
                            

     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{


        
               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
               return factory.createOMElement(dataSource,MY_QNAME);
            
        }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       javax.xml.stream.XMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               javax.xml.stream.XMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();
                    writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://opcfoundation.org/webservices/XMLDA/1.0/");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":SubscriptionPolledRefreshResponse",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "SubscriptionPolledRefreshResponse",
                           xmlWriter);
                   }

               
                   }
               
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "DataBufferOverflow",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDataBufferOverflow), xmlWriter);

                                            
                                      }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localDataBufferOverflow is null");
                                      }
                                     if (localSubscriptionPolledRefreshResultTracker){
                                            if (localSubscriptionPolledRefreshResult==null){
                                                 throw new org.apache.axis2.databinding.ADBException("SubscriptionPolledRefreshResult cannot be null!!");
                                            }
                                           localSubscriptionPolledRefreshResult.serialize(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","SubscriptionPolledRefreshResult"),
                                               xmlWriter);
                                        } if (localInvalidServerSubHandlesTracker){
                             if (localInvalidServerSubHandles!=null) {
                                   namespace = "http://opcfoundation.org/webservices/XMLDA/1.0/";
                                   for (int i = 0;i < localInvalidServerSubHandles.length;i++){
                                        
                                            if (localInvalidServerSubHandles[i] != null){
                                        
                                                writeStartElement(null, namespace, "InvalidServerSubHandles", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localInvalidServerSubHandles[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // we have to do nothing since minOccurs is zero
                                                       
                                                }

                                   }
                             } else {
                                 
                                         throw new org.apache.axis2.databinding.ADBException("InvalidServerSubHandles cannot be null!!");
                                    
                             }

                        } if (localRItemListTracker){
                                       if (localRItemList!=null){
                                            for (int i = 0;i < localRItemList.length;i++){
                                                if (localRItemList[i] != null){
                                                 localRItemList[i].serialize(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","RItemList"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                        // we don't have to do any thing since minOccures is zero
                                                    
                                                }

                                            }
                                     } else {
                                        
                                               throw new org.apache.axis2.databinding.ADBException("RItemList cannot be null!!");
                                        
                                    }
                                 } if (localErrorsTracker){
                                       if (localErrors!=null){
                                            for (int i = 0;i < localErrors.length;i++){
                                                if (localErrors[i] != null){
                                                 localErrors[i].serialize(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Errors"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                        // we don't have to do any thing since minOccures is zero
                                                    
                                                }

                                            }
                                     } else {
                                        
                                               throw new org.apache.axis2.databinding.ADBException("Errors cannot be null!!");
                                        
                                    }
                                 }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://opcfoundation.org/webservices/XMLDA/1.0/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Utility method to write an element start tag.
         */
        private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                       javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
            if (writerPrefix != null) {
                xmlWriter.writeStartElement(namespace, localPart);
            } else {
                if (namespace.length() == 0) {
                    prefix = "";
                } else if (prefix == null) {
                    prefix = generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, localPart, namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
        }
        
        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace,attName,attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName,attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace,attName,attValue);
            }
        }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                while (true) {
                    java.lang.String uri = nsContext.getNamespaceURI(prefix);
                    if (uri == null || uri.length() == 0) {
                        break;
                    }
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }


  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localSubscriptionPolledRefreshResultTracker){
                            elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                      "SubscriptionPolledRefreshResult"));
                            
                            
                                    if (localSubscriptionPolledRefreshResult==null){
                                         throw new org.apache.axis2.databinding.ADBException("SubscriptionPolledRefreshResult cannot be null!!");
                                    }
                                    elementList.add(localSubscriptionPolledRefreshResult);
                                } if (localInvalidServerSubHandlesTracker){
                            if (localInvalidServerSubHandles!=null){
                                  for (int i = 0;i < localInvalidServerSubHandles.length;i++){
                                      
                                         if (localInvalidServerSubHandles[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                              "InvalidServerSubHandles"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localInvalidServerSubHandles[i]));
                                          } else {
                                             
                                                    // have to do nothing
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    throw new org.apache.axis2.databinding.ADBException("InvalidServerSubHandles cannot be null!!");
                                
                            }

                        } if (localRItemListTracker){
                             if (localRItemList!=null) {
                                 for (int i = 0;i < localRItemList.length;i++){

                                    if (localRItemList[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                          "RItemList"));
                                         elementList.add(localRItemList[i]);
                                    } else {
                                        
                                                // nothing to do
                                            
                                    }

                                 }
                             } else {
                                 
                                        throw new org.apache.axis2.databinding.ADBException("RItemList cannot be null!!");
                                    
                             }

                        } if (localErrorsTracker){
                             if (localErrors!=null) {
                                 for (int i = 0;i < localErrors.length;i++){

                                    if (localErrors[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                          "Errors"));
                                         elementList.add(localErrors[i]);
                                    } else {
                                        
                                                // nothing to do
                                            
                                    }

                                 }
                             } else {
                                 
                                        throw new org.apache.axis2.databinding.ADBException("Errors cannot be null!!");
                                    
                             }

                        }
                            attribList.add(
                            new javax.xml.namespace.QName("","DataBufferOverflow"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDataBufferOverflow));
                                

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        
        

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static SubscriptionPolledRefreshResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            SubscriptionPolledRefreshResponse object =
                new SubscriptionPolledRefreshResponse();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    
                            if (!"SubscriptionPolledRefreshResponse".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (SubscriptionPolledRefreshResponse)org.opcfoundation.xmlda.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    // handle attribute "DataBufferOverflow"
                    java.lang.String tempAttribDataBufferOverflow =
                        
                                reader.getAttributeValue(null,"DataBufferOverflow");
                            
                   if (tempAttribDataBufferOverflow!=null){
                         java.lang.String content = tempAttribDataBufferOverflow;
                        
                                                 object.setDataBufferOverflow(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribDataBufferOverflow));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute DataBufferOverflow is missing");
                           
                    }
                    handledAttributes.add("DataBufferOverflow");
                    
                    
                    reader.next();
                
                        java.util.ArrayList list2 = new java.util.ArrayList();
                    
                        java.util.ArrayList list3 = new java.util.ArrayList();
                    
                        java.util.ArrayList list4 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","SubscriptionPolledRefreshResult").equals(reader.getName())){
                                
                                                object.setSubscriptionPolledRefreshResult(org.opcfoundation.xmlda.ReplyBase.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","InvalidServerSubHandles").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list2.add(reader.getElementText());
                                            
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone2 = false;
                                            while(!loopDone2){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone2 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","InvalidServerSubHandles").equals(reader.getName())){
                                                         list2.add(reader.getElementText());
                                                        
                                                    }else{
                                                        loopDone2 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setInvalidServerSubHandles((java.lang.String[])
                                                        list2.toArray(new java.lang.String[list2.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","RItemList").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list3.add(org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList.Factory.parse(reader));
                                                                
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone3 = false;
                                                        while(!loopDone3){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone3 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","RItemList").equals(reader.getName())){
                                                                    list3.add(org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList.Factory.parse(reader));
                                                                        
                                                                }else{
                                                                    loopDone3 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setRItemList((org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList.class,
                                                                list3));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Errors").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list4.add(org.opcfoundation.xmlda.OPCError.Factory.parse(reader));
                                                                
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone4 = false;
                                                        while(!loopDone4){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone4 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Errors").equals(reader.getName())){
                                                                    list4.add(org.opcfoundation.xmlda.OPCError.Factory.parse(reader));
                                                                        
                                                                }else{
                                                                    loopDone4 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setErrors((org.opcfoundation.xmlda.OPCError[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.opcfoundation.xmlda.OPCError.class,
                                                                list4));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                  
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            
                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    