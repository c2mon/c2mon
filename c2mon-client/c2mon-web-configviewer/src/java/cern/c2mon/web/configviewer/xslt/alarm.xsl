<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!--  link variables  -->
  <xsl:variable name="base_url">../</xsl:variable>
  <xsl:variable name="alarm_url">alarmviewer/</xsl:variable>
  <xsl:variable name="command_url">commandviewer/</xsl:variable>
  <xsl:variable name="datatag_url">tagviewer/</xsl:variable>
  <xsl:variable name="process_xml_url">process/xml/</xsl:variable>
  <xsl:variable name="alarm_xml_url">alarmviewer/xml/</xsl:variable>
  <xsl:variable name="command_xml_url">commandviewer/xml/</xsl:variable>
  <xsl:variable name="report_xml_url">configloader/progress/finalReport/xml/</xsl:variable>
  
  <xsl:variable name="help_alarm_url">http://oraweb.cern.ch/pls/timw3/helpalarm.AlarmForm?p_alarmid=</xsl:variable>

  <!-- page : alarmviewer -->
  <xsl:template match="AlarmValue | alarmValue">

    <div class="row">
    <div class="page-header"> 
      <h2>
        Alarm: 
        <xsl:value-of select="faultFamily"/>:<xsl:value-of select="faultMemeber"/>:<xsl:value-of select="faultCode"/>
        
        <span class="pull-right" style="margin-bottom: 15px;">
          <a href="{$base_url}{$alarm_xml_url}{@id}/" 
            class="btn btn-default btn-large" target="_blank">View Alarm XML >>
          </a>  
          
          <a href="{$help_alarm_url}{@id}" 
            class="btn btn-danger btn-large" target="_blank">View Help Alarm >>
          </a>
        </span>
      </h2>
    </div>
    </div>

    <div class="row">
    <table class="table table-striped table-bordered">
      <thead>
        <th colspan="2">Alarm Value</th>
      </thead>
      <tbody>
        <tr>
          <th>Alarm ID</th>
          <td><xsl:value-of select="@id"/></td>  
        </tr>
        <tr>
          <th>Class</th>
          <td><xsl:value-of select="@class"/></td>  
        </tr>
        <tr>
          <th>DataTag</th>
          <td >
            <a href="{$base_url}{$datatag_url}{tagId}"><xsl:value-of select="tagId"/></a>
          </td>
        </tr>
        <tr>
          <th>State</th>
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
          <th>Fault Code</th>
          <td><xsl:value-of select="faultCode"/></td>  
        </tr>
        <tr>
          <th>Fault Family</th>
          <td><xsl:value-of select="faultFamily"/></td>  
        </tr>
        <tr>
          <th>Fault Member</th>
          <td><xsl:value-of select="faultMemeber"/></td>  
        </tr>
        <tr>
          <th>Info</th>
          <td><xsl:value-of select="info"/></td>  
        </tr>
        <tr>
          <th>Timestamp</th>
          <td><xsl:value-of select="timestamp"/></td>  
        </tr>
      </tbody>
    </table>
    </div>
  </xsl:template>
</xsl:stylesheet>