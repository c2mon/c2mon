<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template match="TagInfo">
		<!-- print the html header with css links -->
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
		  <html>
			<head>
				<title>Configuration viewer</title>
<!--				<link rel="stylesheet" type="text/css" href="tim.css" />-->
<!--				<link rel="stylesheet" type="text/css" href="webConfigViewer.css" />-->
				<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/tim.css" />
				<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/webConfigViewer.css" />
				<script type="text/javascript" src="/c2mon-web-configviewer/js/jquery-1.7.min.js"></script>
				<script type="text/javascript" src="/c2mon-web-configviewer/js/bottom_panel.js"></script>

			</head>
			<body>
				<xsl:apply-templates />
			</body>
		</html>
	</xsl:template>
	
	<!--  link variables  -->
	<xsl:variable name="base_url">/c2mon-web-configviewer/</xsl:variable>
	<xsl:variable name="alarm_url">alarmviewer/</xsl:variable>
	<xsl:variable name="datatag_url">tagviewer/</xsl:variable>
	<xsl:variable name="process_xml_url">process/xml/</xsl:variable>


	<!--  leave the paragraphs untouched -->
	<xsl:template match="p">
		<xsl:copy-of select="." />
	</xsl:template>
	
	<xsl:template match="ClientDataTag">
		<p class="tagName"> 
					<xsl:value-of select="tagName"/>
					(<xsl:value-of select="@id"/>)
		</p>
		<div class="columnA">
		<table class="inline">
			<th colspan="2">ClientDataTag</th>
			<tr>
				<td class="bold"> Value </td>
				<td><xsl:value-of select="tagValue"/></td>
			</tr>
			<tr>
				<td class="bold"> Value description </td>
				<td><xsl:value-of select="valueDescription"/></td>
			</tr>
			<tr>
				<td class="bold"> Source timestamp </td>
				<td><xsl:value-of select="sourceTimestamp"/></td>
			</tr>
			<tr>
				<td class="bold"> Server timestamp </td>
				<td><xsl:value-of select="serverTimestamp"/></td>
			</tr>
			<tr>
				<td class="bold"> Quality code </td>
				<td style="background:red;"><xsl:value-of select="'missing in XML'"/></td>
			</tr>
			<tr>
				<td class="bold"> Quality description </td>
				<td style="background:red;"><xsl:value-of select="'missing in XML'"/></td>
			</tr>
			<tr>
				<td class="bold"> Simulated </td>
				<td><xsl:value-of select="simulated"/></td>
			</tr>
		</table>
		</div>
	</xsl:template>

	<!--  process the XML element TagConfig - take missing information from the element ClientDataTag -->
	<xsl:template match="TagConfig">
		<div class="columnA">
		<table class="inline">
			<th colspan="2">Tag Configuration</th>
			<tr>
				<td class="bold">Tag id </td>
				<td><xsl:value-of select="@id"/></td>
			</tr>
			<tr>
				<td class="bold">Tag name </td>
				<td><xsl:value-of select="../ClientDataTag/tagName"/></td>
			</tr>
			<tr>
				<td class="bold">Description </td>
				<td><xsl:value-of select="../ClientDataTag/description"/></td>
			</tr>
			<tr>
				<td class="bold">Control tag </td>
				<td><xsl:value-of select="controlTag"/></td>
			</tr>
			<tr>
				<td class="bold"> Unit </td>
				<td style="background:red;"><xsl:value-of select="'missing in XML'"/></td>
			</tr>
			<tr>
				<td class="bold"> Mode </td>
				<td><xsl:value-of select="../ClientDataTag/mode"/></td>
			</tr>
			<tr>
				<td class="bold"> Min value</td>
				<td><xsl:value-of select="minValue"/></td>
			</tr>
			<tr>
				<td class="bold"> Max value</td>
				<td><xsl:value-of select="maxValue"/></td>
			</tr>
			<tr>
				<td class="bold"> JMS Topic name </td>
				<td><xsl:value-of select="../ClientDataTag/topicName"/></td>
			</tr>
			<tr>
				<td class="bold"> Data Type </td>
				<td><xsl:value-of select="../ClientDataTag/tagValue/@class"/></td>
			</tr>
			<tr>
				<td class="bold"> Value dictionary </td>
				<td style="background:red;"><xsl:value-of select="'missing in XML'"/></td>
			</tr>
			<tr>
				<td class="bold"> Alarms </td>
				<td>
					 <xsl:for-each select="alarms/alarmValue">
							<a href="{$base_url}{$alarm_url}{faultCode}/"><xsl:value-of select="faultCode"/></a>&#160;
					</xsl:for-each>
				</td>
			</tr>
			<tr>
				<td class="bold"> Rules </td>
				<td><xsl:value-of select="ruleIds"/></td>
			</tr>
			<tr>
				<td class="bold" style="background:red;"> Publications - no JAPC and DIP</td>
				<td><xsl:value-of select="publications"/></td>
			</tr>
			
		</table>
	</div>
	<!--  address configuration -->
	<div class="columnB">
		<table class="inline">
			<th colspan="2">Address Configuration</th>
			<tr>
				<td class="bold"> JMS Priority </td>
				<td><xsl:value-of select="priority"/></td>
			</tr>
			<tr style="background:green;">
				<td class="bold"> Guaranteed delivery - NEW </td>
				<td><xsl:value-of select="guaranteedDelivery"/></td>
			</tr>
		
			<tr>
				<td class="bold"> Time deadband (ms) </td>
				<td><xsl:value-of select="timeDeadband"/></td>
			</tr>
			<tr>
				<td class="bold"> Time to Live (ms)  </td>
				<td style="background:red;"><xsl:value-of select="'missing in XML'"/></td>
			</tr>
			<tr>
				<td class="bold"> Value deadband </td>
				<td><xsl:value-of select="valueDeadband"/></td>
			</tr>
			<tr>
				<td class="bold"> Value deadband type </td>
				<td><xsl:value-of select="valueDeadbandType"/></td>
			</tr>
			<tr>
				<td class="bold"> Hardware address </td>
				<td><pre><xsl:value-of select="hardwareAddress"/></pre></td>
			</tr>

		</table>
		</div>
	
	</xsl:template>

	<!-- process the XML element AlarmValue -->
	<xsl:template match="AlarmValue">
		<p class="tagName"> 
					<xsl:value-of select="faultFamily"/>
					:<xsl:value-of select="faultMemeber"/>
					:<xsl:value-of select="faultCode"/>
		</p>
		<div class="column">
		<table class="inline">
			<th colspan="2">Alarm Value</th>
			<tr>
				<td class="bold"> State </td>
				<td>
					<xsl:choose>
						<xsl:when test="active='false'">
							<xsl:text>TERMINATED</xsl:text>
						</xsl:when>
						<xsl:when test="active='true'">
							<xsl:text>ACTIVE</xsl:text>
						</xsl:when>
					</xsl:choose>
				</td>
			</tr>
			<tr>
				<td class="bold"> Timestamp of last change </td>
				<td><xsl:value-of select="timestamp"/></td>
			</tr>
			<tr>
				<td class="bold"> Info </td>
				<td><xsl:value-of select="info"/></td>
			</tr>
			
		</table>
	</div>
	<div class="column">
		<table class="inline">
			<th colspan="2">Alarm Configuration</th>
			<tr>
				<td class="bold"> Alarm id </td>
				<td><xsl:value-of select="@id"/></td>
			</tr>
			<tr>
				<td class="bold"> Fault Family </td>
				<td><xsl:value-of select="faultFamily"/></td>
			</tr>
			<tr>
				<td class="bold"> Fault member </td>
				<td><xsl:value-of select="faultMemeber"/></td>
			</tr>
			<tr>
				<td class="bold"> Fault code </td>
				<td><xsl:value-of select="faultCode"/></td>
			</tr>
			<tr>
				<td class="bold"> Problem description </td>
				<td style="background:red;"><xsl:value-of select="'missing in XML'"/></td>
			</tr>
			<tr>
				<td class="bold"> DataTag </td>
				<td>
					<a href="{$base_url}{$datatag_url}{tagId}/"><xsl:value-of select="tagId"/></a>
				</td>
			</tr>
			<tr>
				<td class="bold"> Activation condition </td>
				<td style="background:red;"><xsl:value-of select="'missing in XML'"/></td>
			</tr>
		</table>
		</div>
	</xsl:template>
	
	
	
	<!-- process the XML element ClientCommandTagImpl -->
	<xsl:template match="clientCommandTagImpl">
		<p class="tagName"> 
					<xsl:value-of select="name"/>
					(<xsl:value-of select="@id"/>)
		</p>
	<div class="column">
		<table class="inline">
			<th colspan="2">Command Tag</th>
			<tr>
				<td class="bold"> Command id </td>
				<td><xsl:value-of select="@id"/></td>
			</tr>
			<tr>
				<td class="bold"> Name </td>
				<td><xsl:value-of select="name"/></td>
			</tr>
			<tr>
				<td class="bold"> Description </td>
				<td><xsl:value-of select="description"/></td>
			</tr>
			<tr>
				<td class="bold"> ValueType </td>
				<td><xsl:value-of select="valueType"/></td>
			</tr>
			<tr>
				<td class="bold"> ClientTimeout </td>
				<td><xsl:value-of select="clientTimeout"/></td>
			</tr>
		</table>
		</div>
	</xsl:template>
	
	<!-- process the XML element ConfigurationReport -->
	<xsl:template match="ConfigurationReport">
     <p>
     <h2>Overview</h2>
     <table class="inline" border="1">
       <tr>
         <th>Identifier</th>
         <td><xsl:value-of select="id" /></td>
       </tr>
       <tr>
         <th>Name</th>
         <td><xsl:value-of select="name" /></td>
       </tr>
       <tr>
         <th>Applied by</th>
         <td><xsl:value-of select="user" /></td>
       </tr>
       <tr>
         <th>Applied on</th>
         <td><xsl:value-of select="timestamp" /></td>
       </tr>
       <tr>
         <th>Status</th>
         <xsl:choose>
         <xsl:when test="status='OK'">
           <td bgcolor="green"><xsl:value-of select="status" /></td>
         </xsl:when>  
         <xsl:when test="status='WARNING' or status='RESTART'">
           <td bgcolor="yellow"><xsl:value-of select="status" /></td>
         </xsl:when>  
         <xsl:otherwise>
           <td bgcolor="red"><xsl:value-of select="status" /></td>
         </xsl:otherwise>
         </xsl:choose>
       </tr>
       <tr>
         <th>Message</th>
         <td><pre><xsl:value-of select="status-description" /></pre></td>
       </tr>
       <tr>
         <th>DAQs to reboot</th>
         <td><xsl:value-of select="daq-reboot" /></td>
       </tr>
     </table>
     </p>
     <xsl:apply-templates select="ConfigurationElementReports"/>
</xsl:template>


<xsl:template match="ConfigurationElementReports">
     <hr/>
     <p>
     <h2>Detailed Report</h2>
     <table class="inline" border="1">
       <tr>
         <th>Action</th>
         <th>Entity</th>
         <th>Id</th>
         <th>Status</th>
         <th>Report</th>
       </tr>
       <xsl:for-each select="ConfigurationElementReport">
         <tr>
         <td><xsl:value-of select="action" /></td>
         <td><xsl:value-of select="entity" /></td>
         <td><xsl:value-of select="id" /></td>
         <xsl:choose>
         <xsl:when test="status='OK'">
           <td bgcolor="green"><xsl:value-of select="status" /></td>
         </xsl:when>  
         <xsl:when test="status='WARNING'">
           <td bgcolor="yellow"><xsl:value-of select="status" /></td>
         </xsl:when>  
         <xsl:otherwise>
           <td bgcolor="red"><xsl:value-of select="status" /></td>
         </xsl:otherwise>
         </xsl:choose>

         <td>
	   <pre><xsl:value-of select="status-message" /></pre>
           <xsl:apply-templates select="sub-reports"/>
	 </td>
         </tr>
       </xsl:for-each>
     </table>
     </p>
</xsl:template>

<xsl:template match="sub-reports">
    <ul>   
         <xsl:for-each select="ConfigurationElementReport">
	   <li>
	     <xsl:value-of select="action" /> -  
	     <xsl:value-of select="entity" /> - 
	     <xsl:value-of select="id" /> -  
	     <xsl:value-of select="status"/> - 
		 
         <xsl:if test="status-message!=''">
	       <pre>
    	       <xsl:value-of select="status-message"/>
	       </pre>
	     </xsl:if>  
	     
	   </li>
	   <xsl:apply-templates select="sub-reports"/>
	 </xsl:for-each>
    </ul>	 
</xsl:template>


	<!-- process the XML element ProcessConfiguration -->
	<xsl:template match="ProcessConfiguration">
		<div id="top"></div>
		<p class="tagName"> 
					DAQ Process XML Viewer 
					<br></br><br></br>
					<p>Process: <xsl:value-of select="jms-user"/>
					<a href="{$base_url}{$process_xml_url}{jms-user}/">(view as xml)</a></p>
		</p>
	<div class="column">
		<table class="inline">
			<th colspan="2">Process Configuration</th>
			<tr>
				<td class="bold"> process id </td>
				<td><xsl:value-of select="@process-id"/></td>
			</tr>
			<tr>
				<td class="bold"> type </td>
				<td><xsl:value-of select="@type"/></td>
			</tr>
			<tr>
				<td class="bold"> jms-user </td>
				<td><xsl:value-of select="jms-user"/></td>
			</tr>
			<tr>
				<td class="bold"> jms-password </td>
				<td><xsl:value-of select="jms-password"/></td>
			</tr>
			<tr>
				<td class="bold"> jms-qcf-jndi-name </td>
				<td><xsl:value-of select="jms-qcf-jndi-name"/></td>
			</tr>
			<tr>
				<td class="bold"> jms-listener-topic </td>
				<td><xsl:value-of select="jms-listener-topic"/></td>
			</tr>
						<tr>
				<td class="bold"> alive-tag-id </td>
				<td><xsl:value-of select="alive-tag-id"/></td>
			</tr>
			<tr>
				<td class="bold"> alive-interval </td>
				<td><xsl:value-of select="alive-interval"/></td>
			</tr>
			<tr>
				<td class="bold"> max-message-size </td>
				<td><xsl:value-of select="max-message-size"/></td>
			</tr>
			<tr>
				<td class="bold"> max-message-delay </td>
				<td><xsl:value-of select="max-message-delay"/></td>
			</tr>
		</table>
		</div>
		<xsl:apply-templates select="EquipmentUnits"/>
	</xsl:template>
	
<xsl:template match="EquipmentUnits">

	<div class="message"> Scroll to <a href="#top"> top</a>, EquipmentUnit:
	
	<xsl:for-each select="EquipmentUnit">
	
	   <a href="#{@name}"> <xsl:value-of select="@name"/> </a>
	   
    </xsl:for-each>
	
	 </div>
	
		<xsl:apply-templates select="EquipmentUnit"/>
</xsl:template>

	
<xsl:template match="EquipmentUnit">
		<p class="tagName"> 
			<a name="{@name}"> <xsl:value-of select="@name"/> </a> : (<xsl:value-of select="@id"/>)
		</p>
	<div class="column">
		<table class="inline">
			<th colspan="2">EquipmentUnit</th>
			<tr>
				<td class="bold">  id </td>
				<td><xsl:value-of select="@id"/></td>
			</tr>
			<tr>
				<td class="bold"> name </td>
				<td><xsl:value-of select="@name"/></td>
			</tr>
			<tr>
				<td class="bold"> handler-class-name </td>
				<td><xsl:value-of select="handler-class-name"/></td>
			</tr>
			<tr>
				<td class="bold"> commfault-tag-id </td>
				<td><xsl:value-of select="commfault-tag-id"/></td>
			</tr>
			<tr>
				<td class="bold"> commfault-tag-value </td>
				<td><xsl:value-of select="commfault-tag-value"/></td>
			</tr>
			<tr>
				<td class="bold"> alive-tag-id </td>
				<td><xsl:value-of select="alive-tag-id"/></td>
			</tr>
			<tr>
				<td class="bold"> alive-interval </td>
				<td><xsl:value-of select="alive-interval"/></td>
			</tr>	
			<tr>
				<td class="bold"> address </td>
				<td><xsl:value-of select="address"/></td>
			</tr>	
		</table>
		</div>
		<xsl:apply-templates select="DataTags"/>
</xsl:template>

<xsl:template match="DataTags">
		<xsl:apply-templates select="DataTag"/>
</xsl:template>

<xsl:template match="DataTag">
		<p class="tagName"> 
					<a href="{$base_url}{$datatag_url}{@id}/"><xsl:value-of select="@name"/>:(<xsl:value-of select="@id"/>)</a>&#160;
		</p>
		<p>
					This DataTag belongs to Equipment 
					<a href="#{../../@name}"><xsl:value-of select="../../@name" /></a> 
		</p>
	<div class="column">
		<table class="inline">
			<th colspan="2">DataTag</th>
			<tr>
				<td class="bold">  id </td>
				<td><xsl:value-of select="@id"/></td>
			</tr>
			<tr>
				<td class="bold">  name </td>
				<td><xsl:value-of select="@name"/></td>
			</tr>
						<tr>
				<td class="bold">  control </td>
				<td><xsl:value-of select="@control"/></td>
			</tr>
			<tr>
				<td class="bold"> data-type </td>
				<td><xsl:value-of select="data-type"/></td>
			</tr>
			
			<!--  not all data-types need min and max value fields -->
			<xsl:if test="min-value != ''">
			<tr>
				<td class="bold"> min-value </td>
				<td><xsl:value-of select="min-value"/></td>
			</tr>
			</xsl:if>
			
			<xsl:if test="max-value != ''">
			<tr>
				<td class="bold"> max-value </td>
				<td><xsl:value-of select="max-value"/></td>
			</tr>
			</xsl:if>
			
		</table>
		<xsl:apply-templates select="DataTagAddress"/>
		</div>
</xsl:template>

<xsl:template match="DataTagAddress">
	<p class="tagName"></p>
	<div class="column">
		<table class="inline">
			<th colspan="2">DataTagAddress</th>
			<tr>
				<td class="bold"> time-to-live </td>
				<td><xsl:value-of select="time-to-live"/></td>
			</tr>
			
			<xsl:if test="value-deadband-type!=''">
			<tr>
				<td class="bold"> value-deadband-type </td>
				<td><xsl:value-of select="value-deadband-type"/></td>
			</tr>
			</xsl:if>  
			
			<xsl:if test="value-deadband!=''">
			<tr>
				<td class="bold"> value-deadband </td>
				<td><xsl:value-of select="value-deadband"/></td>
			</tr>
			</xsl:if>  
			
			<tr>
				<td class="bold"> priority </td>
				<td><xsl:value-of select="priority"/></td>
			</tr>
			<tr>
				<td class="bold"> guaranteed-delivery </td>
				<td><xsl:value-of select="guaranteed-delivery"/></td>
			</tr>
			
		</table>
		<xsl:apply-templates select="HardwareAddress"/>
		</div>
</xsl:template>

		
<xsl:template match="HardwareAddress">
	<p class="tagName"></p>
	<div class="column">
		<table class="inline">
			<th colspan="2">HardwareAddress</th>
			<tr>
				<td class="bold"> class </td>
				<td><xsl:value-of select="@class"/></td>
			</tr>
			
			<xsl:if test="namespace!=''">
			<tr>
				<td class="bold"> namespace </td>
				<td><xsl:value-of select="namespace"/></td>
			</tr>
			</xsl:if>  
			
			<xsl:if test="opc-item-name!=''">
			<tr>
				<td class="bold"> opc-item-name </td>
				<td><xsl:value-of select="opc-item-name"/></td>
			</tr>
			</xsl:if>  
			
			<xsl:if test="command-pulse-length!=''">
			<tr>
				<td class="bold"> command-pulse-length </td>
				<td><xsl:value-of select="command-pulse-length"/></td>
			</tr>
			</xsl:if>  
			
			<xsl:if test="address-type!=''">
			<tr>
				<td class="bold"> address-type </td>
				<td><xsl:value-of select="address-type"/></td>
			</tr>
			</xsl:if>  
			
			<xsl:if test="command-type!=''">
			<tr>
				<td class="bold"> command-type </td>
				<td><xsl:value-of select="command-type"/></td>
			</tr>			
			</xsl:if>  
			
			<xsl:if test="fault-family!=''">
			<tr>
				<td class="bold"> fault-family </td>
				<td><xsl:value-of select="fault-family"/></td>
			</tr>			
			</xsl:if>  
			
			<xsl:if test="fault-member!=''">
			<tr>
				<td class="bold"> fault-member </td>
				<td><xsl:value-of select="fault-member"/></td>
			</tr>			
			</xsl:if>  
			
			<xsl:if test="fault-code!=''">
			<tr>
				<td class="bold">fault-code</td>
				<td><xsl:value-of select="fault-code"/></td>
			</tr>			
			</xsl:if>  
			  
			<xsl:if test="item-name!=''">
			<tr>
				<td class="bold"> item-name </td>
				<td><xsl:value-of select="item-name"/></td>
			</tr>
			</xsl:if>  
			
			<xsl:if test="field-name!=''">
			<tr>
				<td class="bold"> field-name </td>
				<td><xsl:value-of select="field-name"/></td>
			</tr>
			</xsl:if>  
			
			<xsl:if test="field-index!=''">
			<tr>
				<td class="bold"> field-index </td>
				<td><xsl:value-of select="field-index"/></td>
			</tr>
			</xsl:if>  
			
			<xsl:if test="block-type!=''">
			<tr>
				<td class="bold">block-type</td>
				<td><xsl:value-of select="block-type"/></td>
			</tr>
			</xsl:if>  			
			
			<xsl:if test="word-id!=''">
			<tr>
				<td class="bold">word-id</td>
				<td><xsl:value-of select="word-id"/></td>
			</tr>
			</xsl:if>  	
			
			
			<xsl:if test="bit-id!=''">
			<tr>
				<td class="bold">bit-id</td>
				<td><xsl:value-of select="bit-id"/></td>
			</tr>
			</xsl:if>  	
			
			
			<xsl:if test="physical-min-val!=''">
			<tr>
				<td class="bold">physical-min-val</td>
				<td><xsl:value-of select="physical-min-val"/></td>
			</tr>
			</xsl:if>  	
			
			
			<xsl:if test="physical-max-val!=''">
			<tr>
				<td class="bold">physical-max-val</td>
				<td><xsl:value-of select="physical-max-val"/></td>
			</tr>
			</xsl:if>  	
			
			
			<xsl:if test="resolution-factor!=''">
			<tr>
				<td class="bold">resolution-factor</td>
				<td><xsl:value-of select="resolution-factor"/></td>
			</tr>
			</xsl:if>  	
			
			
			<xsl:if test="native-address!=''">
			<tr>
				<td class="bold">native-address</td>
				<td><xsl:value-of select="native-address"/></td>
			</tr>
			</xsl:if>  	
			
		</table>
		</div>
</xsl:template>		

</xsl:stylesheet>