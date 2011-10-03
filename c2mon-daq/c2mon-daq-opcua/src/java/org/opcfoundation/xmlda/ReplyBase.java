
/**
 * ReplyBase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package org.opcfoundation.xmlda;
            

            /**
            *  ReplyBase bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ReplyBase
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = ReplyBase
                Namespace URI = http://opcfoundation.org/webservices/XMLDA/1.0/
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for RcvTime
                        * This was an Attribute!
                        */

                        
                                    protected java.util.Calendar localRcvTime ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getRcvTime(){
                               return localRcvTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RcvTime
                               */
                               public void setRcvTime(java.util.Calendar param){
                            
                                            this.localRcvTime=param;
                                    

                               }
                            

                        /**
                        * field for ReplyTime
                        * This was an Attribute!
                        */

                        
                                    protected java.util.Calendar localReplyTime ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.util.Calendar
                           */
                           public  java.util.Calendar getReplyTime(){
                               return localReplyTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReplyTime
                               */
                               public void setReplyTime(java.util.Calendar param){
                            
                                            this.localReplyTime=param;
                                    

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
                        * field for RevisedLocaleID
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localRevisedLocaleID ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRevisedLocaleID(){
                               return localRevisedLocaleID;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RevisedLocaleID
                               */
                               public void setRevisedLocaleID(java.lang.String param){
                            
                                            this.localRevisedLocaleID=param;
                                    

                               }
                            

                        /**
                        * field for ServerState
                        * This was an Attribute!
                        */

                        
                                    protected org.opcfoundation.xmlda.ServerState localServerState ;
                                

                           /**
                           * Auto generated getter method
                           * @return org.opcfoundation.webservices.xmlda._1_0.ServerState
                           */
                           public  org.opcfoundation.xmlda.ServerState getServerState(){
                               return localServerState;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ServerState
                               */
                               public void setServerState(org.opcfoundation.xmlda.ServerState param){
                            
                                            this.localServerState=param;
                                    

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
                           namespacePrefix+":ReplyBase",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "ReplyBase",
                           xmlWriter);
                   }

               
                   }
               
                                            if (localRcvTime != null){
                                        
                                                writeAttribute("",
                                                         "RcvTime",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRcvTime), xmlWriter);

                                            
                                      }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localRcvTime is null");
                                      }
                                    
                                            if (localReplyTime != null){
                                        
                                                writeAttribute("",
                                                         "ReplyTime",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReplyTime), xmlWriter);

                                            
                                      }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localReplyTime is null");
                                      }
                                    
                                            if (localClientRequestHandle != null){
                                        
                                                writeAttribute("",
                                                         "ClientRequestHandle",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientRequestHandle), xmlWriter);

                                            
                                      }
                                    
                                            if (localRevisedLocaleID != null){
                                        
                                                writeAttribute("",
                                                         "RevisedLocaleID",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRevisedLocaleID), xmlWriter);

                                            
                                      }
                                    
                                    
                                    if (localServerState != null){
                                        writeAttribute("",
                                           "ServerState",
                                           localServerState.toString(), xmlWriter);
                                    }
                                    
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute localServerState is null");
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
                            new javax.xml.namespace.QName("","RcvTime"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRcvTime));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ReplyTime"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReplyTime));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ClientRequestHandle"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientRequestHandle));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","RevisedLocaleID"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRevisedLocaleID));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ServerState"));
                            
                                      attribList.add(localServerState.toString());
                                

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
        public static ReplyBase parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ReplyBase object =
                new ReplyBase();

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
                    
                            if (!"ReplyBase".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ReplyBase)org.opcfoundation.xmlda.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    // handle attribute "RcvTime"
                    java.lang.String tempAttribRcvTime =
                        
                                reader.getAttributeValue(null,"RcvTime");
                            
                   if (tempAttribRcvTime!=null){
                         java.lang.String content = tempAttribRcvTime;
                        
                                                 object.setRcvTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(tempAttribRcvTime));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute RcvTime is missing");
                           
                    }
                    handledAttributes.add("RcvTime");
                    
                    // handle attribute "ReplyTime"
                    java.lang.String tempAttribReplyTime =
                        
                                reader.getAttributeValue(null,"ReplyTime");
                            
                   if (tempAttribReplyTime!=null){
                         java.lang.String content = tempAttribReplyTime;
                        
                                                 object.setReplyTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDateTime(tempAttribReplyTime));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute ReplyTime is missing");
                           
                    }
                    handledAttributes.add("ReplyTime");
                    
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
                    
                    // handle attribute "RevisedLocaleID"
                    java.lang.String tempAttribRevisedLocaleID =
                        
                                reader.getAttributeValue(null,"RevisedLocaleID");
                            
                   if (tempAttribRevisedLocaleID!=null){
                         java.lang.String content = tempAttribRevisedLocaleID;
                        
                                                 object.setRevisedLocaleID(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribRevisedLocaleID));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("RevisedLocaleID");
                    
                    // handle attribute "ServerState"
                    java.lang.String tempAttribServerState =
                        
                                reader.getAttributeValue(null,"ServerState");
                            
                   if (tempAttribServerState!=null){
                         java.lang.String content = tempAttribServerState;
                        
                                                  object.setServerState(
                                                        org.opcfoundation.xmlda.ServerState.Factory.fromString(reader,tempAttribServerState));
                                            
                    } else {
                       
                               throw new org.apache.axis2.databinding.ADBException("Required attribute ServerState is missing");
                           
                    }
                    handledAttributes.add("ServerState");
                    
                    
                    reader.next();
                



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    