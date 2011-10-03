
/**
 * SubscribeRequestItemList.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package org.opcfoundation.xmlda;
            

            /**
            *  SubscribeRequestItemList bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class SubscribeRequestItemList
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = SubscribeRequestItemList
                Namespace URI = http://opcfoundation.org/webservices/XMLDA/1.0/
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for Items
                        * This was an Array!
                        */

                        
                                    protected org.opcfoundation.xmlda.SubscribeRequestItem[] localItems ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localItemsTracker = false ;

                           public boolean isItemsSpecified(){
                               return localItemsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.opcfoundation.webservices.xmlda._1_0.SubscribeRequestItem[]
                           */
                           public  org.opcfoundation.xmlda.SubscribeRequestItem[] getItems(){
                               return localItems;
                           }

                           
                        


                               
                              /**
                               * validate the array for Items
                               */
                              protected void validateItems(org.opcfoundation.xmlda.SubscribeRequestItem[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Items
                              */
                              public void setItems(org.opcfoundation.xmlda.SubscribeRequestItem[] param){
                              
                                   validateItems(param);

                               localItemsTracker = param != null;
                                      
                                      this.localItems=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.opcfoundation.webservices.xmlda._1_0.SubscribeRequestItem
                             */
                             public void addItems(org.opcfoundation.xmlda.SubscribeRequestItem param){
                                   if (localItems == null){
                                   localItems = new org.opcfoundation.xmlda.SubscribeRequestItem[]{};
                                   }

                            
                                 //update the setting tracker
                                localItemsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localItems);
                               list.add(param);
                               this.localItems =
                             (org.opcfoundation.xmlda.SubscribeRequestItem[])list.toArray(
                            new org.opcfoundation.xmlda.SubscribeRequestItem[list.size()]);

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
                        * field for ReqType
                        * This was an Attribute!
                        */

                        
                                    protected javax.xml.namespace.QName localReqType ;
                                

                           /**
                           * Auto generated getter method
                           * @return javax.xml.namespace.QName
                           */
                           public  javax.xml.namespace.QName getReqType(){
                               return localReqType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ReqType
                               */
                               public void setReqType(javax.xml.namespace.QName param){
                            
                                            this.localReqType=param;
                                    

                               }
                            

                        /**
                        * field for Deadband
                        * This was an Attribute!
                        */

                        
                                    protected float localDeadband ;
                                

                           /**
                           * Auto generated getter method
                           * @return float
                           */
                           public  float getDeadband(){
                               return localDeadband;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Deadband
                               */
                               public void setDeadband(float param){
                            
                                            this.localDeadband=param;
                                    

                               }
                            

                        /**
                        * field for RequestedSamplingRate
                        * This was an Attribute!
                        */

                        
                                    protected int localRequestedSamplingRate ;
                                

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getRequestedSamplingRate(){
                               return localRequestedSamplingRate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RequestedSamplingRate
                               */
                               public void setRequestedSamplingRate(int param){
                            
                                            this.localRequestedSamplingRate=param;
                                    

                               }
                            

                        /**
                        * field for EnableBuffering
                        * This was an Attribute!
                        */

                        
                                    protected boolean localEnableBuffering ;
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getEnableBuffering(){
                               return localEnableBuffering;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param EnableBuffering
                               */
                               public void setEnableBuffering(boolean param){
                            
                                            this.localEnableBuffering=param;
                                    

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
                           namespacePrefix+":SubscribeRequestItemList",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "SubscribeRequestItemList",
                           xmlWriter);
                   }

               
                   }
               
                                            if (localItemPath != null){
                                        
                                                writeAttribute("",
                                                         "ItemPath",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localItemPath), xmlWriter);

                                            
                                      }
                                    
                                            if (localReqType != null){
                                        
                                                writeQNameAttribute("",
                                                         "ReqType",
                                                         localReqType, xmlWriter);

                                            
                                      }
                                    
                                                   if (!java.lang.Float.isNaN(localDeadband)) {
                                               
                                                writeAttribute("",
                                                         "Deadband",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDeadband), xmlWriter);

                                            
                                      }
                                    
                                                   if (localRequestedSamplingRate!=java.lang.Integer.MIN_VALUE) {
                                               
                                                writeAttribute("",
                                                         "RequestedSamplingRate",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRequestedSamplingRate), xmlWriter);

                                            
                                      }
                                    
                                                   if (true) {
                                               
                                                writeAttribute("",
                                                         "EnableBuffering",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEnableBuffering), xmlWriter);

                                            
                                      }
                                     if (localItemsTracker){
                                       if (localItems!=null){
                                            for (int i = 0;i < localItems.length;i++){
                                                if (localItems[i] != null){
                                                 localItems[i].serialize(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Items"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                        // we don't have to do any thing since minOccures is zero
                                                    
                                                }

                                            }
                                     } else {
                                        
                                               throw new org.apache.axis2.databinding.ADBException("Items cannot be null!!");
                                        
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

                 if (localItemsTracker){
                             if (localItems!=null) {
                                 for (int i = 0;i < localItems.length;i++){

                                    if (localItems[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/",
                                                                          "Items"));
                                         elementList.add(localItems[i]);
                                    } else {
                                        
                                                // nothing to do
                                            
                                    }

                                 }
                             } else {
                                 
                                        throw new org.apache.axis2.databinding.ADBException("Items cannot be null!!");
                                    
                             }

                        }
                            attribList.add(
                            new javax.xml.namespace.QName("","ItemPath"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localItemPath));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","ReqType"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localReqType));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","Deadband"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDeadband));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","RequestedSamplingRate"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRequestedSamplingRate));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","EnableBuffering"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEnableBuffering));
                                

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
        public static SubscribeRequestItemList parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            SubscribeRequestItemList object =
                new SubscribeRequestItemList();

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
                    
                            if (!"SubscribeRequestItemList".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (SubscribeRequestItemList)org.opcfoundation.xmlda.ExtensionMapper.getTypeObject(
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
                    
                    // handle attribute "ReqType"
                    java.lang.String tempAttribReqType =
                        
                                reader.getAttributeValue(null,"ReqType");
                            
                   if (tempAttribReqType!=null){
                         java.lang.String content = tempAttribReqType;
                        
                                int index = tempAttribReqType.indexOf(":");
                                if(index > -1){
                                     prefix = tempAttribReqType.substring(0,index);
                                } else {
                                    // i.e this is in default namesace
                                    prefix = "";
                                }
                                namespaceuri = reader.getNamespaceURI(prefix);
                                 
                                         object.setReqType(
                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToQName(tempAttribReqType,namespaceuri));
                                     
                    } else {
                       
                    }
                    handledAttributes.add("ReqType");
                    
                    // handle attribute "Deadband"
                    java.lang.String tempAttribDeadband =
                        
                                reader.getAttributeValue(null,"Deadband");
                            
                   if (tempAttribDeadband!=null){
                         java.lang.String content = tempAttribDeadband;
                        
                                                 object.setDeadband(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToFloat(tempAttribDeadband));
                                            
                    } else {
                       
                                           object.setDeadband(java.lang.Float.NaN);
                                       
                    }
                    handledAttributes.add("Deadband");
                    
                    // handle attribute "RequestedSamplingRate"
                    java.lang.String tempAttribRequestedSamplingRate =
                        
                                reader.getAttributeValue(null,"RequestedSamplingRate");
                            
                   if (tempAttribRequestedSamplingRate!=null){
                         java.lang.String content = tempAttribRequestedSamplingRate;
                        
                                                 object.setRequestedSamplingRate(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(tempAttribRequestedSamplingRate));
                                            
                    } else {
                       
                                           object.setRequestedSamplingRate(java.lang.Integer.MIN_VALUE);
                                       
                    }
                    handledAttributes.add("RequestedSamplingRate");
                    
                    // handle attribute "EnableBuffering"
                    java.lang.String tempAttribEnableBuffering =
                        
                                reader.getAttributeValue(null,"EnableBuffering");
                            
                   if (tempAttribEnableBuffering!=null){
                         java.lang.String content = tempAttribEnableBuffering;
                        
                                                 object.setEnableBuffering(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(tempAttribEnableBuffering));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("EnableBuffering");
                    
                    
                    reader.next();
                
                        java.util.ArrayList list1 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Items").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list1.add(org.opcfoundation.xmlda.SubscribeRequestItem.Factory.parse(reader));
                                                                
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone1 = false;
                                                        while(!loopDone1){
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
                                                                loopDone1 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/","Items").equals(reader.getName())){
                                                                    list1.add(org.opcfoundation.xmlda.SubscribeRequestItem.Factory.parse(reader));
                                                                        
                                                                }else{
                                                                    loopDone1 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setItems((org.opcfoundation.xmlda.SubscribeRequestItem[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.opcfoundation.xmlda.SubscribeRequestItem.class,
                                                                list1));
                                                            
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
           
    