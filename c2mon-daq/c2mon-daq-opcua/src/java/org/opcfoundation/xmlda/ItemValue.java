
/**
 * ItemValue.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package org.opcfoundation.xmlda;
            

            /**
            *  ItemValue bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ItemValue
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = ItemValue
                Namespace URI = http://opcfoundation.org/webservices/XMLDA/1.0/
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for DiagnosticInfo
                        */

                        
                                    protected java.lang.String localDiagnosticInfo ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDiagnosticInfoTracker = false ;

                           public boolean isDiagnosticInfoSpecified(){
                               return localDiagnosticInfoTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDiagnosticInfo(){
                               return localDiagnosticInfo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DiagnosticInfo
                               */
                               public void setDiagnosticInfo(java.lang.String param){
                            localDiagnosticInfoTracker = param != null;
                                   
                                            this.localDiagnosticInfo=param;
                                    

                               }
                            

                        /**
                        * field for Value
                        */

                        
                                    protected org.apache.axiom.om.OMElement localValue ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localValueTracker = false ;

                           public boolean isValueSpecified(){
                               return localValueTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.apache.axiom.om.OMElement
                           */
                           public  org.apache.axiom.om.OMElement getValue(){
                               return localValue;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Value
                               */
                               public void setValue(org.apache.axiom.om.OMElement param){
                            localValueTracker = param != null;
                                   
                                            this.localValue=param;
                                    

                               }
                            

                        /**
                        * field for Quality
                        */

                        
                                    protected org.opcfoundation.xmlda.OPCQuality localQuality ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localQualityTracker = false ;

                           public boolean isQualitySpecified(){
                               return localQualityTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.opcfoundation.webservices.xmlda._1_0.OPCQuality
                           */
                           public  org.opcfoundation.xmlda.OPCQuality getQuality(){
                               return localQuality;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Quality
                               */
                               public void setQuality(org.opcfoundation.xmlda.OPCQuality param){
                            localQualityTracker = param != null;
                                   
                                            this.localQuality=param;
                                    

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
                        * field for ClientItemHandle
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localClientItemHandle ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getClientItemHandle(){
                               return localClientItemHandle;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ClientItemHandle
                               */
                               public void setClientItemHandle(java.lang.String param){
                            
                                            this.localClientItemHandle=param;
                                    

                               }
                            

                        /**
                        * field for Timestamp
                        * This was an Attribute!
                        */

                        
                                    protected java.util.Calendar localTimestamp ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getTimestamp(){
                               return localTimestamp;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Timestamp
                               */
                               public void setTimestamp(java.util.Calendar param){
                            
                                            this.localTimestamp=param;
                                    

                               }
                            

                        /**
                        * field for ResultID
                        * This was an Attribute!
                        */

                        
                                    protected javax.xml.namespace.QName localResultID ;
                                

                           /**
                           * Auto generated getter method
                           * @return javax.xml.namespace.QName
                           */
                           public  javax.xml.namespace.QName getResultID(){
                               return localResultID;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ResultID
                               */
                               public void setResultID(javax.xml.namespace.QName param){
                            
                                            this.localResultID=param;
                                    

                               }
                            

                        /**
                        * field for ValueTypeQualifier
                        * This was an Attribute!
                        */

                        
                                    protected javax.xml.namespace.QName localValueTypeQualifier ;
                                

                           /**
                           * Auto generated getter method
                           * @return javax.xml.namespace.QName
                           */
                           public  javax.xml.namespace.QName getValueTypeQualifier(){
                               return localValueTypeQualifier;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ValueTypeQualifier
                               */
                               public void setValueTypeQualifier(javax.xml.namespace.QName param){
                            
                                            this.localValueTypeQualifier=param;
                                    

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
                           namespacePrefix+":ItemValue",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "ItemValue",
                           xmlWriter);
                   }

               
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
                                    
                                            if (localClientItemHandle != null){
                                        
                                                writeAttribute("",
                                                         "ClientItemHandle",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientItemHandle), xmlWriter);

                                            
                                      }
                                    
                                            if (localTimestamp != null){
                                        
                                                writeAttribute("",
                                                         "Timestamp",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTimestamp), xmlWriter);

                                            
                                      }
                                    
                                            if (localResultID != null){
                                        
                                                writeQNameAttribute("",
                                                         "ResultID",
                                                         localResultID, xmlWriter);

                                            
                                      }
                                    
                                            if (localValueTypeQualifier != null){
                                        
                                                writeQNameAttribute("",
                                                         "ValueTypeQualifier",
                                                         localValueTypeQualifier, xmlWriter);

                                            
                                      }
                                     if (localDiagnosticInfoTracker){
                                    namespace = "http://opcfoundation.org/webservices/XMLDA/1.0/";
                                    writeStartElement(null, namespace, "DiagnosticInfo", xmlWriter);
                             

                                          if (localDiagnosticInfo==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("DiagnosticInfo cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDiagnosticInfo);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localValueTracker){
//                                    namespace = "http://opcfoundation.org/webservices/XMLDA/1.0/";
//                                    writeStartElement(null, namespace, "Value", xmlWriter);
                             

                                          if (localValue==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("Value cannot be null!!");
                                                  
                                          }else{

                                        localValue.serialize(xmlWriter);
                                            
                                          }
                                    
//                                   xmlWriter.writeEndElement();
                             } if (localQualityTracker){
                                            if (localQuality==null){
                                                 throw new org.apache.axis2.databinding.ADBException("Quality cannot be null!!");
                                            }
                                           localQuality.serialize(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Quality"),
                                               xmlWriter);
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

                 if (localDiagnosticInfoTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                      "DiagnosticInfo"));
                                 
                                        if (localDiagnosticInfo != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDiagnosticInfo));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("DiagnosticInfo cannot be null!!");
                                        }
                                    } if (localValueTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                      "Value"));
                                 
                                        if (localValue != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localValue));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("Value cannot be null!!");
                                        }
                                    } if (localQualityTracker){
                            elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                      "Quality"));
                            
                            
                                    if (localQuality==null){
                                         throw new org.apache.axis2.databinding.ADBException("Quality cannot be null!!");
                                    }
                                    elementList.add(localQuality);
                                }
                            attribList.add(
                            new javax.xml.namespace.QName("","ItemPath"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localItemPath));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ItemName"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localItemName));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ClientItemHandle"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientItemHandle));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","Timestamp"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTimestamp));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ResultID"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localResultID));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ValueTypeQualifier"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localValueTypeQualifier));
                                

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
        public static ItemValue parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ItemValue object =
                new ItemValue();

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
                    
                            if (!"ItemValue".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ItemValue)org.opcfoundation.xmlda.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
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
                    
                    // handle attribute "ClientItemHandle"
                    java.lang.String tempAttribClientItemHandle =
                        
                                reader.getAttributeValue(null,"ClientItemHandle");
                            
                   if (tempAttribClientItemHandle!=null){
                         java.lang.String content = tempAttribClientItemHandle;
                        
                                                 object.setClientItemHandle(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribClientItemHandle));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ClientItemHandle");
                    
                    // handle attribute "Timestamp"
                    java.lang.String tempAttribTimestamp =
                        
                                reader.getAttributeValue(null,"Timestamp");
                            
                   if (tempAttribTimestamp!=null){
                         java.lang.String content = tempAttribTimestamp;
                        
                                                 object.setTimestamp(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(tempAttribTimestamp));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("Timestamp");
                    
                    // handle attribute "ResultID"
                    java.lang.String tempAttribResultID =
                        
                                reader.getAttributeValue(null,"ResultID");
                            
                   if (tempAttribResultID!=null){
                         java.lang.String content = tempAttribResultID;
                        
                                int index = tempAttribResultID.indexOf(":");
                                if(index > -1){
                                     prefix = tempAttribResultID.substring(0,index);
                                } else {
                                    // i.e this is in default namesace
                                    prefix = "";
                                }
                                namespaceuri = reader.getNamespaceURI(prefix);
                                 
                                         object.setResultID(
                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToQName(tempAttribResultID,namespaceuri));
                                     
                    } else {
                       
                    }
                    handledAttributes.add("ResultID");
                    
                    // handle attribute "ValueTypeQualifier"
                    java.lang.String tempAttribValueTypeQualifier =
                        
                                reader.getAttributeValue(null,"ValueTypeQualifier");
                            
                   if (tempAttribValueTypeQualifier!=null){
                         java.lang.String content = tempAttribValueTypeQualifier;
                        
                                int index = tempAttribValueTypeQualifier.indexOf(":");
                                if(index > -1){
                                     prefix = tempAttribValueTypeQualifier.substring(0,index);
                                } else {
                                    // i.e this is in default namesace
                                    prefix = "";
                                }
                                namespaceuri = reader.getNamespaceURI(prefix);
                                 
                                         object.setValueTypeQualifier(
                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToQName(tempAttribValueTypeQualifier,namespaceuri));
                                     
                    } else {
                       
                    }
                    handledAttributes.add("ValueTypeQualifier");
                    
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","DiagnosticInfo").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDiagnosticInfo(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                   if (reader.isStartElement()){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                                org.apache.axiom.om.OMFactory fac = org.apache.axiom.om.OMAbstractFactory.getOMFactory();
                                                org.apache.axiom.om.OMNamespace omNs = fac.createOMNamespace("http://opcfoundation.org/webservices/XMLDA/1.0/", "");
                                                org.apache.axiom.om.OMElement _valueValue = fac.createOMElement("Value", omNs);
                                                _valueValue.addChild(fac.createOMText(_valueValue, content));
                                                object.setValue(_valueValue);
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Quality").equals(reader.getName())){
                                
                                                object.setQuality(org.opcfoundation.xmlda.OPCQuality.Factory.parse(reader));
                                              
                                        reader.next();
                                    
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
           
    