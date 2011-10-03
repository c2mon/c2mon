
/**
 * BrowseResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package org.opcfoundation.xmlda;
            

            /**
            *  BrowseResponse bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class BrowseResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://opcfoundation.org/webservices/XMLDA/1.0/",
                "BrowseResponse",
                "ns1");

            

                        /**
                        * field for BrowseResult
                        */

                        
                                    protected org.opcfoundation.xmlda.ReplyBase localBrowseResult ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localBrowseResultTracker = false ;

                           public boolean isBrowseResultSpecified(){
                               return localBrowseResultTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.opcfoundation.webservices.xmlda._1_0.ReplyBase
                           */
                           public  org.opcfoundation.xmlda.ReplyBase getBrowseResult(){
                               return localBrowseResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param BrowseResult
                               */
                               public void setBrowseResult(org.opcfoundation.xmlda.ReplyBase param){
                            localBrowseResultTracker = param != null;
                                   
                                            this.localBrowseResult=param;
                                    

                               }
                            

                        /**
                        * field for Elements
                        * This was an Array!
                        */

                        
                                    protected org.opcfoundation.xmlda.BrowseElement[] localElements ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localElementsTracker = false ;

                           public boolean isElementsSpecified(){
                               return localElementsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.opcfoundation.webservices.xmlda._1_0.BrowseElement[]
                           */
                           public  org.opcfoundation.xmlda.BrowseElement[] getElements(){
                               return localElements;
                           }

                           
                        


                               
                              /**
                               * validate the array for Elements
                               */
                              protected void validateElements(org.opcfoundation.xmlda.BrowseElement[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Elements
                              */
                              public void setElements(org.opcfoundation.xmlda.BrowseElement[] param){
                              
                                   validateElements(param);

                               localElementsTracker = param != null;
                                      
                                      this.localElements=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.opcfoundation.webservices.xmlda._1_0.BrowseElement
                             */
                             public void addElements(org.opcfoundation.xmlda.BrowseElement param){
                                   if (localElements == null){
                                   localElements = new org.opcfoundation.xmlda.BrowseElement[]{};
                                   }

                            
                                 //update the setting tracker
                                localElementsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localElements);
                               list.add(param);
                               this.localElements =
                             (org.opcfoundation.xmlda.BrowseElement[])list.toArray(
                            new org.opcfoundation.xmlda.BrowseElement[list.size()]);

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
                        * field for ContinuationPoint
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localContinuationPoint ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getContinuationPoint(){
                               return localContinuationPoint;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ContinuationPoint
                               */
                               public void setContinuationPoint(java.lang.String param){
                            
                                            this.localContinuationPoint=param;
                                    

                               }
                            

                        /**
                        * field for MoreElements
                        * This was an Attribute!
                        */

                        
                                    protected boolean localMoreElements ;
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getMoreElements(){
                               return localMoreElements;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MoreElements
                               */
                               public void setMoreElements(boolean param){
                            
                                            this.localMoreElements=param;
                                    

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
                           namespacePrefix+":BrowseResponse",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "BrowseResponse",
                           xmlWriter);
                   }

               
                   }
               
                                            if (localContinuationPoint != null){
                                        
                                                writeAttribute("",
                                                         "ContinuationPoint",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localContinuationPoint), xmlWriter);

                                            
                                      }
                                    
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "MoreElements",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMoreElements), xmlWriter);

                                            
                                      }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localMoreElements is null");
                                      }
                                     if (localBrowseResultTracker){
                                            if (localBrowseResult==null){
                                                 throw new org.apache.axis2.databinding.ADBException("BrowseResult cannot be null!!");
                                            }
                                           localBrowseResult.serialize(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","BrowseResult"),
                                               xmlWriter);
                                        } if (localElementsTracker){
                                       if (localElements!=null){
                                            for (int i = 0;i < localElements.length;i++){
                                                if (localElements[i] != null){
                                                 localElements[i].serialize(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Elements"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                        // we don't have to do any thing since minOccures is zero
                                                    
                                                }

                                            }
                                     } else {
                                        
                                               throw new org.apache.axis2.databinding.ADBException("Elements cannot be null!!");
                                        
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

                 if (localBrowseResultTracker){
                            elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                      "BrowseResult"));
                            
                            
                                    if (localBrowseResult==null){
                                         throw new org.apache.axis2.databinding.ADBException("BrowseResult cannot be null!!");
                                    }
                                    elementList.add(localBrowseResult);
                                } if (localElementsTracker){
                             if (localElements!=null) {
                                 for (int i = 0;i < localElements.length;i++){

                                    if (localElements[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                          "Elements"));
                                         elementList.add(localElements[i]);
                                    } else {
                                        
                                                // nothing to do
                                            
                                    }

                                 }
                             } else {
                                 
                                        throw new org.apache.axis2.databinding.ADBException("Elements cannot be null!!");
                                    
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
                            new javax.xml.namespace.QName("","ContinuationPoint"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localContinuationPoint));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","MoreElements"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMoreElements));
                                

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
        public static BrowseResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            BrowseResponse object =
                new BrowseResponse();

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
                    
                            if (!"BrowseResponse".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (BrowseResponse)org.opcfoundation.xmlda.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    // handle attribute "ContinuationPoint"
                    java.lang.String tempAttribContinuationPoint =
                        
                                reader.getAttributeValue(null,"ContinuationPoint");
                            
                   if (tempAttribContinuationPoint!=null){
                         java.lang.String content = tempAttribContinuationPoint;
                        
                                                 object.setContinuationPoint(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribContinuationPoint));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ContinuationPoint");
                    
                    // handle attribute "MoreElements"
                    java.lang.String tempAttribMoreElements =
                        
                                reader.getAttributeValue(null,"MoreElements");
                            
                   if (tempAttribMoreElements!=null){
                         java.lang.String content = tempAttribMoreElements;
                        
                                                 object.setMoreElements(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribMoreElements));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute MoreElements is missing");
                           
                    }
                    handledAttributes.add("MoreElements");
                    
                    
                    reader.next();
                
                        java.util.ArrayList list2 = new java.util.ArrayList();
                    
                        java.util.ArrayList list3 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","BrowseResult").equals(reader.getName())){
                                
                                                object.setBrowseResult(org.opcfoundation.xmlda.ReplyBase.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Elements").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list2.add(org.opcfoundation.xmlda.BrowseElement.Factory.parse(reader));
                                                                
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone2 = false;
                                                        while(!loopDone2){
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
                                                                loopDone2 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Elements").equals(reader.getName())){
                                                                    list2.add(org.opcfoundation.xmlda.BrowseElement.Factory.parse(reader));
                                                                        
                                                                }else{
                                                                    loopDone2 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setElements((org.opcfoundation.xmlda.BrowseElement[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.opcfoundation.xmlda.BrowseElement.class,
                                                                list2));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Errors").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list3.add(org.opcfoundation.xmlda.OPCError.Factory.parse(reader));
                                                                
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
                                                                if (new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Errors").equals(reader.getName())){
                                                                    list3.add(org.opcfoundation.xmlda.OPCError.Factory.parse(reader));
                                                                        
                                                                }else{
                                                                    loopDone3 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setErrors((org.opcfoundation.xmlda.OPCError[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.opcfoundation.xmlda.OPCError.class,
                                                                list3));
                                                            
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
           
    