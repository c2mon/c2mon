<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:my="some.uri"
	exclude-result-prefixes="my">

	<my:map name="value">
		<entry key="description">Tag description</entry>
		<entry key="tagValue">Value</entry>
		<entry key="valueDescription">Value description</entry>
		<entry key="sourceTimestamp">Source timestamp</entry>
		<entry key="serverTimestamp">Server timestamp</entry>
		<entry key="qualityDescription">Quality description</entry>
		<entry key="mode">Mode</entry>
		<entry key="simulated">Simulated</entry>
		<entry key="topicName">JMS Topic name</entry>
	</my:map>

	<my:map name="config">
		<entry key="tagId">Tag id</entry>
		<entry key="tagName">Tag name</entry>
		<entry key="controlTag">Control tag</entry>
		<entry key="minValue">Min value</entry>
		<entry key="maxValue">Max value</entry>
		<entry key="alarmIds">Alarms</entry>
		<entry key="ruleIds">Rules</entry>
		<entry key="publications">Publications</entry>
		<entry key="priority">JMS Priority</entry>
		<entry key="guaranteedDelivery">Guaranteed delivery</entry>
		<entry key="timeDeadband">Time deadband (ms)</entry>
		<entry key="valueDeadband">Value deadband</entry>
		<entry key="valueDeadbandType">Value deadband type</entry>
		<entry key="hardwareAddress">Hardware Address</entry>
	</my:map>
	
	<my:map name="alarmValue">
		<entry key="active">State</entry>
		<entry key="timestamp">Timestamp of last change</entry>
		<entry key="info">Info</entry>
	</my:map>

	<my:map name="alarmConfig">
		<entry key="faultFamily">Fault family</entry>
		<entry key="faultMemeber">Fault member</entry>
		<entry key="faultCode">Fault code</entry>
		<entry key="tagId">Datatag</entry>
	</my:map>

	
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

	<xsl:template match="p">
		<xsl:copy-of select="." />
	</xsl:template>
	
	<xsl:template match="ClientDataTag">
	<p class="tagName"> 
					<xsl:value-of select="tagName"/>
					(<xsl:value-of select="@id"/>)
	</p>
	<div class="column">
		<table class="inline">
			<th colspan="2">Current Value</th>
				<xsl:for-each select="./*">
<!--			<xsl:for-each select="description|tagValue|valueDescription|sourceTimestamp|serverTimestamp|qualityDescription|mode|simulated|topicName">-->
				<xsl:call-template name="createRow">
					<xsl:with-param name="mapName" select="'value'" />
				</xsl:call-template>
			</xsl:for-each>
		</table>
	</div>
	</xsl:template>

	<xsl:template match="TagConfig">
	<div class="column">
		<table class="inline">
			<th colspan="2">Tag Configuration</th>

<!--			<xsl:for-each select="./*">-->
			<xsl:for-each select="priority|hardwareAddress|timeDeadband|valueDeadband|valueDeadbandType">
				<xsl:call-template name="createRow">
					<xsl:with-param name="mapName" select="'config'" />
				</xsl:call-template>
			</xsl:for-each>
		</table>
	</div>
	</xsl:template>

	<xsl:template match="AlarmValue">
	<p class="tagName"> 
					<xsl:value-of select="faultFamily"/>
					:<xsl:value-of select="faultMemeber"/>
					:<xsl:value-of select="faultCode"/>
	</p>
	<div class="column">
		<table class="inline">
			<th colspan="2">Current Value</th>
			<xsl:for-each select="active | timestamp | info">
				<xsl:call-template name="createRow">
					<xsl:with-param name="mapName" select="'alarmValue'" />
				</xsl:call-template>
			</xsl:for-each>
		</table>
	</div>
	<div class="column">
		<table class="inline">
			<th colspan="2">Alarm Configuration</th>
			<xsl:for-each select="faultFamily | faultMemeber | faultCode | tagId | active ">
				<xsl:call-template name="createRow">
					<xsl:with-param name="mapName" select="'alarmConfig'" />
				</xsl:call-template>
			</xsl:for-each>
		</table>
	</div>
	</xsl:template>

<!-- Processes current xml element. 
	 Creates a table row containing 2 cells: the value of the first cell is determined by the value in a "mapName" under the key "nodeName" (name of the currently processed xml element). 
	 All maps are defined at the top of this xsl. The value of the second cell is the content of the xml element with the "nodeName" - current xml element. -->
	<xsl:template name="createRow">
		<xsl:param name="nodeName" select="name(.)" />
		<xsl:param name="mapName" />
		<xsl:if test="document('')/*/my:map[@name=$mapName]/entry[@key=$nodeName] != ''">
		<tr>
			<td class="bold">
				<xsl:value-of select="document('')/*/my:map[@name=$mapName]/entry[@key=$nodeName]" />
			</td>
			<td>
				<xsl:choose> 
					<xsl:when test="$nodeName='hardwareAddress'">
						<pre><xsl:value-of select="." /></pre>
					</xsl:when> 
					<xsl:when test=".='false'">
						<xsl:text>TERMINATED</xsl:text>
					</xsl:when>
					<xsl:when test=".='true'">
						<xsl:text>ACTIVE</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="." />
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</tr>
		</xsl:if>
	</xsl:template>
	

</xsl:stylesheet>