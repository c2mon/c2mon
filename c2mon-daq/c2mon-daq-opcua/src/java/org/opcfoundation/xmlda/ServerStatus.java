
/**
 * ServerStatus.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package org.opcfoundation.xmlda;
            

            /**
            *  ServerStatus bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ServerStatus
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = ServerStatus
                Namespace URI = http://opcfoundation.org/webservices/XMLDA/1.0/
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for StatusInfo
                        */

                        
                                    protected java.lang.String localStatusInfo ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStatusInfoTracker = false ;

                           public boolean isStatusInfoSpecified(){
                               return localStatusInfoTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getStatusInfo(){
                               return localStatusInfo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StatusInfo
                               */
                               public void setStatusInfo(java.lang.String param){
                            localStatusInfoTracker = param != null;
                                   
                                            this.localStatusInfo=param;
                                    

                               }
                            

                        /**
                        * field for VendorInfo
                        */

                        
                                    protected java.lang.String localVendorInfo ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localVendorInfoTracker = false ;

                           public boolean isVendorInfoSpecified(){
                               return localVendorInfoTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getVendorInfo(){
                               return localVendorInfo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param VendorInfo
                               */
                               public void setVendorInfo(java.lang.String param){
                            localVendorInfoTracker = param != null;
                                   
                                            this.localVendorInfo=param;
                                    

                               }
                            

                        /**
                        * field for SupportedLocaleIDs
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localSupportedLocaleIDs ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSupportedLocaleIDsTracker = false ;

                           public boolean isSupportedLocaleIDsSpecified(){
                               return localSupportedLocaleIDsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getSupportedLocaleIDs(){
                               return localSupportedLocaleIDs;
                           }

                           
                        


                               
                              /**
                               * validate the array for SupportedLocaleIDs
                               */
                              protected void validateSupportedLocaleIDs(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param SupportedLocaleIDs
                              */
                              public void setSupportedLocaleIDs(java.lang.String[] param){
                              
                                   validateSupportedLocaleIDs(param);

                               localSupportedLocaleIDsTracker = param != null;
                                      
                                      this.localSupportedLocaleIDs=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addSupportedLocaleIDs(java.lang.String param){
                                   if (localSupportedLocaleIDs == null){
                                   localSupportedLocaleIDs = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localSupportedLocaleIDsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localSupportedLocaleIDs);
                               list.add(param);
                               this.localSupportedLocaleIDs =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for SupportedInterfaceVersions
                        * This was an Array!
                        */

                        
                                    protected org.opcfoundation.xmlda.InterfaceVersion[] localSupportedInterfaceVersions ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSupportedInterfaceVersionsTracker = false ;

                           public boolean isSupportedInterfaceVersionsSpecified(){
                               return localSupportedInterfaceVersionsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.opcfoundation.webservices.xmlda._1_0.InterfaceVersion[]
                           */
                           public  org.opcfoundation.xmlda.InterfaceVersion[] getSupportedInterfaceVersions(){
                               return localSupportedInterfaceVersions;
                           }

                           
                        


                               
                              /**
                               * validate the array for SupportedInterfaceVersions
                               */
                              protected void validateSupportedInterfaceVersions(org.opcfoundation.xmlda.InterfaceVersion[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param SupportedInterfaceVersions
                              */
                              public void setSupportedInterfaceVersions(org.opcfoundation.xmlda.InterfaceVersion[] param){
                              
                                   validateSupportedInterfaceVersions(param);

                               localSupportedInterfaceVersionsTracker = param != null;
                                      
                                      this.localSupportedInterfaceVersions=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.opcfoundation.webservices.xmlda._1_0.InterfaceVersion
                             */
                             public void addSupportedInterfaceVersions(org.opcfoundation.xmlda.InterfaceVersion param){
                                   if (localSupportedInterfaceVersions == null){
                                   localSupportedInterfaceVersions = new org.opcfoundation.xmlda.InterfaceVersion[]{};
                                   }

                            
                                 //update the setting tracker
                                localSupportedInterfaceVersionsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localSupportedInterfaceVersions);
                               list.add(param);
                               this.localSupportedInterfaceVersions =
                             (org.opcfoundation.xmlda.InterfaceVersion[])list.toArray(
                            new org.opcfoundation.xmlda.InterfaceVersion[list.size()]);

                             }
                             

                        /**
                        * field for StartTime
                        * This was an Attribute!
                        */

                        
                                    protected java.util.Calendar localStartTime ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getStartTime(){
                               return localStartTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StartTime
                               */
                               public void setStartTime(java.util.Calendar param){
                            
                                            this.localStartTime=param;
                                    

                               }
                            

                        /**
                        * field for ProductVersion
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localProductVersion ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getProductVersion(){
                               return localProductVersion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ProductVersion
                               */
                               public void setProductVersion(java.lang.String param){
                            
                                            this.localProductVersion=param;
                                    

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
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName);
               return factory.createOMElement(dataSource,parentQName);
            
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
                           namespacePrefix+":ServerStatus",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "ServerStatus",
                           xmlWriter);
                   }

               
                   }
               
                                            if (localStartTime != null){
                                        
                                                writeAttribute("",
                                                         "StartTime",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStartTime), xmlWriter);

                                            
                                      }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localStartTime is null");
                                      }
                                    
                                            if (localProductVersion != null){
                                        
                                                writeAttribute("",
                                                         "ProductVersion",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localProductVersion), xmlWriter);

                                            
                                      }
                                     if (localStatusInfoTracker){
                                    namespace = "http://opcfoundation.org/webservices/XMLDA/1.0/";
                                    writeStartElement(null, namespace, "StatusInfo", xmlWriter);
                             

                                          if (localStatusInfo==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("StatusInfo cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStatusInfo);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localVendorInfoTracker){
                                    namespace = "http://opcfoundation.org/webservices/XMLDA/1.0/";
                                    writeStartElement(null, namespace, "VendorInfo", xmlWriter);
                             

                                          if (localVendorInfo==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("VendorInfo cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localVendorInfo);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSupportedLocaleIDsTracker){
                             if (localSupportedLocaleIDs!=null) {
                                   namespace = "http://opcfoundation.org/webservices/XMLDA/1.0/";
                                   for (int i = 0;i < localSupportedLocaleIDs.length;i++){
                                        
                                            if (localSupportedLocaleIDs[i] != null){
                                        
                                                writeStartElement(null, namespace, "SupportedLocaleIDs", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSupportedLocaleIDs[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // we have to do nothing since minOccurs is zero
                                                       
                                                }

                                   }
                             } else {
                                 
                                         throw new org.apache.axis2.databinding.ADBException("SupportedLocaleIDs cannot be null!!");
                                    
                             }

                        } if (localSupportedInterfaceVersionsTracker){
                                       if (localSupportedInterfaceVersions!=null){
                                            for (int i = 0;i < localSupportedInterfaceVersions.length;i++){
                                                if (localSupportedInterfaceVersions[i] != null){
                                                 localSupportedInterfaceVersions[i].serialize(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","SupportedInterfaceVersions"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                        // we don't have to do any thing since minOccures is zero
                                                    
                                                }

                                            }
                                     } else {
                                        
                                               throw new org.apache.axis2.databinding.ADBException("SupportedInterfaceVersions cannot be null!!");
                                        
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

                 if (localStatusInfoTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                      "StatusInfo"));
                                 
                                        if (localStatusInfo != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStatusInfo));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("StatusInfo cannot be null!!");
                                        }
                                    } if (localVendorInfoTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                      "VendorInfo"));
                                 
                                        if (localVendorInfo != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVendorInfo));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("VendorInfo cannot be null!!");
                                        }
                                    } if (localSupportedLocaleIDsTracker){
                            if (localSupportedLocaleIDs!=null){
                                  for (int i = 0;i < localSupportedLocaleIDs.length;i++){
                                      
                                         if (localSupportedLocaleIDs[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                              "SupportedLocaleIDs"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSupportedLocaleIDs[i]));
                                          } else {
                                             
                                                    // have to do nothing
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    throw new org.apache.axis2.databinding.ADBException("SupportedLocaleIDs cannot be null!!");
                                
                            }

                        } if (localSupportedInterfaceVersionsTracker){
                             if (localSupportedInterfaceVersions!=null) {
                                 for (int i = 0;i < localSupportedInterfaceVersions.length;i++){

                                    if (localSupportedInterfaceVersions[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                          "SupportedInterfaceVersions"));
                                         elementList.add(localSupportedInterfaceVersions[i]);
                                    } else {
                                        
                                                // nothing to do
                                            
                                    }

                                 }
                             } else {
                                 
                                        throw new org.apache.axis2.databinding.ADBException("SupportedInterfaceVersions cannot be null!!");
                                    
                             }

                        }
                            attribList.add(
                            new javax.xml.namespace.QName("","StartTime"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStartTime));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ProductVersion"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localProductVersion));
                                

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
        public static ServerStatus parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ServerStatus object =
                new ServerStatus();

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
                    
                            if (!"ServerStatus".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ServerStatus)org.opcfoundation.xmlda.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    // handle attribute "StartTime"
                    java.lang.String tempAttribStartTime =
                        
                                reader.getAttributeValue(null,"StartTime");
                            
                   if (tempAttribStartTime!=null){
                         java.lang.String content = tempAttribStartTime;
                        
                                                 object.setStartTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(tempAttribStartTime));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute StartTime is missing");
                           
                    }
                    handledAttributes.add("StartTime");
                    
                    // handle attribute "ProductVersion"
                    java.lang.String tempAttribProductVersion =
                        
                                reader.getAttributeValue(null,"ProductVersion");
                            
                   if (tempAttribProductVersion!=null){
                         java.lang.String content = tempAttribProductVersion;
                        
                                                 object.setProductVersion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribProductVersion));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ProductVersion");
                    
                    
                    reader.next();
                
                        java.util.ArrayList list3 = new java.util.ArrayList();
                    
                        java.util.ArrayList list4 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","StatusInfo").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStatusInfo(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","VendorInfo").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setVendorInfo(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","SupportedLocaleIDs").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list3.add(reader.getElementText());
                                            
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone3 = false;
                                            while(!loopDone3){
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
                                                    loopDone3 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","SupportedLocaleIDs").equals(reader.getName())){
                                                         list3.add(reader.getElementText());
                                                        
                                                    }else{
                                                        loopDone3 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setSupportedLocaleIDs((java.lang.String[])
                                                        list3.toArray(new java.lang.String[list3.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","SupportedInterfaceVersions").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list4.add(org.opcfoundation.xmlda.InterfaceVersion.Factory.parse(reader));
                                                                
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
                                                                if (new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","SupportedInterfaceVersions").equals(reader.getName())){
                                                                    list4.add(org.opcfoundation.xmlda.InterfaceVersion.Factory.parse(reader));
                                                                        
                                                                }else{
                                                                    loopDone4 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setSupportedInterfaceVersions((org.opcfoundation.xmlda.InterfaceVersion[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.opcfoundation.xmlda.InterfaceVersion.class,
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
           
    