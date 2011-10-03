
/**
 * SubscriptionPolledRefresh.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package org.opcfoundation.xmlda;
            

            /**
            *  SubscriptionPolledRefresh bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class SubscriptionPolledRefresh
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://opcfoundation.org/webservices/XMLDA/1.0/",
                "SubscriptionPolledRefresh",
                "ns1");

            

                        /**
                        * field for Options
                        */

                        
                                    protected org.opcfoundation.xmlda.RequestOptions localOptions ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localOptionsTracker = false ;

                           public boolean isOptionsSpecified(){
                               return localOptionsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.opcfoundation.webservices.xmlda._1_0.RequestOptions
                           */
                           public  org.opcfoundation.xmlda.RequestOptions getOptions(){
                               return localOptions;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Options
                               */
                               public void setOptions(org.opcfoundation.xmlda.RequestOptions param){
                            localOptionsTracker = param != null;
                                   
                                            this.localOptions=param;
                                    

                               }
                            

                        /**
                        * field for ServerSubHandles
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localServerSubHandles ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localServerSubHandlesTracker = false ;

                           public boolean isServerSubHandlesSpecified(){
                               return localServerSubHandlesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getServerSubHandles(){
                               return localServerSubHandles;
                           }

                           
                        


                               
                              /**
                               * validate the array for ServerSubHandles
                               */
                              protected void validateServerSubHandles(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param ServerSubHandles
                              */
                              public void setServerSubHandles(java.lang.String[] param){
                              
                                   validateServerSubHandles(param);

                               localServerSubHandlesTracker = param != null;
                                      
                                      this.localServerSubHandles=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addServerSubHandles(java.lang.String param){
                                   if (localServerSubHandles == null){
                                   localServerSubHandles = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localServerSubHandlesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localServerSubHandles);
                               list.add(param);
                               this.localServerSubHandles =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for HoldTime
                        * This was an Attribute!
                        */

                        
                                    protected java.util.Calendar localHoldTime ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getHoldTime(){
                               return localHoldTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param HoldTime
                               */
                               public void setHoldTime(java.util.Calendar param){
                            
                                            this.localHoldTime=param;
                                    

                               }
                            

                        /**
                        * field for WaitTime
                        * This was an Attribute!
                        */

                        
                                    protected int localWaitTime ;
                                

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getWaitTime(){
                               return localWaitTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WaitTime
                               */
                               public void setWaitTime(int param){
                            
                                            this.localWaitTime=param;
                                    

                               }
                            

                        /**
                        * field for ReturnAllItems
                        * This was an Attribute!
                        */

                        
                                    protected boolean localReturnAllItems ;
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getReturnAllItems(){
                               return localReturnAllItems;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReturnAllItems
                               */
                               public void setReturnAllItems(boolean param){
                            
                                            this.localReturnAllItems=param;
                                    

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
                           namespacePrefix+":SubscriptionPolledRefresh",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "SubscriptionPolledRefresh",
                           xmlWriter);
                   }

               
                   }
               
                                            if (localHoldTime != null){
                                        
                                                writeAttribute("",
                                                         "HoldTime",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHoldTime), xmlWriter);

                                            
                                      }
                                    
                                                   if (localWaitTime!=java.lang.Integer.MIN_VALUE) {
                                               
                                                writeAttribute("",
                                                         "WaitTime",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWaitTime), xmlWriter);

                                            
                                      }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localWaitTime is null");
                                      }
                                    
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "ReturnAllItems",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnAllItems), xmlWriter);

                                            
                                      }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localReturnAllItems is null");
                                      }
                                     if (localOptionsTracker){
                                            if (localOptions==null){
                                                 throw new org.apache.axis2.databinding.ADBException("Options cannot be null!!");
                                            }
                                           localOptions.serialize(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Options"),
                                               xmlWriter);
                                        } if (localServerSubHandlesTracker){
                             if (localServerSubHandles!=null) {
                                   namespace = "http://opcfoundation.org/webservices/XMLDA/1.0/";
                                   for (int i = 0;i < localServerSubHandles.length;i++){
                                        
                                            if (localServerSubHandles[i] != null){
                                        
                                                writeStartElement(null, namespace, "ServerSubHandles", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localServerSubHandles[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // we have to do nothing since minOccurs is zero
                                                       
                                                }

                                   }
                             } else {
                                 
                                         throw new org.apache.axis2.databinding.ADBException("ServerSubHandles cannot be null!!");
                                    
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

                 if (localOptionsTracker){
                            elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                      "Options"));
                            
                            
                                    if (localOptions==null){
                                         throw new org.apache.axis2.databinding.ADBException("Options cannot be null!!");
                                    }
                                    elementList.add(localOptions);
                                } if (localServerSubHandlesTracker){
                            if (localServerSubHandles!=null){
                                  for (int i = 0;i < localServerSubHandles.length;i++){
                                      
                                         if (localServerSubHandles[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                              "ServerSubHandles"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localServerSubHandles[i]));
                                          } else {
                                             
                                                    // have to do nothing
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    throw new org.apache.axis2.databinding.ADBException("ServerSubHandles cannot be null!!");
                                
                            }

                        }
                            attribList.add(
                            new javax.xml.namespace.QName("","HoldTime"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localHoldTime));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","WaitTime"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWaitTime));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ReturnAllItems"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnAllItems));
                                

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
        public static SubscriptionPolledRefresh parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            SubscriptionPolledRefresh object =
                new SubscriptionPolledRefresh();

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
                    
                            if (!"SubscriptionPolledRefresh".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (SubscriptionPolledRefresh)org.opcfoundation.xmlda.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    // handle attribute "HoldTime"
                    java.lang.String tempAttribHoldTime =
                        
                                reader.getAttributeValue(null,"HoldTime");
                            
                   if (tempAttribHoldTime!=null){
                         java.lang.String content = tempAttribHoldTime;
                        
                                                 object.setHoldTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(tempAttribHoldTime));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("HoldTime");
                    
                    // handle attribute "WaitTime"
                    java.lang.String tempAttribWaitTime =
                        
                                reader.getAttributeValue(null,"WaitTime");
                            
                   if (tempAttribWaitTime!=null){
                         java.lang.String content = tempAttribWaitTime;
                        
                                                 object.setWaitTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(tempAttribWaitTime));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute WaitTime is missing");
                           
                    }
                    handledAttributes.add("WaitTime");
                    
                    // handle attribute "ReturnAllItems"
                    java.lang.String tempAttribReturnAllItems =
                        
                                reader.getAttributeValue(null,"ReturnAllItems");
                            
                   if (tempAttribReturnAllItems!=null){
                         java.lang.String content = tempAttribReturnAllItems;
                        
                                                 object.setReturnAllItems(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribReturnAllItems));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute ReturnAllItems is missing");
                           
                    }
                    handledAttributes.add("ReturnAllItems");
                    
                    
                    reader.next();
                
                        java.util.ArrayList list2 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Options").equals(reader.getName())){
                                
                                                object.setOptions(org.opcfoundation.xmlda.RequestOptions.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","ServerSubHandles").equals(reader.getName())){
                                
                                    
                                    
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
                                                    if (new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","ServerSubHandles").equals(reader.getName())){
                                                         list2.add(reader.getElementText());
                                                        
                                                    }else{
                                                        loopDone2 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setServerSubHandles((java.lang.String[])
                                                        list2.toArray(new java.lang.String[list2.size()]));
                                                
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
           
    