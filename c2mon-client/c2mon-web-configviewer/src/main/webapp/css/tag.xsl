<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template match="TagInfo">
		<!-- print the html header with css links -->
		<html>
			<head>
				<title>Configuration viewer</title>
<!--				<link rel="stylesheet" type="text/css" href="tim.css" />-->
<!--				<link rel="stylesheet" type="text/css" href="webConfigViewer.css" />-->
				<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/tim.css" />
				<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/webConfigViewer.css" />
			</head>
			<body>
				<xsl:apply-templates />
			</body>
		</html>
	</xsl:template>

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
			<th colspan="2">Current Value</th>
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
				<td><xsl:value-of select="alarmIds"/></td>
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
			<th colspan="2">Current Value</th>
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
				<td><xsl:value-of select="tagId"/></td>
			</tr>
			<tr>
				<td class="bold"> Activation condition </td>
				<td style="background:red;"><xsl:value-of select="'missing in XML'"/></td>
			</tr>
		</table>
		</div>
	</xsl:template>

</xsl:stylesheet>