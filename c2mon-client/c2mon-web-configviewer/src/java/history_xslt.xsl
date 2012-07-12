<?xml version="1.0" encoding="UTF-8"?>

  <!--
// TIM. CERN. All rights reserved.
-->
  <!--
    This XSLT transformation is used for transforming HistoryTag XML into
    XHTML representation
  -->
<xsl:stylesheet version="1.0"
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
	
	<xsl:template match="p">
		<xsl:copy-of select="." />
	</xsl:template>

  <xsl:template match="/">
    <html>
      <head>
        <style type="text/css">
          tr.ok
          {
          color : #000000;
          background : #FFFFFF;
          }

          tr.nexist
          {
          color : #FFFFFF;
          background : #990099;
          }

          tr.invalid
          {
          color : #000000;
          background : #66FFFF;
          }
    </style>

        <link rel="stylesheet" type="text/css"
          href="/c2mon-web-configviewer/css/tim.css" />
        <link rel="stylesheet" type="text/css"
          href="/c2mon-web-configviewer/css/webConfigViewer.css" />

      </head>

      <body>
        <p class="tagName">
          Data Tag History
          (<xsl:value-of select="/*/@id" />)
        </p>
        <table class="inline">
          <tr>
            <th>Server Timestamp</th>
            <th>Value</th>
            <th>Quality description</th>
            <th>Value Description</th>
            <th>Source Timestamp</th>
            <th>Mode</th>
          </tr>
          
        <xsl:for-each select="history/HistoryTag">
            <tr>
              <td>
                <xsl:value-of select="serverTimestamp" />
              </td>
              <td>
                <xsl:value-of select="value" />
              </td>
              
              <td>
        				<xsl:for-each select="dataTagQuality/invalidQualityStates/entry">
        					<p>
                	<xsl:value-of
                  	select="tagQualityStatus" />
               			 -
               		 <xsl:value-of select="string" />
               		 </p>
								</xsl:for-each>
              </td>
              
              <td>
                <xsl:value-of select="description" />
              </td>
              <td>
                <xsl:value-of select="sourceTimestamp" />
              </td>
              <td>
                <xsl:value-of select="mode" />
              </td>
            </tr>
       </xsl:for-each>
        </table>
      </body>
    </html>

  </xsl:template>

</xsl:stylesheet>