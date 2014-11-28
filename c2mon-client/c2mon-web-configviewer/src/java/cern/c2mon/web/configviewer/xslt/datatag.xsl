<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- link variables -->
  <xsl:variable name="history_url">historyviewer/</xsl:variable>
  <xsl:variable name="trend_viewer_url">trendviewer/</xsl:variable>
  <xsl:variable name="tag_value_xml_url">tagvalue/xml/</xsl:variable>
  <xsl:variable name="tag_config_xml_url">tagconfig/xml/</xsl:variable>
  <xsl:variable name="help_point_url">https://oraweb.cern.ch/pls/timw3/helpalarm.AlarmList?p_pointid1=</xsl:variable> 

  <!-- include necessary child stylesheets -->
  <xsl:include href="alarm.xsl"/>

  <!-- leave the paragraphs untouched -->
  <xsl:template match="p">
    <xsl:copy-of select="." />
  </xsl:template>

  <!-- page : tag -->
  <xsl:template match="ClientDataTag">
    
    <style>th {width:25%;}</style>
      
      <div class="row">
        <div class="page-header">
          <h2>DataTag: 
            <xsl:value-of select="tagName" /> (<xsl:value-of select="@id" />)
            
            <span>
              <div class="pull-right">
                <a href="{$base_url}" class="btn btn-default btn-large">
                  Home
                  <span class="glyphicon glyphicon-home"></span>
                </a>
                <a href="{$base_url}{$tag_value_xml_url}{@id}/" class="btn btn-default btn-large">View XML >></a>
                <a href="{$base_url}{$history_url}{@id}" class="btn btn-default btn-large">History >> 
                </a>
                <a href="{$base_url}{$trend_viewer_url}{@id}" class="btn btn-default btn-large">Trend >> 
                </a>
                <a href="{$help_point_url}{@id}" class="btn btn-default btn-large btn-danger">View Help Alarm >> 
                </a>
              </div>
            </span>
          </h2>
        </div>
      </div>

      <div class="row">
        <table class="table table-striped table-bordered">
        <thead>
          <th colspan="2">ClientDataTag</th>
          </thead>
          <tbody>
          <tr>
            <th>Tag ID</th>
            <td>
              <xsl:value-of select="@id" />
            </td>
          </tr>
          <tr>
            <th>Tag Name</th>
            <td>
              <xsl:value-of select="tagName" />
            </td>
          </tr>
          <tr>
            <th>Description</th>
            <td>
              <xsl:value-of select="description" />
            </td>
          </tr>
          <tr>
            <th>Tag Value</th>
            <td>
              <xsl:value-of select="tagValue" />
            </td>
          </tr>
          <tr>
            <th>Value Description</th>
            <td>
              <xsl:value-of select="valueDescription" />
            </td>
          </tr>
          <tr>
            <th>Source Timestamp</th>
            <td>
              <xsl:value-of select="sourceTimestamp" />
            </td>
          </tr>
          <tr>
            <th>Server Timestamp</th>
            <td>
              <xsl:value-of select="serverTimestamp" />
            </td>
          </tr>
          <tr>
            <th>Tag Quality</th>
            <td>
              <xsl:choose>
                <xsl:when test="tagQuality/isValid = 'true'">OK</xsl:when>
                <xsl:otherwise>INVALID</xsl:otherwise>
              </xsl:choose>
            </td>
          </tr>
          <tr>
            <th>Mode</th>
            <td>
              <xsl:value-of select="mode" />
            </td>
          </tr>
          <tr>
            <th>Simulated</th>
            <td>
              <xsl:value-of select="simulated" />
            </td>
          </tr>
          <tr>
            <th>Topic Name</th>
            <td>
              <xsl:value-of select="topicName" />
            </td>
          </tr>
          <tr>
            <th>Data Type</th>
            <td>
              <xsl:value-of select="../ClientDataTag/tagValue/@class" />
            </td>
          </tr>
          </tbody>
        </table>
      </div>
      
      <xsl:apply-templates select="alarms" />
      <xsl:apply-templates select="tagQuality" />
  </xsl:template>

  <xsl:template match="tagQuality">
    <xsl:apply-templates select="invalidQualityStates" />
  </xsl:template>

  <!-- process the XML element invalidQualityStates - entry -->
  <xsl:template match="invalidQualityStates">
    <xsl:if test="entry!=''">

    <div class="row">
      <table class="table table-striped table-bordered">
        <thead>
        <th colspan="4">Reason for tag invalidation</th>
        </thead>
        
        <tbody>
        <xsl:for-each select="entry">
          <tr>
            <th>Quality Status</th>
            <td>
              <xsl:value-of select="tagQualityStatus" />
            </td>
          <tr>
          </tr>
            <th>Description</th>
            <td>
              <xsl:value-of select="string" />
            </td>
          </tr>

          <xsl:for-each
            select="*[not(local-name() = 'tagQualityStatus' or local-name() = 'string')]">
            <tr>
              <th>
                <xsl:value-of select="local-name()" />
              </th>
              <td>
                <xsl:value-of select="." />
              </td>
            </tr>
          </xsl:for-each>

        </xsl:for-each>
        </tbody>
      </table>
      </div>
    </xsl:if>

  </xsl:template>


  <!-- process the XML element TagConfig - take missing information from the element ClientDataTag -->
  <!-- page : tag -->
  <xsl:template match="TagConfig">

      <div class="row">
        <div class="page-header">
          <h2>
            DataTag Configuration
            <span>
              <div class="pull-right">
                <a href="{$base_url}{$tag_config_xml_url}{@id}/" class="btn btn-large btn-default">View TagConfig XML >>
                </a>
              </div>
            </span>
          </h2>
        </div>
      </div>
    
      <div class="row">
        <table class="table table-striped table-bordered">
          <thead>
            <th colspan="2">Tag Configuration</th>
          </thead>
          <tbody>
          
          <tr>
            <th>Value Deadband</th>
            <td><xsl:value-of select="valueDeadband" /></td>
          </tr>
          <tr>
            <th>Value Deadband Label</th>
            <td><xsl:value-of select="valueDeadbandLabel" /></td>
          </tr>
          <tr>
            <th>Time Deadband</th>
            <td><xsl:value-of select="timeDeadband" /></td>
          </tr>
          <tr>
            <th>Priority</th>
            <xsl:choose>
              <xsl:when test=".= '2'">
                <td>LOW</td>
              </xsl:when>
              <xsl:otherwise>
                <td>HIGH</td>
              </xsl:otherwise>
            </xsl:choose>
          </tr>
          <tr>
            <th>Guaranteed Delivery</th>
            <td><xsl:value-of select="guaranteedDelivery" /></td>
          </tr>
          <tr>
            <th>Rule IDs</th>
            <td><a href="{$base_url}{$datatag_url}{long}"><xsl:value-of select="ruleIds" /></a></td>
          </tr>
          <tr>
            <th>Control Tag</th>
            <td><xsl:value-of select="controlTag" /></td>
          </tr>
          <tr>
            <th>Logged</th>
            <td><xsl:value-of select="logged" /></td>
          </tr>
          <tr>
            <th>Publications</th>
            <td><xsl:value-of select="publications" /></td>
          </tr>
          <tr>
            <th>Process Names</th>
            <td><xsl:value-of select="processNames" /></td>
          </tr>
          <tr>
            <th>Alarms</th>
            <td>
              <xsl:for-each select="alarmIds">
                <a href="{$base_url}{$alarm_url}{long}">
                  <xsl:value-of select="long" />
                </a>
              </xsl:for-each>
            </td>
          </tr>
          <tr>
            <th>Hardware Address</th>
            <td>
              <pre>
              <xsl:value-of select="hardwareAddress" />
              </pre>
            </td>
          </tr>

          </tbody>
        </table>
      </div>
  </xsl:template>

</xsl:stylesheet>