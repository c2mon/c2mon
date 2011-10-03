
/**
 * RequestOptions.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package org.opcfoundation.xmlda;
            

            /**
            *  RequestOptions bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class RequestOptions
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = RequestOptions
                Namespace URI = http://opcfoundation.org/webservices/XMLDA/1.0/
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for RequestDeadline
                        * This was an Attribute!
                        */

                        
                                    protected java.util.Calendar localRequestDeadline ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getRequestDeadline(){
                               return localRequestDeadline;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RequestDeadline
                               */
                               public void setRequestDeadline(java.util.Calendar param){
                            
                                            this.localRequestDeadline=param;
                                    

                               }
                            

                        /**
                        * field for ReturnErrorText
                        * This was an Attribute!
                        */

                        
                                    protected boolean localReturnErrorText =
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean("true");
                                

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
                        * field for ReturnDiagnosticInfo
                        * This was an Attribute!
                        */

                        
                                    protected boolean localReturnDiagnosticInfo =
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean("false");
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getReturnDiagnosticInfo(){
                               return localReturnDiagnosticInfo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReturnDiagnosticInfo
                               */
                               public void setReturnDiagnosticInfo(boolean param){
                            
                                            this.localReturnDiagnosticInfo=param;
                                    

                               }
                            

                        /**
                        * field for ReturnItemTime
                        * This was an Attribute!
                        */

                        
                                    protected boolean localReturnItemTime =
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean("false");
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getReturnItemTime(){
                               return localReturnItemTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReturnItemTime
                               */
                               public void setReturnItemTime(boolean param){
                            
                                            this.localReturnItemTime=param;
                                    

                               }
                            

                        /**
                        * field for ReturnItemPath
                        * This was an Attribute!
                        */

                        
                                    protected boolean localReturnItemPath =
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean("false");
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getReturnItemPath(){
                               return localReturnItemPath;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReturnItemPath
                               */
                               public void setReturnItemPath(boolean param){
                            
                                            this.localReturnItemPath=param;
                                    

                               }
                            

                        /**
                        * field for ReturnItemName
                        * This was an Attribute!
                        */

                        
                                    protected boolean localReturnItemName =
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean("false");
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getReturnItemName(){
                               return localReturnItemName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReturnItemName
                               */
                               public void setReturnItemName(boolean param){
                            
                                            this.localReturnItemName=param;
                                    

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
                           namespacePrefix+":RequestOptions",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "RequestOptions",
                           xmlWriter);
                   }

               
                   }
               
                                            if (localRequestDeadline != null){
                                        
                                                writeAttribute("",
                                                         "RequestDeadline",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRequestDeadline), xmlWriter);

                                            
                                      }
                                    
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "ReturnErrorText",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnErrorText), xmlWriter);

                                            
                                      }
                                    
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "ReturnDiagnosticInfo",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnDiagnosticInfo), xmlWriter);

                                            
                                      }
                                    
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "ReturnItemTime",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnItemTime), xmlWriter);

                                            
                                      }
                                    
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "ReturnItemPath",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnItemPath), xmlWriter);

                                            
                                      }
                                    
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "ReturnItemName",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnItemName), xmlWriter);

                                            
                                      }
                                    
                                            if (localClientRequestHandle != null){
                                        
                                                writeAttribute("",
                                                         "ClientRequestHandle",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientRequestHandle), xmlWriter);

                                            
                                      }
                                    
                                            if (localLocaleID != null){
                                        
                                                writeAttribute("",
                                                         "LocaleID",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLocaleID), xmlWriter);

                                            
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

                
                            attribList.add(
                            new javax.xml.namespace.QName("","RequestDeadline"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRequestDeadline));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ReturnErrorText"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnErrorText));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ReturnDiagnosticInfo"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnDiagnosticInfo));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ReturnItemTime"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnItemTime));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ReturnItemPath"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnItemPath));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ReturnItemName"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReturnItemName));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ClientRequestHandle"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientRequestHandle));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","LocaleID"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLocaleID));
                                

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
        public static RequestOptions parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            RequestOptions object =
                new RequestOptions();

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
                    
                            if (!"RequestOptions".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (RequestOptions)org.opcfoundation.xmlda.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    // handle attribute "RequestDeadline"
                    java.lang.String tempAttribRequestDeadline =
                        
                                reader.getAttributeValue(null,"RequestDeadline");
                            
                   if (tempAttribRequestDeadline!=null){
                         java.lang.String content = tempAttribRequestDeadline;
                        
                                                 object.setRequestDeadline(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(tempAttribRequestDeadline));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("RequestDeadline");
                    
                    // handle attribute "ReturnErrorText"
                    java.lang.String tempAttribReturnErrorText =
                        
                                reader.getAttributeValue(null,"ReturnErrorText");
                            
                   if (tempAttribReturnErrorText!=null){
                         java.lang.String content = tempAttribReturnErrorText;
                        
                                                 object.setReturnErrorText(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribReturnErrorText));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ReturnErrorText");
                    
                    // handle attribute "ReturnDiagnosticInfo"
                    java.lang.String tempAttribReturnDiagnosticInfo =
                        
                                reader.getAttributeValue(null,"ReturnDiagnosticInfo");
                            
                   if (tempAttribReturnDiagnosticInfo!=null){
                         java.lang.String content = tempAttribReturnDiagnosticInfo;
                        
                                                 object.setReturnDiagnosticInfo(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribReturnDiagnosticInfo));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ReturnDiagnosticInfo");
                    
                    // handle attribute "ReturnItemTime"
                    java.lang.String tempAttribReturnItemTime =
                        
                                reader.getAttributeValue(null,"ReturnItemTime");
                            
                   if (tempAttribReturnItemTime!=null){
                         java.lang.String content = tempAttribReturnItemTime;
                        
                                                 object.setReturnItemTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribReturnItemTime));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ReturnItemTime");
                    
                    // handle attribute "ReturnItemPath"
                    java.lang.String tempAttribReturnItemPath =
                        
                                reader.getAttributeValue(null,"ReturnItemPath");
                            
                   if (tempAttribReturnItemPath!=null){
                         java.lang.String content = tempAttribReturnItemPath;
                        
                                                 object.setReturnItemPath(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribReturnItemPath));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ReturnItemPath");
                    
                    // handle attribute "ReturnItemName"
                    java.lang.String tempAttribReturnItemName =
                        
                                reader.getAttributeValue(null,"ReturnItemName");
                            
                   if (tempAttribReturnItemName!=null){
                         java.lang.String content = tempAttribReturnItemName;
                        
                                                 object.setReturnItemName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribReturnItemName));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("ReturnItemName");
                    
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
                    
                    
                    reader.next();
                



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    