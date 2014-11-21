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
  
  <!-- include necessary child stylesheets -->
  <xsl:include href="equipment.xsl"/>
  
    <!-- leave the paragraphs untouched -->
  <xsl:template match="p">
    <xsl:copy-of select="." />
  </xsl:template>
  
  <!-- process the XML element ProcessConfiguration -->
  <!-- DAQ page -->
  <xsl:template match="ProcessConfiguration">
  
    <style>th {width:25%;}</style>

    <script type="text/javascript">
      $(document).ready(function(){
        function getProcessName() {
          var url = window.location.pathname;
          return url.substring(url.lastIndexOf("/") + 1, url.length);
        }

        $("#process_name").append(getProcessName());
        var urlForRawXmlView = document.getElementById('xml_butt');
        urlForRawXmlView.href = urlForRawXmlView + getProcessName();
      });
    </script>
  
    <div class="row">
      <div class="page-header">
        <h2>
          DAQ Process:
          <span id="process_name"></span>
  
          <span class="pull-right">
            <a href="../" class="btn btn-large btn-default">
              <span class="glyphicon glyphicon-home"></span>
              Home
            </a>
            <a href="{$base_url}{$process_xml_url}" id="xml_butt" class="btn btn-large btn-default">View as XML >>
            </a>
          </span>
        </h2>
      </div>
    </div>

    <div class="row">
      <table class="table table-striped table-bordered">
        <thead>
        <th colspan="2">Process Configuration</th>
        </thead>
        
        <tbody>
        <tr>
          <th>Process ID</th>
          <td>
            <xsl:value-of select="@process-id" />
          </td>
        </tr>
        <tr>
          <th>Type</th>
          <td>
            <xsl:value-of select="@type" />
          </td>
        </tr>
        <tr>
          <th>AliveTag ID</th>
          <td>
            <a href="{$base_url}{$datatag_url}{alive-tag-id}"><xsl:value-of select="alive-tag-id"/></a>
          </td>
        </tr>
        <tr>
          <th>Alive Interval</th>
          <td>
            <xsl:value-of select="alive-interval" />
          </td>
        </tr>
        <tr>
          <th>Max Message Size</th>
          <td>
            <xsl:value-of select="max-message-size" />
          </td>
        </tr>
        <tr>
          <th>Max Message Delay</th>
          <td>
            <xsl:value-of select="max-message-delay" />
          </td>
        </tr>
        </tbody>
      </table>
    </div>
    <xsl:apply-templates select="EquipmentUnits" />

  </xsl:template>
</xsl:stylesheet>