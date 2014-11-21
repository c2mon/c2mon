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
  
  <!-- process the XML element ClientCommandTagImpl -->
  <xsl:template match="ClientCommandTag">

    <style>th {width:25%;}</style>
    
    <div class="row">
      <div class="page-header">
        <h2>
          CommandTag: <xsl:value-of select="@name"/> (<xsl:value-of select="@id"/>)
          
          <div class="pull-right"> 
            <a href="../" class="btn btn-large btn-default">    
            <span class="glyphicon glyphicon-home"></span> Home
            </a>
            <a href="{$base_url}{$command_xml_url}{@id}/" 
              class="btn btn-large btn-default" target="_blank">View Command XML >>
            </a>
          </div>
        </h2>
      </div>
      </div>
  
    <div class="row">
      <table class="table table-striped table-bordered">
        <thead>
          <th colspan="2">Command Tag</th>
        </thead>
        <tbody>
          <tr>
            <th>Command ID</th>
            <td>
              <xsl:value-of select="@id" />
            </td>
          </tr>
          <tr>
            <th>Name</th>
            <td>
              <xsl:value-of select="@name" />
            </td>
          </tr>
  
          <xsl:for-each select="*[not(local-name() = 'HardwareAddress')]">
            <tr>
              <th>
                <xsl:value-of select="local-name()" />
              </th>
              <td>
                <xsl:value-of select="." />
              </td>
            </tr>
          </xsl:for-each>
        </tbody>
      </table>
  
      <xsl:template match="HardwareAddress">
        <table class="table table-striped table-bordered">
          <thead>
            <th colspan="2">HardwareAddress</th>
          </thead>
          <tbody>
            <xsl:for-each select="HardwareAddress/*">
              <tr>
                <th>
                  <xsl:value-of select="local-name()" />
                </th>
                <td>
                  <xsl:value-of select="." />
                </td>
              </tr>
            </xsl:for-each>
          </tbody>
        </table>
      </xsl:template>
    </div>
  </xsl:template>
</xsl:stylesheet>