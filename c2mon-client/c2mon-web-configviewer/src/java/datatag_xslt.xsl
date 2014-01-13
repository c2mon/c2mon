<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!--  link variables  -->
	<xsl:variable name="base_url">
		/c2mon-web-configviewer/
	</xsl:variable>
	<xsl:variable name="alarm_url">
		alarmviewer/
	</xsl:variable>
	<xsl:variable name="command_url">
		commandviewer/
	</xsl:variable>
	<xsl:variable name="datatag_url">
		tagviewer/
	</xsl:variable>
	<xsl:variable name="history_url">
		historyviewer/
	</xsl:variable>		
	<xsl:variable name="trend_viewer_url">
		trendviewer/
	</xsl:variable>		
	<xsl:variable name="process_xml_url">
		process/xml/
	</xsl:variable>
	<xsl:variable name="alarm_xml_url">
		alarmviewer/xml/
	</xsl:variable>
	<xsl:variable name="tag_value_xml_url">
		tagvalue/xml/
	</xsl:variable>	
	<xsl:variable name="tag_config_xml_url">
		tagconfig/xml/
	</xsl:variable>		
	
	<xsl:variable name="help_alarm_url">http://oraweb.cern.ch/pls/timw3/helpalarm.AlarmForm?p_alarmid=</xsl:variable>
	<xsl:variable name="help_point_url">https://oraweb.cern.ch/pls/timw3/helpalarm.AlarmList?p_pointid1=</xsl:variable>
	
	<!--  leave the paragraphs untouched -->
	<xsl:template match="p">
		<xsl:copy-of select="." />
	</xsl:template>

	<xsl:template match="ClientDataTag">
		<p class="tagName">
			<xsl:value-of select="tagName" />
			(
			<xsl:value-of select="@id" />
			)
			<br></br>
			<br></br>
			
			<A href="{$base_url}{$tag_value_xml_url}{@id}/" 
				class="large blue awesome xml_button" target="_blank">View ClientDataTag XML >>
			</A>
			<A href="{$base_url}{$history_url}{@id}/" 
				class="large blue awesome xml_button" target="_blank">History >>
			</A>	
			<A href="{$base_url}{$trend_viewer_url}{/*/@id}/" 
				class="large blue awesome xml_button" target="_blank">Trend >>
			</A>	
			<A href="{$help_point_url}{@id}" 
				class="large red awesome xml_button" target="_blank">View Help Alarm >>
			</A>	
		</p>
		<table class="inline">
			<th colspan="4">ClientDataTag</th>

			<tr>
				<td class="highlight bold">Tag id </td>
				<td>
					<xsl:value-of select="@id" />
				</td>
				<td class="highlight bold">Source Timestamp</td>
				<td><xsl:value-of select="sourceTimestamp"/></td>
			</tr>
			<tr>
				<td class="highlight bold">Tag Quality</td>
				<td>
					<xsl:choose>
						<xsl:when test="tagQuality/isValid = 'true'">
        			OK
        		</xsl:when>
        		<xsl:otherwise>
        			INVALID
    				</xsl:otherwise>
        	</xsl:choose>
				</td>
				<td class="highlight bold">Server Timestamp</td>
				<td><xsl:value-of select="serverTimestamp"/></td>
			</tr>		
			
			<xsl:for-each
				select="*[not(local-name() = 'alarms' or local-name() = 'tagQuality'
					or local-name() = 'sourceTimestamp' or local-name() = 'serverTimestamp')]">

				<xsl:if test="position() mod 2 = 1">
					<xsl:text disable-output-escaping='yes'>&lt;TR></xsl:text>
					<TD class="highlight bold"><xsl:value-of select="local-name()"/></TD>
					<TD width="25%"><xsl:value-of select="."/></TD>
				</xsl:if>
				
				<xsl:if  test="position() mod 2 = 0">
					<TD class="highlight bold"><xsl:value-of select="local-name()"/></TD>
					<TD width="25%"><xsl:value-of select="."/></TD>		
					<xsl:text disable-output-escaping='yes'>&lt;/TR></xsl:text>
				</xsl:if>
				
			</xsl:for-each>
		
		</table>
		<xsl:apply-templates select="alarms"/>
		<xsl:apply-templates select="tagQuality"/>
	</xsl:template>
	
	<xsl:template match="tagQuality">
		<xsl:apply-templates select="invalidQualityStates"/>
	</xsl:template>
	
		<!--  process the XML element invalidQualityStates - entry  -->
	<xsl:template match="invalidQualityStates">
		<xsl:if test="entry!=''">
		<p>
		</p>	
		<table class="inline">
		
			<th colspan="4">Reason for tag invalidation</th>
			
			<xsl:for-each
				select="entry">
			<tr>
				<td class="highlight bold">Quality Status</td>
				<td width="25%"><xsl:value-of select="tagQualityStatus"/></td>
				<td class="highlight bold">Description</td>
				<td width="25%"><xsl:value-of select="string"/></td>
			</tr>
			
			<xsl:for-each select="*[not(local-name() = 'tagQualityStatus' or local-name() = 'string')]">

				<xsl:if test="position() mod 2 = 1">
					<xsl:text disable-output-escaping='yes'>&lt;TR></xsl:text>
					<TD class="highlight bold"><xsl:value-of select="local-name()"/></TD>
					<TD width="25%"><xsl:value-of select="."/></TD>
				</xsl:if>
				
				<xsl:if  test="position() mod 2 = 0">
					<TD class="highlight bold"><xsl:value-of select="local-name()"/></TD>
					<TD width="25%"><xsl:value-of select="."/></TD>		
					<xsl:text disable-output-escaping='yes'>&lt;/TR></xsl:text>
				</xsl:if>
				
			</xsl:for-each>
			</xsl:for-each>
		</table>
		</xsl:if>
	
	</xsl:template>
	
	

	<!--  process the XML element TagConfig - take missing information from the element ClientDataTag -->
	<xsl:template match="TagConfig">
		
		<p>
			<A href="{$base_url}{$tag_config_xml_url}{@id}/" 
				class="large blue awesome xml_button" target="_blank">View TagConfig XML >>
			</A>	
		</p>	
		<table class="inline">
			<th colspan="4">Tag Configuration</th>
			<tr>
				<td class="highlight bold">Tag id </td>
				<td><xsl:value-of select="@id"/></td>
				<td class="highlight bold">Tag name </td>
				<td><xsl:value-of select="../ClientDataTag/tagName"/></td>
			</tr>
			<tr>
				<td class="highlight bold">Description </td>
				<td><xsl:value-of select="../ClientDataTag/description"/></td>
				<td class="highlight bold"> Mode </td>
				<td><xsl:value-of select="../ClientDataTag/mode"/></td>
			</tr>
			<tr>
				<td class="highlight bold"> JMS Topic name </td>
				<td><xsl:value-of select="../ClientDataTag/topicName"/></td>
				<td class="highlight bold"> Data Type </td>
				<td><xsl:value-of select="../ClientDataTag/tagValue/@class"/></td>
			</tr>
			
			<xsl:for-each select="*[not(local-name() = 'alarmIds' or local-name() = 'hardwareAddress')]">

				<xsl:if test="position() mod 2 = 1">
					<xsl:text disable-output-escaping='yes'>&lt;TR></xsl:text>
					<TD class="highlight bold"><xsl:value-of select="local-name()"/></TD>
					<TD width="25%"><xsl:value-of select="."/></TD>
				</xsl:if>
				
				<xsl:if  test="position() mod 2 = 0">
					<TD class="highlight bold"><xsl:value-of select="local-name()"/></TD>
					<TD width="25%"><xsl:value-of select="."/></TD>		
					<xsl:text disable-output-escaping='yes'>&lt;/TR></xsl:text>
				</xsl:if>
				
			</xsl:for-each>
			
			<tr>
				<td class="highlight bold"> Alarms </td>
				<td>
					 <xsl:for-each select="alarmIds">
							<a href="{$base_url}{$alarm_url}{long}/"><xsl:value-of select="long"/></a>
					</xsl:for-each>
				</td>
			</tr>
			
			<tr>
				<td class="highlight bold align_center" colspan="4" >Hardware Address</td>
			</tr>
			<tr>
				<td colspan="4"><xsl:value-of select="hardwareAddress"/></td>
			</tr>
			
			<!--
			<tr>
				<td class="highlight bold" style="background:red;"> Publications - no JAPC and DIP</td>
				<td><xsl:value-of select="publications"/></td>
			</tr>
			-->
			
		</table>
	
	</xsl:template>

	<!-- process the XML element AlarmValue -->
	
	
	<xsl:template match="AlarmValue | alarmValue">
	
			
		<p class="tagName"> 
					<xsl:value-of select="faultFamily"/>
					:<xsl:value-of select="faultMemeber"/>
					:<xsl:value-of select="faultCode"/>
					
			<A href="{$base_url}{$alarm_xml_url}{@id}/" 
				class="large blue awesome xml_button" target="_blank">View Alarm XML >>
			</A>
			
			<A href="{$help_alarm_url}{@id}" 
				class="large red awesome xml_button" target="_blank">View Help Alarm >>
			</A>	
		</p>
		<table class="inline">
			<th colspan="4">Alarm Value</th>
			
			<tr>
				<td class="highlight bold">Alarm id</td>
				<td><xsl:value-of select="@id"/></td>	
				<td class="highlight bold">Class</td>
				<td><xsl:value-of select="@class"/></td>	
			</tr>
			
			<tr>
				<td class="highlight bold"> DataTag </td>
				<td >
					<a href="{$base_url}{$datatag_url}{tagId}/"><xsl:value-of select="tagId"/></a>
				</td>
				<td class="highlight bold"> State </td>
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
			
			<xsl:for-each select="*[not((local-name() = 'tagId') or local-name() = 'active')]">
				
				<xsl:if  test="position() mod 2 = 0">
					<TD class="highlight bold"><xsl:value-of select="local-name()"/></TD>
					<TD width="25%"><xsl:value-of select="."/></TD>		
					<xsl:text disable-output-escaping='yes'>&lt;/TR></xsl:text>
				</xsl:if>
				
				<xsl:if test="position() mod 2 = 1">
					<xsl:text disable-output-escaping='yes'>&lt;TR></xsl:text>
					<TD class="highlight bold"><xsl:value-of select="local-name()"/></TD>
					<TD width="25%"><xsl:value-of select="."/></TD>
				</xsl:if>	
				
			</xsl:for-each>
			
		</table>
	</xsl:template>
	
	
	<!-- process the XML element ClientCommandTagImpl -->
	<xsl:template match="clientCommandTagImpl">
	
			<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
		  <html>
			<head>
				<title>Configuration viewer</title>
				<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/tim.css"></link>
				<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/webConfigViewer.css"></link>
				<script type="text/javascript" src="/c2mon-web-configviewer/js/jquery-1.7.min.js"></script>
				<script type="text/javascript" src="/c2mon-web-configviewer/js/bottom_panel.js"></script>
			</head>
			<body>
	
		<p class="tagName"> 
					<xsl:value-of select="name"/>
					(<xsl:value-of select="@id"/>)
		</p>
	<div class="column">
		<table class="inline">
			<th colspan="2">Command Tag</th>
			
			<tr>
				<td class="highlight bold"> Command id </td>
				<td class=""><xsl:value-of select="@id"/></td>
			</tr>

			<xsl:for-each select="*">
				<TR>
					<TD class="highlight bold"><xsl:value-of select="local-name()"/></TD>
					<TD><xsl:value-of select="."/></TD>
				</TR>
			</xsl:for-each>
			
		</table>
		</div>
		
		</body>
		</html>
	</xsl:template>
	
	<!-- process the XML element ConfigurationReport -->
	<xsl:template match="ConfigurationReport">
	
			<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
		  <html>
			<head>
				<title>Configuration viewer</title>
				<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/tim.css"></link>
				<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/webConfigViewer.css"></link>
				<script type="text/javascript" src="/c2mon-web-configviewer/js/jquery-1.7.min.js"></script>
				<script type="text/javascript" src="/c2mon-web-configviewer/js/bottom_panel.js"></script>
			</head>
			<body>
	
	
     <p>
     <h2>Overview</h2>
     <table class="inline" border="1">
       <tr>
         <th class="bold">Identifier</th>
         <td><xsl:value-of select="id" /></td>
       </tr>
       <tr>
         <th class="bold">Name</th>
         <td><xsl:value-of select="name" /></td>
       </tr>
       <tr>
         <th class="bold">Applied by</th>
         <td><xsl:value-of select="user" /></td>
       </tr>
       <tr>
         <th class="bold">Applied on</th>
         <td><xsl:value-of select="timestamp" /></td>
       </tr>
       <tr>
         <th class="bold">Status</th>
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
         <th class="highlight bold">Message</th>
         <td><pre><xsl:value-of select="status-description" /></pre></td>
       </tr>
       <tr>
         <th class="highlight bold">DAQs to reboot</th>
         <td><xsl:value-of select="daq-reboot" /></td>
       </tr>
     </table>
     </p>
     <xsl:apply-templates select="ConfigurationElementReports"/>
     
     </body>
		</html>
</xsl:template>


<xsl:template match="ConfigurationElementReports">
     <hr/>
     <p>
     <h2>Detailed Report</h2>
     <table class="inline" border="1">
       <tr>
         <th class="bold">Action</th>
         <th class="bold">Entity</th>
         <th class="bold">Id</th>
         <th class="bold">Status</th>
         <th class="bold">Report</th>
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
	
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html></xsl:text>
		  <html>
			<head>
				<title>Configuration viewer</title>
				<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/tim.css"></link>
				<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/webConfigViewer.css"></link>
				<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/buttons.css"></link>
				
				<script type="text/javascript" src="/c2mon-web-configviewer/js/jquery-1.7.min.js"></script>
				<script type="text/javascript" src="/c2mon-web-configviewer/js/bottom_panel.js"></script>
				
			</head>
			<body>
	
		<div id="top"></div>
		<p class="tagName"> 
					DAQ Process XML Viewer 
					<br></br><br></br>
					<p>Process: <xsl:value-of select="jms-user"/>
					<A href="{$base_url}{$process_xml_url}{jms-user}/" 
						class="large blue awesome xml_button" target="_blank">View as XML >>
						</A>
					</p>
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

		<xsl:for-each select="*[not(local-name() = 'EquipmentUnits')]">
		<TR>
			<TD class="bold"><xsl:value-of select="local-name()"/></TD>
			<TD><xsl:value-of select="."/></TD>
		</TR>
        </xsl:for-each>
		
		</table>
		</div>
		<xsl:apply-templates select="EquipmentUnits"/>
		
			</body>
		</html>
	</xsl:template>
	
<xsl:template match="EquipmentUnits">

	<div class="message"> Scroll to <a href="#top"> top</a></div>
	
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

		<xsl:for-each select="*[not(local-name() = 'DataTags')]">
		<TR>
			<TD class="bold"><xsl:value-of select="local-name()"/></TD>
			<TD><xsl:value-of select="."/></TD>
		</TR>
        </xsl:for-each>
			
		</table>
		</div>
		<xsl:apply-templates select="DataTags"/>
		<xsl:apply-templates select="CommandTags"/>
</xsl:template>

<xsl:template match="CommandTags">
		<xsl:apply-templates select="CommandTag"/>
</xsl:template>

<xsl:template match="CommandTag">
		<p class="tagName"> 
					<a href="{$base_url}{$command_url}{@id}/"><xsl:value-of select="@name"/>:(<xsl:value-of select="@id"/>)</a>&#160;
		</p>
		<p>
					This CommandTag belongs to Equipment 
					<a href="#{../../@name}"><xsl:value-of select="../../@name" /></a> 
		</p>
	<div class="column">
		<table class="inline">
			<th colspan="2">CommandTag</th>
			
		<tr>
			<td class="bold">  id </td>
			<td><xsl:value-of select="@id"/></td>
		</tr>
		<tr>
			<td class="bold">  name </td>
			<td><xsl:value-of select="@name"/></td>
		</tr>

		<xsl:for-each select="*">
		
		<TR>
			<TD class="bold"><xsl:value-of select="local-name()"/></TD>
			<TD><xsl:value-of select="."/></TD>
		</TR>
        </xsl:for-each>
		
		</table>
		<xsl:apply-templates select="HardwareAddress"/>
		</div>
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

		<xsl:for-each select="*[not(local-name() = 'DataTagAddress')]">
		<TR>
			<TD class="bold"><xsl:value-of select="local-name()"/></TD>
			<TD><xsl:value-of select="."/></TD>
		</TR>
        </xsl:for-each>
			
		</table>
		<xsl:apply-templates select="DataTagAddress"/>
		</div>
</xsl:template>

<xsl:template match="DataTagAddress">
	<p class="tagName"></p>
	<div class="column">
		<table class="inline">
			<th colspan="2">DataTagAddress</th>

		<xsl:for-each select="*[not(local-name() = 'HardwareAddress')]">
		
		<TR>
			<TD class="bold"><xsl:value-of select="local-name()"/></TD>
			<TD><xsl:value-of select="."/></TD>
		</TR>
        </xsl:for-each>
		</table>
		
		<xsl:apply-templates select="HardwareAddress"/>
		</div>
		
</xsl:template>

<xsl:template match="HardwareAddress">
	<p class="tagName"></p>
	<div class="column">
		<table class="inline">
<th colspan="2">HardwareAddress</th>

        <xsl:for-each select="*">
		<TR>
			<TD class="bold"><xsl:value-of select="local-name()"/></TD>
			<TD><xsl:value-of select="."/></TD>
		</TR>
        </xsl:for-each>

</table>
</div>
</xsl:template>


</xsl:stylesheet>