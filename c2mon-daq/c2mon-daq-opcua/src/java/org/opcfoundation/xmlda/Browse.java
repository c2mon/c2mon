
/**
 * Browse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package org.opcfoundation.xmlda;
            

            /**
            *  Browse bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class Browse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://opcfoundation.org/webservices/XMLDA/1.0/",
                "Browse",
                "ns1");

            

                        /**
                        * field for PropertyNames
                        * This was an Array!
                        */

                        
                                    protected javax.xml.namespace.QName[] localPropertyNames ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPropertyNamesTracker = false ;

                           public boolean isPropertyNamesSpecified(){
                               return localPropertyNamesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return javax.xml.namespace.QName[]
                           */
                           public  javax.xml.namespace.QName[] getPropertyNames(){
                               return localPropertyNames;
                           }

                           
                        


                               
                              /**
                               * validate the array for PropertyNames
                               */
                              protected void validatePropertyNames(javax.xml.namespace.QName[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param PropertyNames
                              */
                              public void setPropertyNames(javax.xml.namespace.QName[] param){
                              
                                   validatePropertyNames(param);

                               localPropertyNamesTracker = param != null;
                                      
                                      this.localPropertyNames=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param javax.xml.namespace.QName
                             */
                             public void addPropertyNames(javax.xml.namespace.QName param){
                                   if (localPropertyNames == null){
                                   localPropertyNames = new javax.xml.namespace.QName[]{};
                                   }

                            
                                 //update the setting tracker
                                localPropertyNamesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localPropertyNames);
                               list.add(param);
                               this.localPropertyNames =
                             (javax.xml.namespace.QName[])list.toArray(
                            new javax.xml.namespace.QName[list.size()]);

                             }
                             

                        /**
                        * field for LocaleID
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localLocaleID ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getLocaleID(){
                               return localLocaleID;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param LocaleID
                               */
                               public void setLocaleID(java.lang.String param){
                            
                                            this.localLocaleID=param;
                                    

                               }
                            

                        /**
                        * field for ClientRequestHandle
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localClientRequestHandle ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getClientRequestHandle(){
                               return localClientRequestHandle;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ClientRequestHandle
                               */
                               public void setClientRequestHandle(java.lang.String param){
                            
                                            this.localClientRequestHandle=param;
                                    

                               }
                            

                        /**
                        * field for ItemPath
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localItemPath ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getItemPath(){
                               return localItemPath;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ItemPath
                               */
                               public void setItemPath(java.lang.String param){
                            
                                            this.localItemPath=param;
                                    

                               }
                            

                        /**
                        * field for ItemName
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localItemName ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getItemName(){
                               return localItemName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ItemName
                               */
                               public void setItemName(java.lang.String param){
                            
                                            this.localItemName=param;
                                    

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
                        * field for MaxElementsReturned
                        * This was an Attribute!
                        */

                        
                                    protected int localMaxElementsReturned ;
                                

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getMaxElementsReturned(){
                               return localMaxElementsReturned;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MaxElementsReturned
                               */
                               public void setMaxElementsReturned(int param){
                            
                                            this.localMaxElementsReturned=param;
                                    

                               }
                            

                        /**
                        * field for BrowseFilter
                        * This was an Attribute!
                        */

                        
                                    protected org.opcfoundation.xmlda.BrowseFilter localBrowseFilter ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.opcfoundation.webservices.xmlda._1_0.BrowseFilter
                           */
                           public  org.opcfoundation.xmlda.BrowseFilter getBrowseFilter(){
                               return localBrowseFilter;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param BrowseFilter
                               */
                               public void setBrowseFilter(org.opcfoundation.xmlda.BrowseFilter param){
                            
                                            this.localBrowseFilter=param;
                                    

                               }
                            

                        /**
                        * field for ElementNameFilter
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localElementNameFilter ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getElementNameFilter(){
                               return localElementNameFilter;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ElementNameFilter
                               */
                               public void setElementNameFilter(java.lang.String param){
                            
                                            this.localElementNameFilter=param;
                                    

                               }
                            

                        /**
                        * field for VendorFilter
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localVendorFilter ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getVendorFilter(){
                               return localVendorFilter;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param VendorFilter
                               */
                               public void setVendorFilter(java.lang.String param){
                            
                                            this.localVendorFilter=param;
                                    

                               }
                            

                        /**
                        * field for ReturnAllProperties
                        * This was an Attribute!
                        */

                        
                                    protected boolean localReturnAllProperties ;
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getReturnAllProperties(){
                               return localReturnAllProperties;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReturnAllProperties
                               */
                               public void setReturnAllProperties(boolean param){
                            
                                            this.localReturnAllProperties=param;
                                    

                               }
                            

                        /**
                        * field for ReturnPropertyValues
                        * This was an Attribute!
                        */

                        
                                    protected boolean localReturnPropertyValues ;
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getReturnPropertyValues(){
                               return localReturnPropertyValues;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReturnPropertyValues
                               */
                               public void setReturnPropertyValues(boolean param){
                            
                                            this.localReturnPropertyValues=param;
                                    

                               }
                            

                        /**
                        * field for ReturnErrorText
                        * This was an Attribute!
                        */

                        
                                    protected boolean localReturnErrorText ;
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getReturnErrorText(){
                               return localReturnErrorText;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReturnErrorText
                               */
                               public void setReturnErrorText(boolean param){
                            
                                            this.localReturnErrorText=param;
                                    

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
                           namespacePrefix+":Browse",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "Browse",
                           xmlWriter);
                   }

               
                   }
               
                                            if (localLocaleID != null){
                                        
                                                writeAttribute("",
                                                         "LocaleID",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLocaleID), xmlWriter);

                                            
                                      }
                                    
                                            if (localClientRequestHandle != null){
                                        
                                                writeAttribute("",
                                                         "ClientRequestHandle",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientRequestHandle), xmlWriter);

                                            
                                      }
                                    
                                            if (localItemPath != null){
                                        
                                                writeAttribute("",
                                                         "ItemPath",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localItemPath), xmlWriter);

                                            
                                      }
                                    
                                            if (localItemName != null){
                                        
                                                writeAttribute("",
                                                         "ItemName",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localItemName), xmlWriter);

                                            
                                      }
                                    
                                            if (localContinuationPoint != null){
                                        
                                                writeAttribute("",
                                                         "ContinuationPoint",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localContinuationPoint), xmlWriter);

                                            
                                      }
                                    
                                                   if (localMaxElementsReturned!=java.lang.Integer.MIN_VALUE) {
                                               
                                                writeAttribute("",
                                                         "MaxElementsReturned",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxElementsReturned), xmlWriter);

                                            
                                      }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localMaxElementsReturned is null");
                                      }
                                    
                                    
                                    if (localBrowseFilter != null){
                                        writeAttribute("",
                                           "BrowseFilter",
                                           localBrowseFilter.toString(), xmlWriter);
                                    }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localBrowseFilter is null");
                                      }
                                    
                                            if (localElementNameFilter != null){
                                        
                                                writeAttribute("",
                                                         "ElementNameFilter",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localElementNameFilter), xmlWriter);

                                            
                                      }
                                    
                                            if (localVendorFilter != null){
                                        
                                                writeAttribute("",
                                                         "VendorFilter",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVendorFilter), xmlWriter);

                                            
                                      }
                                    
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "ReturnAllProperties",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnAllProperties), xmlWriter);

                                            
                                      }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localReturnAllProperties is null");
                                      }
                                    
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "ReturnPropertyValues",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnPropertyValues), xmlWriter);

                                            
                                      }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localReturnPropertyValues is null");
                                      }
                                    
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "ReturnErrorText",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnErrorText), xmlWriter);

                                            
                                      }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localReturnErrorText is null");
                                      }
                                     if (localPropertyNamesTracker){
                             if (localPropertyNames!=null) {
                                   namespace = "http://opcfoundation.org/webservices/XMLDA/1.0/";
                                   for (int i = 0;i < localPropertyNames.length;i++){
                                        
                                            if (localPropertyNames[i] != null){
                                        
                                                writeStartElement(null, namespace, "PropertyNames", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPropertyNames[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // we have to do nothing since minOccurs is zero
                                                       
                                                }

                                   }
                             } else {
                                 
                                         throw new org.apache.axis2.databinding.ADBException("PropertyNames cannot be null!!");
                                    
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

                 if (localPropertyNamesTracker){
                            if (localPropertyNames!=null){
                                  for (int i = 0;i < localPropertyNames.length;i++){
                                      
                                         if (localPropertyNames[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                              "PropertyNames"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPropertyNames[i]));
                                          } else {
                                             
                                                    // have to do nothing
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    throw new org.apache.axis2.databinding.ADBException("PropertyNames cannot be null!!");
                                
                            }

                        }
                            attribList.add(
                            new javax.xml.namespace.QName("","LocaleID"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLocaleID));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ClientRequestHandle"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientRequestHandle));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ItemPath"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localItemPath));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ItemName"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localItemName));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ContinuationPoint"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localContinuationPoint));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","MaxElementsReturned"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMaxElementsReturned));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","BrowseFilter"));
                            
                                      attribList.add(localBrowseFilter.toString());
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ElementNameFilter"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localElementNameFilter));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","VendorFilter"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVendorFilter));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ReturnAllProperties"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnAllProperties));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ReturnPropertyValues"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnPropertyValues));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ReturnErrorText"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnErrorText));
                                

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
        public static Browse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            Browse object =
                new Browse();

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
                    
                            if (!"Browse".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (Browse)org.opcfoundation.xmlda.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    // handle attribute "LocaleID"
                    java.lang.String tempAttribLocaleID =
                        
                                reader.getAttributeValue(null,"LocaleID");
                            
                   if (tempAttribLocaleID!=null){
                         java.lang.String content = tempAttribLocaleID;
                        
                                                 object.setLocaleID(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribLocaleID));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("LocaleID");
                    
                    // handle attribute "ClientRequestHandle"
                    java.lang.String tempAttribClientRequestHandle =
                        
                                reader.getAttributeValue(null,"ClientRequestHandle");
                            
                   if (tempAttribClientRequestHandle!=null){
                         java.lang.String content = tempAttribClientRequestHandle;
                        
                                                 object.setClientRequestHandle(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribClientRequestHandle));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ClientRequestHandle");
                    
                    // handle attribute "ItemPath"
                    java.lang.String tempAttribItemPath =
                        
                                reader.getAttributeValue(null,"ItemPath");
                            
                   if (tempAttribItemPath!=null){
                         java.lang.String content = tempAttribItemPath;
                        
                                                 object.setItemPath(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribItemPath));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ItemPath");
                    
                    // handle attribute "ItemName"
                    java.lang.String tempAttribItemName =
                        
                                reader.getAttributeValue(null,"ItemName");
                            
                   if (tempAttribItemName!=null){
                         java.lang.String content = tempAttribItemName;
                        
                                                 object.setItemName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribItemName));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ItemName");
                    
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
                    
                    // handle attribute "MaxElementsReturned"
                    java.lang.String tempAttribMaxElementsReturned =
                        
                                reader.getAttributeValue(null,"MaxElementsReturned");
                            
                   if (tempAttribMaxElementsReturned!=null){
                         java.lang.String content = tempAttribMaxElementsReturned;
                        
                                                 object.setMaxElementsReturned(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(tempAttribMaxElementsReturned));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute MaxElementsReturned is missing");
                           
                    }
                    handledAttributes.add("MaxElementsReturned");
                    
                    // handle attribute "BrowseFilter"
                    java.lang.String tempAttribBrowseFilter =
                        
                                reader.getAttributeValue(null,"BrowseFilter");
                            
                   if (tempAttribBrowseFilter!=null){
                         java.lang.String content = tempAttribBrowseFilter;
                        
                                                  object.setBrowseFilter(
                                                        org.opcfoundation.xmlda.BrowseFilter.Factory.fromString(reader,tempAttribBrowseFilter));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute BrowseFilter is missing");
                           
                    }
                    handledAttributes.add("BrowseFilter");
                    
                    // handle attribute "ElementNameFilter"
                    java.lang.String tempAttribElementNameFilter =
                        
                                reader.getAttributeValue(null,"ElementNameFilter");
                            
                   if (tempAttribElementNameFilter!=null){
                         java.lang.String content = tempAttribElementNameFilter;
                        
                                                 object.setElementNameFilter(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribElementNameFilter));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ElementNameFilter");
                    
                    // handle attribute "VendorFilter"
                    java.lang.String tempAttribVendorFilter =
                        
                                reader.getAttributeValue(null,"VendorFilter");
                            
                   if (tempAttribVendorFilter!=null){
                         java.lang.String content = tempAttribVendorFilter;
                        
                                                 object.setVendorFilter(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribVendorFilter));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("VendorFilter");
                    
                    // handle attribute "ReturnAllProperties"
                    java.lang.String tempAttribReturnAllProperties =
                        
                                reader.getAttributeValue(null,"ReturnAllProperties");
                            
                   if (tempAttribReturnAllProperties!=null){
                         java.lang.String content = tempAttribReturnAllProperties;
                        
                                                 object.setReturnAllProperties(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribReturnAllProperties));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute ReturnAllProperties is missing");
                           
                    }
                    handledAttributes.add("ReturnAllProperties");
                    
                    // handle attribute "ReturnPropertyValues"
                    java.lang.String tempAttribReturnPropertyValues =
                        
                                reader.getAttributeValue(null,"ReturnPropertyValues");
                            
                   if (tempAttribReturnPropertyValues!=null){
                         java.lang.String content = tempAttribReturnPropertyValues;
                        
                                                 object.setReturnPropertyValues(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribReturnPropertyValues));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute ReturnPropertyValues is missing");
                           
                    }
                    handledAttributes.add("ReturnPropertyValues");
                    
                    // handle attribute "ReturnErrorText"
                    java.lang.String tempAttribReturnErrorText =
                        
                                reader.getAttributeValue(null,"ReturnErrorText");
                            
                   if (tempAttribReturnErrorText!=null){
                         java.lang.String content = tempAttribReturnErrorText;
                        
                                                 object.setReturnErrorText(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribReturnErrorText));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute ReturnErrorText is missing");
                           
                    }
                    handledAttributes.add("ReturnErrorText");
                    
                    
                    reader.next();
                
                        java.util.ArrayList list1 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","PropertyNames").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list1.add(reader.getElementText());
                                            
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone1 = false;
                                            while(!loopDone1){
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
                                                    loopDone1 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","PropertyNames").equals(reader.getName())){
                                                         list1.add(reader.getElementText());
                                                        
                                                    }else{
                                                        loopDone1 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                            object.setPropertyNames((javax.xml.namespace.QName[])
                                                org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                            javax.xml.namespace.QName.class,list1));
                                                
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
           
    